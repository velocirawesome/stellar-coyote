package com.velocirawesome.stellarcoyote;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.tribuo.Dataset;
import org.tribuo.Example;
import org.tribuo.Model;
import org.tribuo.MutableDataset;
import org.tribuo.Trainer;
import org.tribuo.datasource.ListDataSource;
import org.tribuo.impl.ArrayExample;
import org.tribuo.math.optimisers.AdaGrad;
import org.tribuo.provenance.SimpleDataSourceProvenance;
import org.tribuo.regression.RegressionFactory;
import org.tribuo.regression.Regressor;
import org.tribuo.regression.evaluation.RegressionEvaluator;
import org.tribuo.regression.sgd.linear.LinearSGDTrainer;
import org.tribuo.regression.sgd.objectives.SquaredLoss;
import org.tribuo.util.Util;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component

public class TriobuoModelTrainer {
    
    @Autowired
    LedgerRepository ledgerRepo;
    
    // @Cacheable("defaultCache")
     public Mono<TrainingResult> fetchOrTrainModel(String account, LocalDateTime minDate, LocalDateTime maxDate) {
         
         return ledgerRepo.findByAccountAndTimestampBetween(account, minDate, maxDate)
                 .switchIfEmpty(Mono.error(new NoLedgerEntriesFoundException("No entries found for account: " + account + " between " + minDate + " and " + maxDate)))
                 .collectList()
                 
                 .doOnNext(_ -> log.info("Training model for account: {} with lookback {} days", account, Duration.between(minDate, maxDate).toDays()))
                 .map(this::trainModel);
                // .cache();
     }
     
     // functional method that can be tested independently of db
     /** largely taken from https://tribuo.org/learn/4.0/tutorials/regression-tribuo-v4.html */
     protected TrainingResult trainModel(List<LedgerEntry> entries) {

         // Prepare Tribuo regression dataset
         List<Example<Regressor>> examples = new ArrayList<>();
         for (LedgerEntry entry : entries) {
             log.debug("{}", entry);
             double timestampValue = entry.getTimestamp().toEpochSecond(java.time.ZoneOffset.UTC);
             Example<Regressor> example = new ArrayExample<>(
                     new Regressor("total", entry.getTotal()),
                     new String[]{"timestamp"},
                     new double[]{timestampValue}
                     );
             examples.add(example);
         }

         var regressionFactory = new RegressionFactory();
         SimpleDataSourceProvenance provenance = new SimpleDataSourceProvenance("Ledger Regression Dataset", OffsetDateTime.now(), regressionFactory);
         ListDataSource<Regressor> dataSource = new ListDataSource<>(examples, regressionFactory, provenance);

         Dataset<Regressor> dataset = new MutableDataset<>(dataSource);

         // Train a linear regression model
         var lrada = new LinearSGDTrainer(
                 new SquaredLoss(),
                 new AdaGrad(0.01),
                 10,
                 dataset.size()/4,
                 1,
                 1L 
                 );
        // Train the model
         var startTime = System.currentTimeMillis();
         Model<Regressor> model = lrada.train(dataset);
         var endTime = System.currentTimeMillis();
         log.info("Training {} took {}", "Linear Regression (AdaGrad)", Util.formatDuration(startTime,endTime));


         // Train the model using SGD
         var lradaModel = model;

         // Evaluate the model on the training data (this is a useful debugging tool)
         RegressionEvaluator eval = new RegressionEvaluator();
         var evaluation = eval.evaluate(lradaModel, dataset);
         // We create a dimension here to aid pulling out the appropriate statistics.
         // You can also produce the String directly by calling "evaluation.toString()"
         var dimension = new Regressor("total",Double.NaN);
         log.info("Evaluation (train):\n  RMSE {}\n  MAE {}\n  R^2 {}", evaluation.rmse(dimension), evaluation.mae(dimension), evaluation.r2(dimension));

         // bundle these so we can cache them
         return new TrainingResult(
                 lradaModel,
                 evaluation.rmse(dimension),
                 evaluation.mae(dimension),
                 evaluation.r2(dimension)
                 );
     }


    
}

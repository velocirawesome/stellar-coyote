package com.velocirawesome.stellarcoyote;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.tribuo.Example;
import org.tribuo.Model;
import org.tribuo.Prediction;
import org.tribuo.impl.ArrayExample;
import org.tribuo.regression.Regressor;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
public class TribuoService {

    @Autowired
    LedgerRepository ledgerRepo;
    @Autowired
    LedgerService ledgerService;
    @Autowired
    TriobuoModelTrainer modelTrainer;


 //   @EventListener(ApplicationReadyEvent.class)
 //public void predict() {
 //       LocalDateTime now = ledgerService.getNow().block();
 //       LocalDateTime oneMonthHence = now.plusMonths(1);
  //      // this user has 331 transactions
  //      predict("3590736522064285", Duration.ofDays(537), oneMonthHence, now);
  //  }

    public Mono<PredictionResult> predict(String account, Duration lookback, LocalDateTime pointInTime, LocalDateTime now) {
        LocalDateTime maxDate = now;
        LocalDateTime minDate = maxDate.minus(lookback);

        return modelTrainer.fetchOrTrainModel(account, minDate, maxDate)
                .flatMap(model -> predict(model.getModel(), pointInTime)
                        .map(prediction -> 
                        new PredictionResult(
                                prediction, 
                                null, // `actual` will be set later if pointInTime is in the past
                                pointInTime, 
                                lookback, 
                                account,
                                model.getR2()
                                )
                            )
                        );
    }

    protected Mono<Double> predict(Model<Regressor> model, LocalDateTime pointInTime) {

        double timestamp = pointInTime.toEpochSecond(java.time.ZoneOffset.UTC);

        Example<Regressor> futureExample = new ArrayExample<>(
                new Regressor("amount", Double.NaN),
                new String[]{"timestamp"},
                new double[]{timestamp}
                );

        Prediction<Regressor> prediction = model.predict(futureExample);

        log.info("Predicted amount at {}: {}", pointInTime, prediction.getOutput().getValues()[0]);

        double result = prediction.getOutput().getValues()[0];
        return Mono.just(result);

    }
  
}

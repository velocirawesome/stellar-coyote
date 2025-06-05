package com.velocirawesome.stellarcoyote;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.tribuo.Prediction;
import org.tribuo.regression.Regressor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;


public class TribuoModelTrainerTest {

    /**
     * Model was behaving really badly because the scale of the timestamps compared to the amounts was huge
     */
    @Test
    void testTrainModel_perfectGradient() {
        TribuoModelTrainer trainer = new TribuoModelTrainer();
        List<LedgerEntry> entries = new ArrayList<>();
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
        long epoch = startTime.toEpochSecond(ZoneOffset.UTC);

        for (int i = 0; i < 10; i++) {
            LedgerEntry entry = new LedgerEntry();
            entry.setAccount("test-account");
            // Timestamps increase by 1 day
            LocalDateTime timestamp = startTime.plusDays(i);
            entry.setTimestamp(timestamp);
            // Total increases linearly with the day number (i.e., perfect gradient)
            entry.setTotal((double) i * 10); // e.g., 0, 10, 20, ...
            entry.setId((long) i);
            entries.add(entry);
        }

        TrainingResult result = trainer.trainModel(entries);

        // For a perfect gradient, R^2 should be very close to 1.0
        assertThat(result.getR2()).isCloseTo(1.0, within(0.0001));

        // Optionally, check other metrics if they have expected values for a perfect fit
        // For example, RMSE and MAE should be close to 0.0
        assertThat(result.getRmse()).isCloseTo(0.0, within(0.0001));
        assertThat(result.getMae()).isCloseTo(0.0, within(0.0001));

        // You can also inspect the model's predictions if needed
        // For example, predict for one of the training points
        // Create an example with the first timestamp
        double secondTimestampValue = entries.get(1).getTimestamp().toEpochSecond(ZoneOffset.UTC) - epoch;
        org.tribuo.Example<Regressor> example = new org.tribuo.impl.ArrayExample<>(
                new Regressor("total", 10), // Output variable (actual value for comparison)
                new String[]{"timestamp"}, // Feature names
                new double[]{secondTimestampValue} // Feature values
        );
        
        // Predict using the trained model
        Prediction<Regressor> prediction = result.getModel().predict(example);
        double predictedValue = prediction.getOutput().getNames()[0].equals("total") ? prediction.getOutput().getValues()[0] : Double.NaN;

        assertThat(predictedValue).isCloseTo(10, within(0.001));
    }
}

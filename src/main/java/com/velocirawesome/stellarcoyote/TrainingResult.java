package com.velocirawesome.stellarcoyote;

import lombok.Data;
import org.tribuo.Model;
import org.tribuo.regression.Regressor;

@Data
public class TrainingResult {
    private final Model<Regressor> model;
    private final double rmse;
    private final double mae;
    private final double r2;
    private final long epoch;
}

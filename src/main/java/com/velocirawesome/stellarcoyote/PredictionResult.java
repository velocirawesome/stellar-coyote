package com.velocirawesome.stellarcoyote;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Data
@AllArgsConstructor
public class PredictionResult {

    Double predicted;
    @JsonInclude(value = NON_NULL)
    Double actual;
    LocalDateTime pointInTime;
    Duration lookback;
    String account;
    double r2;
    
    
}

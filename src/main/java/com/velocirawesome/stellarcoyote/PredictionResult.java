package com.velocirawesome.stellarcoyote;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"account", "r2", "predictedMoney", "lookbackDays", "pointInTimeDate", "actualMoney"})
public class PredictionResult {

    @JsonIgnore
    Double predicted;
    @JsonIgnore
    Double actual;
    @JsonIgnore
    LocalDateTime pointInTime;
    @JsonIgnore
    Duration lookback;
    String account;
    double r2;

    @JsonInclude(value = NON_NULL)
    public String getPredictedMoney() {
        return predicted == null ? null : String.format("%.2f", predicted);
    }

    @JsonInclude(value = NON_NULL)
    public String getActualMoney() {
        return actual == null ? null : String.format("%.2f", actual);
    }
    
    public long getLookbackDays() {
        return lookback.toDays();
    }

    public String getPointInTimeDate() {
        return pointInTime != null ? pointInTime.toLocalDate().toString() : null;
    }

    public String getPointInTime() {
        return pointInTime != null ? pointInTime.toLocalDate().toString() : null;
    }

    public void setPredictedMoney(String value) {
        this.predicted = value == null ? null : Double.valueOf(value);
    }

    public void setActualMoney(String value) {
        this.actual = value == null ? null : Double.valueOf(value);
    }

    public void setLookbackDays(long days) {
        this.lookback = Duration.ofDays(days);
    }

    public void setPointInTimeDate(String date) {
        this.pointInTime = date == null ? null : LocalDateTime.parse(date + "T00:00:00");
    }

    public void setPointInTime(String date) {
        this.pointInTime = date == null ? null : LocalDateTime.parse(date + "T00:00:00");
    }

}
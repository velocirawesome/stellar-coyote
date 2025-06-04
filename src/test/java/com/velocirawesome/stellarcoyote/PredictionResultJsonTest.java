package com.velocirawesome.stellarcoyote;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.junit.jupiter.api.Assertions.*;

/**
 * quick test thrown together when other tests were failing die to serialization issues
 * added @JsonPropertyOrder in PredictionResult as a quick way of producing consistent tostrings
 */
public class PredictionResultJsonTest {
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(com.fasterxml.jackson.databind.SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

    @Test
    void testSerialization() throws Exception {
        PredictionResult result = new PredictionResult(
                123.45,
                67.89,
                LocalDateTime.of(2025, 6, 4, 12, 0),
                Duration.ofDays(5),
                "test-account",
                0.98
        );
        String json = objectMapper.writeValueAsString(result);
        assertThat(json).isEqualTo("""
                {"account":"test-account","r2":0.98,"predictedMoney":"123.45","lookbackDays":5,"pointInTimeDate":"2025-06-04","actualMoney":"67.89"}""");
    }

    @Test
    void testDeserialization() throws Exception {
        String json = """
                {"account":"test-account","r2":0.98,"predictedMoney":"123.45","lookbackDays":5,"pointInTimeDate":"2025-06-04","actualMoney":"67.89"}""";
        PredictionResult result = objectMapper.readValue(json, PredictionResult.class);
        assertThat(result.getAccount()).isEqualTo("test-account");
        assertThat(result.getR2()).isCloseTo(0.98, offset(0.0001));
        assertThat(result.getPredicted()).isCloseTo(123.45, offset(0.0001));
        assertThat(result.getActual()).isCloseTo(67.89, offset(0.0001));
        assertThat(result.getLookback().toDays()).isEqualTo(5);
        assertThat(result.getPointInTime()).isEqualTo(LocalDateTime.of(2025, 6, 4, 0, 0).toLocalDate().toString());
    }
}
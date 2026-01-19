package dev.dote.qtrack.qualityrecord;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class QualityRecordResponse {
    public record List(
            Long id,
            Long dailyProductionId,
            Long processId,
            Integer okQuantity,
            Integer ngQuantity,
            Integer totalQuantity,
            BigDecimal ngRate,
            String expertEvaluation,
            Boolean evaluationRequired,
            String evaluationReason) {
    }

    public record Get(
            Long id,
            Long dailyProductionId,
            Long processId,
            Integer okQuantity,
            Integer ngQuantity,
            Integer totalQuantity,
            BigDecimal ngRate,
            String expertEvaluation,
            Boolean evaluationRequired,
            String evaluationReason,
            @JsonFormat(pattern = "yyyy-MM-dd") LocalDate evaluatedAt,
            Long evaluatedBy) {  // String → Long (User ID)
    }

    public record Create(
            Long id,
            Long dailyProductionId,
            Long processId,
            Integer okQuantity,
            Integer ngQuantity,
            Integer totalQuantity,
            BigDecimal ngRate,
            Boolean evaluationRequired,
            String evaluationReason) {
    }

    public record Update(
            Long id,
            Long dailyProductionId,
            Long processId,
            Integer okQuantity,
            Integer ngQuantity,
            Integer totalQuantity,
            BigDecimal ngRate,
            Boolean evaluationRequired,
            String evaluationReason) {
    }

    public record Delete(Long id) {
    }

    public record Evaluate(
            Long id,
            Long dailyProductionId,
            Long processId,
            String expertEvaluation,
            Long evaluatedBy,
            @JsonFormat(pattern = "yyyy-MM-dd") LocalDate evaluatedAt) {
    }

    public record StatisticsByProcess(
            Long processId,
            String processCode,
            String processName,
            Integer totalNgQuantity,
            Integer totalQuantity,
            BigDecimal ngRate) {
    }

    public record StatisticsByItem(
            Long itemId,
            String itemCode,
            String itemName,
            Integer totalNgQuantity,
            Integer totalQuantity,
            BigDecimal ngRate) {
    }
}

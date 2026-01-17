package dev.dote.qtrack.qualityrecord;

import java.math.BigDecimal;
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
            LocalDateTime evaluatedAt,
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
            LocalDateTime evaluatedAt) {
    }
}

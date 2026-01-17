package dev.dote.qtrack.qualityrecord;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class QualityRecordRequest {
    public record Create(
            @NotNull(message = "일별 생산 ID는 필수입니다") Long dailyProductionId,
            @NotNull(message = "공정 ID는 필수입니다") Long processId,
            @NotNull(message = "OK 수량은 필수입니다")
            @Min(value = 0, message = "OK 수량은 0 이상이어야 합니다") Integer okQuantity,
            @NotNull(message = "NG 수량은 필수입니다")
            @Min(value = 0, message = "NG 수량은 0 이상이어야 합니다") Integer ngQuantity) {
    }

    public record Update(
            @NotNull(message = "OK 수량은 필수입니다")
            @Min(value = 0, message = "OK 수량은 0 이상이어야 합니다") Integer okQuantity,
            @NotNull(message = "NG 수량은 필수입니다")
            @Min(value = 0, message = "NG 수량은 0 이상이어야 합니다") Integer ngQuantity) {
    }

    public record Evaluate(
            @NotNull(message = "전문가 평가 내용은 필수입니다") String expertEvaluation) {
    }
}

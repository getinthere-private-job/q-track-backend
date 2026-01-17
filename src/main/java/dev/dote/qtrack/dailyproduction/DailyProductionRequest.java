package dev.dote.qtrack.dailyproduction;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class DailyProductionRequest {
    public record Create(
            @NotNull(message = "부품 ID는 필수입니다") Long itemId,
            @NotNull(message = "생산 일자는 필수입니다") LocalDate productionDate,
            @NotNull(message = "총 생산 수량은 필수입니다")
            @Min(value = 0, message = "총 생산 수량은 0 이상이어야 합니다") Integer totalQuantity) {
    }

    public record Update(
            @NotNull(message = "총 생산 수량은 필수입니다")
            @Min(value = 0, message = "총 생산 수량은 0 이상이어야 합니다") Integer totalQuantity) {
    }
}

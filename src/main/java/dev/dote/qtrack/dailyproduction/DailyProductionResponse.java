package dev.dote.qtrack.dailyproduction;

import java.time.LocalDate;

public class DailyProductionResponse {
    public record List(Long id, Long itemId, LocalDate productionDate, Integer totalQuantity) {
    }

    public record Get(Long id, Long itemId, LocalDate productionDate, Integer totalQuantity) {
    }

    public record Create(Long id, Long itemId, LocalDate productionDate, Integer totalQuantity) {
    }

    public record Update(Long id, Long itemId, LocalDate productionDate, Integer totalQuantity) {
    }

    public record Delete(Long id) {
    }
}

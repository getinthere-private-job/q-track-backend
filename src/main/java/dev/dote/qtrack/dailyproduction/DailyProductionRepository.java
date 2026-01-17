package dev.dote.qtrack.dailyproduction;

import dev.dote.qtrack.item.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyProductionRepository extends JpaRepository<DailyProduction, Long> {
    boolean existsByItemAndProductionDate(Item item, LocalDate productionDate);
    Optional<DailyProduction> findByItemAndProductionDate(Item item, LocalDate productionDate);
}

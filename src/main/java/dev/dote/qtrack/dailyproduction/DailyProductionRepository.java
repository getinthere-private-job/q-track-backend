package dev.dote.qtrack.dailyproduction;

import dev.dote.qtrack.item.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyProductionRepository extends JpaRepository<DailyProduction, Long> {
    boolean existsByItemAndProductionDate(Item item, LocalDate productionDate);

    Optional<DailyProduction> findByItemAndProductionDate(Item item, LocalDate productionDate);

    @Query("SELECT dp FROM DailyProduction dp JOIN FETCH dp.item")
    List<DailyProduction> findAllWithItem();

    @Query("SELECT dp FROM DailyProduction dp JOIN FETCH dp.item WHERE dp.id = :id")
    Optional<DailyProduction> findByIdWithItem(@Param("id") Long id);

    @Query("SELECT dp FROM DailyProduction dp JOIN FETCH dp.item WHERE dp.item = :item AND dp.productionDate = :productionDate")
    Optional<DailyProduction> findByItemAndProductionDateWithItem(@Param("item") Item item,
            @Param("productionDate") LocalDate productionDate);
}

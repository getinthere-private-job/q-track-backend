package dev.dote.qtrack.dailyproduction;

import dev.dote.qtrack.item.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query(value = "SELECT dp FROM DailyProduction dp JOIN dp.item i " +
            "WHERE (:itemId IS NULL OR i.id = :itemId) " +
            "AND (:startDate IS NULL OR dp.productionDate >= :startDate) " +
            "AND (:endDate IS NULL OR dp.productionDate <= :endDate) " +
            "ORDER BY dp.productionDate DESC, i.code ASC",
            countQuery = "SELECT COUNT(dp) FROM DailyProduction dp JOIN dp.item i " +
            "WHERE (:itemId IS NULL OR i.id = :itemId) " +
            "AND (:startDate IS NULL OR dp.productionDate >= :startDate) " +
            "AND (:endDate IS NULL OR dp.productionDate <= :endDate)")
    Page<DailyProduction> findAllWithFilters(Pageable pageable,
            @Param("itemId") Long itemId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT dp FROM DailyProduction dp JOIN FETCH dp.item WHERE dp.id = :id")
    Optional<DailyProduction> findByIdWithItem(@Param("id") Long id);

    @Query("SELECT dp FROM DailyProduction dp JOIN FETCH dp.item WHERE dp.item = :item AND dp.productionDate = :productionDate")
    Optional<DailyProduction> findByItemAndProductionDateWithItem(@Param("item") Item item,
            @Param("productionDate") LocalDate productionDate);
}

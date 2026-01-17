package dev.dote.qtrack.qualityrecord;

import dev.dote.qtrack.dailyproduction.DailyProduction;
import dev.dote.qtrack.process.Process;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface QualityRecordRepository extends JpaRepository<QualityRecord, Long> {
    boolean existsByDailyProductionAndProcess(DailyProduction dailyProduction, Process process);

    Optional<QualityRecord> findByDailyProductionAndProcess(DailyProduction dailyProduction, Process process);

    List<QualityRecord> findByDailyProduction(DailyProduction dailyProduction);

    List<QualityRecord> findByEvaluationRequired(Boolean evaluationRequired);

    @Query("SELECT qr FROM QualityRecord qr " +
            "JOIN FETCH qr.dailyProduction " +
            "JOIN FETCH qr.process " +
            "LEFT JOIN FETCH qr.evaluatedBy")
    List<QualityRecord> findAllWithJoins();

    @Query("SELECT qr FROM QualityRecord qr " +
            "JOIN FETCH qr.dailyProduction " +
            "JOIN FETCH qr.process " +
            "LEFT JOIN FETCH qr.evaluatedBy " +
            "WHERE qr.evaluationRequired = :evaluationRequired")
    List<QualityRecord> findByEvaluationRequiredWithJoins(@Param("evaluationRequired") Boolean evaluationRequired);

    @Query("SELECT qr FROM QualityRecord qr " +
            "JOIN FETCH qr.dailyProduction " +
            "JOIN FETCH qr.process " +
            "LEFT JOIN FETCH qr.evaluatedBy " +
            "WHERE qr.id = :id")
    Optional<QualityRecord> findByIdWithJoins(@Param("id") Long id);

    @Query("SELECT qr FROM QualityRecord qr " +
            "JOIN FETCH qr.dailyProduction dp " +
            "JOIN FETCH qr.process " +
            "WHERE (:startDate IS NULL OR dp.productionDate >= :startDate) " +
            "AND (:endDate IS NULL OR dp.productionDate <= :endDate)")
    List<QualityRecord> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}

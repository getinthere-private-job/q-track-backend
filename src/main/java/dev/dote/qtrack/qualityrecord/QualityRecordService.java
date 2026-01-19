package dev.dote.qtrack.qualityrecord;

import dev.dote.qtrack._core.errors.ex.Exception400;
import dev.dote.qtrack.dailyproduction.DailyProduction;
import dev.dote.qtrack.dailyproduction.DailyProductionRepository;
import dev.dote.qtrack.process.Process;
import dev.dote.qtrack.process.ProcessRepository;
import dev.dote.qtrack.systemcode.SystemCodeService;
import dev.dote.qtrack.user.User;
import dev.dote.qtrack.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 품질 기록 비즈니스 로직 처리
 * - 품질 기록 조회, 생성, 수정, 삭제 기능
 * - NG 비율 자동 계산
 * - 평가 필요 여부 자동 판단 (NG 비율 임계값 초과, 전일 대비 급증)
 * - 평가 필요 목록 조회
 * - 품질 기록 평가 기능
 * - 공정별/부품별 NG 비율 통계
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class QualityRecordService {
    private final QualityRecordRepository qualityRecordRepository;
    private final DailyProductionRepository dailyProductionRepository;
    private final ProcessRepository processRepository;
    private final SystemCodeService systemCodeService;
    private final UserRepository userRepository;

    public List<QualityRecordResponse.List> findAll() {
        return qualityRecordRepository.findAllWithJoins().stream()
                .map(qr -> new QualityRecordResponse.List(
                        qr.getId(),
                        qr.getDailyProduction().getId(),
                        qr.getProcess().getId(),
                        qr.getOkQuantity(),
                        qr.getNgQuantity(),
                        qr.getTotalQuantity(),
                        qr.getNgRate(),
                        qr.getExpertEvaluation(),
                        qr.getEvaluationRequired(),
                        qr.getEvaluationReason()))
                .toList();
    }

    public Page<QualityRecordResponse.List> findAll(Pageable pageable, LocalDate productionDate, Integer year, Integer month) {
        // year와 month를 LocalDate 범위로 변환
        LocalDate startDate = null;
        LocalDate endDate = null;
        
        if (year != null && month != null) {
            // 특정 년월: 해당 월의 시작일 ~ 종료일
            startDate = LocalDate.of(year, month, 1);
            endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        } else if (year != null) {
            // 특정 년도: 해당 년도의 시작일 ~ 종료일
            startDate = LocalDate.of(year, 1, 1);
            endDate = LocalDate.of(year, 12, 31);
        }
        
        return qualityRecordRepository.findAllWithFilters(pageable, productionDate, startDate, endDate)
                .map(qr -> new QualityRecordResponse.List(
                        qr.getId(),
                        qr.getDailyProduction().getId(),
                        qr.getProcess().getId(),
                        qr.getOkQuantity(),
                        qr.getNgQuantity(),
                        qr.getTotalQuantity(),
                        qr.getNgRate(),
                        qr.getExpertEvaluation(),
                        qr.getEvaluationRequired(),
                        qr.getEvaluationReason()));
    }

    public List<QualityRecordResponse.List> getEvaluationRequiredList() {
        return qualityRecordRepository.findByEvaluationRequiredWithJoins(true).stream()
                .map(qr -> new QualityRecordResponse.List(
                        qr.getId(),
                        qr.getDailyProduction().getId(),
                        qr.getProcess().getId(),
                        qr.getOkQuantity(),
                        qr.getNgQuantity(),
                        qr.getTotalQuantity(),
                        qr.getNgRate(),
                        qr.getExpertEvaluation(),
                        qr.getEvaluationRequired(),
                        qr.getEvaluationReason()))
                .toList();
    }

    public QualityRecordResponse.Get findById(Long id) {
        QualityRecord qualityRecord = qualityRecordRepository.findByIdWithJoins(id)
                .orElseThrow(() -> new Exception400("품질 기록을 찾을 수 없습니다: " + id));
        return new QualityRecordResponse.Get(
                qualityRecord.getId(),
                qualityRecord.getDailyProduction().getId(),
                qualityRecord.getProcess().getId(),
                qualityRecord.getOkQuantity(),
                qualityRecord.getNgQuantity(),
                qualityRecord.getTotalQuantity(),
                qualityRecord.getNgRate(),
                qualityRecord.getExpertEvaluation(),
                qualityRecord.getEvaluationRequired(),
                qualityRecord.getEvaluationReason(),
                qualityRecord.getEvaluatedAt() != null ? qualityRecord.getEvaluatedAt().toLocalDate() : null,
                qualityRecord.getEvaluatedBy() != null ? qualityRecord.getEvaluatedBy().getId() : null);
    }

    @Transactional
    public QualityRecordResponse.Create create(
            Long dailyProductionId,
            Long processId,
            Integer okQuantity,
            Integer ngQuantity) {
        // totalQuantity 검증
        if (okQuantity == null || ngQuantity == null || okQuantity < 0 || ngQuantity < 0) {
            throw new Exception400("OK 수량과 NG 수량은 0 이상이어야 합니다");
        }

        DailyProduction dailyProduction = dailyProductionRepository.findById(dailyProductionId)
                .orElseThrow(() -> new Exception400("일별 생산 데이터를 찾을 수 없습니다: " + dailyProductionId));

        Process process = processRepository.findById(processId)
                .orElseThrow(() -> new Exception400("공정을 찾을 수 없습니다: " + processId));

        // 중복 검증
        if (qualityRecordRepository.existsByDailyProductionAndProcess(dailyProduction, process)) {
            throw new Exception400("이미 존재하는 품질 기록입니다: 일별생산ID=" + dailyProductionId + ", 공정ID=" + processId);
        }

        QualityRecord qualityRecord = new QualityRecord(dailyProduction, process, okQuantity, ngQuantity);
        calculateEvaluationRequired(qualityRecord);
        QualityRecord saved = qualityRecordRepository.save(qualityRecord);

        return new QualityRecordResponse.Create(
                saved.getId(),
                saved.getDailyProduction().getId(),
                saved.getProcess().getId(),
                saved.getOkQuantity(),
                saved.getNgQuantity(),
                saved.getTotalQuantity(),
                saved.getNgRate(),
                saved.getEvaluationRequired(),
                saved.getEvaluationReason());
    }

    @Transactional
    public QualityRecordResponse.Update update(
            Long id,
            Integer okQuantity,
            Integer ngQuantity) {
        // totalQuantity 검증
        if (okQuantity == null || ngQuantity == null || okQuantity < 0 || ngQuantity < 0) {
            throw new Exception400("OK 수량과 NG 수량은 0 이상이어야 합니다");
        }

        QualityRecord qualityRecord = qualityRecordRepository.findByIdWithJoins(id)
                .orElseThrow(() -> new Exception400("품질 기록을 찾을 수 없습니다: " + id));

        qualityRecord.update(okQuantity, ngQuantity);
        calculateEvaluationRequired(qualityRecord);
        QualityRecord updated = qualityRecordRepository.save(qualityRecord);

        return new QualityRecordResponse.Update(
                updated.getId(),
                updated.getDailyProduction().getId(),
                updated.getProcess().getId(),
                updated.getOkQuantity(),
                updated.getNgQuantity(),
                updated.getTotalQuantity(),
                updated.getNgRate(),
                updated.getEvaluationRequired(),
                updated.getEvaluationReason());
    }

    @Transactional
    public QualityRecordResponse.Delete delete(Long id) {
        QualityRecord qualityRecord = qualityRecordRepository.findById(id)
                .orElseThrow(() -> new Exception400("품질 기록을 찾을 수 없습니다: " + id));
        qualityRecordRepository.delete(qualityRecord);
        return new QualityRecordResponse.Delete(id);
    }

    @Transactional
    public QualityRecordResponse.Evaluate evaluate(Long id, Long userId, String expertEvaluation) {
        QualityRecord qualityRecord = qualityRecordRepository.findByIdWithJoins(id)
                .orElseThrow(() -> new Exception400("품질 기록을 찾을 수 없습니다: " + id));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception400("사용자를 찾을 수 없습니다: " + userId));

        qualityRecord.evaluate(expertEvaluation, user);
        QualityRecord updated = qualityRecordRepository.save(qualityRecord);

        return new QualityRecordResponse.Evaluate(
                updated.getId(),
                updated.getDailyProduction().getId(),
                updated.getProcess().getId(),
                updated.getExpertEvaluation(),
                updated.getEvaluatedBy().getId(),
                updated.getEvaluatedAt() != null ? updated.getEvaluatedAt().toLocalDate() : null);
    }

    private void calculateEvaluationRequired(QualityRecord qualityRecord) {
        BigDecimal ngRate = qualityRecord.getNgRate();
        if (ngRate == null) {
            qualityRecord.setEvaluationRequired(false, null);
            return;
        }

        boolean requiresEvaluation = false;
        String reason = null;

        try {
            // NG 비율 임계값 확인 (0.5%)
            String thresholdStr = systemCodeService.getCodeValue("INDUSTRY_AVERAGE", "NG_RATE_THRESHOLD");
            BigDecimal threshold = new BigDecimal(thresholdStr);

            if (ngRate.compareTo(threshold) > 0) {
                requiresEvaluation = true;
                reason = "NG 비율 임계값 초과";
            }

            // 전일 대비 NG 비율 급증 확인 (2배 이상)
            String increaseRateThresholdStr = systemCodeService.getCodeValue("EVALUATION", "INCREASE_RATE_THRESHOLD");
            BigDecimal increaseRateThreshold = new BigDecimal(increaseRateThresholdStr);

            Optional<QualityRecord> previousRecord = findPreviousDayRecord(qualityRecord);
            if (previousRecord.isPresent() && previousRecord.get().getNgRate() != null) {
                BigDecimal previousNgRate = previousRecord.get().getNgRate();
                if (previousNgRate.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal increaseRate = ngRate.divide(previousNgRate, 4, java.math.RoundingMode.HALF_UP);
                    if (increaseRate.compareTo(increaseRateThreshold) >= 0) {
                        requiresEvaluation = true;
                        reason = reason != null ? reason + ", 전일 대비 급증" : "전일 대비 급증";
                    }
                }
            }
        } catch (Exception e) {
            // 시스템 코드를 찾을 수 없는 경우 기본값 사용
            // 또는 로그만 남기고 계속 진행
        }

        qualityRecord.setEvaluationRequired(requiresEvaluation, reason);
    }

    private Optional<QualityRecord> findPreviousDayRecord(QualityRecord currentRecord) {
        LocalDate currentDate = currentRecord.getDailyProduction().getProductionDate();
        LocalDate previousDate = currentDate.minusDays(1);

        Optional<DailyProduction> previousDailyProduction = dailyProductionRepository
                .findByItemAndProductionDateWithItem(
                        currentRecord.getDailyProduction().getItem(),
                        previousDate);

        if (previousDailyProduction.isPresent()) {
            return qualityRecordRepository.findByDailyProductionAndProcess(
                    previousDailyProduction.get(),
                    currentRecord.getProcess());
        }

        return Optional.empty();
    }

    public List<QualityRecordResponse.StatisticsByProcess> getNgRateByProcess(LocalDate startDate, LocalDate endDate) {
        List<QualityRecord> records;
        if (startDate != null || endDate != null) {
            records = qualityRecordRepository.findByDateRange(startDate, endDate);
        } else {
            records = qualityRecordRepository.findAllWithJoins();
        }

        // 공정별로 그룹화하여 통계 계산
        return records.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        QualityRecord::getProcess,
                        java.util.stream.Collectors.collectingAndThen(
                                java.util.stream.Collectors.toList(),
                                list -> {
                                    int totalNg = list.stream().mapToInt(QualityRecord::getNgQuantity).sum();
                                    int totalQty = list.stream().mapToInt(QualityRecord::getTotalQuantity).sum();
                                    BigDecimal ngRate = totalQty > 0
                                            ? BigDecimal.valueOf(totalNg)
                                                    .divide(BigDecimal.valueOf(totalQty), 4, RoundingMode.HALF_UP)
                                                    .multiply(BigDecimal.valueOf(100))
                                                    .setScale(2, RoundingMode.HALF_UP)
                                            : BigDecimal.ZERO;
                                    return new QualityRecordResponse.StatisticsByProcess(
                                            list.get(0).getProcess().getId(),
                                            list.get(0).getProcess().getCode(),
                                            list.get(0).getProcess().getName(),
                                            totalNg,
                                            totalQty,
                                            ngRate);
                                })))
                .values()
                .stream()
                .sorted((a, b) -> a.processCode().compareTo(b.processCode()))
                .toList();
    }

    public List<QualityRecordResponse.StatisticsByItem> getNgRateByItem(LocalDate startDate, LocalDate endDate) {
        List<QualityRecord> records;
        if (startDate != null || endDate != null) {
            records = qualityRecordRepository.findByDateRange(startDate, endDate);
        } else {
            records = qualityRecordRepository.findAllWithJoins();
        }

        // 부품별로 그룹화하여 통계 계산
        return records.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        qr -> qr.getDailyProduction().getItem(),
                        java.util.stream.Collectors.collectingAndThen(
                                java.util.stream.Collectors.toList(),
                                list -> {
                                    int totalNg = list.stream().mapToInt(QualityRecord::getNgQuantity).sum();
                                    int totalQty = list.stream().mapToInt(QualityRecord::getTotalQuantity).sum();
                                    BigDecimal ngRate = totalQty > 0
                                            ? BigDecimal.valueOf(totalNg)
                                                    .divide(BigDecimal.valueOf(totalQty), 4, RoundingMode.HALF_UP)
                                                    .multiply(BigDecimal.valueOf(100))
                                                    .setScale(2, RoundingMode.HALF_UP)
                                            : BigDecimal.ZERO;
                                    return new QualityRecordResponse.StatisticsByItem(
                                            list.get(0).getDailyProduction().getItem().getId(),
                                            list.get(0).getDailyProduction().getItem().getCode(),
                                            list.get(0).getDailyProduction().getItem().getName(),
                                            totalNg,
                                            totalQty,
                                            ngRate);
                                })))
                .values()
                .stream()
                .sorted((a, b) -> a.itemCode().compareTo(b.itemCode()))
                .toList();
    }
}

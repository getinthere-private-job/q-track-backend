package dev.dote.qtrack.qualityrecord;

import dev.dote.qtrack._core.util.Resp;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 품질 기록 관리 API
 * - 품질 기록의 CRUD 기능 제공
 * - 평가 필요 목록 조회 기능 제공
 * - 품질 기록 평가 기능 제공
 * - 공정별/부품별 NG 비율 통계 기능 제공
 */
@RestController
@RequestMapping("/api/quality-records")
@RequiredArgsConstructor
public class QualityRecordController {
    private final QualityRecordService qualityRecordService;

    @GetMapping
    public ResponseEntity<Resp<Page<QualityRecordResponse.List>>> findAll(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(value = "itemId", required = false) Long itemId,
            @RequestParam(value = "productionDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate productionDate,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "month", required = false) Integer month) {
        Page<QualityRecordResponse.List> response = qualityRecordService.findAll(pageable, itemId, productionDate, startDate, endDate, year, month);
        return Resp.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resp<QualityRecordResponse.Get>> findById(@PathVariable Long id) {
        QualityRecordResponse.Get response = qualityRecordService.findById(id);
        return Resp.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<Resp<QualityRecordResponse.Create>> create(
            @Valid @RequestBody QualityRecordRequest.Create request) {
        QualityRecordResponse.Create response = qualityRecordService.create(
                request.dailyProductionId(),
                request.processId(),
                request.okQuantity(),
                request.ngQuantity());
        return Resp.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<Resp<QualityRecordResponse.Update>> update(
            @PathVariable Long id,
            @Valid @RequestBody QualityRecordRequest.Update request) {
        QualityRecordResponse.Update response = qualityRecordService.update(
                id,
                request.okQuantity(),
                request.ngQuantity());
        return Resp.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Resp<QualityRecordResponse.Delete>> delete(@PathVariable Long id) {
        QualityRecordResponse.Delete response = qualityRecordService.delete(id);
        return Resp.ok(response);
    }

    @PutMapping("/{id}/evaluate")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<Resp<QualityRecordResponse.Evaluate>> evaluate(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody QualityRecordRequest.Evaluate request) {
        QualityRecordResponse.Evaluate response = qualityRecordService.evaluate(
                id,
                userId,
                request.expertEvaluation());
        return Resp.ok(response);
    }

    @GetMapping("/evaluation-required")
    public ResponseEntity<Resp<List<QualityRecordResponse.List>>> getEvaluationRequiredList() {
        List<QualityRecordResponse.List> response = qualityRecordService.getEvaluationRequiredList();
        return Resp.ok(response);
    }

    @GetMapping("/statistics/by-process")
    public ResponseEntity<Resp<List<QualityRecordResponse.StatisticsByProcess>>> getNgRateByProcess(
            @RequestParam(value = "startDate", required = false) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) LocalDate endDate) {
        List<QualityRecordResponse.StatisticsByProcess> response = qualityRecordService.getNgRateByProcess(startDate,
                endDate);
        return Resp.ok(response);
    }

    @GetMapping("/statistics/by-item")
    public ResponseEntity<Resp<List<QualityRecordResponse.StatisticsByItem>>> getNgRateByItem(
            @RequestParam(value = "startDate", required = false) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) LocalDate endDate) {
        List<QualityRecordResponse.StatisticsByItem> response = qualityRecordService.getNgRateByItem(startDate,
                endDate);
        return Resp.ok(response);
    }
}

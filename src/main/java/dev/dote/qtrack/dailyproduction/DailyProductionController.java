package dev.dote.qtrack.dailyproduction;

import dev.dote.qtrack._core.util.Resp;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 일별 생산 데이터 관리 API
 * - 일별 생산 데이터의 CRUD 기능 제공
 * - 부품별 일일 생산 수량 관리
 */
@RestController
@RequestMapping("/api/daily-productions")
@RequiredArgsConstructor
public class DailyProductionController {
    private final DailyProductionService dailyProductionService;

    @GetMapping
    public ResponseEntity<Resp<List<DailyProductionResponse.List>>> findAll() {
        List<DailyProductionResponse.List> response = dailyProductionService.findAll();
        return Resp.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resp<DailyProductionResponse.Get>> findById(@PathVariable Long id) {
        DailyProductionResponse.Get response = dailyProductionService.findById(id);
        return Resp.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<Resp<DailyProductionResponse.Create>> create(
            @Valid @RequestBody DailyProductionRequest.Create request
    ) {
        DailyProductionResponse.Create response = dailyProductionService.create(
                request.itemId(),
                request.productionDate(),
                request.totalQuantity()
        );
        return Resp.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<Resp<DailyProductionResponse.Update>> update(
            @PathVariable Long id,
            @Valid @RequestBody DailyProductionRequest.Update request
    ) {
        DailyProductionResponse.Update response = dailyProductionService.update(
                id,
                request.totalQuantity()
        );
        return Resp.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Resp<DailyProductionResponse.Delete>> delete(@PathVariable Long id) {
        DailyProductionResponse.Delete response = dailyProductionService.delete(id);
        return Resp.ok(response);
    }
}

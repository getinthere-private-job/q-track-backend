package dev.dote.qtrack.dailyproduction;

import dev.dote.qtrack._core.util.Resp;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/daily-productions")
public class DailyProductionController {
    private final DailyProductionService dailyProductionService;

    public DailyProductionController(DailyProductionService dailyProductionService) {
        this.dailyProductionService = dailyProductionService;
    }

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

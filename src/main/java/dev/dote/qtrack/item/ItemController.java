package dev.dote.qtrack.item;

import dev.dote.qtrack._core.util.Resp;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 부품 관리 API
 * - 부품의 CRUD 기능 제공
 * - 부품 정보 관리 및 권한 기반 접근 제어
 */
@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @GetMapping
    public ResponseEntity<Resp<List<ItemResponse.List>>> findAll() {
        List<ItemResponse.List> response = itemService.findAll();
        return Resp.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resp<ItemResponse.Get>> findById(@PathVariable Long id) {
        ItemResponse.Get response = itemService.findById(id);
        return Resp.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Resp<ItemResponse.Create>> create(@Valid @RequestBody ItemRequest.Create request) {
        ItemResponse.Create response = itemService.create(
                request.code(),
                request.name(),
                request.description(),
                request.category()
        );
        return Resp.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Resp<ItemResponse.Update>> update(
            @PathVariable Long id,
            @Valid @RequestBody ItemRequest.Update request
    ) {
        ItemResponse.Update response = itemService.update(
                id,
                request.name(),
                request.description(),
                request.category()
        );
        return Resp.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Resp<ItemResponse.Delete>> delete(@PathVariable Long id) {
        ItemResponse.Delete response = itemService.delete(id);
        return Resp.ok(response);
    }
}

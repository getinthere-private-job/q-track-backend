package dev.dote.qtrack.process;

import dev.dote.qtrack._core.util.Resp;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 공정 관리 API
 * - 공정 정보 조회 기능 제공
 * - 공정 목록 및 상세 정보 관리
 */
@RestController
@RequestMapping("/api/processes")
@RequiredArgsConstructor
public class ProcessController {
    private final ProcessService processService;

    @GetMapping
    public ResponseEntity<Resp<List<ProcessResponse.List>>> findAll() {
        List<ProcessResponse.List> response = processService.findAll();
        return Resp.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resp<ProcessResponse.Get>> findById(@PathVariable Long id) {
        ProcessResponse.Get response = processService.findById(id);
        return Resp.ok(response);
    }
}

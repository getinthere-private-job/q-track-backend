package dev.dote.qtrack.process;

import dev.dote.qtrack._core.util.Resp;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/processes")
public class ProcessController {
    private final ProcessService processService;

    public ProcessController(ProcessService processService) {
        this.processService = processService;
    }

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

package dev.dote.qtrack.systemcode;

import dev.dote.qtrack._core.util.Resp;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system-codes")
public class SystemCodeController {
    private final SystemCodeService systemCodeService;

    public SystemCodeController(SystemCodeService systemCodeService) {
        this.systemCodeService = systemCodeService;
    }

    @GetMapping
    public ResponseEntity<Resp<List<SystemCodeResponse.List>>> findAll(
            @RequestParam(required = false) String codeGroup
    ) {
        List<SystemCodeResponse.List> response;
        if (codeGroup != null && !codeGroup.isEmpty()) {
            response = systemCodeService.findByCodeGroup(codeGroup);
        } else {
            response = systemCodeService.findAll();
        }

        return Resp.ok(response);
    }
}

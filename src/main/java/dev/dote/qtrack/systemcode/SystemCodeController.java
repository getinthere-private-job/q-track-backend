package dev.dote.qtrack.systemcode;

import dev.dote.qtrack._core.util.Resp;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 시스템 코드 관리 API
 * - 시스템 코드 조회 기능 제공
 * - 코드 그룹별 조회 지원
 * - 시스템 설정값 관리
 */
@RestController
@RequestMapping("/api/system-codes")
@RequiredArgsConstructor
public class SystemCodeController {
    private final SystemCodeService systemCodeService;

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

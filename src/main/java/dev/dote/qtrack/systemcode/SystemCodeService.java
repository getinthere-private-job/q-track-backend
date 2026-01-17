package dev.dote.qtrack.systemcode;

import dev.dote.qtrack._core.errors.ex.Exception400;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 시스템 코드 비즈니스 로직 처리
 * - 시스템 코드 조회 기능
 * - 코드 그룹별 조회
 * - 시스템 설정값 조회 (getCodeValue)
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SystemCodeService {
    private final SystemCodeRepository systemCodeRepository;

    public List<SystemCodeResponse.List> findAll() {
        return systemCodeRepository.findAll().stream()
                .map(sc -> new SystemCodeResponse.List(
                        sc.getId(),
                        sc.getCodeGroup(),
                        sc.getCodeKey(),
                        sc.getCodeValue(),
                        sc.getDescription(),
                        sc.getIsActive()))
                .toList();
    }

    public List<SystemCodeResponse.List> findByCodeGroup(String codeGroup) {
        return systemCodeRepository.findByCodeGroup(codeGroup).stream()
                .map(sc -> new SystemCodeResponse.List(
                        sc.getId(),
                        sc.getCodeGroup(),
                        sc.getCodeKey(),
                        sc.getCodeValue(),
                        sc.getDescription(),
                        sc.getIsActive()))
                .toList();
    }

    public String getCodeValue(String codeGroup, String codeKey) {
        return systemCodeRepository.findByCodeGroupAndCodeKey(codeGroup, codeKey)
                .map(SystemCode::getCodeValue)
                .orElseThrow(() -> new Exception400("시스템 코드를 찾을 수 없습니다: " + codeGroup + "." + codeKey));
    }
}

package dev.dote.qtrack.process;

import dev.dote.qtrack._core.errors.ex.Exception400;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 공정 비즈니스 로직 처리
 * - 공정 목록 조회 및 상세 조회 기능
 * - 공정 정보 관리
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProcessService {
    private final ProcessRepository processRepository;

    public List<ProcessResponse.List> findAll() {
        return processRepository.findAll().stream()
                .map(p -> new ProcessResponse.List(
                        p.getId(),
                        p.getCode(),
                        p.getName(),
                        p.getDescription(),
                        p.getSequence()))
                .toList();
    }

    public ProcessResponse.Get findById(Long id) {
        Process process = processRepository.findById(id)
                .orElseThrow(() -> new Exception400("공정을 찾을 수 없습니다: " + id));
        return new ProcessResponse.Get(
                process.getId(),
                process.getCode(),
                process.getName(),
                process.getDescription(),
                process.getSequence());
    }
}

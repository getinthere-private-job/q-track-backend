package dev.dote.qtrack.process;

import dev.dote.qtrack._core.errors.ex.Exception400;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProcessService {
    private final ProcessRepository processRepository;

    public ProcessService(ProcessRepository processRepository) {
        this.processRepository = processRepository;
    }

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

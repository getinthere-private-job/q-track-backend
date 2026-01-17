package dev.dote.qtrack.process;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProcessRepository extends JpaRepository<Process, Long> {
    Optional<Process> findByCode(String code);
}

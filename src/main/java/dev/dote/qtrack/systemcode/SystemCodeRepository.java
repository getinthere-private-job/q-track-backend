package dev.dote.qtrack.systemcode;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SystemCodeRepository extends JpaRepository<SystemCode, Long> {
    Optional<SystemCode> findByCodeGroupAndCodeKey(String codeGroup, String codeKey);
    List<SystemCode> findByCodeGroup(String codeGroup);
}

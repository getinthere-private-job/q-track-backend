package dev.dote.qtrack.systemcode;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "system_code_tb", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "code_group", "code_key" })
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class SystemCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code_group", nullable = false, length = 50)
    private String codeGroup;

    @Column(name = "code_key", nullable = false, length = 50)
    private String codeKey;

    @Column(name = "code_value", nullable = false, length = 200)
    private String codeValue;

    @Column(length = 500)
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public SystemCode(String codeGroup, String codeKey, String codeValue, String description, Boolean isActive) {
        this.codeGroup = codeGroup;
        this.codeKey = codeKey;
        this.codeValue = codeValue;
        this.description = description;
        this.isActive = isActive;
    }
}

package dev.dote.qtrack.qualityrecord;

import dev.dote.qtrack.dailyproduction.DailyProduction;
import dev.dote.qtrack.process.Process;
import dev.dote.qtrack.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Table(name = "quality_record_tb", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "daily_production_id", "process_id" })
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class QualityRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_production_id", nullable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
    private DailyProduction dailyProduction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "process_id", nullable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
    private Process process;

    @Column(name = "ok_quantity", nullable = false)
    private Integer okQuantity;

    @Column(name = "ng_quantity", nullable = false)
    private Integer ngQuantity;

    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity;

    @Column(name = "ng_rate", precision = 5, scale = 2)
    private BigDecimal ngRate;

    @Column(name = "expert_evaluation", columnDefinition = "TEXT")
    private String expertEvaluation;

    @Column(name = "evaluation_required", nullable = false)
    private Boolean evaluationRequired;

    @Column(name = "evaluation_reason", length = 200)
    private String evaluationReason;

    @Column(name = "evaluated_at")
    private LocalDateTime evaluatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluated_by", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
    private User evaluatedBy;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public QualityRecord(DailyProduction dailyProduction, Process process, Integer okQuantity, Integer ngQuantity) {
        this.dailyProduction = dailyProduction;
        this.process = process;
        this.okQuantity = okQuantity;
        this.ngQuantity = ngQuantity;
        this.totalQuantity = okQuantity + ngQuantity;
        this.evaluationRequired = false;
        calculateNgRate();
    }

    public void update(Integer okQuantity, Integer ngQuantity) {
        this.okQuantity = okQuantity;
        this.ngQuantity = ngQuantity;
        this.totalQuantity = okQuantity + ngQuantity;
        calculateNgRate();
    }

    public void setEvaluationRequired(Boolean evaluationRequired, String evaluationReason) {
        this.evaluationRequired = evaluationRequired;
        this.evaluationReason = evaluationReason;
    }

    public void evaluate(String expertEvaluation, User evaluatedBy) {
        this.expertEvaluation = expertEvaluation;
        this.evaluatedBy = evaluatedBy;
        this.evaluatedAt = LocalDateTime.now();
    }

    @PrePersist
    @PreUpdate
    private void calculateNgRate() {
        if (totalQuantity != null && totalQuantity > 0) {
            BigDecimal ngQty = BigDecimal.valueOf(ngQuantity);
            BigDecimal totalQty = BigDecimal.valueOf(totalQuantity);
            this.ngRate = ngQty.divide(totalQty, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        } else {
            this.ngRate = BigDecimal.ZERO;
        }
    }
}

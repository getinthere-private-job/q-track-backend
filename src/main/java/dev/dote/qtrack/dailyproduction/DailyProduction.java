package dev.dote.qtrack.dailyproduction;

import dev.dote.qtrack.item.Item;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_production_tb", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"item_id", "production_date"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class DailyProduction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
    private Item item;

    @Column(name = "production_date", nullable = false)
    private LocalDate productionDate;

    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public DailyProduction(Item item, LocalDate productionDate, Integer totalQuantity) {
        this.item = item;
        this.productionDate = productionDate;
        this.totalQuantity = totalQuantity;
    }

    public void update(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }
}

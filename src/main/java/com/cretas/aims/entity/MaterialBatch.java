package com.cretas.aims.entity;

import com.cretas.aims.entity.enums.MaterialBatchStatus;
import lombok.*;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
/**
 * 原材料批次实体类
 *
 * @author Cretas Team
 * @version 1.0.0
 * @since 2025-01-09
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(exclude = {"factory", "materialType", "supplier", "createdBy", "consumptions", "adjustments", "planBatchUsages"})
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "material_batches",
       indexes = {
           @Index(name = "idx_batch_factory", columnList = "factory_id"),
           @Index(name = "idx_batch_status", columnList = "status"),
           @Index(name = "idx_batch_expire", columnList = "expire_date"),
           @Index(name = "idx_batch_material", columnList = "material_type_id")
       }
)
public class MaterialBatch extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;
    @Column(name = "factory_id", nullable = false)
    private String factoryId;
    @Column(name = "batch_number", nullable = false, unique = true, length = 50)
    private String batchNumber;
    @Column(name = "material_type_id", nullable = false)
    private Integer materialTypeId;
    @Column(name = "supplier_id")
    private Integer supplierId;
    @Column(name = "receipt_date", nullable = false)
    private LocalDate receiptDate;
    @Column(name = "purchase_date")
    private LocalDate purchaseDate;  // 数据库遗留字段，与receiptDate同步
    @Column(name = "expire_date")
    private LocalDate expireDate;
    @Column(name = "receipt_quantity", nullable = false, precision = 10, scale = 2)
    private BigDecimal receiptQuantity;
    @Column(name = "initial_quantity", precision = 10, scale = 2)
    private BigDecimal initialQuantityField;  // 数据库遗留字段，与receiptQuantity同步
    @Column(name = "quantity_unit", nullable = false, length = 20)
    private String quantityUnit;
    @Column(name = "weight_per_unit", precision = 10, scale = 3)
    private BigDecimal weightPerUnit;
    @Column(name = "total_weight", nullable = false, precision = 10, scale = 3)
    private BigDecimal totalWeight;
    @Column(name = "current_quantity", nullable = false, precision = 10, scale = 2)
    private BigDecimal currentQuantity;
    @Column(name = "total_quantity", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalQuantity;
    @Column(name = "remaining_quantity", nullable = false, precision = 10, scale = 2)
    private BigDecimal remainingQuantity;
    @Column(name = "used_quantity", nullable = false, precision = 10, scale = 2)
    private BigDecimal usedQuantity = BigDecimal.ZERO;
    @Column(name = "reserved_quantity", nullable = false, precision = 10, scale = 2)
    private BigDecimal reservedQuantity = BigDecimal.ZERO;
    @Column(name = "total_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalValue;
    @Column(name = "unit_price", precision = 10, scale = 2)
    private BigDecimal unitPrice;
    @Column(name = "total_price", precision = 10, scale = 2)
    private BigDecimal totalPrice;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MaterialBatchStatus status = MaterialBatchStatus.AVAILABLE;
    @Column(name = "storage_location", length = 100)
    private String storageLocation;
    @Column(name = "quality_certificate", length = 100)
    private String qualityCertificate;
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    @Column(name = "created_by", nullable = false)
    private Integer createdBy;
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;
    // 关联关系
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factory_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Factory factory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_type_id", referencedColumnName = "id", insertable = false, updatable = false)
    private RawMaterialType materialType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", referencedColumnName = "id", insertable = false, updatable = false)
    private User createdByUser;

    @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MaterialConsumption> consumptions = new ArrayList<>();

    @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MaterialBatchAdjustment> adjustments = new ArrayList<>();

    @OneToMany(mappedBy = "materialBatch", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductionPlanBatchUsage> planBatchUsages = new ArrayList<>();

    /**
     * Convenience method to get initial quantity (alias for receiptQuantity)
     */
    public BigDecimal getInitialQuantity() {
        return this.receiptQuantity;
    }

    /**
     * Convenience method to set initial quantity (alias for receiptQuantity)
     * 同时设置数据库遗留字段initial_quantity
     */
    public void setInitialQuantity(BigDecimal initialQuantity) {
        this.receiptQuantity = initialQuantity;
        this.initialQuantityField = initialQuantity;  // 同步到遗留字段
    }

    /**
     * Convenience method to get received quantity (alias for receiptQuantity)
     */
    public BigDecimal getReceivedQuantity() {
        return this.receiptQuantity;
    }

    /**
     * Convenience method to set received quantity (alias for receiptQuantity)
     * 同时设置数据库遗留字段initial_quantity
     */
    public void setReceivedQuantity(BigDecimal receivedQuantity) {
        this.receiptQuantity = receivedQuantity;
        this.initialQuantityField = receivedQuantity;  // 同步到遗留字段
    }
}

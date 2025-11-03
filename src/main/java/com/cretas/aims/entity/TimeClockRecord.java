package com.cretas.aims.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
/**
 * 考勤打卡记录实体
 *
 * @author Cretas Team
 * @version 1.0.0
 * @since 2025-01-09
 */
@Data
@Entity
@Table(name = "time_clock_records",
       indexes = {
           @Index(name = "idx_clock_factory_user", columnList = "factory_id, user_id"),
           @Index(name = "idx_clock_date", columnList = "clock_date"),
           @Index(name = "idx_clock_status", columnList = "status")
       }
)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeClockRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 工厂ID
     */
    @Column(name = "factory_id", nullable = false, length = 50)
    private String factoryId;
     /**
      * 用户ID
      */
    @Column(name = "user_id", nullable = false)
    private Integer userId;
     /**
      * 用户名
      */
    @Column(name = "username", length = 50)
    private String username;
     /**
      * 打卡日期
      */
    @Column(name = "clock_date", nullable = false)
    private LocalDate clockDate;
     /**
      * 上班打卡时间
      */
    @Column(name = "clock_in_time")
    private LocalDateTime clockInTime;
     /**
      * 下班打卡时间
      */
    @Column(name = "clock_out_time")
    private LocalDateTime clockOutTime;
     /**
      * 休息开始时间
      */
    @Column(name = "break_start_time")
    private LocalDateTime breakStartTime;
     /**
      * 休息结束时间
      */
    @Column(name = "break_end_time")
    private LocalDateTime breakEndTime;
     /**
      * 工作时长（分钟）
      */
    @Column(name = "work_duration_minutes")
    private Integer workDurationMinutes;
     /**
      * 休息时长（分钟）
      */
    @Column(name = "break_duration_minutes")
    private Integer breakDurationMinutes;
     /**
      * 加班时长（分钟）
      */
    @Column(name = "overtime_minutes")
    private Integer overtimeMinutes;
     /**
      * 状态: WORKING, ON_BREAK, OFF_WORK
      */
    @Column(name = "status", length = 20)
    private String status;
     /**
      * 考勤状态: NORMAL, LATE, EARLY_LEAVE, ABSENT
      */
    @Column(name = "attendance_status", length = 20)
    private String attendanceStatus;
     /**
      * 工作类型ID
      */
    @Column(name = "work_type_id")
    private Integer workTypeId;
     /**
      * 工作类型名称
      */
    @Column(name = "work_type_name", length = 50)
    private String workTypeName;
     /**
      * 打卡位置
      */
    @Column(name = "clock_location", length = 200)
    private String clockLocation;
     /**
      * 打卡设备
      */
    @Column(name = "clock_device", length = 100)
    private String clockDevice;
     /**
      * 备注
      */
    @Column(name = "notes", length = 500)
    private String notes;
     /**
      * 是否为手动修改
      */
    @Column(name = "is_manual_edit")
    @Builder.Default
    private Boolean isManualEdit = false;
     /**
      * 修改人ID
      */
    @Column(name = "edited_by")
    private Integer editedBy;
     /**
      * 修改原因
      */
    @Column(name = "edit_reason", length = 500)
    private String editReason;
     /**
      * 创建时间
      */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
     /**
      * 更新时间
      */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (clockDate == null) {
            clockDate = LocalDate.now();
        }
    }
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * 计算工作时长
     */
    public void calculateWorkDuration() {
        if (clockInTime != null && clockOutTime != null) {
            long totalMinutes = java.time.Duration.between(clockInTime, clockOutTime).toMinutes();
            // 减去休息时间
            if (breakStartTime != null && breakEndTime != null) {
                long breakMinutes = java.time.Duration.between(breakStartTime, breakEndTime).toMinutes();
                this.breakDurationMinutes = (int) breakMinutes;
                totalMinutes -= breakMinutes;
            }
            this.workDurationMinutes = (int) totalMinutes;
            // 计算加班时间（假设标准工作时间是8小时）
            int standardWorkMinutes = 8 * 60;
            if (this.workDurationMinutes > standardWorkMinutes) {
                this.overtimeMinutes = this.workDurationMinutes - standardWorkMinutes;
            } else {
                this.overtimeMinutes = 0;
            }
        }
    }
}

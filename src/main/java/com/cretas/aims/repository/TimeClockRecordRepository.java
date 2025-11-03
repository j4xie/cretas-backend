package com.cretas.aims.repository;

import com.cretas.aims.entity.TimeClockRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 考勤打卡记录数据访问接口
 *
 * @author Cretas Team
 * @version 1.0.0
 * @since 2025-01-09
 */
@Repository
public interface TimeClockRecordRepository extends JpaRepository<TimeClockRecord, Long> {
    
    /**
     * 根据工厂ID和用户ID查找今日打卡记录
     */
    @Query("SELECT t FROM TimeClockRecord t WHERE t.factoryId = :factoryId AND t.userId = :userId AND t.clockDate = :date")
    Optional<TimeClockRecord> findByFactoryIdAndUserIdAndClockDate(
            @Param("factoryId") String factoryId,
            @Param("userId") Integer userId,
            @Param("date") LocalDate date
    );
    
    /**
     * 根据工厂ID和用户ID查找打卡记录列表
     */
    List<TimeClockRecord> findByFactoryIdAndUserIdOrderByClockDateDesc(String factoryId, Integer userId);
    
    /**
     * 根据工厂ID和用户ID以及日期范围查找打卡记录
     */
    @Query("SELECT t FROM TimeClockRecord t WHERE t.factoryId = :factoryId AND t.userId = :userId " +
           "AND t.clockDate BETWEEN :startDate AND :endDate ORDER BY t.clockDate DESC, t.clockInTime DESC")
    List<TimeClockRecord> findByFactoryIdAndUserIdAndClockDateBetween(
            @Param("factoryId") String factoryId,
            @Param("userId") Integer userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    
    /**
     * 根据工厂ID和用户ID以及日期范围查找打卡记录（分页）
     */
    @Query("SELECT t FROM TimeClockRecord t WHERE t.factoryId = :factoryId AND t.userId = :userId " +
           "AND t.clockDate BETWEEN :startDate AND :endDate ORDER BY t.clockDate DESC, t.clockInTime DESC")
    Page<TimeClockRecord> findByFactoryIdAndUserIdAndClockDateBetween(
            @Param("factoryId") String factoryId,
            @Param("userId") Integer userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );
    
    /**
     * 根据工厂ID和日期查找打卡记录
     */
    @Query("SELECT t FROM TimeClockRecord t WHERE t.factoryId = :factoryId AND t.clockDate = :date")
    List<TimeClockRecord> findByFactoryIdAndClockDate(
            @Param("factoryId") String factoryId,
            @Param("date") LocalDate date
    );
    
    /**
     * 根据工厂ID和部门查找打卡记录
     */
    @Query("SELECT t FROM TimeClockRecord t JOIN User u ON t.userId = u.id " +
           "WHERE t.factoryId = :factoryId AND u.department = :department AND t.clockDate = :date")
    List<TimeClockRecord> findByFactoryIdAndDepartmentAndClockDate(
            @Param("factoryId") String factoryId,
            @Param("department") String department,
            @Param("date") LocalDate date
    );
    
    /**
     * 查找用户最后一次打卡记录
     */
    @Query("SELECT t FROM TimeClockRecord t WHERE t.factoryId = :factoryId AND t.userId = :userId " +
           "ORDER BY t.clockDate DESC, t.clockInTime DESC")
    List<TimeClockRecord> findLatestByFactoryIdAndUserId(
            @Param("factoryId") String factoryId,
            @Param("userId") Integer userId,
            Pageable pageable
    );
}


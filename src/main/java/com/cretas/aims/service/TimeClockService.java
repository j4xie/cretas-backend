package com.cretas.aims.service;

import com.cretas.aims.dto.common.PageRequest;
import com.cretas.aims.dto.common.PageResponse;
import com.cretas.aims.entity.TimeClockRecord;
import java.time.LocalDate;
import java.util.Map;
/**
 * 考勤打卡服务接口
 *
 * @author Cretas Team
 * @version 1.0.0
 * @since 2025-01-09
 */
public interface TimeClockService {
    /**
     * 上班打卡
     */
    TimeClockRecord clockIn(String factoryId, Integer userId, String location, String device);
     /**
     * 下班打卡
      */
    TimeClockRecord clockOut(String factoryId, Integer userId);
     /**
     * 开始休息
      */
    TimeClockRecord breakStart(String factoryId, Integer userId);
     /**
     * 结束休息
      */
    TimeClockRecord breakEnd(String factoryId, Integer userId);
     /**
     * 获取打卡状态
      */
    Map<String, Object> getClockStatus(String factoryId, Integer userId);
     /**
     * 获取打卡历史
      */
    PageResponse<TimeClockRecord> getClockHistory(String factoryId, Integer userId,
                                                  LocalDate startDate, LocalDate endDate,
                                                  PageRequest pageRequest);
     /**
     * 获取今日打卡记录
      */
    TimeClockRecord getTodayRecord(String factoryId, Integer userId);
     /**
     * 手动修改打卡记录
      */
    TimeClockRecord editClockRecord(String factoryId, Long recordId, TimeClockRecord record,
                                    Integer editedBy, String reason);
     /**
     * 获取考勤统计
      */
    Map<String, Object> getAttendanceStatistics(String factoryId, Integer userId,
                                                 LocalDate startDate, LocalDate endDate);
     /**
     * 获取部门考勤统计
      */
    Map<String, Object> getDepartmentAttendance(String factoryId, String department, LocalDate date);
     /**
     * 批量导出考勤记录
      */
    byte[] exportAttendanceRecords(String factoryId, LocalDate startDate, LocalDate endDate);
}

package com.cretas.aims.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
/**
 * 移动端数据传输对象
 *
 * @author Cretas Team
 * @version 1.0.0
 * @since 2025-01-09
 */
public class MobileDTO {
    /**
     * 移动端登录请求
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        @NotBlank(message = "用户名不能为空")
        private String username;
        @NotBlank(message = "密码不能为空")
        private String password;
        private String factoryId; // 可选，如不提供则系统自动推断
        private DeviceInfo deviceInfo;
    }

    /**
     * 设备信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeviceInfo {
        @NotBlank(message = "设备ID不能为空")
        private String deviceId;
        private String deviceType; // iOS, Android, HarmonyOS
        private String osVersion;
        private String appVersion;
        private String manufacturer;
        private String model;
        private String networkType;
        private String carrier;
        private LocationInfo location;
        private Map<String, Object> extra;
    }

    /**
     * 位置信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationInfo {
        private Double latitude;
        private Double longitude;
        private String address;
        private String city;
        private String province;
        private String country;
    }

    /**
     * 登录响应
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginResponse {
        private Integer userId;
        private String username;
        private String factoryId;
        private String factoryName;
        private String role;
        private List<String> permissions;
        private String token;
        private String refreshToken;
        private Long expiresIn;
        private UserProfile profile;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime lastLoginTime;

        /**
         * accessToken 别名（兼容前端）
         * 前端使用 accessToken 字段，后端使用 token 字段
         */
        @com.fasterxml.jackson.annotation.JsonProperty("accessToken")
        public String getAccessToken() {
            return token;
        }
    }

    /**
     * 用户简要信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserProfile {
        private String name;
        private String avatar;
        private String department;
        private String position;
        private String phoneNumber;
        private String email;
    }

    /**
     * 文件上传请求
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UploadRequest {
        @NotNull(message = "文件不能为空")
        private List<FileData> files;
        private String category;
        private Map<String, Object> metadata;
    }

    /**
     * 文件数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileData {
        private String filename;
        private String contentType;
        private String base64Data;
        private Long size;
    }

    /**
     * 文件上传响应
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UploadResponse {
        private List<UploadedFile> files;
        private Integer successCount;
        private Integer failedCount;
    }

    /**
     * 上传的文件信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UploadedFile {
        private String id;
        private String url;
        private String thumbnailUrl;
        private String originalName;
        private String contentType;
        private Long size;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime uploadTime;
    }

    /**
     * 激活请求
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivationRequest {
        @NotBlank(message = "激活码不能为空")
        private String activationCode;
        @NotNull(message = "设备信息不能为空")
        private DeviceInfo deviceInfo;
    }

    /**
     * 激活响应
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivationResponse {
        private Boolean success;
        private String message;
        private String factoryId;
        private String factoryName;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime activatedAt;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime validUntil;
        private List<String> features;
        private Map<String, Object> configuration;
    }

    /**
     * 数据同步请求
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SyncRequest {
        private String lastSyncTime;
        private List<String> dataTypes;
        private Map<String, Object> localChanges;
    }

    /**
     * 数据同步响应
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SyncResponse {
        private Map<String, List<Object>> serverData;
        private Map<String, Integer> conflictCount;
        private String nextSyncToken;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime syncTime;
    }

    /**
     * 移动端仪表盘数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardData {
        // 今日统计
        private TodayStats todayStats;
        // 待办事项
        private List<TodoItem> todoItems;
        // 最近活动
        private List<ActivityLog> recentActivities;
        // 预警信息
        private List<Alert> alerts;
        // 快捷操作
        private List<QuickAction> quickActions;
    }

    /**
     * 今日统计
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TodayStats {
        private Integer productionCount;
        private Integer qualityCheckCount;
        private Integer materialReceived;
        private Integer ordersCompleted;
        private Double productionEfficiency;
        private Integer activeWorkers;
    }

    /**
     * 待办事项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TodoItem {
        private String id;
        private String title;
        private String description;
        private String priority; // HIGH, MEDIUM, LOW
        private String status;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime dueTime;
    }

    /**
     * 活动日志
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityLog {
        private String id;
        private String type;
        private String title;
        private String description;
        private String operator;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime time;
    }

    /**
     * 预警信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Alert {
        private String id;
        private String type; // WARNING, ERROR, INFO
        private String title;
        private String message;
        private String severity; // HIGH, MEDIUM, LOW
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime time;
    }

    /**
     * 快捷操作
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuickAction {
        private String id;
        private String label;
        private String icon;
        private String title;
        private String action;
        private String color;
        private Integer orderIndex;
    }

    /**
     * 推送通知注册
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PushRegistration {
        @NotBlank(message = "设备令牌不能为空")
        private String deviceToken;
        @NotBlank(message = "平台不能为空")
        private String platform; // iOS, Android, HarmonyOS
        private List<String> topics;
        private Map<String, Object> preferences;
    }

    /**
     * 版本检查响应
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VersionCheckResponse {
        private String currentVersion;
        private String latestVersion;
        private Boolean updateRequired;
        private Boolean updateAvailable;
        private String downloadUrl;
        private String releaseNotes;
        private Long fileSize;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime releaseDate;
    }

    /**
     * 离线数据包
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OfflineDataPackage {
        private String packageId;
        private String version;
        private Map<String, Object> baseData;
        private Map<String, Object> configData;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime generatedAt;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime expiresAt;
    }

    // ==================== 注册相关 ====================
    /**
     * 移动端注册第一阶段请求（验证手机号）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterPhaseOneRequest {
        @NotBlank(message = "手机号不能为空")
        private String phoneNumber;
        private String factoryId; // 可选，如不提供则通过手机号从白名单推断
    }

    /**
     * 移动端注册第一阶段响应
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterPhaseOneResponse {
        private Boolean success;
        private String tempToken;
        private Long expiresAt;
        private String phoneNumber;
        private String factoryId;
        private Boolean isNewUser;
        private String message;
    }

    /**
     * 移动端注册第二阶段请求（创建账户）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterPhaseTwoRequest {
        @NotBlank(message = "临时令牌不能为空")
        private String tempToken;
        @NotBlank(message = "用户名不能为空")
        private String username;
        @NotBlank(message = "密码不能为空")
        private String password;
        @NotBlank(message = "真实姓名不能为空")
        private String realName;
        private String factoryId; // 可选，从第一阶段响应中获取或自动推断
        private String position;
        private String email;
        private DeviceInfo deviceInfo;
    }

    /**
     * 移动端注册第二阶段响应
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterPhaseTwoResponse {
        private Boolean success;
        private Integer userId;
        private String username;
        private String role;
        private String token;
        private UserProfile profile;
        private String message;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime registeredAt;
    }

    /**
     * AI成本分析请求
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AICostAnalysisRequest {
        @NotBlank(message = "批次ID不能为空")
        private String batchId;

        /**
         * 用户问题（follow-up时必填）
         */
        private String question;

        /**
         * Python Session ID（多轮对话时传递）
         */
        private String session_id;

        /**
         * 报告类型：default(默认批次分析), followup(追问), historical(历史综合报告)
         */
        private String reportType;

        /**
         * 历史报告的时间范围（仅historical类型时使用）
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime startDate;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime endDate;
    }

    /**
     * AI成本分析响应
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AICostAnalysisResponse {
        /**
         * 是否成功
         */
        private Boolean success;

        /**
         * 报告ID（用于查询历史报告）
         */
        private Long reportId;

        /**
         * AI分析结果（Markdown格式）
         */
        private String analysis;

        /**
         * Python Session ID（用于后续follow-up）
         */
        @JsonProperty("session_id")  // JSON输出时使用下划线格式
        private String sessionId;

        /**
         * 对话消息计数
         */
        private Integer messageCount;

        /**
         * 配额信息
         */
        private AIQuotaInfo quota;

        /**
         * 是否命中缓存
         */
        private Boolean cacheHit;

        /**
         * 处理时间（毫秒）
         */
        private Long processingTimeMs;

        /**
         * 配额消耗量
         */
        private Integer quotaConsumed;

        /**
         * 错误信息（失败时返回）
         */
        private String errorMessage;

        /**
         * 报告生成时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime generatedAt;

        /**
         * 报告有效期至
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime expiresAt;
    }

    /**
     * AI配额信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AIQuotaInfo {
        /**
         * 配额总数
         */
        private Integer total;

        /**
         * 已使用配额
         */
        private Integer used;

        /**
         * 剩余配额
         */
        private Integer remaining;

        /**
         * 使用率（百分比）
         */
        private Double usageRate;

        /**
         * 配额重置时间（下周一）
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime resetDate;

        /**
         * 是否已超额
         */
        private Boolean exceeded;
    }

    /**
     * AI报告列表请求
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AIReportListRequest {
        /**
         * 报告类型：batch, weekly, monthly, historical
         */
        private String reportType;

        /**
         * 时间范围开始
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime startDate;

        /**
         * 时间范围结束
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime endDate;
    }

    /**
     * AI报告列表响应
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AIReportListResponse {
        private List<AIReportSummary> reports;
        private Integer total;
    }

    /**
     * AI报告摘要
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AIReportSummary {
        private Long id;
        private String batchId;
        private String reportType;
        private String summaryText; // 摘要（前200字）

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime periodStart;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime periodEnd;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime expiresAt;

        private Boolean isAutoGenerated;
    }
}

package com.cretas.aims.controller;

import com.cretas.aims.dto.MobileDTO;
import com.cretas.aims.dto.common.ApiResponse;
import com.cretas.aims.dto.user.UserDTO;
import com.cretas.aims.service.MobileService;
import com.cretas.aims.utils.SecurityUtils;
import com.cretas.aims.utils.TokenUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 移动端接口控制器
 *
 * @author Cretas Team
 * @version 1.0.0
 * @since 2025-01-09
 */
@Slf4j
@RestController
@RequestMapping("/api/mobile")
@Tag(name = "移动端接口", description = "移动端专用接口")
@RequiredArgsConstructor
public class MobileController {

    private final MobileService mobileService;

    // ==================== 认证相关接口 ====================

    @PostMapping("/auth/unified-login")
    @Operation(summary = "统一登录接口")
    public ApiResponse<MobileDTO.LoginResponse> unifiedLogin(
            @RequestBody @Valid MobileDTO.LoginRequest request) {
        log.info("移动端统一登录: username={}", request.getUsername());
        MobileDTO.LoginResponse response = mobileService.unifiedLogin(request);
        return ApiResponse.success(response);
    }

    @PostMapping("/auth/refresh")
    @Operation(summary = "刷新访问令牌")
    public ApiResponse<MobileDTO.LoginResponse> refreshToken(
            @RequestParam @Parameter(description = "刷新令牌") String refreshToken) {
        log.debug("刷新令牌");
        MobileDTO.LoginResponse response = mobileService.refreshToken(refreshToken);
        return ApiResponse.success(response);
    }

    @PostMapping("/auth/logout")
    @Operation(summary = "用户登出")
    public ApiResponse<Void> logout(
            @RequestParam(required = false) @Parameter(description = "设备ID") String deviceId) {
        Integer userId = SecurityUtils.getCurrentUserId();
        log.info("用户登出: userId={}, deviceId={}", userId, deviceId);
        mobileService.logout(userId, deviceId);
        return ApiResponse.success();
    }

    // ==================== 设备激活接口 ====================

    @PostMapping("/activation/activate")
    @Operation(summary = "设备激活")
    public ApiResponse<MobileDTO.ActivationResponse> activateDevice(
            @RequestBody @Valid MobileDTO.ActivationRequest request) {
        log.info("设备激活: code={}", request.getActivationCode());
        MobileDTO.ActivationResponse response = mobileService.activateDevice(request);
        return ApiResponse.success(response);
    }

    // ==================== 注册接口 ====================

    @PostMapping("/auth/register-phase-one")
    @Operation(summary = "移动端注册-第一阶段（验证手机号）")
    public ApiResponse<MobileDTO.RegisterPhaseOneResponse> registerPhaseOne(
            @RequestBody @Valid MobileDTO.RegisterPhaseOneRequest request) {
        log.info("移动端注册第一阶段: phone={}", request.getPhoneNumber());
        MobileDTO.RegisterPhaseOneResponse response = mobileService.registerPhaseOne(request);
        return ApiResponse.success(response);
    }

    @PostMapping("/auth/register-phase-two")
    @Operation(summary = "移动端注册-第二阶段（创建账户）")
    public ApiResponse<MobileDTO.RegisterPhaseTwoResponse> registerPhaseTwo(
            @RequestBody @Valid MobileDTO.RegisterPhaseTwoRequest request) {
        log.info("移动端注册第二阶段: factoryId={}, username={}", request.getFactoryId(), request.getUsername());
        MobileDTO.RegisterPhaseTwoResponse response = mobileService.registerPhaseTwo(request);
        return ApiResponse.success(response);
    }

    // ==================== 文件上传接口 ====================

    @PostMapping("/upload")
    @Operation(summary = "移动端文件上传")
    public ApiResponse<MobileDTO.UploadResponse> uploadFiles(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(required = false) @Parameter(description = "文件分类") String category,
            @RequestParam(required = false) @Parameter(description = "元数据") String metadata) {
        log.info("文件上传: count={}, category={}", files.size(), category);
        MobileDTO.UploadResponse response = mobileService.uploadFiles(files, category, metadata);
        return ApiResponse.success(response);
    }

    // ==================== 仪表盘数据接口 ====================

    @GetMapping("/dashboard/{factoryId}")
    @Operation(summary = "获取移动端仪表盘数据")
    public ApiResponse<MobileDTO.DashboardData> getMobileDashboard(
            @PathVariable @Parameter(description = "工厂ID") String factoryId) {
        Integer userId = SecurityUtils.getCurrentUserId();
        log.debug("获取移动端仪表盘数据: factoryId={}, userId={}", factoryId, userId);
        MobileDTO.DashboardData data = mobileService.getDashboardData(factoryId, userId);
        return ApiResponse.success(data);
    }

    // ==================== 数据同步接口 ====================

    @PostMapping("/sync/{factoryId}")
    @Operation(summary = "数据同步")
    public ApiResponse<MobileDTO.SyncResponse> syncData(
            @PathVariable @Parameter(description = "工厂ID") String factoryId,
            @RequestBody MobileDTO.SyncRequest request) {
        log.info("数据同步: factoryId={}", factoryId);
        MobileDTO.SyncResponse response = mobileService.syncData(factoryId, request);
        return ApiResponse.success(response);
    }

    @GetMapping("/offline/{factoryId}")
    @Operation(summary = "获取离线数据包")
    public ApiResponse<MobileDTO.OfflineDataPackage> getOfflineDataPackage(
            @PathVariable @Parameter(description = "工厂ID") String factoryId) {
        Integer userId = SecurityUtils.getCurrentUserId();
        log.info("获取离线数据包: factoryId={}, userId={}", factoryId, userId);
        MobileDTO.OfflineDataPackage data = mobileService.getOfflineDataPackage(factoryId, userId);
        return ApiResponse.success(data);
    }

    // ==================== 推送通知接口 ====================

    @PostMapping("/push/register")
    @Operation(summary = "注册推送通知")
    public ApiResponse<Void> registerPushNotification(
            @RequestBody @Valid MobileDTO.PushRegistration registration) {
        Integer userId = SecurityUtils.getCurrentUserId();
        log.info("注册推送: userId={}, platform={}", userId, registration.getPlatform());
        mobileService.registerPushNotification(userId, registration);
        return ApiResponse.success();
    }

    @DeleteMapping("/push/unregister")
    @Operation(summary = "取消推送通知注册")
    public ApiResponse<Void> unregisterPushNotification(
            @RequestParam @Parameter(description = "设备令牌") String deviceToken) {
        Integer userId = SecurityUtils.getCurrentUserId();
        log.info("取消推送: userId={}, token={}", userId, deviceToken);
        mobileService.unregisterPushNotification(userId, deviceToken);
        return ApiResponse.success();
    }

    // ==================== 设备管理接口 ====================

    @GetMapping("/devices")
    @Operation(summary = "获取用户设备列表")
    public ApiResponse<List<MobileDTO.DeviceInfo>> getUserDevices() {
        Integer userId = SecurityUtils.getCurrentUserId();
        log.debug("获取设备列表: userId={}", userId);
        List<MobileDTO.DeviceInfo> devices = mobileService.getUserDevices(userId);
        return ApiResponse.success(devices);
    }

    @DeleteMapping("/devices/{deviceId}")
    @Operation(summary = "移除设备")
    public ApiResponse<Void> removeDevice(
            @PathVariable @Parameter(description = "设备ID") String deviceId) {
        Integer userId = SecurityUtils.getCurrentUserId();
        log.info("移除设备: userId={}, deviceId={}", userId, deviceId);
        mobileService.removeDevice(userId, deviceId);
        return ApiResponse.success();
    }

    // ==================== 版本管理接口 ====================

    @GetMapping("/version/check")
    @Operation(summary = "检查应用版本")
    public ApiResponse<MobileDTO.VersionCheckResponse> checkVersion(
            @RequestParam @Parameter(description = "当前版本") String currentVersion,
            @RequestParam @Parameter(description = "平台") String platform) {
        log.debug("检查版本: current={}, platform={}", currentVersion, platform);
        MobileDTO.VersionCheckResponse response = mobileService.checkVersion(currentVersion, platform);
        return ApiResponse.success(response);
    }

    // ==================== 配置接口 ====================

    @GetMapping("/config/{factoryId}")
    @Operation(summary = "获取移动端配置")
    public ApiResponse<Object> getMobileConfig(
            @PathVariable @Parameter(description = "工厂ID") String factoryId,
            @RequestParam @Parameter(description = "平台") String platform) {
        log.debug("获取配置: factoryId={}, platform={}", factoryId, platform);
        Object config = mobileService.getMobileConfig(factoryId, platform);
        return ApiResponse.success(config);
    }

    // ==================== 监控接口 ====================

    @PostMapping("/report/crash")
    @Operation(summary = "上报崩溃日志")
    public ApiResponse<Void> reportCrash(
            @RequestBody Map<String, Object> crashData) {
        log.error("收到崩溃报告: {}", crashData);

        // 解析设备信息和崩溃日志
        MobileDTO.DeviceInfo deviceInfo = null;
        String crashLog = null;

        if (crashData.containsKey("deviceInfo")) {
            // TODO: 解析设备信息
        }
        if (crashData.containsKey("crashLog")) {
            crashLog = crashData.get("crashLog").toString();
        }

        mobileService.reportCrash(deviceInfo, crashLog);
        return ApiResponse.success();
    }

    @PostMapping("/report/performance")
    @Operation(summary = "上报性能数据")
    public ApiResponse<Void> reportPerformance(
            @RequestBody Map<String, Object> performanceData) {
        log.debug("收到性能数据: {}", performanceData);

        // 解析设备信息和性能数据
        MobileDTO.DeviceInfo deviceInfo = null;
        Object perfData = performanceData.get("data");

        if (performanceData.containsKey("deviceInfo")) {
            // TODO: 解析设备信息
        }

        mobileService.reportPerformance(deviceInfo, perfData);
        return ApiResponse.success();
    }

    // ==================== 认证工具接口 ====================

    /**
     * 验证Token
     */
    @GetMapping("/auth/validate")
    @Operation(summary = "验证令牌")
    public ApiResponse<Boolean> validateToken(
            @Parameter(description = "访问令牌", required = true)
            @RequestHeader("Authorization") String authorization) {
        String token = TokenUtils.extractToken(authorization);
        boolean isValid = mobileService.validateToken(token);
        return ApiResponse.success(isValid);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/auth/me")
    @Operation(summary = "获取当前用户信息")
    public ApiResponse<UserDTO> getCurrentUser(
            @Parameter(description = "访问令牌", required = true)
            @RequestHeader("Authorization") String authorization) {
        String token = TokenUtils.extractToken(authorization);
        UserDTO user = mobileService.getUserFromToken(token);
        return ApiResponse.success(user);
    }

    /**
     * 修改密码
     */
    @PostMapping("/auth/change-password")
    @Operation(summary = "修改密码")
    public ApiResponse<Void> changePassword(
            @Parameter(description = "访问令牌", required = true)
            @RequestHeader("Authorization") String authorization,
            @Parameter(description = "原密码", required = true)
            @RequestParam String oldPassword,
            @Parameter(description = "新密码", required = true)
            @RequestParam String newPassword) {
        String token = TokenUtils.extractToken(authorization);
        UserDTO user = mobileService.getUserFromToken(token);
        log.info("修改密码: userId={}", user.getId());
        mobileService.changePassword(user.getId(), oldPassword, newPassword);
        return ApiResponse.success("密码修改成功", null);
    }

    /**
     * 重置密码（管理员功能）
     */
    @PostMapping("/auth/reset-password")
    @Operation(summary = "重置密码（管理员）")
    public ApiResponse<Void> resetPassword(
            @Parameter(description = "工厂ID", required = true)
            @RequestParam String factoryId,
            @Parameter(description = "用户名", required = true)
            @RequestParam String username,
            @Parameter(description = "新密码", required = true)
            @RequestParam String newPassword) {
        log.info("重置密码: factoryId={}, username={}", factoryId, username);
        mobileService.resetPassword(factoryId, username, newPassword);
        return ApiResponse.success("密码重置成功", null);
    }
}
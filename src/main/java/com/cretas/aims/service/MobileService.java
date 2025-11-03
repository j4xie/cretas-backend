package com.cretas.aims.service;

import com.cretas.aims.dto.MobileDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 移动端服务接口
 *
 * @author Cretas Team
 * @version 1.0.0
 * @since 2025-01-09
 */
public interface MobileService {

    /**
     * 移动端统一登录
     *
     * @param request 登录请求
     * @return 登录响应
     */
    MobileDTO.LoginResponse unifiedLogin(MobileDTO.LoginRequest request);

    /**
     * 移动端设备激活
     *
     * @param request 激活请求
     * @return 激活响应
     */
    MobileDTO.ActivationResponse activateDevice(MobileDTO.ActivationRequest request);

    /**
     * 移动端文件上传
     *
     * @param files 文件列表
     * @param category 文件分类
     * @param metadata 元数据
     * @return 上传响应
     */
    MobileDTO.UploadResponse uploadFiles(List<MultipartFile> files, String category, String metadata);

    /**
     * 获取移动端仪表盘数据
     *
     * @param factoryId 工厂ID
     * @param userId 用户ID
     * @return 仪表盘数据
     */
    MobileDTO.DashboardData getDashboardData(String factoryId, Integer userId);

    /**
     * 数据同步
     *
     * @param factoryId 工厂ID
     * @param request 同步请求
     * @return 同步响应
     */
    MobileDTO.SyncResponse syncData(String factoryId, MobileDTO.SyncRequest request);

    /**
     * 注册推送通知
     *
     * @param userId 用户ID
     * @param registration 推送注册信息
     */
    void registerPushNotification(Integer userId, MobileDTO.PushRegistration registration);

    /**
     * 取消推送通知注册
     *
     * @param userId 用户ID
     * @param deviceToken 设备令牌
     */
    void unregisterPushNotification(Integer userId, String deviceToken);

    /**
     * 检查应用版本
     *
     * @param currentVersion 当前版本
     * @param platform 平台
     * @return 版本检查响应
     */
    MobileDTO.VersionCheckResponse checkVersion(String currentVersion, String platform);

    /**
     * 获取离线数据包
     *
     * @param factoryId 工厂ID
     * @param userId 用户ID
     * @return 离线数据包
     */
    MobileDTO.OfflineDataPackage getOfflineDataPackage(String factoryId, Integer userId);

    /**
     * 记录设备登录信息
     *
     * @param userId 用户ID
     * @param deviceInfo 设备信息
     */
    void recordDeviceLogin(Integer userId, MobileDTO.DeviceInfo deviceInfo);

    /**
     * 获取用户设备列表
     *
     * @param userId 用户ID
     * @return 设备信息列表
     */
    List<MobileDTO.DeviceInfo> getUserDevices(Integer userId);

    /**
     * 移除设备
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     */
    void removeDevice(Integer userId, String deviceId);

    /**
     * 刷新访问令牌
     *
     * @param refreshToken 刷新令牌
     * @return 新的登录响应
     */
    MobileDTO.LoginResponse refreshToken(String refreshToken);

    /**
     * 移动端登出
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     */
    void logout(Integer userId, String deviceId);

    /**
     * 获取移动端配置
     *
     * @param factoryId 工厂ID
     * @param platform 平台
     * @return 配置信息
     */
    Object getMobileConfig(String factoryId, String platform);

    /**
     * 上报崩溃日志
     *
     * @param deviceInfo 设备信息
     * @param crashLog 崩溃日志
     */
    void reportCrash(MobileDTO.DeviceInfo deviceInfo, String crashLog);

    /**
     * 上报性能数据
     *
     * @param deviceInfo 设备信息
     * @param performanceData 性能数据
     */
    void reportPerformance(MobileDTO.DeviceInfo deviceInfo, Object performanceData);

    /**
     * 移动端注册第一阶段（验证手机号）
     *
     * @param request 注册请求
     * @return 注册响应
     */
    MobileDTO.RegisterPhaseOneResponse registerPhaseOne(MobileDTO.RegisterPhaseOneRequest request);

    /**
     * 移动端注册第二阶段（创建账户）
     *
     * @param request 注册请求
     * @return 注册响应
     */
    MobileDTO.RegisterPhaseTwoResponse registerPhaseTwo(MobileDTO.RegisterPhaseTwoRequest request);

    /**
     * 验证Token
     *
     * @param token 令牌
     * @return 是否有效
     */
    boolean validateToken(String token);

    /**
     * 从Token获取用户信息
     *
     * @param token 令牌
     * @return 用户信息
     */
    com.cretas.aims.dto.user.UserDTO getUserFromToken(String token);

    /**
     * 修改密码
     *
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    void changePassword(Integer userId, String oldPassword, String newPassword);

    /**
     * 重置密码
     *
     * @param factoryId 工厂ID
     * @param username 用户名
     * @param newPassword 新密码
     */
    void resetPassword(String factoryId, String username, String newPassword);
}

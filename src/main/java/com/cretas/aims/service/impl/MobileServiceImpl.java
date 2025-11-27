package com.cretas.aims.service.impl;

import com.cretas.aims.dto.MobileDTO;
import com.cretas.aims.dto.user.UserDTO;
import com.cretas.aims.entity.DeviceActivation;
import com.cretas.aims.entity.PlatformAdmin;
import com.cretas.aims.entity.Session;
import com.cretas.aims.entity.User;
import com.cretas.aims.entity.Whitelist;
import com.cretas.aims.entity.enums.FactoryUserRole;
import com.cretas.aims.entity.enums.WhitelistStatus;
import com.cretas.aims.exception.AuthenticationException;
import com.cretas.aims.exception.BusinessException;
import com.cretas.aims.exception.ResourceNotFoundException;
import com.cretas.aims.mapper.UserMapper;
import com.cretas.aims.repository.DeviceActivationRepository;
import com.cretas.aims.repository.PlatformAdminRepository;
import com.cretas.aims.repository.SessionRepository;
import com.cretas.aims.repository.UserRepository;
import com.cretas.aims.repository.WhitelistRepository;
import com.cretas.aims.service.MobileService;
import com.cretas.aims.service.TempTokenService;
import com.cretas.aims.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 移动端服务实现
 *
 * @author Cretas Team
 * @version 1.0.0
 * @since 2025-01-09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MobileServiceImpl implements MobileService {

    private final UserRepository userRepository;
    private final DeviceActivationRepository deviceActivationRepository;
    private final SessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final UserMapper userMapper;
    private final TempTokenService tempTokenService;
    private final WhitelistRepository whitelistRepository;
    private final PlatformAdminRepository platformAdminRepository;

    private static final long ACCESS_TOKEN_EXPIRES_SECONDS = 3600L;

    @Value("${app.upload.path:uploads/mobile}")
    private String uploadPath;

    @Value("${app.version.latest:1.0.0}")
    private String latestVersion;

    // 模拟设备登录记录（实际应使用数据库）
    private final Map<Integer, List<MobileDTO.DeviceInfo>> userDevices = new ConcurrentHashMap<>();

    @Override
    @Transactional
    public MobileDTO.LoginResponse unifiedLogin(MobileDTO.LoginRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();

        log.info("移动端统一登录: username={}", username);

        // 优先级1: 检查是否为平台管理员
        Optional<PlatformAdmin> platformAdminOpt = platformAdminRepository.findByUsername(username);
        if (platformAdminOpt.isPresent()) {
            log.info("检测到平台管理员登录: username={}", username);
            return loginAsPlatformAdmin(platformAdminOpt.get(), password, request.getDeviceInfo());
        }

        // 优先级2: 工厂用户登录
        String factoryId = request.getFactoryId();

        // 智能推断 factoryId（如果未提供）
        if (factoryId == null || factoryId.trim().isEmpty()) {
            log.info("factoryId未提供，开始智能推断: username={}", username);
            List<User> users = userRepository.findAllByUsername(username);

            if (users.isEmpty()) {
                throw new BusinessException("用户名或密码错误");
            } else if (users.size() == 1) {
                factoryId = users.get(0).getFactoryId();
                log.info("智能推断成功: username={}, factoryId={}", username, factoryId);
            } else {
                // 存在多个同名用户，必须提供 factoryId
                throw new BusinessException("存在多个同名用户，请提供工厂ID进行登录");
            }
        }

        log.info("工厂用户登录: factoryId={}, username={}", factoryId, username);

        // 根据工厂ID和用户名查找用户
        User user = userRepository.findByFactoryIdAndUsername(factoryId, username)
                .orElseThrow(() -> new BusinessException("用户名或密码错误"));

        // 白名单校验（基于手机号）
        validateUserWhitelist(factoryId, user.getPhone());

        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 检查用户状态
        if (!user.getIsActive()) {
            throw new BusinessException("用户账号已被禁用");
        }

        // 记录设备信息
        if (request.getDeviceInfo() != null) {
            recordDeviceLogin(user.getId(), request.getDeviceInfo());
        }

        // 生成令牌（包含角色信息）
        String role = resolveUserRole(user);
        String token = jwtUtil.generateToken(user.getId().toString(), role);
        String refreshToken = jwtUtil.generateRefreshToken(user.getId().toString());

        // 更新最后登录时间
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        saveSession(user.getId(), user.getFactoryId(), token, refreshToken, ACCESS_TOKEN_EXPIRES_SECONDS);

        // 构建响应
        return MobileDTO.LoginResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .factoryId(user.getFactoryId())
                .factoryName(user.getFactory() != null ? user.getFactory().getName() : null)
                .role(role)
                .permissions(parsePermissions(user.getPermissions()))
                .token(token)
                .refreshToken(refreshToken)
                .expiresIn(ACCESS_TOKEN_EXPIRES_SECONDS)
                .lastLoginTime(user.getLastLogin())
                .profile(MobileDTO.UserProfile.builder()
                        .name(user.getName())
                        .avatar(user.getAvatar())
                        .department(user.getDepartment())
                        .position(user.getPosition())
                        .phoneNumber(user.getPhone())
                        .email(null)
                        .build())
                .build();
    }

    /**
     * 平台管理员登录
     */
    private MobileDTO.LoginResponse loginAsPlatformAdmin(PlatformAdmin admin, String password,
                                                         MobileDTO.DeviceInfo deviceInfo) {
        // 验证密码
        if (!passwordEncoder.matches(password, admin.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 检查账号状态
        if (!admin.isActive()) {
            throw new BusinessException("账号已被禁用");
        }

        // 记录设备信息
        if (deviceInfo != null) {
            recordDeviceLogin(admin.getId(), deviceInfo);
        }

        // 生成令牌（使用 "platform_" 前缀区分平台管理员，包含角色信息）
        String role = admin.getPlatformRole() != null ? admin.getPlatformRole().name() : "auditor";
        String token = jwtUtil.generateToken("platform_" + admin.getId(), role);
        String refreshToken = jwtUtil.generateRefreshToken("platform_" + admin.getId());

        // 更新最后登录时间
        admin.setLastLoginAt(LocalDateTime.now());
        platformAdminRepository.save(admin);

        saveSession(admin.getId(), null, token, refreshToken, ACCESS_TOKEN_EXPIRES_SECONDS);

        // 构建响应
        return MobileDTO.LoginResponse.builder()
                .userId(admin.getId())
                .username(admin.getUsername())
                .factoryId(null) // 平台管理员没有 factoryId
                .factoryName("平台管理")
                .role(admin.getPlatformRole().name())
                .permissions(Arrays.asList(admin.getPermissions()))
                .token(token)
                .refreshToken(refreshToken)
                .expiresIn(3600L) // 1小时
                .lastLoginTime(admin.getLastLoginAt())
                .profile(MobileDTO.UserProfile.builder()
                        .name(admin.getRealName())
                        .avatar(null)
                        .department("平台管理部")
                        .position(admin.getPlatformRole().name())
                        .phoneNumber(admin.getPhoneNumber())
                        .email(admin.getEmail())
                        .build())
                .build();
    }

    @Override
    @Transactional
    public MobileDTO.ActivationResponse activateDevice(MobileDTO.ActivationRequest request) {
        log.info("设备激活: code={}, deviceId={}",
                request.getActivationCode(), request.getDeviceInfo().getDeviceId());

        // 查找激活码
        DeviceActivation activation = deviceActivationRepository
                .findByActivationCode(request.getActivationCode())
                .orElseThrow(() -> new BusinessException("无效的激活码"));

        // 验证激活码状态
        if (!"PENDING".equals(activation.getStatus())) {
            throw new BusinessException("激活码已被使用或已过期");
        }

        // 检查是否过期
        if (activation.getExpiresAt() != null &&
            LocalDateTime.now().isAfter(activation.getExpiresAt())) {
            activation.setStatus("EXPIRED");
            deviceActivationRepository.save(activation);
            throw new BusinessException("激活码已过期");
        }

        // 更新激活信息
        MobileDTO.DeviceInfo deviceInfo = request.getDeviceInfo();
        activation.setDeviceId(deviceInfo.getDeviceId());
        activation.setDeviceType(deviceInfo.getDeviceType());
        activation.setDeviceModel(deviceInfo.getModel());
        activation.setOsType(deviceInfo.getDeviceType());
        activation.setOsVersion(deviceInfo.getOsVersion());
        activation.setAppVersion(deviceInfo.getAppVersion());
        activation.setStatus("ACTIVATED");
        activation.setActivatedAt(LocalDateTime.now());
        deviceActivationRepository.save(activation);

        return MobileDTO.ActivationResponse.builder()
                .success(true)
                .factoryId(activation.getFactoryId())
                .factoryName(activation.getFactory() != null ? activation.getFactory().getName() : null)
                .activatedAt(activation.getActivatedAt())
                .validUntil(activation.getExpiresAt())
                .features(Arrays.asList("basic", "camera", "offline", "sync"))
                .configuration(new HashMap<>())
                .build();
    }

    @Override
    @Transactional
    public MobileDTO.UploadResponse uploadFiles(List<MultipartFile> files, String category, String metadata) {
        log.info("移动端文件上传: files={}, category={}", files.size(), category);

        MobileDTO.UploadResponse response = MobileDTO.UploadResponse.builder()
                .files(new ArrayList<>())
                .successCount(0)
                .failedCount(0)
                .build();

        // 确保上传目录存在
        Path uploadDir = Paths.get(uploadPath);
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            log.error("创建上传目录失败", e);
            throw new BusinessException("文件上传服务暂时不可用");
        }

        for (MultipartFile file : files) {
            try {
                // 生成唯一文件名
                String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                Path filepath = uploadDir.resolve(filename);

                // 保存文件
                file.transferTo(filepath.toFile());

                // 添加到响应
                MobileDTO.UploadedFile uploadedFile = MobileDTO.UploadedFile.builder()
                        .id(UUID.randomUUID().toString())
                        .url("/uploads/mobile/" + filename)
                        .originalName(file.getOriginalFilename())
                        .size(file.getSize())
                        .contentType(file.getContentType())
                        .uploadTime(LocalDateTime.now())
                        .build();

                response.getFiles().add(uploadedFile);
                response.setSuccessCount(response.getSuccessCount() + 1);
            } catch (Exception e) {
                log.error("文件上传失败: {}", file.getOriginalFilename(), e);
                response.setFailedCount(response.getFailedCount() + 1);
            }
        }

        return response;
    }

    @Override
    public MobileDTO.DashboardData getDashboardData(String factoryId, Integer userId) {
        log.debug("获取仪表盘数据: factoryId={}, userId={}", factoryId, userId);

        // TODO: 从各个服务获取实际数据
        // 这里返回模拟数据
        return MobileDTO.DashboardData.builder()
                .todayStats(MobileDTO.TodayStats.builder()
                        .productionCount(156)
                        .qualityCheckCount(145)
                        .materialReceived(23)
                        .ordersCompleted(8)
                        .productionEfficiency(92.5)
                        .activeWorkers(45)
                        .build())
                .todoItems(Arrays.asList(
                        MobileDTO.TodoItem.builder()
                                .id("1")
                                .title("质检任务")
                                .description("批次#20250109-001需要质检")
                                .priority("HIGH")
                                .status("PENDING")
                                .dueTime(LocalDateTime.now().plusHours(2))
                                .build()
                ))
                .recentActivities(Arrays.asList(
                        MobileDTO.ActivityLog.builder()
                                .type("PRODUCTION")
                                .title("生产完成")
                                .description("批次#20250109-001生产完成")
                                .operator("张三")
                                .time(LocalDateTime.now().minusHours(1))
                                .build()
                ))
                .alerts(Arrays.asList(
                        MobileDTO.Alert.builder()
                                .type("WARNING")
                                .title("库存预警")
                                .message("原材料A库存不足")
                                .severity("MEDIUM")
                                .time(LocalDateTime.now())
                                .build()
                ))
                .quickActions(Arrays.asList(
                        MobileDTO.QuickAction.builder()
                                .icon("scan")
                                .title("扫码录入")
                                .action("SCAN_INPUT")
                                .color("#4CAF50")
                                .orderIndex(1)
                                .build()
                ))
                .build();
    }

    @Override
    @Transactional
    public MobileDTO.SyncResponse syncData(String factoryId, MobileDTO.SyncRequest request) {
        log.info("数据同步: factoryId={}, dataTypes={}", factoryId, request.getDataTypes());

        // TODO: 实现实际的数据同步逻辑
        return MobileDTO.SyncResponse.builder()
                .serverData(new HashMap<>())
                .conflictCount(new HashMap<>())
                .nextSyncToken(UUID.randomUUID().toString())
                .syncTime(LocalDateTime.now())
                .build();
    }

    @Override
    public void registerPushNotification(Integer userId, MobileDTO.PushRegistration registration) {
        log.info("注册推送通知: userId={}, platform={}", userId, registration.getPlatform());
        // TODO: 实现推送通知注册逻辑
    }

    @Override
    public void unregisterPushNotification(Integer userId, String deviceToken) {
        log.info("取消推送通知: userId={}, token={}", userId, deviceToken);
        // TODO: 实现取消推送通知逻辑
    }

    @Override
    public MobileDTO.VersionCheckResponse checkVersion(String currentVersion, String platform) {
        log.debug("检查版本: current={}, platform={}", currentVersion, platform);

        boolean updateRequired = false;
        boolean updateAvailable = false;

        // 简单版本比较逻辑
        if (!currentVersion.equals(latestVersion)) {
            updateAvailable = true;
            // 如果主版本号不同，则强制更新
            String[] current = currentVersion.split("\\.");
            String[] latest = latestVersion.split("\\.");
            if (!current[0].equals(latest[0])) {
                updateRequired = true;
            }
        }

        return MobileDTO.VersionCheckResponse.builder()
                .currentVersion(currentVersion)
                .latestVersion(latestVersion)
                .updateRequired(updateRequired)
                .updateAvailable(updateAvailable)
                .downloadUrl("https://download.example.com/app-" + platform + "-" + latestVersion + ".apk")
                .releaseNotes("1. 修复已知问题\n2. 性能优化\n3. 新增功能")
                .fileSize(52428800L) // 50MB
                .releaseDate(LocalDateTime.now().minusDays(7))
                .build();
    }

    @Override
    public MobileDTO.OfflineDataPackage getOfflineDataPackage(String factoryId, Integer userId) {
        log.info("获取离线数据包: factoryId={}, userId={}", factoryId, userId);

        // TODO: 生成实际的离线数据包
        return MobileDTO.OfflineDataPackage.builder()
                .packageId(UUID.randomUUID().toString())
                .version("1.0.0")
                .baseData(new HashMap<>())
                .configData(new HashMap<>())
                .generatedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
    }

    @Override
    public void recordDeviceLogin(Integer userId, MobileDTO.DeviceInfo deviceInfo) {
        log.debug("记录设备登录: userId={}, deviceId={}", userId, deviceInfo.getDeviceId());

        userDevices.computeIfAbsent(userId, k -> new ArrayList<>());
        List<MobileDTO.DeviceInfo> devices = userDevices.get(userId);

        // 移除旧的相同设备记录
        devices.removeIf(d -> d.getDeviceId().equals(deviceInfo.getDeviceId()));

        // 添加新记录
        devices.add(deviceInfo);

        // 限制每个用户最多5个设备
        if (devices.size() > 5) {
            devices.remove(0);
        }
    }

    @Override
    public List<MobileDTO.DeviceInfo> getUserDevices(Integer userId) {
        log.debug("获取用户设备列表: userId={}", userId);
        return userDevices.getOrDefault(userId, new ArrayList<>());
    }

    @Override
    public void removeDevice(Integer userId, String deviceId) {
        log.info("移除设备: userId={}, deviceId={}", userId, deviceId);
        List<MobileDTO.DeviceInfo> devices = userDevices.get(userId);
        if (devices != null) {
            devices.removeIf(d -> d.getDeviceId().equals(deviceId));
        }
    }

    @Override
    public MobileDTO.LoginResponse refreshToken(String refreshToken) {
        log.debug("刷新令牌: token={}", refreshToken);

        Session session = sessionRepository.findByRefreshTokenAndIsRevokedFalse(refreshToken)
                .orElseThrow(() -> new BusinessException("刷新令牌无效或已过期"));

        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("刷新令牌已过期");
        }

        User user = userRepository.findById(session.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        String role = resolveUserRole(user);
        String newToken = jwtUtil.generateToken(user.getId().toString(), role);

        session.setToken(newToken);
        session.setExpiresAt(LocalDateTime.now().plusSeconds(ACCESS_TOKEN_EXPIRES_SECONDS));
        sessionRepository.save(session);

        return MobileDTO.LoginResponse.builder()
                .token(newToken)
                .refreshToken(refreshToken) // 保持原刷新令牌
                .expiresIn(ACCESS_TOKEN_EXPIRES_SECONDS)
                .build();
    }

    @Override
    public void logout(Integer userId, String deviceId) {
        log.info("用户登出: userId={}, deviceId={}", userId, deviceId);

        // 移除设备记录
        if (StringUtils.hasText(deviceId)) {
            removeDevice(userId, deviceId);
        }

        sessionRepository.revokeAllUserSessions(userId);
    }

    @Override
    public Object getMobileConfig(String factoryId, String platform) {
        log.debug("获取移动端配置: factoryId={}, platform={}", factoryId, platform);

        // TODO: 从数据库获取实际配置
        Map<String, Object> config = new HashMap<>();
        config.put("theme", "light");
        config.put("language", "zh-CN");
        config.put("features", Arrays.asList("scan", "camera", "location"));
        config.put("syncInterval", 300); // 5分钟
        config.put("offlineMode", true);

        return config;
    }

    @Override
    public void reportCrash(MobileDTO.DeviceInfo deviceInfo, String crashLog) {
        log.error("崩溃报告 - 设备: {}, 日志: {}", deviceInfo, crashLog);
        // TODO: 存储崩溃日志到数据库或日志分析系统
    }

    @Override
    public void reportPerformance(MobileDTO.DeviceInfo deviceInfo, Object performanceData) {
        log.info("性能报告 - 设备: {}, 数据: {}", deviceInfo, performanceData);
        // TODO: 存储性能数据用于分析
    }

    @Override
    @Transactional
    public MobileDTO.RegisterPhaseOneResponse registerPhaseOne(MobileDTO.RegisterPhaseOneRequest request) {
        String phoneNumber = request.getPhoneNumber();
        String factoryId = request.getFactoryId();

        // 智能推断 factoryId（如果未提供）
        if (factoryId == null || factoryId.trim().isEmpty()) {
            log.info("factoryId未提供，通过手机号查找白名单: phone={}", phoneNumber);
            List<Whitelist> whitelists = whitelistRepository.findAllByPhoneNumber(phoneNumber);

            if (whitelists.isEmpty()) {
                throw new BusinessException("该手机号未在任何工厂白名单中，请联系管理员添加");
            } else if (whitelists.size() == 1) {
                factoryId = whitelists.get(0).getFactoryId();
                log.info("智能推断成功: phone={}, factoryId={}", phoneNumber, factoryId);
            } else {
                // 手机号在多个工厂白名单中，必须指定
                throw new BusinessException("该手机号在多个工厂白名单中，请提供工厂ID进行注册");
            }
        }

        log.info("移动端注册第一阶段: phone={}, factoryId={}", phoneNumber, factoryId);

        // 检查白名单
        Whitelist whitelist = whitelistRepository.findByFactoryIdAndPhoneNumber(factoryId, phoneNumber)
                .orElseThrow(() -> new BusinessException("该手机号未在白名单中，无法注册"));

        // 检查状态和有效性
        if (!whitelist.isValid()) {
            if (whitelist.getStatus() != WhitelistStatus.ACTIVE) {
                throw new BusinessException("该手机号已被禁用");
            } else {
                throw new BusinessException("该手机号白名单已过期");
            }
        }

        // 检查用户是否已存在
        Optional<User> existingUser = userRepository.findByFactoryIdAndPhone(factoryId, phoneNumber);
        boolean isNewUser = !existingUser.isPresent();

        // 生成临时令牌（30分钟有效）
        String tempToken = tempTokenService.generateTempToken(phoneNumber, 30);
        long expiresAt = System.currentTimeMillis() + 30 * 60 * 1000;

        log.info("手机验证成功: phone={}, isNewUser={}", phoneNumber, isNewUser);

        return MobileDTO.RegisterPhaseOneResponse.builder()
                .tempToken(tempToken)
                .expiresAt(expiresAt)
                .phoneNumber(phoneNumber)
                .factoryId(factoryId)
                .isNewUser(isNewUser)
                .message(isNewUser ? "验证成功，请继续填写注册信息" : "该手机号已注册")
                .build();
    }

    @Override
    @Transactional
    public MobileDTO.RegisterPhaseTwoResponse registerPhaseTwo(MobileDTO.RegisterPhaseTwoRequest request) {
        log.info("移动端注册第二阶段: factory={}, username={}", request.getFactoryId(), request.getUsername());

        // 验证临时令牌
        String phoneNumber = tempTokenService.validateAndGetPhone(request.getTempToken());
        if (phoneNumber == null) {
            throw new BusinessException("临时令牌无效或已过期，请重新验证手机号");
        }

        // 获取或推断 factoryId
        String factoryId = request.getFactoryId();
        if (factoryId == null || factoryId.trim().isEmpty()) {
            // 从白名单推断 factoryId
            List<Whitelist> whitelists = whitelistRepository.findAllByPhoneNumber(phoneNumber);
            if (whitelists.isEmpty()) {
                throw new BusinessException("无法推断工厂ID，请提供factoryId");
            } else if (whitelists.size() == 1) {
                factoryId = whitelists.get(0).getFactoryId();
                log.info("从白名单推断factoryId: phone={}, factoryId={}", phoneNumber, factoryId);
            } else {
                throw new BusinessException("该手机号在多个工厂白名单中，请提供factoryId");
            }
        }

        // 检查用户名是否已存在（用户名全局唯一）
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("该用户名已被使用");
        }

        // 创建用户
        User user = new User();
        user.setFactoryId(factoryId);
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getRealName());
        user.setPhone(phoneNumber);
        user.setPosition(request.getPosition() != null ? request.getPosition() : FactoryUserRole.unactivated.name());
        user.setIsActive(false); // 需要管理员激活
        user = userRepository.save(user);

        // 删除临时令牌
        tempTokenService.deleteTempToken(request.getTempToken());

        // 构建用户资料
        MobileDTO.UserProfile profile = MobileDTO.UserProfile.builder()
                .name(user.getFullName())
                .phoneNumber(user.getPhone())
                .email(null) // email字段已删除
                .position(user.getPosition())
                .build();

        log.info("移动端注册成功: userId={}, username={}", user.getId(), user.getUsername());

        return MobileDTO.RegisterPhaseTwoResponse.builder()
                .role(user.getPosition() != null ? user.getPosition() : "unactivated") // 使用position字段
                .profile(profile)
                .message("注册成功，请等待管理员激活您的账户")
                .registeredAt(LocalDateTime.now())
                .build();
    }

    /**
     * 解析权限字符串
     */
    private List<String> parsePermissions(String permissions) {
        if (!StringUtils.hasText(permissions)) {
            return new ArrayList<>();
        }
        return Arrays.asList(permissions.split(","));
    }

    // ==================== 从 AuthService 整合的方法 ====================

    @Override
    public boolean validateToken(String token) {
        if (!jwtUtil.validateToken(token)) {
            return false;
        }
        return sessionRepository.findByTokenAndIsRevokedFalse(token)
                .map(session -> session.getExpiresAt().isAfter(LocalDateTime.now()))
                .orElse(false);
    }

    @Override
    public UserDTO getUserFromToken(String token) {
        Integer userId = jwtUtil.getUserIdFromToken(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        return userMapper.toDTO(user);
    }

    @Override
    @Transactional
    public void changePassword(Integer userId, String oldPassword, String newPassword) {
        // 查询用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new BusinessException("原密码错误");
        }

        // 更新密码
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // 撤销所有会话
        sessionRepository.revokeAllUserSessions(userId);
    }

    @Override
    @Transactional
    public void resetPassword(String factoryId, String username, String newPassword) {
        User user = userRepository.findByFactoryIdAndUsername(factoryId, username)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        // 更新密码
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // 撤销所有会话
        sessionRepository.revokeAllUserSessions(user.getId());
    }

    /**
     * 统一解析用户角色
     */
    private String resolveUserRole(User user) {
        if (StringUtils.hasText(user.getPosition())) {
            return user.getPosition();
        }
        if (StringUtils.hasText(user.getRoleCode())) {
            return user.getRoleCode();
        }
        return "viewer";
    }

    /**
     * 持久化会话
     */
    private void saveSession(Integer userId, String factoryId, String token, String refreshToken, long expiresInSeconds) {
        Session session = new Session();
        session.setUserId(userId);
        session.setFactoryId(factoryId);
        session.setToken(token);
        session.setRefreshToken(refreshToken);
        session.setExpiresAt(LocalDateTime.now().plusSeconds(expiresInSeconds));
        session.setIsRevoked(false);
        sessionRepository.save(session);
    }

    /**
     * 校验用户是否在白名单且有效
     */
    private void validateUserWhitelist(String factoryId, String phoneNumber) {
        if (!StringUtils.hasText(phoneNumber)) {
            throw new BusinessException("用户未绑定手机号，无法进行白名单校验");
        }
        Whitelist whitelist = whitelistRepository.findByFactoryIdAndPhoneNumber(factoryId, phoneNumber)
                .orElseThrow(() -> new BusinessException("该手机号未在白名单中，无法登录"));
        if (!whitelist.isValid()) {
            throw new BusinessException("白名单已失效或被禁用");
        }
    }
}

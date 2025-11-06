package com.cretas.aims.service.impl;

import com.cretas.aims.dto.common.PageRequest;
import com.cretas.aims.dto.common.PageResponse;
import com.cretas.aims.dto.user.CreateUserRequest;
import com.cretas.aims.dto.user.UserDTO;
import com.cretas.aims.entity.User;
import com.cretas.aims.entity.enums.FactoryUserRole;
import com.cretas.aims.exception.BusinessException;
import com.cretas.aims.exception.ResourceNotFoundException;
import com.cretas.aims.mapper.UserMapper;
import com.cretas.aims.repository.UserRepository;
import com.cretas.aims.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 *
 * @author Cretas Team
 * @version 1.0.0
 * @since 2025-01-09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserDTO createUser(String factoryId, CreateUserRequest request) {
        // 检查用户名是否已存在（用户名全局唯一）
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        // 创建用户实体
        User user = userMapper.toEntity(request, factoryId);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        // 保存用户
        user = userRepository.save(user);

        log.info("创建用户成功: factoryId={}, username={}", factoryId, user.getUsername());
        return userMapper.toDTO(user);
    }

    @Override
    @Transactional
    public UserDTO updateUser(String factoryId, Integer userId, CreateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户", "id", userId));

        // 验证工厂ID
        if (!user.getFactoryId().equals(factoryId)) {
            throw new BusinessException("无权操作该用户");
        }

        // 更新用户信息
        userMapper.updateEntity(user, request);

        // 如果提供了新密码，则更新密码
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        user = userRepository.save(user);

        log.info("更新用户成功: userId={}", userId);
        return userMapper.toDTO(user);
    }

    @Override
    @Transactional
    public void deleteUser(String factoryId, Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户", "id", userId));

        // 验证工厂ID
        if (!user.getFactoryId().equals(factoryId)) {
            throw new BusinessException("无权操作该用户");
        }

        // 不允许删除超级管理员（通过职位判断）
        if (user.getPosition() != null &&
            (user.getPosition().equals("factory_super_admin") ||
             user.getPosition().equals("超级管理员"))) {
            throw new BusinessException("不能删除超级管理员");
        }

        userRepository.delete(user);
        log.info("删除用户成功: userId={}", userId);
    }

    @Override
    public UserDTO getUserById(String factoryId, Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户", "id", userId));

        // 验证工厂ID
        if (!user.getFactoryId().equals(factoryId)) {
            throw new BusinessException("无权查看该用户");
        }

        return userMapper.toDTO(user);
    }

    @Override
    public PageResponse<UserDTO> getUserList(String factoryId, PageRequest pageRequest) {
        Sort sort = Sort.by(
                pageRequest.getSortDirection().equalsIgnoreCase("DESC") ?
                Sort.Direction.DESC : Sort.Direction.ASC,
                pageRequest.getSortBy()
        );

        org.springframework.data.domain.PageRequest pageable = org.springframework.data.domain.PageRequest.of(
                pageRequest.getPage() - 1,
                pageRequest.getSize(),
                sort
        );

        Page<User> userPage = userRepository.findByFactoryId(factoryId, pageable);

        List<UserDTO> userDTOs = userPage.getContent().stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());

        return PageResponse.of(
                userDTOs,
                pageRequest.getPage(),
                pageRequest.getSize(),
                userPage.getTotalElements()
        );
    }

    @Override
    public List<UserDTO> getUsersByRole(String factoryId, FactoryUserRole role) {
        // 使用职位字段查询（roleCode已删除）
        return userRepository.findByFactoryIdAndPosition(factoryId, role.name())
                .stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void activateUser(String factoryId, Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户", "id", userId));

        // 验证工厂ID
        if (!user.getFactoryId().equals(factoryId)) {
            throw new BusinessException("无权操作该用户");
        }

        user.setIsActive(true);
        userRepository.save(user);

        log.info("激活用户成功: userId={}", userId);
    }

    @Override
    @Transactional
    public void deactivateUser(String factoryId, Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户", "id", userId));

        // 验证工厂ID
        if (!user.getFactoryId().equals(factoryId)) {
            throw new BusinessException("无权操作该用户");
        }

        // 不允许停用超级管理员（通过职位判断）
        if (user.getPosition() != null &&
            (user.getPosition().equals("factory_super_admin") ||
             user.getPosition().equals("超级管理员"))) {
            throw new BusinessException("不能停用超级管理员");
        }

        user.setIsActive(false);
        userRepository.save(user);

        log.info("停用用户成功: userId={}", userId);
    }

    @Override
    @Transactional
    public void updateUserRole(String factoryId, Integer userId, FactoryUserRole newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户", "id", userId));

        // 验证工厂ID
        if (!user.getFactoryId().equals(factoryId)) {
            throw new BusinessException("无权操作该用户");
        }

        // 更新职位字段（roleCode已删除）
        user.setPosition(newRole.name());
        userRepository.save(user);

        log.info("更新用户角色成功: userId={}, newRole={}", userId, newRole);
    }

    @Override
    public boolean checkUsernameExists(String factoryId, String username) {
        // 用户名全局唯一，不区分工厂
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean checkEmailExists(String factoryId, String email) {
        // email字段已删除，返回false
        return false;
    }

    @Override
    public PageResponse<UserDTO> searchUsers(String factoryId, String keyword, PageRequest pageRequest) {
        Sort sort = Sort.by(
                pageRequest.getSortDirection().equalsIgnoreCase("DESC") ?
                Sort.Direction.DESC : Sort.Direction.ASC,
                pageRequest.getSortBy()
        );

        org.springframework.data.domain.PageRequest pageable = org.springframework.data.domain.PageRequest.of(
                pageRequest.getPage() - 1,
                pageRequest.getSize(),
                sort
        );

        Page<User> userPage = userRepository.searchUsers(factoryId, keyword, pageable);

        List<UserDTO> userDTOs = userPage.getContent().stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());

        return PageResponse.of(
                userDTOs,
                pageRequest.getPage(),
                pageRequest.getSize(),
                userPage.getTotalElements()
        );
    }

    @Override
    @Transactional
    public List<UserDTO> batchImportUsers(String factoryId, List<CreateUserRequest> requests) {
        return requests.stream()
                .map(request -> {
                    try {
                        return createUser(factoryId, request);
                    } catch (Exception e) {
                        log.error("导入用户失败: {}", e.getMessage());
                        return null;
                    }
                })
                .filter(user -> user != null)
                .collect(Collectors.toList());
    }

    @Override
    public byte[] exportUsers(String factoryId) {
        // TODO: 实现用户导出功能（Excel格式）
        throw new UnsupportedOperationException("用户导出功能暂未实现");
    }
}

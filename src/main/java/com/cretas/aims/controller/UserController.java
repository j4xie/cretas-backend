package com.cretas.aims.controller;

import com.cretas.aims.dto.common.ApiResponse;
import com.cretas.aims.dto.common.PageRequest;
import com.cretas.aims.dto.common.PageResponse;
import com.cretas.aims.dto.user.CreateUserRequest;
import com.cretas.aims.dto.user.UserDTO;
import com.cretas.aims.entity.enums.FactoryUserRole;
import com.cretas.aims.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 用户管理控制器
 *
 * @author Cretas Team
 * @version 1.0.0
 * @since 2025-01-09
 */
@Slf4j
@RestController
@RequestMapping("/api/{factoryId}/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户管理相关接口")
@PreAuthorize("hasAnyAuthority('factory_super_admin','permission_admin','department_admin')")
public class UserController {

    private final UserService userService;

    /**
     * 创建用户
     */
    @PostMapping
    @Operation(summary = "创建用户")
    public ApiResponse<UserDTO> createUser(
            @Parameter(description = "工厂ID", required = true)
            @PathVariable @NotBlank String factoryId,
            @Valid @RequestBody CreateUserRequest request) {
        log.info("创建用户: factoryId={}, username={}", factoryId, request.getUsername());
        UserDTO user = userService.createUser(factoryId, request);
        return ApiResponse.success("用户创建成功", user);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{userId}")
    @Operation(summary = "更新用户信息")
    public ApiResponse<UserDTO> updateUser(
            @Parameter(description = "工厂ID", required = true)
            @PathVariable @NotBlank String factoryId,
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotNull Integer userId,
            @Valid @RequestBody CreateUserRequest request) {
        log.info("更新用户: factoryId={}, userId={}", factoryId, userId);
        UserDTO user = userService.updateUser(factoryId, userId, request);
        return ApiResponse.success("用户更新成功", user);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{userId}")
    @Operation(summary = "删除用户")
    public ApiResponse<Void> deleteUser(
            @Parameter(description = "工厂ID", required = true)
            @PathVariable @NotBlank String factoryId,
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotNull Integer userId) {
        log.info("删除用户: factoryId={}, userId={}", factoryId, userId);
        userService.deleteUser(factoryId, userId);
        return ApiResponse.success("用户删除成功", null);
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{userId}")
    @Operation(summary = "获取用户详情")
    public ApiResponse<UserDTO> getUserById(
            @Parameter(description = "工厂ID", required = true)
            @PathVariable @NotBlank String factoryId,
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotNull Integer userId) {
        UserDTO user = userService.getUserById(factoryId, userId);
        return ApiResponse.success(user);
    }

    /**
     * 获取用户列表
     */
    @GetMapping
    @Operation(summary = "获取用户列表（分页）")
    public ApiResponse<PageResponse<UserDTO>> getUserList(
            @Parameter(description = "工厂ID", required = true)
            @PathVariable @NotBlank String factoryId,
            @Valid PageRequest pageRequest) {
        PageResponse<UserDTO> response = userService.getUserList(factoryId, pageRequest);
        return ApiResponse.success(response);
    }

    /**
     * 按角色获取用户
     */
    @GetMapping("/role/{roleCode}")
    @Operation(summary = "按角色获取用户列表")
    public ApiResponse<List<UserDTO>> getUsersByRole(
            @Parameter(description = "工厂ID", required = true)
            @PathVariable @NotBlank String factoryId,
            @Parameter(description = "角色代码", required = true)
            @PathVariable FactoryUserRole roleCode) {
        List<UserDTO> users = userService.getUsersByRole(factoryId, roleCode);
        return ApiResponse.success(users);
    }

    /**
     * 激活用户
     */
    @PostMapping("/{userId}/activate")
    @Operation(summary = "激活用户")
    public ApiResponse<Void> activateUser(
            @Parameter(description = "工厂ID", required = true)
            @PathVariable @NotBlank String factoryId,
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotNull Integer userId) {
        log.info("激活用户: factoryId={}, userId={}", factoryId, userId);
        userService.activateUser(factoryId, userId);
        return ApiResponse.success("用户激活成功", null);
    }

    /**
     * 停用用户
     */
    @PostMapping("/{userId}/deactivate")
    @Operation(summary = "停用用户")
    public ApiResponse<Void> deactivateUser(
            @Parameter(description = "工厂ID", required = true)
            @PathVariable @NotBlank String factoryId,
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotNull Integer userId) {
        log.info("停用用户: factoryId={}, userId={}", factoryId, userId);
        userService.deactivateUser(factoryId, userId);
        return ApiResponse.success("用户停用成功", null);
    }

    /**
     * 更新用户角色
     */
    @PutMapping("/{userId}/role")
    @Operation(summary = "更新用户角色")
    public ApiResponse<Void> updateUserRole(
            @Parameter(description = "工厂ID", required = true)
            @PathVariable @NotBlank String factoryId,
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotNull Integer userId,
            @Parameter(description = "新角色", required = true)
            @RequestParam FactoryUserRole newRole) {
        log.info("更新用户角色: factoryId={}, userId={}, newRole={}", factoryId, userId, newRole);
        userService.updateUserRole(factoryId, userId, newRole);
        return ApiResponse.success("角色更新成功", null);
    }

    /**
     * 检查用户名是否存在
     */
    @GetMapping("/check/username")
    @Operation(summary = "检查用户名是否存在")
    public ApiResponse<Boolean> checkUsernameExists(
            @Parameter(description = "工厂ID", required = true)
            @PathVariable @NotBlank String factoryId,
            @Parameter(description = "用户名", required = true)
            @RequestParam @NotBlank String username) {
        boolean exists = userService.checkUsernameExists(factoryId, username);
        return ApiResponse.success(exists);
    }

    /**
     * 检查邮箱是否存在
     */
    @GetMapping("/check/email")
    @Operation(summary = "检查邮箱是否存在")
    public ApiResponse<Boolean> checkEmailExists(
            @Parameter(description = "工厂ID", required = true)
            @PathVariable @NotBlank String factoryId,
            @Parameter(description = "邮箱", required = true)
            @RequestParam @NotBlank String email) {
        boolean exists = userService.checkEmailExists(factoryId, email);
        return ApiResponse.success(exists);
    }

    /**
     * 搜索用户
     */
    @GetMapping("/search")
    @Operation(summary = "搜索用户")
    public ApiResponse<PageResponse<UserDTO>> searchUsers(
            @Parameter(description = "工厂ID", required = true)
            @PathVariable @NotBlank String factoryId,
            @Parameter(description = "搜索关键词", required = true)
            @RequestParam @NotBlank String keyword,
            @Valid PageRequest pageRequest) {
        PageResponse<UserDTO> response = userService.searchUsers(factoryId, keyword, pageRequest);
        return ApiResponse.success(response);
    }

    /**
     * 批量导入用户
     */
    @PostMapping("/import")
    @Operation(summary = "批量导入用户")
    public ApiResponse<List<UserDTO>> batchImportUsers(
            @Parameter(description = "工厂ID", required = true)
            @PathVariable @NotBlank String factoryId,
            @Valid @RequestBody List<CreateUserRequest> requests) {
        log.info("批量导入用户: factoryId={}, count={}", factoryId, requests.size());
        List<UserDTO> users = userService.batchImportUsers(factoryId, requests);
        return ApiResponse.success("导入成功", users);
    }

    /**
     * 导出用户列表
     */
    @GetMapping("/export")
    @Operation(summary = "导出用户列表")
    public byte[] exportUsers(
            @Parameter(description = "工厂ID", required = true)
            @PathVariable @NotBlank String factoryId) {
        log.info("导出用户列表: factoryId={}", factoryId);
        return userService.exportUsers(factoryId);
    }
}

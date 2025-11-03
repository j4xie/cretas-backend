package com.cretas.aims.controller;

import com.cretas.aims.dto.common.ApiResponse;
import com.cretas.aims.dto.common.PageRequest;
import com.cretas.aims.dto.common.PageResponse;
import com.cretas.aims.dto.supplier.CreateSupplierRequest;
import com.cretas.aims.dto.supplier.SupplierDTO;
import com.cretas.aims.service.MobileService;
import com.cretas.aims.service.SupplierService;
import com.cretas.aims.utils.TokenUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 供应商管理控制器
 *
 * @author Cretas Team
 * @version 1.0.0
 * @since 2025-01-09
 */
@Slf4j
@RestController
@RequestMapping("/api/mobile/{factoryId}/suppliers")
@RequiredArgsConstructor
@Tag(name = "供应商管理", description = "供应商管理相关接口")
public class SupplierController {

    private final SupplierService supplierService;
    private final MobileService mobileService;

    /**
     * 创建供应商
     */
    @PostMapping
    @Operation(summary = "创建供应商")
    public ApiResponse<SupplierDTO> createSupplier(
            @Parameter(description = "工厂ID", required = true)
            @PathVariable @NotBlank String factoryId,
            @Parameter(description = "访问令牌", required = true)
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody CreateSupplierRequest request) {

        // 获取当前用户ID
        String token = TokenUtils.extractToken(authorization);
        Integer userId = mobileService.getUserFromToken(token).getId();

        log.info("创建供应商: factoryId={}, name={}", factoryId, request.getName());
        SupplierDTO supplier = supplierService.createSupplier(factoryId, request, userId);
        return ApiResponse.success("供应商创建成功", supplier);
    }

    /**
     * 更新供应商
     */
    @PutMapping("/{supplierId}")
    @Operation(summary = "更新供应商")
    public ApiResponse<SupplierDTO> updateSupplier(
            @Parameter(description = "工厂ID", required = true)
            @PathVariable @NotBlank String factoryId,
            @Parameter(description = "供应商ID", required = true)
            @PathVariable @NotNull Integer supplierId,
            @Valid @RequestBody CreateSupplierRequest request) {

        log.info("更新供应商: factoryId={}, supplierId={}", factoryId, supplierId);
        SupplierDTO supplier = supplierService.updateSupplier(factoryId, supplierId, request);
        return ApiResponse.success("供应商更新成功", supplier);
    }

    /**
     * 删除供应商
     */
    @DeleteMapping("/{supplierId}")
    @Operation(summary = "删除供应商")
    public ApiResponse<Void> deleteSupplier(
            @Parameter(description = "工厂ID", required = true)
            @PathVariable @NotBlank String factoryId,
            @Parameter(description = "供应商ID", required = true)
            @PathVariable @NotNull Integer supplierId) {

        log.info("删除供应商: factoryId={}, supplierId={}", factoryId, supplierId);
        supplierService.deleteSupplier(factoryId, supplierId);
        return ApiResponse.success("供应商删除成功", null);
    }

    /**
     * 获取供应商详情
     */
    @GetMapping("/{supplierId}")
    @Operation(summary = "获取供应商详情")
    public ApiResponse<SupplierDTO> getSupplierById(
            @Parameter(description = "工厂ID", required = true)
            @PathVariable @NotBlank String factoryId,
            @Parameter(description = "供应商ID", required = true)
            @PathVariable @NotNull Integer supplierId) {

        SupplierDTO supplier = supplierService.getSupplierById(factoryId, supplierId);
        return ApiResponse.success(supplier);
    }

    /**
     * 获取供应商列表（分页）
     */
    @GetMapping
    @Operation(summary = "获取供应商列表（分页）")
    public ApiResponse<PageResponse<SupplierDTO>> getSupplierList(
            @Parameter(description = "工厂ID", required = true)
            @PathVariable @NotBlank String factoryId,
            @Valid PageRequest pageRequest) {

        PageResponse<SupplierDTO> response = supplierService.getSupplierList(factoryId, pageRequest);
        return ApiResponse.success(response);
    }

    /**
     * 获取活跃供应商列表
     */
    @GetMapping("/active")
    @Operation(summary = "获取活跃供应商列表")
    public ApiResponse<List<SupplierDTO>> getActiveSuppliers(
            @Parameter(description = "工厂ID", required = true)
            @PathVariable @NotBlank String factoryId) {

        List<SupplierDTO> suppliers = supplierService.getActiveSuppliers(factoryId);
        return ApiResponse.success(suppliers);
    }

    /**
     * 搜索供应商
     */
    @GetMapping("/search")
    @Operation(summary = "搜索供应商")
    public ApiResponse<List<SupplierDTO>> searchSuppliers(
            @Parameter(description = "工厂ID", required = true)
            @PathVariable @NotBlank String factoryId,
            @Parameter(description = "搜索关键词", required = true)
            @RequestParam @NotBlank String keyword) {

        List<SupplierDTO> suppliers = supplierService.searchSuppliersByName(factoryId, keyword);
        return ApiResponse.success(suppliers);
    }

    /**
     * 按材料类型获取供应商
     */
    @GetMapping("/by-material")
    @Operation(summary = "按材料类型获取供应商")
    public ApiResponse<List<SupplierDTO>> getSuppliersByMaterialType(
            @Parameter(description = "工厂ID", required = true)
            @PathVariable @NotBlank String factoryId,
            @Parameter(description = "材料类型", required = true)
            @RequestParam @NotBlank String materialType) {

        List<SupplierDTO> suppliers = supplierService.getSuppliersByMaterialType(factoryId, materialType);
        return ApiResponse.success(suppliers);
    }

    /**
     * 切换供应商状态
     */
    @PutMapping("/{supplierId}/status")
    @Operation(summary = "切换供应商状态")
    public ApiResponse<SupplierDTO> toggleSupplierStatus(
            @Parameter(description = "工厂ID", required = true)
            @PathVariable @NotBlank String factoryId,
            @Parameter(description = "供应商ID", required = true)
            @PathVariable @NotNull Integer supplierId,
            @Parameter(description = "激活状态", required = true)
            @RequestParam @NotNull Boolean isActive) {

        log.info("切换供应商状态: factoryId={}, supplierId={}, isActive={}",
                factoryId, supplierId, isActive);
        SupplierDTO supplier = supplierService.toggleSupplierStatus(factoryId, supplierId, isActive);
        return ApiResponse.success("供应商状态更新成功", supplier);
    }

    /**
     * 更新供应商评级
     */
    @PutMapping("/{supplierId}/rating")
    @Operation(summary = "更新供应商评级")
    public ApiResponse<SupplierDTO> updateSupplierRating(
            @Parameter(description = "工厂ID", required = true)
            @PathVariable @NotBlank String factoryId,
            @Parameter(description = "供应商ID", required = true)
            @PathVariable @NotNull Integer supplierId,
            @Parameter(description = "评级(1-5)", required = true)
            @RequestParam @NotNull Integer rating,
            @Parameter(description = "评级说明")
            @RequestParam(required = false) String notes) {

        log.info("更新供应商评级: factoryId={}, supplierId={}, rating={}",
                factoryId, supplierId, rating);
        SupplierDTO supplier = supplierService.updateSupplierRating(factoryId, supplierId, rating, notes);
        return ApiResponse.success("供应商评级更新成功", supplier);
    }

    /**
     * 更新供应商信用额度
     */
    @PutMapping("/{supplierId}/credit-limit")
    @Operation(summary = "更新供应商信用额度")
    public ApiResponse<SupplierDTO> updateCreditLimit(
            @Parameter(description = "工厂ID", required = true)
            @PathVariable @NotBlank String factoryId,
            @Parameter(description = "供应商ID", required = true)
            @PathVariable @NotNull Integer supplierId,
            @Parameter(description = "信用额度", required = true)
            @RequestParam @NotNull BigDecimal creditLimit) {

        log.info("更新供应商信用额度: factoryId={}, supplierId={}, creditLimit={}",
                factoryId, supplierId, creditLimit);
        SupplierDTO supplier = supplierService.updateCreditLimit(factoryId, supplierId, creditLimit);
        return ApiResponse.success("信用额度更新成功", supplier);
    }

    /**
     * 获取供应商统计信息
     */
    @GetMapping("/{supplierId}/statistics")
    @Operation(summary = "获取供应商统计信息")
    public ApiResponse<Map<String, Object>> getSupplierStatistics(
            @Parameter(description = "工厂ID", required = true)
            @PathVariable @NotBlank String factoryId,
            @Parameter(description = "供应商ID", required = true)
            @PathVariable @NotNull Integer supplierId) {

        Map<String, Object> statistics = supplierService.getSupplierStatistics(factoryId, supplierId);
        return ApiResponse.success(statistics);
    }

    /**
     * 获取供应商供货历史
     */
    @GetMapping("/{supplierId}/history")
    @Operation(summary = "获取供应商供货历史")
    public ApiResponse<List<Map<String, Object>>> getSupplierHistory(
            @Parameter(description = "工厂ID", required = true)
            @PathVariable @NotBlank String factoryId,
            @Parameter(description = "供应商ID", required = true)
            @PathVariable @NotNull Integer supplierId) {

        List<Map<String, Object>> history = supplierService.getSupplierHistory(factoryId, supplierId);
        return ApiResponse.success(history);
    }

    /**
     * 检查供应商代码是否存在
     */
    @GetMapping("/check-code")
    @Operation(summary = "检查供应商代码是否存在")
    public ApiResponse<Boolean> checkSupplierCode(
            @Parameter(description = "工厂ID", required = true)
            @PathVariable @NotBlank String factoryId,
            @Parameter(description = "供应商代码", required = true)
            @RequestParam @NotBlank String supplierCode) {

        boolean exists = supplierService.checkSupplierCodeExists(factoryId, supplierCode);
        return ApiResponse.success(exists);
    }

    /**
     * 导出供应商列表
     */
    @GetMapping("/export")
    @Operation(summary = "导出供应商列表")
    public byte[] exportSupplierList(
            @Parameter(description = "工厂ID", required = true)
            @PathVariable @NotBlank String factoryId) {

        log.info("导出供应商列表: factoryId={}", factoryId);
        return supplierService.exportSupplierList(factoryId);
    }

    /**
     * 批量导入供应商
     */
    @PostMapping("/import")
    @Operation(summary = "批量导入供应商")
    public ApiResponse<List<SupplierDTO>> importSuppliers(
            @Parameter(description = "工厂ID", required = true)
            @PathVariable @NotBlank String factoryId,
            @Parameter(description = "访问令牌", required = true)
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody List<CreateSupplierRequest> requests) {

        // 获取当前用户ID
        String token = TokenUtils.extractToken(authorization);
        Integer userId = mobileService.getUserFromToken(token).getId();

        log.info("批量导入供应商: factoryId={}, count={}", factoryId, requests.size());
        List<SupplierDTO> suppliers = supplierService.importSuppliers(factoryId, requests, userId);
        return ApiResponse.success(String.format("成功导入%d个供应商", suppliers.size()), suppliers);
    }

    /**
     * 获取供应商评级分布
     */
    @GetMapping("/rating-distribution")
    @Operation(summary = "获取供应商评级分布")
    public ApiResponse<Map<Integer, Long>> getSupplierRatingDistribution(
            @Parameter(description = "工厂ID", required = true)
            @PathVariable @NotBlank String factoryId) {

        Map<Integer, Long> distribution = supplierService.getSupplierRatingDistribution(factoryId);
        return ApiResponse.success(distribution);
    }

    /**
     * 获取有欠款的供应商
     */
    @GetMapping("/outstanding-balance")
    @Operation(summary = "获取有欠款的供应商")
    public ApiResponse<List<SupplierDTO>> getSuppliersWithOutstandingBalance(
            @Parameter(description = "工厂ID", required = true)
            @PathVariable @NotBlank String factoryId) {

        List<SupplierDTO> suppliers = supplierService.getSuppliersWithOutstandingBalance(factoryId);
        return ApiResponse.success(suppliers);
    }
}
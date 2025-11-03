package com.cretas.aims.service.impl;

import com.cretas.aims.dto.common.PageRequest;
import com.cretas.aims.dto.common.PageResponse;
import com.cretas.aims.dto.supplier.CreateSupplierRequest;
import com.cretas.aims.dto.supplier.SupplierDTO;
import com.cretas.aims.entity.Supplier;
import com.cretas.aims.exception.BusinessException;
import com.cretas.aims.exception.ResourceNotFoundException;
import com.cretas.aims.mapper.SupplierMapper;
import com.cretas.aims.repository.SupplierRepository;
import com.cretas.aims.service.SupplierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
/**
 * 供应商服务实现
 *
 * @author Cretas Team
 * @version 1.0.0
 * @since 2025-01-09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {
    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;
    @Override
    @Transactional
    public SupplierDTO createSupplier(String factoryId, CreateSupplierRequest request, Integer userId) {
        log.info("创建供应商: factoryId={}, name={}", factoryId, request.getName());
        // 检查供应商名称是否重复
        if (supplierRepository.existsByFactoryIdAndName(factoryId, request.getName())) {
            throw new BusinessException("供应商名称已存在");
        }
        // 创建供应商实体
        Supplier supplier = supplierMapper.toEntity(request, factoryId, userId);
        // 确保供应商代码唯一
        String baseCode = supplier.getSupplierCode();
        int counter = 0;
        while (supplierRepository.existsBySupplierCode(supplier.getSupplierCode())) {
            counter++;
            supplier.setSupplierCode(baseCode + "-" + counter);
        }
        // 保存供应商
        supplier = supplierRepository.save(supplier);
        log.info("供应商创建成功: id={}, code={}", supplier.getId(), supplier.getSupplierCode());
        return supplierMapper.toDTO(supplier);
    }
    @Override
    @Transactional
    public SupplierDTO updateSupplier(String factoryId, Integer supplierId, CreateSupplierRequest request) {
        log.info("更新供应商: factoryId={}, supplierId={}", factoryId, supplierId);
        Supplier supplier = supplierRepository.findByIdAndFactoryId(supplierId, factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("供应商不存在"));
        // 检查名称是否与其他供应商重复
        if (request.getName() != null && !request.getName().equals(supplier.getName())) {
            if (supplierRepository.existsByFactoryIdAndNameAndIdNot(factoryId, request.getName(), supplierId)) {
                throw new BusinessException("供应商名称已存在");
            }
        }
        // 更新供应商信息
        supplierMapper.updateEntity(supplier, request);
        supplier = supplierRepository.save(supplier);
        log.info("供应商更新成功: id={}", supplier.getId());
        return supplierMapper.toDTO(supplier);
    }
    @Override
    @Transactional
    public void deleteSupplier(String factoryId, Integer supplierId) {
        log.info("删除供应商: factoryId={}, supplierId={}", factoryId, supplierId);
        Supplier supplier = supplierRepository.findByIdAndFactoryId(supplierId, factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("供应商不存在"));
        // 检查是否有关联的原材料批次
        if (supplierRepository.hasRelatedMaterialBatches(supplierId)) {
            throw new BusinessException("供应商有关联的原材料批次，无法删除");
        }
        supplierRepository.delete(supplier);
        log.info("供应商删除成功: id={}", supplierId);
    }
    @Override
    @Transactional(readOnly = true)
    public SupplierDTO getSupplierById(String factoryId, Integer supplierId) {
        Supplier supplier = supplierRepository.findByIdAndFactoryId(supplierId, factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("供应商不存在"));
        return supplierMapper.toDTO(supplier);
    }
    @Override
    @Transactional(readOnly = true)
    public PageResponse<SupplierDTO> getSupplierList(String factoryId, PageRequest pageRequest) {
        // 创建分页请求
        org.springframework.data.domain.PageRequest pageable = org.springframework.data.domain.PageRequest.of(
                pageRequest.getPage() - 1,
                pageRequest.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        // 查询供应商
        Page<Supplier> supplierPage = supplierRepository.findByFactoryId(factoryId, pageable);
        // 转换为DTO
        List<SupplierDTO> supplierDTOs = supplierPage.getContent().stream()
                .map(supplierMapper::toDTO)
                .collect(Collectors.toList());
        // 构建分页响应
        PageResponse<SupplierDTO> response = new PageResponse<>();
        response.setContent(supplierDTOs);
        response.setPage(pageRequest.getPage());
        response.setSize(pageRequest.getSize());
        response.setTotalElements(supplierPage.getTotalElements());
        response.setTotalPages(supplierPage.getTotalPages());
        response.setFirst(supplierPage.isFirst());
        response.setLast(supplierPage.isLast());
        return response;
    }
    @Override
    @Transactional(readOnly = true)
    public List<SupplierDTO> getActiveSuppliers(String factoryId) {
        List<Supplier> suppliers = supplierRepository.findByFactoryIdAndIsActive(factoryId, true);
        return suppliers.stream()
                .map(supplierMapper::toDTO)
                .collect(Collectors.toList());
    }
    @Override
    @Transactional(readOnly = true)
    public List<SupplierDTO> searchSuppliersByName(String factoryId, String keyword) {
        List<Supplier> suppliers = supplierRepository.searchByName(factoryId, keyword);
        return suppliers.stream()
                .map(supplierMapper::toDTO)
                .collect(Collectors.toList());
    }
    @Override
    @Transactional(readOnly = true)
    public List<SupplierDTO> getSuppliersByMaterialType(String factoryId, String materialType) {
        List<Supplier> suppliers = supplierRepository.findByFactoryIdAndSuppliedMaterialsContaining(
                factoryId, materialType);
        return suppliers.stream()
                .map(supplierMapper::toDTO)
                .collect(Collectors.toList());
    }
    @Override
    @Transactional
    public SupplierDTO toggleSupplierStatus(String factoryId, Integer supplierId, Boolean isActive) {
        log.info("切换供应商状态: factoryId={}, supplierId={}, isActive={}",
                factoryId, supplierId, isActive);
        Supplier supplier = supplierRepository.findByIdAndFactoryId(supplierId, factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("供应商不存在"));
        supplier.setIsActive(isActive);
        supplier.setUpdatedAt(LocalDateTime.now());
        supplier = supplierRepository.save(supplier);
        log.info("供应商状态更新成功: id={}, isActive={}", supplier.getId(), isActive);
        return supplierMapper.toDTO(supplier);
    }
    @Override
    @Transactional
    public SupplierDTO updateSupplierRating(String factoryId, Integer supplierId,
                                           Integer rating, String notes) {
        log.info("更新供应商评级: factoryId={}, supplierId={}, rating={}",
                factoryId, supplierId, rating);
        Supplier supplier = supplierRepository.findByIdAndFactoryId(supplierId, factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("供应商不存在"));
        if (rating < 1 || rating > 5) {
            throw new BusinessException("评级必须在1-5之间");
        }
        supplier.setRating(rating);
        supplier.setRatingNotes(notes);
        supplier = supplierRepository.save(supplier);
        log.info("供应商评级更新成功: id={}, rating={}", supplier.getId(), rating);
        return supplierMapper.toDTO(supplier);
    }
    @Override
    @Transactional
    public SupplierDTO updateCreditLimit(String factoryId, Integer supplierId, BigDecimal creditLimit) {
        log.info("更新供应商信用额度: factoryId={}, supplierId={}, creditLimit={}",
                factoryId, supplierId, creditLimit);
        Supplier supplier = supplierRepository.findByIdAndFactoryId(supplierId, factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("供应商不存在"));
        if (creditLimit.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("信用额度不能为负数");
        }
        supplier.setCreditLimit(creditLimit);
        supplier = supplierRepository.save(supplier);
        log.info("供应商信用额度更新成功: id={}, creditLimit={}", supplier.getId(), creditLimit);
        return supplierMapper.toDTO(supplier);
    }
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getSupplierStatistics(String factoryId, Integer supplierId) {
        Supplier supplier = supplierRepository.findByIdAndFactoryId(supplierId, factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("供应商不存在"));
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("supplierId", supplier.getId());
        statistics.put("supplierName", supplier.getName());
        statistics.put("rating", supplier.getRating());
        statistics.put("creditLimit", supplier.getCreditLimit());
        statistics.put("currentBalance", supplier.getCurrentBalance());
        statistics.put("isActive", supplier.getIsActive());
        // TODO: 添加订单统计、供货统计等信息
        statistics.put("totalOrders", 0);
        statistics.put("totalAmount", BigDecimal.ZERO);
        statistics.put("averageDeliveryDays", 0);
        statistics.put("onTimeDeliveryRate", 0.0);
        return statistics;
    }
    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getSupplierHistory(String factoryId, Integer supplierId) {
        Supplier supplier = supplierRepository.findByIdAndFactoryId(supplierId, factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("供应商不存在"));
        // TODO: 从原材料批次表中获取供货历史
        List<Map<String, Object>> history = new ArrayList<>();
        return history;
    }
    @Override
    @Transactional(readOnly = true)
    public boolean checkSupplierCodeExists(String factoryId, String supplierCode) {
        return supplierRepository.existsByFactoryIdAndSupplierCode(factoryId, supplierCode);
    }
    @Override
    public byte[] exportSupplierList(String factoryId) {
        // TODO: 实现供应商列表导出功能
        throw new UnsupportedOperationException("供应商列表导出功能待实现");
    }
    @Override
    @Transactional
    public List<SupplierDTO> importSuppliers(String factoryId, List<CreateSupplierRequest> requests,
                                            Integer userId) {
        log.info("批量导入供应商: factoryId={}, count={}", factoryId, requests.size());
        List<SupplierDTO> importedSuppliers = new ArrayList<>();
        for (CreateSupplierRequest request : requests) {
            try {
                SupplierDTO supplier = createSupplier(factoryId, request, userId);
                importedSuppliers.add(supplier);
            } catch (Exception e) {
                log.error("导入供应商失败: name={}, error={}", request.getName(), e.getMessage());
            }
        }
        log.info("批量导入完成，成功导入 {} 个供应商", importedSuppliers.size());
        return importedSuppliers;
    }
    @Override
    @Transactional(readOnly = true)
    public Map<Integer, Long> getSupplierRatingDistribution(String factoryId) {
        List<Object[]> distribution = supplierRepository.getSupplierRatingDistribution(factoryId);
        Map<Integer, Long> result = new HashMap<>();
        for (Object[] row : distribution) {
            Integer rating = (Integer) row[0];
            Long count = (Long) row[1];
            result.put(rating, count);
        }
        // 确保所有评级都有值
        for (int i = 1; i <= 5; i++) {
            result.putIfAbsent(i, 0L);
        }
        return result;
    }
    @Override
    @Transactional(readOnly = true)
    public List<SupplierDTO> getSuppliersWithOutstandingBalance(String factoryId) {
        List<Supplier> suppliers = supplierRepository.findSuppliersWithOutstandingBalance(factoryId);
        return suppliers.stream()
                .map(supplierMapper::toDTO)
                .collect(Collectors.toList());
    }
}

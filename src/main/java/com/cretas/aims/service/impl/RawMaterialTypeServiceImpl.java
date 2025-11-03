package com.cretas.aims.service.impl;

import com.cretas.aims.dto.common.PageRequest;
import com.cretas.aims.dto.common.PageResponse;
import com.cretas.aims.dto.material.RawMaterialTypeDTO;
import com.cretas.aims.entity.RawMaterialType;
import com.cretas.aims.exception.BusinessException;
import com.cretas.aims.exception.ResourceNotFoundException;
import com.cretas.aims.repository.RawMaterialTypeRepository;
import com.cretas.aims.service.RawMaterialTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 原材料类型服务实现
 *
 * @author Cretas Team
 * @version 1.0.0
 * @since 2025-01-09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RawMaterialTypeServiceImpl implements RawMaterialTypeService {

    private final RawMaterialTypeRepository materialTypeRepository;

    @Override
    @Transactional
    public RawMaterialTypeDTO createMaterialType(String factoryId, RawMaterialTypeDTO dto) {
        log.info("创建原材料类型: factoryId={}, code={}", factoryId, dto.getCode());

        // 检查编码是否已存在
        if (materialTypeRepository.existsByFactoryIdAndCode(factoryId, dto.getCode())) {
            throw new BusinessException("原材料编码已存在: " + dto.getCode());
        }

        RawMaterialType materialType = new RawMaterialType();
        materialType.setFactoryId(factoryId);
        materialType.setCode(dto.getCode());
        materialType.setName(dto.getName());
        materialType.setCategory(dto.getCategory());
        materialType.setUnit(dto.getUnit());
        materialType.setUnitPrice(dto.getUnitPrice());
        materialType.setStorageType(dto.getStorageType());
        materialType.setShelfLifeDays(dto.getShelfLifeDays());
        materialType.setMinStock(dto.getMinStock());
        materialType.setMaxStock(dto.getMaxStock());
        materialType.setNotes(dto.getNotes());
        materialType.setIsActive(true);
        // 使用从Controller传入的createdBy，如果为null则使用默认值1
        materialType.setCreatedBy(dto.getCreatedBy() != null ? dto.getCreatedBy() : 1);
        materialType.setCreatedAt(LocalDateTime.now());
        materialType.setUpdatedAt(LocalDateTime.now());

        materialType = materialTypeRepository.save(materialType);

        log.info("原材料类型创建成功: id={}", materialType.getId());
        return convertToDTO(materialType);
    }

    @Override
    @Transactional
    public RawMaterialTypeDTO updateMaterialType(String factoryId, Integer id, RawMaterialTypeDTO dto) {
        log.info("更新原材料类型: factoryId={}, id={}", factoryId, id);

        RawMaterialType materialType = materialTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("原材料类型不存在: " + id));

        if (!materialType.getFactoryId().equals(factoryId)) {
            throw new BusinessException("无权限操作此原材料类型");
        }

        // 检查编码是否重复
        if (dto.getCode() != null && !dto.getCode().equals(materialType.getCode())) {
            if (materialTypeRepository.existsByFactoryIdAndCode(factoryId, dto.getCode())) {
                throw new BusinessException("原材料编码已存在: " + dto.getCode());
            }
            materialType.setCode(dto.getCode());
        }

        // 更新其他字段
        if (dto.getName() != null) materialType.setName(dto.getName());
        if (dto.getCategory() != null) materialType.setCategory(dto.getCategory());
        if (dto.getUnit() != null) materialType.setUnit(dto.getUnit());
        if (dto.getUnitPrice() != null) materialType.setUnitPrice(dto.getUnitPrice());
        if (dto.getStorageType() != null) materialType.setStorageType(dto.getStorageType());
        if (dto.getShelfLifeDays() != null) materialType.setShelfLifeDays(dto.getShelfLifeDays());
        if (dto.getMinStock() != null) materialType.setMinStock(dto.getMinStock());
        if (dto.getMaxStock() != null) materialType.setMaxStock(dto.getMaxStock());
        if (dto.getNotes() != null) materialType.setNotes(dto.getNotes());
        if (dto.getIsActive() != null) materialType.setIsActive(dto.getIsActive());

        materialType.setUpdatedAt(LocalDateTime.now());
        materialType = materialTypeRepository.save(materialType);

        log.info("原材料类型更新成功: id={}", materialType.getId());
        return convertToDTO(materialType);
    }

    @Override
    @Transactional
    public void deleteMaterialType(String factoryId, Integer id) {
        log.info("删除原材料类型: factoryId={}, id={}", factoryId, id);

        RawMaterialType materialType = materialTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("原材料类型不存在: " + id));

        if (!materialType.getFactoryId().equals(factoryId)) {
            throw new BusinessException("无权限操作此原材料类型");
        }

        // TODO: 检查是否有关联的批次
        // if (materialType.getMaterialBatches() != null && !materialType.getMaterialBatches().isEmpty()) {
        //     throw new BusinessException("原材料类型有关联的批次，无法删除");
        // }

        materialTypeRepository.delete(materialType);
        log.info("原材料类型删除成功: id={}", id);
    }

    @Override
    public RawMaterialTypeDTO getMaterialTypeById(String factoryId, Integer id) {
        log.info("获取原材料类型详情: factoryId={}, id={}", factoryId, id);

        RawMaterialType materialType = materialTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("原材料类型不存在: " + id));

        if (!materialType.getFactoryId().equals(factoryId)) {
            throw new BusinessException("无权限查看此原材料类型");
        }

        return convertToDTO(materialType);
    }

    @Override
    public PageResponse<RawMaterialTypeDTO> getMaterialTypes(String factoryId, PageRequest pageRequest) {
        log.info("获取原材料类型列表: factoryId={}, page={}, size={}",
                factoryId, pageRequest.getPage(), pageRequest.getSize());

        org.springframework.data.domain.PageRequest pageable = org.springframework.data.domain.PageRequest.of(
                pageRequest.getPage() - 1,
                pageRequest.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<RawMaterialType> page = materialTypeRepository.findByFactoryId(factoryId, pageable);

        List<RawMaterialTypeDTO> dtos = page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResponse.of(
                dtos,
                pageRequest.getPage(),
                pageRequest.getSize(),
                page.getTotalElements()
        );
    }

    @Override
    public List<RawMaterialTypeDTO> getActiveMaterialTypes(String factoryId) {
        log.info("获取激活的原材料类型: factoryId={}", factoryId);

        List<RawMaterialType> materialTypes = materialTypeRepository.findByFactoryIdAndIsActive(factoryId, true);
        return materialTypes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<RawMaterialTypeDTO> getMaterialTypesByCategory(String factoryId, String category) {
        log.info("根据类别获取原材料类型: factoryId={}, category={}", factoryId, category);

        List<RawMaterialType> materialTypes = materialTypeRepository.findByFactoryIdAndCategory(factoryId, category);
        return materialTypes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<RawMaterialTypeDTO> getMaterialTypesByStorageType(String factoryId, String storageType) {
        log.info("根据存储类型获取原材料类型: factoryId={}, storageType={}", factoryId, storageType);

        List<RawMaterialType> materialTypes = materialTypeRepository.findByFactoryIdAndStorageType(factoryId, storageType);
        return materialTypes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PageResponse<RawMaterialTypeDTO> searchMaterialTypes(String factoryId, String keyword, PageRequest pageRequest) {
        log.info("搜索原材料类型: factoryId={}, keyword={}", factoryId, keyword);

        org.springframework.data.domain.PageRequest pageable = org.springframework.data.domain.PageRequest.of(
                pageRequest.getPage() - 1,
                pageRequest.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<RawMaterialType> page = materialTypeRepository.searchMaterialTypes(factoryId, keyword, pageable);

        List<RawMaterialTypeDTO> dtos = page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResponse.of(
                dtos,
                pageRequest.getPage(),
                pageRequest.getSize(),
                page.getTotalElements()
        );
    }

    @Override
    public List<String> getMaterialCategories(String factoryId) {
        log.info("获取原材料类别列表: factoryId={}", factoryId);

        return materialTypeRepository.findByFactoryId(factoryId).stream()
                .map(RawMaterialType::getCategory)
                .filter(category -> category != null && !category.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public List<RawMaterialTypeDTO> getLowStockMaterials(String factoryId) {
        log.info("获取库存预警的原材料: factoryId={}", factoryId);

        List<RawMaterialType> materialTypes = materialTypeRepository.findMaterialTypesWithStockWarning(factoryId);
        return materialTypes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateMaterialTypesStatus(String factoryId, List<Integer> ids, Boolean isActive) {
        log.info("批量更新原材料类型状态: factoryId={}, ids={}, isActive={}", factoryId, ids, isActive);

        for (Integer id : ids) {
            RawMaterialType materialType = materialTypeRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("原材料类型不存在: " + id));

            if (!materialType.getFactoryId().equals(factoryId)) {
                throw new BusinessException("无权限操作原材料类型: " + id);
            }

            materialType.setIsActive(isActive);
            materialType.setUpdatedAt(LocalDateTime.now());
            materialTypeRepository.save(materialType);
        }

        log.info("批量更新原材料类型状态成功: count={}", ids.size());
    }

    @Override
    public boolean checkCodeExists(String factoryId, String code, Integer excludeId) {
        log.info("检查原材料编码是否存在: factoryId={}, code={}, excludeId={}", factoryId, code, excludeId);

        if (excludeId != null) {
            RawMaterialType existing = materialTypeRepository.findByFactoryIdAndCode(factoryId, code).orElse(null);
            return existing != null && !existing.getId().equals(excludeId);
        }

        return materialTypeRepository.existsByFactoryIdAndCode(factoryId, code);
    }

    /**
     * 转换实体到DTO
     */
    private RawMaterialTypeDTO convertToDTO(RawMaterialType materialType) {
        return RawMaterialTypeDTO.builder()
                .id(materialType.getId())
                .factoryId(materialType.getFactoryId())
                .code(materialType.getCode())
                .name(materialType.getName())
                .category(materialType.getCategory())
                .unit(materialType.getUnit())
                .unitPrice(materialType.getUnitPrice())
                .storageType(materialType.getStorageType())
                .shelfLifeDays(materialType.getShelfLifeDays())
                .minStock(materialType.getMinStock())
                .maxStock(materialType.getMaxStock())
                .isActive(materialType.getIsActive())
                .notes(materialType.getNotes())
                .createdBy(materialType.getCreatedBy())
                .createdAt(materialType.getCreatedAt())
                .updatedAt(materialType.getUpdatedAt())
                .build();
    }
}

package com.cretas.aims.service.impl;

import com.cretas.aims.dto.common.PageRequest;
import com.cretas.aims.dto.common.PageResponse;
import com.cretas.aims.dto.customer.CreateCustomerRequest;
import com.cretas.aims.dto.customer.CustomerDTO;
import com.cretas.aims.entity.Customer;
import com.cretas.aims.exception.BusinessException;
import com.cretas.aims.exception.ResourceNotFoundException;
import com.cretas.aims.mapper.CustomerMapper;
import com.cretas.aims.repository.CustomerRepository;
import com.cretas.aims.service.CustomerService;
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
 * 客户服务实现
 *
 * @author Cretas Team
 * @version 1.0.0
 * @since 2025-01-09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    @Override
    @Transactional
    public CustomerDTO createCustomer(String factoryId, CreateCustomerRequest request, Integer userId) {
        log.info("创建客户: factoryId={}, name={}", factoryId, request.getName());
        // 检查客户名称是否重复
        if (customerRepository.existsByFactoryIdAndName(factoryId, request.getName())) {
            throw new BusinessException("客户名称已存在");
        }
        // 创建客户实体
        Customer customer = customerMapper.toEntity(request, factoryId, userId);
        // 确保客户代码唯一
        String baseCode = customer.getCustomerCode();
        int counter = 0;
        while (customerRepository.existsByCustomerCode(customer.getCustomerCode())) {
            counter++;
            customer.setCustomerCode(baseCode + "-" + counter);
        }
        // 保存客户
        customer = customerRepository.save(customer);
        log.info("客户创建成功: id={}, code={}", customer.getId(), customer.getCustomerCode());
        return customerMapper.toDTO(customer);
    }

    @Override
    @Transactional
    public CustomerDTO updateCustomer(String factoryId, Integer customerId, CreateCustomerRequest request) {
        log.info("更新客户: factoryId={}, customerId={}", factoryId, customerId);
        Customer customer = customerRepository.findByIdAndFactoryId(customerId, factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("客户不存在"));
        // 检查名称是否与其他客户重复
        if (request.getName() != null && !request.getName().equals(customer.getName())) {
            if (customerRepository.existsByFactoryIdAndNameAndIdNot(factoryId, request.getName(), customerId)) {
                throw new BusinessException("客户名称已存在");
            }
        }
        // 更新客户信息
        customerMapper.updateEntity(customer, request);
        customer = customerRepository.save(customer);
        log.info("客户更新成功: id={}", customer.getId());
        return customerMapper.toDTO(customer);
    }

    @Override
    @Transactional
    public void deleteCustomer(String factoryId, Integer customerId) {
        log.info("删除客户: factoryId={}, customerId={}", factoryId, customerId);
        Customer customer = customerRepository.findByIdAndFactoryId(customerId, factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("客户不存在"));
        // 检查是否有关联的出货记录
        if (customerRepository.hasRelatedShipments(customerId)) {
            throw new BusinessException("客户有关联的出货记录，无法删除");
        }
        customerRepository.delete(customer);
        log.info("客户删除成功: id={}", customerId);
    }

    @Override
    public CustomerDTO getCustomerById(String factoryId, Integer customerId) {
        Customer customer = customerRepository.findByIdAndFactoryId(customerId, factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("客户不存在"));
        return customerMapper.toDTO(customer);
    }

    @Override
    public PageResponse<CustomerDTO> getCustomerList(String factoryId, PageRequest pageRequest) {
        // 创建分页请求
        org.springframework.data.domain.PageRequest pageable = org.springframework.data.domain.PageRequest.of(
                pageRequest.getPage() - 1,
                pageRequest.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        // 查询客户
        Page<Customer> customerPage = customerRepository.findByFactoryId(factoryId, pageable);
        // 转换为DTO
        List<CustomerDTO> customerDTOs = customerPage.getContent().stream()
                .map(customerMapper::toDTO)
                .collect(Collectors.toList());
        // 构建分页响应
        PageResponse<CustomerDTO> response = new PageResponse<>();
        response.setContent(customerDTOs);
        response.setPage(pageRequest.getPage());
        response.setSize(pageRequest.getSize());
        response.setTotalElements(customerPage.getTotalElements());
        response.setTotalPages(customerPage.getTotalPages());
        response.setFirst(customerPage.isFirst());
        response.setLast(customerPage.isLast());
        return response;
    }

    @Override
    public List<CustomerDTO> getActiveCustomers(String factoryId) {
        List<Customer> customers = customerRepository.findByFactoryIdAndIsActive(factoryId, true);
        return customers.stream()
                .map(customerMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CustomerDTO> searchCustomersByName(String factoryId, String keyword) {
        List<Customer> customers = customerRepository.searchByName(factoryId, keyword);
        return customers.stream()
                .map(customerMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CustomerDTO> getCustomersByType(String factoryId, String type) {
        List<Customer> customers = customerRepository.findByFactoryIdAndType(factoryId, type);
        return customers.stream()
                .map(customerMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CustomerDTO> getCustomersByIndustry(String factoryId, String industry) {
        List<Customer> customers = customerRepository.findByFactoryIdAndIndustry(factoryId, industry);
        return customers.stream()
                .map(customerMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CustomerDTO toggleCustomerStatus(String factoryId, Integer customerId, Boolean isActive) {
        log.info("切换客户状态: factoryId={}, customerId={}, isActive={}",
                factoryId, customerId, isActive);
        Customer customer = customerRepository.findByIdAndFactoryId(customerId, factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("客户不存在"));
        customer.setIsActive(isActive);
        customer.setUpdatedAt(LocalDateTime.now());
        customer = customerRepository.save(customer);
        log.info("客户状态更新成功: id={}, isActive={}", customer.getId(), isActive);
        return customerMapper.toDTO(customer);
    }

    @Override
    @Transactional
    public CustomerDTO updateCustomerRating(String factoryId, Integer customerId,
                                           Integer rating, String notes) {
        log.info("更新客户评级: factoryId={}, customerId={}, rating={}",
                factoryId, customerId, rating);
        if (rating < 1 || rating > 5) {
            throw new BusinessException("评级必须在1-5之间");
        }
        Customer customer = customerRepository.findByIdAndFactoryId(customerId, factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("客户不存在"));
        customer.setRating(rating);
        customer.setRatingNotes(notes);
        customer = customerRepository.save(customer);
        log.info("客户评级更新成功: id={}, rating={}", customer.getId(), rating);
        return customerMapper.toDTO(customer);
    }

    @Override
    @Transactional
    public CustomerDTO updateCreditLimit(String factoryId, Integer customerId, BigDecimal creditLimit) {
        log.info("更新客户信用额度: factoryId={}, customerId={}, creditLimit={}",
                factoryId, customerId, creditLimit);
        if (creditLimit.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("信用额度不能为负数");
        }
        Customer customer = customerRepository.findByIdAndFactoryId(customerId, factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("客户不存在"));
        customer.setCreditLimit(creditLimit);
        customer = customerRepository.save(customer);
        log.info("客户信用额度更新成功: id={}, creditLimit={}", customer.getId(), creditLimit);
        return customerMapper.toDTO(customer);
    }

    @Override
    @Transactional
    public CustomerDTO updateCurrentBalance(String factoryId, Integer customerId, BigDecimal balance) {
        log.info("更新客户当前余额: factoryId={}, customerId={}, balance={}",
                factoryId, customerId, balance);
        Customer customer = customerRepository.findByIdAndFactoryId(customerId, factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("客户不存在"));
        customer.setCurrentBalance(balance);
        customer = customerRepository.save(customer);
        log.info("客户余额更新成功: id={}, balance={}", customer.getId(), balance);
        return customerMapper.toDTO(customer);
    }

    @Override
    public Map<String, Object> getCustomerStatistics(String factoryId, Integer customerId) {
        Customer customer = customerRepository.findByIdAndFactoryId(customerId, factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("客户不存在"));
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("customerId", customer.getId());
        statistics.put("customerName", customer.getName());
        statistics.put("type", customer.getType());
        statistics.put("industry", customer.getIndustry());
        statistics.put("rating", customer.getRating());
        statistics.put("creditLimit", customer.getCreditLimit());
        statistics.put("currentBalance", customer.getCurrentBalance());
        statistics.put("creditAvailable", customer.getCreditLimit().subtract(customer.getCurrentBalance()));
        statistics.put("isActive", customer.getIsActive());
        // TODO: 添加订单统计、购买历史统计等信息
        statistics.put("totalOrders", 0);
        statistics.put("totalSales", BigDecimal.ZERO);
        statistics.put("averageOrderValue", BigDecimal.ZERO);
        statistics.put("lastOrderDate", null);
        return statistics;
    }

    @Override
    public List<Map<String, Object>> getCustomerPurchaseHistory(String factoryId, Integer customerId) {
        // TODO: 从订单表中获取购买历史
        List<Map<String, Object>> history = new ArrayList<>();
        return history;
    }

    @Override
    public boolean checkCustomerCodeExists(String factoryId, String customerCode) {
        return customerRepository.existsByFactoryIdAndCustomerCode(factoryId, customerCode);
    }

    @Override
    public byte[] exportCustomerList(String factoryId) {
        // TODO: 实现客户列表导出功能
        throw new UnsupportedOperationException("客户列表导出功能待实现");
    }

    @Override
    @Transactional
    public List<CustomerDTO> importCustomers(String factoryId, List<CreateCustomerRequest> requests,
                                            Integer userId) {
        log.info("批量导入客户: factoryId={}, count={}", factoryId, requests.size());
        List<CustomerDTO> importedCustomers = new ArrayList<>();
        for (CreateCustomerRequest request : requests) {
            try {
                CustomerDTO customer = createCustomer(factoryId, request, userId);
                importedCustomers.add(customer);
            } catch (Exception e) {
                log.error("导入客户失败: name={}, error={}", request.getName(), e.getMessage());
            }
        }
        log.info("批量导入完成，成功导入 {} 个客户", importedCustomers.size());
        return importedCustomers;
    }

    @Override
    public Map<Integer, Long> getCustomerRatingDistribution(String factoryId) {
        List<Object[]> distribution = customerRepository.getCustomerRatingDistribution(factoryId);
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
    public List<CustomerDTO> getCustomersWithOutstandingBalance(String factoryId) {
        List<Customer> customers = customerRepository.findCustomersWithOutstandingBalance(factoryId);
        return customers.stream()
                .map(customerMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CustomerDTO> getVIPCustomers(String factoryId, Integer limit) {
        org.springframework.data.domain.PageRequest pageable = org.springframework.data.domain.PageRequest.of(0, limit);
        List<Customer> customers = customerRepository.findTopCustomersByCreditLimit(factoryId, pageable);
        return customers.stream()
                .map(customerMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Long> getCustomerTypeDistribution(String factoryId) {
        List<Object[]> distribution = customerRepository.countByType(factoryId);
        Map<String, Long> result = new HashMap<>();
        for (Object[] row : distribution) {
            String type = (String) row[0];
            Long count = (Long) row[1];
            if (type != null) {
                result.put(type, count);
            } else {
                result.put("未分类", count);
            }
        }
        return result;
    }

    @Override
    public Map<String, Long> getCustomerIndustryDistribution(String factoryId) {
        List<Object[]> distribution = customerRepository.countByIndustry(factoryId);
        Map<String, Long> result = new HashMap<>();
        for (Object[] row : distribution) {
            String industry = (String) row[0];
            Long count = (Long) row[1];
            if (industry != null) {
                result.put(industry, count);
            } else {
                result.put("未分类", count);
            }
        }
        return result;
    }

    @Override
    public Map<String, Object> getOverallCustomerStatistics(String factoryId) {
        Map<String, Object> statistics = new HashMap<>();
        // 客户总数
        long totalCustomers = customerRepository.countByFactoryId(factoryId);
        statistics.put("totalCustomers", totalCustomers);
        // 活跃客户数
        long activeCustomers = customerRepository.countByFactoryIdAndIsActive(factoryId, true);
        statistics.put("activeCustomers", activeCustomers);
        // 平均评级
        Double averageRating = customerRepository.calculateAverageRating(factoryId);
        statistics.put("averageRating", averageRating != null ? averageRating : 0.0);
        // 总欠款
        BigDecimal totalOutstanding = customerRepository.calculateTotalOutstandingBalance(factoryId);
        statistics.put("totalOutstandingBalance", totalOutstanding != null ? totalOutstanding : BigDecimal.ZERO);
        // 总信用额度
        BigDecimal totalCredit = customerRepository.calculateTotalCreditLimit(factoryId);
        statistics.put("totalCreditLimit", totalCredit != null ? totalCredit : BigDecimal.ZERO);
        // 客户类型分布
        statistics.put("typeDistribution", getCustomerTypeDistribution(factoryId));
        // 客户行业分布
        statistics.put("industryDistribution", getCustomerIndustryDistribution(factoryId));
        // 客户评级分布
        statistics.put("ratingDistribution", getCustomerRatingDistribution(factoryId));
        return statistics;
    }
}

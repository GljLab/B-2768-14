package com.employee.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.employee.dto.EmployeeRequest;
import com.employee.entity.Department;
import com.employee.entity.Employee;
import com.employee.mapper.DepartmentMapper;
import com.employee.mapper.EmployeeMapper;
import com.employee.utils.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeMapper employeeMapper;
    private final DepartmentMapper departmentMapper;
    
    public IPage<Employee> getEmployeeList(Integer page, Integer size, String keyword, Long departmentId) {
        Page<Employee> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Employee::getName, keyword)
                             .or().like(Employee::getEmail, keyword)
                             .or().like(Employee::getPhone, keyword)
                             .or().like(Employee::getDepartment, keyword)
                             .or().like(Employee::getPosition, keyword));
        }
        
        if (departmentId != null) {
            wrapper.eq(Employee::getDepartmentId, departmentId);
        }
        
        wrapper.orderByDesc(Employee::getCreatedAt);
        return employeeMapper.selectPage(pageParam, wrapper);
    }
    
    public IPage<Employee> getEmployeeListByDepartmentIds(Integer page, Integer size, String keyword, List<Long> departmentIds) {
        Page<Employee> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Employee::getName, keyword)
                             .or().like(Employee::getEmail, keyword)
                             .or().like(Employee::getPhone, keyword)
                             .or().like(Employee::getDepartment, keyword)
                             .or().like(Employee::getPosition, keyword));
        }
        
        if (!CollectionUtils.isEmpty(departmentIds)) {
            wrapper.in(Employee::getDepartmentId, departmentIds);
        }
        
        wrapper.orderByDesc(Employee::getCreatedAt);
        return employeeMapper.selectPage(pageParam, wrapper);
    }
    
    public Employee getEmployeeById(Long id) {
        return employeeMapper.selectById(id);
    }
    
    @Transactional
    public void createEmployee(EmployeeRequest request) {
        LambdaQueryWrapper<Employee> emailWrapper = new LambdaQueryWrapper<>();
        emailWrapper.eq(Employee::getEmail, request.getEmail());
        if (employeeMapper.selectCount(emailWrapper) > 0) {
            throw new IllegalArgumentException("邮箱已存在");
        }
        
        LambdaQueryWrapper<Employee> phoneWrapper = new LambdaQueryWrapper<>();
        phoneWrapper.eq(Employee::getPhone, request.getPhone());
        if (employeeMapper.selectCount(phoneWrapper) > 0) {
            throw new IllegalArgumentException("手机号已存在");
        }
        
        Employee employee = new Employee();
        BeanUtils.copyProperties(request, employee);
        
        if (request.getDepartmentId() != null) {
            Department dept = departmentMapper.selectById(request.getDepartmentId());
            if (dept != null) {
                employee.setDepartment(dept.getName());
            }
        }
        
        if (employee.getStatus() == null) {
            employee.setStatus(1);
        }
        employee.setIsActive(0);
        employee.setLockStatus(0);
        String initialPassword = getInitialPassword(request.getPhone());
        employee.setPassword(PasswordUtil.encode(initialPassword));
        employeeMapper.insert(employee);
    }
    
    @Transactional
    public void updateEmployee(Long id, EmployeeRequest request) {
        LambdaQueryWrapper<Employee> emailWrapper = new LambdaQueryWrapper<>();
        emailWrapper.eq(Employee::getEmail, request.getEmail())
                   .ne(Employee::getId, id);
        if (employeeMapper.selectCount(emailWrapper) > 0) {
            throw new IllegalArgumentException("邮箱已被其他员工使用");
        }
        
        LambdaQueryWrapper<Employee> phoneWrapper = new LambdaQueryWrapper<>();
        phoneWrapper.eq(Employee::getPhone, request.getPhone())
                   .ne(Employee::getId, id);
        if (employeeMapper.selectCount(phoneWrapper) > 0) {
            throw new IllegalArgumentException("手机号已被其他员工使用");
        }
        
        Employee employee = new Employee();
        employee.setId(id);
        BeanUtils.copyProperties(request, employee);
        
        if (request.getDepartmentId() != null) {
            Department dept = departmentMapper.selectById(request.getDepartmentId());
            if (dept != null) {
                employee.setDepartment(dept.getName());
            }
        }
        
        employeeMapper.updateById(employee);
    }
    
    @Transactional
    public void batchUpdateDepartment(List<Long> employeeIds, Long departmentId) {
        if (CollectionUtils.isEmpty(employeeIds)) {
            throw new IllegalArgumentException("员工ID列表不能为空");
        }
        
        Department dept = departmentMapper.selectById(departmentId);
        if (dept == null) {
            throw new IllegalArgumentException("部门不存在");
        }
        
        for (Long employeeId : employeeIds) {
            Employee employee = new Employee();
            employee.setId(employeeId);
            employee.setDepartmentId(departmentId);
            employee.setDepartment(dept.getName());
            employeeMapper.updateById(employee);
        }
    }
    
    public void deleteEmployee(Long id) {
        employeeMapper.deleteById(id);
    }
    
    public void resetPassword(Long id) {
        Employee employee = employeeMapper.selectById(id);
        if (employee == null) {
            throw new IllegalArgumentException("员工不存在");
        }
        
        String initialPassword = getInitialPassword(employee.getPhone());
        employee.setPassword(PasswordUtil.encode(initialPassword));
        employee.setIsActive(0);
        employeeMapper.updateById(employee);
    }
    
    private String getInitialPassword(String phone) {
        if (phone == null || phone.length() < 6) {
            return "123456";
        }
        return phone.substring(phone.length() - 6);
    }
}

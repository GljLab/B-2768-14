package com.employee.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.employee.entity.Employee;
import com.employee.mapper.EmployeeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmployeeProfileService {
    private final EmployeeMapper employeeMapper;
    
    public Employee getProfile(Long employeeId) {
        return employeeMapper.selectById(employeeId);
    }
    
    public String updateProfile(Long employeeId, Employee employee) {
        Employee existing = employeeMapper.selectById(employeeId);
        if (existing == null) {
            throw new IllegalArgumentException("员工不存在");
        }
        
        String message = "资料更新成功";
        
        if (employee.getPhone() != null && !employee.getPhone().equals(existing.getPhone())) {
            LambdaQueryWrapper<Employee> phoneWrapper = new LambdaQueryWrapper<>();
            phoneWrapper.eq(Employee::getPhone, employee.getPhone())
                       .ne(Employee::getId, employeeId);
            if (employeeMapper.selectCount(phoneWrapper) > 0) {
                throw new IllegalArgumentException("手机号已被其他员工使用");
            }
            existing.setPhone(employee.getPhone());
        }
        
        boolean emailChanged = false;
        if (employee.getEmail() != null && !employee.getEmail().equals(existing.getEmail())) {
            LambdaQueryWrapper<Employee> emailWrapper = new LambdaQueryWrapper<>();
            emailWrapper.eq(Employee::getEmail, employee.getEmail())
                       .ne(Employee::getId, employeeId);
            if (employeeMapper.selectCount(emailWrapper) > 0) {
                throw new IllegalArgumentException("邮箱已被其他员工使用");
            }
            existing.setEmail(employee.getEmail());
            emailChanged = true;
        }
        
        employeeMapper.updateById(existing);
        
        if (emailChanged) {
            message = "邮箱已更新，下次请使用新邮箱登录";
        }
        
        return message;
    }
    
    public void updateAvatar(Long employeeId, String avatarUrl) {
        Employee employee = new Employee();
        employee.setId(employeeId);
        employee.setAvatar(avatarUrl);
        employeeMapper.updateById(employee);
    }
}

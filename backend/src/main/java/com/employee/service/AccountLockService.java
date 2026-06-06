package com.employee.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.employee.entity.AdminUser;
import com.employee.entity.Employee;
import com.employee.mapper.AdminUserMapper;
import com.employee.mapper.EmployeeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class AccountLockService {
    
    public static final int MAX_FAILED_ATTEMPTS = 5;
    public static final int LOCK_DURATION_MINUTES = 30;
    public static final int USER_TYPE_ADMIN = 1;
    public static final int USER_TYPE_EMPLOYEE = 2;
    
    private final EmployeeMapper employeeMapper;
    private final AdminUserMapper adminUserMapper;
    
    private final Map<String, Integer> failedAttempts = new ConcurrentHashMap<>();
    
    public void recordFailedAttempt(String username, int userType) {
        String key = getCacheKey(username, userType);
        int attempts = failedAttempts.getOrDefault(key, 0) + 1;
        failedAttempts.put(key, attempts);
        
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            lockAccount(username, userType);
        }
    }
    
    public void resetFailedAttempts(String username, int userType) {
        String key = getCacheKey(username, userType);
        failedAttempts.remove(key);
    }
    
    public int getRemainingAttempts(String username, int userType) {
        String key = getCacheKey(username, userType);
        int attempts = failedAttempts.getOrDefault(key, 0);
        return MAX_FAILED_ATTEMPTS - attempts;
    }
    
    public boolean isAccountLocked(String username, int userType) {
        if (userType == USER_TYPE_EMPLOYEE) {
            Employee employee = employeeMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Employee>()
                    .eq(Employee::getEmail, username)
            );
            if (employee != null && employee.getLockStatus() != null && employee.getLockStatus() == 1) {
                if (employee.getUnlockTime() != null && LocalDateTime.now().isAfter(employee.getUnlockTime())) {
                    unlockEmployeeAccount(employee.getId());
                    return false;
                }
                return true;
            }
        } else if (userType == USER_TYPE_ADMIN) {
            AdminUser admin = adminUserMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AdminUser>()
                    .eq(AdminUser::getUsername, username)
            );
            if (admin != null && admin.getLockStatus() != null && admin.getLockStatus() == 1) {
                if (admin.getUnlockTime() != null && LocalDateTime.now().isAfter(admin.getUnlockTime())) {
                    unlockAdminAccount(admin.getId());
                    return false;
                }
                return true;
            }
        }
        return false;
    }
    
    public LocalDateTime getUnlockTime(String username, int userType) {
        if (userType == USER_TYPE_EMPLOYEE) {
            Employee employee = employeeMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Employee>()
                    .eq(Employee::getEmail, username)
            );
            return employee != null ? employee.getUnlockTime() : null;
        } else if (userType == USER_TYPE_ADMIN) {
            AdminUser admin = adminUserMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AdminUser>()
                    .eq(AdminUser::getUsername, username)
            );
            return admin != null ? admin.getUnlockTime() : null;
        }
        return null;
    }
    
    private void lockAccount(String username, int userType) {
        LocalDateTime lockTime = LocalDateTime.now();
        LocalDateTime unlockTime = lockTime.plusMinutes(LOCK_DURATION_MINUTES);
        
        if (userType == USER_TYPE_EMPLOYEE) {
            employeeMapper.update(
                new LambdaUpdateWrapper<Employee>()
                    .eq(Employee::getEmail, username)
                    .set(Employee::getLockStatus, 1)
                    .set(Employee::getLockTime, lockTime)
                    .set(Employee::getUnlockTime, unlockTime)
            );
            log.info("员工账号已锁定: {}, 解锁时间: {}", username, unlockTime);
        } else if (userType == USER_TYPE_ADMIN) {
            adminUserMapper.update(
                new LambdaUpdateWrapper<AdminUser>()
                    .eq(AdminUser::getUsername, username)
                    .set(AdminUser::getLockStatus, 1)
                    .set(AdminUser::getLockTime, lockTime)
                    .set(AdminUser::getUnlockTime, unlockTime)
            );
            log.info("管理员账号已锁定: {}, 解锁时间: {}", username, unlockTime);
        }
    }
    
    public void unlockEmployeeAccount(Long employeeId) {
        employeeMapper.update(
            new LambdaUpdateWrapper<Employee>()
                .eq(Employee::getId, employeeId)
                .set(Employee::getLockStatus, 0)
                .set(Employee::getLockTime, null)
                .set(Employee::getUnlockTime, null)
        );
        Employee employee = employeeMapper.selectById(employeeId);
        if (employee != null) {
            resetFailedAttempts(employee.getEmail(), USER_TYPE_EMPLOYEE);
        }
        log.info("员工账号已手动解锁: employeeId={}", employeeId);
    }
    
    public void unlockAdminAccount(Long adminId) {
        adminUserMapper.update(
            new LambdaUpdateWrapper<AdminUser>()
                .eq(AdminUser::getId, adminId)
                .set(AdminUser::getLockStatus, 0)
                .set(AdminUser::getLockTime, null)
                .set(AdminUser::getUnlockTime, null)
        );
        AdminUser admin = adminUserMapper.selectById(adminId);
        if (admin != null) {
            resetFailedAttempts(admin.getUsername(), USER_TYPE_ADMIN);
        }
        log.info("管理员账号已手动解锁: adminId={}", adminId);
    }
    
    private String getCacheKey(String username, int userType) {
        return userType + ":" + username;
    }
}

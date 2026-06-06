package com.employee.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.employee.entity.Employee;
import com.employee.mapper.EmployeeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BirthdaySchedulerService {

    private final EmployeeMapper employeeMapper;
    private final BirthdayWishService wishService;

    @Scheduled(cron = "0 0 1 * * ?")
    public void scanBirthdayEmployees() {
        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int day = today.getDayOfMonth();

        QueryWrapper<Employee> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1)
                .apply("MONTH(birthday_date) = {0} AND DAY(birthday_date) = {1}", month, day);

        List<Employee> employees = employeeMapper.selectList(wrapper);
        for (Employee employee : employees) {
            wishService.sendSystemWish(employee);
        }

        boolean isFeb28NonLeap = month == 2 && day == 28 && !today.isLeapYear();
        if (isFeb28NonLeap) {
            QueryWrapper<Employee> feb29Wrapper = new QueryWrapper<>();
            feb29Wrapper.eq("status", 1)
                    .apply("MONTH(birthday_date) = 2 AND DAY(birthday_date) = 29");
            List<Employee> feb29Employees = employeeMapper.selectList(feb29Wrapper);
            for (Employee employee : feb29Employees) {
                wishService.sendSystemWish(employee);
            }
        }
    }
}

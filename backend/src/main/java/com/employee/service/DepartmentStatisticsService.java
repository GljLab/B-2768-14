package com.employee.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.employee.dto.DepartmentStatisticsVO;
import com.employee.entity.Department;
import com.employee.entity.Employee;
import com.employee.mapper.DepartmentMapper;
import com.employee.mapper.EmployeeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentStatisticsService {
    private final DepartmentMapper departmentMapper;
    private final EmployeeMapper employeeMapper;
    
    public List<DepartmentStatisticsVO> getDepartmentStatistics() {
        List<Department> departments = departmentMapper.selectList(null);
        List<Employee> employees = employeeMapper.selectList(
            new LambdaQueryWrapper<Employee>()
        );
        
        Map<Long, Department> deptMap = departments.stream()
            .collect(Collectors.toMap(Department::getId, d -> d));
        
        Map<Long, List<Employee>> empByDept = employees.stream()
            .filter(e -> e.getDepartmentId() != null)
            .collect(Collectors.groupingBy(Employee::getDepartmentId));
        
        List<DepartmentStatisticsVO> result = new ArrayList<>();
        
        for (Department dept : departments) {
            DepartmentStatisticsVO vo = new DepartmentStatisticsVO();
            vo.setDepartmentId(dept.getId());
            vo.setDepartmentName(dept.getName());
            
            List<Employee> deptEmps = empByDept.getOrDefault(dept.getId(), new ArrayList<>());
            
            int activeCount = (int) deptEmps.stream().filter(e -> e.getStatus() == 1).count();
            int inactiveCount = (int) deptEmps.stream().filter(e -> e.getStatus() == 0).count();
            
            vo.setActiveCount(activeCount);
            vo.setInactiveCount(inactiveCount);
            vo.setTotalCount(deptEmps.size());
            
            double avgTenure = deptEmps.stream()
                .filter(e -> e.getStatus() == 1 && e.getHireDate() != null)
                .mapToDouble(e -> {
                    Period period = Period.between(e.getHireDate(), LocalDate.now());
                    return period.toTotalMonths();
                })
                .average()
                .orElse(0.0);
            
            vo.setAvgTenureMonths(BigDecimal.valueOf(avgTenure).setScale(1, RoundingMode.HALF_UP));
            
            result.add(vo);
        }
        
        return result;
    }
    
    public Map<String, Object> getMonthlyChangeStatistics(int months) {
        Map<String, Object> result = new HashMap<>();
        List<String> labels = new ArrayList<>();
        List<Integer> joinCounts = new ArrayList<>();
        List<Integer> leaveCounts = new ArrayList<>();
        
        LocalDate now = LocalDate.now();
        
        for (int i = months - 1; i >= 0; i--) {
            LocalDate monthStart = now.minusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
            String label = monthStart.getYear() + "-" + String.format("%02d", monthStart.getMonthValue());
            labels.add(label);
            
            int joinCount = countEmployeesByHireDate(monthStart, monthEnd);
            int leaveCount = countEmployeesByLeaveDate(monthStart, monthEnd);
            
            joinCounts.add(joinCount);
            leaveCounts.add(leaveCount);
        }
        
        result.put("labels", labels);
        result.put("joinCounts", joinCounts);
        result.put("leaveCounts", leaveCounts);
        
        return result;
    }
    
    private int countEmployeesByHireDate(LocalDate start, LocalDate end) {
        return Math.toIntExact(employeeMapper.selectCount(
            new LambdaQueryWrapper<Employee>()
                .between(Employee::getHireDate, start, end)
        ));
    }
    
    private int countEmployeesByLeaveDate(LocalDate start, LocalDate end) {
        return 0;
    }
}

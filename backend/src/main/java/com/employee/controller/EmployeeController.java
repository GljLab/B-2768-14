package com.employee.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.employee.common.Result;
import com.employee.dto.EmployeeRequest;
import com.employee.entity.Employee;
import com.employee.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Tag(name = "员工管理", description = "员工增删改查相关接口")
public class EmployeeController {
    private final EmployeeService employeeService;
    
    @GetMapping
    @Operation(summary = "获取员工列表", description = "分页查询员工列表,支持关键词搜索和部门筛选")
    public Result<IPage<Employee>> getEmployeeList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "部门ID") @RequestParam(required = false) Long departmentId) {
        IPage<Employee> employeeList = employeeService.getEmployeeList(page, size, keyword, departmentId);
        return Result.success(employeeList);
    }
    
    @GetMapping("/by-department-ids")
    @Operation(summary = "根据部门ID列表获取员工", description = "根据多个部门ID查询员工列表")
    public Result<IPage<Employee>> getEmployeeListByDepartmentIds(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "部门ID列表") @RequestParam(required = false) List<Long> departmentIds) {
        IPage<Employee> employeeList = employeeService.getEmployeeListByDepartmentIds(page, size, keyword, departmentIds);
        return Result.success(employeeList);
    }
    
    @PostMapping("/batch-department")
    @Operation(summary = "批量调配部门", description = "将多名员工批量调配到指定部门")
    public Result<String> batchUpdateDepartment(@Valid @RequestBody com.employee.dto.BatchDepartmentRequest request) {
        try {
            employeeService.batchUpdateDepartment(request.getEmployeeIds(), request.getDepartmentId());
            return Result.success("批量调配成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取员工详情", description = "根据ID获取员工详情")
    public Result<Employee> getEmployeeById(@Parameter(description = "员工ID") @PathVariable Long id) {
        Employee employee = employeeService.getEmployeeById(id);
        if (employee != null) {
            return Result.success(employee);
        } else {
            return Result.error("员工不存在");
        }
    }
    
    @PostMapping
    @Operation(summary = "创建员工", description = "新增员工信息")
    public Result<String> createEmployee(@Valid @RequestBody EmployeeRequest request) {
        try {
            employeeService.createEmployee(request);
            return Result.success("员工创建成功");
        } catch (Exception e) {
            return Result.error("员工创建失败: " + e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "更新员工", description = "根据ID更新员工信息")
    public Result<String> updateEmployee(
            @Parameter(description = "员工ID") @PathVariable Long id,
            @Valid @RequestBody EmployeeRequest request) {
        try {
            employeeService.updateEmployee(id, request);
            return Result.success("员工更新成功");
        } catch (Exception e) {
            return Result.error("员工更新失败: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除员工", description = "根据ID删除员工")
    public Result<String> deleteEmployee(@Parameter(description = "员工ID") @PathVariable Long id) {
        try {
            employeeService.deleteEmployee(id);
            return Result.success("员工删除成功");
        } catch (Exception e) {
            return Result.error("员工删除失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/{id}/reset-password")
    @Operation(summary = "重置员工密码", description = "将员工密码重置为手机号后6位，并标记为待激活")
    public Result<String> resetPassword(@Parameter(description = "员工ID") @PathVariable Long id) {
        try {
            employeeService.resetPassword(id);
            return Result.success("密码重置成功");
        } catch (Exception e) {
            return Result.error("密码重置失败: " + e.getMessage());
        }
    }
}

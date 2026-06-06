package com.employee.controller;

import com.employee.common.Result;
import com.employee.dto.DepartmentRequest;
import com.employee.entity.Department;
import com.employee.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@Tag(name = "部门管理", description = "部门组织架构相关接口")
public class DepartmentController {
    private final DepartmentService departmentService;
    
    @GetMapping("/tree")
    @Operation(summary = "获取部门树", description = "获取完整的部门层级树状结构")
    public Result<List<Department>> getDepartmentTree() {
        List<Department> tree = departmentService.getDepartmentTree();
        return Result.success(tree);
    }
    
    @GetMapping("/flat")
    @Operation(summary = "获取扁平化部门列表", description = "获取所有部门的扁平化列表")
    public Result<List<Department>> getFlatDepartmentList() {
        List<Department> list = departmentService.getFlatDepartmentList();
        return Result.success(list);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取部门详情", description = "根据ID获取部门详情")
    public Result<Department> getDepartmentById(@Parameter(description = "部门ID") @PathVariable Long id) {
        Department department = departmentService.getDepartmentById(id);
        if (department != null) {
            return Result.success(department);
        } else {
            return Result.error("部门不存在");
        }
    }
    
    @PostMapping
    @Operation(summary = "创建部门", description = "新增部门信息")
    public Result<Department> createDepartment(@Valid @RequestBody DepartmentRequest request) {
        try {
            Department department = departmentService.createDepartment(request);
            return Result.success(department);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "更新部门", description = "根据ID更新部门信息")
    public Result<String> updateDepartment(
            @Parameter(description = "部门ID") @PathVariable Long id,
            @Valid @RequestBody DepartmentRequest request) {
        try {
            departmentService.updateDepartment(id, request);
            return Result.success("部门更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除部门", description = "根据ID删除部门")
    public Result<String> deleteDepartment(@Parameter(description = "部门ID") @PathVariable Long id) {
        try {
            departmentService.deleteDepartment(id);
            return Result.success("部门删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/{id}/descendant-ids")
    @Operation(summary = "获取部门及所有子部门ID", description = "获取指定部门及其所有后代部门的ID列表")
    public Result<List<Long>> getDescendantIds(@Parameter(description = "部门ID") @PathVariable Long id) {
        List<Long> ids = departmentService.getAllDescendantIds(id);
        return Result.success(ids);
    }
}

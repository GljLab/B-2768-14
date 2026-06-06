package com.employee.controller;

import com.employee.common.Result;
import com.employee.dto.AnnouncementCategoryRequest;
import com.employee.entity.AnnouncementCategory;
import com.employee.service.AnnouncementCategoryService;
import com.employee.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/announcement-categories")
@RequiredArgsConstructor
@Tag(name = "信息类别管理", description = "信息类别增删改查接口")
public class AnnouncementCategoryController {
    private final AnnouncementCategoryService categoryService;
    private final JwtUtil jwtUtil;
    
    @GetMapping
    @Operation(summary = "获取所有类别", description = "获取所有信息类别列表")
    public Result<List<AnnouncementCategory>> getAllCategories() {
        return Result.success(categoryService.getAllCategories());
    }
    
    @GetMapping("/enabled")
    @Operation(summary = "获取启用的类别", description = "获取所有启用的信息类别列表")
    public Result<List<AnnouncementCategory>> getEnabledCategories() {
        return Result.success(categoryService.getEnabledCategories());
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取类别详情", description = "根据ID获取类别详情")
    public Result<AnnouncementCategory> getCategoryById(@PathVariable Long id) {
        AnnouncementCategory category = categoryService.getById(id);
        if (category == null) {
            return Result.error("类别不存在");
        }
        return Result.success(category);
    }
    
    @PostMapping
    @Operation(summary = "创建类别", description = "创建新的信息类别")
    public Result<AnnouncementCategory> createCategory(@Valid @RequestBody AnnouncementCategoryRequest request) {
        return Result.success(categoryService.createCategory(request));
    }
    
    @PutMapping
    @Operation(summary = "更新类别", description = "更新信息类别")
    public Result<AnnouncementCategory> updateCategory(@Valid @RequestBody AnnouncementCategoryRequest request) {
        return Result.success(categoryService.updateCategory(request));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除类别", description = "删除信息类别")
    public Result<String> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success("删除成功");
    }
}

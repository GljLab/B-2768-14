package com.employee.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.employee.dto.AnnouncementCategoryRequest;
import com.employee.entity.AnnouncementCategory;
import com.employee.mapper.AnnouncementCategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnnouncementCategoryService {
    private final AnnouncementCategoryMapper categoryMapper;
    
    public List<AnnouncementCategory> getAllCategories() {
        return categoryMapper.selectAll();
    }
    
    public List<AnnouncementCategory> getEnabledCategories() {
        return categoryMapper.selectAllEnabled();
    }
    
    public AnnouncementCategory getById(Long id) {
        return categoryMapper.selectById(id);
    }
    
    public AnnouncementCategory createCategory(AnnouncementCategoryRequest request) {
        LambdaQueryWrapper<AnnouncementCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AnnouncementCategory::getCategoryCode, request.getCategoryCode());
        if (categoryMapper.selectCount(wrapper) > 0) {
            throw new IllegalArgumentException("类别编码已存在");
        }
        
        AnnouncementCategory category = new AnnouncementCategory();
        BeanUtils.copyProperties(request, category);
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        categoryMapper.insert(category);
        return category;
    }
    
    public AnnouncementCategory updateCategory(AnnouncementCategoryRequest request) {
        AnnouncementCategory category = categoryMapper.selectById(request.getId());
        if (category == null) {
            throw new IllegalArgumentException("类别不存在");
        }
        
        if (!category.getCategoryCode().equals(request.getCategoryCode())) {
            LambdaQueryWrapper<AnnouncementCategory> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AnnouncementCategory::getCategoryCode, request.getCategoryCode());
            if (categoryMapper.selectCount(wrapper) > 0) {
                throw new IllegalArgumentException("类别编码已存在");
            }
        }
        
        BeanUtils.copyProperties(request, category);
        categoryMapper.updateById(category);
        return category;
    }
    
    public void deleteCategory(Long id) {
        AnnouncementCategory category = categoryMapper.selectById(id);
        if (category == null) {
            throw new IllegalArgumentException("类别不存在");
        }
        categoryMapper.deleteById(id);
    }
}

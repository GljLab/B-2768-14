package com.employee.controller;

import com.employee.common.Result;
import com.employee.entity.Announcement;
import com.employee.entity.AnnouncementCategory;
import com.employee.service.AnnouncementCategoryService;
import com.employee.service.AnnouncementService;
import com.employee.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employee/announcements")
@RequiredArgsConstructor
@Tag(name = "员工信息查看", description = "员工端信息查看与确认接口")
public class EmployeeAnnouncementController {
    private final AnnouncementService announcementService;
    private final AnnouncementCategoryService categoryService;
    private final JwtUtil jwtUtil;
    
    @GetMapping("/categories")
    @Operation(summary = "获取信息类别", description = "获取所有启用的信息类别")
    public Result<List<AnnouncementCategory>> getCategories() {
        return Result.success(categoryService.getEnabledCategories());
    }
    
    @GetMapping
    @Operation(summary = "获取信息列表", description = "获取员工可见的信息列表")
    public Result<List<Announcement>> getAnnouncements(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        Long employeeId = jwtUtil.getEmployeeIdFromToken(token);
        return Result.success(announcementService.getEmployeeAnnouncements(employeeId));
    }
    
    @GetMapping("/archived")
    @Operation(summary = "获取归档信息", description = "获取已过期的归档信息列表")
    public Result<List<Announcement>> getArchivedAnnouncements(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        Long employeeId = jwtUtil.getEmployeeIdFromToken(token);
        return Result.success(announcementService.getArchivedAnnouncements(employeeId));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取信息详情", description = "查看信息详情并自动标记为已阅")
    public Result<Announcement> getAnnouncementDetail(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        String token = authHeader.substring(7);
        Long employeeId = jwtUtil.getEmployeeIdFromToken(token);
        
        announcementService.markAsRead(employeeId, id);
        
        Announcement announcement = announcementService.getAnnouncementDetail(id);
        if (announcement == null) {
            return Result.error("信息不存在");
        }
        return Result.success(announcement);
    }
    
    @PostMapping("/{id}/confirm")
    @Operation(summary = "确认信息", description = "确认已阅读信息（用于强制确认模式）")
    public Result<String> confirmAnnouncement(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        String token = authHeader.substring(7);
        Long employeeId = jwtUtil.getEmployeeIdFromToken(token);
        announcementService.markAsConfirmed(employeeId, id);
        return Result.success("确认成功");
    }
    
    @GetMapping("/unread-count")
    @Operation(summary = "获取待阅数量", description = "获取员工待阅信息数量")
    public Result<Map<String, Object>> getUnreadCount(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        Long employeeId = jwtUtil.getEmployeeIdFromToken(token);
        
        Map<String, Object> result = new HashMap<>();
        result.put("unreadCount", announcementService.getUnreadCount(employeeId));
        return Result.success(result);
    }
    
    @PostMapping("/clear-unread")
    @Operation(summary = "清空待阅标记", description = "一键清空所有待阅标记")
    public Result<String> clearAllUnread(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        Long employeeId = jwtUtil.getEmployeeIdFromToken(token);
        announcementService.clearAllUnread(employeeId);
        return Result.success("清空成功");
    }
    
    @GetMapping("/unconfirmed")
    @Operation(summary = "获取待确认信息", description = "获取需要强制确认但尚未确认的信息列表")
    public Result<List<Announcement>> getUnconfirmedAnnouncements(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        Long employeeId = jwtUtil.getEmployeeIdFromToken(token);
        return Result.success(announcementService.getUnconfirmedAnnouncements(employeeId));
    }
}

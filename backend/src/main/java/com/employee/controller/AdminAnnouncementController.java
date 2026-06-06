package com.employee.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.employee.common.Result;
import com.employee.dto.AnnouncementQueryRequest;
import com.employee.dto.AnnouncementRequest;
import com.employee.dto.AnnouncementStatisticsVO;
import com.employee.entity.Announcement;
import com.employee.service.AnnouncementService;
import com.employee.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/announcements")
@RequiredArgsConstructor
@Tag(name = "管理员信息管理", description = "管理员端信息发布与管理接口")
public class AdminAnnouncementController {
    private final AnnouncementService announcementService;
    private final JwtUtil jwtUtil;
    
    @GetMapping
    @Operation(summary = "查询信息列表", description = "多条件查询信息列表")
    public Result<IPage<Announcement>> queryAnnouncements(AnnouncementQueryRequest request) {
        return Result.success(announcementService.queryAnnouncements(request));
    }
    
    @GetMapping("/my")
    @Operation(summary = "我发布的信息", description = "获取当前管理员发布的信息列表")
    public Result<IPage<Announcement>> getMyAnnouncements(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        String token = authHeader.substring(7);
        Long adminId = jwtUtil.getAdminIdFromToken(token);
        return Result.success(announcementService.getPublisherAnnouncements(adminId, pageNum, pageSize));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取信息详情", description = "获取信息详细内容")
    public Result<Announcement> getAnnouncementDetail(@PathVariable Long id) {
        Announcement announcement = announcementService.getAnnouncementDetail(id);
        if (announcement == null) {
            return Result.error("信息不存在");
        }
        return Result.success(announcement);
    }
    
    @PostMapping
    @Operation(summary = "发布信息", description = "创建并发布新信息")
    public Result<Announcement> createAnnouncement(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody AnnouncementRequest request) {
        String token = authHeader.substring(7);
        Long adminId = jwtUtil.getAdminIdFromToken(token);
        return Result.success(announcementService.createAnnouncement(adminId, "管理员", request));
    }
    
    @PutMapping
    @Operation(summary = "更新信息", description = "修改已发布或待发布的信息")
    public Result<Announcement> updateAnnouncement(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody AnnouncementRequest request) {
        String token = authHeader.substring(7);
        Long adminId = jwtUtil.getAdminIdFromToken(token);
        return Result.success(announcementService.updateAnnouncement(adminId, request));
    }
    
    @PostMapping("/{id}/withdraw")
    @Operation(summary = "撤回信息", description = "撤回已发布的信息")
    public Result<String> withdrawAnnouncement(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        String token = authHeader.substring(7);
        Long adminId = jwtUtil.getAdminIdFromToken(token);
        announcementService.withdrawAnnouncement(adminId, id);
        return Result.success("撤回成功");
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除信息", description = "彻底删除信息")
    public Result<String> deleteAnnouncement(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        String token = authHeader.substring(7);
        Long adminId = jwtUtil.getAdminIdFromToken(token);
        announcementService.deleteAnnouncement(adminId, id);
        return Result.success("删除成功");
    }
    
    @GetMapping("/{id}/statistics")
    @Operation(summary = "获取信息统计", description = "获取信息阅览统计数据")
    public Result<AnnouncementStatisticsVO> getStatistics(@PathVariable Long id) {
        return Result.success(announcementService.getStatistics(id));
    }
}

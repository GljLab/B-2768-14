package com.employee.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.employee.common.Result;
import com.employee.entity.Feedback;
import com.employee.service.FeedbackService;
import com.employee.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/feedbacks")
@RequiredArgsConstructor
@Tag(name = "管理员意见箱管理", description = "管理员端意见箱管理接口")
public class AdminFeedbackController {
    private final FeedbackService feedbackService;
    private final JwtUtil jwtUtil;

    @GetMapping
    @Operation(summary = "获取意见列表", description = "获取所有意见列表，支持按分类和阅读状态筛选")
    public Result<IPage<Feedback>> getFeedbackList(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer isRead) {
        String token = authHeader.substring(7);
        jwtUtil.getAdminIdFromToken(token);
        return Result.success(feedbackService.getFeedbackList(pageNum, pageSize, category, isRead));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取意见详情", description = "获取意见详细内容")
    public Result<Feedback> getFeedbackDetail(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        String token = authHeader.substring(7);
        jwtUtil.getAdminIdFromToken(token);
        Feedback feedback = feedbackService.getFeedbackDetail(id);
        if (feedback == null) {
            return Result.error("意见不存在");
        }
        return Result.success(feedback);
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "标记已读", description = "将意见标记为已读")
    public Result<String> markAsRead(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        String token = authHeader.substring(7);
        jwtUtil.getAdminIdFromToken(token);
        feedbackService.markAsRead(id);
        return Result.success("标记成功");
    }
}

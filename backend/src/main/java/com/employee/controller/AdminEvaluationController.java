package com.employee.controller;

import com.employee.common.Result;
import com.employee.dto.*;
import com.employee.entity.*;
import com.employee.service.AdminEvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/evaluations")
@RequiredArgsConstructor
@Tag(name = "管理员-评价管理", description = "管理员评价员工和管理评价记录")
public class AdminEvaluationController {
    private final AdminEvaluationService adminEvaluationService;

    @GetMapping("/{cycleId}/employee/{employeeId}")
    @Operation(summary = "获取管理员对员工的评价", description = "查看或编辑管理员对员工的评价")
    public Result<AdminEvaluation> getAdminEvaluation(
            @Parameter(description = "周期ID") @PathVariable Long cycleId,
            @Parameter(description = "员工ID") @PathVariable Long employeeId) {
        AdminEvaluation eval = adminEvaluationService.getAdminEvaluation(cycleId, employeeId);
        return Result.success(eval);
    }

    @PostMapping
    @Operation(summary = "提交管理员评价", description = "管理员提交对员工的评价")
    public Result<String> submitAdminEvaluation(@RequestBody AdminEvaluationRequest request, HttpServletRequest httpRequest) {
        try {
            Long adminId = (Long) httpRequest.getAttribute("adminId");
            String adminName = (String) httpRequest.getAttribute("username");
            adminEvaluationService.submitAdminEvaluation(request, adminId, adminName);
            return Result.success("评价提交成功");
        } catch (Exception e) {
            return Result.error("提交失败: " + e.getMessage());
        }
    }

    @GetMapping("/{cycleId}/colleague-evaluations")
    @Operation(summary = "获取所有同事评价记录", description = "管理员查看所有同事评价记录")
    public Result<List<ColleagueEvaluation>> getAllColleagueEvaluations(@Parameter(description = "周期ID") @PathVariable Long cycleId) {
        List<ColleagueEvaluation> evaluations = adminEvaluationService.getAllColleagueEvaluations(cycleId);
        return Result.success(evaluations);
    }

    @GetMapping("/colleague-evaluation/{evaluationId}/dimension-scores")
    @Operation(summary = "获取同事评价维度得分", description = "查看某条同事评价的各维度得分")
    public Result<List<ColleagueDimensionScore>> getDimensionScores(@Parameter(description = "评价ID") @PathVariable Long evaluationId) {
        List<ColleagueDimensionScore> scores = adminEvaluationService.getDimensionScores(evaluationId);
        return Result.success(scores);
    }

    @DeleteMapping("/colleague-evaluation/{evaluationId}")
    @Operation(summary = "删除同事评价", description = "管理员删除不合理的同事评价")
    public Result<String> deleteColleagueEvaluation(@Parameter(description = "评价ID") @PathVariable Long evaluationId, HttpServletRequest httpRequest) {
        try {
            Long adminId = (Long) httpRequest.getAttribute("adminId");
            adminEvaluationService.deleteColleagueEvaluation(evaluationId, adminId);
            return Result.success("评价已删除");
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    @GetMapping("/{cycleId}/statistics")
    @Operation(summary = "获取评价统计数据", description = "获取评价周期的整体数据统计")
    public Result<EvaluationStatisticsVO> getStatistics(@Parameter(description = "周期ID") @PathVariable Long cycleId) {
        EvaluationStatisticsVO statistics = adminEvaluationService.getStatistics(cycleId);
        return Result.success(statistics);
    }

    @GetMapping("/{cycleId}/feedbacks")
    @Operation(summary = "获取所有反馈意见", description = "管理员查看所有员工的反馈意见")
    public Result<List<EvaluationFeedback>> getAllFeedbacks(@Parameter(description = "周期ID") @PathVariable Long cycleId) {
        List<EvaluationFeedback> feedbacks = adminEvaluationService.getAllFeedbacks(cycleId);
        return Result.success(feedbacks);
    }

    @PostMapping("/feedback/{feedbackId}/handle")
    @Operation(summary = "处理反馈意见", description = "管理员回复和处理员工反馈")
    public Result<String> handleFeedback(
            @Parameter(description = "反馈ID") @PathVariable Long feedbackId,
            @RequestParam String reply,
            @RequestParam(defaultValue = "false") boolean adjust,
            HttpServletRequest httpRequest) {
        try {
            Long adminId = (Long) httpRequest.getAttribute("adminId");
            adminEvaluationService.handleFeedback(feedbackId, reply, adjust, adminId);
            return Result.success("反馈已处理");
        } catch (Exception e) {
            return Result.error("处理失败: " + e.getMessage());
        }
    }

    @GetMapping("/{cycleId}/meetings")
    @Operation(summary = "获取所有一对一沟通预约", description = "管理员查看所有沟通预约")
    public Result<List<OneOnOneMeeting>> getMeetings(
            @Parameter(description = "周期ID") @PathVariable Long cycleId,
            @Parameter(description = "员工ID(可选)") @RequestParam(required = false) Long employeeId) {
        List<OneOnOneMeeting> meetings = adminEvaluationService.getMeetings(cycleId, employeeId);
        return Result.success(meetings);
    }

    @PostMapping("/meeting/update")
    @Operation(summary = "更新沟通记录", description = "管理员更新一对一沟通记录内容")
    public Result<String> updateMeeting(@RequestBody OneOnOneMeetingRequest request) {
        try {
            adminEvaluationService.updateMeeting(request);
            return Result.success("沟通记录已更新");
        } catch (Exception e) {
            return Result.error("更新失败: " + e.getMessage());
        }
    }
}

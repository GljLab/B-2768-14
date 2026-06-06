package com.employee.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.employee.common.Result;
import com.employee.dto.*;
import com.employee.entity.*;
import com.employee.service.EmployeeEvaluationService;
import com.employee.service.EvaluationCycleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employee/evaluations")
@RequiredArgsConstructor
@Tag(name = "员工-成长评价", description = "员工进行目标设定、自评、同事互评等")
public class EmployeeEvaluationController {
    private final EmployeeEvaluationService employeeEvaluationService;
    private final EvaluationCycleService evaluationCycleService;

    @GetMapping("/cycles")
    @Operation(summary = "获取可参与的评价周期", description = "员工获取自己参与的评价周期列表")
    public Result<List<EvaluationCycleEmployee>> getMyCycles(HttpServletRequest request) {
        Long employeeId = (Long) request.getAttribute("employeeId");
        List<EvaluationCycleEmployee> cycles = evaluationCycleService.getCycleEmployees(null).stream()
            .filter(ce -> ce.getEmployeeId().equals(employeeId))
            .toList();
        return Result.success(cycles);
    }

    @GetMapping("/{cycleId}/goals")
    @Operation(summary = "获取我的工作目标", description = "获取某个评价周期下的工作目标")
    public Result<List<WorkGoal>> getMyGoals(@Parameter(description = "周期ID") @PathVariable Long cycleId, HttpServletRequest request) {
        Long employeeId = (Long) request.getAttribute("employeeId");
        List<WorkGoal> goals = employeeEvaluationService.getEmployeeGoals(cycleId, employeeId);
        return Result.success(goals);
    }

    @PostMapping("/{cycleId}/goals")
    @Operation(summary = "保存工作目标", description = "员工保存或编辑工作目标(草稿)")
    public Result<String> saveGoals(
            @Parameter(description = "周期ID") @PathVariable Long cycleId,
            @RequestBody List<WorkGoalRequest> goals,
            HttpServletRequest request) {
        try {
            Long employeeId = (Long) request.getAttribute("employeeId");
            employeeEvaluationService.saveWorkGoals(cycleId, employeeId, goals);
            return Result.success("目标保存成功");
        } catch (Exception e) {
            return Result.error("保存失败: " + e.getMessage());
        }
    }

    @PostMapping("/{cycleId}/goals/submit")
    @Operation(summary = "提交工作目标", description = "员工提交工作目标等待管理员确认")
    public Result<String> submitGoals(@Parameter(description = "周期ID") @PathVariable Long cycleId, HttpServletRequest request) {
        try {
            Long employeeId = (Long) request.getAttribute("employeeId");
            employeeEvaluationService.submitGoals(cycleId, employeeId);
            return Result.success("目标提交成功");
        } catch (Exception e) {
            return Result.error("提交失败: " + e.getMessage());
        }
    }

    @GetMapping("/{cycleId}/highlights")
    @Operation(summary = "获取我的工作亮点", description = "获取某个评价周期下的工作亮点记录")
    public Result<List<WorkHighlight>> getMyHighlights(@Parameter(description = "周期ID") @PathVariable Long cycleId, HttpServletRequest request) {
        Long employeeId = (Long) request.getAttribute("employeeId");
        List<WorkHighlight> highlights = employeeEvaluationService.getEmployeeHighlights(cycleId, employeeId);
        return Result.success(highlights);
    }

    @PostMapping("/highlights")
    @Operation(summary = "添加工作亮点", description = "员工记录工作亮点")
    public Result<Long> addHighlight(@RequestBody WorkHighlightRequest request, HttpServletRequest httpRequest) {
        try {
            Long employeeId = (Long) httpRequest.getAttribute("employeeId");
            Long id = employeeEvaluationService.saveHighlight(request, employeeId);
            return Result.success(id);
        } catch (Exception e) {
            return Result.error("添加失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/highlights/{id}")
    @Operation(summary = "删除工作亮点", description = "删除自己的工作亮点记录")
    public Result<String> deleteHighlight(@Parameter(description = "亮点ID") @PathVariable Long id) {
        try {
            employeeEvaluationService.deleteHighlight(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    @GetMapping("/{cycleId}/self-evaluation")
    @Operation(summary = "获取我的自评", description = "获取某个评价周期的自我总结")
    public Result<SelfEvaluation> getSelfEvaluation(@Parameter(description = "周期ID") @PathVariable Long cycleId, HttpServletRequest request) {
        Long employeeId = (Long) request.getAttribute("employeeId");
        SelfEvaluation selfEval = employeeEvaluationService.getSelfEvaluation(cycleId, employeeId);
        return Result.success(selfEval);
    }

    @PostMapping("/self-evaluation")
    @Operation(summary = "提交自我总结", description = "员工提交自我总结和目标自评")
    public Result<String> submitSelfEvaluation(@RequestBody SelfEvaluationRequest request, HttpServletRequest httpRequest) {
        try {
            Long employeeId = (Long) httpRequest.getAttribute("employeeId");
            employeeEvaluationService.submitSelfEvaluation(request, employeeId);
            return Result.success("自评提交成功");
        } catch (Exception e) {
            return Result.error("提交失败: " + e.getMessage());
        }
    }

    @GetMapping("/{cycleId}/colleague-evaluations/pending")
    @Operation(summary = "获取待评价的同事", description = "获取我需要评价的同事列表")
    public Result<List<ColleagueEvaluationRelation>> getPendingColleagueEvaluations(
            @Parameter(description = "周期ID") @PathVariable Long cycleId,
            HttpServletRequest request) {
        Long evaluatorId = (Long) request.getAttribute("employeeId");
        List<ColleagueEvaluationRelation> relations = employeeEvaluationService.getPendingColleagueEvaluations(cycleId, evaluatorId);
        return Result.success(relations);
    }

    @PostMapping("/colleague-evaluation")
    @Operation(summary = "提交同事评价", description = "员工对同事进行评价")
    public Result<String> submitColleagueEvaluation(@RequestBody ColleagueEvaluationRequest request, HttpServletRequest httpRequest) {
        try {
            Long evaluatorId = (Long) httpRequest.getAttribute("employeeId");
            employeeEvaluationService.submitColleagueEvaluation(request, evaluatorId);
            return Result.success("评价提交成功");
        } catch (Exception e) {
            return Result.error("提交失败: " + e.getMessage());
        }
    }

    @GetMapping("/{cycleId}/report")
    @Operation(summary = "获取我的成长报告", description = "查看某个评价周期的成长报告")
    public Result<GrowthReport> getGrowthReport(@Parameter(description = "周期ID") @PathVariable Long cycleId, HttpServletRequest request) {
        Long employeeId = (Long) request.getAttribute("employeeId");
        GrowthReport report = employeeEvaluationService.getGrowthReport(cycleId, employeeId);
        return Result.success(report);
    }

    @GetMapping("/growth-history")
    @Operation(summary = "获取我的成长历史", description = "查看所有历史评价报告")
    public Result<List<GrowthReport>> getGrowthHistory(HttpServletRequest request) {
        Long employeeId = (Long) request.getAttribute("employeeId");
        List<GrowthReport> history = employeeEvaluationService.getEmployeeGrowthHistory(employeeId);
        return Result.success(history);
    }

    @GetMapping("/growth-plans")
    @Operation(summary = "获取我的成长计划", description = "查看我的成长计划列表")
    public Result<List<GrowthPlan>> getGrowthPlans(HttpServletRequest request) {
        Long employeeId = (Long) request.getAttribute("employeeId");
        List<GrowthPlan> plans = employeeEvaluationService.getEmployeeGrowthPlans(employeeId);
        return Result.success(plans);
    }

    @PostMapping("/growth-plan")
    @Operation(summary = "保存成长计划", description = "员工制定或编辑成长计划")
    public Result<String> saveGrowthPlan(@RequestBody GrowthPlanRequest request, HttpServletRequest httpRequest) {
        try {
            Long employeeId = (Long) httpRequest.getAttribute("employeeId");
            employeeEvaluationService.saveGrowthPlan(request, employeeId);
            return Result.success("计划保存成功");
        } catch (Exception e) {
            return Result.error("保存失败: " + e.getMessage());
        }
    }

    @PostMapping("/feedback")
    @Operation(summary = "提交反馈意见", description = "员工对评价结果提交反馈意见")
    public Result<Long> submitFeedback(@RequestBody EvaluationFeedbackRequest request, HttpServletRequest httpRequest) {
        try {
            Long employeeId = (Long) httpRequest.getAttribute("employeeId");
            Long id = employeeEvaluationService.submitFeedback(request, employeeId);
            return Result.success(id);
        } catch (Exception e) {
            return Result.error("提交失败: " + e.getMessage());
        }
    }

    @GetMapping("/{cycleId}/feedbacks")
    @Operation(summary = "获取我的反馈", description = "查看我提交的反馈意见")
    public Result<List<EvaluationFeedback>> getMyFeedbacks(@Parameter(description = "周期ID") @PathVariable Long cycleId, HttpServletRequest request) {
        Long employeeId = (Long) request.getAttribute("employeeId");
        List<EvaluationFeedback> feedbacks = employeeEvaluationService.getEmployeeFeedbacks(cycleId, employeeId);
        return Result.success(feedbacks);
    }
}

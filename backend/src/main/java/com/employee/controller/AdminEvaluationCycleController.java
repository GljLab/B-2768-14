package com.employee.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.employee.common.Result;
import com.employee.dto.EvaluationCycleRequest;
import com.employee.dto.EvaluationProgressVO;
import com.employee.entity.*;
import com.employee.service.EvaluationCycleService;
import com.employee.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/evaluation-cycles")
@RequiredArgsConstructor
@Tag(name = "管理员-评价周期管理", description = "管理员创建和管理评价周期")
public class AdminEvaluationCycleController {
    private final EvaluationCycleService evaluationCycleService;

    @PostMapping
    @Operation(summary = "创建评价周期", description = "管理员创建新的评价周期")
    public Result<Long> createCycle(@Valid @RequestBody EvaluationCycleRequest request, HttpServletRequest httpRequest) {
        try {
            Long adminId = (Long) httpRequest.getAttribute("adminId");
            String adminName = (String) httpRequest.getAttribute("username");
            Long cycleId = evaluationCycleService.createCycle(request, adminId, adminName);
            return Result.success(cycleId);
        } catch (Exception e) {
            return Result.error("创建失败: " + e.getMessage());
        }
    }

    @GetMapping
    @Operation(summary = "获取评价周期列表", description = "分页查询评价周期列表")
    public Result<IPage<EvaluationCycle>> getCycleList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "状态: 0=草稿, 1=进行中, 2=已结束") @RequestParam(required = false) Integer status) {
        IPage<EvaluationCycle> list = evaluationCycleService.getCycleList(page, size, status);
        return Result.success(list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取评价周期详情", description = "根据ID获取评价周期详情")
    public Result<EvaluationCycle> getCycleById(@Parameter(description = "周期ID") @PathVariable Long id) {
        EvaluationCycle cycle = evaluationCycleService.getCycleById(id);
        if (cycle != null) {
            return Result.success(cycle);
        } else {
            return Result.error("评价周期不存在");
        }
    }

    @GetMapping("/{id}/grade-standards")
    @Operation(summary = "获取成长等级标准", description = "获取评价周期的成长等级标准")
    public Result<List<GradeStandard>> getGradeStandards(@Parameter(description = "周期ID") @PathVariable Long id) {
        List<GradeStandard> standards = evaluationCycleService.getGradeStandards(id);
        return Result.success(standards);
    }

    @GetMapping("/{id}/dimensions")
    @Operation(summary = "获取评价维度", description = "获取评价周期的评价维度配置")
    public Result<List<EvaluationDimension>> getDimensions(@Parameter(description = "周期ID") @PathVariable Long id) {
        List<EvaluationDimension> dimensions = evaluationCycleService.getDimensions(id);
        return Result.success(dimensions);
    }

    @GetMapping("/{id}/employees")
    @Operation(summary = "获取参与员工列表", description = "获取评价周期的所有参与员工")
    public Result<List<EvaluationCycleEmployee>> getCycleEmployees(@Parameter(description = "周期ID") @PathVariable Long id) {
        List<EvaluationCycleEmployee> employees = evaluationCycleService.getCycleEmployees(id);
        return Result.success(employees);
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "启动评价周期", description = "将评价周期状态从草稿改为进行中")
    public Result<String> startCycle(@Parameter(description = "周期ID") @PathVariable Long id) {
        try {
            evaluationCycleService.startCycle(id);
            return Result.success("评价周期已启动");
        } catch (Exception e) {
            return Result.error("启动失败: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/end")
    @Operation(summary = "结束评价周期", description = "将评价周期状态改为已结束")
    public Result<String> endCycle(@Parameter(description = "周期ID") @PathVariable Long id) {
        try {
            evaluationCycleService.endCycle(id);
            return Result.success("评价周期已结束");
        } catch (Exception e) {
            return Result.error("结束失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/progress")
    @Operation(summary = "获取评价进度", description = "获取评价周期的整体推进进度")
    public Result<EvaluationProgressVO> getProgress(@Parameter(description = "周期ID") @PathVariable Long id) {
        EvaluationProgressVO progress = evaluationCycleService.getProgress(id);
        return Result.success(progress);
    }
}

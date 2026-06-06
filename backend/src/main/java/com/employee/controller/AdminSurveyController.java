package com.employee.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.employee.common.Result;
import com.employee.dto.SurveyRequest;
import com.employee.dto.SurveyStatisticsVO;
import com.employee.entity.Survey;
import com.employee.service.SurveyService;
import com.employee.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/surveys")
@RequiredArgsConstructor
@Tag(name = "管理员问卷管理", description = "管理员端问卷创建与管理接口")
public class AdminSurveyController {
    private final SurveyService surveyService;
    private final JwtUtil jwtUtil;

    @GetMapping
    @Operation(summary = "获取问卷列表", description = "获取所有问卷列表")
    public Result<IPage<Survey>> getSurveyList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(surveyService.getSurveyList(pageNum, pageSize));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取问卷详情", description = "获取问卷详细信息，包括题目和选项")
    public Result<Survey> getSurveyDetail(@PathVariable Long id) {
        Survey survey = surveyService.getSurveyDetail(id);
        if (survey == null) {
            return Result.error("问卷不存在");
        }
        return Result.success(survey);
    }

    @PostMapping
    @Operation(summary = "创建问卷", description = "创建新的调查问卷")
    public Result<Survey> createSurvey(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody SurveyRequest request) {
        String token = authHeader.substring(7);
        Long adminId = jwtUtil.getAdminIdFromToken(token);
        return Result.success(surveyService.createSurvey(adminId, "管理员", request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新问卷", description = "修改已创建的问卷")
    public Result<Survey> updateSurvey(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id,
            @Valid @RequestBody SurveyRequest request) {
        String token = authHeader.substring(7);
        jwtUtil.getAdminIdFromToken(token);
        return Result.success(surveyService.updateSurvey(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除问卷", description = "删除指定问卷")
    public Result<String> deleteSurvey(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        String token = authHeader.substring(7);
        jwtUtil.getAdminIdFromToken(token);
        surveyService.deleteSurvey(id);
        return Result.success("删除成功");
    }

    @GetMapping("/{id}/statistics")
    @Operation(summary = "获取问卷统计", description = "获取问卷的统计分析结果")
    public Result<SurveyStatisticsVO> getSurveyStatistics(@PathVariable Long id) {
        return Result.success(surveyService.getSurveyStatistics(id));
    }
}

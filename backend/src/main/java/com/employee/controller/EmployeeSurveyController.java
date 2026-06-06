package com.employee.controller;

import com.employee.common.Result;
import com.employee.dto.SurveyAnswerRequest;
import com.employee.entity.Survey;
import com.employee.entity.SurveyAnswer;
import com.employee.service.SurveyService;
import com.employee.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employee/surveys")
@RequiredArgsConstructor
@Tag(name = "员工问卷填写", description = "员工端问卷查看与填写接口")
public class EmployeeSurveyController {
    private final SurveyService surveyService;
    private final JwtUtil jwtUtil;

    @GetMapping("/pending")
    @Operation(summary = "获取待完成问卷", description = "获取员工待填写的问卷列表")
    public Result<List<Survey>> getPendingSurveys(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        Long employeeId = jwtUtil.getEmployeeIdFromToken(token);
        return Result.success(surveyService.getEmployeePendingSurveys(employeeId));
    }

    @GetMapping("/completed")
    @Operation(summary = "获取已完成问卷", description = "获取员工已完成的问卷列表")
    public Result<List<Survey>> getCompletedSurveys(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        Long employeeId = jwtUtil.getEmployeeIdFromToken(token);
        return Result.success(surveyService.getEmployeeCompletedSurveys(employeeId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取问卷详情", description = "获取问卷详细信息用于填写")
    public Result<Survey> getSurveyDetail(@PathVariable Long id) {
        Survey survey = surveyService.getSurveyDetail(id);
        if (survey == null) {
            return Result.error("问卷不存在");
        }
        return Result.success(survey);
    }

    @PostMapping("/submit")
    @Operation(summary = "提交问卷答案", description = "员工提交问卷答案")
    public Result<String> submitSurvey(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody SurveyAnswerRequest request) {
        String token = authHeader.substring(7);
        Long employeeId = jwtUtil.getEmployeeIdFromToken(token);
        surveyService.submitSurveyAnswer(employeeId, request);
        return Result.success("提交成功");
    }

    @GetMapping("/{id}/my-answer")
    @Operation(summary = "查看我的答案", description = "查看员工自己提交的问卷答案")
    public Result<SurveyAnswer> getMyAnswer(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        String token = authHeader.substring(7);
        Long employeeId = jwtUtil.getEmployeeIdFromToken(token);
        return Result.success(surveyService.getMyAnswer(id, employeeId));
    }

    @GetMapping("/stats")
    @Operation(summary = "获取问卷统计", description = "获取员工问卷统计信息")
    public Result<Map<String, Object>> getSurveyStats(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        Long employeeId = jwtUtil.getEmployeeIdFromToken(token);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("pendingCount", surveyService.getEmployeePendingSurveys(employeeId).size());
        stats.put("completedCount", surveyService.getEmployeeCompletedSurveys(employeeId).size());
        
        return Result.success(stats);
    }
}

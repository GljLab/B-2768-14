package com.employee.controller;

import com.employee.common.Result;
import com.employee.dto.DepartmentStatisticsVO;
import com.employee.service.DepartmentStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/department-statistics")
@RequiredArgsConstructor
@Tag(name = "部门统计", description = "部门统计数据相关接口")
public class DepartmentStatisticsController {
    private final DepartmentStatisticsService statisticsService;
    
    @GetMapping
    @Operation(summary = "获取部门人员统计", description = "获取各部门人员规模、在职离职统计、平均司龄")
    public Result<List<DepartmentStatisticsVO>> getDepartmentStatistics() {
        List<DepartmentStatisticsVO> statistics = statisticsService.getDepartmentStatistics();
        return Result.success(statistics);
    }
    
    @GetMapping("/monthly-change")
    @Operation(summary = "获取月度人员变化统计", description = "按时间轴统计部门人员的增减变化情况")
    public Result<Map<String, Object>> getMonthlyChangeStatistics(
            @Parameter(description = "统计月份数") @RequestParam(defaultValue = "12") int months) {
        Map<String, Object> statistics = statisticsService.getMonthlyChangeStatistics(months);
        return Result.success(statistics);
    }
}

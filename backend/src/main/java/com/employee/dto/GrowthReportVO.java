package com.employee.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class GrowthReportVO {
    private Long reportId;
    private Long cycleId;
    private String cycleName;
    private Long employeeId;
    private String employeeName;
    private String departmentName;
    
    private BigDecimal selfScore;
    private BigDecimal colleagueScore;
    private BigDecimal adminScore;
    private BigDecimal totalScore;
    private String grade;
    
    private Integer departmentRank;
    private Integer companyRank;
    private Integer totalInDepartment;
    private Integer totalInCompany;
    
    private Map<String, BigDecimal> dimensionScores;
    
    private List<GoalResultVO> goalResults;
    
    private ColleagueSummaryVO colleagueSummary;
    
    private AdminEvaluationVO adminEvaluation;
    
    private List<GrowthTrendVO> growthTrend;
}

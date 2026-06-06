package com.employee.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class EvaluationStatisticsVO {
    private Long cycleId;
    private String cycleName;
    
    private Map<String, Integer> gradeDistribution;
    
    private List<DepartmentAverageVO> departmentAverages;
    
    private List<CycleTrendVO> cycleTrends;
    
    private List<TopEmployeeVO> topEmployees;
    
    private List<AttentionEmployeeVO> attentionEmployees;
}

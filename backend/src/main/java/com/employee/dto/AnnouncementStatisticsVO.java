package com.employee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "信息统计VO")
public class AnnouncementStatisticsVO {
    
    @Schema(description = "信息ID")
    private Long announcementId;
    
    @Schema(description = "标题")
    private String title;
    
    @Schema(description = "已阅人数")
    private Integer readCount;
    
    @Schema(description = "总人数")
    private Integer totalCount;
    
    @Schema(description = "已阅比例")
    private String readRate;
    
    @Schema(description = "已确认人数")
    private Integer confirmedCount;
    
    @Schema(description = "已确认比例")
    private String confirmedRate;
    
    @Schema(description = "未阅人员列表")
    private List<UnreadEmployeeVO> unreadEmployees;
    
    @Data
    @Schema(description = "未阅人员VO")
    public static class UnreadEmployeeVO {
        @Schema(description = "员工ID")
        private Long employeeId;
        
        @Schema(description = "员工姓名")
        private String employeeName;
        
        @Schema(description = "部门")
        private String department;
    }
}

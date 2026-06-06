package com.employee.dto;

import lombok.Data;

@Data
public class EvaluationFeedbackRequest {
    private Long id;
    private Long cycleId;
    private String content;
    private String questionPoints;
    private String supplement;
    private String attachments;
    private Integer status;
    private String adminReply;
    private Integer isAdjusted;
}

package com.employee.controller;

import com.employee.common.Result;
import com.employee.dto.FeedbackRequest;
import com.employee.entity.Feedback;
import com.employee.service.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employee/feedbacks")
@RequiredArgsConstructor
@Tag(name = "员工意见箱", description = "员工端意见提交接口")
public class EmployeeFeedbackController {
    private final FeedbackService feedbackService;

    @PostMapping
    @Operation(summary = "提交意见", description = "员工匿名提交意见")
    public Result<Feedback> submitFeedback(@Valid @RequestBody FeedbackRequest request) {
        Feedback feedback = feedbackService.submitFeedback(request);
        return Result.success(feedback);
    }
}

package com.employee.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.employee.dto.FeedbackRequest;
import com.employee.entity.Feedback;
import com.employee.mapper.FeedbackMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class FeedbackService {
    private final FeedbackMapper feedbackMapper;

    @Transactional
    public Feedback submitFeedback(FeedbackRequest request) {
        Feedback feedback = new Feedback();
        feedback.setCategory(request.getCategory());
        feedback.setContent(request.getContent());
        feedback.setIsRead(0);
        feedback.setFeedbackNo(generateFeedbackNo());
        feedbackMapper.insert(feedback);
        return feedback;
    }

    private String generateFeedbackNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "#" + dateStr + String.format("%03d", System.currentTimeMillis() % 1000);
    }

    public IPage<Feedback> getFeedbackList(Integer pageNum, Integer pageSize, String category, Integer isRead) {
        Page<Feedback> page = new Page<>(pageNum, pageSize);
        return feedbackMapper.selectFeedbackList(page, category, isRead);
    }

    @Transactional
    public void markAsRead(Long id) {
        Feedback feedback = feedbackMapper.selectById(id);
        if (feedback != null) {
            feedback.setIsRead(1);
            feedback.setReadTime(LocalDateTime.now());
            feedbackMapper.updateById(feedback);
        }
    }

    public Feedback getFeedbackDetail(Long id) {
        return feedbackMapper.selectById(id);
    }
}

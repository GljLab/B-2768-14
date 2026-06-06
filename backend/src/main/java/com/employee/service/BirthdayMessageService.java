package com.employee.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.employee.common.Result;
import com.employee.dto.BirthdayMessageVO;
import com.employee.entity.BirthdayMessage;
import com.employee.mapper.BirthdayMessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BirthdayMessageService {

    private final BirthdayMessageMapper messageMapper;

    public List<BirthdayMessageVO> getMessages(Long recipientId) {
        QueryWrapper<BirthdayMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("recipient_id", recipientId)
                .orderByDesc("created_at");
        List<BirthdayMessage> messages = messageMapper.selectList(queryWrapper);
        return messages.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    public Integer getUnreadCount(Long recipientId) {
        QueryWrapper<BirthdayMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("recipient_id", recipientId)
                .eq("is_read", 0);
        return Math.toIntExact(messageMapper.selectCount(queryWrapper));
    }

    public Result<?> markAsRead(Long messageId, Long recipientId) {
        BirthdayMessage message = messageMapper.selectById(messageId);
        if (message == null) {
            return Result.error("消息不存在");
        }
        if (!message.getRecipientId().equals(recipientId)) {
            return Result.error("无权操作");
        }
        message.setIsRead(1);
        message.setReadTime(LocalDateTime.now());
        messageMapper.updateById(message);
        return Result.success();
    }

    public Result<?> markAllAsRead(Long recipientId) {
        UpdateWrapper<BirthdayMessage> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("recipient_id", recipientId)
                .eq("is_read", 0)
                .set("is_read", 1)
                .set("read_time", LocalDateTime.now());
        messageMapper.update(null, updateWrapper);
        return Result.success();
    }

    public void sendMessage(Long recipientId, Integer recipientType, String title, String content, String type, Long relatedId) {
        BirthdayMessage message = new BirthdayMessage();
        message.setRecipientId(recipientId);
        message.setRecipientType(recipientType);
        message.setTitle(title);
        message.setContent(content);
        message.setType(type);
        message.setRelatedId(relatedId);
        message.setIsRead(0);
        messageMapper.insert(message);
    }

    private BirthdayMessageVO convertToVO(BirthdayMessage message) {
        BirthdayMessageVO vo = new BirthdayMessageVO();
        vo.setId(message.getId());
        vo.setRecipientId(message.getRecipientId());
        vo.setTitle(message.getTitle());
        vo.setContent(message.getContent());
        vo.setType(message.getType());
        vo.setRelatedId(message.getRelatedId());
        vo.setIsRead(message.getIsRead());
        vo.setCreatedAt(message.getCreatedAt());
        return vo;
    }
}

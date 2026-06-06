package com.employee.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.employee.dto.AnnouncementQueryRequest;
import com.employee.dto.AnnouncementRequest;
import com.employee.dto.AnnouncementStatisticsVO;
import com.employee.entity.Announcement;
import com.employee.entity.AnnouncementReadRecord;
import com.employee.entity.AnnouncementTarget;
import com.employee.entity.Employee;
import com.employee.mapper.AnnouncementMapper;
import com.employee.mapper.AnnouncementReadRecordMapper;
import com.employee.mapper.AnnouncementTargetMapper;
import com.employee.mapper.EmployeeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnnouncementService {
    private final AnnouncementMapper announcementMapper;
    private final AnnouncementTargetMapper targetMapper;
    private final AnnouncementReadRecordMapper readRecordMapper;
    private final EmployeeMapper employeeMapper;
    private final AnnouncementCategoryService categoryService;
    
    public List<Announcement> getEmployeeAnnouncements(Long employeeId) {
        return announcementMapper.selectEmployeeAnnouncements(employeeId);
    }
    
    public IPage<Announcement> getPublisherAnnouncements(Long publisherId, Integer pageNum, Integer pageSize) {
        Page<Announcement> page = new Page<>(pageNum, pageSize);
        return announcementMapper.selectPublisherAnnouncements(page, publisherId);
    }
    
    public Announcement getAnnouncementDetail(Long id) {
        Announcement announcement = announcementMapper.selectDetailById(id);
        if (announcement != null) {
            announcement.setTargets(targetMapper.selectByAnnouncementId(id));
        }
        return announcement;
    }
    
    @Transactional
    public Announcement createAnnouncement(Long publisherId, String publisherName, AnnouncementRequest request) {
        if (request.getIsTop() != null && request.getIsTop() == 1) {
            int topCount = announcementMapper.countTopAnnouncements();
            if (topCount >= 3) {
                throw new IllegalArgumentException("置顶信息最多3条");
            }
        }
        
        Announcement announcement = new Announcement();
        BeanUtils.copyProperties(request, announcement);
        announcement.setPublisherId(publisherId);
        announcement.setPublisherName(publisherName);
        announcement.setViewCount(0);
        
        if (request.getPublishStatus() == null) {
            announcement.setPublishStatus(0);
        }
        
        if (request.getPublishStatus() == 1) {
            announcement.setPublishTime(LocalDateTime.now());
        }
        
        announcementMapper.insert(announcement);
        
        if (request.getTargets() != null && !request.getTargets().isEmpty()) {
            for (AnnouncementRequest.TargetRequest targetReq : request.getTargets()) {
                AnnouncementTarget target = new AnnouncementTarget();
                target.setAnnouncementId(announcement.getId());
                target.setTargetType(targetReq.getTargetType());
                target.setTargetId(targetReq.getTargetId());
                target.setTargetName(targetReq.getTargetName());
                targetMapper.insert(target);
            }
        }
        
        if (request.getPublishStatus() != null && request.getPublishStatus() == 1) {
            createReadRecords(announcement.getId());
        }
        
        return announcement;
    }
    
    @Transactional
    public Announcement updateAnnouncement(Long publisherId, AnnouncementRequest request) {
        Announcement announcement = announcementMapper.selectById(request.getId());
        if (announcement == null) {
            throw new IllegalArgumentException("信息不存在");
        }
        
        if (!announcement.getPublisherId().equals(publisherId)) {
            throw new IllegalArgumentException("无权修改该信息");
        }
        
        if (request.getIsTop() != null && request.getIsTop() == 1 && announcement.getIsTop() != 1) {
            int topCount = announcementMapper.countTopAnnouncements();
            if (topCount >= 3) {
                throw new IllegalArgumentException("置顶信息最多3条");
            }
        }
        
        BeanUtils.copyProperties(request, announcement);
        
        if (request.getPublishStatus() != null && request.getPublishStatus() == 1 && announcement.getPublishTime() == null) {
            announcement.setPublishTime(LocalDateTime.now());
            createReadRecords(announcement.getId());
        }
        
        announcementMapper.updateById(announcement);
        
        if (request.getTargets() != null) {
            LambdaQueryWrapper<AnnouncementTarget> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AnnouncementTarget::getAnnouncementId, announcement.getId());
            targetMapper.delete(wrapper);
            
            for (AnnouncementRequest.TargetRequest targetReq : request.getTargets()) {
                AnnouncementTarget target = new AnnouncementTarget();
                target.setAnnouncementId(announcement.getId());
                target.setTargetType(targetReq.getTargetType());
                target.setTargetId(targetReq.getTargetId());
                target.setTargetName(targetReq.getTargetName());
                targetMapper.insert(target);
            }
        }
        
        return announcement;
    }
    
    @Transactional
    public void withdrawAnnouncement(Long publisherId, Long announcementId) {
        Announcement announcement = announcementMapper.selectById(announcementId);
        if (announcement == null) {
            throw new IllegalArgumentException("信息不存在");
        }
        
        if (!announcement.getPublisherId().equals(publisherId)) {
            throw new IllegalArgumentException("无权撤回该信息");
        }
        
        if (announcement.getPublishStatus() != 1) {
            throw new IllegalArgumentException("只有已发布的信息才能撤回");
        }
        
        announcement.setPublishStatus(0);
        announcementMapper.updateById(announcement);
    }
    
    @Transactional
    public void deleteAnnouncement(Long publisherId, Long announcementId) {
        Announcement announcement = announcementMapper.selectById(announcementId);
        if (announcement == null) {
            throw new IllegalArgumentException("信息不存在");
        }
        
        if (!announcement.getPublisherId().equals(publisherId)) {
            throw new IllegalArgumentException("无权删除该信息");
        }
        
        LambdaQueryWrapper<AnnouncementTarget> targetWrapper = new LambdaQueryWrapper<>();
        targetWrapper.eq(AnnouncementTarget::getAnnouncementId, announcementId);
        targetMapper.delete(targetWrapper);
        
        LambdaQueryWrapper<AnnouncementReadRecord> recordWrapper = new LambdaQueryWrapper<>();
        recordWrapper.eq(AnnouncementReadRecord::getAnnouncementId, announcementId);
        readRecordMapper.delete(recordWrapper);
        
        announcementMapper.deleteById(announcementId);
    }
    
    private void createReadRecords(Long announcementId) {
        List<Long> targetEmployeeIds = targetMapper.selectTargetEmployeeIds(announcementId);
        
        for (Long employeeId : targetEmployeeIds) {
            Employee employee = employeeMapper.selectById(employeeId);
            if (employee != null) {
                AnnouncementReadRecord record = new AnnouncementReadRecord();
                record.setAnnouncementId(announcementId);
                record.setEmployeeId(employeeId);
                record.setEmployeeName(employee.getName());
                record.setDepartmentId(employee.getDepartmentId());
                record.setIsRead(0);
                record.setIsConfirmed(0);
                try {
                    readRecordMapper.insert(record);
                } catch (Exception e) {
                    // 忽略唯一索引冲突
                }
            }
        }
    }
    
    @Transactional
    public void markAsRead(Long employeeId, Long announcementId) {
        LambdaQueryWrapper<AnnouncementReadRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AnnouncementReadRecord::getAnnouncementId, announcementId)
               .eq(AnnouncementReadRecord::getEmployeeId, employeeId);
        AnnouncementReadRecord record = readRecordMapper.selectOne(wrapper);
        
        if (record != null && record.getIsRead() == 0) {
            record.setIsRead(1);
            record.setReadTime(LocalDateTime.now());
            readRecordMapper.updateById(record);
            
            Announcement announcement = announcementMapper.selectById(announcementId);
            if (announcement != null) {
                announcement.setViewCount(announcement.getViewCount() + 1);
                announcementMapper.updateById(announcement);
            }
        }
    }
    
    @Transactional
    public void markAsConfirmed(Long employeeId, Long announcementId) {
        LambdaQueryWrapper<AnnouncementReadRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AnnouncementReadRecord::getAnnouncementId, announcementId)
               .eq(AnnouncementReadRecord::getEmployeeId, employeeId);
        AnnouncementReadRecord record = readRecordMapper.selectOne(wrapper);
        
        if (record != null) {
            if (record.getIsRead() == 0) {
                record.setIsRead(1);
                record.setReadTime(LocalDateTime.now());
            }
            record.setIsConfirmed(1);
            record.setConfirmTime(LocalDateTime.now());
            readRecordMapper.updateById(record);
        }
    }
    
    @Transactional
    public void clearAllUnread(Long employeeId) {
        LambdaQueryWrapper<AnnouncementReadRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AnnouncementReadRecord::getEmployeeId, employeeId)
               .eq(AnnouncementReadRecord::getIsRead, 0);
        
        List<AnnouncementReadRecord> records = readRecordMapper.selectList(wrapper);
        for (AnnouncementReadRecord record : records) {
            record.setIsRead(1);
            record.setReadTime(LocalDateTime.now());
            readRecordMapper.updateById(record);
        }
    }
    
    public int getUnreadCount(Long employeeId) {
        return readRecordMapper.countUnreadByEmployeeId(employeeId);
    }
    
    public List<Announcement> getUnconfirmedAnnouncements(Long employeeId) {
        return announcementMapper.selectUnconfirmedAnnouncements(employeeId);
    }
    
    public AnnouncementStatisticsVO getStatistics(Long announcementId) {
        Announcement announcement = announcementMapper.selectById(announcementId);
        if (announcement == null) {
            throw new IllegalArgumentException("信息不存在");
        }
        
        AnnouncementStatisticsVO vo = new AnnouncementStatisticsVO();
        vo.setAnnouncementId(announcementId);
        vo.setTitle(announcement.getTitle());
        
        int readCount = announcementMapper.countReadByAnnouncementId(announcementId);
        int totalCount = announcementMapper.countTotalByAnnouncementId(announcementId);
        int confirmedCount = readRecordMapper.countConfirmedByAnnouncementId(announcementId);
        
        vo.setReadCount(readCount);
        vo.setTotalCount(totalCount);
        vo.setReadRate(totalCount > 0 ? String.format("%.1f%%", (readCount * 100.0 / totalCount)) : "0%");
        vo.setConfirmedCount(confirmedCount);
        vo.setConfirmedRate(totalCount > 0 ? String.format("%.1f%%", (confirmedCount * 100.0 / totalCount)) : "0%");
        vo.setUnreadEmployees(readRecordMapper.selectUnreadEmployees(announcementId));
        
        return vo;
    }
    
    public IPage<Announcement> queryAnnouncements(AnnouncementQueryRequest request) {
        Page<Announcement> page = new Page<>(request.getPageNum(), request.getPageSize());
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        
        if (request.getCategoryId() != null) {
            wrapper.eq(Announcement::getCategoryId, request.getCategoryId());
        }
        
        if (request.getImportance() != null) {
            wrapper.eq(Announcement::getImportance, request.getImportance());
        }
        
        if (request.getPublishStatus() != null) {
            wrapper.eq(Announcement::getPublishStatus, request.getPublishStatus());
        }
        
        if (request.getPublisherId() != null) {
            wrapper.eq(Announcement::getPublisherId, request.getPublisherId());
        }
        
        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.and(w -> w.like(Announcement::getTitle, request.getKeyword())
                              .or().like(Announcement::getContent, request.getKeyword()));
        }
        
        wrapper.orderByDesc(Announcement::getIsTop)
               .orderByDesc(Announcement::getPublishTime)
               .orderByDesc(Announcement::getCreatedAt);
        
        IPage<Announcement> resultPage = announcementMapper.selectPage(page, wrapper);
        
        for (Announcement ann : resultPage.getRecords()) {
            ann.setCategoryName(categoryService.getById(ann.getCategoryId()) != null ? 
                categoryService.getById(ann.getCategoryId()).getCategoryName() : null);
        }
        
        return resultPage;
    }
    
    public List<Announcement> getArchivedAnnouncements(Long employeeId) {
        List<Announcement> all = announcementMapper.selectEmployeeAnnouncements(employeeId);
        List<Announcement> archived = new ArrayList<>();
        for (Announcement ann : all) {
            if (ann.getExpiryDate() != null && ann.getExpiryDate().isBefore(java.time.LocalDate.now())) {
                archived.add(ann);
            }
        }
        return archived;
    }
}

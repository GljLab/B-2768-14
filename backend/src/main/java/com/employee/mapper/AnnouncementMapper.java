package com.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.employee.entity.Announcement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AnnouncementMapper extends BaseMapper<Announcement> {
    
    @Select("SELECT a.*, ac.category_name as categoryName, ac.category_code as categoryCode, " +
            "CASE WHEN arr.is_read = 1 THEN 1 ELSE 0 END as isRead, " +
            "CASE WHEN arr.is_confirmed = 1 THEN 1 ELSE 0 END as isConfirmed " +
            "FROM announcement a " +
            "LEFT JOIN announcement_category ac ON a.category_id = ac.id " +
            "LEFT JOIN announcement_read_record arr ON a.id = arr.announcement_id AND arr.employee_id = #{employeeId} " +
            "WHERE a.publish_status = 1 " +
            "AND (a.expiry_date IS NULL OR a.expiry_date >= CURDATE()) " +
            "AND (a.publish_date IS NULL OR a.publish_date <= CURDATE()) " +
            "AND EXISTS (" +
            "    SELECT 1 FROM announcement_target t " +
            "    WHERE t.announcement_id = a.id " +
            "    AND (t.target_type = 1 " +
            "         OR (t.target_type = 2 AND EXISTS (SELECT 1 FROM employee e WHERE e.id = #{employeeId} AND e.department_id = t.target_id)) " +
            "         OR (t.target_type = 3 AND t.target_id = #{employeeId})) " +
            ") " +
            "ORDER BY a.is_top DESC, a.publish_time DESC, a.id DESC")
    List<Announcement> selectEmployeeAnnouncements(@Param("employeeId") Long employeeId);
    
    @Select("SELECT a.*, ac.category_name as categoryName, ac.category_code as categoryCode " +
            "FROM announcement a " +
            "LEFT JOIN announcement_category ac ON a.category_id = ac.id " +
            "WHERE a.publisher_id = #{publisherId} " +
            "ORDER BY a.created_at DESC")
    IPage<Announcement> selectPublisherAnnouncements(Page<Announcement> page, @Param("publisherId") Long publisherId);
    
    @Select("SELECT a.*, ac.category_name as categoryName, ac.category_code as categoryCode " +
            "FROM announcement a " +
            "LEFT JOIN announcement_category ac ON a.category_id = ac.id " +
            "WHERE a.id = #{id}")
    Announcement selectDetailById(@Param("id") Long id);
    
    @Select("SELECT COUNT(*) FROM announcement WHERE is_top = 1 AND publish_status = 1")
    int countTopAnnouncements();
    
    @Select("SELECT a.*, ac.category_name as categoryName, ac.category_code as categoryCode, " +
            "CASE WHEN arr.is_read = 1 THEN 1 ELSE 0 END as isRead " +
            "FROM announcement a " +
            "LEFT JOIN announcement_category ac ON a.category_id = ac.id " +
            "LEFT JOIN announcement_read_record arr ON a.id = arr.announcement_id AND arr.employee_id = #{employeeId} " +
            "WHERE a.publish_status = 1 AND a.is_force_confirm = 1 AND arr.is_confirmed = 0 " +
            "AND (a.expiry_date IS NULL OR a.expiry_date >= CURDATE()) " +
            "AND (a.publish_date IS NULL OR a.publish_date <= CURDATE()) " +
            "AND EXISTS (" +
            "    SELECT 1 FROM announcement_target t " +
            "    WHERE t.announcement_id = a.id " +
            "    AND (t.target_type = 1 " +
            "         OR (t.target_type = 2 AND EXISTS (SELECT 1 FROM employee e WHERE e.id = #{employeeId} AND e.department_id = t.target_id)) " +
            "         OR (t.target_type = 3 AND t.target_id = #{employeeId})) " +
            ") " +
            "ORDER BY a.publish_time DESC")
    List<Announcement> selectUnconfirmedAnnouncements(@Param("employeeId") Long employeeId);
    
    @Select("SELECT COUNT(*) FROM announcement_read_record " +
            "WHERE announcement_id = #{announcementId} AND is_read = 1")
    int countReadByAnnouncementId(@Param("announcementId") Long announcementId);
    
    @Select("SELECT COUNT(*) FROM announcement_read_record " +
            "WHERE announcement_id = #{announcementId}")
    int countTotalByAnnouncementId(@Param("announcementId") Long announcementId);
}

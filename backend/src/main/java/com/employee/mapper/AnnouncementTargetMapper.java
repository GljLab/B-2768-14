package com.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.employee.entity.AnnouncementTarget;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AnnouncementTargetMapper extends BaseMapper<AnnouncementTarget> {
    
    @Select("SELECT * FROM announcement_target WHERE announcement_id = #{announcementId}")
    List<AnnouncementTarget> selectByAnnouncementId(@Param("announcementId") Long announcementId);
    
    @Select("SELECT DISTINCT e.id " +
            "FROM employee e " +
            "WHERE e.status = 1 AND EXISTS (" +
            "    SELECT 1 FROM announcement_target t " +
            "    WHERE t.announcement_id = #{announcementId} " +
            "    AND (t.target_type = 1 " +
            "         OR (t.target_type = 2 AND e.department_id = t.target_id) " +
            "         OR (t.target_type = 3 AND t.target_id = e.id)) " +
            ")")
    List<Long> selectTargetEmployeeIds(@Param("announcementId") Long announcementId);
}

package com.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.employee.dto.AnnouncementStatisticsVO;
import com.employee.entity.AnnouncementReadRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AnnouncementReadRecordMapper extends BaseMapper<AnnouncementReadRecord> {
    
    @Select("SELECT COUNT(*) FROM announcement_read_record " +
            "WHERE employee_id = #{employeeId} AND is_read = 0")
    int countUnreadByEmployeeId(@Param("employeeId") Long employeeId);
    
    @Select("SELECT e.id as employeeId, e.name as employeeName, e.department as department " +
            "FROM announcement_read_record arr " +
            "LEFT JOIN employee e ON arr.employee_id = e.id " +
            "WHERE arr.announcement_id = #{announcementId} AND arr.is_read = 0 " +
            "ORDER BY e.department, e.name")
    List<AnnouncementStatisticsVO.UnreadEmployeeVO> selectUnreadEmployees(@Param("announcementId") Long announcementId);
    
    @Select("SELECT COUNT(*) FROM announcement_read_record " +
            "WHERE announcement_id = #{announcementId} AND is_confirmed = 1")
    int countConfirmedByAnnouncementId(@Param("announcementId") Long announcementId);
}

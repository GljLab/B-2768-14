package com.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.employee.entity.Survey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SurveyMapper extends BaseMapper<Survey> {
    
    @Select("SELECT s.*, " +
            "(SELECT COUNT(*) FROM survey_answer sa WHERE sa.survey_id = s.id) as completedCount, " +
            "(SELECT COUNT(DISTINCT e.id) FROM employee e " +
            " WHERE e.status = 1 AND EXISTS (" +
            "   SELECT 1 FROM survey_target st " +
            "   WHERE st.survey_id = s.id " +
            "   AND (st.target_type = 1 OR (st.target_type = 2 AND e.department_id = st.target_id))" +
            " )) as totalCount " +
            "FROM survey s ORDER BY s.created_at DESC")
    IPage<Survey> selectSurveyList(Page<Survey> page);
    
    @Select("SELECT s.*, " +
            "(SELECT COUNT(*) FROM survey_answer sa WHERE sa.survey_id = s.id) as completedCount, " +
            "(SELECT COUNT(DISTINCT e.id) FROM employee e " +
            " WHERE e.status = 1 AND EXISTS (" +
            "   SELECT 1 FROM survey_target st " +
            "   WHERE st.survey_id = s.id " +
            "   AND (st.target_type = 1 OR (st.target_type = 2 AND e.department_id = st.target_id))" +
            " )) as totalCount " +
            "FROM survey s WHERE s.id = #{id}")
    Survey selectSurveyDetail(@Param("id") Long id);
    
    @Select("SELECT s.* FROM survey s " +
            "WHERE s.status = 1 AND s.deadline >= NOW() " +
            "AND EXISTS (" +
            "   SELECT 1 FROM survey_target st " +
            "   WHERE st.survey_id = s.id " +
            "   AND (st.target_type = 1 OR (st.target_type = 2 AND EXISTS (" +
            "       SELECT 1 FROM employee e WHERE e.id = #{employeeId} AND e.department_id = st.target_id" +
            "   ))) " +
            ") " +
            "AND NOT EXISTS (" +
            "   SELECT 1 FROM survey_answer sa WHERE sa.survey_id = s.id AND sa.employee_id = #{employeeId}" +
            ") " +
            "ORDER BY s.created_at DESC")
    List<Survey> selectPendingSurveys(@Param("employeeId") Long employeeId);
    
    @Select("SELECT s.*, sa.submit_time as submitTime " +
            "FROM survey s " +
            "INNER JOIN survey_answer sa ON s.id = sa.survey_id " +
            "WHERE sa.employee_id = #{employeeId} " +
            "ORDER BY sa.submit_time DESC")
    List<Survey> selectCompletedSurveys(@Param("employeeId") Long employeeId);
}

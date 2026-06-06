package com.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.employee.entity.SurveyAnswer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SurveyAnswerMapper extends BaseMapper<SurveyAnswer> {
    
    @Select("SELECT * FROM survey_answer WHERE survey_id = #{surveyId} AND employee_id = #{employeeId}")
    SurveyAnswer selectBySurveyAndEmployee(@Param("surveyId") Long surveyId, @Param("employeeId") Long employeeId);
    
    @Select("SELECT * FROM survey_answer WHERE survey_id = #{surveyId} ORDER BY submit_time DESC")
    List<SurveyAnswer> selectBySurveyId(@Param("surveyId") Long surveyId);
}

package com.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.employee.entity.SurveyQuestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SurveyQuestionMapper extends BaseMapper<SurveyQuestion> {
    
    @Select("SELECT * FROM survey_question WHERE survey_id = #{surveyId} ORDER BY sort_order ASC, id ASC")
    List<SurveyQuestion> selectBySurveyId(@Param("surveyId") Long surveyId);
}

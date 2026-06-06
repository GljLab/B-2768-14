package com.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.employee.entity.SurveyTarget;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SurveyTargetMapper extends BaseMapper<SurveyTarget> {
    
    @Select("SELECT * FROM survey_target WHERE survey_id = #{surveyId}")
    List<SurveyTarget> selectBySurveyId(@Param("surveyId") Long surveyId);
}

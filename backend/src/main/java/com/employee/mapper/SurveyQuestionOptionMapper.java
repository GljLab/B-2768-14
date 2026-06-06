package com.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.employee.entity.SurveyQuestionOption;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SurveyQuestionOptionMapper extends BaseMapper<SurveyQuestionOption> {
    
    @Select("SELECT * FROM survey_question_option WHERE question_id = #{questionId} ORDER BY sort_order ASC, id ASC")
    List<SurveyQuestionOption> selectByQuestionId(@Param("questionId") Long questionId);
    
    @Select("SELECT o.*, " +
            "(SELECT COUNT(*) FROM survey_answer_item ai WHERE ai.question_id = #{questionId} AND ai.option_id = o.id) as selectCount " +
            "FROM survey_question_option o WHERE o.question_id = #{questionId} ORDER BY o.sort_order ASC, o.id ASC")
    List<SurveyQuestionOption> selectWithStatsByQuestionId(@Param("questionId") Long questionId);
}

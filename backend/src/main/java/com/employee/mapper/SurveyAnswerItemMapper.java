package com.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.employee.entity.SurveyAnswerItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SurveyAnswerItemMapper extends BaseMapper<SurveyAnswerItem> {
    
    @Select("SELECT * FROM survey_answer_item WHERE answer_id = #{answerId}")
    List<SurveyAnswerItem> selectByAnswerId(@Param("answerId") Long answerId);
    
    @Select("SELECT * FROM survey_answer_item WHERE answer_id IN (" +
            "SELECT id FROM survey_answer WHERE survey_id = #{surveyId}" +
            ") AND question_type = 3 ORDER BY id DESC")
    List<SurveyAnswerItem> selectTextAnswersBySurveyId(@Param("surveyId") Long surveyId);
    
    @Select("SELECT AVG(rating_score) FROM survey_answer_item " +
            "WHERE answer_id IN (SELECT id FROM survey_answer WHERE survey_id = #{surveyId}) " +
            "AND question_id = #{questionId} AND rating_item = #{ratingItem}")
    Double calculateAverageRating(@Param("surveyId") Long surveyId, @Param("questionId") Long questionId, @Param("ratingItem") String ratingItem);
}

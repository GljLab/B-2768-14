package com.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.employee.entity.Feedback;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface FeedbackMapper extends BaseMapper<Feedback> {
    
    @Select("<script>" +
            "SELECT * FROM feedback " +
            "<where>" +
            "<if test='category != null and category != \"\"'>AND category = #{category}</if>" +
            "<if test='isRead != null'>AND is_read = #{isRead}</if>" +
            "</where>" +
            "ORDER BY created_at DESC" +
            "</script>")
    IPage<Feedback> selectFeedbackList(Page<Feedback> page, @Param("category") String category, @Param("isRead") Integer isRead);
}

package com.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.employee.entity.AnnouncementCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AnnouncementCategoryMapper extends BaseMapper<AnnouncementCategory> {
    
    @Select("SELECT * FROM announcement_category WHERE status = 1 ORDER BY sort_order ASC, id DESC")
    List<AnnouncementCategory> selectAllEnabled();
    
    @Select("SELECT * FROM announcement_category ORDER BY sort_order ASC, id DESC")
    List<AnnouncementCategory> selectAll();
}

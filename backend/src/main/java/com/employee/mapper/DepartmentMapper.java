package com.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.employee.entity.Department;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {
    
    @Select("SELECT * FROM department WHERE parent_id = #{parentId} ORDER BY sort_order ASC, id ASC")
    List<Department> selectByParentId(@Param("parentId") Long parentId);
    
    @Select("SELECT COUNT(*) FROM department WHERE parent_id = #{parentId}")
    int countChildren(@Param("parentId") Long parentId);
    
    @Select("SELECT COUNT(*) FROM employee WHERE department_id = #{departmentId} AND status = 1")
    int countDirectMembers(@Param("departmentId") Long departmentId);
}

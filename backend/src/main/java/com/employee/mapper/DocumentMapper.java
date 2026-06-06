package com.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.employee.entity.Document;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DocumentMapper extends BaseMapper<Document> {
    
    @Select("SELECT d.*, dt.type_name as documentTypeName, dt.type_code as typeCode, e.name as employeeName " +
            "FROM document d " +
            "LEFT JOIN document_type dt ON d.document_type_id = dt.id " +
            "LEFT JOIN employee e ON d.employee_id = e.id " +
            "WHERE d.employee_id = #{employeeId} AND d.is_archived = 0 " +
            "ORDER BY d.created_at DESC")
    List<Document> selectByEmployeeId(@Param("employeeId") Long employeeId);
    
    @Select("SELECT d.*, dt.type_name as documentTypeName, dt.type_code as typeCode, e.name as employeeName " +
            "FROM document d " +
            "LEFT JOIN document_type dt ON d.document_type_id = dt.id " +
            "LEFT JOIN employee e ON d.employee_id = e.id " +
            "WHERE d.is_archived = 0 " +
            "ORDER BY d.created_at DESC")
    IPage<Document> selectDocumentPage(Page<Document> page);
    
    @Select("SELECT d.*, dt.type_name as documentTypeName, dt.type_code as typeCode, e.name as employeeName " +
            "FROM document d " +
            "LEFT JOIN document_type dt ON d.document_type_id = dt.id " +
            "LEFT JOIN employee e ON d.employee_id = e.id " +
            "WHERE d.id = #{id}")
    Document selectDetailById(@Param("id") Long id);
    
    @Select("SELECT COUNT(*) FROM document " +
            "WHERE employee_id = #{employeeId} AND document_type_id = #{documentTypeId} AND is_archived = 0")
    int countByEmployeeAndType(@Param("employeeId") Long employeeId, @Param("documentTypeId") Long documentTypeId);
}

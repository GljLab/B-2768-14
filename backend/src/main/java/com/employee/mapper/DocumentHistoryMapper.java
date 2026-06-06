package com.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.employee.entity.DocumentHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DocumentHistoryMapper extends BaseMapper<DocumentHistory> {
    
    @Select("SELECT * FROM document_history WHERE document_id = #{documentId} ORDER BY version DESC")
    List<DocumentHistory> selectByDocumentId(@Param("documentId") Long documentId);
}

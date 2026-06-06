package com.employee.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.employee.entity.DocumentType;
import com.employee.mapper.DocumentTypeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentTypeService {
    private final DocumentTypeMapper documentTypeMapper;
    
    public List<DocumentType> getAllDocumentTypes() {
        LambdaQueryWrapper<DocumentType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DocumentType::getStatus, 1)
               .orderByAsc(DocumentType::getSortOrder);
        return documentTypeMapper.selectList(wrapper);
    }
    
    public DocumentType getById(Long id) {
        return documentTypeMapper.selectById(id);
    }
    
    public List<DocumentType> getRequiredTypes() {
        LambdaQueryWrapper<DocumentType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DocumentType::getStatus, 1)
               .eq(DocumentType::getIsRequired, 1)
               .orderByAsc(DocumentType::getSortOrder);
        return documentTypeMapper.selectList(wrapper);
    }
}

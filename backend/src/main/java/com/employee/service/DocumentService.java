package com.employee.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.employee.dto.*;
import com.employee.entity.Document;
import com.employee.entity.DocumentHistory;
import com.employee.entity.DocumentType;
import com.employee.entity.Employee;
import com.employee.mapper.DocumentHistoryMapper;
import com.employee.mapper.DocumentMapper;
import com.employee.mapper.EmployeeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DocumentService {
    private final DocumentMapper documentMapper;
    private final DocumentHistoryMapper documentHistoryMapper;
    private final DocumentTypeService documentTypeService;
    private final EmployeeMapper employeeMapper;
    
    public List<Document> getEmployeeDocuments(Long employeeId) {
        return documentMapper.selectByEmployeeId(employeeId);
    }
    
    public Document getDocumentDetail(Long id) {
        return documentMapper.selectDetailById(id);
    }
    
    @Transactional
    public Document uploadDocument(Long employeeId, DocumentUploadRequest request) {
        DocumentType docType = documentTypeService.getById(request.getDocumentTypeId());
        if (docType == null) {
            throw new IllegalArgumentException("证件类型不存在");
        }
        
        Document existingDoc = null;
        LambdaQueryWrapper<Document> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Document::getEmployeeId, employeeId)
               .eq(Document::getDocumentTypeId, request.getDocumentTypeId())
               .eq(Document::getIsArchived, 0);
        List<Document> existingDocs = documentMapper.selectList(wrapper);
        
        if (!existingDocs.isEmpty()) {
            existingDoc = existingDocs.get(0);
            archiveDocument(existingDoc);
        }
        
        Document document = new Document();
        BeanUtils.copyProperties(request, document);
        document.setEmployeeId(employeeId);
        document.setAuditStatus(0);
        document.setVersion(existingDoc != null ? existingDoc.getVersion() + 1 : 1);
        document.setIsArchived(0);
        
        if (docType.getRequireBothSides() == 1 && !StringUtils.hasText(document.getBackImage())) {
            throw new IllegalArgumentException("该证件类型需要上传正反面图片");
        }
        
        documentMapper.insert(document);
        return document;
    }
    
    private void archiveDocument(Document document) {
        DocumentHistory history = new DocumentHistory();
        BeanUtils.copyProperties(document, history);
        history.setDocumentId(document.getId());
        history.setReplacedAt(LocalDateTime.now());
        documentHistoryMapper.insert(history);
        
        document.setIsArchived(1);
        documentMapper.updateById(document);
    }
    
    public void withdrawDocument(Long employeeId, Long documentId) {
        Document document = documentMapper.selectById(documentId);
        if (document == null) {
            throw new IllegalArgumentException("证件不存在");
        }
        
        if (!document.getEmployeeId().equals(employeeId)) {
            throw new IllegalArgumentException("无权操作该证件");
        }
        
        if (document.getAuditStatus() != 0) {
            throw new IllegalArgumentException("只有待审核状态的证件可以撤回");
        }
        
        documentMapper.deleteById(documentId);
    }
    
    public List<DocumentHistory> getDocumentHistory(Long documentId) {
        return documentHistoryMapper.selectByDocumentId(documentId);
    }
    
    @Transactional
    public void auditDocument(DocumentAuditRequest request, Long auditorId) {
        Document document = documentMapper.selectById(request.getDocumentId());
        if (document == null) {
            throw new IllegalArgumentException("证件不存在");
        }
        
        if (document.getAuditStatus() != 0) {
            throw new IllegalArgumentException("该证件已审核");
        }
        
        if (request.getAuditStatus() == 2 && !StringUtils.hasText(request.getAuditReason())) {
            throw new IllegalArgumentException("拒绝时必须填写拒绝理由");
        }
        
        document.setAuditStatus(request.getAuditStatus());
        document.setAuditReason(request.getAuditReason());
        document.setAuditorId(auditorId);
        document.setAuditTime(LocalDateTime.now());
        documentMapper.updateById(document);
    }
    
    public IPage<Document> getPendingAuditDocuments(Integer pageNum, Integer pageSize, Long documentTypeId, String employeeName) {
        Page<Document> page = new Page<>(pageNum, pageSize);
        IPage<Document> resultPage = documentMapper.selectPage(page, buildQueryWrapper(0, documentTypeId, employeeName));
        fillDocumentExtraFields(resultPage.getRecords());
        return resultPage;
    }
    
    public IPage<Document> getAllDocuments(DocumentQueryRequest request) {
        Page<Document> page = new Page<>(request.getPageNum(), request.getPageSize());
        LambdaQueryWrapper<Document> wrapper = buildQueryWrapper(
            request.getAuditStatus(), 
            request.getDocumentTypeId(), 
            request.getEmployeeName()
        );
        
        IPage<Document> resultPage = documentMapper.selectPage(page, wrapper);
        fillDocumentExtraFields(resultPage.getRecords());
        
        if (StringUtils.hasText(request.getExpiryStatus())) {
            List<Document> filtered = new ArrayList<>();
            LocalDate today = LocalDate.now();
            for (Document doc : resultPage.getRecords()) {
                String status = getExpiryStatus(doc, today);
                if (request.getExpiryStatus().equals(status)) {
                    filtered.add(doc);
                }
            }
            resultPage.setRecords(filtered);
        }
        
        return resultPage;
    }
    
    private void fillDocumentExtraFields(List<Document> documents) {
        for (Document doc : documents) {
            DocumentType docType = documentTypeService.getById(doc.getDocumentTypeId());
            if (docType != null) {
                doc.setDocumentTypeName(docType.getTypeName());
                doc.setTypeCode(docType.getTypeCode());
            }
            Employee employee = employeeMapper.selectById(doc.getEmployeeId());
            if (employee != null) {
                doc.setEmployeeName(employee.getName());
            }
        }
    }
    
    private LambdaQueryWrapper<Document> buildQueryWrapper(Integer auditStatus, Long documentTypeId, String employeeName) {
        LambdaQueryWrapper<Document> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Document::getIsArchived, 0);
        
        if (auditStatus != null) {
            wrapper.eq(Document::getAuditStatus, auditStatus);
        }
        
        if (documentTypeId != null) {
            wrapper.eq(Document::getDocumentTypeId, documentTypeId);
        }
        
        if (StringUtils.hasText(employeeName)) {
            List<Employee> employees = employeeMapper.selectList(
                new LambdaQueryWrapper<Employee>().like(Employee::getName, employeeName)
            );
            if (!employees.isEmpty()) {
                List<Long> employeeIds = employees.stream().map(Employee::getId).toList();
                wrapper.in(Document::getEmployeeId, employeeIds);
            } else {
                wrapper.eq(Document::getEmployeeId, -1);
            }
        }
        
        wrapper.orderByDesc(Document::getCreatedAt);
        return wrapper;
    }
    
    private String getExpiryStatus(Document doc, LocalDate today) {
        if (doc.getIsPermanent() != null && doc.getIsPermanent() == 1) {
            return "permanent";
        }
        
        if (doc.getExpiryDate() == null) {
            return "permanent";
        }
        
        long days = ChronoUnit.DAYS.between(today, doc.getExpiryDate());
        
        if (days < 0) {
            return "expired";
        } else if (days <= 30) {
            return "expiring";
        } else {
            return "valid";
        }
    }
    
    public List<DocumentExpiryVO> getExpiringDocuments() {
        List<DocumentExpiryVO> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysLater = today.plusDays(30);
        
        LambdaQueryWrapper<Document> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Document::getIsArchived, 0)
               .eq(Document::getAuditStatus, 1)
               .eq(Document::getIsPermanent, 0)
               .isNotNull(Document::getExpiryDate)
               .le(Document::getExpiryDate, thirtyDaysLater)
               .orderByAsc(Document::getExpiryDate);
        
        List<Document> documents = documentMapper.selectList(wrapper);
        
        for (Document doc : documents) {
            DocumentExpiryVO vo = new DocumentExpiryVO();
            BeanUtils.copyProperties(doc, vo);
            
            DocumentType docType = documentTypeService.getById(doc.getDocumentTypeId());
            if (docType != null) {
                vo.setDocumentTypeName(docType.getTypeName());
            }
            
            Employee employee = employeeMapper.selectById(doc.getEmployeeId());
            if (employee != null) {
                vo.setEmployeeName(employee.getName());
            }
            
            long days = ChronoUnit.DAYS.between(today, doc.getExpiryDate());
            vo.setDaysRemaining((int) days);
            vo.setExpiryStatus(days < 0 ? "expired" : "expiring");
            
            result.add(vo);
        }
        
        return result;
    }
    
    public DocumentStatisticsVO getStatistics() {
        DocumentStatisticsVO stats = new DocumentStatisticsVO();
        
        LambdaQueryWrapper<Employee> empWrapper = new LambdaQueryWrapper<>();
        empWrapper.eq(Employee::getStatus, 1);
        Long totalEmployees = employeeMapper.selectCount(empWrapper);
        stats.setTotalEmployees(totalEmployees.intValue());
        
        List<DocumentType> docTypes = documentTypeService.getAllDocumentTypes();
        Long idCardTypeId = docTypes.stream()
            .filter(t -> "ID_CARD".equals(t.getTypeCode()))
            .map(DocumentType::getId)
            .findFirst().orElse(null);
        Long educationTypeId = docTypes.stream()
            .filter(t -> "EDUCATION".equals(t.getTypeCode()))
            .map(DocumentType::getId)
            .findFirst().orElse(null);
        
        if (idCardTypeId != null) {
            LambdaQueryWrapper<Document> idCardWrapper = new LambdaQueryWrapper<>();
            idCardWrapper.eq(Document::getDocumentTypeId, idCardTypeId)
                        .eq(Document::getIsArchived, 0)
                        .eq(Document::getAuditStatus, 1);
            Long idCardCount = documentMapper.selectCount(idCardWrapper);
            stats.setIdCardCount(idCardCount.intValue());
            stats.setIdCardRate(totalEmployees > 0 ? (idCardCount * 100.0 / totalEmployees) : 0.0);
        }
        
        if (educationTypeId != null) {
            LambdaQueryWrapper<Document> eduWrapper = new LambdaQueryWrapper<>();
            eduWrapper.eq(Document::getDocumentTypeId, educationTypeId)
                     .eq(Document::getIsArchived, 0)
                     .eq(Document::getAuditStatus, 1);
            Long eduCount = documentMapper.selectCount(eduWrapper);
            stats.setEducationCount(eduCount.intValue());
            stats.setEducationRate(totalEmployees > 0 ? (eduCount * 100.0 / totalEmployees) : 0.0);
        }
        
        List<DocumentExpiryVO> expiringList = getExpiringDocuments();
        int expiredCount = (int) expiringList.stream().filter(d -> "expired".equals(d.getExpiryStatus())).count();
        int expiringCount = (int) expiringList.stream().filter(d -> "expiring".equals(d.getExpiryStatus())).count();
        
        stats.setExpiredCount(expiredCount);
        stats.setExpiringCount(expiringCount);
        
        LocalDate today = LocalDate.now();
        LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());
        int expiringThisMonth = (int) expiringList.stream()
            .filter(d -> d.getExpiryDate() != null && 
                        !d.getExpiryDate().isBefore(today) && 
                        !d.getExpiryDate().isAfter(monthEnd))
            .count();
        stats.setExpiringThisMonth(expiringThisMonth);
        
        LambdaQueryWrapper<Document> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(Document::getAuditStatus, 0)
                      .eq(Document::getIsArchived, 0);
        Long pendingCount = documentMapper.selectCount(pendingWrapper);
        stats.setPendingAuditCount(pendingCount.intValue());
        
        return stats;
    }
    
    public List<DocumentExpiryVO> getEmployeeExpiryAlerts(Long employeeId) {
        List<DocumentExpiryVO> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        List<Document> documents = documentMapper.selectByEmployeeId(employeeId);
        
        for (Document doc : documents) {
            if (doc.getAuditStatus() != 1) continue;
            if (doc.getIsPermanent() != null && doc.getIsPermanent() == 1) continue;
            if (doc.getExpiryDate() == null) continue;
            
            long days = ChronoUnit.DAYS.between(today, doc.getExpiryDate());
            
            if (days <= 30) {
                DocumentExpiryVO vo = new DocumentExpiryVO();
                vo.setId(doc.getId());
                vo.setEmployeeId(doc.getEmployeeId());
                vo.setExpiryDate(doc.getExpiryDate());
                vo.setDaysRemaining((int) days);
                vo.setExpiryStatus(days < 0 ? "expired" : "expiring");
                vo.setDocumentTypeName(doc.getDocumentTypeName());
                result.add(vo);
            }
        }
        
        return result;
    }
    
    public List<Map<String, Object>> getEmployeeCompleteness() {
        List<Map<String, Object>> result = new ArrayList<>();
        
        List<DocumentType> requiredTypes = documentTypeService.getAllDocumentTypes();
        int totalRequired = (int) requiredTypes.stream().filter(t -> t.getIsRequired() == 1).count();
        
        LambdaQueryWrapper<Employee> empWrapper = new LambdaQueryWrapper<>();
        empWrapper.eq(Employee::getStatus, 1);
        List<Employee> employees = employeeMapper.selectList(empWrapper);
        
        for (Employee emp : employees) {
            Map<String, Object> item = new HashMap<>();
            item.put("employeeId", emp.getId());
            item.put("employeeName", emp.getName());
            item.put("department", emp.getDepartment());
            item.put("totalRequired", totalRequired);
            
            LambdaQueryWrapper<Document> docWrapper = new LambdaQueryWrapper<>();
            docWrapper.eq(Document::getEmployeeId, emp.getId())
                      .eq(Document::getIsArchived, 0);
            Long uploadedCount = documentMapper.selectCount(docWrapper);
            item.put("uploadedCount", uploadedCount.intValue());
            
            result.add(item);
        }
        
        return result;
    }
    
    @Transactional
    public void adminDeleteDocument(Long documentId) {
        Document document = documentMapper.selectById(documentId);
        if (document == null) {
            throw new IllegalArgumentException("证件不存在");
        }
        
        List<DocumentHistory> histories = documentHistoryMapper.selectByDocumentId(documentId);
        for (DocumentHistory history : histories) {
            documentHistoryMapper.deleteById(history.getId());
        }
        
        documentMapper.deleteById(documentId);
    }
}

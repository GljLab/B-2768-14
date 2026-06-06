package com.employee.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.employee.common.Result;
import com.employee.dto.DocumentAuditRequest;
import com.employee.dto.DocumentExpiryVO;
import com.employee.dto.DocumentQueryRequest;
import com.employee.dto.DocumentStatisticsVO;
import com.employee.entity.Document;
import com.employee.entity.DocumentHistory;
import com.employee.entity.DocumentType;
import com.employee.service.DocumentService;
import com.employee.service.DocumentTypeService;
import com.employee.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/documents")
@RequiredArgsConstructor
@Tag(name = "管理员证件管理", description = "管理员端证件审核与管理接口")
public class AdminDocumentController {
    private final DocumentService documentService;
    private final DocumentTypeService documentTypeService;
    private final JwtUtil jwtUtil;
    
    @GetMapping("/types")
    @Operation(summary = "获取证件类型列表", description = "获取所有可用的证件类型")
    public Result<List<DocumentType>> getDocumentTypes() {
        return Result.success(documentTypeService.getAllDocumentTypes());
    }
    
    @GetMapping("/pending")
    @Operation(summary = "获取待审核证件列表", description = "分页获取所有待审核的证件")
    public Result<IPage<Document>> getPendingAuditList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long documentTypeId,
            @RequestParam(required = false) String employeeName) {
        return Result.success(documentService.getPendingAuditDocuments(pageNum, pageSize, documentTypeId, employeeName));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取证件详情", description = "管理员获取证件详细信息用于审核")
    public Result<Document> getDocumentDetail(@PathVariable Long id) {
        Document document = documentService.getDocumentDetail(id);
        if (document == null) {
            return Result.error("证件不存在");
        }
        return Result.success(document);
    }
    
    @PostMapping("/audit")
    @Operation(summary = "审核证件", description = "管理员审核证件，通过或拒绝")
    public Result<String> auditDocument(@RequestHeader("Authorization") String authHeader,
                                        @Valid @RequestBody DocumentAuditRequest request) {
        String token = authHeader.substring(7);
        Long adminId = jwtUtil.getAdminIdFromToken(token);
        documentService.auditDocument(request, adminId);
        return Result.success("审核完成");
    }
    
    @GetMapping
    @Operation(summary = "证件档案查询", description = "多条件查询所有证件档案")
    public Result<IPage<Document>> queryDocuments(DocumentQueryRequest request) {
        return Result.success(documentService.getAllDocuments(request));
    }
    
    @GetMapping("/{id}/history")
    @Operation(summary = "获取证件历史版本", description = "获取指定证件的所有历史版本")
    public Result<List<DocumentHistory>> getDocumentHistory(@PathVariable Long id) {
        return Result.success(documentService.getDocumentHistory(id));
    }
    
    @GetMapping("/expiry-monitor")
    @Operation(summary = "证件到期监控", description = "获取所有即将到期和已过期的证件列表")
    public Result<List<DocumentExpiryVO>> getExpiryMonitor() {
        return Result.success(documentService.getExpiringDocuments());
    }
    
    @GetMapping("/statistics")
    @Operation(summary = "证件统计数据", description = "获取证件完整度和到期情况统计")
    public Result<DocumentStatisticsVO> getStatistics() {
        return Result.success(documentService.getStatistics());
    }
    
    @GetMapping("/employee-completeness")
    @Operation(summary = "员工证件完整度", description = "获取所有员工的证件上传完整度")
    public Result<List<Map<String, Object>>> getEmployeeCompleteness() {
        return Result.success(documentService.getEmployeeCompleteness());
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除证件", description = "管理员彻底删除证件，需二次确认")
    public Result<String> deleteDocument(@PathVariable Long id) {
        documentService.adminDeleteDocument(id);
        return Result.success("证件已删除");
    }
}

package com.employee.controller;

import com.employee.common.Result;
import com.employee.dto.DocumentExpiryVO;
import com.employee.dto.DocumentUploadRequest;
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

@RestController
@RequestMapping("/api/employee/documents")
@RequiredArgsConstructor
@Tag(name = "员工证件管理", description = "员工端证件相关接口")
public class EmployeeDocumentController {
    private final DocumentService documentService;
    private final DocumentTypeService documentTypeService;
    private final JwtUtil jwtUtil;
    
    @GetMapping("/types")
    @Operation(summary = "获取证件类型列表", description = "获取所有可用的证件类型")
    public Result<List<DocumentType>> getDocumentTypes() {
        return Result.success(documentTypeService.getAllDocumentTypes());
    }
    
    @GetMapping
    @Operation(summary = "获取我的证件列表", description = "获取当前登录员工的所有证件")
    public Result<List<Document>> getMyDocuments(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        Long employeeId = jwtUtil.getEmployeeIdFromToken(token);
        return Result.success(documentService.getEmployeeDocuments(employeeId));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取证件详情", description = "获取指定证件的详细信息")
    public Result<Document> getDocumentDetail(@RequestHeader("Authorization") String authHeader,
                                              @PathVariable Long id) {
        String token = authHeader.substring(7);
        Long employeeId = jwtUtil.getEmployeeIdFromToken(token);
        Document document = documentService.getDocumentDetail(id);
        
        if (document == null || !document.getEmployeeId().equals(employeeId)) {
            return Result.error("证件不存在或无权访问");
        }
        
        return Result.success(document);
    }
    
    @PostMapping
    @Operation(summary = "上传证件", description = "员工上传新证件")
    public Result<Document> uploadDocument(@RequestHeader("Authorization") String authHeader,
                                           @Valid @RequestBody DocumentUploadRequest request) {
        String token = authHeader.substring(7);
        Long employeeId = jwtUtil.getEmployeeIdFromToken(token);
        Document document = documentService.uploadDocument(employeeId, request);
        return Result.success("证件上传成功，等待审核", document);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "撤回证件", description = "撤回待审核状态的证件")
    public Result<String> withdrawDocument(@RequestHeader("Authorization") String authHeader,
                                           @PathVariable Long id) {
        String token = authHeader.substring(7);
        Long employeeId = jwtUtil.getEmployeeIdFromToken(token);
        documentService.withdrawDocument(employeeId, id);
        return Result.success("证件已撤回");
    }
    
    @GetMapping("/{id}/history")
    @Operation(summary = "获取证件历史版本", description = "获取指定证件的所有历史版本")
    public Result<List<DocumentHistory>> getDocumentHistory(@PathVariable Long id) {
        return Result.success(documentService.getDocumentHistory(id));
    }
    
    @GetMapping("/expiry-alerts")
    @Operation(summary = "获取到期提醒", description = "获取当前员工即将到期或已过期的证件")
    public Result<List<DocumentExpiryVO>> getExpiryAlerts(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        Long employeeId = jwtUtil.getEmployeeIdFromToken(token);
        return Result.success(documentService.getEmployeeExpiryAlerts(employeeId));
    }
}

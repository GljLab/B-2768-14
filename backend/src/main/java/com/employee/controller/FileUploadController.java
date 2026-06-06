package com.employee.controller;

import com.employee.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@Tag(name = "文件上传", description = "文件上传相关接口")
public class FileUploadController {
    
    @Value("${file.upload.path:./uploads}")
    private String uploadPath;
    
    @Value("${file.upload.max-size:2097152}")
    private Long maxFileSize;
    
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
        "image/jpeg",
        "image/jpg",
        "image/png"
    );
    
    private static final List<String> ALLOWED_DOCUMENT_TYPES = Arrays.asList(
        "image/jpeg",
        "image/jpg",
        "image/png",
        "application/pdf"
    );
    
    private static final List<String> ALLOWED_DOCUMENT_EXTENSIONS = Arrays.asList(
        ".jpg",
        ".jpeg",
        ".png",
        ".pdf"
    );
    
    private static final List<String> ALLOWED_ANNOUNCEMENT_TYPES = Arrays.asList(
        "image/jpeg",
        "image/jpg",
        "image/png",
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "application/vnd.ms-powerpoint",
        "application/vnd.openxmlformats-officedocument.presentationml.presentation"
    );
    
    private static final List<String> ALLOWED_ANNOUNCEMENT_EXTENSIONS = Arrays.asList(
        ".jpg",
        ".jpeg",
        ".png",
        ".pdf",
        ".doc",
        ".docx",
        ".xls",
        ".xlsx",
        ".ppt",
        ".pptx"
    );
    
    @PostMapping("/avatar")
    @Operation(summary = "上传头像", description = "上传头像图片，支持JPG、PNG格式，大小不超过2MB")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("请选择要上传的文件");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            return Result.error("仅支持JPG或PNG格式的图片");
        }
        
        if (file.getSize() > maxFileSize) {
            return Result.error("图片大小不能超过2MB");
        }
        
        try {
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : ".jpg";
            
            String newFilename = UUID.randomUUID().toString() + extension;
            
            Path filePath = Paths.get(uploadPath, newFilename);
            Files.write(filePath, file.getBytes());
            
            return Result.success("/uploads/" + newFilename);
        } catch (IOException e) {
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/document")
    @Operation(summary = "上传证件", description = "上传证件文件，支持JPG、PNG、PDF格式，大小不超过5MB")
    public Result<String> uploadDocument(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("请选择要上传的文件");
        }
        
        long maxDocumentSize = 5 * 1024 * 1024;
        if (file.getSize() > maxDocumentSize) {
            return Result.error("文件大小不能超过5MB");
        }
        
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
            ? originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase() 
            : "";
        
        boolean validType = false;
        if (contentType != null && ALLOWED_DOCUMENT_TYPES.contains(contentType.toLowerCase())) {
            validType = true;
        }
        if (!validType && ALLOWED_DOCUMENT_EXTENSIONS.contains(extension)) {
            validType = true;
        }
        
        if (!validType) {
            return Result.error("仅支持JPG、PNG或PDF格式的文件");
        }
        
        try {
            String documentPath = uploadPath + "/documents";
            File uploadDir = new File(documentPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            
            if (extension.isEmpty()) {
                extension = ".jpg";
            }
            
            String newFilename = UUID.randomUUID().toString() + extension;
            
            Path filePath = Paths.get(documentPath, newFilename);
            Files.write(filePath, file.getBytes());
            
            return Result.success("/uploads/documents/" + newFilename);
        } catch (IOException e) {
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/announcement")
    @Operation(summary = "上传信息附件", description = "上传信息附件，支持PDF、Office文件、图片格式，大小不超过20MB")
    public Result<java.util.Map<String, Object>> uploadAnnouncementAttachment(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("请选择要上传的文件");
        }
        
        long maxAnnouncementSize = 20 * 1024 * 1024;
        if (file.getSize() > maxAnnouncementSize) {
            return Result.error("文件大小不能超过20MB");
        }
        
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
            ? originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase() 
            : "";
        
        boolean validType = false;
        if (contentType != null && ALLOWED_ANNOUNCEMENT_TYPES.contains(contentType.toLowerCase())) {
            validType = true;
        }
        if (!validType && ALLOWED_ANNOUNCEMENT_EXTENSIONS.contains(extension)) {
            validType = true;
        }
        
        if (!validType) {
            return Result.error("仅支持PDF、Office文件、图片格式");
        }
        
        try {
            String announcementPath = uploadPath + "/announcements";
            File uploadDir = new File(announcementPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            
            if (extension.isEmpty()) {
                extension = ".dat";
            }
            
            String newFilename = UUID.randomUUID().toString() + extension;
            
            Path filePath = Paths.get(announcementPath, newFilename);
            Files.write(filePath, file.getBytes());
            
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("url", "/uploads/announcements/" + newFilename);
            result.put("name", originalFilename);
            result.put("size", file.getSize());
            
            return Result.success(result);
        } catch (IOException e) {
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/party-photo")
    @Operation(summary = "上传生日会照片", description = "上传生日会活动照片，支持JPG、PNG格式，大小不超过10MB")
    public Result<String> uploadPartyPhoto(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("请选择要上传的文件");
        }

        long maxPartyPhotoSize = 10 * 1024 * 1024;
        if (file.getSize() > maxPartyPhotoSize) {
            return Result.error("照片大小不能超过10MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            return Result.error("仅支持JPG或PNG格式的图片");
        }

        try {
            String partyPath = uploadPath + "/party-photos";
            File uploadDir = new File(partyPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";

            long timestamp = System.currentTimeMillis();
            String randomSuffix = UUID.randomUUID().toString().substring(0, 6);
            String newFilename = timestamp + "_" + randomSuffix + extension;

            Path filePath = Paths.get(partyPath, newFilename);
            Files.write(filePath, file.getBytes());

            return Result.success("/uploads/party-photos/" + newFilename);
        } catch (IOException e) {
            return Result.error("照片上传失败: " + e.getMessage());
        }
    }
}

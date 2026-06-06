package com.employee.controller;

import com.employee.common.Result;
import com.employee.dto.*;
import com.employee.entity.AdminUser;
import com.employee.entity.Employee;
import com.employee.mapper.AdminUserMapper;
import com.employee.mapper.BirthdayPartyMapper;
import com.employee.mapper.BirthdayPartyParticipantMapper;
import com.employee.mapper.BirthdayWishMapper;
import com.employee.mapper.EmployeeMapper;
import com.employee.service.BirthdayMessageService;
import com.employee.service.BirthdayMilestoneService;
import com.employee.service.BirthdayPartyService;
import com.employee.service.BirthdayStatisticsService;
import com.employee.service.BirthdayTimelineService;
import com.employee.service.BirthdayWishService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/birthday")
@RequiredArgsConstructor
@Tag(name = "生日关怀-管理", description = "管理员生日关怀相关接口")
public class AdminBirthdayController {

    private final BirthdayWishService wishService;
    private final BirthdayPartyService partyService;
    private final BirthdayMessageService messageService;
    private final BirthdayStatisticsService statisticsService;
    private final BirthdayTimelineService timelineService;
    private final BirthdayMilestoneService milestoneService;
    private final BirthdayWishMapper wishMapper;
    private final BirthdayPartyParticipantMapper participantMapper;
    private final BirthdayPartyMapper partyMapper;
    private final EmployeeMapper employeeMapper;
    private final AdminUserMapper adminUserMapper;

    private Long getUserId(HttpServletRequest request) {
        Long employeeId = (Long) request.getAttribute("employeeId");
        if (employeeId != null) return employeeId;
        Long adminId = (Long) request.getAttribute("adminId");
        if (adminId != null) return -adminId;
        return null;
    }

    private String getUserName(HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("adminId");
        if (adminId != null) {
            AdminUser admin = adminUserMapper.selectById(adminId);
            if (admin != null) return admin.getUsername();
            return "管理员";
        }
        Long employeeId = (Long) request.getAttribute("employeeId");
        if (employeeId != null) {
            Employee emp = employeeMapper.selectById(employeeId);
            if (emp != null) return emp.getName();
        }
        return "管理员";
    }

    @GetMapping("/wall")
    @Operation(summary = "生日墙")
    public Result<?> getBirthdayWall(@RequestParam(required = false) Integer year,
                                      @RequestParam(required = false) Integer month) {
        LocalDate now = LocalDate.now();
        int y = year != null ? year : now.getYear();
        int m = month != null ? month : now.getMonthValue();
        return Result.success(wishService.getBirthdayWallEmployees(y, m));
    }

    @GetMapping("/wishes/{recipientId}")
    @Operation(summary = "查看祝福列表")
    public Result<?> getWishes(@PathVariable Long recipientId, HttpServletRequest request) {
        Long userId = getUserId(request);
        return Result.success(wishService.getWishesForEmployee(recipientId, userId));
    }

    @PostMapping("/wish")
    @Operation(summary = "送祝福")
    public Result<?> sendWish(HttpServletRequest request, @Valid @RequestBody BirthdayWishRequest requestObj) {
        Long userId = getUserId(request);
        String userName = getUserName(request);
        return wishService.sendWishFromAdmin(userId, userName, requestObj);
    }

    @PostMapping("/wish/{wishId}/like")
    @Operation(summary = "点赞祝福")
    public Result<?> likeWish(@PathVariable Long wishId, HttpServletRequest request) {
        Long userId = getUserId(request);
        return wishService.likeWish(wishId, userId);
    }

    @DeleteMapping("/wish/{wishId}/like")
    @Operation(summary = "取消点赞")
    public Result<?> unlikeWish(@PathVariable Long wishId, HttpServletRequest request) {
        Long userId = getUserId(request);
        return wishService.unlikeWish(wishId, userId);
    }

    @PostMapping("/party")
    @Operation(summary = "发起生日会")
    public Result<?> createParty(HttpServletRequest request, @Valid @RequestBody BirthdayPartyRequest requestObj) {
        Long adminId = (Long) request.getAttribute("adminId");
        if (adminId == null) {
            adminId = getUserId(request);
        }
        return partyService.createParty(requestObj, adminId);
    }

    @PutMapping("/party/{partyId}/status")
    @Operation(summary = "更新生日会状态")
    public Result<?> updatePartyStatus(@PathVariable Long partyId, @RequestParam Integer status) {
        return partyService.updatePartyStatus(partyId, status);
    }

    @GetMapping("/party/list")
    @Operation(summary = "生日会列表")
    public Result<?> getPartyList(@RequestParam(required = false) Integer year,
                                  @RequestParam(required = false) Integer month) {
        return Result.success(partyService.getPartyList(year, month));
    }

    @GetMapping("/party/{partyId}")
    @Operation(summary = "生日会详情")
    public Result<?> getPartyDetail(@PathVariable Long partyId) {
        return Result.success(partyService.getPartyDetail(partyId));
    }

    @GetMapping("/party/{partyId}/participants")
    @Operation(summary = "查看参与者")
    public Result<?> getParticipants(@PathVariable Long partyId) {
        return Result.success(partyService.getParticipants(partyId));
    }

    @PostMapping("/party/checkin")
    @Operation(summary = "签到")
    public Result<?> checkin(@Valid @RequestBody CheckinRequest request) {
        return partyService.checkin(request);
    }

    @PostMapping("/party/{partyId}/photos")
    @Operation(summary = "上传照片")
    public Result<?> uploadPhotos(@PathVariable Long partyId,
                                  @RequestBody List<String> photoUrls,
                                  HttpServletRequest request) {
        Long userId = getUserId(request);
        String userName = getUserName(request);
        return partyService.uploadPhotos(partyId, photoUrls, userId, userName);
    }

    @DeleteMapping("/party/photo/{photoId}")
    @Operation(summary = "删除照片")
    public Result<?> deletePhoto(@PathVariable Long photoId, HttpServletRequest request) {
        Long userId = getUserId(request);
        return partyService.deletePhoto(photoId, userId, true);
    }

    @GetMapping("/party/{partyId}/photos")
    @Operation(summary = "查看照片")
    public Result<?> getPhotos(@PathVariable Long partyId, HttpServletRequest request) {
        Long userId = getUserId(request);
        return Result.success(partyService.getPhotos(partyId, userId));
    }

    @PostMapping("/party/{photoId}/photo-like")
    @Operation(summary = "点赞照片")
    public Result<?> likePhoto(@PathVariable Long photoId, HttpServletRequest request) {
        Long userId = getUserId(request);
        return partyService.likePhoto(photoId, userId);
    }

    @DeleteMapping("/party/{photoId}/photo-like")
    @Operation(summary = "取消点赞照片")
    public Result<?> unlikePhoto(@PathVariable Long photoId, HttpServletRequest request) {
        Long userId = getUserId(request);
        return partyService.unlikePhoto(photoId, userId);
    }

    @GetMapping("/statistics")
    @Operation(summary = "数据统计")
    public Result<?> getStatistics() {
        return Result.success(statisticsService.getStatistics());
    }

    @GetMapping("/export/employees")
    @Operation(summary = "导出生日员工名单")
    public ResponseEntity<byte[]> exportBirthdayEmployees(@RequestParam int month) {
        int year = LocalDate.now().getYear();
        byte[] data = statisticsService.exportBirthdayEmployees(year, month);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=birthday_employees.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }

    @GetMapping("/export/wishes")
    @Operation(summary = "导出祝福互动报表")
    public ResponseEntity<byte[]> exportWishReport(@RequestParam(required = false) Integer year) {
        if (year == null) {
            year = LocalDate.now().getYear();
        }
        byte[] data = statisticsService.exportWishReport(year);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=wish_report.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }

    @GetMapping("/profile/{employeeId}")
    @Operation(summary = "查看员工生日档案")
    public Result<?> getEmployeeBirthdayProfile(@PathVariable Long employeeId, HttpServletRequest request) {
        Long adminId = getUserId(request);
        BirthdayProfileVO vo = new BirthdayProfileVO();
        vo.setYearWishes(wishService.getWishesForEmployee(employeeId, employeeId));
        vo.setTotalWishes(vo.getYearWishes().size());
        vo.setParties(partyService.getPartyList(null, null));
        vo.setTotalParties(vo.getParties().size());
        vo.setGrowthData(timelineService.getGrowthData(employeeId));
        vo.setTimeline(timelineService.getTimeline(employeeId, adminId, true));
        return Result.success(vo);
    }

    @GetMapping("/timeline/{employeeId}")
    @Operation(summary = "查看员工生日时间轴")
    public Result<?> getEmployeeTimeline(@PathVariable Long employeeId, HttpServletRequest request) {
        Long adminId = getUserId(request);
        return Result.success(timelineService.getTimeline(employeeId, adminId, true));
    }

    @PostMapping("/admin-message")
    @Operation(summary = "写管理员寄语")
    public Result<?> saveAdminMessage(@Valid @RequestBody AdminMessageRequest request,
                                      HttpServletRequest httpRequest) {
        Long adminId = getUserId(httpRequest);
        String adminName = getUserName(httpRequest);
        return timelineService.saveAdminMessage(adminId, adminName, request.getEmployeeId(),
                request.getYear(), request.getContent());
    }

    @PutMapping("/admin-message/{messageId}")
    @Operation(summary = "修改管理员寄语")
    public Result<?> updateAdminMessage(@PathVariable Long messageId,
                                        @RequestBody AdminMessageRequest request,
                                        HttpServletRequest httpRequest) {
        Long adminId = getUserId(httpRequest);
        return timelineService.updateAdminMessage(messageId, adminId, request.getContent());
    }

    @DeleteMapping("/admin-message/{messageId}")
    @Operation(summary = "删除管理员寄语")
    public Result<?> deleteAdminMessage(@PathVariable Long messageId, HttpServletRequest httpRequest) {
        Long adminId = getUserId(httpRequest);
        return timelineService.deleteAdminMessage(messageId, adminId);
    }

    @GetMapping("/milestones")
    @Operation(summary = "生日大事记")
    public Result<?> getMilestones(@RequestParam(required = false) Integer year,
                                   @RequestParam(required = false) Integer month,
                                   @RequestParam(required = false) String keyword) {
        return Result.success(milestoneService.getMilestones(year, month, keyword));
    }

    @GetMapping("/poster/{employeeId}")
    @Operation(summary = "获取员工纪念海报数据")
    public Result<?> getEmployeePosterData(@PathVariable Long employeeId) {
        return Result.success(timelineService.getPosterData(employeeId));
    }
}

package com.employee.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.employee.common.Result;
import com.employee.dto.*;
import com.employee.entity.BirthdayPartyParticipant;
import com.employee.entity.BirthdayWish;
import com.employee.mapper.BirthdayPartyParticipantMapper;
import com.employee.mapper.BirthdayWishMapper;
import com.employee.service.BirthdayMessageService;
import com.employee.service.BirthdayPartyService;
import com.employee.service.BirthdayTimelineService;
import com.employee.service.BirthdayWishService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/employee/birthday")
@RequiredArgsConstructor
@Tag(name = "生日关怀-员工", description = "员工生日关怀相关接口")
public class BirthdayController {

    private final BirthdayWishService wishService;
    private final BirthdayPartyService partyService;
    private final BirthdayMessageService messageService;
    private final BirthdayTimelineService timelineService;
    private final BirthdayWishMapper wishMapper;
    private final BirthdayPartyParticipantMapper participantMapper;

    @GetMapping("/wall")
    @Operation(summary = "生日墙")
    public Result<List<?>> getBirthdayWall(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        LocalDate now = LocalDate.now();
        int y = year != null ? year : now.getYear();
        int m = month != null ? month : now.getMonthValue();
        return Result.success(wishService.getBirthdayWallEmployees(y, m));
    }

    @GetMapping("/wishes/{recipientId}")
    @Operation(summary = "查看祝福列表")
    public Result<List<BirthdayWishVO>> getWishes(@PathVariable Long recipientId,
                                                   HttpServletRequest request) {
        Long currentEmployeeId = (Long) request.getAttribute("employeeId");
        return Result.success(wishService.getWishesForEmployee(recipientId, currentEmployeeId));
    }

    @PostMapping("/wish")
    @Operation(summary = "送祝福")
    public Result<?> sendWish(@Valid @RequestBody BirthdayWishRequest request,
                              HttpServletRequest httpRequest) {
        Long currentEmployeeId = (Long) httpRequest.getAttribute("employeeId");
        return wishService.sendWish(currentEmployeeId, request);
    }

    @PostMapping("/wish/{wishId}/like")
    @Operation(summary = "点赞祝福")
    public Result<?> likeWish(@PathVariable Long wishId,
                              HttpServletRequest request) {
        Long currentEmployeeId = (Long) request.getAttribute("employeeId");
        return wishService.likeWish(wishId, currentEmployeeId);
    }

    @DeleteMapping("/wish/{wishId}/like")
    @Operation(summary = "取消点赞祝福")
    public Result<?> unlikeWish(@PathVariable Long wishId,
                                HttpServletRequest request) {
        Long currentEmployeeId = (Long) request.getAttribute("employeeId");
        return wishService.unlikeWish(wishId, currentEmployeeId);
    }

    @GetMapping("/party/list")
    @Operation(summary = "生日会列表")
    public Result<List<BirthdayPartyVO>> getPartyList(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        return Result.success(partyService.getPartyList(year, month));
    }

    @GetMapping("/party/{partyId}")
    @Operation(summary = "生日会详情")
    public Result<BirthdayPartyVO> getPartyDetail(@PathVariable Long partyId) {
        return Result.success(partyService.getPartyDetail(partyId));
    }

    @PutMapping("/party/{partyId}/participation")
    @Operation(summary = "确认参加")
    public Result<?> updateParticipation(@PathVariable Long partyId,
                                         @Valid @RequestBody PartyParticipationRequest request,
                                         HttpServletRequest httpRequest) {
        Long currentEmployeeId = (Long) httpRequest.getAttribute("employeeId");
        return partyService.updateParticipation(partyId, currentEmployeeId, request);
    }

    @GetMapping("/party/{partyId}/participants")
    @Operation(summary = "查看参与者")
    public Result<?> getParticipants(@PathVariable Long partyId) {
        return Result.success(partyService.getParticipants(partyId));
    }

    @GetMapping("/party/{partyId}/photos")
    @Operation(summary = "查看照片")
    public Result<?> getPhotos(@PathVariable Long partyId,
                               HttpServletRequest request) {
        Long currentEmployeeId = (Long) request.getAttribute("employeeId");
        return Result.success(partyService.getPhotos(partyId, currentEmployeeId));
    }

    @PostMapping("/party/{photoId}/photo-like")
    @Operation(summary = "点赞照片")
    public Result<?> likePhoto(@PathVariable Long photoId,
                               HttpServletRequest request) {
        Long currentEmployeeId = (Long) request.getAttribute("employeeId");
        return partyService.likePhoto(photoId, currentEmployeeId);
    }

    @DeleteMapping("/party/{photoId}/photo-like")
    @Operation(summary = "取消点赞照片")
    public Result<?> unlikePhoto(@PathVariable Long photoId,
                                 HttpServletRequest request) {
        Long currentEmployeeId = (Long) request.getAttribute("employeeId");
        return partyService.unlikePhoto(photoId, currentEmployeeId);
    }

    @GetMapping("/messages")
    @Operation(summary = "我的消息列表")
    public Result<?> getMessages(HttpServletRequest request) {
        Long currentEmployeeId = (Long) request.getAttribute("employeeId");
        return Result.success(messageService.getMessages(currentEmployeeId));
    }

    @GetMapping("/messages/unread-count")
    @Operation(summary = "未读消息数")
    public Result<Integer> getUnreadCount(HttpServletRequest request) {
        Long currentEmployeeId = (Long) request.getAttribute("employeeId");
        return Result.success(messageService.getUnreadCount(currentEmployeeId));
    }

    @PutMapping("/messages/{messageId}/read")
    @Operation(summary = "标记已读")
    public Result<?> markAsRead(@PathVariable Long messageId,
                                HttpServletRequest request) {
        Long currentEmployeeId = (Long) request.getAttribute("employeeId");
        return messageService.markAsRead(messageId, currentEmployeeId);
    }

    @PutMapping("/messages/read-all")
    @Operation(summary = "全部标记已读")
    public Result<?> markAllAsRead(HttpServletRequest request) {
        Long currentEmployeeId = (Long) request.getAttribute("employeeId");
        return messageService.markAllAsRead(currentEmployeeId);
    }

    @GetMapping("/profile")
    @Operation(summary = "我的生日档案")
    public Result<BirthdayProfileVO> getProfile(HttpServletRequest request) {
        Long currentEmployeeId = (Long) request.getAttribute("employeeId");
        LocalDate now = LocalDate.now();
        int year = now.getYear();

        BirthdayProfileVO profile = new BirthdayProfileVO();

        QueryWrapper<BirthdayWish> wishWrapper = new QueryWrapper<>();
        wishWrapper.eq("recipient_id", currentEmployeeId)
                .apply("YEAR(created_at) = {0}", year);
        List<BirthdayWishVO> yearWishes = wishService.getWishesForEmployee(currentEmployeeId, currentEmployeeId)
                .stream().filter(w -> w.getCreatedAt() != null && w.getCreatedAt().getYear() == year)
                .toList();
        profile.setYearWishes(yearWishes);

        List<BirthdayPartyVO> parties = partyService.getPartyList(year, null);
        profile.setParties(parties);

        QueryWrapper<BirthdayWish> totalWishWrapper = new QueryWrapper<>();
        totalWishWrapper.eq("recipient_id", currentEmployeeId);
        Long totalWishCount = wishMapper.selectCount(totalWishWrapper);
        profile.setTotalWishes(totalWishCount.intValue());

        QueryWrapper<BirthdayPartyParticipant> totalPartyWrapper = new QueryWrapper<>();
        totalPartyWrapper.eq("employee_id", currentEmployeeId).eq("participation_status", 1);
        Long totalPartyCount = participantMapper.selectCount(totalPartyWrapper);
        profile.setTotalParties(totalPartyCount.intValue());

        profile.setGrowthData(timelineService.getGrowthData(currentEmployeeId));
        profile.setTimeline(timelineService.getTimeline(currentEmployeeId, currentEmployeeId, false));

        return Result.success(profile);
    }

    @GetMapping("/timeline")
    @Operation(summary = "我的生日时间轴")
    public Result<?> getTimeline(HttpServletRequest request) {
        Long currentEmployeeId = (Long) request.getAttribute("employeeId");
        return Result.success(timelineService.getTimeline(currentEmployeeId, currentEmployeeId, false));
    }

    @GetMapping("/growth-data")
    @Operation(summary = "我的成长数据")
    public Result<?> getGrowthData(HttpServletRequest request) {
        Long currentEmployeeId = (Long) request.getAttribute("employeeId");
        return Result.success(timelineService.getGrowthData(currentEmployeeId));
    }

    @PostMapping("/yearly-message")
    @Operation(summary = "保存我的年度寄语")
    public Result<?> saveYearlyMessage(@Valid @RequestBody YearlyMessageRequest request,
                                       HttpServletRequest httpRequest) {
        Long currentEmployeeId = (Long) httpRequest.getAttribute("employeeId");
        return timelineService.saveEmployeeMessage(currentEmployeeId, request.getYear(), request.getContent());
    }

    @GetMapping("/poster")
    @Operation(summary = "获取纪念海报数据")
    public Result<?> getPosterData(HttpServletRequest request) {
        Long currentEmployeeId = (Long) request.getAttribute("employeeId");
        return Result.success(timelineService.getPosterData(currentEmployeeId));
    }
}

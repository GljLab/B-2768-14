package com.employee.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.employee.common.Result;
import com.employee.dto.*;
import com.employee.entity.*;
import com.employee.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BirthdayPartyService {

    private final BirthdayPartyMapper partyMapper;
    private final BirthdayPartyParticipantMapper participantMapper;
    private final BirthdayPartyPhotoMapper photoMapper;
    private final BirthdayPartyPhotoLikeMapper photoLikeMapper;
    private final BirthdayWishMapper wishMapper;
    private final BirthdayWishLikeMapper wishLikeMapper;
    private final EmployeeMapper employeeMapper;
    private final BirthdayMessageMapper messageMapper;

    public Result<?> createParty(BirthdayPartyRequest request, Long adminId) {
        LocalDateTime eventTime = request.getEventTime();
        int year = eventTime.getYear();
        int month = eventTime.getMonthValue();

        QueryWrapper<BirthdayParty> qw = new QueryWrapper<>();
        qw.eq("party_year", year).eq("party_month", month);
        Long count = partyMapper.selectCount(qw);
        if (count > 0) {
            return Result.error("本月生日会已发布");
        }

        BirthdayParty party = new BirthdayParty();
        party.setTheme(request.getTheme());
        party.setPartyYear(year);
        party.setPartyMonth(month);
        party.setEventTime(request.getEventTime());
        party.setLocation(request.getLocation());
        party.setFlow(request.getFlow());
        party.setHighlights(request.getHighlights());
        party.setCoverImage(request.getCoverImage());
        party.setCreatedBy(adminId);
        party.setStatus(0);

        Employee admin = employeeMapper.selectById(adminId);
        String adminName = admin != null ? admin.getName() : "管理员";

        partyMapper.insert(party);

        QueryWrapper<Employee> empQw = new QueryWrapper<>();
        empQw.apply("MONTH(birthday_date) = {0}", month).eq("status", 1);
        List<Employee> birthdayEmployees = employeeMapper.selectList(empQw);

        for (Employee emp : birthdayEmployees) {
            BirthdayPartyParticipant participant = new BirthdayPartyParticipant();
            participant.setPartyId(party.getId());
            participant.setEmployeeId(emp.getId());
            participant.setEmployeeName(emp.getName());
            participant.setDepartment(emp.getDepartment());
            participant.setAvatar(emp.getAvatar());
            participant.setParticipationStatus(2);
            participantMapper.insert(participant);

            BirthdayMessage message = new BirthdayMessage();
            message.setRecipientId(emp.getId());
            message.setRecipientType(2);
            message.setType("PARTY_INVITE");
            message.setTitle("生日会邀请");
            message.setContent("您有一场" + month + "月生日会邀请，请查看详情并确认参与。");
            message.setRelatedId(party.getId());
            message.setIsRead(0);
            message.setCreatedAt(LocalDateTime.now());
            messageMapper.insert(message);
        }

        return Result.success(party);
    }

    public Result<?> updatePartyStatus(Long partyId, Integer status) {
        BirthdayParty party = partyMapper.selectById(partyId);
        party.setStatus(status);
        partyMapper.updateById(party);
        return Result.success();
    }

    public BirthdayPartyVO getPartyDetail(Long partyId) {
        BirthdayParty party = partyMapper.selectById(partyId);

        BirthdayPartyVO vo = new BirthdayPartyVO();
        vo.setId(party.getId());
        vo.setTheme(party.getTheme());
        vo.setPartyYear(party.getPartyYear());
        vo.setPartyMonth(party.getPartyMonth());
        vo.setEventTime(party.getEventTime());
        vo.setLocation(party.getLocation());
        vo.setFlow(party.getFlow());
        vo.setHighlights(party.getHighlights());
        vo.setCoverImage(party.getCoverImage());
        vo.setCreatedBy(party.getCreatedBy());
        vo.setStatus(party.getStatus());
        vo.setCreatedAt(party.getCreatedAt());

        QueryWrapper<BirthdayPartyParticipant> pQw = new QueryWrapper<>();
        pQw.eq("party_id", partyId);
        vo.setParticipantCount(participantMapper.selectCount(pQw).intValue());

        QueryWrapper<BirthdayPartyParticipant> attendQw = new QueryWrapper<>();
        attendQw.eq("party_id", partyId).eq("participation_status", 1);
        vo.setAttendCount(participantMapper.selectCount(attendQw).intValue());

        QueryWrapper<BirthdayPartyPhoto> phQw = new QueryWrapper<>();
        phQw.eq("party_id", partyId);
        vo.setPhotoCount(photoMapper.selectCount(phQw).intValue());

        return vo;
    }

    public List<BirthdayPartyVO> getPartyList(Integer year, Integer month) {
        QueryWrapper<BirthdayParty> qw = new QueryWrapper<>();
        if (year != null) {
            qw.eq("party_year", year);
        }
        if (month != null) {
            qw.eq("party_month", month);
        }
        qw.orderByDesc("event_time");

        List<BirthdayParty> parties = partyMapper.selectList(qw);
        List<BirthdayPartyVO> list = new ArrayList<>();

        for (BirthdayParty party : parties) {
            BirthdayPartyVO vo = new BirthdayPartyVO();
            vo.setId(party.getId());
            vo.setTheme(party.getTheme());
            vo.setPartyYear(party.getPartyYear());
            vo.setPartyMonth(party.getPartyMonth());
            vo.setEventTime(party.getEventTime());
            vo.setLocation(party.getLocation());
            vo.setFlow(party.getFlow());
            vo.setHighlights(party.getHighlights());
            vo.setCoverImage(party.getCoverImage());
            vo.setCreatedBy(party.getCreatedBy());
            vo.setStatus(party.getStatus());
            vo.setCreatedAt(party.getCreatedAt());

            QueryWrapper<BirthdayPartyParticipant> pQw = new QueryWrapper<>();
            pQw.eq("party_id", party.getId());
            vo.setParticipantCount(participantMapper.selectCount(pQw).intValue());

            QueryWrapper<BirthdayPartyParticipant> attendQw = new QueryWrapper<>();
            attendQw.eq("party_id", party.getId()).eq("participation_status", 1);
            vo.setAttendCount(participantMapper.selectCount(attendQw).intValue());

            QueryWrapper<BirthdayPartyPhoto> phQw = new QueryWrapper<>();
            phQw.eq("party_id", party.getId());
            vo.setPhotoCount(photoMapper.selectCount(phQw).intValue());

            list.add(vo);
        }

        return list;
    }

    public Result<?> updateParticipation(Long partyId, Long employeeId, PartyParticipationRequest request) {
        QueryWrapper<BirthdayPartyParticipant> qw = new QueryWrapper<>();
        qw.eq("party_id", partyId).eq("employee_id", employeeId);
        BirthdayPartyParticipant participant = participantMapper.selectOne(qw);

        if (participant == null) {
            return Result.error("未找到参与记录");
        }

        if (participant.getCheckinStatus() != null && participant.getCheckinStatus() == 1) {
            return Result.error("已签到的员工不允许修改参与状态");
        }

        BirthdayParty party = partyMapper.selectById(partyId);
        if (party != null && party.getStatus() != null && party.getStatus() == 2) {
            return Result.error("活动已结束，不允许修改参与状态");
        }

        participant.setParticipationStatus(request.getParticipationStatus());
        participant.setRemark(request.getRemark());
        participantMapper.updateById(participant);
        return Result.success();
    }

    public List<BirthdayPartyParticipantVO> getParticipants(Long partyId) {
        QueryWrapper<BirthdayPartyParticipant> qw = new QueryWrapper<>();
        qw.eq("party_id", partyId);
        List<BirthdayPartyParticipant> participants = participantMapper.selectList(qw);

        List<BirthdayPartyParticipantVO> list = new ArrayList<>();
        for (BirthdayPartyParticipant p : participants) {
            BirthdayPartyParticipantVO vo = new BirthdayPartyParticipantVO();
            vo.setId(p.getId());
            vo.setPartyId(p.getPartyId());
            vo.setEmployeeId(p.getEmployeeId());
            vo.setEmployeeName(p.getEmployeeName());
            vo.setParticipationStatus(p.getParticipationStatus());
            vo.setRemark(p.getRemark());
            vo.setCheckinStatus(p.getCheckinStatus());
            vo.setCheckinTime(p.getCheckinTime());
            list.add(vo);
        }
        return list;
    }

    public Result<?> checkin(CheckinRequest request) {
        BirthdayParty party = partyMapper.selectById(request.getPartyId());
        if (party == null) {
            return Result.error("生日会不存在");
        }

        LocalDate eventDate = party.getEventTime().toLocalDate();
        LocalDate today = LocalDate.now();
        if (!today.equals(eventDate)) {
            return Result.error("只能在活动当天进行签到");
        }

        for (Long employeeId : request.getEmployeeIds()) {
            QueryWrapper<BirthdayPartyParticipant> qw = new QueryWrapper<>();
            qw.eq("party_id", request.getPartyId()).eq("employee_id", employeeId);
            BirthdayPartyParticipant participant = participantMapper.selectOne(qw);

            if (participant != null && participant.getCheckinStatus() != null && participant.getCheckinStatus() != 1) {
                participant.setCheckinStatus(1);
                participant.setCheckinTime(LocalDateTime.now());
                participantMapper.updateById(participant);
            }
        }
        return Result.success();
    }

    public Result<?> uploadPhotos(Long partyId, List<String> photoUrls, Long uploaderId, String uploaderName) {
        for (String url : photoUrls) {
            BirthdayPartyPhoto photo = new BirthdayPartyPhoto();
            photo.setPartyId(partyId);
            photo.setPhotoUrl(url);
            photo.setUploadedBy(uploaderId);
            photo.setUploadedByName(uploaderName);
            photoMapper.insert(photo);
        }
        return Result.success();
    }

    public Result<?> deletePhoto(Long photoId, Long operatorId, boolean isAdmin) {
        BirthdayPartyPhoto photo = photoMapper.selectById(photoId);
        if (photo == null) {
            return Result.error("照片不存在");
        }
        if (!isAdmin && !photo.getUploadedBy().equals(operatorId)) {
            return Result.error("无权删除该照片");
        }

        QueryWrapper<BirthdayPartyPhotoLike> qw = new QueryWrapper<>();
        qw.eq("photo_id", photoId);
        photoLikeMapper.delete(qw);

        photoMapper.deleteById(photoId);
        return Result.success();
    }

    public List<BirthdayPartyPhotoVO> getPhotos(Long partyId, Long currentEmployeeId) {
        QueryWrapper<BirthdayPartyPhoto> qw = new QueryWrapper<>();
        qw.eq("party_id", partyId).orderByDesc("created_at");
        List<BirthdayPartyPhoto> photos = photoMapper.selectList(qw);

        List<BirthdayPartyPhotoVO> list = new ArrayList<>();
        for (BirthdayPartyPhoto photo : photos) {
            BirthdayPartyPhotoVO vo = new BirthdayPartyPhotoVO();
            vo.setId(photo.getId());
            vo.setPartyId(photo.getPartyId());
            vo.setPhotoUrl(photo.getPhotoUrl());
            vo.setUploadedBy(photo.getUploadedBy());
            vo.setUploadedByName(photo.getUploadedByName());
            vo.setLikeCount(photo.getLikeCount());
            vo.setCreatedAt(photo.getCreatedAt());

            QueryWrapper<BirthdayPartyPhotoLike> likeQw = new QueryWrapper<>();
            likeQw.eq("photo_id", photo.getId()).eq("employee_id", currentEmployeeId);
            vo.setHasLiked(photoLikeMapper.selectCount(likeQw) > 0);

            list.add(vo);
        }
        return list;
    }

    public Result<?> likePhoto(Long photoId, Long employeeId) {
        QueryWrapper<BirthdayPartyPhotoLike> qw = new QueryWrapper<>();
        qw.eq("photo_id", photoId).eq("employee_id", employeeId);
        if (photoLikeMapper.selectCount(qw) > 0) {
            return Result.error("已点赞过该照片");
        }

        BirthdayPartyPhotoLike like = new BirthdayPartyPhotoLike();
        like.setPhotoId(photoId);
        like.setEmployeeId(employeeId);
        photoLikeMapper.insert(like);

        BirthdayPartyPhoto photo = photoMapper.selectById(photoId);
        photo.setLikeCount(photo.getLikeCount() + 1);
        photoMapper.updateById(photo);

        return Result.success();
    }

    public Result<?> unlikePhoto(Long photoId, Long employeeId) {
        QueryWrapper<BirthdayPartyPhotoLike> qw = new QueryWrapper<>();
        qw.eq("photo_id", photoId).eq("employee_id", employeeId);
        BirthdayPartyPhotoLike like = photoLikeMapper.selectOne(qw);

        if (like == null) {
            return Result.error("未点赞该照片");
        }

        photoLikeMapper.deleteById(like.getId());

        BirthdayPartyPhoto photo = photoMapper.selectById(photoId);
        photo.setLikeCount(photo.getLikeCount() - 1);
        photoMapper.updateById(photo);

        return Result.success();
    }
}

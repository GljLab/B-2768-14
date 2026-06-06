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
import java.time.Period;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BirthdayTimelineService {

    private final EmployeeMapper employeeMapper;
    private final BirthdayWishMapper wishMapper;
    private final BirthdayPartyParticipantMapper participantMapper;
    private final BirthdayPartyMapper partyMapper;
    private final BirthdayPartyPhotoMapper photoMapper;
    private final BirthdayYearlyMessageMapper yearlyMessageMapper;
    private final BirthdayAdminMessageMapper adminMessageMapper;

    public List<TimelineYearVO> getTimeline(Long employeeId, Long currentUserId, boolean isAdmin) {
        Employee employee = employeeMapper.selectById(employeeId);
        if (employee == null) return Collections.emptyList();

        LocalDate hireDate = employee.getHireDate();
        LocalDate birthdayDate = employee.getBirthdayDate();
        LocalDate now = LocalDate.now();

        if (hireDate == null || birthdayDate == null) return Collections.emptyList();

        int hireYear = hireDate.getYear();
        int birthMonth = birthdayDate.getMonthValue();
        int birthDay = birthdayDate.getDayOfMonth();

        List<TimelineYearVO> timeline = new ArrayList<>();
        int order = 0;

        for (int year = hireYear; year <= now.getYear(); year++) {
            LocalDate birthdayThisYear = LocalDate.of(year, birthMonth, birthDay);
            if (birthdayThisYear.isAfter(now)) continue;
            if (birthdayThisYear.isBefore(hireDate)) continue;

            order++;
            TimelineYearVO vo = new TimelineYearVO();
            vo.setYear(year);
            vo.setBirthdayOrder(order);

            Period tenure = Period.between(hireDate, birthdayThisYear);
            long totalMonths = (long) tenure.getYears() * 12 + tenure.getMonths();
            long tenureYears = totalMonths / 12;
            long tenureMonths = totalMonths % 12;
            vo.setTenureDisplay(tenureYears + "年" + tenureMonths + "个月");

            QueryWrapper<BirthdayWish> wishQw = new QueryWrapper<>();
            wishQw.eq("recipient_id", employeeId)
                    .apply("YEAR(created_at) = {0}", year);
            Long wishCount = wishMapper.selectCount(wishQw);
            vo.setWishCount(wishCount.intValue());

            QueryWrapper<BirthdayYearlyMessage> msgQw = new QueryWrapper<>();
            msgQw.eq("employee_id", employeeId).eq("year", year);
            BirthdayYearlyMessage yearlyMsg = yearlyMessageMapper.selectOne(msgQw);
            if (yearlyMsg != null) {
                vo.setEmployeeMessage(yearlyMsg.getContent());
                vo.setEmployeeMessageId(yearlyMsg.getId());
                vo.setCanEditEmployeeMessage(year == now.getYear() && !isAdmin);
            } else {
                vo.setCanEditEmployeeMessage(year == now.getYear() && !isAdmin);
            }

            QueryWrapper<BirthdayAdminMessage> adminMsgQw = new QueryWrapper<>();
            adminMsgQw.eq("employee_id", employeeId).eq("year", year)
                    .orderByDesc("created_at");
            List<BirthdayAdminMessage> adminMsgs = adminMessageMapper.selectList(adminMsgQw);
            List<TimelineYearVO.AdminMessageVO> adminVOs = adminMsgs.stream().map(am -> {
                TimelineYearVO.AdminMessageVO avo = new TimelineYearVO.AdminMessageVO();
                avo.setId(am.getId());
                avo.setAdminId(am.getAdminId());
                avo.setAdminName(am.getAdminName());
                avo.setContent(am.getContent());
                avo.setCreatedAt(am.getCreatedAt());
                avo.setCanEdit(isAdmin && am.getAdminId().equals(currentUserId));
                return avo;
            }).collect(Collectors.toList());
            vo.setAdminMessages(adminVOs);

            QueryWrapper<BirthdayPartyParticipant> ppQw = new QueryWrapper<>();
            ppQw.eq("employee_id", employeeId).eq("participation_status", 1);
            List<BirthdayPartyParticipant> participations = participantMapper.selectList(ppQw);

            TimelineYearVO.PartyInfoVO partyInfo = null;
            for (BirthdayPartyParticipant pp : participations) {
                BirthdayParty party = partyMapper.selectById(pp.getPartyId());
                if (party != null && party.getPartyYear() == year && party.getStatus() == 2) {
                    if (partyInfo == null) {
                        partyInfo = new TimelineYearVO.PartyInfoVO();
                        partyInfo.setPartyId(party.getId());
                        partyInfo.setTheme(party.getTheme());
                        partyInfo.setEventTime(party.getEventTime());
                        partyInfo.setLocation(party.getLocation());

                        QueryWrapper<BirthdayPartyPhoto> photoQw = new QueryWrapper<>();
                        photoQw.eq("party_id", party.getId())
                                .orderByDesc("like_count")
                                .last("LIMIT 3");
                        List<BirthdayPartyPhoto> topPhotos = photoMapper.selectList(photoQw);
                        partyInfo.setPhotoUrls(topPhotos.stream()
                                .map(BirthdayPartyPhoto::getPhotoUrl)
                                .collect(Collectors.toList()));
                    }
                }
            }
            vo.setPartyInfo(partyInfo);

            if (employee.getStatus() != null && employee.getStatus() == 0) {
                LocalDate leaveCheck = YearMonth.from(birthdayThisYear).atEndOfMonth();
                if (hireDate != null && leaveCheck.isAfter(now)) {
                    vo.setIsLeft(true);
                }
            }

            timeline.add(vo);
        }

        Collections.reverse(timeline);
        return timeline;
    }

    public GrowthDataVO getGrowthData(Long employeeId) {
        Employee employee = employeeMapper.selectById(employeeId);
        if (employee == null) return new GrowthDataVO();

        LocalDate hireDate = employee.getHireDate();
        LocalDate birthdayDate = employee.getBirthdayDate();
        LocalDate now = LocalDate.now();

        GrowthDataVO vo = new GrowthDataVO();

        if (hireDate != null && birthdayDate != null) {
            int hireYear = hireDate.getYear();
            int birthMonth = birthdayDate.getMonthValue();
            int count = 0;
            for (int year = hireYear; year <= now.getYear(); year++) {
                LocalDate bd = LocalDate.of(year, birthMonth, birthdayDate.getDayOfMonth());
                if (!bd.isBefore(hireDate) && !bd.isAfter(now)) {
                    count++;
                }
            }
            vo.setBirthdayCount(count);
        } else {
            vo.setBirthdayCount(0);
        }

        QueryWrapper<BirthdayWish> wishQw = new QueryWrapper<>();
        wishQw.eq("recipient_id", employeeId);
        vo.setTotalWishes(wishMapper.selectCount(wishQw).intValue());

        QueryWrapper<BirthdayPartyParticipant> partyQw = new QueryWrapper<>();
        partyQw.eq("employee_id", employeeId).eq("participation_status", 1);
        vo.setTotalParties(participantMapper.selectCount(partyQw).intValue());

        QueryWrapper<BirthdayYearlyMessage> msgQw = new QueryWrapper<>();
        msgQw.eq("employee_id", employeeId);
        vo.setMessageCount(yearlyMessageMapper.selectCount(msgQw).intValue());

        return vo;
    }

    public Result<?> saveEmployeeMessage(Long employeeId, Integer year, String content) {
        Employee employee = employeeMapper.selectById(employeeId);
        if (employee == null) return Result.error("员工不存在");
        if (employee.getStatus() != null && employee.getStatus() == 0) {
            return Result.error("已离职员工不能新增寄语");
        }
        if (year != LocalDate.now().getYear()) {
            return Result.error("只能写当年的寄语");
        }

        QueryWrapper<BirthdayYearlyMessage> qw = new QueryWrapper<>();
        qw.eq("employee_id", employeeId).eq("year", year);
        BirthdayYearlyMessage existing = yearlyMessageMapper.selectOne(qw);

        if (existing != null) {
            existing.setContent(content);
            existing.setUpdatedAt(LocalDateTime.now());
            yearlyMessageMapper.updateById(existing);
        } else {
            BirthdayYearlyMessage msg = new BirthdayYearlyMessage();
            msg.setEmployeeId(employeeId);
            msg.setYear(year);
            msg.setContent(content);
            yearlyMessageMapper.insert(msg);
        }
        return Result.success();
    }

    public Result<?> saveAdminMessage(Long adminId, String adminName, Long employeeId, Integer year, String content) {
        Employee employee = employeeMapper.selectById(employeeId);
        if (employee == null) return Result.error("员工不存在");

        LocalDate hireDate = employee.getHireDate();
        if (hireDate != null && year < hireDate.getYear()) {
            return Result.error("不能写入职前年份的寄语");
        }

        BirthdayAdminMessage msg = new BirthdayAdminMessage();
        msg.setEmployeeId(employeeId);
        msg.setAdminId(adminId);
        msg.setAdminName(adminName);
        msg.setYear(year);
        msg.setContent(content);
        adminMessageMapper.insert(msg);
        return Result.success();
    }

    public Result<?> updateAdminMessage(Long messageId, Long adminId, String content) {
        BirthdayAdminMessage msg = adminMessageMapper.selectById(messageId);
        if (msg == null) return Result.error("寄语不存在");
        if (!msg.getAdminId().equals(adminId)) return Result.error("无权修改他人寄语");
        msg.setContent(content);
        msg.setUpdatedAt(LocalDateTime.now());
        adminMessageMapper.updateById(msg);
        return Result.success();
    }

    public Result<?> deleteAdminMessage(Long messageId, Long adminId) {
        BirthdayAdminMessage msg = adminMessageMapper.selectById(messageId);
        if (msg == null) return Result.error("寄语不存在");
        if (!msg.getAdminId().equals(adminId)) return Result.error("无权删除他人寄语");
        adminMessageMapper.deleteById(messageId);
        return Result.success();
    }

    public PosterDataVO getPosterData(Long employeeId) {
        Employee employee = employeeMapper.selectById(employeeId);
        if (employee == null) return null;

        GrowthDataVO growth = getGrowthData(employeeId);
        PosterDataVO vo = new PosterDataVO();
        vo.setName(employee.getName());
        vo.setAvatar(employee.getAvatar());
        vo.setBirthdayOrder(growth.getBirthdayCount());

        if (employee.getHireDate() != null) {
            Period p = Period.between(employee.getHireDate(), LocalDate.now());
            vo.setTenureDisplay(p.getYears() + "年" + p.getMonths() + "个月");
        } else {
            vo.setTenureDisplay("0年0个月");
        }

        vo.setTotalWishes(growth.getTotalWishes());
        vo.setTotalParties(growth.getTotalParties());
        vo.setBirthdayDate(employee.getBirthdayDate() != null ? employee.getBirthdayDate().toString() : "");
        vo.setCompanyName("公司");

        List<String> allKeywords = Arrays.asList(
                "温暖同行", "携手前行", "感恩有你", "砥砺前行", "不负韶华",
                "茁壮成长", "破茧成蝶", "逐梦前行", "筑梦前行", "踏浪而来",
                "坚守初心", "匠心筑梦", "精益求精", "持之以恒", "勇往直前",
                "团队骨干", "携手共进", "并肩作战", "风雨同舟", "凝心聚力",
                "时光沉淀", "岁月如歌", "青春飞扬", "筑梦年华", "似水流年"
        );
        Collections.shuffle(allKeywords);
        vo.setKeywords(allKeywords.subList(0, 2));

        return vo;
    }
}

package com.employee.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.employee.dto.MilestoneVO;
import com.employee.entity.*;
import com.employee.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BirthdayMilestoneService {

    private final BirthdayPartyMapper partyMapper;
    private final BirthdayPartyParticipantMapper participantMapper;
    private final BirthdayPartyPhotoMapper photoMapper;

    public List<MilestoneVO> getMilestones(Integer year, Integer month, String keyword) {
        QueryWrapper<BirthdayParty> qw = new QueryWrapper<>();
        qw.eq("status", 2);
        if (year != null) {
            qw.eq("party_year", year);
        }
        if (month != null) {
            qw.eq("party_month", month);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            qw.like("theme", keyword.trim());
        }
        qw.orderByDesc("event_time");

        List<BirthdayParty> parties = partyMapper.selectList(qw);
        List<MilestoneVO> list = new ArrayList<>();

        for (BirthdayParty party : parties) {
            MilestoneVO vo = new MilestoneVO();
            vo.setPartyId(party.getId());
            vo.setTheme(party.getTheme());
            vo.setEventTime(party.getEventTime());
            vo.setPartyYear(party.getPartyYear());
            vo.setPartyMonth(party.getPartyMonth());
            vo.setLocation(party.getLocation());
            vo.setHighlights(party.getHighlights());

            QueryWrapper<BirthdayPartyParticipant> allQw = new QueryWrapper<>();
            allQw.eq("party_id", party.getId());
            List<BirthdayPartyParticipant> allParticipants = participantMapper.selectList(allQw);
            vo.setTotalParticipantCount(allParticipants.size());

            long confirmed = allParticipants.stream()
                    .filter(p -> p.getParticipationStatus() != null && p.getParticipationStatus() == 1)
                    .count();
            vo.setConfirmedCount((int) confirmed);

            long checkedIn = allParticipants.stream()
                    .filter(p -> p.getCheckinStatus() != null && p.getCheckinStatus() == 1)
                    .count();
            vo.setCheckedInCount((int) checkedIn);

            if (vo.getHighlights() == null || vo.getHighlights().trim().isEmpty()) {
                vo.setHighlights(confirmed + "位寿星的欢乐时光");
            }

            QueryWrapper<BirthdayPartyPhoto> photoQw = new QueryWrapper<>();
            photoQw.eq("party_id", party.getId())
                    .orderByDesc("like_count")
                    .last("LIMIT 4");
            List<BirthdayPartyPhoto> topPhotos = photoMapper.selectList(photoQw);
            vo.setTopPhotos(topPhotos.stream()
                    .map(BirthdayPartyPhoto::getPhotoUrl)
                    .collect(Collectors.toList()));

            list.add(vo);
        }

        return list;
    }
}

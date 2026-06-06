package com.employee.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.employee.dto.BirthdayStatisticsVO;
import com.employee.entity.BirthdayParty;
import com.employee.entity.BirthdayPartyParticipant;
import com.employee.entity.BirthdayWish;
import com.employee.entity.Employee;
import com.employee.entity.BirthdayYearlyMessage;
import com.employee.entity.BirthdayAdminMessage;
import com.employee.mapper.BirthdayPartyMapper;
import com.employee.mapper.BirthdayPartyParticipantMapper;
import com.employee.mapper.BirthdayWishMapper;
import com.employee.mapper.EmployeeMapper;
import com.employee.mapper.BirthdayYearlyMessageMapper;
import com.employee.mapper.BirthdayAdminMessageMapper;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BirthdayStatisticsService {

    private final EmployeeMapper employeeMapper;
    private final BirthdayWishMapper wishMapper;
    private final BirthdayPartyMapper partyMapper;
    private final BirthdayPartyParticipantMapper participantMapper;
    private final BirthdayYearlyMessageMapper yearlyMessageMapper;
    private final BirthdayAdminMessageMapper adminMessageMapper;

    public BirthdayStatisticsVO getStatistics() {
        int currentYear = LocalDate.now().getYear();
        BirthdayStatisticsVO vo = new BirthdayStatisticsVO();

        List<BirthdayStatisticsVO.MonthCountVO> monthCounts = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            QueryWrapper<Employee> empWrapper = new QueryWrapper<>();
            empWrapper.apply("MONTH(birthday_date) = {0}", month).eq("status", 1);
            Long count = employeeMapper.selectCount(empWrapper);
            BirthdayStatisticsVO.MonthCountVO mc = new BirthdayStatisticsVO.MonthCountVO();
            mc.setMonth(String.valueOf(month));
            mc.setCount(count.intValue());
            monthCounts.add(mc);
        }
        vo.setBirthdayDistribution(monthCounts);

        QueryWrapper<Employee> deptWrapper = new QueryWrapper<>();
        deptWrapper.eq("status", 1);
        List<Employee> activeEmployees = employeeMapper.selectList(deptWrapper);
        Map<String, Long> deptGrouped = activeEmployees.stream()
                .filter(e -> e.getDepartment() != null)
                .collect(Collectors.groupingBy(Employee::getDepartment, Collectors.counting()));
        List<BirthdayStatisticsVO.DeptCountVO> deptCounts = deptGrouped.entrySet().stream()
                .map(entry -> {
                    BirthdayStatisticsVO.DeptCountVO dc = new BirthdayStatisticsVO.DeptCountVO();
                    dc.setDepartment(entry.getKey());
                    dc.setCount(entry.getValue().intValue());
                    return dc;
                })
                .collect(Collectors.toList());
        vo.setDeptDistribution(deptCounts);

        QueryWrapper<BirthdayWish> wishCountWrapper = new QueryWrapper<>();
        wishCountWrapper.apply("YEAR(created_at) = {0}", currentYear);
        Long wishCount = wishMapper.selectCount(wishCountWrapper);
        vo.setYearWishCount(wishCount.intValue());

        QueryWrapper<BirthdayWish> yearWishWrapper = new QueryWrapper<>();
        yearWishWrapper.apply("YEAR(created_at) = {0}", currentYear);
        List<BirthdayWish> yearWishes = wishMapper.selectList(yearWishWrapper);

        List<BirthdayStatisticsVO.RankVO> activeWishers = yearWishes.stream()
                .filter(w -> w.getIsSystem() != null && w.getIsSystem() == 0)
                .collect(Collectors.groupingBy(BirthdayWish::getSenderId, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(10)
                .map(entry -> {
                    BirthdayStatisticsVO.RankVO rank = new BirthdayStatisticsVO.RankVO();
                    rank.setId(entry.getKey());
                    Employee sender = employeeMapper.selectById(entry.getKey());
                    if (sender != null) {
                        rank.setName(sender.getName());
                        rank.setAvatar(sender.getAvatar());
                    }
                    rank.setCount(entry.getValue().intValue());
                    return rank;
                })
                .collect(Collectors.toList());
        vo.setActiveWishers(activeWishers);

        List<BirthdayStatisticsVO.RankVO> popularStars = yearWishes.stream()
                .filter(w -> w.getRecipientId() != null)
                .collect(Collectors.groupingBy(BirthdayWish::getRecipientId, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(10)
                .map(entry -> {
                    BirthdayStatisticsVO.RankVO rank = new BirthdayStatisticsVO.RankVO();
                    rank.setId(entry.getKey());
                    Employee recipient = employeeMapper.selectById(entry.getKey());
                    if (recipient != null) {
                        rank.setName(recipient.getName());
                        rank.setAvatar(recipient.getAvatar());
                    }
                    rank.setCount(entry.getValue().intValue());
                    return rank;
                })
                .collect(Collectors.toList());
        vo.setPopularStars(popularStars);

        QueryWrapper<BirthdayParty> partyWrapper = new QueryWrapper<>();
        partyWrapper.eq("party_year", currentYear);
        Long partyCount = partyMapper.selectCount(partyWrapper);
        vo.setYearPartyCount(partyCount.intValue());

        QueryWrapper<BirthdayParty> yearPartyWrapper = new QueryWrapper<>();
        yearPartyWrapper.eq("party_year", currentYear);
        List<BirthdayParty> yearParties = partyMapper.selectList(yearPartyWrapper);

        long totalParticipants = 0;
        long confirmedParticipants = 0;
        for (BirthdayParty party : yearParties) {
            QueryWrapper<BirthdayPartyParticipant> pWrapper = new QueryWrapper<>();
            pWrapper.eq("party_id", party.getId());
            List<BirthdayPartyParticipant> participants = participantMapper.selectList(pWrapper);
            totalParticipants += participants.size();
            confirmedParticipants += participants.stream()
                    .filter(p -> p.getParticipationStatus() != null && p.getParticipationStatus() == 1)
                    .count();
        }
        double avgRate = totalParticipants > 0 ? (double) confirmedParticipants / totalParticipants : 0.0;
        vo.setAvgParticipationRate(avgRate);

        QueryWrapper<Employee> allEmpQw = new QueryWrapper<>();
        allEmpQw.eq("status", 1);
        Long totalEmployees = employeeMapper.selectCount(allEmpQw);

        QueryWrapper<BirthdayYearlyMessage> msgQw = new QueryWrapper<>();
        msgQw.apply("year = {0}", currentYear);
        List<BirthdayYearlyMessage> yearlyMessages = yearlyMessageMapper.selectList(msgQw);
        long employeesWithMessage = yearlyMessages.stream()
                .map(BirthdayYearlyMessage::getEmployeeId)
                .distinct()
                .count();
        double empMsgRate = totalEmployees > 0 ? (double) employeesWithMessage / totalEmployees : 0.0;
        vo.setEmployeeMessageRate(empMsgRate);

        QueryWrapper<BirthdayAdminMessage> adminMsgQw = new QueryWrapper<>();
        adminMsgQw.apply("year = {0}", currentYear);
        List<BirthdayAdminMessage> adminMessages = adminMessageMapper.selectList(adminMsgQw);
        long employeesWithAdminMsg = adminMessages.stream()
                .map(BirthdayAdminMessage::getEmployeeId)
                .distinct()
                .count();
        double adminMsgRate = totalEmployees > 0 ? (double) employeesWithAdminMsg / totalEmployees : 0.0;
        vo.setAdminMessageCoverageRate(adminMsgRate);

        List<BirthdayStatisticsVO.ParticipationRateVO> rateTrend = new ArrayList<>();
        for (int y = currentYear - 4; y <= currentYear; y++) {
            QueryWrapper<BirthdayParty> ypQw = new QueryWrapper<>();
            ypQw.eq("party_year", y);
            List<BirthdayParty> ypList = partyMapper.selectList(ypQw);
            long yTotal = 0, yConfirmed = 0;
            for (BirthdayParty p : ypList) {
                QueryWrapper<BirthdayPartyParticipant> ppQw = new QueryWrapper<>();
                ppQw.eq("party_id", p.getId());
                List<BirthdayPartyParticipant> ppList = participantMapper.selectList(ppQw);
                yTotal += ppList.size();
                yConfirmed += ppList.stream().filter(pp -> pp.getParticipationStatus() != null && pp.getParticipationStatus() == 1).count();
            }
            BirthdayStatisticsVO.ParticipationRateVO rateVO = new BirthdayStatisticsVO.ParticipationRateVO();
            rateVO.setYear(y);
            rateVO.setRate(yTotal > 0 ? (double) yConfirmed / yTotal : 0.0);
            rateTrend.add(rateVO);
        }
        vo.setParticipationRateTrend(rateTrend);

        QueryWrapper<BirthdayParty> endedPartyQw = new QueryWrapper<>();
        endedPartyQw.eq("status", 2).orderByDesc("event_time");
        List<BirthdayParty> endedParties = partyMapper.selectList(endedPartyQw);
        List<BirthdayStatisticsVO.ActivePartyVO> topParties = new ArrayList<>();
        for (BirthdayParty p : endedParties) {
            QueryWrapper<BirthdayPartyParticipant> ppQw = new QueryWrapper<>();
            ppQw.eq("party_id", p.getId()).eq("checkin_status", 1);
            Long checkinCount = participantMapper.selectCount(ppQw);
            BirthdayStatisticsVO.ActivePartyVO ap = new BirthdayStatisticsVO.ActivePartyVO();
            ap.setId(p.getId());
            ap.setTheme(p.getTheme());
            ap.setCheckinCount(checkinCount.intValue());
            ap.setEventTime(p.getEventTime());
            topParties.add(ap);
        }
        topParties.sort((a, b) -> b.getCheckinCount() - a.getCheckinCount());
        vo.setTopActiveParties(topParties.stream().limit(5).collect(Collectors.toList()));

        return vo;
    }

    public byte[] exportBirthdayEmployees(int year, int month) {
        QueryWrapper<Employee> wrapper = new QueryWrapper<>();
        wrapper.apply("MONTH(birthday_date) = {0}", month).eq("status", 1);
        List<Employee> employees = employeeMapper.selectList(wrapper);

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("生日员工名单");
            Row headerRow = sheet.createRow(0);
            String[] headers = {"员工ID", "姓名", "部门", "职位", "生日日期", "入职日期", "入职年限"};
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            for (int i = 0; i < employees.size(); i++) {
                Employee emp = employees.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(emp.getId() != null ? emp.getId().doubleValue() : 0);
                row.createCell(1).setCellValue(emp.getName() != null ? emp.getName() : "");
                row.createCell(2).setCellValue(emp.getDepartment() != null ? emp.getDepartment() : "");
                row.createCell(3).setCellValue(emp.getPosition() != null ? emp.getPosition() : "");
                row.createCell(4).setCellValue(emp.getBirthdayDate() != null ? emp.getBirthdayDate().toString() : "");
                row.createCell(5).setCellValue(emp.getHireDate() != null ? emp.getHireDate().toString() : "");
                long hireYears = emp.getHireDate() != null ? ChronoUnit.YEARS.between(emp.getHireDate(), LocalDate.now()) : 0;
                row.createCell(6).setCellValue(hireYears);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] exportWishReport(int year) {
        QueryWrapper<BirthdayWish> wrapper = new QueryWrapper<>();
        wrapper.apply("YEAR(created_at) = {0}", year);
        List<BirthdayWish> wishes = wishMapper.selectList(wrapper);

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("祝福互动报表");
            Row headerRow = sheet.createRow(0);
            String[] headers = {"祝福ID", "发送者", "接收者", "祝福内容", "点赞数", "发送时间", "是否系统发送"};
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            for (int i = 0; i < wishes.size(); i++) {
                BirthdayWish wish = wishes.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(wish.getId() != null ? wish.getId().doubleValue() : 0);

                String senderName;
                if (wish.getIsSystem() != null && wish.getIsSystem() == 1) {
                    senderName = "系统";
                } else if (wish.getSenderId() != null && wish.getSenderId() == 0) {
                    senderName = "系统";
                } else if (wish.getSenderId() != null) {
                    Employee sender = employeeMapper.selectById(wish.getSenderId());
                    senderName = sender != null ? sender.getName() : "";
                } else {
                    senderName = "";
                }
                row.createCell(1).setCellValue(senderName);

                String recipientName = "";
                if (wish.getRecipientId() != null) {
                    Employee recipient = employeeMapper.selectById(wish.getRecipientId());
                    recipientName = recipient != null ? recipient.getName() : "";
                }
                row.createCell(2).setCellValue(recipientName);
                row.createCell(3).setCellValue(wish.getContent() != null ? wish.getContent() : "");
                row.createCell(4).setCellValue(wish.getLikeCount() != null ? wish.getLikeCount() : 0);
                row.createCell(5).setCellValue(wish.getCreatedAt() != null ? wish.getCreatedAt().toString() : "");
                row.createCell(6).setCellValue(wish.getIsSystem() != null && wish.getIsSystem() == 1 ? "是" : "否");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

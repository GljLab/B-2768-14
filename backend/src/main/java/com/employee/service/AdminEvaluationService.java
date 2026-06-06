package com.employee.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.employee.dto.*;
import com.employee.entity.*;
import com.employee.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminEvaluationService {
    private final AdminEvaluationMapper adminEvaluationMapper;
    private final AdminGoalEvaluationMapper adminGoalEvaluationMapper;
    private final ColleagueEvaluationMapper colleagueEvaluationMapper;
    private final ColleagueDimensionScoreMapper dimensionScoreMapper;
    private final ColleagueEvaluationRelationMapper colleagueRelationMapper;
    private final EvaluationCycleEmployeeMapper cycleEmployeeMapper;
    private final GrowthReportMapper growthReportMapper;
    private final EvaluationCycleMapper evaluationCycleMapper;
    private final EmployeeEvaluationService employeeEvaluationService;
    private final EvaluationFeedbackMapper feedbackMapper;
    private final OneOnOneMeetingMapper oneOnOneMeetingMapper;
    private final AdminAvailableTimeMapper availableTimeMapper;
    private final EmployeeMapper employeeMapper;

    public AdminEvaluation getAdminEvaluation(Long cycleId, Long employeeId) {
        return adminEvaluationMapper.selectOne(new LambdaQueryWrapper<AdminEvaluation>()
            .eq(AdminEvaluation::getCycleId, cycleId)
            .eq(AdminEvaluation::getEmployeeId, employeeId));
    }

    public List<AdminGoalEvaluation> getAdminGoalEvaluations(Long adminEvalId) {
        return adminGoalEvaluationMapper.selectList(new LambdaQueryWrapper<AdminGoalEvaluation>()
            .eq(AdminGoalEvaluation::getAdminEvalId, adminEvalId));
    }

    @Transactional(rollbackFor = Exception.class)
    public void submitAdminEvaluation(AdminEvaluationRequest req, Long adminId, String adminName) {
        AdminEvaluation existing = getAdminEvaluation(req.getCycleId(), req.getEmployeeId());
        
        AdminEvaluation adminEval = new AdminEvaluation();
        adminEval.setCycleId(req.getCycleId());
        adminEval.setEmployeeId(req.getEmployeeId());
        adminEval.setAdminId(adminId);
        adminEval.setAdminName(adminName);
        adminEval.setOverallScore(req.getOverallScore());
        adminEval.setOverallComment(req.getOverallComment());
        adminEval.setDevelopmentSuggestion(req.getDevelopmentSuggestion());
        adminEval.setSubmitTime(LocalDateTime.now());
        adminEval.setCreatedAt(LocalDateTime.now());
        adminEval.setUpdatedAt(LocalDateTime.now());

        if (existing != null) {
            adminEval.setId(existing.getId());
            adminEvaluationMapper.updateById(adminEval);
            adminGoalEvaluationMapper.delete(new LambdaQueryWrapper<AdminGoalEvaluation>()
                .eq(AdminGoalEvaluation::getAdminEvalId, existing.getId()));
        } else {
            adminEvaluationMapper.insert(adminEval);
        }

        if (req.getGoalEvaluations() != null) {
            for (var ge : req.getGoalEvaluations()) {
                AdminGoalEvaluation age = new AdminGoalEvaluation();
                age.setAdminEvalId(adminEval.getId());
                age.setGoalId(ge.getGoalId());
                age.setScore(ge.getScore());
                age.setComment(ge.getComment());
                age.setCreatedAt(LocalDateTime.now());
                adminGoalEvaluationMapper.insert(age);
            }
        }

        updateCycleEmployeeStatus(req.getCycleId(), req.getEmployeeId(), "adminEval", 1);

        employeeEvaluationService.generateGrowthReport(req.getCycleId(), req.getEmployeeId());
    }

    private void updateCycleEmployeeStatus(Long cycleId, Long employeeId, String type, int status) {
        EvaluationCycleEmployee ce = cycleEmployeeMapper.selectOne(new LambdaQueryWrapper<EvaluationCycleEmployee>()
            .eq(EvaluationCycleEmployee::getCycleId, cycleId)
            .eq(EvaluationCycleEmployee::getEmployeeId, employeeId));
        if (ce == null) return;

        switch (type) {
            case "adminEval":
                ce.setAdminEvalStatus(status);
                break;
            case "oneOnOne":
                ce.setOneOnOneStatus(status);
                break;
        }
        cycleEmployeeMapper.updateById(ce);
    }

    public List<ColleagueEvaluation> getAllColleagueEvaluations(Long cycleId) {
        return colleagueEvaluationMapper.selectList(new LambdaQueryWrapper<ColleagueEvaluation>()
            .eq(ColleagueEvaluation::getCycleId, cycleId)
            .eq(ColleagueEvaluation::getIsDeleted, 0)
            .orderByDesc(ColleagueEvaluation::getCreatedAt));
    }

    public List<ColleagueDimensionScore> getDimensionScores(Long evaluationId) {
        return dimensionScoreMapper.selectList(new LambdaQueryWrapper<ColleagueDimensionScore>()
            .eq(ColleagueDimensionScore::getEvaluationId, evaluationId));
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteColleagueEvaluation(Long evaluationId, Long adminId) {
        ColleagueEvaluation eval = colleagueEvaluationMapper.selectById(evaluationId);
        if (eval == null) throw new RuntimeException("评价记录不存在");
        
        eval.setIsDeleted(1);
        eval.setDeletedBy(adminId);
        eval.setDeletedAt(LocalDateTime.now());
        colleagueEvaluationMapper.updateById(eval);

        employeeEvaluationService.generateGrowthReport(eval.getCycleId(), eval.getEvaluatedId());
    }

    public List<ColleagueEvaluation> getColleagueEvaluationsForEmployee(Long cycleId, Long evaluatedId, boolean isAdmin) {
        LambdaQueryWrapper<ColleagueEvaluation> wrapper = new LambdaQueryWrapper<ColleagueEvaluation>()
            .eq(ColleagueEvaluation::getCycleId, cycleId)
            .eq(ColleagueEvaluation::getEvaluatedId, evaluatedId)
            .eq(ColleagueEvaluation::getIsDeleted, 0);
        
        if (!isAdmin) {
            wrapper.select(ColleagueEvaluation::getId, ColleagueEvaluation::getCycleId, 
                ColleagueEvaluation::getEvaluatedId, ColleagueEvaluation::getComment, 
                ColleagueEvaluation::getStrengths, ColleagueEvaluation::getImprovements,
                ColleagueEvaluation::getSubmitTime);
        }
        
        return colleagueEvaluationMapper.selectList(wrapper);
    }

    public EvaluationStatisticsVO getStatistics(Long cycleId) {
        EvaluationCycle cycle = evaluationCycleMapper.selectById(cycleId);
        if (cycle == null) throw new RuntimeException("评价周期不存在");

        EvaluationStatisticsVO vo = new EvaluationStatisticsVO();
        vo.setCycleId(cycleId);
        vo.setCycleName(cycle.getCycleName());

        List<GrowthReport> reports = growthReportMapper.selectList(new LambdaQueryWrapper<GrowthReport>()
            .eq(GrowthReport::getCycleId, cycleId));

        Map<String, Integer> gradeDist = new LinkedHashMap<>();
        gradeDist.put("卓越", 0);
        gradeDist.put("优秀", 0);
        gradeDist.put("良好", 0);
        gradeDist.put("合格", 0);
        gradeDist.put("待提升", 0);
        for (GrowthReport r : reports) {
            if (r.getGrade() != null) {
                gradeDist.merge(r.getGrade(), 1, Integer::sum);
            }
        }
        vo.setGradeDistribution(gradeDist);

        Map<Long, List<GrowthReport>> deptReports = reports.stream()
            .filter(r -> r.getDepartmentId() != null)
            .collect(Collectors.groupingBy(GrowthReport::getDepartmentId));
        List<DepartmentAverageVO> deptAverages = new ArrayList<>();
        for (Map.Entry<Long, List<GrowthReport>> entry : deptReports.entrySet()) {
            DepartmentAverageVO davg = new DepartmentAverageVO();
            davg.setDepartmentId(entry.getKey());
            davg.setDepartmentName(entry.getValue().get(0).getDepartmentName());
            davg.setEmployeeCount(entry.getValue().size());
            double avg = entry.getValue().stream()
                .filter(r -> r.getTotalScore() != null)
                .mapToDouble(r -> r.getTotalScore().doubleValue())
                .average().orElse(0);
            davg.setAverageScore(BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP));
            deptAverages.add(davg);
        }
        vo.setDepartmentAverages(deptAverages);

        List<GrowthReport> sortedReports = reports.stream()
            .filter(r -> r.getTotalScore() != null)
            .sorted((a, b) -> b.getTotalScore().compareTo(a.getTotalScore()))
            .collect(Collectors.toList());
        
        List<TopEmployeeVO> topEmployees = sortedReports.stream()
            .limit(10)
            .map(r -> {
                TopEmployeeVO te = new TopEmployeeVO();
                te.setEmployeeId(r.getEmployeeId());
                te.setEmployeeName(r.getEmployeeName());
                te.setDepartmentName(r.getDepartmentName());
                te.setTotalScore(r.getTotalScore());
                te.setGrade(r.getGrade());
                return te;
            }).collect(Collectors.toList());
        vo.setTopEmployees(topEmployees);

        List<AttentionEmployeeVO> attentionEmployees = sortedReports.stream()
            .filter(r -> "待提升".equals(r.getGrade()))
            .limit(10)
            .map(r -> {
                AttentionEmployeeVO ae = new AttentionEmployeeVO();
                ae.setEmployeeId(r.getEmployeeId());
                ae.setEmployeeName(r.getEmployeeName());
                ae.setDepartmentName(r.getDepartmentName());
                ae.setTotalScore(r.getTotalScore());
                ae.setGrade(r.getGrade());
                return ae;
            }).collect(Collectors.toList());
        vo.setAttentionEmployees(attentionEmployees);

        return vo;
    }

    public List<EvaluationFeedback> getAllFeedbacks(Long cycleId) {
        return feedbackMapper.selectList(new LambdaQueryWrapper<EvaluationFeedback>()
            .eq(EvaluationFeedback::getCycleId, cycleId)
            .orderByDesc(EvaluationFeedback::getCreatedAt));
    }

    @Transactional(rollbackFor = Exception.class)
    public void handleFeedback(Long feedbackId, String reply, boolean adjust, Long adminId) {
        EvaluationFeedback feedback = feedbackMapper.selectById(feedbackId);
        if (feedback == null) throw new RuntimeException("反馈不存在");
        
        feedback.setStatus(1);
        feedback.setAdminReply(reply);
        feedback.setIsAdjusted(adjust ? 1 : 0);
        feedback.setHandledBy(adminId);
        feedback.setHandledTime(LocalDateTime.now());
        feedback.setUpdatedAt(LocalDateTime.now());
        feedbackMapper.updateById(feedback);

        if (adjust) {
            employeeEvaluationService.generateGrowthReport(feedback.getCycleId(), feedback.getEmployeeId());
        }

        EvaluationCycleEmployee ce = cycleEmployeeMapper.selectOne(new LambdaQueryWrapper<EvaluationCycleEmployee>()
            .eq(EvaluationCycleEmployee::getCycleId, feedback.getCycleId())
            .eq(EvaluationCycleEmployee::getEmployeeId, feedback.getEmployeeId()));
        if (ce != null) {
            ce.setFeedbackStatus(2);
            cycleEmployeeMapper.updateById(ce);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void setAvailableTimes(Long cycleId, Long adminId, List<AdminAvailableTime> times) {
        availableTimeMapper.delete(new LambdaQueryWrapper<AdminAvailableTime>()
            .eq(AdminAvailableTime::getCycleId, cycleId)
            .eq(AdminAvailableTime::getAdminId, adminId));

        for (AdminAvailableTime t : times) {
            t.setCycleId(cycleId);
            t.setAdminId(adminId);
            t.setIsBooked(0);
            t.setCreatedAt(LocalDateTime.now());
            availableTimeMapper.insert(t);
        }
    }

    public List<AdminAvailableTime> getAvailableTimes(Long cycleId, Long adminId) {
        return availableTimeMapper.selectList(new LambdaQueryWrapper<AdminAvailableTime>()
            .eq(AdminAvailableTime::getCycleId, cycleId)
            .eq(AdminAvailableTime::getAdminId, adminId)
            .eq(AdminAvailableTime::getIsBooked, 0)
            .orderByAsc(AdminAvailableTime::getStartTime));
    }

    public List<OneOnOneMeeting> getMeetings(Long cycleId, Long employeeId) {
        LambdaQueryWrapper<OneOnOneMeeting> wrapper = new LambdaQueryWrapper<OneOnOneMeeting>()
            .eq(OneOnOneMeeting::getCycleId, cycleId);
        if (employeeId != null) {
            wrapper.eq(OneOnOneMeeting::getEmployeeId, employeeId);
        }
        wrapper.orderByAsc(OneOnOneMeeting::getMeetingTime);
        return oneOnOneMeetingMapper.selectList(wrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    public void bookMeeting(OneOnOneMeetingRequest req, Long employeeId) {
        OneOnOneMeeting meeting = new OneOnOneMeeting();
        meeting.setCycleId(req.getCycleId());
        meeting.setEmployeeId(employeeId);
        meeting.setAdminId(req.getAdminId());
        meeting.setMeetingTime(req.getMeetingTime());
        meeting.setMeetingLocation(req.getMeetingLocation());
        meeting.setStatus(1);
        meeting.setCreatedAt(LocalDateTime.now());
        meeting.setUpdatedAt(LocalDateTime.now());
        oneOnOneMeetingMapper.insert(meeting);

        updateCycleEmployeeStatus(req.getCycleId(), employeeId, "oneOnOne", 1);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateMeeting(OneOnOneMeetingRequest req) {
        OneOnOneMeeting meeting = oneOnOneMeetingMapper.selectById(req.getId());
        if (meeting == null) throw new RuntimeException("预约不存在");
        
        meeting.setAdvantages(req.getAdvantages());
        meeting.setDevelopAbilities(req.getDevelopAbilities());
        meeting.setNextActions(req.getNextActions());
        meeting.setDevelopmentDirection(req.getDevelopmentDirection());
        meeting.setStatus(req.getStatus() != null ? req.getStatus() : 2);
        meeting.setUpdatedAt(LocalDateTime.now());
        oneOnOneMeetingMapper.updateById(meeting);

        if (req.getStatus() != null && req.getStatus() == 2) {
            updateCycleEmployeeStatus(meeting.getCycleId(), meeting.getEmployeeId(), "oneOnOne", 2);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void confirmMeeting(Long meetingId) {
        OneOnOneMeeting meeting = oneOnOneMeetingMapper.selectById(meetingId);
        if (meeting == null) throw new RuntimeException("预约不存在");
        
        meeting.setEmployeeConfirm(1);
        meeting.setConfirmTime(LocalDateTime.now());
        meeting.setUpdatedAt(LocalDateTime.now());
        oneOnOneMeetingMapper.updateById(meeting);
    }

    public List<WorkGoal> getEmployeeGoals(Long cycleId, Long employeeId) {
        return employeeEvaluationService.getWorkGoals(cycleId, employeeId);
    }

    public GrowthReportVO getEmployeeGrowthReport(Long cycleId, Long employeeId) {
        return employeeEvaluationService.getGrowthReport(cycleId, employeeId);
    }

    public List<GrowthReport> getEmployeeGrowthHistory(Long employeeId) {
        return employeeEvaluationService.getEmployeeGrowthHistory(employeeId);
    }
}

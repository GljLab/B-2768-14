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
public class EmployeeEvaluationService {
    private final WorkGoalMapper workGoalMapper;
    private final WorkHighlightMapper workHighlightMapper;
    private final SelfEvaluationMapper selfEvaluationMapper;
    private final GoalEvaluationMapper goalEvaluationMapper;
    private final ColleagueEvaluationRelationMapper colleagueRelationMapper;
    private final ColleagueEvaluationMapper colleagueEvaluationMapper;
    private final ColleagueDimensionScoreMapper dimensionScoreMapper;
    private final EvaluationCycleEmployeeMapper cycleEmployeeMapper;
    private final GrowthReportMapper growthReportMapper;
    private final EvaluationCycleMapper evaluationCycleMapper;
    private final GradeStandardMapper gradeStandardMapper;
    private final GrowthPlanMapper growthPlanMapper;
    private final OneOnOneMeetingMapper oneOnOneMeetingMapper;
    private final EvaluationFeedbackMapper feedbackMapper;

    public List<WorkGoal> getEmployeeGoals(Long cycleId, Long employeeId) {
        return workGoalMapper.selectList(new LambdaQueryWrapper<WorkGoal>()
            .eq(WorkGoal::getCycleId, cycleId)
            .eq(WorkGoal::getEmployeeId, employeeId)
            .orderByAsc(WorkGoal::getId));
    }

    public List<WorkGoal> getWorkGoals(Long cycleId, Long employeeId) {
        return getEmployeeGoals(cycleId, employeeId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveWorkGoals(Long cycleId, Long employeeId, List<WorkGoalRequest> goals) {
        workGoalMapper.delete(new LambdaQueryWrapper<WorkGoal>()
            .eq(WorkGoal::getCycleId, cycleId)
            .eq(WorkGoal::getEmployeeId, employeeId)
            .in(WorkGoal::getStatus, 1));

        for (WorkGoalRequest req : goals) {
            WorkGoal goal = new WorkGoal();
            goal.setCycleId(cycleId);
            goal.setEmployeeId(employeeId);
            goal.setGoalName(req.getGoalName());
            goal.setPlanContent(req.getPlanContent());
            goal.setWeight(req.getWeight());
            goal.setStatus(req.getStatus() != null ? req.getStatus() : 1);
            goal.setCreatedAt(LocalDateTime.now());
            goal.setUpdatedAt(LocalDateTime.now());
            workGoalMapper.insert(goal);
        }

        updateCycleEmployeeStatus(cycleId, employeeId, "goal", 1);
    }

    @Transactional(rollbackFor = Exception.class)
    public void submitGoals(Long cycleId, Long employeeId) {
        List<WorkGoal> goals = getEmployeeGoals(cycleId, employeeId);
        for (WorkGoal goal : goals) {
            goal.setStatus(2);
            goal.setUpdatedAt(LocalDateTime.now());
            workGoalMapper.updateById(goal);
        }
        updateCycleEmployeeStatus(cycleId, employeeId, "goal", 2);
    }

    @Transactional(rollbackFor = Exception.class)
    public void confirmGoal(Long goalId, String adminAdvice) {
        WorkGoal goal = workGoalMapper.selectById(goalId);
        if (goal == null) throw new RuntimeException("目标不存在");
        goal.setStatus(3);
        goal.setAdminAdvice(adminAdvice);
        goal.setUpdatedAt(LocalDateTime.now());
        workGoalMapper.updateById(goal);

        updateCycleEmployeeStatus(goal.getCycleId(), goal.getEmployeeId(), "goal", 3);
    }

    public List<WorkHighlight> getEmployeeHighlights(Long cycleId, Long employeeId) {
        return workHighlightMapper.selectList(new LambdaQueryWrapper<WorkHighlight>()
            .eq(WorkHighlight::getCycleId, cycleId)
            .eq(WorkHighlight::getEmployeeId, employeeId)
            .orderByDesc(WorkHighlight::getCreatedAt));
    }

    @Transactional(rollbackFor = Exception.class)
    public Long saveHighlight(WorkHighlightRequest req, Long employeeId) {
        WorkHighlight highlight = new WorkHighlight();
        highlight.setCycleId(req.getCycleId());
        highlight.setEmployeeId(employeeId);
        highlight.setTitle(req.getTitle());
        highlight.setContent(req.getContent());
        highlight.setHighlightType(req.getHighlightType() != null ? req.getHighlightType() : 1);
        highlight.setAttachments(req.getAttachments());
        highlight.setCreatedAt(LocalDateTime.now());
        highlight.setUpdatedAt(LocalDateTime.now());
        workHighlightMapper.insert(highlight);
        return highlight.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteHighlight(Long highlightId) {
        workHighlightMapper.deleteById(highlightId);
    }

    public SelfEvaluation getSelfEvaluation(Long cycleId, Long employeeId) {
        return selfEvaluationMapper.selectOne(new LambdaQueryWrapper<SelfEvaluation>()
            .eq(SelfEvaluation::getCycleId, cycleId)
            .eq(SelfEvaluation::getEmployeeId, employeeId));
    }

    public List<GoalEvaluation> getGoalEvaluations(Long cycleId, Long employeeId) {
        return goalEvaluationMapper.selectList(new LambdaQueryWrapper<GoalEvaluation>()
            .eq(GoalEvaluation::getCycleId, cycleId)
            .eq(GoalEvaluation::getEmployeeId, employeeId));
    }

    @Transactional(rollbackFor = Exception.class)
    public void submitSelfEvaluation(SelfEvaluationRequest req, Long employeeId) {
        SelfEvaluation existing = getSelfEvaluation(req.getCycleId(), employeeId);
        if (existing != null && existing.getSubmitTime() != null) {
            throw new RuntimeException("自评已提交，不可修改");
        }

        if (existing == null) {
            SelfEvaluation selfEval = new SelfEvaluation();
            selfEval.setCycleId(req.getCycleId());
            selfEval.setEmployeeId(employeeId);
            selfEval.setSummary(req.getSummary());
            selfEval.setGrowth(req.getGrowth());
            selfEval.setChallenges(req.getChallenges());
            selfEval.setImprovements(req.getImprovements());
            selfEval.setSubmitTime(LocalDateTime.now());
            selfEval.setCreatedAt(LocalDateTime.now());
            selfEval.setUpdatedAt(LocalDateTime.now());
            selfEvaluationMapper.insert(selfEval);
        } else {
            existing.setSummary(req.getSummary());
            existing.setGrowth(req.getGrowth());
            existing.setChallenges(req.getChallenges());
            existing.setImprovements(req.getImprovements());
            existing.setSubmitTime(LocalDateTime.now());
            existing.setUpdatedAt(LocalDateTime.now());
            selfEvaluationMapper.updateById(existing);
        }

        if (req.getGoalEvaluations() != null) {
            for (var geReq : req.getGoalEvaluations()) {
                GoalEvaluation ge = new GoalEvaluation();
                ge.setCycleId(req.getCycleId());
                ge.setEmployeeId(employeeId);
                ge.setGoalId(geReq.getGoalId());
                ge.setSelfScore(geReq.getSelfScore());
                ge.setAchievementDegree(geReq.getAchievementDegree());
                ge.setActualResult(geReq.getActualResult());
                ge.setCreatedAt(LocalDateTime.now());
                ge.setUpdatedAt(LocalDateTime.now());
                try {
                    goalEvaluationMapper.insert(ge);
                } catch (Exception e) {
                    goalEvaluationMapper.update(ge, new LambdaQueryWrapper<GoalEvaluation>()
                        .eq(GoalEvaluation::getGoalId, geReq.getGoalId())
                        .eq(GoalEvaluation::getEmployeeId, employeeId));
                }
            }
        }

        updateCycleEmployeeStatus(req.getCycleId(), employeeId, "selfEval", 2);
    }

    public List<ColleagueEvaluationRelation> getPendingColleagueEvaluations(Long cycleId, Long evaluatorId) {
        return colleagueRelationMapper.selectList(new LambdaQueryWrapper<ColleagueEvaluationRelation>()
            .eq(ColleagueEvaluationRelation::getCycleId, cycleId)
            .eq(ColleagueEvaluationRelation::getEvaluatorId, evaluatorId)
            .orderByAsc(ColleagueEvaluationRelation::getStatus));
    }

    @Transactional(rollbackFor = Exception.class)
    public void submitColleagueEvaluation(ColleagueEvaluationRequest req, Long evaluatorId) {
        ColleagueEvaluationRelation relation = colleagueRelationMapper.selectById(req.getRelationId());
        if (relation == null) throw new RuntimeException("评价关系不存在");
        if (relation.getStatus() == 1) throw new RuntimeException("已提交，不可修改");

        ColleagueEvaluation eval = new ColleagueEvaluation();
        eval.setRelationId(req.getRelationId());
        eval.setCycleId(req.getCycleId());
        eval.setEvaluatorId(evaluatorId);
        eval.setEvaluatedId(req.getEvaluatedId());
        eval.setComment(req.getComment());
        eval.setStrengths(req.getStrengths());
        eval.setImprovements(req.getImprovements());
        eval.setSubmitTime(LocalDateTime.now());
        eval.setIsDeleted(0);
        eval.setCreatedAt(LocalDateTime.now());
        colleagueEvaluationMapper.insert(eval);

        if (req.getDimensionScores() != null) {
            for (var ds : req.getDimensionScores()) {
                ColleagueDimensionScore score = new ColleagueDimensionScore();
                score.setEvaluationId(eval.getId());
                score.setDimensionId(ds.getDimensionId());
                score.setDimensionName(ds.getDimensionName());
                score.setScore(ds.getScore());
                score.setCreatedAt(LocalDateTime.now());
                dimensionScoreMapper.insert(score);
            }
        }

        relation.setStatus(1);
        colleagueRelationMapper.updateById(relation);

        checkAndUpdateColleagueEvalStatus(req.getCycleId(), evaluatorId);
    }

    private void checkAndUpdateColleagueEvalStatus(Long cycleId, Long evaluatorId) {
        List<ColleagueEvaluationRelation> relations = getPendingColleagueEvaluations(cycleId, evaluatorId);
        boolean allDone = relations.stream().allMatch(r -> r.getStatus() == 1);
        if (allDone) {
            updateCycleEmployeeStatus(cycleId, evaluatorId, "colleagueEval", 2);
        } else {
            updateCycleEmployeeStatus(cycleId, evaluatorId, "colleagueEval", 1);
        }
    }

    private void updateCycleEmployeeStatus(Long cycleId, Long employeeId, String type, int status) {
        EvaluationCycleEmployee ce = cycleEmployeeMapper.selectOne(new LambdaQueryWrapper<EvaluationCycleEmployee>()
            .eq(EvaluationCycleEmployee::getCycleId, cycleId)
            .eq(EvaluationCycleEmployee::getEmployeeId, employeeId));
        if (ce == null) return;

        switch (type) {
            case "goal":
                ce.setGoalStatus(status);
                break;
            case "selfEval":
                ce.setSelfEvalStatus(status);
                break;
            case "colleagueEval":
                ce.setColleagueEvalStatus(status);
                break;
            case "adminEval":
                ce.setAdminEvalStatus(status);
                break;
            case "oneOnOne":
                ce.setOneOnOneStatus(status);
                break;
        }
        cycleEmployeeMapper.updateById(ce);
    }

    @Transactional(rollbackFor = Exception.class)
    public GrowthReport generateGrowthReport(Long cycleId, Long employeeId) {
        EvaluationCycle cycle = evaluationCycleMapper.selectById(cycleId);
        if (cycle == null) throw new RuntimeException("评价周期不存在");

        EvaluationCycleEmployee emp = cycleEmployeeMapper.selectOne(new LambdaQueryWrapper<EvaluationCycleEmployee>()
            .eq(EvaluationCycleEmployee::getCycleId, cycleId)
            .eq(EvaluationCycleEmployee::getEmployeeId, employeeId));

        BigDecimal selfScore = calculateSelfScore(cycleId, employeeId);
        BigDecimal colleagueScore = calculateColleagueScore(cycleId, employeeId);
        BigDecimal adminScore = calculateAdminScore(cycleId, employeeId);

        BigDecimal totalScore = BigDecimal.ZERO;
        if (selfScore != null) {
            totalScore = totalScore.add(selfScore.multiply(BigDecimal.valueOf(cycle.getSelfWeight()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)));
        }
        if (colleagueScore != null) {
            totalScore = totalScore.add(colleagueScore.multiply(BigDecimal.valueOf(cycle.getColleagueWeight()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)));
        }
        if (adminScore != null) {
            totalScore = totalScore.add(adminScore.multiply(BigDecimal.valueOf(cycle.getAdminWeight()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)));
        }

        String grade = determineGrade(cycleId, totalScore);

        GrowthReport report = new GrowthReport();
        report.setCycleId(cycleId);
        report.setEmployeeId(employeeId);
        report.setEmployeeName(emp != null ? emp.getEmployeeName() : null);
        report.setDepartmentId(emp != null ? emp.getDepartmentId() : null);
        report.setDepartmentName(emp != null ? emp.getDepartmentName() : null);
        report.setSelfScore(selfScore);
        report.setColleagueScore(colleagueScore);
        report.setAdminScore(adminScore);
        report.setTotalScore(totalScore);
        report.setGrade(grade);
        report.setGeneratedAt(LocalDateTime.now());
        report.setCreatedAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());

        GrowthReport existing = growthReportMapper.selectOne(new LambdaQueryWrapper<GrowthReport>()
            .eq(GrowthReport::getCycleId, cycleId)
            .eq(GrowthReport::getEmployeeId, employeeId));
        if (existing != null) {
            report.setId(existing.getId());
            growthReportMapper.updateById(report);
        } else {
            growthReportMapper.insert(report);
        }

        calculateRanks(cycleId);

        if (emp != null) {
            emp.setReportStatus(1);
            cycleEmployeeMapper.updateById(emp);
        }

        return report;
    }

    private BigDecimal calculateSelfScore(Long cycleId, Long employeeId) {
        List<GoalEvaluation> goalEvals = goalEvaluationMapper.selectList(new LambdaQueryWrapper<GoalEvaluation>()
            .eq(GoalEvaluation::getCycleId, cycleId)
            .eq(GoalEvaluation::getEmployeeId, employeeId));
        if (goalEvals.isEmpty()) return null;

        List<WorkGoal> goals = workGoalMapper.selectList(new LambdaQueryWrapper<WorkGoal>()
            .eq(WorkGoal::getCycleId, cycleId)
            .eq(WorkGoal::getEmployeeId, employeeId));
        Map<Long, Integer> goalWeights = goals.stream()
            .collect(Collectors.toMap(WorkGoal::getId, WorkGoal::getWeight));

        BigDecimal total = BigDecimal.ZERO;
        int totalWeight = 0;
        for (GoalEvaluation ge : goalEvals) {
            Integer weight = goalWeights.get(ge.getGoalId());
            if (weight == null) weight = 0;
            if (ge.getSelfScore() != null) {
                total = total.add(BigDecimal.valueOf(ge.getSelfScore()).multiply(BigDecimal.valueOf(weight)));
                totalWeight += weight;
            }
        }
        return totalWeight > 0 ? total.divide(BigDecimal.valueOf(totalWeight), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    private BigDecimal calculateColleagueScore(Long cycleId, Long employeeId) {
        List<ColleagueEvaluation> evaluations = colleagueEvaluationMapper.selectList(new LambdaQueryWrapper<ColleagueEvaluation>()
            .eq(ColleagueEvaluation::getCycleId, cycleId)
            .eq(ColleagueEvaluation::getEvaluatedId, employeeId)
            .eq(ColleagueEvaluation::getIsDeleted, 0));
        if (evaluations.isEmpty()) return null;

        List<Long> evalIds = evaluations.stream().map(ColleagueEvaluation::getId).collect(Collectors.toList());
        List<ColleagueDimensionScore> scores = dimensionScoreMapper.selectList(new LambdaQueryWrapper<ColleagueDimensionScore>()
            .in(ColleagueDimensionScore::getEvaluationId, evalIds));

        if (scores.isEmpty()) return null;

        double avg = scores.stream().mapToInt(ColleagueDimensionScore::getScore).average().orElse(0);
        return BigDecimal.valueOf(avg * 20).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateAdminScore(Long cycleId, Long employeeId) {
        return null;
    }

    private String determineGrade(Long cycleId, BigDecimal score) {
        List<GradeStandard> standards = gradeStandardMapper.selectList(new LambdaQueryWrapper<GradeStandard>()
            .eq(GradeStandard::getCycleId, cycleId)
            .orderByAsc(GradeStandard::getSortOrder));
        for (GradeStandard gs : standards) {
            if (score.compareTo(BigDecimal.valueOf(gs.getMinScore())) >= 0 
                && score.compareTo(BigDecimal.valueOf(gs.getMaxScore())) <= 0) {
                return gs.getGradeName();
            }
        }
        return "待提升";
    }

    private void calculateRanks(Long cycleId) {
        List<GrowthReport> reports = growthReportMapper.selectList(new LambdaQueryWrapper<GrowthReport>()
            .eq(GrowthReport::getCycleId, cycleId));
        reports.sort((a, b) -> b.getTotalScore().compareTo(a.getTotalScore()));
        for (int i = 0; i < reports.size(); i++) {
            GrowthReport r = reports.get(i);
            r.setCompanyRank(i + 1);
            growthReportMapper.updateById(r);
        }

        Map<Long, List<GrowthReport>> deptReports = reports.stream()
            .filter(r -> r.getDepartmentId() != null)
            .collect(Collectors.groupingBy(GrowthReport::getDepartmentId));
        for (List<GrowthReport> deptList : deptReports.values()) {
            deptList.sort((a, b) -> b.getTotalScore().compareTo(a.getTotalScore()));
            for (int i = 0; i < deptList.size(); i++) {
                GrowthReport r = deptList.get(i);
                r.setDepartmentRank(i + 1);
                growthReportMapper.updateById(r);
            }
        }
    }

    public GrowthReport getGrowthReportEntity(Long cycleId, Long employeeId) {
        return growthReportMapper.selectOne(new LambdaQueryWrapper<GrowthReport>()
            .eq(GrowthReport::getCycleId, cycleId)
            .eq(GrowthReport::getEmployeeId, employeeId));
    }

    public GrowthReportVO getGrowthReport(Long cycleId, Long employeeId) {
        GrowthReport report = getGrowthReportEntity(cycleId, employeeId);
        if (report == null) return null;

        GrowthReportVO vo = new GrowthReportVO();
        vo.setCycleId(report.getCycleId());
        vo.setEmployeeId(report.getEmployeeId());
        vo.setEmployeeName(report.getEmployeeName());
        vo.setDepartmentName(report.getDepartmentName());
        vo.setTotalScore(report.getTotalScore());
        vo.setGrade(report.getGrade());
        vo.setSelfScore(report.getSelfScore());
        vo.setColleagueScore(report.getColleagueScore());
        vo.setAdminScore(report.getAdminScore());
        vo.setDepartmentRank(report.getDepartmentRank());
        vo.setCompanyRank(report.getCompanyRank());

        List<WorkGoal> goals = getWorkGoals(cycleId, employeeId);
        List<GoalResultVO> goalResults = goals.stream().map(g -> {
            GoalResultVO gr = new GoalResultVO();
            gr.setGoalId(g.getId());
            gr.setGoalName(g.getGoalName());
            gr.setWeight(g.getWeight());
            return gr;
        }).collect(Collectors.toList());
        vo.setGoalResults(goalResults);

        return vo;
    }

    public List<GrowthReport> getEmployeeGrowthHistory(Long employeeId) {
        List<GrowthReport> reports = growthReportMapper.selectList(new LambdaQueryWrapper<GrowthReport>()
            .eq(GrowthReport::getEmployeeId, employeeId)
            .orderByDesc(GrowthReport::getCycleId));
        
        if (!reports.isEmpty()) {
            List<Long> cycleIds = reports.stream().map(GrowthReport::getCycleId).distinct().toList();
            List<EvaluationCycle> cycles = evaluationCycleMapper.selectList(
                new LambdaQueryWrapper<EvaluationCycle>().in(EvaluationCycle::getId, cycleIds));
            Map<Long, String> cycleNameMap = cycles.stream()
                .collect(Collectors.toMap(EvaluationCycle::getId, EvaluationCycle::getCycleName));
            
            for (GrowthReport report : reports) {
                report.setCycleName(cycleNameMap.get(report.getCycleId()));
            }
        }
        
        return reports;
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveGrowthPlan(GrowthPlanRequest req, Long employeeId) {
        GrowthPlan plan = new GrowthPlan();
        plan.setEmployeeId(employeeId);
        plan.setCycleId(req.getCycleId());
        plan.setNextCycleId(req.getNextCycleId());
        plan.setAbilityToImprove(req.getAbilityToImprove());
        plan.setSpecificActions(req.getSpecificActions());
        plan.setExpectedCompletion(req.getExpectedCompletion());
        plan.setVerificationMethod(req.getVerificationMethod());
        plan.setStatus(req.getStatus() != null ? req.getStatus() : 1);
        plan.setCreatedAt(LocalDateTime.now());
        plan.setUpdatedAt(LocalDateTime.now());
        if (req.getId() != null) {
            plan.setId(req.getId());
            growthPlanMapper.updateById(plan);
        } else {
            growthPlanMapper.insert(plan);
        }
    }

    public List<GrowthPlan> getEmployeeGrowthPlans(Long employeeId) {
        return growthPlanMapper.selectList(new LambdaQueryWrapper<GrowthPlan>()
            .eq(GrowthPlan::getEmployeeId, employeeId)
            .orderByDesc(GrowthPlan::getCreatedAt));
    }

    @Transactional(rollbackFor = Exception.class)
    public Long submitFeedback(EvaluationFeedbackRequest req, Long employeeId) {
        EvaluationFeedback feedback = new EvaluationFeedback();
        feedback.setCycleId(req.getCycleId());
        feedback.setEmployeeId(employeeId);
        feedback.setContent(req.getContent());
        feedback.setQuestionPoints(req.getQuestionPoints());
        feedback.setSupplement(req.getSupplement());
        feedback.setAttachments(req.getAttachments());
        feedback.setStatus(0);
        feedback.setCreatedAt(LocalDateTime.now());
        feedback.setUpdatedAt(LocalDateTime.now());
        feedbackMapper.insert(feedback);

        updateCycleEmployeeStatus(req.getCycleId(), employeeId, "feedback", 1);
        return feedback.getId();
    }

    public List<EvaluationFeedback> getEmployeeFeedbacks(Long cycleId, Long employeeId) {
        return feedbackMapper.selectList(new LambdaQueryWrapper<EvaluationFeedback>()
            .eq(EvaluationFeedback::getCycleId, cycleId)
            .eq(EvaluationFeedback::getEmployeeId, employeeId)
            .orderByDesc(EvaluationFeedback::getCreatedAt));
    }
}

package com.employee.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
public class EvaluationCycleService {
    private final EvaluationCycleMapper evaluationCycleMapper;
    private final EvaluationCycleDepartmentMapper cycleDepartmentMapper;
    private final EvaluationCycleEmployeeMapper cycleEmployeeMapper;
    private final GradeStandardMapper gradeStandardMapper;
    private final EvaluationDimensionMapper dimensionMapper;
    private final EmployeeMapper employeeMapper;
    private final DepartmentMapper departmentMapper;
    private final ColleagueEvaluationRelationMapper colleagueRelationMapper;

    @Transactional(rollbackFor = Exception.class)
    public Long createCycle(EvaluationCycleRequest request, Long adminId, String adminName) {
        EvaluationCycle cycle = new EvaluationCycle();
        cycle.setCycleName(request.getCycleName());
        cycle.setStartDate(request.getStartDate());
        cycle.setEndDate(request.getEndDate());
        cycle.setTargetType(request.getTargetType());
        cycle.setStatus(0);
        cycle.setSelfWeight(request.getSelfWeight() != null ? request.getSelfWeight() : 20);
        cycle.setColleagueWeight(request.getColleagueWeight() != null ? request.getColleagueWeight() : 30);
        cycle.setAdminWeight(request.getAdminWeight() != null ? request.getAdminWeight() : 50);
        cycle.setGoalDeadline(request.getGoalDeadline());
        cycle.setSelfEvalStart(request.getSelfEvalStart());
        cycle.setSelfEvalDeadline(request.getSelfEvalDeadline());
        cycle.setColleagueEvalStart(request.getColleagueEvalStart());
        cycle.setColleagueEvalDeadline(request.getColleagueEvalDeadline());
        cycle.setAdminEvalDeadline(request.getAdminEvalDeadline());
        cycle.setOneOnOneDeadline(request.getOneOnOneDeadline());
        cycle.setFeedbackDeadline(request.getFeedbackDeadline());
        cycle.setCreatedBy(adminId);
        cycle.setCreatedByName(adminName);
        cycle.setCreatedAt(LocalDateTime.now());
        evaluationCycleMapper.insert(cycle);

        Long cycleId = cycle.getId();

        if (request.getGradeStandards() != null && !request.getGradeStandards().isEmpty()) {
            for (var gs : request.getGradeStandards()) {
                GradeStandard standard = new GradeStandard();
                standard.setCycleId(cycleId);
                standard.setGradeName(gs.getGradeName());
                standard.setMinScore(gs.getMinScore());
                standard.setMaxScore(gs.getMaxScore());
                standard.setSortOrder(gs.getSortOrder());
                standard.setCreatedAt(LocalDateTime.now());
                gradeStandardMapper.insert(standard);
            }
        } else {
            initDefaultGradeStandards(cycleId);
        }

        if (request.getDimensions() != null && !request.getDimensions().isEmpty()) {
            for (var dim : request.getDimensions()) {
                EvaluationDimension dimension = new EvaluationDimension();
                dimension.setCycleId(cycleId);
                dimension.setDimensionName(dim.getDimensionName());
                dimension.setDimensionDesc(dim.getDimensionDesc());
                dimension.setMaxScore(dim.getMaxScore() != null ? dim.getMaxScore() : 5);
                dimension.setSortOrder(dim.getSortOrder());
                dimension.setCreatedAt(LocalDateTime.now());
                dimensionMapper.insert(dimension);
            }
        } else {
            initDefaultDimensions(cycleId);
        }

        initCycleParticipants(cycleId, request.getTargetType(), request.getDepartmentIds(), request.getEmployeeIds());

        return cycleId;
    }

    private void initDefaultGradeStandards(Long cycleId) {
        List<GradeStandard> standards = new ArrayList<>();
        standards.add(createGradeStandard(cycleId, "卓越", 90, 100, 1));
        standards.add(createGradeStandard(cycleId, "优秀", 80, 89, 2));
        standards.add(createGradeStandard(cycleId, "良好", 70, 79, 3));
        standards.add(createGradeStandard(cycleId, "合格", 60, 69, 4));
        standards.add(createGradeStandard(cycleId, "待提升", 0, 59, 5));
        for (GradeStandard s : standards) {
            gradeStandardMapper.insert(s);
        }
    }

    private GradeStandard createGradeStandard(Long cycleId, String name, int min, int max, int sort) {
        GradeStandard s = new GradeStandard();
        s.setCycleId(cycleId);
        s.setGradeName(name);
        s.setMinScore(min);
        s.setMaxScore(max);
        s.setSortOrder(sort);
        s.setCreatedAt(LocalDateTime.now());
        return s;
    }

    private void initDefaultDimensions(Long cycleId) {
        String[][] dims = {
            {"工作交付质量", "工作成果的质量和准确性"},
            {"团队协作精神", "与团队成员的合作能力"},
            {"沟通响应效率", "沟通的及时性和有效性"},
            {"专业能力展现", "专业技能和知识水平"},
            {"主动帮助他人", "主动帮助同事的意愿和行动"}
        };
        for (int i = 0; i < dims.length; i++) {
            EvaluationDimension d = new EvaluationDimension();
            d.setCycleId(cycleId);
            d.setDimensionName(dims[i][0]);
            d.setDimensionDesc(dims[i][1]);
            d.setMaxScore(5);
            d.setSortOrder(i + 1);
            d.setCreatedAt(LocalDateTime.now());
            dimensionMapper.insert(d);
        }
    }

    private void initCycleParticipants(Long cycleId, Integer targetType, List<Long> departmentIds, List<Long> employeeIds) {
        List<Employee> employees = new ArrayList<>();
        
        if (targetType == 1) {
            employees = employeeMapper.selectList(new LambdaQueryWrapper<Employee>().eq(Employee::getStatus, 1));
        } else if (targetType == 2 && departmentIds != null && !departmentIds.isEmpty()) {
            for (Long deptId : departmentIds) {
                EvaluationCycleDepartment cd = new EvaluationCycleDepartment();
                cd.setCycleId(cycleId);
                cd.setDepartmentId(deptId);
                Department dept = departmentMapper.selectById(deptId);
                cd.setDepartmentName(dept != null ? dept.getName() : null);
                cd.setCreatedAt(LocalDateTime.now());
                cycleDepartmentMapper.insert(cd);
            }
            employees = employeeMapper.selectList(new LambdaQueryWrapper<Employee>()
                .eq(Employee::getStatus, 1)
                .in(Employee::getDepartmentId, departmentIds));
        } else if (targetType == 3 && employeeIds != null && !employeeIds.isEmpty()) {
            employees = employeeMapper.selectList(new LambdaQueryWrapper<Employee>()
                .eq(Employee::getStatus, 1)
                .in(Employee::getId, employeeIds));
        }

        for (Employee emp : employees) {
            EvaluationCycleEmployee ce = new EvaluationCycleEmployee();
            ce.setCycleId(cycleId);
            ce.setEmployeeId(emp.getId());
            ce.setEmployeeName(emp.getName());
            ce.setDepartmentId(emp.getDepartmentId());
            ce.setDepartmentName(emp.getDepartment());
            ce.setGoalStatus(0);
            ce.setSelfEvalStatus(0);
            ce.setColleagueEvalStatus(0);
            ce.setAdminEvalStatus(0);
            ce.setOneOnOneStatus(0);
            ce.setFeedbackStatus(0);
            ce.setReportStatus(0);
            ce.setCreatedAt(LocalDateTime.now());
            cycleEmployeeMapper.insert(ce);
        }

        autoAssignColleagueRelations(cycleId, employees);
    }

    private void autoAssignColleagueRelations(Long cycleId, List<Employee> employees) {
        Map<Long, List<Employee>> deptEmployees = employees.stream()
            .filter(e -> e.getDepartmentId() != null)
            .collect(Collectors.groupingBy(Employee::getDepartmentId));

        for (List<Employee> deptEmps : deptEmployees.values()) {
            if (deptEmps.size() < 2) continue;
            for (Employee evaluator : deptEmps) {
                for (Employee evaluated : deptEmps) {
                    if (evaluator.getId().equals(evaluated.getId())) continue;
                    ColleagueEvaluationRelation relation = new ColleagueEvaluationRelation();
                    relation.setCycleId(cycleId);
                    relation.setEvaluatorId(evaluator.getId());
                    relation.setEvaluatorName(evaluator.getName());
                    relation.setEvaluatedId(evaluated.getId());
                    relation.setEvaluatedName(evaluated.getName());
                    relation.setStatus(0);
                    relation.setCreatedAt(LocalDateTime.now());
                    try {
                        colleagueRelationMapper.insert(relation);
                    } catch (Exception ignored) {}
                }
            }
        }
    }

    public IPage<EvaluationCycle> getCycleList(Integer page, Integer size, Integer status) {
        Page<EvaluationCycle> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<EvaluationCycle> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(EvaluationCycle::getStatus, status);
        }
        wrapper.orderByDesc(EvaluationCycle::getCreatedAt);
        return evaluationCycleMapper.selectPage(pageParam, wrapper);
    }

    public EvaluationCycle getCycleById(Long id) {
        return evaluationCycleMapper.selectById(id);
    }

    public List<GradeStandard> getGradeStandards(Long cycleId) {
        return gradeStandardMapper.selectList(new LambdaQueryWrapper<GradeStandard>()
            .eq(GradeStandard::getCycleId, cycleId)
            .orderByAsc(GradeStandard::getSortOrder));
    }

    public List<EvaluationDimension> getDimensions(Long cycleId) {
        return dimensionMapper.selectList(new LambdaQueryWrapper<EvaluationDimension>()
            .eq(EvaluationDimension::getCycleId, cycleId)
            .orderByAsc(EvaluationDimension::getSortOrder));
    }

    public List<EvaluationCycleEmployee> getCycleEmployees(Long cycleId) {
        return cycleEmployeeMapper.selectList(new LambdaQueryWrapper<EvaluationCycleEmployee>()
            .eq(EvaluationCycleEmployee::getCycleId, cycleId));
    }

    @Transactional(rollbackFor = Exception.class)
    public void startCycle(Long cycleId) {
        EvaluationCycle cycle = evaluationCycleMapper.selectById(cycleId);
        if (cycle == null) throw new RuntimeException("评价周期不存在");
        cycle.setStatus(1);
        cycle.setUpdatedAt(LocalDateTime.now());
        evaluationCycleMapper.updateById(cycle);
    }

    @Transactional(rollbackFor = Exception.class)
    public void endCycle(Long cycleId) {
        EvaluationCycle cycle = evaluationCycleMapper.selectById(cycleId);
        if (cycle == null) throw new RuntimeException("评价周期不存在");
        cycle.setStatus(2);
        cycle.setUpdatedAt(LocalDateTime.now());
        evaluationCycleMapper.updateById(cycle);
    }

    public EvaluationProgressVO getProgress(Long cycleId) {
        EvaluationCycle cycle = evaluationCycleMapper.selectById(cycleId);
        if (cycle == null) throw new RuntimeException("评价周期不存在");

        List<EvaluationCycleEmployee> employees = getCycleEmployees(cycleId);
        
        EvaluationProgressVO progress = new EvaluationProgressVO();
        progress.setCycleId(cycleId);
        progress.setCycleName(cycle.getCycleName());
        progress.setTotalEmployees(employees.size());
        
        progress.setGoalCompleted((int) employees.stream().filter(e -> e.getGoalStatus() != null && e.getGoalStatus() >= 2).count());
        progress.setGoalPending(employees.size() - progress.getGoalCompleted());
        
        progress.setSelfEvalCompleted((int) employees.stream().filter(e -> e.getSelfEvalStatus() != null && e.getSelfEvalStatus() == 2).count());
        progress.setSelfEvalPending(employees.size() - progress.getSelfEvalCompleted());
        
        progress.setColleagueEvalCompleted((int) employees.stream().filter(e -> e.getColleagueEvalStatus() != null && e.getColleagueEvalStatus() == 2).count());
        progress.setColleagueEvalPending(employees.size() - progress.getColleagueEvalCompleted());
        
        progress.setAdminEvalCompleted((int) employees.stream().filter(e -> e.getAdminEvalStatus() != null && e.getAdminEvalStatus() == 1).count());
        progress.setAdminEvalPending(employees.size() - progress.getAdminEvalCompleted());
        
        progress.setOneOnOneCompleted((int) employees.stream().filter(e -> e.getOneOnOneStatus() != null && e.getOneOnOneStatus() == 2).count());
        progress.setOneOnOnePending(employees.size() - progress.getOneOnOneCompleted());

        List<EmployeeProgressVO> empList = employees.stream().map(e -> {
            EmployeeProgressVO ep = new EmployeeProgressVO();
            ep.setEmployeeId(e.getEmployeeId());
            ep.setEmployeeName(e.getEmployeeName());
            ep.setDepartmentName(e.getDepartmentName());
            ep.setGoalStatus(e.getGoalStatus());
            ep.setSelfEvalStatus(e.getSelfEvalStatus());
            ep.setColleagueEvalStatus(e.getColleagueEvalStatus());
            ep.setAdminEvalStatus(e.getAdminEvalStatus());
            ep.setOneOnOneStatus(e.getOneOnOneStatus());
            return ep;
        }).collect(Collectors.toList());
        progress.setEmployees(empList);

        return progress;
    }
}

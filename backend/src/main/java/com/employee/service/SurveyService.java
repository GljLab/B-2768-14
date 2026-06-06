package com.employee.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.employee.dto.SurveyAnswerRequest;
import com.employee.dto.SurveyRequest;
import com.employee.dto.SurveyStatisticsVO;
import com.employee.entity.*;
import com.employee.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SurveyService {
    private final SurveyMapper surveyMapper;
    private final SurveyQuestionMapper questionMapper;
    private final SurveyQuestionOptionMapper optionMapper;
    private final SurveyTargetMapper targetMapper;
    private final SurveyAnswerMapper answerMapper;
    private final SurveyAnswerItemMapper answerItemMapper;
    private final EmployeeMapper employeeMapper;

    public IPage<Survey> getSurveyList(Integer pageNum, Integer pageSize) {
        Page<Survey> page = new Page<>(pageNum, pageSize);
        return surveyMapper.selectSurveyList(page);
    }

    public Survey getSurveyDetail(Long id) {
        Survey survey = surveyMapper.selectSurveyDetail(id);
        if (survey != null) {
            List<SurveyQuestion> questions = questionMapper.selectBySurveyId(id);
            for (SurveyQuestion question : questions) {
                question.setOptions(optionMapper.selectByQuestionId(question.getId()));
            }
            survey.setQuestions(questions);
            survey.setTargets(targetMapper.selectBySurveyId(id));
        }
        return survey;
    }

    @Transactional
    public Survey createSurvey(Long creatorId, String creatorName, SurveyRequest request) {
        Survey survey = new Survey();
        BeanUtils.copyProperties(request, survey);
        survey.setCreatedBy(creatorId);
        survey.setCreatedByName(creatorName);
        survey.setStatus(1);
        surveyMapper.insert(survey);

        saveTargets(survey.getId(), request.getTargets());
        saveQuestions(survey.getId(), request.getQuestions());

        return survey;
    }

    @Transactional
    public Survey updateSurvey(Long id, SurveyRequest request) {
        Survey survey = surveyMapper.selectById(id);
        if (survey == null) {
            throw new IllegalArgumentException("问卷不存在");
        }

        BeanUtils.copyProperties(request, survey);
        survey.setId(id);
        surveyMapper.updateById(survey);

        targetMapper.delete(new LambdaQueryWrapper<SurveyTarget>().eq(SurveyTarget::getSurveyId, id));
        saveTargets(id, request.getTargets());

        List<SurveyQuestion> oldQuestions = questionMapper.selectBySurveyId(id);
        for (SurveyQuestion q : oldQuestions) {
            optionMapper.delete(new LambdaQueryWrapper<SurveyQuestionOption>().eq(SurveyQuestionOption::getQuestionId, q.getId()));
        }
        questionMapper.delete(new LambdaQueryWrapper<SurveyQuestion>().eq(SurveyQuestion::getSurveyId, id));
        saveQuestions(id, request.getQuestions());

        return survey;
    }

    @Transactional
    public void deleteSurvey(Long id) {
        targetMapper.delete(new LambdaQueryWrapper<SurveyTarget>().eq(SurveyTarget::getSurveyId, id));
        List<SurveyQuestion> questions = questionMapper.selectBySurveyId(id);
        for (SurveyQuestion q : questions) {
            optionMapper.delete(new LambdaQueryWrapper<SurveyQuestionOption>().eq(SurveyQuestionOption::getQuestionId, q.getId()));
        }
        questionMapper.delete(new LambdaQueryWrapper<SurveyQuestion>().eq(SurveyQuestion::getSurveyId, id));
        List<SurveyAnswer> answers = answerMapper.selectBySurveyId(id);
        for (SurveyAnswer a : answers) {
            answerItemMapper.delete(new LambdaQueryWrapper<SurveyAnswerItem>().eq(SurveyAnswerItem::getAnswerId, a.getId()));
        }
        answerMapper.delete(new LambdaQueryWrapper<SurveyAnswer>().eq(SurveyAnswer::getSurveyId, id));
        surveyMapper.deleteById(id);
    }

    private void saveTargets(Long surveyId, List<SurveyRequest.TargetRequest> targets) {
        if (targets != null && !targets.isEmpty()) {
            for (SurveyRequest.TargetRequest targetReq : targets) {
                SurveyTarget target = new SurveyTarget();
                target.setSurveyId(surveyId);
                target.setTargetType(targetReq.getTargetType());
                target.setTargetId(targetReq.getTargetId());
                target.setTargetName(targetReq.getTargetName());
                targetMapper.insert(target);
            }
        }
    }

    private void saveQuestions(Long surveyId, List<SurveyRequest.QuestionRequest> questions) {
        if (questions != null && !questions.isEmpty()) {
            for (SurveyRequest.QuestionRequest qReq : questions) {
                SurveyQuestion question = new SurveyQuestion();
                BeanUtils.copyProperties(qReq, question);
                question.setSurveyId(surveyId);
                questionMapper.insert(question);

                if (qReq.getOptions() != null && !qReq.getOptions().isEmpty()) {
                    for (SurveyRequest.OptionRequest oReq : qReq.getOptions()) {
                        SurveyQuestionOption option = new SurveyQuestionOption();
                        BeanUtils.copyProperties(oReq, option);
                        option.setQuestionId(question.getId());
                        optionMapper.insert(option);
                    }
                }
            }
        }
    }

    public List<Survey> getEmployeePendingSurveys(Long employeeId) {
        return surveyMapper.selectPendingSurveys(employeeId);
    }

    public List<Survey> getEmployeeCompletedSurveys(Long employeeId) {
        return surveyMapper.selectCompletedSurveys(employeeId);
    }

    @Transactional
    public void submitSurveyAnswer(Long employeeId, SurveyAnswerRequest request) {
        Survey survey = surveyMapper.selectById(request.getSurveyId());
        if (survey == null) {
            throw new IllegalArgumentException("问卷不存在");
        }
        if (survey.getDeadline().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("问卷已截止，无法提交");
        }

        SurveyAnswer existing = answerMapper.selectBySurveyAndEmployee(request.getSurveyId(), employeeId);
        if (existing != null) {
            throw new IllegalArgumentException("您已提交过此问卷");
        }

        Employee employee = employeeMapper.selectById(employeeId);

        SurveyAnswer answer = new SurveyAnswer();
        answer.setSurveyId(request.getSurveyId());
        answer.setEmployeeId(employeeId);
        answer.setEmployeeName(employee.getName());
        answer.setDepartmentId(employee.getDepartmentId());
        answer.setDepartmentName(employee.getDepartment());
        answer.setSubmitTime(LocalDateTime.now());
        answerMapper.insert(answer);

        if (request.getAnswers() != null && !request.getAnswers().isEmpty()) {
            for (SurveyAnswerRequest.AnswerItemRequest itemReq : request.getAnswers()) {
                SurveyAnswerItem item = new SurveyAnswerItem();
                BeanUtils.copyProperties(itemReq, item);
                item.setAnswerId(answer.getId());
                answerItemMapper.insert(item);
            }
        }
    }

    public SurveyAnswer getMyAnswer(Long surveyId, Long employeeId) {
        SurveyAnswer answer = answerMapper.selectBySurveyAndEmployee(surveyId, employeeId);
        if (answer != null) {
            answer.setAnswerItems(answerItemMapper.selectByAnswerId(answer.getId()));
        }
        return answer;
    }

    public SurveyStatisticsVO getSurveyStatistics(Long surveyId) {
        Survey survey = surveyMapper.selectSurveyDetail(surveyId);
        if (survey == null) {
            throw new IllegalArgumentException("问卷不存在");
        }

        SurveyStatisticsVO vo = new SurveyStatisticsVO();
        BeanUtils.copyProperties(survey, vo);
        vo.setSurveyId(surveyId);
        vo.setCompletedCount(survey.getCompletedCount());
        vo.setTotalCount(survey.getTotalCount());
        vo.setCompletionRate(survey.getTotalCount() > 0 ? (double) survey.getCompletedCount() / survey.getTotalCount() * 100 : 0);

        List<SurveyTarget> targets = targetMapper.selectBySurveyId(surveyId);
        vo.setTargetNames(targets.stream().map(SurveyTarget::getTargetName).collect(Collectors.toList()));

        List<SurveyQuestion> questions = questionMapper.selectBySurveyId(surveyId);
        List<SurveyStatisticsVO.QuestionStatistics> questionStats = new ArrayList<>();

        for (SurveyQuestion question : questions) {
            SurveyStatisticsVO.QuestionStatistics qs = new SurveyStatisticsVO.QuestionStatistics();
            qs.setQuestionId(question.getId());
            qs.setQuestionType(question.getQuestionType());
            qs.setTitle(question.getTitle());

            if (question.getQuestionType() == 1) {
                List<SurveyQuestionOption> options = optionMapper.selectByQuestionId(question.getId());
                List<SurveyStatisticsVO.RatingStatistics> ratingStats = new ArrayList<>();
                for (SurveyQuestionOption opt : options) {
                    SurveyStatisticsVO.RatingStatistics rs = new SurveyStatisticsVO.RatingStatistics();
                    rs.setRatingItem(opt.getOptionText());
                    Double avg = answerItemMapper.calculateAverageRating(surveyId, question.getId(), opt.getOptionText());
                    rs.setAverageScore(avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0);
                    ratingStats.add(rs);
                }
                qs.setRatingStatistics(ratingStats);
            } else if (question.getQuestionType() == 2) {
                List<SurveyQuestionOption> options = optionMapper.selectWithStatsByQuestionId(question.getId());
                List<SurveyStatisticsVO.OptionStatistics> optStats = new ArrayList<>();
                int total = options.stream().mapToInt(o -> o.getSelectCount() != null ? o.getSelectCount() : 0).sum();
                for (SurveyQuestionOption opt : options) {
                    SurveyStatisticsVO.OptionStatistics os = new SurveyStatisticsVO.OptionStatistics();
                    os.setOptionId(opt.getId());
                    os.setOptionText(opt.getOptionText());
                    os.setSelectCount(opt.getSelectCount() != null ? opt.getSelectCount() : 0);
                    os.setPercentage(total > 0 ? Math.round((double) os.getSelectCount() / total * 1000.0) / 10.0 : 0);
                    optStats.add(os);
                }
                qs.setOptionStatistics(optStats);
            } else if (question.getQuestionType() == 3) {
                List<SurveyAnswerItem> textItems = answerItemMapper.selectTextAnswersBySurveyId(surveyId);
                List<SurveyAnswer> answers = answerMapper.selectBySurveyId(surveyId);
                Map<Long, SurveyAnswer> answerMap = new HashMap<>();
                for (SurveyAnswer a : answers) {
                    answerMap.put(a.getId(), a);
                }

                List<SurveyStatisticsVO.TextAnswer> textAnswers = new ArrayList<>();
                for (SurveyAnswerItem item : textItems) {
                    if (item.getQuestionId().equals(question.getId())) {
                        SurveyStatisticsVO.TextAnswer ta = new SurveyStatisticsVO.TextAnswer();
                        ta.setContent(item.getTextContent());
                        if (survey.getIsAnonymous() == 0) {
                            SurveyAnswer a = answerMap.get(item.getAnswerId());
                            if (a != null) {
                                ta.setEmployeeName(a.getEmployeeName());
                                ta.setDepartmentName(a.getDepartmentName());
                            }
                        }
                        textAnswers.add(ta);
                    }
                }
                qs.setTextAnswers(textAnswers);
            }

            questionStats.add(qs);
        }

        vo.setQuestionStatistics(questionStats);
        return vo;
    }
}

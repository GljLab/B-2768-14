import request from '@/utils/request'

export function getCycleList(params) {
  return request({
    url: '/admin/evaluation-cycles',
    method: 'get',
    params
  })
}

export function getCycleDetail(id) {
  return request({
    url: `/admin/evaluation-cycles/${id}`,
    method: 'get'
  })
}

export function updateCycle(id, data) {
  return request({
    url: `/admin/evaluation-cycles/${id}`,
    method: 'put',
    data
  })
}

export function createCycle(data) {
  return request({
    url: '/admin/evaluation-cycles',
    method: 'post',
    data
  })
}

export function startCycle(id) {
  return request({
    url: `/admin/evaluation-cycles/${id}/start`,
    method: 'post'
  })
}

export function endCycle(id) {
  return request({
    url: `/admin/evaluation-cycles/${id}/end`,
    method: 'post'
  })
}

export function getCycleProgress(id) {
  return request({
    url: `/admin/evaluation-cycles/${id}/progress`,
    method: 'get'
  })
}

export function getCycleEmployees(id) {
  return request({
    url: `/admin/evaluation-cycles/${id}/employees`,
    method: 'get'
  })
}

export function getGradeStandards(id) {
  return request({
    url: `/admin/evaluation-cycles/${id}/grade-standards`,
    method: 'get'
  })
}

export function getDimensions(id) {
  return request({
    url: `/admin/evaluation-cycles/${id}/dimensions`,
    method: 'get'
  })
}

export function getAdminEvaluation(cycleId, employeeId) {
  return request({
    url: `/admin/evaluations/${cycleId}/employee/${employeeId}`,
    method: 'get'
  })
}

export function submitAdminEvaluation(data) {
  return request({
    url: '/admin/evaluations',
    method: 'post',
    data
  })
}

export function getAllColleagueEvaluations(cycleId) {
  return request({
    url: `/admin/evaluations/${cycleId}/colleague-evaluations`,
    method: 'get'
  })
}

export function getColleagueDimensionScores(evaluationId) {
  return request({
    url: `/admin/evaluations/colleague-evaluation/${evaluationId}/dimension-scores`,
    method: 'get'
  })
}

export function deleteColleagueEvaluation(evaluationId) {
  return request({
    url: `/admin/evaluations/colleague-evaluation/${evaluationId}`,
    method: 'delete'
  })
}

export function getEvaluationStatistics(cycleId) {
  return request({
    url: `/admin/evaluations/${cycleId}/statistics`,
    method: 'get'
  })
}

export function getAllFeedbacks(cycleId) {
  return request({
    url: `/admin/evaluations/${cycleId}/feedbacks`,
    method: 'get'
  })
}

export function handleFeedback(feedbackId, reply, adjust) {
  return request({
    url: `/admin/evaluations/feedback/${feedbackId}/handle`,
    method: 'post',
    params: { reply, adjust }
  })
}

export function getAdminMeetings(cycleId, employeeId) {
  return request({
    url: `/admin/evaluations/${cycleId}/meetings`,
    method: 'get',
    params: { employeeId }
  })
}

export function updateAdminMeeting(data) {
  return request({
    url: '/admin/evaluations/meeting/update',
    method: 'post',
    data
  })
}

export function getEmployeeGoals(cycleId, employeeId) {
  return request({
    url: `/admin/evaluations/${cycleId}/employee/${employeeId}/goals`,
    method: 'get'
  })
}

export function getEmployeeGrowthReport(cycleId, employeeId) {
  return request({
    url: `/admin/evaluations/${cycleId}/employee/${employeeId}/report`,
    method: 'get'
  })
}

export function getEmployeeGrowthHistory(employeeId) {
  return request({
    url: `/admin/evaluations/employee/${employeeId}/growth-history`,
    method: 'get'
  })
}

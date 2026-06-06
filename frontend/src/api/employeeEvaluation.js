import request from '@/utils/request'

export function getMyCycles() {
  return request({
    url: '/employee/evaluations/cycles',
    method: 'get'
  })
}

export function getMyGoals(cycleId) {
  return request({
    url: `/employee/evaluations/${cycleId}/goals`,
    method: 'get'
  })
}

export function saveGoals(cycleId, goals) {
  return request({
    url: `/employee/evaluations/${cycleId}/goals`,
    method: 'post',
    data: goals
  })
}

export function submitGoals(cycleId) {
  return request({
    url: `/employee/evaluations/${cycleId}/goals/submit`,
    method: 'post'
  })
}

export function getMyHighlights(cycleId) {
  return request({
    url: `/employee/evaluations/${cycleId}/highlights`,
    method: 'get'
  })
}

export function addHighlight(data) {
  return request({
    url: '/employee/evaluations/highlights',
    method: 'post',
    data
  })
}

export function deleteHighlight(id) {
  return request({
    url: `/employee/evaluations/highlights/${id}`,
    method: 'delete'
  })
}

export function getMySelfEvaluation(cycleId) {
  return request({
    url: `/employee/evaluations/${cycleId}/self-evaluation`,
    method: 'get'
  })
}

export function submitSelfEvaluation(data) {
  return request({
    url: '/employee/evaluations/self-evaluation',
    method: 'post',
    data
  })
}

export function getPendingColleagueEvaluations(cycleId) {
  return request({
    url: `/employee/evaluations/${cycleId}/colleague-evaluations/pending`,
    method: 'get'
  })
}

export function submitColleagueEvaluation(data) {
  return request({
    url: '/employee/evaluations/colleague-evaluation',
    method: 'post',
    data
  })
}

export function getMyGrowthReport(cycleId) {
  return request({
    url: `/employee/evaluations/${cycleId}/report`,
    method: 'get'
  })
}

export function getGrowthHistory() {
  return request({
    url: '/employee/evaluations/growth-history',
    method: 'get'
  })
}

export function getGrowthPlans() {
  return request({
    url: '/employee/evaluations/growth-plans',
    method: 'get'
  })
}

export function saveGrowthPlan(data) {
  return request({
    url: '/employee/evaluations/growth-plan',
    method: 'post',
    data
  })
}

export function submitFeedback(data) {
  return request({
    url: '/employee/evaluations/feedback',
    method: 'post',
    data
  })
}

export function getMyFeedbacks(cycleId) {
  return request({
    url: `/employee/evaluations/${cycleId}/feedbacks`,
    method: 'get'
  })
}

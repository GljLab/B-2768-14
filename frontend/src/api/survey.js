import request from '@/utils/request'

export function getSurveyList(params) {
  return request({
    url: '/admin/surveys',
    method: 'get',
    params
  })
}

export function getSurveyDetail(id) {
  return request({
    url: `/admin/surveys/${id}`,
    method: 'get'
  })
}

export function createSurvey(data) {
  return request({
    url: '/admin/surveys',
    method: 'post',
    data
  })
}

export function updateSurvey(id, data) {
  return request({
    url: `/admin/surveys/${id}`,
    method: 'put',
    data
  })
}

export function deleteSurvey(id) {
  return request({
    url: `/admin/surveys/${id}`,
    method: 'delete'
  })
}

export function getSurveyStatistics(id) {
  return request({
    url: `/admin/surveys/${id}/statistics`,
    method: 'get'
  })
}

export function getPendingSurveys() {
  return request({
    url: '/employee/surveys/pending',
    method: 'get'
  })
}

export function getCompletedSurveys() {
  return request({
    url: '/employee/surveys/completed',
    method: 'get'
  })
}

export function getEmployeeSurveyDetail(id) {
  return request({
    url: `/employee/surveys/${id}`,
    method: 'get'
  })
}

export function submitSurvey(data) {
  return request({
    url: '/employee/surveys/submit',
    method: 'post',
    data
  })
}

export function getMyAnswer(id) {
  return request({
    url: `/employee/surveys/${id}/my-answer`,
    method: 'get'
  })
}

export function getSurveyStats() {
  return request({
    url: '/employee/surveys/stats',
    method: 'get'
  })
}

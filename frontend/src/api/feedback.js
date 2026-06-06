import request from '@/utils/request'

export function submitFeedback(data) {
  return request({
    url: '/employee/feedbacks',
    method: 'post',
    data
  })
}

export function getFeedbackList(params) {
  return request({
    url: '/admin/feedbacks',
    method: 'get',
    params
  })
}

export function getFeedbackDetail(id) {
  return request({
    url: `/admin/feedbacks/${id}`,
    method: 'get'
  })
}

export function markFeedbackRead(id) {
  return request({
    url: `/admin/feedbacks/${id}/read`,
    method: 'put'
  })
}

import request from '@/utils/request'

export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

export function employeeLogin(data) {
  return request({
    url: '/auth/employee/login',
    method: 'post',
    data
  })
}

export function firstLoginChangePassword(data) {
  return request({
    url: '/auth/employee/first-login/change-password',
    method: 'post',
    data
  })
}

import request from '@/utils/request'

export function getEmployeeLoginLogs() {
  return request({
    url: '/employee/security/login-logs',
    method: 'get'
  })
}

export function getEmployeeSessions() {
  return request({
    url: '/employee/security/sessions',
    method: 'get'
  })
}

export function revokeSession(sessionId) {
  return request({
    url: `/employee/security/sessions/${sessionId}/revoke`,
    method: 'post'
  })
}

export function getLatestAbnormalLogin() {
  return request({
    url: '/employee/security/latest-abnormal',
    method: 'get'
  })
}

export function getAdminLoginLogs(params) {
  return request({
    url: '/admin/security/login-logs',
    method: 'get',
    params
  })
}

export function getAbnormalLoginLogs() {
  return request({
    url: '/admin/security/abnormal-logs',
    method: 'get'
  })
}

export function unlockEmployee(employeeId) {
  return request({
    url: `/admin/security/employees/${employeeId}/unlock`,
    method: 'post'
  })
}

export function unlockAdmin(adminId) {
  return request({
    url: `/admin/security/admin/${adminId}/unlock`,
    method: 'post'
  })
}

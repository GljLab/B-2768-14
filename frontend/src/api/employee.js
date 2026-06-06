import request from '@/utils/request'

export function getEmployeeList(params) {
  return request({
    url: '/employees',
    method: 'get',
    params
  })
}

export function getEmployeeById(id) {
  return request({
    url: `/employees/${id}`,
    method: 'get'
  })
}

export function createEmployee(data) {
  return request({
    url: '/employees',
    method: 'post',
    data
  })
}

export function updateEmployee(id, data) {
  return request({
    url: `/employees/${id}`,
    method: 'put',
    data
  })
}

export function deleteEmployee(id) {
  return request({
    url: `/employees/${id}`,
    method: 'delete'
  })
}

export function getEmployeeListByDepartmentIds(params) {
  return request({
    url: '/employees/by-department-ids',
    method: 'get',
    params
  })
}

export function batchUpdateDepartment(data) {
  return request({
    url: '/employees/batch-department',
    method: 'post',
    data
  })
}

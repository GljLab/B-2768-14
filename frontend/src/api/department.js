import request from '@/utils/request'

export const getDepartmentTree = () => {
  return request({
    url: '/departments/tree',
    method: 'get'
  })
}

export const getFlatDepartmentList = () => {
  return request({
    url: '/departments/flat',
    method: 'get'
  })
}

export const getDepartmentById = (id) => {
  return request({
    url: `/departments/${id}`,
    method: 'get'
  })
}

export const createDepartment = (data) => {
  return request({
    url: '/departments',
    method: 'post',
    data
  })
}

export const updateDepartment = (id, data) => {
  return request({
    url: `/departments/${id}`,
    method: 'put',
    data
  })
}

export const deleteDepartment = (id) => {
  return request({
    url: `/departments/${id}`,
    method: 'delete'
  })
}

export const getDepartmentDescendantIds = (id) => {
  return request({
    url: `/departments/${id}/descendant-ids`,
    method: 'get'
  })
}

export const getDepartmentStatistics = () => {
  return request({
    url: '/department-statistics',
    method: 'get'
  })
}

export const getMonthlyChangeStatistics = (months = 12) => {
  return request({
    url: '/department-statistics/monthly-change',
    method: 'get',
    params: { months }
  })
}

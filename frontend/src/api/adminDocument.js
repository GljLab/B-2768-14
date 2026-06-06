import request from '@/utils/request'

export function getDocumentTypes() {
  return request({
    url: '/admin/documents/types',
    method: 'get'
  })
}

export function getPendingAuditList(params) {
  return request({
    url: '/admin/documents/pending',
    method: 'get',
    params
  })
}

export function getAdminDocumentDetail(id) {
  return request({
    url: `/admin/documents/${id}`,
    method: 'get'
  })
}

export function auditDocument(data) {
  return request({
    url: '/admin/documents/audit',
    method: 'post',
    data
  })
}

export function queryDocuments(params) {
  return request({
    url: '/admin/documents',
    method: 'get',
    params
  })
}

export function getAdminDocumentHistory(id) {
  return request({
    url: `/admin/documents/${id}/history`,
    method: 'get'
  })
}

export function getExpiryMonitor() {
  return request({
    url: '/admin/documents/expiry-monitor',
    method: 'get'
  })
}

export function getDocumentStatistics() {
  return request({
    url: '/admin/documents/statistics',
    method: 'get'
  })
}

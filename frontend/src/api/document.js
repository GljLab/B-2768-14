import request from '@/utils/request'

export function getDocumentTypes() {
  return request({
    url: '/employee/documents/types',
    method: 'get'
  })
}

export function getMyDocuments() {
  return request({
    url: '/employee/documents',
    method: 'get'
  })
}

export function getDocumentDetail(id) {
  return request({
    url: `/employee/documents/${id}`,
    method: 'get'
  })
}

export function uploadDocument(data) {
  return request({
    url: '/employee/documents',
    method: 'post',
    data
  })
}

export function withdrawDocument(id) {
  return request({
    url: `/employee/documents/${id}`,
    method: 'delete'
  })
}

export function getDocumentHistory(id) {
  return request({
    url: `/employee/documents/${id}/history`,
    method: 'get'
  })
}

export function getExpiryAlerts() {
  return request({
    url: '/employee/documents/expiry-alerts',
    method: 'get'
  })
}

export function uploadDocumentFile(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/upload/document',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

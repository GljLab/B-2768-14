import request from '@/utils/request'

export function getBirthdayWall(params) {
  return request({
    url: '/employee/birthday/wall',
    method: 'get',
    params
  })
}

export function getAdminBirthdayWall(params) {
  return request({
    url: '/admin/birthday/wall',
    method: 'get',
    params
  })
}

export function getWishes(recipientId) {
  const role = localStorage.getItem('role')
  const baseUrl = role === 'admin' ? '/admin/birthday' : '/employee/birthday'
  return request({
    url: `${baseUrl}/wishes/${recipientId}`,
    method: 'get'
  })
}

export function sendWish(data) {
  const role = localStorage.getItem('role')
  const baseUrl = role === 'admin' ? '/admin/birthday' : '/employee/birthday'
  return request({
    url: `${baseUrl}/wish`,
    method: 'post',
    data
  })
}

export function likeWish(wishId) {
  const role = localStorage.getItem('role')
  const baseUrl = role === 'admin' ? '/admin/birthday' : '/employee/birthday'
  return request({
    url: `${baseUrl}/wish/${wishId}/like`,
    method: 'post'
  })
}

export function unlikeWish(wishId) {
  const role = localStorage.getItem('role')
  const baseUrl = role === 'admin' ? '/admin/birthday' : '/employee/birthday'
  return request({
    url: `${baseUrl}/wish/${wishId}/like`,
    method: 'delete'
  })
}

export function getPartyList(params) {
  const role = localStorage.getItem('role')
  const baseUrl = role === 'admin' ? '/admin/birthday' : '/employee/birthday'
  return request({
    url: `${baseUrl}/party/list`,
    method: 'get',
    params
  })
}

export function getPartyDetail(partyId) {
  const role = localStorage.getItem('role')
  const baseUrl = role === 'admin' ? '/admin/birthday' : '/employee/birthday'
  return request({
    url: `${baseUrl}/party/${partyId}`,
    method: 'get'
  })
}

export function createParty(data) {
  return request({
    url: '/admin/birthday/party',
    method: 'post',
    data
  })
}

export function updatePartyStatus(partyId, status) {
  return request({
    url: '/admin/birthday/party/' + partyId + '/status',
    method: 'put',
    params: { status }
  })
}

export function updateParticipation(partyId, data) {
  return request({
    url: `/employee/birthday/party/${partyId}/participation`,
    method: 'put',
    data
  })
}

export function getParticipants(partyId) {
  const role = localStorage.getItem('role')
  const baseUrl = role === 'admin' ? '/admin/birthday' : '/employee/birthday'
  return request({
    url: `${baseUrl}/party/${partyId}/participants`,
    method: 'get'
  })
}

export function checkin(data) {
  return request({
    url: '/admin/birthday/party/checkin',
    method: 'post',
    data
  })
}

export function uploadPartyPhotos(partyId, photoUrls) {
  return request({
    url: `/admin/birthday/party/${partyId}/photos`,
    method: 'post',
    data: photoUrls
  })
}

export function deletePhoto(photoId) {
  return request({
    url: `/admin/birthday/party/photo/${photoId}`,
    method: 'delete'
  })
}

export function getPhotos(partyId) {
  const role = localStorage.getItem('role')
  const baseUrl = role === 'admin' ? '/admin/birthday' : '/employee/birthday'
  return request({
    url: `${baseUrl}/party/${partyId}/photos`,
    method: 'get'
  })
}

export function likePhoto(photoId) {
  const role = localStorage.getItem('role')
  const baseUrl = role === 'admin' ? '/admin/birthday' : '/employee/birthday'
  return request({
    url: `${baseUrl}/party/${photoId}/photo-like`,
    method: 'post'
  })
}

export function unlikePhoto(photoId) {
  const role = localStorage.getItem('role')
  const baseUrl = role === 'admin' ? '/admin/birthday' : '/employee/birthday'
  return request({
    url: `${baseUrl}/party/${photoId}/photo-like`,
    method: 'delete'
  })
}

export function getMessages() {
  return request({
    url: '/employee/birthday/messages',
    method: 'get'
  })
}

export function getUnreadCount() {
  return request({
    url: '/employee/birthday/messages/unread-count',
    method: 'get'
  })
}

export function markMessageRead(messageId) {
  return request({
    url: `/employee/birthday/messages/${messageId}/read`,
    method: 'put'
  })
}

export function markAllRead() {
  return request({
    url: '/employee/birthday/messages/read-all',
    method: 'put'
  })
}

export function getProfile() {
  return request({
    url: '/employee/birthday/profile',
    method: 'get'
  })
}

export function getEmployeeProfile(employeeId) {
  return request({
    url: `/admin/birthday/profile/${employeeId}`,
    method: 'get'
  })
}

export function getStatistics() {
  return request({
    url: '/admin/birthday/statistics',
    method: 'get'
  })
}

export function exportBirthdayEmployees(month) {
  return request({
    url: '/admin/birthday/export/employees',
    method: 'get',
    params: { month },
    responseType: 'blob'
  })
}

export function exportWishReport(year) {
  return request({
    url: '/admin/birthday/export/wishes',
    method: 'get',
    params: { year },
    responseType: 'blob'
  })
}

export function uploadPartyPhoto(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/upload/party-photo',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function getTimeline() {
  return request({
    url: '/employee/birthday/timeline',
    method: 'get'
  })
}

export function getGrowthData() {
  return request({
    url: '/employee/birthday/growth-data',
    method: 'get'
  })
}

export function saveYearlyMessage(data) {
  return request({
    url: '/employee/birthday/yearly-message',
    method: 'post',
    data
  })
}

export function getPosterData() {
  return request({
    url: '/employee/birthday/poster',
    method: 'get'
  })
}

export function getAdminTimeline(employeeId) {
  return request({
    url: `/admin/birthday/timeline/${employeeId}`,
    method: 'get'
  })
}

export function saveAdminMessage(data) {
  return request({
    url: '/admin/birthday/admin-message',
    method: 'post',
    data
  })
}

export function updateAdminMessage(messageId, data) {
  return request({
    url: `/admin/birthday/admin-message/${messageId}`,
    method: 'put',
    data
  })
}

export function deleteAdminMessage(messageId) {
  return request({
    url: `/admin/birthday/admin-message/${messageId}`,
    method: 'delete'
  })
}

export function getMilestones(params) {
  return request({
    url: '/admin/birthday/milestones',
    method: 'get',
    params
  })
}

export function getAdminPosterData(employeeId) {
  return request({
    url: `/admin/birthday/poster/${employeeId}`,
    method: 'get'
  })
}

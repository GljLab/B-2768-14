import request from '@/utils/request'

export function getProfile() {
  return request({
    url: '/employee/profile',
    method: 'get'
  })
}

export function updateProfile(data) {
  return request({
    url: '/employee/profile',
    method: 'put',
    data
  })
}

export function updateAvatar(avatarUrl) {
  return request({
    url: '/employee/profile/avatar',
    method: 'put',
    data: { avatarUrl: avatarUrl }
  })
}

export function changePassword(data) {
  return request({
    url: '/employee/profile/change-password',
    method: 'put',
    data
  })
}

export function uploadAvatar(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/upload/avatar',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function resetEmployeePassword(id) {
  return request({
    url: `/employees/${id}/reset-password`,
    method: 'post'
  })
}

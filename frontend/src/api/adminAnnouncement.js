import request from '@/utils/request'
import axios from 'axios'
import { useUserStore } from '@/stores/user'

export function uploadAnnouncementAttachment(file, onProgress) {
  const userStore = useUserStore()
  const formData = new FormData()
  formData.append('file', file)
  
  return axios.post('/api/upload/announcement', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
      'Authorization': `Bearer ${userStore.getToken()}`
    },
    onUploadProgress: (progressEvent) => {
      if (onProgress && progressEvent.total) {
        const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total)
        onProgress(percentCompleted)
      }
    }
  })
}

export function getCategories() {
  return request({
    url: '/admin/announcement-categories',
    method: 'get'
  })
}

export function createCategory(data) {
  return request({
    url: '/admin/announcement-categories',
    method: 'post',
    data
  })
}

export function updateCategory(data) {
  return request({
    url: '/admin/announcement-categories',
    method: 'put',
    data
  })
}

export function deleteCategory(id) {
  return request({
    url: `/admin/announcement-categories/${id}`,
    method: 'delete'
  })
}

export function getAnnouncements(params) {
  return request({
    url: '/admin/announcements',
    method: 'get',
    params
  })
}

export function getMyAnnouncements(params) {
  return request({
    url: '/admin/announcements/my',
    method: 'get',
    params
  })
}

export function getAnnouncementDetail(id) {
  return request({
    url: `/admin/announcements/${id}`,
    method: 'get'
  })
}

export function createAnnouncement(data) {
  return request({
    url: '/admin/announcements',
    method: 'post',
    data
  })
}

export function updateAnnouncement(data) {
  return request({
    url: '/admin/announcements',
    method: 'put',
    data
  })
}

export function withdrawAnnouncement(id) {
  return request({
    url: `/admin/announcements/${id}/withdraw`,
    method: 'post'
  })
}

export function deleteAnnouncement(id) {
  return request({
    url: `/admin/announcements/${id}`,
    method: 'delete'
  })
}

export function getAnnouncementStatistics(id) {
  return request({
    url: `/admin/announcements/${id}/statistics`,
    method: 'get'
  })
}

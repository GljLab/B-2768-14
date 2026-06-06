import request from '@/utils/request'

export function getCategories() {
  return request({
    url: '/employee/announcements/categories',
    method: 'get'
  })
}

export function getAnnouncements() {
  return request({
    url: '/employee/announcements',
    method: 'get'
  })
}

export function getArchivedAnnouncements() {
  return request({
    url: '/employee/announcements/archived',
    method: 'get'
  })
}

export function getAnnouncementDetail(id) {
  return request({
    url: `/employee/announcements/${id}`,
    method: 'get'
  })
}

export function confirmAnnouncement(id) {
  return request({
    url: `/employee/announcements/${id}/confirm`,
    method: 'post'
  })
}

export function getUnreadCount() {
  return request({
    url: '/employee/announcements/unread-count',
    method: 'get'
  })
}

export function clearAllUnread() {
  return request({
    url: '/employee/announcements/clear-unread',
    method: 'post'
  })
}

export function getUnconfirmedAnnouncements() {
  return request({
    url: '/employee/announcements/unconfirmed',
    method: 'get'
  })
}

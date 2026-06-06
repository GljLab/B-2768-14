<template>
  <div class="announcements-container">
    <div v-if="urgentAnnouncements.length > 0" class="urgent-banner">
      <div class="urgent-content">
        <WarningOutlined class="urgent-icon" />
        <div class="urgent-list">
          <div
            v-for="item in urgentAnnouncements"
            :key="item.id"
            class="urgent-item"
            @click="viewDetail(item)"
          >
            <span class="urgent-tag">紧急</span>
            <span class="urgent-title">{{ item.title }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="main-content">
      <div class="filter-section">
        <div class="filter-row">
          <a-select v-model:value="filterCategory" placeholder="选择类别" style="width: 160px" allow-clear @change="handleFilter">
            <a-select-option value="all">全部类别</a-select-option>
            <a-select-option v-for="cat in categories" :key="cat.id" :value="cat.id">
              {{ cat.categoryName }}
            </a-select-option>
          </a-select>

          <a-select v-model:value="filterPriority" placeholder="重要程度" style="width: 140px" allow-clear @change="handleFilter">
            <a-select-option value="all">全部程度</a-select-option>
            <a-select-option value="normal">常规</a-select-option>
            <a-select-option value="attention">关注</a-select-option>
            <a-select-option value="urgent">紧急</a-select-option>
          </a-select>

          <a-select v-model:value="filterReadStatus" placeholder="阅览状态" style="width: 140px" allow-clear @change="handleFilter">
            <a-select-option value="all">全部状态</a-select-option>
            <a-select-option value="unread">未阅</a-select-option>
            <a-select-option value="read">已阅</a-select-option>
          </a-select>

          <a-select v-model:value="activeTab" style="width: 140px" @change="handleTabChange">
            <a-select-option value="active">当前信息</a-select-option>
            <a-select-option value="archived">过期归档</a-select-option>
          </a-select>

          <a-input-search
            v-model:value="searchKeyword"
            placeholder="搜索关键词"
            style="width: 240px"
            @search="handleSearch"
            enter-button
            allow-clear
          />
          <a-button v-if="unreadCount > 0" type="link" @click="handleClearAllUnread">
            全部已阅
          </a-button>
        </div>
      </div>

      <div v-if="pinnedAnnouncements.length > 0 && activeTab === 'active'" class="pinned-section">
        <div class="section-title">
          <PushpinOutlined /> 置顶信息
        </div>
        <div
          v-for="item in pinnedAnnouncements"
          :key="'pinned-' + item.id"
          class="announcement-card pinned"
          :class="getPriorityClass(item.priority)"
          @click="viewDetail(item)"
        >
          <div class="card-left">
            <a-badge v-if="!item.isRead" color="red" :offset="[2, 2]">
              <div class="priority-badge" :class="'badge-' + item.priority">
                {{ getPriorityText(item.priority) }}
              </div>
            </a-badge>
            <a-badge v-else>
              <div class="priority-badge" :class="'badge-' + item.priority">
                {{ getPriorityText(item.priority) }}
              </div>
            </a-badge>
            <PushpinOutlined class="pinned-icon" />
          </div>
          <div class="card-content">
            <div class="card-header">
              <h3 class="card-title" v-html="highlightKeyword(item.title)"></h3>
              <a-tag color="blue">{{ item.categoryName }}</a-tag>
            </div>
            <div class="card-meta">
              <span><UserOutlined /> {{ item.publisherName }}</span>
              <span><CalendarOutlined /> {{ formatDateTime(item.publishTime) }}</span>
              <span v-if="item.forceConfirm === 1" class="force-confirm">
                <SafetyOutlined /> 需确认
              </span>
            </div>
            <div class="card-summary" v-html="highlightKeyword(stripHtml(item.content))"></div>
          </div>
          <div class="card-right">
            <div class="read-status" :class="item.isRead ? 'read' : 'unread'">
              {{ item.isRead ? '已阅' : '未阅' }}
            </div>
            <RightOutlined />
          </div>
        </div>
      </div>

      <div class="section-title">
        {{ activeTab === 'active' ? '全部信息' : '归档信息' }}
        <span class="count-badge">{{ filteredAnnouncements.length }} 条</span>
      </div>

      <div v-if="filteredAnnouncements.length === 0 && !loading" class="empty-state">
        <div class="empty-icon">
          <InboxOutlined />
        </div>
        <p class="empty-text">暂无信息</p>
        <p class="empty-desc">{{ activeTab === 'active' ? '当前没有需要查看的信息' : '暂无已过期的归档信息' }}</p>
      </div>

      <div v-else class="announcement-list">
        <div
          v-for="item in displayedAnnouncements"
          :key="item.id"
          class="announcement-card"
          :class="[getPriorityClass(item.priority), { 'unread-card': !item.isRead }]"
          @click="viewDetail(item)"
        >
          <div class="card-left">
            <a-badge v-if="!item.isRead" color="red" :offset="[2, 2]">
              <div class="priority-badge" :class="'badge-' + item.priority">
                {{ getPriorityText(item.priority) }}
              </div>
            </a-badge>
            <a-badge v-else>
              <div class="priority-badge" :class="'badge-' + item.priority">
                {{ getPriorityText(item.priority) }}
              </div>
            </a-badge>
          </div>
          <div class="card-content">
            <div class="card-header">
              <h3 class="card-title" v-html="highlightKeyword(item.title)"></h3>
              <a-tag color="blue">{{ item.categoryName }}</a-tag>
            </div>
            <div class="card-meta">
              <span><UserOutlined /> {{ item.publisherName }}</span>
              <span><CalendarOutlined /> {{ formatDateTime(item.publishTime) }}</span>
              <span v-if="item.forceConfirm === 1" class="force-confirm">
                <SafetyOutlined /> 需确认
              </span>
              <span v-if="activeTab === 'archived'" class="archived-tag">
                <ClockCircleOutlined /> 已归档
              </span>
            </div>
            <div class="card-summary" v-html="highlightKeyword(stripHtml(item.content))"></div>
            <div v-if="item.attachments && item.attachments.length > 0" class="card-attachments">
              <span v-for="(att, idx) in item.attachments" :key="idx" class="attachment-item">
                <PaperClipOutlined /> {{ att.fileName }}
              </span>
            </div>
          </div>
          <div class="card-right">
            <div class="read-status" :class="item.isRead ? 'read' : 'unread'">
              {{ item.isRead ? '已阅' : '未阅' }}
            </div>
            <RightOutlined />
          </div>
        </div>

        <div v-if="hasMore" class="load-more" @click="loadMore">
          <a-button type="text" :loading="loadingMore">
            加载更多
          </a-button>
        </div>
      </div>
    </div>

    <a-modal
      v-model:open="showDetailModal"
      title="信息详情"
      :footer="null"
      :closable="true"
      width="800px"
      @cancel="handleDetailClose"
    >
      <div v-if="currentAnnouncement" class="detail-content">
        <div class="detail-header">
          <div class="detail-priority" :class="'badge-' + currentAnnouncement.priority">
            {{ getPriorityText(currentAnnouncement.priority) }}
          </div>
          <h2 class="detail-title">{{ currentAnnouncement.title }}</h2>
          <div class="detail-tags">
            <a-tag color="blue">{{ currentAnnouncement.categoryName }}</a-tag>
            <a-tag v-if="currentAnnouncement.isTop === 1" color="red">置顶</a-tag>
            <a-tag v-if="currentAnnouncement.forceConfirm === 1" color="orange">需确认</a-tag>
          </div>
        </div>

        <div class="detail-meta">
          <span><UserOutlined /> 发布者：{{ currentAnnouncement.publisherName }}</span>
          <span><CalendarOutlined /> 发布时间：{{ formatDateTime(currentAnnouncement.publishTime) }}</span>
          <span><ClockCircleOutlined /> 有效期：{{ formatDate(currentAnnouncement.startDate) }} ~ {{ formatDate(currentAnnouncement.endDate) }}</span>
        </div>

        <a-divider />

        <div class="detail-body" v-html="currentAnnouncement.content"></div>

        <div v-if="currentAnnouncement.attachments && currentAnnouncement.attachments.length > 0" class="detail-attachments">
          <h4><PaperClipOutlined /> 附加材料</h4>
          <div class="attachment-list">
            <a
              v-for="(att, idx) in currentAnnouncement.attachments"
              :key="idx"
              class="attachment-link"
              :href="getAttachmentUrl(att.filePath)"
              target="_blank"
            >
              <FileOutlined />
              <span class="attachment-name">{{ att.fileName }}</span>
              <span class="attachment-size">({{ formatFileSize(att.fileSize) }})</span>
              <DownloadOutlined />
            </a>
          </div>
        </div>

        <div class="detail-statistics">
          <a-descriptions :column="3" bordered size="small">
            <a-descriptions-item label="已阅人数">
              <span class="stat-number read">{{ currentAnnouncement.readCount || 0 }}</span>
            </a-descriptions-item>
            <a-descriptions-item label="待阅人数">
              <span class="stat-number unread">{{ currentAnnouncement.unreadCount || 0 }}</span>
            </a-descriptions-item>
            <a-descriptions-item label="阅读率">
              <span class="stat-number rate">{{ getReadRate() }}%</span>
            </a-descriptions-item>
          </a-descriptions>
        </div>

        <div class="detail-actions">
          <a-button v-if="currentAnnouncement.forceConfirm === 1 && !currentAnnouncement.isConfirmed" type="primary" @click="handleConfirm" :loading="confirming">
            <SafetyOutlined /> 确认知晓
          </a-button>
          <a-button @click="handleDetailClose">关闭</a-button>
        </div>
      </div>
    </a-modal>

    <a-modal
      v-model:open="showForceConfirmModal"
      :title="null"
      :footer="null"
      :closable="false"
      :maskClosable="false"
      width="500px"
      class="force-confirm-modal"
    >
      <div v-if="forceConfirmItem" class="force-confirm-content">
        <div class="force-confirm-header">
          <WarningOutlined class="force-confirm-icon" />
          <h3>重要通知</h3>
        </div>
        <div class="force-confirm-body">
          <div class="force-confirm-title">{{ forceConfirmItem.title }}</div>
          <div class="force-confirm-desc" v-html="forceConfirmItem.content"></div>
        </div>
        <div class="force-confirm-actions">
          <a-button type="primary" size="large" block @click="handleForceConfirm" :loading="confirming">
            确认知晓
          </a-button>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  UserOutlined,
  CalendarOutlined,
  ClockCircleOutlined,
  RightOutlined,
  InboxOutlined,
  PaperClipOutlined,
  FileOutlined,
  DownloadOutlined,
  PushpinOutlined,
  WarningOutlined,
  SafetyOutlined
} from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import {
  getCategories,
  getAnnouncements,
  getArchivedAnnouncements,
  getAnnouncementDetail,
  confirmAnnouncement,
  getUnreadCount,
  clearAllUnread,
  getUnconfirmedAnnouncements
} from '@/api/employeeAnnouncement'

const loading = ref(false)
const loadingMore = ref(false)
const confirming = ref(false)
const categories = ref([])
const announcements = ref([])
const archivedAnnouncements = ref([])
const unreadCount = ref(0)
const searchKeyword = ref('')
const filterCategory = ref('all')
const filterPriority = ref('all')
const filterReadStatus = ref('all')
const activeTab = ref('active')
const showUnreadOnly = ref(false)
const pageSize = ref(10)
const currentPage = ref(1)

const showDetailModal = ref(false)
const showForceConfirmModal = ref(false)
const currentAnnouncement = ref(null)
const forceConfirmItem = ref(null)
const forceConfirmList = ref([])

const pinnedAnnouncements = computed(() => {
  return announcements.value.filter(item => item.isTop === 1)
})

const urgentAnnouncements = computed(() => {
  return announcements.value.filter(item => item.priority === 'urgent' && !item.isRead)
})

const allAnnouncements = computed(() => {
  return activeTab.value === 'active' ? announcements.value : archivedAnnouncements.value
})

const filteredAnnouncements = computed(() => {
  let result = [...allAnnouncements.value]
  
  if (activeTab.value === 'active') {
    result = result.filter(item => item.isTop !== 1)
  }

  if (filterCategory.value !== 'all') {
    result = result.filter(item => item.categoryId === filterCategory.value)
  }

  if (filterPriority.value !== 'all') {
    result = result.filter(item => item.priority === filterPriority.value)
  }

  if (filterReadStatus.value !== 'all') {
    result = result.filter(item => 
      filterReadStatus.value === 'read' ? item.isRead : !item.isRead
    )
  }

  if (showUnreadOnly.value) {
    result = result.filter(item => !item.isRead)
  }

  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(item => 
      item.title.toLowerCase().includes(keyword) || 
      stripHtml(item.content).toLowerCase().includes(keyword)
    )
  }

  return result
})

const displayedAnnouncements = computed(() => {
  const endIndex = currentPage.value * pageSize.value
  return filteredAnnouncements.value.slice(0, endIndex)
})

const hasMore = computed(() => {
  return displayedAnnouncements.value.length < filteredAnnouncements.value.length
})

const fetchCategories = async () => {
  try {
    const res = await getCategories()
    categories.value = res.data
  } catch (error) {
    console.error('获取类别失败:', error)
  }
}

const fetchAnnouncements = async () => {
  loading.value = true
  try {
    const res = await getAnnouncements()
    announcements.value = res.data
  } catch (error) {
    console.error('获取信息列表失败:', error)
  } finally {
    loading.value = false
  }
}

const fetchArchivedAnnouncements = async () => {
  try {
    const res = await getArchivedAnnouncements()
    archivedAnnouncements.value = res.data
  } catch (error) {
    console.error('获取归档信息失败:', error)
  }
}

const fetchUnreadCount = async () => {
  try {
    const res = await getUnreadCount()
    unreadCount.value = res.data.unreadCount || 0
  } catch (error) {
    console.error('获取待阅数量失败:', error)
  }
}

const fetchUnconfirmedAnnouncements = async () => {
  try {
    const res = await getUnconfirmedAnnouncements()
    forceConfirmList.value = res.data || []
    if (forceConfirmList.value.length > 0) {
      forceConfirmItem.value = forceConfirmList.value[0]
      showForceConfirmModal.value = true
    }
  } catch (error) {
    console.error('获取待确认信息失败:', error)
  }
}

const getPriorityClass = (priority) => {
  const classes = {
    normal: 'priority-normal',
    attention: 'priority-attention',
    urgent: 'priority-urgent'
  }
  return classes[priority] || 'priority-normal'
}

const getPriorityText = (priority) => {
  const texts = {
    normal: '常规',
    attention: '关注',
    urgent: '紧急'
  }
  return texts[priority] || '常规'
}

const stripHtml = (html) => {
  if (!html) return ''
  return html.replace(/<[^>]*>/g, '').substring(0, 150)
}

const highlightKeyword = (text) => {
  if (!searchKeyword.value || !text) return text
  const regex = new RegExp(`(${searchKeyword.value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')})`, 'gi')
  return text.replace(regex, '<span class="highlight">$1</span>')
}

const formatDate = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD')
}

const formatDateTime = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const formatFileSize = (bytes) => {
  if (!bytes) return '0B'
  if (bytes < 1024) return bytes + 'B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + 'KB'
  return (bytes / (1024 * 1024)).toFixed(2) + 'MB'
}

const getAvatarUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  if (url.startsWith('/')) return url
  return '/' + url
}

const getAttachmentUrl = (url) => {
  return getAvatarUrl(url)
}

const getReadRate = () => {
  const read = currentAnnouncement.value?.readCount || 0
  const unread = currentAnnouncement.value?.unreadCount || 0
  const total = read + unread
  if (total === 0) return 0
  return ((read / total) * 100).toFixed(1)
}

const handleFilter = () => {
  currentPage.value = 1
}

const handleSearch = () => {
  currentPage.value = 1
}

const handleTabChange = () => {
  currentPage.value = 1
  if (activeTab.value === 'archived') {
    fetchArchivedAnnouncements()
  }
}

const loadMore = () => {
  loadingMore.value = true
  setTimeout(() => {
    currentPage.value++
    loadingMore.value = false
  }, 500)
}

const viewDetail = async (item) => {
  try {
    const res = await getAnnouncementDetail(item.id)
    currentAnnouncement.value = res.data
    showDetailModal.value = true
    
    if (!item.isRead) {
      item.isRead = true
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    }
  } catch (error) {
    console.error('获取详情失败:', error)
  }
}

const handleDetailClose = () => {
  showDetailModal.value = false
  currentAnnouncement.value = null
}

const handleConfirm = async () => {
  if (!currentAnnouncement.value) return
  confirming.value = true
  try {
    await confirmAnnouncement(currentAnnouncement.value.id)
    message.success('确认成功')
    currentAnnouncement.value.isConfirmed = true
    showDetailModal.value = false
  } catch (error) {
    console.error('确认失败:', error)
  } finally {
    confirming.value = false
  }
}

const handleForceConfirm = async () => {
  if (!forceConfirmItem.value) return
  confirming.value = true
  try {
    await confirmAnnouncement(forceConfirmItem.value.id)
    message.success('确认成功')
    
    forceConfirmList.value.shift()
    if (forceConfirmList.value.length > 0) {
      forceConfirmItem.value = forceConfirmList.value[0]
    } else {
      showForceConfirmModal.value = false
      forceConfirmItem.value = null
    }
  } catch (error) {
    console.error('确认失败:', error)
  } finally {
    confirming.value = false
  }
}

const handleClearAllUnread = () => {
  Modal.confirm({
    title: '确认操作',
    content: '确定要将所有信息标记为已阅吗？',
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      try {
        await clearAllUnread()
        message.success('已清空所有待阅标记')
        unreadCount.value = 0
        announcements.value.forEach(item => {
          item.isRead = true
        })
      } catch (error) {
        console.error('清空失败:', error)
      }
    }
  })
}

onMounted(() => {
  fetchCategories()
  fetchAnnouncements()
  fetchUnreadCount()
  fetchUnconfirmedAnnouncements()
})
</script>

<style scoped>
.urgent-banner {
  background: linear-gradient(90deg, #fff1f0 0%, #ffebe8 100%);
  border-bottom: 2px solid #ff4d4f;
  padding: 12px 24px;
  position: sticky;
  top: 0;
  z-index: 100;
}

.urgent-content {
  display: flex;
  align-items: center;
  gap: 16px;
  max-width: 1400px;
  margin: 0 auto;
}

.urgent-icon {
  font-size: 24px;
  color: #ff4d4f;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.urgent-list {
  flex: 1;
  display: flex;
  gap: 24px;
  overflow-x: auto;
}

.urgent-item {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  white-space: nowrap;
}

.urgent-tag {
  background: #ff4d4f;
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
}

.urgent-title {
  color: #cf1322;
  font-weight: 500;
}

.main-content {
  padding: 0;
  max-width: 1200px;
  margin: 0 auto;
}

.filter-section {
  background: white;
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.filter-row {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin: 20px 0 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.count-badge {
  font-size: 13px;
  color: #999;
  font-weight: normal;
  background: #f0f0f0;
  padding: 2px 8px;
  border-radius: 10px;
}

.pinned-section {
  margin-bottom: 24px;
}

.announcement-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.announcement-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  gap: 16px;
  cursor: pointer;
  transition: all 0.3s;
  border-left: 4px solid transparent;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.announcement-card:hover {
  transform: translateX(4px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.announcement-card.pinned {
  background: linear-gradient(135deg, #fffbe6 0%, #fff7cc 100%);
  border-left-color: #faad14;
}

.announcement-card.priority-urgent {
  border-left-color: #ff4d4f;
}

.announcement-card.priority-attention {
  border-left-color: #faad14;
}

.announcement-card.priority-normal {
  border-left-color: #1890ff;
}

.announcement-card.unread-card {
  background: #f0f7ff;
}

.card-left {
  position: relative;
}

.priority-badge {
  width: 56px;
  height: 56px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 13px;
  color: white;
}

.badge-normal {
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
}

.badge-attention {
  background: linear-gradient(135deg, #faad14 0%, #d48806 100%);
}

.badge-urgent {
  background: linear-gradient(135deg, #ff4d4f 0%, #cf1322 100%);
}

.pinned-icon {
  position: absolute;
  top: -8px;
  right: -8px;
  color: #faad14;
  font-size: 18px;
  background: white;
  border-radius: 50%;
  padding: 2px;
}

.card-content {
  flex: 1;
  min-width: 0;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #999;
  margin-bottom: 8px;
  flex-wrap: wrap;
}

.force-confirm {
  color: #fa8c16;
  font-weight: 500;
}

.archived-tag {
  color: #999;
}

.card-summary {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.card-attachments {
  display: flex;
  gap: 12px;
  margin-top: 8px;
  flex-wrap: wrap;
}

.attachment-item {
  font-size: 12px;
  color: #1890ff;
  display: flex;
  align-items: center;
  gap: 4px;
  background: #e6f7ff;
  padding: 4px 8px;
  border-radius: 4px;
}

.card-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  justify-content: space-between;
  flex-shrink: 0;
}

.read-status {
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 12px;
  font-weight: 500;
}

.read-status.read {
  background: #f6ffed;
  color: #52c41a;
}

.read-status.unread {
  background: #fff1f0;
  color: #ff4d4f;
}

.empty-state {
  text-align: center;
  padding: 80px 40px;
  background: white;
  border-radius: 12px;
}

.empty-icon {
  font-size: 64px;
  color: #d9d9d9;
  margin-bottom: 16px;
}

.empty-text {
  font-size: 18px;
  color: #666;
  margin-bottom: 8px;
}

.empty-desc {
  font-size: 14px;
  color: #999;
}

.load-more {
  text-align: center;
  padding: 20px;
}

.detail-content {
  max-height: 70vh;
  overflow-y: auto;
}

.detail-header {
  text-align: center;
  margin-bottom: 20px;
}

.detail-priority {
  display: inline-flex;
  padding: 4px 12px;
  border-radius: 20px;
  color: white;
  font-weight: 500;
  font-size: 13px;
  margin-bottom: 12px;
}

.detail-title {
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 12px;
}

.detail-tags {
  display: flex;
  justify-content: center;
  gap: 8px;
}

.detail-meta {
  display: flex;
  justify-content: center;
  gap: 24px;
  font-size: 14px;
  color: #666;
  flex-wrap: wrap;
}

.detail-body {
  font-size: 15px;
  line-height: 1.8;
  color: #333;
  margin: 20px 0;
}

.detail-body :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
}

.detail-body :deep(p) {
  margin-bottom: 12px;
}

.detail-attachments {
  margin-top: 24px;
}

.detail-attachments h4 {
  margin: 0 0 12px;
  font-size: 15px;
  font-weight: 600;
}

.attachment-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.attachment-link {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: #fafafa;
  border-radius: 8px;
  text-decoration: none;
  color: inherit;
  transition: all 0.3s;
}

.attachment-link:hover {
  background: #f0f0f0;
}

.attachment-name {
  flex: 1;
  color: #1890ff;
}

.attachment-size {
  color: #999;
  font-size: 13px;
}

.detail-statistics {
  margin-top: 24px;
}

.stat-number {
  font-size: 20px;
  font-weight: 600;
}

.stat-number.read {
  color: #52c41a;
}

.stat-number.unread {
  color: #ff4d4f;
}

.stat-number.rate {
  color: #1890ff;
}

.detail-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.highlight {
  background: #fff59d;
  padding: 0 2px;
  border-radius: 2px;
  color: #d48806;
}

.force-confirm-modal :deep(.ant-modal-content) {
  border-radius: 12px;
  overflow: hidden;
}

.force-confirm-content {
  text-align: center;
}

.force-confirm-header {
  background: linear-gradient(135deg, #fff1f0 0%, #ffccc7 100%);
  padding: 32px 24px;
  margin: -24px -24px 24px;
}

.force-confirm-icon {
  font-size: 48px;
  color: #ff4d4f;
  margin-bottom: 12px;
  animation: pulse 2s infinite;
}

.force-confirm-header h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #cf1322;
}

.force-confirm-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 16px;
}

.force-confirm-desc {
  font-size: 14px;
  color: #666;
  line-height: 1.8;
  max-height: 200px;
  overflow-y: auto;
  text-align: left;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
}

.force-confirm-actions {
  margin-top: 24px;
}

:deep(.ant-btn-primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: all 0.3s;
}

:deep(.ant-btn-primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}
</style>

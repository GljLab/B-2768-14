<template>
  <div class="admin-feedback-container">
    <div class="page-header">
      <h2 class="page-title">意见箱管理</h2>
    </div>

    <div class="main-content">
      <a-row :gutter="[16, 16]">
        <a-col :span="6">
          <a-card class="stat-card" :bordered="false">
            <div class="stat-icon stat-total">
              <InboxOutlined />
            </div>
            <div class="stat-info">
              <p class="stat-value">{{ statistics.totalCount || 0 }}</p>
              <p class="stat-label">总意见数</p>
            </div>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="stat-card" :bordered="false">
            <div class="stat-icon stat-unread">
              <MessageOutlined />
            </div>
            <div class="stat-info">
              <p class="stat-value">{{ statistics.unreadCount || 0 }}</p>
              <p class="stat-label">未读</p>
            </div>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="stat-card" :bordered="false">
            <div class="stat-icon stat-read">
              <CheckCircleOutlined />
            </div>
            <div class="stat-info">
              <p class="stat-value">{{ statistics.readCount || 0 }}</p>
              <p class="stat-label">已读</p>
            </div>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="stat-card" :bordered="false">
            <div class="stat-icon stat-today">
              <CalendarOutlined />
            </div>
            <div class="stat-info">
              <p class="stat-value">{{ statistics.todayCount || 0 }}</p>
              <p class="stat-label">今日新增</p>
            </div>
          </a-card>
        </a-col>
      </a-row>

      <a-card :bordered="false" class="content-card" style="margin-top: 16px">
        <div class="toolbar">
          <a-select v-model:value="filterCategory" style="width: 160px" @change="fetchFeedbackList" allow-clear placeholder="选择分类">
            <a-select-option value="制度建议">制度建议</a-select-option>
            <a-select-option value="薪酬福利">薪酬福利</a-select-option>
            <a-select-option value="工作环境">工作环境</a-select-option>
            <a-select-option value="团队管理">团队管理</a-select-option>
            <a-select-option value="其他">其他</a-select-option>
          </a-select>
          <a-select v-model:value="filterRead" style="width: 120px" @change="fetchFeedbackList" allow-clear placeholder="阅读状态">
            <a-select-option :value="0">未读</a-select-option>
            <a-select-option :value="1">已读</a-select-option>
          </a-select>
        </div>

        <a-table
          :columns="feedbackColumns"
          :data-source="feedbackList"
          :loading="loading"
          :pagination="feedbackPagination"
          @change="handleTableChange"
          @expand="handleExpand"
          :expandable="{ expandedRowRender: expandedRowRender }"
          row-key="id"
          size="middle"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'feedbackNo'">
              <span class="feedback-no">{{ record.feedbackNo }}</span>
            </template>
            <template v-if="column.key === 'category'">
              <a-tag :color="getCategoryColor(record.category)">
                {{ record.category }}
              </a-tag>
            </template>
            <template v-if="column.key === 'content'">
              <span class="content-preview">{{ getContentPreview(record.content) }}</span>
            </template>
            <template v-if="column.key === 'isRead'">
              <a-tag :color="record.isRead === 1 ? 'default' : 'red'">
                {{ record.isRead === 1 ? '已读' : '未读' }}
              </a-tag>
            </template>
            <template v-if="column.key === 'createdAt'">
              {{ formatDate(record.createdAt) }}
            </template>
            <template v-if="column.key === 'actions'">
              <a-space size="small">
                <a-button v-if="record.isRead === 0" type="link" size="small" @click="markAsRead(record.id)">
                  标记已读
                </a-button>
              </a-space>
            </template>
          </template>
        </a-table>
      </a-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, h } from 'vue'
import { message } from 'ant-design-vue'
import {
  InboxOutlined,
  MessageOutlined,
  CheckCircleOutlined,
  CalendarOutlined
} from '@ant-design/icons-vue'
import { getFeedbackList, markFeedbackRead } from '@/api/feedback'
import dayjs from 'dayjs'

const filterCategory = ref('')
const filterRead = ref(null)
const loading = ref(false)
const feedbackList = ref([])
const feedbackPagination = ref({
  current: 1,
  pageSize: 10,
  total: 0
})

const statistics = ref({
  totalCount: 0,
  unreadCount: 0,
  readCount: 0,
  todayCount: 0
})

const feedbackColumns = [
  {
    title: '意见编号',
    dataIndex: 'feedbackNo',
    key: 'feedbackNo',
    width: '15%'
  },
  {
    title: '分类',
    dataIndex: 'category',
    key: 'category',
    width: '12%'
  },
  {
    title: '内容预览',
    dataIndex: 'content',
    key: 'content',
    width: '40%'
  },
  {
    title: '状态',
    dataIndex: 'isRead',
    key: 'isRead',
    width: '10%'
  },
  {
    title: '提交时间',
    dataIndex: 'createdAt',
    key: 'createdAt',
    width: '15%'
  },
  {
    title: '操作',
    key: 'actions',
    width: '8%'
  }
]

const expandedRowRender = ({ record }) => {
  return h('div', { class: 'expanded-content' }, record.content)
}

const getCategoryColor = (category) => {
  const colors = {
    '制度建议': 'blue',
    '薪酬福利': 'orange',
    '工作环境': 'green',
    '团队管理': 'purple',
    '其他': 'default'
  }
  return colors[category] || 'default'
}

const getContentPreview = (content) => {
  if (!content) return ''
  return content.length > 50 ? content.substring(0, 50) + '...' : content
}

const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const fetchFeedbackList = async () => {
  loading.value = true
  try {
    const res = await getFeedbackList({
      pageNum: feedbackPagination.value.current,
      pageSize: feedbackPagination.value.pageSize,
      category: filterCategory.value || undefined,
      isRead: filterRead.value !== null ? filterRead.value : undefined
    })
    if (res.code === 200) {
      feedbackList.value = res.data.records || []
      feedbackPagination.value.total = res.data.total
      
      statistics.value = {
        totalCount: res.data.total || 0,
        unreadCount: (res.data.records || []).filter(f => f.isRead === 0).length,
        readCount: (res.data.records || []).filter(f => f.isRead === 1).length,
        todayCount: (res.data.records || []).filter(f => dayjs(f.createdAt).isSame(dayjs(), 'day')).length
      }
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleTableChange = (pagination) => {
  feedbackPagination.value.current = pagination.current
  feedbackPagination.value.pageSize = pagination.pageSize
  fetchFeedbackList()
}

const handleExpand = (expanded, record) => {
  if (expanded && record.isRead === 0) {
    markAsRead(record.id)
  }
}

const markAsRead = async (id) => {
  try {
    const res = await markFeedbackRead(id)
    if (res.code === 200) {
      message.success('标记成功')
      fetchFeedbackList()
    } else {
      message.error(res.message || '操作失败')
    }
  } catch (e) {
    message.error('操作失败')
  }
}

onMounted(() => {
  fetchFeedbackList()
})
</script>

<style scoped>
.admin-feedback-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-right: 16px;
}

.stat-total {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.stat-unread {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
  color: white;
}

.stat-read {
  background: linear-gradient(135deg, #56ab2f 0%, #a8e063 100%);
  color: white;
}

.stat-today {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.stat-info .stat-value {
  font-size: 28px;
  font-weight: 600;
  margin: 0;
  color: #333;
}

.stat-info .stat-label {
  font-size: 14px;
  color: #666;
  margin: 4px 0 0 0;
}

.content-card {
  border-radius: 8px;
}

.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.feedback-no {
  font-family: monospace;
  font-weight: 600;
  color: #1890ff;
}

.content-preview {
  color: #666;
}

.expanded-content {
  padding: 12px;
  background: #fafafa;
  border-radius: 4px;
  line-height: 1.6;
  color: #333;
}
</style>

<template>
  <div class="admin-survey-container">
    <div class="main-content">
      <a-row :gutter="[16, 16]">
        <a-col :span="8">
          <a-card class="stat-card" :bordered="false">
            <div class="stat-icon stat-active">
              <FileTextOutlined />
            </div>
            <div class="stat-info">
              <p class="stat-value">{{ statistics.activeCount || 0 }}</p>
              <p class="stat-label">进行中问卷</p>
            </div>
          </a-card>
        </a-col>
        <a-col :span="8">
          <a-card class="stat-card" :bordered="false">
            <div class="stat-icon stat-completed">
              <CheckCircleOutlined />
            </div>
            <div class="stat-info">
              <p class="stat-value">{{ statistics.endedCount || 0 }}</p>
              <p class="stat-label">已结束问卷</p>
            </div>
          </a-card>
        </a-col>
        <a-col :span="8">
          <a-card class="stat-card" :bordered="false">
            <div class="stat-icon stat-response">
              <TeamOutlined />
            </div>
            <div class="stat-info">
              <p class="stat-value">{{ statistics.totalResponses || 0 }}</p>
              <p class="stat-label">总回收数</p>
            </div>
          </a-card>
        </a-col>
      </a-row>

      <a-card :bordered="false" class="content-card" style="margin-top: 16px">
        <div class="tab-toolbar">
          <a-space>
            <a-input-search
              v-model:value="searchKeyword"
              placeholder="搜索问卷标题"
              style="width: 240px"
              @search="fetchSurveyList"
              allow-clear
            />
            <a-select v-model:value="filterStatus" style="width: 140px" @change="fetchSurveyList" allow-clear placeholder="选择状态">
              <a-select-option :value="1">进行中</a-select-option>
              <a-select-option :value="2">已结束</a-select-option>
            </a-select>
          </a-space>
          <a-button type="primary" @click="goToCreate">
            <PlusOutlined /> 新建问卷
          </a-button>
        </div>

        <a-table
          :columns="surveyColumns"
          :data-source="surveyList"
          :loading="loading"
          :pagination="surveyPagination"
          @change="handleTableChange"
          row-key="id"
          size="middle"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'title'">
              <div class="title-cell">
                <span class="survey-title">{{ record.title }}</span>
                <a-tag v-if="record.isAnonymous === 1" color="purple">匿名</a-tag>
              </div>
            </template>
            <template v-if="column.key === 'status'">
              <a-tag :color="record.status === 1 ? 'green' : 'default'">
                {{ record.status === 1 ? '进行中' : '已结束' }}
              </a-tag>
            </template>
            <template v-if="column.key === 'completion'">
              <div class="completion-cell">
                <a-progress
                  :percent="Math.round((record.completedCount || 0) / (record.totalCount || 1) * 100)"
                  :size="['small', 8]"
                  :show-info="false"
                />
                <span class="completion-text">{{ record.completedCount || 0 }}/{{ record.totalCount || 0 }}</span>
              </div>
            </template>
            <template v-if="column.key === 'deadline'">
              <div class="deadline-cell">
                <p class="deadline-date">{{ formatDate(record.deadline) }}</p>
                <p v-if="record.status === 1" class="deadline-days" :class="{ urgent: getDaysRemaining(record.deadline) <= 3 }">
                  剩余 {{ getDaysRemaining(record.deadline) }} 天
                </p>
              </div>
            </template>
            <template v-if="column.key === 'actions'">
              <a-space size="small">
                <a-button type="link" size="small" @click="goToStatistics(record.id)">查看统计</a-button>
                <a-button type="link" size="small" @click="goToEdit(record.id)">编辑</a-button>
                <a-popconfirm title="确定要删除这个问卷吗？删除后不可恢复" @confirm="handleDeleteSurvey(record.id)">
                  <a-button type="link" size="small" danger>删除</a-button>
                </a-popconfirm>
              </a-space>
            </template>
          </template>
        </a-table>
      </a-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  FileTextOutlined,
  CheckCircleOutlined,
  TeamOutlined,
  PlusOutlined
} from '@ant-design/icons-vue'
import { getSurveyList, deleteSurvey } from '@/api/survey'
import dayjs from 'dayjs'

const router = useRouter()

const searchKeyword = ref('')
const filterStatus = ref(null)
const loading = ref(false)
const surveyList = ref([])
const surveyPagination = ref({
  current: 1,
  pageSize: 10,
  total: 0
})

const statistics = ref({
  activeCount: 0,
  endedCount: 0,
  totalResponses: 0
})

const surveyColumns = [
  {
    title: '问卷标题',
    dataIndex: 'title',
    key: 'title',
    width: '30%'
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: '10%'
  },
  {
    title: '完成情况',
    dataIndex: 'completion',
    key: 'completion',
    width: '20%'
  },
  {
    title: '截止日期',
    dataIndex: 'deadline',
    key: 'deadline',
    width: '20%'
  },
  {
    title: '操作',
    key: 'actions',
    width: '20%'
  }
]

const fetchSurveyList = async () => {
  loading.value = true
  try {
    const res = await getSurveyList({
      pageNum: surveyPagination.value.current,
      pageSize: surveyPagination.value.pageSize
    })
    if (res.code === 200) {
      surveyList.value = res.data.records
      surveyPagination.value.total = res.data.total
      
      statistics.value = {
        activeCount: surveyList.value.filter(s => s.status === 1).length,
        endedCount: surveyList.value.filter(s => s.status === 2).length,
        totalResponses: surveyList.value.reduce((sum, s) => sum + (s.completedCount || 0), 0)
      }
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleTableChange = (pagination) => {
  surveyPagination.value.current = pagination.current
  surveyPagination.value.pageSize = pagination.pageSize
  fetchSurveyList()
}

const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const getDaysRemaining = (deadline) => {
  return dayjs(deadline).diff(dayjs(), 'day') + 1
}

const goToCreate = () => {
  router.push('/survey/edit')
}

const goToEdit = (id) => {
  router.push(`/survey/edit/${id}`)
}

const goToStatistics = (id) => {
  router.push(`/survey/statistics/${id}`)
}

const handleDeleteSurvey = async (id) => {
  try {
    const res = await deleteSurvey(id)
    if (res.code === 200) {
      message.success('删除成功')
      fetchSurveyList()
    } else {
      message.error(res.message || '删除失败')
    }
  } catch (e) {
    message.error('删除失败')
  }
}

onMounted(() => {
  fetchSurveyList()
})
</script>

<style scoped>
.admin-survey-container {
  padding: 20px;
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

.stat-active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.stat-completed {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.stat-response {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
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

.tab-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.title-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.survey-title {
  font-weight: 500;
}

.completion-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.completion-text {
  font-size: 12px;
  color: #666;
}

.deadline-cell p {
  margin: 0;
}

.deadline-date {
  font-size: 14px;
  color: #333;
}

.deadline-days {
  font-size: 12px;
  color: #52c41a;
}

.deadline-days.urgent {
  color: #ff4d4f;
}
</style>

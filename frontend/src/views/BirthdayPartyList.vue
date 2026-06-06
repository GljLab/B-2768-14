<template>
  <div class="birthday-party-container">
    <div class="page-header">
      <h2 class="page-title">🎉 集体生日会</h2>
      <a-space :size="12">
        <a-select v-model:value="filterYear" style="width: 100px" @change="fetchPartyList">
          <a-select-option v-for="y in yearOptions" :key="y" :value="y">{{ y }}年</a-select-option>
        </a-select>
        <a-select v-model:value="filterMonth" style="width: 90px" @change="fetchPartyList">
          <a-select-option v-for="m in 12" :key="m" :value="m">{{ m }}月</a-select-option>
        </a-select>
        <a-tooltip :title="currentMonthHasParty ? '本月生日会已发布' : ''">
          <a-button
            type="primary"
            :disabled="currentMonthHasParty"
            @click="openCreateModal"
          >
            <PlusOutlined /> 发起生日会
          </a-button>
        </a-tooltip>
      </a-space>
    </div>

    <a-spin :spinning="loading">
      <div v-if="partyList.length === 0 && !loading" class="empty-state">
        <a-empty description="暂无生日会活动" />
      </div>

      <a-row :gutter="[16, 16]">
        <a-col v-for="party in partyList" :key="party.id" :xs="24" :sm="24" :md="12" :lg="8" :xl="8">
          <a-card class="party-card" :bordered="false" hoverable>
            <div class="party-cover" v-if="party.coverImage">
              <img :src="party.coverImage" alt="cover" />
            </div>
            <div class="party-cover party-cover-placeholder" v-else>
              <GiftOutlined class="placeholder-icon" />
            </div>

            <div class="party-body">
              <div class="party-header">
                <h3 class="party-theme">{{ party.theme }}</h3>
                <a-tag :color="getStatusColor(party.status)">{{ getStatusText(party.status) }}</a-tag>
              </div>

              <div class="party-info">
                <div class="info-row">
                  <ClockCircleOutlined />
                  <span>{{ formatDateTime(party.eventTime) }}</span>
                </div>
                <div class="info-row">
                  <EnvironmentOutlined />
                  <span>{{ party.location }}</span>
                </div>
              </div>

              <div class="party-stats">
                <div class="stat-item">
                  <TeamOutlined />
                  <span>{{ party.participantCount || 0 }} 人参与</span>
                </div>
                <div class="stat-item">
                  <CameraOutlined />
                  <span>{{ party.photoCount || 0 }} 张照片</span>
                </div>
              </div>

              <div class="party-actions">
                <a-button type="link" size="small" @click="goToDetail(party.id)">
                  查看详情
                </a-button>
                <a-button
                  v-if="party.status === 0"
                  type="primary"
                  size="small"
                  @click="handleStartParty(party)"
                >
                  开始活动
                </a-button>
                <a-button
                  v-if="party.status === 1"
                  size="small"
                  @click="handleEndParty(party)"
                >
                  结束活动
                </a-button>
              </div>
            </div>
          </a-card>
        </a-col>
      </a-row>
    </a-spin>

    <a-modal
      v-model:open="showCreateModal"
      title="发起生日会"
      :confirm-loading="submitting"
      width="600px"
      :destroyOnClose="true"
      @ok="handleCreateParty"
      @cancel="resetCreateForm"
      ok-text="确认发起"
      cancel-text="取消"
    >
      <a-form :model="createForm" layout="vertical">
        <a-form-item label="主题" required>
          <a-input v-model:value="createForm.theme" placeholder="请输入生日会主题" maxlength="100" show-count />
        </a-form-item>

        <a-form-item label="活动时间" required>
          <a-date-picker
            v-model:value="createForm.eventTime"
            show-time
            format="YYYY-MM-DD HH:mm"
            placeholder="请选择活动时间"
            style="width: 100%"
          />
        </a-form-item>

        <a-form-item label="活动地点" required>
          <a-input v-model:value="createForm.location" placeholder="请输入活动地点" maxlength="200" />
        </a-form-item>

        <a-form-item label="活动流程">
          <a-textarea
            v-model:value="createForm.flow"
            placeholder="请输入活动流程安排"
            :rows="4"
            maxlength="1000"
            show-count
          />
        </a-form-item>

        <a-form-item label="活动亮点">
          <a-textarea
            v-model:value="createForm.highlights"
            placeholder="请输入活动亮点描述（100字以内）"
            :rows="2"
            maxlength="100"
            show-count
          />
        </a-form-item>

        <a-form-item label="封面图片">
          <a-upload
            list-type="picture-card"
            :file-list="coverFileList"
            :custom-request="handleCoverUpload"
            :before-upload="beforeCoverUpload"
            @remove="handleCoverRemove"
            accept="image/*"
          >
            <div v-if="coverFileList.length === 0">
              <PlusOutlined />
              <div style="margin-top: 8px">上传封面</div>
            </div>
          </a-upload>
        </a-form-item>

        <a-form-item v-if="birthdayEmployeeCount > 0">
          <a-alert
            :message="`本月共有 ${birthdayEmployeeCount} 位员工过生日`"
            type="info"
            show-icon
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import {
  PlusOutlined,
  GiftOutlined,
  ClockCircleOutlined,
  EnvironmentOutlined,
  TeamOutlined,
  CameraOutlined
} from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import {
  getPartyList,
  createParty,
  updatePartyStatus,
  getAdminBirthdayWall,
  uploadPartyPhoto
} from '@/api/birthday'

const router = useRouter()

const loading = ref(false)
const partyList = ref([])
const showCreateModal = ref(false)
const submitting = ref(false)
const birthdayEmployeeCount = ref(0)
const coverFileList = ref([])

const now = dayjs()
const filterYear = ref(now.year())
const filterMonth = ref(now.month() + 1)

const yearOptions = computed(() => {
  const current = now.year()
  const years = []
  for (let y = current - 2; y <= current + 1; y++) {
    years.push(y)
  }
  return years
})

const currentMonthHasParty = computed(() => {
  return partyList.value.some(
    p => p.partyYear === filterYear.value && p.partyMonth === filterMonth.value
  )
})

const createForm = reactive({
  theme: '',
  eventTime: null,
  location: '',
  flow: '',
  coverImage: ''
})

const getStatusColor = (status) => {
  const map = { 0: 'blue', 1: 'green', 2: 'default' }
  return map[status] ?? 'default'
}

const getStatusText = (status) => {
  const map = { 0: '未开始', 1: '进行中', 2: '已结束' }
  return map[status] ?? '未知'
}

const formatDateTime = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const fetchPartyList = async () => {
  loading.value = true
  try {
    const res = await getPartyList({
      year: filterYear.value,
      month: filterMonth.value
    })
    partyList.value = res.data || []
  } catch (error) {
    console.error('获取生日会列表失败:', error)
  } finally {
    loading.value = false
  }
}

const fetchBirthdayWall = async () => {
  try {
    const res = await getAdminBirthdayWall({
      year: filterYear.value,
      month: filterMonth.value
    })
    birthdayEmployeeCount.value = (res.data || []).length
  } catch (error) {
    console.error('获取生日墙数据失败:', error)
  }
}

const goToDetail = (id) => {
  router.push(`/birthday-party-detail/${id}`)
}

const handleStartParty = (party) => {
  Modal.confirm({
    title: '确认开始',
    content: `确定要开始「${party.theme}」活动吗？`,
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      try {
        await updatePartyStatus(party.id, 1)
        message.success('活动已开始')
        fetchPartyList()
      } catch (error) {
        console.error('操作失败:', error)
      }
    }
  })
}

const handleEndParty = (party) => {
  Modal.confirm({
    title: '确认结束',
    content: `确定要结束「${party.theme}」活动吗？结束后将无法恢复。`,
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      try {
        await updatePartyStatus(party.id, 2)
        message.success('活动已结束')
        fetchPartyList()
      } catch (error) {
        console.error('操作失败:', error)
      }
    }
  })
}

const openCreateModal = () => {
  resetCreateForm()
  fetchBirthdayWall()
  showCreateModal.value = true
}

const resetCreateForm = () => {
  createForm.theme = ''
  createForm.eventTime = null
  createForm.location = ''
  createForm.flow = ''
  createForm.coverImage = ''
  coverFileList.value = []
  showCreateModal.value = false
}

const beforeCoverUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    message.error('只能上传图片文件')
    return false
  }
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    message.error('图片大小不能超过 5MB')
    return false
  }
  return true
}

const handleCoverUpload = async ({ file, onSuccess, onError }) => {
  try {
    const res = await uploadPartyPhoto(file)
    createForm.coverImage = res.data
    onSuccess(res)
  } catch (error) {
    console.error('上传封面失败:', error)
    onError(error)
    message.error('封面图片上传失败')
  }
}

const handleCoverRemove = () => {
  createForm.coverImage = ''
}

const handleCreateParty = async () => {
  if (!createForm.theme) {
    message.warning('请输入主题')
    return
  }
  if (!createForm.eventTime) {
    message.warning('请选择活动时间')
    return
  }
  if (!createForm.location) {
    message.warning('请输入活动地点')
    return
  }

  submitting.value = true
  try {
    await createParty({
      theme: createForm.theme,
      eventTime: createForm.eventTime.format('YYYY-MM-DD HH:mm'),
      location: createForm.location,
      flow: createForm.flow,
      highlights: createForm.highlights,
      coverImage: createForm.coverImage
    })
    message.success('生日会发起成功')
    resetCreateForm()
    fetchPartyList()
  } catch (error) {
    console.error('发起失败:', error)
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchPartyList()
})
</script>

<style scoped>
.birthday-party-container {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  color: #1f2937;
}

.empty-state {
  padding: 80px 0;
  text-align: center;
}

.party-card {
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s;
}

.party-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(255, 140, 50, 0.15);
}

.party-cover {
  width: 100%;
  height: 160px;
  overflow: hidden;
  margin: -24px -24px 16px -24px;
}

.party-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.party-cover-placeholder {
  background: linear-gradient(135deg, #ff9a56 0%, #ff6b6b 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.placeholder-icon {
  font-size: 48px;
  color: rgba(255, 255, 255, 0.7);
}

.party-body {
  padding: 0;
}

.party-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
  gap: 8px;
}

.party-theme {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.party-info {
  margin-bottom: 12px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #666;
  margin-bottom: 4px;
}

.info-row .anticon {
  color: #ff8c32;
  font-size: 14px;
}

.party-stats {
  display: flex;
  gap: 20px;
  padding: 10px 0;
  border-top: 1px solid #f5f5f5;
  border-bottom: 1px solid #f5f5f5;
  margin-bottom: 12px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #888;
}

.stat-item .anticon {
  color: #ff8c32;
}

.party-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

:deep(.ant-btn-primary) {
  background: linear-gradient(135deg, #ff9a56 0%, #ff6b6b 100%);
  border: none;
  transition: all 0.3s;
}

:deep(.ant-btn-primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 107, 107, 0.4);
}

:deep(.ant-btn-primary:disabled) {
  transform: none;
  box-shadow: none;
  opacity: 0.5;
}

:deep(.ant-tag-blue) {
  background: #e6f7ff;
  border-color: #91d5ff;
  color: #1890ff;
}

:deep(.ant-tag-green) {
  background: #f6ffed;
  border-color: #b7eb8f;
  color: #52c41a;
}

:deep(.ant-card-hoverable:hover) {
  border-color: #ffd591;
}
</style>

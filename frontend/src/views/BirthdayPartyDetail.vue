<template>
  <div class="party-detail-container">
    <div class="page-header">
      <a-button @click="goBack"><ArrowLeftOutlined /> 返回</a-button>
      <h2 class="page-title">生日派对详情</h2>
    </div>

    <a-tabs v-model:activeKey="activeTab" class="party-tabs">
      <a-tab-pane key="info" tab="活动信息">
        <a-spin :spinning="loading">
          <div v-if="party.coverImage" class="cover-image">
            <img :src="resolveUrl(party.coverImage)" alt="cover" />
          </div>
          <div class="party-info-section">
            <h1 class="party-theme">{{ party.theme || '生日派对' }}</h1>
            <div class="info-grid">
              <div class="info-item">
                <CalendarOutlined class="info-icon" />
                <div>
                  <span class="info-label">活动时间</span>
                  <span class="info-value">{{ formatDateTime(party.eventTime) }}</span>
                </div>
              </div>
              <div class="info-item">
                <EnvironmentOutlined class="info-icon" />
                <div>
                  <span class="info-label">活动地点</span>
                  <span class="info-value">{{ party.location || '-' }}</span>
                </div>
              </div>
              <div class="info-item">
                <ScheduleOutlined class="info-icon" />
                <div>
                  <span class="info-label">活动流程</span>
                  <span class="info-value">{{ party.flow || '-' }}</span>
                </div>
              </div>
            </div>
          </div>
          <div v-if="userStore.isEmployee()" class="participation-section">
            <a-divider>参与确认</a-divider>
            <div class="participation-actions">
              <a-button
                :type="participationForm.participationStatus === 1 ? 'primary' : 'default'"
                @click="participationForm.participationStatus = 1"
                class="btn-attend"
              >参加</a-button>
              <a-button
                :type="participationForm.participationStatus === 0 ? 'primary' : 'default'"
                :danger="participationForm.participationStatus === 0"
                @click="participationForm.participationStatus = 0"
                class="btn-decline"
              >不参加</a-button>
              <a-button
                :type="participationForm.participationStatus === 2 ? 'primary' : 'default'"
                @click="participationForm.participationStatus = 2"
                class="btn-pending"
              >待定</a-button>
            </div>
            <a-textarea
              v-model:value="participationForm.remark"
              placeholder="备注（选填）"
              :rows="3"
              style="margin-top: 16px"
            />
            <a-button
              type="primary"
              @click="handleSaveParticipation"
              :loading="savingParticipation"
              style="margin-top: 16px"
            >保存</a-button>
          </div>
        </a-spin>
      </a-tab-pane>

      <a-tab-pane key="participants" tab="寿星名单">
        <a-spin :spinning="participantsLoading">
          <div v-if="participants.length" class="avatar-wall">
            <div v-for="p in participants" :key="p.id" class="participant-card">
              <a-checkbox
                v-if="userStore.isAdmin()"
                :checked="selectedCheckins.includes(p.employeeId)"
                @change="toggleCheckin(p.employeeId)"
                class="checkin-checkbox"
              />
              <a-avatar :size="64" :src="p.avatar ? resolveUrl(p.avatar) : undefined">
                {{ p.employeeName?.charAt(0) }}
              </a-avatar>
              <div class="participant-info">
                <span class="participant-name">{{ p.employeeName }}</span>
                <span class="participant-dept">{{ p.department }}</span>
                <div class="participant-badges">
                  <a-tag :color="statusColor(p.participationStatus)" size="small">
                    {{ statusLabel(p.participationStatus) }}
                  </a-tag>
                  <a-tag v-if="p.checkinStatus === 1" color="cyan" size="small">
                    <CheckCircleOutlined /> 已签到
                  </a-tag>
                </div>
              </div>
            </div>
          </div>
          <a-empty v-else description="暂无参与者" />
          <div v-if="userStore.isAdmin() && selectedCheckins.length > 0" class="checkin-actions">
            <a-button type="primary" @click="handleCheckin" :loading="checkinLoading">
              确认签到 ({{ selectedCheckins.length }}人)
            </a-button>
          </div>
        </a-spin>
      </a-tab-pane>

      <a-tab-pane key="photos" tab="活动相册">
        <div class="photo-header">
          <a-upload
            :customRequest="handlePhotoUpload"
            :showUploadList="false"
            accept="image/*"
            multiple
          >
            <a-button type="primary" :loading="uploadingPhoto">
              <UploadOutlined /> 上传照片
            </a-button>
          </a-upload>
        </div>
        <a-spin :spinning="photosLoading">
          <div v-if="photos.length" class="photo-grid">
            <div v-for="photo in photos" :key="photo.id" class="photo-card">
              <div class="photo-wrapper">
                <a-image :src="resolveUrl(photo.photoUrl)" class="photo-img" />
                <div class="photo-overlay">
                  <a-popconfirm
                    v-if="canDeletePhoto(photo)"
                    title="确定删除这张照片吗？"
                    @confirm="handleDeletePhoto(photo.id)"
                  >
                    <DeleteOutlined class="photo-action-icon" />
                  </a-popconfirm>
                </div>
              </div>
              <div class="photo-footer">
                <span class="photo-uploader">{{ photo.uploadedByName }}</span>
                <span class="photo-like" @click="toggleLike(photo)">
                  <HeartFilled v-if="photo.hasLiked" style="color: #ff4d4f" />
                  <HeartOutlined v-else />
                  {{ photo.likeCount }}
                </span>
              </div>
            </div>
          </div>
          <a-empty v-else description="暂无照片" />
        </a-spin>
      </a-tab-pane>

      <a-tab-pane v-if="userStore.isAdmin()" key="stats" tab="参与统计">
        <a-row :gutter="[16, 16]">
          <a-col :span="6">
            <a-card class="stat-card total">
              <a-statistic title="总参与人数" :value="stats.total" />
            </a-card>
          </a-col>
          <a-col :span="6">
            <a-card class="stat-card attending">
              <a-statistic title="参加" :value="stats.attending" />
            </a-card>
          </a-col>
          <a-col :span="6">
            <a-card class="stat-card not-attending">
              <a-statistic title="不参加" :value="stats.notAttending" />
            </a-card>
          </a-col>
          <a-col :span="6">
            <a-card class="stat-card pending-stat">
              <a-statistic title="待定" :value="stats.pending" />
            </a-card>
          </a-col>
        </a-row>
        <a-row :gutter="[16, 16]" style="margin-top: 16px">
          <a-col :span="12">
            <a-card class="stat-card checked-in">
              <a-statistic title="已签到" :value="stats.checkedIn" />
            </a-card>
          </a-col>
          <a-col :span="12">
            <a-card class="stat-card rate">
              <a-statistic title="参与率">
                <template #formatter>
                  <span class="rate-value">{{ stats.rate }}%</span>
                </template>
              </a-statistic>
            </a-card>
          </a-col>
        </a-row>
      </a-tab-pane>
    </a-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  ArrowLeftOutlined,
  CalendarOutlined,
  EnvironmentOutlined,
  ScheduleOutlined,
  CheckCircleOutlined,
  UploadOutlined,
  DeleteOutlined,
  HeartFilled,
  HeartOutlined
} from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import { useUserStore } from '@/stores/user'
import {
  getPartyDetail,
  getParticipants,
  getPhotos,
  updateParticipation,
  checkin,
  uploadPartyPhoto,
  uploadPartyPhotos,
  deletePhoto,
  likePhoto,
  unlikePhoto
} from '@/api/birthday'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const partyId = route.params.id

const activeTab = ref('info')
const loading = ref(false)
const participantsLoading = ref(false)
const photosLoading = ref(false)
const savingParticipation = ref(false)
const checkinLoading = ref(false)
const uploadingPhoto = ref(false)

const party = ref({})
const participants = ref([])
const photos = ref([])
const selectedCheckins = ref([])

const participationForm = reactive({
  participationStatus: 2,
  remark: ''
})

const stats = computed(() => {
  const list = participants.value
  const total = list.length
  const attending = list.filter(p => p.participationStatus === 1).length
  const notAttending = list.filter(p => p.participationStatus === 0).length
  const pending = list.filter(p => p.participationStatus === 2).length
  const checkedIn = list.filter(p => p.checkinStatus === 1).length
  const rate = total > 0 ? Math.round((attending / total) * 100) : 0
  return { total, attending, notAttending, pending, checkedIn, rate }
})

const statusLabel = (status) => {
  const map = { 0: '不参加', 1: '参加', 2: '待定' }
  return map[status] ?? '未知'
}

const statusColor = (status) => {
  const map = { 0: 'red', 1: 'green', 2: 'orange' }
  return map[status] ?? 'default'
}

const formatDateTime = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const resolveUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  if (url.startsWith('/')) return url
  return '/' + url
}

const canDeletePhoto = (photo) => {
  return userStore.isAdmin() || String(photo.uploadedBy) === String(userStore.employeeId)
}

const loadDetail = async () => {
  loading.value = true
  try {
    const res = await getPartyDetail(partyId)
    if (res.code === 200) {
      party.value = res.data
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const loadParticipants = async () => {
  participantsLoading.value = true
  try {
    const res = await getParticipants(partyId)
    if (res.code === 200) {
      participants.value = res.data || []
      if (userStore.isEmployee()) {
        const mine = participants.value.find(
          p => String(p.employeeId) === String(userStore.employeeId)
        )
        if (mine) {
          participationForm.participationStatus = mine.participationStatus
          participationForm.remark = mine.remark || ''
        }
      }
    }
  } catch (e) {
    console.error(e)
  } finally {
    participantsLoading.value = false
  }
}

const loadPhotos = async () => {
  photosLoading.value = true
  try {
    const res = await getPhotos(partyId)
    if (res.code === 200) {
      photos.value = res.data || []
    }
  } catch (e) {
    console.error(e)
  } finally {
    photosLoading.value = false
  }
}

const handleSaveParticipation = async () => {
  savingParticipation.value = true
  try {
    const res = await updateParticipation(partyId, {
      participationStatus: participationForm.participationStatus,
      remark: participationForm.remark
    })
    if (res.code === 200) {
      message.success('参与状态已更新')
      loadParticipants()
    }
  } catch (e) {
    console.error(e)
    message.error('更新失败')
  } finally {
    savingParticipation.value = false
  }
}

const toggleCheckin = (employeeId) => {
  const idx = selectedCheckins.value.indexOf(employeeId)
  if (idx >= 0) {
    selectedCheckins.value.splice(idx, 1)
  } else {
    selectedCheckins.value.push(employeeId)
  }
}

const handleCheckin = async () => {
  checkinLoading.value = true
  try {
    const res = await checkin({ partyId: Number(partyId), employeeIds: selectedCheckins.value })
    if (res.code === 200) {
      message.success('签到成功')
      selectedCheckins.value = []
      loadParticipants()
    }
  } catch (e) {
    console.error(e)
    message.error('签到失败')
  } finally {
    checkinLoading.value = false
  }
}

const handlePhotoUpload = async (options) => {
  const { file, onSuccess, onError } = options
  uploadingPhoto.value = true
  try {
    const uploadRes = await uploadPartyPhoto(file)
    const photoUrl = uploadRes.data
    await uploadPartyPhotos(partyId, [photoUrl])
    message.success('照片上传成功')
    loadPhotos()
    onSuccess && onSuccess(uploadRes, file)
  } catch (e) {
    console.error(e)
    message.error('照片上传失败')
    onError && onError(e)
  } finally {
    uploadingPhoto.value = false
  }
}

const handleDeletePhoto = async (photoId) => {
  try {
    const res = await deletePhoto(photoId)
    if (res.code === 200) {
      message.success('照片已删除')
      loadPhotos()
    }
  } catch (e) {
    console.error(e)
    message.error('删除失败')
  }
}

const toggleLike = async (photo) => {
  try {
    if (photo.hasLiked) {
      await unlikePhoto(photo.id)
    } else {
      await likePhoto(photo.id)
    }
    loadPhotos()
  } catch (e) {
    console.error(e)
  }
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  loadDetail()
  loadParticipants()
  loadPhotos()
})
</script>

<style scoped>
.party-detail-container {
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  background: linear-gradient(135deg, #ff6b6b, #ffa500);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.cover-image {
  width: 100%;
  height: 300px;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 24px;
}

.cover-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.party-info-section {
  padding: 0 4px;
}

.party-theme {
  font-size: 24px;
  font-weight: 700;
  color: #333;
  margin: 0 0 20px 0;
}

.info-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px 16px;
  background: linear-gradient(135deg, #fff5f5, #fff8f0);
  border-radius: 10px;
}

.info-icon {
  font-size: 20px;
  color: #ff7a45;
  margin-top: 2px;
}

.info-label {
  display: block;
  font-size: 12px;
  color: #999;
  margin-bottom: 2px;
}

.info-value {
  display: block;
  font-size: 14px;
  color: #333;
  white-space: pre-line;
}

.participation-section {
  margin-top: 24px;
  padding: 20px;
  background: #fffbf0;
  border-radius: 12px;
  border: 1px dashed #ffd591;
}

.participation-actions {
  display: flex;
  gap: 12px;
}

.btn-attend.ant-btn-primary {
  background: #52c41a;
  border-color: #52c41a;
}

.btn-decline.ant-btn-primary {
  background: #ff4d4f;
  border-color: #ff4d4f;
}

.btn-pending.ant-btn-primary {
  background: #fa8c16;
  border-color: #fa8c16;
}

.avatar-wall {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}

.participant-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  position: relative;
  transition: transform 0.2s, box-shadow 0.2s;
}

.participant-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.checkin-checkbox {
  position: absolute;
  top: 8px;
  left: 8px;
}

.participant-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.participant-name {
  font-weight: 600;
  font-size: 15px;
  color: #333;
}

.participant-dept {
  font-size: 12px;
  color: #999;
}

.participant-badges {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.checkin-actions {
  margin-top: 16px;
  padding: 16px;
  background: #f6ffed;
  border-radius: 8px;
  text-align: center;
}

.photo-header {
  margin-bottom: 16px;
}

.photo-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.photo-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: transform 0.2s, box-shadow 0.2s;
}

.photo-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.photo-wrapper {
  position: relative;
  aspect-ratio: 1;
  overflow: hidden;
}

.photo-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.photo-wrapper :deep(.ant-image) {
  width: 100%;
  height: 100%;
}

.photo-wrapper :deep(img) {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.photo-card:hover .photo-wrapper :deep(img) {
  transform: scale(1.05);
}

.photo-overlay {
  position: absolute;
  top: 0;
  right: 0;
  padding: 8px;
  opacity: 0;
  transition: opacity 0.2s;
}

.photo-card:hover .photo-overlay {
  opacity: 1;
}

.photo-action-icon {
  font-size: 18px;
  color: #ff4d4f;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 50%;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background 0.2s;
}

.photo-action-icon:hover {
  background: #fff1f0;
}

.photo-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
}

.photo-uploader {
  font-size: 13px;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 60%;
}

.photo-like {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #999;
  cursor: pointer;
  transition: color 0.2s;
  user-select: none;
}

.photo-like:hover {
  color: #ff4d4f;
}

.stat-card {
  border-radius: 12px;
  text-align: center;
  transition: transform 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-card.total :deep(.ant-statistic-title) {
  color: #666;
}

.stat-card.attending :deep(.ant-statistic-title) {
  color: #52c41a;
}

.stat-card.attending :deep(.ant-statistic-content) {
  color: #52c41a;
}

.stat-card.not-attending :deep(.ant-statistic-title) {
  color: #ff4d4f;
}

.stat-card.not-attending :deep(.ant-statistic-content) {
  color: #ff4d4f;
}

.stat-card.pending-stat :deep(.ant-statistic-title) {
  color: #fa8c16;
}

.stat-card.pending-stat :deep(.ant-statistic-content) {
  color: #fa8c16;
}

.stat-card.checked-in :deep(.ant-statistic-title) {
  color: #13c2c2;
}

.stat-card.checked-in :deep(.ant-statistic-content) {
  color: #13c2c2;
}

.rate-value {
  font-size: 28px;
  font-weight: 600;
  background: linear-gradient(135deg, #ff6b6b, #ffa500);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

:deep(.ant-btn-primary) {
  background: linear-gradient(135deg, #ff6b6b, #ffa500);
  border: none;
  transition: all 0.3s;
}

:deep(.ant-btn-primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 107, 107, 0.4);
}

:deep(.ant-tabs-tab.ant-tabs-tab-active .ant-tabs-tab-btn) {
  color: #ff6b6b;
}

:deep(.ant-tabs-ink-bar) {
  background: linear-gradient(135deg, #ff6b6b, #ffa500);
}

@media (max-width: 1200px) {
  .photo-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .photo-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .avatar-wall {
    grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  }
}
</style>

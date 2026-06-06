<template>
  <div class="milestones-container">
    <div class="page-header">
      <h2 class="page-title">📋 生日大事记</h2>
      <a-space :size="12">
        <a-select v-model:value="filterYear" style="width: 100px" @change="fetchMilestones" allowClear placeholder="年份">
          <a-select-option v-for="y in yearOptions" :key="y" :value="y">{{ y }}年</a-select-option>
        </a-select>
        <a-select v-model:value="filterMonth" style="width: 90px" @change="fetchMilestones" allowClear placeholder="月份">
          <a-select-option v-for="m in 12" :key="m" :value="m">{{ m }}月</a-select-option>
        </a-select>
        <a-input-search v-model:value="keyword" placeholder="搜索主题" style="width: 180px" @search="fetchMilestones" allowClear />
      </a-space>
    </div>

    <a-spin :spinning="loading">
      <div v-if="milestones.length === 0 && !loading" class="empty-state">
        <a-empty description="暂无生日大事记" />
      </div>

      <div v-else class="timeline-container">
        <div v-for="(item, index) in milestones" :key="item.partyId" class="timeline-node">
          <div class="timeline-line-area">
            <div class="timeline-dot"></div>
            <div v-if="index < milestones.length - 1" class="timeline-line"></div>
          </div>
          <div class="timeline-content" @click="goToDetail(item.partyId)">
            <div class="ms-header">
              <div class="ms-title">{{ item.theme }}</div>
              <a-tag color="orange">{{ item.partyYear }}年{{ item.partyMonth }}月</a-tag>
            </div>
            <div class="ms-meta">
              <span><CalendarOutlined /> {{ formatDateTime(item.eventTime) }}</span>
              <span><EnvironmentOutlined /> {{ item.location }}</span>
            </div>
            <div class="ms-stats">
              <span class="ms-stat-item">
                <CheckCircleOutlined /> {{ item.checkedInCount }}人签到
              </span>
              <span class="ms-stat-item">
                <TeamOutlined /> {{ item.confirmedCount }}人确认
              </span>
              <span class="ms-stat-item">
                <UserOutlined /> {{ item.totalParticipantCount }}人邀请
              </span>
            </div>
            <div class="ms-highlights">
              <StarOutlined class="hl-icon" />
              <span>{{ item.highlights }}</span>
            </div>
            <div v-if="item.topPhotos && item.topPhotos.length" class="ms-photos">
              <div class="ms-photo-grid">
                <img v-for="(url, idx) in item.topPhotos" :key="idx" :src="getImageUrl(url)" class="ms-photo" />
                <div v-for="n in (4 - item.topPhotos.length)" :key="'ph-' + n" class="ms-photo ms-photo-placeholder">
                  <GiftOutlined />
                </div>
              </div>
            </div>
            <div class="ms-action">
              <a-button type="link" size="small">查看详情 <RightOutlined /></a-button>
            </div>
          </div>
        </div>
      </div>
    </a-spin>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  CalendarOutlined, EnvironmentOutlined, TeamOutlined, UserOutlined,
  CheckCircleOutlined, StarOutlined, GiftOutlined, RightOutlined
} from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import { getMilestones } from '@/api/birthday'

const router = useRouter()
const loading = ref(false)
const milestones = ref([])
const filterYear = ref(undefined)
const filterMonth = ref(undefined)
const keyword = ref('')

const currentYear = new Date().getFullYear()
const yearOptions = computed(() => {
  const years = []
  for (let y = currentYear + 1; y >= currentYear - 5; y--) years.push(y)
  return years
})

const getImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  if (url.startsWith('/')) return url
  return '/' + url
}

const formatDateTime = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const goToDetail = (id) => router.push(`/birthday-party-detail/${id}`)

const fetchMilestones = async () => {
  loading.value = true
  try {
    const params = {}
    if (filterYear.value) params.year = filterYear.value
    if (filterMonth.value) params.month = filterMonth.value
    if (keyword.value) params.keyword = keyword.value
    const res = await getMilestones(params)
    milestones.value = res.data || []
  } catch (e) {
    console.error('获取大事记失败:', e)
  } finally {
    loading.value = false
  }
}

onMounted(() => fetchMilestones())
</script>

<style scoped>
.milestones-container {
  max-width: 900px;
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

.timeline-container {
  position: relative;
}

.timeline-node {
  display: flex;
  gap: 20px;
  position: relative;
}

.timeline-line-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 20px;
  flex-shrink: 0;
}

.timeline-dot {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: #fa8c16;
  border: 3px solid #fff;
  box-shadow: 0 0 0 2px #fa8c16;
  flex-shrink: 0;
  z-index: 1;
}

.timeline-line {
  width: 2px;
  flex: 1;
  background: repeating-linear-gradient(to bottom, #d9d9d9 0, #d9d9d9 4px, transparent 4px, transparent 8px);
  min-height: 20px;
}

.timeline-content {
  flex: 1;
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 20px;
  border: 1px solid #f0f0f0;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  cursor: pointer;
  transition: all 0.3s;
}

.timeline-content:hover {
  border-color: #ffd591;
  box-shadow: 0 4px 16px rgba(250,140,22,0.1);
}

.ms-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.ms-title {
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
}

.ms-meta {
  display: flex;
  gap: 20px;
  margin-bottom: 12px;
  font-size: 13px;
  color: #8c8c8c;
}

.ms-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.ms-meta .anticon {
  color: #fa8c16;
}

.ms-stats {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
}

.ms-stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #666;
  background: #f5f5f5;
  padding: 4px 10px;
  border-radius: 6px;
}

.ms-stat-item .anticon {
  color: #fa8c16;
}

.ms-highlights {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 14px;
  background: linear-gradient(135deg, #fff7e6, #fffbf0);
  border-radius: 8px;
  margin-bottom: 14px;
  font-size: 14px;
  color: #ad6800;
}

.hl-icon {
  color: #fa8c16;
  font-size: 16px;
}

.ms-photos {
  margin-bottom: 12px;
}

.ms-photo-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
}

.ms-photo {
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
  border-radius: 8px;
}

.ms-photo-placeholder {
  background: linear-gradient(135deg, #ffe7ba, #fffbf0);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffd591;
  font-size: 24px;
}

.ms-action {
  text-align: right;
}

.ms-action :deep(.ant-btn-link) {
  color: #fa8c16;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .ms-meta {
    flex-direction: column;
    gap: 4px;
  }

  .ms-stats {
    flex-wrap: wrap;
  }

  .timeline-content {
    padding: 16px;
  }
}
</style>

<template>
  <a-modal :open="visible" title="生日纪念海报" :footer="null" :width="460" @cancel="handleClose" :destroyOnClose="true">
    <div class="poster-wrapper">
      <a-spin :spinning="loading">
        <div class="poster-canvas" ref="posterRef">
          <div class="poster-bg">
            <div class="poster-avatar-area">
              <div class="poster-avatar-ring">
                <img v-if="posterData.avatar" :src="getImageUrl(posterData.avatar)" class="poster-avatar-img" />
                <div v-else class="poster-avatar-default" :style="{ background: avatarColor }">
                  {{ posterData.name ? posterData.name.charAt(0) : '?' }}
                </div>
              </div>
            </div>
            <div class="poster-title">{{ posterData.name }}在公司的第{{ posterData.birthdayOrder }}个生日</div>
            <div class="poster-data-cards">
              <div class="p-data-card">
                <div class="p-data-value">{{ posterData.tenureDisplay }}</div>
                <div class="p-data-label">入职时长</div>
              </div>
              <div class="p-data-card">
                <div class="p-data-value">{{ posterData.totalWishes }}条祝福</div>
                <div class="p-data-label">收到祝福</div>
              </div>
              <div class="p-data-card">
                <div class="p-data-value">{{ posterData.totalParties }}次</div>
                <div class="p-data-label">参加生日会</div>
              </div>
            </div>
            <div class="poster-keywords" v-if="posterData.keywords && posterData.keywords.length">
              <span v-for="kw in posterData.keywords" :key="kw" class="p-keyword">{{ kw }}</span>
            </div>
            <div class="poster-footer">
              <div class="p-date">{{ posterData.birthdayDate }}</div>
              <div class="p-company">{{ posterData.companyName }}</div>
            </div>
          </div>
        </div>
      </a-spin>
      <div class="poster-actions">
        <a-button type="primary" @click="downloadPoster" :loading="downloading">
          <template #icon><DownloadOutlined /></template>
          下载海报
        </a-button>
      </div>
    </div>
  </a-modal>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { message } from 'ant-design-vue'
import { DownloadOutlined } from '@ant-design/icons-vue'
import { getPosterData } from '@/api/birthday'
import html2canvas from 'html2canvas'

const props = defineProps({
  visible: Boolean
})

const emit = defineEmits(['close'])

const loading = ref(false)
const downloading = ref(false)
const posterData = ref({})
const posterRef = ref(null)

const avatarColors = ['#f56a00', '#7265e6', '#ffbf00', '#00a2ae', '#eb2f96', '#52c41a', '#1890ff', '#fa541c']
const avatarColor = computed(() => {
  if (!posterData.value.name) return '#1890ff'
  let hash = 0
  for (let i = 0; i < posterData.value.name.length; i++) hash = posterData.value.name.charCodeAt(i) + ((hash << 5) - hash)
  return avatarColors[Math.abs(hash) % avatarColors.length]
})

const getImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  if (url.startsWith('/')) return url
  return '/' + url
}

const fetchPosterData = async () => {
  loading.value = true
  try {
    const res = await getPosterData()
    posterData.value = res.data || {}
  } catch (e) {
    message.error('获取海报数据失败')
  } finally {
    loading.value = false
  }
}

const downloadPoster = async () => {
  if (!posterRef.value) return
  downloading.value = true
  try {
    const canvas = await html2canvas(posterRef.value, {
      scale: 2,
      useCORS: true,
      backgroundColor: null,
      width: 375,
      height: 667
    })
    const link = document.createElement('a')
    link.download = `${posterData.value.name || '生日'}_纪念海报.png`
    link.href = canvas.toDataURL('image/png')
    link.click()
    message.success('海报已下载')
  } catch (e) {
    message.error('海报生成失败')
  } finally {
    downloading.value = false
  }
}

const handleClose = () => {
  emit('close')
}

onMounted(() => fetchPosterData())
</script>

<style scoped>
.poster-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.poster-canvas {
  width: 375px;
  height: 667px;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 16px;
}

.poster-bg {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 25%, #fdfcfb 50%, #a18cd1 75%, #fbc2eb 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 30px;
  position: relative;
}

.poster-avatar-area {
  margin-top: 20px;
  margin-bottom: 20px;
}

.poster-avatar-ring {
  width: 90px;
  height: 90px;
  border-radius: 50%;
  border: 3px solid rgba(255,255,255,0.8);
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0,0,0,0.15);
}

.poster-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.poster-avatar-default {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  color: white;
  font-weight: 700;
}

.poster-title {
  font-size: 20px;
  font-weight: 700;
  color: #fff;
  text-shadow: 0 2px 8px rgba(0,0,0,0.15);
  margin-bottom: 30px;
  text-align: center;
}

.poster-data-cards {
  display: flex;
  gap: 12px;
  width: 100%;
  margin-bottom: 30px;
}

.p-data-card {
  flex: 1;
  background: rgba(255,255,255,0.85);
  border-radius: 10px;
  padding: 14px 8px;
  text-align: center;
  backdrop-filter: blur(10px);
}

.p-data-value {
  font-size: 15px;
  font-weight: 700;
  color: #1f2937;
  line-height: 1.3;
}

.p-data-label {
  font-size: 11px;
  color: #8c8c8c;
  margin-top: 4px;
}

.poster-keywords {
  display: flex;
  gap: 10px;
  margin-bottom: 30px;
}

.p-keyword {
  padding: 6px 14px;
  border-radius: 16px;
  font-size: 13px;
  font-weight: 500;
  color: #fff;
  background: linear-gradient(135deg, #ff9a56, #ff6b6b);
}

.poster-footer {
  position: absolute;
  bottom: 30px;
  text-align: center;
}

.p-date {
  font-size: 14px;
  color: rgba(255,255,255,0.9);
  margin-bottom: 4px;
}

.p-company {
  font-size: 12px;
  color: rgba(255,255,255,0.7);
}

.poster-actions {
  width: 100%;
  display: flex;
  justify-content: center;
}

.poster-actions :deep(.ant-btn-primary) {
  background: linear-gradient(135deg, #ff9a56 0%, #ff6b6b 100%);
  border: none;
  border-radius: 8px;
  min-width: 200px;
}
</style>

<template>
  <div class="birthday-profile-container">
    <a-spin :spinning="loading">
      <div class="top-bar">
        <div class="growth-cards">
          <div class="growth-card gc-birthday">
            <div class="gc-icon">🎂</div>
            <div class="gc-value">{{ growthData.birthdayCount || 0 }}</div>
            <div class="gc-label">在公司过生日</div>
          </div>
          <div class="growth-card gc-wish">
            <div class="gc-icon">💌</div>
            <div class="gc-value">{{ growthData.totalWishes || 0 }}</div>
            <div class="gc-label">收到祝福</div>
          </div>
          <div class="growth-card gc-party">
            <div class="gc-icon">🎉</div>
            <div class="gc-value">{{ growthData.totalParties || 0 }}</div>
            <div class="gc-label">参加生日会</div>
          </div>
          <div class="growth-card gc-msg">
            <div class="gc-icon">✍️</div>
            <div class="gc-value">{{ growthData.messageCount || 0 }}</div>
            <div class="gc-label">写过的寄语</div>
          </div>
        </div>
        <a-button type="primary" class="poster-btn" @click="showPoster = true">
          <template #icon><PictureOutlined /></template>
          生成纪念海报
        </a-button>
      </div>

      <a-tabs v-model:activeKey="activeTab" class="profile-tabs">
        <a-tab-pane key="wishes" tab="💌 今年祝福">
          <div class="section">
            <div v-if="!profile.yearWishes || profile.yearWishes.length === 0" class="empty-wrapper">
              <a-empty description="今年还没有收到祝福哦" />
            </div>
            <div v-else class="wish-list">
              <div v-for="wish in profile.yearWishes" :key="wish.id" class="wish-item">
                <div class="wish-avatar">
                  <a-avatar v-if="wish.senderAvatar" :src="getAvatarUrl(wish.senderAvatar)" :size="44" />
                  <a-avatar v-else :size="44" :style="{ backgroundColor: getAvatarColor(wish.senderName) }">
                    {{ wish.senderName ? wish.senderName.charAt(0) : '?' }}
                  </a-avatar>
                </div>
                <div class="wish-body">
                  <div class="wish-header">
                    <span class="wish-sender">{{ wish.senderName }}</span>
                    <a-tag v-if="wish.isSystem" color="blue" size="small" class="system-tag">系统</a-tag>
                    <span class="wish-time">{{ formatDateTime(wish.createTime) }}</span>
                  </div>
                  <div class="wish-content">{{ wish.content }}</div>
                  <div class="wish-footer">
                    <span class="wish-likes">
                      <HeartOutlined :style="{ color: wish.isLiked ? '#ff4d4f' : '#999' }" />
                      <span :class="{ 'liked': wish.isLiked }">{{ wish.likeCount || 0 }}</span>
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </a-tab-pane>

        <a-tab-pane key="parties" tab="🎂 参加的生日会">
          <div class="section">
            <div v-if="!profile.parties || profile.parties.length === 0" class="empty-wrapper">
              <a-empty description="还没有参加过生日会" />
            </div>
            <div v-else class="party-list">
              <div v-for="party in profile.parties" :key="party.id" class="party-card" @click="goToPartyDetail(party.id)">
                <div class="party-theme">{{ party.theme }}</div>
                <div class="party-meta">
                  <div class="party-meta-item"><CalendarOutlined /><span>{{ formatDateTime(party.eventTime) }}</span></div>
                  <div class="party-meta-item"><EnvironmentOutlined /><span>{{ party.location || '-' }}</span></div>
                </div>
                <div class="party-footer">
                  <a-tag :color="getPartyStatusColor(party.status)">{{ getPartyStatusText(party.status) }}</a-tag>
                  <RightOutlined class="party-arrow" />
                </div>
              </div>
            </div>
          </div>
        </a-tab-pane>

        <a-tab-pane key="timeline" tab="📅 生日时间轴">
          <div class="timeline-section">
            <div v-if="!timeline || timeline.length === 0" class="empty-wrapper">
              <a-empty description="暂无时间轴记录" />
            </div>
            <div v-else class="timeline-container">
              <div v-for="(item, index) in timeline" :key="item.year" class="timeline-node">
                <div class="timeline-line-area">
                  <div class="timeline-dot" :class="{ 'dot-current': item.year === currentYear }"></div>
                  <div v-if="index < timeline.length - 1" class="timeline-line"></div>
                </div>
                <div class="timeline-content" :class="{ 'content-current': item.year === currentYear }">
                  <div class="timeline-header">
                    <div class="timeline-year">{{ item.year }}年 - 在公司的第{{ item.birthdayOrder }}个生日</div>
                    <a-tag v-if="item.isLeft" color="red">已离职</a-tag>
                  </div>
                  <div class="timeline-tenure">入职 {{ item.tenureDisplay }}</div>
                  <div class="timeline-stats">
                    <span class="tl-stat"><HeartOutlined /> {{ item.wishCount || 0 }} 条祝福</span>
                  </div>

                  <div class="timeline-messages">
                    <div v-if="item.employeeMessage" class="msg-block msg-employee">
                      <div class="msg-label">我的寄语</div>
                      <div class="msg-content">{{ item.employeeMessage }}</div>
                      <a-button v-if="item.canEditEmployeeMessage" type="link" size="small" @click="openEditMessage(item)">修改</a-button>
                    </div>
                    <div v-else-if="item.canEditEmployeeMessage" class="msg-block msg-employee msg-empty" @click="openEditMessage(item)">
                      点击添加寄语
                    </div>

                    <div v-for="am in item.adminMessages" :key="am.id" class="msg-block msg-admin">
                      <div class="msg-label">管理员寄语 · {{ am.adminName }}</div>
                      <div class="msg-content">{{ am.content }}</div>
                    </div>
                  </div>

                  <div v-if="item.partyInfo" class="timeline-party">
                    <div class="tl-party-title"><GiftOutlined /> {{ item.partyInfo.theme }}</div>
                    <div class="tl-party-meta">{{ formatDateTime(item.partyInfo.eventTime) }} · {{ item.partyInfo.location }}</div>
                    <div v-if="item.partyInfo.photoUrls && item.partyInfo.photoUrls.length" class="tl-party-photos">
                      <img v-for="(url, idx) in item.partyInfo.photoUrls" :key="idx" :src="getAvatarUrl(url)" class="tl-photo-thumb" />
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </a-tab-pane>
      </a-tabs>
    </a-spin>

    <a-modal v-model:open="msgModalVisible" title="编辑我的寄语" @ok="handleSaveMessage" :confirm-loading="msgSaving">
      <a-textarea v-model:value="msgContent" :maxlength="200" show-count :rows="4" placeholder="对自己说一句话或新年愿望..." />
    </a-modal>

    <BirthdayPoster v-if="showPoster" :visible="showPoster" @close="showPoster = false" />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { HeartOutlined, CalendarOutlined, EnvironmentOutlined, RightOutlined, GiftOutlined, PictureOutlined } from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import { getProfile, saveYearlyMessage } from '@/api/birthday'
import BirthdayPoster from './BirthdayPoster.vue'

const router = useRouter()
const loading = ref(false)
const activeTab = ref('wishes')
const showPoster = ref(false)
const profile = ref({ totalWishes: 0, totalParties: 0, yearWishes: [], parties: [], growthData: {}, timeline: [] })
const growthData = computed(() => profile.value.growthData || {})
const timeline = computed(() => profile.value.timeline || [])
const currentYear = computed(() => new Date().getFullYear())

const msgModalVisible = ref(false)
const msgContent = ref('')
const msgYear = ref(null)
const msgSaving = ref(false)

const avatarColors = ['#f56a00', '#7265e6', '#ffbf00', '#00a2ae', '#eb2f96', '#52c41a', '#1890ff', '#fa541c']

const getAvatarColor = (name) => {
  if (!name) return '#1890ff'
  let hash = 0
  for (let i = 0; i < name.length; i++) hash = name.charCodeAt(i) + ((hash << 5) - hash)
  return avatarColors[Math.abs(hash) % avatarColors.length]
}

const getAvatarUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  if (url.startsWith('/')) return url
  return '/' + url
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm')
}

const getPartyStatusColor = (status) => ({ 0: 'default', 1: 'processing', 2: 'success', 3: 'warning' }[status] ?? 'default')
const getPartyStatusText = (status) => ({ 0: '未开始', 1: '进行中', 2: '已完成', 3: '已取消' }[status] ?? '未知')

const goToPartyDetail = (id) => router.push(`/emp-birthday-party-detail/${id}`)

const openEditMessage = (item) => {
  msgYear.value = item.year
  msgContent.value = item.employeeMessage || ''
  msgModalVisible.value = true
}

const handleSaveMessage = async () => {
  if (!msgContent.value.trim()) { message.warning('请输入寄语内容'); return }
  msgSaving.value = true
  try {
    await saveYearlyMessage({ year: msgYear.value, content: msgContent.value })
    message.success('保存成功')
    msgModalVisible.value = false
    fetchProfile()
  } catch (e) {
    message.error('保存失败')
  } finally {
    msgSaving.value = false
  }
}

const fetchProfile = async () => {
  loading.value = true
  try {
    const res = await getProfile()
    profile.value = res.data || { totalWishes: 0, totalParties: 0, yearWishes: [], parties: [], growthData: {}, timeline: [] }
  } catch (error) {
    message.error('获取生日档案失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => fetchProfile())
</script>

<style scoped>
.birthday-profile-container {
  max-width: 960px;
  margin: 0 auto;
}

.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  gap: 16px;
}

.growth-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  flex: 1;
}

.growth-card {
  border-radius: 14px;
  padding: 20px 16px;
  text-align: center;
  color: white;
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
  transition: transform 0.3s;
}

.growth-card:hover {
  transform: translateY(-4px);
}

.gc-birthday { background: linear-gradient(135deg, #ff9a56 0%, #ff6b6b 100%); }
.gc-wish { background: linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%); }
.gc-party { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.gc-msg { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); }

.gc-icon { font-size: 28px; margin-bottom: 6px; }
.gc-value { font-size: 28px; font-weight: 700; line-height: 1.2; }
.gc-label { font-size: 13px; opacity: 0.9; margin-top: 4px; }

.poster-btn {
  background: linear-gradient(135deg, #ff9a56 0%, #ff6b6b 100%);
  border: none;
  border-radius: 10px;
  height: 44px;
  flex-shrink: 0;
}

.poster-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255,107,107,0.4);
}

.profile-tabs :deep(.ant-tabs-nav) {
  margin-bottom: 0;
}

.section {
  background: white;
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  margin-top: 20px;
}

.empty-wrapper {
  padding: 40px 0;
}

.wish-list { display: flex; flex-direction: column; }
.wish-item { display: flex; gap: 16px; padding: 18px 0; border-bottom: 1px solid #f5f5f5; }
.wish-item:last-child { border-bottom: none; }
.wish-avatar { flex-shrink: 0; }
.wish-body { flex: 1; min-width: 0; }
.wish-header { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; }
.wish-sender { font-size: 15px; font-weight: 600; color: #1f2937; }
.system-tag { font-size: 11px; line-height: 1; padding: 0 6px; margin: 0; }
.wish-time { font-size: 12px; color: #bfbfbf; margin-left: auto; }
.wish-content { font-size: 14px; color: #4b5563; line-height: 1.7; margin-bottom: 8px; word-break: break-word; }
.wish-footer { display: flex; align-items: center; }
.wish-likes { display: flex; align-items: center; gap: 4px; font-size: 13px; color: #999; cursor: default; }
.wish-likes .liked { color: #ff4d4f; }

.party-list { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; }
.party-card { background: linear-gradient(135deg, #fffbf0 0%, #fff5eb 100%); border: 1px solid #ffe7ba; border-radius: 12px; padding: 20px; cursor: pointer; transition: all 0.3s; }
.party-card:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(250,173,20,0.15); border-color: #ffd591; }
.party-theme { font-size: 16px; font-weight: 600; color: #1f2937; margin-bottom: 12px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.party-meta { display: flex; flex-direction: column; gap: 6px; margin-bottom: 14px; }
.party-meta-item { display: flex; align-items: center; gap: 6px; font-size: 13px; color: #8c8c8c; }
.party-footer { display: flex; align-items: center; justify-content: space-between; }
.party-arrow { color: #d9d9d9; font-size: 12px; transition: color 0.3s, transform 0.3s; }
.party-card:hover .party-arrow { color: #fa8c16; transform: translateX(2px); }

.timeline-section {
  background: white;
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  margin-top: 20px;
}

.timeline-container { position: relative; }

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
  background: #d9d9d9;
  border: 3px solid #fff;
  box-shadow: 0 0 0 2px #d9d9d9;
  flex-shrink: 0;
  z-index: 1;
}

.timeline-dot.dot-current {
  background: #fa8c16;
  box-shadow: 0 0 0 2px #fa8c16, 0 0 8px rgba(250,140,22,0.4);
}

.timeline-line {
  width: 2px;
  flex: 1;
  background: repeating-linear-gradient(to bottom, #d9d9d9 0, #d9d9d9 4px, transparent 4px, transparent 8px);
  min-height: 20px;
}

.timeline-content {
  flex: 1;
  background: #fafafa;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  border: 1px solid #f0f0f0;
}

.timeline-content.content-current {
  border-color: #ffd591;
  background: #fffbf0;
}

.timeline-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.timeline-year {
  font-size: 17px;
  font-weight: 700;
  color: #1f2937;
}

.timeline-tenure {
  font-size: 13px;
  color: #8c8c8c;
  margin-bottom: 10px;
}

.timeline-stats {
  display: flex;
  gap: 16px;
  margin-bottom: 14px;
}

.tl-stat {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #666;
}

.timeline-messages {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 14px;
}

.msg-block {
  padding: 12px 16px;
  border-radius: 8px;
  border-left: 3px solid;
}

.msg-block.msg-employee {
  background: #f0f5ff;
  border-left-color: #1890ff;
}

.msg-block.msg-admin {
  background: #fff7e6;
  border-left-color: #fa8c16;
}

.msg-block.msg-empty {
  cursor: pointer;
  color: #999;
  font-style: italic;
  border-left-color: #d9d9d9;
  background: #fafafa;
}

.msg-block.msg-empty:hover {
  background: #f0f0f0;
}

.msg-label {
  font-size: 12px;
  color: #8c8c8c;
  margin-bottom: 4px;
}

.msg-content {
  font-size: 14px;
  color: #1f2937;
  line-height: 1.6;
  font-style: italic;
}

.timeline-party {
  padding: 12px 16px;
  background: linear-gradient(135deg, #fff5eb, #fffbf0);
  border-radius: 8px;
  border: 1px solid #ffe7ba;
}

.tl-party-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
}

.tl-party-meta {
  font-size: 12px;
  color: #8c8c8c;
  margin-bottom: 8px;
}

.tl-party-photos {
  display: flex;
  gap: 8px;
}

.tl-photo-thumb {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
}

@media (max-width: 768px) {
  .top-bar {
    flex-direction: column;
  }
  .growth-cards {
    grid-template-columns: repeat(2, 1fr);
  }
  .poster-btn {
    width: 100%;
  }
  .party-list {
    grid-template-columns: 1fr;
  }
  .section, .timeline-section {
    padding: 20px;
  }
}

@media (min-width: 769px) and (max-width: 1024px) {
  .growth-cards {
    gap: 10px;
  }
  .growth-card {
    padding: 16px 12px;
  }
  .gc-value {
    font-size: 24px;
  }
}
</style>

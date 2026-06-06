<template>
  <div class="birthday-wall">
    <div class="wall-header">
      <div class="header-left">
        <h1 class="page-title">🎂 生日墙</h1>
        <span class="employee-count" v-if="employees.length">
          共 {{ employees.length }} 位寿星
        </span>
      </div>
      <div class="month-nav">
        <a-button class="nav-btn" @click="prevMonth" :disabled="loading">
          <LeftOutlined />
        </a-button>
        <span class="month-display">{{ currentYear }}年{{ currentMonth }}月</span>
        <a-button class="nav-btn" @click="nextMonth" :disabled="loading">
          <RightOutlined />
        </a-button>
      </div>
    </div>

    <a-spin :spinning="loading">
      <div v-if="!loading && employees.length === 0" class="empty-state">
        <a-empty description="本月暂无过生日的同事" />
      </div>

      <a-row :gutter="[20, 20]" v-else>
        <a-col
          v-for="emp in employees"
          :key="emp.id"
          :xs="24" :sm="12" :md="8" :lg="6"
        >
          <div
            :class="['birthday-card', { 'today-birthday': emp.isToday }]"
            @click="openWishModal(emp)"
          >
            <div v-if="emp.isToday" class="today-badge">🎂今日生日</div>
            <div class="card-content">
              <a-avatar
                :size="80"
                :src="emp.avatar ? getAvatarUrl(emp.avatar) : undefined"
                :style="!emp.avatar ? { backgroundColor: getAvatarColor(emp.name), fontSize: '32px' } : {}"
              >
                {{ emp.name ? emp.name.charAt(0) : 'U' }}
              </a-avatar>
              <div class="emp-name">{{ emp.name }}</div>
              <div class="emp-department">{{ emp.department }}</div>
              <div class="emp-birthday">{{ formatBirthday(emp.birthdayDisplay || emp.birthdayDate) }}</div>
              <div class="emp-hire-years">入职{{ emp.hireYears }}年</div>
            </div>
            <div class="card-hover-overlay">
              <a-button type="primary" shape="round" class="wish-btn">
                🎉 送祝福
              </a-button>
            </div>
          </div>
        </a-col>
      </a-row>
    </a-spin>

    <a-modal
      v-model:open="wishModalOpen"
      :title="`给 ${selectedEmployee?.name} 送祝福`"
      :footer="null"
      width="560px"
      :destroyOnClose="true"
      class="wish-modal"
      @cancel="closeWishModal"
    >
      <div class="wish-send-area">
        <a-textarea
          v-model:value="wishContent"
          placeholder="写下你的祝福吧..."
          :auto-size="{ minRows: 3, maxRows: 5 }"
          :maxlength="200"
          show-count
        />
        <a-button
          type="primary"
          class="send-wish-btn"
          :loading="sendingWish"
          :disabled="!wishContent.trim()"
          @click="handleSendWish"
        >
          发送祝福
        </a-button>
      </div>

      <a-divider style="margin: 16px 0" />

      <div v-if="isAdmin" class="admin-message-area" style="margin-bottom: 16px">
        <a-button type="dashed" block @click="showAdminMsgModal = true">
          <template #icon><EditOutlined /></template>
          写生日寄语
        </a-button>
      </div>

      <div class="wish-list-header">
        <span>祝福墙</span>
        <span class="wish-count">{{ wishes.length }} 条祝福</span>
      </div>

      <a-spin :spinning="loadingWishes">
        <div v-if="!loadingWishes && wishes.length === 0" class="wish-empty">
          <p>还没有人送祝福，快来抢沙发吧！🎉</p>
        </div>
        <div v-else class="wish-list">
          <div v-for="wish in wishes" :key="wish.id" class="wish-item">
            <a-avatar
              :size="36"
              :src="wish.senderAvatar ? getAvatarUrl(wish.senderAvatar) : undefined"
              :style="!wish.senderAvatar ? { backgroundColor: getAvatarColor(wish.senderName), fontSize: '14px' } : {}"
            >
              {{ wish.senderName ? wish.senderName.charAt(0) : 'U' }}
            </a-avatar>
            <div class="wish-body">
              <div class="wish-sender">
                <span class="sender-name">{{ wish.senderName }}</span>
                <a-tag v-if="wish.isSystem" color="blue" size="small" style="margin-left: 6px; font-size: 11px">系统</a-tag>
                <span class="wish-time">{{ formatWishTime(wish.createdAt) }}</span>
              </div>
              <div class="wish-content">{{ wish.content }}</div>
              <div class="wish-actions">
                <span
                  :class="['like-btn', { liked: wish.hasLiked }]"
                  @click.stop="toggleLike(wish)"
                >
                  <HeartFilled v-if="wish.hasLiked" />
                  <HeartOutlined v-else />
                  {{ wish.likeCount || 0 }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </a-spin>
    </a-modal>

    <a-modal v-model:open="showAdminMsgModal" title="写生日寄语" @ok="handleSaveAdminMsg" :confirm-loading="savingAdminMsg">
      <a-form layout="vertical">
        <a-form-item label="选择年份">
          <a-select v-model:value="adminMsgYear" style="width: 100%">
            <a-select-option v-for="y in adminMsgYearOptions" :key="y" :value="y">{{ y }}年</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="寄语内容">
          <a-textarea v-model:value="adminMsgContent" :maxlength="500" show-count :rows="4" placeholder="写下你的寄语..." />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  LeftOutlined,
  RightOutlined,
  HeartOutlined,
  HeartFilled,
  EditOutlined
} from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
import { useUserStore } from '@/stores/user'
import {
  getBirthdayWall,
  getAdminBirthdayWall,
  getWishes,
  sendWish,
  likeWish,
  unlikeWish,
  saveAdminMessage
} from '@/api/birthday'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const userStore = useUserStore()

const currentYear = ref(dayjs().year())
const currentMonth = ref(dayjs().month() + 1)
const employees = ref([])
const loading = ref(false)

const wishModalOpen = ref(false)
const selectedEmployee = ref(null)
const wishes = ref([])
const loadingWishes = ref(false)
const wishContent = ref('')
const sendingWish = ref(false)
const isAdmin = computed(() => userStore.isAdmin?.() || userStore.role === 'admin')
const showAdminMsgModal = ref(false)
const adminMsgYear = ref(new Date().getFullYear())
const adminMsgContent = ref('')
const savingAdminMsg = ref(false)
const adminMsgYearOptions = computed(() => {
  const current = new Date().getFullYear()
  const years = []
  for (let y = current; y >= current - 10; y--) years.push(y)
  return years
})

const AVATAR_COLORS = [
  '#f56a00', '#7265e6', '#ffbf00', '#00a2ae',
  '#eb2f96', '#52c41a', '#1890ff', '#fa8c16'
]

const getAvatarUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  if (url.startsWith('/')) return url
  return '/' + url
}

const getAvatarColor = (name) => {
  if (!name) return AVATAR_COLORS[0]
  let hash = 0
  for (let i = 0; i < name.length; i++) {
    hash = name.charCodeAt(i) + ((hash << 5) - hash)
  }
  return AVATAR_COLORS[Math.abs(hash) % AVATAR_COLORS.length]
}

const formatBirthday = (dateStr) => {
  if (!dateStr) return ''
  const d = dayjs(dateStr)
  if (d.isValid()) return d.format('MM-DD')
  if (typeof dateStr === 'string' && dateStr.length >= 5) {
    return dateStr.substring(5, 10)
  }
  return dateStr
}

const formatWishTime = (dateTime) => {
  if (!dateTime) return ''
  const d = dayjs(dateTime)
  const now = dayjs()
  if (now.diff(d, 'day') < 7) return d.fromNow()
  return d.format('MM-DD HH:mm')
}

const fetchEmployees = async () => {
  loading.value = true
  try {
    const params = { year: currentYear.value, month: currentMonth.value }
    const fetchFn = userStore.isAdmin() ? getAdminBirthdayWall : getBirthdayWall
    const res = await fetchFn(params)
    employees.value = res.data || []
  } catch (error) {
    console.error('获取生日墙数据失败:', error)
    message.error('获取生日墙数据失败')
  } finally {
    loading.value = false
  }
}

const prevMonth = () => {
  if (currentMonth.value === 1) {
    currentMonth.value = 12
    currentYear.value--
  } else {
    currentMonth.value--
  }
  fetchEmployees()
}

const nextMonth = () => {
  if (currentMonth.value === 12) {
    currentMonth.value = 1
    currentYear.value++
  } else {
    currentMonth.value++
  }
  fetchEmployees()
}

const openWishModal = async (emp) => {
  selectedEmployee.value = emp
  wishModalOpen.value = true
  wishContent.value = ''
  await fetchWishes(emp.id)
}

const closeWishModal = () => {
  wishModalOpen.value = false
  selectedEmployee.value = null
  wishes.value = []
  wishContent.value = ''
}

const fetchWishes = async (recipientId) => {
  loadingWishes.value = true
  try {
    const res = await getWishes(recipientId)
    wishes.value = res.data || []
  } catch (error) {
    console.error('获取祝福列表失败:', error)
  } finally {
    loadingWishes.value = false
  }
}

const handleSendWish = async () => {
  if (!wishContent.value.trim() || !selectedEmployee.value) return
  sendingWish.value = true
  try {
    await sendWish({
      recipientId: selectedEmployee.value.id,
      content: wishContent.value.trim()
    })
    message.success('祝福发送成功 🎉')
    wishContent.value = ''
    await fetchWishes(selectedEmployee.value.id)
  } catch (error) {
    console.error('发送祝福失败:', error)
    message.error('发送祝福失败')
  } finally {
    sendingWish.value = false
  }
}

const toggleLike = async (wish) => {
  try {
    if (wish.hasLiked) {
      await unlikeWish(wish.id)
      wish.hasLiked = false
      wish.likeCount = Math.max(0, (wish.likeCount || 1) - 1)
    } else {
      await likeWish(wish.id)
      wish.hasLiked = true
      wish.likeCount = (wish.likeCount || 0) + 1
    }
  } catch (error) {
    console.error('操作失败:', error)
    message.error('操作失败')
  }
}

const handleSaveAdminMsg = async () => {
  if (!selectedEmployee.value) return
  if (!adminMsgContent.value.trim()) { message.warning('请输入寄语内容'); return }
  savingAdminMsg.value = true
  try {
    await saveAdminMessage({
      employeeId: selectedEmployee.value.id,
      year: adminMsgYear.value,
      content: adminMsgContent.value
    })
    message.success('寄语发送成功')
    showAdminMsgModal.value = false
    adminMsgContent.value = ''
  } catch (e) {
    message.error('寄语发送失败')
  } finally {
    savingAdminMsg.value = false
  }
}

onMounted(() => {
  fetchEmployees()
})
</script>

<style scoped>
@keyframes gradientBorder {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-4px); }
}

.birthday-wall {
  padding: 24px;
  min-height: 100%;
}

.wall-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 28px;
  flex-wrap: wrap;
  gap: 16px;
}

.header-left {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.page-title {
  font-size: 26px;
  font-weight: 700;
  margin: 0;
  background: linear-gradient(135deg, #ff6b6b, #ffa502, #ff6348, #ee5a24);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.employee-count {
  font-size: 14px;
  color: #999;
}

.month-nav {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #fff;
  padding: 6px 16px;
  border-radius: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.nav-btn {
  border: none;
  box-shadow: none !important;
  color: #ff6b6b !important;
}

.month-display {
  font-size: 16px;
  font-weight: 600;
  min-width: 110px;
  text-align: center;
  color: #333;
}

.empty-state {
  padding: 80px 0;
  text-align: center;
}

.birthday-card {
  position: relative;
  background: #fff;
  border-radius: 16px;
  padding: 28px 16px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  border: 2px solid transparent;
}

.birthday-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 8px 24px rgba(255, 107, 107, 0.18);
}

.birthday-card:hover .card-hover-overlay {
  opacity: 1;
}

.birthday-card.today-birthday {
  border: none;
  background: linear-gradient(#fff, #fff) padding-box,
    linear-gradient(135deg, #ff6b6b, #ffa502, #a855f7, #ff6b6b) border-box;
  border: 2px solid transparent;
  background-size: 100% 100%, 300% 300%;
  animation: gradientBorder 3s ease infinite;
}

.birthday-card.today-birthday:hover {
  animation: gradientBorder 3s ease infinite, float 2s ease-in-out infinite;
  box-shadow: 0 8px 28px rgba(255, 107, 107, 0.25);
}

.today-badge {
  position: absolute;
  top: 10px;
  right: -28px;
  background: linear-gradient(135deg, #ff6b6b, #ffa502);
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  padding: 2px 32px;
  transform: rotate(45deg);
  box-shadow: 0 2px 6px rgba(255, 107, 107, 0.3);
  z-index: 1;
}

.card-content {
  position: relative;
  z-index: 0;
}

.emp-name {
  font-size: 16px;
  font-weight: 700;
  color: #1f2937;
  margin-top: 12px;
  margin-bottom: 4px;
}

.emp-department {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 6px;
}

.emp-birthday {
  font-size: 13px;
  color: #ffa502;
  font-weight: 500;
  margin-bottom: 4px;
}

.emp-hire-years {
  font-size: 12px;
  color: #a78bfa;
  background: #f5f3ff;
  display: inline-block;
  padding: 2px 10px;
  border-radius: 10px;
}

.card-hover-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  top: 0;
  background: rgba(255, 107, 107, 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
  border-radius: 14px;
  z-index: 2;
}

.wish-btn {
  background: linear-gradient(135deg, #ff6b6b, #ffa502) !important;
  border: none !important;
  font-size: 14px;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(255, 107, 107, 0.35);
}

.wish-btn:hover {
  transform: scale(1.05);
}

.wish-send-area {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.wish-send-area :deep(.ant-input) {
  border-radius: 12px;
  border-color: #ffd6d6;
}

.wish-send-area :deep(.ant-input:focus) {
  border-color: #ff6b6b;
  box-shadow: 0 0 0 2px rgba(255, 107, 107, 0.1);
}

.send-wish-btn {
  background: linear-gradient(135deg, #ff6b6b, #ffa502) !important;
  border: none !important;
  border-radius: 10px !important;
  height: 40px;
  min-width: 100px;
  font-weight: 600;
  white-space: nowrap;
}

.wish-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 15px;
  margin-bottom: 12px;
  color: #333;
}

.wish-count {
  font-size: 13px;
  font-weight: 400;
  color: #999;
}

.wish-empty {
  text-align: center;
  padding: 32px 0;
  color: #bbb;
  font-size: 14px;
}

.wish-list {
  max-height: 400px;
  overflow-y: auto;
  padding-right: 4px;
}

.wish-item {
  display: flex;
  gap: 12px;
  padding: 14px 0;
  border-bottom: 1px solid #f5f5f5;
}

.wish-item:last-child {
  border-bottom: none;
}

.wish-body {
  flex: 1;
  min-width: 0;
}

.wish-sender {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 6px;
}

.sender-name {
  font-weight: 600;
  font-size: 14px;
  color: #333;
}

.wish-time {
  font-size: 12px;
  color: #bbb;
  margin-left: auto;
}

.wish-content {
  font-size: 14px;
  color: #444;
  line-height: 1.6;
  word-break: break-word;
}

.wish-actions {
  margin-top: 8px;
}

.like-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #bbb;
  cursor: pointer;
  transition: all 0.2s;
  user-select: none;
  padding: 2px 6px;
  border-radius: 12px;
}

.like-btn:hover {
  color: #ff6b6b;
  background: #fff5f5;
}

.like-btn.liked {
  color: #ff4d4f;
}

.like-btn.liked :deep(svg) {
  animation: likePopIn 0.3s ease;
}

@keyframes likePopIn {
  0% { transform: scale(1); }
  50% { transform: scale(1.3); }
  100% { transform: scale(1); }
}

:deep(.ant-modal-header) {
  border-radius: 12px 12px 0 0;
}

:deep(.ant-modal-content) {
  border-radius: 12px;
}

:deep(.ant-modal-title) {
  font-weight: 700;
  font-size: 17px;
}

:deep(.ant-empty-description) {
  color: #bbb;
}

:deep(.ant-avatar) {
  flex-shrink: 0;
}
</style>

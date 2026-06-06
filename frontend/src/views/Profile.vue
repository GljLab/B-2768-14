<template>
  <div class="profile-container">
    <div v-if="showAbnormalAlert" class="abnormal-alert">
      <a-alert
        message="安全提醒"
        :description="abnormalAlertMessage"
        type="warning"
        show-icon
        closable
        @close="dismissAbnormalAlert"
      />
    </div>

    <div class="main-content">
      <a-row :gutter="24">
        <a-col :span="16">
          <div class="profile-card">
            <div class="profile-header">
              <div class="avatar-section">
                <a-upload
                  :show-upload-list="false"
                  :before-upload="beforeAvatarUpload"
                  :custom-request="customAvatarUpload"
                  accept="image/jpeg,image/png"
                >
                  <div class="avatar-wrapper">
                    <img v-if="profile.avatar" :src="getAvatarUrl(profile.avatar)" class="avatar-img" />
                    <a-avatar v-else :size="120">
                      {{ profile.name ? profile.name.charAt(0) : 'U' }}
                    </a-avatar>
                    <div class="avatar-overlay">
                      <CameraOutlined style="font-size: 24px; color: white" />
                      <span>更换头像</span>
                    </div>
                  </div>
                </a-upload>
              </div>
              <div class="info-section">
                <h2 class="user-name">{{ profile.name || '-' }}</h2>
                <p class="user-position">{{ profile.position || '-' }}</p>
                <div class="hire-days">
                  <CalendarOutlined />
                  <span>入职第 {{ hireDays }} 天</span>
                </div>
                <a-button type="link" @click="showPasswordModal = true" style="padding: 0; margin-top: 8px;">
                  <KeyOutlined /> 修改密码
                </a-button>
              </div>
            </div>

            <a-divider />

            <div class="profile-form">
              <a-form layout="vertical">
                <a-row :gutter="24">
                  <a-col :span="12">
                    <a-form-item label="姓名">
                      <a-input :value="profile.name" disabled />
                    </a-form-item>
                  </a-col>
                  <a-col :span="12">
                    <a-form-item label="部门">
                      <a-input :value="profile.department" disabled />
                    </a-form-item>
                  </a-col>
                </a-row>
                <a-row :gutter="24">
                  <a-col :span="12">
                    <a-form-item label="职位">
                      <a-input :value="profile.position" disabled />
                    </a-form-item>
                  </a-col>
                  <a-col :span="12">
                    <a-form-item label="入职日期">
                      <a-input :value="profile.hireDate" disabled />
                    </a-form-item>
                  </a-col>
                </a-row>
                <a-row :gutter="24">
                  <a-col :span="12">
                    <a-form-item label="在职状态">
                      <a-tag :color="profile.status === 1 ? 'success' : 'default'">
                        {{ profile.status === 1 ? '在职' : '离职' }}
                      </a-tag>
                    </a-form-item>
                  </a-col>
                  <a-col :span="12"></a-col>
                </a-row>

                <a-divider>可编辑信息</a-divider>

                <a-row :gutter="24">
                  <a-col :span="12">
                    <a-form-item label="手机号码">
                      <a-input
                        v-model:value="editForm.phone"
                        placeholder="请输入手机号码"
                      />
                    </a-form-item>
                  </a-col>
                  <a-col :span="12">
                    <a-form-item label="电子邮箱">
                      <a-input
                        v-model:value="editForm.email"
                        placeholder="请输入电子邮箱"
                      />
                    </a-form-item>
                  </a-col>
                </a-row>

                <div class="form-actions">
                  <a-button type="primary" @click="handleSave" :loading="saving">
                    保存修改
                  </a-button>
                  <a-button @click="handleReset">重置</a-button>
                </div>
              </a-form>
            </div>
          </div>
        </a-col>

        <a-col :span="8">
          <a-card title="在线设备" :bordered="false" class="security-card">
            <template #extra>
              <a-button type="link" size="small" @click="refreshSessions">
                <ReloadOutlined /> 刷新
              </a-button>
            </template>
            <div v-if="sessions.length === 0" class="empty-state">
              暂无在线设备
            </div>
            <div v-else class="session-list">
              <div 
                v-for="session in sessions" 
                :key="session.id" 
                :class="['session-item', { 'current': session.id === currentSessionId }]"
              >
                <div class="session-icon">
                  <DesktopOutlined v-if="session.deviceType === 'PC'" />
                  <MobileOutlined v-else />
                </div>
                <div class="session-info">
                  <div class="session-device">
                    {{ session.deviceType === 'PC' ? '电脑' : '移动设备' }}
                    <a-tag v-if="session.id === currentSessionId" color="green" size="small">当前设备</a-tag>
                  </div>
                  <div class="session-detail">
                    <span>{{ session.ipAddress }}</span>
                    <span class="session-time">{{ formatDateTime(session.loginTime) }}</span>
                  </div>
                </div>
                <a-button 
                  v-if="session.id !== currentSessionId"
                  type="link" 
                  danger 
                  size="small"
                  @click="handleRevokeSession(session.id)"
                >
                  下线
                </a-button>
              </div>
            </div>
          </a-card>

          <a-card title="登录记录" :bordered="false" class="security-card" style="margin-top: 24px">
            <div v-if="loginLogs.length === 0" class="empty-state">
              暂无登录记录
            </div>
            <div v-else class="login-log-list">
              <div v-for="log in loginLogs" :key="log.id" class="log-item">
                <div class="log-status">
                  <CheckCircleOutlined v-if="log.loginResult === 1" style="color: #52c41a" />
                  <CloseCircleOutlined v-else style="color: #ff4d4f" />
                </div>
                <div class="log-info">
                  <div class="log-time">{{ formatDateTime(log.loginTime) }}</div>
                  <div class="log-detail">
                    <span>{{ log.ipAddress }}</span>
                    <a-tag v-if="log.isAbnormal" color="red" size="small">异常</a-tag>
                  </div>
                  <div v-if="log.loginResult === 0 && log.failureReason" class="log-failure">
                    {{ log.failureReason }}
                  </div>
                </div>
              </div>
            </div>
          </a-card>
        </a-col>
      </a-row>
    </div>

    <a-modal
      v-model:open="showPasswordModal"
      title="修改密码"
      :confirm-loading="passwordLoading"
      @ok="handlePasswordSubmit"
      @cancel="handlePasswordCancel"
      width="480px"
      :destroyOnClose="true"
    >
      <a-form
        :model="passwordForm"
        :rules="passwordRules"
        layout="vertical"
        ref="passwordFormRef"
      >
        <a-form-item label="当前密码" name="currentPassword">
          <a-input-password
            v-model:value="passwordForm.currentPassword"
            placeholder="请输入当前密码"
          />
        </a-form-item>
        <a-form-item label="新密码" name="newPassword">
          <a-input-password
            v-model:value="passwordForm.newPassword"
            placeholder="请输入新密码"
            @input="checkPasswordStrength"
          />
          <div class="password-strength">
            <div class="strength-bar">
              <div 
                :class="['strength-segment', { active: passwordStrength.level >= 1 }]"
                :style="{ backgroundColor: passwordStrength.color }"
              ></div>
              <div 
                :class="['strength-segment', { active: passwordStrength.level >= 2 }]"
                :style="{ backgroundColor: passwordStrength.level >= 2 ? passwordStrength.color : '#e8e8e8' }"
              ></div>
              <div 
                :class="['strength-segment', { active: passwordStrength.level >= 3 }]"
                :style="{ backgroundColor: passwordStrength.level >= 3 ? passwordStrength.color : '#e8e8e8' }"
              ></div>
            </div>
            <span class="strength-text" :style="{ color: passwordStrength.color }">
              {{ passwordStrength.text }}
            </span>
          </div>
          <div v-if="passwordStrength.suggestion" class="strength-suggestion">
            {{ passwordStrength.suggestion }}
          </div>
          <div v-if="passwordSameAsCurrent" class="password-warning">
            <WarningOutlined /> 新密码不能与当前密码相同
          </div>
        </a-form-item>
        <a-form-item label="确认新密码" name="confirmPassword">
          <a-input-password
            v-model:value="passwordForm.confirmPassword"
            placeholder="请再次输入新密码"
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
  CameraOutlined,
  CalendarOutlined,
  KeyOutlined,
  DesktopOutlined,
  MobileOutlined,
  ReloadOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  WarningOutlined
} from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import { useUserStore } from '@/stores/user'
import {
  getProfile,
  updateProfile,
  updateAvatar,
  changePassword,
  uploadAvatar
} from '@/api/profile'
import {
  getEmployeeLoginLogs,
  getEmployeeSessions,
  revokeSession,
  getLatestAbnormalLogin
} from '@/api/security'

const router = useRouter()
const userStore = useUserStore()

const profile = ref({})
const editForm = reactive({
  phone: '',
  email: ''
})
const saving = ref(false)
const showPasswordModal = ref(false)
const passwordLoading = ref(false)
const passwordFormRef = ref()
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const loginLogs = ref([])
const sessions = ref([])
const currentSessionId = ref(null)

const showAbnormalAlert = ref(false)
const abnormalAlertMessage = ref('')

const passwordStrength = reactive({
  level: 0,
  text: '请输入密码',
  color: '#e8e8e8',
  suggestion: ''
})
const passwordSameAsCurrent = ref(false)

const hireDays = computed(() => {
  if (!profile.value.hireDate) return 0
  const hireDate = dayjs(profile.value.hireDate)
  const today = dayjs()
  return today.diff(hireDate, 'day') + 1
})

const checkPasswordStrength = () => {
  const password = passwordForm.newPassword
  if (!password) {
    passwordStrength.level = 0
    passwordStrength.text = '请输入密码'
    passwordStrength.color = '#e8e8e8'
    passwordStrength.suggestion = ''
    passwordSameAsCurrent.value = false
    return
  }

  if (passwordForm.currentPassword && password === passwordForm.currentPassword) {
    passwordSameAsCurrent.value = true
  } else {
    passwordSameAsCurrent.value = false
  }

  let score = 0
  const hasNumber = /\d/.test(password)
  const hasLetter = /[a-zA-Z]/.test(password)
  const hasSpecial = /[!@#$%^&*(),.?":{}|<>]/.test(password)
  const isLongEnough = password.length >= 8

  if (hasNumber) score++
  if (hasLetter) score++
  if (hasSpecial) score++
  if (isLongEnough) score++

  if (score <= 1) {
    passwordStrength.level = 1
    passwordStrength.text = '弱'
    passwordStrength.color = '#ff4d4f'
    passwordStrength.suggestion = '建议添加字母、数字和特殊字符'
  } else if (score === 2 || score === 3) {
    passwordStrength.level = 2
    passwordStrength.text = '中'
    passwordStrength.color = '#fa8c16'
    passwordStrength.suggestion = '建议添加特殊字符以增强安全性'
  } else {
    passwordStrength.level = 3
    passwordStrength.text = '强'
    passwordStrength.color = '#52c41a'
    passwordStrength.suggestion = '密码强度很好'
  }
}

const validatePassword = (rule, value) => {
  if (!value) {
    return Promise.reject('请输入新密码')
  }
  if (value.length < 8) {
    return Promise.reject('密码长度至少8位')
  }
  if (passwordSameAsCurrent.value) {
    return Promise.reject('新密码不能与当前密码相同')
  }
  return Promise.resolve()
}

const validateConfirmPassword = (rule, value) => {
  if (!value) {
    return Promise.reject('请确认新密码')
  }
  if (value !== passwordForm.newPassword) {
    return Promise.reject('两次输入的密码不一致')
  }
  return Promise.resolve()
}

const passwordRules = {
  currentPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const fetchProfile = async () => {
  try {
    const res = await getProfile()
    profile.value = res.data
    editForm.phone = res.data.phone
    editForm.email = res.data.email
  } catch (error) {
    console.error('获取个人资料失败:', error)
  }
}

const fetchLoginLogs = async () => {
  try {
    const res = await getEmployeeLoginLogs()
    loginLogs.value = res.data
  } catch (error) {
    console.error('获取登录记录失败:', error)
  }
}

const fetchSessions = async () => {
  try {
    const res = await getEmployeeSessions()
    sessions.value = res.data.sessions
    currentSessionId.value = res.data.currentSessionId
  } catch (error) {
    console.error('获取在线设备失败:', error)
  }
}

const checkAbnormalLogin = async () => {
  try {
    const res = await getLatestAbnormalLogin()
    if (res.data) {
      const log = res.data
      abnormalAlertMessage.value = `检测到您的账号在${formatDateTime(log.loginTime)}从IP ${log.ipAddress}登录（${log.abnormalType}），如非本人操作请及时修改密码`
      showAbnormalAlert.value = true
    }
  } catch (error) {
    console.error('检查异常登录失败:', error)
  }
}

const dismissAbnormalAlert = () => {
  showAbnormalAlert.value = false
}

const refreshSessions = () => {
  fetchSessions()
  message.success('已刷新')
}

const handleRevokeSession = async (sessionId) => {
  Modal.confirm({
    title: '确认下线',
    content: '确定要下线此设备吗？该设备需要重新登录才能访问系统。',
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      try {
        await revokeSession(sessionId)
        message.success('设备已下线')
        fetchSessions()
      } catch (error) {
        console.error('下线设备失败:', error)
      }
    }
  })
}

const getAvatarUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  if (url.startsWith('/')) {
    return url
  }
  return '/' + url
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss')
}

const beforeAvatarUpload = (file) => {
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png'
  if (!isJpgOrPng) {
    message.error('仅支持JPG或PNG格式的图片')
    return false
  }
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    message.error('图片大小不能超过2MB')
    return false
  }
  return true
}

const customAvatarUpload = async (options) => {
  const { file, onSuccess, onError } = options
  try {
    const uploadRes = await uploadAvatar(file)
    const avatarUrl = uploadRes.data
    
    await updateAvatar(avatarUrl)
    
    profile.value.avatar = avatarUrl
    userStore.updateAvatar(avatarUrl)
    message.success('头像更新成功')
    onSuccess && onSuccess(uploadRes.data)
  } catch (error) {
    console.error('头像上传失败:', error)
    message.error('头像上传失败')
    onError && onError(error)
  }
}

const handleSave = async () => {
  saving.value = true
  try {
    const res = await updateProfile({
      phone: editForm.phone,
      email: editForm.email
    })
    message.success(res.data || '保存成功')
    profile.value.phone = editForm.phone
    profile.value.email = editForm.email
  } catch (error) {
    console.error('保存失败:', error)
  } finally {
    saving.value = false
  }
}

const handleReset = () => {
  editForm.phone = profile.value.phone
  editForm.email = profile.value.email
}

const handlePasswordCancel = () => {
  showPasswordModal.value = false
  passwordForm.currentPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
}

const handlePasswordSubmit = async () => {
  try {
    await passwordFormRef.value.validate()
    passwordLoading.value = true
    
    await changePassword(passwordForm)
    message.success('密码修改成功，请重新登录')
    
    showPasswordModal.value = false
    userStore.logout()
    router.push('/login')
  } catch (error) {
    console.error('密码修改失败:', error)
  } finally {
    passwordLoading.value = false
  }
}

onMounted(() => {
  fetchProfile()
  fetchLoginLogs()
  fetchSessions()
  checkAbnormalLogin()
})
</script>

<style scoped>
.abnormal-alert {
  padding: 16px 24px 0;
}

.main-content {
  padding: 0;
}

.profile-card {
  background: white;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.security-card {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 32px;
  padding-bottom: 16px;
}

.avatar-section {
  position: relative;
}

.avatar-wrapper {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
  color: white;
  font-size: 12px;
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.info-section {
  flex: 1;
}

.user-name {
  font-size: 28px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.user-position {
  font-size: 16px;
  color: #6b7280;
  margin: 0 0 16px 0;
}

.hire-days {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #667eea;
  font-size: 14px;
}

.profile-form {
  margin-top: 24px;
}

.form-actions {
  display: flex;
  gap: 16px;
  margin-top: 24px;
}

.empty-state {
  text-align: center;
  padding: 40px 0;
  color: #999;
}

.session-list {
  max-height: 300px;
  overflow-y: auto;
}

.session-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 8px;
  background: #fafafa;
  transition: all 0.3s;
}

.session-item:hover {
  background: #f0f0f0;
}

.session-item.current {
  background: #f0f7ff;
  border: 1px solid #91caff;
}

.session-icon {
  font-size: 24px;
  color: #667eea;
  margin-right: 12px;
}

.session-info {
  flex: 1;
}

.session-device {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
  margin-bottom: 4px;
}

.session-detail {
  font-size: 12px;
  color: #6b7280;
}

.session-time {
  margin-left: 8px;
}

.login-log-list {
  max-height: 400px;
  overflow-y: auto;
}

.log-item {
  display: flex;
  align-items: flex-start;
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.log-item:last-child {
  border-bottom: none;
}

.log-status {
  font-size: 18px;
  margin-right: 12px;
  margin-top: 2px;
}

.log-info {
  flex: 1;
}

.log-time {
  font-size: 14px;
  color: #1f2937;
  margin-bottom: 4px;
}

.log-detail {
  font-size: 12px;
  color: #6b7280;
  display: flex;
  align-items: center;
  gap: 8px;
}

.log-failure {
  font-size: 12px;
  color: #ff4d4f;
  margin-top: 4px;
}

.password-strength {
  display: flex;
  align-items: center;
  margin-top: 8px;
  gap: 12px;
}

.strength-bar {
  display: flex;
  gap: 4px;
}

.strength-segment {
  width: 60px;
  height: 6px;
  border-radius: 3px;
  background: #e8e8e8;
  transition: all 0.3s;
}

.strength-segment.active {
  background: #52c41a;
}

.strength-text {
  font-size: 12px;
  font-weight: 500;
}

.strength-suggestion {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.password-warning {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #ff4d4f;
  margin-top: 4px;
}

:deep(.ant-form-item) {
  margin-bottom: 16px;
}

:deep(.ant-input[disabled]) {
  background: #f5f5f5;
  color: #999;
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

:deep(.ant-divider-inner-text) {
  color: #999;
  font-size: 14px;
}
</style>

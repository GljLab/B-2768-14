<template>
  <div class="first-login-container">
    <div class="password-change-box">
      <div class="header">
        <div class="welcome-icon">👋</div>
        <h2>欢迎首次登录</h2>
        <p>为了您的账号安全，请修改初始密码</p>
      </div>

      <a-form
        :model="formState"
        :rules="rules"
        @finish="handleSubmit"
        layout="vertical"
        class="password-form"
      >
        <a-form-item label="当前密码" name="currentPassword">
          <a-input-password
            v-model:value="formState.currentPassword"
            size="large"
            placeholder="请输入当前密码"
          />
        </a-form-item>

        <a-form-item label="新密码" name="newPassword">
          <a-input-password
            v-model:value="formState.newPassword"
            size="large"
            placeholder="请输入新密码（至少8位，包含字母和数字）"
          />
        </a-form-item>

        <a-form-item label="确认新密码" name="confirmPassword">
          <a-input-password
            v-model:value="formState.confirmPassword"
            size="large"
            placeholder="请再次输入新密码"
          />
        </a-form-item>

        <a-form-item>
          <a-button
            type="primary"
            html-type="submit"
            size="large"
            :loading="loading"
            block
          >
            确认修改
          </a-button>
        </a-form-item>
      </a-form>

      <div class="tips">
        <a-alert
          message="密码要求：长度至少8位，必须包含字母和数字的组合"
          type="warning"
          show-icon
          :closable="false"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { firstLoginChangePassword } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)

const formState = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validatePassword = (rule, value) => {
  if (!value) {
    return Promise.reject('请输入新密码')
  }
  if (value.length < 8) {
    return Promise.reject('密码长度至少8位')
  }
  if (!/[a-zA-Z]/.test(value) || !/\d/.test(value)) {
    return Promise.reject('密码必须包含字母和数字')
  }
  return Promise.resolve()
}

const validateConfirmPassword = (rule, value) => {
  if (!value) {
    return Promise.reject('请确认新密码')
  }
  if (value !== formState.newPassword) {
    return Promise.reject('两次输入的密码不一致')
  }
  return Promise.resolve()
}

const rules = {
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

const handleSubmit = async () => {
  loading.value = true
  try {
    await firstLoginChangePassword(formState)
    message.success('密码修改成功，请重新登录')
    userStore.logout()
    router.push('/login')
  } catch (error) {
    console.error('密码修改失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.first-login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.password-change-box {
  width: 100%;
  max-width: 480px;
  padding: 48px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.header {
  text-align: center;
  margin-bottom: 32px;
}

.welcome-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.header h2 {
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.header p {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

.password-form {
  margin-bottom: 24px;
}

.tips {
  margin-top: 16px;
}

:deep(.ant-input-affix-wrapper) {
  border-radius: 8px;
}

:deep(.ant-btn-primary) {
  height: 44px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: all 0.3s;
}

:deep(.ant-btn-primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(102, 126, 234, 0.4);
}
</style>

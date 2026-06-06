<template>
  <div class="login-container">
    <!-- 左侧品牌区域 -->
    <div class="login-left">
      <div class="brand-content">
        <h1 class="brand-title">员工管理系统</h1>
        <p class="brand-slogan">高效 · 便捷 · 专业</p>
        <div class="brand-features">
          <div class="feature-item">
            <span class="feature-icon">✓</span>
            <span>智能化员工信息管理</span>
          </div>
          <div class="feature-item">
            <span class="feature-icon">✓</span>
            <span>快速查询与数据统计</span>
          </div>
          <div class="feature-item">
            <span class="feature-icon">✓</span>
            <span>安全可靠的数据存储</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧登录表单 -->
    <div class="login-right">
      <div class="login-box">
        <h2 class="login-title">欢迎登录</h2>
        <p class="login-subtitle">请选择登录方式</p>
        
        <!-- 角色选择 -->
        <div class="role-selector">
          <div 
            :class="['role-option', { active: loginRole === 'admin' }]"
            @click="switchRole('admin')"
          >
            <div class="role-icon">👨‍💼</div>
            <span>管理员登录</span>
          </div>
          <div 
            :class="['role-option', { active: loginRole === 'employee' }]"
            @click="switchRole('employee')"
          >
            <div class="role-icon">👤</div>
            <span>员工登录</span>
          </div>
        </div>
        
        <a-form
          :model="formState"
          :rules="rules"
          @finish="handleLogin"
          layout="vertical"
          class="login-form"
        >
          <a-form-item v-if="loginRole === 'admin'" label="用户名" name="username">
            <a-input
              v-model:value="formState.username"
              size="large"
              placeholder="请输入用户名"
              :prefix="h(UserOutlined)"
            />
          </a-form-item>
          
          <a-form-item v-if="loginRole === 'employee'" label="邮箱" name="email">
            <a-input
              v-model:value="formState.email"
              size="large"
              placeholder="请输入邮箱"
              :prefix="h(MailOutlined)"
            />
          </a-form-item>

          <a-form-item label="密码" name="password">
            <a-input-password
              v-model:value="formState.password"
              size="large"
              placeholder="请输入密码"
              :prefix="h(LockOutlined)"
            />
          </a-form-item>

          <div v-if="loginError" class="login-error">
            <a-alert
              :message="loginError.message"
              type="error"
              show-icon
              :closable="false"
            />
            <div v-if="loginError.remainingAttempts !== undefined && loginError.remainingAttempts > 0" class="error-extra">
              还可尝试 {{ loginError.remainingAttempts }} 次
            </div>
            <div v-if="loginError.locked" class="error-extra locked">
              账号已被锁定，解锁时间：{{ formatUnlockTime(loginError.unlockTime) }}
            </div>
          </div>

          <a-form-item>
            <a-button
              type="primary"
              html-type="submit"
              size="large"
              :loading="loading"
              block
            >
              登录
            </a-button>
          </a-form-item>
        </a-form>

        <div class="login-tips" v-if="loginRole === 'admin'">
          <a-alert
            message="管理员默认账号: admin / 123456"
            type="info"
            show-icon
            :closable="false"
          />
        </div>
        <div class="login-tips" v-else>
          <a-alert
            message="员工初始密码为手机号后6位"
            type="info"
            show-icon
            :closable="false"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { h, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { UserOutlined, LockOutlined, MailOutlined } from '@ant-design/icons-vue'
import { login, employeeLogin } from '@/api/auth'
import { useUserStore } from '@/stores/user'
import dayjs from 'dayjs'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const loginRole = ref('admin')
const loginError = ref(null)

const formState = reactive({
  username: '',
  email: '',
  password: ''
})

const formatUnlockTime = (time) => {
  if (!time) return '-'
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const switchRole = (role) => {
  loginRole.value = role
  formState.username = ''
  formState.email = ''
  formState.password = ''
  loginError.value = null
}

const handleLogin = async () => {
  loading.value = true
  loginError.value = null
  try {
    let res
    if (loginRole.value === 'admin') {
      res = await login({
        username: formState.username,
        password: formState.password
      })
    } else {
      res = await employeeLogin({
        email: formState.email,
        password: formState.password
      })
    }
    
    const result = res.data
    if (!result.success) {
      loginError.value = {
        message: result.message || '登录失败',
        remainingAttempts: result.remainingAttempts,
        locked: result.locked,
        unlockTime: result.unlockTime
      }
      return
    }
    
    message.success('登录成功')
    
    const loginResponse = result.loginResponse
    userStore.login(
      loginResponse.token,
      loginResponse.username,
      loginResponse.role,
      loginResponse.employeeId,
      loginResponse.name,
      loginResponse.avatar,
      loginResponse.isActive
    )
    
    if (result.abnormalLogin) {
      message.warning(result.abnormalMessage)
    }
    
    if (loginResponse.role === 'admin') {
      router.push('/employee')
    } else {
      if (loginResponse.isActive === 0) {
        router.push('/first-login')
      } else {
        router.push('/profile')
      }
    }
  } catch (error) {
    console.error('登录失败:', error)
    if (error.response && error.response.data) {
      loginError.value = {
        message: error.response.data.message || '登录失败'
      }
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  min-height: 100vh;
  background: #f0f2f5;
}

.login-left {
  flex: 1;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
  position: relative;
  overflow: hidden;
}

.login-left::before {
  content: '';
  position: absolute;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 1px, transparent 1px);
  background-size: 50px 50px;
  animation: drift 20s linear infinite;
}

@keyframes drift {
  from {
    transform: translate(0, 0);
  }
  to {
    transform: translate(50px, 50px);
  }
}

.brand-content {
  position: relative;
  z-index: 1;
  color: white;
  max-width: 500px;
}

.brand-title {
  font-size: 48px;
  font-weight: 700;
  margin-bottom: 16px;
  color: white;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}

.brand-slogan {
  font-size: 20px;
  margin-bottom: 48px;
  opacity: 0.95;
  letter-spacing: 2px;
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.feature-item {
  display: flex;
  align-items: center;
  font-size: 16px;
  opacity: 0.9;
}

.feature-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  margin-right: 12px;
  font-weight: bold;
}

.login-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
}

.login-box {
  width: 100%;
  max-width: 420px;
  padding: 48px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.login-title {
  font-size: 28px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8px;
  text-align: center;
}

.login-subtitle {
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 32px;
  text-align: center;
}

.login-form {
  margin-bottom: 24px;
}

.login-tips {
  margin-top: 16px;
}

.login-error {
  margin-bottom: 16px;
}

.error-extra {
  margin-top: 8px;
  font-size: 13px;
  color: #ff4d4f;
}

.error-extra.locked {
  color: #fa8c16;
}

.role-selector {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
}

.role-option {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.role-option:hover {
  border-color: #667eea;
  background: #f8f9ff;
}

.role-option.active {
  border-color: #667eea;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
}

.role-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.role-option span {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
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

@media (max-width: 768px) {
  .login-container {
    flex-direction: column;
  }
  
  .login-left {
    min-height: 300px;
    padding: 40px 20px;
  }
  
  .brand-title {
    font-size: 32px;
  }
  
  .login-right {
    padding: 40px 20px;
  }
  
  .login-box {
    padding: 32px 24px;
  }
}
</style>

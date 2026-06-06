<template>
  <div class="employee-feedback-container">
    <div class="page-header">
      <h2 class="page-title">意见箱</h2>
      <p class="page-desc">您的意见对我们非常重要，所有反馈均为匿名提交</p>
    </div>

    <div class="main-content">
      <a-card :bordered="false" class="content-card">
        <a-form
          :model="formData"
          layout="vertical"
          style="max-width: 600px; margin: 0 auto;"
        >
          <a-form-item label="意见分类" required>
            <a-select v-model:value="formData.category" placeholder="请选择意见分类" style="width: 100%">
              <a-select-option value="制度建议">制度建议</a-select-option>
              <a-select-option value="薪酬福利">薪酬福利</a-select-option>
              <a-select-option value="工作环境">工作环境</a-select-option>
              <a-select-option value="团队管理">团队管理</a-select-option>
              <a-select-option value="其他">其他</a-select-option>
            </a-select>
          </a-form-item>
          
          <a-form-item label="意见内容" required>
            <a-textarea
              v-model:value="formData.content"
              placeholder="请详细描述您的意见或建议..."
              :rows="8"
              maxlength="2000"
              show-count
            />
          </a-form-item>
          
          <div class="submit-area">
            <a-button type="primary" size="large" @click="handleSubmit" :loading="submitting">
              <InboxOutlined /> 匿名提交
            </a-button>
          </div>
        </a-form>
      </a-card>

      <div class="tips-card">
        <a-card :bordered="false">
          <div class="tips-content">
            <div class="tip-icon">
              <BulbOutlined />
            </div>
            <div class="tip-text">
              <h4>温馨提示</h4>
              <ul>
                <li>所有意见均为匿名提交，您的身份信息不会被记录</li>
                <li>请尽量详细描述您的意见，以便我们更好地改进</li>
                <li>提交后系统会自动生成唯一编号，方便您追踪</li>
                <li>您的每一条建议对我们都很重要，感谢您的参与</li>
              </ul>
            </div>
          </div>
        </a-card>
      </div>
    </div>

    <a-modal
      v-model:open="showSuccessModal"
      title="提交成功"
      :footer="null"
      :closable="false"
    >
      <div class="success-content">
        <div class="success-icon">
          <CheckCircleOutlined />
        </div>
        <h3>感谢您的反馈</h3>
        <p>您的意见已成功提交</p>
        <p class="feedback-no">意见编号：<span>{{ feedbackNo }}</span></p>
        <a-button type="primary" @click="resetForm">
          继续提交
        </a-button>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { message } from 'ant-design-vue'
import {
  InboxOutlined,
  BulbOutlined,
  CheckCircleOutlined
} from '@ant-design/icons-vue'
import { submitFeedback } from '@/api/feedback'

const submitting = ref(false)
const showSuccessModal = ref(false)
const feedbackNo = ref('')

const formData = reactive({
  category: '',
  content: ''
})

const validateForm = () => {
  if (!formData.category) {
    message.error('请选择意见分类')
    return false
  }
  if (!formData.content.trim()) {
    message.error('请填写意见内容')
    return false
  }
  if (formData.content.trim().length < 10) {
    message.error('意见内容至少需要10个字符')
    return false
  }
  return true
}

const handleSubmit = async () => {
  if (!validateForm()) return
  
  submitting.value = true
  try {
    const res = await submitFeedback(formData)
    
    if (res.code === 200) {
      feedbackNo.value = res.data.feedbackNo
      showSuccessModal.value = true
    } else {
      message.error(res.message || '提交失败')
    }
  } catch (e) {
    message.error('提交失败')
  } finally {
    submitting.value = false
  }
}

const resetForm = () => {
  formData.category = ''
  formData.content = ''
  showSuccessModal.value = false
}
</script>

<style scoped>
.employee-feedback-container {
  padding: 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 32px;
}

.page-title {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.page-desc {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.content-card {
  border-radius: 8px;
  margin-bottom: 24px;
}

.submit-area {
  text-align: center;
  padding-top: 16px;
}

.tips-card {
  max-width: 600px;
  margin: 0 auto;
}

.tips-content {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.tip-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #ffd89b 0%, #19547b 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
  flex-shrink: 0;
}

.tip-text h4 {
  margin: 0 0 12px 0;
  font-size: 16px;
  color: #333;
}

.tip-text ul {
  margin: 0;
  padding-left: 20px;
  color: #666;
  line-height: 2;
}

.success-content {
  text-align: center;
  padding: 20px;
}

.success-icon {
  font-size: 64px;
  color: #52c41a;
  margin-bottom: 16px;
}

.success-content h3 {
  margin: 0 0 8px 0;
  font-size: 20px;
  color: #333;
}

.success-content p {
  margin: 0 0 12px 0;
  color: #666;
}

.feedback-no {
  font-size: 16px;
  color: #666;
}

.feedback-no span {
  font-family: monospace;
  font-weight: 600;
  color: #1890ff;
}
</style>

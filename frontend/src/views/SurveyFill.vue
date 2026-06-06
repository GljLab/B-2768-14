<template>
  <div class="survey-fill-container">
    <div class="page-header">
      <a-button @click="goBack">
        <ArrowLeftOutlined /> 返回
      </a-button>
      <h2 class="page-title">填写问卷</h2>
    </div>

    <div class="main-content" v-loading="loading">
      <a-card :bordered="false" class="content-card" v-if="surveyDetail">
        <div class="survey-info">
          <h3 class="survey-title">{{ surveyDetail.title }}</h3>
          <p class="survey-desc">{{ surveyDetail.description }}</p>
          <div class="survey-meta">
            <span v-if="surveyDetail.isAnonymous === 1" class="anonymous-tag">匿名问卷</span>
            <span>截止日期：{{ formatDate(surveyDetail.deadline) }}</span>
          </div>
        </div>

        <div class="questions-area">
          <div
            v-for="(question, index) in surveyDetail.questions"
            :key="question.id"
            class="question-item"
          >
            <div class="question-header">
              <span class="question-no">Q{{ index + 1 }}</span>
              <span class="question-title">{{ question.title }}</span>
              <span v-if="question.required === 1" class="required">*必填</span>
            </div>

            <div v-if="question.questionType === 1" class="rating-question">
              <div
                v-for="option in question.options"
                :key="option.id"
                class="rating-item"
              >
                <span class="rating-label">{{ option.optionText }}</span>
                <a-rate
                  :value="getRatingValue(question.id, option.optionText)"
                  :allow-half="false"
                  @update:value="(val) => setRatingValue(question.id, option.optionText, val)"
                />
                <span class="rating-value">{{ getRatingValue(question.id, option.optionText) || 0 }}分</span>
              </div>
            </div>

            <div v-if="question.questionType === 2" class="single-question">
              <a-radio-group v-model:value="answersMap[question.id]">
                <a-space direction="vertical">
                  <a-radio
                    v-for="option in question.options"
                    :key="option.id"
                    :value="option.id"
                  >
                    {{ option.optionText }}
                  </a-radio>
                </a-space>
              </a-radio-group>
            </div>

            <div v-if="question.questionType === 3" class="text-question">
              <a-textarea
                v-model:value="answersMap[question.id]"
                placeholder="请输入您的意见或建议"
                :rows="4"
                maxlength="1000"
                show-count
              />
            </div>
          </div>
        </div>

        <div class="submit-area">
          <a-button type="primary" size="large" @click="handleSubmit" :loading="submitting">
            提交问卷
          </a-button>
        </div>
      </a-card>
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
        <h3>感谢您的参与</h3>
        <p>您的问卷已成功提交</p>
        <a-button type="primary" @click="goBack">
          返回问卷列表
        </a-button>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import {
  ArrowLeftOutlined,
  CheckCircleOutlined
} from '@ant-design/icons-vue'
import { getEmployeeSurveyDetail, submitSurvey } from '@/api/survey'
import dayjs from 'dayjs'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const submitting = ref(false)
const surveyDetail = ref(null)
const answersMap = reactive({})
const showSuccessModal = ref(false)

const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const getRatingKey = (questionId, optionText) => {
  return `${questionId}_${optionText}`
}

const getRatingValue = (questionId, optionText) => {
  return answersMap[getRatingKey(questionId, optionText)] || 0
}

const setRatingValue = (questionId, optionText, value) => {
  answersMap[getRatingKey(questionId, optionText)] = value
}

const loadSurveyDetail = async () => {
  loading.value = true
  try {
    const res = await getEmployeeSurveyDetail(route.params.id)
    if (res.code === 200) {
      surveyDetail.value = res.data
    } else {
      message.error(res.message || '加载问卷失败')
    }
  } catch (e) {
    message.error('加载问卷失败')
  } finally {
    loading.value = false
  }
}

const validateAnswers = () => {
  if (!surveyDetail.value || !surveyDetail.value.questions) return false
  
  for (const question of surveyDetail.value.questions) {
    if (question.required === 1) {
      if (question.questionType === 1) {
        for (const option of question.options) {
          const key = getRatingKey(question.id, option.optionText)
          if (!answersMap[key] || answersMap[key] === 0) {
            message.error(`请为"${option.optionText}"评分`)
            return false
          }
        }
      } else if (question.questionType === 2) {
        if (!answersMap[question.id]) {
          message.error(`请选择"${question.title}"的答案`)
          return false
        }
      } else if (question.questionType === 3) {
        if (!answersMap[question.id] || !answersMap[question.id].trim()) {
          message.error(`请填写"${question.title}"的内容`)
          return false
        }
      }
    }
  }
  return true
}

const buildSubmitData = () => {
  const answers = []
  
  for (const question of surveyDetail.value.questions) {
    if (question.questionType === 1) {
      for (const option of question.options) {
        const key = getRatingKey(question.id, option.optionText)
        if (answersMap[key]) {
          answers.push({
            questionId: question.id,
            questionType: 1,
            ratingItem: option.optionText,
            ratingScore: answersMap[key]
          })
        }
      }
    } else if (question.questionType === 2) {
      if (answersMap[question.id]) {
        answers.push({
          questionId: question.id,
          questionType: 2,
          optionId: answersMap[question.id]
        })
      }
    } else if (question.questionType === 3) {
      if (answersMap[question.id]) {
        answers.push({
          questionId: question.id,
          questionType: 3,
          textContent: answersMap[question.id]
        })
      }
    }
  }
  
  return {
    surveyId: route.params.id,
    answers
  }
}

const handleSubmit = () => {
  Modal.confirm({
    title: '确认提交',
    content: '提交后无法修改，确认提交吗？',
    okText: '确认提交',
    cancelText: '取消',
    onOk: async () => {
      if (!validateAnswers()) return
      
      submitting.value = true
      try {
        const submitData = buildSubmitData()
        const res = await submitSurvey(submitData)
        
        if (res.code === 200) {
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
  })
}

const goBack = () => {
  router.push('/my-surveys')
}

onMounted(() => {
  loadSurveyDetail()
})
</script>

<style scoped>
.survey-fill-container {
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
}

.content-card {
  border-radius: 8px;
  max-width: 900px;
  margin: 0 auto;
}

.survey-info {
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 24px;
}

.survey-title {
  margin: 0 0 12px 0;
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.survey-desc {
  margin: 0 0 12px 0;
  color: #666;
  line-height: 1.6;
}

.survey-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  color: #888;
  font-size: 14px;
}

.anonymous-tag {
  padding: 2px 8px;
  background: #f3e8ff;
  color: #7c3aed;
  border-radius: 4px;
}

.questions-area {
  margin-bottom: 32px;
}

.question-item {
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 1px dashed #e8e8e8;
}

.question-item:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.question-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.question-no {
  padding: 4px 10px;
  background: #1890ff;
  color: white;
  border-radius: 4px;
  font-weight: 600;
  font-size: 14px;
}

.question-title {
  flex: 1;
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.required {
  color: #ff4d4f;
  font-size: 14px;
}

.rating-question {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding-left: 40px;
}

.rating-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.rating-label {
  width: 120px;
  font-weight: 500;
}

.rating-value {
  color: #fa8c16;
  font-weight: 600;
}

.single-question {
  padding-left: 40px;
}

.text-question {
  padding-left: 40px;
}

.submit-area {
  display: flex;
  justify-content: center;
  padding-top: 24px;
  border-top: 1px solid #f0f0f0;
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
  margin: 0 0 24px 0;
  color: #666;
}
</style>

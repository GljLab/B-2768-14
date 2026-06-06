<template>
  <div class="my-surveys-container">
    <div class="page-header">
      <h2 class="page-title">我的问卷</h2>
    </div>

    <div class="main-content">
      <a-card :bordered="false" class="content-card">
        <a-tabs v-model:activeKey="activeTab">
          <a-tab-pane key="pending" tab="待完成">
            <div v-if="pendingSurveys.length > 0" class="survey-list">
              <div
                v-for="survey in pendingSurveys"
                :key="survey.id"
                class="survey-card"
              >
                <div class="survey-header">
                  <h3 class="survey-title">{{ survey.title }}</h3>
                  <a-tag v-if="survey.isAnonymous === 1" color="purple">匿名</a-tag>
                </div>
                <p class="survey-desc">{{ survey.description || '暂无说明' }}</p>
                <div class="survey-footer">
                  <div class="deadline-info">
                    <ClockCircleOutlined />
                    <span>截止：{{ formatDate(survey.deadline) }}</span>
                    <span class="countdown" :class="{ urgent: getDaysRemaining(survey.deadline) <= 3 }">
                      剩余 {{ getDaysRemaining(survey.deadline) }} 天
                    </span>
                  </div>
                  <a-button type="primary" @click="goToFill(survey.id)">
                    立即填写
                  </a-button>
                </div>
              </div>
            </div>
            <a-empty v-else description="暂无待完成的问卷" />
          </a-tab-pane>
          
          <a-tab-pane key="completed" tab="已完成">
            <div v-if="completedSurveys.length > 0" class="survey-list">
              <div
                v-for="survey in completedSurveys"
                :key="survey.id"
                class="survey-card completed"
              >
                <div class="survey-header">
                  <h3 class="survey-title">{{ survey.title }}</h3>
                  <a-tag color="green">已提交</a-tag>
                </div>
                <p class="survey-desc">{{ survey.description || '暂无说明' }}</p>
                <div class="survey-footer">
                  <div class="submit-info">
                    <CheckCircleOutlined />
                    <span>提交时间：{{ formatDate(survey.submitTime || survey.updatedAt) }}</span>
                  </div>
                  <a-button @click="viewMyAnswer(survey.id)">
                    查看我的答案
                  </a-button>
                </div>
              </div>
            </div>
            <a-empty v-else description="暂无已完成的问卷" />
          </a-tab-pane>
        </a-tabs>
      </a-card>
    </div>

    <a-modal
      v-model:open="showAnswerModal"
      title="查看我的答案"
      :width="800"
      :footer="null"
    >
      <div v-if="myAnswer && myAnswer.answerItems" class="answer-detail">
        <div
          v-for="(item, index) in myAnswer.answerItems"
          :key="item.id"
          class="answer-item"
        >
          <div class="answer-question">
            <span class="question-no">Q{{ index + 1 }}</span>
            <span class="question-title">{{ getQuestionTitle(item.questionId) }}</span>
          </div>
          <div class="answer-content">
            <template v-if="item.questionType === 1">
              <span class="rating-label">{{ item.ratingItem }}：</span>
              <a-rate v-model:value="item.ratingScore" disabled />
              <span class="score-text">{{ item.ratingScore }}分</span>
            </template>
            <template v-else-if="item.questionType === 2">
              <span>{{ getOptionText(item.questionId, item.optionId) || '已选择' }}</span>
            </template>
            <template v-else>
              <p class="text-answer">{{ item.textContent }}</p>
            </template>
          </div>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  ClockCircleOutlined,
  CheckCircleOutlined
} from '@ant-design/icons-vue'
import { getPendingSurveys, getCompletedSurveys, getMyAnswer, getEmployeeSurveyDetail } from '@/api/survey'
import dayjs from 'dayjs'

const router = useRouter()

const activeTab = ref('pending')
const pendingSurveys = ref([])
const completedSurveys = ref([])
const showAnswerModal = ref(false)
const myAnswer = ref(null)
const currentSurveyDetail = ref(null)

const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const getDaysRemaining = (deadline) => {
  return dayjs(deadline).diff(dayjs(), 'day') + 1
}

const goToFill = (id) => {
  router.push(`/survey/fill/${id}`)
}

const viewMyAnswer = async (id) => {
  try {
    const [answerRes, detailRes] = await Promise.all([
      getMyAnswer(id),
      getEmployeeSurveyDetail(id)
    ])
    
    if (answerRes.code === 200) {
      myAnswer.value = answerRes.data
    }
    if (detailRes.code === 200) {
      currentSurveyDetail.value = detailRes.data
    }
    
    showAnswerModal.value = true
  } catch (e) {
    message.error('获取答案失败')
  }
}

const getQuestionTitle = (questionId) => {
  if (!currentSurveyDetail.value || !currentSurveyDetail.value.questions) return ''
  const q = currentSurveyDetail.value.questions.find(q => q.id === questionId)
  return q ? q.title : ''
}

const getOptionText = (questionId, optionId) => {
  if (!currentSurveyDetail.value || !currentSurveyDetail.value.questions) return ''
  const q = currentSurveyDetail.value.questions.find(q => q.id === questionId)
  if (!q || !q.options) return ''
  const opt = q.options.find(o => o.id === optionId)
  return opt ? opt.optionText : ''
}

const fetchData = async () => {
  try {
    const [pendingRes, completedRes] = await Promise.all([
      getPendingSurveys(),
      getCompletedSurveys()
    ])
    
    if (pendingRes.code === 200) {
      pendingSurveys.value = pendingRes.data || []
    }
    if (completedRes.code === 200) {
      completedSurveys.value = completedRes.data || []
    }
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.my-surveys-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.content-card {
  border-radius: 8px;
}

.survey-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.survey-card {
  padding: 20px;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  transition: all 0.3s;
}

.survey-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.survey-card.completed {
  background: #fafafa;
}

.survey-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.survey-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.survey-desc {
  margin: 0 0 16px 0;
  color: #666;
  line-height: 1.6;
}

.survey-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.deadline-info,
.submit-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
  font-size: 14px;
}

.countdown {
  color: #52c41a;
  font-weight: 500;
}

.countdown.urgent {
  color: #ff4d4f;
}

.answer-detail {
  max-height: 60vh;
  overflow-y: auto;
}

.answer-item {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.answer-item:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.answer-question {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.question-no {
  padding: 2px 8px;
  background: #1890ff;
  color: white;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
}

.question-title {
  font-weight: 500;
  color: #333;
}

.answer-content {
  padding-left: 40px;
  color: #666;
}

.rating-label {
  margin-right: 8px;
}

.score-text {
  margin-left: 8px;
  color: #fa8c16;
  font-weight: 600;
}

.text-answer {
  margin: 0;
  line-height: 1.6;
  padding: 12px;
  background: #fafafa;
  border-radius: 4px;
}
</style>

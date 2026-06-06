<template>
  <div class="survey-statistics-container">
    <div class="page-header">
      <a-button @click="goBack">
        <ArrowLeftOutlined /> 返回
      </a-button>
      <h2 class="page-title">问卷统计分析</h2>
    </div>

    <div class="main-content" v-loading="loading">
      <a-card :bordered="false" class="content-card">
        <div class="survey-header">
          <h3 class="survey-title">{{ statistics.title }}</h3>
          <p class="survey-desc">{{ statistics.description }}</p>
          <div class="survey-meta">
            <span>截止日期：{{ formatDate(statistics.deadline) }}</span>
            <span v-if="statistics.isAnonymous === 1" class="anonymous-tag">匿名问卷</span>
          </div>
        </div>

        <a-row :gutter="[16, 16]" style="margin-top: 20px">
          <a-col :span="8">
            <div class="stat-box">
              <div class="stat-label">发放范围</div>
              <div class="stat-value">{{ statistics.targetNames?.join('、') || '-' }}</div>
            </div>
          </a-col>
          <a-col :span="8">
            <div class="stat-box">
              <div class="stat-label">完成情况</div>
              <div class="stat-value">{{ statistics.completedCount || 0 }} / {{ statistics.totalCount || 0 }} 人</div>
            </div>
          </a-col>
          <a-col :span="8">
            <div class="stat-box">
              <div class="stat-label">完成率</div>
              <div class="stat-value progress-value">
                <a-progress
                  type="circle"
                  :percent="Math.round(statistics.completionRate || 0)"
                  :size="80"
                />
              </div>
            </div>
          </a-col>
        </a-row>
      </a-card>

      <a-card
        v-for="(question, index) in statistics.questionStatistics"
        :key="question.questionId"
        :bordered="false"
        class="content-card"
        style="margin-top: 16px"
      >
        <template #title>
          <div class="question-title">
            <span class="question-no">Q{{ index + 1 }}</span>
            <span class="question-text">{{ question.title }}</span>
            <a-tag color="blue">{{ getQuestionTypeLabel(question.questionType) }}</a-tag>
          </div>
        </template>

        <div v-if="question.questionType === 1" class="rating-statistics">
          <a-row :gutter="[16, 16]">
            <a-col :span="12">
              <div class="rating-list">
                <div v-for="(item, idx) in question.ratingStatistics" :key="idx" class="rating-item">
                  <span class="rating-label">{{ item.ratingItem }}</span>
                  <div class="rating-score">
                    <a-rate :value="Math.round(item.averageScore)" disabled allow-half />
                    <span class="score-text">{{ item.averageScore.toFixed(1) }} 分</span>
                  </div>
                </div>
              </div>
            </a-col>
            <a-col :span="12">
              <div :ref="el => setBarChartRef(el, index)" class="chart-container"></div>
            </a-col>
          </a-row>
        </div>

        <div v-if="question.questionType === 2" class="single-statistics">
          <a-row :gutter="[16, 16]">
            <a-col :span="12">
              <div class="option-list">
                <div v-for="(opt, idx) in question.optionStatistics" :key="idx" class="option-stat-item">
                  <span class="option-label">{{ opt.optionText }}</span>
                  <div class="option-progress">
                    <a-progress :percent="opt.percentage" :show-info="false" />
                    <span class="option-count">{{ opt.selectCount }}人 ({{ opt.percentage }}%)</span>
                  </div>
                </div>
              </div>
            </a-col>
            <a-col :span="12">
              <div :ref="el => setPieChartRef(el, index)" class="chart-container"></div>
            </a-col>
          </a-row>
        </div>

        <div v-if="question.questionType === 3" class="text-statistics">
          <div v-if="question.textAnswers && question.textAnswers.length > 0" class="text-answers">
            <div v-for="(answer, idx) in question.textAnswers" :key="idx" class="text-answer-item">
              <div class="answer-content">{{ answer.content }}</div>
              <div v-if="answer.employeeName" class="answer-meta">
                {{ answer.employeeName }} - {{ answer.departmentName }}
              </div>
            </div>
          </div>
          <a-empty v-else description="暂无回答" />
        </div>
      </a-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  ArrowLeftOutlined
} from '@ant-design/icons-vue'
import { getSurveyStatistics } from '@/api/survey'
import * as echarts from 'echarts'
import dayjs from 'dayjs'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const statistics = ref({
  title: '',
  description: '',
  deadline: null,
  isAnonymous: 0,
  targetNames: [],
  completedCount: 0,
  totalCount: 0,
  completionRate: 0,
  questionStatistics: []
})

const chartRefs = ref([])

const setBarChartRef = (el, index) => {
  chartRefs.value[index] = el
}

const setPieChartRef = (el, index) => {
  chartRefs.value[index] = el
}

const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const getQuestionTypeLabel = (type) => {
  const labels = { 1: '评分题', 2: '单选题', 3: '开放题' }
  return labels[type] || '未知'
}

const initCharts = () => {
  nextTick(() => {
    statistics.value.questionStatistics.forEach((question, index) => {
      if (!chartRefs.value[index]) return
      
      const chart = echarts.init(chartRefs.value[index])
      
      if (question.questionType === 1) {
        const option = {
          tooltip: {
            trigger: 'axis',
            axisPointer: { type: 'shadow' }
          },
          grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
          },
          xAxis: {
            type: 'category',
            data: question.ratingStatistics?.map(s => s.ratingItem) || [],
            axisLabel: {
              interval: 0,
              rotate: 30
            }
          },
          yAxis: {
            type: 'value',
            min: 0,
            max: 5
          },
          series: [{
            type: 'bar',
            data: question.ratingStatistics?.map(s => s.averageScore) || [],
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#667eea' },
                { offset: 1, color: '#764ba2' }
              ])
            },
            barWidth: '40%'
          }]
        }
        chart.setOption(option)
      } else if (question.questionType === 2) {
        const colors = ['#667eea', '#764ba2', '#f093fb', '#f5576c', '#4facfe']
        const option = {
          tooltip: {
            trigger: 'item',
            formatter: '{b}: {c}人 ({d}%)'
          },
          legend: {
            orient: 'vertical',
            left: 'left'
          },
          series: [{
            type: 'pie',
            radius: ['40%', '70%'],
            avoidLabelOverlap: false,
            itemStyle: {
              borderRadius: 10,
              borderColor: '#fff',
              borderWidth: 2
            },
            label: {
              show: true,
              formatter: '{b}\n{d}%'
            },
            data: question.optionStatistics?.map((opt, idx) => ({
              value: opt.selectCount,
              name: opt.optionText,
              itemStyle: { color: colors[idx % colors.length] }
            })) || []
          }]
        }
        chart.setOption(option)
      }
    })
  })
}

const loadStatistics = async () => {
  loading.value = true
  try {
    const res = await getSurveyStatistics(route.params.id)
    if (res.code === 200) {
      statistics.value = res.data
      initCharts()
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.push('/surveys')
}

onMounted(() => {
  loadStatistics()
})
</script>

<style scoped>
.survey-statistics-container {
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
}

.survey-header {
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 20px;
}

.survey-title {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 600;
}

.survey-desc {
  margin: 0 0 12px 0;
  color: #666;
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

.stat-box {
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
  text-align: center;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.progress-value {
  display: flex;
  justify-content: center;
}

.question-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.question-no {
  padding: 4px 10px;
  background: #1890ff;
  color: white;
  border-radius: 4px;
  font-weight: 600;
}

.question-text {
  flex: 1;
  font-weight: 500;
}

.chart-container {
  height: 300px;
}

.rating-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.rating-item {
  padding: 12px;
  background: #fafafa;
  border-radius: 8px;
}

.rating-label {
  font-weight: 500;
  margin-bottom: 8px;
}

.rating-score {
  display: flex;
  align-items: center;
  gap: 12px;
}

.score-text {
  font-size: 16px;
  font-weight: 600;
  color: #fa8c16;
}

.option-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.option-stat-item {
  padding: 12px;
  background: #fafafa;
  border-radius: 8px;
}

.option-label {
  font-weight: 500;
  margin-bottom: 8px;
}

.option-progress {
  display: flex;
  align-items: center;
  gap: 12px;
}

.option-count {
  font-size: 12px;
  color: #666;
  min-width: 100px;
}

.text-answers {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.text-answer-item {
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
  border-left: 4px solid #1890ff;
}

.answer-content {
  color: #333;
  line-height: 1.6;
}

.answer-meta {
  margin-top: 8px;
  font-size: 12px;
  color: #999;
  text-align: right;
}
</style>

<template>
  <div class="growth-report">
    <a-page-header title="成长报告" @back="() => $router.back()" />
    
    <div v-if="report" class="report-content">
      <a-card style="margin-top: 20px">
        <a-row :gutter="24">
          <a-col :span="8">
            <div class="score-card">
              <div class="score-label">综合得分</div>
              <div class="score-value">{{ report.totalScore || '-' }}</div>
              <a-tag :color="getGradeColor(report.grade)" class="grade-tag">
                {{ report.grade || '-' }}
              </a-tag>
            </div>
          </a-col>
          <a-col :span="8">
            <div class="rank-item">
              <div class="rank-label">部门排名</div>
              <div class="rank-value">{{ report.departmentRank || '-' }} / {{ totalInDepartment }}</div>
            </div>
            <div class="rank-item" style="margin-top: 20px">
              <div class="rank-label">全公司排名</div>
              <div class="rank-value">{{ report.companyRank || '-' }} / {{ totalInCompany }}</div>
            </div>
          </a-col>
          <a-col :span="8">
            <div class="score-detail">
              <div class="score-detail-item">
                <span class="detail-label">自评</span>
                <span class="detail-value">{{ report.selfScore || '-' }}分</span>
              </div>
              <div class="score-detail-item">
                <span class="detail-label">同事评价</span>
                <span class="detail-value">{{ report.colleagueScore || '-' }}分</span>
              </div>
              <div class="score-detail-item">
                <span class="detail-label">管理员评价</span>
                <span class="detail-value">{{ report.adminScore || '-' }}分</span>
              </div>
            </div>
          </a-col>
        </a-row>
      </a-card>

      <a-row :gutter="16" style="margin-top: 20px">
        <a-col :span="12">
          <a-card title="能力雷达图">
            <div ref="radarChart" style="height: 300px"></div>
          </a-card>
        </a-col>
        <a-col :span="12">
          <a-card title="成长趋势">
            <div ref="trendChart" style="height: 300px"></div>
          </a-card>
        </a-col>
      </a-row>

      <a-card title="工作目标达成情况" style="margin-top: 20px">
        <a-table :columns="goalColumns" :data-source="goalResults" :pagination="false">
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'achievement'">
              <a-tag :color="getAchievementColor(record.achievementDegree)">
                {{ getAchievementText(record.achievementDegree) }}
              </a-tag>
            </template>
          </template>
        </a-table>
      </a-card>

      <a-row :gutter="16" style="margin-top: 20px">
        <a-col :span="12">
          <a-card title="同事评价摘要">
            <div v-if="colleagueSummary">
              <p><strong>平均得分：</strong>{{ colleagueSummary.averageScore }}分</p>
              <p><strong>评价人数：</strong>{{ colleagueSummary.evaluatorCount }}人</p>
              <div v-if="colleagueSummary.strengths?.length">
                <p><strong>优势：</strong></p>
                <a-tag v-for="(s, i) in colleagueSummary.strengths" :key="i" color="green" style="margin: 4px">
                  {{ s }}
                </a-tag>
              </div>
            </div>
          </a-card>
        </a-col>
        <a-col :span="12">
          <a-card title="管理员评价">
            <div v-if="adminEvaluation">
              <p><strong>综合评分：</strong>{{ adminEvaluation.overallScore }}分</p>
              <p><strong>综合评价：</strong>{{ adminEvaluation.overallComment }}</p>
              <p><strong>发展建议：</strong>{{ adminEvaluation.developmentSuggestion }}</p>
            </div>
          </a-card>
        </a-col>
      </a-row>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import * as echarts from 'echarts'
import { getMyGrowthReport, getMyGoals, getGrowthHistory } from '@/api/employeeEvaluation'
import { getEmployeeGrowthReport, getEmployeeGoals, getEmployeeGrowthHistory } from '@/api/adminEvaluation'

const route = useRoute()
const userStore = useUserStore()
const isAdmin = computed(() => userStore.role === 'admin')
const report = ref(null)
const goalResults = ref([])
const colleagueSummary = ref(null)
const adminEvaluation = ref(null)
const growthHistory = ref([])
const radarChart = ref(null)
const trendChart = ref(null)

const goalColumns = [
  { title: '目标名称', dataIndex: 'goalName', key: 'goalName' },
  { title: '权重', dataIndex: 'weight', key: 'weight', width: 80 },
  { title: '自评分', dataIndex: 'selfScore', key: 'selfScore', width: 100 },
  { title: '达成度', key: 'achievement', width: 120 }
]

const totalInDepartment = ref(0)
const totalInCompany = ref(0)

const getGradeColor = (grade) => {
  const colors = {
    '卓越': 'gold',
    '优秀': 'green',
    '良好': 'blue',
    '合格': 'default',
    '待提升': 'red'
  }
  return colors[grade] || 'default'
}

const getAchievementColor = (degree) => {
  const colors = { 1: 'gold', 2: 'green', 3: 'blue', 4: 'red' }
  return colors[degree] || 'default'
}

const getAchievementText = (degree) => {
  const texts = { 1: '超出预期', 2: '达到预期', 3: '接近预期', 4: '未达到' }
  return texts[degree] || '未知'
}

const fetchReport = async () => {
  const { cycleId, employeeId } = route.params
  let res, goalsRes, historyRes
  
  if (isAdmin.value) {
    res = await getEmployeeGrowthReport(cycleId, employeeId)
    goalsRes = await getEmployeeGoals(cycleId, employeeId)
    historyRes = await getEmployeeGrowthHistory(employeeId)
  } else {
    res = await getMyGrowthReport(cycleId)
    goalsRes = await getMyGoals(cycleId)
    historyRes = await getGrowthHistory()
  }
  report.value = res.data
  goalResults.value = goalsRes.data || []
  growthHistory.value = historyRes.data || []
  
  await nextTick()
  renderCharts()
}

const renderCharts = () => {
  if (radarChart.value) {
    const chart = echarts.init(radarChart.value)
    const dimensions = ['工作交付质量', '团队协作精神', '沟通响应效率', '专业能力展现', '主动帮助他人']
    chart.setOption({
      tooltip: {},
      radar: {
        indicator: dimensions.map(d => ({ name: d, max: 100 }))
      },
      series: [{
        type: 'radar',
        data: [{
          value: [85, 80, 75, 82, 78],
          name: '本期得分'
        }]
      }]
    })
  }

  if (trendChart.value && growthHistory.value.length > 0) {
    const chart = echarts.init(trendChart.value)
    const cycles = growthHistory.value.map(h => h.cycleName)
    const scores = growthHistory.value.map(h => h.totalScore)
    chart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: cycles },
      yAxis: { type: 'value', max: 100 },
      series: [{
        type: 'line',
        data: scores,
        smooth: true,
        itemStyle: { color: '#1890ff' }
      }]
    })
  }
}

onMounted(() => {
  fetchReport()
})
</script>

<style scoped>
.growth-report {
  padding: 20px;
}

.score-card {
  text-align: center;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: white;
}

.score-label {
  font-size: 14px;
  opacity: 0.9;
}

.score-value {
  font-size: 48px;
  font-weight: bold;
  margin: 10px 0;
}

.grade-tag {
  font-size: 16px;
  padding: 4px 16px;
}

.rank-item {
  text-align: center;
}

.rank-label {
  color: #666;
  font-size: 14px;
}

.rank-value {
  font-size: 24px;
  font-weight: bold;
  color: #1890ff;
  margin-top: 8px;
}

.score-detail {
  padding: 10px;
}

.score-detail-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.detail-label {
  color: #666;
}

.detail-value {
  font-weight: bold;
  color: #1890ff;
}
</style>

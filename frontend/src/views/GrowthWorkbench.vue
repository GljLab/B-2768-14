<template>
  <div class="growth-workbench">
    <a-page-header title="成长工作台" />
    
    <a-row :gutter="16" style="margin-top: 20px">
      <a-col :span="16">
        <a-card title="进行中的评价周期">
          <a-list :data-source="myCycles" size="large">
            <template #renderItem="{ item }">
              <a-list-item>
                <a-list-item-meta>
                  <template #title>
                    <a @click="enterCycle(item)">{{ item.cycleName }}</a>
                  </template>
                  <template #description>
                    <a-space>
                      <span>{{ item.startDate }} ~ {{ item.endDate }}</span>
                      <a-tag :color="getGoalStatusColor(item.goalStatus)">
                        目标: {{ getGoalStatusText(item.goalStatus) }}
                      </a-tag>
                      <a-tag :color="getEvalStatusColor(item.selfEvalStatus)">
                        自评: {{ getEvalStatusText(item.selfEvalStatus) }}
                      </a-tag>
                    </a-space>
                  </template>
                </a-list-item-meta>
                <a-space>
                  <a-button type="primary" size="small" @click="enterCycle(item)">进入</a-button>
                </a-space>
              </a-list-item>
            </template>
          </a-list>
        </a-card>
        
        <a-card title="工作亮点记录" style="margin-top: 20px">
          <template #extra>
            <a-button type="primary" size="small" @click="showHighlightModal">
              + 添加亮点
            </a-button>
          </template>
          <a-timeline>
            <a-timeline-item v-for="h in highlights" :key="h.id">
              <template #dot>
                <a-badge :status="getHighlightTypeStatus(h.highlightType)" />
              </template>
              <p><strong>{{ h.title }}</strong></p>
              <p style="color: #666">{{ h.content }}</p>
              <p style="color: #999; font-size: 12px">{{ h.createdAt }}</p>
            </a-timeline-item>
          </a-timeline>
        </a-card>
      </a-col>
      
      <a-col :span="8">
        <a-card title="我的成长档案">
          <a-statistic title="总评价次数" :value="growthHistory.length" style="margin-bottom: 20px" />
          <a-divider>历史成长曲线</a-divider>
          <div ref="historyChart" style="height: 200px"></div>
        </a-card>
        
        <a-card title="待办事项" style="margin-top: 20px">
          <a-list size="small">
            <a-list-item v-for="(todo, i) in todos" :key="i">
              <a-checkbox v-model:checked="todo.done">{{ todo.text }}</a-checkbox>
            </a-list-item>
          </a-list>
        </a-card>
      </a-col>
    </a-row>

    <a-modal
      v-model:open="highlightModalVisible"
      title="添加工作亮点"
      @ok="submitHighlight"
      @cancel="highlightModalVisible = false"
    >
      <a-form :model="highlightForm" :label-col="{ span: 6 }">
        <a-form-item label="亮点标题" required>
          <a-input v-model:value="highlightForm.title" />
        </a-form-item>
        <a-form-item label="亮点类型">
          <a-select v-model:value="highlightForm.highlightType">
            <a-select-option :value="1">重要工作</a-select-option>
            <a-select-option :value="2">解决难题</a-select-option>
            <a-select-option :value="3">新技能</a-select-option>
            <a-select-option :value="4">帮助同事</a-select-option>
            <a-select-option :value="5">其他</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="详细描述">
          <a-textarea v-model:value="highlightForm.content" :rows="4" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { message } from 'ant-design-vue'
import {
  getMyCycles,
  getMyHighlights,
  addHighlight,
  getGrowthHistory
} from '@/api/employeeEvaluation'

const router = useRouter()
const myCycles = ref([])
const highlights = ref([])
const growthHistory = ref([])
const historyChart = ref(null)
const highlightModalVisible = ref(false)

const highlightForm = reactive({
  cycleId: null,
  title: '',
  highlightType: 1,
  content: ''
})

const todos = ref([
  { text: '完成本季度工作目标设定', done: false },
  { text: '记录本月工作亮点', done: true },
  { text: '准备自我评价', done: false }
])

const getGoalStatusColor = (status) => {
  const colors = { 0: 'default', 1: 'processing', 2: 'blue', 3: 'success' }
  return colors[status] || 'default'
}

const getGoalStatusText = (status) => {
  const texts = { 0: '未开始', 1: '草稿中', 2: '已提交', 3: '已确认' }
  return texts[status] || '未知'
}

const getEvalStatusColor = (status) => {
  const colors = { 0: 'default', 1: 'processing', 2: 'success' }
  return colors[status] || 'default'
}

const getEvalStatusText = (status) => {
  const texts = { 0: '未开始', 1: '进行中', 2: '已完成' }
  return texts[status] || '未知'
}

const getHighlightTypeStatus = (type) => {
  const types = { 1: 'success', 2: 'warning', 3: 'processing', 4: 'blue', 5: 'default' }
  return types[type] || 'default'
}

const fetchData = async () => {
  const cyclesRes = await getMyCycles()
  myCycles.value = cyclesRes.data || []
  
  if (myCycles.value.length > 0) {
    highlightForm.cycleId = myCycles.value[0].cycleId
    const highlightsRes = await getMyHighlights(myCycles.value[0].cycleId)
    highlights.value = highlightsRes.data || []
  }
  
  const historyRes = await getGrowthHistory()
  growthHistory.value = historyRes.data || []
  
  await nextTick()
  renderHistoryChart()
}

const renderHistoryChart = () => {
  if (historyChart.value && growthHistory.value.length > 0) {
    const chart = echarts.init(historyChart.value)
    const cycles = growthHistory.value.map(h => h.cycleName)
    const scores = growthHistory.value.map(h => h.totalScore || 0)
    chart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: cycles, axisLabel: { fontSize: 10 } },
      yAxis: { type: 'value', max: 100 },
      series: [{
        type: 'line',
        data: scores,
        smooth: true,
        itemStyle: { color: '#52c41a' }
      }]
    })
  }
}

const enterCycle = (item) => {
  router.push(`/goal-setting/${item.cycleId}`)
}

const showHighlightModal = () => {
  highlightModalVisible.value = true
}

const submitHighlight = async () => {
  if (!highlightForm.title) {
    message.error('请输入亮点标题')
    return
  }
  try {
    await addHighlight(highlightForm)
    message.success('添加成功')
    highlightModalVisible.value = false
    highlightForm.title = ''
    highlightForm.content = ''
    if (highlightForm.cycleId) {
      const res = await getMyHighlights(highlightForm.cycleId)
      highlights.value = res.data || []
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
.growth-workbench {
  padding: 20px;
}
</style>

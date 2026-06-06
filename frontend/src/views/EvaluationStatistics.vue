<template>
  <div class="evaluation-statistics">
    <a-page-header title="评价数据统计" @back="() => $router.back()" />
    
    <a-row :gutter="16" style="margin-top: 20px">
      <a-col :span="12">
        <a-card title="成长等级分布">
          <div ref="gradeChart" style="height: 300px"></div>
        </a-card>
      </a-col>
      <a-col :span="12">
        <a-card title="各部门平均分对比">
          <div ref="departmentChart" style="height: 300px"></div>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16" style="margin-top: 20px">
      <a-col :span="12">
        <a-card title="高成长员工榜">
          <a-table :columns="topColumns" :data-source="statistics?.topEmployees || []" :pagination="false" size="small">
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'grade'">
                <a-tag :color="getGradeColor(record.grade)">{{ record.grade }}</a-tag>
              </template>
            </template>
          </a-table>
        </a-card>
      </a-col>
      <a-col :span="12">
        <a-card title="需要关注的员工">
          <a-table :columns="attentionColumns" :data-source="statistics?.attentionEmployees || []" :pagination="false" size="small">
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'grade'">
                <a-tag color="warning">{{ record.grade }}</a-tag>
              </template>
            </template>
          </a-table>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import * as echarts from 'echarts'
import { getEvaluationStatistics } from '@/api/adminEvaluation'

const route = useRoute()
const statistics = ref(null)
const gradeChart = ref(null)
const departmentChart = ref(null)
let gradeChartInstance = null
let deptChartInstance = null

const topColumns = [
  { title: '排名', dataIndex: 'companyRank', key: 'rank', width: 60 },
  { title: '员工姓名', dataIndex: 'employeeName', key: 'employeeName' },
  { title: '部门', dataIndex: 'departmentName', key: 'departmentName' },
  { title: '得分', dataIndex: 'totalScore', key: 'totalScore' },
  { title: '等级', key: 'grade' }
]

const attentionColumns = [
  { title: '员工姓名', dataIndex: 'employeeName', key: 'employeeName' },
  { title: '部门', dataIndex: 'departmentName', key: 'departmentName' },
  { title: '得分', dataIndex: 'totalScore', key: 'totalScore' },
  { title: '等级', key: 'grade' }
]

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

const fetchStatistics = async () => {
  const cycleId = route.params.id
  const res = await getEvaluationStatistics(cycleId)
  statistics.value = res.data
  
  await nextTick()
  renderCharts()
}

const renderCharts = () => {
  if (gradeChart.value && statistics.value?.gradeDistribution) {
    gradeChartInstance = echarts.init(gradeChart.value)
    const grades = Object.keys(statistics.value.gradeDistribution)
    const values = Object.values(statistics.value.gradeDistribution)
    
    gradeChartInstance.setOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: '0%' },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        data: grades.map((g, i) => ({ name: g, value: values[i] }))
      }]
    })
  }

  if (departmentChart.value && statistics.value?.departmentAverages) {
    deptChartInstance = echarts.init(departmentChart.value)
    const depts = statistics.value.departmentAverages.map(d => d.departmentName)
    const scores = statistics.value.departmentAverages.map(d => d.averageScore)
    
    deptChartInstance.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: depts },
      yAxis: { type: 'value', max: 100 },
      series: [{
        type: 'bar',
        data: scores,
        itemStyle: { color: '#1890ff' }
      }]
    })
  }
}

onMounted(() => {
  fetchStatistics()
})
</script>

<style scoped>
.evaluation-statistics {
  padding: 20px;
}
</style>

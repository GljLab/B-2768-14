<template>
  <div class="statistics-container">
    <div class="main-content">
      <a-row :gutter="[16, 16]">
        <a-col :span="6">
          <a-card :bordered="false" class="stat-card">
            <div class="stat-item">
              <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
                <UserOutlined />
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ totalEmployees }}</div>
                <div class="stat-label">员工总数</div>
              </div>
            </div>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card :bordered="false" class="stat-card">
            <div class="stat-item">
              <div class="stat-icon" style="background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);">
                <TeamOutlined />
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ activeEmployees }}</div>
                <div class="stat-label">在职员工</div>
              </div>
            </div>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card :bordered="false" class="stat-card">
            <div class="stat-item">
              <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
                <ApartmentOutlined />
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ totalDepartments }}</div>
                <div class="stat-label">部门数量</div>
              </div>
            </div>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card :bordered="false" class="stat-card">
            <div class="stat-item">
              <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
                <ClockCircleOutlined />
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ avgTenure }}</div>
                <div class="stat-label">平均司龄(月)</div>
              </div>
            </div>
          </a-card>
        </a-col>
      </a-row>

      <a-row :gutter="[16, 16]" style="margin-top: 16px">
        <a-col :span="12">
          <a-card :bordered="false" class="chart-card">
            <template #title>各部门人员规模对比</template>
            <div ref="barChartRef" class="chart"></div>
          </a-card>
        </a-col>
        <a-col :span="12">
          <a-card :bordered="false" class="chart-card">
            <template #title>部门人员分布</template>
            <div ref="pieChartRef" class="chart"></div>
          </a-card>
        </a-col>
      </a-row>

      <a-row :gutter="[16, 16]" style="margin-top: 16px">
        <a-col :span="12">
          <a-card :bordered="false" class="chart-card">
            <template #title>在职/离职人员统计</template>
            <div ref="stackedBarChartRef" class="chart"></div>
          </a-card>
        </a-col>
        <a-col :span="12">
          <a-card :bordered="false" class="chart-card">
            <template #title>月度人员增减变化</template>
            <div ref="lineChartRef" class="chart"></div>
          </a-card>
        </a-col>
      </a-row>

      <a-row style="margin-top: 16px">
        <a-col :span="24">
          <a-card :bordered="false" class="table-card">
            <template #title>各部门详细统计</template>
            <a-table
              :columns="tableColumns"
              :data-source="statisticsData"
              :pagination="false"
              row-key="departmentId"
              size="middle"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'totalCount'">
                  <a-tag color="blue">{{ record.totalCount }}</a-tag>
                </template>
                <template v-else-if="column.key === 'activeCount'">
                  <a-tag color="green">{{ record.activeCount }}</a-tag>
                </template>
                <template v-else-if="column.key === 'inactiveCount'">
                  <a-tag color="default">{{ record.inactiveCount }}</a-tag>
                </template>
                <template v-else-if="column.key === 'avgTenureMonths'">
                  {{ record.avgTenureMonths }} 个月
                </template>
              </template>
            </a-table>
          </a-card>
        </a-col>
      </a-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { 
  UserOutlined, TeamOutlined, ApartmentOutlined, ClockCircleOutlined 
} from '@ant-design/icons-vue'
import * as echarts from 'echarts'
import { getDepartmentStatistics, getMonthlyChangeStatistics, getFlatDepartmentList } from '@/api/department'
import { getEmployeeList } from '@/api/employee'

const barChartRef = ref(null)
const pieChartRef = ref(null)
const stackedBarChartRef = ref(null)
const lineChartRef = ref(null)

let barChart = null
let pieChart = null
let stackedBarChart = null
let lineChart = null

const statisticsData = ref([])
const monthlyData = ref(null)
const departmentList = ref([])
const employeeList = ref([])

const totalEmployees = computed(() => {
  return statisticsData.value.reduce((sum, item) => sum + (item.totalCount || 0), 0)
})

const activeEmployees = computed(() => {
  return statisticsData.value.reduce((sum, item) => sum + (item.activeCount || 0), 0)
})

const totalDepartments = computed(() => {
  return departmentList.value.length
})

const avgTenure = computed(() => {
  if (statisticsData.value.length === 0) return '0'
  const total = statisticsData.value.reduce((sum, item) => {
    return sum + (parseFloat(item.avgTenureMonths) || 0) * (item.totalCount || 0)
  }, 0)
  return (total / totalEmployees.value || 0).toFixed(1)
})

const tableColumns = [
  { title: '部门名称', dataIndex: 'departmentName', key: 'departmentName', width: 200 },
  { title: '总人数', dataIndex: 'totalCount', key: 'totalCount', width: 100, align: 'center' },
  { title: '在职人数', dataIndex: 'activeCount', key: 'activeCount', width: 100, align: 'center' },
  { title: '离职人数', dataIndex: 'inactiveCount', key: 'inactiveCount', width: 100, align: 'center' },
  { title: '平均司龄', dataIndex: 'avgTenureMonths', key: 'avgTenureMonths', width: 120, align: 'center' }
]

const fetchData = async () => {
  try {
    const [statsRes, monthlyRes, deptRes, empRes] = await Promise.all([
      getDepartmentStatistics(),
      getMonthlyChangeStatistics(12),
      getFlatDepartmentList(),
      getEmployeeList({ page: 1, size: 1000 })
    ])
    
    statisticsData.value = statsRes.data
    monthlyData.value = monthlyRes.data
    departmentList.value = deptRes.data
    employeeList.value = empRes.data.records
    
    renderCharts()
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

const renderCharts = () => {
  renderBarChart()
  renderPieChart()
  renderStackedBarChart()
  renderLineChart()
}

const renderBarChart = () => {
  if (!barChartRef.value) return
  
  if (!barChart) {
    barChart = echarts.init(barChartRef.value)
  }

  const sortedData = [...statisticsData.value]
    .sort((a, b) => (b.totalCount || 0) - (a.totalCount || 0))
    .slice(0, 10)

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
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'category',
      data: sortedData.map(item => item.departmentName),
      axisLine: { show: false },
      axisTick: { show: false }
    },
    series: [
      {
        name: '人员数量',
        type: 'bar',
        data: sortedData.map(item => item.totalCount || 0),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#667eea' },
            { offset: 1, color: '#764ba2' }
          ]),
          borderRadius: [0, 4, 4, 0]
        },
        barWidth: 20
      }
    ]
  }

  barChart.setOption(option)
}

const renderPieChart = () => {
  if (!pieChartRef.value) return
  
  if (!pieChart) {
    pieChart = echarts.init(pieChartRef.value)
  }

  const pieData = statisticsData.value
    .filter(item => item.totalCount > 0)
    .map(item => ({
      name: item.departmentName,
      value: item.totalCount
    }))

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}人 ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center',
      textStyle: { fontSize: 12 }
    },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['35%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 4,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 'bold'
          }
        },
        data: pieData
      }
    ]
  }

  pieChart.setOption(option)
}

const renderStackedBarChart = () => {
  if (!stackedBarChartRef.value) return
  
  if (!stackedBarChart) {
    stackedBarChart = echarts.init(stackedBarChartRef.value)
  }

  const sortedData = [...statisticsData.value]
    .sort((a, b) => (b.totalCount || 0) - (a.totalCount || 0))
    .slice(0, 10)

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    legend: {
      data: ['在职', '离职'],
      top: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: 40,
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: sortedData.map(item => item.departmentName),
      axisLine: { lineStyle: { color: '#e8e8e8' } },
      axisLabel: { interval: 0, rotate: 30 }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { lineStyle: { type: 'dashed', color: '#e8e8e8' } }
    },
    series: [
      {
        name: '在职',
        type: 'bar',
        stack: 'total',
        data: sortedData.map(item => item.activeCount || 0),
        itemStyle: { color: '#52c41a' },
        barWidth: 20
      },
      {
        name: '离职',
        type: 'bar',
        stack: 'total',
        data: sortedData.map(item => item.inactiveCount || 0),
        itemStyle: { color: '#d9d9d9' },
        barWidth: 20
      }
    ]
  }

  stackedBarChart.setOption(option)
}

const renderLineChart = () => {
  if (!lineChartRef.value || !monthlyData.value) return
  
  if (!lineChart) {
    lineChart = echarts.init(lineChartRef.value)
  }

  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['入职人数', '离职人数'],
      top: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: 40,
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: monthlyData.value.labels || [],
      axisLine: { lineStyle: { color: '#e8e8e8' } }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { lineStyle: { type: 'dashed', color: '#e8e8e8' } }
    },
    series: [
      {
        name: '入职人数',
        type: 'line',
        smooth: true,
        data: monthlyData.value.joinCounts || [],
        itemStyle: { color: '#52c41a' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(82, 196, 26, 0.3)' },
            { offset: 1, color: 'rgba(82, 196, 26, 0.05)' }
          ])
        }
      },
      {
        name: '离职人数',
        type: 'line',
        smooth: true,
        data: monthlyData.value.leaveCounts || [],
        itemStyle: { color: '#ff4d4f' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(255, 77, 79, 0.3)' },
            { offset: 1, color: 'rgba(255, 77, 79, 0.05)' }
          ])
        }
      }
    ]
  }

  lineChart.setOption(option)
}

const handleResize = () => {
  barChart?.resize()
  pieChart?.resize()
  stackedBarChart?.resize()
  lineChart?.resize()
}

onMounted(() => {
  fetchData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  barChart?.dispose()
  pieChart?.dispose()
  stackedBarChart?.dispose()
  lineChart?.dispose()
})
</script>

<style scoped>
.statistics-container {
}

.main-content {
  padding: 0;
}

.stat-card,
.chart-card,
.table-card {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: #6b7280;
  margin-top: 4px;
}

.chart {
  width: 100%;
  height: 300px;
}
</style>

<template>
  <div class="org-chart-container">
    <div class="main-content">
      <a-card :bordered="false" class="chart-card">
        <template #title>
          <span>组织架构图谱</span>
          <a-space style="margin-left: 16px">
            <a-button-group size="small">
              <a-button @click="zoomIn">放大</a-button>
              <a-button @click="zoomOut">缩小</a-button>
              <a-button @click="resetZoom">重置</a-button>
            </a-button-group>
            <a-radio-group v-model:value="layoutType" size="small" @change="changeLayout">
              <a-radio-button value="orthogonal">标准树</a-radio-button>
              <a-radio-button value="radial">放射状</a-radio-button>
            </a-radio-group>
          </a-space>
        </template>
        <div ref="chartRef" class="chart-container"></div>
      </a-card>
    </div>

    <a-modal
      v-model:open="detailModalVisible"
      :title="selectedDepartment?.name"
      width="700px"
      :footer="null"
    >
      <template v-if="selectedDepartment">
        <a-descriptions :column="2" bordered size="small">
          <a-descriptions-item label="部门编码">
            {{ selectedDepartment.code }}
          </a-descriptions-item>
          <a-descriptions-item label="责任人">
            {{ selectedDepartment.managerName || '未设置' }}
          </a-descriptions-item>
          <a-descriptions-item label="直接下属人数">
            {{ selectedDepartment.directMemberCount || 0 }} 人
          </a-descriptions-item>
          <a-descriptions-item label="总人数(含子部门)">
            {{ selectedDepartment.totalMemberCount || 0 }} 人
          </a-descriptions-item>
          <a-descriptions-item label="备注" :span="2">
            {{ selectedDepartment.remark || '无' }}
          </a-descriptions-item>
        </a-descriptions>

        <a-divider orientation="left">部门成员</a-divider>
        <a-table
          :columns="memberColumns"
          :data-source="memberList"
          :pagination="false"
          :loading="memberLoading"
          row-key="id"
          size="small"
          :scroll="{ y: 200 }"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'status'">
              <a-tag :color="record.status === 1 ? 'success' : 'default'" size="small">
                {{ record.status === 1 ? '在职' : '离职' }}
              </a-tag>
            </template>
          </template>
        </a-table>
      </template>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getDepartmentTree, getDepartmentDescendantIds } from '@/api/department'
import { getEmployeeListByDepartmentIds } from '@/api/employee'

const chartRef = ref(null)
let chartInstance = null
const layoutType = ref('orthogonal')

const treeData = ref([])
const departmentMap = ref(new Map())

const fetchDepartmentTree = async () => {
  try {
    const res = await getDepartmentTree()
    treeData.value = res.data
    buildDepartmentMap(res.data)
    renderChart()
  } catch (error) {
    console.error('获取部门树失败:', error)
  }
}

const buildDepartmentMap = (departments) => {
  departments.forEach(dept => {
    departmentMap.value.set(dept.id, dept)
    if (dept.children && dept.children.length > 0) {
      buildDepartmentMap(dept.children)
    }
  })
}

const transformChartData = (data) => {
  return data.map(item => ({
    name: item.name,
    value: item.totalMemberCount || 0,
    managerName: item.managerName,
    directMemberCount: item.directMemberCount,
    totalMemberCount: item.totalMemberCount,
    code: item.code,
    id: item.id,
    children: item.children && item.children.length > 0 ? transformChartData(item.children) : null
  }))
}

const renderChart = () => {
  if (!chartRef.value) return
  
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
    chartInstance.on('click', handleChartClick)
  }

  const chartData = transformChartData(treeData.value)

  const option = {
    tooltip: {
      trigger: 'item',
      triggerOn: 'mousemove',
      formatter: (params) => {
        const data = params.data
        return `
          <div style="padding: 8px;">
            <div style="font-weight: bold; font-size: 14px; margin-bottom: 4px;">${data.name}</div>
            <div style="color: #666;">编码: ${data.code}</div>
            <div style="color: #666;">负责人: ${data.managerName || '未设置'}</div>
            <div style="color: #666;">团队规模: ${data.value} 人</div>
            <div style="color: #999; font-size: 12px; margin-top: 4px;">点击查看详情</div>
          </div>
        `
      }
    },
    toolbox: {
      show: true,
      feature: {
        saveAsImage: {
          show: true,
          title: '保存图片'
        }
      }
    },
    series: [
      {
        type: 'tree',
        data: chartData,
        top: '10%',
        bottom: '10%',
        layout: layoutType.value,
        symbol: 'rect',
        symbolSize: [160, 60],
        orient: 'TB',
        label: {
          position: 'inside',
          formatter: (params) => {
            return `{name|${params.data.name}}\n{info|${params.data.managerName || '无负责人'} | ${params.value}人}`
          },
          rich: {
            name: {
              fontSize: 13,
              fontWeight: 'bold',
              color: '#fff',
              lineHeight: 22
            },
            info: {
              fontSize: 11,
              color: 'rgba(255,255,255,0.8)',
              lineHeight: 20
            }
          }
        },
        leaves: {
          label: {
            position: 'inside'
          }
        },
        emphasis: {
          focus: 'descendant'
        },
        expandAndCollapse: true,
        animationDuration: 550,
        animationDurationUpdate: 750,
        initialTreeDepth: 2,
        lineStyle: {
          color: '#c7d2fe',
          width: 2,
          curveness: 0.5
        },
        itemStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: '#667eea' },
              { offset: 1, color: '#764ba2' }
            ]
          },
          borderColor: '#5b5fbb',
          borderWidth: 2,
          borderRadius: 8
        }
      }
    ]
  }

  chartInstance.setOption(option)
}

const zoomIn = () => {
  if (chartInstance) {
    chartInstance.dispatchAction({
      type: 'zoom',
      scale: 1.2
    })
  }
}

const zoomOut = () => {
  if (chartInstance) {
    chartInstance.dispatchAction({
      type: 'zoom',
      scale: 0.8
    })
  }
}

const resetZoom = () => {
  renderChart()
}

const changeLayout = () => {
  renderChart()
}

const detailModalVisible = ref(false)
const selectedDepartment = ref(null)
const memberLoading = ref(false)
const memberList = ref([])

const memberColumns = [
  { title: '姓名', dataIndex: 'name', key: 'name', width: 100 },
  { title: '职位', dataIndex: 'position', key: 'position', width: 120 },
  { title: '部门', dataIndex: 'department', key: 'department', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 80 }
]

const handleChartClick = async (params) => {
  if (params.data && params.data.id) {
    selectedDepartment.value = departmentMap.value.get(params.data.id)
    if (selectedDepartment.value) {
      detailModalVisible.value = true
      await fetchDepartmentMembers(selectedDepartment.value.id)
    }
  }
}

const fetchDepartmentMembers = async (departmentId) => {
  memberLoading.value = true
  try {
    const res = await getDepartmentDescendantIds(departmentId)
    const departmentIds = res.data
    
    const empRes = await getEmployeeListByDepartmentIds({
      page: 1,
      size: 100,
      departmentIds: departmentIds.join(',')
    })
    
    memberList.value = empRes.data.records
  } catch (error) {
    console.error('获取部门成员失败:', error)
  } finally {
    memberLoading.value = false
  }
}

const handleResize = () => {
  if (chartInstance) {
    chartInstance.resize()
  }
}

onMounted(() => {
  fetchDepartmentTree()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
})
</script>

<style scoped>
.org-chart-container {
}

.main-content {
  padding: 0;
}

.chart-card {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.chart-container {
  width: 100%;
  height: calc(100vh - 200px);
  min-height: 600px;
}
</style>

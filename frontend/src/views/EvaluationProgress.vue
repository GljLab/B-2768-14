<template>
  <div class="evaluation-progress">
    <a-page-header title="评价进度看板" @back="() => $router.back()" />
    
    <a-card title="整体进度" style="margin-top: 20px">
      <a-row :gutter="16">
        <a-col :span="4">
          <a-statistic title="参与总人数" :value="progress?.totalEmployees || 0" />
        </a-col>
        <a-col :span="4">
          <a-statistic title="目标设定完成" :value="progress?.goalCompleted || 0" suffix="/" style="display: inline-block" />
          <a-statistic :value="progress?.totalEmployees || 0" style="display: inline-block; margin-left: 8px" />
        </a-col>
        <a-col :span="4">
          <a-statistic title="自评完成" :value="progress?.selfEvalCompleted || 0" suffix="/" style="display: inline-block" />
          <a-statistic :value="progress?.totalEmployees || 0" style="display: inline-block; margin-left: 8px" />
        </a-col>
        <a-col :span="4">
          <a-statistic title="同事评价完成" :value="progress?.colleagueEvalCompleted || 0" suffix="/" style="display: inline-block" />
          <a-statistic :value="progress?.totalEmployees || 0" style="display: inline-block; margin-left: 8px" />
        </a-col>
        <a-col :span="4">
          <a-statistic title="管理员评价完成" :value="progress?.adminEvalCompleted || 0" suffix="/" style="display: inline-block" />
          <a-statistic :value="progress?.totalEmployees || 0" style="display: inline-block; margin-left: 8px" />
        </a-col>
        <a-col :span="4">
          <a-statistic title="一对一沟通完成" :value="progress?.oneOnOneCompleted || 0" suffix="/" style="display: inline-block" />
          <a-statistic :value="progress?.totalEmployees || 0" style="display: inline-block; margin-left: 8px" />
        </a-col>
      </a-row>
    </a-card>

    <a-card title="员工详细进度" style="margin-top: 20px">
      <a-table :columns="columns" :data-source="progress?.employees || []" :pagination="{ pageSize: 10 }">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'goalStatus'">
            <a-tag :color="getStatusColor(record.goalStatus)">
              {{ getGoalStatusText(record.goalStatus) }}
            </a-tag>
          </template>
          <template v-if="column.key === 'selfEvalStatus'">
            <a-tag :color="getStatusColor(record.selfEvalStatus, 2)">
              {{ getEvalStatusText(record.selfEvalStatus) }}
            </a-tag>
          </template>
          <template v-if="column.key === 'colleagueEvalStatus'">
            <a-tag :color="getStatusColor(record.colleagueEvalStatus, 2)">
              {{ getEvalStatusText(record.colleagueEvalStatus) }}
            </a-tag>
          </template>
          <template v-if="column.key === 'adminEvalStatus'">
            <a-tag :color="getStatusColor(record.adminEvalStatus, 1)">
              {{ getAdminEvalStatusText(record.adminEvalStatus) }}
            </a-tag>
          </template>
          <template v-if="column.key === 'oneOnOneStatus'">
            <a-tag :color="getStatusColor(record.oneOnOneStatus, 2)">
              {{ getOneOnOneStatusText(record.oneOnOneStatus) }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-button type="link" @click="evaluateEmployee(record.employeeId)">评价</a-button>
            <a-button type="link" @click="viewReport(record.employeeId)">查看报告</a-button>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCycleProgress } from '@/api/adminEvaluation'

const route = useRoute()
const router = useRouter()
const progress = ref(null)

const columns = [
  { title: '员工姓名', dataIndex: 'employeeName', key: 'employeeName' },
  { title: '部门', dataIndex: 'departmentName', key: 'departmentName' },
  { title: '目标设定', key: 'goalStatus' },
  { title: '自评', key: 'selfEvalStatus' },
  { title: '同事评价', key: 'colleagueEvalStatus' },
  { title: '管理员评价', key: 'adminEvalStatus' },
  { title: '一对一沟通', key: 'oneOnOneStatus' },
  { title: '操作', key: 'action', width: 180 }
]

const getStatusColor = (status, completedValue = 3) => {
  if (status >= completedValue) return 'success'
  if (status > 0) return 'processing'
  return 'default'
}

const getGoalStatusText = (status) => {
  const map = { 0: '未开始', 1: '草稿中', 2: '已提交', 3: '已确认' }
  return map[status] || '未知'
}

const getEvalStatusText = (status) => {
  const map = { 0: '未开始', 1: '进行中', 2: '已完成' }
  return map[status] || '未知'
}

const getAdminEvalStatusText = (status) => {
  const map = { 0: '未评价', 1: '已评价' }
  return map[status] || '未知'
}

const getOneOnOneStatusText = (status) => {
  const map = { 0: '未开始', 1: '已预约', 2: '已完成' }
  return map[status] || '未知'
}

const fetchProgress = async () => {
  const cycleId = route.params.id
  const res = await getCycleProgress(cycleId)
  progress.value = res.data
}

const evaluateEmployee = (employeeId) => {
  const cycleId = route.params.id
  router.push(`/admin-evaluate/${cycleId}/${employeeId}`)
}

const viewReport = (employeeId) => {
  const cycleId = route.params.id
  router.push(`/growth-report/${cycleId}/${employeeId}`)
}

onMounted(() => {
  fetchProgress()
})
</script>

<style scoped>
.evaluation-progress {
  padding: 20px;
}
</style>

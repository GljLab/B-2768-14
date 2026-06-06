<template>
  <div class="evaluation-cycle-manage">
    <a-card title="评价周期管理">
      <template #extra>
        <a-button type="primary" @click="showCreateModal">
          + 新建评价周期
        </a-button>
      </template>
      
      <a-table :columns="columns" :data-source="cycleList" :loading="loading" :pagination="pagination">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ getStatusText(record.status) }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-button type="link" @click="viewProgress(record.id)">进度看板</a-button>
            <a-button v-if="record.status === 0" type="link" @click="handleEdit(record)">编辑</a-button>
            <a-button v-if="record.status === 0" type="link" @click="handleStart(record.id)">启动</a-button>
            <a-button v-if="record.status === 1" type="link" @click="handleEnd(record.id)">结束</a-button>
            <a-button type="link" @click="viewEvaluations(record.id)">评价记录</a-button>
            <a-button type="link" @click="viewStatistics(record.id)">数据统计</a-button>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:open="createModalVisible"
      :title="editingId ? '编辑评价周期' : '创建评价周期'"
      width="800px"
      :confirm-loading="submitLoading"
      @ok="handleSubmit"
      @cancel="handleCancel"
    >
      <a-form :model="formState" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="周期名称" required>
          <a-input v-model:value="formState.cycleName" placeholder="请输入周期名称" />
        </a-form-item>
        <a-form-item label="时间范围" required>
          <a-range-picker v-model:value="formState.dateRange" style="width: 100%" />
        </a-form-item>
        <a-form-item label="参与范围" required>
          <a-select v-model:value="formState.targetType">
            <a-select-option :value="1">全员</a-select-option>
            <a-select-option :value="2">指定部门</a-select-option>
            <a-select-option :value="3">手动选择</a-select-option>
          </a-select>
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="自评权重(%)">
              <a-input-number v-model:value="formState.selfWeight" :min="0" :max="100" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="同事评价权重(%)">
              <a-input-number v-model:value="formState.colleagueWeight" :min="0" :max="100" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="管理员评价权重(%)">
          <a-input-number v-model:value="formState.adminWeight" :min="0" :max="100" />
        </a-form-item>
        <a-divider>阶段时间设置</a-divider>
        <a-form-item label="目标设定截止">
          <a-date-picker v-model:value="formState.goalDeadline" show-time style="width: 100%" />
        </a-form-item>
        <a-form-item label="自评时间段">
          <a-range-picker v-model:value="formState.selfEvalRange" show-time style="width: 100%" />
        </a-form-item>
        <a-form-item label="同事评价时间段">
          <a-range-picker v-model:value="formState.colleagueEvalRange" show-time style="width: 100%" />
        </a-form-item>
        <a-form-item label="管理员评价截止">
          <a-date-picker v-model:value="formState.adminEvalDeadline" show-time style="width: 100%" />
        </a-form-item>
        <a-form-item label="一对一沟通截止">
          <a-date-picker v-model:value="formState.oneOnOneDeadline" show-time style="width: 100%" />
        </a-form-item>
        <a-form-item label="反馈意见截止">
          <a-date-picker v-model:value="formState.feedbackDeadline" show-time style="width: 100%" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import {
  getCycleList,
  createCycle,
  updateCycle,
  getCycleDetail,
  startCycle,
  endCycle
} from '@/api/adminEvaluation'
import dayjs from 'dayjs'

const router = useRouter()
const loading = ref(false)
const submitLoading = ref(false)
const createModalVisible = ref(false)
const editingId = ref(null)
const cycleList = ref([])

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  onChange: (page) => {
    pagination.current = page
    fetchCycles()
  }
})

const columns = [
  { title: '周期名称', dataIndex: 'cycleName', key: 'cycleName' },
  { title: '开始日期', dataIndex: 'startDate', key: 'startDate' },
  { title: '结束日期', dataIndex: 'endDate', key: 'endDate' },
  { title: '状态', key: 'status' },
  { title: '操作', key: 'action', width: 320 }
]

const formState = reactive({
  cycleName: '',
  dateRange: [],
  targetType: 1,
  departmentIds: [],
  employeeIds: [],
  selfWeight: 20,
  colleagueWeight: 30,
  adminWeight: 50,
  goalDeadline: null,
  selfEvalRange: [],
  colleagueEvalRange: [],
  adminEvalDeadline: null,
  oneOnOneDeadline: null,
  feedbackDeadline: null
})

const statusMap = {
  0: { text: '草稿', color: 'default' },
  1: { text: '进行中', color: 'processing' },
  2: { text: '已结束', color: 'success' }
}

const getStatusColor = (status) => {
  return statusMap[status]?.color || 'default'
}

const getStatusText = (status) => {
  return statusMap[status]?.text || '未知'
}

const fetchCycles = async () => {
  loading.value = true
  try {
    const res = await getCycleList({
      page: pagination.current,
      size: pagination.pageSize
    })
    cycleList.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const showCreateModal = () => {
  editingId.value = null
  resetForm()
  createModalVisible.value = true
}

const handleEdit = async (record) => {
  editingId.value = record.id
  try {
    const res = await getCycleDetail(record.id)
    const data = res.data
    formState.cycleName = data.cycleName
    formState.dateRange = [dayjs(data.startDate), dayjs(data.endDate)]
    formState.targetType = data.targetType
    formState.selfWeight = data.selfWeight
    formState.colleagueWeight = data.colleagueWeight
    formState.adminWeight = data.adminWeight
    formState.goalDeadline = data.goalDeadline ? dayjs(data.goalDeadline) : null
    formState.selfEvalRange = (data.selfEvalStart && data.selfEvalDeadline) ? [dayjs(data.selfEvalStart), dayjs(data.selfEvalDeadline)] : []
    formState.colleagueEvalRange = (data.colleagueEvalStart && data.colleagueEvalDeadline) ? [dayjs(data.colleagueEvalStart), dayjs(data.colleagueEvalDeadline)] : []
    formState.adminEvalDeadline = data.adminEvalDeadline ? dayjs(data.adminEvalDeadline) : null
    formState.oneOnOneDeadline = data.oneOnOneDeadline ? dayjs(data.oneOnOneDeadline) : null
    formState.feedbackDeadline = data.feedbackDeadline ? dayjs(data.feedbackDeadline) : null
    createModalVisible.value = true
  } catch (e) {
    console.error(e)
    message.error('获取详情失败')
  }
}

const resetForm = () => {
  formState.cycleName = ''
  formState.dateRange = []
  formState.targetType = 1
  formState.departmentIds = []
  formState.employeeIds = []
  formState.selfWeight = 20
  formState.colleagueWeight = 30
  formState.adminWeight = 50
  formState.goalDeadline = null
  formState.selfEvalRange = []
  formState.colleagueEvalRange = []
  formState.adminEvalDeadline = null
  formState.oneOnOneDeadline = null
  formState.feedbackDeadline = null
}

const handleCancel = () => {
  createModalVisible.value = false
  resetForm()
  editingId.value = null
}

const handleSubmit = async () => {
  if (!formState.cycleName) {
    message.error('请输入周期名称')
    return
  }
  if (!formState.dateRange || formState.dateRange.length < 2) {
    message.error('请选择时间范围')
    return
  }
  
  submitLoading.value = true
  try {
    const data = {
      cycleName: formState.cycleName,
      startDate: formState.dateRange[0].format('YYYY-MM-DD'),
      endDate: formState.dateRange[1].format('YYYY-MM-DD'),
      targetType: formState.targetType,
      selfWeight: formState.selfWeight,
      colleagueWeight: formState.colleagueWeight,
      adminWeight: formState.adminWeight,
      goalDeadline: formState.goalDeadline?.format('YYYY-MM-DD HH:mm:ss'),
      selfEvalStart: formState.selfEvalRange?.[0]?.format('YYYY-MM-DD HH:mm:ss'),
      selfEvalDeadline: formState.selfEvalRange?.[1]?.format('YYYY-MM-DD HH:mm:ss'),
      colleagueEvalStart: formState.colleagueEvalRange?.[0]?.format('YYYY-MM-DD HH:mm:ss'),
      colleagueEvalDeadline: formState.colleagueEvalRange?.[1]?.format('YYYY-MM-DD HH:mm:ss'),
      adminEvalDeadline: formState.adminEvalDeadline?.format('YYYY-MM-DD HH:mm:ss'),
      oneOnOneDeadline: formState.oneOnOneDeadline?.format('YYYY-MM-DD HH:mm:ss'),
      feedbackDeadline: formState.feedbackDeadline?.format('YYYY-MM-DD HH:mm:ss')
    }
    
    if (editingId.value) {
      await updateCycle(editingId.value, data)
      message.success('更新成功')
    } else {
      data.gradeStandards = [
        { gradeName: '卓越', minScore: 90, maxScore: 100, sortOrder: 1 },
        { gradeName: '优秀', minScore: 80, maxScore: 89, sortOrder: 2 },
        { gradeName: '良好', minScore: 70, maxScore: 79, sortOrder: 3 },
        { gradeName: '合格', minScore: 60, maxScore: 69, sortOrder: 4 },
        { gradeName: '待提升', minScore: 0, maxScore: 59, sortOrder: 5 }
      ]
      data.dimensions = [
        { dimensionName: '工作交付质量', dimensionDesc: '工作成果的质量和准确性', maxScore: 5, sortOrder: 1 },
        { dimensionName: '团队协作精神', dimensionDesc: '与团队成员的合作能力', maxScore: 5, sortOrder: 2 },
        { dimensionName: '沟通响应效率', dimensionDesc: '沟通的及时性和有效性', maxScore: 5, sortOrder: 3 },
        { dimensionName: '专业能力展现', dimensionDesc: '专业技能和知识水平', maxScore: 5, sortOrder: 4 },
        { dimensionName: '主动帮助他人', dimensionDesc: '主动帮助同事的意愿和行动', maxScore: 5, sortOrder: 5 }
      ]
      await createCycle(data)
      message.success('创建成功')
    }
    createModalVisible.value = false
    resetForm()
    editingId.value = null
    fetchCycles()
  } catch (e) {
    console.error(e)
  } finally {
    submitLoading.value = false
  }
}

const handleStart = async (id) => {
  try {
    await startCycle(id)
    message.success('已启动')
    fetchCycles()
  } catch (e) {
    console.error(e)
  }
}

const handleEnd = async (id) => {
  try {
    await endCycle(id)
    message.success('已结束')
    fetchCycles()
  } catch (e) {
    console.error(e)
  }
}

const viewProgress = (id) => {
  router.push(`/evaluation-progress/${id}`)
}

const viewEvaluations = (id) => {
  router.push(`/evaluation-records/${id}`)
}

const viewStatistics = (id) => {
  router.push(`/evaluation-statistics/${id}`)
}

onMounted(() => {
  fetchCycles()
})
</script>

<style scoped>
.evaluation-cycle-manage {
  padding: 20px;
}
</style>

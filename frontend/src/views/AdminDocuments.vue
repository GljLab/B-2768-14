<template>
  <div class="admin-documents-container">
    <div class="main-content">
      <a-row :gutter="[16, 16]">
        <a-col :span="6">
          <a-card class="stat-card" :bordered="false">
            <div class="stat-icon stat-pending">
              <ClockCircleOutlined />
            </div>
            <div class="stat-info">
              <p class="stat-value">{{ statistics.pendingAuditCount || 0 }}</p>
              <p class="stat-label">待审核证件</p>
            </div>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="stat-card" :bordered="false">
            <div class="stat-icon stat-expiring">
              <ExclamationCircleOutlined />
            </div>
            <div class="stat-info">
              <p class="stat-value">{{ statistics.expiringThisMonth || 0 }}</p>
              <p class="stat-label">本月到期</p>
            </div>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="stat-card" :bordered="false">
            <div class="stat-icon stat-expired">
              <WarningOutlined />
            </div>
            <div class="stat-info">
              <p class="stat-value">{{ statistics.expiredCount || 0 }}</p>
              <p class="stat-label">已过期证件</p>
            </div>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="stat-card" :bordered="false">
            <div class="stat-icon stat-idcard">
              <IdcardOutlined />
            </div>
            <div class="stat-info">
              <p class="stat-value">{{ statistics.idCardRate?.toFixed(1) || 0 }}%</p>
              <p class="stat-label">身份证覆盖率</p>
            </div>
          </a-card>
        </a-col>
      </a-row>

      <a-card :bordered="false" class="content-card" style="margin-top: 16px">
        <a-tabs v-model:activeKey="activeTab" @change="handleTabChange">
          <a-tab-pane key="pending" tab="待审核">
            <div class="tab-toolbar">
              <a-input-search
                v-model:value="searchKeyword"
                placeholder="搜索员工姓名"
                style="width: 240px"
                @search="fetchPendingList"
                allow-clear
              />
              <a-select v-model:value="filterTypeId" style="width: 160px" @change="fetchPendingList" allow-clear placeholder="选择证件类型">
                <a-select-option v-for="type in documentTypes" :key="type.id" :value="type.id">
                  {{ type.typeName }}
                </a-select-option>
              </a-select>
            </div>
            <a-table
              :columns="pendingColumns"
              :data-source="pendingList"
              :loading="loading"
              :pagination="pendingPagination"
              @change="handlePendingTableChange"
              row-key="id"
              size="middle"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'documentType'">
                  <span>
                    <component :is="getTypeIcon(record.typeCode)" class="type-icon" />
                    {{ record.documentTypeName }}
                  </span>
                </template>
                <template v-else-if="column.key === 'action'">
                  <a-space>
                    <a-button type="link" @click="viewAuditDetail(record)">
                      审核
                    </a-button>
                  </a-space>
                </template>
              </template>
            </a-table>
          </a-tab-pane>

          <a-tab-pane key="monitor" tab="到期监控">
            <div class="monitor-stats">
              <a-space size="large">
                <span class="monitor-stat">
                  <span class="stat-num text-danger">{{ statistics.expiredCount || 0 }}</span>
                  <span class="stat-label">已过期</span>
                </span>
                <span class="monitor-stat">
                  <span class="stat-num text-warning">{{ statistics.expiringCount || 0 }}</span>
                  <span class="stat-label">即将到期</span>
                </span>
              </a-space>
            </div>
            <a-table
              :columns="expiryColumns"
              :data-source="expiryList"
              :loading="loading"
              :pagination="false"
              row-key="id"
              size="middle"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'expiryStatus'">
                  <a-tag :color="record.expiryStatus === 'expired' ? 'red' : 'gold'">
                    {{ record.expiryStatus === 'expired' ? '已过期' : '即将到期' }}
                  </a-tag>
                </template>
                <template v-else-if="column.key === 'daysRemaining'">
                  <span :class="record.daysRemaining < 0 ? 'text-danger' : 'text-warning'">
                    {{ record.daysRemaining < 0 ? `已过期 ${Math.abs(record.daysRemaining)} 天` : `还剩 ${record.daysRemaining} 天` }}
                  </span>
                </template>
              </template>
            </a-table>
          </a-tab-pane>

          <a-tab-pane key="statistics" tab="完整度统计">
            <div class="statistics-content">
              <div class="stat-overview">
                <div class="overview-item">
                  <div class="overview-icon">
                    <UserOutlined />
                  </div>
                  <div class="overview-info">
                    <p class="overview-value">{{ statistics.totalEmployees || 0 }}</p>
                    <p class="overview-label">员工总数</p>
                  </div>
                </div>
              </div>
              <div class="progress-section">
                <h4>必备证件完整度</h4>
                <div class="progress-item">
                  <div class="progress-header">
                    <span>身份证上传</span>
                    <span>{{ statistics.idCardCount || 0 }} / {{ statistics.totalEmployees || 0 }}</span>
                  </div>
                  <a-progress :percent="statistics.idCardRate || 0" :stroke-color="getProgressColor(statistics.idCardRate)" />
                </div>
                <div class="progress-item">
                  <div class="progress-header">
                    <span>学历证书上传</span>
                    <span>{{ statistics.educationCount || 0 }} / {{ statistics.totalEmployees || 0 }}</span>
                  </div>
                  <a-progress :percent="statistics.educationRate || 0" :stroke-color="getProgressColor(statistics.educationRate)" />
                </div>
              </div>
            </div>
          </a-tab-pane>

          <a-tab-pane key="query" tab="证件档案">
            <div class="query-toolbar">
              <a-input-search
                v-model:value="queryParams.employeeName"
                placeholder="搜索员工姓名"
                style="width: 200px"
                @search="fetchDocumentList"
                allow-clear
              />
              <a-select v-model:value="queryParams.documentTypeId" style="width: 160px" @change="fetchDocumentList" allow-clear placeholder="证件类型">
                <a-select-option v-for="type in documentTypes" :key="type.id" :value="type.id">
                  {{ type.typeName }}
                </a-select-option>
              </a-select>
              <a-select v-model:value="queryParams.auditStatus" style="width: 140px" @change="fetchDocumentList" allow-clear placeholder="审核状态">
                <a-select-option :value="0">待审核</a-select-option>
                <a-select-option :value="1">已通过</a-select-option>
                <a-select-option :value="2">已拒绝</a-select-option>
              </a-select>
              <a-select v-model:value="queryParams.expiryStatus" style="width: 140px" @change="fetchDocumentList" allow-clear placeholder="有效期状态">
                <a-select-option value="valid">有效</a-select-option>
                <a-select-option value="expiring">即将到期</a-select-option>
                <a-select-option value="expired">已过期</a-select-option>
                <a-select-option value="permanent">长期有效</a-select-option>
              </a-select>
            </div>
            <a-table
              :columns="documentColumns"
              :data-source="documentList"
              :loading="loading"
              :pagination="documentPagination"
              @change="handleDocumentTableChange"
              row-key="id"
              size="middle"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'documentType'">
                  <span>
                    <component :is="getTypeIcon(record.typeCode)" class="type-icon" />
                    {{ record.documentTypeName }}
                  </span>
                </template>
                <template v-else-if="column.key === 'auditStatus'">
                  <a-tag :color="getAuditStatusColor(record.auditStatus)">
                    {{ getAuditStatusText(record.auditStatus) }}
                  </a-tag>
                </template>
                <template v-else-if="column.key === 'action'">
                  <a-space>
                    <a-button type="link" @click="viewDocumentDetail(record)">
                      详情
                    </a-button>
                  </a-space>
                </template>
              </template>
            </a-table>
          </a-tab-pane>
        </a-tabs>
      </a-card>
    </div>

    <a-modal
      v-model:open="showAuditModal"
      title="证件审核"
      :footer="null"
      width="900px"
      :destroyOnClose="true"
    >
      <div v-if="auditDocument" class="audit-content">
        <div class="audit-header">
          <div class="audit-employee">
            <a-avatar size="large">
              {{ auditDocument.employeeName?.charAt(0) || 'U' }}
            </a-avatar>
            <div class="employee-info">
              <h4>{{ auditDocument.employeeName }}</h4>
              <p>{{ auditDocument.documentTypeName }}</p>
            </div>
          </div>
          <a-tag color="gold">待审核</a-tag>
        </div>
        <div class="audit-body">
          <div class="audit-images">
            <div class="image-section">
              <h5>正面</h5>
              <img :src="getImageUrl(auditDocument.frontImage)" alt="正面" @click="previewImage(auditDocument.frontImage)" />
            </div>
            <div v-if="auditDocument.backImage" class="image-section">
              <h5>反面</h5>
              <img :src="getImageUrl(auditDocument.backImage)" alt="反面" @click="previewImage(auditDocument.backImage)" />
            </div>
          </div>
          <div class="audit-info">
            <h5>证件信息</h5>
            <a-descriptions :column="1" size="small" bordered>
              <template v-if="auditDocument.typeCode === 'ID_CARD'">
                <a-descriptions-item label="证件号码">{{ auditDocument.documentNumber }}</a-descriptions-item>
                <a-descriptions-item label="签发机关">{{ auditDocument.issueAuthority }}</a-descriptions-item>
                <a-descriptions-item label="有效期">
                  {{ auditDocument.isPermanent === 1 ? '长期有效' : formatDate(auditDocument.expiryDate) }}
                </a-descriptions-item>
              </template>
              <template v-else-if="auditDocument.typeCode === 'EDUCATION'">
                <a-descriptions-item label="毕业院校">{{ auditDocument.school }}</a-descriptions-item>
                <a-descriptions-item label="专业">{{ auditDocument.major }}</a-descriptions-item>
                <a-descriptions-item label="毕业时间">{{ formatDate(auditDocument.graduationDate) }}</a-descriptions-item>
                <a-descriptions-item label="证书编号">{{ auditDocument.documentNumber }}</a-descriptions-item>
              </template>
              <template v-else-if="auditDocument.typeCode === 'PROFESSIONAL'">
                <a-descriptions-item label="证书名称">{{ auditDocument.certificateName }}</a-descriptions-item>
                <a-descriptions-item label="发证机关">{{ auditDocument.issueAuthority }}</a-descriptions-item>
                <a-descriptions-item label="获证时间">{{ formatDate(auditDocument.issueDate) }}</a-descriptions-item>
                <a-descriptions-item label="有效期">
                  {{ auditDocument.isPermanent === 1 ? '长期有效' : (auditDocument.expiryDate ? formatDate(auditDocument.expiryDate) : '无') }}
                </a-descriptions-item>
              </template>
              <a-descriptions-item label="上传时间">{{ formatDateTime(auditDocument.createdAt) }}</a-descriptions-item>
            </a-descriptions>
          </div>
        </div>
        <div class="audit-form">
          <a-form :model="auditForm" layout="vertical">
            <a-form-item label="审核结果">
              <a-radio-group v-model:value="auditForm.auditStatus">
                <a-radio :value="1">通过</a-radio>
                <a-radio :value="2">拒绝</a-radio>
              </a-radio-group>
            </a-form-item>
            <a-form-item v-if="auditForm.auditStatus === 2" label="拒绝理由" required>
              <a-select v-model:value="quickReason" style="width: 100%; margin-bottom: 8px" allow-clear placeholder="快捷选择理由" @change="handleQuickReason">
                <a-select-option value="照片模糊无法辨认">照片模糊无法辨认</a-select-option>
                <a-select-option value="证件信息填写错误">证件信息填写错误</a-select-option>
                <a-select-option value="证件已过期">证件已过期</a-select-option>
                <a-select-option value="证件不完整">证件不完整</a-select-option>
              </a-select>
              <a-textarea
                v-model:value="auditForm.auditReason"
                :rows="3"
                placeholder="请输入拒绝理由"
              />
            </a-form-item>
          </a-form>
        </div>
        <div class="audit-actions">
          <a-button @click="showAuditModal = false">取消</a-button>
          <a-button type="primary" @click="submitAudit" :loading="auditSubmitting" :disabled="!canSubmitAudit">
            提交审核
          </a-button>
        </div>
      </div>
    </a-modal>

    <a-modal
      v-model:open="showDetailModal"
      title="证件详情"
      :footer="null"
      width="800px"
      :destroyOnClose="true"
    >
      <div v-if="detailDocument" class="detail-content">
        <div class="detail-header">
          <div class="detail-employee">
            <a-avatar size="large">
              {{ detailDocument.employeeName?.charAt(0) || 'U' }}
            </a-avatar>
            <div class="employee-info">
              <h4>{{ detailDocument.employeeName }}</h4>
              <p>{{ detailDocument.documentTypeName }}</p>
            </div>
          </div>
          <a-tag :color="getAuditStatusColor(detailDocument.auditStatus)">
            {{ getAuditStatusText(detailDocument.auditStatus) }}
          </a-tag>
        </div>
        <div class="detail-images">
          <div class="image-section">
            <h5>正面</h5>
            <img :src="getImageUrl(detailDocument.frontImage)" alt="正面" />
          </div>
          <div v-if="detailDocument.backImage" class="image-section">
            <h5>反面</h5>
            <img :src="getImageUrl(detailDocument.backImage)" alt="反面" />
          </div>
        </div>
        <div class="detail-info">
          <a-descriptions :column="2" size="small" bordered>
            <template v-if="detailDocument.typeCode === 'ID_CARD'">
              <a-descriptions-item label="证件号码">{{ detailDocument.documentNumber }}</a-descriptions-item>
              <a-descriptions-item label="签发机关">{{ detailDocument.issueAuthority }}</a-descriptions-item>
              <a-descriptions-item label="有效期">
                {{ detailDocument.isPermanent === 1 ? '长期有效' : formatDate(detailDocument.expiryDate) }}
              </a-descriptions-item>
              <a-descriptions-item label="上传时间">{{ formatDateTime(detailDocument.createdAt) }}</a-descriptions-item>
            </template>
            <template v-else-if="detailDocument.typeCode === 'EDUCATION'">
              <a-descriptions-item label="毕业院校">{{ detailDocument.school }}</a-descriptions-item>
              <a-descriptions-item label="专业">{{ detailDocument.major }}</a-descriptions-item>
              <a-descriptions-item label="毕业时间">{{ formatDate(detailDocument.graduationDate) }}</a-descriptions-item>
              <a-descriptions-item label="证书编号">{{ detailDocument.documentNumber }}</a-descriptions-item>
            </template>
            <template v-else-if="detailDocument.typeCode === 'PROFESSIONAL'">
              <a-descriptions-item label="证书名称">{{ detailDocument.certificateName }}</a-descriptions-item>
              <a-descriptions-item label="发证机关">{{ detailDocument.issueAuthority }}</a-descriptions-item>
              <a-descriptions-item label="获证时间">{{ formatDate(detailDocument.issueDate) }}</a-descriptions-item>
              <a-descriptions-item label="有效期">
                {{ detailDocument.isPermanent === 1 ? '长期有效' : (detailDocument.expiryDate ? formatDate(detailDocument.expiryDate) : '无') }}
              </a-descriptions-item>
            </template>
            <a-descriptions-item v-if="detailDocument.auditStatus === 2" label="拒绝理由" :span="2">
              {{ detailDocument.auditReason }}
            </a-descriptions-item>
          </a-descriptions>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, markRaw, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  FileTextOutlined,
  ClockCircleOutlined,
  ExclamationCircleOutlined,
  WarningOutlined,
  IdcardOutlined,
  UserOutlined,
  BookOutlined,
  TrophyOutlined
} from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import {
  getDocumentTypes,
  getPendingAuditList,
  getAdminDocumentDetail,
  auditDocument as apiAuditDocument,
  queryDocuments,
  getExpiryMonitor,
  getDocumentStatistics
} from '@/api/adminDocument'

const activeTab = ref('pending')
const loading = ref(false)
const documentTypes = ref([])
const statistics = ref({})

const searchKeyword = ref('')
const filterTypeId = ref(null)
const pendingList = ref([])
const pendingPagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

const expiryList = ref([])

const queryParams = reactive({
  employeeName: '',
  documentTypeId: null,
  auditStatus: null,
  expiryStatus: ''
})
const documentList = ref([])
const documentPagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

const showAuditModal = ref(false)
const showDetailModal = ref(false)
const auditDocument = ref(null)
const detailDocument = ref(null)
const auditSubmitting = ref(false)
const quickReason = ref('')

const auditForm = reactive({
  documentId: null,
  auditStatus: 1,
  auditReason: ''
})

const iconMap = {
  ID_CARD: markRaw(IdcardOutlined),
  EDUCATION: markRaw(BookOutlined),
  PROFESSIONAL: markRaw(TrophyOutlined)
}

const canSubmitAudit = computed(() => {
  if (auditForm.auditStatus === 2) {
    return !!auditForm.auditReason
  }
  return true
})

const pendingColumns = [
  { title: '员工姓名', dataIndex: 'employeeName', key: 'employeeName' },
  { title: '证件类型', dataIndex: 'documentTypeName', key: 'documentType' },
  { title: '提交时间', dataIndex: 'createdAt', key: 'createdAt',
    customRender: ({ text }) => formatDateTime(text)
  },
  { title: '操作', key: 'action', width: 120 }
]

const expiryColumns = [
  { title: '员工姓名', dataIndex: 'employeeName', key: 'employeeName' },
  { title: '证件类型', dataIndex: 'documentTypeName', key: 'documentTypeName' },
  { title: '有效期至', dataIndex: 'expiryDate', key: 'expiryDate',
    customRender: ({ text }) => formatDate(text)
  },
  { title: '到期状态', key: 'expiryStatus', width: 100 },
  { title: '剩余天数', key: 'daysRemaining', width: 150 }
]

const documentColumns = [
  { title: '员工姓名', dataIndex: 'employeeName', key: 'employeeName' },
  { title: '证件类型', dataIndex: 'documentTypeName', key: 'documentType' },
  { title: '审核状态', key: 'auditStatus', width: 100 },
  { title: '上传时间', dataIndex: 'createdAt', key: 'createdAt',
    customRender: ({ text }) => formatDateTime(text)
  },
  { title: '操作', key: 'action', width: 100 }
]

const getTypeIcon = (typeCode) => {
  return iconMap[typeCode] || FileTextOutlined
}

const getAuditStatusColor = (status) => {
  const colors = { 0: 'gold', 1: 'green', 2: 'red' }
  return colors[status] || 'default'
}

const getAuditStatusText = (status) => {
  const texts = { 0: '待审核', 1: '已通过', 2: '已拒绝' }
  return texts[status] || '未知'
}

const getProgressColor = (percent) => {
  if (percent >= 90) return '#52c41a'
  if (percent >= 70) return '#1890ff'
  if (percent >= 50) return '#faad14'
  return '#ff4d4f'
}

const formatDate = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD')
}

const formatDateTime = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const getImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  return url.startsWith('/') ? url : '/' + url
}

const fetchDocumentTypes = async () => {
  try {
    const res = await getDocumentTypes()
    documentTypes.value = res.data
  } catch (error) {
    console.error('获取证件类型失败:', error)
  }
}

const fetchStatistics = async () => {
  try {
    const res = await getDocumentStatistics()
    statistics.value = res.data
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

const fetchPendingList = async () => {
  loading.value = true
  try {
    const res = await getPendingAuditList({
      pageNum: pendingPagination.current,
      pageSize: pendingPagination.pageSize,
      documentTypeId: filterTypeId.value,
      employeeName: searchKeyword.value
    })
    pendingList.value = res.data.records
    pendingPagination.total = res.data.total
  } catch (error) {
    console.error('获取待审核列表失败:', error)
  } finally {
    loading.value = false
  }
}

const fetchExpiryList = async () => {
  try {
    const res = await getExpiryMonitor()
    expiryList.value = res.data
  } catch (error) {
    console.error('获取到期监控失败:', error)
  }
}

const fetchDocumentList = async () => {
  loading.value = true
  try {
    const res = await queryDocuments({
      pageNum: documentPagination.current,
      pageSize: documentPagination.pageSize,
      ...queryParams
    })
    documentList.value = res.data.records
    documentPagination.total = res.data.total
  } catch (error) {
    console.error('获取证件列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleTabChange = (key) => {
  if (key === 'pending') {
    fetchPendingList()
  } else if (key === 'monitor') {
    fetchExpiryList()
  } else if (key === 'query') {
    fetchDocumentList()
  }
}

const handlePendingTableChange = (pagination) => {
  pendingPagination.current = pagination.current
  pendingPagination.pageSize = pagination.pageSize
  fetchPendingList()
}

const handleDocumentTableChange = (pagination) => {
  documentPagination.current = pagination.current
  documentPagination.pageSize = pagination.pageSize
  fetchDocumentList()
}

const viewAuditDetail = async (record) => {
  try {
    const res = await getAdminDocumentDetail(record.id)
    auditDocument.value = res.data
    auditForm.documentId = record.id
    auditForm.auditStatus = 1
    auditForm.auditReason = ''
    quickReason.value = ''
    showAuditModal.value = true
  } catch (error) {
    console.error('获取证件详情失败:', error)
  }
}

const viewDocumentDetail = async (record) => {
  try {
    const res = await getAdminDocumentDetail(record.id)
    detailDocument.value = res.data
    showDetailModal.value = true
  } catch (error) {
    console.error('获取证件详情失败:', error)
  }
}

const handleQuickReason = (value) => {
  auditForm.auditReason = value
}

const submitAudit = async () => {
  if (auditForm.auditStatus === 2 && !auditForm.auditReason) {
    message.warning('请输入拒绝理由')
    return
  }
  
  auditSubmitting.value = true
  try {
    await apiAuditDocument(auditForm)
    message.success('审核完成')
    showAuditModal.value = false
    fetchPendingList()
    fetchStatistics()
  } catch (error) {
    console.error('审核失败:', error)
  } finally {
    auditSubmitting.value = false
  }
}

const previewImage = (url) => {
  window.open(getImageUrl(url), '_blank')
}

onMounted(() => {
  fetchDocumentTypes()
  fetchStatistics()
  fetchPendingList()
})
</script>

<style scoped>
.admin-documents-container {
}

.main-content {
  padding: 0;
}

.stat-card {
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
  font-size: 24px;
  color: white;
}

.stat-pending {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-expiring {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-expired {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.stat-idcard {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-info .stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.stat-info .stat-label {
  font-size: 13px;
  color: #999;
  margin: 4px 0 0;
}

.content-card {
  border-radius: 12px;
}

.tab-toolbar,
.query-toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.monitor-stats {
  margin-bottom: 16px;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
}

.monitor-stat {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.monitor-stat .stat-num {
  font-size: 24px;
  font-weight: 600;
}

.monitor-stat .stat-label {
  font-size: 14px;
  color: #666;
}

.text-danger {
  color: #ff4d4f !important;
}

.text-warning {
  color: #faad14 !important;
}

.type-icon {
  margin-right: 4px;
  color: #667eea;
}

.statistics-content {
  padding: 24px;
}

.stat-overview {
  margin-bottom: 32px;
}

.overview-item {
  display: flex;
  align-items: center;
  gap: 16px;
}

.overview-icon {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: white;
}

.overview-value {
  font-size: 32px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.overview-label {
  font-size: 14px;
  color: #999;
  margin: 4px 0 0;
}

.progress-section h4 {
  font-size: 16px;
  font-weight: 500;
  margin: 0 0 20px;
  color: #333;
}

.progress-item {
  margin-bottom: 24px;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 14px;
  color: #666;
}

.audit-content .audit-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.audit-employee {
  display: flex;
  align-items: center;
  gap: 12px;
}

.employee-info h4 {
  margin: 0 0 4px;
  font-size: 16px;
  font-weight: 500;
}

.employee-info p {
  margin: 0;
  font-size: 13px;
  color: #999;
}

.audit-body {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  margin-bottom: 24px;
}

.audit-images {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.image-section h5 {
  margin: 0 0 8px;
  font-size: 13px;
  font-weight: 500;
  color: #666;
}

.image-section img {
  width: 100%;
  max-height: 200px;
  object-fit: contain;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  cursor: pointer;
}

.audit-info h5 {
  margin: 0 0 12px;
  font-size: 13px;
  font-weight: 500;
  color: #666;
}

.audit-form {
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.audit-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.detail-content .detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.detail-employee {
  display: flex;
  align-items: center;
  gap: 12px;
}

.detail-images {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
}

.detail-images .image-section {
  flex: 1;
}

:deep(.ant-btn-primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: all 0.3s;
}

:deep(.ant-btn-primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}
</style>

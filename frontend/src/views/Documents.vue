<template>
  <div class="documents-container">
    <div class="main-content">
      <div v-if="expiryAlerts.length > 0" class="expiry-banner" :class="bannerClass">
        <ExclamationCircleOutlined class="banner-icon" />
        <span class="banner-text">
          您有 {{ expiryAlerts.length }} 个证件{{ hasExpired ? '已过期' : '即将到期' }}，请及时更新
        </span>
        <a-button type="link" class="banner-btn" @click="scrollToDocuments">立即处理</a-button>
      </div>

      <div class="toolbar">
        <div class="filter-group">
          <a-select v-model:value="statusFilter" style="width: 140px" @change="fetchDocuments">
            <a-select-option value="all">全部证件</a-select-option>
            <a-select-option value="valid">有效证件</a-select-option>
            <a-select-option value="expiring">即将到期</a-select-option>
            <a-select-option value="expired">已过期</a-select-option>
            <a-select-option value="permanent">长期有效</a-select-option>
          </a-select>
          <a-select v-model:value="typeFilter" style="width: 140px" @change="fetchDocuments">
            <a-select-option value="all">全部类型</a-select-option>
            <a-select-option v-for="type in documentTypes" :key="type.id" :value="type.id">
              {{ type.typeName }}
            </a-select-option>
          </a-select>
        </div>
        <a-button type="primary" @click="showUploadModal = true">
          <PlusOutlined /> 添加证件
        </a-button>
      </div>

      <div v-if="filteredDocuments.length === 0 && missingRequiredTypes.length === 0 && !loading" class="empty-state">
        <div class="empty-icon">
          <InboxOutlined />
        </div>
        <p class="empty-text">您还没有上传任何证件</p>
        <p class="empty-desc">点击下方按钮开始上传您的证件资料</p>
        <a-button type="primary" size="large" @click="showUploadModal = true">
          <PlusOutlined /> 上传证件
        </a-button>
      </div>

      <div v-else-if="filteredDocuments.length > 0 || missingRequiredTypes.length > 0" class="documents-grid">
        <div
          v-for="doc in filteredDocuments"
          :key="doc.id"
          class="document-card"
          :class="{ 'card-expired': getExpiryStatus(doc) === 'expired' }"
          @click="viewDocument(doc)"
        >
          <div class="card-header">
            <div class="card-icon" :class="getTypeIconClass(doc.typeCode)">
              <component :is="getTypeIcon(doc.typeCode)" />
            </div>
            <div class="card-status">
              <a-tag :color="getAuditStatusColor(doc.auditStatus)">
                {{ getAuditStatusText(doc.auditStatus) }}
              </a-tag>
            </div>
          </div>
          <div class="card-body">
            <h3 class="card-title">{{ doc.documentTypeName }}</h3>
            <div class="card-thumbnail">
              <img v-if="doc.frontImage" :src="getImageUrl(doc.frontImage)" alt="证件缩略图" />
              <div v-else class="thumbnail-placeholder">
                <FileImageOutlined />
              </div>
            </div>
            <div class="card-info">
              <p class="info-row">
                <span class="info-label">上传时间：</span>
                <span class="info-value">{{ formatDate(doc.createdAt) }}</span>
              </p>
              <p class="info-row expiry-row" :class="getExpiryStatusClass(doc)">
                <span class="info-label">有效期：</span>
                <span class="info-value">{{ getExpiryText(doc) }}</span>
              </p>
            </div>
          </div>
          <div class="card-footer">
            <a-tag :color="getExpiryColor(doc)" size="small">
              {{ getExpiryStatusText(doc) }}
            </a-tag>
          </div>
        </div>

        <div
          v-for="type in missingRequiredTypes"
          :key="'missing-' + type.id"
          class="document-card card-placeholder"
          @click="startUploadForType(type)"
        >
          <div class="card-header">
            <div class="card-icon" :class="getTypeIconClass(type.typeCode)">
              <component :is="getTypeIcon(type.typeCode)" />
            </div>
            <div class="card-status">
              <a-tag color="default">未上传</a-tag>
            </div>
          </div>
          <div class="card-body">
            <h3 class="card-title">{{ type.typeName }}</h3>
            <div class="card-thumbnail placeholder-thumb">
              <PlusOutlined class="placeholder-icon" />
            </div>
            <div class="card-info">
              <p class="info-row placeholder-hint">
                <span class="info-value">请上传{{ type.typeName }}</span>
              </p>
            </div>
          </div>
          <div class="card-footer">
            <a-tag color="default" size="small">必备</a-tag>
          </div>
        </div>
      </div>
    </div>

    <a-modal
      v-model:open="showUploadModal"
      title="上传证件"
      :footer="null"
      :closable="true"
      width="720px"
      @cancel="resetUploadForm"
    >
      <a-steps :current="currentStep" class="upload-steps">
        <a-step title="选择证件类型" />
        <a-step title="上传证件图片" />
        <a-step title="填写证件信息" />
        <a-step title="确认提交" />
      </a-steps>

      <div class="step-content">
        <div v-if="currentStep === 0" class="type-selector">
          <div
            v-for="type in documentTypes"
            :key="type.id"
            class="type-card"
            :class="{ selected: selectedType?.id === type.id }"
            @click="selectType(type)"
          >
            <div class="type-icon" :class="getTypeIconClass(type.typeCode)">
              <component :is="getTypeIcon(type.typeCode)" />
            </div>
            <div class="type-info">
              <h4 class="type-name">
                {{ type.typeName }}
                <a-tag v-if="type.isRequired === 1" color="red" size="small">必备</a-tag>
                <a-tag v-else color="default" size="small">选填</a-tag>
              </h4>
              <p class="type-desc">
                {{ getTypeDescription(type.typeCode) }}
              </p>
            </div>
            <div class="type-action">
              <EyeOutlined @click.stop="showExample(type)" class="example-link" />
            </div>
          </div>
        </div>

        <div v-else-if="currentStep === 1" class="upload-area">
          <div v-if="selectedType?.requireBothSides === 1" class="double-upload">
            <div class="upload-item">
              <h4>正面照片</h4>
              <a-upload-dragger
                :show-upload-list="false"
                :before-upload="(file) => beforeUpload(file, 'front')"
                :custom-request="(options) => handleUpload(options, 'front')"
                accept=".jpg,.jpeg,.png,.pdf"
              >
                <div v-if="!formData.frontImage" class="upload-placeholder">
                  <InboxOutlined class="upload-icon" />
                  <p class="upload-text">点击或拖拽文件到此区域上传</p>
                  <p class="upload-hint">支持 JPG、PNG、PDF，不超过 5MB</p>
                </div>
                <img v-else :src="getImageUrl(formData.frontImage)" class="upload-preview" alt="正面预览" />
              </a-upload-dragger>
            </div>
            <div class="upload-item">
              <h4>反面照片</h4>
              <a-upload-dragger
                :show-upload-list="false"
                :before-upload="(file) => beforeUpload(file, 'back')"
                :custom-request="(options) => handleUpload(options, 'back')"
                accept=".jpg,.jpeg,.png,.pdf"
              >
                <div v-if="!formData.backImage" class="upload-placeholder">
                  <InboxOutlined class="upload-icon" />
                  <p class="upload-text">点击或拖拽文件到此区域上传</p>
                  <p class="upload-hint">支持 JPG、PNG、PDF，不超过 5MB</p>
                </div>
                <img v-else :src="getImageUrl(formData.backImage)" class="upload-preview" alt="反面预览" />
              </a-upload-dragger>
            </div>
          </div>
          <div v-else class="single-upload">
            <a-upload-dragger
              :show-upload-list="false"
              :before-upload="(file) => beforeUpload(file, 'front')"
              :custom-request="(options) => handleUpload(options, 'front')"
              accept=".jpg,.jpeg,.png,.pdf"
            >
              <div v-if="!formData.frontImage" class="upload-placeholder">
                <InboxOutlined class="upload-icon" />
                <p class="upload-text">点击或拖拽文件到此区域上传</p>
                <p class="upload-hint">支持 JPG、PNG、PDF，不超过 5MB</p>
              </div>
              <img v-else :src="getImageUrl(formData.frontImage)" class="upload-preview" alt="证件预览" />
            </a-upload-dragger>
          </div>
        </div>

        <div v-else-if="currentStep === 2" class="form-area">
          <a-form :model="formData" layout="vertical">
            <template v-if="selectedType?.typeCode === 'ID_CARD'">
              <a-form-item label="证件号码" required>
                <a-input
                  v-model:value="formData.documentNumber"
                  placeholder="请输入18位身份证号码"
                  maxlength="18"
                />
                <p class="form-hint">请输入18位身份证号码</p>
              </a-form-item>
              <a-form-item label="签发机关" required>
                <a-input v-model:value="formData.issueAuthority" placeholder="请输入签发机关" />
              </a-form-item>
              <a-form-item label="有效期" required>
                <a-date-picker
                  v-model:value="formData.expiryDate"
                  :disabled="formData.isPermanent === 1"
                  style="width: 100%"
                  placeholder="请选择有效期至"
                />
                <a-checkbox v-model:checked="isPermanentChecked" @change="togglePermanent">
                  长期有效
                </a-checkbox>
              </a-form-item>
            </template>

            <template v-else-if="selectedType?.typeCode === 'EDUCATION'">
              <a-form-item label="毕业院校" required>
                <a-input v-model:value="formData.school" placeholder="请输入毕业院校" />
              </a-form-item>
              <a-form-item label="专业" required>
                <a-input v-model:value="formData.major" placeholder="请输入专业名称" />
              </a-form-item>
              <a-form-item label="毕业时间" required>
                <a-date-picker
                  v-model:value="formData.graduationDate"
                  style="width: 100%"
                  placeholder="请选择毕业时间"
                  picker="month"
                />
              </a-form-item>
              <a-form-item label="证书编号" required>
                <a-input v-model:value="formData.documentNumber" placeholder="请输入证书编号" />
              </a-form-item>
            </template>

            <template v-else-if="selectedType?.typeCode === 'PROFESSIONAL'">
              <a-form-item label="证书名称" required>
                <a-input v-model:value="formData.certificateName" placeholder="请输入证书名称" />
              </a-form-item>
              <a-form-item label="发证机关" required>
                <a-input v-model:value="formData.issueAuthority" placeholder="请输入发证机关" />
              </a-form-item>
              <a-form-item label="获证时间" required>
                <a-date-picker
                  v-model:value="formData.issueDate"
                  style="width: 100%"
                  placeholder="请选择获证时间"
                />
              </a-form-item>
              <a-form-item label="有效期">
                <a-date-picker
                  v-model:value="formData.expiryDate"
                  :disabled="formData.isPermanent === 1"
                  style="width: 100%"
                  placeholder="请选择有效期至（可选）"
                />
                <a-checkbox v-model:checked="isPermanentChecked" @change="togglePermanent">
                  长期有效
                </a-checkbox>
              </a-form-item>
            </template>
          </a-form>
        </div>

        <div v-else-if="currentStep === 3" class="confirm-area">
          <div class="confirm-card">
            <div class="confirm-header">
              <div class="confirm-icon" :class="getTypeIconClass(selectedType?.typeCode)">
                <component :is="getTypeIcon(selectedType?.typeCode)" />
              </div>
              <h3>{{ selectedType?.typeName }}</h3>
            </div>
            <div class="confirm-images">
              <div class="confirm-image">
                <img :src="getImageUrl(formData.frontImage)" alt="证件预览" />
              </div>
              <div v-if="selectedType?.requireBothSides === 1" class="confirm-image">
                <img :src="getImageUrl(formData.backImage)" alt="证件预览" />
              </div>
            </div>
            <div class="confirm-info">
              <template v-if="selectedType?.typeCode === 'ID_CARD'">
                <p><span>证件号码：</span>{{ formData.documentNumber }}</p>
                <p><span>签发机关：</span>{{ formData.issueAuthority }}</p>
                <p><span>有效期：</span>{{ formData.isPermanent === 1 ? '长期有效' : formatDate(formData.expiryDate) }}</p>
              </template>
              <template v-else-if="selectedType?.typeCode === 'EDUCATION'">
                <p><span>毕业院校：</span>{{ formData.school }}</p>
                <p><span>专业：</span>{{ formData.major }}</p>
                <p><span>毕业时间：</span>{{ formatDate(formData.graduationDate) }}</p>
                <p><span>证书编号：</span>{{ formData.documentNumber }}</p>
              </template>
              <template v-else-if="selectedType?.typeCode === 'PROFESSIONAL'">
                <p><span>证书名称：</span>{{ formData.certificateName }}</p>
                <p><span>发证机关：</span>{{ formData.issueAuthority }}</p>
                <p><span>获证时间：</span>{{ formatDate(formData.issueDate) }}</p>
                <p><span>有效期：</span>{{ formData.isPermanent === 1 ? '长期有效' : (formData.expiryDate ? formatDate(formData.expiryDate) : '无') }}</p>
              </template>
            </div>
          </div>
        </div>
      </div>

      <div class="step-actions">
        <a-button v-if="currentStep > 0" @click="prevStep">上一步</a-button>
        <a-button v-if="currentStep < 3" type="primary" @click="nextStep" :disabled="!canProceed">
          下一步
        </a-button>
        <a-button v-if="currentStep === 3" type="primary" @click="submitDocument" :loading="submitting">
          提交审核
        </a-button>
      </div>
    </a-modal>

    <a-modal
      v-model:open="showDetailModal"
      title="证件详情"
      :footer="null"
      width="800px"
    >
      <div v-if="selectedDocument" class="detail-content">
        <div class="detail-header">
          <div class="detail-icon" :class="getTypeIconClass(selectedDocument.typeCode)">
            <component :is="getTypeIcon(selectedDocument.typeCode)" />
          </div>
          <div class="detail-title">
            <h3>{{ selectedDocument.documentTypeName }}</h3>
            <a-tag :color="getAuditStatusColor(selectedDocument.auditStatus)">
              {{ getAuditStatusText(selectedDocument.auditStatus) }}
            </a-tag>
          </div>
        </div>

        <a-tabs v-model:activeKey="detailTab">
          <a-tab-pane key="info" tab="证件信息">
            <div class="detail-images">
              <div class="detail-image">
                <img :src="getImageUrl(selectedDocument.frontImage)" alt="正面" @click="previewImage(selectedDocument.frontImage)" />
              </div>
              <div v-if="selectedDocument.backImage" class="detail-image">
                <img :src="getImageUrl(selectedDocument.backImage)" alt="反面" @click="previewImage(selectedDocument.backImage)" />
              </div>
            </div>
            <div class="detail-info">
              <template v-if="selectedDocument.typeCode === 'ID_CARD'">
                <a-descriptions :column="2" bordered>
                  <a-descriptions-item label="证件号码">{{ selectedDocument.documentNumber }}</a-descriptions-item>
                  <a-descriptions-item label="签发机关">{{ selectedDocument.issueAuthority }}</a-descriptions-item>
                  <a-descriptions-item label="有效期">
                    {{ selectedDocument.isPermanent === 1 ? '长期有效' : formatDate(selectedDocument.expiryDate) }}
                  </a-descriptions-item>
                  <a-descriptions-item label="上传时间">{{ formatDateTime(selectedDocument.createdAt) }}</a-descriptions-item>
                </a-descriptions>
              </template>
              <template v-else-if="selectedDocument.typeCode === 'EDUCATION'">
                <a-descriptions :column="2" bordered>
                  <a-descriptions-item label="毕业院校">{{ selectedDocument.school }}</a-descriptions-item>
                  <a-descriptions-item label="专业">{{ selectedDocument.major }}</a-descriptions-item>
                  <a-descriptions-item label="毕业时间">{{ formatDate(selectedDocument.graduationDate) }}</a-descriptions-item>
                  <a-descriptions-item label="证书编号">{{ selectedDocument.documentNumber }}</a-descriptions-item>
                  <a-descriptions-item label="上传时间" :span="2">{{ formatDateTime(selectedDocument.createdAt) }}</a-descriptions-item>
                </a-descriptions>
              </template>
              <template v-else-if="selectedDocument.typeCode === 'PROFESSIONAL'">
                <a-descriptions :column="2" bordered>
                  <a-descriptions-item label="证书名称">{{ selectedDocument.certificateName }}</a-descriptions-item>
                  <a-descriptions-item label="发证机关">{{ selectedDocument.issueAuthority }}</a-descriptions-item>
                  <a-descriptions-item label="获证时间">{{ formatDate(selectedDocument.issueDate) }}</a-descriptions-item>
                  <a-descriptions-item label="有效期">
                    {{ selectedDocument.isPermanent === 1 ? '长期有效' : (selectedDocument.expiryDate ? formatDate(selectedDocument.expiryDate) : '无') }}
                  </a-descriptions-item>
                  <a-descriptions-item label="上传时间" :span="2">{{ formatDateTime(selectedDocument.createdAt) }}</a-descriptions-item>
                </a-descriptions>
              </template>
            </div>
            <div v-if="selectedDocument.auditStatus === 2" class="reject-reason">
              <a-alert message="审核拒绝" :description="selectedDocument.auditReason" type="error" show-icon />
            </div>
          </a-tab-pane>
          <a-tab-pane key="history" tab="历史版本">
            <div v-if="documentHistory.length === 0" class="no-history">
              <FileTextOutlined />
              <p>暂无历史版本</p>
            </div>
            <div v-else class="history-list">
              <div v-for="(item, index) in documentHistory" :key="item.id" class="history-item">
                <div class="history-version">v{{ item.version }}</div>
                <div class="history-info">
                  <p class="history-date">{{ formatDateTime(item.replacedAt) }}</p>
                  <p class="history-status">
                    状态：<a-tag :color="getAuditStatusColor(item.auditStatus)" size="small">
                      {{ getAuditStatusText(item.auditStatus) }}
                    </a-tag>
                  </p>
                </div>
              </div>
            </div>
          </a-tab-pane>
        </a-tabs>

        <div class="detail-actions">
          <a-button v-if="selectedDocument.auditStatus === 0" @click="withdrawDocument">
            撤回
          </a-button>
          <a-button type="primary" @click="renewDocument">
            更新证件
          </a-button>
        </div>
      </div>
    </a-modal>

    <a-modal
      v-model:open="showExampleModal"
      title="上传示例"
      :footer="null"
      width="600px"
    >
      <div class="example-content">
        <div class="example-image">
          <img :src="getExampleImage()" alt="示例图片" />
        </div>
        <div class="example-tips">
          <h4>拍摄要求：</h4>
          <ul>
            <li v-for="(tip, index) in getExampleTips()" :key="index">{{ tip }}</li>
          </ul>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, markRaw, toRaw } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  FileTextOutlined,
  PlusOutlined,
  InboxOutlined,
  ExclamationCircleOutlined,
  EyeOutlined,
  FileImageOutlined,
  IdcardOutlined,
  BookOutlined,
  TrophyOutlined
} from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import {
  getDocumentTypes,
  getMyDocuments,
  getDocumentDetail,
  uploadDocument,
  withdrawDocument as apiWithdrawDocument,
  getDocumentHistory,
  getExpiryAlerts,
  uploadDocumentFile
} from '@/api/document'

const loading = ref(false)
const documents = ref([])
const documentTypes = ref([])
const expiryAlerts = ref([])

const statusFilter = ref('all')
const typeFilter = ref('all')

const showUploadModal = ref(false)
const showDetailModal = ref(false)
const showExampleModal = ref(false)
const currentStep = ref(0)
const selectedType = ref(null)
const selectedDocument = ref(null)
const exampleType = ref(null)
const documentHistory = ref([])
const detailTab = ref('info')
const submitting = ref(false)
const isPermanentChecked = ref(false)

const formData = reactive({
  documentTypeId: null,
  documentNumber: '',
  frontImage: '',
  backImage: '',
  issueAuthority: '',
  issueDate: null,
  expiryDate: null,
  isPermanent: 0,
  school: '',
  major: '',
  graduationDate: null,
  certificateName: ''
})

const iconMap = {
  ID_CARD: markRaw(IdcardOutlined),
  EDUCATION: markRaw(BookOutlined),
  PROFESSIONAL: markRaw(TrophyOutlined)
}

const hasExpired = computed(() => {
  return expiryAlerts.value.some(alert => alert.expiryStatus === 'expired')
})

const bannerClass = computed(() => {
  return hasExpired.value ? 'banner-expired' : 'banner-expiring'
})

const filteredDocuments = computed(() => {
  let result = documents.value
  
  if (statusFilter.value !== 'all') {
    result = result.filter(doc => getExpiryStatus(doc) === statusFilter.value)
  }
  
  if (typeFilter.value !== 'all') {
    result = result.filter(doc => doc.documentTypeId === typeFilter.value)
  }
  
  return result
})

const missingRequiredTypes = computed(() => {
  const uploadedTypeIds = documents.value.map(doc => doc.documentTypeId)
  return documentTypes.value.filter(type => type.isRequired === 1 && !uploadedTypeIds.includes(type.id))
})

const canProceed = computed(() => {
  if (currentStep.value === 0) {
    return selectedType.value !== null
  } else if (currentStep.value === 1) {
    if (selectedType.value?.requireBothSides === 1) {
      return formData.frontImage && formData.backImage
    }
    return !!formData.frontImage
  } else if (currentStep.value === 2) {
    return validateForm()
  }
  return true
})

const fetchDocumentTypes = async () => {
  try {
    const res = await getDocumentTypes()
    documentTypes.value = res.data
  } catch (error) {
    console.error('获取证件类型失败:', error)
  }
}

const fetchDocuments = async () => {
  loading.value = true
  try {
    const res = await getMyDocuments()
    documents.value = res.data
  } catch (error) {
    console.error('获取证件列表失败:', error)
  } finally {
    loading.value = false
  }
}

const fetchExpiryAlerts = async () => {
  try {
    const res = await getExpiryAlerts()
    expiryAlerts.value = res.data
  } catch (error) {
    console.error('获取到期提醒失败:', error)
  }
}

const getTypeIcon = (typeCode) => {
  return iconMap[typeCode] || FileTextOutlined
}

const getTypeIconClass = (typeCode) => {
  const classes = {
    ID_CARD: 'icon-idcard',
    EDUCATION: 'icon-education',
    PROFESSIONAL: 'icon-professional'
  }
  return classes[typeCode] || 'icon-default'
}

const getTypeDescription = (typeCode) => {
  const descriptions = {
    ID_CARD: '请上传身份证正反面照片',
    EDUCATION: '请上传学历证书照片',
    PROFESSIONAL: '请上传职业资格证书照片'
  }
  return descriptions[typeCode] || ''
}

const getAuditStatusColor = (status) => {
  const colors = {
    0: 'gold',
    1: 'green',
    2: 'red'
  }
  return colors[status] || 'default'
}

const getAuditStatusText = (status) => {
  const texts = {
    0: '待审核',
    1: '已认证',
    2: '已拒绝'
  }
  return texts[status] || '未知'
}

const getExpiryStatus = (doc) => {
  if (doc.isPermanent === 1 || !doc.expiryDate) {
    return 'permanent'
  }
  
  const today = dayjs()
  const expiry = dayjs(doc.expiryDate)
  const days = expiry.diff(today, 'day')
  
  if (days < 0) {
    return 'expired'
  } else if (days <= 30) {
    return 'expiring'
  } else {
    return 'valid'
  }
}

const getExpiryStatusText = (doc) => {
  const status = getExpiryStatus(doc)
  const texts = {
    valid: '有效',
    expiring: '即将到期',
    expired: '已过期',
    permanent: '长期有效'
  }
  return texts[status] || '未知'
}

const getExpiryColor = (doc) => {
  const status = getExpiryStatus(doc)
  const colors = {
    valid: 'green',
    expiring: 'gold',
    expired: 'red',
    permanent: 'default'
  }
  return colors[status] || 'default'
}

const getExpiryStatusClass = (doc) => {
  const status = getExpiryStatus(doc)
  return `expiry-${status}`
}

const getExpiryText = (doc) => {
  if (doc.isPermanent === 1 || !doc.expiryDate) {
    return '长期有效'
  }
  
  const today = dayjs()
  const expiry = dayjs(doc.expiryDate)
  const days = expiry.diff(today, 'day')
  
  if (days < 0) {
    return `已过期 ${Math.abs(days)} 天`
  } else if (days <= 30) {
    return `还剩 ${days} 天到期`
  } else {
    return `至 ${formatDate(doc.expiryDate)}`
  }
}

const formatDate = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD')
}

const formatDateTime = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

const getAvatarUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  if (url.startsWith('/')) {
    return url
  }
  return '/' + url
}

const getImageUrl = (url) => {
  return getAvatarUrl(url)
}

const selectType = (type) => {
  selectedType.value = type
  formData.documentTypeId = type.id
}

const showExample = (type) => {
  exampleType.value = type
  showExampleModal.value = true
}

const getExampleImage = () => {
  const images = {
    ID_CARD: 'https://images.unsplash.com/photo-1544099858-75feeb57f01b?w=400&h=250&fit=crop',
    EDUCATION: 'https://images.unsplash.com/photo-1523050854058-8df90110c9f1?w=400&h=250&fit=crop',
    PROFESSIONAL: 'https://images.unsplash.com/photo-1434030216411-0b793f4b4173?w=400&h=250&fit=crop'
  }
  return images[exampleType.value?.typeCode] || ''
}

const getExampleTips = () => {
  const tips = {
    ID_CARD: [
      '请将身份证平放在纯色背景上拍摄',
      '确保四角完整、文字清晰、无反光',
      '请上传身份证的正反面照片'
    ],
    EDUCATION: [
      '请拍摄证书全貌',
      '确保姓名、学校、专业、毕业时间等信息清晰可见',
      '避免反光和模糊'
    ],
    PROFESSIONAL: [
      '请拍摄证书的关键信息区域',
      '确保证书名称、发证机关、获证时间清晰',
      '保持图片水平端正'
    ]
  }
  return tips[exampleType.value?.typeCode] || []
}

const beforeUpload = (file) => {
  const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'application/pdf']
  const isAllowedType = allowedTypes.includes(file.type) || 
    /\.(jpg|jpeg|png|pdf)$/i.test(file.name)
  
  if (!isAllowedType) {
    message.error('仅支持 JPG、PNG 或 PDF 格式的文件')
    return false
  }
  
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    message.error('文件大小不能超过 5MB')
    return false
  }
  
  return true
}

const handleUpload = async (options, side) => {
  const { file, onSuccess, onError } = options
  try {
    const res = await uploadDocumentFile(file)
    if (side === 'front') {
      formData.frontImage = res.data
    } else {
      formData.backImage = res.data
    }
    message.success('上传成功')
    onSuccess && onSuccess(res.data)
  } catch (error) {
    message.error('上传失败')
    onError && onError(error)
  }
}

const togglePermanent = (e) => {
  isPermanentChecked.value = e.target.checked
  formData.isPermanent = e.target.checked ? 1 : 0
  if (e.target.checked) {
    formData.expiryDate = null
  }
}

const validateForm = () => {
  const type = selectedType.value?.typeCode
  if (type === 'ID_CARD') {
    return formData.documentNumber && formData.issueAuthority && 
      (formData.isPermanent === 1 || formData.expiryDate)
  } else if (type === 'EDUCATION') {
    return formData.school && formData.major && 
      formData.graduationDate && formData.documentNumber
  } else if (type === 'PROFESSIONAL') {
    return formData.certificateName && formData.issueAuthority && formData.issueDate
  }
  return true
}

const nextStep = () => {
  if (currentStep.value < 3) {
    currentStep.value++
  }
}

const prevStep = () => {
  if (currentStep.value > 0) {
    currentStep.value--
  }
}

const resetUploadForm = () => {
  currentStep.value = 0
  selectedType.value = null
  isPermanentChecked.value = false
  Object.assign(formData, {
    documentTypeId: null,
    documentNumber: '',
    frontImage: '',
    backImage: '',
    issueAuthority: '',
    issueDate: null,
    expiryDate: null,
    isPermanent: 0,
    school: '',
    major: '',
    graduationDate: null,
    certificateName: ''
  })
}

const formatDateValue = (value) => {
  if (!value) return null
  const raw = toRaw(value)
  if (raw && typeof raw.format === 'function') {
    return raw.format('YYYY-MM-DD')
  }
  if (typeof raw === 'string') {
    return raw
  }
  return null
}

const submitDocument = async () => {
  submitting.value = true
  try {
    const submitData = { ...toRaw(formData) }
    submitData.issueDate = formatDateValue(submitData.issueDate)
    submitData.expiryDate = formatDateValue(submitData.expiryDate)
    submitData.graduationDate = formatDateValue(submitData.graduationDate)
    if (selectedType.value?.typeCode === 'EDUCATION') {
      delete submitData.expiryDate
      delete submitData.isPermanent
    }
    await uploadDocument(submitData)
    message.success('证件上传成功，等待审核')
    showUploadModal.value = false
    resetUploadForm()
    fetchDocuments()
    fetchExpiryAlerts()
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitting.value = false
  }
}

const viewDocument = async (doc) => {
  try {
    const res = await getDocumentDetail(doc.id)
    selectedDocument.value = res.data
    showDetailModal.value = true
  } catch (error) {
    console.error('获取证件详情失败:', error)
  }
}

const withdrawDocument = () => {
  Modal.confirm({
    title: '确认撤回',
    content: '确定要撤回该证件吗？撤回后可以重新上传。',
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      try {
        await apiWithdrawDocument(selectedDocument.value.id)
        message.success('撤回成功')
        showDetailModal.value = false
        fetchDocuments()
      } catch (error) {
        console.error('撤回失败:', error)
      }
    }
  })
}

const renewDocument = () => {
  showDetailModal.value = false
  selectedType.value = documentTypes.value.find(t => t.id === selectedDocument.value.documentTypeId)
  formData.documentTypeId = selectedType.value.id
  showUploadModal.value = true
  currentStep.value = 1
}

const startUploadForType = (type) => {
  selectedType.value = type
  formData.documentTypeId = type.id
  showUploadModal.value = true
  currentStep.value = 1
}

const scrollToDocuments = () => {
  document.querySelector('.main-content')?.scrollIntoView({ behavior: 'smooth' })
}

const previewImage = (url) => {
  window.open(getImageUrl(url), '_blank')
}

onMounted(() => {
  fetchDocumentTypes()
  fetchDocuments()
  fetchExpiryAlerts()
})
</script>

<style scoped>
.main-content {
  padding: 0;
}

.expiry-banner {
  padding: 16px 24px;
  border-radius: 8px;
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.banner-expiring {
  background: #fffbe6;
  border: 1px solid #ffe58f;
}

.banner-expired {
  background: #fff2f0;
  border: 1px solid #ffccc7;
}

.banner-icon {
  font-size: 24px;
  color: #faad14;
}

.banner-expired .banner-icon {
  color: #ff4d4f;
}

.banner-text {
  flex: 1;
  font-size: 14px;
}

.banner-btn {
  font-size: 14px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.filter-group {
  display: flex;
  gap: 12px;
}

.empty-state {
  text-align: center;
  padding: 80px 40px;
  background: white;
  border-radius: 12px;
}

.empty-icon {
  font-size: 64px;
  color: #d9d9d9;
  margin-bottom: 16px;
}

.empty-text {
  font-size: 18px;
  color: #666;
  margin-bottom: 8px;
}

.empty-desc {
  font-size: 14px;
  color: #999;
  margin-bottom: 24px;
}

.documents-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.document-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  cursor: pointer;
  transition: all 0.3s;
  border: 2px solid transparent;
}

.document-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.card-expired {
  border-color: #ff4d4f;
}

.card-placeholder {
  border: 2px dashed #d9d9d9;
  background: #fafafa;
  opacity: 0.75;
}

.card-placeholder:hover {
  opacity: 1;
  border-color: #667eea;
}

.placeholder-thumb {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f0f0;
}

.placeholder-icon {
  font-size: 36px;
  color: #bbb;
}

.placeholder-hint .info-value {
  color: #999;
  font-size: 13px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 16px 12px;
}

.card-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.icon-idcard {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.icon-education {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.icon-professional {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.card-body {
  padding: 0 16px 16px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 12px;
}

.card-thumbnail {
  width: 100%;
  height: 120px;
  background: #f5f5f5;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 12px;
}

.card-thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.thumbnail-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #d9d9d9;
  font-size: 32px;
}

.card-info {
  font-size: 13px;
}

.info-row {
  display: flex;
  margin-bottom: 4px;
}

.info-label {
  color: #999;
}

.info-value {
  color: #666;
  flex: 1;
}

.expiry-row.expiry-expired .info-value,
.expiry-row.expiry-expiring .info-value {
  color: #ff4d4f;
  font-weight: 500;
}

.card-footer {
  padding: 12px 16px;
  border-top: 1px solid #f0f0f0;
}

.upload-steps {
  margin-bottom: 32px;
}

.step-content {
  min-height: 320px;
}

.type-selector {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.type-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border: 2px solid #f0f0f0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.type-card:hover {
  border-color: #667eea;
  background: #fafaff;
}

.type-card.selected {
  border-color: #667eea;
  background: #f0f4ff;
}

.type-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: white;
  flex-shrink: 0;
}

.type-info {
  flex: 1;
}

.type-name {
  font-size: 15px;
  font-weight: 500;
  margin: 0 0 4px;
}

.type-desc {
  font-size: 13px;
  color: #999;
  margin: 0;
}

.type-action {
  flex-shrink: 0;
}

.example-link {
  font-size: 18px;
  color: #667eea;
  cursor: pointer;
}

.double-upload {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.upload-item h4 {
  margin: 0 0 12px;
  font-size: 14px;
  font-weight: 500;
}

.upload-placeholder {
  text-align: center;
  padding: 40px 20px;
}

.upload-icon {
  font-size: 48px;
  color: #d9d9d9;
  margin-bottom: 16px;
}

.upload-text {
  font-size: 16px;
  color: #666;
  margin: 0 0 8px;
}

.upload-hint {
  font-size: 12px;
  color: #999;
  margin: 0;
}

.upload-preview {
  width: 100%;
  height: 200px;
  object-fit: contain;
  padding: 20px;
}

.form-area {
  max-width: 500px;
  margin: 0 auto;
}

.form-hint {
  font-size: 12px;
  color: #999;
  margin: 4px 0 0;
}

.confirm-card {
  background: #fafafa;
  border-radius: 12px;
  padding: 24px;
}

.confirm-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.confirm-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: white;
}

.confirm-header h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.confirm-images {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
}

.confirm-image {
  flex: 1;
  height: 160px;
  background: white;
  border-radius: 8px;
  overflow: hidden;
}

.confirm-image img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.confirm-info p {
  display: flex;
  margin: 8px 0;
  font-size: 14px;
}

.confirm-info span:first-child {
  color: #999;
  min-width: 80px;
}

.step-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.detail-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: white;
}

.detail-title h3 {
  margin: 0 0 8px;
  font-size: 20px;
  font-weight: 600;
}

.detail-images {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
}

.detail-image {
  flex: 1;
  height: 200px;
  background: #f5f5f5;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
}

.detail-image img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.reject-reason {
  margin-top: 24px;
}

.no-history {
  text-align: center;
  padding: 40px;
  color: #999;
}

.no-history .anticon {
  font-size: 48px;
  margin-bottom: 16px;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.history-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
}

.history-version {
  width: 56px;
  height: 56px;
  background: #667eea;
  color: white;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 16px;
}

.history-info p {
  margin: 4px 0;
}

.history-date {
  color: #666;
  font-size: 13px;
}

.history-status {
  font-size: 13px;
}

.detail-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}

.example-content {
  text-align: center;
}

.example-image {
  margin-bottom: 24px;
}

.example-image img {
  max-width: 100%;
  border-radius: 8px;
}

.example-tips {
  text-align: left;
}

.example-tips h4 {
  margin: 0 0 12px;
  font-size: 15px;
  font-weight: 500;
}

.example-tips ul {
  margin: 0;
  padding-left: 20px;
}

.example-tips li {
  margin-bottom: 8px;
  color: #666;
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

<template>
  <div class="admin-announcements-container">
    <div class="main-content">
      <a-row :gutter="[16, 16]">
        <a-col :span="6">
          <a-card class="stat-card" :bordered="false">
            <div class="stat-icon stat-published">
              <FileTextOutlined />
            </div>
            <div class="stat-info">
              <p class="stat-value">{{ statistics.publishedCount || 0 }}</p>
              <p class="stat-label">已发布信息</p>
            </div>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="stat-card" :bordered="false">
            <div class="stat-icon stat-important">
              <ExclamationCircleOutlined />
            </div>
            <div class="stat-info">
              <p class="stat-value">{{ statistics.importantCount || 0 }}</p>
              <p class="stat-label">紧急/关注</p>
            </div>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="stat-card" :bordered="false">
            <div class="stat-icon stat-top">
              <PushpinOutlined />
            </div>
            <div class="stat-info">
              <p class="stat-value">{{ statistics.topCount || 0 }}</p>
              <p class="stat-label">置顶信息</p>
            </div>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="stat-card" :bordered="false">
            <div class="stat-icon stat-draft">
              <EditOutlined />
            </div>
            <div class="stat-info">
              <p class="stat-value">{{ statistics.draftCount || 0 }}</p>
              <p class="stat-label">待发布</p>
            </div>
          </a-card>
        </a-col>
      </a-row>

      <a-card :bordered="false" class="content-card" style="margin-top: 16px">
        <a-tabs v-model:activeKey="activeTab">
          <a-tab-pane key="list" tab="信息列表">
            <div class="tab-toolbar">
              <a-space :size="12" wrap>
                <a-input-search
                  v-model:value="searchKeyword"
                  placeholder="搜索标题或内容"
                  style="width: 240px"
                  @search="fetchAnnouncementList"
                  allow-clear
                />
                <a-select v-model:value="filterCategoryId" style="width: 140px" @change="fetchAnnouncementList" allow-clear placeholder="选择类别">
                  <a-select-option v-for="cat in categories" :key="cat.id" :value="cat.id">
                    {{ cat.categoryName }}
                  </a-select-option>
                </a-select>
                <a-select v-model:value="filterImportance" style="width: 120px" @change="fetchAnnouncementList" allow-clear placeholder="重要程度">
                  <a-select-option :value="1">常规</a-select-option>
                  <a-select-option :value="2">关注</a-select-option>
                  <a-select-option :value="3">紧急</a-select-option>
                </a-select>
                <a-select v-model:value="filterPublishStatus" style="width: 120px" @change="fetchAnnouncementList" allow-clear placeholder="发布状态">
                  <a-select-option :value="0">待发布</a-select-option>
                  <a-select-option :value="1">已发布</a-select-option>
                  <a-select-option :value="2">已撤回</a-select-option>
                </a-select>
              </a-space>
              <a-space>
                <a-button @click="showCategoryModal = true">
                  <SettingOutlined /> 类别管理
                </a-button>
                <a-button type="primary" @click="openCreateModal">
                  <PlusOutlined /> 发布信息
                </a-button>
              </a-space>
            </div>
            <a-table
              :columns="announcementColumns"
              :data-source="announcementList"
              :loading="loading"
              :pagination="announcementPagination"
              @change="handleTableChange"
              row-key="id"
              size="middle"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'title'">
                  <div class="title-cell">
                    <PushpinOutlined v-if="record.isTop === 1" class="top-icon" />
                    <span class="title-text" @click="viewDetail(record)">{{ record.title }}</span>
                  </div>
                </template>
                <template v-else-if="column.key === 'importance'">
                  <a-tag :color="getImportanceColor(record.importance)">
                    {{ getImportanceText(record.importance) }}
                  </a-tag>
                </template>
                <template v-else-if="column.key === 'publishStatus'">
                  <a-tag :color="getPublishStatusColor(record.publishStatus)">
                    {{ getPublishStatusText(record.publishStatus) }}
                  </a-tag>
                </template>
                <template v-else-if="column.key === 'action'">
                  <a-space>
                    <a-button type="link" size="small" @click="viewStatistics(record)">
                      统计
                    </a-button>
                    <a-button type="link" size="small" @click="editAnnouncement(record)">
                      编辑
                    </a-button>
                    <a-button v-if="record.publishStatus === 1" type="link" size="small" @click="withdrawAnnouncement(record)">
                      撤回
                    </a-button>
                    <a-button type="link" size="small" danger @click="deleteAnnouncement(record)">
                      删除
                    </a-button>
                  </a-space>
                </template>
              </template>
            </a-table>
          </a-tab-pane>

          <a-tab-pane key="my" tab="我发布的">
            <div class="tab-toolbar">
              <a-button type="primary" @click="openCreateModal">
                <PlusOutlined /> 发布信息
              </a-button>
            </div>
            <a-table
              :columns="myAnnouncementColumns"
              :data-source="myAnnouncementList"
              :loading="loading"
              :pagination="myAnnouncementPagination"
              @change="handleMyTableChange"
              row-key="id"
              size="middle"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'importance'">
                  <a-tag :color="getImportanceColor(record.importance)">
                    {{ getImportanceText(record.importance) }}
                  </a-tag>
                </template>
                <template v-else-if="column.key === 'publishStatus'">
                  <a-tag :color="getPublishStatusColor(record.publishStatus)">
                    {{ getPublishStatusText(record.publishStatus) }}
                  </a-tag>
                </template>
                <template v-else-if="column.key === 'action'">
                  <a-space>
                    <a-button type="link" size="small" @click="viewStatistics(record)">
                      统计
                    </a-button>
                    <a-button type="link" size="small" @click="editAnnouncement(record)">
                      编辑
                    </a-button>
                    <a-button v-if="record.publishStatus === 1" type="link" size="small" @click="withdrawAnnouncement(record)">
                      撤回
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
      v-model:open="showFormModal"
      :title="isEdit ? '编辑信息' : '发布新信息'"
      :footer="null"
      width="900px"
      :destroyOnClose="true"
      @cancel="resetForm"
    >
      <a-form :model="formData" layout="vertical" :label-col="{ span: 24 }" :wrapper-col="{ span: 24 }">
        <a-row :gutter="16">
          <a-col :span="16">
            <a-form-item label="标题" required>
              <a-input v-model:value="formData.title" placeholder="请输入信息标题" maxlength="200" show-count />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="所属类别" required>
              <a-select v-model:value="formData.categoryId" placeholder="请选择类别">
                <a-select-option v-for="cat in categories" :key="cat.id" :value="cat.id">
                  {{ cat.categoryName }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="重要程度">
              <a-radio-group v-model:value="formData.importance">
                <a-radio :value="1">常规</a-radio>
                <a-radio :value="2">关注</a-radio>
                <a-radio :value="3">紧急</a-radio>
              </a-radio-group>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="顶部展示">
              <a-switch v-model:checked="isTopChecked" @change="handleTopChange" />
              <span class="form-tip">（最多置顶3条信息）</span>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="强制确认">
              <a-switch v-model:checked="isForceConfirmChecked" @change="handleForceConfirmChange" />
              <span class="form-tip">（员工登录时需确认）</span>
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="起始日期">
              <a-date-picker v-model:value="formData.publishDate" style="width: 100%" placeholder="选择开始展示日期" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="截止日期">
              <a-date-picker v-model:value="formData.expiryDate" style="width: 100%" placeholder="选择截止展示日期" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="接收对象" required>
          <a-radio-group v-model:value="targetType" @change="handleTargetTypeChange">
            <a-radio :value="1">全公司</a-radio>
            <a-radio :value="2">按部门选择</a-radio>
            <a-radio :value="3">指定人员</a-radio>
          </a-radio-group>
          <div v-if="targetType === 2" class="target-selector">
            <a-tree-select
              v-model:value="selectedDepartmentIds"
              :tree-data="departmentTree"
              tree-checkable
              :show-checked-strategy="SHOW_PARENT"
              placeholder="请选择部门"
              :max-tag-count="3"
              style="width: 100%; margin-top: 12px"
              :field-names="{ children: 'children', label: 'name', value: 'id' }"
            />
          </div>
          <div v-if="targetType === 3" class="target-selector">
            <a-select
              v-model:value="selectedEmployeeIds"
              mode="multiple"
              placeholder="请选择员工"
              style="width: 100%; margin-top: 12px"
              :max-tag-count="5"
              :filter-option="filterEmployeeOption"
              @search="handleEmployeeSearch"
            >
              <a-select-option v-for="emp in employeeList" :key="emp.id" :value="emp.id">
                {{ emp.name }} - {{ emp.department }}
              </a-select-option>
            </a-select>
          </div>
        </a-form-item>

        <a-form-item label="详细内容">
          <div class="rich-editor-wrapper">
            <div class="editor-toolbar">
              <a-space :size="4">
                <a-button size="small" @click="execCommand('bold')"><BoldOutlined /></a-button>
                <a-button size="small" @click="execCommand('italic')"><ItalicOutlined /></a-button>
                <a-button size="small" @click="execCommand('underline')"><UnderlineOutlined /></a-button>
                <a-divider type="vertical" />
                <a-button size="small" @click="execCommand('insertUnorderedList')"><UnorderedListOutlined /></a-button>
                <a-button size="small" @click="execCommand('insertOrderedList')"><OrderedListOutlined /></a-button>
                <a-divider type="vertical" />
                <a-select size="small" style="width: 100px" placeholder="字号" @change="handleFontSize">
                  <a-select-option value="1">小</a-select-option>
                  <a-select-option value="3">中</a-select-option>
                  <a-select-option value="5">大</a-select-option>
                  <a-select-option value="7">特大</a-select-option>
                </a-select>
                <a-select size="small" style="width: 100px" placeholder="颜色" @change="handleFontColor">
                  <a-select-option value="#000000">黑色</a-select-option>
                  <a-select-option value="#ff0000">红色</a-select-option>
                  <a-select-option value="#0000ff">蓝色</a-select-option>
                  <a-select-option value="#008000">绿色</a-select-option>
                </a-select>
              </a-space>
            </div>
            <div
              ref="editorRef"
              class="editor-content"
              contenteditable="true"
              placeholder="请输入详细内容..."
              @input="handleContentInput"
            ></div>
          </div>
        </a-form-item>

        <a-form-item label="相关材料">
          <a-upload
            v-model:file-list="attachmentList"
            :custom-request="handleUploadAttachment"
            :before-upload="beforeUploadAttachment"
            :show-upload-list="true"
            multiple
            accept=".pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.jpg,.jpeg,.png"
          >
            <a-button>
              <UploadOutlined /> 上传附件
            </a-button>
            <template #tip>
              <div class="ant-upload-text">
                支持 PDF、Office文件、图片，单文件不超过 20MB
              </div>
            </template>
            <template #itemRender="{ originNode, file, actions }">
              <div class="upload-item">
                <div class="upload-item-info">
                  <FileOutlined />
                  <span class="upload-item-name">{{ file.name }}</span>
                  <span class="upload-item-size">({{ formatFileSize(file.size) }})</span>
                </div>
                <div v-if="file.status === 'uploading'" class="upload-progress">
                  <a-progress :percent="file.percent || 0" :show-info="true" size="small" />
                </div>
                <div v-if="file.status === 'done'" class="upload-success">
                  <CheckCircleOutlined style="color: #52c41a" />
                  <a-button type="text" size="small" @click="actions.remove">删除</a-button>
                </div>
                <div v-if="file.status === 'error'" class="upload-error">
                  <CloseCircleOutlined style="color: #ff4d4f" />
                  <span class="error-text">上传失败</span>
                  <a-button type="text" size="small" @click="actions.remove">删除</a-button>
                </div>
              </div>
            </template>
          </a-upload>
        </a-form-item>
      </a-form>

      <div class="modal-footer">
        <a-button @click="resetForm">取消</a-button>
        <a-space>
          <a-button @click="saveAsDraft" :loading="submitting">保存草稿</a-button>
          <a-button type="primary" @click="publishAnnouncement" :loading="submitting">立即发布</a-button>
        </a-space>
      </div>
    </a-modal>

    <a-modal
      v-model:open="showCategoryModal"
      title="类别管理"
      :footer="null"
      width="600px"
      :destroyOnClose="true"
    >
      <div class="category-toolbar">
        <a-button type="primary" size="small" @click="openCategoryForm">
          <PlusOutlined /> 新增类别
        </a-button>
      </div>
      <a-table
        :columns="categoryColumns"
        :data-source="categories"
        :loading="categoryLoading"
        :pagination="false"
        row-key="id"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'green' : 'default'">
              {{ record.status === 1 ? '启用' : '禁用' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="editCategory(record)">编辑</a-button>
              <a-button type="link" size="small" danger @click="deleteCategory(record)">删除</a-button>
            </a-space>
          </template>
        </template>
      </a-table>

      <a-modal
        v-model:open="showCategoryFormModal"
        :title="isCategoryEdit ? '编辑类别' : '新增类别'"
        :footer="null"
        width="400px"
        :destroyOnClose="true"
      >
        <a-form :model="categoryForm" layout="vertical">
          <a-form-item label="类别名称" required>
            <a-input v-model:value="categoryForm.categoryName" placeholder="请输入类别名称" />
          </a-form-item>
          <a-form-item label="类别编码" required>
            <a-input v-model:value="categoryForm.categoryCode" placeholder="请输入类别编码" />
          </a-form-item>
          <a-form-item label="排序">
            <a-input-number v-model:value="categoryForm.sortOrder" :min="0" style="width: 100%" />
          </a-form-item>
          <a-form-item label="状态">
            <a-switch v-model:checked="categoryStatusChecked" />
          </a-form-item>
        </a-form>
        <div class="modal-footer">
          <a-button @click="showCategoryFormModal = false">取消</a-button>
          <a-button type="primary" @click="saveCategory" :loading="categorySubmitting">确定</a-button>
        </div>
      </a-modal>
    </a-modal>

    <a-modal
      v-model:open="showStatisticsModal"
      title="阅览统计"
      :footer="null"
      width="700px"
      :destroyOnClose="true"
    >
      <div v-if="statisticsData" class="statistics-content">
        <div class="statistics-header">
          <h3>{{ statisticsData.title }}</h3>
        </div>
        <div class="statistics-overview">
          <a-row :gutter="16">
            <a-col :span="8">
              <div class="overview-card">
                <p class="overview-value">{{ statisticsData.readCount }}</p>
                <p class="overview-label">已阅人数</p>
              </div>
            </a-col>
            <a-col :span="8">
              <div class="overview-card">
                <p class="overview-value">{{ statisticsData.totalCount }}</p>
                <p class="overview-label">总人数</p>
              </div>
            </a-col>
            <a-col :span="8">
              <div class="overview-card">
                <p class="overview-value">{{ statisticsData.readRate }}</p>
                <p class="overview-label">阅读率</p>
              </div>
            </a-col>
          </a-row>
        </div>
        <div class="statistics-progress">
          <h4>阅读进度</h4>
          <a-progress 
            :percent="parseFloat(statisticsData.readRate)" 
            :stroke-color="getProgressColor(parseFloat(statisticsData.readRate))"
          />
        </div>
        <div class="unread-list">
          <h4>未阅人员 ({{ statisticsData.unreadEmployees?.length || 0 }}人)</h4>
          <div v-if="statisticsData.unreadEmployees?.length > 0" class="unread-grid">
            <a-tag v-for="emp in statisticsData.unreadEmployees" :key="emp.employeeId" color="default">
              {{ emp.employeeName }}
              <span class="dept-tag">{{ emp.department }}</span>
            </a-tag>
          </div>
          <a-empty v-else description="所有人员均已阅读" />
        </div>
      </div>
    </a-modal>

    <a-modal
      v-model:open="showDetailModal"
      title="信息详情"
      :footer="null"
      width="800px"
      :destroyOnClose="true"
    >
      <div v-if="detailData" class="detail-content">
        <div class="detail-header">
          <h2>{{ detailData.title }}</h2>
          <div class="detail-meta">
            <a-tag :color="getImportanceColor(detailData.importance)">
              {{ getImportanceText(detailData.importance) }}
            </a-tag>
            <span class="meta-item">
              <FolderOutlined /> {{ detailData.categoryName }}
            </span>
            <span class="meta-item">
              <UserOutlined /> {{ detailData.publisherName }}
            </span>
            <span class="meta-item">
              <ClockCircleOutlined /> {{ formatDateTime(detailData.publishTime || detailData.createdAt) }}
            </span>
          </div>
        </div>
        <div class="detail-body" v-html="detailData.content"></div>
        <div v-if="detailData.attachments" class="detail-attachments">
          <h4>相关材料</h4>
          <div class="attachment-list">
            <a v-for="(att, index) in parseAttachments(detailData.attachments)" :key="index" class="attachment-item" :href="getAttachmentUrl(att.url)" target="_blank">
              <FileTextOutlined />
              <span class="att-name">{{ att.name }}</span>
              <span class="att-size">{{ formatFileSize(att.size) }}</span>
            </a>
          </div>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  FileTextOutlined,
  ExclamationCircleOutlined,
  PushpinOutlined,
  EditOutlined,
  PlusOutlined,
  SettingOutlined,
  BoldOutlined,
  ItalicOutlined,
  UnderlineOutlined,
  UnorderedListOutlined,
  OrderedListOutlined,
  UploadOutlined,
  UserOutlined,
  ClockCircleOutlined,
  FolderOutlined,
  FileOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined
} from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import {
  getCategories,
  createCategory,
  updateCategory,
  deleteCategory as apiDeleteCategory,
  getAnnouncements,
  getMyAnnouncements,
  getAnnouncementDetail,
  createAnnouncement as apiCreateAnnouncement,
  updateAnnouncement as apiUpdateAnnouncement,
  withdrawAnnouncement as apiWithdrawAnnouncement,
  deleteAnnouncement as apiDeleteAnnouncement,
  getAnnouncementStatistics,
  uploadAnnouncementAttachment
} from '@/api/adminAnnouncement'
import { getDepartmentTree } from '@/api/department'
import { getEmployeeList } from '@/api/employee'

const SHOW_PARENT = 'SHOW_PARENT'

const activeTab = ref('list')
const loading = ref(false)
const categories = ref([])
const statistics = ref({})

const searchKeyword = ref('')
const filterCategoryId = ref(null)
const filterImportance = ref(null)
const filterPublishStatus = ref(null)
const announcementList = ref([])
const announcementPagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

const myAnnouncementList = ref([])
const myAnnouncementPagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

const showFormModal = ref(false)
const showCategoryModal = ref(false)
const showCategoryFormModal = ref(false)
const showStatisticsModal = ref(false)
const showDetailModal = ref(false)
const isEdit = ref(false)
const isCategoryEdit = ref(false)
const submitting = ref(false)
const categoryLoading = ref(false)
const categorySubmitting = ref(false)
const departmentTree = ref([])
const employeeList = ref([])
const statisticsData = ref(null)
const detailData = ref(null)
const editorRef = ref(null)

const formData = reactive({
  id: null,
  title: '',
  categoryId: null,
  content: '',
  importance: 1,
  isTop: 0,
  isForceConfirm: 0,
  publishDate: null,
  expiryDate: null,
  publishStatus: 0,
  targets: []
})

const isTopChecked = ref(false)
const isForceConfirmChecked = ref(false)
const targetType = ref(1)
const selectedDepartmentIds = ref([])
const selectedEmployeeIds = ref([])
const attachmentList = ref([])

const categoryForm = reactive({
  id: null,
  categoryName: '',
  categoryCode: '',
  sortOrder: 0,
  status: 1
})
const categoryStatusChecked = ref(true)

const announcementColumns = [
  { title: '标题', dataIndex: 'title', key: 'title', ellipsis: true },
  { title: '类别', dataIndex: 'categoryName', key: 'categoryName', width: 100 },
  { title: '重要程度', key: 'importance', width: 100 },
  { title: '发布状态', key: 'publishStatus', width: 100 },
  { title: '发布时间', dataIndex: 'publishTime', key: 'publishTime', width: 160,
    customRender: ({ text }) => text ? formatDateTime(text) : '-'
  },
  { title: '操作', key: 'action', width: 200, fixed: 'right' }
]

const myAnnouncementColumns = [
  { title: '标题', dataIndex: 'title', key: 'title', ellipsis: true },
  { title: '类别', dataIndex: 'categoryName', key: 'categoryName', width: 100 },
  { title: '重要程度', key: 'importance', width: 100 },
  { title: '发布状态', key: 'publishStatus', width: 100 },
  { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', width: 160,
    customRender: ({ text }) => formatDateTime(text)
  },
  { title: '操作', key: 'action', width: 180, fixed: 'right' }
]

const categoryColumns = [
  { title: '类别名称', dataIndex: 'categoryName', key: 'categoryName' },
  { title: '类别编码', dataIndex: 'categoryCode', key: 'categoryCode', width: 120 },
  { title: '排序', dataIndex: 'sortOrder', key: 'sortOrder', width: 80 },
  { title: '状态', key: 'status', width: 80 },
  { title: '操作', key: 'action', width: 120 }
]

const getImportanceColor = (importance) => {
  const colors = { 1: 'default', 2: 'blue', 3: 'red' }
  return colors[importance] || 'default'
}

const getImportanceText = (importance) => {
  const texts = { 1: '常规', 2: '关注', 3: '紧急' }
  return texts[importance] || '未知'
}

const getPublishStatusColor = (status) => {
  const colors = { 0: 'gold', 1: 'green', 2: 'default' }
  return colors[status] || 'default'
}

const getPublishStatusText = (status) => {
  const texts = { 0: '待发布', 1: '已发布', 2: '已撤回' }
  return texts[status] || '未知'
}

const getProgressColor = (percent) => {
  if (percent >= 90) return '#52c41a'
  if (percent >= 70) return '#1890ff'
  if (percent >= 50) return '#faad14'
  return '#ff4d4f'
}

const formatDateTime = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const formatFileSize = (bytes) => {
  if (!bytes) return ''
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

const getAttachmentUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  return url.startsWith('/') ? url : '/' + url
}

const parseAttachments = (attachments) => {
  if (!attachments) return []
  try {
    return JSON.parse(attachments)
  } catch {
    return []
  }
}

const fetchCategories = async () => {
  try {
    const res = await getCategories()
    categories.value = res.data
  } catch (error) {
    console.error('获取类别失败:', error)
  }
}

const fetchStatistics = async () => {
  try {
    const res = await getAnnouncements({ pageNum: 1, pageSize: 100 })
    const list = res.data.records || []
    statistics.value = {
      publishedCount: list.filter(a => a.publishStatus === 1).length,
      importantCount: list.filter(a => a.importance === 2 || a.importance === 3).length,
      topCount: list.filter(a => a.isTop === 1).length,
      draftCount: list.filter(a => a.publishStatus === 0).length
    }
  } catch (error) {
    console.error('获取统计失败:', error)
  }
}

const fetchAnnouncementList = async () => {
  loading.value = true
  try {
    const res = await getAnnouncements({
      pageNum: announcementPagination.current,
      pageSize: announcementPagination.pageSize,
      categoryId: filterCategoryId.value,
      importance: filterImportance.value,
      publishStatus: filterPublishStatus.value,
      keyword: searchKeyword.value
    })
    announcementList.value = res.data.records || []
    announcementPagination.total = res.data.total || 0
  } catch (error) {
    console.error('获取信息列表失败:', error)
  } finally {
    loading.value = false
  }
}

const fetchMyAnnouncementList = async () => {
  loading.value = true
  try {
    const res = await getMyAnnouncements({
      pageNum: myAnnouncementPagination.current,
      pageSize: myAnnouncementPagination.pageSize
    })
    myAnnouncementList.value = res.data.records || []
    myAnnouncementPagination.total = res.data.total || 0
  } catch (error) {
    console.error('获取我发布的信息失败:', error)
  } finally {
    loading.value = false
  }
}

const fetchDepartments = async () => {
  try {
    const res = await getDepartmentTree()
    departmentTree.value = res.data
  } catch (error) {
    console.error('获取部门列表失败:', error)
  }
}

const fetchEmployees = async () => {
  try {
    const res = await getEmployeeList({ pageNum: 1, pageSize: 1000 })
    employeeList.value = res.data.records || []
  } catch (error) {
    console.error('获取员工列表失败:', error)
  }
}

const handleTableChange = (pagination) => {
  announcementPagination.current = pagination.current
  announcementPagination.pageSize = pagination.pageSize
  fetchAnnouncementList()
}

const handleMyTableChange = (pagination) => {
  myAnnouncementPagination.current = pagination.current
  myAnnouncementPagination.pageSize = pagination.pageSize
  fetchMyAnnouncementList()
}

const openCreateModal = () => {
  isEdit.value = false
  resetForm()
  fetchDepartments()
  fetchEmployees()
  showFormModal.value = true
}

const editAnnouncement = async (record) => {
  isEdit.value = true
  try {
    const res = await getAnnouncementDetail(record.id)
    const data = res.data
    formData.id = data.id
    formData.title = data.title
    formData.categoryId = data.categoryId
    formData.content = data.content || ''
    formData.importance = data.importance
    formData.isTop = data.isTop
    formData.isForceConfirm = data.isForceConfirm
    formData.publishDate = data.publishDate ? dayjs(data.publishDate) : null
    formData.expiryDate = data.expiryDate ? dayjs(data.expiryDate) : null
    formData.publishStatus = data.publishStatus
    
    isTopChecked.value = data.isTop === 1
    isForceConfirmChecked.value = data.isForceConfirm === 1
    
    if (data.targets && data.targets.length > 0) {
      const firstTarget = data.targets[0]
      targetType.value = firstTarget.targetType
      if (firstTarget.targetType === 2) {
        selectedDepartmentIds.value = data.targets.map(t => t.targetId)
      } else if (firstTarget.targetType === 3) {
        selectedEmployeeIds.value = data.targets.map(t => t.targetId)
      }
    }
    
    if (data.attachments) {
      try {
        const atts = JSON.parse(data.attachments)
        attachmentList.value = atts.map((a, i) => ({
          uid: i,
          name: a.name,
          status: 'done',
          url: a.url,
          size: a.size
        }))
      } catch {}
    }
    
    await nextTick()
    if (editorRef.value) {
      editorRef.value.innerHTML = data.content || ''
    }
    
    fetchDepartments()
    fetchEmployees()
    showFormModal.value = true
  } catch (error) {
    console.error('获取信息详情失败:', error)
  }
}

const resetForm = () => {
  formData.id = null
  formData.title = ''
  formData.categoryId = null
  formData.content = ''
  formData.importance = 1
  formData.isTop = 0
  formData.isForceConfirm = 0
  formData.publishDate = null
  formData.expiryDate = null
  formData.publishStatus = 0
  formData.targets = []
  isTopChecked.value = false
  isForceConfirmChecked.value = false
  targetType.value = 1
  selectedDepartmentIds.value = []
  selectedEmployeeIds.value = []
  attachmentList.value = []
  if (editorRef.value) {
    editorRef.value.innerHTML = ''
  }
  showFormModal.value = false
}

const handleTopChange = (checked) => {
  formData.isTop = checked ? 1 : 0
}

const handleForceConfirmChange = (checked) => {
  formData.isForceConfirm = checked ? 1 : 0
}

const handleTargetTypeChange = () => {
  selectedDepartmentIds.value = []
  selectedEmployeeIds.value = []
}

const handleEmployeeSearch = (value) => {
  if (value) {
    getEmployeeList({ pageNum: 1, pageSize: 1000, keyword: value }).then(res => {
      employeeList.value = res.data.records || []
    })
  }
}

const filterEmployeeOption = (input, option) => {
  return option.children.toLowerCase().includes(input.toLowerCase())
}

const execCommand = (command, value = null) => {
  document.execCommand(command, false, value)
  editorRef.value?.focus()
}

const handleFontSize = (value) => {
  execCommand('fontSize', value)
}

const handleFontColor = (value) => {
  execCommand('foreColor', value)
}

const handleContentInput = () => {
  if (editorRef.value) {
    formData.content = editorRef.value.innerHTML
  }
}

const beforeUploadAttachment = (file) => {
  const allowedTypes = ['application/pdf', 
    'application/msword',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    'application/vnd.ms-excel',
    'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    'application/vnd.ms-powerpoint',
    'application/vnd.openxmlformats-officedocument.presentationml.presentation',
    'image/jpeg',
    'image/jpg',
    'image/png'
  ]
  
  const isAllowed = allowedTypes.includes(file.type) || 
    /\.(pdf|doc|docx|xls|xlsx|ppt|pptx|jpg|jpeg|png)$/i.test(file.name)
  
  if (!isAllowed) {
    message.error('仅支持 PDF、Office文件、图片格式')
    return false
  }
  
  const isLt20M = file.size / 1024 / 1024 < 20
  if (!isLt20M) {
    message.error('文件大小不能超过 20MB')
    return false
  }
  
  return true
}

const handleUploadAttachment = async ({ file, onSuccess, onError, onProgress }) => {
  try {
    const res = await uploadAnnouncementAttachment(file, (percent) => {
      if (onProgress) {
        onProgress({ percent })
      }
    })
    if (res.data.code === 200) {
      onSuccess({
        url: res.data.data.url,
        name: res.data.data.name,
        size: res.data.data.size
      })
      message.success(`${file.name} 上传成功`)
    } else {
      onError(new Error(res.data.message || '上传失败'))
      message.error(res.data.message || '上传失败')
    }
  } catch (error) {
    console.error('上传失败:', error)
    onError(error)
    message.error(error.response?.data?.message || '上传失败')
  }
}

const buildTargets = () => {
  const targets = []
  if (targetType.value === 1) {
    targets.push({ targetType: 1, targetId: null, targetName: '全公司' })
  } else if (targetType.value === 2) {
    selectedDepartmentIds.value.forEach(id => {
      const dept = findDepartment(departmentTree.value, id)
      targets.push({ targetType: 2, targetId: id, targetName: dept?.name || '' })
    })
  } else if (targetType.value === 3) {
    selectedEmployeeIds.value.forEach(id => {
      const emp = employeeList.value.find(e => e.id === id)
      targets.push({ targetType: 3, targetId: id, targetName: emp?.name || '' })
    })
  }
  return targets
}

const findDepartment = (tree, id) => {
  for (const node of tree) {
    if (node.id === id) return node
    if (node.children) {
      const found = findDepartment(node.children, id)
      if (found) return found
    }
  }
  return null
}

const formatSubmitData = (data) => {
  const submitData = { ...data }
  if (submitData.publishDate && typeof submitData.publishDate.format === 'function') {
    submitData.publishDate = submitData.publishDate.format('YYYY-MM-DD')
  }
  if (submitData.expiryDate && typeof submitData.expiryDate.format === 'function') {
    submitData.expiryDate = submitData.expiryDate.format('YYYY-MM-DD')
  }
  return submitData
}

const saveAsDraft = async () => {
  if (!formData.title) {
    message.warning('请输入标题')
    return
  }
  if (!formData.categoryId) {
    message.warning('请选择类别')
    return
  }
  
  submitting.value = true
  try {
    const data = formatSubmitData({
      ...formData,
      publishStatus: 0,
      targets: buildTargets(),
      attachments: JSON.stringify(attachmentList.value.map(f => ({
        name: f.name,
        url: f.url || f.response?.url,
        size: f.size
      })))
    })
    
    if (isEdit.value) {
      await apiUpdateAnnouncement(data)
      message.success('草稿保存成功')
    } else {
      await apiCreateAnnouncement(data)
      message.success('草稿保存成功')
    }
    
    resetForm()
    fetchAnnouncementList()
    fetchStatistics()
  } catch (error) {
    console.error('保存失败:', error)
  } finally {
    submitting.value = false
  }
}

const publishAnnouncement = async () => {
  if (!formData.title) {
    message.warning('请输入标题')
    return
  }
  if (!formData.categoryId) {
    message.warning('请选择类别')
    return
  }
  if (targetType.value === 2 && selectedDepartmentIds.value.length === 0) {
    message.warning('请选择接收部门')
    return
  }
  if (targetType.value === 3 && selectedEmployeeIds.value.length === 0) {
    message.warning('请选择接收人员')
    return
  }
  
  submitting.value = true
  try {
    const data = formatSubmitData({
      ...formData,
      publishStatus: 1,
      targets: buildTargets(),
      attachments: JSON.stringify(attachmentList.value.map(f => ({
        name: f.name,
        url: f.url || f.response?.url,
        size: f.size
      })))
    })
    
    if (isEdit.value) {
      await apiUpdateAnnouncement(data)
      message.success('发布成功')
    } else {
      await apiCreateAnnouncement(data)
      message.success('发布成功')
    }
    
    resetForm()
    fetchAnnouncementList()
    fetchStatistics()
  } catch (error) {
    message.error(error.response?.data?.message || '发布失败')
  } finally {
    submitting.value = false
  }
}

const withdrawAnnouncement = (record) => {
  Modal.confirm({
    title: '确认撤回',
    content: '确定要撤回该信息吗？撤回后状态将变为待发布。',
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      try {
        await apiWithdrawAnnouncement(record.id)
        message.success('撤回成功')
        fetchAnnouncementList()
        fetchMyAnnouncementList()
        fetchStatistics()
      } catch (error) {
        console.error('撤回失败:', error)
      }
    }
  })
}

const deleteAnnouncement = (record) => {
  Modal.confirm({
    title: '确认删除',
    content: '确定要删除该信息吗？此操作不可恢复。',
    okText: '确认',
    cancelText: '取消',
    okType: 'danger',
    onOk: async () => {
      try {
        await apiDeleteAnnouncement(record.id)
        message.success('删除成功')
        fetchAnnouncementList()
        fetchMyAnnouncementList()
        fetchStatistics()
      } catch (error) {
        console.error('删除失败:', error)
      }
    }
  })
}

const viewStatistics = async (record) => {
  try {
    const res = await getAnnouncementStatistics(record.id)
    statisticsData.value = res.data
    showStatisticsModal.value = true
  } catch (error) {
    console.error('获取统计失败:', error)
  }
}

const viewDetail = async (record) => {
  try {
    const res = await getAnnouncementDetail(record.id)
    detailData.value = res.data
    showDetailModal.value = true
  } catch (error) {
    console.error('获取详情失败:', error)
  }
}

const openCategoryForm = () => {
  isCategoryEdit.value = false
  categoryForm.id = null
  categoryForm.categoryName = ''
  categoryForm.categoryCode = ''
  categoryForm.sortOrder = 0
  categoryForm.status = 1
  categoryStatusChecked.value = true
  showCategoryFormModal.value = true
}

const editCategory = (record) => {
  isCategoryEdit.value = true
  categoryForm.id = record.id
  categoryForm.categoryName = record.categoryName
  categoryForm.categoryCode = record.categoryCode
  categoryForm.sortOrder = record.sortOrder
  categoryForm.status = record.status
  categoryStatusChecked.value = record.status === 1
  showCategoryFormModal.value = true
}

const saveCategory = async () => {
  if (!categoryForm.categoryName) {
    message.warning('请输入类别名称')
    return
  }
  if (!categoryForm.categoryCode) {
    message.warning('请输入类别编码')
    return
  }
  
  categorySubmitting.value = true
  try {
    categoryForm.status = categoryStatusChecked.value ? 1 : 0
    if (isCategoryEdit.value) {
      await updateCategory(categoryForm)
    } else {
      await createCategory(categoryForm)
    }
    message.success('保存成功')
    showCategoryFormModal.value = false
    fetchCategories()
  } catch (error) {
    console.error('保存失败:', error)
  } finally {
    categorySubmitting.value = false
  }
}

const deleteCategory = (record) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除类别「${record.categoryName}」吗？`,
    okText: '确认',
    cancelText: '取消',
    okType: 'danger',
    onOk: async () => {
      try {
        await apiDeleteCategory(record.id)
        message.success('删除成功')
        fetchCategories()
      } catch (error) {
        console.error('删除失败:', error)
      }
    }
  })
}

onMounted(() => {
  fetchCategories()
  fetchStatistics()
  fetchAnnouncementList()
})
</script>

<style scoped>
.admin-announcements-container {
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

.stat-published {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-important {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-top {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.stat-draft {
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

.tab-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}

.title-cell {
  display: flex;
  align-items: center;
  gap: 4px;
}

.top-icon {
  color: #faad14;
}

.title-text {
  cursor: pointer;
  color: #1890ff;
}

.title-text:hover {
  text-decoration: underline;
}

.form-tip {
  margin-left: 8px;
  font-size: 12px;
  color: #999;
}

.target-selector {
  margin-top: 12px;
}

.rich-editor-wrapper {
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  overflow: hidden;
}

.editor-toolbar {
  padding: 8px 12px;
  background: #fafafa;
  border-bottom: 1px solid #d9d9d9;
}

.editor-content {
  min-height: 300px;
  padding: 12px;
  outline: none;
}

.editor-content:empty:before {
  content: attr(placeholder);
  color: #bfbfbf;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.category-toolbar {
  margin-bottom: 16px;
}

.statistics-content .statistics-header {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.statistics-content .statistics-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
}

.statistics-overview {
  margin-bottom: 24px;
}

.overview-card {
  text-align: center;
  padding: 20px;
  background: #fafafa;
  border-radius: 8px;
}

.overview-card .overview-value {
  font-size: 28px;
  font-weight: 600;
  color: #667eea;
  margin: 0 0 4px;
}

.overview-card .overview-label {
  font-size: 13px;
  color: #999;
  margin: 0;
}

.statistics-progress {
  margin-bottom: 24px;
}

.statistics-progress h4 {
  margin: 0 0 12px;
  font-size: 14px;
  font-weight: 500;
}

.unread-list h4 {
  margin: 0 0 12px;
  font-size: 14px;
  font-weight: 500;
}

.unread-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.dept-tag {
  margin-left: 4px;
  color: #999;
  font-size: 12px;
}

.detail-content .detail-header {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.detail-content .detail-header h2 {
  margin: 0 0 12px;
  font-size: 20px;
  font-weight: 600;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #666;
}

.detail-body {
  margin-bottom: 24px;
  line-height: 1.8;
}

.detail-attachments h4 {
  margin: 0 0 12px;
  font-size: 14px;
  font-weight: 500;
}

.attachment-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.attachment-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: #fafafa;
  border-radius: 6px;
  text-decoration: none;
  color: #333;
}

.attachment-item:hover {
  background: #f0f0f0;
}

.att-name {
  flex: 1;
}

.att-size {
  color: #999;
  font-size: 12px;
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

.upload-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #fafafa;
  border-radius: 8px;
  margin-bottom: 8px;
}

.upload-item-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.upload-item-name {
  font-size: 14px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 300px;
}

.upload-item-size {
  font-size: 12px;
  color: #999;
  flex-shrink: 0;
}

.upload-progress {
  width: 200px;
  flex-shrink: 0;
}

.upload-success {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.upload-error {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.error-text {
  color: #ff4d4f;
  font-size: 13px;
}
</style>

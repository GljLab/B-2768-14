<template>
  <div class="employee-container">
    <div class="main-content">
      <a-card :bordered="false" class="content-card">
        <div class="toolbar">
          <div class="toolbar-left">
            <a-input-search
              v-model:value="searchKeyword"
              placeholder="搜索员工姓名、邮箱、手机号或职位"
              style="width: 300px"
              @search="handleSearch"
              enter-button
              allow-clear
            />
            <a-tree-select
              v-model:value="filterDepartmentId"
              :tree-data="departmentTreeData"
              :field-names="{ label: 'name', value: 'id', children: 'children' }"
              placeholder="按部门筛选"
              allow-clear
              tree-default-expand-all
              style="width: 200px; margin-left: 12px"
              @change="handleSearch"
            />
          </div>
          <div class="toolbar-right">
            <a-button 
              v-if="selectedRowKeys.length > 0" 
              @click="showBatchModal" 
              type="primary" 
              :icon="h(TeamOutlined)"
            >
              批量调配部门 ({{ selectedRowKeys.length }})
            </a-button>
            <a-button type="primary" @click="handleAdd" :icon="h(PlusOutlined)" style="margin-left: 12px">
              新增员工
            </a-button>
          </div>
        </div>

        <a-table
          :columns="columns"
          :data-source="employeeList"
          :loading="loading"
          :pagination="pagination"
          @change="handleTableChange"
          :row-selection="rowSelection"
          row-key="id"
          class="employee-table"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'department'">
              <a-tag color="blue">{{ record.department }}</a-tag>
            </template>
            <template v-else-if="column.key === 'status'">
              <a-tag :color="record.status === 1 ? 'success' : 'default'">
                {{ record.status === 1 ? '在职' : '离职' }}
              </a-tag>
            </template>
            <template v-else-if="column.key === 'isActive'">
              <a-tag :color="record.isActive === 1 ? 'green' : 'orange'">
                {{ record.isActive === 1 ? '已激活' : '待激活' }}
              </a-tag>
            </template>
            <template v-else-if="column.key === 'lockStatus'">
              <a-tag v-if="record.lockStatus === 1" color="red">已锁定</a-tag>
              <a-tag v-else color="success">正常</a-tag>
            </template>
            <template v-else-if="column.key === 'action'">
              <a-space>
                <a-button type="link" @click="handleEdit(record)" :icon="h(EditOutlined)">
                  编辑
                </a-button>
                <a-button type="link" @click="handleResetPassword(record)" :icon="h(KeyOutlined)">
                  重置密码
                </a-button>
                <a-button 
                  v-if="record.lockStatus === 1" 
                  type="link" 
                  @click="handleUnlock(record)"
                  style="color: #fa8c16"
                >
                  解锁
                </a-button>
                <a-button type="link" danger @click="handleDelete(record)" :icon="h(DeleteOutlined)">
                  删除
                </a-button>
              </a-space>
            </template>
          </template>
        </a-table>
      </a-card>
    </div>

    <a-modal
      v-model:open="modalVisible"
      :title="modalTitle"
      :confirm-loading="modalLoading"
      @ok="handleModalOk"
      @cancel="handleModalCancel"
      width="600px"
      :destroyOnClose="true"
    >
      <a-form
        ref="formRef"
        :model="formState"
        :rules="formRules"
        layout="vertical"
        class="employee-form"
      >
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="姓名" name="name">
              <a-input v-model:value="formState.name" placeholder="请输入姓名" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="邮箱" name="email">
              <a-input v-model:value="formState.email" placeholder="请输入邮箱" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="手机号" name="phone">
              <a-input v-model:value="formState.phone" placeholder="请输入手机号" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="部门" name="departmentId">
              <a-tree-select
                v-model:value="formState.departmentId"
                :tree-data="departmentTreeData"
                :field-names="{ label: 'name', value: 'id', children: 'children' }"
                placeholder="请选择部门"
                allow-clear
                tree-default-expand-all
                style="width: 100%"
                show-search
                tree-node-filter-prop="name"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="职位" name="position">
              <a-input v-model:value="formState.position" placeholder="请输入职位" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="入职日期" name="hireDate">
              <a-date-picker
                v-model:value="formState.hireDate"
                placeholder="请选择入职日期"
                style="width: 100%"
                format="YYYY-MM-DD"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="状态" name="status">
          <a-radio-group v-model:value="formState.status">
            <a-radio :value="1">在职</a-radio>
            <a-radio :value="0">离职</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal
      v-model:open="batchModalVisible"
      title="批量调配部门"
      :confirm-loading="batchModalLoading"
      @ok="handleBatchOk"
      @cancel="handleBatchCancel"
      width="500px"
    >
      <a-form :model="batchFormState" layout="vertical">
        <a-form-item label="目标部门" required>
          <a-tree-select
            v-model:value="batchFormState.departmentId"
            :tree-data="departmentTreeData"
            :field-names="{ label: 'name', value: 'id', children: 'children' }"
            placeholder="请选择目标部门"
            allow-clear
            tree-default-expand-all
            style="width: 100%"
            show-search
            tree-node-filter-prop="name"
          />
        </a-form-item>
        <a-alert
          message="操作说明"
          description="将已选中的 {{ selectedRowKeys.length }} 名员工调配到目标部门"
          type="info"
          show-icon
        />
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { h, ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { 
  PlusOutlined, EditOutlined, DeleteOutlined, KeyOutlined, TeamOutlined 
} from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import { getEmployeeList, createEmployee, updateEmployee, deleteEmployee, batchUpdateDepartment } from '@/api/employee'
import { getDepartmentTree } from '@/api/department'
import { resetEmployeePassword } from '@/api/profile'
import { unlockEmployee } from '@/api/security'

const loading = ref(false)
const employeeList = ref([])
const searchKeyword = ref('')
const filterDepartmentId = ref(null)
const departmentTreeData = ref([])
const selectedRowKeys = ref([])

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total) => `共 ${total} 条记录`
})

const columns = [
  { title: '姓名', dataIndex: 'name', key: 'name', width: 100 },
  { title: '邮箱', dataIndex: 'email', key: 'email', width: 200 },
  { title: '手机号', dataIndex: 'phone', key: 'phone', width: 130 },
  { title: '部门', dataIndex: 'department', key: 'department', width: 120 },
  { title: '职位', dataIndex: 'position', key: 'position', width: 120 },
  { title: '入职日期', dataIndex: 'hireDate', key: 'hireDate', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 80 },
  { title: '激活状态', dataIndex: 'isActive', key: 'isActive', width: 100 },
  { title: '锁定状态', dataIndex: 'lockStatus', key: 'lockStatus', width: 100 },
  { title: '操作', key: 'action', width: 250, fixed: 'right' }
]

const rowSelection = {
  selectedRowKeys,
  onChange: (keys) => {
    selectedRowKeys.value = keys
  }
}

const fetchDepartmentTree = async () => {
  try {
    const res = await getDepartmentTree()
    departmentTreeData.value = res.data
  } catch (error) {
    console.error('获取部门树失败:', error)
  }
}

const fetchEmployeeList = async () => {
  loading.value = true
  try {
    const res = await getEmployeeList({
      page: pagination.current,
      size: pagination.pageSize,
      keyword: searchKeyword.value,
      departmentId: filterDepartmentId.value
    })
    employeeList.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchEmployeeList()
}

const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchEmployeeList()
}

const modalVisible = ref(false)
const modalLoading = ref(false)
const modalTitle = ref('')
const isEdit = ref(false)
const editId = ref(null)
const formRef = ref()

const formState = reactive({
  name: '',
  email: '',
  phone: '',
  departmentId: null,
  position: '',
  hireDate: null,
  status: 1
})

const formRules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在2-20个字符之间', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ],
  departmentId: [
    { required: true, message: '请选择部门', trigger: 'change' }
  ],
  position: [
    { required: true, message: '请输入职位', trigger: 'blur' }
  ],
  hireDate: [
    { required: true, message: '请选择入职日期', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

const handleAdd = () => {
  isEdit.value = false
  modalTitle.value = '新增员工'
  resetForm()
  modalVisible.value = true
}

const handleEdit = (record) => {
  isEdit.value = true
  editId.value = record.id
  modalTitle.value = '编辑员工'
  
  formState.name = record.name
  formState.email = record.email
  formState.phone = record.phone
  formState.departmentId = record.departmentId
  formState.position = record.position
  formState.hireDate = record.hireDate ? dayjs(record.hireDate) : null
  formState.status = record.status
  
  modalVisible.value = true
}

const handleDelete = (record) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除员工 "${record.name}" 吗？此操作不可恢复。`,
    okText: '确认',
    cancelText: '取消',
    okType: 'danger',
    onOk: async () => {
      try {
        await deleteEmployee(record.id)
        message.success('员工删除成功')
        fetchEmployeeList()
      } catch (error) {
        // 错误已在拦截器中处理
      }
    }
  })
}

const handleModalOk = async () => {
  try {
    await formRef.value.validate()
    modalLoading.value = true
    
    const data = {
      ...formState,
      hireDate: formState.hireDate ? formState.hireDate.format('YYYY-MM-DD') : null
    }
    
    if (isEdit.value) {
      await updateEmployee(editId.value, data)
      message.success('员工更新成功')
    } else {
      await createEmployee(data)
      message.success('员工创建成功')
    }
    
    modalVisible.value = false
    fetchEmployeeList()
    fetchDepartmentTree()
  } catch (error) {
    // 表单验证失败或请求失败
  } finally {
    modalLoading.value = false
  }
}

const handleModalCancel = () => {
  resetForm()
}

const resetForm = () => {
  formState.name = ''
  formState.email = ''
  formState.phone = ''
  formState.departmentId = null
  formState.position = ''
  formState.hireDate = null
  formState.status = 1
  formRef.value?.resetFields()
}

const handleResetPassword = (record) => {
  Modal.confirm({
    title: '确认重置密码',
    content: `确定要将员工 "${record.name}" 的密码重置为手机号后6位吗？重置后账号将变为待激活状态。`,
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      try {
        await resetEmployeePassword(record.id)
        message.success('密码重置成功')
        fetchEmployeeList()
      } catch (error) {
        // 错误已在拦截器中处理
      }
    }
  })
}

const handleUnlock = (record) => {
  Modal.confirm({
    title: '确认解锁',
    content: `确定要解锁员工 "${record.name}" 的账号吗？解锁后错误次数将重置为0。`,
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      try {
        await unlockEmployee(record.id)
        message.success('账号已解锁')
        fetchEmployeeList()
      } catch (error) {
        console.error('解锁失败:', error)
      }
    }
  })
}

const batchModalVisible = ref(false)
const batchModalLoading = ref(false)
const batchFormState = reactive({
  departmentId: null
})

const showBatchModal = () => {
  batchFormState.departmentId = null
  batchModalVisible.value = true
}

const handleBatchCancel = () => {
  batchModalVisible.value = false
}

const handleBatchOk = async () => {
  if (!batchFormState.departmentId) {
    message.warning('请选择目标部门')
    return
  }
  
  batchModalLoading.value = true
  try {
    await batchUpdateDepartment({
      employeeIds: selectedRowKeys.value,
      departmentId: batchFormState.departmentId
    })
    message.success('批量调配成功')
    batchModalVisible.value = false
    selectedRowKeys.value = []
    fetchEmployeeList()
    fetchDepartmentTree()
  } catch (error) {
    // 错误已在拦截器中处理
  } finally {
    batchModalLoading.value = false
  }
}

onMounted(() => {
  fetchEmployeeList()
  fetchDepartmentTree()
})
</script>

<style scoped>
.employee-container {
}

.main-content {
  padding: 0;
}

.content-card {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.toolbar-left {
  display: flex;
  align-items: center;
}

.toolbar-right {
  display: flex;
  align-items: center;
}

.employee-table {
  margin-top: 16px;
}

:deep(.ant-table) {
  border-radius: 8px;
}

:deep(.ant-table-thead > tr > th) {
  background: #fafafa;
  font-weight: 600;
}

:deep(.ant-table-tbody > tr:hover > td) {
  background: #f5f7fa;
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

.employee-form {
  margin-top: 24px;
}

:deep(.ant-modal-header) {
  border-bottom: 1px solid #f0f0f0;
  padding: 16px 24px;
}

:deep(.ant-modal-title) {
  font-size: 18px;
  font-weight: 600;
}
</style>

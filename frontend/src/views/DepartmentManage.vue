<template>
  <div class="department-container">
    <div class="main-content">
      <a-row :gutter="24">
        <a-col :span="6">
          <a-card :bordered="false" class="tree-card">
            <template #title>
              <span>部门列表</span>
              <a-button type="primary" size="small" @click="handleAddRoot" :icon="h(PlusOutlined)" style="margin-left: 12px">
                新增根部门
              </a-button>
            </template>
            <a-tree
              v-model:selectedKeys="selectedKeys"
              :tree-data="treeData"
              :expanded-keys="expandedKeys"
              @expand="onExpand"
              @select="onSelect"
              :draggable="true"
              @drop="onDrop"
              class="department-tree"
            >
              <template #title="{ title, directMemberCount, totalMemberCount, managerName }">
                <div class="tree-node">
                  <span class="node-title">{{ title }}</span>
                  <span class="node-info">
                    <a-tag color="blue" size="small">直接: {{ directMemberCount || 0 }}</a-tag>
                    <a-tag color="green" size="small">总计: {{ totalMemberCount || 0 }}</a-tag>
                  </span>
                </div>
              </template>
            </a-tree>
          </a-card>
        </a-col>

        <a-col :span="18">
          <a-card :bordered="false" class="detail-card">
            <template #title>
              <span>部门详情</span>
              <template v-if="selectedDepartment">
                <a-space style="margin-left: 12px">
                  <a-button size="small" @click="handleAddChild" :icon="h(PlusOutlined)">
                    新增子部门
                  </a-button>
                  <a-button size="small" @click="handleEdit" :icon="h(EditOutlined)">
                    编辑
                  </a-button>
                  <a-button size="small" danger @click="handleDelete" :icon="h(DeleteOutlined)">
                    删除
                  </a-button>
                  <a-button size="small" @click="handleViewMembers" :icon="h(TeamOutlined)">
                    查看成员
                  </a-button>
                </a-space>
              </template>
            </template>
            
            <a-empty v-if="!selectedDepartment" description="请选择一个部门查看详情" />
            
            <template v-else>
              <a-descriptions :column="2" bordered>
                <a-descriptions-item label="部门名称">
                  {{ selectedDepartment.name }}
                </a-descriptions-item>
                <a-descriptions-item label="部门编码">
                  {{ selectedDepartment.code }}
                </a-descriptions-item>
                <a-descriptions-item label="上级部门">
                  {{ getParentName(selectedDepartment.parentId) || '无' }}
                </a-descriptions-item>
                <a-descriptions-item label="责任人">
                  <a v-if="selectedDepartment.managerName" @click="viewEmployee(selectedDepartment.managerId)" class="link">
                    {{ selectedDepartment.managerName }}
                  </a>
                  <span v-else>未设置</span>
                </a-descriptions-item>
                <a-descriptions-item label="直接下属人数">
                  <a-tag color="blue">{{ selectedDepartment.directMemberCount || 0 }} 人</a-tag>
                </a-descriptions-item>
                <a-descriptions-item label="总人数(含子部门)">
                  <a-tag color="green">{{ selectedDepartment.totalMemberCount || 0 }} 人</a-tag>
                </a-descriptions-item>
                <a-descriptions-item label="排序" :span="2">
                  {{ selectedDepartment.sortOrder }}
                </a-descriptions-item>
                <a-descriptions-item label="备注" :span="2">
                  {{ selectedDepartment.remark || '无' }}
                </a-descriptions-item>
              </a-descriptions>
            </template>
          </a-card>
        </a-col>
      </a-row>
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
        class="department-form"
      >
        <a-form-item label="部门名称" name="name">
          <a-input v-model:value="formState.name" placeholder="请输入部门名称" />
        </a-form-item>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="部门编码" name="code">
              <a-input v-model:value="formState.code" placeholder="请输入部门编码" :disabled="isEdit" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="排序" name="sortOrder">
              <a-input-number v-model:value="formState.sortOrder" :min="0" style="width: 100%" placeholder="数字越小越靠前" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="上级部门" name="parentId">
          <a-tree-select
            v-model:value="formState.parentId"
            :tree-data="treeSelectData"
            :field-names="{ label: 'name', value: 'id', children: 'children' }"
            placeholder="请选择上级部门（留空为根部门）"
            allow-clear
            tree-default-expand-all
            style="width: 100%"
          />
        </a-form-item>

        <a-form-item label="责任人" name="managerId">
          <a-select
            v-model:value="formState.managerId"
            placeholder="请选择责任人"
            allow-clear
            show-search
            :filter-option="filterEmployee"
            style="width: 100%"
          >
            <a-select-option v-for="emp in employeeList" :key="emp.id" :value="emp.id">
              {{ emp.name }} ({{ emp.department || '未分配' }})
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="备注" name="remark">
          <a-textarea v-model:value="formState.remark" placeholder="请输入备注说明" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal
      v-model:open="memberModalVisible"
      title="部门成员"
      width="800px"
      :footer="null"
    >
      <a-table
        :columns="memberColumns"
        :data-source="memberList"
        :loading="memberLoading"
        :pagination="memberPagination"
        @change="handleMemberTableChange"
        row-key="id"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'success' : 'default'">
              {{ record.status === 1 ? '在职' : '离职' }}
            </a-tag>
          </template>
        </template>
      </a-table>
    </a-modal>
  </div>
</template>

<script setup>
import { h, ref, reactive, onMounted, computed } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { 
  PlusOutlined, EditOutlined, DeleteOutlined, TeamOutlined 
} from '@ant-design/icons-vue'
import { 
  getDepartmentTree, getFlatDepartmentList, createDepartment, 
  updateDepartment, deleteDepartment, getDepartmentDescendantIds 
} from '@/api/department'
import { getEmployeeList, getEmployeeListByDepartmentIds } from '@/api/employee'

const treeData = ref([])
const flatDepartmentList = ref([])
const selectedKeys = ref([])
const expandedKeys = ref([])
const selectedDepartment = ref(null)

const fetchDepartmentTree = async () => {
  try {
    const res = await getDepartmentTree()
    treeData.value = transformTreeData(res.data)
  } catch (error) {
    console.error('获取部门树失败:', error)
  }
}

const fetchFlatDepartmentList = async () => {
  try {
    const res = await getFlatDepartmentList()
    flatDepartmentList.value = res.data
  } catch (error) {
    console.error('获取部门列表失败:', error)
  }
}

const transformTreeData = (data) => {
  return data.map(item => ({
    ...item,
    title: item.name,
    key: item.id,
    children: item.children && item.children.length > 0 ? transformTreeData(item.children) : null
  }))
}

const treeSelectData = computed(() => {
  const transform = (items) => {
    return items.map(item => ({
      id: item.id,
      name: item.name,
      disabled: selectedDepartment.value && item.id === selectedDepartment.value.id,
      children: item.children && item.children.length > 0 ? transform(item.children) : null
    }))
  }
  return transform(treeData.value)
})

const onExpand = (keys) => {
  expandedKeys.value = keys
}

const onSelect = (keys, info) => {
  if (keys.length > 0) {
    selectedDepartment.value = info.node
  } else {
    selectedDepartment.value = null
  }
}

const onDrop = async (info) => {
  const dropKey = info.node.key
  const dragKey = info.dragNode.key
  const dropPos = info.node.pos.split('-')
  const dropPosition = info.dropPosition - Number(dropPos[dropPos.length - 1])

  if (dropPosition === 0) {
    try {
      const dept = flatDepartmentList.value.find(d => d.id === dragKey)
      await updateDepartment(dragKey, {
        ...dept,
        parentId: dropKey
      })
      message.success('部门移动成功')
      fetchDepartmentTree()
      fetchFlatDepartmentList()
    } catch (error) {
      message.error('部门移动失败')
    }
  }
}

const getParentName = (parentId) => {
  if (!parentId) return ''
  const parent = flatDepartmentList.value.find(d => d.id === parentId)
  return parent ? parent.name : ''
}

const viewEmployee = (employeeId) => {
  message.info('跳转到员工详情页功能待实现')
}

const modalVisible = ref(false)
const modalLoading = ref(false)
const modalTitle = ref('')
const isEdit = ref(false)
const editId = ref(null)
const formRef = ref()

const formState = reactive({
  name: '',
  code: '',
  parentId: null,
  managerId: null,
  remark: '',
  sortOrder: 0
})

const formRules = {
  name: [
    { required: true, message: '请输入部门名称', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入部门编码', trigger: 'blur' }
  ]
}

const employeeList = ref([])

const fetchEmployeeList = async () => {
  try {
    const res = await getEmployeeList({ page: 1, size: 1000 })
    employeeList.value = res.data.records.filter(e => e.status === 1)
  } catch (error) {
    console.error('获取员工列表失败:', error)
  }
}

const filterEmployee = (input, option) => {
  return option.label.toLowerCase().includes(input.toLowerCase())
}

const handleAddRoot = () => {
  isEdit.value = false
  modalTitle.value = '新增根部门'
  resetForm()
  formState.parentId = null
  modalVisible.value = true
}

const handleAddChild = () => {
  isEdit.value = false
  modalTitle.value = '新增子部门'
  resetForm()
  formState.parentId = selectedDepartment.value.id
  modalVisible.value = true
}

const handleEdit = () => {
  isEdit.value = true
  editId.value = selectedDepartment.value.id
  modalTitle.value = '编辑部门'
  
  formState.name = selectedDepartment.value.name
  formState.code = selectedDepartment.value.code
  formState.parentId = selectedDepartment.value.parentId
  formState.managerId = selectedDepartment.value.managerId
  formState.remark = selectedDepartment.value.remark
  formState.sortOrder = selectedDepartment.value.sortOrder || 0
  
  modalVisible.value = true
}

const handleDelete = () => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除部门 "${selectedDepartment.value.name}" 吗？`,
    okText: '确认',
    cancelText: '取消',
    okType: 'danger',
    onOk: async () => {
      try {
        await deleteDepartment(selectedDepartment.value.id)
        message.success('部门删除成功')
        selectedKeys.value = []
        selectedDepartment.value = null
        fetchDepartmentTree()
        fetchFlatDepartmentList()
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
    
    if (isEdit.value) {
      await updateDepartment(editId.value, formState)
      message.success('部门更新成功')
    } else {
      await createDepartment(formState)
      message.success('部门创建成功')
    }
    
    modalVisible.value = false
    fetchDepartmentTree()
    fetchFlatDepartmentList()
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
  formState.code = ''
  formState.parentId = null
  formState.managerId = null
  formState.remark = ''
  formState.sortOrder = 0
  formRef.value?.resetFields()
}

const memberModalVisible = ref(false)
const memberLoading = ref(false)
const memberList = ref([])
const memberPagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total) => `共 ${total} 条记录`
})

const memberColumns = [
  { title: '姓名', dataIndex: 'name', key: 'name', width: 100 },
  { title: '邮箱', dataIndex: 'email', key: 'email' },
  { title: '部门', dataIndex: 'department', key: 'department', width: 120 },
  { title: '职位', dataIndex: 'position', key: 'position', width: 120 },
  { title: '入职日期', dataIndex: 'hireDate', key: 'hireDate', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 80 }
]

const handleViewMembers = async () => {
  memberModalVisible.value = true
  memberPagination.current = 1
  fetchMemberList()
}

const fetchMemberList = async () => {
  if (!selectedDepartment.value) return
  
  memberLoading.value = true
  try {
    const res = await getDepartmentDescendantIds(selectedDepartment.value.id)
    const departmentIds = res.data
    
    const empRes = await getEmployeeListByDepartmentIds({
      page: memberPagination.current,
      size: memberPagination.pageSize,
      departmentIds: departmentIds.join(',')
    })
    
    memberList.value = empRes.data.records
    memberPagination.total = empRes.data.total
  } catch (error) {
    console.error('获取部门成员失败:', error)
  } finally {
    memberLoading.value = false
  }
}

const handleMemberTableChange = (pag) => {
  memberPagination.current = pag.current
  memberPagination.pageSize = pag.pageSize
  fetchMemberList()
}

onMounted(() => {
  fetchDepartmentTree()
  fetchFlatDepartmentList()
  fetchEmployeeList()
})
</script>

<style scoped>
.department-container {
}

.main-content {
  padding: 0;
}

.tree-card,
.detail-card {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.department-tree {
  margin-top: 16px;
}

.tree-node {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 4px 0;
}

.node-title {
  font-size: 14px;
}

.node-info {
  display: flex;
  gap: 4px;
}

.link {
  color: #1890ff;
  cursor: pointer;
}

.link:hover {
  color: #40a9ff;
}

.department-form {
  margin-top: 16px;
}

:deep(.ant-tree-node-content-wrapper) {
  width: calc(100% - 24px);
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

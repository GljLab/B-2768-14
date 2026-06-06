<template>
  <div class="employee-container">
    <div class="main-content">
      <a-card :bordered="false" class="content-card">
        <div class="toolbar">
          <div class="toolbar-left">
            <a-input-search
              v-model:value="searchUsername"
              placeholder="搜索用户名/邮箱"
              style="width: 250px"
              @search="handleSearch"
              enter-button
              allow-clear
            />
            <a-select
              v-model:value="filterUserType"
              placeholder="用户类型"
              allow-clear
              style="width: 150px; margin-left: 12px"
              @change="handleSearch"
            >
              <a-select-option :value="1">管理员</a-select-option>
              <a-select-option :value="2">员工</a-select-option>
            </a-select>
            <a-select
              v-model:value="filterLoginResult"
              placeholder="登录结果"
              allow-clear
              style="width: 150px; margin-left: 12px"
              @change="handleSearch"
            >
              <a-select-option :value="1">成功</a-select-option>
              <a-select-option :value="0">失败</a-select-option>
            </a-select>
            <a-range-picker
              v-model:value="dateRange"
              style="margin-left: 12px"
              @change="handleSearch"
            />
          </div>
        </div>

        <a-table
          :columns="columns"
          :data-source="logList"
          :loading="loading"
          :pagination="pagination"
          @change="handleTableChange"
          row-key="id"
          class="log-table"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'userType'">
              <a-tag :color="record.userType === 1 ? 'blue' : 'green'">
                {{ record.userType === 1 ? '管理员' : '员工' }}
              </a-tag>
            </template>
            <template v-else-if="column.key === 'loginResult'">
              <a-tag :color="record.loginResult === 1 ? 'success' : 'error'">
                {{ record.loginResult === 1 ? '成功' : '失败' }}
              </a-tag>
            </template>
            <template v-else-if="column.key === 'isAbnormal'">
              <a-tag v-if="record.isAbnormal" color="red">异常</a-tag>
              <span v-else>-</span>
            </template>
            <template v-else-if="column.key === 'abnormalType'">
              <span v-if="record.abnormalType">{{ record.abnormalType }}</span>
              <span v-else>-</span>
            </template>
            <template v-else-if="column.key === 'failureReason'">
              <span v-if="record.failureReason">{{ record.failureReason }}</span>
              <span v-else>-</span>
            </template>
          </template>
        </a-table>
      </a-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import dayjs from 'dayjs'
import { getAdminLoginLogs } from '@/api/security'

const loading = ref(false)
const logList = ref([])
const searchUsername = ref('')
const filterUserType = ref(null)
const filterLoginResult = ref(null)
const dateRange = ref([])

const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showTotal: (total) => `共 ${total} 条记录`
})

const columns = [
  { title: '用户类型', dataIndex: 'userType', key: 'userType', width: 100 },
  { title: '用户名/邮箱', dataIndex: 'username', key: 'username', width: 200 },
  { title: 'IP地址', dataIndex: 'ipAddress', key: 'ipAddress', width: 130 },
  { title: '设备类型', dataIndex: 'deviceType', key: 'deviceType', width: 100 },
  { title: '登录时间', dataIndex: 'loginTime', key: 'loginTime', width: 180 },
  { title: '登录结果', dataIndex: 'loginResult', key: 'loginResult', width: 100 },
  { title: '失败原因', dataIndex: 'failureReason', key: 'failureReason', width: 200 },
  { title: '是否异常', dataIndex: 'isAbnormal', key: 'isAbnormal', width: 100 },
  { title: '异常类型', dataIndex: 'abnormalType', key: 'abnormalType', width: 150 }
]

const fetchLogs = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.current,
      size: pagination.pageSize,
      username: searchUsername.value || undefined,
      userType: filterUserType.value,
      loginResult: filterLoginResult.value,
      startTime: dateRange.value && dateRange.value.length > 0 ? dateRange.value[0].toISOString() : undefined,
      endTime: dateRange.value && dateRange.value.length > 1 ? dateRange.value[1].toISOString() : undefined
    }
    
    const res = await getAdminLoginLogs(params)
    logList.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    console.error('获取登录日志失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchLogs()
}

const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchLogs()
}

onMounted(() => {
  fetchLogs()
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

.log-table {
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
</style>

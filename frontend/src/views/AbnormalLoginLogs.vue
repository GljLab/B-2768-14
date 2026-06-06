<template>
  <div class="employee-container">
    <div class="main-content">
      <a-card :bordered="false" class="content-card">
        <a-alert
          message="安全说明"
          description="以下是检测到的异常登录记录，包括凌晨登录、短时间内IP地址变化较大等情况。请关注并确认是否为用户本人操作。"
          type="warning"
          show-icon
          class="alert-tip"
        />
        
        <a-table
          :columns="columns"
          :data-source="logList"
          :loading="loading"
          :pagination="false"
          row-key="id"
          class="log-table"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'userType'">
              <a-tag :color="record.userType === 1 ? 'blue' : 'green'">
                {{ record.userType === 1 ? '管理员' : '员工' }}
              </a-tag>
            </template>
            <template v-else-if="column.key === 'abnormalType'">
              <a-tag color="red">{{ record.abnormalType }}</a-tag>
            </template>
          </template>
        </a-table>
        
        <div v-if="logList.length === 0 && !loading" class="empty-state">
          <img src="https://gw.alipayobjects.com/zos/antfincdn/ZHrcdLPrvN/empty.svg" alt="empty" />
          <p class="empty-text">暂无异常登录记录</p>
        </div>
      </a-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAbnormalLoginLogs } from '@/api/security'

const loading = ref(false)
const logList = ref([])

const columns = [
  { title: '用户类型', dataIndex: 'userType', key: 'userType', width: 100 },
  { title: '用户名/邮箱', dataIndex: 'username', key: 'username', width: 200 },
  { title: 'IP地址', dataIndex: 'ipAddress', key: 'ipAddress', width: 130 },
  { title: '设备类型', dataIndex: 'deviceType', key: 'deviceType', width: 100 },
  { title: '登录时间', dataIndex: 'loginTime', key: 'loginTime', width: 180 },
  { title: '异常类型', dataIndex: 'abnormalType', key: 'abnormalType', width: 150 }
]

const fetchLogs = async () => {
  loading.value = true
  try {
    const res = await getAbnormalLoginLogs()
    logList.value = res.data
  } catch (error) {
    console.error('获取异常登录记录失败:', error)
  } finally {
    loading.value = false
  }
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

.alert-tip {
  margin-bottom: 16px;
}

.log-table {
  margin-top: 16px;
}

.empty-state {
  text-align: center;
  padding: 60px 0;
}

.empty-state img {
  width: 120px;
  margin-bottom: 16px;
}

.empty-text {
  color: #999;
  font-size: 14px;
}

:deep(.ant-table) {
  border-radius: 8px;
}

:deep(.ant-table-thead > tr > th) {
  background: #fafafa;
  font-weight: 600;
}

:deep(.ant-table-tbody > tr:hover > td) {
  background: #fff1f0;
}
</style>

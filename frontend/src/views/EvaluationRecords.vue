<template>
  <div class="evaluation-records">
    <a-page-header title="同事评价记录管理" @back="() => $router.back()" />
    
    <a-card style="margin-top: 20px">
      <a-table :columns="columns" :data-source="evaluationList" :loading="loading" :pagination="{ pageSize: 10 }">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'scores'">
            <div v-for="score in getDimensionScores(record.id)" :key="score.dimensionId">
              {{ score.dimensionName }}: {{ score.score }}分
            </div>
          </template>
          <template v-if="column.key === 'action'">
            <a-popconfirm title="确认删除该评价？" @confirm="handleDelete(record.id)">
              <a-button type="link" danger>删除</a-button>
            </a-popconfirm>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { getAllColleagueEvaluations, getColleagueDimensionScores, deleteColleagueEvaluation } from '@/api/adminEvaluation'

const route = useRoute()
const loading = ref(false)
const evaluationList = ref([])
const dimensionScoresMap = reactive({})

const columns = [
  { title: '评价人', dataIndex: 'evaluatorId', key: 'evaluatorId' },
  { title: '被评价人', dataIndex: 'evaluatedId', key: 'evaluatedId' },
  { title: '评语', dataIndex: 'comment', key: 'comment', ellipsis: true },
  { title: '各维度得分', key: 'scores', width: 200 },
  { title: '提交时间', dataIndex: 'submitTime', key: 'submitTime' },
  { title: '操作', key: 'action', width: 100 }
]

const fetchEvaluations = async () => {
  loading.value = true
  try {
    const cycleId = route.params.id
    const res = await getAllColleagueEvaluations(cycleId)
    evaluationList.value = res.data || []
    
    for (const item of evaluationList.value) {
      const scoresRes = await getColleagueDimensionScores(item.id)
      dimensionScoresMap[item.id] = scoresRes.data || []
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const getDimensionScores = (evaluationId) => {
  return dimensionScoresMap[evaluationId] || []
}

const handleDelete = async (id) => {
  try {
    await deleteColleagueEvaluation(id)
    message.success('删除成功')
    fetchEvaluations()
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  fetchEvaluations()
})
</script>

<style scoped>
.evaluation-records {
  padding: 20px;
}
</style>

<template>
  <div class="admin-evaluate">
    <a-page-header title="评价员工" @back="() => $router.back()" />
    
    <a-card style="margin-top: 20px">
      <a-form :model="formState" :label-col="{ span: 4 }" :wrapper-col="{ span: 18 }">
        <a-divider>工作目标评价</a-divider>
        
        <div v-for="goal in goals" :key="goal.id" style="margin-bottom: 20px; padding: 15px; background: #f5f5f5; border-radius: 8px">
          <h4>{{ goal.goalName }} (权重: {{ goal.weight }}%)</h4>
          <p style="color: #666; margin-bottom: 10px">{{ goal.planContent }}</p>
          <a-form-item :label="'评分'">
            <a-slider v-model:value="goalScores[goal.id]" :min="0" :max="100" :marks="{ 0: '0', 60: '60', 80: '80', 100: '100' }" />
          </a-form-item>
          <a-form-item :label="'评价意见'">
            <a-textarea v-model:value="goalComments[goal.id]" :rows="2" placeholder="请输入评价意见" />
          </a-form-item>
        </div>

        <a-divider>综合评价</a-divider>
        
        <a-form-item label="综合评分">
          <a-slider v-model:value="formState.overallScore" :min="0" :max="100" :marks="{ 0: '0', 60: '60', 80: '80', 100: '100' }" />
          <div style="text-align: center; font-size: 24px; font-weight: bold; color: #1890ff">
            {{ formState.overallScore }}分
          </div>
        </a-form-item>
        
        <a-form-item label="综合评价">
          <a-textarea v-model:value="formState.overallComment" :rows="4" placeholder="请输入综合评价" />
        </a-form-item>
        
        <a-form-item label="发展建议">
          <a-textarea v-model:value="formState.developmentSuggestion" :rows="4" placeholder="请输入发展建议" />
        </a-form-item>
        
        <a-form-item :wrapper-col="{ offset: 4, span: 18 }">
          <a-button type="primary" :loading="submitting" @click="handleSubmit">
            提交评价
          </a-button>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { getAdminEvaluation, submitAdminEvaluation } from '@/api/adminEvaluation'
import { getMyGoals } from '@/api/employeeEvaluation'

const route = useRoute()
const submitting = ref(false)
const goals = ref([])
const goalScores = reactive({})
const goalComments = reactive({})

const formState = reactive({
  cycleId: null,
  employeeId: null,
  overallScore: 80,
  overallComment: '',
  developmentSuggestion: '',
  goalEvaluations: []
})

const fetchGoals = async () => {
  const { cycleId, employeeId } = route.params
  formState.cycleId = Number(cycleId)
  formState.employeeId = Number(employeeId)
  
  const res = await getMyGoals(cycleId)
  goals.value = res.data || []
  
  for (const goal of goals.value) {
    goalScores[goal.id] = 80
    goalComments[goal.id] = ''
  }
}

const getGoalScore = (goalId) => {
  return goalScores[goalId]
}

const getGoalComment = (goalId) => {
  return goalComments[goalId]
}

const handleSubmit = async () => {
  submitting.value = true
  try {
    const goalEvaluations = goals.value.map(g => ({
      goalId: g.id,
      score: goalScores[g.id],
      comment: goalComments[g.id]
    }))
    
    await submitAdminEvaluation({
      ...formState,
      goalEvaluations
    })
    message.success('评价提交成功')
  } catch (e) {
    console.error(e)
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchGoals()
})
</script>

<style scoped>
.admin-evaluate {
  padding: 20px;
}
</style>

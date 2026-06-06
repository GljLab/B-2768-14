<template>
  <div class="goal-setting">
    <a-page-header title="工作目标设定" @back="() => $router.back()" />
    
    <a-card style="margin-top: 20px">
      <a-steps :current="currentStep" style="margin-bottom: 30px">
        <a-step title="设定目标" description="添加本期工作目标" />
        <a-step title="自我总结" description="回顾目标完成情况" />
        <a-step title="同事评价" description="评价身边的同事" />
        <a-step title="查看报告" description="查看成长报告" />
      </a-steps>

      <div v-if="currentStep === 0">
        <a-empty v-if="goals.length === 0" description="暂无目标，请添加工作目标" />
        
        <div v-for="(goal, index) in goals" :key="goal.id || index" class="goal-item">
          <a-card size="small">
            <template #extra>
              <a-button type="link" danger @click="removeGoal(index)">删除</a-button>
            </template>
            <a-form :model="goal" :label-col="{ span: 3 }">
              <a-form-item label="目标名称" required>
                <a-input v-model:value="goal.goalName" placeholder="请输入目标名称" />
              </a-form-item>
              <a-form-item label="计划与标准">
                <a-textarea v-model:value="goal.planContent" :rows="3" placeholder="请输入具体计划和衡量标准" />
              </a-form-item>
              <a-form-item label="重要度权重">
                <a-slider v-model:value="goal.weight" :min="10" :max="100" :step="10" />
                <span>当前权重: {{ goal.weight }}%</span>
              </a-form-item>
            </a-form>
          </a-card>
        </div>

        <a-button type="dashed" style="width: 100%; margin-top: 16px" @click="addGoal">
          + 添加目标
        </a-button>

        <div style="margin-top: 24px; text-align: right">
          <a-space>
            <a-button @click="saveGoals">保存草稿</a-button>
            <a-button type="primary" @click="handleSubmitGoals">提交目标</a-button>
          </a-space>
        </div>
      </div>

      <div v-if="currentStep === 1">
        <a-empty v-if="goals.length === 0" description="请先设定工作目标" />
        
        <div v-for="goal in goals" :key="goal.id" class="goal-eval-item">
          <a-card size="small" :title="goal.goalName">
            <p style="color: #666; margin-bottom: 16px">{{ goal.planContent }}</p>
            <a-form :label-col="{ span: 4 }">
              <a-form-item label="自评分">
                <a-slider v-model:value="goal.selfScore" :min="0" :max="100" :marks="{ 0: '0', 60: '60', 80: '80', 100: '100' }" />
                <div style="text-align: right; font-weight: bold">{{ goal.selfScore }}分</div>
              </a-form-item>
              <a-form-item label="达成度">
                <a-radio-group v-model:value="goal.achievementDegree">
                  <a-radio :value="1">超出预期</a-radio>
                  <a-radio :value="2">达到预期</a-radio>
                  <a-radio :value="3">接近预期</a-radio>
                  <a-radio :value="4">未达到</a-radio>
                </a-radio-group>
              </a-form-item>
              <a-form-item label="实际完成情况">
                <a-textarea v-model:value="goal.actualResult" :rows="3" />
              </a-form-item>
            </a-form>
          </a-card>
        </div>

        <a-card title="工作总结" style="margin-top: 20px">
          <a-form :label-col="{ span: 3 }">
            <a-form-item label="收获和成长">
              <a-textarea v-model:value="selfEvalForm.growth" :rows="3" />
            </a-form-item>
            <a-form-item label="遇到的挑战">
              <a-textarea v-model:value="selfEvalForm.challenges" :rows="3" />
            </a-form-item>
            <a-form-item label="需要改进的地方">
              <a-textarea v-model:value="selfEvalForm.improvements" :rows="3" />
            </a-form-item>
            <a-form-item label="本期工作总结">
              <a-textarea v-model:value="selfEvalForm.summary" :rows="4" />
            </a-form-item>
          </a-form>
        </a-card>

        <div style="margin-top: 24px; text-align: right">
          <a-button type="primary" @click="submitSelfEvaluation">提交自评</a-button>
        </div>
      </div>

      <div v-if="currentStep === 2">
        <a-empty v-if="pendingEvaluations.length === 0" description="暂无待评价的同事" />
        
        <div v-for="relation in pendingEvaluations" :key="relation.id" class="colleague-eval-item">
          <a-card size="small" :title="`评价: ${relation.evaluatedName}`">
            <a-form :label-col="{ span: 4 }">
              <div v-for="dim in dimensions" :key="dim.id">
                <a-form-item :label="dim.dimensionName">
                  <a-rate v-model:value="dimensionScores[relation.id][dim.id]" :count="dim.maxScore || 5" />
                </a-form-item>
              </div>
              <a-form-item label="闪光点">
                <a-textarea v-model:value="evalForms[relation.id].strengths" :rows="2" />
              </a-form-item>
              <a-form-item label="可提升的地方">
                <a-textarea v-model:value="evalForms[relation.id].improvements" :rows="2" />
              </a-form-item>
              <a-form-item label="综合评语">
                <a-textarea v-model:value="evalForms[relation.id].comment" :rows="3" />
              </a-form-item>
            </a-form>
            <div style="text-align: right">
              <a-button type="primary" size="small" @click="submitColleagueEval(relation)">提交评价</a-button>
            </div>
          </a-card>
        </div>
      </div>

      <div v-if="currentStep === 3">
        <a-empty v-if="!report" description="成长报告尚未生成" />
        <div v-else>
          <a-alert
            message="成长报告已生成"
            type="success"
            show-icon
            style="margin-bottom: 20px"
          />
          <a-button type="primary" @click="viewReport">查看详细报告</a-button>
        </div>
      </div>
    </a-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  getMyGoals,
  saveGoals as apiSaveGoals,
  submitGoals as apiSubmitGoals,
  getPendingColleagueEvaluations,
  submitColleagueEvaluation as apiSubmitColleagueEvaluation,
  submitSelfEvaluation as apiSubmitSelfEvaluation,
  getMyGrowthReport,
  getDimensions
} from '@/api/employeeEvaluation'

const route = useRoute()
const router = useRouter()
const cycleId = computed(() => route.params.id)
const currentStep = ref(0)
const goals = ref([])
const pendingEvaluations = ref([])
const dimensions = ref([])
const report = ref(null)
const dimensionScores = reactive({})
const evalForms = reactive({})

const selfEvalForm = reactive({
  summary: '',
  growth: '',
  challenges: '',
  improvements: ''
})

const addGoal = () => {
  goals.value.push({
    goalName: '',
    planContent: '',
    weight: 25,
    selfScore: 80,
    achievementDegree: 2,
    actualResult: ''
  })
}

const removeGoal = (index) => {
  goals.value.splice(index, 1)
}

const fetchGoals = async () => {
  const res = await getMyGoals(cycleId.value)
  goals.value = res.data || []
  if (goals.value.length === 0) {
    addGoal()
    addGoal()
    addGoal()
    addGoal()
  } else {
    goals.value = goals.value.map(g => ({
      ...g,
      selfScore: 80,
      achievementDegree: 2,
      actualResult: ''
    }))
  }
}

const saveGoals = async () => {
  try {
    await apiSaveGoals(cycleId.value, goals.value)
    message.success('保存成功')
  } catch (e) {
    console.error(e)
  }
}

const handleSubmitGoals = async () => {
  try {
    await apiSaveGoals(cycleId.value, goals.value)
    await apiSubmitGoals(cycleId.value)
    message.success('目标提交成功')
    currentStep.value = 1
  } catch (e) {
    console.error(e)
  }
}

const submitSelfEvaluation = async () => {
  try {
    const data = {
      cycleId: Number(cycleId.value),
      summary: selfEvalForm.summary,
      growth: selfEvalForm.growth,
      challenges: selfEvalForm.challenges,
      improvements: selfEvalForm.improvements,
      goalEvaluations: goals.value.map(g => ({
        goalId: g.id,
        selfScore: g.selfScore,
        achievementDegree: g.achievementDegree,
        actualResult: g.actualResult
      }))
    }
    await apiSubmitSelfEvaluation(data)
    message.success('自评提交成功')
    currentStep.value = 2
    fetchPendingEvaluations()
  } catch (e) {
    console.error(e)
  }
}

const fetchPendingEvaluations = async () => {
  const res = await getPendingColleagueEvaluations(cycleId.value)
  pendingEvaluations.value = res.data || []
  
  const dimRes = await getAdminDimensions(cycleId.value)
  dimensions.value = dimRes.data || []
  
  for (const r of pendingEvaluations.value) {
    evalForms[r.id] = {
      strengths: '',
      improvements: '',
      comment: ''
    }
    dimensionScores[r.id] = {}
    for (const dim of dimensions.value) {
      dimensionScores[r.id][dim.id] = 3
    }
  }
}

const getDimensionScore = (relationId, dimId) => {
  return dimensionScores[relationId]?.[dimId] || 3
}

const getEvalForm = (relationId) => {
  if (!evalForms[relationId]) {
    evalForms[relationId] = { strengths: '', improvements: '', comment: '' }
  }
  return evalForms[relationId]
}

const submitColleagueEval = async (relation) => {
  try {
    const data = {
      relationId: relation.id,
      cycleId: Number(cycleId.value),
      evaluatedId: relation.evaluatedId,
      comment: getEvalForm(relation.id).comment,
      strengths: getEvalForm(relation.id).strengths,
      improvements: getEvalForm(relation.id).improvements,
      dimensionScores: dimensions.value.map(d => ({
        dimensionId: d.id,
        dimensionName: d.dimensionName,
        score: getDimensionScore(relation.id, d.id)
      }))
    }
    await apiSubmitColleagueEvaluation(data)
    message.success('评价提交成功')
    relation.status = 1
  } catch (e) {
    console.error(e)
  }
}

const viewReport = () => {
  router.push(`/my-report/${cycleId.value}`)
}

const fetchReport = async () => {
  const res = await getMyGrowthReport(cycleId.value)
  report.value = res.data
}

onMounted(() => {
  fetchGoals()
  fetchPendingEvaluations()
})
</script>

<style scoped>
.goal-setting {
  padding: 20px;
}

.goal-item,
.goal-eval-item,
.colleague-eval-item {
  margin-bottom: 16px;
}
</style>

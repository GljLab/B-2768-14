<template>
  <div class="survey-edit-container">
    <div class="page-header">
      <a-button @click="goBack">
        <ArrowLeftOutlined /> 返回
      </a-button>
      <h2 class="page-title">{{ isEdit ? '编辑问卷' : '新建问卷' }}</h2>
    </div>

    <div class="main-content">
      <a-card :bordered="false" class="content-card">
        <a-form
          :model="formData"
          :label-col="{ span: 4 }"
          :wrapper-col="{ span: 16 }"
          layout="horizontal"
        >
          <a-form-item label="问卷标题" required>
            <a-input v-model:value="formData.title" placeholder="请输入问卷标题" maxlength="200" />
          </a-form-item>
          
          <a-form-item label="问卷说明">
            <a-textarea
              v-model:value="formData.description"
              placeholder="请输入问卷说明，向员工解释调查目的和填写要求"
              :rows="3"
              maxlength="500"
              show-count
            />
          </a-form-item>
          
          <a-form-item label="截止日期" required>
            <a-date-picker
              v-model:value="formData.deadline"
              show-time
              style="width: 100%"
              :disabled-date="disabledDate"
              placeholder="请选择截止日期"
            />
          </a-form-item>
          
          <a-form-item label="发放范围" required>
            <a-radio-group v-model:value="targetType" @change="handleTargetTypeChange">
              <a-radio :value="1">全体员工</a-radio>
              <a-radio :value="2">指定部门</a-radio>
            </a-radio-group>
            <div v-if="targetType === 2" class="dept-selector">
              <a-checkbox-group v-model:value="selectedDepts">
                <a-space direction="vertical">
                  <a-checkbox v-for="dept in departments" :key="dept.id" :value="dept.id">
                    {{ dept.name }}
                  </a-checkbox>
                </a-space>
              </a-checkbox-group>
            </div>
          </a-form-item>
          
          <a-form-item label="匿名设置">
            <a-switch v-model="isAnonymousSwitch" checked-children="匿名" un-checked-children="实名" @change="handleAnonymousChange" />
            <span class="switch-desc">开启后，管理员查看结果时看不到具体是谁填写的</span>
          </a-form-item>
        </a-form>
      </a-card>

      <a-card :bordered="false" class="content-card" style="margin-top: 16px">
        <template #title>
          <div class="card-title">
            <span>题目设置</span>
            <span class="title-hint">请添加问卷题目，支持评分题、单选题、开放题</span>
          </div>
        </template>
        
        <div class="question-list">
          <div
            v-for="(question, index) in formData.questions"
            :key="question.id || index"
            class="question-item"
          >
            <div class="question-header">
              <div class="question-type-tag">
                {{ getQuestionTypeLabel(question.questionType) }}
              </div>
              <span class="question-title-preview">Q{{ index + 1 }}. {{ question.title || '未设置题目' }}</span>
              <div class="question-actions">
                <a-button type="link" size="small" @click="moveQuestion(index, -1)" :disabled="index === 0">
                  <UpOutlined />
                </a-button>
                <a-button type="link" size="small" @click="moveQuestion(index, 1)" :disabled="index === formData.questions.length - 1">
                  <DownOutlined />
                </a-button>
                <a-button type="link" size="small" danger @click="deleteQuestion(index)">
                  <DeleteOutlined />
                </a-button>
              </div>
            </div>
            
            <div class="question-body">
              <a-form-item label="题目标题" :label-col="{ span: 3 }" :wrapper-col="{ span: 20 }">
                <a-input v-model:value="question.title" placeholder="请输入题目标题" />
              </a-form-item>
              
              <div v-if="question.questionType !== 3">
                <a-form-item label="选项设置" :label-col="{ span: 3 }" :wrapper-col="{ span: 20 }">
                  <div class="option-list">
                    <div v-for="(option, optIndex) in question.options" :key="optIndex" class="option-item">
                      <a-input
                        v-model:value="option.optionText"
                        :placeholder="question.questionType === 1 ? '评分项名称，如：工作环境' : '选项文本'"
                        style="flex: 1"
                      />
                      <a-button type="link" danger size="small" @click="deleteOption(question, optIndex)">
                        <MinusOutlined />
                      </a-button>
                    </div>
                    <a-button type="dashed" block @click="addOption(question)">
                      <PlusOutlined /> 添加选项
                    </a-button>
                  </div>
                </a-form-item>
              </div>
            </div>
          </div>
        </div>
        
        <div class="add-question-area">
          <a-space>
            <a-button type="dashed" @click="addQuestion(1)">
              <StarOutlined /> 添加评分题
            </a-button>
            <a-button type="dashed" @click="addQuestion(2)">
              <CheckCircleOutlined /> 添加单选题
            </a-button>
            <a-button type="dashed" @click="addQuestion(3)">
              <EditOutlined /> 添加开放题
            </a-button>
          </a-space>
        </div>
      </a-card>

      <div class="submit-area">
        <a-button @click="goBack" size="large">取消</a-button>
        <a-button type="primary" size="large" @click="handleSubmit" :loading="submitting">
          {{ isEdit ? '保存修改' : '创建问卷' }}
        </a-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  ArrowLeftOutlined,
  UpOutlined,
  DownOutlined,
  DeleteOutlined,
  PlusOutlined,
  MinusOutlined,
  StarOutlined,
  CheckCircleOutlined,
  EditOutlined
} from '@ant-design/icons-vue'
import { getSurveyDetail, createSurvey, updateSurvey } from '@/api/survey'
import { getFlatDepartmentList } from '@/api/department'
import dayjs from 'dayjs'

const router = useRouter()
const route = useRoute()

const isEdit = computed(() => !!route.params.id)
const submitting = ref(false)
const targetType = ref(1)
const selectedDepts = ref([])
const departments = ref([])
const isAnonymousSwitch = ref(false)

const formData = ref({
  title: '',
  description: '',
  deadline: null,
  isAnonymous: 0,
  targets: [],
  questions: []
})

const handleAnonymousChange = (checked) => {
  formData.value.isAnonymous = checked ? 1 : 0
}

const disabledDate = (current) => {
  return current && current < dayjs().startOf('day')
}

const getQuestionTypeLabel = (type) => {
  const labels = { 1: '评分题', 2: '单选题', 3: '开放题' }
  return labels[type] || '未知'
}

const handleTargetTypeChange = () => {
  if (targetType.value === 1) {
    selectedDepts.value = []
  }
}

const addQuestion = (type) => {
  const newQuestion = {
    id: null,
    questionType: type,
    title: '',
    sortOrder: formData.value.questions.length,
    required: 1,
    options: []
  }
  
  if (type !== 3) {
    newQuestion.options = [
      { id: null, optionText: '', sortOrder: 0 },
      { id: null, optionText: '', sortOrder: 1 }
    ]
  }
  
  formData.value.questions.push(newQuestion)
}

const deleteQuestion = (index) => {
  formData.value.questions.splice(index, 1)
}

const moveQuestion = (index, direction) => {
  const questions = formData.value.questions
  const newIndex = index + direction
  if (newIndex < 0 || newIndex >= questions.length) return
  
  const temp = questions[index]
  questions[index] = questions[newIndex]
  questions[newIndex] = temp
}

const addOption = (question) => {
  if (!question.options) {
    question.options = []
  }
  question.options.push({
    id: null,
    optionText: '',
    sortOrder: question.options.length
  })
}

const deleteOption = (question, index) => {
  question.options.splice(index, 1)
}

const buildTargets = () => {
  if (targetType.value === 1) {
    return [{ targetType: 1, targetId: null, targetName: '全体员工' }]
  } else {
    return selectedDepts.value.map(deptId => {
      const dept = departments.value.find(d => d.id === deptId)
      return {
        targetType: 2,
        targetId: deptId,
        targetName: dept ? dept.name : ''
      }
    })
  }
}

const validateForm = () => {
  if (!formData.value.title.trim()) {
    message.error('请输入问卷标题')
    return false
  }
  if (!formData.value.deadline) {
    message.error('请选择截止日期')
    return false
  }
  if (targetType.value === 2 && selectedDepts.value.length === 0) {
    message.error('请至少选择一个部门')
    return false
  }
  if (formData.value.questions.length === 0) {
    message.error('请至少添加一道题目')
    return false
  }
  for (const q of formData.value.questions) {
    if (!q.title.trim()) {
      message.error('请填写所有题目标题')
      return false
    }
    if (q.questionType !== 3 && (!q.options || q.options.length === 0)) {
      message.error('请为评分题/单选题添加选项')
      return false
    }
    if (q.options) {
      for (const opt of q.options) {
        if (!opt.optionText.trim()) {
          message.error('请填写所有选项文本')
          return false
        }
      }
    }
  }
  return true
}

const handleSubmit = async () => {
  if (!validateForm()) return
  
  submitting.value = true
  try {
    const submitData = {
      ...formData.value,
      deadline: formData.value.deadline ? formData.value.deadline.format('YYYY-MM-DD HH:mm:ss') : null,
      targets: buildTargets()
    }
    
    let res
    if (isEdit.value) {
      res = await updateSurvey(route.params.id, submitData)
    } else {
      res = await createSurvey(submitData)
    }
    
    if (res.code === 200) {
      message.success(isEdit.value ? '保存成功' : '创建成功')
      router.push('/surveys')
    } else {
      message.error(res.message || '操作失败')
    }
  } catch (e) {
    message.error('操作失败')
  } finally {
    submitting.value = false
  }
}

const goBack = () => {
  router.push('/surveys')
}

const loadDepartments = async () => {
  try {
    const res = await getFlatDepartmentList()
    if (res.code === 200) {
      departments.value = res.data
    }
  } catch (e) {
    console.error(e)
  }
}

const loadSurveyDetail = async () => {
  try {
    const res = await getSurveyDetail(route.params.id)
    if (res.code === 200) {
      const data = res.data
      formData.value = {
        title: data.title,
        description: data.description,
        deadline: dayjs(data.deadline),
        isAnonymous: data.isAnonymous,
        targets: data.targets || [],
        questions: data.questions || []
      }
      
      isAnonymousSwitch.value = data.isAnonymous === 1
      
      if (data.targets && data.targets.length > 0) {
        if (data.targets[0].targetType === 1) {
          targetType.value = 1
        } else {
          targetType.value = 2
          selectedDepts.value = data.targets.map(t => t.targetId)
        }
      }
    }
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  loadDepartments()
  if (isEdit.value) {
    loadSurveyDetail()
  }
})
</script>

<style scoped>
.survey-edit-container {
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.content-card {
  border-radius: 8px;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-hint {
  font-size: 12px;
  color: #999;
  font-weight: normal;
}

.switch-desc {
  margin-left: 12px;
  color: #666;
  font-size: 13px;
}

.dept-selector {
  margin-top: 12px;
  padding: 12px;
  background: #f5f5f5;
  border-radius: 4px;
}

.question-list {
  margin-bottom: 20px;
}

.question-item {
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  margin-bottom: 16px;
  overflow: hidden;
}

.question-header {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: #fafafa;
  border-bottom: 1px solid #e8e8e8;
}

.question-type-tag {
  padding: 2px 8px;
  background: #1890ff;
  color: white;
  border-radius: 4px;
  font-size: 12px;
  margin-right: 12px;
}

.question-title-preview {
  flex: 1;
  font-weight: 500;
}

.question-actions {
  display: flex;
  gap: 4px;
}

.question-body {
  padding: 16px;
}

.option-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.add-question-area {
  padding: 16px 0;
  border-top: 1px dashed #e8e8e8;
}

.submit-area {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 24px;
  padding: 20px;
  background: white;
  border-radius: 8px;
}
</style>

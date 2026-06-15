<template>
  <el-dialog
    v-model="visible"
    title="发起定制邀约"
    width="680px"
    :close-on-click-modal="false"
    @close="handleClose"
    class="invitation-dialog"
  >
    <div class="dialog-intro" v-if="workTitle">
      <el-alert
        type="info"
        :closable="false"
        show-icon
      >
        <template #title>
          基于作品「<strong>{{ workTitle }}</strong>」的风格发起定制邀约
        </template>
      </el-alert>
    </div>

    <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" class="invitation-form">
      <el-form-item label="邀约标题" prop="title">
        <el-input
          v-model="form.title"
          placeholder="请输入邀约标题，如：定制一款手工编织围巾"
          maxlength="100"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="定制需求" prop="requirements">
        <el-input
          v-model="form.requirements"
          type="textarea"
          :rows="5"
          placeholder="请详细描述您的定制需求，包括尺寸、颜色、风格、用途、材质偏好等"
          maxlength="2000"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="预算范围">
        <div class="budget-inputs">
          <el-input-number
            v-model="form.budgetMin"
            :min="0"
            :precision="2"
            :step="50"
            :controls-position="'right'"
            placeholder="最低预算"
            style="width: 180px"
          />
          <span class="separator">—</span>
          <el-input-number
            v-model="form.budgetMax"
            :min="0"
            :precision="2"
            :step="50"
            :controls-position="'right'"
            placeholder="最高预算"
            style="width: 180px"
          />
          <span class="unit">元</span>
        </div>
        <div class="quick-budgets">
          <el-tag
            v-for="budget in quickBudgets"
            :key="budget.label"
            size="small"
            :type="isBudgetSelected(budget) ? 'primary' : 'info'"
            :effect="isBudgetSelected(budget) ? 'dark' : 'plain'"
            class="budget-tag"
            @click="selectQuickBudget(budget)"
          >
            {{ budget.label }}
          </el-tag>
        </div>
        <div class="form-tip">选填，可留空表示面议</div>
      </el-form-item>

      <el-form-item label="期望周期">
        <div class="budget-inputs">
          <el-input-number
            v-model="form.expectedDays"
            :min="1"
            :max="365"
            :controls-position="'right'"
            placeholder="天数"
            style="width: 180px"
          />
          <span class="unit">天</span>
        </div>
        <div class="quick-periods">
          <el-tag
            v-for="period in quickPeriods"
            :key="period"
            size="small"
            :type="form.expectedDays === period ? 'primary' : 'info'"
            :effect="form.expectedDays === period ? 'dark' : 'plain'"
            class="budget-tag"
            @click="form.expectedDays = period"
          >
            {{ period }}天
          </el-tag>
        </div>
        <div class="form-tip">选填，可留空表示面议</div>
      </el-form-item>

      <el-form-item label="参考图片">
        <el-upload
          v-model:file-list="fileList"
          :action="uploadUrl"
          :headers="uploadHeaders"
          list-type="picture-card"
          :on-success="handleUploadSuccess"
          :on-remove="handleUploadRemove"
          :before-upload="beforeUpload"
          :limit="9"
          accept="image/*"
          class="upload-wrapper"
        >
          <el-icon><Plus /></el-icon>
        </el-upload>
        <div class="form-tip">最多上传9张参考图片，单张不超过5MB</div>
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose" size="large">取消</el-button>
        <el-button
          type="primary"
          size="large"
          :loading="submitting"
          @click="handleSubmit"
          class="submit-btn"
        >
          <el-icon><Promotion /></el-icon>
          发起邀约
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Promotion } from '@element-plus/icons-vue'
import request from '@/utils/request'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  creatorId: {
    type: Number,
    default: null
  },
  workId: {
    type: Number,
    default: null
  },
  workTitle: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = ref(props.modelValue)
const submitting = ref(false)
const formRef = ref(null)
const fileList = ref([])
const uploadedImages = ref([])

const uploadUrl = computed(() => {
  return '/api/file/upload'
})

const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
})

const quickBudgets = [
  { label: '100-300元', min: 100, max: 300 },
  { label: '300-500元', min: 300, max: 500 },
  { label: '500-1000元', min: 500, max: 1000 },
  { label: '1000-2000元', min: 1000, max: 2000 },
  { label: '2000元以上', min: 2000, max: null }
]

const quickPeriods = [3, 7, 14, 30, 60]

const form = reactive({
  title: '',
  requirements: '',
  budgetMin: null,
  budgetMax: null,
  expectedDays: null
})

const rules = {
  title: [{ required: true, message: '请输入邀约标题', trigger: 'blur' }],
  requirements: [{ required: true, message: '请输入定制需求', trigger: 'blur' }]
}

function isBudgetSelected(budget) {
  return form.budgetMin === budget.min &&
    ((budget.max === null && form.budgetMax === null) || form.budgetMax === budget.max)
}

function selectQuickBudget(budget) {
  form.budgetMin = budget.min
  form.budgetMax = budget.max
}

watch(() => props.modelValue, (val) => {
  visible.value = val
})

watch(visible, (val) => {
  emit('update:modelValue', val)
  if (!val) {
    resetForm()
  }
})

function resetForm() {
  form.title = ''
  form.requirements = ''
  form.budgetMin = null
  form.budgetMax = null
  form.expectedDays = null
  fileList.value = []
  uploadedImages.value = []
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

function beforeUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过5MB')
    return false
  }
  return true
}

function handleUploadSuccess(response, uploadFile) {
  if (response.code === 200 && response.data) {
    uploadedImages.value.push(response.data)
  } else {
    ElMessage.error('图片上传失败')
    fileList.value = fileList.value.filter(f => f.uid !== uploadFile.uid)
  }
}

function handleUploadRemove(uploadFile) {
  const index = fileList.value.findIndex(f => f.uid === uploadFile.uid)
  if (index > -1) {
    uploadedImages.value.splice(index, 1)
  }
}

async function handleSubmit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }
  if (form.budgetMin && form.budgetMax && form.budgetMin > form.budgetMax) {
    ElMessage.warning('预算下限不能大于上限')
    return
  }
  submitting.value = true
  try {
    const payload = {
      creatorId: props.creatorId,
      workId: props.workId,
      title: form.title,
      requirements: form.requirements
    }
    if (form.budgetMin !== null) payload.budgetMin = form.budgetMin
    if (form.budgetMax !== null) payload.budgetMax = form.budgetMax
    if (form.expectedDays !== null) payload.expectedDays = form.expectedDays
    if (uploadedImages.value.length > 0) {
      payload.referenceImages = JSON.stringify(uploadedImages.value)
    }

    const res = await request.post('/invitation', payload)
    if (res.code === 200 && res.data) {
      ElMessage.success('邀约已发送')
      emit('success', res.data)
      visible.value = false
    } else {
      ElMessage.error(res.message || '发送失败')
    }
  } catch (e) {
    console.error('发起邀约失败', e)
  } finally {
    submitting.value = false
  }
}

function handleClose() {
  visible.value = false
}
</script>

<style scoped>
.invitation-dialog :deep(.el-dialog__header) {
  padding: 24px 32px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  margin: 0;
}

.invitation-dialog :deep(.el-dialog__title) {
  color: #fff;
  font-size: 20px;
  font-weight: 600;
}

.invitation-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: #fff;
  font-size: 20px;
}

.invitation-dialog :deep(.el-dialog__body) {
  padding: 24px 32px;
}

.dialog-intro {
  margin-bottom: 20px;
}

.dialog-intro :deep(.el-alert__title) {
  font-size: 14px;
}

.invitation-form {
  margin-top: 8px;
}

.budget-inputs {
  display: flex;
  align-items: center;
  gap: 12px;
}

.separator {
  color: #909399;
  font-weight: 500;
}

.unit {
  color: #606266;
  font-weight: 500;
}

.quick-budgets,
.quick-periods {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}

.budget-tag {
  cursor: pointer;
  transition: all 0.2s;
}

.budget-tag:hover {
  transform: translateY(-1px);
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 6px;
}

.upload-wrapper {
  width: 100%;
}

.upload-wrapper :deep(.el-upload--picture-card) {
  width: 90px;
  height: 90px;
}

.upload-wrapper :deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 90px;
  height: 90px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.submit-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 12px 28px;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.45);
}
</style>

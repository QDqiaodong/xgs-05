<template>
  <el-dialog
    v-model="visible"
    title="发起定制邀约"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
      <el-form-item label="邀约标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入邀约标题，如：定制一款手工编织围巾" maxlength="100" show-word-limit />
      </el-form-item>
      <el-form-item label="定制需求" prop="requirements">
        <el-input
          v-model="form.requirements"
          type="textarea"
          :rows="5"
          placeholder="请详细描述您的定制需求，包括尺寸、颜色、风格、用途等"
          maxlength="1000"
          show-word-limit
        />
      </el-form-item>
      <el-form-item label="预算范围">
        <div class="budget-inputs">
          <el-input-number v-model="form.budgetMin" :min="0" :precision="2" placeholder="最低" />
          <span class="separator">—</span>
          <el-input-number v-model="form.budgetMax" :min="0" :precision="2" placeholder="最高" />
          <span class="unit">元</span>
        </div>
        <div class="form-tip">选填，可留空表示面议</div>
      </el-form-item>
      <el-form-item label="期望周期">
        <div class="budget-inputs">
          <el-input-number v-model="form.expectedDays" :min="1" placeholder="天数" />
          <span class="unit">天</span>
        </div>
        <div class="form-tip">选填，可留空表示面议</div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">
        发起邀约
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
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
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = ref(props.modelValue)
const submitting = ref(false)
const formRef = ref(null)

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
  if (formRef.value) {
    formRef.value.clearValidate()
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
.budget-inputs {
  display: flex;
  align-items: center;
  gap: 12px;
}

.separator {
  color: #909399;
}

.unit {
  color: #606266;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>

<template>
  <el-dialog
    :model-value="modelValue"
    @update:model-value="(val) => emit('update:modelValue', val)"
    title="申请创作者认证"
    width="600px"
    :close-on-click-modal="false"
  >
    <div class="verify-intro">
      <el-alert
        type="info"
        :closable="false"
        show-icon
        title="成为认证创作者，享受更多权益"
      >
        <template #default>
          <div class="benefits">
            <div>✓ 头像旁显示认证标识，彰显身份</div>
            <div>✓ 作品获得更多曝光推荐权重（提升30%）</div>
            <div>✓ 优先获得平台活动和合作机会</div>
          </div>
        </template>
      </el-alert>
    </div>

    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      class="verify-form"
    >
      <el-form-item label="真实姓名" prop="realName">
        <el-input v-model="form.realName" placeholder="请输入您的真实姓名" maxlength="50" />
      </el-form-item>

      <el-form-item label="联系方式" prop="contactInfo">
        <el-input v-model="form.contactInfo" placeholder="手机号或邮箱，方便我们联系您" maxlength="200" />
      </el-form-item>

      <el-form-item label="擅长领域" prop="expertiseField">
        <el-input
          v-model="form.expertiseField"
          placeholder="例如：手工编织、陶艺制作、布艺缝纫等"
          maxlength="500"
        />
      </el-form-item>

      <el-form-item label="作品集链接" prop="portfolioLinks">
        <div class="link-list">
          <div
            v-for="(link, index) in form.portfolioLinks"
            :key="index"
            class="link-item"
          >
            <el-input
              v-model="form.portfolioLinks[index]"
              placeholder="请输入链接地址"
            />
            <el-button
              type="danger"
              text
              @click="removeLink(index)"
              :disabled="form.portfolioLinks.length <= 1"
            >
              删除
            </el-button>
          </div>
          <el-button type="primary" text @click="addLink">
            + 添加链接
          </el-button>
        </div>
      </el-form-item>

      <el-form-item label="创作经历" prop="creationExperience">
        <el-input
          v-model="form.creationExperience"
          type="textarea"
          :rows="5"
          placeholder="请描述您的创作经历、获奖情况、代表作品等（不少于50字）"
          maxlength="2000"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="补充材料">
        <el-input
          v-model="form.additionalMaterials"
          type="textarea"
          :rows="3"
          placeholder="其他您想补充说明的内容（选填）"
          maxlength="500"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button
        type="primary"
        :loading="submitting"
        @click="handleSubmit"
      >
        提交申请
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
  }
})
const emit = defineEmits(['update:modelValue', 'success'])

const formRef = ref(null)
const submitting = ref(false)

const form = reactive({
  realName: '',
  contactInfo: '',
  expertiseField: '',
  portfolioLinks: [''],
  creationExperience: '',
  additionalMaterials: ''
})

const rules = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  contactInfo: [
    { required: true, message: '请输入联系方式', trigger: 'blur' }
  ],
  expertiseField: [
    { required: true, message: '请输入擅长领域', trigger: 'blur' }
  ],
  creationExperience: [
    { required: true, message: '请输入创作经历', trigger: 'blur' },
    { min: 50, message: '创作经历不少于50字', trigger: 'blur' }
  ]
}

function addLink() {
  form.portfolioLinks.push('')
}

function removeLink(index) {
  if (form.portfolioLinks.length > 1) {
    form.portfolioLinks.splice(index, 1)
  }
}

function resetForm() {
  form.realName = ''
  form.contactInfo = ''
  form.expertiseField = ''
  form.portfolioLinks = ['']
  form.creationExperience = ''
  form.additionalMaterials = ''
  formRef.value?.clearValidate()
}

watch(() => props.modelValue, (val) => {
  if (val) {
    resetForm()
  }
})

function handleClose() {
  emit('update:modelValue', false)
}

async function handleSubmit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }

  const validLinks = form.portfolioLinks.filter(l => l && l.trim())
  if (validLinks.length === 0) {
    ElMessage.warning('请至少填写一个作品集链接')
    return
  }

  submitting.value = true
  try {
    const payload = {
      realName: form.realName,
      contactInfo: form.contactInfo,
      expertiseField: form.expertiseField,
      portfolioLinks: JSON.stringify(validLinks),
      creationExperience: form.creationExperience,
      additionalMaterials: form.additionalMaterials || ''
    }
    const res = await request.post('/creator-verification/submit', payload)
    if (res.code === 200) {
      ElMessage.success('申请提交成功，请等待审核')
      emit('success')
      emit('update:modelValue', false)
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch (e) {
    console.error('提交认证申请失败', e)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.verify-intro {
  margin-bottom: 24px;
}

.benefits {
  margin-top: 8px;
  font-size: 13px;
  line-height: 1.8;
  color: #606266;
}

.verify-form {
  margin-top: 16px;
}

.link-list {
  width: 100%;
}

.link-item {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
  align-items: center;
}

.link-item .el-input {
  flex: 1;
}
</style>

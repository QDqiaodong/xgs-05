<template>
  <div class="publish container">
    <div class="publish-card card">
      <h2 class="page-title">发布手作作品</h2>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" class="publish-form">
        <el-form-item label="作品标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入作品标题" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="作品图片" prop="images">
          <el-upload
            v-model:file-list="fileList"
            class="upload-demo"
            action="#"
            list-type="picture-card"
            :auto-upload="false"
            :on-preview="handlePreview"
            :on-remove="handleRemove"
            :on-change="handleChange"
            :limit="9"
            accept="image/*"
          >
            <el-icon><Plus /></el-icon>
            <template #tip>
              <div class="el-upload__tip">
                最多上传9张图片，支持jpg、png格式，单张不超过5MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item label="手作品类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择手作品类" style="width: 200px;">
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="制作周期" prop="productionCycle">
          <el-input v-model="form.productionCycle" placeholder="如：7天、1个月" style="width: 200px;" />
        </el-form-item>
        <el-form-item label="用料清单" prop="materials">
          <el-input
            v-model="form.materials"
            type="textarea"
            :rows="3"
            placeholder="请详细列出制作所需材料，如：纯羊毛线500g，棒针一副"
          />
        </el-form-item>
        <el-form-item label="创作思路" prop="creationIdea">
          <el-input
            v-model="form.creationIdea"
            type="textarea"
            :rows="4"
            placeholder="分享你的创作灵感、设计理念和制作心得"
          />
        </el-form-item>
        <el-form-item label="作品描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请详细描述作品的特点、用途等信息"
          />
        </el-form-item>
        <el-form-item label="制作步骤">
          <div class="steps-editor">
            <div class="steps-header">
              <span class="steps-tip">点击下方按钮添加制作步骤，帮助学习者更好地跟随教程</span>
              <el-button type="primary" plain :icon="Plus" size="small" @click="addStep">
                添加步骤
              </el-button>
            </div>
            <div class="steps-list" v-if="form.steps.length > 0">
              <div v-for="(step, index) in form.steps" :key="index" class="step-editor-item">
                <div class="step-editor-header">
                  <div class="step-editor-number">
                    <span>步骤 {{ index + 1 }}</span>
                  </div>
                  <div class="step-editor-actions">
                    <el-button
                      v-if="index > 0"
                      type="default"
                      size="small"
                      :icon="Top"
                      text
                      @click="moveStep(index, -1)"
                    >
                      上移
                    </el-button>
                    <el-button
                      v-if="index < form.steps.length - 1"
                      type="default"
                      size="small"
                      :icon="Bottom"
                      text
                      @click="moveStep(index, 1)"
                    >
                      下移
                    </el-button>
                    <el-button
                      type="danger"
                      size="small"
                      :icon="Delete"
                      text
                      @click="removeStep(index)"
                    >
                      删除
                    </el-button>
                  </div>
                </div>
                <div class="step-editor-body">
                  <el-form-item label="步骤标题" :prop="'steps.' + index + '.title'" :rules="{ required: true, message: '请输入步骤标题', trigger: 'blur' }">
                    <el-input v-model="step.title" placeholder="如：准备材料与工具" maxlength="50" show-word-limit />
                  </el-form-item>
                  <el-form-item label="步骤图片">
                    <el-upload
                      v-model:file-list="step.fileList"
                      class="step-upload"
                      action="#"
                      list-type="picture-card"
                      :auto-upload="false"
                      :on-preview="(file) => handleStepPreview(file, index)"
                      :on-remove="(file, uploadFiles) => handleStepRemove(file, uploadFiles, index)"
                      :on-change="(file, uploadFiles) => handleStepChange(file, uploadFiles, index)"
                      :limit="1"
                      accept="image/*"
                    >
                      <el-icon><Plus /></el-icon>
                    </el-upload>
                  </el-form-item>
                  <el-form-item label="步骤描述" :prop="'steps.' + index + '.description'" :rules="{ required: true, message: '请输入步骤描述', trigger: 'blur' }">
                    <el-input
                      v-model="step.description"
                      type="textarea"
                      :rows="4"
                      placeholder="详细描述这一步的操作方法、注意事项等"
                      maxlength="1000"
                      show-word-limit
                    />
                  </el-form-item>
                  <el-form-item label="小贴士">
                    <div class="tips-editor">
                      <div v-for="(tip, tipIdx) in step.tips" :key="tipIdx" class="tip-item">
                        <el-input v-model="step.tips[tipIdx]" placeholder="输入一条实用小技巧" />
                        <el-button type="danger" text :icon="Close" @click="removeTip(index, tipIdx)" />
                      </div>
                      <el-button type="primary" plain size="small" :icon="Plus" @click="addTip(index)">
                        添加小贴士
                      </el-button>
                    </div>
                  </el-form-item>
                </div>
              </div>
            </div>
            <div v-else class="steps-empty">
              <el-icon :size="48" color="#c0c4cc"><DocumentAdd /></el-icon>
              <p>还没有添加步骤，点击「添加步骤」开始创建教程</p>
            </div>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">发布作品</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    <el-dialog v-model="previewVisible" title="图片预览" width="80%">
      <img w-full :src="previewUrl" alt="预览图片" style="width: 100%;" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Top, Bottom, Delete, Close, DocumentAdd } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const submitting = ref(false)
const previewVisible = ref(false)
const previewUrl = ref('')
const fileList = ref([])
const uploadedImages = ref([])

const categories = ref([
  { id: 1, name: '编织' },
  { id: 2, name: '陶艺' },
  { id: 3, name: '布艺' },
  { id: 4, name: '木艺' }
])

function createEmptyStep() {
  return {
    title: '',
    description: '',
    image: '',
    tips: [],
    fileList: [],
    uploadedImage: ''
  }
}

const form = reactive({
  title: '',
  categoryId: null,
  productionCycle: '',
  materials: '',
  creationIdea: '',
  description: '',
  steps: [createEmptyStep()]
})

const rules = {
  title: [
    { required: true, message: '请输入作品标题', trigger: 'blur' },
    { min: 2, max: 50, message: '标题长度在2到50个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择手作品类', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入作品描述', trigger: 'blur' }
  ]
}

async function uploadImageFile(file) {
  const formData = new FormData()
  formData.append('files', file.raw || file)
  const res = await request.post('/file/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
  if (res.code === 200 && res.data && res.data.length > 0) {
    return res.data[0]
  }
  throw new Error('上传失败')
}

function handlePreview(file) {
  previewUrl.value = file.url
  previewVisible.value = true
}

function handleRemove(file, uploadFiles) {
  fileList.value = uploadFiles
  uploadedImages.value = uploadFiles.map(f => f.uploadedUrl || f.url).filter(Boolean)
}

async function handleChange(file, uploadFiles) {
  fileList.value = uploadFiles
  if (file.raw && !file.uploadedUrl) {
    try {
      const url = await uploadImageFile(file)
      file.uploadedUrl = url
      uploadedImages.value = fileList.value.map(f => f.uploadedUrl || f.url).filter(Boolean)
      ElMessage.success('图片上传成功')
    } catch (e) {
      ElMessage.error('图片上传失败')
    }
  }
}

function handleStepPreview(file, stepIndex) {
  previewUrl.value = file.url
  previewVisible.value = true
}

function handleStepRemove(file, uploadFiles, stepIndex) {
  form.steps[stepIndex].fileList = uploadFiles
  form.steps[stepIndex].uploadedImage = uploadFiles[0]?.uploadedUrl || uploadFiles[0]?.url || ''
  if (!form.steps[stepIndex].uploadedImage) {
    form.steps[stepIndex].image = ''
  }
}

async function handleStepChange(file, uploadFiles, stepIndex) {
  form.steps[stepIndex].fileList = uploadFiles
  if (file.raw && !file.uploadedUrl) {
    try {
      const url = await uploadImageFile(file)
      file.uploadedUrl = url
      form.steps[stepIndex].uploadedImage = url
      form.steps[stepIndex].image = url
      ElMessage.success('步骤图片上传成功')
    } catch (e) {
      ElMessage.error('步骤图片上传失败')
    }
  }
}

function addStep() {
  form.steps.push(createEmptyStep())
}

function removeStep(index) {
  if (form.steps.length <= 1) {
    ElMessage.warning('至少保留一个步骤')
    return
  }
  ElMessageBox.confirm('确定要删除这个步骤吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    form.steps.splice(index, 1)
    ElMessage.success('已删除步骤')
  }).catch(() => {})
}

function moveStep(index, direction) {
  const targetIndex = index + direction
  if (targetIndex < 0 || targetIndex >= form.steps.length) return
  const temp = form.steps[index]
  form.steps[index] = form.steps[targetIndex]
  form.steps[targetIndex] = temp
}

function addTip(stepIndex) {
  form.steps[stepIndex].tips.push('')
}

function removeTip(stepIndex, tipIndex) {
  form.steps[stepIndex].tips.splice(tipIndex, 1)
}

function buildStepsPayload() {
  return form.steps
    .filter(s => s.title && s.description)
    .map(s => ({
      title: s.title,
      description: s.description,
      image: s.uploadedImage || s.image || '',
      tips: (s.tips || []).filter(t => t && t.trim())
    }))
}

async function handleSubmit() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再发布作品')
    return
  }

  formRef.value.validate(async (valid) => {
    if (!valid) return

    if (uploadedImages.value.length === 0) {
      ElMessage.warning('请至少上传一张作品图片')
      return
    }

    const validSteps = form.steps.filter(s => s.title && s.description)
    if (form.steps.length > 0 && validSteps.length === 0) {
      ElMessage.warning('请填写步骤的标题和描述，或删除空步骤')
      return
    }

    submitting.value = true
    try {
      const payload = {
        userId: userStore.userInfo?.id || 1,
        title: form.title,
        categoryId: form.categoryId,
        productionCycle: form.productionCycle,
        materials: form.materials,
        creationIdea: form.creationIdea,
        description: form.description,
        coverImage: uploadedImages.value[0] || '',
        images: JSON.stringify(uploadedImages.value),
        steps: JSON.stringify(buildStepsPayload()),
        viewCount: 0,
        favoriteCount: 0,
        likeCount: 0,
        status: 1,
        isHot: 0
      }

      const res = await request.post('/work', payload)
      if (res.code === 200) {
        ElMessage.success('作品发布成功！')
        setTimeout(() => {
          router.push(`/work/${res.data}`)
        }, 800)
      } else {
        ElMessage.error(res.message || '发布失败')
      }
    } catch (e) {
      ElMessage.error(e.response?.data?.message || '发布失败，请稍后重试')
    } finally {
      submitting.value = false
    }
  })
}

function resetForm() {
  formRef.value.resetFields()
  fileList.value = []
  uploadedImages.value = []
  form.steps = [createEmptyStep()]
}

onMounted(() => {
  userStore.initFromStorage()
})
</script>

<style scoped>
.publish {
  padding: 30px 0;
}

.publish-card {
  max-width: 800px;
  margin: 0 auto;
  padding: 40px;
}

.page-title {
  font-size: 24px;
  color: #333;
  margin-bottom: 30px;
  text-align: center;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.publish-form {
  padding: 0 40px;
}

.upload-demo :deep(.el-upload--picture-card) {
  width: 100px;
  height: 100px;
  line-height: 100px;
}

.upload-demo :deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 100px;
  height: 100px;
}

.el-upload__tip {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}

.steps-editor {
  width: 100%;
}

.steps-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.steps-tip {
  font-size: 13px;
  color: #909399;
}

.steps-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.step-editor-item {
  border: 1px solid #e4e7ed;
  border-radius: 12px;
  overflow: hidden;
  transition: box-shadow 0.3s;
}

.step-editor-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.step-editor-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf3 100%);
  border-bottom: 1px solid #ebeef5;
}

.step-editor-number {
  display: flex;
  align-items: center;
  gap: 8px;
}

.step-editor-number span {
  font-size: 15px;
  font-weight: 600;
  color: #667eea;
}

.step-editor-actions {
  display: flex;
  gap: 4px;
}

.step-editor-body {
  padding: 20px;
}

.step-editor-body :deep(.el-form-item) {
  margin-bottom: 16px;
}

.step-editor-body :deep(.el-form-item:last-child) {
  margin-bottom: 0;
}

.step-upload :deep(.el-upload--picture-card) {
  width: 100px;
  height: 100px;
  line-height: 100px;
}

.step-upload :deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 100px;
  height: 100px;
}

.tips-editor {
  width: 100%;
}

.tip-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.tip-item :deep(.el-input) {
  flex: 1;
}

.steps-empty {
  padding: 60px 40px;
  text-align: center;
  background: #fafafa;
  border: 2px dashed #dcdfe6;
  border-radius: 12px;
  color: #909399;
}

.steps-empty p {
  margin-top: 12px;
  font-size: 14px;
}
</style>

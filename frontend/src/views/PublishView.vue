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
        <el-form-item label="用料清单" prop="materials" class="materials-form-item">
          <div class="materials-editor">
            <div class="materials-tags" v-if="materialTags.length > 0">
              <el-tag
                v-for="(tag, index) in materialTags"
                :key="index"
                closable
                type="primary"
                effect="light"
                class="material-tag"
                @close="removeMaterialTag(index)"
              >
                {{ tag }}
              </el-tag>
            </div>
            <el-autocomplete
              v-model="materialInput"
              :fetch-suggestions="queryMaterialSuggestions"
              placeholder="输入用料名称，支持智能联想，回车或点击添加"
              class="material-autocomplete"
              :trigger-on-focus="true"
              @select="handleMaterialSelect"
              @keyup.enter="handleMaterialEnter"
              popper-class="material-suggest-popper"
            >
              <template #default="{ item }">
                <div class="material-suggest-item">
                  <span class="material-suggest-name">{{ item.name }}</span>
                  <span class="material-suggest-count" v-if="item.count > 0">
                    已有 {{ item.count }} 人使用
                  </span>
                </div>
              </template>
            </el-autocomplete>
            <div class="materials-hot">
              <div class="materials-hot-label">
                <el-icon><TrendCharts /></el-icon>
                <span>热门用料（点击快速添加）</span>
              </div>
              <div class="materials-hot-tags">
                <el-tag
                  v-for="item in hotMaterials"
                  :key="item.name"
                  class="hot-tag"
                  effect="plain"
                  :type="isMaterialAdded(item.name) ? 'info' : 'success'"
                  @click="toggleHotMaterial(item.name)"
                >
                  {{ item.name }}
                  <el-icon v-if="isMaterialAdded(item.name)" class="hot-tag-check"><Check /></el-icon>
                </el-tag>
              </div>
            </div>
            <div class="materials-batch">
              <el-button
                type="primary"
                plain
                size="small"
                :icon="Plus"
                :disabled="hotMaterials.length === 0"
                @click="addAllHotMaterials"
              >
                一键添加全部热门用料
              </el-button>
              <el-button
                type="default"
                plain
                size="small"
                :icon="Delete"
                :disabled="materialTags.length === 0"
                @click="clearAllMaterials"
              >
                清空所有用料
              </el-button>
            </div>
          </div>
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
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Top, Bottom, Delete, Close, DocumentAdd, TrendCharts, Check } from '@element-plus/icons-vue'
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

const materialInput = ref('')
const materialTags = ref([])
const hotMaterials = ref([])
const loadingSuggest = ref(false)
const suggestCache = ref({})

watch(materialTags, (val) => {
  form.materials = val.join('，')
}, { immediate: true })

function isMaterialAdded(name) {
  return materialTags.value.includes(name)
}

async function fetchHotMaterials() {
  try {
    const res = await request.get('/work/materials/suggest', { params: { limit: 12 } })
    if (res.code === 200 && res.data) {
      hotMaterials.value = res.data
    }
  } catch (e) {
    console.warn('获取热门用料失败', e)
    hotMaterials.value = [
      { name: '纯羊毛线500g', count: 5 },
      { name: '棒针一副', count: 5 },
      { name: '钩针', count: 4 },
      { name: '陶土500g', count: 4 },
      { name: '纯棉布料', count: 3 },
      { name: '松木', count: 3 }
    ]
  }
}

async function queryMaterialSuggestions(queryString, cb) {
  if (!queryString || !queryString.trim()) {
    cb(hotMaterials.value.slice(0, 8))
    return
  }
  const keyword = queryString.trim()
  if (suggestCache.value[keyword]) {
    cb(suggestCache.value[keyword])
    return
  }
  loadingSuggest.value = true
  try {
    const res = await request.get('/work/materials/suggest', { params: { keyword, limit: 10 } })
    let list = []
    if (res.code === 200 && res.data) {
      list = res.data
      suggestCache.value[keyword] = list
    }
    cb(list)
  } catch (e) {
    cb([])
  } finally {
    loadingSuggest.value = false
  }
}

function addMaterialTag(name) {
  const trimmed = (name || '').trim()
  if (!trimmed) return
  if (materialTags.value.includes(trimmed)) {
    ElMessage.warning('该用料已添加')
    return
  }
  materialTags.value.push(trimmed)
}

function handleMaterialSelect(item) {
  addMaterialTag(item.name)
  materialInput.value = ''
}

function handleMaterialEnter() {
  if (materialInput.value && materialInput.value.trim()) {
    const parts = materialInput.value.split(/[,，、;；]+/)
    parts.forEach(p => addMaterialTag(p))
    materialInput.value = ''
  }
}

function removeMaterialTag(index) {
  materialTags.value.splice(index, 1)
}

function toggleHotMaterial(name) {
  if (isMaterialAdded(name)) {
    const idx = materialTags.value.indexOf(name)
    if (idx > -1) materialTags.value.splice(idx, 1)
  } else {
    addMaterialTag(name)
  }
}

function addAllHotMaterials() {
  let addedCount = 0
  hotMaterials.value.forEach(item => {
    if (!materialTags.value.includes(item.name)) {
      materialTags.value.push(item.name)
      addedCount++
    }
  })
  if (addedCount > 0) {
    ElMessage.success(`已批量添加 ${addedCount} 个热门用料`)
  } else {
    ElMessage.info('所有热门用料已添加')
  }
}

function clearAllMaterials() {
  if (materialTags.value.length === 0) return
  ElMessageBox.confirm('确定要清空所有已添加的用料吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    materialTags.value = []
    ElMessage.success('已清空所有用料')
  }).catch(() => {})
}

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
  materialTags.value = []
  materialInput.value = ''
  form.steps = [createEmptyStep()]
}

onMounted(() => {
  userStore.initFromStorage()
  fetchHotMaterials()
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

.materials-form-item :deep(.el-form-item__content) {
  width: 100%;
}

.materials-editor {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.materials-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.material-tag {
  font-size: 13px;
}

.material-autocomplete {
  width: 100%;
}

.materials-hot {
  padding: 12px 16px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border-radius: 8px;
  border: 1px solid #bae6fd;
}

.materials-hot-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: #0369a1;
  margin-bottom: 10px;
}

.materials-hot-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.hot-tag {
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s;
  user-select: none;
}

.hot-tag:hover {
  transform: translateY(-1px);
}

.hot-tag-check {
  margin-left: 4px;
}

.materials-batch {
  display: flex;
  gap: 8px;
}
</style>

<style>
.material-suggest-popper {
  padding: 6px 0;
}

.material-suggest-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  font-size: 14px;
}

.material-suggest-item:hover {
  background: #f5f7fa;
}

.material-suggest-name {
  color: #303133;
}

.material-suggest-count {
  font-size: 12px;
  color: #909399;
}
</style>

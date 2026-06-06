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
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const formRef = ref(null)
const submitting = ref(false)
const previewVisible = ref(false)
const previewUrl = ref('')
const fileList = ref([])

const categories = ref([
  { id: 1, name: '编织' },
  { id: 2, name: '陶艺' },
  { id: 3, name: '布艺' },
  { id: 4, name: '木艺' }
])

const form = reactive({
  title: '',
  categoryId: null,
  productionCycle: '',
  materials: '',
  creationIdea: '',
  description: ''
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

function handlePreview(file) {
  previewUrl.value = file.url
  previewVisible.value = true
}

function handleRemove(file, uploadFiles) {
  fileList.value = uploadFiles
}

function handleChange(file, uploadFiles) {
  fileList.value = uploadFiles
}

function handleSubmit() {
  formRef.value.validate((valid) => {
    if (valid) {
      if (fileList.value.length === 0) {
        ElMessage.warning('请至少上传一张作品图片')
        return
      }
      submitting.value = true
      setTimeout(() => {
        ElMessage.success('作品发布成功！')
        submitting.value = false
        router.push('/')
      }, 1500)
    }
  })
}

function resetForm() {
  formRef.value.resetFields()
  fileList.value = []
}
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
</style>

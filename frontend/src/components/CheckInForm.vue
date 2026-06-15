<template>
  <div class="checkin-form card">
    <div class="form-header">
      <div class="header-left">
        <div class="date-badge">
          <span class="date-icon">📅</span>
          <span class="date-text">{{ todayStr }}</span>
        </div>
        <h2 class="form-title">{{ hasCheckedIn ? '更新今日打卡' : '今日创作打卡' }}</h2>
      </div>
      <div v-if="hasCheckedIn" class="checked-badge">
        <el-icon><CircleCheckFilled /></el-icon>
        <span>已打卡</span>
      </div>
    </div>

    <el-form :model="form" :rules="rules" ref="formRef" label-width="90px" class="checkin-form-body">
      <el-form-item label="打卡标题" prop="title">
        <el-input v-model="form.title" placeholder="今天做了什么？如：完成毛衣第三部分" maxlength="50" show-word-limit />
      </el-form-item>

      <el-form-item label="进度照片" prop="images">
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
              最多上传9张图片，记录创作进度
            </div>
          </template>
        </el-upload>
      </el-form-item>

      <el-form-item label="创作时长">
        <div class="duration-input">
          <el-input-number v-model="form.workDuration" :min="0" :max="1440" :step="15" />
          <span class="duration-unit">分钟</span>
          <div class="duration-quick">
            <el-tag v-for="t in quickDurations" :key="t" class="quick-tag" effect="plain" @click="form.workDuration = t">
              {{ t }}分钟
            </el-tag>
          </div>
        </div>
      </el-form-item>

      <el-form-item label="创作心得" prop="content">
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="5"
          placeholder="记录今天的创作心得、遇到的问题、收获的灵感..."
          maxlength="1000"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="心情标签">
        <div class="tags-group">
          <el-tag
            v-for="mood in moodTags"
            :key="mood"
            :class="{ 'tag-selected': form.moodTag === mood }"
            class="selectable-tag"
            :effect="form.moodTag === mood ? 'dark' : 'plain'"
            @click="form.moodTag = form.moodTag === mood ? '' : mood"
          >
            {{ mood }}
          </el-tag>
        </div>
      </el-form-item>

      <el-form-item label="环境标签">
        <div class="tags-group">
          <el-tag
            v-for="weather in weatherTags"
            :key="weather"
            :class="{ 'tag-selected': form.weatherTag === weather }"
            class="selectable-tag"
            :effect="form.weatherTag === weather ? 'dark' : 'plain'"
            @click="form.weatherTag = form.weatherTag === weather ? '' : weather"
          >
            {{ weather }}
          </el-tag>
        </div>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="handleSubmit" :loading="submitting" class="submit-btn">
          <el-icon><EditPen /></el-icon>
          <span>{{ hasCheckedIn ? '更新打卡' : '提交打卡' }}</span>
        </el-button>
        <el-button @click="resetForm">重置</el-button>
      </el-form-item>
    </el-form>

    <el-dialog v-model="previewVisible" title="图片预览" width="80%">
      <img w-full :src="previewUrl" alt="预览图片" style="width: 100%;" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { Plus, CircleCheckFilled, EditPen } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'

const emit = defineEmits(['checkin-success'])

const userStore = useUserStore()
const formRef = ref(null)
const submitting = ref(false)
const previewVisible = ref(false)
const previewUrl = ref('')
const fileList = ref([])
const uploadedImages = ref([])
const hasCheckedIn = ref(false)

const todayStr = computed(() => {
  const d = new Date()
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日`
})

const quickDurations = [30, 60, 90, 120, 180]
const moodTags = ['😊 开心', '🤔 思考', '😌 平静', '💪 专注', '🎉 成就感', '😓 困难', '💡 灵感迸发']
const weatherTags = ['☀️ 晴天', '🌧️ 雨天', '🌙 夜晚', '🏠 家中', '☕ 咖啡馆', '🎵 听音乐', '🌸 户外']

const form = reactive({
  title: '',
  content: '',
  workDuration: 60,
  moodTag: '',
  weatherTag: ''
})

const rules = {
  title: [
    { required: true, message: '请输入打卡标题', trigger: 'blur' }
  ]
}

async function loadTodayRecord() {
  if (!userStore.isLoggedIn) return
  try {
    const res = await request.get('/checkin/today')
    if (res.code === 200 && res.data) {
      hasCheckedIn.value = true
      form.title = res.data.title || ''
      form.content = res.data.content || ''
      form.workDuration = res.data.workDuration || 0
      form.moodTag = res.data.moodTag || ''
      form.weatherTag = res.data.weatherTag || ''

      if (res.data.images) {
        try {
          const imgs = JSON.parse(res.data.images)
          uploadedImages.value = imgs
          fileList.value = imgs.map((url, idx) => ({
            name: `image-${idx}`,
            url: url,
            uploadedUrl: { original: url }
          }))
        } catch (e) {
          console.warn('解析图片失败', e)
        }
      }
    }
  } catch (e) {
    console.warn('获取今日打卡失败', e)
  }
}

async function uploadImageFile(file) {
  const formData = new FormData()
  formData.append('files', file.raw || file)
  const res = await request.post('/file/upload/thumbs', formData, {
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
  uploadedImages.value = uploadFiles.map(f => f.uploadedUrl?.original || f.uploadedUrl || f.url).filter(Boolean)
}

async function handleChange(file, uploadFiles) {
  fileList.value = uploadFiles
  if (file.raw && !file.uploadedUrl) {
    try {
      const urlData = await uploadImageFile(file)
      file.uploadedUrl = urlData
      uploadedImages.value = fileList.value.map(f => f.uploadedUrl?.original || f.uploadedUrl || f.url).filter(Boolean)
      ElMessage.success('图片上传成功')
    } catch (e) {
      ElMessage.error('图片上传失败')
    }
  }
}

async function handleSubmit() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再打卡')
    return
  }

  formRef.value.validate(async (valid) => {
    if (!valid) return

    if (uploadedImages.value.length === 0) {
      try {
        await ElMessageBox.confirm('还没有上传进度照片，确定要提交吗？', '提示', {
          confirmButtonText: '确定提交',
          cancelButtonText: '去上传',
          type: 'warning'
        })
      } catch (e) {
        return
      }
    }

    submitting.value = true
    try {
      const payload = {
        title: form.title,
        content: form.content,
        workDuration: form.workDuration,
        moodTag: form.moodTag,
        weatherTag: form.weatherTag,
        images: JSON.stringify(uploadedImages.value)
      }

      const res = await request.post('/checkin', payload)
      if (res.code === 200) {
        hasCheckedIn.value = true
        ElMessage.success(hasCheckedIn.value ? '打卡更新成功！' : '打卡成功！创作的每一天都值得记录 ✨')
        emit('checkin-success', res.data)
      } else {
        ElMessage.error(res.message || '打卡失败')
      }
    } catch (e) {
      ElMessage.error(e.response?.data?.message || '打卡失败，请稍后重试')
    } finally {
      submitting.value = false
    }
  })
}

function resetForm() {
  formRef.value.resetFields()
  form.workDuration = 60
  form.moodTag = ''
  form.weatherTag = ''
  fileList.value = []
  uploadedImages.value = []
}

onMounted(() => {
  userStore.initFromStorage()
  loadTodayRecord()
})
</script>

<style scoped>
.checkin-form {
  padding: 32px;
}

.form-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 28px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.date-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  color: #fff;
  font-size: 14px;
  font-weight: 500;
}

.date-icon {
  font-size: 16px;
}

.form-title {
  font-size: 22px;
  color: #303133;
  margin: 0;
  font-weight: 600;
}

.checked-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 20px;
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  border-radius: 20px;
  color: #fff;
  font-size: 14px;
  font-weight: 500;
}

.checkin-form-body {
  padding: 0 20px;
}

.duration-input {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.duration-unit {
  color: #606266;
  font-size: 14px;
}

.duration-quick {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.quick-tag {
  cursor: pointer;
  transition: all 0.2s;
  user-select: none;
}

.quick-tag:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.tags-group {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.selectable-tag {
  cursor: pointer;
  transition: all 0.2s;
  user-select: none;
  padding: 6px 14px;
}

.selectable-tag:hover {
  transform: translateY(-1px);
}

.tag-selected {
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.4);
}

.submit-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
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

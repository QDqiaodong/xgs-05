<template>
  <div class="work-detail container">
    <div v-if="loading" class="loading-wrapper">
      <el-skeleton :rows="10" animated />
    </div>
    <div v-else-if="error" class="error-wrapper">
      <el-result icon="error" title="加载失败" :sub-title="errorMsg">
        <template #extra>
          <el-button type="primary" @click="fetchWorkDetail">重新加载</el-button>
        </template>
      </el-result>
    </div>
    <div v-else-if="!work.id" class="empty-wrapper">
      <el-result icon="warning" title="作品不存在" sub-title="该作品可能已被删除或下架">
        <template #extra>
          <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
        </template>
      </el-result>
    </div>
    <template v-else>
      <div class="view-toggle" v-if="steps.length > 0">
        <el-radio-group v-model="viewMode" size="large">
          <el-radio-button value="detail">
            <el-icon><Document /></el-icon>
            详情浏览
          </el-radio-button>
          <el-radio-button value="steps">
            <el-icon><List /></el-icon>
            步骤学习
          </el-radio-button>
        </el-radio-group>
      </div>

      <div v-if="viewMode === 'detail'" class="detail-card card">
        <div class="work-gallery">
          <div class="main-image" @click="openViewer(activeImage)">
            <ImageMagnifier
              :image-url="displayImages[activeImage] || 'https://picsum.photos/800/600'"
              alt="作品主图"
              :magnifications="[2, 3, 4]"
              :default-magnification="2"
            />
          </div>
          <div class="thumbnails" v-if="displayImages.length > 0">
            <img v-for="(img, index) in displayImages" :key="index" :src="img" :class="{ active: activeImage === index }" @click="activeImage = index; openViewer(index)" />
          </div>
        </div>
        <div class="work-content">
          <div class="work-header">
            <h1>{{ work.title }}</h1>
            <div class="work-tags">
              <el-tag type="success">{{ categoryName }}</el-tag>
              <el-tag v-if="work.productionCycle" type="info">制作周期: {{ work.productionCycle }}</el-tag>
            </div>
          </div>
          <div class="author-section">
            <router-link :to="`/profile/${work.userId}`" class="author-info">
              <img :src="author.avatar || 'https://via.placeholder.com/60'" alt="作者头像" />
              <div>
                <h3>{{ author.nickname || author.username || '手作达人' }}</h3>
                <p>{{ author.bio || '热爱手工创作的艺术家' }}</p>
              </div>
            </router-link>
            <el-button type="primary" plain>关注</el-button>
          </div>
          <div class="work-stats">
            <div class="stat">
              <span class="stat-value">{{ work.viewCount || 0 }}</span>
              <span class="stat-label">浏览</span>
            </div>
            <div class="stat">
              <span class="stat-value">{{ work.favoriteCount || 0 }}</span>
              <span class="stat-label">收藏</span>
            </div>
            <div class="stat">
              <span class="stat-value">{{ work.likeCount || 0 }}</span>
              <span class="stat-label">喜欢</span>
            </div>
          </div>
          <div class="work-section">
            <h3>用料清单</h3>
            <p class="section-content">{{ work.materials || '暂无详细用料信息' }}</p>
          </div>
          <div class="work-section">
            <h3>创作思路</h3>
            <p class="section-content">{{ work.creationIdea || '暂无创作思路描述' }}</p>
          </div>
          <div class="work-section">
            <h3>作品描述</h3>
            <p class="section-content">{{ work.description || '暂无描述' }}</p>
          </div>
          <div class="work-section" v-if="steps.length > 0">
            <h3>制作步骤</h3>
            <div class="steps-preview">
              <div v-for="(step, index) in steps" :key="index" class="step-preview-item" @click="jumpToStepMode">
                <span class="step-preview-num">{{ index + 1 }}</span>
                <span class="step-preview-title">{{ step.title }}</span>
                <el-icon class="step-preview-arrow"><ArrowRight /></el-icon>
              </div>
            </div>
            <el-button type="primary" plain class="enter-step-btn" @click="jumpToStepMode">
              <el-icon><VideoPlay /></el-icon>
              进入步骤学习模式
            </el-button>
          </div>
          <div class="action-buttons">
            <el-button :type="isFavorited ? 'danger' : 'primary'" :icon="isFavorited ? StarFilled : Star" @click="toggleFavorite">
              {{ isFavorited ? '已收藏' : '收藏作品' }}
            </el-button>
            <el-button type="success" :icon="Share">分享</el-button>
          </div>
        </div>
      </div>

      <div v-else class="steps-view card">
        <div class="steps-view-header">
          <div class="back-link" @click="viewMode = 'detail'">
            <el-icon><ArrowLeft /></el-icon>
            返回详情
          </div>
          <h2 class="steps-view-title">{{ work.title }} · 制作教程</h2>
          <div class="steps-view-subtitle">共 {{ steps.length }} 个步骤，跟随学习更轻松</div>
        </div>
        <StepBrowser v-if="steps.length > 0" :steps="steps" @finish="onStepsFinish" />
        <div v-else class="no-steps">
          <el-icon :size="64" color="#c0c4cc"><Warning /></el-icon>
          <p>该作品暂无制作步骤</p>
        </div>
      </div>
    </template>
    <ImageViewer
      v-if="displayImages.length > 0"
      :visible="viewerVisible"
      :images="displayImages"
      :initial-index="viewerInitialIndex"
      @close="closeViewer"
      @change="onViewerChange"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Star, StarFilled, Share, Document, List, ArrowRight, VideoPlay, ArrowLeft, Warning, ZoomIn } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import StepBrowser from '../components/StepBrowser.vue'
import ImageViewer from '../components/ImageViewer.vue'
import ImageMagnifier from '../components/ImageMagnifier.vue'
import request from '@/utils/request'

const route = useRoute()
const activeImage = ref(0)
const isFavorited = ref(false)
const viewMode = ref('detail')
const loading = ref(true)
const error = ref(false)
const errorMsg = ref('')
const viewerVisible = ref(false)
const viewerInitialIndex = ref(0)

const work = ref({})
const author = ref({})
const workImages = ref([])
const steps = ref([])

const displayImages = computed(() => {
  const cover = work.value.coverImage
  const images = workImages.value || []
  if (cover && !images.includes(cover)) {
    return [cover, ...images]
  }
  if (images.length > 0) {
    return images
  }
  if (cover) {
    return [cover]
  }
  return []
})

const categoryName = computed(() => {
  const categories = { 1: '编织', 2: '陶艺', 3: '布艺', 4: '木艺' }
  return categories[work.value.categoryId] || '其他'
})

function parseJSON(str, fallback) {
  if (!str) return fallback
  try {
    return JSON.parse(str)
  } catch (e) {
    return fallback
  }
}

async function fetchWorkDetail() {
  loading.value = true
  error.value = false
  try {
    const workId = route.params.id
    const res = await request.get(`/work/${workId}`)
    if (res.code === 200 && res.data) {
      work.value = res.data
      workImages.value = parseJSON(res.data.images, [])
      steps.value = parseJSON(res.data.steps, [])
      if (res.data.userId) {
        await fetchAuthorInfo(res.data.userId)
      }
    } else {
      work.value = {}
    }
  } catch (e) {
    error.value = true
    errorMsg.value = e.response?.data?.message || e.message || '网络请求失败'
  } finally {
    loading.value = false
  }
}

async function fetchAuthorInfo(userId) {
  try {
    const res = await request.get(`/user/${userId}`)
    if (res.code === 200 && res.data) {
      author.value = res.data
    }
  } catch (e) {
    console.warn('获取作者信息失败', e)
  }
}

function toggleFavorite() {
  isFavorited.value = !isFavorited.value
  if (isFavorited.value) {
    work.value.favoriteCount = (work.value.favoriteCount || 0) + 1
    ElMessage.success('已收藏作品')
  } else {
    work.value.favoriteCount = Math.max(0, (work.value.favoriteCount || 0) - 1)
    ElMessage.info('已取消收藏')
  }
}

function jumpToStepMode() {
  viewMode.value = 'steps'
}

function onStepsFinish() {
  console.log('用户完成了所有步骤学习')
}

function openViewer(index) {
  if (displayImages.value.length === 0) return
  viewerInitialIndex.value = index || 0
  viewerVisible.value = true
}

function closeViewer() {
  viewerVisible.value = false
}

function onViewerChange(index) {
  activeImage.value = index
}

onMounted(() => {
  fetchWorkDetail()
})
</script>

<style scoped>
.work-detail {
  padding: 30px 0;
}

.loading-wrapper,
.error-wrapper,
.empty-wrapper {
  padding: 60px 20px;
}

.detail-card {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 40px;
  padding: 30px;
}

.work-gallery {
  position: sticky;
  top: 100px;
  height: fit-content;
}

.main-image {
  border-radius: 8px;
  overflow: visible;
  margin-bottom: 16px;
  position: relative;
}

.thumbnails {
  display: flex;
  gap: 12px;
}

.thumbnails img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
  cursor: pointer;
  border: 2px solid transparent;
  transition: border-color 0.3s;
}

.thumbnails img.active {
  border-color: #667eea;
}

.work-header h1 {
  font-size: 28px;
  color: #333;
  margin-bottom: 16px;
}

.work-tags {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

.author-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 24px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.author-info img {
  width: 60px;
  height: 60px;
  border-radius: 50%;
}

.author-info h3 {
  font-size: 16px;
  color: #333;
  margin-bottom: 4px;
}

.author-info p {
  font-size: 13px;
  color: #666;
}

.work-stats {
  display: flex;
  gap: 40px;
  padding: 20px 0;
  border-top: 1px solid #eee;
  border-bottom: 1px solid #eee;
  margin-bottom: 24px;
}

.stat {
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  color: #999;
}

.work-section {
  margin-bottom: 24px;
}

.work-section h3 {
  font-size: 16px;
  color: #333;
  margin-bottom: 12px;
  padding-left: 12px;
  border-left: 3px solid #667eea;
}

.section-content {
  font-size: 14px;
  color: #666;
  line-height: 1.8;
  padding-left: 15px;
}

.action-buttons {
  display: flex;
  gap: 16px;
  margin-top: 30px;
}

.action-buttons .el-button {
  flex: 1;
  padding: 12px 24px;
}

.view-toggle {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
}

.view-toggle :deep(.el-radio-button__inner) {
  padding: 12px 24px;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.steps-preview {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

.step-preview-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: #f8f9fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.step-preview-item:hover {
  background: #eef0ff;
  transform: translateX(4px);
}

.step-preview-num {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  flex-shrink: 0;
}

.step-preview-title {
  flex: 1;
  font-size: 14px;
  color: #606266;
}

.step-preview-arrow {
  color: #c0c4cc;
  font-size: 16px;
}

.enter-step-btn {
  width: 100%;
  padding: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-weight: 500;
}

.steps-view {
  padding: 30px;
}

.steps-view-header {
  text-align: center;
  margin-bottom: 30px;
  padding-bottom: 24px;
  border-bottom: 1px solid #ebeef5;
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #667eea;
  font-size: 14px;
  cursor: pointer;
  margin-bottom: 16px;
  transition: color 0.3s;
}

.back-link:hover {
  color: #764ba2;
}

.steps-view-title {
  font-size: 24px;
  color: #303133;
  margin: 0 0 8px 0;
}

.steps-view-subtitle {
  font-size: 14px;
  color: #909399;
}

.no-steps {
  padding: 80px 40px;
  text-align: center;
  color: #909399;
}

.no-steps p {
  margin-top: 16px;
  font-size: 16px;
}

@media (max-width: 968px) {
  .detail-card {
    grid-template-columns: 1fr;
  }

  .steps-view {
    padding: 20px;
  }

  .steps-view-title {
    font-size: 20px;
  }
}
</style>

<template>
  <div class="growth-timeline card">
    <div class="timeline-header">
      <h3 class="timeline-title">
        <span class="title-icon">🌱</span>
        <span>创作成长轨迹</span>
      </h3>
      <div class="timeline-subtitle">见证每一步的进步与蜕变</div>
    </div>

    <div v-if="timeline.length === 0" class="timeline-empty">
      <el-empty description="还没有打卡记录，开始今天的第一次打卡吧！">
        <template #image>
          <div class="empty-icon">📝</div>
        </template>
      </el-empty>
    </div>

    <div v-else class="timeline-container">
      <div class="timeline-line"></div>

      <div
        v-for="(item, index) in timeline"
        :key="item.id"
        class="timeline-item"
        :class="{ 'item-reverse': index % 2 === 1 }"
      >
        <div class="timeline-dot">
          <div class="dot-inner"></div>
        </div>

        <div class="timeline-card">
          <div class="card-date">
            <el-icon><Calendar /></el-icon>
            <span>{{ formatDate(item.checkInDate) }}</span>
            <el-tag v-if="item.workDuration" size="small" type="warning" effect="light" class="duration-tag">
              ⏱ {{ formatDuration(item.workDuration) }}
            </el-tag>
          </div>

          <h4 class="card-title">{{ item.title || '（无标题）' }}</h4>

          <div v-if="getImages(item).length > 0" class="card-images">
            <div class="card-images-main">
              <img
                :src="getImages(item)[0]"
                class="main-image"
                @click="handleImageClick(getImages(item), 0)"
              />
              <div v-if="getImages(item).length > 1" class="images-count">
                +{{ getImages(item).length - 1 }}
              </div>
            </div>
            <div v-if="getImages(item).length > 1" class="card-images-thumbs">
              <img
                v-for="(img, i) in getImages(item).slice(1, 4)"
                :key="i"
                :src="img"
                class="thumb-image"
                @click="handleImageClick(getImages(item), i + 1)"
              />
            </div>
          </div>

          <p v-if="item.content" class="card-content">{{ truncateText(item.content, 120) }}</p>

          <div class="card-tags" v-if="item.moodTag || item.weatherTag">
            <el-tag v-if="item.moodTag" size="small" type="primary" effect="light">{{ item.moodTag }}</el-tag>
            <el-tag v-if="item.weatherTag" size="small" type="success" effect="light">{{ item.weatherTag }}</el-tag>
          </div>

          <div class="card-footer">
            <div class="footer-stats" v-if="item.likeCount > 0">
              <el-icon><Star /></el-icon>
              <span>{{ item.likeCount }}</span>
            </div>
            <el-button type="primary" link size="small" @click="viewDetail(item)">
              查看详情 →
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <el-image-viewer
      v-if="imageViewerVisible"
      :url-list="imageViewerUrls"
      :initial-index="imageViewerIndex"
      :on-close="() => imageViewerVisible = false"
    />

    <el-dialog v-model="detailVisible" title="打卡详情" width="600px" class="timeline-detail-dialog">
      <div v-if="selectedItem" class="detail-wrapper">
        <div class="detail-header-row">
          <div class="detail-date-badge">
            <el-icon><Calendar /></el-icon>
            <span>{{ formatDate(selectedItem.checkInDate) }}</span>
          </div>
          <el-tag v-if="selectedItem.workDuration" type="warning" effect="light">
            ⏱ {{ formatDuration(selectedItem.workDuration) }}
          </el-tag>
        </div>

        <h3 class="detail-main-title">{{ selectedItem.title || '（无标题）' }}</h3>

        <div v-if="getImages(selectedItem).length > 0" class="detail-images-section">
          <el-image
            v-for="(img, i) in getImages(selectedItem)"
            :key="i"
            :src="img"
            :preview-src-list="getImages(selectedItem)"
            :initial-index="i"
            fit="cover"
            class="detail-gallery-image"
            :preview-teleported="true"
          />
        </div>

        <div v-if="selectedItem.content" class="detail-content-section">
          <div class="section-label">创作心得</div>
          <p class="detail-paragraph">{{ selectedItem.content }}</p>
        </div>

        <div class="detail-tags-section" v-if="selectedItem.moodTag || selectedItem.weatherTag">
          <el-tag v-if="selectedItem.moodTag" effect="light" type="primary">
            <el-icon><ChatDotRound /></el-icon>
            {{ selectedItem.moodTag }}
          </el-tag>
          <el-tag v-if="selectedItem.weatherTag" effect="light" type="success">
            <el-icon><Sunny /></el-icon>
            {{ selectedItem.weatherTag }}
          </el-tag>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Calendar, Star, ChatDotRound, Sunny } from '@element-plus/icons-vue'
import { ElImageViewer } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'

const props = defineProps({
  userId: {
    type: Number,
    default: null
  },
  limit: {
    type: Number,
    default: 20
  }
})

const userStore = useUserStore()
const timeline = ref([])
const loading = ref(false)
const imageViewerVisible = ref(false)
const imageViewerUrls = ref([])
const imageViewerIndex = ref(0)
const detailVisible = ref(false)
const selectedItem = ref(null)

function getImages(item) {
  if (!item || !item.images) return []
  try {
    return JSON.parse(item.images) || []
  } catch (e) {
    return []
  }
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日`
}

function formatDuration(minutes) {
  if (!minutes) return '0分钟'
  if (minutes < 60) return `${minutes}分钟`
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  return m > 0 ? `${h}小时${m}分钟` : `${h}小时`
}

function truncateText(text, maxLen) {
  if (!text) return ''
  if (text.length <= maxLen) return text
  return text.slice(0, maxLen) + '...'
}

async function loadTimeline() {
  if (!userStore.isLoggedIn) return
  loading.value = true
  try {
    const res = await request.get('/checkin/timeline', {
      params: {
        userId: props.userId,
        limit: props.limit
      }
    })
    if (res.code === 200 && res.data) {
      timeline.value = res.data
    }
  } catch (e) {
    console.warn('加载成长轨迹失败', e)
  } finally {
    loading.value = false
  }
}

function handleImageClick(urls, index) {
  imageViewerUrls.value = urls
  imageViewerIndex.value = index
  imageViewerVisible.value = true
}

function viewDetail(item) {
  selectedItem.value = item
  detailVisible.value = true
}

onMounted(() => {
  userStore.initFromStorage()
  loadTimeline()
})

defineExpose({ loadTimeline })
</script>

<style scoped>
.growth-timeline {
  padding: 32px;
}

.timeline-header {
  text-align: center;
  margin-bottom: 40px;
}

.timeline-title {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 8px 0;
  display: inline-flex;
  align-items: center;
  gap: 10px;
}

.title-icon {
  font-size: 28px;
}

.timeline-subtitle {
  font-size: 14px;
  color: #909399;
}

.timeline-empty {
  padding: 60px 20px;
}

.empty-icon {
  font-size: 60px;
  opacity: 0.5;
}

.timeline-container {
  position: relative;
  padding: 20px 0;
}

.timeline-line {
  position: absolute;
  left: 50%;
  top: 0;
  bottom: 0;
  width: 3px;
  background: linear-gradient(180deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
  transform: translateX(-50%);
  border-radius: 2px;
}

@media (max-width: 768px) {
  .timeline-line {
    left: 24px;
  }
}

.timeline-item {
  display: flex;
  margin-bottom: 36px;
  position: relative;
}

.timeline-item:nth-child(odd) {
  justify-content: flex-start;
  padding-right: calc(50% + 30px);
}

.timeline-item:nth-child(even) {
  justify-content: flex-end;
  padding-left: calc(50% + 30px);
}

.timeline-item.item-reverse .timeline-card {
}

@media (max-width: 768px) {
  .timeline-item,
  .timeline-item:nth-child(odd),
  .timeline-item:nth-child(even) {
    padding-left: 60px;
    padding-right: 0;
    justify-content: flex-start;
  }
}

.timeline-dot {
  position: absolute;
  left: 50%;
  top: 20px;
  width: 18px;
  height: 18px;
  background: #fff;
  border: 3px solid #667eea;
  border-radius: 50%;
  transform: translateX(-50%);
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.timeline-item:hover .timeline-dot {
  border-color: #f093fb;
  transform: translateX(-50%) scale(1.2);
}

.dot-inner {
  width: 6px;
  height: 6px;
  background: #667eea;
  border-radius: 50%;
}

.timeline-item:hover .dot-inner {
  background: #f093fb;
}

@media (max-width: 768px) {
  .timeline-dot {
    left: 24px;
  }
}

.timeline-card {
  width: 100%;
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.08);
  border: 1px solid #f0f0f0;
  transition: all 0.3s;
}

.timeline-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 28px rgba(102, 126, 234, 0.15);
  border-color: #667eea44;
}

.card-date {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #667eea;
  margin-bottom: 10px;
  font-weight: 500;
}

.duration-tag {
  margin-left: auto;
}

.card-title {
  font-size: 17px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 14px 0;
  line-height: 1.4;
}

.card-images {
  margin-bottom: 14px;
  display: flex;
  gap: 8px;
}

.card-images-main {
  position: relative;
  flex: 1;
  border-radius: 10px;
  overflow: hidden;
  aspect-ratio: 4/3;
  background: #f5f5f5;
  cursor: pointer;
}

.main-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.card-images-main:hover .main-image {
  transform: scale(1.05);
}

.images-count {
  position: absolute;
  bottom: 8px;
  right: 8px;
  background: rgba(0, 0, 0, 0.65);
  color: #fff;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.card-images-thumbs {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 80px;
  flex-shrink: 0;
}

.thumb-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.2s;
}

.thumb-image:hover {
  transform: scale(1.03);
}

.card-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.7;
  margin: 0 0 12px 0;
  white-space: pre-wrap;
}

.card-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f5f5f5;
}

.footer-stats {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #e6a23c;
  font-size: 13px;
}

.detail-wrapper {
  padding: 4px;
}

.detail-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 10px;
}

.detail-date-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
}

.detail-main-title {
  font-size: 20px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 20px 0;
}

.detail-images-section {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-bottom: 24px;
}

@media (max-width: 600px) {
  .detail-images-section {
    grid-template-columns: 1fr;
  }
}

.detail-gallery-image {
  width: 100%;
  aspect-ratio: 1;
  border-radius: 10px;
  cursor: pointer;
}

.detail-content-section {
  background: #fafbfc;
  padding: 18px;
  border-radius: 12px;
  margin-bottom: 16px;
}

.section-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 10px;
  font-weight: 600;
}

.detail-paragraph {
  margin: 0;
  color: #606266;
  line-height: 1.8;
  white-space: pre-wrap;
  font-size: 14px;
}

.detail-tags-section {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.detail-tags-section :deep(.el-tag) {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

:deep(.timeline-detail-dialog .el-dialog__body) {
  max-height: 75vh;
  overflow-y: auto;
}
</style>

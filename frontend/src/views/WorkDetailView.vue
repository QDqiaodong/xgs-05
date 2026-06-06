<template>
  <div class="work-detail container">
    <div class="detail-card card">
      <div class="work-gallery">
        <div class="main-image">
          <img :src="work.coverImage || 'https://picsum.photos/800/600'" alt="作品主图" />
        </div>
        <div class="thumbnails">
          <img v-for="(img, index) in work.images || []" :key="index" :src="img" :class="{ active: activeImage === index }" @click="activeImage = index" />
        </div>
      </div>
      <div class="work-content">
        <div class="work-header">
          <h1>{{ work.title }}</h1>
          <div class="work-tags">
            <el-tag type="success">{{ categoryName }}</el-tag>
            <el-tag type="info">制作周期: {{ work.productionCycle }}</el-tag>
          </div>
        </div>
        <div class="author-section">
          <router-link :to="`/profile/${work.authorId}`" class="author-info">
            <img :src="work.authorAvatar || 'https://via.placeholder.com/60'" alt="作者头像" />
            <div>
              <h3>{{ work.authorName }}</h3>
              <p>{{ work.authorBio || '热爱手工创作的艺术家' }}</p>
            </div>
          </router-link>
          <el-button type="primary" plain>关注</el-button>
        </div>
        <div class="work-stats">
          <div class="stat">
            <span class="stat-value">{{ work.viewCount }}</span>
            <span class="stat-label">浏览</span>
          </div>
          <div class="stat">
            <span class="stat-value">{{ work.favoriteCount }}</span>
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
          <p class="section-content">{{ work.description }}</p>
        </div>
        <div class="action-buttons">
          <el-button :type="isFavorited ? 'danger' : 'primary'" :icon="isFavorited ? StarFilled : Star" @click="toggleFavorite">
            {{ isFavorited ? '已收藏' : '收藏作品' }}
          </el-button>
          <el-button type="success" :icon="Share">分享</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Star, StarFilled, Share } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const activeImage = ref(0)
const isFavorited = ref(false)

const work = ref({
  id: 1,
  title: '手工编织毛衣',
  description: '温暖的羊毛手工编织，耗时一个月完成，采用纯羊毛线，手感柔软舒适。',
  coverImage: 'https://picsum.photos/800/600?random=10',
  images: [
    'https://picsum.photos/800/600?random=10',
    'https://picsum.photos/800/600?random=11',
    'https://picsum.photos/800/600?random=12',
    'https://picsum.photos/800/600?random=13'
  ],
  authorId: 1,
  authorName: '小手巧',
  authorAvatar: 'https://via.placeholder.com/60',
  authorBio: '专注手工编织5年，热爱所有美好的事物',
  viewCount: 1256,
  favoriteCount: 89,
  likeCount: 156,
  categoryId: 1,
  materials: '纯羊毛线 500g，棒针一副，缝针一枚',
  creationIdea: '这款毛衣的设计灵感来源于北欧风格，采用简约的几何图案，既保暖又时尚。选择了天然的羊毛材质，让穿着者感受到大自然的温暖。',
  productionCycle: '30天'
})

const categoryName = computed(() => {
  const categories = { 1: '编织', 2: '陶艺', 3: '布艺', 4: '木艺' }
  return categories[work.value.categoryId] || '其他'
})

function toggleFavorite() {
  isFavorited.value = !isFavorited.value
  if (isFavorited.value) {
    work.value.favoriteCount++
    ElMessage.success('已收藏作品')
  } else {
    work.value.favoriteCount--
    ElMessage.info('已取消收藏')
  }
}

onMounted(() => {
  console.log('Work ID:', route.params.id)
})
</script>

<style scoped>
.work-detail {
  padding: 30px 0;
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
  overflow: hidden;
  margin-bottom: 16px;
}

.main-image img {
  width: 100%;
  display: block;
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

@media (max-width: 968px) {
  .detail-card {
    grid-template-columns: 1fr;
  }
}
</style>

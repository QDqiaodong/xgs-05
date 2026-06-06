<template>
  <div class="work-card masonry-item card" @click="goToDetail">
    <div class="work-image">
      <img :src="work.coverImage || 'https://via.placeholder.com/300x400'" :alt="work.title" loading="lazy" />
      <div v-if="work.isHot" class="hot-tag">热门</div>
    </div>
    <div class="work-info">
      <h3 class="work-title">{{ work.title }}</h3>
      <p class="work-desc">{{ work.description }}</p>
      <div class="work-meta">
        <router-link :to="`/profile/${work.authorId}`" class="author" @click.stop>
          <img :src="work.authorAvatar || 'https://via.placeholder.com/24'" alt="author" />
          <span>{{ work.authorName }}</span>
        </router-link>
        <div class="stats">
          <span class="stat-item">
            <el-icon><View /></el-icon>
            {{ work.viewCount }}
          </span>
          <span class="stat-item" @click.stop="toggleFavorite">
            <el-icon :class="{ 'is-favorite': isFavorited }">
              <StarFilled v-if="isFavorited" />
              <Star v-else />
            </el-icon>
            {{ work.favoriteCount }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Star, StarFilled, View } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  work: {
    type: Object,
    required: true
  }
})

const router = useRouter()
const isFavorited = ref(false)

function goToDetail() {
  router.push(`/work/${props.work.id}`)
}

function toggleFavorite() {
  isFavorited.value = !isFavorited.value
  if (isFavorited.value) {
    ElMessage.success('已收藏')
  } else {
    ElMessage.info('已取消收藏')
  }
}
</script>

<style scoped>
.work-card {
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
}

.work-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.work-image {
  position: relative;
  overflow: hidden;
}

.work-image img {
  width: 100%;
  display: block;
  transition: transform 0.5s;
}

.work-card:hover .work-image img {
  transform: scale(1.05);
}

.hot-tag {
  position: absolute;
  top: 12px;
  right: 12px;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: #fff;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.work-info {
  padding: 16px;
}

.work-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.work-desc {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.work-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.author {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #666;
}

.author img {
  width: 24px;
  height: 24px;
  border-radius: 50%;
}

.stats {
  display: flex;
  gap: 12px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #999;
}

.stat-item .el-icon {
  font-size: 14px;
  cursor: pointer;
  transition: color 0.3s;
}

.stat-item .el-icon:hover {
  color: #667eea;
}

.stat-item .el-icon.is-favorite {
  color: #f56c6c;
  fill: #f56c6c;
}
</style>

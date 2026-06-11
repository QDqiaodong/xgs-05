<template>
  <div :class="cardClass" @click="goToDetail">
    <div :class="imageClass" :style="imageStyle">
      <div v-if="!imgLoaded && !imgError" class="image-placeholder">
        <div class="skeleton-shimmer"></div>
      </div>
      <img
        ref="imgRef"
        :src="coverImageSmall"
        :alt="work.title"
        loading="lazy"
        :class="{ 'img-loaded': imgLoaded, 'img-hidden': !imgLoaded }"
        @load="onImgLoad"
        @error="onImgError"
      />
      <div v-if="imgError" class="image-error">
        <el-icon :size="32"><PictureFilled /></el-icon>
        <span>加载失败</span>
      </div>
      <div v-if="work.isHot" class="hot-tag">热门</div>
    </div>
    <div class="work-info">
      <h3 class="work-title">{{ work.title }}</h3>
      <p v-if="layout !== 'list'" class="work-desc">{{ work.description }}</p>
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
          <span class="stat-item" @click.stop="handleFavoriteClick">
            <el-icon :class="{ 'is-favorite': isFavorited }">
              <StarFilled v-if="isFavorited" />
              <Star v-else />
            </el-icon>
            {{ displayFavoriteCount }}
          </span>
        </div>
      </div>
    </div>
    <FolderSelectorDialog
      v-model="folderSelectorVisible"
      :work-id="work.id"
      @change="onFolderChange"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { Star, StarFilled, View, PictureFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getSmallImage } from '@/utils/image'
import { useFavoriteStore } from '@/store/favorite'
import { useUserStore } from '@/store/user'
import FolderSelectorDialog from './FolderSelectorDialog.vue'

const props = defineProps({
  work: {
    type: Object,
    required: true
  },
  layout: {
    type: String,
    default: 'masonry',
    validator: (val) => ['masonry', 'grid', 'list'].includes(val)
  }
})

const cardClass = computed(() => {
  const classes = ['work-card', 'card']
  if (props.layout === 'masonry') {
    classes.push('masonry-item')
  }
  classes.push(`layout-${props.layout}`)
  return classes
})

const imageClass = computed(() => {
  return ['work-image', `image-${props.layout}`]
})

const imageStyle = computed(() => {
  if (props.layout === 'masonry') {
    return { aspectRatio: imageRatio.value }
  }
  if (props.layout === 'grid') {
    return { aspectRatio: '1 / 1' }
  }
  if (props.layout === 'list') {
    return { aspectRatio: '4 / 3', width: '200px', flexShrink: '0' }
  }
  return {}
})

const router = useRouter()
const favoriteStore = useFavoriteStore()
const userStore = useUserStore()
const imgLoaded = ref(false)
const imgError = ref(false)
const imgRef = ref(null)
const workLocal = ref({ ...props.work })
const folderSelectorVisible = ref(false)

const isFavorited = computed(() => {
  favoriteStore.version
  return favoriteStore.isFavorited(props.work.id)
})

const displayFavoriteCount = computed(() => {
  favoriteStore.version
  const base = props.work.favoriteCount || 0
  return base
})

const placeholderColors = [
  'linear-gradient(135deg, #f5f7fa 0%, #e4e7eb 100%)',
  'linear-gradient(135deg, #f0f4f8 0%, #d9e2ec 100%)',
  'linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%)',
  'linear-gradient(135deg, #eef2f7 0%, #dde3ec 100%)'
]

const placeholderStyle = computed(() => ({
  background: placeholderColors[props.work.id % placeholderColors.length]
}))

const imageRatio = computed(() => {
  const url = props.work.coverImage || ''
  const match = url.match(/\/(\d+)\/(\d+)/)
  if (match) {
    const w = parseInt(match[1])
    const h = parseInt(match[2])
    if (w && h) return `${w} / ${h}`
  }
  return '3 / 4'
})

const coverImageSmall = computed(() => {
  return getSmallImage(props.work.coverImage) || 'https://via.placeholder.com/300x400'
})

function onImgLoad() {
  imgLoaded.value = true
}

function onImgError() {
  imgError.value = true
}

function goToDetail() {
  router.push(`/work/${props.work.id}`)
}

async function handleFavoriteClick() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  if (!props.work.id) return
  folderSelectorVisible.value = true
}

function onFolderChange() {
  favoriteStore.touchVersion()
}

onMounted(async () => {
  nextTick(() => {
    if (imgRef.value && imgRef.value.complete) {
      if (imgRef.value.naturalWidth === 0) {
        imgError.value = true
      } else {
        imgLoaded.value = true
      }
    }
  })
  if (userStore.isLoggedIn && props.work.id) {
    favoriteStore.checkFavorite(props.work.id)
  }
})
</script>

<style scoped>
.work-card {
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.work-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.work-card.layout-list {
  display: flex;
  flex-direction: row;
}

.work-card.layout-list:hover {
  transform: translateX(5px);
}

.work-image {
  position: relative;
  overflow: hidden;
  background: #f0f0f0;
}

.work-image.image-list {
  border-radius: 8px 0 0 8px;
}

.image-placeholder {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #f5f5f5 0%, #e8e8e8 100%);
  overflow: hidden;
}

.skeleton-shimmer {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    90deg,
    transparent 0%,
    rgba(255, 255, 255, 0.5) 50%,
    transparent 100%
  );
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

@keyframes shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

.work-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: opacity 0.6s ease-out, transform 0.5s;
}

.work-image img.img-hidden {
  opacity: 0;
}

.work-image img.img-loaded {
  opacity: 1;
}

.work-card:hover .work-image img {
  transform: scale(1.05);
}

.image-error {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  background: linear-gradient(135deg, #f5f5f5 0%, #e8e8e8 100%);
  color: #999;
  font-size: 12px;
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
  z-index: 1;
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

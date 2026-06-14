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
      <div v-if="workLocal.difficultyLevel" :class="difficultyClass">
        {{ difficultyText }}
      </div>
      <el-dropdown
        v-if="userStore.isAdmin"
        trigger="click"
        class="admin-difficulty-dropdown"
        @command="(val) => handleSetDifficulty(val, $event)"
      >
        <div class="admin-difficulty-btn" @click.stop>
          <el-icon :size="14"><Setting /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item :command="1">
              <span class="diff-item diff-1">● 入门</span>
            </el-dropdown-item>
            <el-dropdown-item :command="2">
              <span class="diff-item diff-2">● 进阶</span>
            </el-dropdown-item>
            <el-dropdown-item :command="3">
              <span class="diff-item diff-3">● 大师</span>
            </el-dropdown-item>
            <el-dropdown-item :command="0" divided>
              <span>取消评定</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
    <div class="work-info">
      <h3 class="work-title">{{ work.title }}</h3>
      <p v-if="layout !== 'list'" class="work-desc">{{ work.description }}</p>
      <div class="work-meta">
        <router-link :to="`/profile/${work.authorId}`" class="author" @click.stop>
          <img :src="work.authorAvatar || 'https://via.placeholder.com/24'" alt="author" />
          <div class="author-info-inline">
            <span class="author-name">{{ work.authorName }}</span>
            <CreatorLevelBadge v-if="work.authorLevel" :level="work.authorLevel" size="small" :show-name="false" />
            <CreatorVerifiedBadge v-if="work.authorIsCertified === 1" size="small" />
          </div>
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
import { Star, StarFilled, View, PictureFilled, Setting } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getSmallImage } from '@/utils/image'
import { setWorkDifficulty } from '@/utils/request'
import { useFavoriteStore } from '@/store/favorite'
import { useUserStore } from '@/store/user'
import FolderSelectorDialog from './FolderSelectorDialog.vue'
import CreatorLevelBadge from './CreatorLevelBadge.vue'
import CreatorVerifiedBadge from './CreatorVerifiedBadge.vue'

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

const difficultyText = computed(() => {
  const map = { 1: '入门', 2: '进阶', 3: '大师' }
  return map[workLocal.value.difficultyLevel] || ''
})

const difficultyClass = computed(() => {
  return ['difficulty-tag', 'difficulty-' + workLocal.value.difficultyLevel]
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

async function handleSetDifficulty(level, event) {
  if (!props.work.id) return
  const difficulty = level === 0 ? null : level
  try {
    const res = await setWorkDifficulty(props.work.id, difficulty)
    if (res.code === 200) {
      workLocal.value.difficultyLevel = difficulty
      ElMessage.success('难度等级设置成功')
      sessionStorage.setItem('workListRefreshKey', Date.now().toString())
    } else {
      ElMessage.error(res.message || '设置失败')
    }
  } catch (e) {
    console.error('设置难度失败', e)
  }
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

.difficulty-tag {
  position: absolute;
  top: 12px;
  left: 12px;
  color: #fff;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  z-index: 1;
}

.difficulty-tag.difficulty-1 {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
}

.difficulty-tag.difficulty-2 {
  background: linear-gradient(135deg, #f2994a 0%, #f2c94c 100%);
}

.difficulty-tag.difficulty-3 {
  background: linear-gradient(135deg, #eb3349 0%, #f45c43 100%);
}

.admin-difficulty-dropdown {
  position: absolute;
  top: 12px;
  right: 12px;
  z-index: 2;
}

.admin-difficulty-btn {
  width: 28px;
  height: 28px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  transition: all 0.3s;
  color: #667eea;
}

.admin-difficulty-btn:hover {
  background: #667eea;
  color: #fff;
  transform: scale(1.1);
}

.diff-item {
  font-weight: 500;
}

.diff-1 {
  color: #11998e;
}

.diff-2 {
  color: #f2994a;
}

.diff-3 {
  color: #eb3349;
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

.author-info-inline {
  display: flex;
  align-items: center;
  gap: 4px;
}

.author-name {
  font-size: 13px;
  color: #666;
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

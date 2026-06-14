<template>
  <div class="profile container">
    <div class="profile-header card">
      <div class="profile-info">
        <img :src="user.avatar || 'https://via.placeholder.com/120'" alt="avatar" class="avatar" />
        <div class="info">
          <div class="username-row">
            <h1>{{ user.nickname || user.username }}</h1>
            <CreatorLevelBadge v-if="user.creatorLevel" :level="user.creatorLevel" size="medium" />
          </div>
          <p class="bio">{{ user.bio || '热爱手工创作的艺术家' }}</p>
          <div class="level-progress" v-if="levelInfo.score !== undefined">
            <div class="level-info">
              <span class="level-name">{{ levelInfo.levelName }}</span>
              <span class="level-score">{{ levelInfo.score }} 分</span>
            </div>
            <el-progress
              :percentage="levelProgress"
              :color="levelGradient"
              :stroke-width="8"
              :show-text="false"
            />
            <div class="level-next" v-if="levelInfo.nextLevelName">
              距离 {{ levelInfo.nextLevelName }} 还需 {{ levelInfo.nextLevelScore - levelInfo.score }} 分
            </div>
          </div>
          <div class="user-stats">
            <div class="stat">
              <span class="stat-value">{{ stats.works }}</span>
              <span class="stat-label">作品</span>
            </div>
            <div class="stat">
              <span class="stat-value">{{ stats.totalViews }}</span>
              <span class="stat-label">总浏览</span>
            </div>
            <div class="stat">
              <span class="stat-value">{{ stats.totalFavorites }}</span>
              <span class="stat-label">总收藏</span>
            </div>
            <div class="stat">
              <span class="stat-value">{{ stats.totalLikes }}</span>
              <span class="stat-label">总获赞</span>
            </div>
          </div>
        </div>
      </div>
      <div class="profile-actions" v-if="isOwner">
        <el-button type="primary">编辑资料</el-button>
      </div>
      <div class="profile-actions" v-else>
        <el-button type="primary">+ 关注</el-button>
        <el-button @click="showInviteDialog = true">💌 定制邀约</el-button>
      </div>
    </div>

    <div class="profile-tabs">
      <div class="tabs-nav-wrapper">
        <div class="tabs-nav">
          <div :class="['tab-item', { 'is-active': activeTab === 'works' }]" @click="activeTab = 'works'">全部作品</div>
          <div :class="['tab-item', { 'is-active': activeTab === 'favorites' }]" @click="activeTab = 'favorites'">收藏作品</div>
          <div :class="['tab-item', { 'is-active': activeTab === 'invitations' }]" v-if="isOwner" @click="activeTab = 'invitations'">
            收到的邀约
            <span v-if="pendingInvitationCount > 0" class="tab-badge">{{ pendingInvitationCount }}</span>
          </div>
        </div>
        <div class="layout-switcher">
          <span class="layout-label">展示模式：</span>
          <el-radio-group v-model="activeLayout" size="small">
            <el-radio-button value="masonry">
              <el-tooltip content="瀑布流" placement="top">
                <el-icon><Grid /></el-icon>
              </el-tooltip>
            </el-radio-button>
            <el-radio-button value="grid">
              <el-tooltip content="对称网格" placement="top">
                <el-icon><Menu /></el-icon>
              </el-tooltip>
            </el-radio-button>
            <el-radio-button value="list">
              <el-tooltip content="简约列表" placement="top">
                <el-icon><List /></el-icon>
              </el-tooltip>
            </el-radio-button>
          </el-radio-group>
        </div>
      </div>

      <div v-show="activeTab === 'works'">
        <div v-if="loading" class="loading">
          <el-skeleton :rows="5" animated />
        </div>
        <div v-else-if="works.length > 0" :class="gridContainerClass">
          <WorkCard v-for="work in works" :key="work.id" :work="work" :layout="activeLayout" />
        </div>
        <div v-else class="empty">
          <el-empty description="暂无作品" />
        </div>
        <div v-if="hasMore && works.length > 0" class="load-more">
          <el-button :loading="loadingMore" @click="loadMoreWorks">加载更多</el-button>
        </div>
      </div>

      <div v-show="activeTab === 'favorites'">
        <div v-if="displayedFavorites.length > 0" :class="gridContainerClass">
          <WorkCard v-for="work in displayedFavorites" :key="work.id" :work="work" :layout="activeLayout" />
        </div>
        <div v-if="displayedFavorites.length === 0" class="empty">
          <el-empty description="暂无收藏" />
        </div>
      </div>

      <div v-show="activeTab === 'invitations'">
        <div v-if="invitationsLoading" class="loading">
          <el-skeleton :rows="5" animated />
        </div>
        <div v-else-if="profileInvitations.length > 0" class="invitation-list">
          <router-link
            v-for="item in profileInvitations"
            :key="item.invitation.id"
            :to="`/invitation/${item.invitation.id}`"
            class="invitation-card card"
          >
            <div class="invitation-header">
              <div class="invitation-title">{{ item.invitation.title }}</div>
              <el-tag :type="getInvitationStatusType(item.invitation.status)" size="small">
                {{ getInvitationStatusText(item.invitation.status) }}
              </el-tag>
            </div>
            <div class="invitation-info">
              <div class="info-item">
                <span class="label">客户：</span>
                <span class="value">{{ item.client?.nickname || item.client?.username }}</span>
              </div>
              <div class="info-item" v-if="item.invitation.budgetMin || item.invitation.budgetMax">
                <span class="label">预算：</span>
                <span class="value">{{ formatInvitationBudget(item.invitation.budgetMin, item.invitation.budgetMax) }}</span>
              </div>
              <div class="info-item">
                <span class="label">发起时间：</span>
                <span class="value">{{ formatInvitationDate(item.invitation.createTime) }}</span>
              </div>
            </div>
            <div class="invitation-requirements" v-if="item.invitation.requirements">
              {{ item.invitation.requirements }}
            </div>
          </router-link>
        </div>
        <div v-else class="empty">
          <el-empty description="暂无收到的邀约" />
        </div>
      </div>
    </div>

    <CreateInvitationDialog
      v-model="showInviteDialog"
      :creator-id="userId"
      @success="handleInvitationSent"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useFavoriteStore } from '@/store/favorite'
import { Grid, Menu, List } from '@element-plus/icons-vue'
import WorkCard from '@/components/WorkCard.vue'
import CreatorLevelBadge from '@/components/CreatorLevelBadge.vue'
import CreateInvitationDialog from '@/components/CreateInvitationDialog.vue'
import request from '@/utils/request'
import { getLevelGradient, calculateLevelProgress } from '@/utils/creatorLevel'
import { ElMessage } from 'element-plus'

const route = useRoute()
const userStore = useUserStore()
const favoriteStore = useFavoriteStore()
const activeTab = ref('works')
const activeLayout = ref(localStorage.getItem('profileLayout') || 'masonry')
const loading = ref(true)
const loadingMore = ref(false)
const hasMore = ref(true)
const page = ref(1)
const pageSize = 10
const userId = ref(null)
const showInviteDialog = ref(false)
const profileInvitations = ref([])
const invitationsLoading = ref(false)
const pendingInvitationCount = ref(0)

const gridContainerClass = computed(() => {
  return `works-container works-${activeLayout.value}`
})

watch(activeLayout, (newVal) => {
  localStorage.setItem('profileLayout', newVal)
})

const user = ref({
  id: 1,
  username: '手作达人',
  nickname: '手作达人',
  avatar: '',
  bio: '热爱手工创作的艺术家'
})

const stats = ref({
  works: 0,
  followers: 0,
  following: 0,
  totalViews: 0,
  totalFavorites: 0,
  totalLikes: 0
})

const levelInfo = ref({})

const levelProgress = computed(() => {
  if (levelInfo.value.score === undefined || !user.value.creatorLevel) {
    return 0
  }
  return calculateLevelProgress(levelInfo.value.score, user.value.creatorLevel)
})

const levelGradient = computed(() => {
  return getLevelGradient(user.value.creatorLevel || 1)
})

const isOwner = computed(() => {
  return userStore.userInfo?.id === user.value.id
})

const works = ref([])

function transformWork(item) {
  return {
    id: item.id,
    title: item.title,
    description: item.description,
    coverImage: item.coverImage,
    categoryId: item.categoryId,
    viewCount: item.viewCount || 0,
    favoriteCount: item.favoriteCount || 0,
    likeCount: item.likeCount || 0,
    isHot: item.isHot,
    difficultyLevel: item.difficultyLevel,
    authorId: item.userId,
    authorName: user.value.nickname || user.value.username || '手作达人',
    authorAvatar: user.value.avatar || '',
    authorLevel: user.value.creatorLevel || 1
  }
}

async function loadUserInfo(uid) {
  try {
    const res = await request.get(`/user/${uid}`)
    if (res.code === 200 && res.data) {
      user.value = res.data
      stats.value.totalFavorites = res.data.totalFavoriteCount || 0
      stats.value.totalLikes = res.data.totalLikeCount || 0
    }
    const levelRes = await request.get(`/user/${uid}/level`)
    if (levelRes.code === 200 && levelRes.data) {
      levelInfo.value = levelRes.data
      stats.value.works = levelRes.data.totalWorkCount || 0
      stats.value.totalViews = levelRes.data.totalViewCount || 0
      stats.value.totalFavorites = levelRes.data.totalFavoriteCount || 0
      stats.value.totalLikes = levelRes.data.totalLikeCount || 0
    }
  } catch (e) {
    console.warn('加载用户信息失败', e)
  }
}

async function loadUserWorks(reset = false) {
  if (!userId.value) return
  if (reset) {
    page.value = 1
    works.value = []
    hasMore.value = true
  }
  if (page.value === 1) {
    loading.value = true
  }
  try {
    const res = await request.get(`/work/user/${userId.value}`, {
      params: { page: page.value, size: pageSize }
    })
    if (res.code === 200 && res.data) {
      const records = res.data.records || []
      if (page.value === 1) {
        works.value = records.map(transformWork)
      } else {
        works.value = [...works.value, ...records.map(transformWork)]
      }
      hasMore.value = works.value.length < res.data.total
      stats.value.works = res.data.total || 0
      const totalViews = records.reduce((sum, w) => sum + (w.viewCount || 0), 0)
      if (page.value === 1) {
        stats.value.totalViews = totalViews
      }
    } else {
      works.value = []
    }
  } catch (e) {
    console.error('加载用户作品失败', e)
  } finally {
    loading.value = false
  }
}

async function loadMoreWorks() {
  if (loadingMore.value || !hasMore.value) return
  loadingMore.value = true
  try {
    page.value++
    const res = await request.get(`/work/user/${userId.value}`, {
      params: { page: page.value, size: pageSize }
    })
    if (res.code === 200 && res.data) {
      const records = res.data.records || []
      works.value = [...works.value, ...records.map(transformWork)]
      hasMore.value = works.value.length < res.data.total
    }
  } catch (e) {
    console.error('加载更多作品失败', e)
    page.value--
  } finally {
    loadingMore.value = false
  }
}

const favorites = ref([])
const favoritesLoading = ref(false)

const displayedFavorites = computed(() => {
  favoriteStore.version
  return favorites.value.filter(w => favoriteStore.isFavorited(w.id))
})

async function loadProfileFavorites() {
  if (!userStore.isLoggedIn || !userStore.userInfo?.id) {
    favorites.value = []
    return
  }
  if (!isOwner.value) {
    favorites.value = []
    return
  }
  favoritesLoading.value = true
  try {
    const res = await request.get(`/favorite/list/${userStore.userInfo.id}`, {
      params: { page: 1, size: 50 }
    })
    if (res.code === 200 && res.data) {
      favorites.value = res.data.records || []
      const ids = favorites.value.map(w => Number(w.id))
      ids.forEach(id => favoriteStore.favoriteWorkIds.add(id))
      favoriteStore.touchVersion()
    }
  } catch (e) {
    console.warn('加载收藏失败', e)
  } finally {
    favoritesLoading.value = false
  }
}

function getInvitationStatusText(status) {
  const map = {
    0: '待接受',
    1: '已接受',
    2: '已拒绝',
    3: '进行中',
    4: '已完成',
    5: '已取消'
  }
  return map[status] || '未知'
}

function getInvitationStatusType(status) {
  const map = {
    0: 'warning',
    1: 'success',
    2: 'danger',
    3: 'primary',
    4: 'success',
    5: 'info'
  }
  return map[status] || 'info'
}

function formatInvitationBudget(min, max) {
  if (min && max) {
    return `¥${min} - ¥${max}`
  } else if (min) {
    return `¥${min} 起`
  } else if (max) {
    return `¥${max} 以内`
  }
  return '面议'
}

function formatInvitationDate(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

async function loadProfileInvitations() {
  if (!userStore.isLoggedIn || !isOwner.value || !userId.value) {
    profileInvitations.value = []
    pendingInvitationCount.value = 0
    return
  }
  invitationsLoading.value = true
  try {
    const res = await request.get('/invitation/creator', {
      params: { page: 1, size: 20 }
    })
    if (res.code === 200 && res.data) {
      profileInvitations.value = res.data.records || []
      pendingInvitationCount.value = profileInvitations.value.filter(r => r.invitation.status === 0).length
    }
  } catch (e) {
    console.warn('加载邀约失败', e)
  } finally {
    invitationsLoading.value = false
  }
}

function handleInvitationSent() {
  ElMessage.success('邀约已发送，请等待创作者回复')
}

let lastRefreshKey = 0

function checkNeedRefresh() {
  const refreshKey = parseInt(sessionStorage.getItem('workListRefreshKey') || '0')
  if (refreshKey > lastRefreshKey) {
    lastRefreshKey = refreshKey
    loadUserWorks(true)
  }
}

async function initProfile() {
  const uid = parseInt(route.params.userId) || userStore.userInfo?.id
  if (!uid) {
    ElMessage.warning('用户不存在')
    return
  }
  userId.value = uid
  user.value.id = uid
  await Promise.all([
    loadUserInfo(uid),
    loadUserWorks(true)
  ])
}

onMounted(() => {
  initProfile()
  if (activeTab.value === 'favorites') {
    loadProfileFavorites()
  }
  if (activeTab.value === 'invitations') {
    loadProfileInvitations()
  }
  window.addEventListener('focus', checkNeedRefresh)
})

onUnmounted(() => {
  window.removeEventListener('focus', checkNeedRefresh)
})

watch(() => route.params.userId, () => {
  initProfile()
})

watch(activeTab, (val) => {
  if (val === 'favorites') {
    loadProfileFavorites()
  } else if (val === 'invitations') {
    loadProfileInvitations()
  }
})

watch(() => favoriteStore.version, () => {
  if (activeTab.value === 'favorites' && isOwner.value) {
    loadProfileFavorites()
  }
})

watch(() => userStore.isLoggedIn, (val) => {
  if (val && activeTab.value === 'favorites') {
    loadProfileFavorites()
  } else if (!val) {
    favorites.value = []
  }
  if (val && activeTab.value === 'invitations') {
    loadProfileInvitations()
  } else if (!val) {
    profileInvitations.value = []
    pendingInvitationCount.value = 0
  }
})
</script>

<style scoped>
.profile {
  padding: 30px 0;
}

.profile-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 40px;
  margin-bottom: 30px;
}

.profile-info {
  display: flex;
  gap: 30px;
  align-items: flex-start;
}

.avatar {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  border: 4px solid #667eea;
}

.info h1 {
  font-size: 28px;
  color: #333;
  margin-bottom: 12px;
}

.username-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.username-row h1 {
  margin: 0;
}

.bio {
  font-size: 14px;
  color: #666;
  margin-bottom: 16px;
}

.level-progress {
  margin-bottom: 20px;
  padding: 12px 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.level-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.level-info .level-name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.level-info .level-score {
  font-size: 13px;
  color: #999;
}

.level-next {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
  text-align: right;
}

.user-stats {
  display: flex;
  gap: 40px;
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

.profile-actions {
  display: flex;
  gap: 12px;
}

.profile-tabs {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.tabs-nav-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 2px solid #e4e7ed;
  margin-bottom: 24px;
}

.tabs-nav {
  display: flex;
  gap: 32px;
}

.tab-item {
  padding: 12px 0;
  font-size: 16px;
  color: #606266;
  cursor: pointer;
  position: relative;
  transition: color 0.3s;
  margin-bottom: -2px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.tab-badge {
  background: #f56c6c;
  color: #fff;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
  line-height: 1;
}

.tab-item:hover {
  color: #667eea;
}

.tab-item.is-active {
  color: #667eea;
  font-weight: 600;
}

.tab-item.is-active::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  right: 0;
  height: 2px;
  background: #667eea;
}

.layout-switcher {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-bottom: 2px;
}

.layout-label {
  font-size: 13px;
  color: #666;
}

.empty {
  padding: 60px 0;
}

.loading {
  padding: 40px 0;
}

.load-more {
  text-align: center;
  padding: 30px 0;
}

.works-masonry {
  column-count: 4;
  column-gap: 20px;
}

.works-masonry :deep(.work-card) {
  break-inside: avoid;
  margin-bottom: 20px;
}

.works-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.works-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

@media (max-width: 1200px) {
  .works-masonry {
    column-count: 3;
  }
  .works-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .works-masonry {
    column-count: 2;
  }
  .works-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .tabs-nav-wrapper {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  .tabs-nav {
    width: 100%;
    gap: 24px;
  }
  .layout-switcher {
    width: 100%;
    padding-bottom: 12px;
  }
}

@media (max-width: 480px) {
  .works-masonry {
    column-count: 1;
  }
  .works-grid {
    grid-template-columns: 1fr;
  }
}

.invitation-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.invitation-card {
  padding: 20px 24px;
  cursor: pointer;
  transition: all 0.3s;
  display: block;
}

.invitation-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
}

.invitation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.invitation-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.invitation-info {
  display: flex;
  flex-wrap: wrap;
  gap: 16px 32px;
  margin-bottom: 12px;
}

.invitation-info .info-item {
  font-size: 14px;
}

.invitation-info .info-item .label {
  color: #909399;
}

.invitation-info .info-item .value {
  color: #333;
}

.invitation-requirements {
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 6px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>

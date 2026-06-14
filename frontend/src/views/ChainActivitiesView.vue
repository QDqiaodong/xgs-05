<template>
  <div class="chain-activities container">
    <div class="page-header card">
      <h1>作品接龙挑战</h1>
      <p class="subtitle">基于灵感作品二次创作，共建创意接龙链</p>
      <div class="intro-banner">
        <div class="intro-item">
          <div class="intro-icon">🎨</div>
          <div class="intro-text">
            <strong>灵感接力</strong>
            <span>选择心仪作品作为灵感来源</span>
          </div>
        </div>
        <div class="intro-item">
          <div class="intro-icon">🔗</div>
          <div class="intro-text">
            <strong>接龙创作</strong>
            <span>二次创作形成作品接龙链</span>
          </div>
        </div>
        <div class="intro-item">
          <div class="intro-icon">🌳</div>
          <div class="intro-text">
            <strong>谱系图谱</strong>
            <span>可视化查看完整接龙谱系</span>
          </div>
        </div>
      </div>
      <div class="filter-tabs">
        <div
          :class="['filter-tab', { 'is-active': activeTab === 'ongoing' }]"
          @click="activeTab = 'ongoing'; loadList(true)"
        >
          进行中
        </div>
        <div
          :class="['filter-tab', { 'is-active': activeTab === 'all' }]"
          @click="activeTab = 'all'; loadList(true)"
        >
          全部活动
        </div>
      </div>
    </div>

    <div v-if="loading && activities.length === 0" class="loading card">
      <el-skeleton :rows="5" animated />
    </div>

    <div v-else-if="activities.length > 0" class="activity-grid">
      <div
        v-for="activity in activities"
        :key="activity.id"
        class="activity-card card"
        @click="goToDetail(activity.id)"
      >
        <div
          class="activity-cover"
          :style="{ background: getCoverBg(activity) }"
        >
          <div v-if="isOngoing(activity)" class="status-badge ongoing">进行中</div>
          <div v-else-if="isUpcoming(activity)" class="status-badge upcoming">即将开始</div>
          <div v-else class="status-badge ended">已结束</div>
          <h2 class="activity-title">{{ activity.title }}</h2>
          <div v-if="activity.theme" class="theme-tag">
            <el-icon><MagicStick /></el-icon>
            主题创作
          </div>
        </div>
        <div class="activity-info">
          <p v-if="activity.description" class="activity-desc">{{ activity.description }}</p>
          <div class="activity-meta">
            <span class="meta-item">
              <el-icon><Calendar /></el-icon>
              {{ formatDate(activity.startTime) }} - {{ formatDate(activity.endTime) }}
            </span>
          </div>
          <div class="activity-stats">
            <span class="stat">
              <el-icon><Link /></el-icon>
              {{ activity.workCount || 0 }} 接龙作品
            </span>
            <span class="stat">
              <el-icon><User /></el-icon>
              {{ activity.participantCount || 0 }} 参与人数
            </span>
            <span class="stat">
              <el-icon><View /></el-icon>
              {{ activity.viewCount || 0 }} 浏览
            </span>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="empty card">
      <el-empty description="暂无接龙活动" />
    </div>

    <div v-if="hasMore && !loading" class="load-more">
      <el-button :loading="loadingMore" :disabled="loadingMore" @click="loadMore">
        加载更多
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Calendar, View, User, Link, MagicStick } from '@element-plus/icons-vue'
import { getChainActivityList, getOngoingChainActivities } from '@/utils/request'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('ongoing')
const activities = ref([])
const loading = ref(true)
const loadingMore = ref(false)
const hasMore = ref(true)
const page = ref(1)
const pageSize = 10
let fetchVersion = 0

const coverBgs = [
  'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
  'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
  'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
  'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
  'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)',
  'linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%)',
  'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)'
]

function getCoverBg(activity) {
  if (activity.coverImage) {
    return `url(${activity.coverImage}) center/cover no-repeat`
  }
  return coverBgs[activity.id % coverBgs.length]
}

function isOngoing(activity) {
  const now = new Date()
  const start = new Date(activity.startTime)
  const end = new Date(activity.endTime)
  return activity.status === 1 && now >= start && now <= end
}

function isUpcoming(activity) {
  const now = new Date()
  const start = new Date(activity.startTime)
  return now < start
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getMonth() + 1}月${d.getDate()}日`
}

function goToDetail(id) {
  router.push(`/chain-activity/${id}`)
}

async function loadList(reset = false) {
  if (reset) {
    page.value = 1
    activities.value = []
    hasMore.value = true
  }
  if (loading.value || loadingMore.value) return
  if (!hasMore.value) return

  const version = ++fetchVersion
  if (reset) {
    loading.value = true
  } else {
    loadingMore.value = true
  }
  try {
    const params = { page: page.value, size: pageSize }
    let res
    if (activeTab.value === 'ongoing') {
      res = await getOngoingChainActivities(params)
    } else {
      res = await getChainActivityList(params)
    }
    if (version !== fetchVersion) return
    if (res.code === 200 && res.data) {
      const records = res.data.records || []
      activities.value = activities.value.concat(records)
      hasMore.value = records.length >= pageSize
      page.value++
    } else {
      ElMessage.error(res.message || '加载失败')
    }
  } catch (e) {
    console.error('加载接龙活动列表失败', e)
  } finally {
    if (version === fetchVersion) {
      loading.value = false
      loadingMore.value = false
    }
  }
}

function loadMore() {
  loadList(false)
}

onMounted(() => {
  loadList(true)
})
</script>

<style scoped>
.chain-activities {
  padding: 24px 0;
}

.page-header {
  padding: 32px;
  margin-bottom: 24px;
}

.page-header h1 {
  font-size: 28px;
  margin: 0 0 8px;
  background: linear-gradient(135deg, #667eea 0%, #f093fb 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.subtitle {
  color: #666;
  margin: 0 0 24px;
  font-size: 14px;
}

.intro-banner {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 24px;
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf3 100%);
  border-radius: 12px;
}

.intro-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.intro-icon {
  font-size: 32px;
  flex-shrink: 0;
}

.intro-text {
  display: flex;
  flex-direction: column;
}

.intro-text strong {
  font-size: 15px;
  color: #333;
  margin-bottom: 2px;
}

.intro-text span {
  font-size: 12px;
  color: #999;
}

.filter-tabs {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.filter-tab {
  padding: 8px 20px;
  border-radius: 20px;
  background: #f5f5f5;
  color: #666;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
}

.filter-tab:hover {
  background: #e8e8e8;
}

.filter-tab.is-active {
  background: linear-gradient(135deg, #667eea 0%, #f093fb 100%);
  color: #fff;
}

.activity-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.activity-card {
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
  overflow: hidden;
}

.activity-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.activity-cover {
  height: 180px;
  position: relative;
  display: flex;
  align-items: flex-end;
  padding: 20px;
}

.activity-cover::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(transparent 40%, rgba(0, 0, 0, 0.5));
}

.status-badge {
  position: absolute;
  top: 16px;
  right: 16px;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  color: #fff;
  z-index: 1;
}

.status-badge.ongoing {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
}

.status-badge.upcoming {
  background: linear-gradient(135deg, #f2994a 0%, #f2c94c 100%);
}

.status-badge.ended {
  background: #999;
}

.theme-tag {
  position: absolute;
  top: 16px;
  left: 16px;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  background: rgba(255, 255, 255, 0.25);
  color: #fff;
  backdrop-filter: blur(8px);
  z-index: 1;
}

.activity-title {
  color: #fff;
  font-size: 20px;
  margin: 0;
  position: relative;
  z-index: 1;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.activity-info {
  padding: 16px;
}

.activity-desc {
  color: #666;
  font-size: 14px;
  line-height: 1.5;
  margin: 0 0 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.activity-meta {
  margin-bottom: 12px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #999;
  font-size: 13px;
}

.meta-item .el-icon {
  font-size: 14px;
}

.activity-stats {
  display: flex;
  gap: 16px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
  flex-wrap: wrap;
}

.stat {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #999;
  font-size: 13px;
}

.stat .el-icon {
  font-size: 14px;
}

.empty {
  padding: 60px;
  text-align: center;
}

.loading {
  padding: 24px;
}

.load-more {
  text-align: center;
  padding: 32px 0;
}

@media (max-width: 768px) {
  .intro-banner {
    grid-template-columns: 1fr;
  }
}
</style>

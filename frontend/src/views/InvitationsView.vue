<template>
  <div class="invitations container">
    <div class="view-hero">
      <div class="hero-content">
        <div class="hero-title-row">
          <h1 class="hero-title">定制邀约中心</h1>
          <div class="hero-badge">💌 连接创意与需求</div>
        </div>
        <p class="hero-subtitle">
          向心仪创作者发起专属定制邀约，追踪每一个创作节点，让沟通更高效
        </p>
      </div>
      <div class="hero-stats">
        <div class="stat-card">
          <div class="stat-icon received">📥</div>
          <div class="stat-info">
            <div class="stat-value">{{ receivedTotal }}</div>
            <div class="stat-label">收到的邀约</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon pending">⏳</div>
          <div class="stat-info">
            <div class="stat-value">{{ pendingCount }}</div>
            <div class="stat-label">待处理</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon sent">📤</div>
          <div class="stat-info">
            <div class="stat-value">{{ sentTotal }}</div>
            <div class="stat-label">发起的邀约</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon done">✅</div>
          <div class="stat-info">
            <div class="stat-value">{{ completedCount }}</div>
            <div class="stat-label">已完成</div>
          </div>
        </div>
      </div>
    </div>

    <div class="tabs-section card">
      <div class="tabs-nav">
        <div
          :class="['tab-item', { 'is-active': activeTab === 'received' }]"
          @click="switchTab('received')"
        >
          <div class="tab-icon">📥</div>
          <div class="tab-text">
            <span class="tab-name">收到的邀约</span>
            <span v-if="receivedPending > 0" class="badge">{{ receivedPending }}</span>
          </div>
        </div>
        <div
          :class="['tab-item', { 'is-active': activeTab === 'sent' }]"
          @click="switchTab('sent')"
        >
          <div class="tab-icon">📤</div>
          <div class="tab-text">
            <span class="tab-name">发起的邀约</span>
          </div>
        </div>
      </div>

      <div class="filter-row">
        <div class="status-filter">
          <el-radio-group v-model="statusFilter" size="default" @change="loadInvitations(true)">
            <el-radio-button :value="null">
              <el-icon><Collection /></el-icon>
              全部
            </el-radio-button>
            <el-radio-button :value="0">
              <el-icon><Clock /></el-icon>
              待接受
            </el-radio-button>
            <el-radio-button :value="1">
              <el-icon><CircleCheck /></el-icon>
              已接受
            </el-radio-button>
            <el-radio-button :value="2">
              <el-icon><CircleClose /></el-icon>
              已拒绝
            </el-radio-button>
            <el-radio-button :value="3">
              <el-icon><Loading /></el-icon>
              进行中
            </el-radio-button>
            <el-radio-button :value="4">
              <el-icon><CircleCheckFilled /></el-icon>
              已完成
            </el-radio-button>
            <el-radio-button :value="5">
              <el-icon><Delete /></el-icon>
              已取消
            </el-radio-button>
          </el-radio-group>
        </div>
      </div>
    </div>

    <div v-if="loading" class="loading-wrapper">
      <el-skeleton :rows="4" animated />
    </div>
    <div v-else-if="invitations.length > 0" class="invitation-list">
      <router-link
        v-for="item in invitations"
        :key="item.invitation.id"
        :to="`/invitation/${item.invitation.id}`"
        class="invitation-card card"
      >
        <div class="card-left">
          <div class="status-indicator" :class="'status-' + item.invitation.status"></div>
          <div class="card-avatar">
            <img
              :src="activeTab === 'sent'
                ? (item.creator?.avatar || 'https://via.placeholder.com/60')
                : (item.client?.avatar || 'https://via.placeholder.com/60')"
              alt=""
            />
          </div>
        </div>
        <div class="card-main">
          <div class="card-header">
            <h3 class="invitation-title">{{ item.invitation.title }}</h3>
            <el-tag :type="getStatusType(item.invitation.status)" size="default" effect="dark" class="status-tag">
              <el-icon class="tag-icon"><component :is="getStatusIcon(item.invitation.status)" /></el-icon>
              {{ getStatusText(item.invitation.status) }}
            </el-tag>
          </div>
          <div class="party-row">
            <div class="party-item">
              <span class="party-label">{{ activeTab === 'sent' ? '创作者' : '客户' }}</span>
              <span class="party-name">
                {{ activeTab === 'sent'
                  ? (item.creator?.nickname || item.creator?.username || '匿名用户')
                  : (item.client?.nickname || item.client?.username || '匿名用户') }}
              </span>
            </div>
            <div class="party-divider"></div>
            <div class="party-item" v-if="item.invitation.budgetMin || item.invitation.budgetMax">
              <span class="party-label">预算</span>
              <span class="party-amount">{{ formatBudget(item.invitation.budgetMin, item.invitation.budgetMax) }}</span>
            </div>
            <div class="party-divider" v-if="item.invitation.budgetMin || item.invitation.budgetMax"></div>
            <div class="party-item" v-if="item.invitation.expectedDays">
              <span class="party-label">周期</span>
              <span class="party-days">{{ item.invitation.expectedDays }} 天</span>
            </div>
            <div class="party-divider" v-if="item.invitation.expectedDays"></div>
            <div class="party-item">
              <span class="party-label">发起时间</span>
              <span class="party-date">{{ formatDate(item.invitation.createTime) }}</span>
            </div>
          </div>
          <div class="requirements-preview" v-if="item.invitation.requirements">
            <p>{{ item.invitation.requirements }}</p>
          </div>
          <div class="card-footer">
            <div class="tags-row" v-if="item.invitation.referenceImages">
              <el-tag type="warning" effect="light" size="small">
                <el-icon><Picture /></el-icon>
                含参考图片
              </el-tag>
            </div>
            <div class="view-detail">
              查看详情
              <el-icon><ArrowRight /></el-icon>
            </div>
          </div>
        </div>
      </router-link>
    </div>
    <div v-else class="empty-wrapper card">
      <div class="empty-icon">{{ activeTab === 'sent' ? '📭' : '📬' }}</div>
      <h3 class="empty-title">{{ activeTab === 'sent' ? '还没有发起过邀约' : '还没有收到邀约' }}</h3>
      <p class="empty-desc">
        {{ activeTab === 'sent'
          ? '去发现心仪的创作者，开启你的专属定制之旅吧'
          : '当有用户向你发起定制邀约时，会在这里显示' }}
      </p>
      <el-button
        v-if="activeTab === 'sent'"
        type="primary"
        size="large"
        @click="$router.push('/')"
        class="explore-btn"
      >
        <el-icon><Compass /></el-icon>
        去发现创作者
      </el-button>
    </div>

    <div v-if="hasMore && invitations.length > 0" class="load-more">
      <el-button :loading="loadingMore" @click="loadMore" size="large" class="load-more-btn">
        <el-icon><ArrowDown /></el-icon>
        加载更多
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Collection, Clock, CircleCheck, CircleCheckFilled, CircleClose, Loading, Delete,
  Picture, ArrowRight, Compass, ArrowDown
} from '@element-plus/icons-vue'
import request from '@/utils/request'

const route = useRoute()
const activeTab = ref('received')
const statusFilter = ref(null)
const loading = ref(true)
const loadingMore = ref(false)
const hasMore = ref(true)
const page = ref(1)
const pageSize = 10
const invitations = ref([])

const receivedTotal = ref(0)
const sentTotal = ref(0)
const pendingCount = ref(0)
const completedCount = ref(0)
const receivedPending = ref(0)

function getStatusText(status) {
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

function getStatusType(status) {
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

function getStatusIcon(status) {
  const map = {
    0: Clock,
    1: CircleCheck,
    2: CircleClose,
    3: Loading,
    4: CircleCheckFilled,
    5: Delete
  }
  return map[status] || Clock
}

function formatBudget(min, max) {
  if (min && max) {
    return `¥${min} - ¥${max}`
  } else if (min) {
    return `¥${min} 起`
  } else if (max) {
    return `¥${max} 以内`
  }
  return '面议'
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

async function loadStats() {
  try {
    const [receivedRes, sentRes] = await Promise.all([
      request.get('/invitation/creator', { params: { page: 1, size: 100 } }),
      request.get('/invitation/client', { params: { page: 1, size: 100 } })
    ])
    if (receivedRes.code === 200) {
      const records = receivedRes.data?.records || []
      receivedTotal.value = receivedRes.data?.total || 0
      receivedPending.value = records.filter(r => r.invitation.status === 0).length
    }
    if (sentRes.code === 200) {
      const records = sentRes.data?.records || []
      sentTotal.value = sentRes.data?.total || 0
    }
    pendingCount.value = receivedPending.value
    const allRecords = [
      ...(receivedRes.data?.records || []),
      ...(sentRes.data?.records || [])
    ]
    completedCount.value = allRecords.filter(r => r.invitation.status === 4).length
  } catch (e) {
    console.warn('加载统计数据失败', e)
  }
}

async function loadInvitations(reset = false) {
  if (reset) {
    page.value = 1
    invitations.value = []
    hasMore.value = true
  }
  if (page.value === 1) {
    loading.value = true
  }
  try {
    const url = activeTab.value === 'sent' ? '/invitation/client' : '/invitation/creator'
    const params = {
      page: page.value,
      size: pageSize
    }
    if (statusFilter.value !== null) {
      params.status = statusFilter.value
    }
    const res = await request.get(url, { params })
    if (res.code === 200 && res.data) {
      const records = res.data.records || []
      if (page.value === 1) {
        invitations.value = records
      } else {
        invitations.value = [...invitations.value, ...records]
      }
      hasMore.value = invitations.value.length < res.data.total
    }
  } catch (e) {
    console.error('加载邀约列表失败', e)
  } finally {
    loading.value = false
  }
}

async function loadMore() {
  if (loadingMore.value || !hasMore.value) return
  loadingMore.value = true
  try {
    page.value++
    await loadInvitations()
  } finally {
    loadingMore.value = false
  }
}

function switchTab(tab) {
  if (activeTab.value === tab) return
  activeTab.value = tab
  statusFilter.value = null
  loadInvitations(true)
}

onMounted(() => {
  const tab = route.query.tab
  if (tab === 'sent') {
    activeTab.value = 'sent'
  }
  loadStats()
  loadInvitations(true)
})
</script>

<style scoped>
.invitations {
  padding: 24px 0 60px 0;
}

.view-hero {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
  border-radius: 20px;
  padding: 48px 40px 40px;
  margin-bottom: 32px;
  color: #fff;
  position: relative;
  overflow: hidden;
}

.view-hero::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -20%;
  width: 400px;
  height: 400px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 50%;
}

.view-hero::after {
  content: '';
  position: absolute;
  bottom: -30%;
  left: -10%;
  width: 300px;
  height: 300px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 50%;
}

.hero-content {
  position: relative;
  z-index: 1;
  max-width: 700px;
  margin-bottom: 32px;
}

.hero-title-row {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.hero-title {
  font-size: 36px;
  font-weight: 800;
  margin: 0;
  background: linear-gradient(135deg, #fff 0%, #ffe8f4 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-badge {
  padding: 6px 16px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  backdrop-filter: blur(10px);
}

.hero-subtitle {
  font-size: 16px;
  margin: 0;
  opacity: 0.92;
  line-height: 1.7;
}

.hero-stats {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  flex-shrink: 0;
}

.stat-icon.received {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
}

.stat-icon.pending {
  background: linear-gradient(135deg, #f2994a 0%, #f2c94c 100%);
}

.stat-icon.sent {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon.done {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-info {
  min-width: 0;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  line-height: 1.1;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  opacity: 0.85;
}

.tabs-section {
  padding: 8px 24px 20px;
  margin-bottom: 24px;
}

.tabs-nav {
  display: flex;
  gap: 32px;
  border-bottom: 2px solid #e4e7ed;
  margin-bottom: 20px;
}

.tab-item {
  padding: 18px 0 16px;
  cursor: pointer;
  position: relative;
  transition: all 0.3s;
  margin-bottom: -2px;
  display: flex;
  align-items: center;
  gap: 12px;
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
  height: 3px;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  border-radius: 3px 3px 0 0;
}

.tab-icon {
  font-size: 22px;
}

.tab-text {
  display: flex;
  align-items: center;
  gap: 10px;
}

.tab-name {
  font-size: 16px;
}

.badge {
  background: #f56c6c;
  color: #fff;
  font-size: 12px;
  padding: 2px 10px;
  border-radius: 10px;
  font-weight: 500;
  line-height: 1.5;
}

.filter-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status-filter :deep(.el-radio-button__inner) {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 18px;
}

.loading-wrapper {
  padding: 40px 20px;
}

.invitation-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.invitation-card {
  padding: 0;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  overflow: hidden;
  border: 1px solid transparent;
}

.invitation-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.15);
  border-color: #e0e7ff;
}

.card-left {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px 20px;
  background: #fafbff;
  gap: 16px;
}

.status-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.status-indicator.status-0 {
  background: #e6a23c;
  box-shadow: 0 0 0 4px rgba(230, 162, 60, 0.15);
}

.status-indicator.status-1 {
  background: #67c23a;
  box-shadow: 0 0 0 4px rgba(103, 194, 58, 0.15);
}

.status-indicator.status-2 {
  background: #f56c6c;
  box-shadow: 0 0 0 4px rgba(245, 108, 108, 0.15);
}

.status-indicator.status-3 {
  background: #409eff;
  box-shadow: 0 0 0 4px rgba(64, 158, 255, 0.15);
}

.status-indicator.status-4 {
  background: #67c23a;
  box-shadow: 0 0 0 4px rgba(103, 194, 58, 0.15);
}

.status-indicator.status-5 {
  background: #909399;
  box-shadow: 0 0 0 4px rgba(144, 147, 153, 0.15);
}

.card-avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  flex-shrink: 0;
}

.card-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.card-main {
  flex: 1;
  padding: 24px 28px;
  min-width: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  gap: 16px;
}

.invitation-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0;
  line-height: 1.4;
}

.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  flex-shrink: 0;
}

.tag-icon {
  font-size: 13px;
}

.party-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 10px;
}

.party-item {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.party-label {
  font-size: 12px;
  color: #909399;
}

.party-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.party-amount {
  font-size: 15px;
  font-weight: 600;
  color: #f56c6c;
}

.party-days {
  font-size: 14px;
  font-weight: 500;
  color: #409eff;
}

.party-date {
  font-size: 13px;
  color: #606266;
}

.party-divider {
  width: 1px;
  height: 16px;
  background: #dcdfe6;
}

.requirements-preview {
  margin-bottom: 16px;
}

.requirements-preview p {
  color: #606266;
  font-size: 14px;
  line-height: 1.7;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 14px;
  border-top: 1px solid #f0f0f0;
}

.tags-row {
  display: flex;
  gap: 8px;
}

.view-detail {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #667eea;
  font-size: 14px;
  font-weight: 500;
  transition: gap 0.2s;
}

.invitation-card:hover .view-detail {
  gap: 8px;
}

.empty-wrapper {
  padding: 60px 40px;
  text-align: center;
}

.empty-icon {
  font-size: 72px;
  margin-bottom: 20px;
}

.empty-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 10px 0;
}

.empty-desc {
  font-size: 14px;
  color: #909399;
  margin: 0 0 28px 0;
  line-height: 1.6;
}

.explore-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 14px 32px;
}

.explore-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.load-more {
  text-align: center;
  padding: 36px 0 10px;
}

.load-more-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 14px 40px;
}

@media (max-width: 1024px) {
  .hero-stats {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .view-hero {
    padding: 32px 24px;
  }

  .hero-title {
    font-size: 26px;
  }

  .hero-stats {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .stat-card {
    padding: 16px;
  }

  .stat-icon {
    width: 44px;
    height: 44px;
    font-size: 22px;
  }

  .stat-value {
    font-size: 22px;
  }

  .tabs-section {
    padding: 4px 16px 16px;
  }

  .invitation-card {
    flex-direction: column;
  }

  .card-left {
    flex-direction: row;
    padding: 16px 20px;
  }

  .card-main {
    padding: 16px 20px 20px;
  }
}
</style>

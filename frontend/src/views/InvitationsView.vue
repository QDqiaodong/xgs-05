<template>
  <div class="invitations container">
    <h1 class="page-title">定制邀约</h1>

    <div class="tabs-nav">
      <div
        :class="['tab-item', { 'is-active': activeTab === 'received' }]"
        @click="activeTab = 'received'"
      >
        收到的邀约
        <span v-if="receivedCount > 0" class="badge">{{ receivedCount }}</span>
      </div>
      <div
        :class="['tab-item', { 'is-active': activeTab === 'sent' }]"
        @click="activeTab = 'sent'"
      >
        发起的邀约
      </div>
    </div>

    <div class="status-filter">
      <el-radio-group v-model="statusFilter" size="small" @change="loadInvitations(true)">
        <el-radio-button :value="null">全部</el-radio-button>
        <el-radio-button :value="0">待接受</el-radio-button>
        <el-radio-button :value="1">已接受</el-radio-button>
        <el-radio-button :value="2">已拒绝</el-radio-button>
        <el-radio-button :value="3">进行中</el-radio-button>
        <el-radio-button :value="4">已完成</el-radio-button>
        <el-radio-button :value="5">已取消</el-radio-button>
      </el-radio-group>
    </div>

    <div v-if="loading" class="loading">
      <el-skeleton :rows="5" animated />
    </div>
    <div v-else-if="invitations.length > 0" class="invitation-list">
      <router-link
        v-for="item in invitations"
        :key="item.invitation.id"
        :to="`/invitation/${item.invitation.id}`"
        class="invitation-card card"
      >
        <div class="invitation-header">
          <div class="invitation-title">{{ item.invitation.title }}</div>
          <el-tag :type="getStatusType(item.invitation.status)" size="small">
            {{ getStatusText(item.invitation.status) }}
          </el-tag>
        </div>
        <div class="invitation-info">
          <div class="info-item">
            <span class="label">{{ activeTab === 'sent' ? '创作者' : '客户' }}：</span>
            <span class="value">
              {{ activeTab === 'sent' ? (item.creator?.nickname || item.creator?.username) : (item.client?.nickname || item.client?.username) }}
            </span>
          </div>
          <div class="info-item" v-if="item.invitation.budgetMin || item.invitation.budgetMax">
            <span class="label">预算：</span>
            <span class="value">
              {{ formatBudget(item.invitation.budgetMin, item.invitation.budgetMax) }}
            </span>
          </div>
          <div class="info-item" v-if="item.invitation.expectedDays">
            <span class="label">期望周期：</span>
            <span class="value">{{ item.invitation.expectedDays }} 天</span>
          </div>
          <div class="info-item">
            <span class="label">发起时间：</span>
            <span class="value">{{ formatDate(item.invitation.createTime) }}</span>
          </div>
        </div>
        <div class="invitation-requirements" v-if="item.invitation.requirements">
          {{ item.invitation.requirements }}
        </div>
      </router-link>
    </div>
    <div v-else class="empty">
      <el-empty :description="activeTab === 'sent' ? '暂无发起的邀约' : '暂无收到的邀约'" />
    </div>

    <div v-if="hasMore && invitations.length > 0" class="load-more">
      <el-button :loading="loadingMore" @click="loadMore">加载更多</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
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
const receivedCount = ref(0)

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
      if (activeTab.value === 'received' && statusFilter.value === null) {
        receivedCount.value = records.filter(r => r.invitation.status === 0).length
      }
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

watch(activeTab, () => {
  loadInvitations(true)
})

onMounted(() => {
  const tab = route.query.tab
  if (tab === 'sent') {
    activeTab.value = 'sent'
  }
  loadInvitations(true)
})
</script>

<style scoped>
.invitations {
  padding: 30px 0;
}

.page-title {
  font-size: 28px;
  color: #333;
  margin-bottom: 24px;
}

.tabs-nav {
  display: flex;
  gap: 32px;
  border-bottom: 2px solid #e4e7ed;
  margin-bottom: 20px;
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
  gap: 8px;
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

.badge {
  background: #f56c6c;
  color: #fff;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
}

.status-filter {
  margin-bottom: 20px;
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

.info-item {
  font-size: 14px;
}

.info-item .label {
  color: #909399;
}

.info-item .value {
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
</style>

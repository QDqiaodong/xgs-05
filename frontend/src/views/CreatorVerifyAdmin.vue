<template>
  <div class="creator-verify-admin container">
    <div class="page-header card">
      <h1>创作者认证审核</h1>
      <div class="filter-tabs">
        <div
          :class="['filter-tab', { 'is-active': activeStatus === null }]"
          @click="activeStatus = null; loadList(true)"
        >
          全部 <span class="count">{{ totalCounts.all || 0 }}</span>
        </div>
        <div
          :class="['filter-tab', { 'is-active': activeStatus === 0 }]"
          @click="activeStatus = 0; loadList(true)"
        >
          待审核 <span class="count warning">{{ totalCounts.pending || 0 }}</span>
        </div>
        <div
          :class="['filter-tab', { 'is-active': activeStatus === 1 }]"
          @click="activeStatus = 1; loadList(true)"
        >
          已通过 <span class="count success">{{ totalCounts.approved || 0 }}</span>
        </div>
        <div
          :class="['filter-tab', { 'is-active': activeStatus === 2 }]"
          @click="activeStatus = 2; loadList(true)"
        >
          已拒绝 <span class="count danger">{{ totalCounts.rejected || 0 }}</span>
        </div>
      </div>
    </div>

    <div class="verify-list">
      <div v-if="loading" class="loading card">
        <el-skeleton :rows="5" animated />
      </div>
      <div v-else-if="records.length > 0">
        <div
          v-for="item in records"
          :key="item.verification.id"
          class="verify-item card"
        >
          <div class="item-header">
            <div class="user-info">
              <img
                :src="item.user?.avatar || 'https://via.placeholder.com/60'"
                alt="avatar"
                class="user-avatar"
              />
              <div class="user-detail">
                <div class="username-row">
                  <span class="nickname">{{ item.user?.nickname || item.user?.username }}</span>
                  <CreatorLevelBadge
                    v-if="item.user?.creatorLevel"
                    :level="item.user.creatorLevel"
                    size="small"
                  />
                </div>
                <div class="user-meta">
                  <span>@{{ item.user?.username }}</span>
                  <span class="divider">·</span>
                  <span>申请时间：{{ formatDate(item.verification.createTime) }}</span>
                </div>
              </div>
            </div>
            <el-tag :type="getStatusType(item.verification.status)" size="large">
              {{ getStatusText(item.verification.status) }}
            </el-tag>
          </div>

          <div class="item-body">
            <div class="info-grid">
              <div class="info-item">
                <span class="label">真实姓名</span>
                <span class="value">{{ item.verification.realName || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="label">联系方式</span>
                <span class="value">{{ item.verification.contactInfo || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="label">擅长领域</span>
                <span class="value">{{ item.verification.expertiseField || '-' }}</span>
              </div>
            </div>

            <div class="info-block">
              <div class="block-title">作品集链接</div>
              <div v-if="parseLinks(item.verification.portfolioLinks).length > 0" class="link-list">
                <a
                  v-for="(link, idx) in parseLinks(item.verification.portfolioLinks)"
                  :key="idx"
                  :href="link"
                  target="_blank"
                  rel="noopener noreferrer"
                  class="link-item"
                >
                  <el-icon><Link /></el-icon>
                  {{ link }}
                </a>
              </div>
              <div v-else class="empty-text">-</div>
            </div>

            <div class="info-block">
              <div class="block-title">创作经历</div>
              <div class="block-content">
                {{ item.verification.creationExperience || '-' }}
              </div>
            </div>

            <div v-if="item.verification.additionalMaterials" class="info-block">
              <div class="block-title">补充材料</div>
              <div class="block-content">
                {{ item.verification.additionalMaterials }}
              </div>
            </div>

            <div v-if="item.verification.status !== 0" class="review-block">
              <el-divider />
              <div class="review-info">
                <span class="label">审核时间：</span>
                <span>{{ formatDate(item.verification.reviewTime) }}</span>
              </div>
              <div v-if="item.verification.reviewRemark" class="review-info">
                <span class="label">审核备注：</span>
                <span>{{ item.verification.reviewRemark }}</span>
              </div>
            </div>
          </div>

          <div v-if="item.verification.status === 0" class="item-actions">
            <el-button
              type="danger"
              :loading="rejectingId === item.verification.id"
              @click="openRejectDialog(item)"
            >
              拒绝
            </el-button>
            <el-button
              type="success"
              :loading="approvingId === item.verification.id"
              @click="handleApprove(item.verification.id)"
            >
              通过
            </el-button>
          </div>
        </div>
      </div>
      <div v-else class="empty card">
        <el-empty description="暂无申请记录" />
      </div>

      <div v-if="hasMore && records.length > 0" class="load-more">
        <el-button :loading="loadingMore" @click="loadMore">加载更多</el-button>
      </div>
    </div>

    <el-dialog
      v-model="rejectDialogVisible"
      title="拒绝认证申请"
      width="500px"
    >
      <el-form label-width="80px">
        <el-form-item label="拒绝原因">
          <el-input
            v-model="rejectRemark"
            type="textarea"
            :rows="4"
            placeholder="请输入拒绝原因（选填）"
            maxlength="500"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Link } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import {
  getVerificationList,
  approveVerification,
  rejectVerification
} from '@/utils/request'
import CreatorLevelBadge from '@/components/CreatorLevelBadge.vue'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const loadingMore = ref(false)
const hasMore = ref(true)
const page = ref(1)
const pageSize = 10
const activeStatus = ref(null)
const records = ref([])
const totalCounts = ref({
  all: 0,
  pending: 0,
  approved: 0,
  rejected: 0
})

const approvingId = ref(null)
const rejectingId = ref(null)
const rejectDialogVisible = ref(false)
const rejectRemark = ref('')
const currentRejectItem = ref(null)

function getStatusText(status) {
  const map = { 0: '待审核', 1: '已通过', 2: '已拒绝' }
  return map[status] || '未知'
}

function getStatusType(status) {
  const map = { 0: 'warning', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

function parseLinks(str) {
  if (!str) return []
  try {
    const arr = JSON.parse(str)
    return Array.isArray(arr) ? arr.filter(l => l && l.trim()) : []
  } catch (e) {
    return str ? [str] : []
  }
}

async function loadList(reset = false) {
  if (reset) {
    page.value = 1
    records.value = []
    hasMore.value = true
  }
  if (page.value === 1) {
    loading.value = true
  }
  try {
    const res = await getVerificationList({
      page: page.value,
      size: pageSize,
      status: activeStatus.value
    })
    if (res.code === 200 && res.data) {
      const list = res.data.records || []
      if (page.value === 1) {
        records.value = list
      } else {
        records.value = [...records.value, ...list]
      }
      hasMore.value = records.value.length < (res.data.total || 0)
    }
  } catch (e) {
    console.error('加载审核列表失败', e)
  } finally {
    loading.value = false
  }
}

async function loadMore() {
  if (loadingMore.value || !hasMore.value) return
  loadingMore.value = true
  try {
    page.value++
    await loadList(false)
  } finally {
    loadingMore.value = false
  }
}

async function loadCounts() {
  try {
    const results = await Promise.all([
      getVerificationList({ page: 1, size: 1, status: null }),
      getVerificationList({ page: 1, size: 1, status: 0 }),
      getVerificationList({ page: 1, size: 1, status: 1 }),
      getVerificationList({ page: 1, size: 1, status: 2 })
    ])
    totalCounts.value = {
      all: results[0]?.data?.total || 0,
      pending: results[1]?.data?.total || 0,
      approved: results[2]?.data?.total || 0,
      rejected: results[3]?.data?.total || 0
    }
  } catch (e) {
    console.warn('加载统计数量失败', e)
  }
}

async function handleApprove(id) {
  try {
    await ElMessageBox.confirm('确定通过此创作者认证申请吗？通过后用户将获得认证标识和作品推荐权重。', '确认通过', {
      type: 'success'
    })
  } catch (e) {
    return
  }
  approvingId.value = id
  try {
    const res = await approveVerification(id, '')
    if (res.code === 200) {
      ElMessage.success('已通过认证')
      loadCounts()
      loadList(true)
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    console.error('通过认证失败', e)
  } finally {
    approvingId.value = null
  }
}

function openRejectDialog(item) {
  currentRejectItem.value = item
  rejectRemark.value = ''
  rejectDialogVisible.value = true
}

async function confirmReject() {
  if (!currentRejectItem.value) return
  const id = currentRejectItem.value.verification.id
  rejectingId.value = id
  try {
    const res = await rejectVerification(id, rejectRemark.value)
    if (res.code === 200) {
      ElMessage.success('已拒绝申请')
      rejectDialogVisible.value = false
      loadCounts()
      loadList(true)
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    console.error('拒绝认证失败', e)
  } finally {
    rejectingId.value = null
  }
}

onMounted(() => {
  if (!userStore.isAdmin) {
    ElMessage.warning('无权限访问该页面')
    router.replace('/')
    return
  }
  loadList(true)
  loadCounts()
})

watch(activeStatus, () => {
  loadList(true)
})
</script>

<style scoped>
.creator-verify-admin {
  padding: 30px 0;
}

.page-header {
  padding: 24px 32px;
  margin-bottom: 24px;
}

.page-header h1 {
  font-size: 24px;
  color: #333;
  margin: 0 0 20px 0;
}

.filter-tabs {
  display: flex;
  gap: 32px;
  border-bottom: 2px solid #e4e7ed;
  padding-bottom: 4px;
  margin-bottom: -2px;
}

.filter-tab {
  padding: 12px 0;
  font-size: 15px;
  color: #606266;
  cursor: pointer;
  position: relative;
  transition: color 0.3s;
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: -4px;
}

.filter-tab:hover {
  color: #667eea;
}

.filter-tab.is-active {
  color: #667eea;
  font-weight: 600;
}

.filter-tab.is-active::after {
  content: '';
  position: absolute;
  bottom: -4px;
  left: 0;
  right: 0;
  height: 2px;
  background: #667eea;
}

.filter-tab .count {
  background: #f0f0f0;
  color: #909399;
  padding: 1px 8px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: normal;
}

.filter-tab .count.warning {
  background: #fdf6ec;
  color: #e6a23c;
}

.filter-tab .count.success {
  background: #f0f9eb;
  color: #67c23a;
}

.filter-tab .count.danger {
  background: #fef0f0;
  color: #f56c6c;
}

.verify-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.verify-item {
  padding: 24px 28px;
}

.item-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 20px;
}

.user-info {
  display: flex;
  gap: 16px;
  align-items: center;
}

.user-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  border: 3px solid #667eea;
}

.user-detail {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.username-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.nickname {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.user-meta {
  font-size: 13px;
  color: #909399;
}

.user-meta .divider {
  margin: 0 8px;
}

.item-body {
  background: #fafafa;
  border-radius: 8px;
  padding: 16px 20px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px 24px;
  margin-bottom: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-item .label {
  font-size: 12px;
  color: #909399;
}

.info-item .value {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.info-block {
  margin-top: 16px;
}

.info-block:first-of-type {
  margin-top: 0;
}

.block-title {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}

.block-content {
  font-size: 14px;
  color: #333;
  line-height: 1.7;
  background: #fff;
  padding: 12px 16px;
  border-radius: 6px;
  border: 1px solid #ebeef5;
  white-space: pre-wrap;
}

.link-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.link-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  font-size: 13px;
  color: #667eea;
  text-decoration: none;
  transition: all 0.3s;
  max-width: fit-content;
}

.link-item:hover {
  background: #f0f3ff;
  border-color: #667eea;
}

.empty-text {
  color: #c0c4cc;
  font-size: 14px;
}

.review-block {
  margin-top: 16px;
}

.review-info {
  font-size: 13px;
  color: #606266;
  margin-bottom: 6px;
}

.review-info .label {
  color: #909399;
}

.item-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.loading, .empty {
  padding: 40px;
}

.load-more {
  text-align: center;
  padding: 30px 0;
}

@media (max-width: 768px) {
  .info-grid {
    grid-template-columns: 1fr;
  }

  .item-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
}
</style>

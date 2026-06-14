<template>
  <div class="activity-detail">
    <div
      class="activity-banner"
      :style="{ background: getBannerBg() }"
    >
      <div class="banner-overlay"></div>
      <div class="container banner-content">
        <div v-if="isOngoing" class="status-tag ongoing">进行中</div>
        <div v-else-if="isUpcoming" class="status-tag upcoming">即将开始</div>
        <div v-else class="status-tag ended">已结束</div>
        <h1 class="activity-title">{{ activity?.title }}</h1>
        <p v-if="activity?.description" class="activity-desc">{{ activity.description }}</p>
        <div class="activity-info-row">
          <span class="info-item">
            <el-icon><Calendar /></el-icon>
            {{ formatDateTime(activity?.startTime) }} - {{ formatDateTime(activity?.endTime) }}
          </span>
          <span v-if="isVoting" class="info-item voting">
            <el-icon><Star /></el-icon>
            投票进行中
          </span>
        </div>
        <div class="activity-stats">
          <span class="stat"><strong>{{ activity?.workCount || 0 }}</strong>参赛作品</span>
          <span class="stat"><strong>{{ activity?.voteCount || 0 }}</strong>累计投票</span>
          <span class="stat"><strong>{{ activity?.viewCount || 0 }}</strong>浏览量</span>
        </div>
      </div>
    </div>

    <div class="container main-content">
      <div class="content-left">
        <div class="section card">
          <div class="section-header">
            <h2>参赛作品</h2>
            <div class="sort-tabs">
              <div
                :class="['sort-tab', { 'is-active': sortBy === 'time' }]"
                @click="sortBy = 'time'; loadWorks(true)"
              >
                最新投稿
              </div>
              <div
                :class="['sort-tab', { 'is-active': sortBy === 'vote' }]"
                @click="sortBy = 'vote'; loadWorks(true)"
              >
                人气排行
              </div>
            </div>
          </div>

          <div v-if="worksLoading && works.length === 0" class="loading">
            <el-skeleton :rows="5" animated />
          </div>

          <div v-else-if="works.length > 0" class="works-grid">
            <div
              v-for="(aw, index) in works"
              :key="aw.id"
              class="work-item card"
            >
              <div class="work-cover" @click="goToWork(aw.workId)">
                <img
                  v-if="aw.work?.coverImage"
                  :src="aw.work.coverImage"
                  :alt="aw.work.title"
                />
                <div v-else class="cover-placeholder">
                  <el-icon :size="48"><Picture /></el-icon>
                </div>
                <div v-if="sortBy === 'vote' && index < 3" :class="['rank-badge', 'rank-' + (index + 1)]">
                  {{ index + 1 }}
                </div>
              </div>
              <div class="work-body">
                <h3 class="work-title" @click="goToWork(aw.workId)">{{ aw.work?.title }}</h3>
                <div class="author-row" @click="goToProfile(aw.userId)">
                  <img :src="aw.user?.avatar || 'https://via.placeholder.com/24'" alt="avatar" />
                  <span>{{ aw.user?.nickname || aw.user?.username }}</span>
                </div>
                <div class="work-footer">
                  <div class="vote-count">
                    <el-icon class="vote-icon"><StarFilled /></el-icon>
                    <span>{{ aw.voteCount || 0 }} 票</span>
                  </div>
                  <el-button
                    v-if="isVoting && userStore.isLoggedIn"
                    type="primary"
                    size="small"
                    :loading="votingId === aw.id"
                    :disabled="!!aw._hasVoted"
                    @click="handleVote(aw)"
                  >
                    {{ aw._hasVoted ? '已投票' : '投票' }}
                  </el-button>
                  <el-tooltip v-else-if="isVoting && !userStore.isLoggedIn" content="请先登录">
                    <el-button type="primary" size="small" disabled>投票</el-button>
                  </el-tooltip>
                </div>
              </div>
            </div>
          </div>

          <div v-else class="empty">
            <el-empty description="暂无参赛作品" />
          </div>

          <div v-if="hasMoreWorks && !worksLoading" class="load-more">
            <el-button :loading="worksLoadingMore" :disabled="worksLoadingMore" @click="loadMoreWorks">
              加载更多
            </el-button>
          </div>
        </div>
      </div>

      <div class="content-right">
        <div class="sidebar-card card">
          <div v-if="detail?.mySubmitCount !== undefined && activity" class="my-status">
            <div class="status-row">
              <span>我的投稿</span>
              <strong>{{ detail.mySubmitCount }} / {{ activity.maxSubmitPerUser || 1 }}</strong>
            </div>
            <div class="status-row">
              <span>我的投票</span>
              <strong>{{ detail.myVoteCount }} / {{ activity.maxVotePerUser || 10 }}</strong>
            </div>
          </div>

          <el-button
            v-if="isOngoing && userStore.isLoggedIn"
            type="primary"
            size="large"
            class="submit-btn"
            @click="showSubmitDialog = true"
          >
            投稿参赛
          </el-button>
          <el-tooltip v-else-if="isOngoing && !userStore.isLoggedIn" content="请先登录">
            <el-button type="primary" size="large" class="submit-btn" disabled>投稿参赛</el-button>
          </el-tooltip>
        </div>

        <div v-if="activity?.rules" class="sidebar-card card">
          <h3 class="sidebar-title">参与规则</h3>
          <div class="rules-content">{{ activity.rules }}</div>
        </div>
      </div>
    </div>

    <el-dialog v-model="showSubmitDialog" title="投稿参赛" width="500px">
      <el-form :model="submitForm" label-width="100px">
        <el-form-item label="选择作品">
          <el-select v-model="submitForm.workId" placeholder="请选择要投稿的作品" style="width: 100%">
            <el-option
              v-for="work in myWorks"
              :key="work.id"
              :label="work.title"
              :value="work.id"
            />
          </el-select>
          <div v-if="myWorks.length === 0" class="empty-tip">
            暂无可用作品，请先
            <router-link to="/publish">发布作品</router-link>
          </div>
        </el-form-item>
        <el-form-item label="投稿说明">
          <el-input
            v-model="submitForm.submitRemark"
            type="textarea"
            :rows="3"
            placeholder="请输入投稿说明（选填）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showSubmitDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确认投稿</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Calendar, Star, Picture, StarFilled } from '@element-plus/icons-vue'
import {
  getActivityDetail,
  getActivityWorks,
  submitWorkToActivity,
  voteActivityWork,
  checkHasVoted
} from '@/utils/request'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activity = ref(null)
const detail = ref(null)
const works = ref([])
const sortBy = ref('time')
const worksLoading = ref(true)
const worksLoadingMore = ref(false)
const hasMoreWorks = ref(true)
const worksPage = ref(1)
const worksPageSize = 12
let worksFetchVersion = 0

const showSubmitDialog = ref(false)
const submitting = ref(false)
const submitForm = ref({ workId: null, submitRemark: '' })
const myWorks = ref([])

const votingId = ref(null)

const bannerBgs = [
  'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
  'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
  'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)'
]

function getBannerBg() {
  if (activity.value?.bannerImage) {
    return `url(${activity.value.bannerImage}) center/cover no-repeat`
  }
  if (activity.value?.coverImage) {
    return `url(${activity.value.coverImage}) center/cover no-repeat`
  }
  return bannerBgs[(route.params.id || 0) % bannerBgs.length]
}

const isOngoing = computed(() => {
  if (!activity.value) return false
  const now = new Date()
  const start = new Date(activity.value.startTime)
  const end = new Date(activity.value.endTime)
  return activity.value.status === 1 && now >= start && now <= end
})

const isUpcoming = computed(() => {
  if (!activity.value) return false
  const now = new Date()
  const start = new Date(activity.value.startTime)
  return now < start
})

const isVoting = computed(() => {
  if (!activity.value) return false
  const now = new Date()
  const voteStart = activity.value.voteStartTime ? new Date(activity.value.voteStartTime) : new Date(activity.value.startTime)
  const voteEnd = activity.value.voteEndTime ? new Date(activity.value.voteEndTime) : new Date(activity.value.endTime)
  return now >= voteStart && now <= voteEnd
})

function formatDateTime(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(2, '0')}.${String(d.getDate()).padStart(2, '0')}`
}

function goToWork(workId) {
  router.push(`/work/${workId}`)
}

function goToProfile(userId) {
  router.push(`/profile/${userId}`)
}

async function loadActivity() {
  try {
    const res = await getActivityDetail(route.params.id)
    if (res.code === 200 && res.data) {
      activity.value = res.data.activity
      detail.value = res.data
    } else {
      ElMessage.error(res.message || '加载活动详情失败')
    }
  } catch (e) {
    console.error('加载活动详情失败', e)
  }
}

async function loadWorks(reset = false) {
  if (reset) {
    worksPage.value = 1
    works.value = []
    hasMoreWorks.value = true
  }
  if (worksLoading.value || worksLoadingMore.value) return
  if (!hasMoreWorks.value) return

  const version = ++worksFetchVersion
  if (reset) {
    worksLoading.value = true
  } else {
    worksLoadingMore.value = true
  }
  try {
    const res = await getActivityWorks({
      activityId: route.params.id,
      page: worksPage.value,
      size: worksPageSize,
      sortBy: sortBy.value
    })
    if (version !== worksFetchVersion) return
    if (res.code === 200 && res.data) {
      const records = res.data.records || []
      for (const item of records) {
        if (userStore.isLoggedIn) {
          try {
            const voteRes = await checkHasVoted(route.params.id, item.id)
            if (voteRes.code === 200 && voteRes.data) {
              item._hasVoted = voteRes.data.hasVoted
            }
          } catch (e) {
            // ignore
          }
        }
      }
      works.value = works.value.concat(records)
      hasMoreWorks.value = records.length >= worksPageSize
      worksPage.value++
    }
  } catch (e) {
    console.error('加载作品列表失败', e)
  } finally {
    if (version === worksFetchVersion) {
      worksLoading.value = false
      worksLoadingMore.value = false
    }
  }
}

function loadMoreWorks() {
  loadWorks(false)
}

async function loadMyWorks() {
  if (!userStore.isLoggedIn || !userStore.userInfo?.id) return
  try {
    const res = await request.get(`/work/user/${userStore.userInfo.id}`, {
      params: { page: 1, size: 50 }
    })
    if (res.code === 200 && res.data) {
      myWorks.value = res.data.records || []
    }
  } catch (e) {
    console.error('加载我的作品失败', e)
  }
}

async function handleSubmit() {
  if (!submitForm.value.workId) {
    ElMessage.warning('请选择要投稿的作品')
    return
  }
  submitting.value = true
  try {
    const res = await submitWorkToActivity({
      activityId: route.params.id,
      workId: submitForm.value.workId,
      submitRemark: submitForm.value.submitRemark
    })
    if (res.code === 200) {
      ElMessage.success('投稿成功')
      showSubmitDialog.value = false
      submitForm.value = { workId: null, submitRemark: '' }
      loadActivity()
      loadWorks(true)
    } else {
      ElMessage.error(res.message || '投稿失败')
    }
  } catch (e) {
    console.error('投稿失败', e)
  } finally {
    submitting.value = false
  }
}

async function handleVote(aw) {
  votingId.value = aw.id
  try {
    const res = await voteActivityWork({
      activityId: route.params.id,
      activityWorkId: aw.id,
      workId: aw.workId
    })
    if (res.code === 200) {
      ElMessage.success('投票成功')
      aw.voteCount = (aw.voteCount || 0) + 1
      aw._hasVoted = true
      loadActivity()
    } else {
      ElMessage.error(res.message || '投票失败')
    }
  } catch (e) {
    console.error('投票失败', e)
  } finally {
    votingId.value = null
  }
}

onMounted(() => {
  loadActivity()
  loadWorks(true)
  loadMyWorks()
})
</script>

<style scoped>
.activity-detail {
  min-height: 100vh;
}

.activity-banner {
  position: relative;
  padding: 60px 0;
  color: #fff;
}

.banner-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(transparent 0%, rgba(0, 0, 0, 0.4) 100%);
}

.banner-content {
  position: relative;
  z-index: 1;
}

.status-tag {
  display: inline-block;
  padding: 4px 16px;
  border-radius: 16px;
  font-size: 13px;
  margin-bottom: 16px;
}

.status-tag.ongoing {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
}

.status-tag.upcoming {
  background: linear-gradient(135deg, #f2994a 0%, #f2c94c 100%);
}

.status-tag.ended {
  background: rgba(255, 255, 255, 0.3);
}

.activity-title {
  font-size: 36px;
  margin: 0 0 12px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.activity-desc {
  font-size: 16px;
  line-height: 1.6;
  max-width: 800px;
  margin: 0 0 20px;
  opacity: 0.9;
}

.activity-info-row {
  display: flex;
  gap: 24px;
  align-items: center;
  margin-bottom: 20px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  opacity: 0.9;
}

.info-item.voting {
  background: rgba(255, 255, 255, 0.2);
  padding: 4px 12px;
  border-radius: 12px;
}

.activity-stats {
  display: flex;
  gap: 32px;
}

.stat {
  font-size: 14px;
  opacity: 0.9;
}

.stat strong {
  font-size: 24px;
  margin-right: 4px;
  font-weight: 600;
}

.main-content {
  display: flex;
  gap: 24px;
  padding: 24px 0;
  align-items: flex-start;
}

.content-left {
  flex: 1;
  min-width: 0;
}

.content-right {
  width: 300px;
  flex-shrink: 0;
  position: sticky;
  top: 80px;
}

.section {
  padding: 24px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.section-header h2 {
  font-size: 20px;
  margin: 0;
}

.sort-tabs {
  display: flex;
  gap: 4px;
}

.sort-tab {
  padding: 6px 16px;
  border-radius: 16px;
  cursor: pointer;
  font-size: 14px;
  color: #666;
  transition: all 0.3s;
}

.sort-tab:hover {
  background: #f5f5f5;
}

.sort-tab.is-active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.works-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
}

.work-item {
  overflow: hidden;
}

.work-cover {
  position: relative;
  aspect-ratio: 1 / 1;
  overflow: hidden;
  cursor: pointer;
  background: #f5f5f5;
}

.work-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.work-item:hover .work-cover img {
  transform: scale(1.05);
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ccc;
}

.rank-badge {
  position: absolute;
  top: 8px;
  left: 8px;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: bold;
  font-size: 14px;
}

.rank-badge.rank-1 {
  background: linear-gradient(135deg, #ffd700 0%, #ffb347 100%);
}

.rank-badge.rank-2 {
  background: linear-gradient(135deg, #c0c0c0 0%, #e8e8e8 100%);
  color: #666;
}

.rank-badge.rank-3 {
  background: linear-gradient(135deg, #cd7f32 0%, #daa06d 100%);
}

.work-body {
  padding: 12px;
}

.work-title {
  font-size: 15px;
  margin: 0 0 8px;
  cursor: pointer;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #333;
}

.work-title:hover {
  color: #667eea;
}

.author-row {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 12px;
  cursor: pointer;
  color: #666;
  font-size: 13px;
}

.author-row img {
  width: 20px;
  height: 20px;
  border-radius: 50%;
}

.work-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.vote-count {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #f56c6c;
  font-weight: 500;
  font-size: 14px;
}

.vote-icon {
  font-size: 16px;
}

.sidebar-card {
  padding: 20px;
  margin-bottom: 16px;
}

.my-status {
  margin-bottom: 16px;
}

.status-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  font-size: 14px;
  color: #666;
}

.status-row strong {
  color: #667eea;
  font-size: 16px;
}

.submit-btn {
  width: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.sidebar-title {
  font-size: 16px;
  margin: 0 0 12px;
  color: #333;
}

.rules-content {
  color: #666;
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
}

.empty {
  padding: 40px 0;
}

.empty-tip {
  margin-top: 8px;
  font-size: 13px;
  color: #999;
}

.empty-tip a {
  color: #667eea;
}

.loading {
  padding: 24px;
}

.load-more {
  text-align: center;
  padding: 24px 0 0;
}

@media (max-width: 900px) {
  .main-content {
    flex-direction: column;
  }
  .content-right {
    width: 100%;
    position: static;
  }
  .activity-title {
    font-size: 28px;
  }
}
</style>

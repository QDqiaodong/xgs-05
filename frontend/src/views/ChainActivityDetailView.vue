<template>
  <div class="chain-activity-detail">
    <div
      class="activity-banner"
      :style="{ background: getBannerBg() }"
    >
      <div class="banner-overlay"></div>
      <div class="container banner-content">
        <div v-if="isOngoing" class="status-tag ongoing">接龙进行中</div>
        <div v-else-if="isUpcoming" class="status-tag upcoming">即将开始</div>
        <div v-else class="status-tag ended">已结束</div>
        <h1 class="activity-title">{{ activity?.title }}</h1>
        <p v-if="activity?.theme" class="theme-text">
          <el-icon><MagicStick /></el-icon>
          主题：{{ activity.theme }}
        </p>
        <p v-if="activity?.description" class="activity-desc">{{ activity.description }}</p>
        <div class="activity-info-row">
          <span class="info-item">
            <el-icon><Calendar /></el-icon>
            {{ formatDateTime(activity?.startTime) }} - {{ formatDateTime(activity?.endTime) }}
          </span>
          <span v-if="activity?.maxChainDepth > 0" class="info-item">
            <el-icon><Link /></el-icon>
            最大深度：{{ activity.maxChainDepth }} 层
          </span>
        </div>
        <div class="activity-stats">
          <span class="stat">
            <strong>{{ activity?.workCount || 0 }}</strong>接龙作品
          </span>
          <span class="stat">
            <strong>{{ genealogy?.totalParticipants || 0 }}</strong>参与人数
          </span>
          <span class="stat">
            <strong>{{ genealogy?.maxDepth || 0 }}</strong>最大深度
          </span>
          <span class="stat">
            <strong>{{ activity?.viewCount || 0 }}</strong>浏览量
          </span>
        </div>
      </div>
    </div>

    <div class="container main-content">
      <div class="content-left">
        <div class="section card">
          <div class="section-header">
            <h2>
              <el-icon><Share /></el-icon>
              接龙谱系图
            </h2>
            <div class="view-toggle">
              <div
                :class="['toggle-item', { 'is-active': viewMode === 'tree' }]"
                @click="viewMode = 'tree'"
              >
                树形视图
              </div>
              <div
                :class="['toggle-item', { 'is-active': viewMode === 'list' }]"
                @click="viewMode = 'list'; loadWorks(true)"
              >
                作品列表
              </div>
            </div>
          </div>

          <div v-if="viewMode === 'tree'" class="genealogy-container">
            <div v-if="genealogyLoading" class="loading">
              <el-skeleton :rows="5" animated />
            </div>
            <div v-else-if="genealogy?.roots?.length > 0" class="genealogy-tree">
              <div
                v-for="root in genealogy.roots"
                :key="root.chainWorkId"
                class="tree-root"
              >
                <genealogy-node
                  :node="root"
                  :is-root="true"
                  @node-click="handleNodeClick"
                />
              </div>
            </div>
            <div v-else class="empty">
              <el-empty description="暂无接龙作品，快来成为第一个创作者吧！" />
            </div>
          </div>

          <div v-else class="works-section">
            <div class="sort-tabs">
              <div
                :class="['sort-tab', { 'is-active': sortBy === 'time' }]"
                @click="sortBy = 'time'; loadWorks(true)"
              >
                最新投稿
              </div>
              <div
                :class="['sort-tab', { 'is-active': sortBy === 'level' }]"
                @click="sortBy = 'level'; loadWorks(true)"
              >
                按层级
              </div>
              <div
                :class="['sort-tab', { 'is-active': sortBy === 'like' }]"
                @click="sortBy = 'like'; loadWorks(true)"
              >
                人气排行
              </div>
            </div>

            <div v-if="worksLoading && works.length === 0" class="loading">
              <el-skeleton :rows="5" animated />
            </div>

            <div v-else-if="works.length > 0" class="works-grid">
              <div
                v-for="cw in works"
                :key="cw.chainWork?.id"
                class="work-item card"
              >
                <div class="work-cover" @click="goToWork(cw.work?.id)">
                  <img
                    v-if="cw.work?.coverImage"
                    :src="cw.work.coverImage"
                    :alt="cw.work.title"
                  />
                  <div v-else class="cover-placeholder">
                    <el-icon :size="48"><Picture /></el-icon>
                  </div>
                  <div class="level-badge">第 {{ cw.chainWork?.chainLevel }} 层</div>
                </div>
                <div class="work-body">
                  <h3 class="work-title" @click="goToWork(cw.work?.id)">
                    {{ cw.work?.title }}
                  </h3>
                  <div class="author-row" @click="goToProfile(cw.user?.id)">
                    <img :src="cw.user?.avatar || 'https://via.placeholder.com/24'" alt="avatar" />
                    <span>{{ cw.user?.nickname || cw.user?.username }}</span>
                  </div>
                  <div v-if="cw.parentWork" class="inspiration-row" @click="goToWork(cw.parentWork?.id)">
                    <el-icon><ArrowUp /></el-icon>
                    <span class="inspiration-label">灵感来自：</span>
                    <span class="inspiration-work">{{ cw.parentWork?.title }}</span>
                  </div>
                  <div v-if="cw.chainWork?.inspirationRemark" class="inspiration-remark">
                    "{{ cw.chainWork.inspirationRemark }}"
                  </div>
                  <div class="work-footer">
                    <span class="branch-stat">
                      <el-icon><Share /></el-icon>
                      {{ cw.chainWork?.branchCount || 0 }} 分支
                    </span>
                    <span class="like-stat">
                      <el-icon><Star /></el-icon>
                      {{ cw.chainWork?.likeCount || 0 }} 赞
                    </span>
                  </div>
                </div>
              </div>
            </div>

            <div v-else class="empty">
              <el-empty description="暂无接龙作品" />
            </div>

            <div v-if="hasMoreWorks && !worksLoading" class="load-more">
              <el-button :loading="worksLoadingMore" :disabled="worksLoadingMore" @click="loadMoreWorks">
                加载更多
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <div class="content-right">
        <div class="sidebar-card card">
          <div v-if="detail?.mySubmitCount !== undefined" class="my-status">
            <div class="status-row">
              <span>我的投稿</span>
              <strong>{{ detail.mySubmitCount }} 件作品</strong>
            </div>
          </div>

          <el-button
            v-if="isOngoing && userStore.isLoggedIn"
            type="primary"
            size="large"
            class="submit-btn"
            @click="goToPublish"
          >
            <el-icon><Edit /></el-icon>
            参与接龙创作
          </el-button>
          <el-tooltip v-else-if="isOngoing && !userStore.isLoggedIn" content="请先登录">
            <el-button type="primary" size="large" class="submit-btn" disabled>参与接龙创作</el-button>
          </el-tooltip>
        </div>

        <div v-if="activity?.rules" class="sidebar-card card">
          <h3 class="sidebar-title">
            <el-icon><Document /></el-icon>
            接龙规则
          </h3>
          <div class="rules-content">{{ activity.rules }}</div>
        </div>

        <div v-if="genealogy?.totalParticipants > 0" class="sidebar-card card">
          <h3 class="sidebar-title">
            <el-icon><TrendCharts /></el-icon>
            接龙数据
          </h3>
          <div class="data-list">
            <div class="data-item">
              <span>总作品数</span>
              <strong>{{ genealogy.totalWorks }}</strong>
            </div>
            <div class="data-item">
              <span>参与人数</span>
              <strong>{{ genealogy.totalParticipants }}</strong>
            </div>
            <div class="data-item">
              <span>最大深度</span>
              <strong>{{ genealogy.maxDepth }} 层</strong>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { h } from 'vue'

const GenealogyNode = {
  name: 'GenealogyNode',
  props: {
    node: { type: Object, required: true },
    isRoot: { type: Boolean, default: false }
  },
  emits: ['node-click'],
  setup(props, { emit }) {
    const hasChildren = () => props.node.children && props.node.children.length > 0

    const handleClick = () => {
      emit('node-click', props.node)
    }

    return () => h('div', { class: ['tree-node', { 'is-root': props.isRoot }] }, [
      h('div', {
        class: 'node-card',
        onClick: handleClick,
        style: {
          borderColor: getLevelColor(props.node.chainLevel)
        }
      }, [
        h('div', { class: 'node-level', style: { background: getLevelColor(props.node.chainLevel) } }, [
          'L' + props.node.chainLevel
        ]),
        h('div', { class: 'node-avatar' }, [
          props.node.workCoverImage
            ? h('img', { src: props.node.workCoverImage, alt: props.node.workTitle })
            : h('span', { class: 'avatar-placeholder' }, '🎨')
        ]),
        h('div', { class: 'node-info' }, [
          h('div', { class: 'node-title' }, props.node.workTitle || '无题'),
          h('div', { class: 'node-author' }, [
            props.node.userAvatar
              ? h('img', { src: props.node.userAvatar, class: 'author-avatar' })
              : null,
            h('span', {}, props.node.username || '匿名')
          ]),
          props.node.branchCount > 0
            ? h('div', { class: 'node-branches' }, [
                h('span', {}, `🌿 ${props.node.branchCount} 分支`)
              ])
            : null
        ])
      ]),
      hasChildren()
        ? h('div', { class: 'node-children' },
            props.node.children.map(child =>
              h(GenealogyNode, {
                key: child.chainWorkId,
                node: child,
                isRoot: false,
                onNodeClick: (n) => emit('node-click', n)
              })
            )
          )
        : null
    ])
  }
}

function getLevelColor(level) {
  const colors = [
    '#667eea',
    '#f093fb',
    '#4facfe',
    '#43e97b',
    '#fa709a',
    '#fee140',
    '#a8edea',
    '#ff9a9e'
  ]
  return colors[(level - 1) % colors.length]
}

export default {
  components: { GenealogyNode },
  setup() {
    return { getLevelColor }
  }
}
</script>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Calendar, Picture, Star, View, Share, Edit, Document,
  ArrowUp, MagicStick, Link, TrendCharts, User
} from '@element-plus/icons-vue'
import {
  getChainActivityDetail,
  getChainWorks,
  getChainGenealogy
} from '@/utils/request'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activity = ref(null)
const detail = ref(null)
const genealogy = ref(null)
const genealogyLoading = ref(true)
const viewMode = ref('tree')

const works = ref([])
const sortBy = ref('time')
const worksLoading = ref(true)
const worksLoadingMore = ref(false)
const hasMoreWorks = ref(true)
const worksPage = ref(1)
const worksPageSize = 12
let worksFetchVersion = 0

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

function formatDateTime(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(2, '0')}.${String(d.getDate()).padStart(2, '0')}`
}

function goToWork(workId) {
  if (workId) router.push(`/work/${workId}`)
}

function goToProfile(userId) {
  if (userId) router.push(`/profile/${userId}`)
}

function handleNodeClick(node) {
  goToWork(node.workId)
}

function goToPublish() {
  router.push(`/chain-publish/${route.params.id}`)
}

async function loadActivity() {
  try {
    const res = await getChainActivityDetail(route.params.id)
    if (res.code === 200 && res.data) {
      activity.value = res.data.chainActivity
      detail.value = res.data
    } else {
      ElMessage.error(res.message || '加载接龙活动详情失败')
    }
  } catch (e) {
    console.error('加载接龙活动详情失败', e)
  }
}

async function loadGenealogy() {
  genealogyLoading.value = true
  try {
    const res = await getChainGenealogy(route.params.id)
    if (res.code === 200 && res.data) {
      genealogy.value = res.data
    }
  } catch (e) {
    console.error('加载接龙谱系图失败', e)
  } finally {
    genealogyLoading.value = false
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
    const res = await getChainWorks({
      chainActivityId: route.params.id,
      page: worksPage.value,
      size: worksPageSize,
      sortBy: sortBy.value
    })
    if (version !== worksFetchVersion) return
    if (res.code === 200 && res.data) {
      const records = res.data.records || []
      works.value = works.value.concat(records)
      hasMoreWorks.value = records.length >= worksPageSize
      worksPage.value++
    }
  } catch (e) {
    console.error('加载接龙作品列表失败', e)
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

onMounted(() => {
  loadActivity()
  loadGenealogy()
  loadWorks(true)
})
</script>

<style scoped>
.chain-activity-detail {
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
  margin: 0 0 8px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.theme-text {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 15px;
  margin: 0 0 12px;
  padding: 6px 14px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  display: inline-flex;
  backdrop-filter: blur(8px);
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
  flex-wrap: wrap;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  opacity: 0.9;
}

.activity-stats {
  display: flex;
  gap: 32px;
  flex-wrap: wrap;
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
  display: flex;
  align-items: center;
  gap: 6px;
}

.view-toggle {
  display: flex;
  background: #f5f5f5;
  border-radius: 20px;
  overflow: hidden;
}

.toggle-item {
  padding: 6px 18px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
  color: #666;
}

.toggle-item:hover {
  background: #e8e8e8;
}

.toggle-item.is-active {
  background: linear-gradient(135deg, #667eea 0%, #f093fb 100%);
  color: #fff;
}

.genealogy-container {
  min-height: 300px;
}

.genealogy-tree {
  padding: 20px;
  overflow-x: auto;
}

.tree-root {
  display: inline-block;
}

.tree-node {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.node-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: #fff;
  border: 2px solid #e8e8e8;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
  min-width: 220px;
}

.node-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.node-card.is-root {
  min-width: 260px;
}

.node-level {
  position: absolute;
  top: -10px;
  left: 12px;
  padding: 2px 10px;
  border-radius: 10px;
  color: #fff;
  font-size: 11px;
  font-weight: 600;
}

.node-avatar {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  overflow: hidden;
  background: #f5f5f5;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.node-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  font-size: 24px;
}

.node-info {
  flex: 1;
  min-width: 0;
}

.node-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.node-author {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}

.author-avatar {
  width: 16px;
  height: 16px;
  border-radius: 50%;
}

.node-branches {
  font-size: 11px;
  color: #667eea;
}

.node-children {
  display: flex;
  gap: 24px;
  margin-top: 24px;
  position: relative;
  padding-top: 24px;
}

.node-children::before {
  content: '';
  position: absolute;
  top: 0;
  left: 50%;
  right: 50%;
  height: 24px;
  border-top: 2px solid #e0e0e0;
}

.node-children > .tree-node {
  position: relative;
}

.node-children > .tree-node::before {
  content: '';
  position: absolute;
  top: -24px;
  left: 50%;
  width: 2px;
  height: 24px;
  background: #e0e0e0;
}

.works-section .sort-tabs {
  display: flex;
  gap: 4px;
  margin-bottom: 20px;
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
  background: linear-gradient(135deg, #667eea 0%, #f093fb 100%);
  color: #fff;
}

.works-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
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

.level-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 4px 12px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #f093fb 100%);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
}

.work-body {
  padding: 14px;
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
  margin-bottom: 10px;
  cursor: pointer;
  color: #666;
  font-size: 13px;
}

.author-row img {
  width: 20px;
  height: 20px;
  border-radius: 50%;
}

.inspiration-row {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 10px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border-radius: 6px;
  font-size: 12px;
  color: #0369a1;
  margin-bottom: 8px;
  cursor: pointer;
  overflow: hidden;
}

.inspiration-label {
  flex-shrink: 0;
}

.inspiration-work {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 500;
}

.inspiration-remark {
  font-size: 12px;
  color: #999;
  font-style: italic;
  margin-bottom: 10px;
  line-height: 1.5;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.work-footer {
  display: flex;
  gap: 16px;
  padding-top: 10px;
  border-top: 1px solid #f0f0f0;
}

.branch-stat,
.like-stat {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #999;
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
  background: linear-gradient(135deg, #667eea 0%, #f093fb 100%);
  border: none;
}

.sidebar-title {
  font-size: 16px;
  margin: 0 0 12px;
  color: #333;
  display: flex;
  align-items: center;
  gap: 6px;
}

.rules-content {
  color: #666;
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
}

.data-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.data-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  background: #fafafa;
  border-radius: 8px;
  font-size: 14px;
  color: #666;
}

.data-item strong {
  color: #667eea;
  font-size: 16px;
}

.empty {
  padding: 40px 0;
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

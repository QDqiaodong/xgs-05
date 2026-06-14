<template>
  <div class="chain-publish container">
    <div class="publish-header card">
      <router-link :to="`/chain-activity/${route.params.chainActivityId}`" class="back-link">
        <el-icon><ArrowLeft /></el-icon>
        返回活动详情
      </router-link>
      <h1 class="page-title">参与接龙创作</h1>
      <p class="page-subtitle">{{ activity?.title }}</p>
      <p v-if="activity?.theme" class="theme-tag">
        <el-icon><MagicStick /></el-icon>
        主题：{{ activity.theme }}
      </p>
    </div>

    <div class="publish-content">
      <div class="main-column">
        <div class="step-card card">
          <div class="step-header">
            <div class="step-number">1</div>
            <h2 class="step-title">选择你的作品</h2>
          </div>
          <div class="step-body">
            <div v-if="myWorksLoading" class="loading-inline">
              <el-skeleton :rows="3" animated />
            </div>
            <div v-else-if="myWorks.length > 0" class="my-works-grid">
              <div
                v-for="work in myWorks"
                :key="work.id"
                :class="['work-select-card', { 'is-selected': selectedWorkId === work.id }]"
                @click="selectWork(work.id)"
              >
                <div class="work-thumb">
                  <img v-if="work.coverImage" :src="work.coverImage" :alt="work.title" />
                  <div v-else class="thumb-placeholder">
                    <el-icon :size="32"><Picture /></el-icon>
                  </div>
                  <div v-if="selectedWorkId === work.id" class="selected-check">
                    <el-icon><Check /></el-icon>
                  </div>
                </div>
                <div class="work-meta">
                  <h3 class="work-name">{{ work.title }}</h3>
                  <span class="work-category">{{ getCategoryName(work.categoryId) }}</span>
                </div>
              </div>
            </div>
            <div v-else class="empty-works">
              <el-empty description="你还没有作品">
                <el-button type="primary" @click="goCreateWork">
                  <el-icon><Plus /></el-icon>
                  去发布作品
                </el-button>
              </el-empty>
            </div>
          </div>
        </div>

        <div class="step-card card">
          <div class="step-header">
            <div class="step-number">2</div>
            <h2 class="step-title">选择灵感来源作品（可选）</h2>
            <span class="step-tip">选择接龙链中某个作品作为你二次创作的灵感来源</span>
          </div>
          <div class="step-body">
            <div v-if="parentChainWorksLoading" class="loading-inline">
              <el-skeleton :rows="3" animated />
            </div>
            <div v-else>
              <div class="inspiration-search">
                <el-input
                  v-model="searchKeyword"
                  placeholder="搜索接龙作品作为灵感来源"
                  clearable
                  :prefix-icon="Search"
                  @input="filterInspirationWorks"
                />
              </div>
              <div v-if="selectedParent" class="selected-inspiration">
                <div class="selected-header">
                  <el-icon class="inspiration-icon"><MagicStick /></el-icon>
                  <span>已选择灵感来源</span>
                  <el-button link type="primary" size="small" @click="clearParentSelection">
                    不选灵感来源（作为根作品）
                  </el-button>
                </div>
                <div class="selected-card">
                  <div class="selected-thumb">
                    <img v-if="selectedParent.work?.coverImage" :src="selectedParent.work?.coverImage" :alt="selectedParent.work?.title" />
                    <div v-else class="thumb-placeholder small">
                      <el-icon :size="24"><Picture /></el-icon>
                    </div>
                  </div>
                  <div class="selected-info">
                    <h4 class="selected-title">{{ selectedParent.work?.title }}</h4>
                    <div class="selected-author">
                      <img :src="selectedParent.user?.avatar || 'https://via.placeholder.com/20'" alt="" />
                      <span>{{ selectedParent.user?.nickname || selectedParent.user?.username }}</span>
                    </div>
                    <div class="selected-level">第 {{ selectedParent.chainWork?.chainLevel }} 层接龙</div>
                  </div>
                </div>
              </div>
              <div v-if="filteredParentWorks.length > 0" class="inspiration-works">
                <div
                  v-for="cw in filteredParentWorks"
                  :key="cw.chainWork?.id"
                  :class="['inspiration-item', { 'is-selected': selectedParentChainWorkId === cw.chainWork?.id, 'is-disabled': selectedParentChainWorkId && selectedParentChainWorkId !== cw.chainWork?.id }]"
                  @click="selectParent(cw)"
                >
                  <div class="inspiration-thumb">
                    <img v-if="cw.work?.coverImage" :src="cw.work?.coverImage" :alt="cw.work?.title" />
                    <div v-else class="thumb-placeholder tiny">
                      <el-icon :size="20"><Picture /></el-icon>
                    </div>
                    <div class="level-tag">L{{ cw.chainWork?.chainLevel }}</div>
                  </div>
                  <div class="inspiration-info">
                    <h4 class="inspiration-title">{{ cw.work?.title }}</h4>
                    <div class="inspiration-author">
                      <img :src="cw.user?.avatar || 'https://via.placeholder.com/16'" alt="" />
                      <span>{{ cw.user?.nickname || cw.user?.username }}</span>
                    </div>
                    <div v-if="cw.chainWork?.branchCount > 0" class="inspiration-branches">
                      {{ cw.chainWork.branchCount }} 分支
                    </div>
                  </div>
                </div>
              </div>
              <div v-else-if="!selectedParent" class="empty-inspiration">
                <el-empty :description="searchKeyword ? '没有找到匹配的作品' : '暂无接龙作品，你可以作为首位创作者'" />
              </div>
              <div v-if="hasMoreParentWorks && !parentChainWorksLoadingMore" class="load-more-inline">
                <el-button
                  type="primary"
                  plain
                  size="small"
                  :loading="parentChainWorksLoadingMore"
                  @click="loadMoreParentWorks"
                >
                  加载更多灵感作品
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <div class="step-card card">
          <div class="step-header">
            <div class="step-number">3</div>
            <h2 class="step-title">填写灵感说明</h2>
          </div>
          <div class="step-body">
            <el-input
              v-model="inspirationRemark"
              type="textarea"
              :rows="5"
              maxlength="500"
              show-word-limit
              :placeholder="selectedParent ? '说明你从灵感来源作品中获得了什么启发，进行了哪些改编和创新...' : '介绍你的作品创作理念，为后续接龙者提供灵感方向...'"
            />
          </div>
        </div>
      </div>

      <div class="side-column">
        <div class="preview-card card">
          <h3 class="preview-title">接龙提交预览</h3>
          <div class="preview-content">
            <div class="preview-row">
              <span class="preview-label">接龙活动</span>
              <span class="preview-value">{{ activity?.title || '加载中...' }}</span>
            </div>
            <div class="preview-row">
              <span class="preview-label">我的作品</span>
              <span class="preview-value" :class="{ 'text-muted': !selectedWorkId }">
                {{ selectedWork ? selectedWork.title : '请选择作品' }}
              </span>
            </div>
            <div class="preview-row">
              <span class="preview-label">灵感来源</span>
              <span class="preview-value" :class="{ 'text-muted': !selectedParentChainWorkId }">
                {{ selectedParent ? selectedParent.work?.title : '无（作为根作品）' }}
              </span>
            </div>
            <div v-if="selectedParent" class="preview-row">
              <span class="preview-label">预计层级</span>
              <span class="preview-value level-hint">
                第 {{ (selectedParent.chainWork?.chainLevel || 0) + 1 }} 层
              </span>
            </div>
            <div v-else class="preview-row">
              <span class="preview-label">预计层级</span>
              <span class="preview-value level-hint">第 1 层（根）</span>
            </div>
          </div>
          <el-button
            type="primary"
            size="large"
            class="submit-btn"
            :disabled="!canSubmit"
            :loading="submitting"
            @click="handleSubmit"
          >
            <el-icon><Link /></el-icon>
            提交接龙作品
          </el-button>
          <p class="submit-tip">
            <el-icon><InfoFilled /></el-icon>
            提交后将自动进入接龙谱系
          </p>
        </div>

        <div v-if="activity?.rules" class="rules-card card">
          <h3 class="rules-title">
            <el-icon><Document /></el-icon>
            接龙规则
          </h3>
          <div class="rules-text">{{ activity.rules }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft, Plus, Check, Search, MagicStick,
  Picture, Link, Document, InfoFilled
} from '@element-plus/icons-vue'
import {
  getChainActivityDetail,
  getChainWorks,
  submitChainWork
} from '@/utils/request'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activity = ref(null)
const submitting = ref(false)

const myWorks = ref([])
const myWorksLoading = ref(true)
const selectedWorkId = ref(null)
const selectedWork = computed(() => myWorks.value.find(w => w.id === selectedWorkId.value))

const parentChainWorks = ref([])
const filteredParentWorks = ref([])
const parentChainWorksLoading = ref(true)
const parentChainWorksLoadingMore = ref(false)
const hasMoreParentWorks = ref(true)
const parentWorksPage = ref(1)
const parentWorksPageSize = 20
const searchKeyword = ref('')

const selectedParentChainWorkId = ref(null)
const selectedParent = computed(() =>
  parentChainWorks.value.find(cw => cw.chainWork?.id === selectedParentChainWorkId.value)
)

const inspirationRemark = ref('')

const categories = [
  { id: 1, name: '编织' },
  { id: 2, name: '陶艺' },
  { id: 3, name: '布艺' },
  { id: 4, name: '木艺' }
]

const canSubmit = computed(() => {
  return selectedWorkId.value && !submitting.value
})

function getCategoryName(id) {
  const cat = categories.find(c => c.id === id)
  return cat ? cat.name : '未分类'
}

function selectWork(id) {
  selectedWorkId.value = id
}

function selectParent(cw) {
  if (selectedParentChainWorkId.value === cw.chainWork?.id) {
    selectedParentChainWorkId.value = null
  } else {
    selectedParentChainWorkId.value = cw.chainWork?.id
  }
}

function clearParentSelection() {
  selectedParentChainWorkId.value = null
}

function goCreateWork() {
  router.push('/publish')
}

function filterInspirationWorks() {
  if (!searchKeyword.value.trim()) {
    filteredParentWorks.value = parentChainWorks.value
    return
  }
  const kw = searchKeyword.value.trim().toLowerCase()
  filteredParentWorks.value = parentChainWorks.value.filter(cw => {
    const title = (cw.work?.title || '').toLowerCase()
    const username = (cw.user?.nickname || cw.user?.username || '').toLowerCase()
    return title.includes(kw) || username.includes(kw)
  })
}

async function loadActivity() {
  try {
    const res = await getChainActivityDetail(route.params.chainActivityId)
    if (res.code === 200 && res.data) {
      activity.value = res.data.chainActivity
    } else {
      ElMessage.error(res.message || '加载活动信息失败')
    }
  } catch (e) {
    console.error('加载活动信息失败', e)
  }
}

async function loadMyWorks() {
  if (!userStore.isLoggedIn || !userStore.userInfo?.id) {
    myWorksLoading.value = false
    return
  }
  myWorksLoading.value = true
  try {
    const res = await request.get(`/work/user/${userStore.userInfo.id}`, {
      params: { page: 1, size: 50 }
    })
    if (res.code === 200 && res.data) {
      myWorks.value = res.data.records || []
    }
  } catch (e) {
    console.error('加载我的作品失败', e)
  } finally {
    myWorksLoading.value = false
  }
}

async function loadParentWorks(reset = false) {
  if (reset) {
    parentWorksPage.value = 1
    parentChainWorks.value = []
    filteredParentWorks.value = []
    hasMoreParentWorks.value = true
  }
  if (parentChainWorksLoading.value || parentChainWorksLoadingMore.value) return
  if (!hasMoreParentWorks.value) return

  if (reset) {
    parentChainWorksLoading.value = true
  } else {
    parentChainWorksLoadingMore.value = true
  }
  try {
    const res = await getChainWorks({
      chainActivityId: route.params.chainActivityId,
      page: parentWorksPage.value,
      size: parentWorksPageSize,
      sortBy: 'time'
    })
    if (res.code === 200 && res.data) {
      const records = res.data.records || []
      parentChainWorks.value = parentChainWorks.value.concat(records)
      filteredParentWorks.value = parentChainWorks.value
      hasMoreParentWorks.value = records.length >= parentWorksPageSize
      parentWorksPage.value++
    }
  } catch (e) {
    console.error('加载接龙作品失败', e)
  } finally {
    parentChainWorksLoading.value = false
    parentChainWorksLoadingMore.value = false
  }
}

function loadMoreParentWorks() {
  loadParentWorks(false)
}

async function handleSubmit() {
  if (!selectedWorkId.value) {
    ElMessage.warning('请选择你的作品')
    return
  }
  submitting.value = true
  try {
    const res = await submitChainWork({
      chainActivityId: route.params.chainActivityId,
      workId: selectedWorkId.value,
      parentChainWorkId: selectedParentChainWorkId.value || null,
      inspirationRemark: inspirationRemark.value
    })
    if (res.code === 200) {
      ElMessage.success('接龙作品提交成功！')
      setTimeout(() => {
        router.push(`/chain-activity/${route.params.chainActivityId}`)
      }, 1000)
    } else {
      ElMessage.error(res.message || '提交失败，请检查活动是否仍在进行中')
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '提交失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后参与接龙')
    router.push(`/chain-activity/${route.params.chainActivityId}`)
    return
  }
  loadActivity()
  loadMyWorks()
  loadParentWorks(true)
})
</script>

<style scoped>
.chain-publish {
  padding: 24px 0;
}

.publish-header {
  padding: 24px 32px;
  margin-bottom: 24px;
  position: relative;
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #667eea;
  font-size: 14px;
  margin-bottom: 12px;
  text-decoration: none;
}

.back-link:hover {
  opacity: 0.8;
}

.page-title {
  font-size: 26px;
  margin: 0 0 6px;
  background: linear-gradient(135deg, #667eea 0%, #f093fb 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-subtitle {
  font-size: 14px;
  color: #666;
  margin: 0 0 8px;
}

.theme-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  background: linear-gradient(135deg, #fff7ed 0%, #ffedd5 100%);
  border-radius: 14px;
  font-size: 13px;
  color: #c2410c;
}

.publish-content {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.main-column {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.side-column {
  width: 320px;
  flex-shrink: 0;
  position: sticky;
  top: 80px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.step-card {
  padding: 24px;
}

.step-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.step-number {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #f093fb 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 15px;
  flex-shrink: 0;
}

.step-title {
  font-size: 18px;
  margin: 0;
  color: #333;
}

.step-tip {
  font-size: 13px;
  color: #999;
}

.step-body {
  padding-left: 44px;
}

.loading-inline {
  padding: 16px 0;
}

.my-works-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 12px;
}

.work-select-card {
  border: 2px solid #e8e8e8;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  background: #fff;
}

.work-select-card:hover {
  border-color: #c7d2fe;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.work-select-card.is-selected {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.15);
}

.work-thumb {
  position: relative;
  aspect-ratio: 1 / 1;
  background: #f5f5f5;
  overflow: hidden;
}

.work-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.thumb-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ccc;
}

.thumb-placeholder.small {
  font-size: 24px;
}

.thumb-placeholder.tiny {
  font-size: 20px;
}

.selected-check {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #f093fb 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

.work-meta {
  padding: 10px 12px;
}

.work-name {
  font-size: 14px;
  margin: 0 0 4px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.work-category {
  font-size: 12px;
  color: #999;
}

.empty-works {
  padding: 40px 0;
}

.inspiration-search {
  margin-bottom: 16px;
}

.selected-inspiration {
  margin-bottom: 16px;
}

.selected-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #667eea;
  font-weight: 500;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.inspiration-icon {
  font-size: 16px;
}

.selected-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border: 1px solid #bae6fd;
  border-radius: 12px;
}

.selected-thumb {
  width: 56px;
  height: 56px;
  border-radius: 10px;
  overflow: hidden;
  flex-shrink: 0;
  background: #fff;
}

.selected-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.selected-info {
  flex: 1;
  min-width: 0;
}

.selected-title {
  font-size: 14px;
  font-weight: 600;
  margin: 0 0 4px;
  color: #0c4a6e;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.selected-author {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #0369a1;
  margin-bottom: 2px;
}

.selected-author img {
  width: 16px;
  height: 16px;
  border-radius: 50%;
}

.selected-level {
  font-size: 12px;
  color: #0891b2;
}

.inspiration-works {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 10px;
  margin-bottom: 12px;
}

.inspiration-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  border: 1px solid #e8e8e8;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.25s;
  background: #fff;
}

.inspiration-item:hover {
  border-color: #c7d2fe;
  background: #fafbff;
  transform: translateY(-1px);
}

.inspiration-item.is-selected {
  border-color: #667eea;
  background: linear-gradient(135deg, #eef2ff 0%, #faf5ff 100%);
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
}

.inspiration-item.is-disabled {
  opacity: 0.5;
}

.inspiration-thumb {
  position: relative;
  width: 48px;
  height: 48px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
  background: #f5f5f5;
}

.inspiration-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.level-tag {
  position: absolute;
  bottom: 2px;
  left: 2px;
  padding: 1px 5px;
  border-radius: 6px;
  background: linear-gradient(135deg, #667eea 0%, #f093fb 100%);
  color: #fff;
  font-size: 10px;
  font-weight: 600;
}

.inspiration-info {
  flex: 1;
  min-width: 0;
}

.inspiration-title {
  font-size: 13px;
  margin: 0 0 3px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.inspiration-author {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: #999;
  margin-bottom: 2px;
}

.inspiration-author img {
  width: 14px;
  height: 14px;
  border-radius: 50%;
}

.inspiration-branches {
  font-size: 11px;
  color: #667eea;
}

.empty-inspiration {
  padding: 30px 0;
}

.load-more-inline {
  text-align: center;
  padding-top: 8px;
}

.preview-card {
  padding: 20px;
}

.preview-title {
  font-size: 16px;
  margin: 0 0 16px;
  color: #333;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.preview-content {
  margin-bottom: 20px;
}

.preview-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 8px 0;
  gap: 10px;
}

.preview-label {
  font-size: 13px;
  color: #999;
  flex-shrink: 0;
}

.preview-value {
  font-size: 13px;
  color: #333;
  text-align: right;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.preview-value.text-muted {
  color: #bbb;
  font-weight: 400;
}

.preview-value.level-hint {
  color: #667eea;
}

.submit-btn {
  width: 100%;
  background: linear-gradient(135deg, #667eea 0%, #f093fb 100%);
  border: none;
}

.submit-tip {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #999;
  margin: 12px 0 0;
  justify-content: center;
}

.rules-card {
  padding: 20px;
}

.rules-title {
  font-size: 15px;
  margin: 0 0 12px;
  color: #333;
  display: flex;
  align-items: center;
  gap: 4px;
}

.rules-text {
  font-size: 13px;
  color: #666;
  line-height: 1.6;
  white-space: pre-wrap;
}

@media (max-width: 900px) {
  .publish-content {
    flex-direction: column;
  }
  .side-column {
    width: 100%;
    position: static;
  }
  .step-body {
    padding-left: 0;
  }
}
</style>

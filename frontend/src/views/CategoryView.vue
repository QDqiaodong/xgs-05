<template>
  <div class="category container">
    <div class="category-header">
      <h1>{{ currentCategory?.name || '手作品类' }}</h1>
      <p class="subtitle">{{ currentCategory?.description || '发现更多精彩手作作品' }}</p>
    </div>

    <div class="category-filters">
      <el-radio-group v-model="activeCategory" size="large">
        <el-radio-button v-for="cat in categories" :key="cat.id" :label="cat.id">
          {{ cat.name }}
        </el-radio-button>
      </el-radio-group>
    </div>

    <div class="difficulty-filters">
      <span class="filter-label">难度筛选：</span>
      <el-radio-group v-model="activeDifficulty" size="default">
        <el-radio-button :label="null">全部</el-radio-button>
        <el-radio-button :label="1">
          <span class="diff-btn diff-1">● 入门</span>
        </el-radio-button>
        <el-radio-button :label="2">
          <span class="diff-btn diff-2">● 进阶</span>
        </el-radio-button>
        <el-radio-button :label="3">
          <span class="diff-btn diff-3">● 大师</span>
        </el-radio-button>
      </el-radio-group>
    </div>

    <div v-if="loading && works.length === 0" class="loading">
      <el-skeleton :rows="5" animated />
    </div>
    <div v-else class="masonry-grid">
      <WorkCard v-for="work in works" :key="work.id" :work="work" />
    </div>

    <div v-if="hasMore && !loading" class="load-more">
      <el-button :loading="loadingMore" :disabled="loadingMore" @click="loadMore">加载更多</el-button>
    </div>
    <div v-if="!hasMore && works.length > 0 && !loading" class="no-more">
      <span>已加载全部作品</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import WorkCard from '@/components/WorkCard.vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const loadingMore = ref(false)
const hasMore = ref(true)
const page = ref(1)
const pageSize = 10
const activeCategory = ref(1)
const activeDifficulty = ref(null)
const loadedIds = ref(new Set())
let fetchVersion = 0

const categories = ref([
  { id: 1, name: '编织', description: '一针一线，编织美好生活' },
  { id: 2, name: '陶艺', description: '泥土的艺术，指尖的温度' },
  { id: 3, name: '布艺', description: '布料的魔法，匠心的传承' },
  { id: 4, name: '木艺', description: '天然木材，手工雕刻' }
])

const currentCategory = computed(() => {
  return categories.value.find(c => c.id === activeCategory.value)
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
    authorName: '手作达人',
    authorAvatar: ''
  }
}

function appendWorks(records) {
  const newItems = []
  for (const item of records) {
    const work = transformWork(item)
    if (!loadedIds.value.has(work.id)) {
      loadedIds.value.add(work.id)
      newItems.push(work)
    }
  }
  works.value = [...works.value, ...newItems]
}

async function loadWorks(reset = false) {
  if (reset) {
    fetchVersion++
    page.value = 1
    works.value = []
    loadedIds.value = new Set()
    hasMore.value = true
  }
  loading.value = true
  const currentVersion = fetchVersion
  try {
    const params = {
      page: page.value,
      size: pageSize
    }
    if (activeCategory.value && activeCategory.value !== 'all') {
      params.categoryId = activeCategory.value
    }
    if (activeDifficulty.value !== null) {
      params.difficultyLevel = activeDifficulty.value
    }
    const res = await request.get('/work/list', { params })
    if (currentVersion !== fetchVersion) return
    if (res.code === 200 && res.data) {
      const records = res.data.records || []
      if (page.value === 1) {
        loadedIds.value = new Set()
        works.value = records.map(transformWork)
        works.value.forEach(w => loadedIds.value.add(w.id))
      } else {
        appendWorks(records)
      }
      hasMore.value = records.length >= pageSize
    } else {
      works.value = []
    }
  } catch (e) {
    if (currentVersion !== fetchVersion) return
    console.error('加载分类作品失败', e)
    ElMessage.error('加载失败，请刷新重试')
  } finally {
    if (currentVersion === fetchVersion) {
      loading.value = false
    }
  }
}

async function loadMore() {
  if (loadingMore.value || !hasMore.value || loading.value) return
  loadingMore.value = true
  try {
    page.value++
    const currentVersion = fetchVersion
    const params = {
      page: page.value,
      size: pageSize
    }
    if (activeCategory.value && activeCategory.value !== 'all') {
      params.categoryId = activeCategory.value
    }
    if (activeDifficulty.value !== null) {
      params.difficultyLevel = activeDifficulty.value
    }
    const res = await request.get('/work/list', { params })
    if (currentVersion !== fetchVersion) return
    if (res.code === 200 && res.data) {
      const records = res.data.records || []
      appendWorks(records)
      hasMore.value = records.length >= pageSize
    }
  } catch (e) {
    if (currentVersion === fetchVersion) {
      page.value--
    }
    console.error('加载更多失败', e)
  } finally {
    loadingMore.value = false
  }
}

let lastRefreshKey = 0

function checkNeedRefresh() {
  const refreshKey = parseInt(sessionStorage.getItem('workListRefreshKey') || '0')
  if (refreshKey > lastRefreshKey) {
    lastRefreshKey = refreshKey
    loadWorks(true)
  }
}

watch(activeCategory, (newVal) => {
  const routeCategoryId = parseInt(route.params.categoryId) || 1
  if (newVal !== routeCategoryId) {
    router.replace({ name: 'Category', params: { categoryId: newVal } })
  }
  loadWorks(true)
})

watch(activeDifficulty, () => {
  loadWorks(true)
})

watch(
  () => route.params.categoryId,
  (newCategoryId) => {
    const parsedId = parseInt(newCategoryId) || 1
    if (parsedId !== activeCategory.value) {
      activeCategory.value = parsedId
    }
  }
)

onMounted(() => {
  const categoryId = parseInt(route.params.categoryId) || 1
  activeCategory.value = categoryId
  checkNeedRefresh()
  if (works.value.length === 0) {
    loadWorks()
  }
  window.addEventListener('focus', checkNeedRefresh)
})

onUnmounted(() => {
  window.removeEventListener('focus', checkNeedRefresh)
})
</script>

<style scoped>
.category {
  padding: 30px 0;
}

.category-header {
  text-align: center;
  margin-bottom: 30px;
}

.category-header h1 {
  font-size: 32px;
  color: #333;
  margin-bottom: 12px;
}

.subtitle {
  font-size: 16px;
  color: #999;
}

.category-filters {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

.difficulty-filters {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  margin-bottom: 30px;
}

.filter-label {
  font-size: 14px;
  color: #666;
}

.diff-btn {
  font-weight: 500;
}

.diff-btn.diff-1 {
  color: #11998e;
}

.diff-btn.diff-2 {
  color: #f2994a;
}

.diff-btn.diff-3 {
  color: #eb3349;
}

.loading {
  padding: 40px 0;
}

.load-more {
  text-align: center;
  padding: 30px 0;
}

.no-more {
  text-align: center;
  padding: 20px 0;
  color: #999;
  font-size: 14px;
}
</style>

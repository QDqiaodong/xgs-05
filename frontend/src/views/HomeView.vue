<template>
  <div class="home">
    <el-carousel height="300px" class="banner">
      <el-carousel-item v-for="banner in banners" :key="banner.id">
        <div class="banner-item" :style="{ background: banner.bg }">
          <div class="banner-content">
            <h2>{{ banner.title }}</h2>
            <p>{{ banner.subtitle }}</p>
          </div>
        </div>
      </el-carousel-item>
    </el-carousel>

    <div class="container">
      <div class="section">
        <div class="section-header">
          <h2>热门作品</h2>
          <router-link to="/category/all" class="more">查看更多 →</router-link>
        </div>
        <div v-if="loading" class="loading">
          <el-skeleton :rows="5" animated />
        </div>
        <div v-else class="masonry-grid">
          <WorkCard v-for="work in works" :key="work.id" :work="work" />
        </div>
        <div v-if="hasMore" class="load-more" ref="loadMoreRef">
          <el-button :loading="loadingMore" @click="loadMore">加载更多</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import WorkCard from '@/components/WorkCard.vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const route = useRoute()

const banners = ref([
  {
    id: 1,
    title: '编织艺术展',
    subtitle: '一针一线，编织美好生活',
    bg: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
  },
  {
    id: 2,
    title: '陶艺作品展',
    subtitle: '泥土的艺术，指尖的温度',
    bg: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
  },
  {
    id: 3,
    title: '布艺手作集',
    subtitle: '布料的魔法，匠心的传承',
    bg: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
  }
])

const works = ref([])
const loading = ref(true)
const loadingMore = ref(false)
const hasMore = ref(true)
const page = ref(1)
const pageSize = 10
const loadMoreRef = ref(null)

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

async function loadWorks(reset = false) {
  if (reset) {
    page.value = 1
    works.value = []
    hasMore.value = true
  }
  loading.value = true
  try {
    const res = await request.get('/work/hot', {
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
    } else {
      works.value = []
    }
  } catch (e) {
    console.error('加载作品列表失败', e)
    ElMessage.error('加载失败，请刷新重试')
  } finally {
    loading.value = false
  }
}

async function loadMore() {
  if (loadingMore.value || !hasMore.value) return
  loadingMore.value = true
  try {
    page.value++
    const res = await request.get('/work/hot', {
      params: { page: page.value, size: pageSize }
    })
    if (res.code === 200 && res.data) {
      const records = res.data.records || []
      works.value = [...works.value, ...records.map(transformWork)]
      hasMore.value = works.value.length < res.data.total
    }
  } catch (e) {
    console.error('加载更多失败', e)
    page.value--
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

onMounted(() => {
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
.home {
  min-height: calc(100vh - 64px);
}

.banner {
  margin-bottom: 40px;
}

.banner-item {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.banner-content h2 {
  font-size: 36px;
  margin-bottom: 12px;
}

.banner-content p {
  font-size: 18px;
  opacity: 0.9;
}

.section {
  margin-bottom: 40px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.section-header h2 {
  font-size: 24px;
  color: #333;
}

.more {
  color: #667eea;
  font-size: 14px;
}

.loading {
  padding: 40px 0;
}

.load-more {
  text-align: center;
  padding: 30px 0;
}
</style>

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
import WorkCard from '@/components/WorkCard.vue'

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
const loadMoreRef = ref(null)

const mockWorks = [
  { id: 1, title: '手工编织毛衣', description: '温暖的羊毛手工编织，耗时一个月完成', coverImage: 'https://picsum.photos/300/400?random=1', authorId: 1, authorName: '小手巧', authorAvatar: 'https://via.placeholder.com/24', viewCount: 1256, favoriteCount: 89, isHot: true, categoryId: 1 },
  { id: 2, title: '陶艺花瓶', description: '手工拉坯制作，釉色温润如玉', coverImage: 'https://picsum.photos/300/350?random=2', authorId: 2, authorName: '陶然', authorAvatar: 'https://via.placeholder.com/24', viewCount: 892, favoriteCount: 67, isHot: false, categoryId: 2 },
  { id: 3, title: '布艺玩偶套装', description: '可爱的小动物布艺玩偶，送给孩子的礼物', coverImage: 'https://picsum.photos/300/450?random=3', authorId: 3, authorName: '布布', authorAvatar: 'https://via.placeholder.com/24', viewCount: 2341, favoriteCount: 156, isHot: true, categoryId: 3 },
  { id: 4, title: '原木摆件', description: '天然木材手工雕刻，保留原木质感', coverImage: 'https://picsum.photos/300/380?random=4', authorId: 4, authorName: '木工匠', authorAvatar: 'https://via.placeholder.com/24', viewCount: 567, favoriteCount: 34, isHot: false, categoryId: 4 },
  { id: 5, title: '毛线围巾', description: '柔软的马海毛线，温暖整个冬天', coverImage: 'https://picsum.photos/300/420?random=5', authorId: 1, authorName: '小手巧', authorAvatar: 'https://via.placeholder.com/24', viewCount: 1890, favoriteCount: 123, isHot: true, categoryId: 1 },
  { id: 6, title: '手工茶具', description: '一套精美的手工茶具，品茗必备', coverImage: 'https://picsum.photos/300/360?random=6', authorId: 2, authorName: '陶然', authorAvatar: 'https://via.placeholder.com/24', viewCount: 1456, favoriteCount: 98, isHot: false, categoryId: 2 },
  { id: 7, title: '刺绣手帕', description: '精致的苏绣工艺，传统与现代结合', coverImage: 'https://picsum.photos/300/390?random=7', authorId: 3, authorName: '布布', authorAvatar: 'https://via.placeholder.com/24', viewCount: 789, favoriteCount: 56, isHot: false, categoryId: 3 },
  { id: 8, title: '木质首饰盒', description: '胡桃木制作，精美的收纳盒', coverImage: 'https://picsum.photos/300/340?random=8', authorId: 4, authorName: '木工匠', authorAvatar: 'https://via.placeholder.com/24', viewCount: 678, favoriteCount: 45, isHot: false, categoryId: 4 }
]

function loadWorks() {
  loading.value = true
  setTimeout(() => {
    works.value = mockWorks
    loading.value = false
  }, 500)
}

function loadMore() {
  loadingMore.value = true
  setTimeout(() => {
    const newWorks = mockWorks.map((w, i) => ({
      ...w,
      id: w.id + works.value.length,
      coverImage: `https://picsum.photos/300/${350 + Math.random() * 100}?random=${works.value.length + i}`
    }))
    works.value = [...works.value, ...newWorks]
    page.value++
    if (page.value >= 3) {
      hasMore.value = false
    }
    loadingMore.value = false
  }, 800)
}

onMounted(() => {
  loadWorks()
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

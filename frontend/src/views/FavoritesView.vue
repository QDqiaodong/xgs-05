<template>
  <div class="favorites container">
    <div class="page-header">
      <h1>我的收藏</h1>
      <p class="subtitle">共收藏 {{ favorites.length }} 件作品</p>
    </div>
    <div v-if="loading" class="loading">
      <el-skeleton :rows="5" animated />
    </div>
    <div v-else-if="favorites.length > 0" class="masonry-grid">
      <WorkCard v-for="work in favorites" :key="work.id" :work="work" />
    </div>
    <div v-else class="empty-state">
      <el-empty description="暂无收藏作品">
        <el-button type="primary" @click="$router.push('/')">去发现作品</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import WorkCard from '@/components/WorkCard.vue'

const loading = ref(true)
const favorites = ref([])

const mockFavorites = [
  { id: 1, title: '手工编织毛衣', description: '温暖的羊毛手工编织，耗时一个月完成', coverImage: 'https://picsum.photos/300/400?random=30', authorId: 1, authorName: '小手巧', authorAvatar: 'https://via.placeholder.com/24', viewCount: 1256, favoriteCount: 89, isHot: true, categoryId: 1 },
  { id: 2, title: '陶艺花瓶', description: '手工拉坯制作，釉色温润如玉', coverImage: 'https://picsum.photos/300/350?random=31', authorId: 2, authorName: '陶然', authorAvatar: 'https://via.placeholder.com/24', viewCount: 892, favoriteCount: 67, isHot: false, categoryId: 2 },
  { id: 3, title: '布艺玩偶套装', description: '可爱的小动物布艺玩偶，送给孩子的礼物', coverImage: 'https://picsum.photos/300/450?random=32', authorId: 3, authorName: '布布', authorAvatar: 'https://via.placeholder.com/24', viewCount: 2341, favoriteCount: 156, isHot: true, categoryId: 3 }
]

function loadFavorites() {
  loading.value = true
  setTimeout(() => {
    favorites.value = mockFavorites
    loading.value = false
  }, 500)
}

onMounted(() => {
  loadFavorites()
})
</script>

<style scoped>
.favorites {
  padding: 30px 0;
}

.page-header {
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 28px;
  color: #333;
  margin-bottom: 8px;
}

.subtitle {
  font-size: 14px;
  color: #999;
}

.loading {
  padding: 40px 0;
}

.empty-state {
  padding: 80px 0;
}
</style>

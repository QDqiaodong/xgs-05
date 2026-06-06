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

    <div v-if="loading" class="loading">
      <el-skeleton :rows="5" animated />
    </div>
    <div v-else class="masonry-grid">
      <WorkCard v-for="work in works" :key="work.id" :work="work" />
    </div>

    <div v-if="hasMore" class="load-more">
      <el-button :loading="loadingMore" @click="loadMore">加载更多</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import WorkCard from '@/components/WorkCard.vue'

const route = useRoute()
const loading = ref(true)
const loadingMore = ref(false)
const hasMore = ref(true)
const page = ref(1)
const activeCategory = ref(1)

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

const mockWorks = {
  1: [
    { id: 1, title: '手工编织毛衣', description: '温暖的羊毛手工编织，耗时一个月完成', coverImage: 'https://picsum.photos/300/400?random=40', authorId: 1, authorName: '小手巧', authorAvatar: 'https://via.placeholder.com/24', viewCount: 1256, favoriteCount: 89, isHot: true, categoryId: 1 },
    { id: 2, title: '毛线围巾', description: '柔软的马海毛线，温暖整个冬天', coverImage: 'https://picsum.photos/300/420?random=41', authorId: 1, authorName: '小手巧', authorAvatar: 'https://via.placeholder.com/24', viewCount: 1890, favoriteCount: 123, isHot: true, categoryId: 1 },
    { id: 3, title: '针织手套', description: '冬日必备，温暖双手', coverImage: 'https://picsum.photos/300/350?random=42', authorId: 1, authorName: '小手巧', authorAvatar: 'https://via.placeholder.com/24', viewCount: 567, favoriteCount: 45, isHot: false, categoryId: 1 },
    { id: 4, title: '婴儿毛毯', description: '给宝宝最柔软的呵护', coverImage: 'https://picsum.photos/300/380?random=43', authorId: 1, authorName: '小手巧', authorAvatar: 'https://via.placeholder.com/24', viewCount: 2341, favoriteCount: 156, isHot: true, categoryId: 1 }
  ],
  2: [
    { id: 5, title: '陶艺花瓶', description: '手工拉坯制作，釉色温润如玉', coverImage: 'https://picsum.photos/300/350?random=44', authorId: 2, authorName: '陶然', authorAvatar: 'https://via.placeholder.com/24', viewCount: 892, favoriteCount: 67, isHot: false, categoryId: 2 },
    { id: 6, title: '手工茶具', description: '一套精美的手工茶具，品茗必备', coverImage: 'https://picsum.photos/300/360?random=45', authorId: 2, authorName: '陶然', authorAvatar: 'https://via.placeholder.com/24', viewCount: 1456, favoriteCount: 98, isHot: false, categoryId: 2 },
    { id: 7, title: '陶土摆件', description: '可爱的小动物陶土摆件', coverImage: 'https://picsum.photos/300/400?random=46', authorId: 2, authorName: '陶然', authorAvatar: 'https://via.placeholder.com/24', viewCount: 756, favoriteCount: 43, isHot: false, categoryId: 2 }
  ],
  3: [
    { id: 8, title: '布艺玩偶套装', description: '可爱的小动物布艺玩偶，送给孩子的礼物', coverImage: 'https://picsum.photos/300/450?random=47', authorId: 3, authorName: '布布', authorAvatar: 'https://via.placeholder.com/24', viewCount: 2341, favoriteCount: 156, isHot: true, categoryId: 3 },
    { id: 9, title: '刺绣手帕', description: '精致的苏绣工艺，传统与现代结合', coverImage: 'https://picsum.photos/300/390?random=48', authorId: 3, authorName: '布布', authorAvatar: 'https://via.placeholder.com/24', viewCount: 789, favoriteCount: 56, isHot: false, categoryId: 3 }
  ],
  4: [
    { id: 10, title: '原木摆件', description: '天然木材手工雕刻，保留原木质感', coverImage: 'https://picsum.photos/300/380?random=49', authorId: 4, authorName: '木工匠', authorAvatar: 'https://via.placeholder.com/24', viewCount: 567, favoriteCount: 34, isHot: false, categoryId: 4 },
    { id: 11, title: '木质首饰盒', description: '胡桃木制作，精美的收纳盒', coverImage: 'https://picsum.photos/300/340?random=50', authorId: 4, authorName: '木工匠', authorAvatar: 'https://via.placeholder.com/24', viewCount: 678, favoriteCount: 45, isHot: false, categoryId: 4 }
  ]
}

function loadWorks() {
  loading.value = true
  page.value = 1
  hasMore.value = true
  setTimeout(() => {
    works.value = mockWorks[activeCategory.value] || []
    loading.value = false
  }, 500)
}

function loadMore() {
  loadingMore.value = true
  setTimeout(() => {
    const newWorks = (mockWorks[activeCategory.value] || []).map((w, i) => ({
      ...w,
      id: w.id + works.value.length,
      coverImage: `https://picsum.photos/300/${350 + Math.random() * 100}?random=${100 + works.value.length + i}`
    }))
    works.value = [...works.value, ...newWorks]
    page.value++
    if (page.value >= 3) {
      hasMore.value = false
    }
    loadingMore.value = false
  }, 800)
}

watch(activeCategory, () => {
  loadWorks()
})

onMounted(() => {
  const categoryId = parseInt(route.params.categoryId) || 1
  activeCategory.value = categoryId
  loadWorks()
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
  margin-bottom: 30px;
}

.loading {
  padding: 40px 0;
}

.load-more {
  text-align: center;
  padding: 30px 0;
}
</style>

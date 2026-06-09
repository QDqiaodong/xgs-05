<template>
  <div class="favorites container">
    <div class="page-header">
      <h1>我的收藏</h1>
      <p class="subtitle">共收藏 {{ displayedFavorites.length }} 件作品</p>
    </div>
    <div v-if="loading" class="loading">
      <el-skeleton :rows="5" animated />
    </div>
    <div v-else-if="displayedFavorites.length > 0" class="masonry-grid">
      <WorkCard v-for="work in displayedFavorites" :key="work.id" :work="work" />
    </div>
    <div v-else class="empty-state">
      <el-empty description="暂无收藏作品">
        <el-button type="primary" @click="$router.push('/')">去发现作品</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import WorkCard from '@/components/WorkCard.vue'
import { useFavoriteStore } from '@/store/favorite'
import { useUserStore } from '@/store/user'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const favoriteStore = useFavoriteStore()
const userStore = useUserStore()
const loading = ref(true)
const favorites = ref([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

const displayedFavorites = computed(() => {
  favoriteStore.version
  return favorites.value.filter(w => favoriteStore.isFavorited(w.id))
})

async function loadFavorites() {
  if (!userStore.isLoggedIn || !userStore.userInfo?.id) {
    loading.value = false
    favorites.value = []
    return
  }
  loading.value = true
  try {
    const res = await request.get(`/favorite/list/${userStore.userInfo.id}`, {
      params: { page: currentPage.value, size: pageSize.value }
    })
    if (res.code === 200 && res.data) {
      favorites.value = res.data.records || []
      total.value = res.data.total || 0
      const ids = favorites.value.map(w => Number(w.id))
      ids.forEach(id => favoriteStore.favoriteWorkIds.add(id))
      favoriteStore.touchVersion()
    } else {
      favorites.value = []
    }
  } catch (e) {
    ElMessage.error('加载收藏列表失败')
    favorites.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadFavorites()
})

watch(() => favoriteStore.version, () => {
  if (userStore.isLoggedIn) {
    loadFavorites()
  }
})

watch(() => userStore.isLoggedIn, (val) => {
  if (val) {
    loadFavorites()
  } else {
    favorites.value = []
  }
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

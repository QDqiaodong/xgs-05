<template>
  <div class="profile container">
    <div class="profile-header card">
      <div class="profile-info">
        <img :src="user.avatar || 'https://via.placeholder.com/120'" alt="avatar" class="avatar" />
        <div class="info">
          <h1>{{ user.username }}</h1>
          <p class="bio">{{ user.bio || '热爱手工创作的艺术家' }}</p>
          <div class="user-stats">
            <div class="stat">
              <span class="stat-value">{{ stats.works }}</span>
              <span class="stat-label">作品</span>
            </div>
            <div class="stat">
              <span class="stat-value">{{ stats.followers }}</span>
              <span class="stat-label">粉丝</span>
            </div>
            <div class="stat">
              <span class="stat-value">{{ stats.following }}</span>
              <span class="stat-label">关注</span>
            </div>
            <div class="stat">
              <span class="stat-value">{{ stats.totalViews }}</span>
              <span class="stat-label">总浏览</span>
            </div>
          </div>
        </div>
      </div>
      <div class="profile-actions" v-if="isOwner">
        <el-button type="primary">编辑资料</el-button>
      </div>
      <div class="profile-actions" v-else>
        <el-button type="primary">+ 关注</el-button>
      </div>
    </div>

    <div class="profile-tabs">
      <el-tabs v-model="activeTab" class="tabs">
        <el-tab-pane label="全部作品" name="works">
          <div class="masonry-grid">
            <WorkCard v-for="work in works" :key="work.id" :work="work" />
          </div>
          <div v-if="works.length === 0" class="empty">
            <el-empty description="暂无作品" />
          </div>
        </el-tab-pane>
        <el-tab-pane label="收藏作品" name="favorites">
          <div class="masonry-grid">
            <WorkCard v-for="work in favorites" :key="work.id" :work="work" />
          </div>
          <div v-if="favorites.length === 0" class="empty">
            <el-empty description="暂无收藏" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import WorkCard from '@/components/WorkCard.vue'

const route = useRoute()
const userStore = useUserStore()
const activeTab = ref('works')

const user = ref({
  id: 1,
  username: '小手巧',
  avatar: 'https://via.placeholder.com/120',
  bio: '专注手工编织5年，热爱所有美好的事物'
})

const stats = ref({
  works: 28,
  followers: 1256,
  following: 89,
  totalViews: 56789
})

const isOwner = computed(() => {
  return userStore.userInfo?.id === user.value.id
})

const works = ref([
  { id: 1, title: '手工编织毛衣', description: '温暖的羊毛手工编织，耗时一个月完成', coverImage: 'https://picsum.photos/300/400?random=20', authorId: 1, authorName: '小手巧', authorAvatar: 'https://via.placeholder.com/24', viewCount: 1256, favoriteCount: 89, isHot: true, categoryId: 1 },
  { id: 2, title: '毛线围巾', description: '柔软的马海毛线，温暖整个冬天', coverImage: 'https://picsum.photos/300/420?random=21', authorId: 1, authorName: '小手巧', authorAvatar: 'https://via.placeholder.com/24', viewCount: 1890, favoriteCount: 123, isHot: true, categoryId: 1 },
  { id: 3, title: '针织手套', description: '冬日必备，温暖双手', coverImage: 'https://picsum.photos/300/350?random=22', authorId: 1, authorName: '小手巧', authorAvatar: 'https://via.placeholder.com/24', viewCount: 567, favoriteCount: 45, isHot: false, categoryId: 1 },
  { id: 4, title: '婴儿毛毯', description: '给宝宝最柔软的呵护', coverImage: 'https://picsum.photos/300/380?random=23', authorId: 1, authorName: '小手巧', authorAvatar: 'https://via.placeholder.com/24', viewCount: 2341, favoriteCount: 156, isHot: true, categoryId: 1 }
])

const favorites = ref([
  { id: 10, title: '陶艺花瓶', description: '手工拉坯制作，釉色温润如玉', coverImage: 'https://picsum.photos/300/350?random=24', authorId: 2, authorName: '陶然', authorAvatar: 'https://via.placeholder.com/24', viewCount: 892, favoriteCount: 67, isHot: false, categoryId: 2 }
])

onMounted(() => {
  console.log('User ID:', route.params.userId)
})
</script>

<style scoped>
.profile {
  padding: 30px 0;
}

.profile-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 40px;
  margin-bottom: 30px;
}

.profile-info {
  display: flex;
  gap: 30px;
  align-items: flex-start;
}

.avatar {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  border: 4px solid #667eea;
}

.info h1 {
  font-size: 28px;
  color: #333;
  margin-bottom: 12px;
}

.bio {
  font-size: 14px;
  color: #666;
  margin-bottom: 20px;
}

.user-stats {
  display: flex;
  gap: 40px;
}

.stat {
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  color: #999;
}

.profile-actions {
  display: flex;
  gap: 12px;
}

.profile-tabs {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.tabs :deep(.el-tabs__header) {
  margin-bottom: 24px;
}

.empty {
  padding: 60px 0;
}
</style>

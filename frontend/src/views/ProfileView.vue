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
      <div class="tabs-nav-wrapper">
        <div class="tabs-nav">
          <div :class="['tab-item', { 'is-active': activeTab === 'works' }]" @click="activeTab = 'works'">全部作品</div>
          <div :class="['tab-item', { 'is-active': activeTab === 'favorites' }]" @click="activeTab = 'favorites'">收藏作品</div>
        </div>
        <div class="layout-switcher">
          <span class="layout-label">展示模式：</span>
          <el-radio-group v-model="activeLayout" size="small">
            <el-radio-button value="masonry">
              <el-tooltip content="瀑布流" placement="top">
                <el-icon><Grid /></el-icon>
              </el-tooltip>
            </el-radio-button>
            <el-radio-button value="grid">
              <el-tooltip content="对称网格" placement="top">
                <el-icon><Menu /></el-icon>
              </el-tooltip>
            </el-radio-button>
            <el-radio-button value="list">
              <el-tooltip content="简约列表" placement="top">
                <el-icon><List /></el-icon>
              </el-tooltip>
            </el-radio-button>
          </el-radio-group>
        </div>
      </div>

      <div v-show="activeTab === 'works'">
        <div v-if="works.length > 0" :class="gridContainerClass">
          <WorkCard v-for="work in works" :key="work.id" :work="work" :layout="activeLayout" />
        </div>
        <div v-if="works.length === 0" class="empty">
          <el-empty description="暂无作品" />
        </div>
      </div>

      <div v-show="activeTab === 'favorites'">
        <div v-if="favorites.length > 0" :class="gridContainerClass">
          <WorkCard v-for="work in favorites" :key="work.id" :work="work" :layout="activeLayout" />
        </div>
        <div v-if="favorites.length === 0" class="empty">
          <el-empty description="暂无收藏" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { Grid, Menu, List } from '@element-plus/icons-vue'
import WorkCard from '@/components/WorkCard.vue'

const route = useRoute()
const userStore = useUserStore()
const activeTab = ref('works')
const activeLayout = ref(localStorage.getItem('profileLayout') || 'masonry')

const gridContainerClass = computed(() => {
  return `works-container works-${activeLayout.value}`
})

watch(activeLayout, (newVal) => {
  localStorage.setItem('profileLayout', newVal)
})

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
  { id: 4, title: '婴儿毛毯', description: '给宝宝最柔软的呵护', coverImage: 'https://picsum.photos/300/380?random=23', authorId: 1, authorName: '小手巧', authorAvatar: 'https://via.placeholder.com/24', viewCount: 2341, favoriteCount: 156, isHot: true, categoryId: 1 },
  { id: 5, title: '手工编织帽', description: '可爱的贝雷帽，冬日时尚单品', coverImage: 'https://picsum.photos/300/360?random=24', authorId: 1, authorName: '小手巧', authorAvatar: 'https://via.placeholder.com/24', viewCount: 892, favoriteCount: 67, isHot: false, categoryId: 1 },
  { id: 6, title: '钩针杯垫套装', description: '精致的杯垫，为家居增添温馨', coverImage: 'https://picsum.photos/300/300?random=25', authorId: 1, authorName: '小手巧', authorAvatar: 'https://via.placeholder.com/24', viewCount: 456, favoriteCount: 38, isHot: false, categoryId: 1 },
  { id: 7, title: '毛线玩偶', description: '可爱的小兔子玩偶，送给孩子的礼物', coverImage: 'https://picsum.photos/300/450?random=26', authorId: 1, authorName: '小手巧', authorAvatar: 'https://via.placeholder.com/24', viewCount: 1678, favoriteCount: 134, isHot: true, categoryId: 1 },
  { id: 8, title: '手工编织包包', description: '时尚的草编包，夏日必备单品', coverImage: 'https://picsum.photos/300/390?random=27', authorId: 1, authorName: '小手巧', authorAvatar: 'https://via.placeholder.com/24', viewCount: 2134, favoriteCount: 178, isHot: true, categoryId: 1 }
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

.tabs-nav-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 2px solid #e4e7ed;
  margin-bottom: 24px;
}

.tabs-nav {
  display: flex;
  gap: 32px;
}

.tab-item {
  padding: 12px 0;
  font-size: 16px;
  color: #606266;
  cursor: pointer;
  position: relative;
  transition: color 0.3s;
  margin-bottom: -2px;
}

.tab-item:hover {
  color: #667eea;
}

.tab-item.is-active {
  color: #667eea;
  font-weight: 600;
}

.tab-item.is-active::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  right: 0;
  height: 2px;
  background: #667eea;
}

.layout-switcher {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-bottom: 2px;
}

.layout-label {
  font-size: 13px;
  color: #666;
}

.empty {
  padding: 60px 0;
}

.works-masonry {
  column-count: 4;
  column-gap: 20px;
}

.works-masonry :deep(.work-card) {
  break-inside: avoid;
  margin-bottom: 20px;
}

.works-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.works-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

@media (max-width: 1200px) {
  .works-masonry {
    column-count: 3;
  }
  .works-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .works-masonry {
    column-count: 2;
  }
  .works-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .tabs-nav-wrapper {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  .tabs-nav {
    width: 100%;
    gap: 24px;
  }
  .layout-switcher {
    width: 100%;
    padding-bottom: 12px;
  }
}

@media (max-width: 480px) {
  .works-masonry {
    column-count: 1;
  }
  .works-grid {
    grid-template-columns: 1fr;
  }
}
</style>

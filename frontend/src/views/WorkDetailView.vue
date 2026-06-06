<template>
  <div class="work-detail container">
    <div class="view-toggle">
      <el-radio-group v-model="viewMode" size="large">
        <el-radio-button value="detail">
          <el-icon><Document /></el-icon>
          详情浏览
        </el-radio-button>
        <el-radio-button value="steps">
          <el-icon><List /></el-icon>
          步骤学习
        </el-radio-button>
      </el-radio-group>
    </div>

    <div v-if="viewMode === 'detail'" class="detail-card card">
      <div class="work-gallery">
        <div class="main-image">
          <img :src="work.coverImage || 'https://picsum.photos/800/600'" alt="作品主图" />
        </div>
        <div class="thumbnails">
          <img v-for="(img, index) in work.images || []" :key="index" :src="img" :class="{ active: activeImage === index }" @click="activeImage = index" />
        </div>
      </div>
      <div class="work-content">
        <div class="work-header">
          <h1>{{ work.title }}</h1>
          <div class="work-tags">
            <el-tag type="success">{{ categoryName }}</el-tag>
            <el-tag type="info">制作周期: {{ work.productionCycle }}</el-tag>
          </div>
        </div>
        <div class="author-section">
          <router-link :to="`/profile/${work.authorId}`" class="author-info">
            <img :src="work.authorAvatar || 'https://via.placeholder.com/60'" alt="作者头像" />
            <div>
              <h3>{{ work.authorName }}</h3>
              <p>{{ work.authorBio || '热爱手工创作的艺术家' }}</p>
            </div>
          </router-link>
          <el-button type="primary" plain>关注</el-button>
        </div>
        <div class="work-stats">
          <div class="stat">
            <span class="stat-value">{{ work.viewCount }}</span>
            <span class="stat-label">浏览</span>
          </div>
          <div class="stat">
            <span class="stat-value">{{ work.favoriteCount }}</span>
            <span class="stat-label">收藏</span>
          </div>
          <div class="stat">
            <span class="stat-value">{{ work.likeCount || 0 }}</span>
            <span class="stat-label">喜欢</span>
          </div>
        </div>
        <div class="work-section">
          <h3>用料清单</h3>
          <p class="section-content">{{ work.materials || '暂无详细用料信息' }}</p>
        </div>
        <div class="work-section">
          <h3>创作思路</h3>
          <p class="section-content">{{ work.creationIdea || '暂无创作思路描述' }}</p>
        </div>
        <div class="work-section">
          <h3>作品描述</h3>
          <p class="section-content">{{ work.description }}</p>
        </div>
        <div class="work-section" v-if="steps.length > 0">
          <h3>制作步骤</h3>
          <div class="steps-preview">
            <div v-for="(step, index) in steps" :key="index" class="step-preview-item" @click="jumpToStepMode">
              <span class="step-preview-num">{{ index + 1 }}</span>
              <span class="step-preview-title">{{ step.title }}</span>
              <el-icon class="step-preview-arrow"><ArrowRight /></el-icon>
            </div>
          </div>
          <el-button type="primary" plain class="enter-step-btn" @click="jumpToStepMode">
            <el-icon><VideoPlay /></el-icon>
            进入步骤学习模式
          </el-button>
        </div>
        <div class="action-buttons">
          <el-button :type="isFavorited ? 'danger' : 'primary'" :icon="isFavorited ? StarFilled : Star" @click="toggleFavorite">
            {{ isFavorited ? '已收藏' : '收藏作品' }}
          </el-button>
          <el-button type="success" :icon="Share">分享</el-button>
        </div>
      </div>
    </div>

    <div v-else class="steps-view card">
      <div class="steps-view-header">
        <div class="back-link" @click="viewMode = 'detail'">
          <el-icon><ArrowLeft /></el-icon>
          返回详情
        </div>
        <h2 class="steps-view-title">{{ work.title }} · 制作教程</h2>
        <div class="steps-view-subtitle">共 {{ steps.length }} 个步骤，跟随学习更轻松</div>
      </div>
      <StepBrowser v-if="steps.length > 0" :steps="steps" @finish="onStepsFinish" />
      <div v-else class="no-steps">
        <el-icon :size="64" color="#c0c4cc"><Warning /></el-icon>
        <p>该作品暂无制作步骤</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Star, StarFilled, Share, Document, List, ArrowRight, VideoPlay, ArrowLeft, Warning } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import StepBrowser from '../components/StepBrowser.vue'

const route = useRoute()
const activeImage = ref(0)
const isFavorited = ref(false)
const viewMode = ref('detail')

const work = ref({
  id: 1,
  title: '手工编织毛衣',
  description: '温暖的羊毛手工编织，耗时一个月完成，采用纯羊毛线，手感柔软舒适。',
  coverImage: 'https://picsum.photos/800/600?random=10',
  images: [
    'https://picsum.photos/800/600?random=10',
    'https://picsum.photos/800/600?random=11',
    'https://picsum.photos/800/600?random=12',
    'https://picsum.photos/800/600?random=13'
  ],
  authorId: 1,
  authorName: '小手巧',
  authorAvatar: 'https://via.placeholder.com/60',
  authorBio: '专注手工编织5年，热爱所有美好的事物',
  viewCount: 1256,
  favoriteCount: 89,
  likeCount: 156,
  categoryId: 1,
  materials: '纯羊毛线 500g，棒针一副，缝针一枚',
  creationIdea: '这款毛衣的设计灵感来源于北欧风格，采用简约的几何图案，既保暖又时尚。选择了天然的羊毛材质，让穿着者感受到大自然的温暖。',
  productionCycle: '30天'
})

const steps = ref([
  {
    title: '准备材料与工具',
    description: '首先准备好所有需要的材料：500g 纯羊毛线（建议选择柔软亲肤的材质）、一副适合线材粗细的棒针（通常为 4.5mm-5mm）、一枚缝针用于收尾。将毛线整理成球状，避免编织过程中打结。',
    image: 'https://picsum.photos/600/400?random=21',
    tips: [
      '建议选择有光泽的羊毛线，成品会更加美观',
      '新手可以选择浅色线材，更容易看清针脚',
      '棒针长度建议选择 40cm，方便操作'
    ]
  },
  {
    title: '起针与底边编织',
    description: '使用长尾起针法起针，根据胸围尺寸计算针数（本款毛衣起 120 针）。起针完成后，编织 2 正 2 反的罗纹针作为底边，长度约 8 厘米。罗纹针能够让毛衣下摆更有弹性，穿着更舒适。',
    image: 'https://picsum.photos/600/400?random=22',
    tips: [
      '起针时不要拉太紧，否则底边会发硬',
      '每一排结束时要注意边缘针的处理，保持整齐',
      '可以用记号扣标记起始位置'
    ]
  },
  {
    title: '正身花样编织',
    description: '底边完成后，开始编织正身部分。本款采用简单的平针编织，正面织下针，反面织上针。每隔 10 行在两侧各加 1 针，共加 6 次，形成自然的腰线弧度。编织至腋下位置（约 40 厘米）停止。',
    image: 'https://picsum.photos/600/400?random=23',
    tips: [
      '加针要均匀对称，避免左右不对称',
      '可以每织完一段就量一下尺寸，确保长度合适',
      '注意保持针脚松紧一致'
    ]
  },
  {
    title: '袖窿与领口收针',
    description: '到达腋下位置后，开始袖窿收针：先平收 4 针，然后每 2 行收 2 针收 3 次，每 2 行收 1 针收 4 次。前后片都按照同样方法处理。领口部分中间平收 20 针，两侧每 2 行收 3 针收 2 次，每 2 行收 2 针收 2 次。',
    image: 'https://picsum.photos/600/400?random=24',
    tips: [
      '收针时注意不要拉太紧，保持边缘平整',
      '前后片的袖窿要对称',
      '领口弧度要圆润自然'
    ]
  },
  {
    title: '袖子编织与缝合',
    description: '单独编织袖子：从袖口开始起 48 针，织 6 厘米罗纹边，然后开始加针，每 6 行加 2 针，加至 72 针后不加不减编织，直到袖长合适。最后将袖子与正身缝合，注意对准袖窿中点和肩缝。',
    image: 'https://picsum.photos/600/400?random=25',
    tips: [
      '袖子的加针要均匀，形成自然的锥形',
      '缝合时用线要与主体颜色一致',
      '建议用隐形缝合法，接缝更美观'
    ]
  },
  {
    title: '收尾整理与定型',
    description: '使用缝针将所有线头藏好，然后将毛衣平铺在湿布上，用手轻轻拉伸至理想尺寸，让其自然晾干定型。最后修剪掉多余的线头，一件温暖舒适的手工毛衣就完成啦！',
    image: 'https://picsum.photos/600/400?random=26',
    tips: [
      '定型时不要拉扯过度，避免变形',
      '羊毛制品建议平铺晾干，不要悬挂',
      '可以用蒸汽熨斗低温熨烫，注意垫布'
    ]
  }
])

const categoryName = computed(() => {
  const categories = { 1: '编织', 2: '陶艺', 3: '布艺', 4: '木艺' }
  return categories[work.value.categoryId] || '其他'
})

function toggleFavorite() {
  isFavorited.value = !isFavorited.value
  if (isFavorited.value) {
    work.value.favoriteCount++
    ElMessage.success('已收藏作品')
  } else {
    work.value.favoriteCount--
    ElMessage.info('已取消收藏')
  }
}

function jumpToStepMode() {
  viewMode.value = 'steps'
}

function onStepsFinish() {
  console.log('用户完成了所有步骤学习')
}

onMounted(() => {
  console.log('Work ID:', route.params.id)
})
</script>

<style scoped>
.work-detail {
  padding: 30px 0;
}

.detail-card {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 40px;
  padding: 30px;
}

.work-gallery {
  position: sticky;
  top: 100px;
  height: fit-content;
}

.main-image {
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 16px;
}

.main-image img {
  width: 100%;
  display: block;
}

.thumbnails {
  display: flex;
  gap: 12px;
}

.thumbnails img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
  cursor: pointer;
  border: 2px solid transparent;
  transition: border-color 0.3s;
}

.thumbnails img.active {
  border-color: #667eea;
}

.work-header h1 {
  font-size: 28px;
  color: #333;
  margin-bottom: 16px;
}

.work-tags {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

.author-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 24px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.author-info img {
  width: 60px;
  height: 60px;
  border-radius: 50%;
}

.author-info h3 {
  font-size: 16px;
  color: #333;
  margin-bottom: 4px;
}

.author-info p {
  font-size: 13px;
  color: #666;
}

.work-stats {
  display: flex;
  gap: 40px;
  padding: 20px 0;
  border-top: 1px solid #eee;
  border-bottom: 1px solid #eee;
  margin-bottom: 24px;
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

.work-section {
  margin-bottom: 24px;
}

.work-section h3 {
  font-size: 16px;
  color: #333;
  margin-bottom: 12px;
  padding-left: 12px;
  border-left: 3px solid #667eea;
}

.section-content {
  font-size: 14px;
  color: #666;
  line-height: 1.8;
  padding-left: 15px;
}

.action-buttons {
  display: flex;
  gap: 16px;
  margin-top: 30px;
}

.action-buttons .el-button {
  flex: 1;
  padding: 12px 24px;
}

.view-toggle {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
}

.view-toggle :deep(.el-radio-button__inner) {
  padding: 12px 24px;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.steps-preview {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

.step-preview-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: #f8f9fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.step-preview-item:hover {
  background: #eef0ff;
  transform: translateX(4px);
}

.step-preview-num {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  flex-shrink: 0;
}

.step-preview-title {
  flex: 1;
  font-size: 14px;
  color: #606266;
}

.step-preview-arrow {
  color: #c0c4cc;
  font-size: 16px;
}

.enter-step-btn {
  width: 100%;
  padding: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-weight: 500;
}

.steps-view {
  padding: 30px;
}

.steps-view-header {
  text-align: center;
  margin-bottom: 30px;
  padding-bottom: 24px;
  border-bottom: 1px solid #ebeef5;
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #667eea;
  font-size: 14px;
  cursor: pointer;
  margin-bottom: 16px;
  transition: color 0.3s;
}

.back-link:hover {
  color: #764ba2;
}

.steps-view-title {
  font-size: 24px;
  color: #303133;
  margin: 0 0 8px 0;
}

.steps-view-subtitle {
  font-size: 14px;
  color: #909399;
}

.no-steps {
  padding: 80px 40px;
  text-align: center;
  color: #909399;
}

.no-steps p {
  margin-top: 16px;
  font-size: 16px;
}

@media (max-width: 968px) {
  .detail-card {
    grid-template-columns: 1fr;
  }

  .steps-view {
    padding: 20px;
  }

  .steps-view-title {
    font-size: 20px;
  }
}
</style>

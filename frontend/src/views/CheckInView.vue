<template>
  <div class="checkin-view container">
    <div class="view-hero">
      <div class="hero-content">
        <div class="hero-title-row">
          <h1 class="hero-title">每日创作打卡</h1>
          <div class="hero-badge">✨ 记录每一步成长</div>
        </div>
        <p class="hero-subtitle">
          每天记录创作进度，打卡日历墙见证你的坚持，成长轨迹记录每一次蜕变
        </p>
      </div>
    </div>

    <div class="checkin-content">
      <CheckInStats ref="statsRef" />

      <div class="section-row" id="checkin-form-section">
        <div class="section-left">
          <CheckInForm @checkin-success="handleCheckInSuccess" />
        </div>
        <div class="section-right">
          <div class="quick-actions card">
            <h3 class="action-title">
              <span>⚡ 快捷操作</span>
            </h3>
            <div class="action-buttons">
              <el-button type="primary" @click="scrollToForm" class="action-btn primary-btn">
                <el-icon><EditPen /></el-icon>
                <span>立即打卡</span>
              </el-button>
              <el-button @click="scrollToCalendar" class="action-btn">
                <el-icon><Calendar /></el-icon>
                <span>查看日历</span>
              </el-button>
              <el-button @click="scrollToTimeline" class="action-btn">
                <el-icon><Histogram /></el-icon>
                <span>成长轨迹</span>
              </el-button>
            </div>
            <div class="tip-section">
              <div class="tip-title">
                <el-icon><InfoFilled /></el-icon>
                <span>打卡小贴士</span>
              </div>
              <ul class="tip-list">
                <li>每天坚持打卡，养成创作习惯</li>
                <li>上传进度照片，记录作品变化</li>
                <li>记录创作心得，总结经验与灵感</li>
                <li>连续打卡7天解锁成就徽章</li>
              </ul>
            </div>
          </div>
        </div>
      </div>

      <div id="calendar-section">
        <div class="section-header">
          <h2 class="section-title">
            <span class="section-icon">🗓️</span>
            <span>打卡日历墙</span>
          </h2>
          <p class="section-desc">回顾每一天的创作足迹</p>
        </div>
        <CheckInCalendar ref="calendarRef" />
      </div>

      <div id="timeline-section">
        <CheckInTimeline ref="timelineRef" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { EditPen, Calendar, Histogram, InfoFilled } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import CheckInStats from '@/components/CheckInStats.vue'
import CheckInForm from '@/components/CheckInForm.vue'
import CheckInCalendar from '@/components/CheckInCalendar.vue'
import CheckInTimeline from '@/components/CheckInTimeline.vue'

const userStore = useUserStore()
const statsRef = ref(null)
const calendarRef = ref(null)
const timelineRef = ref(null)

function handleCheckInSuccess() {
  ElMessage.success('数据已更新')
  if (statsRef.value) statsRef.value.loadStats()
  if (calendarRef.value) calendarRef.value.loadCalendar()
  if (timelineRef.value) timelineRef.value.loadTimeline()
}

function scrollToForm() {
  document.getElementById('checkin-form-section')?.scrollIntoView({ behavior: 'smooth' })
}

function scrollToCalendar() {
  document.getElementById('calendar-section')?.scrollIntoView({ behavior: 'smooth' })
}

function scrollToTimeline() {
  document.getElementById('timeline-section')?.scrollIntoView({ behavior: 'smooth' })
}

function handleScrollEvent() {
  scrollToForm()
}

onMounted(() => {
  userStore.initFromStorage()
  window.addEventListener('scroll-to-checkin', handleScrollEvent)
})

onUnmounted(() => {
  window.removeEventListener('scroll-to-checkin', handleScrollEvent)
})
</script>

<style scoped>
.checkin-view {
  padding: 24px 0 60px 0;
}

.view-hero {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
  border-radius: 20px;
  padding: 48px 40px;
  margin-bottom: 32px;
  color: #fff;
  position: relative;
  overflow: hidden;
}

.view-hero::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -20%;
  width: 400px;
  height: 400px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 50%;
}

.view-hero::after {
  content: '';
  position: absolute;
  bottom: -30%;
  left: -10%;
  width: 300px;
  height: 300px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 50%;
}

.hero-content {
  position: relative;
  z-index: 1;
  max-width: 700px;
}

.hero-title-row {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.hero-title {
  font-size: 36px;
  font-weight: 800;
  margin: 0;
  background: linear-gradient(135deg, #fff 0%, #ffe8f4 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-badge {
  padding: 6px 16px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  backdrop-filter: blur(10px);
}

.hero-subtitle {
  font-size: 16px;
  margin: 0;
  opacity: 0.9;
  line-height: 1.7;
}

.section-row {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 24px;
  margin-bottom: 40px;
}

@media (max-width: 1024px) {
  .section-row {
    grid-template-columns: 1fr;
  }
}

.section-left {
  min-width: 0;
}

.section-right {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.quick-actions {
  padding: 24px;
  position: sticky;
  top: 90px;
}

@media (max-width: 1024px) {
  .quick-actions {
    position: static;
  }
}

.action-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 20px 0;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 24px;
}

.action-btn {
  width: 100%;
  padding: 14px 16px;
  font-size: 15px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border-radius: 12px;
  height: auto;
}

.primary-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 4px 14px rgba(102, 126, 234, 0.35);
}

.primary-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.45);
}

.tip-section {
  background: linear-gradient(135deg, #fffbeb 0%, #fef3c7 100%);
  border-radius: 12px;
  padding: 16px;
  border: 1px solid #fde68a;
}

.tip-title {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #b45309;
  font-weight: 600;
  font-size: 14px;
  margin-bottom: 12px;
}

.tip-list {
  margin: 0;
  padding-left: 18px;
  color: #92400e;
  font-size: 13px;
  line-height: 1.9;
}

.tip-list li {
  margin-bottom: 2px;
}

#calendar-section {
  margin-bottom: 40px;
}

.section-header {
  text-align: center;
  margin-bottom: 24px;
}

.section-title {
  font-size: 26px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 8px 0;
  display: inline-flex;
  align-items: center;
  gap: 10px;
}

.section-icon {
  font-size: 28px;
}

.section-desc {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

@media (max-width: 768px) {
  .view-hero {
    padding: 32px 24px;
  }

  .hero-title {
    font-size: 26px;
  }

  .hero-subtitle {
    font-size: 14px;
  }
}
</style>

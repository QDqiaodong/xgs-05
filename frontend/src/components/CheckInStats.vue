<template>
  <div class="checkin-stats">
    <div class="stats-grid">
      <div class="stat-card streak-card">
        <div class="stat-icon">🔥</div>
        <div class="stat-content">
          <div class="stat-number">{{ stats.currentStreak }}</div>
          <div class="stat-label">连续打卡天数</div>
        </div>
        <div class="stat-flame" v-if="stats.currentStreak >= 7">
          <el-tag type="danger" effect="dark" size="small" round>
            连续{{ stats.currentStreak }}天达人
          </el-tag>
        </div>
      </div>

      <div class="stat-card total-card">
        <div class="stat-icon">📅</div>
        <div class="stat-content">
          <div class="stat-number">{{ stats.totalDays }}</div>
          <div class="stat-label">累计打卡天数</div>
        </div>
      </div>

      <div class="stat-card duration-card">
        <div class="stat-icon">⏱️</div>
        <div class="stat-content">
          <div class="stat-number duration-number">{{ formatDuration(stats.totalDurationMinutes) }}</div>
          <div class="stat-label">累计创作时长</div>
        </div>
      </div>

      <div class="stat-card longest-card">
        <div class="stat-icon">🏆</div>
        <div class="stat-content">
          <div class="stat-number">{{ stats.longestStreak }}</div>
          <div class="stat-label">最长连续天数</div>
        </div>
      </div>

      <div class="stat-card month-card">
        <div class="stat-icon">📊</div>
        <div class="stat-content">
          <div class="stat-number">{{ stats.thisMonthDays }}</div>
          <div class="stat-label">本月打卡天数</div>
        </div>
        <div class="month-progress" v-if="daysInMonth > 0">
          <el-progress
            :percentage="Math.round((stats.thisMonthDays / daysInMonth) * 100)"
            :color="progressColor"
            :show-text="false"
            :stroke-width="6"
          />
          <span class="progress-text">{{ stats.thisMonthDays }}/{{ daysInMonth }}天</span>
        </div>
      </div>

      <div class="stat-card works-card">
        <div class="stat-icon">🎨</div>
        <div class="stat-content">
          <div class="stat-number">{{ stats.totalWorks }}</div>
          <div class="stat-label">关联作品数</div>
        </div>
      </div>
    </div>

    <div class="motivation-section" v-if="showMotivation">
      <el-alert
        :title="motivationText"
        type="success"
        :closable="false"
        show-icon
        class="motivation-alert"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'

const props = defineProps({
  userId: {
    type: Number,
    default: null
  }
})

const userStore = useUserStore()
const loading = ref(false)
const stats = reactive({
  totalDays: 0,
  currentStreak: 0,
  longestStreak: 0,
  totalDurationMinutes: 0,
  totalWorks: 0,
  thisMonthDays: 0
})

const now = new Date()
const daysInMonth = computed(() => new Date(now.getFullYear(), now.getMonth() + 1, 0).getDate())

const progressColor = computed(() => {
  const p = daysInMonth.value > 0 ? (stats.thisMonthDays / daysInMonth.value) : 0
  if (p >= 0.8) return '#67c23a'
  if (p >= 0.5) return '#409eff'
  if (p >= 0.3) return '#e6a23c'
  return '#909399'
})

const showMotivation = computed(() => {
  return stats.currentStreak >= 3 || stats.thisMonthDays >= 10
})

const motivationText = computed(() => {
  if (stats.currentStreak >= 30) {
    return `🎉 太厉害了！已经连续创作${stats.currentStreak}天，坚持就是最大的胜利！`
  } else if (stats.currentStreak >= 14) {
    return `💪 连续${stats.currentStreak}天，创作的习惯已经养成，继续加油！`
  } else if (stats.currentStreak >= 7) {
    return `🔥 一周达成！连续${stats.currentStreak}天的坚持，作品会见证你的成长！`
  } else if (stats.currentStreak >= 3) {
    return `✨ 连续${stats.currentStreak}天了，继续保持这个节奏！`
  } else if (stats.thisMonthDays >= 20) {
    return `🌟 本月已打卡${stats.thisMonthDays}天，创作节奏非常棒！`
  } else if (stats.thisMonthDays >= 10) {
    return `🌱 本月已打卡${stats.thisMonthDays}天，循序渐进，未来可期！`
  }
  return ''
})

function formatDuration(minutes) {
  if (!minutes) return '0分钟'
  if (minutes < 60) return `${minutes}分钟`
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  if (h < 24) {
    return m > 0 ? `${h}h${m}m` : `${h}小时`
  }
  const days = Math.floor(h / 24)
  const remainH = h % 24
  return remainH > 0 ? `${days}天${remainH}h` : `${days}天`
}

async function loadStats() {
  if (!userStore.isLoggedIn) return
  loading.value = true
  try {
    const res = await request.get('/checkin/stats', {
      params: props.userId ? { userId: props.userId } : {}
    })
    if (res.code === 200 && res.data) {
      Object.assign(stats, res.data)
    }
  } catch (e) {
    console.warn('加载统计数据失败', e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  userStore.initFromStorage()
  loadStats()
})

defineExpose({ loadStats })
</script>

<style scoped>
.checkin-stats {
  margin-bottom: 24px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

@media (max-width: 900px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 600px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}

.stat-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  display: flex;
  align-items: center;
  gap: 16px;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  border-radius: 4px 4px 0 0;
}

.streak-card::before {
  background: linear-gradient(90deg, #f093fb 0%, #f5576c 100%);
}

.total-card::before {
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
}

.duration-card::before {
  background: linear-gradient(90deg, #4facfe 0%, #00f2fe 100%);
}

.longest-card::before {
  background: linear-gradient(90deg, #fa709a 0%, #fee140 100%);
}

.month-card::before {
  background: linear-gradient(90deg, #30cfd0 0%, #330867 100%);
}

.works-card::before {
  background: linear-gradient(90deg, #a8edea 0%, #fed6e3 100%);
}

.stat-icon {
  font-size: 36px;
  flex-shrink: 0;
}

.stat-content {
  flex: 1;
  min-width: 0;
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
  margin-bottom: 4px;
}

.duration-number {
  font-size: 24px;
}

.stat-label {
  font-size: 13px;
  color: #909399;
}

.stat-flame {
  position: absolute;
  top: 12px;
  right: 12px;
}

.month-progress {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 8px;
  min-width: 100px;
}

.month-progress :deep(.el-progress) {
  flex: 1;
}

.progress-text {
  font-size: 12px;
  color: #606266;
  white-space: nowrap;
}

.motivation-alert {
  border-radius: 12px;
  border: none;
  background: linear-gradient(135deg, #f0fdf4 0%, #dcfce7 100%);
}

.motivation-alert :deep(.el-alert__title) {
  color: #166534;
  font-weight: 500;
}
</style>

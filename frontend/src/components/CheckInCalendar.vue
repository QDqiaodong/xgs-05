<template>
  <div class="calendar-wall card">
    <div class="calendar-header">
      <div class="month-nav">
        <el-button circle :icon="ArrowLeft" @click="prevMonth" />
        <h3 class="month-title">{{ currentYear }}年{{ currentMonth }}月</h3>
        <el-button circle :icon="ArrowRight" @click="nextMonth" />
      </div>
      <div class="legend">
        <div class="legend-item">
          <span class="legend-dot checked"></span>
          <span>已打卡</span>
        </div>
        <div class="legend-item">
          <span class="legend-dot today"></span>
          <span>今天</span>
        </div>
        <div class="legend-item">
          <span class="legend-dot future"></span>
          <span>未来</span>
        </div>
      </div>
    </div>

    <div class="weekdays">
      <div v-for="w in weekdays" :key="w" class="weekday-item">{{ w }}</div>
    </div>

    <div class="calendar-grid">
      <div
        v-for="(day, idx) in calendarCells"
        :key="idx"
        class="calendar-cell"
        :class="{
          'cell-empty': !day.date,
          'cell-today': day.isToday,
          'cell-checked': day.checkedIn,
          'cell-future': day.isFuture
        }"
        @click="handleCellClick(day)"
      >
        <template v-if="day.date">
          <div class="cell-date">{{ day.day }}</div>
          <div v-if="day.checkedIn" class="cell-content">
            <div class="cell-photo" v-if="getFirstImage(day.images)">
              <img :src="getFirstImage(day.images)" alt="" class="photo-img" />
              <div class="photo-overlay">
                <span class="duration-badge" v-if="day.workDuration">
                  ⏱ {{ formatDuration(day.workDuration) }}
                </span>
              </div>
            </div>
            <div v-else class="cell-check-icon">✓</div>
            <div class="cell-mood" v-if="day.moodTag" :title="day.moodTag">
              {{ getMoodEmoji(day.moodTag) }}
            </div>
          </div>
        </template>
      </div>
    </div>

    <el-dialog v-model="detailVisible" title="打卡详情" width="560px" class="checkin-detail-dialog">
      <div v-if="selectedDay && selectedDay.checkedIn" class="detail-content">
        <div class="detail-header">
          <div class="detail-date-badge">
            <el-icon><Calendar /></el-icon>
            <span>{{ selectedDay.date }}</span>
          </div>
          <div class="detail-tags" v-if="selectedDay.moodTag || selectedDay.weatherTag">
            <el-tag v-if="selectedDay.moodTag" size="small" type="primary" effect="light">{{ selectedDay.moodTag }}</el-tag>
            <el-tag v-if="selectedDay.weatherTag" size="small" type="success" effect="light">{{ selectedDay.weatherTag }}</el-tag>
          </div>
        </div>
        <h4 class="detail-title">{{ selectedDay.title || '（无标题）' }}</h4>
        <div class="detail-duration" v-if="selectedDay.workDuration">
          <el-icon><Timer /></el-icon>
          <span>今日创作时长：{{ formatDuration(selectedDay.workDuration) }}</span>
        </div>
        <div class="detail-images" v-if="parseImages(selectedDay.images).length > 0">
          <div class="detail-images-label">进度照片</div>
          <div class="detail-images-grid">
            <img
              v-for="(img, i) in parseImages(selectedDay.images)"
              :key="i"
              :src="img"
              class="detail-image"
              @click="previewImage(img)"
            />
          </div>
        </div>
        <div class="detail-content-text" v-if="selectedDay.content">
          <div class="detail-label">创作心得</div>
          <p class="detail-paragraph">{{ selectedDay.content }}</p>
        </div>
      </div>
      <div v-else class="detail-empty">
        <el-empty description="这天还没有打卡记录" />
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="primary" v-if="selectedDay && selectedDay.isToday" @click="goToCheckIn">
          {{ selectedDay.checkedIn ? '更新打卡' : '去打卡' }}
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="imagePreviewVisible" title="图片预览" width="80%">
      <img w-full :src="previewImageUrl" alt="" style="width: 100%;" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ArrowLeft, ArrowRight, Calendar, Timer } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()

const weekdays = ['日', '一', '二', '三', '四', '五', '六']
const now = new Date()
const currentYear = ref(now.getFullYear())
const currentMonth = ref(now.getMonth() + 1)
const calendarData = ref([])
const loading = ref(false)
const detailVisible = ref(false)
const selectedDay = ref(null)
const imagePreviewVisible = ref(false)
const previewImageUrl = ref('')

const calendarCells = computed(() => {
  const cells = []
  const firstDay = new Date(currentYear.value, currentMonth.value - 1, 1)
  const lastDay = new Date(currentYear.value, currentMonth.value, 0)
  const startWeekday = firstDay.getDay()
  const daysInMonth = lastDay.getDate()

  for (let i = 0; i < startWeekday; i++) {
    cells.push({ date: null })
  }

  const dataMap = {}
  calendarData.value.forEach(d => {
    dataMap[d.date] = d
  })

  for (let day = 1; day <= daysInMonth; day++) {
    const dateStr = `${currentYear.value}-${String(currentMonth.value).padStart(2, '0')}-${String(day).padStart(2, '0')}`
    const today = new Date()
    const todayStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`
    const d = new Date(dateStr)
    const todayDate = new Date(todayStr)

    const cell = {
      date: dateStr,
      day: day,
      isToday: dateStr === todayStr,
      isFuture: d > todayDate,
      checkedIn: false
    }

    if (dataMap[dateStr]) {
      Object.assign(cell, dataMap[dateStr])
    }

    cells.push(cell)
  }

  const remaining = 7 - (cells.length % 7)
  if (remaining < 7) {
    for (let i = 0; i < remaining; i++) {
      cells.push({ date: null })
    }
  }

  return cells
})

async function loadCalendar() {
  if (!userStore.isLoggedIn) return
  loading.value = true
  try {
    const res = await request.get('/checkin/calendar', {
      params: {
        year: currentYear.value,
        month: currentMonth.value
      }
    })
    if (res.code === 200 && res.data) {
      calendarData.value = res.data
    }
  } catch (e) {
    console.warn('加载日历失败', e)
  } finally {
    loading.value = false
  }
}

function prevMonth() {
  if (currentMonth.value === 1) {
    currentMonth.value = 12
    currentYear.value--
  } else {
    currentMonth.value--
  }
}

function nextMonth() {
  if (currentMonth.value === 12) {
    currentMonth.value = 1
    currentYear.value++
  } else {
    currentMonth.value++
  }
}

function handleCellClick(day) {
  if (!day.date) return
  selectedDay.value = day
  detailVisible.value = true
}

function parseImages(imagesStr) {
  if (!imagesStr) return []
  try {
    return JSON.parse(imagesStr) || []
  } catch (e) {
    return []
  }
}

function getFirstImage(imagesStr) {
  const imgs = parseImages(imagesStr)
  return imgs.length > 0 ? imgs[0] : null
}

function formatDuration(minutes) {
  if (!minutes) return '0分钟'
  if (minutes < 60) return `${minutes}分钟`
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  return m > 0 ? `${h}小时${m}分钟` : `${h}小时`
}

function getMoodEmoji(moodTag) {
  if (!moodTag) return ''
  const match = moodTag.match(/[\u{1F300}-\u{1F9FF}\u{2600}-\u{27BF}]/u)
  return match ? match[0] : '📝'
}

function previewImage(url) {
  previewImageUrl.value = url
  imagePreviewVisible.value = true
}

function goToCheckIn() {
  detailVisible.value = false
  const event = new CustomEvent('scroll-to-checkin')
  window.dispatchEvent(event)
}

watch([currentYear, currentMonth], () => {
  loadCalendar()
})

onMounted(() => {
  userStore.initFromStorage()
  loadCalendar()
})

defineExpose({ loadCalendar })
</script>

<style scoped>
.calendar-wall {
  padding: 28px 32px;
}

.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.month-nav {
  display: flex;
  align-items: center;
  gap: 16px;
}

.month-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0;
  min-width: 140px;
  text-align: center;
}

.legend {
  display: flex;
  gap: 20px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #606266;
}

.legend-dot {
  width: 14px;
  height: 14px;
  border-radius: 4px;
  display: inline-block;
}

.legend-dot.checked {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.legend-dot.today {
  background: #f56c6c;
  border: 2px solid #f56c6c;
}

.legend-dot.future {
  background: #f5f7fa;
  border: 1px dashed #dcdfe6;
}

.weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 8px;
  margin-bottom: 12px;
}

.weekday-item {
  text-align: center;
  font-size: 14px;
  font-weight: 600;
  color: #909399;
  padding: 8px 0;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 8px;
}

.calendar-cell {
  aspect-ratio: 1;
  border-radius: 12px;
  background: #fafafa;
  border: 2px solid transparent;
  padding: 8px;
  cursor: pointer;
  transition: all 0.25s ease;
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  min-height: 90px;
}

.calendar-cell:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.cell-empty {
  background: transparent;
  cursor: default;
}

.cell-empty:hover {
  transform: none;
  box-shadow: none;
}

.cell-today {
  border-color: #f56c6c;
  background: #fff5f5;
}

.cell-future {
  background: #fafafa;
  border: 2px dashed #ebeef5;
  cursor: default;
}

.cell-future:hover {
  transform: none;
  box-shadow: none;
}

.cell-checked {
  background: linear-gradient(135deg, #667eea22 0%, #764ba222 100%);
  border-color: #667eea55;
}

.cell-checked.cell-today {
  border-color: #f56c6c;
  background: linear-gradient(135deg, #fff5f5 0%, #ffe8e8 100%);
}

.cell-date {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.cell-future .cell-date {
  color: #c0c4cc;
}

.cell-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 4px;
}

.cell-photo {
  width: 100%;
  height: 44px;
  border-radius: 6px;
  overflow: hidden;
  position: relative;
  background: #f0f0f0;
}

.photo-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.photo-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.6));
  padding: 4px;
}

.duration-badge {
  color: #fff;
  font-size: 10px;
  font-weight: 500;
}

.cell-check-icon {
  width: 28px;
  height: 28px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: bold;
}

.cell-mood {
  font-size: 14px;
}

.detail-content {
  padding: 4px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}

.detail-date-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
}

.detail-tags {
  display: flex;
  gap: 8px;
}

.detail-title {
  font-size: 18px;
  color: #303133;
  margin: 0 0 12px 0;
  font-weight: 600;
}

.detail-duration {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #e6a23c;
  font-weight: 500;
  margin-bottom: 16px;
  padding: 10px 14px;
  background: #fdf6ec;
  border-radius: 8px;
}

.detail-images {
  margin-bottom: 16px;
}

.detail-images-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 10px;
  font-weight: 500;
}

.detail-images-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

.detail-image {
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.2s;
}

.detail-image:hover {
  transform: scale(1.02);
}

.detail-content-text {
  background: #fafbfc;
  padding: 16px;
  border-radius: 10px;
}

.detail-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
  font-weight: 500;
}

.detail-paragraph {
  margin: 0;
  color: #606266;
  line-height: 1.7;
  white-space: pre-wrap;
}

.detail-empty {
  padding: 40px 0;
}

:deep(.checkin-detail-dialog .el-dialog__body) {
  max-height: 70vh;
  overflow-y: auto;
}
</style>

<template>
  <div
    class="canvas-card"
    :class="{ 'is-selected': isSelected, 'is-dragging': isDragging, 'is-resizing': isResizing }"
    :style="cardStyle"
    @mousedown.stop="onMouseDown"
  >
    <div class="card-header" @mousedown.stop="onMouseDown">
      <div class="card-title">{{ item.work?.title || '作品' }}</div>
      <div class="card-actions">
        <el-button
          type="text"
          size="small"
          @click.stop="onDelete"
          class="action-btn"
        >
          <el-icon><Close /></el-icon>
        </el-button>
      </div>
    </div>
    <div class="card-image">
      <img
        :src="coverImageSmall"
        :alt="item.work?.title"
        draggable="false"
        @load="onImgLoad"
      />
    </div>
    <div class="card-info">
      <div class="card-desc">{{ item.work?.description }}</div>
      <div class="card-author">
        <img v-if="item.work?.authorAvatar" :src="item.work.authorAvatar" alt="" />
        <span>{{ item.work?.authorName }}</span>
      </div>
    </div>
    <div
      v-if="isSelected"
      class="resize-handle resize-br"
      @mousedown.stop="onResizeStart"
    ></div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Close } from '@element-plus/icons-vue'
import { useInspirationStore } from '@/store/inspiration'
import { getSmallImage } from '@/utils/image'

const props = defineProps({
  item: {
    type: Object,
    required: true
  },
  isSelected: {
    type: Boolean,
    default: false
  },
  canvasRef: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['select'])

const store = useInspirationStore()
const router = useRouter()

const isDragging = ref(false)
const isResizing = ref(false)
const dragStart = ref({ x: 0, y: 0, itemX: 0, itemY: 0 })
const resizeStart = ref({ x: 0, y: 0, width: 0, height: 0 })

const MIN_WIDTH = 160
const MIN_HEIGHT = 200

const cardStyle = computed(() => ({
  left: `${props.item.x}px`,
  top: `${props.item.y}px`,
  width: `${props.item.width}px`,
  height: `${props.item.height}px`,
  zIndex: props.isSelected ? 100 : 1
}))

const coverImageSmall = computed(() => {
  return getSmallImage(props.item.work?.coverImage)
})

function onMouseDown(e) {
  if (e.button !== 0) return
  emit('select', props.item.id)
  store.bringToFront(props.item.id)
  isDragging.value = true
  dragStart.value = {
    x: e.clientX,
    y: e.clientY,
    itemX: props.item.x,
    itemY: props.item.y
  }
  window.addEventListener('mousemove', onMouseMove)
  window.addEventListener('mouseup', onMouseUp)
}

function onMouseMove(e) {
  if (!isDragging.value) return
  const dx = e.clientX - dragStart.value.x
  const dy = e.clientY - dragStart.value.y
  let newX = dragStart.value.itemX + dx
  let newY = dragStart.value.itemY + dy
  newX = Math.max(0, newX)
  newY = Math.max(0, newY)
  store.updateItem(props.item.id, { x: newX, y: newY })
}

function onMouseUp() {
  isDragging.value = false
  window.removeEventListener('mousemove', onMouseMove)
  window.removeEventListener('mouseup', onMouseUp)
}

function onResizeStart(e) {
  isResizing.value = true
  resizeStart.value = {
    x: e.clientX,
    y: e.clientY,
    width: props.item.width,
    height: props.item.height
  }
  window.addEventListener('mousemove', onResizeMove)
  window.addEventListener('mouseup', onResizeEnd)
}

function onResizeMove(e) {
  if (!isResizing.value) return
  const dx = e.clientX - resizeStart.value.x
  const dy = e.clientY - resizeStart.value.y
  const newWidth = Math.max(MIN_WIDTH, resizeStart.value.width + dx)
  const newHeight = Math.max(MIN_HEIGHT, resizeStart.value.height + dy)
  store.updateItem(props.item.id, { width: newWidth, height: newHeight })
}

function onResizeEnd() {
  isResizing.value = false
  window.removeEventListener('mousemove', onResizeMove)
  window.removeEventListener('mouseup', onResizeEnd)
}

function onDelete() {
  store.deleteItem(props.item.id)
}

function onImgLoad() {
}

onUnmounted(() => {
  window.removeEventListener('mousemove', onMouseMove)
  window.removeEventListener('mouseup', onMouseUp)
  window.removeEventListener('mousemove', onResizeMove)
  window.removeEventListener('mouseup', onResizeEnd)
})
</script>

<style scoped>
.canvas-card {
  position: absolute;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  cursor: move;
  user-select: none;
  display: flex;
  flex-direction: column;
  transition: box-shadow 0.2s;
}

.canvas-card:hover {
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.15);
}

.canvas-card.is-selected {
  box-shadow: 0 0 0 2px #667eea, 0 6px 24px rgba(102, 126, 234, 0.3);
}

.canvas-card.is-dragging {
  cursor: grabbing;
  opacity: 0.9;
}

.canvas-card.is-resizing {
  cursor: default;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  cursor: move;
  flex-shrink: 0;
}

.card-title {
  font-size: 13px;
  font-weight: 500;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-actions {
  display: flex;
  gap: 4px;
}

.action-btn {
  color: #fff;
  padding: 2px;
}

.action-btn:hover {
  color: #f56c6c;
}

.card-image {
  flex: 1;
  overflow: hidden;
  background: #f5f5f5;
  min-height: 100px;
}

.card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.card-info {
  padding: 10px 12px;
  flex-shrink: 0;
  background: #fafafa;
}

.card-desc {
  font-size: 12px;
  color: #666;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 6px;
}

.card-author {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  color: #999;
}

.card-author img {
  width: 18px;
  height: 18px;
  border-radius: 50%;
}

.resize-handle {
  position: absolute;
  background: #667eea;
  border: 2px solid #fff;
  border-radius: 50%;
}

.resize-handle.resize-br {
  right: -6px;
  bottom: -6px;
  width: 14px;
  height: 14px;
  cursor: nwse-resize;
}
</style>

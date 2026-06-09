<template>
  <Teleport to="body">
    <Transition name="viewer-fade">
      <div v-if="visible" class="image-viewer" @click.self="close">
        <div class="viewer-header">
          <div class="viewer-counter">
            <span class="current">{{ currentIndex + 1 }}</span>
            <span class="divider">/</span>
            <span class="total">{{ images.length }}</span>
          </div>
          <button class="viewer-close" @click="close" aria-label="关闭">
            <el-icon :size="24"><Close /></el-icon>
          </button>
        </div>

        <div
          ref="stageRef"
          class="viewer-stage"
          @touchstart="onTouchStart"
          @touchmove="onTouchMove"
          @touchend="onTouchEnd"
          @wheel.prevent="onWheel"
          @mousedown="onMouseDown"
          @mousemove="onMouseMove"
          @mouseup="onMouseUp"
          @mouseleave="onMouseUp"
        >
          <div
            class="viewer-image-wrapper"
            :style="{
              transform: `translate3d(${translateX}px, ${translateY}px, 0) scale(${scale})`,
              transition: isAnimating ? 'transform 0.3s ease' : 'none'
            }"
          >
            <img
              v-if="!viewerImageError"
              :src="images[currentIndex]"
              alt="作品图片"
              class="viewer-image"
              draggable="false"
              @load="onImageLoad"
              @error="onViewerImageError"
            />
            <div v-else class="viewer-image-error">
              <el-icon :size="80" color="#888"><Warning /></el-icon>
              <p>图片加载失败</p>
            </div>
          </div>
        </div>

        <button
          v-if="currentIndex > 0"
          class="viewer-nav viewer-nav-prev"
          @click.stop="prevImage"
          aria-label="上一张"
        >
          <el-icon :size="32"><ArrowLeft /></el-icon>
        </button>
        <button
          v-if="currentIndex < images.length - 1"
          class="viewer-nav viewer-nav-next"
          @click.stop="nextImage"
          aria-label="下一张"
        >
          <el-icon :size="32"><ArrowRight /></el-icon>
        </button>

        <div class="viewer-footer">
          <div class="viewer-thumbnails">
            <div
              v-for="(img, index) in images"
              :key="index"
              class="viewer-thumb"
              :class="{ active: index === currentIndex }"
              @click.stop="goToImage(index)"
            >
              <img v-if="!viewerThumbErrors[index]" :src="img" :alt="'缩略图' + (index + 1)" @error="handleViewerThumbError(index)" />
              <div v-else class="viewer-thumb-error">
                <el-icon :size="20" color="#888"><Warning /></el-icon>
              </div>
            </div>
          </div>
          <div class="viewer-actions">
            <button class="action-btn" @click.stop="zoomOut" aria-label="缩小">
              <el-icon :size="20"><ZoomOut /></el-icon>
            </button>
            <span class="scale-text">{{ Math.round(scale * 100) }}%</span>
            <button class="action-btn" @click.stop="zoomIn" aria-label="放大">
              <el-icon :size="20"><ZoomIn /></el-icon>
            </button>
            <button class="action-btn" @click.stop="resetView" aria-label="重置">
              <el-icon :size="20"><RefreshRight /></el-icon>
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { Close, ArrowLeft, ArrowRight, ZoomIn, ZoomOut, RefreshRight } from '@element-plus/icons-vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  images: {
    type: Array,
    default: () => []
  },
  initialIndex: {
    type: Number,
    default: 0
  }
})

const emit = defineEmits(['close', 'change'])

const stageRef = ref(null)
const currentIndex = ref(props.initialIndex)
const scale = ref(1)
const translateX = ref(0)
const translateY = ref(0)
const isAnimating = ref(false)
const viewerImageError = ref(false)
const viewerThumbErrors = ref({})

const MIN_SCALE = 0.5
const MAX_SCALE = 4
const SCALE_STEP = 0.25

const touchState = ref({
  startX: 0,
  startY: 0,
  startTranslateX: 0,
  startTranslateY: 0,
  startDistance: 0,
  startScale: 1,
  isDragging: false,
  isPinching: false,
  touchCount: 0
})

const mouseState = ref({
  startX: 0,
  startY: 0,
  startTranslateX: 0,
  startTranslateY: 0,
  isDragging: false
})

function getDistance(touches) {
  const dx = touches[0].clientX - touches[1].clientX
  const dy = touches[0].clientY - touches[1].clientY
  return Math.sqrt(dx * dx + dy * dy)
}

function clampScale(value) {
  return Math.min(MAX_SCALE, Math.max(MIN_SCALE, value))
}

function resetTransform() {
  scale.value = 1
  translateX.value = 0
  translateY.value = 0
}

function goToImage(index) {
  if (index < 0 || index >= props.images.length || index === currentIndex.value) return
  isAnimating.value = true
  currentIndex.value = index
  resetTransform()
  viewerImageError.value = false
  emit('change', index)
  setTimeout(() => {
    isAnimating.value = false
  }, 300)
}

function nextImage() {
  goToImage(currentIndex.value + 1)
}

function prevImage() {
  goToImage(currentIndex.value - 1)
}

function zoomIn() {
  isAnimating.value = true
  scale.value = clampScale(scale.value + SCALE_STEP)
  setTimeout(() => {
    isAnimating.value = false
  }, 300)
}

function zoomOut() {
  isAnimating.value = true
  scale.value = clampScale(scale.value - SCALE_STEP)
  if (scale.value <= 1) {
    translateX.value = 0
    translateY.value = 0
  }
  setTimeout(() => {
    isAnimating.value = false
  }, 300)
}

function resetView() {
  isAnimating.value = true
  resetTransform()
  setTimeout(() => {
    isAnimating.value = false
  }, 300)
}

function close() {
  emit('close')
}

function onTouchStart(e) {
  const touches = e.touches
  touchState.value.touchCount = touches.length

  if (touches.length === 1) {
    touchState.value.startX = touches[0].clientX
    touchState.value.startY = touches[0].clientY
    touchState.value.startTranslateX = translateX.value
    touchState.value.startTranslateY = translateY.value
    touchState.value.isDragging = true
    touchState.value.isPinching = false
  } else if (touches.length === 2) {
    touchState.value.startDistance = getDistance(touches)
    touchState.value.startScale = scale.value
    touchState.value.isPinching = true
    touchState.value.isDragging = false
  }
}

function onTouchMove(e) {
  const touches = e.touches

  if (touchState.value.isPinching && touches.length >= 2) {
    const distance = getDistance(touches)
    const ratio = distance / touchState.value.startDistance
    scale.value = clampScale(touchState.value.startScale * ratio)
    return
  }

  if (touchState.value.isDragging && touches.length === 1) {
    const deltaX = touches[0].clientX - touchState.value.startX
    const deltaY = touches[0].clientY - touchState.value.startY

    if (scale.value > 1) {
      translateX.value = touchState.value.startTranslateX + deltaX
      translateY.value = touchState.value.startTranslateY + deltaY
    } else if (Math.abs(deltaX) > Math.abs(deltaY)) {
      translateX.value = deltaX
    }
  }
}

function onTouchEnd(e) {
  const wasPinching = touchState.value.isPinching
  const wasDragging = touchState.value.isDragging
  const lastTranslateX = translateX.value
  const lastTranslateY = translateY.value

  touchState.value.isDragging = false
  touchState.value.isPinching = false
  touchState.value.touchCount = e.touches.length

  if (wasPinching) {
    if (scale.value <= 1) {
      isAnimating.value = true
      resetTransform()
      setTimeout(() => {
        isAnimating.value = false
      }, 300)
    }
    return
  }

  if (wasDragging && scale.value <= 1) {
    const threshold = 80
    if (lastTranslateX > threshold && currentIndex.value > 0) {
      prevImage()
    } else if (lastTranslateX < -threshold && currentIndex.value < props.images.length - 1) {
      nextImage()
    } else {
      isAnimating.value = true
      translateX.value = 0
      translateY.value = 0
      setTimeout(() => {
        isAnimating.value = false
      }, 300)
    }
  }
}

function onWheel(e) {
  const delta = e.deltaY > 0 ? -SCALE_STEP : SCALE_STEP
  const newScale = clampScale(scale.value + delta)
  isAnimating.value = true
  scale.value = newScale
  if (newScale <= 1) {
    translateX.value = 0
    translateY.value = 0
  }
  setTimeout(() => {
    isAnimating.value = false
  }, 150)
}

function onMouseDown(e) {
  if (e.button !== 0) return
  mouseState.value.startX = e.clientX
  mouseState.value.startY = e.clientY
  mouseState.value.startTranslateX = translateX.value
  mouseState.value.startTranslateY = translateY.value
  mouseState.value.isDragging = true
}

function onMouseMove(e) {
  if (!mouseState.value.isDragging || scale.value <= 1) return
  const deltaX = e.clientX - mouseState.value.startX
  const deltaY = e.clientY - mouseState.value.startY
  translateX.value = mouseState.value.startTranslateX + deltaX
  translateY.value = mouseState.value.startTranslateY + deltaY
}

function onMouseUp() {
  mouseState.value.isDragging = false
}

function onKeyDown(e) {
  if (!props.visible) return
  switch (e.key) {
    case 'Escape':
      close()
      break
    case 'ArrowLeft':
      prevImage()
      break
    case 'ArrowRight':
      nextImage()
      break
    case '+':
    case '=':
      zoomIn()
      break
    case '-':
    case '_':
      zoomOut()
      break
    case '0':
      resetView()
      break
  }
}

function onImageLoad() {
  viewerImageError.value = false
  resetTransform()
}

function onViewerImageError() {
  viewerImageError.value = true
}

function handleViewerThumbError(index) {
  viewerThumbErrors.value = { ...viewerThumbErrors.value, [index]: true }
}

watch(() => props.visible, (val) => {
  if (val) {
    currentIndex.value = props.initialIndex
    resetTransform()
    viewerImageError.value = false
    viewerThumbErrors.value = {}
    document.body.style.overflow = 'hidden'
  } else {
    document.body.style.overflow = ''
  }
})

watch(() => props.initialIndex, (val) => {
  if (props.visible) {
    currentIndex.value = val
    resetTransform()
  }
})

onMounted(() => {
  window.addEventListener('keydown', onKeyDown)
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', onKeyDown)
  document.body.style.overflow = ''
})
</script>

<style scoped>
.image-viewer {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.95);
  z-index: 9999;
  display: flex;
  flex-direction: column;
  user-select: none;
  -webkit-user-select: none;
}

.viewer-header {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  z-index: 10;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0.6) 0%, transparent 100%);
}

.viewer-counter {
  color: #fff;
  font-size: 18px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 4px;
}

.viewer-counter .current {
  font-size: 24px;
  font-weight: 600;
  color: #667eea;
}

.viewer-counter .divider {
  color: rgba(255, 255, 255, 0.5);
  margin: 0 2px;
}

.viewer-counter .total {
  color: rgba(255, 255, 255, 0.7);
}

.viewer-close {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  border: none;
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.viewer-close:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: rotate(90deg);
}

.viewer-stage {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  cursor: grab;
  touch-action: none;
}

.viewer-stage:active {
  cursor: grabbing;
}

.viewer-image-wrapper {
  max-width: 90vw;
  max-height: 90vh;
  will-change: transform;
  transform-origin: center center;
}

.viewer-image {
  max-width: 90vw;
  max-height: 80vh;
  object-fit: contain;
  display: block;
  pointer-events: none;
  -webkit-user-drag: none;
}

.viewer-image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  color: #aaa;
  min-width: 300px;
  min-height: 300px;
}

.viewer-image-error p {
  margin: 0;
  font-size: 16px;
}

.viewer-thumb-error {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.05);
}

.viewer-nav {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  border: none;
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
  z-index: 10;
}

.viewer-nav:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-50%) scale(1.1);
}

.viewer-nav-prev {
  left: 24px;
}

.viewer-nav-next {
  right: 24px;
}

.viewer-footer {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  background: linear-gradient(0deg, rgba(0, 0, 0, 0.6) 0%, transparent 100%);
  z-index: 10;
}

.viewer-thumbnails {
  display: flex;
  gap: 10px;
  overflow-x: auto;
  max-width: 60%;
  padding: 4px 0;
  scrollbar-width: thin;
  scrollbar-color: rgba(255, 255, 255, 0.3) transparent;
}

.viewer-thumbnails::-webkit-scrollbar {
  height: 4px;
}

.viewer-thumbnails::-webkit-scrollbar-track {
  background: transparent;
}

.viewer-thumbnails::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.3);
  border-radius: 2px;
}

.viewer-thumb {
  flex-shrink: 0;
  width: 64px;
  height: 64px;
  border-radius: 6px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
  opacity: 0.6;
  transition: all 0.3s;
}

.viewer-thumb:hover {
  opacity: 0.9;
}

.viewer-thumb.active {
  border-color: #667eea;
  opacity: 1;
}

.viewer-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.viewer-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  background: rgba(255, 255, 255, 0.08);
  padding: 8px 16px;
  border-radius: 28px;
  backdrop-filter: blur(10px);
}

.action-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: transparent;
  border: none;
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.action-btn:hover {
  background: rgba(255, 255, 255, 0.15);
}

.scale-text {
  color: #fff;
  font-size: 13px;
  font-weight: 500;
  min-width: 48px;
  text-align: center;
}

.viewer-fade-enter-active,
.viewer-fade-leave-active {
  transition: opacity 0.3s ease;
}

.viewer-fade-enter-from,
.viewer-fade-leave-to {
  opacity: 0;
}

@media (max-width: 768px) {
  .viewer-header {
    height: 52px;
    padding: 0 16px;
  }

  .viewer-counter {
    font-size: 16px;
  }

  .viewer-counter .current {
    font-size: 20px;
  }

  .viewer-close {
    width: 40px;
    height: 40px;
  }

  .viewer-nav {
    width: 44px;
    height: 44px;
  }

  .viewer-nav-prev {
    left: 12px;
  }

  .viewer-nav-next {
    right: 12px;
  }

  .viewer-footer {
    flex-direction: column;
    padding: 16px;
    gap: 16px;
  }

  .viewer-thumbnails {
    max-width: 100%;
    justify-content: center;
  }

  .viewer-thumb {
    width: 52px;
    height: 52px;
  }

  .viewer-actions {
    padding: 6px 12px;
  }

  .action-btn {
    width: 32px;
    height: 32px;
  }
}
</style>

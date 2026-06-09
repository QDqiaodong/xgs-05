<template>
  <div
    class="image-magnifier-container"
    @mouseenter="handleMouseEnter"
    @mouseleave="handleMouseLeave"
    @mousemove="handleMouseMove"
    ref="containerRef"
  >
    <div class="main-image-wrapper" ref="wrapperRef">
      <img
        :src="imageUrl"
        :alt="alt"
        class="main-image"
        ref="imageRef"
        @load="onImageLoad"
        draggable="false"
      />
      <div
        v-if="isHovering"
        class="lens"
        :style="lensStyle"
      ></div>
      <div class="magnifier-hint" :class="{ hidden: isHovering }">
        <el-icon><ZoomIn /></el-icon>
        <span>悬停查看细节</span>
      </div>
    </div>
  </div>

  <Teleport to="body">
    <Transition name="magnifier-fade">
      <div
        v-if="isHovering && showMagnifier"
        class="magnified-view"
        :style="magnifiedViewStyle"
      >
        <div class="magnifier-toolbar">
          <div class="magnification-buttons">
            <button
              v-for="(m, idx) in magnifications"
              :key="idx"
              class="mag-btn"
              :class="{ active: magnification === m }"
              @click.stop="handleSetMagnification(m)"
            >
              {{ m }}x
            </button>
          </div>
        </div>
        <div class="magnifier-label">
          <el-icon><View /></el-icon>
          <span>{{ magnification }}倍放大</span>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ZoomIn, View } from '@element-plus/icons-vue'
import { useImageRect } from '@/composables/useImageRect'
import { useMagnifier } from '@/composables/useMagnifier'

const props = defineProps({
  imageUrl: {
    type: String,
    required: true
  },
  alt: {
    type: String,
    default: '图片'
  },
  magnifications: {
    type: Array,
    default: () => [2, 3, 4]
  },
  defaultMagnification: {
    type: Number,
    default: 2
  },
  lensSize: {
    type: Number,
    default: 120
  },
  magnifierSize: {
    type: Number,
    default: 320
  }
})

const emit = defineEmits(['magnification-change'])

const containerRef = ref(null)
const imageRef = ref(null)
const wrapperRef = ref(null)

const { imgRect, updateImageRect } = useImageRect(imageRef, containerRef)

const {
  isHovering,
  isMouseOnImage,
  magnification,
  lensPosition,
  bgPosition,
  showMagnifier,
  onMouseEnter,
  onMouseLeave,
  onMouseMove,
  setMagnification
} = useMagnifier({
  lensSize: props.lensSize,
  magnifierSize: props.magnifierSize,
  defaultMagnification: props.defaultMagnification,
  imgRect,
  containerRef,
  emit
})

const lensStyle = computed(() => ({
  width: props.lensSize + 'px',
  height: props.lensSize + 'px',
  left: lensPosition.x + 'px',
  top: lensPosition.y + 'px',
  opacity: isMouseOnImage.value ? 1 : 0
}))

const magnifiedViewPosition = computed(() => {
  if (!wrapperRef.value) return { left: 0, top: 0 }
  const rect = wrapperRef.value.getBoundingClientRect()
  const gap = 24
  let left = rect.left - props.magnifierSize - gap
  let top = rect.top
  const viewportWidth = window.innerWidth
  if (left < 16) {
    left = rect.right + gap
  }
  if (left + props.magnifierSize > viewportWidth - 16) {
    left = Math.max(16, viewportWidth - props.magnifierSize - 16)
  }
  return { left, top }
})

const magnifiedViewStyle = computed(() => ({
  width: props.magnifierSize + 'px',
  height: props.magnifierSize + 'px',
  left: magnifiedViewPosition.value.left + 'px',
  top: magnifiedViewPosition.value.top + 'px',
  backgroundImage: `url(${props.imageUrl})`,
  backgroundSize: `${imgRect.width * magnification.value}px ${imgRect.height * magnification.value}px`,
  backgroundPosition: `${-bgPosition.x}px ${-bgPosition.y}px`
}))

function onImageLoad() {
  updateImageRect()
}

function handleMouseEnter() {
  updateImageRect()
  onMouseEnter()
}

function handleMouseLeave() {
  onMouseLeave()
}

function handleMouseMove(e) {
  onMouseMove(e)
}

function handleSetMagnification(m) {
  setMagnification(m)
}

watch(
  () => props.imageUrl,
  () => {
    updateImageRect()
  }
)
</script>

<style scoped>
.image-magnifier-container {
  position: relative;
  width: 100%;
  border-radius: 8px;
  overflow: hidden;
  cursor: crosshair;
}

.main-image-wrapper {
  position: relative;
  width: 100%;
}

.main-image {
  width: 100%;
  display: block;
  transition: transform 0.3s ease;
  user-select: none;
  -webkit-user-drag: none;
}

.image-magnifier-container:hover .main-image {
  transform: scale(1.01);
}

.lens {
  position: absolute;
  border: 2px solid #667eea;
  border-radius: 4px;
  background: rgba(102, 126, 234, 0.15);
  pointer-events: none;
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.5), 0 4px 20px rgba(0, 0, 0, 0.2);
  transition: opacity 0.2s ease;
  z-index: 2;
}

.lens::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: rgba(102, 126, 234, 0.5);
}

.lens::after {
  content: '';
  position: absolute;
  left: 50%;
  top: 0;
  bottom: 0;
  width: 1px;
  background: rgba(102, 126, 234, 0.5);
}

.magnified-view {
  position: fixed;
  border-radius: 8px;
  background-repeat: no-repeat;
  background-color: #fff;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2), 0 0 0 1px rgba(102, 126, 234, 0.3);
  z-index: 9999;
  overflow: hidden;
}

.magnifier-toolbar {
  position: absolute;
  top: 12px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 2;
}

.magnification-buttons {
  display: flex;
  gap: 6px;
  background: rgba(255, 255, 255, 0.95);
  padding: 6px;
  border-radius: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  backdrop-filter: blur(10px);
}

.mag-btn {
  padding: 6px 14px;
  border: none;
  background: transparent;
  border-radius: 18px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  color: #606266;
  transition: all 0.25s ease;
}

.mag-btn:hover {
  background: #f0f2ff;
  color: #667eea;
}

.mag-btn.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.4);
}

.magnifier-label {
  position: absolute;
  bottom: 12px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  border-radius: 20px;
  font-size: 12px;
  backdrop-filter: blur(10px);
  z-index: 2;
}

.magnifier-label .el-icon {
  font-size: 14px;
}

.magnifier-hint {
  position: absolute;
  bottom: 16px;
  right: 16px;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  border-radius: 20px;
  font-size: 13px;
  backdrop-filter: blur(10px);
  opacity: 1;
  transform: translateY(0);
  transition: all 0.3s ease;
  pointer-events: none;
  z-index: 3;
}

.magnifier-hint.hidden {
  opacity: 0;
  transform: translateY(10px);
}

.magnifier-fade-enter-active,
.magnifier-fade-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.magnifier-fade-enter-from,
.magnifier-fade-leave-to {
  opacity: 0;
  transform: translateX(-10px);
}

@media (max-width: 968px) {
  .magnified-view {
    display: none;
  }

  .magnifier-hint {
    display: none;
  }

  .lens {
    display: none;
  }

  .image-magnifier-container {
    cursor: zoom-in;
  }
}
</style>

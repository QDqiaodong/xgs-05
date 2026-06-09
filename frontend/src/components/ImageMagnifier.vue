<template>
  <div
    class="image-magnifier-container"
    @mouseenter="onMouseEnter"
    @mouseleave="onMouseLeave"
    @mousemove="onMouseMove"
    ref="containerRef"
  >
    <div class="main-image-wrapper">
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
        :style="{
          width: lensSize + 'px',
          height: lensSize + 'px',
          left: lensPosition.x + 'px',
          top: lensPosition.y + 'px',
          opacity: isMouseOnImage ? 1 : 0
        }"
      ></div>
      <div class="magnifier-hint" :class="{ hidden: isHovering }">
        <el-icon><ZoomIn /></el-icon>
        <span>悬停查看细节</span>
      </div>
    </div>

    <Transition name="magnifier-fade">
      <div
        v-if="isHovering && showMagnifier"
        class="magnified-view"
        :style="{
          width: magnifierSize + 'px',
          height: magnifierSize + 'px',
          backgroundImage: `url(${imageUrl})`,
          backgroundSize: `${imgRect.width * magnification}px ${imgRect.height * magnification}px`,
          backgroundPosition: `${-bgPosition.x}px ${-bgPosition.y}px`
        }"
      >
        <div class="magnifier-toolbar">
          <div class="magnification-buttons">
            <button
              v-for="(m, idx) in magnifications"
              :key="idx"
              class="mag-btn"
              :class="{ active: magnification === m }"
              @click.stop="setMagnification(m)"
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
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { ZoomIn, View } from '@element-plus/icons-vue'

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

const isHovering = ref(false)
const isMouseOnImage = ref(false)
const magnification = ref(props.defaultMagnification)

const lensPosition = reactive({ x: 0, y: 0 })
const bgPosition = reactive({ x: 0, y: 0 })

const imgRect = reactive({
  width: 0,
  height: 0,
  offsetLeft: 0,
  offsetTop: 0
})

const showMagnifier = computed(() => imgRect.width > 0 && imgRect.height > 0)

function onImageLoad() {
  updateImageRect()
}

function updateImageRect() {
  if (!imageRef.value || !containerRef.value) return
  const img = imageRef.value.getBoundingClientRect()
  const container = containerRef.value.getBoundingClientRect()
  imgRect.width = img.width
  imgRect.height = img.height
  imgRect.offsetLeft = img.left - container.left
  imgRect.offsetTop = img.top - container.top
}

function onMouseEnter() {
  isHovering.value = true
  updateImageRect()
}

function onMouseLeave() {
  isHovering.value = false
  isMouseOnImage.value = false
}

function onMouseMove(e) {
  if (!containerRef.value) return
  const container = containerRef.value.getBoundingClientRect()
  const mouseX = e.clientX - container.left
  const mouseY = e.clientY - container.top

  const relX = mouseX - imgRect.offsetLeft
  const relY = mouseY - imgRect.offsetTop

  if (
    relX >= 0 &&
    relX <= imgRect.width &&
    relY >= 0 &&
    relY <= imgRect.height
  ) {
    isMouseOnImage.value = true

    const halfLens = props.lensSize / 2
    let lensX = relX - halfLens
    let lensY = relY - halfLens

    lensX = Math.max(0, Math.min(lensX, imgRect.width - props.lensSize))
    lensY = Math.max(0, Math.min(lensY, imgRect.height - props.lensSize))

    lensPosition.x = lensX + imgRect.offsetLeft
    lensPosition.y = lensY + imgRect.offsetTop

    const ratio = magnification.value
    bgPosition.x = (lensX + halfLens) * ratio - props.magnifierSize / 2
    bgPosition.y = (lensY + halfLens) * ratio - props.magnifierSize / 2
  } else {
    isMouseOnImage.value = false
  }
}

function setMagnification(m) {
  magnification.value = m
  emit('magnification-change', m)
}

function onResize() {
  updateImageRect()
}

watch(
  () => props.imageUrl,
  () => {
    updateImageRect()
  }
)

onMounted(() => {
  window.addEventListener('resize', onResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
})
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
  position: absolute;
  top: 0;
  right: calc(100% + 24px);
  border-radius: 8px;
  background-repeat: no-repeat;
  background-color: #fff;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2), 0 0 0 1px rgba(102, 126, 234, 0.3);
  z-index: 10;
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

<template>
  <div
    class="sticky-note"
    :class="{ 'is-selected': isSelected, 'is-dragging': isDragging, 'is-resizing': isResizing }"
    :style="noteStyle"
    @mousedown.stop="onMouseDown"
  >
    <div class="note-header" @mousedown.stop="onMouseDown">
      <div class="note-colors">
        <span
          v-for="c in noteColors"
          :key="c"
          class="color-dot"
          :style="{ background: c }"
          :class="{ active: item.color === c }"
          @click.stop="changeColor(c)"
        ></span>
      </div>
      <div class="note-actions">
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
    <div class="note-body">
      <textarea
        v-model="localText"
        class="note-text"
        placeholder="在此输入备注..."
        @input="onTextChange"
        @mousedown.stop
      ></textarea>
    </div>
    <div
      v-if="isSelected"
      class="resize-handle resize-br"
      @mousedown.stop="onResizeStart"
    ></div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onUnmounted } from 'vue'
import { Close } from '@element-plus/icons-vue'
import { useInspirationStore } from '@/store/inspiration'

const props = defineProps({
  item: {
    type: Object,
    required: true
  },
  isSelected: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['select'])

const store = useInspirationStore()

const isDragging = ref(false)
const isResizing = ref(false)
const dragStart = ref({ x: 0, y: 0, itemX: 0, itemY: 0 })
const resizeStart = ref({ x: 0, y: 0, width: 0, height: 0 })
const localText = ref(props.item.text || '')

const MIN_WIDTH = 140
const MIN_HEIGHT = 100

const noteColors = ['#fff3cd', '#d4edda', '#d1ecf1', '#f8d7da', '#e2d8f5']

watch(() => props.item.text, (val) => {
  if (val !== localText.value) {
    localText.value = val || ''
  }
})

const noteStyle = computed(() => ({
  left: `${props.item.x}px`,
  top: `${props.item.y}px`,
  width: `${props.item.width}px`,
  height: `${props.item.height}px`,
  background: props.item.color,
  zIndex: props.isSelected ? 100 : 1
}))

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

function onTextChange() {
  store.updateItem(props.item.id, { text: localText.value })
}

function changeColor(c) {
  store.updateItem(props.item.id, { color: c })
}

function onDelete() {
  store.deleteItem(props.item.id)
}

onUnmounted(() => {
  window.removeEventListener('mousemove', onMouseMove)
  window.removeEventListener('mouseup', onMouseUp)
  window.removeEventListener('mousemove', onResizeMove)
  window.removeEventListener('mouseup', onResizeEnd)
})
</script>

<style scoped>
.sticky-note {
  position: absolute;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  cursor: move;
  user-select: none;
  display: flex;
  flex-direction: column;
  transition: box-shadow 0.2s, transform 0.1s;
}

.sticky-note:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
}

.sticky-note.is-selected {
  box-shadow: 0 0 0 2px #667eea, 0 4px 16px rgba(102, 126, 234, 0.3);
}

.sticky-note.is-dragging {
  cursor: grabbing;
  opacity: 0.9;
}

.sticky-note.is-resizing {
  cursor: default;
}

.note-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 10px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
  cursor: move;
  flex-shrink: 0;
}

.note-colors {
  display: flex;
  gap: 4px;
}

.color-dot {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  cursor: pointer;
  border: 2px solid transparent;
  transition: transform 0.15s, border-color 0.15s;
}

.color-dot:hover {
  transform: scale(1.15);
}

.color-dot.active {
  border-color: #333;
}

.action-btn {
  color: #666;
  padding: 2px;
}

.action-btn:hover {
  color: #f56c6c;
}

.note-body {
  flex: 1;
  padding: 8px 12px;
  display: flex;
  overflow: hidden;
}

.note-text {
  width: 100%;
  height: 100%;
  background: transparent;
  border: none;
  outline: none;
  resize: none;
  font-size: 13px;
  line-height: 1.6;
  color: #333;
  font-family: inherit;
}

.note-text::placeholder {
  color: rgba(0, 0, 0, 0.35);
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

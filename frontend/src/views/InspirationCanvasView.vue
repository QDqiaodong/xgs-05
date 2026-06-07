<template>
  <div class="inspiration-canvas">
    <div class="sidebar sidebar-left">
      <div class="sidebar-header">
        <h3>我的画板</h3>
        <el-button
          type="primary"
          size="small"
          :icon="Plus"
          @click="showCreateBoardDialog"
          circle
        ></el-button>
      </div>
      <div class="board-list">
        <div
          v-for="board in store.boards"
          :key="board.id"
          class="board-item"
          :class="{ active: board.id === store.activeBoardId }"
          @click="store.setActiveBoard(board.id)"
        >
          <div class="board-color-dot" :style="{ background: board.color }"></div>
          <span
            v-if="editingBoardId !== board.id"
            class="board-name"
            @dblclick.stop="startRename(board.id)"
          >{{ board.name }}</span>
          <el-input
            v-else
            v-model="renameValue"
            size="small"
            @blur="saveRename(board.id)"
            @keyup.enter="saveRename(board.id)"
            ref="renameInputRef"
          />
          <span class="board-count">{{ board.items.length }}</span>
          <el-dropdown trigger="click" @click.stop @command="(cmd) => handleBoardAction(cmd, board.id)">
            <el-button type="text" class="board-more" size="small">
              <el-icon><MoreFilled /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="rename">重命名</el-dropdown-item>
                <el-dropdown-item command="delete" divided>删除画板</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>

    <div class="canvas-container" ref="canvasContainerRef">
      <div class="canvas-toolbar">
        <div class="toolbar-left">
          <span class="current-board">
            <span class="color-badge" :style="{ background: store.activeBoard?.color }"></span>
            {{ store.activeBoard?.name || '未选择画板' }}
          </span>
          <span class="item-count" v-if="store.activeBoard">
            {{ store.activeBoard.items.length }} 个素材
          </span>
        </div>
        <div class="toolbar-right">
          <el-button :icon="EditPen" @click="addNote">添加备注</el-button>
          <el-button type="danger" plain :icon="Delete" @click="clearBoard" v-if="store.activeBoard?.items.length">
            清空画布
          </el-button>
        </div>
      </div>

      <div
        class="canvas-area"
        ref="canvasAreaRef"
        @click="onCanvasClick"
        @dragover.prevent
        @drop="onCanvasDrop"
      >
        <div
          v-if="store.activeBoard && store.activeBoard.items.length === 0"
          class="empty-canvas"
        >
          <el-icon :size="64" color="#ccc"><Grid /></el-icon>
          <p>从右侧拖拽作品到画布</p>
          <p class="hint">或点击「添加备注」创建文字便签</p>
        </div>

        <template v-if="store.activeBoard">
          <CanvasCard
            v-for="item in store.activeBoard.items.filter(i => i.type === 'work')"
            :key="item.id"
            :item="item"
            :is-selected="selectedItemId === item.id"
            :canvas-ref="canvasAreaRef"
            @select="selectItem"
          />
          <StickyNote
            v-for="item in store.activeBoard.items.filter(i => i.type === 'note')"
            :key="item.id"
            :item="item"
            :is-selected="selectedItemId === item.id"
            @select="selectItem"
          />
        </template>
      </div>
    </div>

    <div class="sidebar sidebar-right">
      <div class="sidebar-header">
        <h3>作品素材库</h3>
      </div>
      <div class="search-box">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索作品..."
          :prefix-icon="Search"
          clearable
        />
      </div>
      <div class="work-library">
        <div
          v-for="work in filteredWorks"
          :key="work.id"
          class="library-item"
          draggable="true"
          @dragstart="onWorkDragStart($event, work)"
        >
          <div class="library-thumb">
            <img :src="work.coverImage" :alt="work.title" />
          </div>
          <div class="library-info">
            <div class="library-title">{{ work.title }}</div>
            <div class="library-author">{{ work.authorName }}</div>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="createBoardVisible" title="创建新画板" width="400px">
      <el-form :model="newBoardForm" label-width="80px">
        <el-form-item label="画板名称">
          <el-input v-model="newBoardForm.name" placeholder="请输入画板名称" maxlength="20" show-word-limit />
        </el-form-item>
        <el-form-item label="主题颜色">
          <el-color-picker v-model="newBoardForm.color" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createBoardVisible = false">取消</el-button>
        <el-button type="primary" @click="createBoard">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import {
  Plus,
  MoreFilled,
  EditPen,
  Delete,
  Grid,
  Search
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useInspirationStore } from '@/store/inspiration'
import CanvasCard from '@/components/CanvasCard.vue'
import StickyNote from '@/components/StickyNote.vue'

const store = useInspirationStore()
const router = useRouter()

const canvasContainerRef = ref(null)
const canvasAreaRef = ref(null)
const renameInputRef = ref(null)

const selectedItemId = ref(null)
const searchKeyword = ref('')
const editingBoardId = ref(null)
const renameValue = ref('')

const createBoardVisible = ref(false)
const newBoardForm = ref({
  name: '',
  color: '#667eea'
})

const mockWorks = [
  { id: 101, title: '手工编织毛衣', description: '温暖的羊毛手工编织', coverImage: 'https://picsum.photos/300/400?random=101', authorId: 1, authorName: '小手巧', authorAvatar: 'https://via.placeholder.com/24', viewCount: 1256, favoriteCount: 89 },
  { id: 102, title: '陶艺花瓶', description: '手工拉坯制作，釉色温润如玉', coverImage: 'https://picsum.photos/300/350?random=102', authorId: 2, authorName: '陶然', authorAvatar: 'https://via.placeholder.com/24', viewCount: 892, favoriteCount: 67 },
  { id: 103, title: '布艺玩偶套装', description: '可爱的小动物布艺玩偶', coverImage: 'https://picsum.photos/300/450?random=103', authorId: 3, authorName: '布布', authorAvatar: 'https://via.placeholder.com/24', viewCount: 2341, favoriteCount: 156 },
  { id: 104, title: '原木摆件', description: '天然木材手工雕刻', coverImage: 'https://picsum.photos/300/380?random=104', authorId: 4, authorName: '木工匠', authorAvatar: 'https://via.placeholder.com/24', viewCount: 567, favoriteCount: 34 },
  { id: 105, title: '毛线围巾', description: '柔软的马海毛线', coverImage: 'https://picsum.photos/300/420?random=105', authorId: 1, authorName: '小手巧', authorAvatar: 'https://via.placeholder.com/24', viewCount: 1890, favoriteCount: 123 },
  { id: 106, title: '手工茶具', description: '一套精美的手工茶具', coverImage: 'https://picsum.photos/300/360?random=106', authorId: 2, authorName: '陶然', authorAvatar: 'https://via.placeholder.com/24', viewCount: 1456, favoriteCount: 98 },
  { id: 107, title: '刺绣手帕', description: '精致的苏绣工艺', coverImage: 'https://picsum.photos/300/390?random=107', authorId: 3, authorName: '布布', authorAvatar: 'https://via.placeholder.com/24', viewCount: 789, favoriteCount: 56 },
  { id: 108, title: '木质首饰盒', description: '胡桃木制作', coverImage: 'https://picsum.photos/300/340?random=108', authorId: 4, authorName: '木工匠', authorAvatar: 'https://via.placeholder.com/24', viewCount: 678, favoriteCount: 45 },
  { id: 109, title: '编织毛衣', description: '温暖的羊毛手工编织', coverImage: 'https://picsum.photos/300/410?random=109', authorId: 1, authorName: '小手巧', authorAvatar: 'https://via.placeholder.com/24', viewCount: 1100, favoriteCount: 78 },
  { id: 110, title: '陶艺茶具', description: '品茗必备', coverImage: 'https://picsum.photos/300/370?random=110', authorId: 2, authorName: '陶然', authorAvatar: 'https://via.placeholder.com/24', viewCount: 980, favoriteCount: 65 }
]

const filteredWorks = computed(() => {
  if (!searchKeyword.value) return mockWorks
  const kw = searchKeyword.value.toLowerCase()
  return mockWorks.filter(w =>
    w.title.toLowerCase().includes(kw) ||
    w.authorName.toLowerCase().includes(kw) ||
    w.description.toLowerCase().includes(kw)
  )
})

function onCanvasClick(e) {
  if (e.target === canvasAreaRef.value) {
    selectedItemId.value = null
  }
}

function selectItem(id) {
  selectedItemId.value = id
}

function onWorkDragStart(e, work) {
  e.dataTransfer.setData('application/json', JSON.stringify(work))
  e.dataTransfer.effectAllowed = 'copy'
}

function onCanvasDrop(e) {
  const data = e.dataTransfer.getData('application/json')
  if (!data) return
  try {
    const work = JSON.parse(data)
    const rect = canvasAreaRef.value.getBoundingClientRect()
    const x = e.clientX - rect.left - 110
    const y = e.clientY - rect.top - 150
    store.addWorkCard(work, Math.max(0, x), Math.max(0, y))
    ElMessage.success('已添加到画布')
  } catch (err) {
    console.error(err)
  }
}

function addNote() {
  if (!store.activeBoard) {
    ElMessage.warning('请先创建或选择一个画板')
    return
  }
  store.addNote(120 + Math.random() * 100, 120 + Math.random() * 100)
}

function clearBoard() {
  if (!store.activeBoard) return
  ElMessageBox.confirm(
    `确定要清空「${store.activeBoard.name}」上的所有素材吗？`,
    '提示',
    {
      confirmButtonText: '确定清空',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    const items = [...store.activeBoard.items]
    items.forEach(item => store.deleteItem(item.id))
    ElMessage.success('画布已清空')
  }).catch(() => {})
}

function showCreateBoardDialog() {
  newBoardForm.value = { name: '', color: '#667eea' }
  createBoardVisible.value = true
}

function createBoard() {
  if (!newBoardForm.value.name.trim()) {
    ElMessage.warning('请输入画板名称')
    return
  }
  store.createBoard(newBoardForm.value.name.trim(), newBoardForm.value.color)
  createBoardVisible.value = false
  ElMessage.success('画板已创建')
}

function handleBoardAction(cmd, boardId) {
  if (cmd === 'rename') {
    startRename(boardId)
  } else if (cmd === 'delete') {
    const board = store.boards.find(b => b.id === boardId)
    if (store.boards.length <= 1) {
      ElMessage.warning('至少保留一个画板')
      return
    }
    ElMessageBox.confirm(
      `确定要删除画板「${board?.name}」吗？该画板上的所有素材将被移除。`,
      '删除画板',
      { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' }
    ).then(() => {
      store.deleteBoard(boardId)
      ElMessage.success('画板已删除')
    }).catch(() => {})
  }
}

function startRename(boardId) {
  const board = store.boards.find(b => b.id === boardId)
  if (!board) return
  editingBoardId.value = boardId
  renameValue.value = board.name
  nextTick(() => {
    if (renameInputRef.value && renameInputRef.value[0]) {
      renameInputRef.value[0].focus()
      renameInputRef.value[0].select()
    }
  })
}

function saveRename(boardId) {
  if (!editingBoardId.value) return
  const name = renameValue.value.trim()
  if (name) {
    store.renameBoard(boardId, name)
  }
  editingBoardId.value = null
}

onMounted(() => {
  store.init()
})
</script>

<style scoped>
.inspiration-canvas {
  position: fixed;
  inset: 0;
  top: 64px;
  display: flex;
  background: #f5f5f5;
  overflow: hidden;
}

.sidebar {
  display: flex;
  flex-direction: column;
  background: #fff;
  box-shadow: 0 0 16px rgba(0, 0, 0, 0.06);
  z-index: 10;
  overflow: hidden;
}

.sidebar-left {
  width: 240px;
  border-right: 1px solid #ebeef5;
}

.sidebar-right {
  width: 280px;
  border-left: 1px solid #ebeef5;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
}

.sidebar-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.board-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.board-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
  margin-bottom: 4px;
  position: relative;
}

.board-item:hover {
  background: #f5f7fa;
}

.board-item.active {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
}

.board-color-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

.board-name {
  flex: 1;
  font-size: 14px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.board-item.active .board-name {
  font-weight: 600;
  color: #667eea;
}

.board-count {
  font-size: 12px;
  color: #999;
  background: #f0f0f0;
  padding: 2px 8px;
  border-radius: 10px;
}

.board-more {
  opacity: 0;
  color: #999;
  transition: opacity 0.2s;
  padding: 0;
}

.board-item:hover .board-more {
  opacity: 1;
}

.canvas-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
}

.canvas-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 24px;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  flex-shrink: 0;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.current-board {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.color-badge {
  width: 14px;
  height: 14px;
  border-radius: 4px;
}

.item-count {
  font-size: 13px;
  color: #999;
}

.toolbar-right {
  display: flex;
  gap: 8px;
}

.canvas-area {
  flex: 1;
  position: relative;
  overflow: auto;
  background-color: #f5f5f5;
  background-image:
    linear-gradient(rgba(0, 0, 0, 0.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 0, 0, 0.04) 1px, transparent 1px);
  background-size: 24px 24px;
  min-height: 100%;
}

.empty-canvas {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
  pointer-events: none;
}

.empty-canvas p {
  margin-top: 12px;
  font-size: 14px;
}

.empty-canvas .hint {
  font-size: 12px;
  color: #bbb;
  margin-top: 4px;
}

.search-box {
  padding: 12px 16px;
  flex-shrink: 0;
}

.work-library {
  flex: 1;
  overflow-y: auto;
  padding: 0 16px 16px;
}

.library-item {
  display: flex;
  gap: 10px;
  padding: 10px;
  border-radius: 8px;
  cursor: grab;
  transition: background 0.2s, transform 0.2s;
  margin-bottom: 8px;
  border: 1px solid transparent;
}

.library-item:hover {
  background: #f5f7fa;
  border-color: #ebeef5;
  transform: translateX(-2px);
}

.library-item:active {
  cursor: grabbing;
}

.library-thumb {
  width: 56px;
  height: 56px;
  border-radius: 6px;
  overflow: hidden;
  flex-shrink: 0;
  background: #f0f0f0;
}

.library-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.library-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-width: 0;
}

.library-title {
  font-size: 13px;
  color: #333;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 4px;
}

.library-author {
  font-size: 12px;
  color: #999;
}
</style>

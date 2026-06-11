<template>
  <el-dialog
    v-model="visible"
    :title="isFavorited ? '管理收藏夹' : '收藏到'"
    width="480px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="folder-selector">
      <div class="folder-header" v-if="!isFavorited">
        <span>请选择要收藏到的收藏夹：</span>
      </div>
      <div class="folder-header" v-else>
        <span>当前已收藏到以下收藏夹，可继续添加或取消：</span>
      </div>

      <div class="folder-list">
        <div
          v-for="folder in folderList"
          :key="folder.id"
          class="folder-item"
          :class="{ active: isInFolder(folder.id), default: folder.isDefault === 1 }"
          @click="toggleFolder(folder)"
        >
          <div class="folder-icon">
            <el-icon :size="24"><Folder /></el-icon>
          </div>
          <div class="folder-info">
            <div class="folder-name">
              {{ folder.name }}
              <el-tag v-if="folder.isDefault === 1" size="small" type="info" class="default-tag">默认</el-tag>
            </div>
            <div class="folder-count">{{ folder.count }} 件作品</div>
          </div>
          <div class="folder-check">
            <el-checkbox :model-value="isInFolder(folder.id)" />
          </div>
        </div>
      </div>

      <div class="create-folder-section">
        <el-input
          v-model="newFolderName"
          placeholder="新建收藏夹名称"
          maxlength="20"
          show-word-limit
          size="large"
          class="new-folder-input"
          @keyup.enter="handleCreateFolder"
        >
          <template #append>
            <el-button :icon="Plus" type="primary" @click="handleCreateFolder" :loading="creatingFolder">
              新建
            </el-button>
          </template>
        </el-input>
      </div>
    </div>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleConfirm" :loading="saving">
        确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { Folder, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useFavoriteStore } from '@/store/favorite'

const props = defineProps({
  modelValue: Boolean,
  workId: {
    type: [Number, String],
    required: true
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const favoriteStore = useFavoriteStore()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const newFolderName = ref('')
const creatingFolder = ref(false)
const saving = ref(false)
const folderList = ref([])
const selectedFolders = ref(new Set())
const originalFolders = ref(new Set())

const isFavorited = computed(() => {
  return favoriteStore.isFavorited(props.workId)
})

function isInFolder(folderId) {
  return selectedFolders.value.has(Number(folderId))
}

async function loadFolders() {
  const list = await favoriteStore.loadFolders(true)
  folderList.value = list
  const currentFolderIds = favoriteStore.getWorkFolderIds(props.workId)
  selectedFolders.value = new Set(currentFolderIds.map(Number))
  originalFolders.value = new Set(currentFolderIds.map(Number))
}

function toggleFolder(folder) {
  const id = Number(folder.id)
  if (selectedFolders.value.has(id)) {
    selectedFolders.value.delete(id)
  } else {
    selectedFolders.value.add(id)
  }
  selectedFolders.value = new Set(selectedFolders.value)
}

async function handleCreateFolder() {
  if (!newFolderName.value.trim()) {
    ElMessage.warning('请输入收藏夹名称')
    return
  }
  creatingFolder.value = true
  try {
    const folder = await favoriteStore.createFolder({
      name: newFolderName.value.trim(),
      description: ''
    })
    if (folder) {
      ElMessage.success('收藏夹创建成功')
      newFolderName.value = ''
      await loadFolders()
      selectedFolders.value.add(Number(folder.id))
      selectedFolders.value = new Set(selectedFolders.value)
    } else {
      ElMessage.error('创建失败，请重试')
    }
  } finally {
    creatingFolder.value = false
  }
}

async function handleConfirm() {
  saving.value = true
  try {
    const toAdd = []
    const toRemove = []

    for (const fid of selectedFolders.value) {
      if (!originalFolders.value.has(fid)) {
        toAdd.push(fid)
      }
    }
    for (const fid of originalFolders.value) {
      if (!selectedFolders.value.has(fid)) {
        toRemove.push(fid)
      }
    }

    if (toAdd.length === 0 && toRemove.length === 0) {
      visible.value = false
      return
    }

    let allSuccess = true
    for (const fid of toAdd) {
      const res = await favoriteStore.addToFolder(props.workId, fid)
      if (!res.success) {
        allSuccess = false
        ElMessage.error(res.message || '添加失败')
      }
    }

    for (const fid of toRemove) {
      const res = await favoriteStore.removeFromFolder(props.workId, fid)
      if (!res.success) {
        allSuccess = false
        ElMessage.error(res.message || '移除失败')
      }
    }

    if (allSuccess) {
      if (toAdd.length > 0 && toRemove.length === 0) {
        ElMessage.success('已添加到收藏夹')
      } else if (toRemove.length > 0 && toAdd.length === 0) {
        ElMessage.success('已从收藏夹移除')
      } else {
        ElMessage.success('收藏夹已更新')
      }
      emit('change')
      visible.value = false
    }
  } finally {
    saving.value = false
  }
}

function handleClose() {
  visible.value = false
}

watch(() => props.modelValue, (val) => {
  if (val) {
    loadFolders()
  }
})
</script>

<style scoped>
.folder-selector {
  padding: 8px 0;
}

.folder-header {
  font-size: 14px;
  color: #606266;
  margin-bottom: 16px;
}

.folder-list {
  max-height: 280px;
  overflow-y: auto;
  margin-bottom: 16px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
}

.folder-item {
  display: flex;
  align-items: center;
  padding: 14px 16px;
  cursor: pointer;
  transition: background-color 0.2s;
  border-bottom: 1px solid #f5f7fa;
}

.folder-item:last-child {
  border-bottom: none;
}

.folder-item:hover {
  background-color: #f5f7fa;
}

.folder-item.active {
  background-color: #ecf5ff;
}

.folder-icon {
  color: #667eea;
  margin-right: 12px;
}

.folder-item.default .folder-icon {
  color: #f5a623;
}

.folder-info {
  flex: 1;
}

.folder-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

.default-tag {
  font-size: 11px;
  padding: 0 6px;
  height: 20px;
  line-height: 18px;
}

.folder-count {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.folder-check {
  margin-left: 12px;
}

.create-folder-section {
  padding-top: 8px;
}

.new-folder-input {
  width: 100%;
}
</style>

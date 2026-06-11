<template>
  <div class="favorites-page container">
    <div class="page-header">
      <h1>我的收藏</h1>
      <p class="subtitle">共收藏 {{ totalCount }} 件作品</p>
    </div>

    <div class="favorites-layout" v-if="userStore.isLoggedIn">
      <aside class="folder-sidebar">
        <div class="sidebar-header">
          <h3>收藏夹</h3>
          <el-button type="primary" size="small" :icon="Plus" circle @click="openCreateDialog" />
        </div>

        <div class="folder-list">
          <div
            v-for="folder in folderList"
            :key="folder.id"
            class="folder-item"
            :class="{ active: activeFolderId === folder.id, default: folder.isDefault === 1 }"
            @click="selectFolder(folder.id)"
          >
            <div class="folder-left">
              <el-icon class="folder-icon" :size="18">
                <Folder />
              </el-icon>
              <div class="folder-text">
                <div class="folder-name">
                  {{ folder.name }}
                  <el-tag v-if="folder.isDefault === 1" size="small" type="info" class="default-tag">默认</el-tag>
                </div>
                <div class="folder-meta">{{ folder.count }} 件作品</div>
              </div>
            </div>
            <div class="folder-actions" v-if="folder.isDefault !== 1" @click.stop>
              <el-dropdown trigger="click" @command="(cmd) => handleFolderAction(cmd, folder)">
                <el-button size="small" text :icon="More" />
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="rename" :icon="Edit">重命名</el-dropdown-item>
                    <el-dropdown-item command="delete" :icon="Delete" divided>删除收藏夹</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </div>
      </aside>

      <section class="works-section">
        <div class="section-header">
          <h2>{{ activeFolderName }}</h2>
          <span class="works-count">共 {{ works.length }} 件</span>
        </div>

        <div v-if="worksLoading" class="loading">
          <el-skeleton :rows="5" animated />
        </div>
        <div v-else-if="works.length > 0" class="masonry-grid">
          <WorkCard v-for="work in works" :key="work.id" :work="work" />
        </div>
        <div v-else class="empty-state">
          <el-empty description="该收藏夹暂无作品">
            <el-button type="primary" @click="$router.push('/')">去发现作品</el-button>
          </el-empty>
        </div>
      </section>
    </div>

    <div v-else class="login-required">
      <el-empty description="请先登录后查看收藏">
        <el-button type="primary" @click="$router.push('/')">去首页浏览</el-button>
      </el-empty>
    </div>

    <el-dialog
      v-model="createDialogVisible"
      :title="editingFolder ? '重命名收藏夹' : '新建收藏夹'"
      width="420px"
      :close-on-click-modal="false"
    >
      <el-form :model="folderForm" label-width="80px">
        <el-form-item label="名称">
          <el-input
            v-model="folderForm.name"
            placeholder="请输入收藏夹名称"
            maxlength="20"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="folderForm.description"
            type="textarea"
            :rows="3"
            placeholder="收藏夹描述（选填）"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="folderActionLoading" @click="submitFolderForm">
          {{ editingFolder ? '保存' : '创建' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { Plus, Folder, More, Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import WorkCard from '@/components/WorkCard.vue'
import { useFavoriteStore } from '@/store/favorite'
import { useUserStore } from '@/store/user'

const favoriteStore = useFavoriteStore()
const userStore = useUserStore()

const folderList = ref([])
const activeFolderId = ref(null)
const works = ref([])
const worksLoading = ref(false)
const createDialogVisible = ref(false)
const folderActionLoading = ref(false)
const editingFolder = ref(null)
const folderForm = ref({
  name: '',
  description: ''
})

const totalCount = computed(() => {
  return folderList.value.reduce((sum, f) => sum + (Number(f.count) || 0), 0)
})

const activeFolderName = computed(() => {
  const folder = folderList.value.find(f => f.id === activeFolderId.value)
  return folder ? folder.name : '收藏夹'
})

async function loadFolderList() {
  if (!userStore.userInfo?.id) return
  const list = await favoriteStore.loadFolders(true)
  folderList.value = list
  if (list.length > 0 && !activeFolderId.value) {
    const defaultFolder = list.find(f => f.isDefault === 1) || list[0]
    activeFolderId.value = defaultFolder.id
  }
  if (activeFolderId.value) {
    await loadFolderWorks(activeFolderId.value)
  }
}

async function loadFolderWorks(folderId) {
  if (!folderId) return
  worksLoading.value = true
  try {
    const data = await favoriteStore.loadFolderWorks(folderId, 1, 100)
    if (data && data.records) {
      works.value = data.records
    } else {
      works.value = []
    }
  } catch (e) {
    ElMessage.error('加载作品失败')
    works.value = []
  } finally {
    worksLoading.value = false
  }
}

function selectFolder(folderId) {
  if (activeFolderId.value === folderId) return
  activeFolderId.value = folderId
  loadFolderWorks(folderId)
}

function openCreateDialog() {
  editingFolder.value = null
  folderForm.value = { name: '', description: '' }
  createDialogVisible.value = true
}

async function handleFolderAction(command, folder) {
  if (command === 'rename') {
    editingFolder.value = folder
    folderForm.value = {
      name: folder.name,
      description: folder.description || ''
    }
    createDialogVisible.value = true
  } else if (command === 'delete') {
    try {
      await ElMessageBox.confirm(
        `确定删除收藏夹"${folder.name}"吗？该收藏夹中的作品将被移动到默认收藏夹。`,
        '删除确认',
        {
          type: 'warning',
          confirmButtonText: '确定删除',
          cancelButtonText: '取消'
        }
      )
      const success = await favoriteStore.deleteFolder(folder.id)
      if (success) {
        ElMessage.success('删除成功')
        if (activeFolderId.value === folder.id) {
          activeFolderId.value = null
        }
        await loadFolderList()
      } else {
        ElMessage.error('删除失败')
      }
    } catch (e) {
      if (e !== 'cancel') {
        console.error(e)
      }
    }
  }
}

async function submitFolderForm() {
  if (!folderForm.value.name.trim()) {
    ElMessage.warning('请输入收藏夹名称')
    return
  }
  folderActionLoading.value = true
  try {
    if (editingFolder.value) {
      const success = await favoriteStore.updateFolder(editingFolder.value.id, {
        name: folderForm.value.name.trim(),
        description: folderForm.value.description
      })
      if (success) {
        ElMessage.success('修改成功')
        createDialogVisible.value = false
      } else {
        ElMessage.error('修改失败')
      }
    } else {
      const folder = await favoriteStore.createFolder({
        name: folderForm.value.name.trim(),
        description: folderForm.value.description
      })
      if (folder) {
        ElMessage.success('创建成功')
        createDialogVisible.value = false
        activeFolderId.value = folder.id
        await loadFolderList()
      } else {
        ElMessage.error('创建失败')
      }
    }
  } finally {
    folderActionLoading.value = false
  }
}

onMounted(async () => {
  if (userStore.isLoggedIn) {
    await loadFolderList()
  }
})

watch(() => userStore.isLoggedIn, async (val) => {
  if (val) {
    await loadFolderList()
  } else {
    folderList.value = []
    works.value = []
  }
})

watch(() => favoriteStore.version, async () => {
  if (userStore.isLoggedIn) {
    await loadFolderList()
  }
})
</script>

<style scoped>
.favorites-page {
  padding: 30px 0;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h1 {
  font-size: 28px;
  color: #333;
  margin-bottom: 8px;
}

.subtitle {
  font-size: 14px;
  color: #999;
}

.favorites-layout {
  display: grid;
  grid-template-columns: 260px 1fr;
  gap: 24px;
  align-items: start;
}

.folder-sidebar {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  padding: 20px;
  position: sticky;
  top: 100px;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.sidebar-header h3 {
  margin: 0;
  font-size: 16px;
  color: #333;
  font-weight: 600;
}

.folder-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.folder-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  gap: 8px;
}

.folder-item:hover {
  background: #f5f7fa;
}

.folder-item.active {
  background: linear-gradient(135deg, #667eea10 0%, #764ba210 100%);
  border: 1px solid #667eea30;
}

.folder-left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
  min-width: 0;
}

.folder-icon {
  color: #667eea;
  flex-shrink: 0;
}

.folder-item.default .folder-icon {
  color: #f5a623;
}

.folder-item.active .folder-icon {
  color: #667eea;
}

.folder-text {
  flex: 1;
  min-width: 0;
}

.folder-name {
  font-size: 14px;
  color: #333;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.default-tag {
  font-size: 10px;
  padding: 0 6px;
  height: 18px;
  line-height: 16px;
  flex-shrink: 0;
}

.folder-meta {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}

.folder-actions {
  flex-shrink: 0;
  opacity: 0;
  transition: opacity 0.2s;
}

.folder-item:hover .folder-actions {
  opacity: 1;
}

.works-section {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  padding: 24px;
  min-height: 600px;
}

.section-header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.section-header h2 {
  margin: 0;
  font-size: 20px;
  color: #333;
  font-weight: 600;
}

.works-count {
  font-size: 14px;
  color: #999;
}

.loading {
  padding: 40px 0;
}

.empty-state {
  padding: 80px 0;
}

.login-required {
  padding: 120px 0;
}

:deep(.masonry-grid) {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
  align-items: start;
}

@media (max-width: 968px) {
  .favorites-layout {
    grid-template-columns: 1fr;
  }

  .folder-sidebar {
    position: static;
  }

  .folder-list {
    flex-direction: row;
    overflow-x: auto;
    gap: 8px;
    padding-bottom: 8px;
  }

  .folder-item {
    flex-shrink: 0;
    min-width: 160px;
  }

  .folder-actions {
    opacity: 1;
  }
}
</style>

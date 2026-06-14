<template>
  <div class="activity-admin container">
    <div class="page-header card">
      <h1>活动管理</h1>
      <div class="header-actions">
        <el-button type="primary" @click="openCreateDialog">
          <el-icon><Plus /></el-icon>
          创建活动
        </el-button>
      </div>
    </div>

    <div v-if="loading" class="loading card">
      <el-skeleton :rows="5" animated />
    </div>

    <div v-else class="activity-list card">
      <el-table :data="activities" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="活动主题">
          <template #default="{ row }">
            <div class="activity-title-cell">
              <div
                class="title-cover"
                :style="{ background: getCoverBg(row) }"
              ></div>
              <div class="title-info">
                <div class="title-text">{{ row.title }}</div>
                <div class="title-desc" v-if="row.description">{{ row.description }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="活动时间" width="240">
          <template #default="{ row }">
            <div class="time-info">
              <div>{{ formatDateTime(row.startTime) }}</div>
              <div class="time-sep">至</div>
              <div>{{ formatDateTime(row.endTime) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '开启' : '关闭' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="workCount" label="作品数" width="80" />
        <el-table-column prop="voteCount" label="投票数" width="80" />
        <el-table-column prop="viewCount" label="浏览量" width="80" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="goToDetail(row.id)">
              查看
            </el-button>
            <el-button type="primary" link size="small" @click="openEditDialog(row)">
              编辑
            </el-button>
            <el-popconfirm
              title="确认删除该活动？"
              @confirm="handleDelete(row)"
            >
              <template #reference>
                <el-button type="danger" link size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="hasMore && !loading" class="load-more">
        <el-button :loading="loadingMore" :disabled="loadingMore" @click="loadMore">
          加载更多
        </el-button>
      </div>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑活动' : '创建活动'"
      width="700px"
      destroy-on-close
    >
      <el-form :model="formData" label-width="120px">
        <el-form-item label="活动主题" required>
          <el-input v-model="formData.title" placeholder="请输入活动主题" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="活动介绍">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入活动介绍"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="参与规则">
          <el-input
            v-model="formData.rules"
            type="textarea"
            :rows="4"
            placeholder="请输入参与规则"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="封面图片">
          <el-input v-model="formData.coverImage" placeholder="请输入封面图片URL" />
        </el-form-item>
        <el-form-item label="Banner图片">
          <el-input v-model="formData.bannerImage" placeholder="请输入Banner图片URL" />
        </el-form-item>
        <el-form-item label="关联分类">
          <el-select v-model="formData.categoryId" placeholder="不限分类" clearable style="width: 100%">
            <el-option label="编织" :value="1" />
            <el-option label="陶艺" :value="2" />
            <el-option label="布艺" :value="3" />
            <el-option label="木艺" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="活动时间" required>
          <el-date-picker
            v-model="formData.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="投票时间">
          <el-date-picker
            v-model="formData.voteDateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="投票开始时间"
            end-placeholder="投票结束时间"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
          <div class="form-tip">不填则默认使用活动时间</div>
        </el-form-item>
        <el-form-item label="每人投稿数">
          <el-input-number v-model="formData.maxSubmitPerUser" :min="1" :max="100" />
        </el-form-item>
        <el-form-item label="每人投票数">
          <el-input-number v-model="formData.maxVotePerUser" :min="1" :max="1000" />
        </el-form-item>
        <el-form-item label="同作品多投">
          <el-switch v-model="formData.allowSameWorkMultivote" :active-value="1" :inactive-value="0" />
          <span class="form-tip">是否允许对同一作品投多票</span>
        </el-form-item>
        <el-form-item label="活动状态">
          <el-switch v-model="formData.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">
          {{ isEdit ? '保存修改' : '创建活动' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getActivityList, createActivity, updateActivity, deleteActivity } from '@/utils/request'

const router = useRouter()

const activities = ref([])
const loading = ref(true)
const loadingMore = ref(false)
const hasMore = ref(true)
const page = ref(1)
const pageSize = 10
let fetchVersion = 0

const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const formData = ref(getDefaultForm())

function getDefaultForm() {
  return {
    id: null,
    title: '',
    description: '',
    rules: '',
    coverImage: '',
    bannerImage: '',
    categoryId: null,
    dateRange: [],
    voteDateRange: [],
    startTime: null,
    endTime: null,
    voteStartTime: null,
    voteEndTime: null,
    maxSubmitPerUser: 1,
    maxVotePerUser: 10,
    allowSameWorkMultivote: 0,
    status: 1
  }
}

const coverBgs = [
  'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
  'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
  'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)'
]

function getCoverBg(activity) {
  if (activity.coverImage) {
    return `url(${activity.coverImage}) center/cover no-repeat`
  }
  return coverBgs[activity.id % coverBgs.length]
}

function formatDateTime(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

function goToDetail(id) {
  router.push(`/activity/${id}`)
}

async function loadList(reset = false) {
  if (reset) {
    page.value = 1
    activities.value = []
    hasMore.value = true
  }
  if (loading.value || loadingMore.value) return
  if (!hasMore.value) return

  const version = ++fetchVersion
  if (reset) {
    loading.value = true
  } else {
    loadingMore.value = true
  }
  try {
    const res = await getActivityList({ page: page.value, size: pageSize })
    if (version !== fetchVersion) return
    if (res.code === 200 && res.data) {
      const records = res.data.records || []
      activities.value = activities.value.concat(records)
      hasMore.value = records.length >= pageSize
      page.value++
    } else {
      ElMessage.error(res.message || '加载失败')
    }
  } catch (e) {
    console.error('加载活动列表失败', e)
  } finally {
    if (version === fetchVersion) {
      loading.value = false
      loadingMore.value = false
    }
  }
}

function loadMore() {
  loadList(false)
}

function openCreateDialog() {
  isEdit.value = false
  formData.value = getDefaultForm()
  dialogVisible.value = true
}

function openEditDialog(row) {
  isEdit.value = true
  formData.value = {
    id: row.id,
    title: row.title || '',
    description: row.description || '',
    rules: row.rules || '',
    coverImage: row.coverImage || '',
    bannerImage: row.bannerImage || '',
    categoryId: row.categoryId || null,
    dateRange: row.startTime && row.endTime ? [row.startTime, row.endTime] : [],
    voteDateRange: row.voteStartTime && row.voteEndTime ? [row.voteStartTime, row.voteEndTime] : [],
    startTime: row.startTime,
    endTime: row.endTime,
    voteStartTime: row.voteStartTime,
    voteEndTime: row.voteEndTime,
    maxSubmitPerUser: row.maxSubmitPerUser || 1,
    maxVotePerUser: row.maxVotePerUser || 10,
    allowSameWorkMultivote: row.allowSameWorkMultivote || 0,
    status: row.status ?? 1
  }
  dialogVisible.value = true
}

async function handleSave() {
  if (!formData.value.title) {
    ElMessage.warning('请输入活动主题')
    return
  }
  if (!formData.value.dateRange || formData.value.dateRange.length !== 2) {
    ElMessage.warning('请选择活动时间')
    return
  }
  saving.value = true
  try {
    const submitData = {
      title: formData.value.title,
      description: formData.value.description,
      rules: formData.value.rules,
      coverImage: formData.value.coverImage,
      bannerImage: formData.value.bannerImage,
      categoryId: formData.value.categoryId,
      startTime: formData.value.dateRange[0],
      endTime: formData.value.dateRange[1],
      voteStartTime: formData.value.voteDateRange?.[0] || null,
      voteEndTime: formData.value.voteDateRange?.[1] || null,
      maxSubmitPerUser: formData.value.maxSubmitPerUser,
      maxVotePerUser: formData.value.maxVotePerUser,
      allowSameWorkMultivote: formData.value.allowSameWorkMultivote,
      status: formData.value.status
    }
    let res
    if (isEdit.value) {
      res = await updateActivity(formData.value.id, submitData)
    } else {
      res = await createActivity(submitData)
    }
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '修改成功' : '创建成功')
      dialogVisible.value = false
      loadList(true)
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (e) {
    console.error('保存活动失败', e)
  } finally {
    saving.value = false
  }
}

async function handleDelete(row) {
  try {
    const res = await deleteActivity(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadList(true)
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (e) {
    console.error('删除活动失败', e)
  }
}

onMounted(() => {
  loadList(true)
})
</script>

<style scoped>
.activity-admin {
  padding: 24px 0;
}

.page-header {
  padding: 24px 32px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.page-header h1 {
  font-size: 24px;
  margin: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.activity-list {
  padding: 16px;
}

.activity-title-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-cover {
  width: 48px;
  height: 48px;
  border-radius: 6px;
  flex-shrink: 0;
}

.title-info {
  min-width: 0;
}

.title-text {
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.title-desc {
  font-size: 12px;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 300px;
}

.time-info {
  font-size: 13px;
  color: #666;
  line-height: 1.6;
}

.time-sep {
  color: #ccc;
  font-size: 12px;
}

.form-tip {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.loading {
  padding: 24px;
}

.load-more {
  text-align: center;
  padding: 24px 0 0;
}
</style>

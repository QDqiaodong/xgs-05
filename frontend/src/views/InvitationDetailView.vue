<template>
  <div class="invitation-detail container" v-if="detail">
    <el-button link @click="goBack" class="back-btn">
      <el-icon><ArrowLeft /></el-icon>
      返回列表
    </el-button>

    <div class="detail-card card">
      <div class="detail-header">
        <h1 class="invitation-title">{{ detail.invitation.title }}</h1>
        <el-tag :type="getStatusType(detail.invitation.status)" size="large">
          {{ getStatusText(detail.invitation.status) }}
        </el-tag>
      </div>

      <div class="parties">
        <div class="party">
          <img :src="detail.client?.avatar || 'https://via.placeholder.com/60'" alt="" class="party-avatar" />
          <div class="party-info">
            <div class="party-label">客户</div>
            <div class="party-name">{{ detail.client?.nickname || detail.client?.username }}</div>
          </div>
        </div>
        <el-icon class="arrow-icon"><Right /></el-icon>
        <div class="party">
          <img :src="detail.creator?.avatar || 'https://via.placeholder.com/60'" alt="" class="party-avatar" />
          <div class="party-info">
            <div class="party-label">创作者</div>
            <div class="party-name">{{ detail.creator?.nickname || detail.creator?.username }}</div>
          </div>
        </div>
      </div>

      <div class="info-grid">
        <div class="info-block">
          <div class="info-label">预算范围</div>
          <div class="info-value">{{ formatBudget(detail.invitation.budgetMin, detail.invitation.budgetMax) }}</div>
        </div>
        <div class="info-block">
          <div class="info-label">期望周期</div>
          <div class="info-value">{{ detail.invitation.expectedDays ? detail.invitation.expectedDays + ' 天' : '面议' }}</div>
        </div>
        <div class="info-block">
          <div class="info-label">发起时间</div>
          <div class="info-value">{{ formatDate(detail.invitation.createTime) }}</div>
        </div>
        <div class="info-block" v-if="detail.invitation.rejectReason">
          <div class="info-label">拒绝原因</div>
          <div class="info-value danger">{{ detail.invitation.rejectReason }}</div>
        </div>
      </div>

      <div class="section">
        <div class="section-title">定制需求</div>
        <div class="requirements-content">{{ detail.invitation.requirements || '暂无详细描述' }}</div>
      </div>

      <div class="section" v-if="referenceImages && referenceImages.length > 0">
        <div class="section-title">参考图片</div>
        <div class="reference-images">
          <img v-for="(img, idx) in referenceImages" :key="idx" :src="img" class="ref-image" @click="previewImage(img)" />
        </div>
      </div>

      <div class="actions" v-if="canAccept || canReject || canStart || canComplete || canCancel">
        <el-button
          v-if="canAccept"
          type="success"
          :loading="actionLoading"
          @click="handleAccept"
        >
          接受邀约
        </el-button>
        <el-button
          v-if="canReject"
          type="danger"
          :loading="actionLoading"
          @click="showRejectDialog = true"
        >
          拒绝邀约
        </el-button>
        <el-button
          v-if="canStart"
          type="primary"
          :loading="actionLoading"
          @click="handleStart"
        >
          开始制作
        </el-button>
        <el-button
          v-if="canComplete"
          type="success"
          :loading="actionLoading"
          @click="handleComplete"
        >
          标记完成
        </el-button>
        <el-button
          v-if="canCancel"
          :loading="actionLoading"
          @click="handleCancel"
        >
          取消邀约
        </el-button>
      </div>
    </div>

    <div class="messages-card card">
      <div class="section-title">沟通留言</div>
      <div class="messages-list" ref="messagesListRef">
        <div
          v-for="msg in detail.messages"
          :key="msg.id"
          :class="['message-item', { 'is-mine': msg.senderId === userStore.userInfo?.id }]"
        >
          <img :src="getSenderAvatar(msg.senderId)" alt="" class="msg-avatar" />
          <div class="msg-content">
            <div class="msg-sender">{{ getSenderName(msg.senderId) }} · {{ formatDate(msg.createTime) }}</div>
            <div class="msg-text">{{ msg.content }}</div>
            <div v-if="getMsgImages(msg).length > 0" class="msg-images">
              <img v-for="(img, idx) in getMsgImages(msg)" :key="idx" :src="img" class="msg-image" @click="previewImage(img)" />
            </div>
          </div>
        </div>
        <div v-if="!detail.messages || detail.messages.length === 0" class="empty-msgs">
          暂无留言，开始沟通吧
        </div>
      </div>

      <div class="message-input">
        <el-input
          v-model="messageContent"
          type="textarea"
          :rows="3"
          placeholder="输入消息内容..."
          @keydown.enter.ctrl="sendMessage"
        />
        <div class="input-actions">
          <span class="tip">Ctrl + Enter 发送</span>
          <el-button type="primary" :loading="sendLoading" @click="sendMessage">
            发送
          </el-button>
        </div>
      </div>
    </div>

    <el-dialog v-model="showRejectDialog" title="拒绝邀约" width="400px">
      <el-input
        v-model="rejectReason"
        type="textarea"
        :rows="4"
        placeholder="请输入拒绝原因"
      />
      <template #footer>
        <el-button @click="showRejectDialog = false">取消</el-button>
        <el-button type="danger" :loading="actionLoading" @click="handleReject">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Right } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const detail = ref(null)
const messageContent = ref('')
const sendLoading = ref(false)
const actionLoading = ref(false)
const showRejectDialog = ref(false)
const rejectReason = ref('')
const messagesListRef = ref(null)

const referenceImages = computed(() => {
  if (!detail.value?.invitation?.referenceImages) return []
  try {
    return JSON.parse(detail.value.invitation.referenceImages)
  } catch (e) {
    return []
  }
})

const isCreator = computed(() => {
  return detail.value?.invitation?.creatorId === userStore.userInfo?.id
})

const isClient = computed(() => {
  return detail.value?.invitation?.clientId === userStore.userInfo?.id
})

const canAccept = computed(() => {
  return isCreator.value && detail.value?.invitation?.status === 0
})

const canReject = computed(() => {
  return isCreator.value && detail.value?.invitation?.status === 0
})

const canStart = computed(() => {
  return isCreator.value && detail.value?.invitation?.status === 1
})

const canComplete = computed(() => {
  return isCreator.value && detail.value?.invitation?.status === 3
})

const canCancel = computed(() => {
  return isClient.value && (detail.value?.invitation?.status === 0 || detail.value?.invitation?.status === 1)
})

function getStatusText(status) {
  const map = {
    0: '待接受',
    1: '已接受',
    2: '已拒绝',
    3: '进行中',
    4: '已完成',
    5: '已取消'
  }
  return map[status] || '未知'
}

function getStatusType(status) {
  const map = {
    0: 'warning',
    1: 'success',
    2: 'danger',
    3: 'primary',
    4: 'success',
    5: 'info'
  }
  return map[status] || 'info'
}

function formatBudget(min, max) {
  if (min && max) {
    return `¥${min} - ¥${max}`
  } else if (min) {
    return `¥${min} 起`
  } else if (max) {
    return `¥${max} 以内`
  }
  return '面议'
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

function getSenderAvatar(senderId) {
  if (senderId === detail.value?.client?.id) {
    return detail.value.client?.avatar || 'https://via.placeholder.com/40'
  }
  return detail.value?.creator?.avatar || 'https://via.placeholder.com/40'
}

function getSenderName(senderId) {
  if (senderId === detail.value?.client?.id) {
    return detail.value.client?.nickname || detail.value.client?.username || '客户'
  }
  return detail.value?.creator?.nickname || detail.value?.creator?.username || '创作者'
}

function getMsgImages(msg) {
  if (!msg.images) return []
  try {
    return JSON.parse(msg.images)
  } catch (e) {
    return []
  }
}

function previewImage(src) {
  window.open(src, '_blank')
}

async function loadDetail() {
  try {
    const res = await request.get(`/invitation/${route.params.id}`)
    if (res.code === 200 && res.data) {
      detail.value = res.data
      await nextTick()
      scrollToBottom()
    } else {
      ElMessage.error(res.message || '加载失败')
    }
  } catch (e) {
    console.error('加载邀约详情失败', e)
  }
}

async function scrollToBottom() {
  await nextTick()
  if (messagesListRef.value) {
    messagesListRef.value.scrollTop = messagesListRef.value.scrollHeight
  }
}

async function sendMessage() {
  if (!messageContent.value.trim()) {
    ElMessage.warning('请输入消息内容')
    return
  }
  sendLoading.value = true
  try {
    const res = await request.post(`/invitation/${route.params.id}/message`, {
      content: messageContent.value
    })
    if (res.code === 200 && res.data) {
      messageContent.value = ''
      detail.value.messages.push(res.data)
      await scrollToBottom()
    } else {
      ElMessage.error(res.message || '发送失败')
    }
  } catch (e) {
    console.error('发送消息失败', e)
  } finally {
    sendLoading.value = false
  }
}

async function handleAccept() {
  actionLoading.value = true
  try {
    const res = await request.put(`/invitation/${route.params.id}/accept`)
    if (res.code === 200) {
      ElMessage.success('已接受邀约')
      await loadDetail()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    console.error('接受邀约失败', e)
  } finally {
    actionLoading.value = false
  }
}

async function handleReject() {
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  actionLoading.value = true
  try {
    const res = await request.put(`/invitation/${route.params.id}/reject`, {
      rejectReason: rejectReason.value
    })
    if (res.code === 200) {
      ElMessage.success('已拒绝邀约')
      showRejectDialog.value = false
      rejectReason.value = ''
      await loadDetail()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    console.error('拒绝邀约失败', e)
  } finally {
    actionLoading.value = false
  }
}

async function handleStart() {
  actionLoading.value = true
  try {
    const res = await request.put(`/invitation/${route.params.id}/start`)
    if (res.code === 200) {
      ElMessage.success('已开始制作')
      await loadDetail()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    console.error('开始制作失败', e)
  } finally {
    actionLoading.value = false
  }
}

async function handleComplete() {
  try {
    await ElMessageBox.confirm('确认标记此邀约为已完成吗？', '提示', {
      type: 'warning'
    })
  } catch (e) {
    return
  }
  actionLoading.value = true
  try {
    const res = await request.put(`/invitation/${route.params.id}/complete`)
    if (res.code === 200) {
      ElMessage.success('邀约已完成')
      await loadDetail()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    console.error('完成邀约失败', e)
  } finally {
    actionLoading.value = false
  }
}

async function handleCancel() {
  try {
    await ElMessageBox.confirm('确认取消此邀约吗？取消后无法恢复。', '提示', {
      type: 'warning'
    })
  } catch (e) {
    return
  }
  actionLoading.value = true
  try {
    const res = await request.put(`/invitation/${route.params.id}/cancel`)
    if (res.code === 200) {
      ElMessage.success('已取消邀约')
      await loadDetail()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    console.error('取消邀约失败', e)
  } finally {
    actionLoading.value = false
  }
}

function goBack() {
  router.back()
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
.invitation-detail {
  padding: 30px 0;
}

.back-btn {
  margin-bottom: 16px;
  color: #667eea;
}

.detail-card {
  padding: 30px 40px;
  margin-bottom: 24px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.invitation-title {
  font-size: 24px;
  color: #333;
  margin: 0;
}

.parties {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 24px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}

.party {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.party-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  border: 3px solid #667eea;
}

.party-info {
  flex: 1;
}

.party-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.party-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.arrow-icon {
  font-size: 24px;
  color: #c0c4cc;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px 32px;
  margin-bottom: 24px;
}

.info-block {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.info-label {
  font-size: 13px;
  color: #909399;
}

.info-value {
  font-size: 16px;
  color: #333;
  font-weight: 500;
}

.info-value.danger {
  color: #f56c6c;
}

.section {
  margin-top: 24px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
  padding-left: 10px;
  border-left: 4px solid #667eea;
}

.requirements-content {
  padding: 16px 20px;
  background: #f5f7fa;
  border-radius: 8px;
  line-height: 1.8;
  color: #333;
  white-space: pre-wrap;
}

.reference-images {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.ref-image {
  width: 120px;
  height: 120px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.3s;
}

.ref-image:hover {
  transform: scale(1.05);
}

.actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.messages-card {
  padding: 30px 40px;
}

.messages-list {
  max-height: 500px;
  overflow-y: auto;
  padding: 20px 0;
  margin-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-item {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.message-item.is-mine {
  flex-direction: row-reverse;
}

.msg-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  flex-shrink: 0;
}

.msg-content {
  max-width: 70%;
}

.message-item.is-mine .msg-content {
  text-align: right;
}

.msg-sender {
  font-size: 12px;
  color: #909399;
  margin-bottom: 6px;
}

.msg-text {
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 12px;
  line-height: 1.6;
  color: #333;
  display: inline-block;
  text-align: left;
}

.message-item.is-mine .msg-text {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.msg-images {
  display: flex;
  gap: 8px;
  margin-top: 8px;
  flex-wrap: wrap;
}

.message-item.is-mine .msg-images {
  justify-content: flex-end;
}

.msg-image {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
}

.empty-msgs {
  text-align: center;
  color: #909399;
  padding: 40px 0;
}

.message-input {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tip {
  font-size: 12px;
  color: #909399;
}
</style>

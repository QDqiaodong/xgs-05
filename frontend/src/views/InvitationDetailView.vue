<template>
  <div class="invitation-detail container" v-if="detail">
    <el-button link @click="goBack" class="back-btn">
      <el-icon><ArrowLeft /></el-icon>
      返回邀约列表
    </el-button>

    <div class="detail-header card">
      <div class="header-main">
        <div class="title-row">
          <h1 class="invitation-title">{{ detail.invitation.title }}</h1>
          <el-tag :type="getStatusType(detail.invitation.status)" size="large" effect="dark" class="main-status-tag">
            <el-icon><component :is="getStatusIcon(detail.invitation.status)" /></el-icon>
            {{ getStatusText(detail.invitation.status) }}
          </el-tag>
        </div>
        <div class="parties-card">
          <div class="party-block client">
            <div class="party-role-label">发起方</div>
            <div class="party-info">
              <img :src="detail.client?.avatar || 'https://via.placeholder.com/60'" alt="" class="party-avatar" />
              <div class="party-details">
                <div class="party-name">{{ detail.client?.nickname || detail.client?.username || '匿名用户' }}</div>
                <div class="party-sub">客户</div>
              </div>
            </div>
          </div>
          <div class="flow-arrow">
            <div class="arrow-line"></div>
            <el-icon class="arrow-icon"><Right /></el-icon>
            <div class="arrow-line"></div>
          </div>
          <div class="party-block creator">
            <div class="party-role-label">承接方</div>
            <div class="party-info">
              <img :src="detail.creator?.avatar || 'https://via.placeholder.com/60'" alt="" class="party-avatar" />
              <div class="party-details">
                <div class="party-name">{{ detail.creator?.nickname || detail.creator?.username || '匿名用户' }}</div>
                <div class="party-sub">创作者</div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="info-cards">
        <div class="info-card budget">
          <div class="info-card-icon">💰</div>
          <div class="info-card-content">
            <div class="info-card-label">预算范围</div>
            <div class="info-card-value">{{ formatBudget(detail.invitation.budgetMin, detail.invitation.budgetMax) }}</div>
          </div>
        </div>
        <div class="info-card period">
          <div class="info-card-icon">📅</div>
          <div class="info-card-content">
            <div class="info-card-label">期望周期</div>
            <div class="info-card-value">{{ detail.invitation.expectedDays ? detail.invitation.expectedDays + ' 天' : '面议' }}</div>
          </div>
        </div>
        <div class="info-card created">
          <div class="info-card-icon">🕐</div>
          <div class="info-card-content">
            <div class="info-card-label">发起时间</div>
            <div class="info-card-value">{{ formatDate(detail.invitation.createTime) }}</div>
          </div>
        </div>
        <div class="info-card updated" v-if="detail.invitation.updateTime !== detail.invitation.createTime">
          <div class="info-card-icon">🔄</div>
          <div class="info-card-content">
            <div class="info-card-label">更新时间</div>
            <div class="info-card-value">{{ formatDate(detail.invitation.updateTime) }}</div>
          </div>
        </div>
      </div>
    </div>

    <div class="status-timeline card">
      <div class="timeline-title">
        <el-icon><Timer /></el-icon>
        状态流转追踪
      </div>
      <el-steps :active="timelineActiveStep" finish-status="success" process-status="process" align-center>
        <el-step title="发起邀约" :description="formatDate(detail.invitation.createTime)" icon="Edit" />
        <el-step
          v-if="detail.invitation.status >= 1 && detail.invitation.status !== 2 && detail.invitation.status !== 5"
          title="创作者已接受"
          :description="timelineSteps.acceptTime"
          icon="CircleCheck"
        />
        <el-step
          v-if="detail.invitation.status === 2"
          title="已拒绝"
          :description="detail.invitation.rejectReason || '创作者拒绝了此邀约'"
          status="error"
          icon="CircleClose"
        />
        <el-step
          v-if="detail.invitation.status === 5"
          title="已取消"
          description="客户取消了此邀约"
          status="info"
          icon="Delete"
        />
        <el-step
          v-if="detail.invitation.status >= 3 && detail.invitation.status !== 2 && detail.invitation.status !== 5"
          title="制作进行中"
          :description="timelineSteps.startTime"
          icon="Loading"
        />
        <el-step
          v-if="detail.invitation.status === 4"
          title="定制完成"
          :description="timelineSteps.completeTime"
          icon="Finished"
        />
      </el-steps>
      <div v-if="detail.invitation.rejectReason && detail.invitation.status === 2" class="reject-box">
        <el-alert type="error" :closable="false" show-icon>
          <template #title>
            <strong>拒绝原因：</strong>{{ detail.invitation.rejectReason }}
          </template>
        </el-alert>
      </div>
    </div>

    <div class="detail-grid">
      <div class="main-col">
        <div class="detail-section card">
          <div class="section-header">
            <div class="section-icon">📋</div>
            <div class="section-title">定制需求详情</div>
          </div>
          <div class="requirements-content">{{ detail.invitation.requirements || '暂无详细描述' }}</div>
        </div>

        <div class="detail-section card" v-if="referenceImages && referenceImages.length > 0">
          <div class="section-header">
            <div class="section-icon">🖼️</div>
            <div class="section-title">参考图片</div>
            <el-tag type="warning" effect="light" size="small">{{ referenceImages.length }} 张</el-tag>
          </div>
          <div class="reference-images">
            <el-image
              v-for="(img, idx) in referenceImages"
              :key="idx"
              :src="img"
              :preview-src-list="referenceImages"
              :initial-index="idx"
              fit="cover"
              class="ref-image"
              preview-teleported
            />
          </div>
        </div>

        <div class="action-section card" v-if="canAccept || canReject || canStart || canComplete || canCancel">
          <div class="section-header">
            <div class="section-icon">⚡</div>
            <div class="section-title">可执行操作</div>
          </div>
          <div class="action-buttons">
            <el-button
              v-if="canAccept"
              type="success"
              size="large"
              :loading="actionLoading"
              @click="handleAccept"
              class="action-btn success-btn"
            >
              <el-icon><CircleCheck /></el-icon>
              接受邀约
            </el-button>
            <el-button
              v-if="canReject"
              type="danger"
              size="large"
              :loading="actionLoading"
              @click="showRejectDialog = true"
              class="action-btn danger-btn"
            >
              <el-icon><CircleClose /></el-icon>
              拒绝邀约
            </el-button>
            <el-button
              v-if="canStart"
              type="primary"
              size="large"
              :loading="actionLoading"
              @click="handleStart"
              class="action-btn primary-btn"
            >
              <el-icon><Loading /></el-icon>
              开始制作
            </el-button>
            <el-button
              v-if="canComplete"
              type="success"
              size="large"
              :loading="actionLoading"
              @click="handleComplete"
              class="action-btn complete-btn"
            >
              <el-icon><Finished /></el-icon>
              标记完成
            </el-button>
            <el-button
              v-if="canCancel"
              size="large"
              :loading="actionLoading"
              @click="handleCancel"
              class="action-btn cancel-btn"
            >
              <el-icon><Delete /></el-icon>
              取消邀约
            </el-button>
          </div>
          <div class="action-tips">
            <el-icon><InfoFilled /></el-icon>
            <span>{{ actionTipText }}</span>
          </div>
        </div>
      </div>

      <div class="side-col">
        <div class="messages-card card">
          <div class="section-header sticky-header">
            <div class="section-icon">💬</div>
            <div class="section-title">沟通留言</div>
            <el-badge :value="detail.messages?.length || 0" class="msg-badge" />
          </div>
          <div class="messages-list" ref="messagesListRef">
            <div
              v-for="msg in detail.messages"
              :key="msg.id"
              :class="['message-item', { 'is-mine': msg.senderId === userStore.userInfo?.id }]"
            >
              <img :src="getSenderAvatar(msg.senderId)" alt="" class="msg-avatar" />
              <div class="msg-wrapper">
                <div class="msg-meta">
                  <span class="msg-sender">{{ getSenderName(msg.senderId) }}</span>
                  <span class="msg-time">{{ formatDate(msg.createTime) }}</span>
                </div>
                <div class="msg-bubble">
                  <div class="msg-text">{{ msg.content }}</div>
                </div>
                <div v-if="getMsgImages(msg).length > 0" class="msg-images">
                  <el-image
                    v-for="(img, idx) in getMsgImages(msg)"
                    :key="idx"
                    :src="img"
                    :preview-src-list="getMsgImages(msg)"
                    :initial-index="idx"
                    fit="cover"
                    class="msg-image"
                    preview-teleported
                  />
                </div>
              </div>
            </div>
            <div v-if="!detail.messages || detail.messages.length === 0" class="empty-msgs">
              <div class="empty-icon">💭</div>
              <p>暂无留言，开始沟通吧</p>
            </div>
          </div>

          <div class="message-input-area">
            <el-input
              v-model="messageContent"
              type="textarea"
              :rows="3"
              placeholder="输入消息内容，与对方沟通定制细节..."
              @keydown.enter.ctrl="sendMessage"
              resize="none"
            />
            <div class="input-footer">
              <span class="tip">
                <el-icon><Key /></el-icon>
                Ctrl + Enter 快捷发送
              </span>
              <el-button type="primary" :loading="sendLoading" @click="sendMessage" class="send-btn">
                <el-icon><Promotion /></el-icon>
                发送消息
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="showRejectDialog" title="拒绝邀约" width="480px" class="reject-dialog">
      <div class="reject-intro">
        <el-icon :size="24" color="#f56c6c"><Warning /></el-icon>
        <div>
          <div class="reject-title">请填写拒绝原因</div>
          <div class="reject-sub">这将帮助客户了解您的情况，便于后续改进和沟通</div>
        </div>
      </div>
      <el-input
        v-model="rejectReason"
        type="textarea"
        :rows="5"
        placeholder="请详细说明拒绝原因，如：档期已满、需求不匹配等..."
        maxlength="500"
        show-word-limit
      />
      <template #footer>
        <el-button @click="showRejectDialog = false" size="large">取消</el-button>
        <el-button type="danger" :loading="actionLoading" @click="handleReject" size="large">
          确认拒绝
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft, Right, Clock, CircleCheck, CircleClose, Loading, Finished, Delete,
  Promotion, InfoFilled, Warning, Timer, Key, Edit
} from '@element-plus/icons-vue'
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

const actionTipText = computed(() => {
  if (canAccept.value || canReject.value) {
    return '您有新的邀约待处理，请及时回复客户'
  }
  if (canStart.value) {
    return '客户已确认接受，请开始制作并随时沟通进度'
  }
  if (canComplete.value) {
    return '制作完成后请标记为完成，等待客户确认'
  }
  if (canCancel.value) {
    return '如有变动，您可以取消此次邀约（仅待接受/已接受状态可取消）'
  }
  return ''
})

const timelineActiveStep = computed(() => {
  const status = detail.value?.invitation?.status
  if (status === 0) return 0
  if (status === 2 || status === 5) return 1
  if (status === 1) return 1
  if (status === 3) return 2
  if (status === 4) return 3
  return 0
})

const timelineSteps = computed(() => {
  const updateTime = detail.value?.invitation?.updateTime ? formatDate(detail.value.invitation.updateTime) : '-'
  return {
    acceptTime: detail.value?.invitation?.status >= 1 ? updateTime : '—',
    startTime: detail.value?.invitation?.status >= 3 ? updateTime : '—',
    completeTime: detail.value?.invitation?.status === 4 ? updateTime : '—'
  }
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

function getStatusIcon(status) {
  const map = {
    0: Clock,
    1: CircleCheck,
    2: CircleClose,
    3: Loading,
    4: Finished,
    5: Delete
  }
  return map[status] || Clock
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
      ElMessage.success('已开始制作，记得随时与客户沟通进度')
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
    await ElMessageBox.confirm(
      '确认标记此邀约为已完成吗？完成后客户将收到通知。',
      '提示',
      { type: 'warning', confirmButtonText: '确认完成', cancelButtonText: '再想想' }
    )
  } catch (e) {
    return
  }
  actionLoading.value = true
  try {
    const res = await request.put(`/invitation/${route.params.id}/complete`)
    if (res.code === 200) {
      ElMessage.success('🎉 邀约已完成，辛苦了！')
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
    await ElMessageBox.confirm(
      '确认取消此邀约吗？取消后将无法恢复，创作者也会收到通知。',
      '提示',
      { type: 'warning', confirmButtonText: '确认取消', cancelButtonText: '再考虑' }
    )
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
  padding: 30px 0 60px;
}

.back-btn {
  margin-bottom: 16px;
  color: #667eea;
  font-size: 14px;
  font-weight: 500;
}

.detail-header {
  padding: 32px 36px;
  margin-bottom: 24px;
  background: linear-gradient(135deg, #fafbff 0%, #f0f4ff 100%);
  border: 1px solid #e0e7ff;
}

.title-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
  margin-bottom: 28px;
  flex-wrap: wrap;
}

.invitation-title {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  margin: 0;
  line-height: 1.3;
}

.main-status-tag {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 20px;
  font-size: 14px;
  font-weight: 500;
}

.parties-card {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  gap: 20px;
  align-items: center;
  background: #fff;
  border-radius: 16px;
  padding: 24px 28px;
  box-shadow: 0 2px 12px rgba(102, 126, 234, 0.08);
  margin-bottom: 28px;
}

.party-block {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.party-block.creator {
  text-align: right;
}

.party-block.creator .party-info {
  justify-content: flex-end;
}

.party-role-label {
  font-size: 12px;
  padding: 4px 12px;
  border-radius: 12px;
  display: inline-block;
  font-weight: 500;
  width: fit-content;
}

.party-block.client .party-role-label {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.party-block.creator .party-role-label {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  color: #fff;
  margin-left: auto;
}

.party-info {
  display: flex;
  align-items: center;
  gap: 14px;
}

.party-avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  border: 3px solid #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.party-block.creator .party-avatar {
  border-color: #d1fae5;
}

.party-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.party-name {
  font-size: 17px;
  font-weight: 600;
  color: #303133;
}

.party-sub {
  font-size: 13px;
  color: #909399;
}

.flow-arrow {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 12px;
}

.arrow-line {
  flex: 1;
  height: 2px;
  background: linear-gradient(90deg, #c7d2fe 0%, #86efac 100%);
  border-radius: 2px;
  min-width: 30px;
}

.arrow-icon {
  font-size: 24px;
  color: #667eea;
}

.info-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.info-card {
  display: flex;
  align-items: center;
  gap: 14px;
  background: #fff;
  border-radius: 12px;
  padding: 16px 18px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.04);
  transition: transform 0.2s, box-shadow 0.2s;
}

.info-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.info-card-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  flex-shrink: 0;
}

.info-card.budget .info-card-icon {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
}

.info-card.period .info-card-icon {
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
}

.info-card.created .info-card-icon {
  background: linear-gradient(135deg, #fce7f3 0%, #fbcfe8 100%);
}

.info-card.updated .info-card-icon {
  background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%);
}

.info-card-content {
  min-width: 0;
}

.info-card-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.info-card-value {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.status-timeline {
  padding: 28px 32px;
  margin-bottom: 24px;
}

.timeline-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 28px;
  padding-left: 10px;
  border-left: 4px solid #667eea;
}

.reject-box {
  margin-top: 24px;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 420px;
  gap: 24px;
  align-items: flex-start;
}

.main-col {
  display: flex;
  flex-direction: column;
  gap: 24px;
  min-width: 0;
}

.side-col {
  position: sticky;
  top: 90px;
}

.detail-section {
  padding: 28px 32px;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
}

.section-icon {
  font-size: 22px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  flex: 1;
}

.requirements-content {
  padding: 20px 24px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 12px;
  line-height: 1.9;
  color: #303133;
  white-space: pre-wrap;
  font-size: 14.5px;
  border: 1px solid #e2e8f0;
}

.reference-images {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}

.ref-image {
  width: 100%;
  aspect-ratio: 1;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
}

.ref-image:hover {
  transform: scale(1.03);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.action-section {
  padding: 28px 32px;
  background: linear-gradient(135deg, #fffbeb 0%, #fef3c7 20%, #fff 100%);
  border: 1px solid #fde68a;
}

.action-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  margin-bottom: 18px;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 14px 24px;
  font-size: 15px;
  font-weight: 500;
  border-radius: 12px;
  border: none;
  transition: all 0.3s;
}

.success-btn {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  box-shadow: 0 4px 14px rgba(17, 153, 142, 0.3);
}

.danger-btn {
  background: linear-gradient(135deg, #eb3349 0%, #f45c43 100%);
  box-shadow: 0 4px 14px rgba(235, 51, 73, 0.3);
}

.primary-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 4px 14px rgba(102, 126, 234, 0.35);
}

.complete-btn {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  color: #065f46;
  box-shadow: 0 4px 14px rgba(67, 233, 123, 0.35);
}

.cancel-btn {
  background: #fff;
  border: 1px solid #d1d5db !important;
  color: #6b7280;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.action-btn:hover {
  transform: translateY(-3px);
}

.success-btn:hover { box-shadow: 0 8px 22px rgba(17, 153, 142, 0.4); }
.danger-btn:hover { box-shadow: 0 8px 22px rgba(235, 51, 73, 0.4); }
.primary-btn:hover { box-shadow: 0 8px 22px rgba(102, 126, 234, 0.45); }
.complete-btn:hover { box-shadow: 0 8px 22px rgba(67, 233, 123, 0.45); }
.cancel-btn:hover { background: #f9fafb; transform: translateY(-2px); }

.action-tips {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 10px;
  color: #92400e;
  font-size: 13px;
}

.messages-card {
  padding: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  height: calc(100vh - 150px);
  min-height: 600px;
}

.sticky-header {
  padding: 20px 24px;
  border-bottom: 1px solid #ebeef5;
  background: #fff;
  z-index: 2;
  flex-shrink: 0;
}

.msg-badge {
  margin-left: auto;
}

.messages-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  background: #fafbfc;
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
  width: 38px;
  height: 38px;
  border-radius: 50%;
  flex-shrink: 0;
  border: 2px solid #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.msg-wrapper {
  max-width: 78%;
}

.message-item.is-mine .msg-wrapper {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.msg-meta {
  display: flex;
  gap: 10px;
  margin-bottom: 6px;
  font-size: 12px;
}

.message-item.is-mine .msg-meta {
  flex-direction: row-reverse;
}

.msg-sender {
  font-weight: 600;
  color: #303133;
}

.msg-time {
  color: #9ca3af;
}

.msg-bubble {
  display: inline-block;
}

.msg-text {
  padding: 12px 16px;
  background: #fff;
  border-radius: 14px;
  border-top-left-radius: 4px;
  line-height: 1.6;
  color: #303133;
  font-size: 14px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  white-space: pre-wrap;
  word-break: break-word;
}

.message-item.is-mine .msg-text {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-top-left-radius: 14px;
  border-top-right-radius: 4px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.25);
}

.msg-images {
  display: flex;
  gap: 8px;
  margin-top: 10px;
  flex-wrap: wrap;
}

.message-item.is-mine .msg-images {
  justify-content: flex-end;
}

.msg-image {
  width: 110px;
  height: 110px;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
}

.empty-msgs {
  text-align: center;
  color: #9ca3af;
  padding: 60px 20px;
}

.empty-msgs .empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.empty-msgs p {
  margin: 0;
  font-size: 14px;
}

.message-input-area {
  padding: 18px 24px 22px;
  border-top: 1px solid #ebeef5;
  background: #fff;
  flex-shrink: 0;
}

.input-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
}

.tip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #9ca3af;
}

.send-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  padding: 12px 22px;
  border-radius: 10px;
}

.send-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 18px rgba(102, 126, 234, 0.35);
}

.reject-dialog :deep(.el-dialog__header) {
  padding-bottom: 12px;
}

.reject-intro {
  display: flex;
  gap: 14px;
  padding: 16px 18px;
  background: #fef2f2;
  border-radius: 10px;
  margin-bottom: 18px;
}

.reject-title {
  font-size: 15px;
  font-weight: 600;
  color: #991b1b;
  margin-bottom: 4px;
}

.reject-sub {
  font-size: 13px;
  color: #b91c1c;
}

@media (max-width: 1200px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }

  .side-col {
    position: static;
  }

  .messages-card {
    height: auto;
    min-height: 500px;
  }

  .info-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .detail-header {
    padding: 24px 20px;
  }

  .invitation-title {
    font-size: 22px;
  }

  .parties-card {
    grid-template-columns: 1fr;
    gap: 16px;
    padding: 20px;
  }

  .flow-arrow {
    justify-content: center;
    padding: 4px 0;
  }

  .party-block.creator {
    text-align: left;
  }

  .party-block.creator .party-info {
    justify-content: flex-start;
  }

  .party-block.creator .party-role-label {
    margin-left: 0;
  }

  .info-cards {
    grid-template-columns: 1fr;
  }

  .detail-section,
  .action-section,
  .status-timeline {
    padding: 24px 20px;
  }

  .reference-images {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>

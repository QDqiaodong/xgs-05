<template>
  <div class="step-browser">
    <div class="step-header">
      <div class="step-progress-bar">
        <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
        <div
          v-for="(step, index) in steps"
          :key="index"
          class="progress-node"
          :class="{ active: index === currentStep, completed: index < currentStep }"
          @click="goToStep(index)"
        >
          <span class="node-number">{{ index + 1 }}</span>
        </div>
      </div>
      <div class="step-info">
        <span class="step-counter">步骤 {{ currentStep + 1 }} / {{ steps.length }}</span>
        <span class="step-percent">{{ Math.round(progressPercent) }}%</span>
      </div>
    </div>

    <div class="step-body">
      <div class="step-card card">
        <div class="step-card-header">
          <div class="step-badge">
            <span class="badge-number">{{ currentStep + 1 }}</span>
          </div>
          <h3 class="step-title">{{ currentStepData.title }}</h3>
        </div>

        <div class="step-card-content">
          <div class="step-image-wrapper">
            <img
              v-if="currentStepData.image && !currentStepImageError"
              :src="currentStepImage"
              :alt="'步骤' + (currentStep + 1)"
              class="step-image"
              @error="handleCurrentStepImageError"
              @load="handleCurrentStepImageLoad"
            />
            <div v-else class="step-image-placeholder">
              <el-icon :size="80" color="#c0c4cc"><Picture /></el-icon>
              <p>{{ currentStepImageError ? '图片加载失败' : '暂无步骤图片' }}</p>
            </div>
          </div>
          <div class="step-description">
            <p>{{ currentStepData.description }}</p>
            <div v-if="currentStepData.tips && currentStepData.tips.length > 0" class="step-tips">
              <h4>
                <el-icon color="#e6a23c"><Warning /></el-icon>
                小贴士
              </h4>
              <ul>
                <li v-for="(tip, idx) in currentStepData.tips" :key="idx">{{ tip }}</li>
              </ul>
            </div>
          </div>
        </div>

        <div class="step-card-footer">
          <el-button
            type="default"
            :disabled="currentStep === 0"
            :icon="ArrowLeft"
            @click="prevStep"
          >
            上一步
          </el-button>
          <el-button
            v-if="currentStep < steps.length - 1"
            type="primary"
            :icon="ArrowRight"
            @click="nextStep"
          >
            下一步
          </el-button>
          <el-button v-else type="success" :icon="CircleCheck" @click="onFinish">
            完成学习
          </el-button>
        </div>
      </div>
    </div>

    <div class="step-thumbnails">
      <div
        v-for="(step, index) in steps"
        :key="index"
        class="thumbnail-item"
        :class="{ active: index === currentStep, completed: index < currentStep }"
        @click="goToStep(index)"
      >
        <div class="thumbnail-image">
          <img v-if="step.image && !thumbnailErrors[index]" :src="getThumbnailImage(step)" :alt="'步骤' + (index + 1)" @error="handleThumbnailError(index)" />
          <div v-else class="thumbnail-placeholder">
            <el-icon :size="24" color="#c0c4cc"><Picture /></el-icon>
          </div>
          <div class="thumbnail-overlay">
            <span v-if="index < currentStep" class="overlay-check">
              <el-icon color="#fff"><CircleCheck /></el-icon>
            </span>
            <span v-else class="overlay-number">{{ index + 1 }}</span>
          </div>
        </div>
        <p class="thumbnail-title">{{ step.title }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ArrowLeft, ArrowRight, CircleCheck, Picture, Warning } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getSmallImage, getMediumImage } from '@/utils/image'

const props = defineProps({
  steps: {
    type: Array,
    required: true,
    default: () => []
  }
})

const emit = defineEmits(['finish'])

const currentStep = ref(0)
const thumbnailErrors = ref({})
const currentStepImageError = ref(false)

const progressPercent = computed(() => {
  if (!props.steps.length) return 0
  return ((currentStep.value + 1) / props.steps.length) * 100
})

const currentStepData = computed(() => {
  return props.steps[currentStep.value] || { title: '', description: '', image: '', tips: [] }
})

const currentStepImage = computed(() => {
  return getMediumImage(currentStepData.value.image)
})

const getThumbnailImage = (step) => {
  return getSmallImage(step.image)
}

function nextStep() {
  if (currentStep.value < props.steps.length - 1) {
    currentStep.value++
    currentStepImageError.value = false
  }
}

function prevStep() {
  if (currentStep.value > 0) {
    currentStep.value--
    currentStepImageError.value = false
  }
}

function goToStep(index) {
  if (index >= 0 && index < props.steps.length) {
    currentStep.value = index
    currentStepImageError.value = false
  }
}

function handleCurrentStepImageError() {
  currentStepImageError.value = true
}

function handleCurrentStepImageLoad() {
  currentStepImageError.value = false
}

function handleThumbnailError(index) {
  thumbnailErrors.value = { ...thumbnailErrors.value, [index]: true }
}

function onFinish() {
  ElMessage.success('恭喜你完成了所有步骤的学习！')
  emit('finish')
}
</script>

<style scoped>
.step-browser {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.step-header {
  padding: 20px 0;
}

.step-progress-bar {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  margin-bottom: 16px;
}

.step-progress-bar::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 20px;
  right: 20px;
  height: 4px;
  background: #e4e7ed;
  transform: translateY(-50%);
  border-radius: 2px;
  z-index: 0;
}

.progress-fill {
  position: absolute;
  top: 50%;
  left: 20px;
  height: 4px;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  transform: translateY(-50%);
  border-radius: 2px;
  z-index: 1;
  transition: width 0.3s ease;
}

.progress-node {
  position: relative;
  z-index: 2;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #fff;
  border: 3px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.progress-node:hover {
  transform: scale(1.1);
}

.progress-node.active {
  border-color: #667eea;
  background: #667eea;
  box-shadow: 0 0 0 6px rgba(102, 126, 234, 0.2);
}

.progress-node.completed {
  border-color: #667eea;
  background: #667eea;
}

.node-number {
  font-size: 14px;
  font-weight: 600;
  color: #909399;
}

.progress-node.active .node-number,
.progress-node.completed .node-number {
  color: #fff;
}

.step-info {
  display: flex;
  justify-content: space-between;
  padding: 0 20px;
}

.step-counter {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.step-percent {
  font-size: 14px;
  color: #667eea;
  font-weight: 600;
}

.step-body {
  min-height: 400px;
}

.step-card {
  padding: 0;
  overflow: hidden;
}

.step-card-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px 30px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf3 100%);
  border-bottom: 1px solid #ebeef5;
}

.step-badge {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.badge-number {
  font-size: 22px;
  font-weight: 700;
  color: #fff;
}

.step-title {
  font-size: 22px;
  color: #303133;
  margin: 0;
  font-weight: 600;
}

.step-card-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
  padding: 30px;
}

.step-image-wrapper {
  border-radius: 12px;
  overflow: hidden;
  background: #f5f7fa;
  min-height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.step-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.step-image-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #909399;
}

.step-image-placeholder p {
  font-size: 14px;
}

.step-description {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.step-description p {
  font-size: 15px;
  line-height: 1.9;
  color: #606266;
  margin: 0;
}

.step-tips {
  background: #fdf6ec;
  border-radius: 8px;
  padding: 16px 20px;
  border-left: 4px solid #e6a23c;
}

.step-tips h4 {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #e6a23c;
  margin: 0 0 12px 0;
}

.step-tips ul {
  margin: 0;
  padding-left: 20px;
}

.step-tips li {
  font-size: 13px;
  color: #906e1b;
  line-height: 1.8;
  margin-bottom: 4px;
}

.step-tips li:last-child {
  margin-bottom: 0;
}

.step-card-footer {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 20px 30px;
  border-top: 1px solid #ebeef5;
  background: #fafafa;
}

.step-card-footer .el-button {
  min-width: 120px;
  padding: 10px 24px;
  font-size: 14px;
}

.step-thumbnails {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 16px;
  padding: 20px 0;
}

.thumbnail-item {
  cursor: pointer;
  transition: all 0.3s ease;
}

.thumbnail-item:hover {
  transform: translateY(-4px);
}

.thumbnail-image {
  position: relative;
  width: 100%;
  aspect-ratio: 1;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f7fa;
  border: 2px solid transparent;
  transition: all 0.3s ease;
}

.thumbnail-item.active .thumbnail-image {
  border-color: #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.thumbnail-item.completed .thumbnail-image {
  border-color: #67c23a;
}

.thumbnail-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.thumbnail-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.thumbnail-overlay {
  position: absolute;
  top: 8px;
  left: 8px;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
}

.thumbnail-item.completed .thumbnail-overlay {
  background: #67c23a;
}

.thumbnail-item.active .thumbnail-overlay {
  background: #667eea;
}

.overlay-number {
  font-size: 13px;
  font-weight: 600;
  color: #fff;
}

.overlay-check {
  display: flex;
  align-items: center;
  justify-content: center;
}

.thumbnail-title {
  font-size: 13px;
  color: #606266;
  margin: 8px 0 0 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.thumbnail-item.active .thumbnail-title {
  color: #667eea;
  font-weight: 500;
}

@media (max-width: 768px) {
  .step-card-content {
    grid-template-columns: 1fr;
  }

  .step-card-header {
    padding: 20px;
  }

  .step-title {
    font-size: 18px;
  }

  .step-card-content {
    padding: 20px;
  }

  .step-image-wrapper {
    min-height: 200px;
  }

  .step-thumbnails {
    grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
    gap: 12px;
  }
}
</style>

<template>
  <div :class="['creator-level-badge', size]" :style="badgeStyle" :title="levelInfo.name">
    <span class="level-icon">{{ levelInfo.icon }}</span>
    <span v-if="showName" class="level-name">{{ levelInfo.name }}</span>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { getLevelInfo } from '@/utils/creatorLevel'

const props = defineProps({
  level: {
    type: Number,
    default: 1
  },
  size: {
    type: String,
    default: 'medium',
    validator: (val) => ['small', 'medium', 'large'].includes(val)
  },
  showName: {
    type: Boolean,
    default: true
  }
})

const levelInfo = computed(() => getLevelInfo(props.level))

const badgeStyle = computed(() => ({
  background: levelInfo.value.gradient,
  color: '#fff'
}))
</script>

<style scoped>
.creator-level-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px 10px;
  border-radius: 12px;
  font-weight: 500;
  white-space: nowrap;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.creator-level-badge.small {
  font-size: 12px;
  padding: 1px 8px;
  gap: 3px;
}

.creator-level-badge.small .level-icon {
  font-size: 12px;
}

.creator-level-badge.medium {
  font-size: 13px;
  padding: 3px 12px;
  gap: 5px;
}

.creator-level-badge.medium .level-icon {
  font-size: 14px;
}

.creator-level-badge.large {
  font-size: 15px;
  padding: 5px 16px;
  gap: 6px;
  border-radius: 16px;
}

.creator-level-badge.large .level-icon {
  font-size: 18px;
}

.level-icon {
  line-height: 1;
}

.level-name {
  line-height: 1;
}
</style>

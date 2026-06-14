<template>
  <span
    :class="['creator-verified-badge', size]"
    title="认证创作者"
  >
    <span class="verified-icon">✓</span>
    <span v-if="showName" class="verified-text">认证创作者</span>
  </span>
</template>

<script setup>
defineProps({
  size: {
    type: String,
    default: 'small',
    validator: (val) => ['small', 'medium', 'large'].includes(val)
  },
  showName: {
    type: Boolean,
    default: false
  }
})
</script>

<style scoped>
.creator-verified-badge {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  padding: 1px 8px;
  border-radius: 10px;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 50%, #ffd26f 100%);
  color: #fff;
  font-weight: 600;
  white-space: nowrap;
  box-shadow: 0 2px 6px rgba(245, 87, 108, 0.3);
  line-height: 1;
}

.creator-verified-badge.small {
  font-size: 11px;
  padding: 1px 6px;
}

.creator-verified-badge.small .verified-icon {
  font-size: 10px;
}

.creator-verified-badge.medium {
  font-size: 12px;
  padding: 2px 10px;
}

.creator-verified-badge.medium .verified-icon {
  font-size: 12px;
}

.creator-verified-badge.large {
  font-size: 14px;
  padding: 3px 14px;
  border-radius: 14px;
}

.creator-verified-badge.large .verified-icon {
  font-size: 15px;
}

.verified-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 14px;
  height: 14px;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  font-size: 9px;
  line-height: 1;
}

.creator-verified-badge.small .verified-icon {
  width: 12px;
  height: 12px;
}

.creator-verified-badge.medium .verified-icon {
  width: 14px;
  height: 14px;
}

.creator-verified-badge.large .verified-icon {
  width: 18px;
  height: 18px;
}
</style>

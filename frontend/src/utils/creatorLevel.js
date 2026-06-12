export const CREATOR_LEVELS = {
  1: { name: '学徒', icon: '🌱', color: '#909399', gradient: 'linear-gradient(135deg, #909399 0%, #c0c4cc 100%)' },
  2: { name: '匠人', icon: '🔨', color: '#67c23a', gradient: 'linear-gradient(135deg, #67c23a 0%, #85ce61 100%)' },
  3: { name: '熟练匠人', icon: '⚒️', color: '#409eff', gradient: 'linear-gradient(135deg, #409eff 0%, #66b1ff 100%)' },
  4: { name: '工艺师', icon: '🏆', color: '#e6a23c', gradient: 'linear-gradient(135deg, #e6a23c 0%, #f0c78a 100%)' },
  5: { name: '工艺大师', icon: '👑', color: '#f56c6c', gradient: 'linear-gradient(135deg, #f56c6c 0%, #f78989 100%)' }
}

export function getLevelInfo(level) {
  const lv = level || 1
  return CREATOR_LEVELS[lv] || CREATOR_LEVELS[1]
}

export function getLevelName(level) {
  return getLevelInfo(level).name
}

export function getLevelIcon(level) {
  return getLevelInfo(level).icon
}

export function getLevelColor(level) {
  return getLevelInfo(level).color
}

export function getLevelGradient(level) {
  return getLevelInfo(level).gradient
}

export function calculateLevelProgress(currentScore, currentLevel) {
  const levelThresholds = {
    1: { min: 0, max: 99 },
    2: { min: 100, max: 499 },
    3: { min: 500, max: 1999 },
    4: { min: 2000, max: 4999 },
    5: { min: 5000, max: Infinity }
  }

  const threshold = levelThresholds[currentLevel] || levelThresholds[1]
  if (currentLevel >= 5) {
    return 100
  }

  const range = threshold.max - threshold.min
  const progress = currentScore - threshold.min
  const percent = Math.min(100, Math.max(0, (progress / range) * 100))
  return Math.round(percent)
}

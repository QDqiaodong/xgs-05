import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '@/utils/request'
import { useUserStore } from './user'

export const useFavoriteStore = defineStore('favorite', () => {
  const favoriteWorkIds = ref(new Set())
  const version = ref(0)
  const loadingMap = ref(new Map())

  const userStore = useUserStore()

  const favoritedCount = computed(() => favoriteWorkIds.value.size)

  function isFavorited(workId) {
    return favoriteWorkIds.value.has(Number(workId))
  }

  function touchVersion() {
    version.value++
  }

  async function initFavorites(userId) {
    if (!userId) return
    try {
      let page = 1
      const size = 100
      let allIds = []
      while (true) {
        const res = await request.get(`/favorite/list/${userId}`, {
          params: { page, size }
        })
        if (res.code === 200 && res.data) {
          const records = res.data.records || []
          const ids = records.map(w => Number(w.id))
          allIds = allIds.concat(ids)
          if (records.length < size) break
          page++
        } else {
          break
        }
      }
      favoriteWorkIds.value = new Set(allIds)
      touchVersion()
    } catch (e) {
      console.warn('初始化收藏列表失败', e)
    }
  }

  async function checkFavorite(workId) {
    if (!userStore.userInfo?.id) return false
    try {
      const res = await request.get('/favorite/check', {
        params: {
          userId: userStore.userInfo.id,
          workId
        }
      })
      if (res.code === 200) {
        if (res.data) {
          favoriteWorkIds.value.add(Number(workId))
        } else {
          favoriteWorkIds.value.delete(Number(workId))
        }
        touchVersion()
        return res.data
      }
    } catch (e) {
      console.warn('检查收藏状态失败', e)
    }
    return favoriteWorkIds.value.has(Number(workId))
  }

  async function toggleFavorite(workId) {
    if (!userStore.userInfo?.id) {
      return { success: false, message: '请先登录' }
    }
    const wid = Number(workId)
    if (loadingMap.value.get(wid)) {
      return { success: false, message: '操作频繁，请稍后再试' }
    }
    loadingMap.value.set(wid, true)

    const currentlyFavorited = favoriteWorkIds.value.has(wid)
    const optimisticFavorited = !currentlyFavorited

    if (optimisticFavorited) {
      favoriteWorkIds.value.add(wid)
    } else {
      favoriteWorkIds.value.delete(wid)
    }
    touchVersion()

    try {
      let res
      if (optimisticFavorited) {
        res = await request.post('/favorite', {
          userId: userStore.userInfo.id,
          workId: wid
        })
      } else {
        res = await request.delete('/favorite', {
          params: {
            userId: userStore.userInfo.id,
            workId: wid
          }
        })
      }

      if (res.code !== 200) {
        if (currentlyFavorited) {
          favoriteWorkIds.value.add(wid)
        } else {
          favoriteWorkIds.value.delete(wid)
        }
        touchVersion()
        return { success: false, message: res.message || '操作失败' }
      }

      return {
        success: true,
        favorited: optimisticFavorited,
        message: optimisticFavorited ? '已收藏' : '已取消收藏'
      }
    } catch (e) {
      if (currentlyFavorited) {
        favoriteWorkIds.value.add(wid)
      } else {
        favoriteWorkIds.value.delete(wid)
      }
      touchVersion()
      return { success: false, message: e.message || '网络错误' }
    } finally {
      loadingMap.value.delete(wid)
    }
  }

  function reset() {
    favoriteWorkIds.value = new Set()
    version.value = 0
    loadingMap.value.clear()
  }

  return {
    favoriteWorkIds,
    version,
    favoritedCount,
    isFavorited,
    initFavorites,
    checkFavorite,
    toggleFavorite,
    reset,
    touchVersion
  }
})

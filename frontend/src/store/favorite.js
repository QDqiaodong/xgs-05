import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '@/utils/request'
import { useUserStore } from './user'

export const useFavoriteStore = defineStore('favorite', () => {
  const favoriteWorkIds = ref(new Set())
  const workFolderMap = ref(new Map())
  const folders = ref([])
  const foldersLoaded = ref(false)
  const version = ref(0)
  const loadingMap = ref(new Map())

  const userStore = useUserStore()

  const favoritedCount = computed(() => favoriteWorkIds.value.size)

  function isFavorited(workId) {
    return favoriteWorkIds.value.has(Number(workId))
  }

  function getWorkFolderIds(workId) {
    return workFolderMap.value.get(Number(workId)) || []
  }

  function touchVersion() {
    version.value++
  }

  async function loadFolders(force = false) {
    if (!userStore.userInfo?.id) return []
    if (foldersLoaded.value && !force) return folders.value
    try {
      const res = await request.get(`/favorite-folder/list/${userStore.userInfo.id}`)
      if (res.code === 200 && res.data) {
        folders.value = res.data
        foldersLoaded.value = true
        touchVersion()
      }
    } catch (e) {
      console.warn('加载收藏夹列表失败', e)
    }
    return folders.value
  }

  async function createFolder(data) {
    if (!userStore.userInfo?.id) return null
    try {
      const res = await request.post('/favorite-folder', {
        userId: userStore.userInfo.id,
        name: data.name,
        description: data.description || '',
        coverImage: data.coverImage || '',
        sort: data.sort || 0
      })
      if (res.code === 200 && res.data) {
        await loadFolders(true)
        return res.data
      }
    } catch (e) {
      console.warn('创建收藏夹失败', e)
    }
    return null
  }

  async function updateFolder(folderId, data) {
    try {
      const res = await request.put('/favorite-folder', {
        id: folderId,
        name: data.name,
        description: data.description,
        coverImage: data.coverImage,
        sort: data.sort
      })
      if (res.code === 200) {
        await loadFolders(true)
        return true
      }
    } catch (e) {
      console.warn('更新收藏夹失败', e)
    }
    return false
  }

  async function deleteFolder(folderId) {
    try {
      const res = await request.delete(`/favorite-folder/${folderId}`)
      if (res.code === 200) {
        await loadFolders(true)
        await initFavorites(userStore.userInfo?.id)
        return true
      }
    } catch (e) {
      console.warn('删除收藏夹失败', e)
    }
    return false
  }

  async function loadFolderWorks(folderId, page = 1, size = 20) {
    try {
      const res = await request.get(`/favorite-folder/works/${folderId}`, {
        params: { page, size }
      })
      if (res.code === 200) {
        return res.data
      }
    } catch (e) {
      console.warn('加载收藏夹作品失败', e)
    }
    return null
  }

  async function initFavorites(userId) {
    if (!userId) return
    try {
      await loadFolders(true)
      let page = 1
      const size = 100
      let allIds = []
      let allWorkFolders = new Map()
      while (true) {
        const res = await request.get(`/favorite/list/${userId}`, {
          params: { page, size }
        })
        if (res.code === 200 && res.data) {
          const records = res.data.records || []
          const ids = records.map(w => Number(w.id))
          allIds = allIds.concat(ids)
          for (const work of records) {
            const folderCheckRes = await request.get('/favorite/check', {
              params: { userId, workId: work.id }
            })
            if (folderCheckRes.code === 200 && folderCheckRes.data) {
              allWorkFolders.set(Number(work.id), folderCheckRes.data.folderIds || [])
            }
          }
          if (records.length < size) break
          page++
        } else {
          break
        }
      }
      favoriteWorkIds.value = new Set(allIds)
      workFolderMap.value = allWorkFolders
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
      if (res.code === 200 && res.data) {
        const favorited = res.data.favorited
        const folderIds = res.data.folderIds || []
        if (favorited) {
          favoriteWorkIds.value.add(Number(workId))
        } else {
          favoriteWorkIds.value.delete(Number(workId))
        }
        workFolderMap.value.set(Number(workId), folderIds)
        touchVersion()
        return favorited
      }
    } catch (e) {
      console.warn('检查收藏状态失败', e)
    }
    return favoriteWorkIds.value.has(Number(workId))
  }

  async function toggleFavorite(workId, folderId = null) {
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
      if (folderId) {
        const current = workFolderMap.value.get(wid) || []
        if (!current.includes(folderId)) {
          workFolderMap.value.set(wid, [...current, folderId])
        }
      }
    } else {
      favoriteWorkIds.value.delete(wid)
      workFolderMap.value.delete(wid)
    }
    touchVersion()

    try {
      let res
      if (optimisticFavorited) {
        const payload = {
          userId: userStore.userInfo.id,
          workId: wid
        }
        if (folderId) {
          payload.folderId = folderId
        }
        res = await request.post('/favorite', payload)
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

      await checkFavorite(wid)
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

  async function addToFolder(workId, folderId) {
    if (!userStore.userInfo?.id) {
      return { success: false, message: '请先登录' }
    }
    const wid = Number(workId)
    try {
      const res = await request.post('/favorite', {
        userId: userStore.userInfo.id,
        workId: wid,
        folderId: folderId
      })
      if (res.code === 200) {
        favoriteWorkIds.value.add(wid)
        const current = workFolderMap.value.get(wid) || []
        if (!current.includes(folderId)) {
          workFolderMap.value.set(wid, [...current, folderId])
        }
        touchVersion()
        return { success: true, message: '已添加到收藏夹' }
      }
      return { success: false, message: res.message || '添加失败' }
    } catch (e) {
      return { success: false, message: e.message || '网络错误' }
    }
  }

  async function removeFromFolder(workId, folderId) {
    if (!userStore.userInfo?.id) {
      return { success: false, message: '请先登录' }
    }
    const wid = Number(workId)
    try {
      const res = await request.delete('/favorite', {
        params: {
          userId: userStore.userInfo.id,
          workId: wid,
          folderId: folderId
        }
      })
      if (res.code === 200) {
        await checkFavorite(wid)
        return { success: true, message: '已从收藏夹移除' }
      }
      return { success: false, message: res.message || '移除失败' }
    } catch (e) {
      return { success: false, message: e.message || '网络错误' }
    }
  }

  async function moveToFolder(workId, fromFolderId, toFolderId) {
    if (!userStore.userInfo?.id) {
      return { success: false, message: '请先登录' }
    }
    const wid = Number(workId)
    try {
      const res = await request.post('/favorite/move', null, {
        params: {
          userId: userStore.userInfo.id,
          workId: wid,
          fromFolderId,
          toFolderId
        }
      })
      if (res.code === 200) {
        await checkFavorite(wid)
        return { success: true, message: '已移动收藏夹' }
      }
      return { success: false, message: res.message || '移动失败' }
    } catch (e) {
      return { success: false, message: e.message || '网络错误' }
    }
  }

  function reset() {
    favoriteWorkIds.value = new Set()
    workFolderMap.value = new Map()
    folders.value = []
    foldersLoaded.value = false
    version.value = 0
    loadingMap.value.clear()
  }

  return {
    favoriteWorkIds,
    workFolderMap,
    folders,
    foldersLoaded,
    version,
    favoritedCount,
    isFavorited,
    getWorkFolderIds,
    initFavorites,
    checkFavorite,
    toggleFavorite,
    addToFolder,
    removeFromFolder,
    moveToFolder,
    loadFolders,
    createFolder,
    updateFolder,
    deleteFolder,
    loadFolderWorks,
    reset,
    touchVersion
  }
})

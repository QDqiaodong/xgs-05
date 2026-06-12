import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref(null)
  const isLoggedIn = ref(false)
  const token = ref('')

  const isAdmin = computed(() => {
    return userInfo.value?.role === 2
  })

  function setUser(data) {
    userInfo.value = data.user
    token.value = data.token
    isLoggedIn.value = true
    localStorage.setItem('userInfo', JSON.stringify({ user: data.user, token: data.token }))
  }

  function logout() {
    userInfo.value = null
    token.value = ''
    isLoggedIn.value = false
    localStorage.removeItem('userInfo')
  }

  function initFromStorage() {
    const stored = localStorage.getItem('userInfo')
    if (stored) {
      try {
        const data = JSON.parse(stored)
        if (data.user && data.token) {
          userInfo.value = data.user
          token.value = data.token
          isLoggedIn.value = true
        } else {
          userInfo.value = data
          isLoggedIn.value = true
        }
      } catch (e) {
        userInfo.value = JSON.parse(stored)
        isLoggedIn.value = true
      }
    }
  }

  return { userInfo, isLoggedIn, token, isAdmin, setUser, logout, initFromStorage }
})

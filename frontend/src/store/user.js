import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref(null)
  const isLoggedIn = ref(false)

  function setUser(info) {
    userInfo.value = info
    isLoggedIn.value = true
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  function logout() {
    userInfo.value = null
    isLoggedIn.value = false
    localStorage.removeItem('userInfo')
  }

  function initFromStorage() {
    const stored = localStorage.getItem('userInfo')
    if (stored) {
      userInfo.value = JSON.parse(stored)
      isLoggedIn.value = true
    }
  }

  return { userInfo, isLoggedIn, setUser, logout, initFromStorage }
})

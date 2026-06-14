import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

request.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers['Authorization'] = `Bearer ${userStore.token}`
    } else {
      const userInfo = localStorage.getItem('userInfo')
      if (userInfo) {
        try {
          const data = JSON.parse(userInfo)
          if (data.token) {
            config.headers['Authorization'] = `Bearer ${data.token}`
          }
        } catch (e) {
          // ignore
        }
      }
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  (response) => {
    return response.data
  },
  (error) => {
    ElMessage.error(error.response?.data?.message || '请求失败')
    return Promise.reject(error)
  }
)

export function setWorkDifficulty(workId, difficultyLevel) {
  return request.put(`/work/${workId}/difficulty`, null, {
    params: { difficultyLevel }
  })
}

export function getMyVerificationStatus() {
  return request.get('/creator-verification/my-status')
}

export function getUserVerificationStatus(userId) {
  return request.get(`/creator-verification/user/${userId}/status`)
}

export function getVerificationList(params) {
  return request.get('/creator-verification/list', { params })
}

export function getVerificationDetail(id) {
  return request.get(`/creator-verification/${id}`)
}

export function approveVerification(id, remark) {
  return request.put(`/creator-verification/${id}/approve`, null, {
    params: { remark }
  })
}

export function rejectVerification(id, remark) {
  return request.put(`/creator-verification/${id}/reject`, null, {
    params: { remark }
  })
}

export default request

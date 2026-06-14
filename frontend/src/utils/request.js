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

export function getActivityList(params) {
  return request.get('/activity/list', { params })
}

export function getOngoingActivities(params) {
  return request.get('/activity/ongoing', { params })
}

export function getActivityDetail(id) {
  return request.get(`/activity/${id}`)
}

export function createActivity(data) {
  return request.post('/activity', data)
}

export function updateActivity(id, data) {
  return request.put(`/activity/${id}`, data)
}

export function deleteActivity(id) {
  return request.delete(`/activity/${id}`)
}

export function getActivityWorks(params) {
  return request.get('/activity-work/list', { params })
}

export function getMySubmissions(params) {
  return request.get('/activity-work/my', { params })
}

export function submitWorkToActivity(data) {
  return request.post('/activity-work/submit', data)
}

export function auditActivityWork(id, auditStatus, auditRemark) {
  return request.put(`/activity-work/${id}/audit`, null, {
    params: { auditStatus, auditRemark }
  })
}

export function voteActivityWork(data) {
  return request.post('/activity-vote/vote', data)
}

export function getMyVoteCount(activityId) {
  return request.get('/activity-vote/my-vote-count', { params: { activityId } })
}

export function checkHasVoted(activityId, activityWorkId) {
  return request.get('/activity-vote/has-voted', { params: { activityId, activityWorkId } })
}

export default request

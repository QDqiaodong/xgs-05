<template>
  <header class="header">
    <div class="container header-content">
      <router-link to="/" class="logo">
        <span class="logo-icon">🎨</span>
        <span class="logo-text">手作空间</span>
      </router-link>
      <nav class="nav">
        <router-link to="/" class="nav-item">首页</router-link>
        <div class="nav-item dropdown">
          <span>分类</span>
          <div class="dropdown-menu">
            <router-link v-for="cat in categories" :key="cat.id" :to="`/category/${cat.id}`" class="dropdown-item">
              {{ cat.name }}
            </router-link>
          </div>
        </div>
        <router-link to="/publish" class="nav-item publish-btn">发布作品</router-link>
      </nav>
      <div class="user-area">
        <template v-if="userStore.isLoggedIn">
          <router-link :to="`/profile/${userStore.userInfo?.id}`" class="user-avatar">
            <img :src="userStore.userInfo?.avatar || 'https://via.placeholder.com/40'" alt="avatar" />
          </router-link>
          <router-link to="/favorites" class="nav-item">收藏</router-link>
          <router-link to="/inspiration" class="nav-item">灵感画布</router-link>
          <span @click="handleLogout" class="nav-item logout">退出</span>
        </template>
        <template v-else>
          <el-button type="primary" @click="showLogin = true">登录</el-button>
        </template>
      </div>
    </div>
    <el-dialog v-model="showLogin" title="登录" width="400px">
      <el-form :model="loginForm" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="loginForm.username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="loginForm.password" type="password" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showLogin = false">取消</el-button>
        <el-button type="primary" :loading="loginLoading" @click="handleLogin">登录</el-button>
      </template>
    </el-dialog>
  </header>
</template>

<script setup>
import { ref } from 'vue'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const userStore = useUserStore()
const showLogin = ref(false)
const loginForm = ref({
  username: '',
  password: ''
})
const loginLoading = ref(false)

const categories = ref([
  { id: 1, name: '编织' },
  { id: 2, name: '陶艺' },
  { id: 3, name: '布艺' },
  { id: 4, name: '木艺' }
])

async function handleLogin() {
  if (!loginForm.value.username || !loginForm.value.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loginLoading.value = true
  try {
    const res = await request.post('/user/login', loginForm.value)
    if (res.code === 200 && res.data) {
      userStore.setUser(res.data)
      showLogin.value = false
      ElMessage.success('登录成功')
      loginForm.value = { username: '', password: '' }
    } else {
      ElMessage.error(res.message || '登录失败')
    }
  } catch (e) {
    console.error('登录失败', e)
  } finally {
    loginLoading.value = false
  }
}

function handleLogout() {
  userStore.logout()
  ElMessage.success('已退出登录')
}
</script>

<style scoped>
.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: bold;
  color: #fff;
}

.logo-icon {
  font-size: 28px;
}

.nav {
  display: flex;
  align-items: center;
  gap: 32px;
}

.nav-item {
  color: #fff;
  cursor: pointer;
  position: relative;
  padding: 8px 0;
  transition: opacity 0.3s;
}

.nav-item:hover {
  opacity: 0.8;
}

.publish-btn {
  background: rgba(255, 255, 255, 0.2);
  padding: 8px 20px;
  border-radius: 20px;
}

.dropdown {
  cursor: pointer;
}

.dropdown-menu {
  display: none;
  position: absolute;
  top: 100%;
  left: 0;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  min-width: 120px;
  overflow: hidden;
}

.dropdown:hover .dropdown-menu {
  display: block;
}

.dropdown-item {
  display: block;
  padding: 12px 16px;
  color: #333;
  transition: background 0.3s;
}

.dropdown-item:hover {
  background: #f5f5f5;
}

.user-area {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-avatar img {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.5);
}

.logout {
  font-size: 14px;
}
</style>

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import './assets/styles/global.scss'
import { useUserStore } from '@/store/user'
import { useInspirationStore } from '@/store/inspiration'
import { useFavoriteStore } from '@/store/favorite'

const app = createApp(App)
const pinia = createPinia()

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(pinia)
app.use(router)
app.use(ElementPlus)

const userStore = useUserStore(pinia)
userStore.initFromStorage()

const inspirationStore = useInspirationStore(pinia)
inspirationStore.init()

const favoriteStore = useFavoriteStore(pinia)
if (userStore.isLoggedIn && userStore.userInfo?.id) {
  favoriteStore.initFavorites(userStore.userInfo.id)
}

app.mount('#app')

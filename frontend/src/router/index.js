import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/HomeView.vue')
  },
  {
    path: '/work/:id',
    name: 'WorkDetail',
    component: () => import('@/views/WorkDetailView.vue')
  },
  {
    path: '/publish',
    name: 'Publish',
    component: () => import('@/views/PublishView.vue')
  },
  {
    path: '/profile/:userId',
    name: 'Profile',
    component: () => import('@/views/ProfileView.vue')
  },
  {
    path: '/favorites',
    name: 'Favorites',
    component: () => import('@/views/FavoritesView.vue')
  },
  {
    path: '/category/:categoryId',
    name: 'Category',
    component: () => import('@/views/CategoryView.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router

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
  },
  {
    path: '/inspiration',
    name: 'Inspiration',
    component: () => import('@/views/InspirationCanvasView.vue')
  },
  {
    path: '/invitations',
    name: 'Invitations',
    component: () => import('@/views/InvitationsView.vue')
  },
  {
    path: '/invitation/:id',
    name: 'InvitationDetail',
    component: () => import('@/views/InvitationDetailView.vue')
  },
  {
    path: '/admin/creator-verify',
    name: 'CreatorVerifyAdmin',
    component: () => import('@/views/CreatorVerifyAdmin.vue')
  },
  {
    path: '/activities',
    name: 'Activities',
    component: () => import('@/views/ActivitiesView.vue')
  },
  {
    path: '/activity/:id',
    name: 'ActivityDetail',
    component: () => import('@/views/ActivityDetailView.vue')
  },
  {
    path: '/admin/activity',
    name: 'ActivityAdmin',
    component: () => import('@/views/ActivityAdmin.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router

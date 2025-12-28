import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '../layout/MainLayout.vue'
import Login from '../views/Login.vue'
import Dashboard from '../views/Dashboard.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/',
    component: MainLayout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: Dashboard
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('../views/Dashboard.vue') // Placeholder
      },
      {
        path: 'partners',
        name: 'Partners',
        component: () => import('../views/Dashboard.vue') // Placeholder
      },
      {
        path: 'proposals',
        name: 'Proposals',
        component: () => import('../views/Dashboard.vue') // Placeholder
      },
      {
        path: 'tasks',
        name: 'Tasks',
        component: () => import('../views/Dashboard.vue') // Placeholder
      },
      {
        path: 'results',
        name: 'Results',
        component: () => import('../views/Dashboard.vue') // Placeholder
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.name !== 'Login' && !token) {
    next({ name: 'Login' })
  } else {
    next()
  }
})

export default router

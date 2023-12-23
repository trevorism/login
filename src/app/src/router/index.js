import { createRouter, createWebHistory } from 'vue-router'
import { useCookies } from 'vue3-cookies'

const { cookies } = useCookies()

import Login from '../components/Login.vue'
import ForgotPassword from '../components/ForgotPassword.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'Login',
      component: Login
    },
    {
      path: '/forgot',
      name: 'ForgotPassword',
      component: ForgotPassword
    }
  ]
})

export default router

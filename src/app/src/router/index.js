import { createRouter, createWebHistory } from 'vue-router'
import Login from '../components/Login.vue'
import ForgotPassword from '../components/ForgotPassword.vue'
import mixpanel from 'mixpanel-browser';

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

router.afterEach((to) => {
  mixpanel.track(to.fullPath)
})

export default router

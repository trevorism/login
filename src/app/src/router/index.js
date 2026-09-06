import { createRouter, createWebHistory } from 'vue-router'
import Login from '../components/Login.vue'
import Authorize from '../components/Authorize.vue'
import ForgotPassword from '../components/ForgotPassword.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/authorize',
      name: 'Authorize',
      component: Authorize
    },
    {
      path: '/forgot/:guid?',
      name: 'ForgotPassword',
      component: ForgotPassword,
      props: true
    },
    {
      path: '/:guid?',
      name: 'Login',
      component: Login,
      props: true
    }
  ]
})

export default router

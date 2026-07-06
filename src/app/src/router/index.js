import { createRouter, createWebHistory } from 'vue-router'
import Login from '../components/Login.vue'
import ForgotPassword from '../components/ForgotPassword.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/:guid?',
      name: 'Login',
      component: Login,
      props: true
    },
    {
      path: '/forgot/:guid?',
      name: 'ForgotPassword',
      component: ForgotPassword,
      props: true
    }
  ]
})

export default router

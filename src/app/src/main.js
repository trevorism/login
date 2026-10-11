import { createApp } from 'vue'
import axios from 'axios'

import App from './App.vue'
import router from './router'

import { TrevorismAuth } from '@trevorism/ui-auth'
import VueClickAway from 'vue3-click-away'
import { createVuestic } from 'vuestic-ui'
import config from '../vuestic.config.js'
import './style.css'

const app = createApp(App)
app.use(router)
app.use(TrevorismAuth, { router, loginPath: '/', axios: axios.create() })
app.use(VueClickAway)
app.use(createVuestic({ config }))
app.mount('#app')

<template>
  <Login v-if="showForm" :guid="guid" :redirect-uri="redirectUri" :state="state" />
  <div v-else class="authorize-status grid justify-items-center">
    <div v-if="checking" class="py-12">
      <VaInnerLoading :loading="true">
        <p class="px-8">Checking your session</p>
      </VaInnerLoading>
    </div>
    <va-alert v-else class="w-80 text-center my-12" color="danger">{{ errorMessage }}</va-alert>
  </div>
</template>

<script>
import axios from 'axios'
import Login from './Login.vue'

export default {
  name: 'Authorize',
  components: { Login },
  data() {
    return {
      checking: true,
      errorMessage: ''
    }
  },
  computed: {
    showForm() {
      return !this.checking && this.errorMessage.length === 0
    },
    redirectUri() {
      return this.$route.query.redirect_uri || ''
    },
    state() {
      return this.$route.query.state || ''
    },
    guid() {
      return this.$route.query.tenant || null
    }
  },
  mounted() {
    if (!this.redirectUri) {
      this.checking = false
      this.errorMessage = 'This login link is missing a redirect URI'
      return
    }
    axios
      .post('api/authorize', { redirectUri: this.redirectUri, state: this.state, guid: this.guid })
      .then((response) => {
        window.location.href = response.data.location
      })
      .catch((error) => {
        this.checking = false
        const status = error.response && error.response.status
        if (status === 400) {
          this.errorMessage = 'This application is not allowed to sign you in'
        } else if (status !== 401) {
          console.warn('Unable to check the existing session, falling back to the login form', error)
        }
      })
  }
}
</script>

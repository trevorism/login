<template>
  <div class="grid justify-items-center" id="prototype">
    <header-bar :local="false"></header-bar>
    <div id="login" class="container">
      <div class="grid justify-items-center">
        <h2 class="text-xl font-bold py-6 my-6">Login to Trevorism</h2>
        <div class="grid justify-items-right">
          <va-chip flat class="" to="/forgot">Forgot Password?</va-chip>
        </div>

        <va-form ref="loginForm" class="border-2 rounded-md w-80" tag="form" @submit.prevent="invokeButton">
          <div class="mx-4 mt-4 mb-4">
            <va-input
              v-model="username"
              class="mb-6 w-full"
              :rules="[(v) => v.length >= 3]"
              label="Username"
              minlength="3"
              type="text"
              autofocus
              required
              error-messages="Must be at least 3 characters"
            />
            <va-input
              v-model="password"
              class="mb-6 w-full"
              :rules="[(v) => v.length >= 6]"
              label="Password"
              minlength="6"
              type="password"
              required
              error-messages="Must be at least 6 characters"
            />

            <div class="grid justify-items-center">
              <va-button color="success" :disabled="disabled" type="submit">
                <VaInnerLoading :loading="disabled"> Submit </VaInnerLoading>
              </va-button>
            </div>
          </div>
        </va-form>
        <va-alert v-if="errorMessage.length > 0" class="w-80 text-center" color="danger">{{ errorMessage }}</va-alert>
      </div>
    </div>
  </div>
</template>

<script>
import HeaderBar from '@trevorism/ui-header-bar'
import axios from 'axios'
import mixpanel from 'mixpanel-browser';

export default {
  name: 'Login',
  components: { HeaderBar },
  data() {
    return {
      username: '',
      password: '',
      errorMessage: '',
      disabled: false
    }
  },
  mounted() {
    axios.get('api/authWarmup')
  },
  methods: {
    invokeButton: function () {
      let self = this
      let request = {
        username: this.username,
        password: this.password
      }
      this.disabled = true
      this.errorMessage = ''
      axios
        .post('api/login', request)
        .then(() => {
          this.disabled = false
          this.clear()
          mixpanel.identify(self.username)
          let returnUrl = self.$route.query.return_url
          if (returnUrl) {
            window.location.href = returnUrl
          } else {
            window.location.href = 'https://trevorism.com'
          }
        })
        .catch(() => {
          this.errorMessage = 'Unable to login'
          this.clear()
          this.disabled = false
        })
    },
    clear: function () {
      this.username = ''
      this.password = ''
      this.$refs.loginForm.reset()
    }
  }
}
</script>

<style scoped></style>

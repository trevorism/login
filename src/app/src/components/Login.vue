<template>
  <div class="grid justify-items-center" id="prototype">
    <header-bar :local="false"></header-bar>
    <div id="login" class="container">
      <div class="grid justify-items-center">
        <h2 class="text-xl font-bold py-6 my-6">Login to Trevorism</h2>
        <div class="grid justify-items-right">
          <va-chip flat class="" :to="{ name: 'ForgotPassword', params: { guid: guid } }">Forgot Password?</va-chip>
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
  <div class="grid justify-items-center">
    <h4 class="text-large font-bold py-6">-- or --</h4>
    <button class="my-2 gsi-material-button" style="width:215px" @click="loginMicrosoft">
      <div class="gsi-material-button-state"></div>
      <div class="gsi-material-button-content-wrapper">
        <div class="gsi-material-button-icon">
          <svg xmlns="http://www.w3.org/2000/svg" version="1.1" height="22" width="22" style="display: block;">
            <rect height="22" width="22" fill="#ffffff"/>
            <rect height="11" width="11" x="0" y="0" fill="#f8682c"/>
            <rect height="11" width="11" x="11" y="0" fill="#91c300"/>
            <rect height="11" width="11" x="0" y="11" fill="#00b4f1"/>
            <rect height="11" width="11" x="11" y="11" fill="#ffc300"/>
          </svg>
        </div>
        <span class="gsi-material-button-contents">Sign in with Microsoft</span>
        <span style="display: none;">Sign in with Microsoft</span>
      </div>
    </button>

    <button class="gsi-material-button" style="width:215px" @click="loginGoogle">
      <div class="gsi-material-button-state"></div>
      <div class="gsi-material-button-content-wrapper">
        <div class="gsi-material-button-icon">
          <svg version="1.1" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 48 48" xmlns:xlink="http://www.w3.org/1999/xlink" style="display: block;">
            <path fill="#EA4335" d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.72 17.74 9.5 24 9.5z"></path>
            <path fill="#4285F4" d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.78 7.18l7.73 6c4.51-4.18 7.09-10.36 7.09-17.65z"></path>
            <path fill="#FBBC05" d="M10.53 28.59c-.48-1.45-.76-2.99-.76-4.59s.27-3.14.76-4.59l-7.98-6.19C.92 16.46 0 20.12 0 24c0 3.88.92 7.54 2.56 10.78l7.97-6.19z"></path>
            <path fill="#34A853" d="M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.15 1.45-4.92 2.3-8.16 2.3-6.26 0-11.57-4.22-13.47-9.91l-7.98 6.19C6.51 42.62 14.62 48 24 48z"></path>
            <path fill="none" d="M0 0h48v48H0z"></path>
          </svg>
        </div>
        <span class="gsi-material-button-contents">Sign in with Google</span>
        <span style="display: none;">Sign in with Google</span>
      </div>
    </button>
  </div>
</template>

<script>
import HeaderBar from '@trevorism/ui-header-bar'
import axios from 'axios'
import {VaButton} from "vuestic-ui";

export default {
  props: ['guid'],
  inject: ['mixpanel'],
  name: 'Login',
  components: {VaButton, HeaderBar },
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
    loginGoogle: function() {
      let returnUrl = this.$route.query.return_url
      let url = this.guid ? 'api/google/' + this.guid : 'api/google'
      if(returnUrl) {
        url += '?return_url=' + encodeURIComponent(returnUrl)
      }
      axios.get(url)
          .then(response => {
            window.location.href = response.data
          })
          .catch(() => {
            this.errorMessage = 'Unable to login with Google'
          })
    },
    loginMicrosoft: function() {
      let returnUrl = this.$route.query.return_url
      let url = this.guid ? 'api/microsoft/' + this.guid : 'api/microsoft'
      if(returnUrl) {
        url += '?return_url=' + encodeURIComponent(returnUrl)
      }
      axios.get(url)
        .then(response => {
          window.location.href = response.data
        })
        .catch(() => {
          this.errorMessage = 'Unable to login with Microsoft'
        })
    },
    invokeButton: function () {
      let self = this
      let request = {
        username: this.username,
        password: this.password
      }
      this.disabled = true
      this.errorMessage = ''
      let url = this.guid ? 'api/login/' + this.guid : 'api/login'
      axios.post(url, request)
        .then(() => {
          this.disabled = false
          let returnUrl = self.$route.query.return_url
          // Non-essential side effects must never block the redirect on a
          // successful login, otherwise a thrown error here routes a valid
          // 200 into .catch() and shows "Unable to login".
          try {
            this.mixpanel.identify(self.username)
            this.clear()
          } catch (e) {
            console.warn('Post-login side effect failed', e)
          }
          window.location.href = returnUrl || 'https://trevorism.com'
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

<style scoped>
.gsi-material-button {
  -moz-user-select: none;
  -webkit-user-select: none;
  -ms-user-select: none;
  -webkit-appearance: none;
  background-color: WHITE;
  background-image: none;
  border: 1px solid #747775;
  -webkit-border-radius: 4px;
  border-radius: 4px;
  -webkit-box-sizing: border-box;
  box-sizing: border-box;
  color: #1f1f1f;
  cursor: pointer;
  font-family: 'Roboto', arial, sans-serif;
  font-size: 14px;
  height: 40px;
  letter-spacing: 0.25px;
  outline: none;
  overflow: hidden;
  padding: 0 12px;
  position: relative;
  text-align: center;
  -webkit-transition: background-color .218s, border-color .218s, box-shadow .218s;
  transition: background-color .218s, border-color .218s, box-shadow .218s;
  vertical-align: middle;
  white-space: nowrap;
  width: auto;
  max-width: 400px;
  min-width: min-content;
}

.gsi-material-button .gsi-material-button-icon {
  height: 20px;
  margin-right: 12px;
  min-width: 20px;
  width: 20px;
}

.gsi-material-button .gsi-material-button-content-wrapper {
  -webkit-align-items: center;
  align-items: center;
  display: flex;
  -webkit-flex-direction: row;
  flex-direction: row;
  -webkit-flex-wrap: nowrap;
  flex-wrap: nowrap;
  height: 100%;
  justify-content: space-between;
  position: relative;
  width: 100%;
}

.gsi-material-button .gsi-material-button-contents {
  -webkit-flex-grow: 1;
  flex-grow: 1;
  font-family: 'Roboto', arial, sans-serif;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  vertical-align: top;
}

.gsi-material-button .gsi-material-button-state {
  -webkit-transition: opacity .218s;
  transition: opacity .218s;
  bottom: 0;
  left: 0;
  opacity: 0;
  position: absolute;
  right: 0;
  top: 0;
}

.gsi-material-button:disabled {
  cursor: default;
  background-color: #ffffff61;
  border-color: #1f1f1f1f;
}

.gsi-material-button:disabled .gsi-material-button-contents {
  opacity: 38%;
}

.gsi-material-button:disabled .gsi-material-button-icon {
  opacity: 38%;
}

.gsi-material-button:not(:disabled):active .gsi-material-button-state,
.gsi-material-button:not(:disabled):focus .gsi-material-button-state {
  background-color: #303030;
  opacity: 12%;
}

.gsi-material-button:not(:disabled):hover {
  -webkit-box-shadow: 0 1px 2px 0 rgba(60, 64, 67, .30), 0 1px 3px 1px rgba(60, 64, 67, .15);
  box-shadow: 0 1px 2px 0 rgba(60, 64, 67, .30), 0 1px 3px 1px rgba(60, 64, 67, .15);
}

.gsi-material-button:not(:disabled):hover .gsi-material-button-state {
  background-color: #303030;
  opacity: 8%;
}

</style>

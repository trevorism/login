<template>
  <div id="forgot" class="grid justify-items-center">
    <header-bar :local=false></header-bar>
    <div class="grid justify-items-center">
      <h2 class=" text-xl font-bold py-6 my-6">Forgot Password on Trevorism</h2>
      <div class="mb-2 ml-2">We will send you a link to reset your password.</div>
    </div>
    <va-form class="border-2 rounded-md w-80" autofocus>
      <div class="mx-4 mt-4 mb-4">
        <va-input type="email"
                  class="mb-6 w-full"
                  required
                  label="Email"
                  autofocus
                  error-messages="Must be a valid email address"
                  v-model="email">
        </va-input>
        <div class="w-full flex justify-between">
          <va-button color="success" :disabled="disabled" @click="invokeButton">
            Submit
          </va-button>

          <va-button color="danger" to="/">
            Cancel
          </va-button>
        </div>
      </div>
    </va-form>
    <div v-if="successMessage !== ''" class="w-80 text-center">
      {{ successMessage }}
      <va-chip flat class="grid justify-items-center basis-1/4" to="/">Login</va-chip>
    </div>
    <va-alert v-if="errorMessage.length > 0" class="w-80 text-center" color="danger">{{ errorMessage }}</va-alert>
  </div>
</template>

<script>
import HeaderBar from '@trevorism/ui-header-bar'
import axios from 'axios'

export default {
  name: 'ForgotPassword',
  components: {HeaderBar},
  data() {
    return {
      email: '',
      disabled: false,
      errorMessage: '',
      successMessage: ''
    }
  },
  methods: {
    invokeButton: function () {
      let request = {
        email: this.email
      }
      this.disabled = true
      axios.post('api/login/forgot', request)
          .then(() => {
            this.disabled = false
            this.errorMessage = ''
            this.successMessage = 'Email sent successfully!'
          })
          .catch(() => {
            this.errorMessage = 'Unable to find the email address'
            this.successMessage = ''
            this.disabled = false
          })
    }
  }
}
</script>

<style scoped>
.loginBorder {
  border: #dddddd 1px solid;
  background: #efefef;
}

.formWidth {
  width: 400px
}
</style>

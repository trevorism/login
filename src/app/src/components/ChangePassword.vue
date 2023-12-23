<template>
  <div class="grid justify-items-center" id="change">
    <HeaderBar :local="false"></HeaderBar>
    <div class="container grid justify-items-center">
      <h2 class=" text-xl font-bold py-6 my-6">Change Password on Trevorism</h2>

      <va-form ref="changeForm" class="border-2 rounded-md w-80">
        <div class="mx-4 mt-4 mb-4">
          <va-input v-model="username"
                    type="text"
                    class="mb-6 w-full"
                    :rules="[(v) => v.length >= 3]"
                    required
                    error-messages="Must be at least 3 characters"
                    autofocus="true"
                    label="Username"
                    minlength="3">
          </va-input>
          <va-input type="password"
                    class="mb-6 w-full"
                    required
                    :rules="[(v) => v.length >= 6]"
                    error-messages="Must be at least 6 characters"
                    label="Current Password"
                    minlength="6"
                    v-model="currentPassword">
          </va-input>
          <va-input type="password"
                    class="mb-6 w-full"
                    required
                    :rules="[(v) => v.length >= 6]"
                    error-messages="Must be at least 6 characters"
                    label="Password"
                    minlength="6"
                    v-model="newPassword">
          </va-input>
          <va-input type="password"
                    class="mb-6 w-full"
                    required
                    :rules="[(v) => v.length >= 6, (v) => (v === this.newPassword)]"
                    error-messages="Must be at least 6 characters and match the new password"
                    label="Repeat Password"
                    minlength="6"
                    v-model="repeatPassword">
          </va-input>
          <div class="grid justify-items-center">
            <va-button color="success" :disabled="disabled" @click="invokeButton">
              <VaInnerLoading :loading="disabled">
                Submit
              </VaInnerLoading>
            </va-button>
          </div>
        </div>
      </va-form>
      <va-alert v-if="errorMsg.length > 0" class="w-80 text-center" color="danger">{{errorMsg}}</va-alert>

    </div>
  </div>
</template>

<script>
import HeaderBar from '@trevorism/ui-header-bar'
import axios from 'axios'

export default {
  name: 'ChangePassword',
  components: {HeaderBar},
  data () {
    return {
      username: '',
      currentPassword: '',
      newPassword: '',
      repeatPassword: '',
      errorMsg: '',
      disabled: false
    }
  },
  methods: {
    invokeButton: function () {
      let self = this

      if (this.newPassword !== this.repeatPassword) {
        this.errorMsg = 'The new and repeat passwords do not match'
        return
      }

      let request = {
        username: this.username,
        currentPassword: this.currentPassword,
        desiredPassword: this.repeatPassword
      }
      this.disabled = true
      this.errorMessage = ''
      axios.post('api/login/change', request)
        .then(() => {
          this.disabled = false
          this.clearFields()
          self.$router.push('/profile')
        })
        .catch(() => {
          this.errorMsg = 'Unable to change password'
          this.disabled = false
          this.clearFields()
        })
    },
    clearFields: function () {
      this.username = ''
      this.currentPassword = ''
      this.desiredPassword = ''
      this.repeatPassword = ''
      this.$refs.changeForm.reset();
    }
  }
}
</script>

<style scoped>

</style>

import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import axios from 'axios'
import ForgotPassword from '../src/components/ForgotPassword.vue'

vi.mock('axios', () => ({
  default: { get: vi.fn(), post: vi.fn() }
}))

const stubs = {
  'header-bar': true,
  'va-form': true,
  'va-input': true,
  'va-button': true,
  'va-inner-loading': true,
  'va-chip': true,
  'va-alert': true
}

const flush = () => new Promise((resolve) => setTimeout(resolve, 0))

function mountForgot(guid = null) {
  return mount(ForgotPassword, { props: { guid }, global: { stubs } })
}

describe('ForgotPassword.vue', () => {
  beforeEach(() => {
    axios.post.mockReset()
  })

  it('posts the email and tenant and shows a success message', async () => {
    const wrapper = mountForgot('tenant-9')
    await wrapper.setData({ email: 'a@trevorism.com' })
    axios.post.mockResolvedValueOnce({})

    await wrapper.vm.invokeButton()
    await flush()

    expect(axios.post).toHaveBeenCalledWith('api/login/forgot', {
      email: 'a@trevorism.com',
      tenantId: 'tenant-9'
    })
    expect(wrapper.vm.successMessage).toBe('Email sent successfully!')
    expect(wrapper.vm.errorMessage).toBe('')
    expect(wrapper.vm.disabled).toBe(false)
  })

  it('shows an error message on failure', async () => {
    const wrapper = mountForgot()
    await wrapper.setData({ email: 'missing@trevorism.com' })
    axios.post.mockRejectedValueOnce(new Error('404'))

    await wrapper.vm.invokeButton()
    await flush()

    expect(wrapper.vm.errorMessage).toBe('Unable to find the email address')
    expect(wrapper.vm.successMessage).toBe('')
    expect(wrapper.vm.disabled).toBe(false)
  })
})

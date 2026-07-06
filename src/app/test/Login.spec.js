import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import axios from 'axios'
import Login from '../src/components/Login.vue'

vi.mock('axios', () => ({
  default: { get: vi.fn(), post: vi.fn() }
}))

// va-form must expose reset() because clear() calls this.$refs.loginForm.reset().
const VaFormStub = { template: '<form><slot/></form>', methods: { reset() {} } }

const stubs = {
  'header-bar': true,
  'va-chip': true,
  'va-form': VaFormStub,
  'va-input': true,
  'va-button': true,
  'va-inner-loading': true,
  VaInnerLoading: true,
  'va-alert': true
}

function mountLogin({ guid = null, query = {} } = {}) {
  return mount(Login, {
    props: { guid },
    global: { stubs, mocks: { $route: { query } } }
  })
}

// Flush pending promise callbacks (microtasks) triggered by the mocked axios calls.
const flush = () => new Promise((resolve) => setTimeout(resolve, 0))

describe('Login.vue', () => {
  let originalLocation

  beforeEach(() => {
    axios.get.mockReset()
    axios.post.mockReset()
    axios.get.mockResolvedValue({ data: '' })
    axios.post.mockResolvedValue({})
    originalLocation = window.location
    Object.defineProperty(window, 'location', {
      configurable: true,
      writable: true,
      value: { href: '' }
    })
  })

  afterEach(() => {
    Object.defineProperty(window, 'location', {
      configurable: true,
      writable: true,
      value: originalLocation
    })
  })

  it('calls the warmup endpoint on mount', () => {
    mountLogin()
    expect(axios.get).toHaveBeenCalledWith('api/authWarmup')
  })

  it('loginGoogle requests the google endpoint and redirects', async () => {
    const wrapper = mountLogin()
    axios.get.mockResolvedValueOnce({ data: 'https://google-redirect' })

    await wrapper.vm.loginGoogle()
    await flush()

    expect(axios.get).toHaveBeenLastCalledWith('api/google')
    expect(window.location.href).toBe('https://google-redirect')
  })

  it('loginGoogle includes the guid and encoded return_url', async () => {
    const wrapper = mountLogin({ guid: 'abc', query: { return_url: 'https://ret.com/x?y=1' } })
    axios.get.mockResolvedValueOnce({ data: 'u' })

    await wrapper.vm.loginGoogle()
    await flush()

    expect(axios.get).toHaveBeenLastCalledWith(
      'api/google/abc?return_url=' + encodeURIComponent('https://ret.com/x?y=1')
    )
  })

  it('loginGoogle sets an error message on failure', async () => {
    const wrapper = mountLogin()
    axios.get.mockRejectedValueOnce(new Error('nope'))

    await wrapper.vm.loginGoogle()
    await flush()

    expect(wrapper.vm.errorMessage).toBe('Unable to login with Google')
  })

  it('loginMicrosoft requests the microsoft endpoint and redirects', async () => {
    const wrapper = mountLogin()
    axios.get.mockResolvedValueOnce({ data: 'https://ms-redirect' })

    await wrapper.vm.loginMicrosoft()
    await flush()

    expect(axios.get).toHaveBeenLastCalledWith('api/microsoft')
    expect(window.location.href).toBe('https://ms-redirect')
  })

  it('loginMicrosoft sets an error message on failure', async () => {
    const wrapper = mountLogin()
    axios.get.mockRejectedValueOnce(new Error('nope'))

    await wrapper.vm.loginMicrosoft()
    await flush()

    expect(wrapper.vm.errorMessage).toBe('Unable to login with Microsoft')
  })

  it('invokeButton posts credentials and redirects to the default url on success', async () => {
    const wrapper = mountLogin()
    await wrapper.setData({ username: 'alice', password: 'secret1' })
    axios.post.mockResolvedValueOnce({})

    await wrapper.vm.invokeButton()
    await flush()

    expect(axios.post).toHaveBeenCalledWith('api/login', { username: 'alice', password: 'secret1' })
    expect(window.location.href).toBe('https://trevorism.com')
    expect(wrapper.vm.disabled).toBe(false)
  })

  it('invokeButton posts to the tenant url and redirects to return_url', async () => {
    const wrapper = mountLogin({ guid: 't1', query: { return_url: 'https://return.example' } })
    await wrapper.setData({ username: 'alice', password: 'secret1' })
    axios.post.mockResolvedValueOnce({})

    await wrapper.vm.invokeButton()
    await flush()

    expect(axios.post).toHaveBeenCalledWith('api/login/t1', { username: 'alice', password: 'secret1' })
    expect(window.location.href).toBe('https://return.example')
  })

  it('invokeButton shows an error and re-enables the form on failure', async () => {
    const wrapper = mountLogin()
    await wrapper.setData({ username: 'alice', password: 'secret1' })
    axios.post.mockRejectedValueOnce(new Error('bad'))

    await wrapper.vm.invokeButton()
    await flush()

    expect(wrapper.vm.errorMessage).toBe('Unable to login')
    expect(wrapper.vm.disabled).toBe(false)
  })
})

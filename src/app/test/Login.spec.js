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

  it('invokeButton posts to the tenant url and redirects to a platform return_url', async () => {
    const wrapper = mountLogin({ guid: 't1', query: { return_url: 'https://certs.project.trevorism.com/report' } })
    await wrapper.setData({ username: 'alice', password: 'secret1' })
    axios.post.mockResolvedValueOnce({})

    await wrapper.vm.invokeButton()
    await flush()

    expect(axios.post).toHaveBeenCalledWith('api/login/t1', { username: 'alice', password: 'secret1' })
    expect(window.location.href).toBe('https://certs.project.trevorism.com/report')
  })

  it('invokeButton refuses a return_url that is not a platform host', async () => {
    const wrapper = mountLogin({ query: { return_url: 'https://evil.example.org' } })
    await wrapper.setData({ username: 'alice', password: 'secret1' })
    axios.post.mockResolvedValueOnce({})

    await wrapper.vm.invokeButton()
    await flush()

    expect(window.location.href).toBe('https://trevorism.com')
  })

  it('invokeButton sends the handoff details and follows the returned location', async () => {
    const wrapper = mount(Login, {
      props: { guid: null, redirectUri: 'https://certs.project.trevorism.com/api/auth/callback', state: 'abc' },
      global: { stubs, mocks: { $route: { query: {} } } }
    })
    await wrapper.setData({ username: 'alice', password: 'secret1' })
    axios.post.mockResolvedValueOnce({ data: { location: 'https://certs.project.trevorism.com/api/auth/callback?code=1000.s&state=abc' } })

    await wrapper.vm.invokeButton()
    await flush()

    expect(axios.post).toHaveBeenCalledWith('api/login', {
      username: 'alice',
      password: 'secret1',
      redirectUri: 'https://certs.project.trevorism.com/api/auth/callback',
      state: 'abc'
    })
    expect(window.location.href).toBe('https://certs.project.trevorism.com/api/auth/callback?code=1000.s&state=abc')
  })

  it('loginGoogle carries the handoff details instead of return_url', async () => {
    const wrapper = mount(Login, {
      props: { guid: null, redirectUri: 'https://certs.project.trevorism.com/api/auth/callback', state: 'abc' },
      global: { stubs, mocks: { $route: { query: { return_url: 'https://ignored.trevorism.com' } } } }
    })
    axios.get.mockResolvedValueOnce({ data: 'https://accounts.google.com/o/oauth2' })

    await wrapper.vm.loginGoogle()
    await flush()

    const requested = axios.get.mock.calls[0][0]
    expect(requested).toContain('redirect_uri=' + encodeURIComponent('https://certs.project.trevorism.com/api/auth/callback'))
    expect(requested).toContain('state=abc')
    expect(requested).not.toContain('return_url')
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

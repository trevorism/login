import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import axios from 'axios'
import Authorize from '../src/components/Authorize.vue'

vi.mock('axios', () => ({
  default: { get: vi.fn(), post: vi.fn() }
}))

const AlertStub = { template: '<div class="alert"><slot/></div>' }

const stubs = {
  Login: true,
  'va-alert': AlertStub,
  VaInnerLoading: true
}

const CALLBACK = 'https://certs.project.trevorism.com/api/auth/callback'

function mountAuthorize(query) {
  return mount(Authorize, { global: { stubs, mocks: { $route: { query } } } })
}

describe('Authorize', () => {
  let assign

  beforeEach(() => {
    assign = vi.fn()
    Object.defineProperty(window, 'location', { configurable: true, value: { href: '' } })
    vi.spyOn(window.location, 'href', 'set').mockImplementation(assign)
  })

  afterEach(() => {
    vi.clearAllMocks()
  })

  it('hands an existing session straight to the application', async () => {
    axios.post.mockResolvedValue({ data: { location: CALLBACK + '?code=1000.secret&state=abc' } })

    mountAuthorize({ redirect_uri: CALLBACK, state: 'abc' })
    await flushPromises()

    expect(axios.post).toHaveBeenCalledWith('api/authorize', {
      redirectUri: CALLBACK,
      state: 'abc',
      guid: null
    })
    expect(assign).toHaveBeenCalledWith(CALLBACK + '?code=1000.secret&state=abc')
  })

  it('passes the tenant through as a guid', async () => {
    axios.post.mockResolvedValue({ data: { location: CALLBACK } })

    mountAuthorize({ redirect_uri: CALLBACK, state: 'abc', tenant: 'guid-1' })
    await flushPromises()

    expect(axios.post.mock.calls[0][1].guid).toBe('guid-1')
  })

  it('shows the login form when there is no session', async () => {
    axios.post.mockRejectedValue({ response: { status: 401 } })

    const wrapper = mountAuthorize({ redirect_uri: CALLBACK, state: 'abc' })
    await flushPromises()

    expect(wrapper.findComponent({ name: 'Login' }).exists()).toBe(true)
    expect(assign).not.toHaveBeenCalled()
  })

  it('renders the login form unwrapped so the header bar can span the page', async () => {
    axios.post.mockRejectedValue({ response: { status: 401 } })

    const wrapper = mountAuthorize({ redirect_uri: CALLBACK, state: 'abc' })
    await flushPromises()

    expect(wrapper.find('.authorize-status').exists()).toBe(false)
    expect(wrapper.element).toBe(wrapper.findComponent({ name: 'Login' }).element)
  })

  it('passes the handoff details down to the login form', async () => {
    axios.post.mockRejectedValue({ response: { status: 401 } })

    const wrapper = mountAuthorize({ redirect_uri: CALLBACK, state: 'abc', tenant: 'guid-1' })
    await flushPromises()

    const login = wrapper.findComponent({ name: 'Login' })
    expect(login.props('redirectUri')).toBe(CALLBACK)
    expect(login.props('state')).toBe('abc')
    expect(login.props('guid')).toBe('guid-1')
  })

  it('refuses a redirect uri the platform does not allow', async () => {
    axios.post.mockRejectedValue({ response: { status: 400 } })

    const wrapper = mountAuthorize({ redirect_uri: 'https://evil.example.org/api/auth/callback' })
    await flushPromises()

    expect(wrapper.findComponent({ name: 'Login' }).exists()).toBe(false)
    expect(wrapper.text()).toContain('not allowed')
    expect(assign).not.toHaveBeenCalled()
  })

  it('does not call the backend without a redirect uri', async () => {
    const wrapper = mountAuthorize({})
    await flushPromises()

    expect(axios.post).not.toHaveBeenCalled()
    expect(wrapper.text()).toContain('missing a redirect URI')
  })
})

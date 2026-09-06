import { describe, it, expect } from 'vitest'
import router from '../src/router/index.js'

describe('router', () => {
  it('defines the Login and ForgotPassword routes', () => {
    const names = router.getRoutes().map((r) => r.name)
    expect(names).toContain('Login')
    expect(names).toContain('ForgotPassword')
  })

  it('the Login route accepts an optional guid and passes props', () => {
    const login = router.getRoutes().find((r) => r.name === 'Login')
    expect(login.path).toBe('/:guid?')
    expect(login.props).toBeTruthy()
  })

  it('the ForgotPassword route is nested under /forgot', () => {
    const forgot = router.getRoutes().find((r) => r.name === 'ForgotPassword')
    expect(forgot.path).toBe('/forgot/:guid?')
  })

  it('resolves a guid path param to the Login route', () => {
    const resolved = router.resolve('/abc123')
    expect(resolved.name).toBe('Login')
    expect(resolved.params.guid).toBe('abc123')
  })

  it('resolves /authorize to the Authorize route rather than treating it as a guid', () => {
    const resolved = router.resolve('/authorize')
    expect(resolved.name).toBe('Authorize')
    expect(resolved.params.guid).toBeUndefined()
  })

  it('keeps the query intact on the Authorize route', () => {
    const resolved = router.resolve('/authorize?redirect_uri=https%3A%2F%2Fa.trevorism.com%2Fapi%2Fauth%2Fcallback&state=abc')
    expect(resolved.name).toBe('Authorize')
    expect(resolved.query.redirect_uri).toBe('https://a.trevorism.com/api/auth/callback')
    expect(resolved.query.state).toBe('abc')
  })
})

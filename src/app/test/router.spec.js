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
})

const SESSION_MAX_AGE_SECONDS = 15 * 60
const REFRESH_MAX_AGE_SECONDS = 24 * 60 * 60
const CLOCK_TOLERANCE_SECONDS = 300

const ORIGIN = Cypress.config('baseUrl')

function expectExpiryNear(cookie, seconds) {
  const remaining = cookie.expiry - Math.floor(Date.now() / 1000)
  expect(remaining, `${cookie.name} lifetime`).to.be.closeTo(seconds, CLOCK_TOLERANCE_SECONDS)
}

function credentials() {
  return cy.env(['USERNAME', 'PASSWORD']).then(({ USERNAME, PASSWORD }) => {
    expect(USERNAME, 'CYPRESS_USERNAME must be set').to.be.a('string').and.not.be.empty
    expect(PASSWORD, 'CYPRESS_PASSWORD must be set').to.be.a('string').and.not.be.empty
    return { USERNAME, PASSWORD }
  })
}

function visitLoginReturningTo(path) {
  cy.visit('/?return_url=' + encodeURIComponent(ORIGIN + path))
}

function submitCredentials() {
  cy.intercept('POST', '**/api/login').as('loginAttempt')

  credentials().then(({ USERNAME, PASSWORD }) => {
    cy.get('#login input[type="text"]').type(USERNAME)
    cy.get('#login input[type="password"]').type(PASSWORD, { log: false })
  })

  cy.contains('button', 'Submit').click()
  cy.wait('@loginAttempt').its('response.statusCode').should('equal', 200)
}

describe('a successful login', () => {
  beforeEach(() => {
    cy.clearAllCookies()
  })

  it('issues a short lived, http only session cookie for the whole trevorism.com domain', () => {
    visitLoginReturningTo('/')
    submitCredentials()
    cy.location('search', { timeout: 20000 }).should('equal', '')

    cy.getCookie('session').should('not.be.null').then((cookie) => {
      expect(cookie.domain).to.equal('.trevorism.com')
      expect(cookie.path).to.equal('/')
      expect(cookie.httpOnly).to.be.true
      expect(cookie.secure).to.be.true
      expectExpiryNear(cookie, SESSION_MAX_AGE_SECONDS)
    })

    cy.getCookie('refresh_token').should('not.be.null').then((cookie) => {
      expect(cookie.domain).to.equal('.trevorism.com')
      expect(cookie.path).to.equal('/')
      expect(cookie.httpOnly).to.be.true
      expect(cookie.secure).to.be.true
      expect(cookie.value).to.not.be.empty
      expectExpiryNear(cookie, REFRESH_MAX_AGE_SECONDS)
    })
  })

  it('exposes the identity cookies that downstream apps read from the browser', () => {
    visitLoginReturningTo('/')
    submitCredentials()
    cy.location('search', { timeout: 20000 }).should('equal', '')

    credentials().then(({ USERNAME }) => {
      cy.getCookie('user_name').should('not.be.null').then((cookie) => {
        expect(cookie.domain).to.equal('.trevorism.com')
        expect(cookie.httpOnly).to.be.false
        expect(cookie.value).to.equal(USERNAME)
        expectExpiryNear(cookie, REFRESH_MAX_AGE_SECONDS)
      })
    })

    cy.getCookie('admin').should('not.be.null').then((cookie) => {
      expect(cookie.domain).to.equal('.trevorism.com')
      expect(cookie.httpOnly).to.be.false
      expect(cookie.value).to.be.oneOf(['true', 'false'])
    })
  })

  it('hands the browser off to the return url it was given', () => {
    visitLoginReturningTo('/forgot')
    submitCredentials()

    cy.location('pathname', { timeout: 20000 }).should('equal', '/forgot')
    cy.get('h2').should('contain.text', 'Forgot Password on Trevorism')
    cy.getCookie('session').should('not.be.null')
  })
})

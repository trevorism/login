describe('a rejected login', () => {
  beforeEach(() => {
    cy.clearAllCookies()
    cy.visit('/')
  })

  it('reports the failure and leaves no session behind', () => {
    cy.intercept('POST', '**/api/login').as('loginAttempt')

    cy.get('#login input[type="text"]').type('nosuchuser')
    cy.get('#login input[type="password"]').type('nosuchpassword', { log: false })
    cy.contains('button', 'Submit').click()

    cy.wait('@loginAttempt').its('response.statusCode').should('be.gte', 400)

    cy.contains('Unable to login').should('be.visible')
    cy.location('hostname').should('equal', 'login.auth.trevorism.com')

    cy.getCookie('session').should('be.null')
    cy.getCookie('refresh_token').should('be.null')
    cy.getCookie('user_name').should('be.null')
    cy.getCookie('admin').should('be.null')
  })
})

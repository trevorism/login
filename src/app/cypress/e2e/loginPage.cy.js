describe('the login page', () => {
  beforeEach(() => {
    cy.visit('/')
  })

  it('renders the login form', () => {
    cy.title().should('equal', 'Trevorism')
    cy.get('h2').should('contain.text', 'Login to Trevorism')
    cy.get('#login input[type="text"]').should('exist')
    cy.get('#login input[type="password"]').should('exist')
    cy.contains('button', 'Submit').should('be.visible')
  })

  it('offers the federated identity providers', () => {
    cy.contains('Sign in with Google').should('be.visible')
    cy.contains('Sign in with Microsoft').should('be.visible')
  })

  it('routes to the forgot password page and back', () => {
    cy.contains('Forgot Password?').click()

    cy.location('pathname').should('match', /^\/forgot\/?$/)
    cy.get('h2').should('contain.text', 'Forgot Password on Trevorism')
    cy.get('#forgot input[type="email"]').should('exist')

    cy.contains('Cancel').click()

    cy.location('pathname').should('match', /^\/?$/)
    cy.get('h2').should('contain.text', 'Login to Trevorism')
  })
})

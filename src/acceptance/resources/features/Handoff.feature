Feature: Handing a session to another Trevorism app
  The authorize and logout routes gate on the redirect allowlist, so neither can
  be used to move a session or a visitor somewhere the platform does not trust.

  Scenario: Authorizing without a session is refused
    Given the application is alive
    When an authorize request is made for "https://certs.project.trevorism.com/api/auth/callback" with no cookies
    Then the authorize request is unauthorized

  Scenario: Authorizing to a redirect URI outside the allowlist is refused
    Given the application is alive
    When an authorize request is made for "https://evil.example.org/api/auth/callback" with no cookies
    Then the authorize request is rejected

  Scenario: Logging out returns to the home page rather than an untrusted host
    Given the application is alive
    When a logout is requested with a return to "https://evil.example.org"
    Then the logout sends the browser to "https://trevorism.com"

  Scenario: Logging out returns to a platform host
    Given the application is alive
    When a logout is requested with a return to "https://certs.project.trevorism.com"
    Then the logout sends the browser to "https://certs.project.trevorism.com"

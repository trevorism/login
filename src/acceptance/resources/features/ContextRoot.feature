Feature: Context Root of this API
  In order to use the API, it must be available

  Scenario: HTTP GET on the ContextRoot
    Given the application is alive
    When I navigate to https://login.auth.trevorism.com/api
    Then the API returns a link to the help page

  Scenario: Ping
    Given the application is alive
    When I navigate to /ping on https://login.auth.trevorism.com/api
    Then pong is returned, to indicate the service is alive
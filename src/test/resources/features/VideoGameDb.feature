Feature: Video Game Database API

  Scenario Outline: Manage video games
    When I <action> a video game with name "<name>" and id "<id>"
    Then the video game should be <result> successfully

    Examples:
      | action  | name        | id | result  |
      | create  | Super Mario | 1  | created |
      | delete  |             | 1  | deleted |

  Scenario: Retrieve all video games
    When I request all video games
    Then I should receive a list of video games

  Scenario: Retrieve a video game by ID
    When I request a video game with ID "1"
    Then I should receive the video game details with ID "1"

  Scenario: Update a video game by ID
    When I update a video game with ID "1" and new name "Mario"
    Then the video game details should reflect the updates with name "Mario"
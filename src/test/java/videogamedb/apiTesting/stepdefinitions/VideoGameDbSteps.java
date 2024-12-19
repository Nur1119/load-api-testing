package videogamedb.apiTesting.stepdefinitions;

import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import videogamedb.apiTesting.utils.RequestBodyBuilder;

import java.util.Random;

import static org.hamcrest.Matchers.*;

public class VideoGameDbSteps {
    private Response response;
    private final Random random = new Random();
    private String currentGameName;

    @When("I {word} a video game with name {string} and id {string}")
    public void i_manage_a_video_game(String action, String name, String id) {
        this.currentGameName = name;

        if ("create".equals(action)) {
            JSONObject randomGame = getRandomGame();
            String requestBody = RequestBodyBuilder.buildCreateGameBody(Hooks.getNewGameTemplate(), id, name, randomGame);

            response = Hooks.request
                    .contentType("application/json")
                    .body(requestBody)
                    .post("/videogame");
        } else if ("delete".equals(action)) {
            response = Hooks.request.delete("/videogame/" + id);
        }
    }

    private JSONObject getRandomGame() {
        JSONArray gameData = Hooks.getGameData();
        return gameData.getJSONObject(random.nextInt(gameData.length()));
    }

    @Then("the video game should be {word} successfully")
    public void the_video_game_should_be_managed_successfully(String result) {
        response.then().statusCode(200);
        if ("created".equals(result)) {
            response.then().body("name", equalTo(currentGameName));
        } else if ("deleted".equals(result)) {
            response.then().body(equalTo("Video game deleted"));
        }
    }

    @When("I request all video games")
    public void i_request_all_video_games() {
        response = Hooks.request.get("/videogame");
    }

    @Then("I should receive a list of video games")
    public void i_should_receive_a_list_of_video_games() {
        response.then()
                .statusCode(200)
                .body("$", hasSize(greaterThan(0)));
    }

    @When("I request a video game with ID {string}")
    public void i_request_a_video_game_with_id(String id) {
        response = Hooks.request.get("/videogame/" + id);
    }

    @Then("I should receive the video game details with ID {string}")
    public void i_should_receive_the_video_game_details(String id) {
        response.then()
                .statusCode(200)
                .body("id", equalTo(Integer.parseInt(id)));
    }

    @When("I update a video game with ID {string} and new name {string}")
    public void i_update_a_video_game_with_id_and_new_name(String id, String newName) {
        String updateBody = RequestBodyBuilder.buildUpdateGameBody(id, newName);

        response = Hooks.request
                .contentType("application/json")
                .body(updateBody)
                .put("/videogame/" + id);
    }

    @Then("the video game details should reflect the updates with name {string}")
    public void the_video_game_details_should_reflect_the_updates(String expectedName) {
        response.then()
                .statusCode(200)
                .body("name", equalTo(expectedName));
    }
}

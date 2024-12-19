package videogamedb.apiTesting.stepdefinitions;

import io.cucumber.java.Before;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.json.JSONArray;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Hooks {
    private static final String BASE_URI = "https://videogamedb.uk/api";
    private static final String GAME_DATA_FILE_PATH = "src/test/resources/data/gameJsonFile.json";
    private static final String NEW_GAME_TEMPLATE_PATH = "src/test/resources/bodies/newGameTemplate.json";
    private static JSONArray gameData;
    private static String newGameTemplate;
    public static RequestSpecification request;

    @Before
    public void setUp() throws IOException {
        RestAssured.baseURI = BASE_URI;
        request = RestAssured.given();
        loadGameData();
        loadNewGameTemplate();
        authenticate();
    }

    private void loadGameData() throws IOException {
        String content = new String(Files.readAllBytes(
                Paths.get(GAME_DATA_FILE_PATH)
        ));
        gameData = new JSONArray(content);
    }

    private void loadNewGameTemplate() throws IOException {
        newGameTemplate = new String(Files.readAllBytes(
                Paths.get(NEW_GAME_TEMPLATE_PATH)
        ));
    }

    private void authenticate() {
        // Так как Background требует аутентификацию, мы можем сделать её прямо здесь
        // Если вам нужна аутентификация для всех сценариев, логичнее делать её в хуке:
        String body = "{\"password\": \"admin\", \"username\": \"admin\"}";
        var authResponse = request
                .contentType("application/json")
                .body(body)
                .post("/authenticate");

        authResponse.then().statusCode(200);
        String authToken = authResponse.jsonPath().getString("token");

        if (authToken == null || authToken.isEmpty()) {
            throw new RuntimeException("Failed to obtain authentication token");
        }

        request = RestAssured.given().header("Authorization", "Bearer " + authToken);
    }

    public static JSONArray getGameData() {
        return gameData;
    }

    public static String getNewGameTemplate() {
        return newGameTemplate;
    }
}


package videogamedb.apiTesting.utils;

import org.json.JSONObject;

public class RequestBodyBuilder {
    public static String buildCreateGameBody(String template, String id, String name, JSONObject randomGame) {
        return template
                .replace("#{id}", id)
                .replace("#{name}", name)
                .replace("#{releaseDate}", randomGame.getString("releaseDate"))
                .replace("#{reviewScore}", String.valueOf(randomGame.getInt("reviewScore")))
                .replace("#{category}", randomGame.getString("category"))
                .replace("#{rating}", randomGame.getString("rating"));
    }

    public static String buildUpdateGameBody(String id, String newName) {
        JSONObject updateGameRequest = new JSONObject();
        updateGameRequest.put("id", Integer.parseInt(id));
        updateGameRequest.put("name", newName);
        updateGameRequest.put("releaseDate", "2023-10-10");
        updateGameRequest.put("reviewScore", 85);
        updateGameRequest.put("category", "Adventure");
        updateGameRequest.put("rating", "Mature");
        return updateGameRequest.toString();
    }
}


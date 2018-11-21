package Pubg.Api.Client;

import Pubg.Api.Models.Player;
import Pubg.Api.Models.Season.Season;
import Util.ConfigHandler;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class PubgApiClient {
    private HttpURLConnection connection;
    private String apiKey = ConfigHandler.getConfig("api.key");
    private String API_BASE_URL = "https://api.pubg.com/shards/steam/";
    private JsonParser parser = new JsonParser();

    /*
     * Retrieve the player ID which is used as the key in all other API calls.
     * Pubg API allows you to search up to 6 players at once, I'm just assuming we will always do 1
     * and skipping implementing the ability to search for many.
     */
    public String getPlayerIdFromName(String playerName) throws Exception
    {
        URL url = new URL(API_BASE_URL + "players?filter[playerNames]=" + playerName);
        activateConnection(url);

        JsonObject playerJson = (JsonObject) parser.parse(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
        Type listType = new TypeToken<ArrayList<Player>>(){}.getType();
        List<Player> players = new Gson().fromJson(playerJson.get("data"), listType);

        System.out.println(players.get(0).getId());
        return players.get(0).getId();
    }

    /*
     * Retrieve the current season ID used in getting player stats.
     * Assumes we don't care about our past. Because we are always moving forward
     * getting better, faster, stronger. We are great men.
     */
    public String getLatestSeasonId() throws Exception
    {
        URL url = new URL(API_BASE_URL + "seasons");
        activateConnection(url);

        JsonObject seasonJson = (JsonObject) parser.parse(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
        Type listType = new TypeToken<ArrayList<Season>>(){}.getType();
        List<Season> seasons = new Gson().fromJson(seasonJson.get("data"), listType);

        for (Season season : seasons) {
            if (season.getSeasonAttributes().getIsCurrentSeason()) {
                return season.getId();
            }
        }
        throw new Exception("No current season found.");
    }

    private void activateConnection(URL url) throws Exception
    {
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", apiKey);
        connection.setRequestProperty("Accept", "application/vnd.api+json");
        connection.connect();
    }
}

package Pubg.Api.Models.PlayerSeason;

import com.google.gson.annotations.SerializedName;

public class PlayerSeasonAttributes {

    @SerializedName("gameModeStats")
    private GameModeHandler gameModeHandler;

    public GameModeHandler getGameModeHandler()
    {
        return gameModeHandler;
    }
}

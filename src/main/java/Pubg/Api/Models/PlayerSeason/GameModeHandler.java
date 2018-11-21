package Pubg.Api.Models.PlayerSeason;

import com.google.gson.annotations.SerializedName;

public class GameModeHandler {

    @SerializedName("duo-fpp")
    private GameModeStats duos;

    @SerializedName("squad-fpp")
    private GameModeStats squads;

    public GameModeStats getDuos()
    {
        return duos;
    }

    public GameModeStats getSquads()
    {
        return squads;
    }
}

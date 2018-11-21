package Pubg.Api.Models.PlayerSeason;

import com.google.gson.annotations.SerializedName;

public class PlayerSeason {

    @SerializedName("attributes")
    private PlayerSeasonAttributes playerSeasonAttributes;

    public PlayerSeasonAttributes getPlayerSeasonAttributes()
    {
        return playerSeasonAttributes;
    }

}

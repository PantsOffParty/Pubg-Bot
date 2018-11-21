package Pubg.Api.Models.Season;

import com.google.gson.annotations.SerializedName;

/*
 * Map of the Season response.
 */
public class Season {

    private String id;

    @SerializedName("attributes")
    private SeasonAttributes seasonAttributes;

    public String getId()
    {
        return id;
    }

    public SeasonAttributes getSeasonAttributes()
    {
        return seasonAttributes;
    }

}

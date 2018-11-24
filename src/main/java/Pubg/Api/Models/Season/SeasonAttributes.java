package Pubg.Api.Models.Season;

/*
 * Mapping of the seasons attributes,
 * required to find out if it is the current season.
 */
public class SeasonAttributes {

    private boolean isCurrentSeason;
    private boolean isOffSeason;

    public boolean getIsCurrentSeason() {
        return isCurrentSeason;
    }

}

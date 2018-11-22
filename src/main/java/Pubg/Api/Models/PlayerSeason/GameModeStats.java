package Pubg.Api.Models.PlayerSeason;

import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Field;

public class GameModeStats {

    private double assists;
    private float bestRankPoint;
    private double boosts;

    @SerializedName("dBNOs")
    private double dbnos;
    private double dailyKills;
    private double dailyWins;
    private float damageDealt;
    private double days;
    private double headshotKills;
    private double heals;
    private double killPoints;
    private double kills;
    private float longestKill;
    private float longestTimeSurvived;
    private double losses;
    private double maxKillStreaks;
    private float mostSurvivalTime;
    private float rankPoints;
    private double revives;
    private float rideDistance;
    private double roadKills;
    private double roundMostKills;
    private double roundsPlayed;
    private double suicides;
    private float swimDistance;
    private double teamKills;
    private float timeSurvived;
    private double top10s;
    private double vehicleDestroys;
    private float walkDistance;
    private double weaponsAcquired;
    private double weeklyKills;
    private double weeklyWins;
    private double winPoints;
    private double wins;

    public double getAssists() {
        return assists;
    }

    public float getBestRankPoint() {
        return bestRankPoint;
    }

    public double getBoosts() {
        return boosts;
    }

    public double getDbnos() {
        return dbnos;
    }

    public double getDailyKills() {
        return dailyKills;
    }

    public double getDailyWins() {
        return dailyWins;
    }

    public float getDamageDealt() {
        return damageDealt;
    }

    public double getDays() {
        return days;
    }

    public double getHeadshotKills() {
        return headshotKills;
    }

    public double getHeals() {
        return heals;
    }

    public double getKillPoints() {
        return killPoints;
    }

    public double getKills() {
        return kills;
    }

    public float getLongestKill() {
        return longestKill;
    }

    public float getLongestTimeSurvived() {
        return longestTimeSurvived;
    }

    public double getLosses() {
        return losses;
    }

    public double getMaxKillStreaks() {
        return maxKillStreaks;
    }

    public float getMostSurvivalTime() {
        return mostSurvivalTime;
    }

    public float getRankPoints() {
        return rankPoints;
    }

    public double getRevives() {
        return revives;
    }

    public float getRideDistance() {
        return rideDistance;
    }

    public double getRoadKills() {
        return roadKills;
    }

    public double getRoundMostKills() {
        return roundMostKills;
    }

    public double getRoundsPlayed() {
        return roundsPlayed;
    }

    public double getSuicides() {
        return suicides;
    }

    public float getSwimDistance() {
        return swimDistance;
    }

    public double getTeamKills() {
        return teamKills;
    }

    public float getTimeSurvived() {
        return timeSurvived;
    }

    public double getTop10s() {
        return top10s;
    }

    public double getVehicleDestroys() {
        return vehicleDestroys;
    }

    public float getWalkDistance() {
        return walkDistance;
    }

    public double getWeaponsAcquired() {
        return weaponsAcquired;
    }

    public double getWeeklyKills() {
        return weeklyKills;
    }

    public double getWeeklyWins() {
        return weeklyWins;
    }

    public double getWinPoints() {
        return winPoints;
    }

    public double getWins() {
        return wins;
    }

    //Print all fields and values for instance of this class
    public String toString() {
        StringBuilder result = new StringBuilder();
        String newLine = System.getProperty("line.separator");
        result.append("Stats {");
        result.append(newLine);
        Field[] fields = this.getClass().getDeclaredFields();

        for (Field field : fields) {
            result.append("  ");
            try {
                result.append(field.getName());
                result.append(": ");
                //requires access to private field:
                result.append(field.get(this));
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
            }
            result.append(newLine);
        }
        result.append("}");

        return result.toString();
    }


}

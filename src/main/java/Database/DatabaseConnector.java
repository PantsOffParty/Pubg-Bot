package Database;


import Util.ConfigHandler;
import com.mysql.cj.jdbc.MysqlDataSource;

import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/*
 * Picked MySQL for the db since its free.
 * This class connects to the database assuming it's stored
 * locally and set up with the appropriate credentials
 * no ORM because I'm lazy. Just queries.
 */
public class DatabaseConnector {

    //Opens connection to db with necessary info from config
    private Connection getConnection() throws SQLException {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setUser(ConfigHandler.getDatabaseConfig("username"));
        dataSource.setPassword(ConfigHandler.getDatabaseConfig("password"));
        dataSource.setServerName(ConfigHandler.getDatabaseConfig("server"));
        dataSource.setDatabaseName(ConfigHandler.getDatabaseConfig("database"));
        dataSource.setUseSSL(false);
        return dataSource.getConnection();
    }

    //Test method, don't imagine we use
    public void getWinY() {
        try (Connection conn = getConnection();
             Statement statement = conn.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT y_coord FROM win");
            while (resultSet.next()) {
                System.out.println(resultSet.getString("y_coord"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*
     * Store an account in the database to avoid
     * having to hit the api to get the player id every time
     */
    public void storeAccount(String playerName, String accountId) {
        String query = String.format(
                "INSERT INTO `god_bot`.`account` " +
                        "(`player_name`, " +
                        "`account_id`) " +
                        "VALUES" +
                        "('%s', " +
                        "'%s');", playerName, accountId);

        try (Connection conn = getConnection();
             PreparedStatement statement = conn.prepareStatement(query))
        {
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Unable to insert account for " + playerName);
            e.printStackTrace();
        }
    }

    //Is the account in the db already?
    public boolean isAccountStored(String playerName) {
        String query = String.format(
                "SELECT player_name " +
                        "FROM account " +
                        "WHERE player_name = '%s';", playerName);

        try (Connection conn = getConnection();
             Statement statement = conn.createStatement())
        {
            ResultSet resultSet = statement.executeQuery(query);
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("SQL Failure");
    }

    //Retrieve a player id from the database
    public String getAccountId(String playerName) {
        String query = String.format(
                "SELECT account_id " +
                        "FROM account " +
                        "WHERE player_name = '%s';", playerName);

        try (Connection conn = getConnection();
             Statement statement = conn.createStatement()) {
            ResultSet resultSet = statement.executeQuery(query);
            resultSet.next();
            return resultSet.getString("account_id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("SQL Failure");
    }

    /*
     * Insert a win into the database
     */
    public void insertWin(float x, float y, String map)
    {
        String query = String.format("INSERT INTO `god_bot`.`win` " +
                "(`x_coord`, " +
                "`y_coord`, " +
                "`map`) " +
                "VALUES" +
                "(" +
                "%f, " +
                "%f, " +
                "'%s'); ", x, y, map);

        try (Connection conn = getConnection();
             PreparedStatement statement = conn.prepareStatement(query))
        {
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Unable to insert win..");
            e.printStackTrace();
        }
    }

    /*
     * Retrieve all winning coordinates stored in the database
     * for a given map key.
     * May consider storing more map info later, for now
     * just use the s, m, e as a key
     */
    public List<Point> getAllWinCoordinatesByMap(String map)
    {
        List<Point> winPoints = new ArrayList<>();
        String query = String.format(
                "SELECT x_coord, y_coord " +
                        "FROM win " +
                        "WHERE map = '%s';", map);

        try (Connection conn = getConnection();
             Statement statement = conn.createStatement()) {
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                winPoints.add(new Point((int)resultSet.getFloat("x_coord"),
                        (int)resultSet.getFloat("y_coord")));
            }
            return winPoints;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("SQL Failure");
    }

}

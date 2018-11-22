package Database;


import Util.ConfigHandler;
import com.mysql.cj.jdbc.MysqlDataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/*
 * Picked MySQL for the db since its free.
 * This class connects to the database assuming it's stored
 * locally and set up with the appropriate credentials
 * no ORM because I'm lazy. Just queries.
 */
public class DatabaseConnector {

    private Connection getConnection() throws Exception {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setUser(ConfigHandler.getDatabaseConfig("username"));
        dataSource.setPassword(ConfigHandler.getDatabaseConfig("password"));
        dataSource.setServerName(ConfigHandler.getDatabaseConfig("server"));
        dataSource.setDatabaseName(ConfigHandler.getDatabaseConfig("database"));
        dataSource.setUseSSL(false);
        return dataSource.getConnection();
    }

    public void getWinY() {
        try(Connection conn = getConnection();
            Statement statement = conn.createStatement())
        {
            ResultSet resultSet = statement.executeQuery("SELECT y_coord FROM win");
            while (resultSet.next())
            {
                System.out.println(resultSet.getString("y_coord"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

}

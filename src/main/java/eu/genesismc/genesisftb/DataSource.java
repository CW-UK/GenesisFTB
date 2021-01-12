package eu.genesismc.genesisftb;

import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.sql.*;
import java.util.UUID;

public class DataSource {

    private final HikariDataSource hikari = new HikariDataSource();
    private final FileConfiguration config = GenesisFTB.getPlugin().getConfig();

    public DataSource() {
        startSQL();
    }
    public void closeSQL() {
        hikari.close();
    }
    public HikariDataSource getHikari() {
        return hikari;
    }

    public void startSQL() {
        hikari.setLeakDetectionThreshold(3000);
        hikari.setMaximumPoolSize(10);
        hikari.setConnectionTestQuery("SELECT 1;");
        hikari.addDataSourceProperty("autoReconnect", true);
        hikari.setJdbcUrl("jdbc:sqlite:" + new File(GenesisFTB.getPlugin().getDataFolder(), "database.db"));
    }

    public void createTable(){
        try {
            Connection connection = hikari.getConnection();
            Statement statement = connection.createStatement();
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS ftb_scores(UUID VARCHAR(36), name VARCHAR(16), wins INT, PRIMARY KEY (UUID));"
            );
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Integer getWins(UUID uuid) {
        try {
            Connection getWinsConnection = GenesisFTB.getDataSource().getConnection();
            String checkUUID = uuid.toString();
            PreparedStatement getStatement = getWinsConnection.prepareStatement(
                    "SELECT wins FROM ftb_scores WHERE UUID =?;"
            );
            getStatement.setString(1, checkUUID);
            getWinsConnection.commit();
            ResultSet wins = getStatement.executeQuery();

            try {
                if (wins.next()) {
                    int totalWins = wins.getInt("wins");
                    getStatement.close();
                    getWinsConnection.close();
                    return totalWins;
                }
                getStatement.close();
                getWinsConnection.close();
                return 0;
            } catch (SQLException e) {
                return 0;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return 0;
    }

    public boolean emptyDatabase() {
        try {
            Connection connection = hikari.getConnection();
            Statement statement = connection.createStatement();
            statement.executeUpdate(
                    "DELETE FROM ftb_scores;"
            );
            statement.close();
            connection.close();
            GenesisFTB.getPlugin().newResetCode();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Connection getConnection() {
        try {
            Connection connection = hikari.getConnection();
            connection.setAutoCommit(false);
            return connection;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

}
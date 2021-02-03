package eu.genesismc.genesisftb;

import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
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
        hikari.setLeakDetectionThreshold(2000);
        hikari.setConnectionTimeout(1000);
        hikari.setMaximumPoolSize(10);
        hikari.setMinimumIdle(5);
        hikari.setConnectionTestQuery("SELECT 1;");
        hikari.addDataSourceProperty("autoReconnect", true);
        hikari.setJdbcUrl("jdbc:sqlite:" + new File(GenesisFTB.getPlugin().getDataFolder(), "database.db"));
    }

    public void createTable(){
        try {
            Connection connection = hikari.getConnection();
            Statement statement = connection.createStatement();
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS ftb_scores(UUID VARCHAR(36), name VARCHAR(16), wins INT, PRIMARY KEY (UUID));" +
                    "CREATE TABLE IF NOT EXISTS ftb_doors(world VARCHAR(32), x INT, y INT, z INT, type VARCHAR(4));"
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

    public void updateWins(UUID id, String name, Integer wins) {
        try {
            Connection updateConnection = GenesisFTB.getDataSource().getConnection();
            String uuid = id.toString();
            PreparedStatement updateStatement = updateConnection.prepareStatement(
                    "REPLACE INTO ftb_scores (UUID, name, wins) VALUES (?,?,?);"
            );
            updateStatement.setString(1, uuid);
            updateStatement.setString(2, name);
            updateStatement.setInt(3, wins);
            updateStatement.executeUpdate();
            updateConnection.commit();
            updateStatement.close();
            updateConnection.close();
            Bukkit.getLogger().info("[GenesisFTB] " + name + " found a button. Updated count to " + wins);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void removeUser(UUID id, String name, Integer wins) {
        try {
            Connection updateConnection = GenesisFTB.getDataSource().getConnection();
            String uuid = id.toString();
            PreparedStatement updateStatement = updateConnection.prepareStatement(
                    "DELETE FROM ftb_scores WHERE uuid=?;"
            );
            updateStatement.setString(1, uuid);
            updateStatement.executeUpdate();
            updateConnection.commit();
            updateStatement.close();
            updateConnection.close();
            Bukkit.getLogger().info("[GenesisFTB] " + name + " removed from the database.");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
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
            GenesisFTB.utils().newResetCode();
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
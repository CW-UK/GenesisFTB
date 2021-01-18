package eu.genesismc.genesisftb;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Holograms extends PlaceholderExpansion implements Listener {

    @Override
    public String getAuthor() {
        return "CW_UK";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String getIdentifier() {
        return "ftb";
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public boolean persist() {
        return true;
    }

    public List<String> topPlayers = new ArrayList<>();
    public List<String> topWins = new ArrayList<>();

    @Override
    public String onPlaceholderRequest(Player player, String ph) {

        if (player == null || ph == null) { return ""; }

        FileConfiguration config = GenesisFTB.getPlugin().getConfig();
        String noPlaceholder = config.getString("settings.na-placeholder");

        getTop();

        if (ph.startsWith("top_player_")) {
            try {
                int topPlace = Integer.parseInt(ph.replace("top_player_", ""));
                return topPlayers.get(topPlace) != null ? topPlayers.get(topPlace) : noPlaceholder;
            }
            catch (IndexOutOfBoundsException e) {
                return noPlaceholder;
            }
        }

        if (ph.startsWith("top_wins_")) {
            try {
                int topPlace = Integer.parseInt(ph.replace("top_wins_", ""));
                return topWins.get(topPlace) != null ? topWins.get(topPlace) : noPlaceholder;
            }
            catch (IndexOutOfBoundsException e) {
                return noPlaceholder;
            }
        }

        return noPlaceholder;

    }

    public void getTop() {

        try {
            Connection getWinsConnection = GenesisFTB.getDataSource().getConnection();
            PreparedStatement getStatement = getWinsConnection.prepareStatement(
                    "SELECT name, wins FROM ftb_scores ORDER BY wins DESC LIMIT 10;"
            );
            getWinsConnection.commit();
            ResultSet wins = getStatement.executeQuery();

            topPlayers.clear();
            topWins.clear();
            topPlayers.add("dummy");
            topWins.add("0");

            try {
                while (wins != null && wins.next()) {
                    topPlayers.add(wins.getString("name"));
                    topWins.add(String.valueOf(wins.getInt("wins")));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            getStatement.close();
            getWinsConnection.close();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

}

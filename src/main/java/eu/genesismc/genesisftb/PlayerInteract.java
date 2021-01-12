package eu.genesismc.genesisftb;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class PlayerInteract implements Listener, Cancellable {

    @EventHandler
    public void on(PlayerInteractEvent e) {

        final Block block = e.getClickedBlock();
        if (block == null) return;

        if (WorldGuardManager.isFTB(e.getClickedBlock().getLocation())) {

            if (!e.getClickedBlock().getType().toString().contains("_BUTTON")) {
                return;
            }

            FileConfiguration config = GenesisFTB.getPlugin().getConfig();
            String player = e.getPlayer().getName();
            Player p = e.getPlayer();

            if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {

                if (!GenesisFTB.getPlugin().inGame) {
                    if (p.hasPermission("genesisftb.admin") && GenesisFTB.getPlugin().buttons.contains(e.getClickedBlock().getLocation())) {
                        p.sendMessage(color(config.getString("settings.prefix")) + ChatColor.RED + " You can't press game buttons before the game has started.");
                        e.setCancelled(true);
                        return;
                    }
                    return;
                }

                if (!GenesisFTB.getPlugin().buttons.contains(e.getClickedBlock().getLocation())) {
                    p.sendMessage(
                            ChatColor.GOLD + "That button is decorative and "+ChatColor.WHITE+ChatColor.BOLD+"not"+ChatColor.WHITE+ChatColor.BOLD+" part of the game!"
                    );
                    return;
                }

                GenesisFTB.getPlugin().buttons.remove(e.getClickedBlock().getLocation());
                int totalButtons = GenesisFTB.getPlugin().buttons.size();

                Integer foundButtons = GenesisFTB.getDataSource().getWins(p.getUniqueId());
                Integer totalWins = foundButtons + 1;
                updateWins(e.getPlayer().getUniqueId(), e.getPlayer().getName(), totalWins);

                if (config.getBoolean("settings.remove-button")) {
                    e.setCancelled(true);
                    e.getClickedBlock().setType(Material.AIR);
                }

                for (Player pl : Bukkit.getOnlinePlayers()) {
                    pl.sendMessage(""+
                            color(config.getString("settings.prefix")) + " " +
                            color(config.getString("settings.found-message").replace("%player%", player).replace("%count%", String.valueOf(totalButtons)))
                    );
                }

                Location loc = e.getClickedBlock().getLocation();
                GenesisFTB.getPlugin().foundList.add("&f" + player + " &efound a button at &fx" + loc.getX() + " y" + loc.getY() + " z" + loc.getZ());

                p.sendMessage(""+
                        color(config.getString("settings.prefix")) + " " +
                        color(config.getString("settings.reward-message"))
                );

                for (String str : GenesisFTB.getPlugin().getConfig().getStringList("rewards")) {
                    String rewardCommand = str.replace("%player%", player);
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), rewardCommand);
                }

                p.sendMessage(ChatColor.GOLD + "You have now found " + ChatColor.WHITE + totalWins + ChatColor.GOLD + " buttons.");

                if (totalButtons == 0) {
                    GenesisFTB.getPlugin().inGame = false;
                    for (Player pl : Bukkit.getOnlinePlayers()) {
                        pl.sendMessage("" +
                                color(config.getString("settings.prefix")) + " " +
                                color(config.getString("settings.end-message").replace("%player%", player).replace("%count%", String.valueOf(totalButtons)))
                        );
                    }
                }

            }

            if (e.getAction() == Action.LEFT_CLICK_BLOCK && p.hasPermission("genesisftb.admin")) {
                if (!GenesisFTB.getPlugin().buttons.contains(e.getClickedBlock().getLocation())) { return; }
                if (GenesisFTB.getPlugin().inGame) {
                    p.sendMessage(color(config.getString("settings.prefix")) + ChatColor.RED + " You can't remove a button while a game is running!");
                    e.setCancelled(true);
                    return;
                }
                GenesisFTB.getPlugin().buttons.remove(e.getClickedBlock().getLocation());
                int totalButtons = GenesisFTB.getPlugin().buttons.size();
                Location loc = e.getClickedBlock().getLocation();
                e.getPlayer().sendMessage(ChatColor.GOLD + "Button at "+ ChatColor.WHITE + "x" + ChatColor.WHITE + loc.getX() + " y" + loc.getY() + " z" + loc.getZ() + ChatColor.GOLD + " was removed from the game.");

                if (config.getBoolean("settings.announce-remove")) {
                    for (Player pl : Bukkit.getOnlinePlayers()) {
                        pl.sendMessage("" +
                                color(config.getString("settings.prefix")) + " " +
                                color(config.getString("settings.removed-message").replace("%player%", player).replace("%count%", String.valueOf(totalButtons)))
                        );
                    }
                }
            }
        }
    }

    private void updateWins(UUID id, String name, Integer wins) {
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

    public String color(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
    @Override
    public boolean isCancelled() { return false; }
    @Override
    public void setCancelled(boolean b) { }
}

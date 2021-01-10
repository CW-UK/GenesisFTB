package eu.genesismc.genesisftb;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;


public class BlockPlace implements Listener {

    @EventHandler
    public void on(BlockPlaceEvent e) {

        if (!e.getBlock().getType().toString().contains("_BUTTON")) {
            return;
        }

        if (WorldGuardManager.isFTB(e.getBlock().getLocation()) && e.getPlayer().hasPermission("genesisftb.admin")) {

            FileConfiguration config = GenesisFTB.getPlugin().getConfig();

            if (GenesisFTB.getPlugin().inGame) {
                e.getPlayer().sendMessage(color(config.getString("settings.prefix")) + ChatColor.RED + " You can't add buttons while a game is running!");
                e.setCancelled(true);
                return;
            }

            String player = e.getPlayer().getName();
            Location loc = e.getBlock().getLocation();
            GenesisFTB.getPlugin().buttons.add(loc);
            int totalButtons = GenesisFTB.getPlugin().buttons.size();

            e.getPlayer().sendMessage(ChatColor.GOLD + "Button at " + ChatColor.WHITE + "x" + ChatColor.WHITE + loc.getX() + " y" + loc.getY() + " z" + loc.getZ() + ChatColor.GOLD + " is now part of the game.");

            if (config.getBoolean("settings.announce-place")) {
                for (Player pl : Bukkit.getOnlinePlayers()) {
                    pl.sendMessage("" +
                            color(config.getString("settings.prefix")) + " " +
                            color(config.getString("settings.placed-message").replace("%player%", player).replace("%count%", String.valueOf(totalButtons)))
                    );
                }
            }
        }
    }

    public String color(String s) { return ChatColor.translateAlternateColorCodes('&', s); }

}

package eu.genesismc.genesisftb;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.UUID;

public class CommandHandler implements CommandExecutor, Listener {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        FileConfiguration config = GenesisFTB.getInstance().getConfig();

        /*************************
         *  EMPTY COMMAND
         *************************/

        if (args.length < 1) {
            UUID uuid = Bukkit.getPlayer(sender.getName()).getUniqueId();
            Integer wins = GenesisFTB.getDataSource().getWins(uuid);
            sender.sendMessage(ChatColor.GOLD + "You have found " + ChatColor.WHITE + wins + ChatColor.GOLD + " buttons.");
            return true;
        }

        /*************************
         *  RELOAD COMMAND
         *************************/

        if (args[0].equals("reload") && sender.hasPermission("genesisftb.admin")) {
            GenesisFTB.getPlugin().reloadConfig();
            sender.sendMessage(color(config.getString("settings.prefix")) + ChatColor.RED + " Configuration reloaded.");
            return true;
        }

        /*************************
         *  START COMMAND
         *************************/

        if (args[0].equals("start") && sender.hasPermission("genesisftb.admin")) {
            int totalButtons = GenesisFTB.getPlugin().buttons.size();
            if (totalButtons == 0) {
                sender.sendMessage(color(config.getString("settings.prefix")) + ChatColor.RED + " You can't start a game with no buttons!");
                return true;
            }
            if (GenesisFTB.getPlugin().inGame) {
                sender.sendMessage(color(config.getString("settings.prefix")) + ChatColor.RED + " A game is already in progress!");
                return true;
            }
            GenesisFTB.getPlugin().inGame = true;
            for (Player pl : Bukkit.getOnlinePlayers()) {
                pl.sendMessage("" +
                        color(config.getString("settings.prefix")) + " " +
                        color(config.getString("settings.start-message").replace("%count%", String.valueOf(totalButtons)))
                );
            }
            return true;
        }

        /*************************
         *  RESET COMMAND
         *************************/

        if (args[0].equals("reset") && sender.hasPermission("genesisftb.admin")) {
            for (Location l : GenesisFTB.getPlugin().buttons) {
                l.getBlock().setType(Material.AIR);
            }
            GenesisFTB.getPlugin().buttons.clear();
            GenesisFTB.getPlugin().inGame = false;
            for (Player pl : Bukkit.getOnlinePlayers()) {
                pl.sendMessage("" +
                        color(config.getString("settings.prefix")) + " " +
                        color(config.getString("settings.reset-message"))
                );
            }
            return true;
        }

        /*************************
         *  LIST COMMAND
         *************************/

        if (args[0].equals("list") && sender.hasPermission("genesisftb.admin")) {
            if (GenesisFTB.getPlugin().buttons.size() < 1) {
                sender.sendMessage(color(config.getString("settings.prefix")) + ChatColor.RED + " No buttons have been placed.");
                return true;
            }
            sender.sendMessage(color(config.getString("settings.prefix")) + ChatColor.YELLOW + " There are " + ChatColor.WHITE + ChatColor.BOLD + GenesisFTB.getPlugin().buttons.size() + ChatColor.YELLOW + " buttons still to find:");
            for (int i = 0; i < GenesisFTB.getPlugin().buttons.size(); i++) {
                String buttonLocation = "x" + GenesisFTB.getPlugin().buttons.get(i).getX() + " y" + GenesisFTB.getPlugin().buttons.get(i).getY() + " z" + GenesisFTB.getPlugin().buttons.get(i).getZ();
                int iN = i + 1;
                sender.sendMessage(color(config.getString("settings.prefix")) + ChatColor.WHITE + " Button " + iN + ": " + ChatColor.YELLOW + buttonLocation);
            }
            return true;
        }

        /*************************
         *  PLAYER LOOKUP COMMAND
         *************************/

        if (args.length == 1) {
            UUID uuid;
            String name;
            try {
                uuid = Bukkit.getPlayer(args[0]).getUniqueId();
                name = Bukkit.getPlayer(args[0]).getName();
            }
            catch (NullPointerException e) {
                sender.sendMessage(ChatColor.GOLD + "Player " + ChatColor.WHITE + args[0] + ChatColor.GOLD + " could not be found.");
                return true;
            }
            Integer wins = GenesisFTB.getDataSource().getWins(uuid);
            sender.sendMessage(ChatColor.GOLD + name + " has found " + ChatColor.WHITE + wins + ChatColor.GOLD + " buttons.");
            return true;

        }
        return false;
    }

    public String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

}

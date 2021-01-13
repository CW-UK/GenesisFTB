package eu.genesismc.genesisftb;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Openable;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.util.StringUtil;

import java.util.*;

public class CommandHandler implements CommandExecutor, Listener, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        FileConfiguration config = GenesisFTB.getInstance().getConfig();
        Player player = (Player) sender;

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
            GenesisFTB.getPlugin().config = GenesisFTB.getPlugin().getConfig();
            sender.sendMessage(color(config.getString("settings.prefix")) + ChatColor.RED + " Configuration reloaded.");
            return true;
        }

        /*******************
         *  DOORADD COMMAND
         *******************/

        if (args[0].equals("dooradd") && sender.hasPermission("genesisftb.admin")) {

            try {
                Block block = player.getTargetBlock(null, 50);
                Openable toOpen = (Openable) block.getBlockData();
                if (!(block == null) && toOpen instanceof Openable) {

                    List list = config.getList("doors");
                    if (list.contains(block.getLocation())) {
                        sender.sendMessage(color(config.getString("settings.prefix")) + ChatColor.RED + " That door is already linked.");
                        return true;
                    }

                    list.add(block.getLocation());
                    Location loc = block.getLocation();
                    config.set("doors", list);
                    GenesisFTB.getPlugin().saveConfig();
                    sender.sendMessage(
                            color(config.getString("settings.prefix")) +
                                    ChatColor.GREEN + " Door at " + ChatColor.WHITE + "x" + ChatColor.WHITE + loc.getX() + " y" + loc.getY() + " z" + loc.getZ() + ChatColor.GREEN + " is now a game door.");
                    return true;
                }
                sender.sendMessage(color(config.getString("settings.prefix")) + ChatColor.RED + " That block is not openable.");
                return true;

            } catch (ClassCastException exc) {
                sender.sendMessage(color(config.getString("settings.prefix")) + ChatColor.RED + " That block is not openable.");
                return true;
            }
        }


        /*******************
         *  DOORDEL COMMAND
         *******************/

        if (args[0].equals("doordel") && sender.hasPermission("genesisftb.admin")) {

            try {
                Block block = player.getTargetBlock(null, 50);
                Openable toOpen = (Openable) block.getBlockData();
                if (!(block == null) && toOpen instanceof Openable) {

                    List list = config.getList("doors");
                    if (!list.contains(block.getLocation())) {
                        sender.sendMessage(color(config.getString("settings.prefix")) + ChatColor.RED + " That door is not part of the game.");
                        return true;
                    }

                    list.remove(block.getLocation());
                    Location loc = block.getLocation();
                    config.set("doors", list);
                    GenesisFTB.getPlugin().saveConfig();
                    sender.sendMessage(
                            color(config.getString("settings.prefix")) +
                                    ChatColor.GREEN + " Door at " + ChatColor.WHITE + "x" + ChatColor.WHITE + loc.getX() + " y" + loc.getY() + " z" + loc.getZ() + ChatColor.GREEN + " is no longer a game door.");
                    return true;
                }
                sender.sendMessage(color(config.getString("settings.prefix")) + ChatColor.RED + " That block is not openable.");
                return true;
            }
            catch (ClassCastException exc) {
                sender.sendMessage(color(config.getString("settings.prefix")) + ChatColor.RED + " That block is not openable.");
                return true;
            }
        }

        /*************************
         *  OPENDOORS COMMAND
         *************************/

        if (args[0].equals("opendoors") && sender.hasPermission("genesisftb.admin")) {
            GenesisFTB.getPlugin().openDoors();
            sender.sendMessage(color(config.getString("settings.prefix")) + ChatColor.GREEN + " Opening game doors..");
            return true;
        }

        /*************************
         *  CLOSEDOORS COMMAND
         *************************/

        if (args[0].equals("closedoors") && sender.hasPermission("genesisftb.admin")) {
            GenesisFTB.getPlugin().closeDoors();
            sender.sendMessage(color(config.getString("settings.prefix")) + ChatColor.GREEN + " Closing game doors..");
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
            GenesisFTB.getPlugin().foundList.clear();
            for (Player pl : Bukkit.getOnlinePlayers()) {
                pl.sendMessage("" +
                        color(config.getString("settings.prefix")) + " " +
                        color(config.getString("settings.start-message").replace("%count%", String.valueOf(totalButtons)))
                );
            }
            if (config.getBoolean("settings.open-doors")) {
                GenesisFTB.getPlugin().openDoors();
                sender.sendMessage(color(config.getString("settings.prefix")) + ChatColor.GREEN + " Opening game doors..");
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
            GenesisFTB.getPlugin().foundList.clear();
            for (Player pl : Bukkit.getOnlinePlayers()) {
                pl.sendMessage("" +
                        color(config.getString("settings.prefix")) + " " +
                        color(config.getString("settings.reset-message"))
                );
            }
            if (config.getBoolean("settings.close-doors")) {
                GenesisFTB.getPlugin().closeDoors();
                sender.sendMessage(color(config.getString("settings.prefix")) + ChatColor.GREEN + " Closing game doors..");
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
         *  BROADCAST COMMAND
         *************************/

        if (args[0].equals("broadcast") && sender.hasPermission("genesisftb.admin")) {

            String broadcast = StringUtils.join(ArrayUtils.subarray(args, 1, args.length), " ");

            if (broadcast.length() < 1) {
                sender.sendMessage(color(config.getString("settings.prefix")) + ChatColor.RED + " You need to provide a message.");
                return true;
            }

            for (Player pl : Bukkit.getOnlinePlayers()) {
                pl.sendMessage("" +
                        color(config.getString("settings.prefix") + " " + ChatColor.RESET + broadcast)
                );
            }
            return true;
        }

        /*************************
         *  FOUND COMMAND
         *************************/

        if (args[0].equals("found") && sender.hasPermission("genesisftb.admin")) {

            String preMessage = GenesisFTB.getPlugin().inGame ? "In this game, " : "In the last game, ";
            String postMessage = GenesisFTB.getPlugin().inGame ? "have been" : "were";

            if (GenesisFTB.getPlugin().foundList.size() < 1) {
                sender.sendMessage(color(config.getString("settings.prefix")) + " " + ChatColor.YELLOW + " No buttons have been found yet.");
                return true;
            }

            sender.sendMessage(color(config.getString("settings.prefix")) + " " + ChatColor.YELLOW + preMessage + ChatColor.WHITE + ChatColor.BOLD + GenesisFTB.getPlugin().foundList.size() + ChatColor.YELLOW + " buttons " + postMessage + " found:");
            for (int i = 0; i < GenesisFTB.getPlugin().foundList.size(); i++) {
                sender.sendMessage(color(config.getString("settings.prefix")) + " " + color(GenesisFTB.getPlugin().foundList.get(i)));
            }
            return true;
        }

        /**************************
         *  DATABASE RESET COMMAND
         **************************/

        if (args[0].equals("cleardatabase") && sender.hasPermission("genesisftb.admin") && sender.isOp()) {

            if (args.length > 1) {
                if (args[1].equals(GenesisFTB.getPlugin().resetCode)) {
                    GenesisFTB.getDataSource().emptyDatabase();
                    sender.sendMessage(color(config.getString("settings.prefix")) + ChatColor.GREEN + " The database has been reset!");
                } else {
                    sender.sendMessage(color(config.getString("settings.prefix")) + ChatColor.RED + " Invalid reset code.");
                }
            } else {
                sender.sendMessage(color(config.getString("settings.prefix")) + ChatColor.GREEN +
                        " This will delete " + ChatColor.BOLD + "ALL DATA" + ChatColor.RESET + "! Confirm with " + ChatColor.YELLOW + "/ftb cleardatabase " + GenesisFTB.getPlugin().resetCode
                );
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

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (cmd.getName().equalsIgnoreCase("ftb")) {
            if (args.length >= 1) {
                if (sender.hasPermission("genesisftb.admin")) {
                    final List<String> commands = Arrays.asList(
                            "found", "cleardatabase", "list", "reload", "reset", "opendoors",
                            "closedoors", "doordel", "dooradd", "start", "broadcast"
                    );

                    return (args.length < 2) ? StringUtil.copyPartialMatches(args[0], commands, new ArrayList<>()) : null;
                }
            }
        }
        return null;
    }

}

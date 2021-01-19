package eu.genesismc.genesisftb;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.util.StringUtil;

import java.util.*;
import java.util.stream.Collectors;

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
         *  TOOL COMMAND
         *************************/

        if (args[0].equals("tool") && hasMainPerm(sender) && sender.isOp()) {
            GenesisFTB.getPlugin().getItemTools().giveStackingTool((Player) sender);
            sender.sendMessage(ChatColor.GOLD + "The FTB tool has been added to your inventory.");
            return true;
        }

        /*************************
         *  RELOAD COMMAND
         *************************/

        if (args[0].equals("reload") && hasMainPerm(sender)) {
            GenesisFTB.getPlugin().reloadConfig();
            GenesisFTB.getPlugin().config = GenesisFTB.getPlugin().getConfig();
            sender.sendMessage(color(config.getString("settings.prefix")) + ChatColor.RED + " Configuration reloaded.");
            return true;
        }

        /*************************
         *  TOGGLE COMMAND
         *************************/

        if (args[0].equals("toggle") && hasMainPerm(sender)) {
            GenesisFTB.getPlugin().config = GenesisFTB.getPlugin().getConfig();

            if (GenesisFTB.getPlugin().inGame) {
                sender.sendMessage(color(config.getString("settings.prefix")) + ChatColor.RED + "You cannot toggle FTB while a game is running!");
                return true;
            }

            if (GenesisFTB.getPlugin().gameMode) {
                GenesisFTB.getPlugin().gameMode = false;
                GenesisFTB.utils().sendToAdmins(ChatColor.RED + "FTB has now been disabled by " + sender.getName(), true);
            }
            else {
                GenesisFTB.getPlugin().gameMode = true;
                GenesisFTB.utils().sendToAdmins(ChatColor.GREEN + "FTB has been enabled by " + sender.getName(), true);
            }

            return true;
        }

        /*************************
         *  OPENDOORS COMMAND
         *************************/

        if (args[0].equals("opendoors") && hasMainPerm(sender)) {
            if (args.length < 2) {
                sender.sendMessage(color(config.getString("settings.prefix")) + ChatColor.GREEN + " Specify door type: /ftb opendoors <main|game>");
                return true;
            }
            if (args[1].equals("main") || args[1].equals("game")) {
                GenesisFTB.utils().openDoors(args[1], true);
                sender.sendMessage(color(config.getString("settings.prefix")) + ChatColor.GREEN + " Opening " + args[1] + " doors..");
                return true;
            }
            sender.sendMessage(color(config.getString("settings.prefix")) + ChatColor.GREEN + " Specify door type: /ftb opendoors <main|game>");
            return true;
        }

        /*************************
         *  CLOSEDOORS COMMAND
         *************************/

        if (args[0].equals("closedoors") && hasMainPerm(sender)) {
            if (args.length < 2) {
                sender.sendMessage(color(config.getString("settings.prefix")) + ChatColor.GREEN + " Specify door type: /ftb closedoors <main|game>");
                return true;
            }
            if (args[1].equals("main") || args[1].equals("game")) {
                GenesisFTB.utils().openDoors(args[1], false);
                sender.sendMessage(color(config.getString("settings.prefix")) + ChatColor.GREEN + " Closing game doors..");
                return true;
            }
            sender.sendMessage(color(config.getString("settings.prefix")) + ChatColor.GREEN + " Specify door type: /ftb closedoors <main|game>");
            return true;
        }

        /*************************
         *  START COMMAND
         *************************/

        if (args[0].equals("start") && hasMainPerm(sender)) {
            int totalButtons = GenesisFTB.getPlugin().buttons.size();
            if (totalButtons == 0) {
                sender.sendMessage(color(config.getString("settings.prefix")) + ChatColor.RED + " You can't start a game with no buttons!");
                return true;
            }
            if (GenesisFTB.getPlugin().inGame) {
                sender.sendMessage(color(config.getString("settings.prefix")) + ChatColor.RED + " A game is already in progress!");
                return true;
            }
            if (!GenesisFTB.getPlugin().gameMode) {
                sender.sendMessage(color(config.getString("settings.prefix")) + ChatColor.RED + " You can't start a game as FTB is currently disabled!");
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

            sender.sendMessage(color(config.getString("settings.prefix")) + ChatColor.GREEN + " Setting all doors..");
            GenesisFTB.utils().openDoors("main", config.getBoolean("settings.set-maindoors-on-start"));
            GenesisFTB.utils().openDoors("game", config.getBoolean("settings.set-gamedoors-on-start"));

            return true;
        }

        /*************************
         *  RESET COMMAND
         *************************/

        if (args[0].equals("reset") && hasMainPerm(sender)) {
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
            sender.sendMessage(color(config.getString("settings.prefix")) + ChatColor.GREEN + " Setting all doors..");
            GenesisFTB.utils().openDoors("main", config.getBoolean("settings.set-maindoors-on-reset"));
            GenesisFTB.utils().openDoors("game", config.getBoolean("settings.set-gamedoors-on-reset"));
            return true;
        }

        /*************************
         *  LIST COMMAND
         *************************/

        if (args[0].equals("list") && hasMainPerm(sender)) {
            if (GenesisFTB.getPlugin().buttons.size() < 1) {
                sender.sendMessage(color(config.getString("settings.prefix")) + ChatColor.RED + " No buttons have been placed.");
                return true;
            }
            sender.sendMessage(color(config.getString("settings.prefix")) + ChatColor.YELLOW + " There are " + ChatColor.WHITE + ChatColor.BOLD + GenesisFTB.getPlugin().buttons.size() + ChatColor.YELLOW + " buttons still to find:");
            for (int i = 0; i < GenesisFTB.getPlugin().buttons.size(); i++) {
                Location listButton = GenesisFTB.getPlugin().buttons.get(i);
                String buttonLocation = "x" + listButton.getX() + " y" + listButton.getY() + " z" + listButton.getZ() + " (" + listButton.getWorld().getName() + ")";
                int iN = i + 1;
                String locW = listButton.getWorld().getName();
                int locX = (int) listButton.getX();
                int locY = (int) listButton.getY();
                int locZ = (int) listButton.getZ();
                sender.spigot().sendMessage(GenesisFTB.utils().clickToTeleport(locW, locX, locY, locZ, ChatColor.WHITE + "Button " + iN + ": " + ChatColor.YELLOW + buttonLocation, iN));
            }
            return true;
        }

        /*************************
         *  TELEPORT COMMAND
         *************************/

        if (args[0].equals("teleport") && hasMainPerm(sender)) {
            World w = Bukkit.getServer().getWorld(args[1]);
            Location newLoc = new Location(w,Integer.parseInt(args[2]),Integer.parseInt(args[3]),Integer.parseInt(args[4]));
            ((Player) sender).setGameMode(GameMode.SPECTATOR);
            ((Player) sender).teleport(newLoc);
            return true;
        }

        /*************************
         *  BROADCAST COMMAND
         *************************/

        if (args[0].equals("broadcast") && hasMainPerm(sender)) {

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

        if (args[0].equals("found") && hasMainPerm(sender)) {

            String preMessage = GenesisFTB.getPlugin().inGame ? "In this game, " : "In the last game, ";
            String postMessage = GenesisFTB.getPlugin().inGame ? "have been" : "were";

            if (GenesisFTB.getPlugin().foundList.size() < 1) {
                sender.sendMessage(color(config.getString("settings.prefix")) + " " + ChatColor.YELLOW + " No buttons have been found yet.");
                return true;
            }

            sender.sendMessage(color(config.getString("settings.prefix")) + " " + ChatColor.YELLOW + preMessage + ChatColor.WHITE + ChatColor.BOLD + GenesisFTB.getPlugin().foundList.size() + ChatColor.YELLOW + " buttons " + postMessage + " found:");
            for (int i = 0; i < GenesisFTB.getPlugin().foundList.size(); i++) {
                List<String> sortedList = GenesisFTB.getPlugin().foundList.stream().sorted().collect(Collectors.toList());
                sender.sendMessage(color(config.getString("settings.prefix")) + " " + color(sortedList.get(i)));
            }
            return true;
        }

        /**************************
         *  DATABASE RESET COMMAND
         **************************/

        if (args[0].equals("cleardatabase") && hasMainPerm(sender) && sender.isOp()) {

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
         *  SETSCORE COMMAND
         *************************/

        if (args[0].equals("setscore") && hasMainPerm(sender) && sender.isOp()) {
            UUID uuid;
            String name;
            if (args.length < 3) {
                sender.sendMessage(color(config.getString("settings.prefix")) + ChatColor.RED + " You need to specify a new score. Zero removes player from the database.");
                return true;
            }
            try {
                uuid = Bukkit.getPlayer(args[1]).getUniqueId();
                name = Bukkit.getPlayer(args[1]).getName();
            }
            catch (NullPointerException e) {
                sender.sendMessage(ChatColor.GOLD + "Player " + ChatColor.WHITE + args[1] + ChatColor.GOLD + " is not known to the server.");
                return true;
            }
            int wins = GenesisFTB.getDataSource().getWins(uuid);
            int newWins = Integer.parseInt(args[2]);
            if (newWins < 1) {
                sender.sendMessage(ChatColor.WHITE + name + ChatColor.GOLD + " has been removed from the database.");
                GenesisFTB.getDataSource().removeUser(uuid, name, newWins);
            }
            else {
                sender.sendMessage(ChatColor.WHITE + name + ChatColor.GOLD + "'s score changed from " + ChatColor.WHITE + wins + ChatColor.GOLD + " to " + ChatColor.WHITE + newWins);
                GenesisFTB.getDataSource().updateWins(uuid, name, newWins);
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

    public boolean hasMainPerm(CommandSender s) {
        Player p = (Player) s;
        return p.hasPermission("genesisftb.admin");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (cmd.getName().equalsIgnoreCase("ftb")) {
            if (hasMainPerm(sender)) {
                // First param - 0
                if (args.length == 1) {
                    final List<String> commands = Arrays.asList(
                            "found", "cleardatabase", "list", "reload", "reset", "opendoors",
                            "closedoors", "start", "broadcast", "setscore", "toggle"
                    );
                    return StringUtil.copyPartialMatches(args[0], commands, new ArrayList<>());
                }
                // Second param - 1
                if (args.length == 2 && (args[0].equals("closedoors") || args[0].equals("opendoors"))) {
                    final List<String> commands = Arrays.asList("main", "game");
                    return StringUtil.copyPartialMatches(args[1], commands, new ArrayList<>());
                }
                else { return null; }
            }
        }
        return null;
    }

}

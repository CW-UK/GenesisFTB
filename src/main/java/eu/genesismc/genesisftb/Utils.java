package eu.genesismc.genesisftb;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.Openable;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Random;

public class Utils {

    public void newResetCode() {
        Random r = new Random();
        String alphabet = "0123456789";
        String preparedCode = "";
        for (int i = 0; i < 5; i++) {
            preparedCode = preparedCode + alphabet.charAt(r.nextInt(alphabet.length()));
        }
        GenesisFTB.getPlugin().resetCode = preparedCode;
    }

    public void openDoors(String doorTypes, Boolean state) {
        Bukkit.getLogger().info("Trying to open " + doorTypes + "doors");
        try {
            for (Object openList : GenesisFTB.getPlugin().config.getList(doorTypes + "doors")) {
                Location loc = (Location) openList;
                Block b = loc.getBlock();
                Openable d = (Openable) b.getBlockData();
                d.setOpen(state);
                b.setBlockData(d);
            }
            if (doorTypes.equals("main")) { GenesisFTB.getPlugin().mainDoorsOpen = state; }
            else { GenesisFTB.getPlugin().gameDoorsOpen = state; }
        }
        catch (NullPointerException exc) {
            Bukkit.getServer().getLogger().info("GenesisFTB: No doors!");
        }
    }

    public boolean isDoor(Location loc) {
        try {
            List<?> list;
            list = GenesisFTB.getPlugin().config.getList("maindoors");
            if (list.contains(loc)) {
                return true;
            }
            list = GenesisFTB.getPlugin().config.getList("gamedoors");
            return list.contains(loc);
        }
        catch (NullPointerException npe) {
            return false;
        }
    }

    public String whichDoor(Location loc) {
        try {
            List<?> list;
            list = GenesisFTB.getPlugin().config.getList("maindoors");
            if (list.contains(loc)) {
                return "maindoors";
            }
            list = GenesisFTB.getPlugin().config.getList("gamedoors");
            if (list.contains(loc)) {
                return "gamedoors";
            }
            return null;
        }
        catch (NullPointerException npe) {
            return null;
        }
    }

    public boolean addDoor(Player p, Location loc, String doorType) {
        Block block = loc.getBlock();
        Openable toOpen = (Openable) block.getBlockData();

        List list = GenesisFTB.getPlugin().config.getList(doorType);
        list.add(block.getLocation());
        String doorBlock = block.getType().toString();
        GenesisFTB.getPlugin().config.set(doorType, list);
        GenesisFTB.getPlugin().saveConfig();
        String doorMsg = (doorType.equals("maindoors")) ? "MAIN door" : "GAME door";
        p.sendMessage(
                color(GenesisFTB.getPlugin().config.getString("settings.prefix")) +
                        ChatColor.WHITE + " " + doorBlock + ChatColor.GREEN + " at " + ChatColor.WHITE + "x" + ChatColor.WHITE + loc.getX() + " y" + loc.getY() + " z" + loc.getZ() + ChatColor.GREEN + " is now a " + doorMsg + ".");
        return true;
    }

    public boolean removeDoor(Player p, Location loc) {
        Block block = loc.getBlock();
        Openable toOpen = (Openable) block.getBlockData();
        String doorType = whichDoor(loc);

        List list = GenesisFTB.getPlugin().config.getList(doorType);
        list.remove(block.getLocation());
        String doorBlock = block.getType().toString();
        GenesisFTB.getPlugin().config.set(doorType, list);
        GenesisFTB.getPlugin().saveConfig();
        String doorMsg = (doorType.equals("maindoors")) ? "MAIN door" : "GAME door";
        p.sendMessage(
                color(GenesisFTB.getPlugin().config.getString("settings.prefix")) +
                        ChatColor.WHITE + " " + doorBlock + ChatColor.GREEN + " at " + ChatColor.WHITE + "x" + ChatColor.WHITE + loc.getX() + " y" + loc.getY() + " z" + loc.getZ() + ChatColor.GREEN + " is no longer part of the game.");
        return true;
    }

    public void sendToAll(String msg, Boolean prefix) {
        String newMsg;
        if (prefix) { newMsg = color(GenesisFTB.getPlugin().config.getString("settings.prefix") + " " + msg); }
        else { newMsg = color(msg); }
        for (Player pl : Bukkit.getOnlinePlayers()) {
            pl.sendMessage(newMsg);
        }
    }

    public void sendToAdmins(String msg, Boolean prefix) {
        String newMsg;
        if (prefix) { newMsg = color(GenesisFTB.getPlugin().config.getString("settings.prefix") + " " + msg); }
        else { newMsg = color(msg); }
        Bukkit.broadcast(newMsg, "genesisftb.admin");
    }

    public TextComponent clickToTeleport(String w, int x, int y, int z, String msg, int buttonNumber) {
        TextComponent message = new TextComponent();
        TextComponent startMsg = new TextComponent(msg);
        startMsg.setClickEvent( new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ftb teleport "+w+" "+x+" "+y+" "+z));
        startMsg.setHoverEvent( new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.GREEN + "Teleport to button " + ChatColor.WHITE + buttonNumber + ChatColor.GREEN + " (auto GMSP)")));
        message.addExtra(startMsg);
        return message;
    }

    public String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

}

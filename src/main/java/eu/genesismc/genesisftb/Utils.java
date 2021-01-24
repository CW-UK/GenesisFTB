package eu.genesismc.genesisftb;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Openable;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
        try {
            Connection openDoor = GenesisFTB.getDataSource().getConnection();
            PreparedStatement statement = openDoor.prepareStatement(
                    "SELECT * FROM ftb_doors WHERE type=?;"
            );
            statement.setString(1, doorTypes);
            openDoor.commit();
            ResultSet doors = statement.executeQuery();
            while (doors.next()) {
                World world = Bukkit.getWorld(doors.getString("world"));
                int x = doors.getInt("x");
                int y = doors.getInt("y");
                int z = doors.getInt("z");
                Location loc = new Location(world,x,y,z);
                Block b = loc.getBlock();
                Openable d = (Openable) b.getBlockData();
                d.setOpen(state);
                b.setBlockData(d);
            }
            if (doorTypes.equals("main")) { GenesisFTB.getPlugin().mainDoorsOpen = state; }
            else { GenesisFTB.getPlugin().gameDoorsOpen = state; }
            statement.close();
            doors.close();
            openDoor.close();
        }
        catch (NullPointerException | SQLException exc) {
            Bukkit.getServer().getLogger().info("GenesisFTB: No doors!");
        }
    }

    public boolean isDoor(Location loc) {
        try {
            Connection isDoor = GenesisFTB.getDataSource().getConnection();
            PreparedStatement statement = isDoor.prepareStatement(
                    "SELECT * FROM ftb_doors WHERE world=? AND x=? AND y=? AND z=?;"
            );
            statement.setString(1, loc.getWorld().getName());
            statement.setInt(2, (int) loc.getX());
            statement.setInt(3, (int) loc.getY());
            statement.setInt(4, (int) loc.getZ());
            isDoor.commit();
            ResultSet doors = statement.executeQuery();
            boolean doorExists = doors.next();
            statement.close();
            isDoor.close();
            return doorExists;
        }
        catch (NullPointerException | SQLException e) {
            return false;
        }
    }

    public String whichDoor(Location loc) {
        String doorType = null;
        try {
            Connection whichDoor = GenesisFTB.getDataSource().getConnection();
            PreparedStatement statement = whichDoor.prepareStatement(
                    "SELECT * FROM ftb_doors WHERE world=? AND x=? AND y=? AND z=?;"
            );
            statement.setString(1, loc.getWorld().getName());
            statement.setInt(2, (int) loc.getX());
            statement.setInt(3, (int) loc.getY());
            statement.setInt(4, (int) loc.getZ());
            statement.executeQuery();
            whichDoor.commit();
            ResultSet doors = statement.executeQuery();
            if (doors.next()) {
                doorType = doors.getString("type");
            }
            statement.close();
            whichDoor.close();
            return doorType;
        }
        catch (NullPointerException | SQLException npe) {
            return null;
        }
    }

    public boolean addDoor(Player p, Location loc, String doorType) {
        Block block = loc.getBlock();
        try {
            Connection addDoor = GenesisFTB.getDataSource().getConnection();
            PreparedStatement statement = addDoor.prepareStatement(
                    "INSERT INTO ftb_doors (world, x, y, z, type) VALUES (?,?,?,?,?);"
            );
            statement.setString(1, loc.getWorld().getName());
            statement.setInt(2, (int) loc.getX());
            statement.setInt(3, (int) loc.getY());
            statement.setInt(4, (int) loc.getZ());
            statement.setString(5, doorType);
            statement.executeUpdate();
            addDoor.commit();
            statement.close();
            addDoor.close();
        }
        catch (NullPointerException | SQLException npe) {
            p.sendMessage(ChatColor.RED + "A fatal error occurred while trying add this door.");
            return false;
        }

        String doorBlock = block.getType().toString();
        p.sendMessage(
                color(GenesisFTB.getPlugin().config.getString("settings.prefix")) +
                        ChatColor.WHITE + " " + doorBlock + ChatColor.GREEN + " at " + ChatColor.WHITE + "x" + ChatColor.WHITE + loc.getX() + " y" + loc.getY() + " z" + loc.getZ() + ChatColor.GREEN + " is now a " + doorType.toUpperCase() + " door.");
        return true;
    }

    public boolean removeDoor(Player p, Location loc) {
        Block block = loc.getBlock();
        try {
            Connection removeDoor = GenesisFTB.getDataSource().getConnection();
            PreparedStatement statement = removeDoor.prepareStatement(
                    "DELETE FROM ftb_doors WHERE world=? AND x=? AND y=? AND z=?;"
            );
            statement.setString(1, loc.getWorld().getName());
            statement.setInt(2, (int) loc.getX());
            statement.setInt(3, (int) loc.getY());
            statement.setInt(4, (int) loc.getZ());
            statement.executeUpdate();
            removeDoor.commit();
            statement.close();
            removeDoor.close();
        }
        catch (NullPointerException | SQLException npe) {
            p.sendMessage(ChatColor.RED + "A fatal error occurred while trying remove this door.");
            return false;
        }
        String doorBlock = block.getType().toString();
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

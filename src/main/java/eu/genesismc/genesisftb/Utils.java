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
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.type.Door;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Utils {



    /**
     * Generates a new code that must be specified in
     * order to reset the database.
     */
    public void newResetCode() {
        Random r = new Random();
        String alphabet = "0123456789";
        String preparedCode = "";
        for (int i = 0; i < 5; i++) {
            preparedCode = preparedCode + alphabet.charAt(r.nextInt(alphabet.length()));
        }
        GenesisFTB.getPlugin().resetCode = preparedCode;
    }

    /**
     * Instructs the plugin to set the state of doors.
     *
     * @param doorTypes  The type of door to control. Can be either
     *                   'game' or 'main'
     * @param state      Whether the door should be open or not.
     *                   Use <code>true</code> to open.
     *                   Use <code>false</code> to close.
     */
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
            Bukkit.getServer().getLogger().info("GenesisFTB: No main doors!");
        }
    }

    /**
     * Instructs the plugin to reset game doors.
     *
     */
    public void resetGameDoors() {
        try {
            Connection gameDoors = GenesisFTB.getDataSource().getConnection();
            PreparedStatement statement = gameDoors.prepareStatement(
                    "SELECT * FROM ftb_doors WHERE type=?;"
            );
            statement.setString(1, "game");
            gameDoors.commit();
            ResultSet doors = statement.executeQuery();
            while (doors.next()) {
                World world = Bukkit.getWorld(doors.getString("world"));
                int x = doors.getInt("x");
                int y = doors.getInt("y");
                int z = doors.getInt("z");
                Location loc = new Location(world,x,y,z);
                String doorState = doors.getString("doorstate");
                Block b = loc.getBlock();
                Openable d = (Openable) b.getBlockData();
                d.setOpen(doorState.equals("open"));
                b.setBlockData(d);
            }
            statement.close();
            doors.close();
            gameDoors.close();
        }
        catch (NullPointerException | SQLException exc) {
            Bukkit.getServer().getLogger().info("GenesisFTB: No game doors!");
        }
    }

    /**
     * Checks whether the block at the specified location is
     * a door known to the plugin.
     *
     * @param loc  Location of the block. Must be a valid {@link org.bukkit.Location} object.
     * @return <code>true</code> or <code>false</code>
     */
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

    /**
     * Returns information about a door, such as location,
     * type, and reset state (open/closed)
     *
     * @param p    {@link org.bukkit.entity.Player} object to send feedback message.
     * @param loc  Location of the block. Must be a valid {@link org.bukkit.Location} object.
     */
    public void doorInfo(Player p, Location loc) {
        FileConfiguration config = GenesisFTB.getPlugin().getConfig();
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
                World world = Bukkit.getWorld(doors.getString("world"));
                int x = doors.getInt("x");
                int y = doors.getInt("y");
                int z = doors.getInt("z");
                String type = doors.getString("type");
                String state = doors.getString("doorstate");
                p.sendMessage(color(config.getString("settings.prefix") + " &r&f" + loc.getBlock().getType().toString().toUpperCase() + " &eat &fx" + x + " y" + y + " z" + z + " &ein &f" + world.getName() + " &eis a &f" + type.toUpperCase() + " &edoor, with a reset state of &f" + state.toUpperCase()));
            }
            statement.close();
            whichDoor.close();
        }
        catch (NullPointerException | SQLException npe) {
            // ignore
        }
    }

    /**
     * Adds the block at the specified location to the
     * database of known doors.
     *
     * @param p         {@link org.bukkit.entity.Player} object to notify
     * @param loc       Location of the block. Must be a valid {@link org.bukkit.Location} object
     * @param doorType  Type of door, can be 'main' or 'game'
     * @return <code>true</code> or <code>false</code>
     */
    public boolean addDoor(Player p, Location loc, String doorType) {
        Block block = loc.getBlock();
        String doorState = ((Openable)block.getState().getBlockData()).isOpen() ? "open" : "closed";
        try {
            Connection addDoor = GenesisFTB.getDataSource().getConnection();
            PreparedStatement statement = addDoor.prepareStatement(
                    "INSERT INTO ftb_doors (world, x, y, z, type, doorstate) VALUES (?,?,?,?,?,?);"
            );
            statement.setString(1, loc.getWorld().getName());
            statement.setInt(2, (int) loc.getX());
            statement.setInt(3, (int) loc.getY());
            statement.setInt(4, (int) loc.getZ());
            statement.setString(5, doorType);
            statement.setString(6, doorState);
            statement.executeUpdate();
            addDoor.commit();
            statement.close();
            addDoor.close();
        }
        catch (NullPointerException | SQLException npe) {
            p.sendMessage(ChatColor.RED + "A fatal error occurred while trying add this door.");
            p.sendMessage(ChatColor.RED + "Door was in state " + doorState);
            p.sendMessage(ChatColor.RED + "At " + (int)loc.getX() + " " + (int)loc.getY() + " " + (int)loc.getZ() + ", it is a type of " + doorType);
            return false;
        }

        String doorBlock = block.getType().toString();
        if (doorType.equalsIgnoreCase("MAIN")) {
            p.sendMessage(
                    color(GenesisFTB.getPlugin().config.getString("settings.prefix")) +
                            ChatColor.WHITE + " " + doorBlock + ChatColor.GREEN + " at " + ChatColor.WHITE + "x" + ChatColor.WHITE + (int)loc.getX() + " y" + (int)loc.getY() + " z" + (int)loc.getZ() + ChatColor.GREEN + " is now a " + ChatColor.WHITE + doorType.toUpperCase() + ChatColor.GREEN + " door.");
        }
        else {
            p.sendMessage(
                    color(GenesisFTB.getPlugin().config.getString("settings.prefix")) +
                            ChatColor.WHITE + " " + doorBlock + ChatColor.GREEN + " at " + ChatColor.WHITE + "x" + ChatColor.WHITE + (int)loc.getX() + " y" + (int)loc.getY() + " z" + (int)loc.getZ() + ChatColor.GREEN + " is now a " + doorType.toUpperCase() + " door and will reset to a " + ChatColor.WHITE + doorState.toUpperCase() + ChatColor.GREEN + " position.");
        }
        return true;
    }

    /**
     * Removes the block at the specified location from the
     * database of known doors.
     *
     * @param p         {@link org.bukkit.entity.Player} object to notify
     * @param loc       Location of the block. Must be a valid {@link org.bukkit.Location} object
     * @return <code>true</code> or <code>false</code>
     */
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
                        ChatColor.WHITE + " " + doorBlock + ChatColor.GREEN + " at " + ChatColor.WHITE + "x" + ChatColor.WHITE + (int)loc.getX() + " y" + (int)loc.getY() + " z" + (int)loc.getZ() + ChatColor.GREEN + " is no longer part of the game.");
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

    public TextComponent clickToTeleportButton(String w, int x, int y, int z, String msg, int buttonNumber) {
        TextComponent message = new TextComponent();
        TextComponent startMsg = new TextComponent(msg);
        startMsg.setClickEvent( new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ftb teleport "+w+" "+x+" "+y+" "+z));
        startMsg.setHoverEvent( new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.GREEN + "Teleport to button " + ChatColor.WHITE + buttonNumber + ChatColor.GREEN + " (auto GMSP)")));
        message.addExtra(startMsg);
        return message;
    }

    public TextComponent clickToTeleportDoor(String w, int x, int y, int z, String msg) {
        TextComponent message = new TextComponent();
        TextComponent startMsg = new TextComponent(msg);
        startMsg.setClickEvent( new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ftb teleport "+w+" "+x+" "+y+" "+z));
        startMsg.setHoverEvent( new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.GREEN + "Teleport to this door (auto GMSP)")));
        message.addExtra(startMsg);
        return message;
    }

    public List<String> getPlayerList() {
        List<String> output = new ArrayList<String>();
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            output.add(p.getName());
        }
        return output;
    }

    public String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

}

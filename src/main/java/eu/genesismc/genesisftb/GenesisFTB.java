package eu.genesismc.genesisftb;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.Openable;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Random;

public final class GenesisFTB extends JavaPlugin implements Listener, CommandExecutor {

    private static GenesisFTB plugin;
    private DataSource dataSource;
    public String resetCode = "";

    public static GenesisFTB getPlugin() {
        return plugin;
    }
    public static DataSource getDataSource() {
        return getInstance().dataSource;
    }
    public static GenesisFTB getInstance() {
        return (GenesisFTB) Bukkit.getPluginManager().getPlugin("GenesisFTB");
    }

    public ArrayList<Location> buttons = new ArrayList<Location>();
    public ArrayList<String> foundList = new ArrayList<String>();
    public Boolean inGame = false;
    public Boolean doorsOpen = false;
    public FileConfiguration config = this.getConfig();

    @Override
    public void onLoad() {
        getLogger().info(ChatColor.GREEN + "GenesisFTB" + ChatColor.AQUA + " Hooking into WorldGuard..");
        WorldGuardManager.getInstance().registerFlags();
    }

    @Override
    public void onDisable() {
        getLogger().info("GenesisFTB has been disabled.");
        GenesisFTB.getDataSource().closeSQL();
        plugin = null;
    }

    @Override
    public void onEnable() {
        plugin = this;
        loadDefaultConfig();
        newResetCode();
        PluginManager pm = Bukkit.getServer().getPluginManager(); // register plugin manager
        this.getCommand("ftb").setExecutor(new CommandHandler());
        this.getCommand("ftb").setTabCompleter(new CommandHandler());
        pm.registerEvents(this, plugin);
        pm.registerEvents(new PlayerInteract(), plugin);
        pm.registerEvents(new BlockPlace(), plugin);
        new Holograms().register();
        dataSource = new DataSource();
        dataSource.createTable();
        getLogger().info(ChatColor.GREEN + "GenesisFTB" + ChatColor.AQUA + ChatColor.BOLD + " successfully loaded!");
    }

    public void loadDefaultConfig() {
        final FileConfiguration config = this.getConfig();
        config.addDefault("settings.enabled", true);
        config.addDefault("settings.remove-button", true);
        config.addDefault("settings.open-doors", true);
        config.addDefault("settings.open-doors-end-game", false);
        config.addDefault("settings.close-doors", true);
        config.addDefault("settings.close-doors-first-button", true);
        config.addDefault("settings.announce-place", true);
        config.addDefault("settings.announce-remove", true);
        config.addDefault("settings.announce-reset", true);
        config.addDefault("settings.prefix", "[FTB]");
        config.addDefault("settings.na-placeholder", "N/A");
        config.addDefault("settings.found-message", "%player% has found the button!");
        config.addDefault("settings.placed-message", "%player% has placed a button!");
        config.addDefault("settings.removed-message", "%player% has removed a button!");
        config.addDefault("settings.start-message", "The game has started! There are %count% buttons to find!");
        config.addDefault("settings.reset-message", "The game has been reset and all buttons have been removed.");
        config.addDefault("settings.end-message", "The game has ended! All buttons have been found.");
        config.addDefault("settings.reward-message", "You have been awarded prizes!");
        config.addDefault("settings.doors-closed-message", "The arena doors have now been closed!");
        config.addDefault("rewards", "");
        config.addDefault("doors", "");

        /*******************************
         * SQL SUPPORT - NOT IMPLEMENTED
         ******************************//*
        config.addDefault("sql.ip", "localhost");
        config.addDefault("sql.port", "3306");
        config.addDefault("sql.database", "genesisftb");
        config.addDefault("sql.table-prefix", "genesisftb_");
        config.addDefault("sql.username", "minecraft");
        config.addDefault("sql.password", "password");*/
        config.options().copyDefaults(true);
        saveConfig();
    }

    public void newResetCode() {
        Random r = new Random();
        String alphabet = "0123456789";
        String preparedCode = "";
        for (int i = 0; i < 5; i++) {
            preparedCode = preparedCode + alphabet.charAt(r.nextInt(alphabet.length()));
        }
        GenesisFTB.getPlugin().resetCode = preparedCode;
    }

    public void openDoors() {
        for (Object openList : config.getList("doors")) {
            Location loc = (Location) openList;
            Block b = loc.getBlock();
            Openable d = (Openable) b.getBlockData();
            d.setOpen(true);
            b.setBlockData(d);
        }
        doorsOpen = true;
    }

    public void closeDoors() {
        for (Object openList : config.getList("doors")) {
            Location loc = (Location) openList;
            Block b = loc.getBlock();
            Openable d = (Openable) b.getBlockData();
            d.setOpen(false);
            b.setBlockData(d);
        }
        doorsOpen = false;
    }

}

package eu.genesismc.genesisftb;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.data.Openable;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import eu.genesismc.genesisftb.ItemTools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;

public final class GenesisFTB extends JavaPlugin implements Listener {

    private static GenesisFTB plugin;
    private DataSource dataSource;
    private Utils utils;
    public String resetCode = "";
    public boolean gameMode = true;
    private final NamespacedKey toolKey = new NamespacedKey(this, "ftb-tool");
    public ArrayList<Location> buttons = new ArrayList<Location>();
    public ArrayList<String> foundList = new ArrayList<String>();
    public Map<String, Integer> foundCount = new HashMap<String, Integer>();
    public Boolean inGame = false;
    public Boolean mainDoorsOpen = false;
    public Boolean gameDoorsOpen = false;
    public FileConfiguration config = this.getConfig();

    private static ItemTools itemTools;
    public static ItemTools getItemTools() {
        return itemTools;
    }

    public static GenesisFTB getPlugin() {
        return plugin;
    }
    public static DataSource getDataSource() {
        return getInstance().dataSource;
    }
    public static Utils utils() {
        return getInstance().utils;
    }
    public static GenesisFTB getInstance() {
        return (GenesisFTB) Bukkit.getPluginManager().getPlugin("GenesisFTB");
    }
    public NamespacedKey getToolKey() {
        return toolKey;
    }

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
        PluginManager pm = Bukkit.getServer().getPluginManager(); // register plugin manager
        this.getCommand("ftb").setExecutor(new CommandHandler());
        this.getCommand("ftb").setTabCompleter(new CommandHandler());
        pm.registerEvents(this, this);
        pm.registerEvents(new PlayerInteract(), this);
        pm.registerEvents(new BlockPlace(), this);
        new Holograms().register();
        dataSource = new DataSource();
        dataSource.createTable();
        itemTools = new ItemTools(this);
        utils = new Utils();
        getLogger().info(ChatColor.GREEN + "GenesisFTB" + ChatColor.AQUA + ChatColor.BOLD + " successfully loaded!");
        GenesisFTB.utils().newResetCode();
    }

    public void loadDefaultConfig() {
        final FileConfiguration config = this.getConfig();
        config.addDefault("settings.enabled", true);
        config.addDefault("settings.remove-button", true);
        config.addDefault("settings.set-maindoors-on-start", true); // true = open
        config.addDefault("settings.set-maindoors-on-end", false);  // false = closed
        config.addDefault("settings.set-maindoors-on-reset", false);
        config.addDefault("settings.set-gamedoors-on-start", false);
        config.addDefault("settings.set-gamedoors-on-end", false);
        config.addDefault("settings.set-gamedoors-on-reset", false);
        config.addDefault("settings.close-maindoors-first-button", true);
        config.addDefault("settings.announce-place", true);
        config.addDefault("settings.announce-remove", true);
        config.addDefault("settings.announce-reset", true);
        config.addDefault("settings.prefix", "[FTB]");
        config.addDefault("settings.na-placeholder", "N/A");
        config.addDefault("settings.max-top-places", 10);
        config.addDefault("settings.found-message", "%player% has found the button!");
        config.addDefault("settings.placed-message", "%player% has placed a button!");
        config.addDefault("settings.removed-message", "%player% has removed a button!");
        config.addDefault("settings.start-message", "The game has started! There are %count% buttons to find!");
        config.addDefault("settings.reset-message", "The game has been reset and all buttons have been removed.");
        config.addDefault("settings.end-message", "The game has ended! All buttons have been found.");
        config.addDefault("settings.reward-message", "You have been awarded prizes!");
        config.addDefault("settings.doors-closed-message", "The arena doors have now been closed!");
        config.addDefault("rewards", Collections.emptyList());

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

}

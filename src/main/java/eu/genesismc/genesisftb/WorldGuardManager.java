package eu.genesismc.genesisftb;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.BooleanFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.RegionQuery;

public class WorldGuardManager {


    public static final BooleanFlag FTB_ZONE = new BooleanFlag("ftb-zone");
    private static WorldGuardManager instance;

    public static boolean isFTB(Location location) {
        com.sk89q.worldedit.util.Location loc = BukkitAdapter.adapt(location);
        RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        Boolean value = query.queryValue(loc, null, (BooleanFlag) FTB_ZONE);
        if (value == null) { return false; }
        else { return value.booleanValue(); }
    }

    public static WorldGuardManager getInstance() {
        if(instance == null) {
            instance = new WorldGuardManager();
        }
        return instance;
    }

    public void registerFlags() {
        try {
            FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
            try {
                registry.register(FTB_ZONE);
                Bukkit.getLogger().info("[GenesisFTB]" + ChatColor.GREEN + " GenesisFTB" + ChatColor.AQUA + " WorldGuard hook successful!");
            } catch (FlagConflictException e) {
                e.printStackTrace();
                Bukkit.getLogger().info("[GenesisFTB]" + ChatColor.GREEN + " GenesisFTB" + ChatColor.RED + " WorldGuard hook failed!");
            }
        } catch (NoClassDefFoundError e) {
            Bukkit.getLogger().info("[GenesisFTB]" + ChatColor.GREEN + " GenesisFTB" + ChatColor.RED + " WorldGuard hook failed! Install WorldGuard!");
        }
    }

}

package eu.genesismc.genesisftb;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Openable;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class PlayerInteract implements Listener, Cancellable {

    @EventHandler
    public void on(PlayerInteractEvent e) {

        final Block block = e.getClickedBlock();
        if (block == null) return;

        if (WorldGuardManager.isFTB(e.getClickedBlock().getLocation())) {

            FileConfiguration config = GenesisFTB.getPlugin().getConfig();
            String player = e.getPlayer().getName();
            Player p = e.getPlayer();

            if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getHand() != EquipmentSlot.OFF_HAND) {

                /***************************
                 *  ADMIN TOOL CHECKS
                 **************************/
                if (GenesisFTB.getItemTools().isStackingTool(p.getInventory().getItemInMainHand())) {
                    if (!p.hasPermission("genesisftb.admin")) { return; }
                    if (e.getHand() == EquipmentSlot.OFF_HAND) { return; }
                    if (p.isOp()) {
                        if (p.isSneaking()) {
                            GenesisFTB.getItemTools().shiftMode(p);
                            e.setUseInteractedBlock(Event.Result.DENY);
                            e.setCancelled(true);
                            return;
                        }
                        if (!(e.getClickedBlock().getBlockData() instanceof Openable)) {
                            p.sendMessage(color(config.getString("settings.prefix")) + ChatColor.RED + " That block is not openable.");
                            return;
                        }
                        GenesisFTB.getItemTools().doAction(p, e.getClickedBlock());
                        e.setUseInteractedBlock(Event.Result.DENY);
                        e.setCancelled(true);
                        return;
                    }
                }

                /***************************
                 *  NORMAL GAMEPLAY ACTIONS
                 **************************/
                if (!e.getClickedBlock().getType().toString().contains("_BUTTON")) {
                    return;
                }

                if (!GenesisFTB.getPlugin().inGame) {
                     if (p.hasPermission("genesisftb.admin") && GenesisFTB.getPlugin().buttons.contains(e.getClickedBlock().getLocation())) {
                         p.sendMessage(color(config.getString("settings.prefix")) + ChatColor.RED + " You can't press game buttons before the game has started.");
                         e.setCancelled(true);
                         return;
                     }
                     return;
                }

                if (!GenesisFTB.getPlugin().buttons.contains(e.getClickedBlock().getLocation())) {
                    p.sendMessage(
                            ChatColor.GOLD + "That button is decorative and " + ChatColor.WHITE + ChatColor.BOLD + "not" + ChatColor.WHITE + ChatColor.BOLD + " part of the game!"
                    );
                    return;
                }

                GenesisFTB.getPlugin().buttons.remove(e.getClickedBlock().getLocation());
                int totalButtons = GenesisFTB.getPlugin().buttons.size();

                Integer foundButtons = GenesisFTB.getDataSource().getWins(p.getUniqueId());
                Integer totalWins = foundButtons + 1;
                GenesisFTB.getDataSource().updateWins(e.getPlayer().getUniqueId(), e.getPlayer().getName(), totalWins);

                if (config.getBoolean("settings.remove-button")) {
                    e.setCancelled(true);
                    e.getClickedBlock().setType(Material.AIR);
                }

                GenesisFTB.utils().sendToAll(config.getString("settings.found-message").replace("%player%", player).replace("%count%", String.valueOf(totalButtons)), true);
                if (GenesisFTB.getPlugin().mainDoorsOpen) {
                    GenesisFTB.utils().sendToAll(config.getString("settings.doors-closed-message"), true);
                }

                if (config.getBoolean("settings.close-maindoors-first-button") && GenesisFTB.getPlugin().foundList.size() == 0) {
                    GenesisFTB.utils().openDoors("main", false);
                }

                Location loc = e.getClickedBlock().getLocation();
                GenesisFTB.getPlugin().foundList.add("&f" + player + " &efound a button at &fx" + loc.getX() + " y" + loc.getY() + " z" + loc.getZ());

                p.sendMessage(""+
                        color(config.getString("settings.prefix")) + " " +
                        color(config.getString("settings.reward-message"))
                );

                for (String str : GenesisFTB.getPlugin().getConfig().getStringList("rewards")) {
                    String rewardCommand = str.replace("%player%", player);
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), rewardCommand);
                }

                p.sendMessage(ChatColor.GOLD + "You have now found " + ChatColor.WHITE + totalWins + ChatColor.GOLD + " buttons.");

                if (totalButtons == 0) {
                    GenesisFTB.getPlugin().inGame = false;
                    GenesisFTB.utils().sendToAll(config.getString("settings.end-message").replace("%player%", player).replace("%count%", String.valueOf(totalButtons)), true);
                    GenesisFTB.utils().openDoors("main", config.getBoolean("settings.set-maindoors-on-end"));
                    GenesisFTB.utils().openDoors("game", config.getBoolean("settings.set-gamedoors-on-end"));
                }
            }

            if (e.getAction() == Action.LEFT_CLICK_BLOCK && p.hasPermission("genesisftb.admin")) {
                if (!GenesisFTB.getPlugin().buttons.contains(e.getClickedBlock().getLocation())) { return; }
                if (GenesisFTB.getPlugin().inGame) {
                    p.sendMessage(color(config.getString("settings.prefix")) + ChatColor.RED + " You can't remove a button while a game is running!");
                    e.setCancelled(true);
                    return;
                }
                GenesisFTB.getPlugin().buttons.remove(e.getClickedBlock().getLocation());
                int totalButtons = GenesisFTB.getPlugin().buttons.size();
                Location loc = e.getClickedBlock().getLocation();
                e.getPlayer().sendMessage(ChatColor.GOLD + "Button at "+ ChatColor.WHITE + "x" + ChatColor.WHITE + loc.getX() + " y" + loc.getY() + " z" + loc.getZ() + ChatColor.GOLD + " was removed from the game.");

                if (config.getBoolean("settings.announce-remove")) {
                    GenesisFTB.utils().sendToAll(config.getString("settings.removed-message").replace("%player%", player).replace("%count%", String.valueOf(totalButtons)), true);
                }
            }
        }
    }

    public String color(String s) { return ChatColor.translateAlternateColorCodes('&', s); }

    @Override
    public boolean isCancelled() { return false; }
    @Override
    public void setCancelled(boolean b) { }
}

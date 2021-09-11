package eu.genesismc.genesisftb;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.Arrays;

public class ItemTools {

    private final GenesisFTB ftb;
    public ItemTools(GenesisFTB ftb) {
        this.ftb = ftb;
    }

    public ItemStack createStackingTool() {
        ItemStack is = new ItemStack(Material.BLAZE_ROD, 1);
        is.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);
        ItemMeta itemMeta = is.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "FTB Arena Setup Tool");
        itemMeta.setLore(Arrays.asList(
                " ",
                ChatColor.GREEN + "Shiny stick used for setting",
                ChatColor.GREEN + "up Find the Button arenas.",
                " ",
                ChatColor.GOLD + "- Right-click to perform action" ,
                ChatColor.GOLD + "- Shift-right-click to change mode"));
        itemMeta.getPersistentDataContainer().set(ftb.getToolKey(), PersistentDataType.INTEGER, 1);
        is.setItemMeta(itemMeta);
        return is;
    }

    public void giveStackingTool(Player p) {
        p.getInventory().addItem(createStackingTool());
    }

    public boolean isStackingTool(ItemStack is) {
        if (is.getItemMeta() == null) {
            return false;
        }
        return is.getItemMeta().getPersistentDataContainer().has(ftb.getToolKey(), PersistentDataType.INTEGER);
    }

    public int getModeId(Player p) {
        ItemStack itemStack = p.getInventory().getItemInMainHand();
        return itemStack.getItemMeta().getPersistentDataContainer().getOrDefault(GenesisFTB.getPlugin().getToolKey(), PersistentDataType.INTEGER, 1);
    }

    public ToolMode getMode(Player p) {
        for (ToolMode t : ToolMode.values()) {
            if (t.getId() == getModeId(p)) {
                return t;
            }
        }
        throw new UnsupportedOperationException("No tool for ID");
    }

    public void shiftMode(Player p) {
        ItemStack itemStack = p.getInventory().getItemInMainHand();
        ItemMeta itemMeta = p.getInventory().getItemInMainHand().getItemMeta();
        int nextMode = (getModeId(p) + 1) > ToolMode.values().length ? 1 : getModeId(p) + 1;
        itemMeta.getPersistentDataContainer().set(GenesisFTB.getPlugin().getToolKey(), PersistentDataType.INTEGER, nextMode);
        itemStack.setItemMeta(itemMeta);
        p.getInventory().setItemInMainHand(itemStack);
        switch (getMode(p)) {
            case ADD_GAME_DOOR:
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "[+] Add a GAME door"));
                break;
            case ADD_MAIN_DOOR:
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "[+] Add a MAIN door"));
                break;
            case REMOVE_DOOR:
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.DARK_RED + "" + ChatColor.BOLD + "[-] Remove a door"));
                break;
        }
    }

    public void doAction(Player p, Block block) {
        Location loc = block.getLocation();
        String prefix = GenesisFTB.getPlugin().config.getString("settings.prefix");

        switch (getMode(p)) {

            case ADD_GAME_DOOR:
                if (GenesisFTB.utils().isDoor(loc)) {
                    p.sendMessage(color(prefix) + ChatColor.RED + " That door is already linked.");
                    break;
                }
                GenesisFTB.utils().addDoor(p, loc, "game");
                break;

            case ADD_MAIN_DOOR:
                if (GenesisFTB.utils().isDoor(loc)) {
                    p.sendMessage(color(prefix) + ChatColor.RED + " That door is already linked.");
                    break;
                }
                GenesisFTB.utils().addDoor(p, loc, "main");
                break;

            case REMOVE_DOOR:
                if (GenesisFTB.utils().isDoor(loc)) {
                    GenesisFTB.utils().removeDoor(p, loc);
                    break;
                }
                p.sendMessage(color(prefix) + ChatColor.RED + " That door is not linked.");
                break;
        }
    }

    enum ToolMode {
        ADD_GAME_DOOR(1),
        ADD_MAIN_DOOR(2),
        REMOVE_DOOR(3);

        private final int id;
        ToolMode(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

    }

    public String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

}

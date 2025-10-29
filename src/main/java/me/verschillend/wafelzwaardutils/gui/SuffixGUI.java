package me.verschillend.wafelzwaardutils.gui;

import me.verschillend.wafelzwaardutils.Wafelzwaardutils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class SuffixGUI implements Listener {

    private final Wafelzwaardutils plugin;
    private final String GUI_TITLE = "§6§lSuffixes";
    private final int ITEMS_PER_PAGE = 28; // 7x4 grid (excluding navigation items)
    private final Map<UUID, Integer> playerPages = new HashMap<>();

    public SuffixGUI(Wafelzwaardutils plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void openGUI(Player player) {
        openGUI(player, 0);
    }

    public void openGUI(Player player, int page) {
        playerPages.put(player.getUniqueId(), page);

        Inventory gui = Bukkit.createInventory(null, 54, GUI_TITLE + " §8(Page " + (page + 1) + ")");

        // get suffixes from config
        ConfigurationSection suffixesSection = plugin.getConfig().getConfigurationSection("suffixes");
        if (suffixesSection == null) {
            player.sendMessage("§cNo suffixes configured!");
            return;
        }

        List<String> suffixKeys = new ArrayList<>(suffixesSection.getKeys(false));
        Collections.sort(suffixKeys); // sort alphabetically

        int totalPages = (int) Math.ceil((double) suffixKeys.size() / ITEMS_PER_PAGE);
        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, suffixKeys.size());

        // add suffix items (slots 10-16, 19-25, 28-34, 37-43)
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};
        int slotIndex = 0;

        for (int i = startIndex; i < endIndex && slotIndex < slots.length; i++, slotIndex++) {
            String suffixKey = suffixKeys.get(i);
            ConfigurationSection suffixConfig = suffixesSection.getConfigurationSection(suffixKey);

            if (suffixConfig == null) continue;

            String suffix = suffixConfig.getString("suffix", "");
            String permission = suffixConfig.getString("permission", "suffix." + suffixKey);
            String materialName = suffixConfig.getString("material", "NAME_TAG");
            String displayName = suffixConfig.getString("display-name", suffix);

            Material material;
            try {
                material = Material.valueOf(materialName.toUpperCase());
            } catch (IllegalArgumentException e) {
                material = Material.NAME_TAG;
            }

            if (player.hasPermission(permission)) {
                gui.setItem(slots[slotIndex], createSuffixItem(material, displayName, suffix, "§aClick to set this suffix!", suffixKey));
            } else {
                gui.setItem(slots[slotIndex], createItem(Material.BARRIER, displayName, "§cRequires permission: " + permission));
            }
        }

        // add navigation items
        if (page > 0) {
            gui.setItem(45, createItem(Material.ARROW, "§ePrevious Page", "§7Click to go to page " + page));
        }

        if (page < totalPages - 1) {
            gui.setItem(53, createItem(Material.ARROW, "§eNext Page", "§7Click to go to page " + (page + 2)));
        }

        // add remove suffix item
        gui.setItem(48, createItem(Material.BARRIER, "§cRemove Suffix", "§7Click to remove your current suffix"));

        // add close item
        gui.setItem(49, createItem(Material.RED_STAINED_GLASS_PANE, "§cClose", "§7Click to close this menu"));

        player.openInventory(gui);
    }

    private ItemStack createSuffixItem(Material material, String displayName, String suffix, String lore, String suffixKey) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        meta.setLore(Arrays.asList(
                "§7Suffix: " + suffix,
                "",
                lore
        ));

        // store suffix key in item for identification
        List<String> itemLore = new ArrayList<>(meta.getLore());
        itemLore.add("§0" + suffixKey); // Hidden identifier
        meta.setLore(itemLore);

        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().startsWith(GUI_TITLE)) return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        int slot = event.getSlot();
        String itemName = clickedItem.getItemMeta().getDisplayName();

        // handle navigation
        if (slot == 45 && itemName.equals("§ePrevious Page")) {
            int currentPage = playerPages.getOrDefault(player.getUniqueId(), 0);
            openGUI(player, Math.max(0, currentPage - 1));
            return;
        }

        if (slot == 53 && itemName.equals("§eNext Page")) {
            int currentPage = playerPages.getOrDefault(player.getUniqueId(), 0);
            openGUI(player, currentPage + 1);
            return;
        }

        // handle close
        if (slot == 49 && itemName.equals("§cClose")) {
            player.closeInventory();
            return;
        }

        // handle remove suffix
        if (slot == 48 && itemName.equals("§cRemove Suffix")) {
            player.closeInventory();
            removeSuffix(player);
            return;
        }

        // handle suffix selection
        if (clickedItem.getType() != Material.BARRIER && clickedItem.hasItemMeta() && clickedItem.getItemMeta().hasLore()) {
            List<String> lore = clickedItem.getItemMeta().getLore();
            if (lore.size() >= 4) {
                String suffixKey = lore.get(lore.size() - 1).substring(2); // Remove "§0" prefix
                setSuffix(player, suffixKey);
                player.closeInventory();
            }
        }
    }

    private void setSuffix(Player player, String suffixKey) {
        ConfigurationSection suffixConfig = plugin.getConfig().getConfigurationSection("suffixes." + suffixKey);
        if (suffixConfig == null) {
            player.sendMessage("§cSuffix not found!");
            return;
        }

        String suffix = suffixConfig.getString("suffix", "");
        String permission = suffixConfig.getString("permission", "suffix." + suffixKey);

        if (!player.hasPermission(permission)) {
            player.sendMessage("§cYou don't have permission to use this suffix!");
            return;
        }

        // execute LuckPerms command
        String command = "lp user " + player.getName() + " meta setsuffix 1 \"" + suffix + "\"";
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);

        player.sendMessage("§aYour suffix has been set to: " + suffix);
    }

    private void removeSuffix(Player player) {
        String command = "lp user " + player.getName() + " meta removesuffix 1";
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);

        player.sendMessage("§aYour suffix has been removed!");
    }
}
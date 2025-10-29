package me.verschillend.wafelzwaardutils.gui;

import me.verschillend.wafelzwaardutils.Wafelzwaardutils;
import me.verschillend.wafelzwaardutils.config.GemShopConfig;
import net.kyori.adventure.text.Component;
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

public class GemShopGUI implements Listener {

    private final Wafelzwaardutils plugin;
    private final GemShopConfig gemShopConfig;
    private final String MAIN_GUI_TITLE = "§6§lGem Shop";
    private final String SECTION_GUI_TITLE = "§6§lGem Shop §8- ";
    private final String CONFIRM_GUI_TITLE = "§c§lInsufficient Gems";
    private final Map<UUID, String> playerCurrentSection = new HashMap<>();
    private final String CONFIRM_PURCHASE_TITLE = "§a§lConfirm Purchase";
    private final Map<UUID, PurchaseData> pendingPurchases = new HashMap<>();

    private static class PurchaseData {
        String sectionKey;
        String itemKey;
        double price;
        String itemName;

        PurchaseData(String sectionKey, String itemKey, double price, String itemName) {
            this.sectionKey = sectionKey;
            this.itemKey = itemKey;
            this.price = price;
            this.itemName = itemName;
        }
    }

    public GemShopGUI(Wafelzwaardutils plugin, GemShopConfig gemShopConfig) {
        this.plugin = plugin;
        this.gemShopConfig = gemShopConfig;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void openMainGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, MAIN_GUI_TITLE);

        plugin.getDatabaseManager().getPlayerGems(player.getUniqueId()).thenAccept(gems -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                setupMainGUI(gui, player, gems);
                player.openInventory(gui);
            });
        });
    }

    private void setupMainGUI(Inventory gui, Player player, double gems) {
        ConfigurationSection mainSection = gemShopConfig.getConfig().getConfigurationSection("main-menu");
        if (mainSection == null) {
            gui.setItem(22, createItem(Material.BARRIER, "§cNo shop configured", "§7Contact an administrator"));
            return;
        }

        // load main menu items (decorative/info items)
        ConfigurationSection itemsSection = mainSection.getConfigurationSection("items");
        if (itemsSection != null) {
            for (String itemKey : itemsSection.getKeys(false)) {
                ConfigurationSection itemConfig = itemsSection.getConfigurationSection(itemKey);
                if (itemConfig == null) continue;

                int slot = itemConfig.getInt("slot", -1);
                if (slot < 0 || slot >= 54) continue;

                ItemStack item = createConfiguredItem(itemConfig);
                if (item != null) {
                    gui.setItem(slot, item);
                }
            }
        }

        // load sections in top row (slots 0-8)
        ConfigurationSection sectionsConfig = gemShopConfig.getConfig().getConfigurationSection("sections");
        if (sectionsConfig != null) {
            List<String> sectionKeys = new ArrayList<>(sectionsConfig.getKeys(false));
            int slotIndex = 0;

            for (String sectionKey : sectionKeys) {
                if (slotIndex >= 9) break; // only top row

                ConfigurationSection sectionConfig = sectionsConfig.getConfigurationSection(sectionKey);
                if (sectionConfig == null) continue;

                String displayName = sectionConfig.getString("display-name", "§eSection");
                String materialName = sectionConfig.getString("material", "CHEST");
                List<String> lore = sectionConfig.getStringList("lore");
                boolean enchanted = sectionConfig.getBoolean("enchanted", false);
                List<String> flags = sectionConfig.getStringList("flags");

                Material material;
                try {
                    material = Material.valueOf(materialName.toUpperCase());
                } catch (IllegalArgumentException e) {
                    material = Material.CHEST;
                }

                ItemStack sectionItem = new ItemStack(material);
                ItemMeta meta = sectionItem.getItemMeta();
                meta.setDisplayName(displayName);
                meta.setLore(lore);

                if (enchanted) {
                    meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                }

                applyItemFlags(meta, flags);
                sectionItem.setItemMeta(meta);

                gui.setItem(slotIndex, sectionItem);
                slotIndex++;
            }
        }

        // gem balance (slot 53 - bottom right)
        gui.setItem(53, createItem(Material.EMERALD,
                "§aYour Gems",
                "§7Balance: §6" + String.format("%.2f", gems) + " §7gems"
        ));

        // close button (slot 49 - bottom middle)
        gui.setItem(49, createItem(Material.BARRIER, "§cClose Menu", "§7Click to close"));
    }

    public void openSectionGUI(Player player, String sectionKey) {
        ConfigurationSection sectionConfig = gemShopConfig.getConfig().getConfigurationSection("sections." + sectionKey);
        if (sectionConfig == null) {
            player.sendMessage("§cSection not found!");
            return;
        }

        String sectionName = sectionConfig.getString("display-name", "§eSection");
        Inventory gui = Bukkit.createInventory(null, 54, SECTION_GUI_TITLE + sectionName);

        playerCurrentSection.put(player.getUniqueId(), sectionKey);

        plugin.getDatabaseManager().getPlayerGems(player.getUniqueId()).thenAccept(gems -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                setupSectionGUI(gui, player, sectionKey, sectionConfig, gems);
                player.openInventory(gui);
            });
        });
    }

    private void setupSectionGUI(Inventory gui, Player player, String sectionKey, ConfigurationSection sectionConfig, double gems) {
        ConfigurationSection itemsSection = sectionConfig.getConfigurationSection("items");
        if (itemsSection == null) {
            gui.setItem(22, createItem(Material.BARRIER, "§cNo items configured", "§7Contact an administrator"));
            return;
        }

        // load section items
        for (String itemKey : itemsSection.getKeys(false)) {
            ConfigurationSection itemConfig = itemsSection.getConfigurationSection(itemKey);
            if (itemConfig == null) continue;

            int slot = itemConfig.getInt("slot", -1);
            if (slot < 0 || slot >= 54) continue;

            ItemStack item = createConfiguredItem(itemConfig);
            if (item != null) {
                // check if item requires permission
                String requiredPermission = itemConfig.getString("required-permission", "");
                boolean hasPermission = requiredPermission.isEmpty() || player.hasPermission(requiredPermission);

                // add price and permission info to lore if it's a shop item
                if (itemConfig.getBoolean("shop-item", true)) {
                    double price = itemConfig.getDouble("price", 0.0);
                    ItemMeta meta = item.getItemMeta();
                    List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
                    lore.add("");

                    if (!hasPermission) {
                        // item is locked
                        lore.add("§7Required permission:");
                        lore.add("§e" + requiredPermission);
                    } else {
                        lore.add("§7Price: §6" + String.format("%.2f", price) + " §7gems");
                        lore.add("§eClick to purchase!");
                    }

                    meta.setLore(lore);
                    item.setItemMeta(meta);
                }

                gui.setItem(slot, item);
            }
        }

        // gem balance (slot 53)
        gui.setItem(53, createItem(Material.EMERALD,
                "§aYour Gems",
                "§7Balance: §6" + String.format("%.2f", gems) + " §7gems"
        ));

        // back button (slot 45)
        gui.setItem(45, createItem(Material.ARROW, "§eBack to Main Menu", "§7Click to go back"));

        // close button (slot 49)
        gui.setItem(49, createItem(Material.BARRIER, "§cClose Menu", "§7Click to close"));
    }

    public void openInsufficientGemsGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, CONFIRM_GUI_TITLE);

        // emerald block - go to store
        gui.setItem(11, createItem(Material.EMERALD_BLOCK,
                "§a§lVisit Store",
                "§7Click to visit our store",
                "§7to purchase more gems!"
        ));

        // redstone block - close
        gui.setItem(15, createItem(Material.REDSTONE_BLOCK,
                "§c§lCancel",
                "§7Click to close"
        ));

        player.openInventory(gui);
    }

    public void openPurchaseConfirmGUI(Player player, String sectionKey, String itemKey, double price, String itemName) {
        Inventory gui = Bukkit.createInventory(null, 27, CONFIRM_PURCHASE_TITLE);

        // store purchase data
        pendingPurchases.put(player.getUniqueId(), new PurchaseData(sectionKey, itemKey, price, itemName));

        // emerald block - confirm purchase
        gui.setItem(11, createItem(Material.EMERALD_BLOCK,
                "§a§lConfirm Purchase",
                "§7Item: " + itemName,
                "§7Price: §6" + String.format("%.2f", price) + " §7gems",
                "",
                "§eClick to confirm!"
        ));

        // redstone block - cancel
        gui.setItem(15, createItem(Material.REDSTONE_BLOCK,
                "§c§lCancel",
                "§7Click to cancel purchase"
        ));

        player.openInventory(gui);
    }

    private ItemStack createConfiguredItem(ConfigurationSection config) {
        String materialName = config.getString("material", "STONE");
        Material material;
        try {
            material = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid material: " + materialName);
            return null;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        String displayName = config.getString("display-name", "§eItem");
        meta.setDisplayName(displayName);

        List<String> lore = config.getStringList("lore");
        meta.setLore(lore);

        if (config.getBoolean("enchanted", false)) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        }

        List<String> flags = config.getStringList("flags");
        applyItemFlags(meta, flags);

        item.setItemMeta(meta);
        return item;
    }

    private void applyItemFlags(ItemMeta meta, List<String> flags) {
        for (String flag : flags) {
            try {
                ItemFlag itemFlag = ItemFlag.valueOf(flag.toUpperCase());
                meta.addItemFlags(itemFlag);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid item flag: " + flag);
            }
        }
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
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = event.getView().getTitle();

        // handle main GUI
        if (title.equals(MAIN_GUI_TITLE)) {
            event.setCancelled(true);
            handleMainGUIClick(player, event.getSlot());
        }
        // handle section GUI
        else if (title.startsWith(SECTION_GUI_TITLE)) {
            event.setCancelled(true);
            handleSectionGUIClick(player, event.getSlot(), event.getCurrentItem());
        }
        // handle insufficient gems GUI
        else if (title.equals(CONFIRM_GUI_TITLE)) {
            event.setCancelled(true);
            handleInsufficientGemsClick(player, event.getSlot());
        }
        // handle purchase confirmation GUI
        else if (title.equals(CONFIRM_PURCHASE_TITLE)) {
            event.setCancelled(true);
            handlePurchaseConfirmClick(player, event.getSlot());
        }
    }

    private void handleMainGUIClick(Player player, int slot) {
        // check if clicked on a section (top row slots 0-8)
        if (slot >= 0 && slot < 9) {
            ConfigurationSection sectionsConfig = gemShopConfig.getConfig().getConfigurationSection("sections");
            if (sectionsConfig != null) {
                List<String> sectionKeys = new ArrayList<>(sectionsConfig.getKeys(false));
                if (slot < sectionKeys.size()) {
                    openSectionGUI(player, sectionKeys.get(slot));
                }
            }
        }
        // close button
        else if (slot == 49) {
            player.closeInventory();
        }
    }

    private void handleSectionGUIClick(Player player, int slot, ItemStack clickedItem) {
        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        // back button
        if (slot == 45) {
            openMainGUI(player);
            return;
        }

        // close button
        if (slot == 49) {
            player.closeInventory();
            return;
        }

        // gem balance display
        if (slot == 53) {
            return;
        }

        // handle item purchase
        String sectionKey = playerCurrentSection.get(player.getUniqueId());
        if (sectionKey == null) return;

        ConfigurationSection itemsSection = gemShopConfig.getConfig().getConfigurationSection("sections." + sectionKey + ".items");
        if (itemsSection == null) return;

        // find the item config by slot
        for (String itemKey : itemsSection.getKeys(false)) {
            ConfigurationSection itemConfig = itemsSection.getConfigurationSection(itemKey);
            if (itemConfig == null) continue;

            if (itemConfig.getInt("slot", -1) == slot) {
                // check if it's a shop item
                if (!itemConfig.getBoolean("shop-item", true)) {
                    return; // not purchasable
                }

                // check if player has required permission (if specified)
                String requiredPermission = itemConfig.getString("required-permission", "");
                if (!requiredPermission.isEmpty() && !player.hasPermission(requiredPermission)) {
                    player.sendMessage("§c§lNO PERMISSION!");
                    player.sendMessage("§7You need the permission §e" + requiredPermission + "§7 to purchase this item!");
                    return;
                }

                double price = itemConfig.getDouble("price", 0.0);

                // open confirmation GUI instead of immediate purchase
                Bukkit.getScheduler().runTask(plugin, () -> {
                    openPurchaseConfirmGUI(player, sectionKey, itemKey, price, clickedItem.getItemMeta().getDisplayName());
                });
                break;
            }
        }
    }

    private void handleInsufficientGemsClick(Player player, int slot) {
        if (slot == 11) { // emerald block - go to store
            String storeURL = gemShopConfig.getConfig().getString("store-url", "https://example.com/store");
            player.closeInventory();
            player.sendMessage(Component.text("§a§lVisit our store: §e" + storeURL));
            player.sendMessage(Component.text("§7Click to open: ").append(
                    Component.text("§b§n" + storeURL)
                            .clickEvent(net.kyori.adventure.text.event.ClickEvent.openUrl(storeURL))
            ));
        } else if (slot == 15) { // redstone block - cancel
            player.closeInventory();
        }
    }

    private void handlePurchaseConfirmClick(Player player, int slot) {
        PurchaseData purchaseData = pendingPurchases.get(player.getUniqueId());

        if (purchaseData == null) {
            player.closeInventory();
            return;
        }

        if (slot == 11) { // Confirm purchase
            // check if player has enough gems
            plugin.getDatabaseManager().getPlayerGems(player.getUniqueId()).thenAccept(gems -> {
                if (gems >= purchaseData.price) {
                    // process purchase
                    plugin.getDatabaseManager().removePlayerGems(player.getUniqueId(), player.getName(), purchaseData.price).thenAccept(newBalance -> {
                        // get item config to execute commands
                        ConfigurationSection itemConfig = gemShopConfig.getConfig().getConfigurationSection(
                                "sections." + purchaseData.sectionKey + ".items." + purchaseData.itemKey
                        );

                        if (itemConfig != null) {
                            // execute purchase commands
                            List<String> commands = itemConfig.getStringList("commands");
                            for (String cmd : commands) {
                                String processedCommand = cmd.replace("%player%", player.getName());
                                Bukkit.getScheduler().runTask(plugin, () -> {
                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
                                });
                            }
                        }

                        player.sendMessage("§a§lPURCHASE SUCCESSFUL!");
                        player.sendMessage("§7Item: " + purchaseData.itemName);
                        player.sendMessage("§7Cost: §6" + String.format("%.2f", purchaseData.price) + " §7gems");
                        player.sendMessage("§7New Balance: §6" + String.format("%.2f", newBalance) + " §7gems");

                        // clean up and go back to section GUI
                        pendingPurchases.remove(player.getUniqueId());
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            openSectionGUI(player, purchaseData.sectionKey);
                        });
                    });
                } else {
                    // not enough gems
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        player.sendMessage("§c§lINSUFFICIENT GEMS!");
                        player.sendMessage("§7Required: §6" + String.format("%.2f", purchaseData.price) + " §7gems");
                        player.sendMessage("§7You have: §6" + String.format("%.2f", gems) + " §7gems");
                        pendingPurchases.remove(player.getUniqueId());
                        openInsufficientGemsGUI(player);
                    });
                }
            });
        } else if (slot == 15) { // cancel - go back to section
            String sectionKey = purchaseData.sectionKey;
            pendingPurchases.remove(player.getUniqueId());
            openSectionGUI(player, sectionKey);
        }
    }
}

package me.verschillend.wafelzwaardutils.gui;

import me.verschillend.wafelzwaardutils.Wafelzwaardutils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CCGUI implements Listener {

    private final Wafelzwaardutils plugin;
    private final String GUI_TITLE = "§f§lChatcolors";

    public CCGUI(Wafelzwaardutils plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void openGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 36, GUI_TITLE);

        // Red - slot 10
        if (player.hasPermission("chatcolor.red")) {
            gui.setItem(10, createItem(Material.RED_DYE, "§cRed", "§aClick!"));
        } else {
            gui.setItem(10, createItem(Material.BARRIER, "§cRed", "§cRequires permission 'chatcolor.red' to use!"));
        }

        // Dark Red - slot 11
        if (player.hasPermission("chatcolor.darkred")) {
            gui.setItem(11, createLeatherArmor(Material.LEATHER_CHESTPLATE, "§4Dark red", Color.fromRGB(127, 0, 0), "§aClick!"));
        } else {
            gui.setItem(11, createItem(Material.BARRIER, "§4Dark red", "§cRequires permission 'chatcolor.darkred' to use!"));
        }

        // Gold - slot 12
        if (player.hasPermission("chatcolor.gold")) {
            gui.setItem(12, createItem(Material.ORANGE_DYE, "§6Gold", "§aClick!"));
        } else {
            gui.setItem(12, createItem(Material.BARRIER, "§6Gold", "§cRequires permission 'chatcolor.gold' to use!"));
        }

        // Yellow - slot 13
        if (player.hasPermission("chatcolor.yellow")) {
            gui.setItem(13, createItem(Material.YELLOW_DYE, "§eYellow", "§aClick!"));
        } else {
            gui.setItem(13, createItem(Material.BARRIER, "§eYellow", "§cRequires permission 'chatcolor.yellow' to use!"));
        }

        // Lime - slot 14
        if (player.hasPermission("chatcolor.lime")) {
            gui.setItem(14, createItem(Material.LIME_DYE, "§aLime", "§aClick!"));
        } else {
            gui.setItem(14, createItem(Material.BARRIER, "§aLime", "§cRequires permission 'chatcolor.lime' to use!"));
        }

        // Green - slot 15
        if (player.hasPermission("chatcolor.green")) {
            gui.setItem(15, createItem(Material.GREEN_DYE, "§2Green", "§aClick!"));
        } else {
            gui.setItem(15, createItem(Material.BARRIER, "§2Green", "§cRequires permission 'chatcolor.green' to use!"));
        }

        // Aqua - slot 16
        if (player.hasPermission("chatcolor.aqua")) {
            gui.setItem(16, createItem(Material.LIGHT_BLUE_DYE, "§bAqua", "§aClick!"));
        } else {
            gui.setItem(16, createItem(Material.BARRIER, "§bAqua", "§cRequires permission 'chatcolor.aqua' to use!"));
        }

        // Cyan - slot 19
        if (player.hasPermission("chatcolor.cyan")) {
            gui.setItem(19, createItem(Material.CYAN_DYE, "§3Cyan", "§aClick!"));
        } else {
            gui.setItem(19, createItem(Material.BARRIER, "§3Cyan", "§cRequires permission 'chatcolor.cyan' to use!"));
        }

        // Dark Blue - slot 20
        if (player.hasPermission("chatcolor.darkblue")) {
            gui.setItem(20, createLeatherArmor(Material.LEATHER_CHESTPLATE, "§1Dark blue", Color.fromRGB(0, 0, 170), "§aClick!"));
        } else {
            gui.setItem(20, createItem(Material.BARRIER, "§1Dark blue", "§cRequires permission 'chatcolor.darkblue' to use!"));
        }

        // Blue - slot 21
        if (player.hasPermission("chatcolor.blue")) {
            gui.setItem(21, createItem(Material.BLUE_DYE, "§9Blue", "§aClick!"));
        } else {
            gui.setItem(21, createItem(Material.BARRIER, "§9Blue", "§cRequires permission 'chatcolor.blue' to use!"));
        }

        // Pink - slot 22
        if (player.hasPermission("chatcolor.pink")) {
            gui.setItem(22, createItem(Material.PINK_DYE, "§dPink", "§aClick!"));
        } else {
            gui.setItem(22, createItem(Material.BARRIER, "§dPink", "§cRequires permission 'chatcolor.pink' to use!"));
        }

        // Purple - slot 23
        if (player.hasPermission("chatcolor.purple")) {
            gui.setItem(23, createItem(Material.PURPLE_DYE, "§5Purple", "§aClick!"));
        } else {
            gui.setItem(23, createItem(Material.BARRIER, "§5Purple", "§cRequires permission 'chatcolor.purple' to use!"));
        }

        // White - slot 24
        if (player.hasPermission("chatcolor.white")) {
            gui.setItem(24, createItem(Material.WHITE_DYE, "§fWhite", "§aClick!"));
        } else {
            gui.setItem(24, createItem(Material.BARRIER, "§fWhite", "§cRequires permission 'chatcolor.white' to use!"));
        }

        // Gray - slot 25
        if (player.hasPermission("chatcolor.gray")) {
            gui.setItem(25, createItem(Material.GRAY_DYE, "§8Gray", "§aClick!"));
        } else {
            gui.setItem(25, createItem(Material.BARRIER, "§8Gray", "§cRequires permission 'chatcolor.gray' to use!"));
        }

        // Reset - slot 35
        ItemStack resetItem = createItem(Material.BARRIER, "§7Reset color", "§aClick!");
        resetItem.addUnsafeEnchantment(Enchantment.UNBREAKING, 1);
        ItemMeta resetMeta = resetItem.getItemMeta();
        resetMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        resetItem.setItemMeta(resetMeta);
        gui.setItem(35, resetItem);

        // Highlight current color
        plugin.getDatabaseManager().getPlayerColor(player.getUniqueId()).thenAccept(colorCode -> {
            highlightCurrentColor(gui, colorCode);
            // Update inventory for the player
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (player.getOpenInventory().getTitle().equals(GUI_TITLE)) {
                    player.updateInventory();
                }
            });
        });

        player.openInventory(gui);
    }

    private void highlightCurrentColor(Inventory gui, int colorCode) {
        Map<Integer, Integer> colorSlotMap = new HashMap<>();
        colorSlotMap.put(12, 10); // c -> Red
        colorSlotMap.put(4, 11);  // 4 -> Dark Red
        colorSlotMap.put(6, 12);  // 6 -> Gold
        colorSlotMap.put(14, 13); // e -> Yellow
        colorSlotMap.put(10, 14); // a -> Lime
        colorSlotMap.put(2, 15);  // 2 -> Green
        colorSlotMap.put(11, 16); // b -> Aqua
        colorSlotMap.put(3, 19);  // 3 -> Cyan
        colorSlotMap.put(1, 20);  // 1 -> Dark Blue
        colorSlotMap.put(9, 21);  // 9 -> Blue
        colorSlotMap.put(13, 22); // d -> Pink
        colorSlotMap.put(5, 23);  // 5 -> Purple
        colorSlotMap.put(15, 24); // f -> White
        colorSlotMap.put(8, 25);  // 8 -> Gray
        colorSlotMap.put(7, 35);  // 7 -> Reset

        Integer slot = colorSlotMap.get(colorCode);
        if (slot != null) {
            ItemStack item = gui.getItem(slot);
            if (item != null && item.getType() != Material.BARRIER) {
                item.addUnsafeEnchantment(Enchantment.UNBREAKING, 1);
                ItemMeta meta = item.getItemMeta();
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
                item.setItemMeta(meta);
                gui.setItem(slot, item);
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

    private ItemStack createLeatherArmor(Material material, String name, Color color, String... lore) {
        ItemStack item = new ItemStack(material);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        meta.setColor(color);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DYE);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(GUI_TITLE)) return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;

        int slot = event.getSlot();

        switch (slot) {
            case 35 -> { // Reset
                player.closeInventory();
                setChatColor(player, '7');
                player.sendMessage("§aYour chat color has been reset to §7this§a!");
            }
            case 10 -> { // Red
                if (player.hasPermission("chatcolor.red")) {
                    player.closeInventory();
                    setChatColor(player, 'c');
                    player.sendMessage("§aYour chat color has been set to §cRed§a!");
                }
            }
            case 11 -> { // Dark Red
                if (player.hasPermission("chatcolor.darkred")) {
                    player.closeInventory();
                    setChatColor(player, '4');
                    player.sendMessage("§aYour chat color has been set to §4Dark Red§a!");
                }
            }
            case 12 -> { // Gold
                if (player.hasPermission("chatcolor.gold")) {
                    player.closeInventory();
                    setChatColor(player, '6');
                    player.sendMessage("§aYour chat color has been set to §6Gold§a!");
                }
            }
            case 13 -> { // Yellow
                if (player.hasPermission("chatcolor.yellow")) {
                    player.closeInventory();
                    setChatColor(player, 'e');
                    player.sendMessage("§aYour chat color has been set to §eYellow§a!");
                }
            }
            case 14 -> { // Lime
                if (player.hasPermission("chatcolor.lime")) {
                    player.closeInventory();
                    setChatColor(player, 'a');
                    player.sendMessage("§aYour chat color has been set to §aLime§a!");
                }
            }
            case 15 -> { // Green
                if (player.hasPermission("chatcolor.green")) {
                    player.closeInventory();
                    setChatColor(player, '2');
                    player.sendMessage("§aYour chat color has been set to §2Green§a!");
                }
            }
            case 16 -> { // Aqua
                if (player.hasPermission("chatcolor.aqua")) {
                    player.closeInventory();
                    setChatColor(player, 'b');
                    player.sendMessage("§aYour chat color has been set to §bAqua§a!");
                }
            }
            case 19 -> { // Cyan
                if (player.hasPermission("chatcolor.cyan")) {
                    player.closeInventory();
                    setChatColor(player, '3');
                    player.sendMessage("§aYour chat color has been set to §3Cyan§a!");
                }
            }
            case 20 -> { // Dark Blue
                if (player.hasPermission("chatcolor.darkblue")) {
                    player.closeInventory();
                    setChatColor(player, '1');
                    player.sendMessage("§aYour chat color has been set to §1Dark Blue§a!");
                }
            }
            case 21 -> { // Blue
                if (player.hasPermission("chatcolor.blue")) {
                    player.closeInventory();
                    setChatColor(player, '9');
                    player.sendMessage("§aYour chat color has been set to §9Blue§a!");
                }
            }
            case 22 -> { // Pink
                if (player.hasPermission("chatcolor.pink")) {
                    player.closeInventory();
                    setChatColor(player, 'd');
                    player.sendMessage("§aYour chat color has been set to §dPink§a!");
                }
            }
            case 23 -> { // Purple
                if (player.hasPermission("chatcolor.purple")) {
                    player.closeInventory();
                    setChatColor(player, '5');
                    player.sendMessage("§aYour chat color has been set to §5Purple§a!");
                }
            }
            case 24 -> { // White
                if (player.hasPermission("chatcolor.white")) {
                    player.closeInventory();
                    setChatColor(player, 'f');
                    player.sendMessage("§aYour chat color has been set to §fWhite§a!");
                }
            }
            case 25 -> { // Gray
                if (player.hasPermission("chatcolor.gray")) {
                    player.closeInventory();
                    setChatColor(player, '8');
                    player.sendMessage("§aYour chat color has been set to §8Gray§a!");
                }
            }
        }
    }

    private void setChatColor(Player player, char colorCode) {
        plugin.getDatabaseManager().getPlayerGems(player.getUniqueId()).thenAccept(gems -> {
            plugin.getDatabaseManager().savePlayerData(player.getUniqueId(), player.getName(), colorCode, gems);
        });
        //plugin.getDatabaseManager().savePlayerData(player.getUniqueId(), player.getName(), colorCode, gems2);
    }
}
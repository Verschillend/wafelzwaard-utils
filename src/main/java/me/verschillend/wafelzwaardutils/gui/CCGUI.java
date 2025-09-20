package me.verschillend.wafelzwaardutils.gui;

import me.verschillend.wafelzwaardutils.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class CCGUI implements Listener {

    private final Wafelzwaardutils plugin;
    private final String GUI_TITLE = "§6Chat colors";

    public CCGUI(Wafelzwaardutils plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void openGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, GUI_TITLE);

        gui.setItem(10, createItem(Material.DIAMOND, "§bAdd Points", "§7Click to add 10 points"));
        gui.setItem(12, createItem(Material.EMERALD, "§aView Points", "§7Click to see your points"));
        gui.setItem(14, createItem(Material.REDSTONE, "§cRemove Points", "§7Click to remove 5 points"));
        gui.setItem(16, createItem(Material.BARRIER, "§cClose", "§7Click to close this GUI"));

        player.openInventory(gui);
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
        if (!event.getView().getTitle().equals(GUI_TITLE)) return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        String itemName = clickedItem.getItemMeta().getDisplayName();

        switch (itemName) {
            case "§bAdd Points" -> {
                /*plugin.getDatabaseManager().getPlayerColor(player.getUniqueId()).thenAccept(currentPoints -> {
                    int newPoints = currentPoints + 10;
                    plugin.getDatabaseManager().savePlayerData(player.getUniqueId(), player.getName(), newPoints);
                    player.sendMessage("§a+10 points! You now have " + newPoints + " points.");
                });*/
            }
            case "§aView Points" -> {
                /*plugin.getDatabaseManager().getPlayerColor(player.getUniqueId()).thenAccept(points -> {
                    player.sendMessage("§eYou have §6" + points + "§e points!");
                });*/
            }
            case "§cRemove Points" -> {
                /*plugin.getDatabaseManager().getPlayerColor(player.getUniqueId()).thenAccept(currentPoints -> {
                    int newPoints = Math.max(0, currentPoints - 5);
                    plugin.getDatabaseManager().savePlayerData(player.getUniqueId(), player.getName(), newPoints);
                    player.sendMessage("§c-5 points! You now have " + newPoints + " points.");
                });*/
            }
            case "§cClose" -> player.closeInventory();
        }
    }
}

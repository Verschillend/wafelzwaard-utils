package me.verschillend.wafelzwaardutils.gui;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.verschillend.wafelzwaardutils.*;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import me.clip.placeholderapi.PlaceholderAPI;

import java.util.Arrays;
import java.util.List;

public class BwGUI implements Listener {

    private final Wafelzwaardutils plugin;
    private final String GUI_TITLE = "§6§lGamemode selector";

    public BwGUI(Wafelzwaardutils plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void openGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, GUI_TITLE);

        String bedwars = PlaceholderAPI.setPlaceholders(player, "%bungee_bedwars-1%");
        String fbf = PlaceholderAPI.setPlaceholders(player, "%bungee_fireball-fight-1%");

        gui.setItem(11, createItem(Material.RED_BED, "§aBedwars",
                "", "§e§lPlayers online: " + bedwars));
        gui.setItem(15, createItem(Material.FIRE_CHARGE, "§aFireball Fight",
                "", "§e§lPlayers online: " + fbf));
        gui.setItem(13, createItem(Material.BARRIER, "§aPractice", "\n§7This mode is coming soon!"));

        player.openInventory(gui);
    }

    private ItemStack createItem(Material material, String name, String lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
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

    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
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
            case "§aBedwars" -> {
                player.closeInventory();
                player.sendMessage(Component.text("§8Sending you to bedwars..."));

                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("Connect");
                out.writeUTF("bedwars-1");

                player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
            }
            case "§aFireball Fight" -> {
                player.closeInventory();
                player.sendMessage(Component.text("§8Sending you to fireball fight..."));

                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("Connect");
                out.writeUTF("fireball-fight-1");

                player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
            }
            case "§aPractice" -> {
                player.closeInventory();
                player.sendMessage("§cThis mode is not finished yet!");
            }
            //case "§cClose" -> player.closeInventory();
        }
    }
}

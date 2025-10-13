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

public class ReferralGUI implements Listener {

    private final Wafelzwaardutils plugin;
    private final String GUI_TITLE = "§6§lReferral System";
    private final Map<UUID, Integer> playerPages = new HashMap<>();

    public ReferralGUI(Wafelzwaardutils plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void openGUI(Player player) {
        openGUI(player, 0);
    }

    public void openGUI(Player player, int page) {
        playerPages.put(player.getUniqueId(), page);

        Inventory gui = Bukkit.createInventory(null, 54, GUI_TITLE);

        // Get player's referral data
        plugin.getDatabaseManager().getReferralCount(player.getUniqueId()).thenAccept(referralCount -> {
            plugin.getDatabaseManager().getPlayerReferralCode(player.getUniqueId()).thenAccept(referralCode -> {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    setupGUI(gui, player, referralCount, referralCode, page);
                    player.openInventory(gui);
                });
            });
        });
    }

    private void setupGUI(Inventory gui, Player player, int referralCount, String referralCode, int page) {
        // Info item (slot 4)
        gui.setItem(4, createItem(Material.BOOK,
                "§6Your Referral Info",
                "§7Referral Code: §e" + (referralCode != null ? referralCode : "Generating..."),
                "§7Total Referrals: §a" + referralCount,
                "",
                "§7Share your referral code with friends!",
                "§7They can use §e/refer " + (referralCode != null ? referralCode : "CODE") + "§7 to get rewards!"
        ));

        // Get milestones from config
        ConfigurationSection milestonesSection = plugin.getConfig().getConfigurationSection("referral-milestones");
        if (milestonesSection == null) {
            gui.setItem(22, createItem(Material.BARRIER, "§cNo milestones configured", "§7Contact an administrator"));
            return;
        }

        List<String> milestoneKeys = new ArrayList<>(milestonesSection.getKeys(false));
        milestoneKeys.sort((a, b) -> {
            try {
                return Integer.compare(Integer.parseInt(a), Integer.parseInt(b));
            } catch (NumberFormatException e) {
                return a.compareTo(b);
            }
        });

        // Pagination
        int itemsPerPage = 8; // Slots 19-26 (8 slots)
        int totalPages = (int) Math.ceil((double) milestoneKeys.size() / itemsPerPage);
        int startIndex = page * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, milestoneKeys.size());

        // Milestone slots: 19, 20, 21, 22, 23, 24, 25, 26
        int[] milestoneSlots = {19, 20, 21, 22, 23, 24, 25, 26};
        int slotIndex = 0;

        for (int i = startIndex; i < endIndex && slotIndex < milestoneSlots.length; i++) {
            String milestoneKey = milestoneKeys.get(i);
            ConfigurationSection milestoneConfig = milestonesSection.getConfigurationSection(milestoneKey);

            if (milestoneConfig == null) continue;

            int requiredReferrals = Integer.parseInt(milestoneKey);
            String command = milestoneConfig.getString("command", "").replace("%player%", player.getName());
            String description = milestoneConfig.getString("description", "Milestone reward");

            boolean completed = referralCount >= requiredReferrals;
            Material material = completed ? Material.EMERALD : Material.COAL;

            List<String> lore = new ArrayList<>();
            lore.add("§7Required Referrals: §e" + requiredReferrals);
            lore.add("§7Reward: §a" + description);
            lore.add("");

            if (completed) {
                lore.add("§a✓ Milestone Reached!");
            } else {
                lore.add("§7Progress: §e" + referralCount + "§7/§e" + requiredReferrals);
                int progress = Math.min(10, (int) ((double) referralCount / requiredReferrals * 10));
                StringBuilder progressBar = new StringBuilder("§7[");
                for (int j = 0; j < 10; j++) {
                    if (j < progress) {
                        progressBar.append("§a■");
                    } else {
                        progressBar.append("§7■");
                    }
                }
                progressBar.append("§7]");
                lore.add(progressBar.toString());
            }

            ItemStack milestoneItem = createItem(material,
                    "§6" + requiredReferrals + " Referrals Milestone",
                    lore.toArray(new String[0])
            );

            if (completed) {
                milestoneItem.addUnsafeEnchantment(Enchantment.UNBREAKING, 1);
                ItemMeta meta = milestoneItem.getItemMeta();
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                milestoneItem.setItemMeta(meta);
            }

            gui.setItem(milestoneSlots[slotIndex], milestoneItem);
            slotIndex++;
        }

        // Navigation
        if (page > 0) {
            gui.setItem(45, createItem(Material.ARROW, "§ePrevious Page", "§7Go to page " + page));
        }

        if (page < totalPages - 1) {
            gui.setItem(53, createItem(Material.ARROW, "§eNext Page", "§7Go to page " + (page + 2)));
        }

        // Close button
        gui.setItem(49, createItem(Material.RED_STAINED_GLASS_PANE, "§cClose", "§7Close this menu"));
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

        int slot = event.getSlot();

        // Navigation
        if (slot == 45) { // Previous page
            int currentPage = playerPages.getOrDefault(player.getUniqueId(), 0);
            openGUI(player, Math.max(0, currentPage - 1));
        } else if (slot == 53) { // Next page
            int currentPage = playerPages.getOrDefault(player.getUniqueId(), 0);
            openGUI(player, currentPage + 1);
        } else if (slot == 49) { // Close
            player.closeInventory();
        }
    }
}
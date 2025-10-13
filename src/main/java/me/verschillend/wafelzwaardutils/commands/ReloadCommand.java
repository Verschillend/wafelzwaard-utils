package me.verschillend.wafelzwaardutils.commands;

import me.verschillend.wafelzwaardutils.Wafelzwaardutils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Random;

public class ReloadCommand implements CommandExecutor {

    private final Wafelzwaardutils plugin;
    private BukkitTask broadcastTask;
    private final Random random = new Random();

    public ReloadCommand(Wafelzwaardutils plugin) {
        this.plugin = plugin;
        startBroadcastLoop();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("wafelzwaardutils.reload")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        try {
            sender.sendMessage("§eReloading WafelzwaardUtils configuration...");

            // Close any open GUIs to prevent issues
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getOpenInventory().getTitle().contains("Suffixes") ||
                        player.getOpenInventory().getTitle().contains("Chatcolors")) {
                    player.closeInventory();
                }
            }

            // Reload the config
            plugin.reloadConfig();

            restartBroadcastLoop();

            // Clear any cached data if needed
            // For example, if you have cached suffix data, clear it here

            sender.sendMessage("§aWafelzwaardUtils config has been reloaded successfully!");
            sender.sendMessage("§7Note: Players with open GUIs had them closed automatically.");

            plugin.getLogger().info("Config reloaded by " + sender.getName());

        } catch (Exception e) {
            sender.sendMessage("§cFailed to reload config: " + e.getMessage());
            plugin.getLogger().severe("Failed to reload config: " + e.getMessage());
            e.printStackTrace();
        }

        return true;
    }

    private void startBroadcastLoop() {
        broadcastTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            List<String> broadcasts = plugin.getConfig().getStringList("broadcasts");

            if (broadcasts == null || broadcasts.isEmpty()) {
                return; // no broadcasts defined
            }

            String message = broadcasts.get(random.nextInt(broadcasts.size()));
            Bukkit.broadcast(MiniMessage.miniMessage().deserialize(message));
        }, 0L, 6000L); // every 5 mins
    }

    private void restartBroadcastLoop() {
        if (broadcastTask != null && !broadcastTask.isCancelled()) {
            broadcastTask.cancel();
        }
        startBroadcastLoop();
    }
}

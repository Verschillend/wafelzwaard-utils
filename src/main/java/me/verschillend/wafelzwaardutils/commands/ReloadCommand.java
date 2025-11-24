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
    private BukkitTask serverTask;
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

            // close any open GUIs to prevent issues
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getOpenInventory().getTitle().contains("Suffixes") ||
                        player.getOpenInventory().getTitle().contains("Chatcolors")) {
                    player.closeInventory();
                }
            }

            // reload main config
            plugin.reloadConfig();

            // reload gem shop config
            plugin.getGemShopConfig().reloadConfig();

            restartBroadcastLoop();

            // clear any cached data if needed

            sender.sendMessage("§aWafelzwaardUtils config has been reloaded successfully!");
            sender.sendMessage("§aGem shop config has been reloaded successfully!");
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
        boolean oneblock = plugin.getConfig().getBoolean("server.oneblock", false);
        boolean lobby = plugin.getConfig().getBoolean("server.lobby", false);
        boolean fbf = plugin.getConfig().getBoolean("server.fbf", false);
        boolean minigames = plugin.getConfig().getBoolean("server.minigames", false);
        serverTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            int count = Bukkit.getOnlinePlayers().size();
            if (oneblock) {
                plugin.getDatabaseManager().updateServerPlayerCount("oneblock", count);
            }
            if (lobby) {
                plugin.getDatabaseManager().updateServerPlayerCount("lobby", count);
            }
            if (fbf) {
                plugin.getDatabaseManager().updateServerPlayerCount("fireball-fight-1", count);
            }
            if (minigames) {
                plugin.getDatabaseManager().updateServerPlayerCount("minigames", count);
            }
        }, 0L, 15 * 20L); // every 15 seconds

    }

    private void restartBroadcastLoop() {
        if (broadcastTask != null && !broadcastTask.isCancelled()) {
            broadcastTask.cancel();
        }
        if (serverTask != null && !serverTask.isCancelled()) {
            serverTask.cancel();
        }
        startBroadcastLoop();
    }
}

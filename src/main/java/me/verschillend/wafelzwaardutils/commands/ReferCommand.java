package me.verschillend.wafelzwaardutils.commands;

import me.verschillend.wafelzwaardutils.Wafelzwaardutils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ReferCommand implements CommandExecutor {

    private final Wafelzwaardutils plugin;

    public ReferCommand(Wafelzwaardutils plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }

        if (!player.hasPermission("wafelzwaardutils.refer")) {
            player.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§cUsage: /refer <referral_code>");
            player.sendMessage("§7Example: /refer ABC123");
            return true;
        }

        String referralCode = args[0].toUpperCase().trim();

        // basic validation
        if (referralCode.length() < 3 || referralCode.length() > 10) {
            player.sendMessage("§cInvalid referral code format!");
            return true;
        }

        // save data just in case
        CompletableFuture<Double> future = plugin.getDatabaseManager().getPlayerGems(player.getUniqueId());
        Double result = null;
        try {
            result = future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        CompletableFuture<Character> future2 = plugin.getDatabaseManager().getPlayerColor(player.getUniqueId());
        Character result2 = null;
        try {
            result2 = future2.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        if (result == 0.0) {
            plugin.getDatabaseManager().savePlayerData(player.getUniqueId(), player.getName(), result2, 0.0).join();
        }

        // check if player was already referred
        plugin.getDatabaseManager().wasPlayerReferred(player.getUniqueId()).thenAccept(wasReferred -> {
            if (wasReferred) {
                player.sendMessage("§cYou have already been referred by someone!");
                return;
            }

            // check if player has referred someone (prevents them from being referred)
            plugin.getDatabaseManager().hasReferredSomeone(player.getUniqueId()).thenAccept(hasReferred -> {
                if (hasReferred) {
                    player.sendMessage("§cYou cannot be referred because you have already referred someone!");
                    return;
                }

                // process the referral
                plugin.getDatabaseManager().processReferral(player.getUniqueId(), referralCode).thenAccept(success -> {
                    if (success) {
                        player.sendMessage("§a§lREFERRAL SUCCESS!");
                        player.sendMessage("§aYou have been successfully referred!");

                        // reward the referred player (person who used the code)
                        executeReferralCommands("referral.referred-player-commands", player.getName());

                        // get referrer and reward them + notify + check milestones
                        plugin.getDatabaseManager().getPlayerByReferralCode(referralCode).thenAccept(referrerUuidStr -> {
                            if (referrerUuidStr != null) {
                                java.util.UUID referrerUuid = java.util.UUID.fromString(referrerUuidStr);
                                Player referrer = Bukkit.getPlayer(referrerUuid);

                                if (referrer != null) {
                                    // online referrer - use commands and notify
                                    executeReferralCommands("referral.referrer-commands", referrer.getName());

                                    referrer.sendMessage("§a§l🎉 " + player.getName() + " used your referral code!");
                                    referrer.sendMessage("§aYou earned rewards for the referral!");

                                    // check milestones
                                    plugin.getDatabaseManager().getReferralCount(referrer.getUniqueId()).thenAccept(count -> {
                                        referrer.sendMessage("§7Total referrals: §e" + count);
                                        checkMilestones(referrer, count);
                                    });
                                } else {
                                    // offline referrer - give gems directly to database
                                    double referrerGems = plugin.getConfig().getDouble("referral.referrer-gems", 50.0);
                                    plugin.getDatabaseManager().addPlayerGems(referrerUuid, "OFFLINE_PLAYER", referrerGems).thenAccept(newAmount -> {
                                        plugin.getLogger().info("Gave " + referrerGems + " gems to offline referrer " + referrerUuid);
                                    });

                                    // check milestones for offline player
                                    plugin.getDatabaseManager().getReferralCount(referrerUuid).thenAccept(count -> {
                                        // log milestone achievement for offline player
                                        checkOfflineMilestones(referrerUuid, count);
                                    });
                                }
                            }
                        });
                    } else {
                        player.sendMessage("§cInvalid referral code, or you cannot use this code!");
                        player.sendMessage("§7Make sure the code is correct and you haven't been referred before.");
                    }
                });
            });
        });

        return true;
    }

    private void executeReferralCommands(String configPath, String playerName) {
        List<String> commands = plugin.getConfig().getStringList(configPath);
        if (commands.isEmpty()) return;

        for (String cmd : commands) {
            String processedCommand = cmd.replace("%player%", playerName);
            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to execute referral command: " + processedCommand);
                    plugin.getLogger().warning("Error: " + e.getMessage());
                }
            });
        }
    }

    private void checkMilestones(Player player, int referralCount) {
        ConfigurationSection milestonesSection = plugin.getConfig().getConfigurationSection("referral-milestones");
        if (milestonesSection == null) return;

        for (String milestoneKey : milestonesSection.getKeys(false)) {
            try {
                int requiredReferrals = Integer.parseInt(milestoneKey);
                if (referralCount == requiredReferrals) { // Exactly reached this milestone
                    ConfigurationSection milestoneConfig = milestonesSection.getConfigurationSection(milestoneKey);
                    if (milestoneConfig != null) {
                        // notify player
                        player.sendMessage("§6§l✨ MILESTONE REACHED! ✨");
                        player.sendMessage("§aYou reached §6" + requiredReferrals + " §areferrals!");

                        String description = milestoneConfig.getString("description", "Milestone reward");
                        player.sendMessage("§aReward: §6" + description);

                        // execute milestone rewards
                        executeMilestoneRewards(milestoneConfig, player.getName());

                        // broadcast milestone achievement (optional)
                        if (plugin.getConfig().getBoolean("referral.broadcast-milestones", false)) {
                            Bukkit.broadcastMessage("§6" + player.getName() + " §areached §6" + requiredReferrals + " §areferrals! 🎉");
                        }
                    }
                }
            } catch (NumberFormatException ignored) {
                // skip invalid milestone keys
                plugin.getLogger().warning("Invalid milestone key in config: " + milestoneKey);
            }
        }
    }

    private void checkOfflineMilestones(java.util.UUID playerUuid, int referralCount) {
        ConfigurationSection milestonesSection = plugin.getConfig().getConfigurationSection("referral-milestones");
        if (milestonesSection == null) return;

        for (String milestoneKey : milestonesSection.getKeys(false)) {
            try {
                int requiredReferrals = Integer.parseInt(milestoneKey);
                if (referralCount == requiredReferrals) { // Exactly reached this milestone
                    ConfigurationSection milestoneConfig = milestonesSection.getConfigurationSection(milestoneKey);
                    if (milestoneConfig != null) {
                        // execute milestone rewards for offline player
                        String offlinePlayerName = Bukkit.getOfflinePlayer(playerUuid).getName();
                        if (offlinePlayerName != null) {
                            executeMilestoneRewards(milestoneConfig, offlinePlayerName);
                            plugin.getLogger().info("Offline player " + offlinePlayerName + " reached milestone: " + requiredReferrals + " referrals");
                        }
                    }
                }
            } catch (NumberFormatException ignored) {
                // skip invalid milestone keys
            }
        }
    }

    private void executeMilestoneRewards(ConfigurationSection milestoneConfig, String playerName) {
        // handle single command (backwards compatibility)
        if (milestoneConfig.contains("command")) {
            String command = milestoneConfig.getString("command", "").replace("%player%", playerName);
            if (!command.isEmpty()) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    try {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                    } catch (Exception e) {
                        plugin.getLogger().warning("Failed to execute milestone command: " + command);
                    }
                });
            }
        }

        // handle multiple commands
        if (milestoneConfig.contains("commands")) {
            List<String> commands = milestoneConfig.getStringList("commands");
            for (String cmd : commands) {
                String processedCommand = cmd.replace("%player%", playerName);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    try {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
                    } catch (Exception e) {
                        plugin.getLogger().warning("Failed to execute milestone command: " + processedCommand);
                    }
                });
            }
        }
    }
}

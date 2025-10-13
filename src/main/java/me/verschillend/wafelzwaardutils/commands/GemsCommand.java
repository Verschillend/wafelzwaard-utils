package me.verschillend.wafelzwaardutils.commands;

import me.verschillend.wafelzwaardutils.Wafelzwaardutils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GemsCommand implements CommandExecutor {

    private final Wafelzwaardutils plugin;

    public GemsCommand(Wafelzwaardutils plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cConsole must specify a player!");
                return true;
            }

            // Show own gems
            plugin.getDatabaseManager().getPlayerGems(player.getUniqueId()).thenAccept(gems -> {
                player.sendMessage("§eYou have §6" + String.format("%.2f", gems) + " §egems!");
            });
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "set" -> {
                if (!sender.hasPermission("wafelzwaardutils.gems.admin")) {
                    sender.sendMessage("§cYou don't have permission to use this command!");
                    return true;
                }

                if (args.length < 3) {
                    sender.sendMessage("§cUsage: /gems set <player> <amount>");
                    return true;
                }

                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage("§cPlayer not found!");
                    return true;
                }

                try {
                    double amount = Double.parseDouble(args[2]);
                    plugin.getDatabaseManager().setPlayerGems(target.getUniqueId(), target.getName(), amount).thenRun(() -> {
                        sender.sendMessage("§aSet " + target.getName() + "'s gems to " + String.format("%.2f", amount));
                        target.sendMessage("§aYour gems have been set to §6" + String.format("%.2f", amount) + "§a!");
                    });
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cInvalid number format!");
                }
            }

            case "add", "give" -> {
                if (!sender.hasPermission("wafelzwaardutils.gems.admin")) {
                    sender.sendMessage("§cYou don't have permission to use this command!");
                    return true;
                }

                if (args.length < 3) {
                    sender.sendMessage("§cUsage: /gems add <player> <amount>");
                    return true;
                }

                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage("§cPlayer not found!");
                    return true;
                }

                try {
                    double amount = Double.parseDouble(args[2]);
                    if (amount <= 0) {
                        sender.sendMessage("§cAmount must be positive!");
                        return true;
                    }

                    plugin.getDatabaseManager().addPlayerGems(target.getUniqueId(), target.getName(), amount).thenAccept(newAmount -> {
                        sender.sendMessage("§aAdded " + String.format("%.2f", amount) + " gems to " + target.getName() + " (Total: " + String.format("%.2f", newAmount) + ")");
                        target.sendMessage("§aYou received §6" + String.format("%.2f", amount) + " §agems! Total: §6" + String.format("%.2f", newAmount));
                    });
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cInvalid number format!");
                }
            }

            case "remove", "take" -> {
                if (!sender.hasPermission("wafelzwaardutils.gems.admin")) {
                    sender.sendMessage("§cYou don't have permission to use this command!");
                    return true;
                }

                if (args.length < 3) {
                    sender.sendMessage("§cUsage: /gems remove <player> <amount>");
                    return true;
                }

                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage("§cPlayer not found!");
                    return true;
                }

                try {
                    double amount = Double.parseDouble(args[2]);
                    if (amount <= 0) {
                        sender.sendMessage("§cAmount must be positive!");
                        return true;
                    }

                    plugin.getDatabaseManager().removePlayerGems(target.getUniqueId(), target.getName(), amount).thenAccept(newAmount -> {
                        sender.sendMessage("§aRemoved " + String.format("%.2f", amount) + " gems from " + target.getName() + " (Total: " + String.format("%.2f", newAmount) + ")");
                        target.sendMessage("§c" + String.format("%.2f", amount) + " §cgems were removed from your account! Total: §6" + String.format("%.2f", newAmount));
                    });
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cInvalid number format!");
                }
            }

            case "check" -> {
                if (!sender.hasPermission("wafelzwaardutils.gems.admin")) {
                    sender.sendMessage("§cYou don't have permission to use this command!");
                    return true;
                }

                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /gems check <player>");
                    return true;
                }

                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage("§cPlayer not found!");
                    return true;
                }

                plugin.getDatabaseManager().getPlayerGems(target.getUniqueId()).thenAccept(gems -> {
                    sender.sendMessage("§e" + target.getName() + " has §6" + String.format("%.2f", gems) + " §egems");
                });
            }

            default -> {
                sender.sendMessage("§cUsage:");
                sender.sendMessage("§7/gems - Check your gems");
                if (sender.hasPermission("wafelzwaardutils.gems.admin")) {
                    sender.sendMessage("§7/gems set <player> <amount>");
                    sender.sendMessage("§7/gems add <player> <amount>");
                    sender.sendMessage("§7/gems remove <player> <amount>");
                    sender.sendMessage("§7/gems check <player>");
                }
            }
        }

        return true;
    }
}
package me.verschillend.wafelzwaardutils.commands;

import me.verschillend.wafelzwaardutils.Wafelzwaardutils;
import me.verschillend.wafelzwaardutils.gui.ReferralGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReferralCommand implements CommandExecutor {

    private final Wafelzwaardutils plugin;
    private final ReferralGUI referralGUI;

    public ReferralCommand(Wafelzwaardutils plugin, ReferralGUI referralGUI) {
        this.plugin = plugin;
        this.referralGUI = referralGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }

        if (!player.hasPermission("wafelzwaardutils.referral")) {
            player.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        if (args.length == 0) {
            referralGUI.openGUI(player);
            return true;
        }

        // Handle subcommands if needed
        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "code" -> {
                // Show player's referral code
                plugin.getDatabaseManager().getPlayerReferralCode(player.getUniqueId()).thenAccept(code -> {
                    if (code != null) {
                        player.sendMessage("§eYour referral code: §6" + code);
                        player.sendMessage("§7Share this with friends: §e/refer " + code);
                    } else {
                        player.sendMessage("§cGenerating your referral code...");
                        plugin.getDatabaseManager().generateReferralCode(player.getUniqueId()).thenAccept(newCode -> {
                            if (newCode != null) {
                                player.sendMessage("§eYour referral code: §6" + newCode);
                                player.sendMessage("§7Share this with friends: §e/refer " + newCode);
                            } else {
                                player.sendMessage("§cFailed to generate referral code!");
                            }
                        });
                    }
                });
            }

            case "count", "stats" -> {
                // Show referral statistics
                plugin.getDatabaseManager().getReferralCount(player.getUniqueId()).thenAccept(count -> {
                    player.sendMessage("§eYou have referred §6" + count + "§e players!");
                });
            }

            case "help" -> {
                player.sendMessage("§6§lReferral System Help:");
                player.sendMessage("§e/referral §7- Open referral GUI");
                player.sendMessage("§e/referral code §7- Show your referral code");
                player.sendMessage("§e/referral count §7- Show your referral count");
                player.sendMessage("§e/refer <code> §7- Use someone's referral code");
            }

            default -> {
                referralGUI.openGUI(player);
            }
        }

        return true;
    }
}

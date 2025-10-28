package me.verschillend.wafelzwaardutils.commands;

import me.verschillend.wafelzwaardutils.Wafelzwaardutils;
import me.verschillend.wafelzwaardutils.config.DealerConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class DealerCommand implements CommandExecutor {

    private final Wafelzwaardutils plugin;
    private final DealerConfig dealerConfig;

    public DealerCommand(Wafelzwaardutils plugin, DealerConfig dealerConfig) {
        this.plugin = plugin;
        this.dealerConfig = dealerConfig;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("wafelzwaardutils.dealer")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        if (args.length == 0) {
            // Show dealer gems
            double dealerGems = dealerConfig.getDealerGems();
            sender.sendMessage("§6§lDealer Gems: §e" + String.format("%.2f", dealerGems) + " gems");
            sender.sendMessage("§7Use §e/dealer <add|remove|set> <amount>§7 to manage");
            return true;
        }

        String action = args[0].toLowerCase();

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /dealer <add|remove|set> <amount>");
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid amount!");
            return true;
        }

        switch (action) {
            case "add", "give" -> {
                dealerConfig.addDealerGems(amount);
                sender.sendMessage("§aAdded §6" + String.format("%.2f", amount) + " §agems to dealer");
                sender.sendMessage("§7New balance: §6" + String.format("%.2f", dealerConfig.getDealerGems()) + " gems");
            }
            case "remove", "take" -> {
                dealerConfig.removeDealerGems(amount);
                sender.sendMessage("§aRemoved §6" + String.format("%.2f", amount) + " §agems from dealer");
                sender.sendMessage("§7New balance: §6" + String.format("%.2f", dealerConfig.getDealerGems()) + " gems");
            }
            case "set" -> {
                dealerConfig.setDealerGems(amount);
                sender.sendMessage("§aSet dealer gems to §6" + String.format("%.2f", amount) + " gems");
            }
            default -> {
                sender.sendMessage("§cUsage: /dealer <add|remove|set> <amount>");
            }
        }

        return true;
    }
}

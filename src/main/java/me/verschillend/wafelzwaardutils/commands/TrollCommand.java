package me.verschillend.wafelzwaardutils.commands;

import me.verschillend.wafelzwaardutils.Wafelzwaardutils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TrollCommand implements CommandExecutor {

    private final Wafelzwaardutils plugin;

    public TrollCommand(Wafelzwaardutils plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("wafelzwaardutils.troll")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can run this command!");
            return true;
        }
        Player player2 = Bukkit.getPlayer(args[1]);
        assert player2 != null;
        if (player2.isOnline()) {
            if (args[0].equalsIgnoreCase("demo")) {
                player2.showDemoScreen();
            }
            else if (args[0].equalsIgnoreCase("win")) {
                player2.showWinScreen();
            }
            else if (args[0].equalsIgnoreCase("elderguardian")) {
                player2.showElderGuardian();
            }
            else if (args[0].equalsIgnoreCase("hurt")) {
                player2.sendHurtAnimation(10);
            }
        }
        else {
            sender.sendMessage("player is not online");
        }

        return true;
    }
}

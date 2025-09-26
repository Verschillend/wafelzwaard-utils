package me.verschillend.wafelzwaardutils.commands;


import me.verschillend.wafelzwaardutils.Wafelzwaardutils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommand implements CommandExecutor {

    private final Wafelzwaardutils plugin;

    public SpawnCommand(Wafelzwaardutils plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("wafelzwaard.spawn")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        boolean lobby = plugin.getConfig().getBoolean("server.lobby", false);
        boolean oneblock = plugin.getConfig().getBoolean("server.oneblock", false);
        if (!lobby && !oneblock) {
            sender.sendMessage("§cThis command is not set up on this server! Please contact a administrator if you believe this is a error!");
            return true;
        }

        if (args.length > 0) {
            // Teleport another player
            if (!sender.hasPermission("wafelzwaard.spawn.others")) {
                sender.sendMessage("§cYou don't have permission to teleport other players!");
                return true;
            }

            Player targetPlayer = Bukkit.getPlayer(args[0]);
            if (targetPlayer == null) {
                sender.sendMessage("§cPlayer not found!");
                return true;
            }

            teleportToSpawn(targetPlayer);
            sender.sendMessage("§aTeleported " + targetPlayer.getName() + " to spawn!");
            targetPlayer.sendMessage("§aTeleported to spawn!");
        } else {
            // Teleport sender
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cUsage: /spawn [<player>]");
                return true;
            }

            teleportToSpawn(player);
            player.sendMessage("§aTeleported to spawn!");
        }

        return true;
    }
    private void teleportToSpawn(Player player) {
        boolean lobby = plugin.getConfig().getBoolean("server.lobby", false);
        boolean oneblock = plugin.getConfig().getBoolean("server.oneblock", false);
        World world = Bukkit.getWorld("world");
        if (world == null) {
            player.sendMessage("§cWorld 'world' not found!");
            return;
        }
        Location Loc = new Location(world, -212.5, 74.0, 147.5, 180, 0);
        if (oneblock) {
            Loc = new Location(world, -222.5, 161, -32.5, 0, 0);
        }
        else if (lobby) {
            Loc = new Location(world, -212.5, 74.0, 147.5, 180, 0);
        }
        player.teleport(Loc);
    }
}

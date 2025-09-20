package me.verschillend.wafelzwaardutils.commands;

import me.verschillend.wafelzwaardutils.Wafelzwaardutils;
import me.verschillend.wafelzwaardutils.gui.CCGUI;
import me.verschillend.wafelzwaardutils.gui.BwGUI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor {

    private Wafelzwaardutils plugin;
    private final Boolean lobby = plugin.getConfig().getBoolean("server.lobby", false);
    private CCGUI CCGUI;
    private BwGUI BwGUI;

    public Commands(CCGUI CCgui) {
        this.CCGUI = CCGUI;
    }

    public Commands(BwGUI Bwgui) {
        this.BwGUI = BwGUI;
    }

    public Commands() {
        //empty
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("chatcolor")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cThis command can only be used by players!");
                return true;
            }

            CCGUI.openGUI(player);
        }
        else if (command.getName().equalsIgnoreCase("bw")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cThis command can only be used by players!");
                return true;
            }
            else if (!lobby) {
                sender.sendMessage("§cThis command is not available on this server! Please contact a administrator if you believe this is a error!");
                return true;
            }

            BwGUI.openGUI(player);
        }
        else if (command.getName().equalsIgnoreCase("spawn")) {
            if (!(sender instanceof Player player) && args.length != 0) {
                sender.sendMessage("§cUsage: /spawn [<player>]");
                return true;
            }

            if (args.length > 0) {
                if (lobby) {
                    World world = Bukkit.getWorld("world");
                    Location loc = new Location(world, -212.5, 74.0, 147.5, 180, 0);
                    Player player = Bukkit.getPlayer(args[0]);
                    player.teleport(loc);
                    return true;
                }
                else {
                    sender.sendMessage("§cThis command is not set up on this server! Please contact a administrator if you believe this is a error!");
                    return true;
                }
            }
            else {
                if (lobby) {
                    World world = Bukkit.getWorld("world");
                    Location loc = new Location(world, -212.5, 74.0, 147.5, 180, 0);
                    Player player = (Player) sender;
                    player.teleport(loc);
                    return true;
                }
                else {
                    sender.sendMessage("§cThis command is not set up on this server! Please contact a administrator if you believe this is a error!");
                    return true;
                }
            }
        }
        return true;
    }
}

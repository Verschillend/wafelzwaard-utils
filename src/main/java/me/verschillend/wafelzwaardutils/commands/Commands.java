package me.verschillend.wafelzwaardutils.commands;

import me.verschillend.wafelzwaardutils.gui.CCGUI;
import me.verschillend.wafelzwaardutils.gui.BwGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor {

    private CCGUI CCGUI;
    private BwGUI BwGUI;

    public Commands(CCGUI CCgui) {
        this.CCGUI = CCGUI;
    }

    public Commands(BwGUI Bwgui) {
        this.BwGUI = BwGUI;
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

            BwGUI.openGUI(player);
        }
        return true;
    }
}

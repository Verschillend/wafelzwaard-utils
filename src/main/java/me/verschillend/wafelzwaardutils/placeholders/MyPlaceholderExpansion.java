package me.verschillend.wafelzwaardutils.placeholders;

import me.verschillend.wafelzwaardutils.Wafelzwaardutils;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class MyPlaceholderExpansion extends PlaceholderExpansion {

    private final Wafelzwaardutils plugin;

    public MyPlaceholderExpansion(Wafelzwaardutils plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "wafelzwaard";
    }

    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true; // This is required or else PlaceholderAPI will unregister the Expansion on reload
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        if (player == null) {
            return "";
        }

        // %wafelzwaard_cc%
        if (params.equals("cc")) {
            try {
                return String.valueOf(plugin.getDatabaseManager().getPlayerColor(player.getUniqueId()).get());
            } catch (Exception e) {
                return "7";
            }
        }
        /*
        // %myplugin_rank%
        if (params.equals("rank")) {
            // You could implement a ranking system here
            return "Newbie";
        }
        */

        return null;
    }
}

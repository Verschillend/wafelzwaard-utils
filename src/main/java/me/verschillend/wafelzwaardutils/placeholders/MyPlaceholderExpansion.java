package me.verschillend.wafelzwaardutils.placeholders;

import me.clip.placeholderapi.PlaceholderAPI;
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
        return true; //This is required or else PlaceholderAPI will unregister the Expansion on reload
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
        if (params.equals("bw")) {
            try {
                String bedwars = PlaceholderAPI.setPlaceholders(player, "%bungee_bedwars-1%");
                String fbf = PlaceholderAPI.setPlaceholders(player, "%bungee_fireball-fight-1%");
                String practice = PlaceholderAPI.setPlaceholders(player, "%bungee_practice%");
                int result = (Integer.parseInt(bedwars) + Integer.parseInt(fbf) + Integer.parseInt(practice));
                return Integer.toString(result);
            } catch (Exception e) {
                return "0";
            }
        }

        return null;
    }
}

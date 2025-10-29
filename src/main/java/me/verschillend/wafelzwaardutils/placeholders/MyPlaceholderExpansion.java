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

        // %wafelzwaard_gems%
        if (params.equals("gems")) {
            try {
                return String.format("%.2f", plugin.getDatabaseManager().getPlayerGems(player.getUniqueId()).get());
            } catch (Exception e) {
                return "0.00";
            }
        }

        // %wafelzwaard_playercount_lobby%, %wafelzwaard_playercount_oneblock%, etc.
        if (params.startsWith("playercount_")) {
            String serverName = params.substring("playercount_".length());
            try {
                int count = plugin.getDatabaseManager().getServerPlayerCount(serverName).get();
                return String.valueOf(count);
            } catch (Exception e) {
                return "0";
            }
        }

        // %wafelzwaard_gems_formatted%
        if (params.equals("gems_formatted")) {
            try {
                double gems = plugin.getDatabaseManager().getPlayerGems(player.getUniqueId()).get();
                return formatNumber(gems);
            } catch (Exception e) {
                return "0";
            }
        }

        // %wafelzwaard_bw%
        if (params.equals("bw")) {
            try {
                String bedwars = PlaceholderAPI.setPlaceholders(player, "%bungee_bedwars-1%");
                String fbf = PlaceholderAPI.setPlaceholders(player, "%bungee_fireball-fight-1%");
                String practice = PlaceholderAPI.setPlaceholders(player, "%bungee_practice%");

                int total = safeInt(bedwars) + safeInt(fbf) + safeInt(practice);
                return String.valueOf(total);
            } catch (Exception e) {
                return "0";
            }
        }

        // %wafelzwaard_players%
        if (params.equalsIgnoreCase("players")) {
            try {
                int total = plugin.getDatabaseManager().getTotalPlayerCount().get();
                return String.valueOf(total);
            } catch (Exception e) {
                return "0";
            }
        }

        return null;
    }

    private int safeInt(String value) {
        if (value == null || value.trim().isEmpty() || value.startsWith("%")) {
            return 0;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String formatNumber(double number) {
        if (number >= 1000000) {
            return String.format("%.1fM", number / 1000000.0);
        } else if (number >= 1000) {
            return String.format("%.1fK", number / 1000.0);
        }
        return String.format("%.2f", number);
    }
}

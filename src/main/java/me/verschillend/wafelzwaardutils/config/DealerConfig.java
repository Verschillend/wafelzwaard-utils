package me.verschillend.wafelzwaardutils.config;

import me.verschillend.wafelzwaardutils.Wafelzwaardutils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class DealerConfig {

    private final Wafelzwaardutils plugin;
    private File configFile;
    private FileConfiguration config;

    public DealerConfig(Wafelzwaardutils plugin) {
        this.plugin = plugin;
        setupConfig();
    }

    private void setupConfig() {
        configFile = new File(plugin.getDataFolder(), "dealer.yml");

        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create dealer.yml!");
                e.printStackTrace();
            }
        }

        config = YamlConfiguration.loadConfiguration(configFile);

        // Set default dealer gems if not present
        if (!config.contains("dealer-gems")) {
            config.set("dealer-gems", 10000.0);
            saveConfig();
        }
    }

    public double getDealerGems() {
        return config.getDouble("dealer-gems", 10000.0);
    }

    public void setDealerGems(double amount) {
        config.set("dealer-gems", amount);
        saveConfig();
    }

    public void addDealerGems(double amount) {
        double current = getDealerGems();
        setDealerGems(current + amount);
    }

    public void removeDealerGems(double amount) {
        double current = getDealerGems();
        setDealerGems(current - amount);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save dealer.yml!");
            e.printStackTrace();
        }
    }
}
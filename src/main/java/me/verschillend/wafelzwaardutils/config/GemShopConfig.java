package me.verschillend.wafelzwaardutils.config;

import me.verschillend.wafelzwaardutils.Wafelzwaardutils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class GemShopConfig {

    private final Wafelzwaardutils plugin;
    private File configFile;
    private FileConfiguration config;

    public GemShopConfig(Wafelzwaardutils plugin) {
        this.plugin = plugin;
        setupConfig();
    }

    private void setupConfig() {
        configFile = new File(plugin.getDataFolder(), "gemshop.yml");

        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            plugin.saveResource("gemshop.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(configFile);

        // Load defaults
        InputStream defaultStream = plugin.getResource("gemshop.yml");
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            config.setDefaults(defaultConfig);
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);

        InputStream defaultStream = plugin.getResource("gemshop.yml");
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            config.setDefaults(defaultConfig);
        }
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save gemshop.yml!");
            e.printStackTrace();
        }
    }
}

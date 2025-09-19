package me.verschillend.wafelzwaardutils;

import org.bukkit.plugin.java.JavaPlugin;
import me.verschillend.wafelzwaardutils.commands.Commands;
import me.verschillend.wafelzwaardutils.database.DatabaseManager;
import me.verschillend.wafelzwaardutils.gui.BwGUI;
import me.verschillend.wafelzwaardutils.gui.CCGUI;
import me.verschillend.wafelzwaardutils.placeholders.MyPlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Wafelzwaardutils extends JavaPlugin {

    private DatabaseManager databaseManager;
    private BwGUI BwGUI;
    private CCGUI CCGUI;

    @Override
    public void onEnable() {
        // Save default config
        saveDefaultConfig();

        // Initialize database
        databaseManager = new DatabaseManager(this);
        databaseManager.initialize();

        // Initialize GUI
        BwGUI = new BwGUI(this);
        CCGUI = new CCGUI(this);

        // Register commands
        getCommand("bw").setExecutor(new Commands(BwGUI));
        getCommand("chatcolor").setExecutor(new Commands(CCGUI));

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new MyPlaceholderExpansion(this).register();
            getLogger().info("PlaceholderAPI integration enabled!");
        }

        getLogger().info("MyPlugin has been enabled!");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
        getLogger().info("MyPlugin has been disabled!");
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public CCGUI getCCGUI() {
        return CCGUI;
    }
    public BwGUI getBwGUI() {
        return BwGUI;
    }
}

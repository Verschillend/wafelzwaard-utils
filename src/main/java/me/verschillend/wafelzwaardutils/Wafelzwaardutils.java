package me.verschillend.wafelzwaardutils;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import me.verschillend.wafelzwaardutils.commands.Commands;
import me.verschillend.wafelzwaardutils.database.DatabaseManager;
import me.verschillend.wafelzwaardutils.gui.BwGUI;
import me.verschillend.wafelzwaardutils.gui.CCGUI;
import me.verschillend.wafelzwaardutils.placeholders.MyPlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.player.PlayerJoinEvent;

public class Wafelzwaardutils extends JavaPlugin {

    private DatabaseManager databaseManager;
    private BwGUI BwGUI;
    private CCGUI CCGUI;
    private final Boolean join = this.getConfig().getBoolean("server.lobby", false);

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
        getCommand("spawn").setExecutor(new Commands());

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new MyPlaceholderExpansion(this).register();
            getLogger().info("PlaceholderAPI integration enabled!");
        }

        getLogger().info("WafelzwaardUtils has been enabled!");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
        getLogger().info("WafelzwaardUtils has been disabled!");
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

    public void onPlayerJoin(PlayerJoinEvent event) {
        //Boolean join = this.getConfig().getBoolean("server.lobby", false);
        if (join) {
            World world = Bukkit.getWorld("world");
            Location loc = new Location(world, -212.5, 74.0, 147.5, 180, 0);
            event.getPlayer().teleport(loc);
        }
    }
}

package me.verschillend.wafelzwaardutils;

import me.verschillend.wafelzwaardutils.commands.BwCommand;
import me.verschillend.wafelzwaardutils.commands.ChatColorCommand;
import me.verschillend.wafelzwaardutils.commands.SpawnCommand;
import me.verschillend.wafelzwaardutils.database.DatabaseManager;
import me.verschillend.wafelzwaardutils.gui.BwGUI;
import me.verschillend.wafelzwaardutils.gui.CCGUI;
import me.verschillend.wafelzwaardutils.placeholders.MyPlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import java.lang.reflect.Constructor;


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
        registerCommands();

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

    private void registerCommands() {
        // Register BW command
        PluginCommand bwCommand = createCommand("bw");
        bwCommand.setExecutor(new BwCommand(this, BwGUI));
        bwCommand.setDescription("Open the BW GUI");
        bwCommand.setPermission("wafelzwaard.bw");
        getServer().getCommandMap().register("wafelzwaardutils", bwCommand);

        // Register ChatColor command
        PluginCommand chatcolorCommand = createCommand("chatcolor");
        chatcolorCommand.setExecutor(new ChatColorCommand(CCGUI));
        chatcolorCommand.setDescription("Open the chat color GUI");
        chatcolorCommand.setPermission("wafelzwaard.cc");
        getServer().getCommandMap().register("wafelzwaardutils", chatcolorCommand);

        // Register Spawn command
        PluginCommand spawnCommand = createCommand("spawn");
        spawnCommand.setExecutor(new SpawnCommand(this));
        spawnCommand.setDescription("Teleport to spawn");
        spawnCommand.setPermission("wafelzwaard.spawn");
        spawnCommand.setUsage("/spawn [<player>]");
        getServer().getCommandMap().register("wafelzwaardutils", spawnCommand);

        getLogger().info("Commands registered successfully!");
    }

    private PluginCommand createCommand(String name) {
        try {
            Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            c.setAccessible(true);
            return c.newInstance(name, this);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create command: " + name, e);
        }
    }
}

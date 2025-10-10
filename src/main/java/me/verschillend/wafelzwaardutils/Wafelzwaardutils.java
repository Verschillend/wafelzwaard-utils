package me.verschillend.wafelzwaardutils;

import me.verschillend.wafelzwaardutils.commands.*;
import me.verschillend.wafelzwaardutils.database.DatabaseManager;
import me.verschillend.wafelzwaardutils.gui.BwGUI;
import me.verschillend.wafelzwaardutils.gui.CCGUI;
import me.verschillend.wafelzwaardutils.gui.SuffixGUI;
import me.verschillend.wafelzwaardutils.placeholders.MyPlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import java.lang.reflect.Constructor;
import java.util.Arrays;

import com.jellypudding.simpleVote.events.VoteEvent;


public class Wafelzwaardutils extends JavaPlugin {

    private DatabaseManager databaseManager;
    private BwGUI BwGUI;
    private CCGUI CCGUI;
    private SuffixGUI suffixGUI;
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
        suffixGUI = new SuffixGUI(this);

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
    public SuffixGUI getSuffixGUI() {
        return suffixGUI;
    }

    public void onPlayerJoin(PlayerJoinEvent event) {
        //Boolean join = this.getConfig().getBoolean("server.lobby", false);
        if (join) {
            World world = Bukkit.getWorld("world");
            Location loc = new Location(world, -212.5, 74.0, 147.5, 180, 0);
            event.getPlayer().teleport(loc);
        }
        this.getDatabaseManager().getPlayerColor(event.getPlayer().getUniqueId()).thenAccept(color -> {
            if (color == 0) {
                this.getDatabaseManager().savePlayerData(
                        event.getPlayer().getUniqueId(),
                        event.getPlayer().getName(),
                        '7'
                );
            }
        });
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
        chatcolorCommand.setAliases(Arrays.asList("chatcolors", "cc"));
        getServer().getCommandMap().register("wafelzwaardutils", chatcolorCommand);

        // Register Spawn command
        PluginCommand spawnCommand = createCommand("spawn");
        spawnCommand.setExecutor(new SpawnCommand(this));
        spawnCommand.setDescription("Teleport to spawn");
        spawnCommand.setPermission("wafelzwaard.spawn");
        spawnCommand.setUsage("/spawn [<player>]");
        getServer().getCommandMap().register("wafelzwaardutils", spawnCommand);

        //suffix gui
        PluginCommand suffixCommand = createCommand("suffix");
        suffixCommand.setExecutor(new SuffixCommand(suffixGUI));
        suffixCommand.setDescription("Open the suffix GUI");
        suffixCommand.setPermission("wafelzwaardutils.suffix");
        suffixCommand.setUsage("/suffix");
        suffixCommand.setAliases(Arrays.asList("suffixes"));
        getServer().getCommandMap().register("wafelzwaardutils", suffixCommand);

        //reload command
        PluginCommand reloadCommand = createCommand("wafelzwaardreload");
        reloadCommand.setExecutor(new ReloadCommand(this));
        reloadCommand.setDescription("Reload the plugin configuration");
        reloadCommand.setPermission("wafelzwaardutils.reload");
        reloadCommand.setUsage("/wafelzwaardreload");
        reloadCommand.setAliases(Arrays.asList("wzreload", "wafelsreload"));
        getServer().getCommandMap().register("wafelzwaardutils", reloadCommand);

        //island invite:
        PluginCommand inviteCommand = createCommand("invite");
        inviteCommand.setExecutor(new InviteCommand(this));
        inviteCommand.setDescription("Invite a player to your oneblock island");
        inviteCommand.setPermission("wafelzwaardutils.oneblock.invite");
        inviteCommand.setUsage("/invite");
        getServer().getCommandMap().register("wafelzwaardutils", inviteCommand);

        //island kick:
        PluginCommand kickCommand = createCommand("ikick");
        kickCommand.setExecutor(new KickCommand(this));
        kickCommand.setDescription("Kick a player from your oneblock island");
        kickCommand.setPermission("wafelzwaardutils.oneblock.kick");
        kickCommand.setUsage("/ikick");
        getServer().getCommandMap().register("wafelzwaardutils", kickCommand);

        //discord:
        PluginCommand discCommand = createCommand("discord");
        discCommand.setExecutor(new DiscordCommand(this));
        discCommand.setDescription("Join the discord");
        discCommand.setPermission("wafelzwaardutils.discord");
        discCommand.setUsage("/discord");
        discCommand.setAliases(Arrays.asList("dc"));
        getServer().getCommandMap().register("wafelzwaardutils", discCommand);

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

    @EventHandler
    public void onVote(VoteEvent event) {
        boolean oneblock = this.getConfig().getBoolean("server.oneblock", false);
        if (oneblock) {
            String playerName = event.getPlayerName();

            Bukkit.dispatchCommand(
                    Bukkit.getConsoleSender(),
                    "crate giveKey vote " + playerName + " 1"
            );

            getLogger().info("Vote from " + playerName + " on " + event.getServiceName());

            Bukkit.broadcastMessage("§aPlayer: §c" + playerName + "§2voted for the server!");
        }
    }
}

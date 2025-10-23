package me.verschillend.wafelzwaardutils;

import com.jellypudding.simpleVote.SimpleVote;
import me.verschillend.wafelzwaardutils.commands.*;
import me.verschillend.wafelzwaardutils.config.GemShopConfig;
import me.verschillend.wafelzwaardutils.database.DatabaseManager;
import me.verschillend.wafelzwaardutils.gui.*;
import me.verschillend.wafelzwaardutils.placeholders.MyPlaceholderExpansion;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

import com.jellypudding.simpleVote.TokenManager;


public class Wafelzwaardutils extends JavaPlugin {

    private DatabaseManager databaseManager;
    private BwGUI BwGUI;
    private CCGUI CCGUI;
    private SuffixGUI suffixGUI;
    private final Boolean join = this.getConfig().getBoolean("server.lobby", false);
    private ReferralGUI referralGUI;
    private GemShopConfig gemShopConfig;
    private GemShopGUI gemShopGUI;

    public ReferralGUI getReferralGUI() {
        return referralGUI;
    }

    @Override
    public void onEnable() {
        // Save default config
        saveDefaultConfig();

        // Initialize database
        databaseManager = new DatabaseManager(this);
        databaseManager.initialize();
        getLogger().info("Database initialized!");

        //initialize gem shop config
        gemShopConfig = new GemShopConfig(this);
        getLogger().info("GemShop config initialized!");

        // Initialize GUI
        BwGUI = new BwGUI(this);
        CCGUI = new CCGUI(this);
        suffixGUI = new SuffixGUI(this);
        referralGUI = new ReferralGUI(this);
        gemShopGUI = new GemShopGUI(this, gemShopConfig);
        getLogger().info("Guis initialized!");

        // Register commands
        registerCommands();
        getLogger().info("Commands registered!");

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new MyPlaceholderExpansion(this).register();
            getLogger().info("PlaceholderAPI integration enabled!");
        }
        if (Bukkit.getPluginManager().getPlugin("SimpleVote") != null) {
            getLogger().info("SimpleVote integration enabled!");
        }
        boolean oneblock = this.getConfig().getBoolean("server.oneblock", false);
        if (oneblock) {
            SimpleVote sv = (SimpleVote) Bukkit.getPluginManager().getPlugin("SimpleVote");
            Bukkit.getScheduler().runTaskTimer(this, () -> {
                TokenManager tm = sv.getTokenManager();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    int tokens = tm.getTokens(p.getUniqueId());
                    if (tokens >= 1) {
                        tm.removeTokens(p.getUniqueId(), tokens);
                        for (int i = 0; i < tokens; i++) {
                            Bukkit.broadcastMessage("§aPlayer: §e" + p.getName() + " §avoted for the server!");
                        }
                        String tokens2 = String.valueOf((int) tokens);
                        Bukkit.dispatchCommand(
                                Bukkit.getConsoleSender(),
                                "crate giveKey vote " + p.getName() + " " + tokens2
                        );
                        p.sendMessage("§aYour " + tokens + " §avote tokens were automatically converted to vote crate keys!");
                    }
                }
            }, 0L, 150L);
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
    public GemShopConfig getGemShopConfig() { return gemShopConfig; }
    public GemShopGUI getGemShopGUI() { return gemShopGUI; }

    public void onPlayerJoin(PlayerJoinEvent event) {
        Boolean join = this.getConfig().getBoolean("server.lobby", false);
        if (join) {
            World world = Bukkit.getWorld("world");
            Location loc = new Location(world, -212.5, 74.0, 147.5, 180, 0);
            event.getPlayer().teleport(loc);
        }
        this.getDatabaseManager().getPlayerColor(event.getPlayer().getUniqueId()).thenAccept(color -> {
            if (color == 0 || color == null) { //new player
                this.getDatabaseManager().savePlayerData(
                        event.getPlayer().getUniqueId(),
                        event.getPlayer().getName(),
                        '7',
                        0.0 //default gems amount
                );

                //Generate referral code for new player
                this.getDatabaseManager().generateReferralCode(event.getPlayer().getUniqueId());
            } else {
                // heck if existing player needs a referral code
                this.getDatabaseManager().getPlayerReferralCode(event.getPlayer().getUniqueId()).thenAccept(code -> {
                    if (code == null) {
                        this.getDatabaseManager().generateReferralCode(event.getPlayer().getUniqueId());
                    }
                });
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

        //gems command
        PluginCommand gemsCommand = createCommand("gems");
        gemsCommand.setExecutor(new GemsCommand(this));
        gemsCommand.setDescription("Manage gems");
        gemsCommand.setPermission("wafelzwaardutils.gems");
        gemsCommand.setUsage("/gems [set|add|remove|check] [player] [amount]");
        gemsCommand.setAliases(Arrays.asList("gem"));
        getServer().getCommandMap().register("wafelzwaardutils", gemsCommand);

        //Referral GUI command
        PluginCommand referralCommand = createCommand("referral");
        referralCommand.setExecutor(new ReferralCommand(this, referralGUI));
        referralCommand.setDescription("Open referral GUI");
        referralCommand.setPermission("wafelzwaardutils.referral");
        referralCommand.setUsage("/referral");
        referralCommand.setAliases(Arrays.asList("referrals", "ref"));
        getServer().getCommandMap().register("wafelzwaardutils", referralCommand);

        //Refer command
        PluginCommand referCommand = createCommand("refer");
        referCommand.setExecutor(new ReferCommand(this));
        referCommand.setDescription("Use a referral code");
        referCommand.setPermission("wafelzwaardutils.refer");
        referCommand.setUsage("/refer <code>");
        getServer().getCommandMap().register("wafelzwaardutils", referCommand);

        //gemshop command
        PluginCommand gemshopCommand = createCommand("gemshop");
        gemshopCommand.setExecutor(new GemShopCommand(this, gemShopGUI));
        gemshopCommand.setDescription("Open the gem shop");
        gemshopCommand.setPermission("wafelzwaardutils.gemshop");
        gemshopCommand.setUsage("/gemshop");
        gemshopCommand.setAliases(Arrays.asList("store", "gshop"));
        getServer().getCommandMap().register("wafelzwaardutils", gemshopCommand);

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

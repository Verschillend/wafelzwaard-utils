package me.verschillend.wafelzwaardutils.gui;

import me.verschillend.wafelzwaardutils.Wafelzwaardutils;
import me.verschillend.wafelzwaardutils.config.DealerConfig;
import me.verschillend.wafelzwaardutils.game.BlackjackGame;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class BlackjackGUI implements Listener {

    private final Wafelzwaardutils plugin;
    private final DealerConfig dealerConfig;
    private final String BET_GUI_TITLE = "§6§lBlackjack - Place Bet";
    private final String GAME_GUI_TITLE = "§6§lBlackjack";
    private final String RESULT_GUI_TITLE = "§6§lBlackjack - Result";
    private final Map<UUID, BlackjackGame> activeGames = new HashMap<>();

    public BlackjackGUI(Wafelzwaardutils plugin, DealerConfig dealerConfig) {
        this.plugin = plugin;
        this.dealerConfig = dealerConfig;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void openBetGUI(Player player) {
        // check if lobby server
        if (!plugin.getConfig().getBoolean("server.lobby", false)) {
            player.sendMessage("§cBlackjack is not available on this server!");
            return;
        }

        Inventory gui = Bukkit.createInventory(null, 27, BET_GUI_TITLE);

        plugin.getDatabaseManager().getPlayerGems(player.getUniqueId()).thenAccept(playerGems -> {
            double dealerGems = dealerConfig.getDealerGems();

            Bukkit.getScheduler().runTask(plugin, () -> {
                setupBetGUI(gui, player, playerGems, dealerGems);
                player.openInventory(gui);
            });
        });
    }

    private void setupBetGUI(Inventory gui, Player player, double playerGems, double dealerGems) {
        // bet options
        double[] betAmounts = {10, 25, 50, 100, 250, 500, 1000};
        int[] slots = {10, 11, 12, 13, 14, 15, 16};

        for (int i = 0; i < betAmounts.length && i < slots.length; i++) {
            double bet = betAmounts[i];

            if (bet > playerGems) {
                // cant afford
                gui.setItem(slots[i], createItem(Material.RED_STAINED_GLASS_PANE,
                        "§c" + String.format("%.0f", bet) + " Gems",
                        "§7You need §6" + String.format("%.2f", bet) + " §7gems",
                        "§cYou only have §6" + String.format("%.2f", playerGems) + " §7gems"
                ));
            } else if (bet > dealerGems) {
                // dealer cant cover
                gui.setItem(slots[i], createItem(Material.ORANGE_STAINED_GLASS_PANE,
                        "§6" + String.format("%.0f", bet) + " Gems",
                        "§7Dealer has insufficient gems",
                        "§cDealer has §6" + String.format("%.2f", dealerGems) + " §7gems"
                ));
            } else {
                // available bet
                gui.setItem(slots[i], createItem(Material.GREEN_STAINED_GLASS_PANE,
                        "§a" + String.format("%.0f", bet) + " Gems",
                        "§7Click to bet §6" + String.format("%.2f", bet) + " §7gems"
                ));
            }
        }

        // info
        gui.setItem(4, createItem(Material.EMERALD,
                "§6§lYour Gems",
                "§7Balance: §6" + String.format("%.2f", playerGems) + " §7gems"
        ));

        gui.setItem(22, createItem(Material.DIAMOND,
                "§b§lDealer Gems",
                "§7Balance: §6" + String.format("%.2f", dealerGems) + " §7gems"
        ));

        // close
        gui.setItem(26, createItem(Material.BARRIER, "§cClose", "§7Click to close"));
    }

    public void startGame(Player player, double bet) {
        BlackjackGame game = new BlackjackGame(bet);
        activeGames.put(player.getUniqueId(), game);

        // remove bet from player
        plugin.getDatabaseManager().removePlayerGems(player.getUniqueId(), player.getName(), bet);

        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);

        // check for immediate blackjack
        if (game.getState() == BlackjackGame.GameState.PLAYER_BLACKJACK) {
            endGame(player, game);
        } else if (game.getState() == BlackjackGame.GameState.DEALER_BLACKJACK) {
            endGame(player, game);
        } else {
            openGameGUI(player);
        }
    }

    public void openGameGUI(Player player) {
        BlackjackGame game = activeGames.get(player.getUniqueId());
        if (game == null) return;

        Inventory gui = Bukkit.createInventory(null, 54, GAME_GUI_TITLE);
        setupGameGUI(gui, player, game);
        player.openInventory(gui);
    }

    private void setupGameGUI(Inventory gui, Player player, BlackjackGame game) {
        // dealers hand (top row, starting at slot 10)
        List<BlackjackGame.Card> dealerHand = game.getDealerHand();
        gui.setItem(4, createItem(Material.SKELETON_SKULL,
                "§c§lDealer's Hand",
                "§7Value: §e" + (game.getState() == BlackjackGame.GameState.PLAYING ? "?" : game.getHandValue(dealerHand))
        ));

        for (int i = 0; i < dealerHand.size(); i++) {
            BlackjackGame.Card card = dealerHand.get(i);
            // Hide dealer's second card if game is still playing
            if (i == 1 && game.getState() == BlackjackGame.GameState.PLAYING) {
                gui.setItem(11 + i, createItem(Material.PURPLE_STAINED_GLASS_PANE,
                        "§d§l?",
                        "§7Hidden card"
                ));
            } else {
                gui.setItem(11 + i, createItem(card.getMaterial(),
                        card.toString(),
                        "§7Value: §e" + card.getValue()
                ));
            }
        }

        // player's hand (middle row, starting at slot 28)
        List<BlackjackGame.Card> playerHand = game.getPlayerHand();
        int playerValue = game.getHandValue(playerHand);
        gui.setItem(22, createItem(Material.PLAYER_HEAD,
                "§a§lYour Hand",
                "§7Value: §e" + playerValue
        ));

        for (int i = 0; i < playerHand.size(); i++) {
            BlackjackGame.Card card = playerHand.get(i);
            gui.setItem(29 + i, createItem(card.getMaterial(),
                    card.toString(),
                    "§7Value: §e" + card.getValue()
            ));
        }

        // action buttons (bottom row)
        if (game.getState() == BlackjackGame.GameState.PLAYING) {
            gui.setItem(45, createItem(Material.LIME_STAINED_GLASS_PANE,
                    "§a§lHIT",
                    "§7Draw another card"
            ));

            gui.setItem(53, createItem(Material.RED_STAINED_GLASS_PANE,
                    "§c§lSTAND",
                    "§7End your turn"
            ));
        }

        // info
        gui.setItem(49, createItem(Material.GOLD_INGOT,
                "§6§lBet Amount",
                "§7Current bet: §6" + String.format("%.2f", game.getBet()) + " §7gems"
        ));
    }

    private void endGame(Player player, BlackjackGame game) {
        double payout = game.calculatePayout();
        double dealerChange = game.getBet() - payout;

        // update dealer gems
        dealerConfig.addDealerGems(dealerChange);

        // give player payout
        if (payout > 0) {
            plugin.getDatabaseManager().addPlayerGems(player.getUniqueId(), player.getName(), payout);
        }

        // play sound based on result
        switch (game.getState()) {
            case PLAYER_BLACKJACK, PLAYER_WIN, DEALER_BUST -> {
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            }
            case PUSH -> {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f);
            }
            default -> {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            }
        }

        openResultGUI(player, game, payout);
    }

    private void openResultGUI(Player player, BlackjackGame game, double payout) {
        Inventory gui = Bukkit.createInventory(null, 54, RESULT_GUI_TITLE);

        // Show final hands
        setupGameGUI(gui, player, game);

        // Result message
        String resultMessage = switch (game.getState()) {
            case PLAYER_BLACKJACK -> "§a§lBLACKJACK! YOU WIN!";
            case PLAYER_WIN -> "§a§lYOU WIN!";
            case DEALER_BUST -> "§a§lDEALER BUST! YOU WIN!";
            case PUSH -> "§e§lPUSH! TIE GAME";
            case DEALER_WIN -> "§c§lDEALER WINS";
            case PLAYER_BUST -> "§c§lBUST! YOU LOSE";
            case DEALER_BLACKJACK -> "§c§lDEALER BLACKJACK";
            default -> "§7Game Over";
        };

        double profit = payout - game.getBet();

        gui.setItem(49, createItem(Material.NETHER_STAR,
                resultMessage,
                "§7Bet: §6" + String.format("%.2f", game.getBet()) + " §7gems",
                "§7Payout: §6" + String.format("%.2f", payout) + " §7gems",
                profit > 0 ? "§a+" + String.format("%.2f", profit) + " gems" : profit < 0 ? "§c" + String.format("%.2f", profit) + " gems" : "§7No change"
        ));

        // Play again button
        gui.setItem(45, createItem(Material.EMERALD_BLOCK,
                "§a§lPlay Again",
                "§7Click to start a new game"
        ));

        // Close button
        gui.setItem(53, createItem(Material.RED_STAINED_GLASS_PANE,
                "§c§lClose",
                "§7Click to close"
        ));

        player.openInventory(gui);

        // Send message
        player.sendMessage("§8§m----------------------------------------");
        player.sendMessage(resultMessage);
        player.sendMessage("§7Bet: §6" + String.format("%.2f", game.getBet()) + " §7gems");
        player.sendMessage("§7Payout: §6" + String.format("%.2f", payout) + " §7gems");
        if (profit > 0) {
            player.sendMessage("§a§lProfit: +" + String.format("%.2f", profit) + " gems");
        } else if (profit < 0) {
            player.sendMessage("§c§lLoss: " + String.format("%.2f", profit) + " gems");
        }
        player.sendMessage("§8§m----------------------------------------");
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = event.getView().getTitle();

        if (title.equals(BET_GUI_TITLE)) {
            event.setCancelled(true);
            handleBetClick(player, event.getSlot(), event.getCurrentItem());
        } else if (title.equals(GAME_GUI_TITLE)) {
            event.setCancelled(true);
            handleGameClick(player, event.getSlot());
        } else if (title.equals(RESULT_GUI_TITLE)) {
            event.setCancelled(true);
            handleResultClick(player, event.getSlot());
        }
    }

    private void handleBetClick(Player player, int slot, ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;

        if (slot == 26) { // close
            player.closeInventory();
            return;
        }

        // check if it's a bet button
        int[] betSlots = {10, 11, 12, 13, 14, 15, 16};
        double[] betAmounts = {10, 25, 50, 100, 250, 500, 1000};

        for (int i = 0; i < betSlots.length; i++) {
            if (slot == betSlots[i] && item.getType() == Material.GREEN_STAINED_GLASS_PANE) {
                double bet = betAmounts[i];
                player.closeInventory();
                startGame(player, bet);
                return;
            }
        }
    }

    private void handleGameClick(Player player, int slot) {
        BlackjackGame game = activeGames.get(player.getUniqueId());
        if (game == null || game.getState() != BlackjackGame.GameState.PLAYING) return;

        if (slot == 45) { // hit
            game.playerHit();
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);

            // check if player busted or reached 21
            if (game.getState() == BlackjackGame.GameState.PLAYER_BUST) {
                // player busted, end game
                endGame(player, game);
            } else if (game.getHandValue(game.getPlayerHand()) == 21) {
                // player reached 21, auto-stand
                player.sendMessage("§6You reached 21! Auto-standing...");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.5f);
                game.playerStand();
                endGame(player, game);
            } else {
                // still playing, refresh GUI to show new card
                openGameGUI(player);
            }
        }
        else if (slot == 53) { // stand
            game.playerStand();
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0f, 1.0f);
            endGame(player, game);
        }
    }

    private void handleResultClick(Player player, int slot) {
        if (slot == 45) { // play again
            activeGames.remove(player.getUniqueId());
            player.closeInventory();
            openBetGUI(player);
        } else if (slot == 53) { // close
            activeGames.remove(player.getUniqueId());
            player.closeInventory();
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        String title = event.getView().getTitle();

        // clean up game if player closes during gameplay
        if (title.equals(GAME_GUI_TITLE)) {
            BlackjackGame game = activeGames.get(player.getUniqueId());
            if (game != null && game.getState() == BlackjackGame.GameState.PLAYING) {
                // auto stand if player closes GUI
                game.playerStand();
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline()) {
                        endGame(player, game);
                    } else {
                        activeGames.remove(player.getUniqueId());
                    }
                }, 1L);
            }
        }
    }
}
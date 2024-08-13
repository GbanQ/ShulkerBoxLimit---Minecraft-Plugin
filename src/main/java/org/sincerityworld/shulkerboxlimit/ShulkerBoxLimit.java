package org.sincerityworld.shulkerboxlimit;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.ShulkerBox;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.plugin.java.JavaPlugin;


import java.io.File;
import java.io.IOException;
import java.util.*;

public class ShulkerBoxLimit extends JavaPlugin implements Listener {

    private int maxShulkerBoxes;
    private FileConfiguration config;
    private File configFile;
    private Map<UUID, Integer> playerShulkerCount = new HashMap<>();


    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        loadConfig();
        if (config == null) {
            setShulkerLimit(2);
        } else {
            maxShulkerBoxes = config.getInt("max-shulker-boxes", 2);
        }
    }

    private void loadConfig() {
        configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            getDataFolder().mkdirs();
            config = getConfig();
            saveConfig();
        } else {
            config = YamlConfiguration.loadConfiguration(configFile);
        }
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            getLogger().severe("Failed to save file configuration: " + e.getMessage());
        }
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("setshulkerlimit")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (player.hasPermission("shulkerlimit.set")) {
                    if (args.length == 1) {
                        int newLimit = Integer.parseInt(args[0]);
                        if (newLimit > 0) {
                            setShulkerLimit(newLimit);
                            player.sendMessage(ChatColor.YELLOW + "Shulker box limit set to " + newLimit);
                            return true;
                        } else {
                            player.sendMessage(ChatColor.RED + "Limit must be a positive number.");
                            return false;
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "Usage: /setshulkerlimit <amount>");
                        return false;
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                    return false;
                }
            } else {
                sender.sendMessage(ChatColor.RED + "This command is only available to players.");
                return false;
            }
        }
        return false;
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        checkShulkerBoxes(player);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        checkShulkerBoxes(player);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        checkShulkerBoxes(player);
    }

    private void checkShulkerBoxes(Player player) {
        int shulkerBoxCount = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType().isBlock() && item.getItemMeta() instanceof BlockStateMeta) {
                BlockStateMeta blockMeta = (BlockStateMeta) item.getItemMeta();
                if (blockMeta.getBlockState() instanceof ShulkerBox) {
                    shulkerBoxCount++;
                }
            }
        }
        int currentPlayerShulkerCount = playerShulkerCount.getOrDefault(player.getUniqueId(), 0);
        if (shulkerBoxCount > maxShulkerBoxes) {
            int excessBoxes = shulkerBoxCount - maxShulkerBoxes;
            for (int i = 0; i < player.getInventory().getSize(); i++) {
                ItemStack item = player.getInventory().getItem(i);
                if (item != null && item.getType().isBlock() && item.getItemMeta() instanceof BlockStateMeta) {
                    BlockStateMeta blockMeta = (BlockStateMeta) item.getItemMeta();
                    if (blockMeta.getBlockState() instanceof ShulkerBox) {
                        Location dropLocation = player.getLocation().add(0, 1, 0);
                        Item droppedItem = player.getWorld().dropItem(dropLocation, item);
                        droppedItem.setPickupDelay(80);
                        player.getInventory().setItem(i, null);

                        excessBoxes--;
                        currentPlayerShulkerCount--;
                        if (excessBoxes <= 0) {
                            break;
                        }
                    }
                }
            }
            playerShulkerCount.put(player.getUniqueId(), currentPlayerShulkerCount);
        } else if (shulkerBoxCount < maxShulkerBoxes) {
            playerShulkerCount.put(player.getUniqueId(), shulkerBoxCount);
        } else {
            playerShulkerCount.put(player.getUniqueId(), shulkerBoxCount);
        }
    }

    private void setShulkerLimit(int newLimit) {
        maxShulkerBoxes = newLimit;
        if (config != null) {
            config.set("max-shulker-boxes", newLimit);
            saveConfig();
        } else {

        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (sender instanceof Player) {
            if (args.length == 1) {
                return Arrays.asList("1", "9", "18", "27", "36");
            }

        }
        return new ArrayList<>();
    }
}

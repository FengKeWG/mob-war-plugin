package org.windguest.mobwar.listener;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Witch;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.entity.EntityType;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.windguest.mobwar.Main;
import org.windguest.mobwar.games.Players;

public class ListenerOthers implements Listener {

    static Main plugin = Main.getInstance();

    @EventHandler
    public void onEndCrystalExplode(EntityExplodeEvent event) {
        if (event.getEntityType() == EntityType.END_CRYSTAL) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEndCrystalDamage(EntityDamageEvent event) {
        if (event.getEntityType() == EntityType.END_CRYSTAL) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
        if (event.getEntity().getType() == EntityType.PAINTING) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHangingBreak(HangingBreakEvent event) {
        if (event.getEntity().getType() == EntityType.PAINTING) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerExpChange(PlayerExpChangeEvent event) {
        Player player = event.getPlayer();
        Players.syncStar(player);
        event.setAmount(0);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        String prefix = Players.getPlayerPrefix(player);
        String chatFormat = prefix + " " + player.getName() + "§f: " + message;
        event.setFormat(chatFormat);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (!Players.isPlayerInHole(player)) return;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (Players.isPlayerInHole(player)) {
                Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "cc open mobsnotp " + player.getName());
            }
        }, 10L);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();
        if (item != null && (item.getType() == Material.COMPASS || item.getType() == Material.BOOK)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        ItemStack item = event.getOldCursor();
        if (item.getType() == Material.COMPASS || item.getType() == Material.BOOK) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityPotionEffect(EntityPotionEffectEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Witch && event.getAction() == EntityPotionEffectEvent.Action.ADDED
                && event.getNewEffect() != null && event.getNewEffect().getType() == PotionEffectType.INSTANT_HEALTH) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage().toLowerCase();
        if ((message.equals("/pl") || message.equals("/plugins")) && !player.isOp()) {
            event.setCancelled(true);
        }
    }
}

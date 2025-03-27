package org.windguest.mobwar.Listener;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.windguest.mobwar.Games.Bounty;
import org.windguest.mobwar.Games.Edit;

public class ListenerInteract implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (Edit.isEditing(player)) {
            event.setCancelled(true);
            return;
        }
        Action action = event.getAction();
        ItemStack item = event.getItem();
        if (item != null) {
            Material itemType = item.getType();
            if (itemType == Material.EMERALD) {
                if (action == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.3f, 1.0f);
                    Bounty.openBountyMenu(player, 0);
                    event.setCancelled(true);
                }
            }
            if ((itemType == Material.PHANTOM_MEMBRANE)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 20, 29, true, false, false));
                item.setAmount(item.getAmount() - 1);
                player.getInventory().setItemInMainHand(item);
                event.setCancelled(true);
            }
            if ((itemType == Material.BARRIER)) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.5f, 0.4f);
                event.setCancelled(true);
            }
        }
        if (action == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
            Block block = event.getClickedBlock();
            if (block != null) {
                Material blockType = block.getType();
                if ((blockType == Material.CRAFTING_TABLE || blockType == Material.FURNACE
                        || blockType == Material.BLAST_FURNACE || blockType == Material.SMOKER
                        || blockType == Material.CHEST || blockType == Material.TRAPPED_CHEST
                        || blockType == Material.BREWING_STAND || blockType == Material.ENCHANTING_TABLE
                        || blockType == Material.ANVIL || blockType == Material.CHIPPED_ANVIL
                        || blockType == Material.DAMAGED_ANVIL || blockType == Material.WATER
                        || blockType == Material.LAVA || blockType == Material.BARREL
                        || blockType == Material.SHULKER_BOX)) {
                    event.setCancelled(true);
                }
            }
        }
    }
}

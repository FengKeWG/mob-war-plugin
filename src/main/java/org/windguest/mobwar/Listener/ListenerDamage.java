package org.windguest.mobwar.Listener;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.windguest.mobwar.Games.Energy;
import org.windguest.mobwar.Games.Players;

public class ListenerDamage implements Listener {

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
                event.setCancelled(true);
            }
            if (event.getCause() == EntityDamageEvent.DamageCause.LIGHTNING) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Firework && event.getDamager().hasMetadata("noDamage")) {
            event.setCancelled(true);
        }
        if (event.getEntity() instanceof Player defender) {
            Player attacker = null;
            double damage = event.getFinalDamage();
            defender.getWorld().spawnParticle(Particle.BLOCK, defender.getLocation().add(0.0, 1.0, 0.0), 30, 0.5, 0.5, 0.5, 0.0, (Object) Material.RED_WOOL.createBlockData());
            if (event.getDamager() instanceof Player) {
                attacker = (Player) event.getDamager();
                Material attackerItem = attacker.getInventory().getItemInMainHand().getType();
                if (isAxe(attackerItem) && defender.isBlocking() && defender.isHandRaised() && event.getFinalDamage() == 0) {
                    attacker.playSound(attacker.getLocation(), Sound.ITEM_SHIELD_BREAK, 1.0f, 1.0f);
                }
            } else if (event.getDamager() instanceof Projectile projectile) {
                if (projectile.getShooter() instanceof Player) {
                    attacker = (Player) projectile.getShooter();
                }
            }
            if (defender.isBlocking() && defender.isHandRaised() && event.getFinalDamage() == 0.0) {
                defender.playSound(defender.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1.0f, 1.0f);
                if (attacker != null) {
                    attacker.playSound(attacker.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1.0f, 1.0f);
                }
            }
            if (attacker != null && Players.isTeam(attacker, defender)) {
                event.setCancelled(true);
                return;
            }
            if (attacker != null && !attacker.equals(defender)) {
                Players.addPlayerDeathKiller(defender, attacker);
                Energy.addEnergyToAllBars(attacker, damage);
            }
        }
    }

    private boolean isAxe(Material material) {
        return switch (material) {
            case WOODEN_AXE, STONE_AXE, IRON_AXE, GOLDEN_AXE, DIAMOND_AXE, NETHERITE_AXE -> true;
            default -> false;
        };
    }
}

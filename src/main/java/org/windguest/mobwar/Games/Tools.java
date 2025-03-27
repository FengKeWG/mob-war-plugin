package org.windguest.mobwar.Games;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;
import org.windguest.mobwar.Main;

import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class Tools {

    private static final Main plugin = Main.getInstance();
    private static final Random random = new Random();

    public static int getMaxValueFromMap(Map<UUID, Integer> map) {
        int maxValue = -1;
        for (int value : map.values()) {
            if (value > maxValue) {
                maxValue = value;
            }
        }
        return maxValue;
    }

    private static void spawnExplosionInSphere(Location center, int radius) {
        int particleCount = radius * 5;
        for (int i = 0; i < particleCount; ++i) {
            double phi = random.nextDouble() * Math.PI * 2.0;
            double costheta = random.nextDouble() * 2.0 - 1.0;
            double u = random.nextDouble();
            double theta = Math.acos(costheta);
            double r = (double) radius * Math.cbrt(u);
            double x = r * Math.sin(theta) * Math.cos(phi);
            double y = r * Math.sin(theta) * Math.sin(phi);
            double z = r * Math.cos(theta);
            Location particleLocation = center.clone().add(x, y, z);
            center.getWorld().spawnParticle(Particle.EXPLOSION, particleLocation, 1);
        }
    }

    public static void bomb(Location explosionCenter, int radius, Player attacker, int maxDamage, double knockBack, boolean particle, PotionEffect potion) {
        if (particle) {
            spawnExplosionInSphere(explosionCenter, radius);
        }
        Vector explosionVector = new Vector(explosionCenter.getX(), explosionCenter.getY(), explosionCenter.getZ());
        for (Entity entity : explosionCenter.getWorld().getNearbyEntities(explosionCenter, radius, radius, radius)) {
            if (!(entity instanceof LivingEntity livingEntity)) continue;
            Vector direction = livingEntity.getLocation().toVector().subtract(explosionVector);
            if (direction.length() > 0) {
                Vector knockbackVector = direction.normalize().multiply(knockBack);
                livingEntity.setVelocity(knockbackVector);
            }
            double distance = livingEntity.getLocation().distance(explosionCenter);
            double damageToApply = maxDamage * (1.0 - distance / radius);
            damageToApply = Math.max(damageToApply, 0.0);
            if (livingEntity instanceof Player victim) {
                if (Players.isTeam(attacker, victim)) continue;
                if (victim.equals(attacker)) continue;
                if (victim.isBlocking() && isFacingLocation(victim, explosionCenter)) {
                    reduceShieldDurability(victim, (int) damageToApply);
                    continue;
                }
            }
            livingEntity.damage(damageToApply, attacker);
            if (potion != null) {
                livingEntity.addPotionEffect(potion);
            }
        }
    }

    public static boolean isFacingLocation(Player player, Location center) {
        Location playerLocation = player.getLocation();
        Vector toExplosion = center.clone().subtract(playerLocation).toVector();
        double dot = toExplosion.normalize().dot(player.getLocation().getDirection());
        return dot > 0.5;
    }

    public static void reduceShieldDurability(Player player, int damage) {
        ItemStack shieldItem = null;
        if (player.getInventory().getItemInMainHand().getType() == Material.SHIELD) {
            shieldItem = player.getInventory().getItemInMainHand();
        } else if (player.getInventory().getItemInOffHand().getType() == Material.SHIELD) {
            shieldItem = player.getInventory().getItemInOffHand();
        }
        if (shieldItem != null) {
            ItemMeta meta = shieldItem.getItemMeta();
            if (meta instanceof Damageable shield) {
                shield.setDamage(shield.getDamage() + damage);
                if (shield.getDamage() > shieldItem.getType().getMaxDurability()) {
                    player.getInventory().removeItemAnySlot(shieldItem);
                } else {
                    shieldItem.setItemMeta(shield);
                }
            }
        }
    }
}

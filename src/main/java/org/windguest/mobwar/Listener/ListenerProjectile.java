package org.windguest.mobwar.Listener;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.windguest.mobwar.Main;

public class ListenerProjectile implements Listener {

    static Main plugin = Main.getInstance();

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        ProjectileSource source = projectile.getShooter();
        if (source instanceof Player shooter) {
            Entity hitEntity = event.getHitEntity();
            if (hitEntity instanceof Player victim) {
                if (projectile.getType() == EntityType.SNOWBALL
                        || projectile.getType() == EntityType.EGG
                        || projectile.getType() == EntityType.ENDER_PEARL) {
                    victim.damage(3.0, shooter);
                    victim.setVelocity(victim.getVelocity().add(projectile.getVelocity().normalize().multiply(0.1)));
                } else if (projectile.getType() == EntityType.FISHING_BOBBER) {
                    victim.damage(4.0, shooter);
                    victim.setVelocity(victim.getVelocity().add(projectile.getVelocity().normalize().multiply(0.2)));
                }
            }
        }
        if (projectile instanceof Arrow || projectile instanceof SpectralArrow) {
            projectile.remove();
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        Projectile projectile = event.getEntity();
        Location initialLocation = projectile.getLocation();
        if (initialLocation.getY() < 0.0) {
            projectile.remove();
        }
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (projectile.isValid() && initialLocation.distance(projectile.getLocation()) > 200.0) {
                projectile.remove();
            }
        }, 0L, 1L);
        if (projectile.getShooter() instanceof Player) {
            if (projectile.getType() == EntityType.ENDER_PEARL) {
                Player shooter = (Player) projectile.getShooter();
                Bukkit.getScheduler().runTaskLater(plugin, () -> shooter.setCooldown(Material.ENDER_PEARL, 0), 1L);
            }
            if (projectile.getType() == EntityType.WIND_CHARGE) {
                Player shooter = (Player) projectile.getShooter();
                Bukkit.getScheduler().runTaskLater(plugin, () -> shooter.setCooldown(Material.WIND_CHARGE, 0), 1L);
            }
        }
    }
}

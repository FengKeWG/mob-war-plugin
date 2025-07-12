package org.windguest.mobwar.listener;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.windguest.mobwar.games.Players;

import java.util.List;

public class ListenerMove implements Listener {

    private record Area(
            double x1, double y1, double z1,
            double x2, double y2, double z2,
            EffectType effectType,
            int level,
            Location targetLocation,
            float yaw
    ) {
        public enum EffectType {
            LEVITATION,
            TELEPORT
        }
    }

    private final List<Area> areas = List.of(
            new Area(-23, 30, -4, -26, 30, -7, Area.EffectType.LEVITATION, 15, null, 0.0f),
            new Area(-61, 58, -18, -64, 58, -21, Area.EffectType.LEVITATION, 31, null, 0.0f),
            new Area(-16, 64, 74, -19, 64, 71, Area.EffectType.LEVITATION, 27, null, 0.0f),
            new Area(65, 55, -6, 68, 55, -3, Area.EffectType.LEVITATION, 35, null, 0.0f),
            new Area(-9, 30, 33, -6, 30, 30, Area.EffectType.LEVITATION, 29, null, 0.0f),
            new Area(-60, 57, 34, -57, 57, 31, Area.EffectType.LEVITATION, 31, null, 0.0f),
            new Area(38, 32, -12, 35, 32, -9, Area.EffectType.LEVITATION, 29, null, 0.0f),
            new Area(-16.5, 64, -75.5, -15.5, 64, -74.5, Area.EffectType.LEVITATION, 30, null, 0.0f),
            new Area(-1, 31, -31, -4, 31, -34, Area.EffectType.LEVITATION, 29, null, 0.0f),
            new Area(-39, 31, 9, -42, 31, 12, Area.EffectType.LEVITATION, 29, null, 0.0f),

            new Area(43, 56, 48, 44, 59, 51, Area.EffectType.TELEPORT, 0,
                    new Location(Bukkit.getWorld("world"), -108, 89, 27), -90.0f),
            new Area(-20, 58, -41, -21, 60, -43, Area.EffectType.TELEPORT, 0,
                    new Location(Bukkit.getWorld("world"), 42.0, 88.0, 104.5), 0.0f),
            new Area(79, 89, -75, 76, 89, -72, Area.EffectType.TELEPORT, 0,
                    new Location(Bukkit.getWorld("world"), -41.5, 30.0, 0.5), -90.0f),
            new Area(109, 87, -18, 110, 89, -15, Area.EffectType.TELEPORT, 0,
                    new Location(Bukkit.getWorld("world"), -56, 58, 21), -90.0f)
    );

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location loc = player.getLocation();
        double y = player.getLocation().getY();
        if (y >= 240.0 && y < 241.0 && !Players.isPlayerInHole(player) && Players.getJobFromPlayer(player).equals("?")) {
            Players.addPlayerInHole(player);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, Integer.MAX_VALUE, 9, true, false, false));
            Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "cc open mobsnotp " + player.getName());
        }
        if (y < 0.0) {
            player.setHealth(0.0);
        }
        if (loc.clone().subtract(0.0, 1.0, 0.0).getBlock().getType() == Material.PURPLE_STAINED_GLASS && Players.getJobFromPlayer(player).equals("?")) {
            player.setHealth(0.0);
        }
        if (loc.clone().subtract(0.0, 1.0, 0.0).getBlock().getType() == Material.SLIME_BLOCK) {
            Vector direction = player.getEyeLocation().getDirection().normalize();
            direction.setY(0);
            direction.normalize();
            Vector velocity = direction.multiply(10.0);
            velocity.setY(2.0);
            player.setVelocity(velocity);
        }
        for (Area area : areas) {
            if (isInArea(loc, area.x1(), area.y1(), area.z1(), area.x2(), area.y2(), area.z2())) {
                switch (area.effectType()) {
                    case LEVITATION -> {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 20, area.level() - 1, false, false, false));
                    }
                    case TELEPORT -> {
                        Location target = new Location(
                                area.targetLocation().getWorld(),
                                area.targetLocation().getX(),
                                area.targetLocation().getY(),
                                area.targetLocation().getZ(),
                                area.yaw(),
                                0
                        );
                        player.teleport(target);
                    }
                }
            }
        }
    }

    private boolean isInArea(Location loc, double x1, double y1, double z1, double x2, double y2, double z2) {
        return loc.getX() >= Math.min(x1, x2) && loc.getX() <= Math.max(x1, x2)
                && loc.getY() >= Math.min(y1, y2) && loc.getY() <= Math.max(y1, y2)
                && loc.getZ() >= Math.min(z1, z2) && loc.getZ() <= Math.max(z1, z2);
    }
}

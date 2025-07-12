package org.windguest.mobwar.events;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.windguest.mobwar.Main;

import java.util.*;

public class End {

    private static final Map<UUID, Integer> endEventMaxHealth = new HashMap<>();
    private static final int eventDuration = 300;
    static Main plugin = Main.getInstance();

    public static void startEvent() {
        String eventName = "末地";
        Bukkit.broadcastMessage("§b[🎮] " + eventName + "事件开始！击杀敌人，获得生命上限！");
        EventsMain.playDragonRoar();
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendTitle("§c" + eventName + "事件开始！", "§a击杀敌人，获得生命上限！", 10, 70, 20);
        }
        EventsMain.setEvent("末地", eventDuration);
        new BukkitRunnable() {
            public void run() {
                if (!EventsMain.isEventActive()) {
                    cancel();
                    return;
                }
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!isInEndArea(player.getLocation())) {
                        teleportToEnd(player);
                        player.sendTitle("", "§c请不要离开末地！", 10, 70, 20);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public static void endEvent() {
        EventsMain.sendLeaderboard(endEventMaxHealth, "末地", "❤");
        endEventMaxHealth.clear();
    }

    public static void teleportToEnd(Player player) {
        Random random = new Random();
        double radius = 10.0;
        double angle = random.nextDouble() * 2.0 * Math.PI;
        double x = 0.5 + radius * Math.cos(angle);
        double z = 0.5 + radius * Math.sin(angle);
        int y = 35 + random.nextInt(6);
        player.teleport(new Location(player.getWorld(), x, y, z));
    }

    private static boolean isInEndArea(Location location) {
        double x = location.getX();
        double z = location.getZ();
        double centerX = 0.5;
        double centerZ = 0.5;
        double radius = 45.0;
        double distanceSquared = Math.pow(x - centerX, 2.0) + Math.pow(z - centerZ, 2.0);
        return distanceSquared <= Math.pow(radius, 2.0);
    }

    public static void addMaxHealth(Player player, double amount) {
        double newMaxHealth = player.getMaxHealth() + amount;
        player.setMaxHealth(newMaxHealth);
        endEventMaxHealth.put(player.getUniqueId(), (int) newMaxHealth);
    }
}

package org.windguest.mobwar.events;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.windguest.mobwar.games.Tools;
import org.windguest.mobwar.Main;
import org.windguest.mobwar.games.Players;

import java.util.*;

public class Spawner {

    private static final Random random = new Random();
    public static final Map<UUID, Integer> spawnerEventKills = new HashMap<>();
    private static final Set<UUID> spawnedMonsters = new HashSet<>();
    private static final int eventDuration = 300;
    static Main plugin = Main.getInstance();

    public static void startEvent() {
        Bukkit.broadcastMessage("§b[🎮] 刷怪笼事件开始！击杀怪物，获得奖励！");
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendTitle("§c刷怪笼事件开始！", "§a击杀怪物，获得奖励！", 10, 70, 20);
        }
        EventsMain.playDragonRoar();
        EventsMain.setEvent("刷怪笼", eventDuration);
        World world = Bukkit.getWorld("world");
        new BukkitRunnable() {

            public void run() {
                if (!EventsMain.isEventActive()) {
                    cancel();
                    return;
                }
                checkAndSpawnZombies(new Location(world, -88.5, 88.0, -0.5));
                checkAndSpawnZombies(new Location(world, -2.5, 89.0, 81.5));
                checkAndSpawnPillagers(new Location(world, 63.5, 87.0, -65.5));
                checkAndSpawnVindicators(new Location(world, 90.5, 87.0, -16.5));
                checkAndSpawnVindicators(new Location(world, -93.5, 87.0, 53.5));
                checkAndSpawnHoglins(new Location(world, -51.5, 61.0, -8.5));
                checkAndSpawnWitches(new Location(world, 7.5, 87.0, -76.5));
                checkAndSpawnBrutes(new Location(world, 55.5, 55.0, -41.5));
            }
        }.runTaskTimer(plugin, 0L, 200L);
    }

    public static void endEvent() {
        clearSpawnedMonsters();
        EventsMain.sendLeaderboard(spawnerEventKills,"刷怪笼","👾");
        spawnerEventKills.clear();
    }

    private static void checkAndSpawnZombies(Location location) {
        long cnt = spawnedMonsters.stream()
                .map(Bukkit::getEntity)
                .filter(entity -> entity instanceof Zombie)
                .count();
        if (cnt < 20) {
            Zombie zombie = (Zombie) location.getWorld().spawnEntity(location, EntityType.ZOMBIE);
            zombie.setHealth(10.0);
            int dropAmount = 2 + random.nextInt(4);
            zombie.setCustomName("§a+" + dropAmount + "💎");
            zombie.setCustomNameVisible(true);
            zombie.setRemoveWhenFarAway(false);
            zombie.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, Integer.MAX_VALUE, 1));
            zombie.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 1));
            zombie.getEquipment().setHelmet(new ItemStack(Material.LEATHER_HELMET));
            spawnedMonsters.add(zombie.getUniqueId());
        }
    }

    private static void checkAndSpawnBrutes(Location location) {
        long cnt = spawnedMonsters.stream()
                .map(Bukkit::getEntity)
                .filter(entity -> entity instanceof PiglinBrute)
                .count();
        if (cnt < 20) {
            PiglinBrute brute = (PiglinBrute) location.getWorld().spawnEntity(location, EntityType.PIGLIN_BRUTE);
            brute.setHealth(10.0);
            int dropAmount = 5 + random.nextInt(11);
            brute.setImmuneToZombification(true);
            brute.setCustomName("§e+" + dropAmount + "💰");
            brute.setCustomNameVisible(true);
            brute.setRemoveWhenFarAway(false);
            brute.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0));
            brute.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, Integer.MAX_VALUE, 1));
            spawnedMonsters.add(brute.getUniqueId());
        }
    }

    private static void checkAndSpawnVindicators(Location location) {
        long cnt = spawnedMonsters.stream()
                .map(Bukkit::getEntity)
                .filter(entity -> entity instanceof Vindicator)
                .count();
        if (cnt < 20) {
            Vindicator vindicator = (Vindicator) location.getWorld().spawnEntity(location, EntityType.VINDICATOR);
            vindicator.setHealth(10.0);
            int points = 5 + random.nextInt(16);
            vindicator.setCustomName("§b+" + points + "⚡");
            vindicator.setCustomNameVisible(true);
            vindicator.setRemoveWhenFarAway(false);
            vindicator.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0));
            vindicator.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, Integer.MAX_VALUE, 1));
            spawnedMonsters.add(vindicator.getUniqueId());
        }
    }

    private static void checkAndSpawnHoglins(Location location) {
        long cnt = spawnedMonsters.stream()
                .map(Bukkit::getEntity)
                .filter(entity -> entity instanceof Hoglin)
                .count();
        if (cnt < 20) {
            Hoglin hoglin = (Hoglin) location.getWorld().spawnEntity(location, EntityType.HOGLIN);
            hoglin.setHealth(10.0);
            int dropAmount = 5 + random.nextInt(11);
            hoglin.setCustomName("§e+" + dropAmount + "💰");
            hoglin.setImmuneToZombification(true);
            hoglin.setCustomNameVisible(true);
            hoglin.setRemoveWhenFarAway(false);
            hoglin.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0));
            spawnedMonsters.add(hoglin.getUniqueId());
        }
    }

    private static void checkAndSpawnWitches(Location location) {
        long cnt = spawnedMonsters.stream()
                .map(Bukkit::getEntity)
                .filter(entity -> entity instanceof Witch)
                .count();
        if (cnt < 20) {
            Witch witch = (Witch) location.getWorld().spawnEntity(location, EntityType.WITCH);
            witch.setHealth(10.0);
            int duration = 5 + random.nextInt(16);
            witch.setCustomName("§d+" + duration + "⚗");
            witch.setCustomNameVisible(true);
            witch.setRemoveWhenFarAway(false);
            witch.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0));
            spawnedMonsters.add(witch.getUniqueId());
        }
    }

    private static void checkAndSpawnPillagers(Location location) {
        long cnt = spawnedMonsters.stream()
                .map(Bukkit::getEntity)
                .filter(entity -> entity instanceof Pillager)
                .count();
        if (cnt < 20) {
            Pillager pillager = (Pillager) location.getWorld().spawnEntity(location, EntityType.PILLAGER);
            pillager.setHealth(10.0);
            int dropAmount = 2 + random.nextInt(4);
            pillager.setCustomName("§b+" + dropAmount + "💎");
            pillager.setCustomNameVisible(true);
            pillager.setRemoveWhenFarAway(false);
            pillager.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 1));
            spawnedMonsters.add(pillager.getUniqueId());
        }
    }

    public static void clearSpawnedMonsters() {
        for (UUID uuid : spawnedMonsters) {
            Entity entity = Bukkit.getEntity(uuid);
            if (entity != null) {
                entity.remove();
            }
        }
        spawnedMonsters.clear();
    }

    public static Player getTopKiller() {
        return Players.getTopPlayerFromMap(spawnerEventKills);
    }

    public static int getMaxKills() {
        return Tools.getMaxValueFromMap(spawnerEventKills);
    }
}

package org.windguest.mobwar.Mobs;

import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.windguest.mobwar.Games.Disguise;
import org.windguest.mobwar.Games.Energy;
import org.windguest.mobwar.Games.Items;
import org.windguest.mobwar.Main;
import org.windguest.mobwar.Games.Players;

import java.util.Arrays;
import java.util.List;

public class Warden implements Listener {

    private static final Main plugin = Main.getInstance();

    private void applyDarknessEffect(Player player) {
        Disguise.disguise(player, 0);
        for (Entity entity : player.getNearbyEntities(5, 5, 5)) {
            if (!(entity instanceof LivingEntity livingEntity)) continue;
            if (entity instanceof Player targetPlayer && Players.isTeam(player, targetPlayer)) {
                continue;
            }
            livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 5 * 20, 0));
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> Disguise.undisguise(player), 15 * 20L);
    }

    private void bigShoot(Player player) {
        Disguise.disguise(player, 0);
        BukkitTask task = new BukkitRunnable() {
            double angle = 0.0;

            public void run() {
                angle += Math.PI / 16;
                double radius = 1.5;
                for (double theta = 0.0; theta < Math.PI * 2; theta += Math.PI / 8) {
                    double x = radius * Math.cos(theta + angle);
                    double z = radius * Math.sin(theta + angle);
                    Location loc = player.getLocation().clone().add(x, 1.0, z);
                    player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, loc, 1, 0.0, 0.0, 0.0, 0.0);
                }
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WARDEN_NEARBY_CLOSE, 1.0f, 1.0f);
            }
        }.runTaskTimer(plugin, 0L, 1L);
        Players.addTaskToPlayer(player, task);
        BukkitTask bigTask = new BukkitRunnable() {

            public void run() {
                task.cancel();
                Players.setJobData(player, "warden_big");
                Vector playerDirection = player.getEyeLocation().getDirection().normalize();
                for (int i = 0; i < 50; ++i) {
                    double spread = 2.0;
                    double angle = Math.random() * 2.0 * Math.PI;
                    double offset = Math.random() * spread;
                    final Vector direction = playerDirection.clone().add(new Vector(offset * Math.cos(angle), offset * Math.sin(angle), offset * Math.sin(angle))).normalize();
                    shoot(player, 12, direction);
                }
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (Players.getJobData(player).equals("warden_big")) {
                        Players.setJobData(player, "warden");
                    }
                    Disguise.undisguise(player);
                }, 2 * 20L);
            }
        }.runTaskLater(plugin, 3 * 20);
        Players.addTaskToPlayer(player, bigTask);
    }

    private static void shoot(Player player, int damage, Vector direction) {
        Location startLocation = player.getEyeLocation().clone();
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1.0f, 1.0f);
        BukkitTask task = new BukkitRunnable() {
            final Location currentLocation = startLocation.clone();

            public void run() {
                if (startLocation.distance(currentLocation) >= 30 || currentLocation.getBlock().getType().isSolid()) {
                    cancel();
                    return;
                }
                currentLocation.add(direction);
                player.getWorld().spawnParticle(Particle.SONIC_BOOM, currentLocation, 0, direction.getX(), direction.getY(), direction.getZ(), 0.1);
                for (Entity entity : player.getWorld().getNearbyEntities(currentLocation, 1.0, 1.0, 1.0)) {
                    if (!(entity instanceof LivingEntity livingEntity)) continue;
                    if (entity.equals(player)) continue;
                    if (entity instanceof Player targetPlayer && Players.isTeam(player, targetPlayer)) {
                        continue;
                    }
                    livingEntity.damage(damage, player);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
        Players.addTaskToPlayer(player, task);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        ItemStack item = event.getItem();
        if (item != null) {
            Material itemType = item.getType();
            ItemMeta itemMeta = item.getItemMeta();
            if (itemType == Material.ECHO_SHARD && !player.hasCooldown(Material.ECHO_SHARD)) {
                if (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) {
                    if (Players.getSkill(player) == 1) {
                        player.setCooldown(Material.ECHO_SHARD, 20);
                    } else {
                        player.setCooldown(Material.ECHO_SHARD, 2 * 20);
                    }
                    Vector direction = player.getEyeLocation().getDirection().normalize();
                    shoot(player, 5, direction);
                }
            }
            if (itemType == Material.SCULK_SENSOR && itemMeta.getDisplayName().equals("§c幽匿声波") && Players.getJobFromPlayer(player).equals("监守者")) {
                event.setCancelled(true);
                bigShoot(player);
                Energy.startDecay(player, "§c幽匿声波", 3 + 1);
            } else if (itemType == Material.SCULK_SHRIEKER && itemMeta.getDisplayName().equals("§9幽匿之力") && Players.getJobFromPlayer(player).equals("监守者")) {
                event.setCancelled(true);
                applyDarknessEffect(player);
                Players.addSkill(player, 1);
                Bukkit.getScheduler().runTaskLater(plugin, () -> Players.removeSkill(player), 15 * 20L);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WARDEN_EMERGE, 1.0f, 1.0f);
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 15 * 20, 0, true, false, true));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 15 * 20, 1, true, false, true));
                Energy.startDecay(player, "§9幽匿之力", 15);
            }
        }
    }

    public static void start(Player player) {
        Players.setJobData(player, "warden");
        player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, Integer.MAX_VALUE, 0, true, false, true));
        Energy.createBar(player, Material.SCULK_SENSOR, "§c幽匿声波", BarColor.RED, 10, 10);
        Energy.createBar(player, Material.SCULK_SHRIEKER, "§9幽匿之力", BarColor.BLUE, 30, 30);
    }

    public static void equipKits(Player player) {
        Inventory inv = player.getInventory();
        inv.clear();

        player.getInventory().setHelmet(Items.createItem(
                Material.LEATHER_HELMET,
                "§9坚守者头盔",
                Arrays.asList(
                        "§6火焰保护 I",
                        "",
                        "§b🛡 §f防御 1"
                ),
                Enchantment.FIRE_PROTECTION, 1,
                Color.BLACK,
                1
        ));

        player.getInventory().setChestplate(Items.createItem(
                Material.LEATHER_CHESTPLATE,
                "§9坚守者胸甲",
                Arrays.asList(
                        "§6火焰保护 I",
                        "",
                        "§b🛡 §f防御 3"
                ),
                Enchantment.FIRE_PROTECTION, 1,
                Color.BLACK,
                1
        ));

        player.getInventory().setLeggings(Items.createItem(
                Material.DIAMOND_LEGGINGS,
                "§9坚守者护腿",
                Arrays.asList(
                        "§6火焰保护 I",
                        "",
                        "§b🛡 §f防御 6"
                ),
                Enchantment.FIRE_PROTECTION, 1,
                null,
                1
        ));

        player.getInventory().setBoots(Items.createItem(
                Material.IRON_BOOTS,
                "§9坚守者靴子",
                Arrays.asList(
                        "§6火焰保护 I",
                        "",
                        "§b🛡 §f防御 2"
                ),
                Enchantment.FIRE_PROTECTION, 1,
                null,
                1
        ));

        inv.setItem(0, Items.createItem(
                Material.IRON_SWORD,
                "§f剑",
                Arrays.asList(
                        "§7坚守者的剑",
                        "",
                        "§c❤ §f伤害 6  §6❇§f 攻速 较快"
                ),
                null,
                0,
                null,
                1
        ));

        inv.setItem(1, Items.createItem(
                Material.ECHO_SHARD,
                "§b回响声波",
                Arrays.asList(
                        "§7按§f[右键]§7发射声波",
                        "",
                        "§c❤ §f伤害 6  §6❇§f 冷却 2 秒"
                ),
                null,
                0,
                null,
                1
        ));

        inv.setItem(2, Items.createItem(
                Material.SCULK_SENSOR,
                "§c幽匿声波",
                Arrays.asList(
                        "§7蓄力后发出大量声波",
                        "",
                        "§c❤ §f伤害 12  §a❖ §f能量 10  §e ⏱ §f时长 3 秒"
                ),
                null,
                0,
                null,
                1
        ));

        inv.setItem(3, Items.createItem(
                Material.SCULK_SHRIEKER,
                "§9幽匿之力",
                Arrays.asList(
                        "§7获得抗性 I、速度 II",
                        "§7回响声波冷却变为 1 秒",
                        "§7同时使 5 格内的玩家黑暗",
                        "",
                        "§a❖ §f能量 30  §e ⏱ §f时长 15 秒"
                ),
                null,
                0,
                null,
                1
        ));

        inv.setItem(7, Items.createItem(
                Material.GOLDEN_APPLE,
                "§f金苹果",
                List.of(
                        "§7长按§f[右键]§7食用恢复生命"
                ),
                null,
                0,
                null,
                3
        ));

        inv.setItem(8, Items.createItem(
                Material.APPLE,
                "§f苹果",
                Arrays.asList(
                        "§7这是一些苹果",
                        "§7长按§f[右键]§7恢复饱食度"
                ),
                null,
                0,
                null,
                16
        ));
    }

}


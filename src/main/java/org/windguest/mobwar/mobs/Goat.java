package org.windguest.mobwar.mobs;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.windguest.mobwar.games.*;
import org.windguest.mobwar.Main;

import java.util.Arrays;
import java.util.List;

public class Goat implements Listener {

    private static final Main plugin = Main.getInstance();

    private void goatRush(Player player) {
        Players.setJobData(player, "goat_rush");
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GOAT_SCREAMING_PREPARE_RAM, 1.0f, 1.0f);
        Disguise.disguise(player, 0);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 20, 1, true, false, true));
        BukkitTask task = new BukkitRunnable() {
            int timeElapsed = 0;

            public void run() {
                if (timeElapsed >= 20 * 20) {
                    if (Players.getJobData(player).equals("goat_rush")) {
                        Players.setJobData(player, "goat");
                    }
                    Disguise.undisguise(player);
                    cancel();
                    return;
                }
                ++timeElapsed;
                for (Entity entity : player.getNearbyEntities(1.0, 1.0, 1.0)) {
                    if (!(entity instanceof LivingEntity)) continue;
                    Vector knockbackDirection = entity.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
                    if (!Double.isFinite(knockbackDirection.getX()) || !Double.isFinite(knockbackDirection.getY()) || !Double.isFinite(knockbackDirection.getZ()))
                        continue;
                    if (entity instanceof Player targetPlayer) {
                        if (Players.isTeam(player, targetPlayer)) continue;
                        if (targetPlayer.isBlocking() && Tools.isFacingLocation(targetPlayer, player.getLocation())) {
                            Tools.reduceShieldDurability(targetPlayer, 10);
                            player.setVelocity(knockbackDirection.multiply(-3));
                            continue;
                        }
                    }
                    entity.setVelocity(knockbackDirection.multiply(3));
                    ((LivingEntity) entity).damage(10, player);
                    entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_GOAT_RAM_IMPACT, 1.0f, 1.0f);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
        Players.addTaskToPlayer(player, task, Players.TaskTag.SKILL);
    }

    private void goatStrengthen(Player player) {
        Players.setJobData(player, "goat_strengthen");
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 20 * 20, 1, true, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 20 * 20, 0, true, false, true));
        for (Entity entity : player.getNearbyEntities(10, 10, 10)) {
            if (entity instanceof Player targetPlayer) {
                if (Players.isTeam(player, targetPlayer)) continue;
            }
            if (entity instanceof LivingEntity livingEntity) {
                livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20 * 20, 0, true, false, true));
                livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 20 * 20, 0, true, false, true));
                livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 20 * 20, 0, true, false, true));
            }
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (Players.getJobData(player).equals("goat_strengthen")) {
                Players.setJobData(player, "goat");
            }
        }, 20 * 20L);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item != null) {
            Material itemType = item.getType();
            ItemMeta itemMeta = item.getItemMeta();
            if (itemMeta != null) {
                if (itemType == Material.GOAT_HORN && itemMeta.getDisplayName().equals("§f鸣叫") && Players.getJobFromPlayer(player).equals("山羊")) {
                    event.setCancelled(true);
                    goatStrengthen(player);
                    Energy.startDecay(player, "§f鸣叫", 20);
                } else if (itemType == Material.FIREWORK_ROCKET && itemMeta.getDisplayName().equals("§c冲撞") && Players.getJobFromPlayer(player).equals("山羊")) {
                    event.setCancelled(true);
                    goatRush(player);
                    Energy.startDecay(player, "§c冲撞", 20);
                }
            }
        }
    }

    public static void start(Player player) {
        Players.setJobData(player, "goat");
        Energy.createBar(player, Material.GOAT_HORN, "§f鸣叫", BarColor.WHITE, 10, 10);
        Energy.createBar(player, Material.FIREWORK_ROCKET, "§c冲撞", BarColor.RED, 20, 20);
    }

    public static void equipKits(Player player) {
        Inventory inv = player.getInventory();
        inv.clear();

        player.getInventory().setHelmet(Items.createItem(
                Material.LEATHER_HELMET,
                "§a山羊头盔",
                Arrays.asList(
                        "",
                        "§b🛡 §f防御 1"
                ),
                null,
                0,
                Color.fromRGB(255, 255, 255),
                1
        ));

        player.getInventory().setChestplate(Items.createItem(
                Material.IRON_CHESTPLATE,
                "§a山羊胸甲",
                Arrays.asList(
                        "",
                        "§b🛡 §f防御 3"
                ),
                null,
                0,
                null,
                1
        ));

        player.getInventory().setLeggings(Items.createItem(
                Material.LEATHER_LEGGINGS,
                "§a山羊护腿",
                Arrays.asList(
                        "",
                        "§b🛡 §f防御 2"
                ),
                null,
                0,
                Color.fromRGB(255, 255, 255),
                1
        ));

        player.getInventory().setBoots(Items.createItem(
                Material.LEATHER_BOOTS,
                "§a山羊靴子",
                Arrays.asList(
                        "",
                        "§b🛡 §f防御 3"
                ),
                null,
                0,
                Color.fromRGB(255, 255, 255),
                1
        ));

        inv.setItem(0, Items.createItem(
                Material.STONE_SWORD,
                "§f剑",
                Arrays.asList(
                        "§7山羊的剑",
                        "",
                        "§c❤ §f伤害 5  §6❇§f 攻速 较快"
                ),
                null,
                0,
                null,
                1
        ));

        inv.setItem(1, Items.createItem(
                Material.GOAT_HORN,
                "§f鸣叫",
                Arrays.asList(
                        "§7给 10 格内的玩家",
                        "§7造成缓慢、虚弱、挖掘疲劳",
                        "§7同时自身获得力量和抗性提升",
                        "",
                        "§a❖ §f能量 10  §e ⏱ §f时长 20 秒"
                ),
                null,
                0,
                null,
                1
        ));

        inv.setItem(2, Items.createItem(
                Material.FIREWORK_ROCKET,
                "§c冲撞",
                Arrays.asList(
                        "§7通过冲撞将玩家击飞",
                        "§7并造成伤害",
                        "",
                        "§c❤ §f伤害 10  §a❖ §f能量 20  §e ⏱ §f时长 20 秒"
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


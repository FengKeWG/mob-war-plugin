package org.windguest.mobwar.mobs;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.windguest.mobwar.games.*;
import org.windguest.mobwar.Main;

public class Creeper implements Listener {

    private static final Main plugin = Main.getInstance();

    public static void creeperRain(Player player) {
        Players.setJobData(player, "creeper_rain");
        Disguise.disguise(player, 0);
        for (int i = 0; i < 40; ++i) {
            BukkitTask task = new BukkitRunnable() {
                @Override
                public void run() {
                    TNTPrimed tnt = player.getWorld().spawn(player.getLocation().add(0.0, 1.0, 0.0), TNTPrimed.class);
                    tnt.setFuseTicks(2 * 20);
                    tnt.setYield(0.0f);
                    Vector direction = randomDirection();
                    tnt.setVelocity(direction);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> Tools.bomb(tnt.getLocation(), 5, player, 15, 1.2, false, null), 2 * 20L + 2L);
                }
            }.runTaskLater(plugin, i * 5L);
            Players.addTaskToPlayer(player, task, Players.TaskTag.SKILL);
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (Players.getJobData(player).equals("creeper_rain")) {
                Players.setJobData(player, "creeper");
            }
            Disguise.undisguise(player);
        }, 40 * 5L);
    }

    public static void creeperBomb(Player player) {
        Disguise.disguise(player, 1);
        BukkitTask task = new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 5 * 20) {
                    Players.setJobData(player, "creeper_bomb");
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
                    Tools.bomb(player.getLocation(), 10, player, 40, 1.5, true, null);
                    player.damage(player.getHealth() / 2.0);
                    Players.setJobData(player, "creeper");
                    Disguise.undisguise(player);
                    cancel();
                    return;
                }
                ticks += 20;
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 1.0f, 1.0f);
                spawnRedWarning(player.getLocation());
            }
        }.runTaskTimer(plugin, 0L, 20L);
        Players.addTaskToPlayer(player, task, Players.TaskTag.SKILL);
    }

    private static void spawnRedWarning(Location center) {
        World world = center.getWorld();
        int particles = (int) (Math.PI * 4 * Math.pow(10, 2));
        for (int i = 0; i < particles; ++i) {
            double theta = Math.random() * Math.PI;
            double phi = Math.random() * 2.0 * Math.PI;
            double r = 10;
            double x = center.getX() + r * Math.sin(theta) * Math.cos(phi);
            double y = center.getY() + r * Math.sin(theta) * Math.sin(phi);
            double z = center.getZ() + r * Math.cos(theta);
            Location particleLocation = new Location(world, x, y, z);
            world.spawnParticle(Particle.DUST, particleLocation, 1, 0.0, 0.0, 0.0, 0.01, new Particle.DustOptions(Color.RED, 1.0f));
        }
    }

    public static Vector randomDirection() {
        double x = ThreadLocalRandom.current().nextDouble(-1.0, 1.0);
        double y = ThreadLocalRandom.current().nextDouble(-1.0, 1.0);
        double z = ThreadLocalRandom.current().nextDouble(-1.0, 1.0);
        return new Vector(x, y, z).normalize();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        ItemStack item = event.getItem();

        if (item != null) {
            Material itemType = item.getType();
            ItemMeta itemMeta = item.getItemMeta();

            if (itemType == Material.TNT && (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) && !player.hasCooldown(Material.TNT)) {
                event.setCancelled(true);
                TNTPrimed tnt = player.getWorld().spawn(player.getEyeLocation().add(player.getLocation().getDirection()), TNTPrimed.class);
                tnt.setVelocity(player.getLocation().getDirection().multiply(1.2));
                tnt.setFuseTicks(20);
                player.setCooldown(Material.TNT, 2 * 20);
                tnt.setYield(0.0f);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_TNT_PRIMED, 1.0f, 1.0f);
                Bukkit.getScheduler().runTaskLater(plugin, () -> Tools.bomb(tnt.getLocation(), 5, player, 15, 1.5, true, null), 20L + 2L);
            } else if (itemType == Material.REDSTONE && itemMeta.getDisplayName().equals("§2天女散花") && Players.getJobFromPlayer(player).equals("苦力怕")) {
                event.setCancelled(true);
                creeperRain(player);
                Energy.startDecay(player, "§2天女散花", 40 / 4);

            } else if (itemType == Material.DIAMOND && itemMeta.getDisplayName().equals("§c自爆") && Players.getJobFromPlayer(player).equals("苦力怕")) {
                event.setCancelled(true);
                creeperBomb(player);
                Energy.startDecay(player, "§c自爆", 5);
            }
        }
    }

    public static void start(Player player) {
        Players.setJobData(player, "creeper");
        Energy.createBar(player, Material.REDSTONE, "§2天女散花", BarColor.GREEN, 10, 10);
        Energy.createBar(player, Material.DIAMOND, "§c自爆", BarColor.RED, 20, 20);
    }


    public static void equipKits(Player player) {
        Inventory inv = player.getInventory();
        inv.clear();

        player.getInventory().setHelmet(Items.createItem(
                Material.LEATHER_HELMET,
                "§a苦力怕头盔",
                Arrays.asList(
                        "",
                        "§b🛡 §f防御 1"
                ),
                null,
                0,
                Color.fromRGB(0, 255, 0),
                1
        ));

        player.getInventory().setChestplate(Items.createItem(
                Material.LEATHER_CHESTPLATE,
                "§a苦力怕胸甲",
                Arrays.asList(
                        "",
                        "§b🛡 §f防御 3"
                ),
                null,
                0,
                Color.fromRGB(0, 150, 0),
                1
        ));

        player.getInventory().setLeggings(Items.createItem(
                Material.LEATHER_LEGGINGS,
                "§a苦力怕护腿",
                Arrays.asList(
                        "",
                        "§b🛡 §f防御 2"
                ),
                null,
                0,
                Color.fromRGB(0, 200, 0),
                1
        ));

        player.getInventory().setBoots(Items.createItem(
                Material.IRON_BOOTS,
                "§a苦力怕靴子",
                Arrays.asList(
                        "",
                        "§b🛡 §f防御 2"
                ),
                null,
                0,
                null,
                1
        ));

        inv.setItem(0, Items.createItem(
                Material.STONE_SWORD,
                "§f剑",
                Arrays.asList(
                        "§7苦力怕的剑",
                        "",
                        "§c❤ §f伤害 5  §6❇§f 攻速 较快"
                ),
                Enchantment.SHARPNESS,
                1,
                null,
                1
        ));

        inv.setItem(1, Items.createItem(
                Material.TNT,
                "§f发射TNT",
                Arrays.asList(
                        "§7一枚可以投掷的TNT",
                        "§7按§f[右键]§7投掷一枚TNT",
                        "",
                        "§c❤ §f伤害 12  §b✳ §f冷却 2 秒"
                ),
                null,
                0,
                null,
                1
        ));

        inv.setItem(2, Items.createItem(
                Material.REDSTONE,
                "§2天女散花",
                Arrays.asList(
                        "§7持续释放大量TNT",
                        "",
                        "§c❤ §f伤害 15  §a❖ §f能量 10  §e ⏱ §f时长 10 秒"
                ),
                null,
                0,
                null,
                1
        ));

        inv.setItem(3, Items.createItem(
                Material.DIAMOND,
                "§c自爆",
                Arrays.asList(
                        "§7充能后产生大范围爆炸",
                        "",
                        "§c❤ §f伤害 40  §a❖ §f能量 20  §e ⏱ §f时长 5 秒 "
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

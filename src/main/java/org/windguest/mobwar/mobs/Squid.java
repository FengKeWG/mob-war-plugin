package org.windguest.mobwar.mobs;

import org.bukkit.*;
import org.bukkit.boss.BarColor;
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
import org.bukkit.util.Vector;
import org.windguest.mobwar.games.*;
import org.windguest.mobwar.Main;

import java.util.Arrays;
import java.util.List;

public class Squid implements Listener {

    private static final Main plugin = Main.getInstance();

    private static void inkGun(Player player, Boolean glow) {
        Location playerLocation = player.getLocation();
        Vector direction = player.getEyeLocation().getDirection();
        double coneAngle = Math.toRadians(45);
        double maxSpread = Math.tan(coneAngle);
        Particle particle;
        double range;
        Sound sound;
        if (glow) {
            particle = Particle.GLOW_SQUID_INK;
            range = 8.0;
            sound = Sound.ENTITY_GLOW_SQUID_SQUIRT;
        } else {
            particle = Particle.SQUID_INK;
            range = 5.0;
            sound = Sound.ENTITY_SQUID_SQUIRT;
        }
        player.getWorld().playSound(player.getLocation(), sound, 1.0f, 1.0f);
        player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1.0f, 1.0f);
        for (double t = 0; t < range; t += 0.2) {
            int numParticles = (int) (Math.max(5, t * 3));
            for (int i = 0; i < numParticles; i++) {
                double spreadX = (Math.random() - 0.5) * maxSpread * t;
                double spreadY = (Math.random() - 0.5) * maxSpread * t;
                double spreadZ = (Math.random() - 0.5) * maxSpread * t;
                Vector offset = new Vector(spreadX, spreadY, spreadZ);
                Location particleLocation = playerLocation.clone().add(direction.clone().multiply(t)).add(offset);
                player.getWorld().spawnParticle(particle, particleLocation, 1, 0, 0, 0, 0.1);
                for (Entity entity : player.getWorld().getNearbyEntities(particleLocation, 0.5, 0.5, 0.5)) {
                    if (entity instanceof LivingEntity livingEntity) {
                        if (livingEntity instanceof Player victim) {
                            if (victim.equals(player) || Players.isTeam(player, victim)) continue;
                            if (victim.isBlocking() && Tools.isFacingLocation(victim, player.getLocation())) {
                                Tools.reduceShieldDurability(victim, 5);
                                continue;
                            }
                        }
                        livingEntity.damage(5, player);
                        Vector knockbackDirection = livingEntity.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
                        knockbackDirection.multiply(0.7);
                        livingEntity.setVelocity(knockbackDirection);
                    }
                }
            }
        }
        if (!glow) {
            Vector recoil = direction.clone().multiply(-1.0);
            player.setVelocity(recoil);
        }
    }


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        ItemStack item = event.getItem();
        if (item != null) {
            Material itemType = item.getType();
            ItemMeta itemMeta = item.getItemMeta();
            if (itemType == Material.INK_SAC) {
                if (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) {
                    event.setCancelled(true);
                    int total = Items.countItems(player, Material.INK_SAC);
                    inkGun(player, false);
                    if (total == 1) {
                        Items.replaceItem(player, "§0墨水喷枪", Material.BARRIER);
                    } else {
                        Items.reduceItemsInInventory(player, Material.INK_SAC);
                    }
                }
            }
            if (itemType == Material.BLACK_DYE && itemMeta.getDisplayName().equals("§e快速装填") && Players.getJobFromPlayer(player).equals("鱿鱼")) {
                event.setCancelled(true);
                Energy.startDecay(player, "§e快速装填", 1);
                int total = Items.countItems(player, Material.INK_SAC);
                if (total == 0) {
                    Items.replaceItem(player, "§0墨水喷枪", Material.INK_SAC);
                }
                if (total < 3) {
                    for (ItemStack i : player.getInventory().getContents()) {
                        if (i != null && i.getType() == Material.INK_SAC) {
                            i.setAmount(3);
                            return;
                        }
                    }
                }
            } else if (itemType == Material.GLOW_INK_SAC && itemMeta.getDisplayName().equals("§b荧光喷枪") && Players.getJobFromPlayer(player).equals("鱿鱼")) {
                event.setCancelled(true);
                inkGun(player, true);
                Players.setJobData(player, "squid_glow");
                Disguise.disguise(player, 0);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        inkGun(player, true);
                    }
                }.runTaskLater(plugin, 10L);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        inkGun(player, true);
                    }
                }.runTaskLater(plugin, 20L);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        inkGun(player, true);
                    }
                }.runTaskLater(plugin, 30L);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        inkGun(player, true);
                        Disguise.undisguise(player);
                        Players.setJobData(player, "squid");
                    }
                }.runTaskLater(plugin, 40L);
                Energy.startDecay(player, "§b荧光喷枪", 3);
            }
        }
    }

    public static void start(Player player) {
        Players.setJobData(player, "squid");
        Items.giveItemsInTimes(player, "§0墨水喷枪", Material.INK_SAC, 3, 3);
        player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, Integer.MAX_VALUE, 0, true, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, Integer.MAX_VALUE, 0, true, false, true));
        Energy.createBar(player, Material.BLACK_DYE, "§e快速装填", BarColor.YELLOW, 10, 10);
        Energy.createBar(player, Material.GLOW_INK_SAC, "§b荧光喷枪", BarColor.BLUE, 20, 20);
    }

    public static void equipKits(Player player) {
        Inventory inv = player.getInventory();
        inv.clear();

        player.getInventory().setHelmet(Items.createItem(
                Material.LEATHER_HELMET,
                "§9鱿鱼头盔",
                Arrays.asList(
                        "",
                        "§b🛡 §f防御 1"
                ),
                null,
                0,
                Color.fromRGB(255, 105, 180),
                1
        ));

        player.getInventory().setChestplate(Items.createItem(
                Material.NETHERITE_CHESTPLATE,
                "§9鱿鱼胸甲",
                Arrays.asList(
                        "",
                        "§b🛡 §f防御 6"
                ),
                null,
                0,
                Color.fromRGB(0, 191, 255),
                1
        ));

        player.getInventory().setLeggings(Items.createItem(
                Material.LEATHER_LEGGINGS,
                "§9鱿鱼护腿",
                Arrays.asList(
                        "",
                        "§b🛡 §f防御 2"
                ),
                null,
                0,
                Color.fromRGB(75, 0, 130),
                1
        ));

        player.getInventory().setBoots(Items.createItem(
                Material.LEATHER_BOOTS,
                "§9鱿鱼靴子",
                Arrays.asList(
                        "",
                        "§b🛡 §f防御 2"
                ),
                null,
                0,
                Color.fromRGB(123, 104, 238),
                1
        ));

        inv.setItem(0, Items.createItem(
                Material.IRON_SWORD,
                "§f剑",
                Arrays.asList(
                        "§7鱿鱼的剑",
                        "",
                        "§c❤ §f伤害 6  §6❇ §f攻速 较快"
                ),
                null,
                0,
                null,
                1
        ));

        inv.setItem(1, Items.createItem(
                Material.INK_SAC,
                "§0墨水喷枪",
                Arrays.asList(
                        "§7按§f[右键]§7喷出墨水",
                        "§7对碰到墨水的生物造成伤害",
                        "§7同时自己被反向击退",
                        "",
                        "§c❤ §f伤害 5  §b✳ §f冷却 3 秒  §e☒ §f上限 3 个"
                ),
                null,
                0,
                null,
                1
        ));

        inv.setItem(2, Items.createItem(
                Material.BLACK_DYE,
                "§e快速装填",
                Arrays.asList(
                        "§7快速装填完墨水喷枪",
                        "",
                        "§a❖ §f能量 10"
                ),
                null,
                0,
                null,
                1
        ));

        inv.setItem(3, Items.createItem(
                Material.GLOW_INK_SAC,
                "§b荧光喷枪",
                Arrays.asList(
                        "§7快速喷射 5 次荧光墨水",
                        "§7对碰到墨水的生物造成伤害",
                        "",
                        "§c❤ §f伤害 5  §a❖ §f能量 20"
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


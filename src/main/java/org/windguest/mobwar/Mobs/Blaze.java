package org.windguest.mobwar.Mobs;

import java.util.*;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
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

public class Blaze implements Listener {

    private static final Main plugin = Main.getInstance();

    private static final Map<UUID, HashSet<Location>> playerFireLocations = new HashMap<>();
    private static final Map<UUID, Long> lastDamageTime = new HashMap<>();

    public static void blazeShield(Player player, int duration) {
        ArrayList<Location> particleLocations = new ArrayList<>();
        for (int i = 0; i < 10; ++i) {
            double angle = Math.PI * 2 * (double) i / (double) 10;
            double yOffset = 0.5 + (double) (i % 4) * 0.5;
            Location loc = player.getLocation().clone().add(new Vector(Math.cos(angle), yOffset, Math.sin(angle)));
            particleLocations.add(loc);
        }
        BukkitTask task = new BukkitRunnable() {
            int ticks = 0;

            public void run() {
                if (ticks >= duration * 20) {
                    cancel();
                    return;
                }
                for (Entity entity : player.getNearbyEntities(1.5, 2.0, 1.5)) {
                    if (!(entity instanceof LivingEntity livingEntity)) continue;
                    if (entity instanceof Player targetPlayer && Players.isTeam(player, targetPlayer)) {
                        continue;
                    }
                    long currentTime = System.currentTimeMillis();
                    long lastPlayTime = lastDamageTime.getOrDefault(livingEntity.getUniqueId(), 0L);
                    if (currentTime - lastPlayTime >= 500L) {
                        double originalKnockbackResistance = livingEntity.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).getBaseValue();
                        livingEntity.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(1.0);
                        livingEntity.damage(2, player);
                        livingEntity.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(originalKnockbackResistance);
                        lastDamageTime.put(livingEntity.getUniqueId(), currentTime);
                    }
                }
                for (int i = 0; i < particleLocations.size(); ++i) {
                    Location loc = player.getLocation().clone();
                    double angle = Math.PI * 2 * (double) i / (double) particleLocations.size() + (double) ticks * 0.1;
                    double yOffset = 0.5 + (double) (i % 4) * 0.5;
                    loc.add(new Vector(Math.cos(angle), yOffset, Math.sin(angle)));
                    player.getWorld().spawnParticle(Particle.FLAME, loc, 0, 0.0, 0.0, 0.0, 0.0);
                }
                ++ticks;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        Players.addTaskToPlayer(player, task);
    }

    public static void blazeGun(Player player) {
        Disguise.disguise(player, 1);
        Players.setJobData(player, "blaze_gun");
        Random random = new Random();
        for (int i = 0; i < 80; ++i) {
            BukkitTask task = new BukkitRunnable() {
                public void run() {
                    if (player.isOnline() && !player.isDead()) {
                        Location playerLocation = player.getLocation();
                        Vector direction = playerLocation.getDirection().normalize();
                        double spread = 0.2;
                        double xSpread = (random.nextDouble() - 0.5) * spread;
                        double ySpread = (random.nextDouble() - 0.5) * spread;
                        double zSpread = (random.nextDouble() - 0.5) * spread;
                        Vector finalDirection = direction.clone().add(new Vector(xSpread, ySpread, zSpread)).normalize();
                        SmallFireball fireball = player.launchProjectile(SmallFireball.class);
                        fireball.setVelocity(finalDirection.multiply(1.5));
                        fireball.setIsIncendiary(false);
                        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.0f, 1.0f);
                    }
                }
            }.runTaskLater(plugin, i * 3);
            Players.addTaskToPlayer(player, task);
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Disguise.undisguise(player);
            if (Players.getJobData(player).equals("blaze_gun")) {
                Players.setJobData(player, "blaze");
            }
        }, 2 * 120);
    }

    public static void blazeFly(Player player) {
        Disguise.disguise(player, 0);
        createFireCircle(player);
        Players.setPlayerFly(player, 0.03f);
        Players.setJobData(player, "blaze_fly");
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_AMBIENT, 1.0f, 1.0f);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (Players.getJobData(player).equals("blaze_fly")) {
                Players.setJobData(player, "blaze");
            }
            Disguise.undisguise(player);
            Players.disablePlayerFly(player);
        }, 20 * 20L);
    }

    private static void startWaterDamageTask(Player player) {
        BukkitTask task = new BukkitRunnable() {
            public void run() {
                if (player.getLocation().getBlock().getType() == Material.WATER
                        || player.getEyeLocation().getBlock().getType() == Material.WATER) {
                    player.damage(4);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
        Players.addTaskToPlayer(player, task);
    }

    private static void createFireCircle(Player player) {
        UUID playerUUID = player.getUniqueId();
        HashSet<Location> fireLocations = new HashSet<>();
        Location playerLocation = player.getLocation();
        int startY = playerLocation.getBlockY() - 1;
        for (int x = -10; x <= 10; ++x) {
            for (int z = -10; z <= 10; ++z) {
                if (x * x + z * z > 10 * 10) continue;
                Location loc = playerLocation.clone().add(x, 0.0, z);
                loc.setY(startY);
                if (!loc.getBlock().getType().isSolid() || loc.clone().add(0.0, 1.0, 0.0).getBlock().getType() != Material.AIR)
                    continue;
                Location fireLoc = loc.clone().add(0.0, 1.0, 0.0);
                if (fireLoc.getBlock().getType() == Material.SOUL_SAND || fireLoc.getBlock().getType() == Material.SOUL_SOIL) {
                    fireLoc.getBlock().setType(Material.SOUL_FIRE);
                } else {
                    fireLoc.getBlock().setType(Material.FIRE);
                }
                fireLocations.add(fireLoc);
            }
        }
        playerFireLocations.put(playerUUID, fireLocations);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            fireLocations.forEach(loc -> {
                Material blockType = loc.getBlock().getType();
                if (blockType == Material.FIRE || blockType == Material.SOUL_FIRE) {
                    loc.getBlock().setType(Material.AIR);
                }
            });
            playerFireLocations.remove(playerUUID);
        }, 20 * 20L);
    }

    public static void clearAllFires() {
        for (HashSet<Location> fireLocations : playerFireLocations.values()) {
            fireLocations.forEach(loc -> {
                Material blockType = loc.getBlock().getType();
                if (blockType == Material.FIRE || blockType == Material.SOUL_FIRE) {
                    loc.getBlock().setType(Material.AIR);
                }
            });
        }
    }

    public static void clearPlayerFires(Player player) {
        UUID playerUUID = player.getUniqueId();
        HashSet<Location> fireLocations = playerFireLocations.remove(playerUUID);
        if (fireLocations != null) {
            fireLocations.forEach(loc -> {
                Material blockType = loc.getBlock().getType();
                if (blockType == Material.FIRE || blockType == Material.SOUL_FIRE) {
                    loc.getBlock().setType(Material.AIR);
                }
            });
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
            if (itemType == Material.BLAZE_ROD && !player.hasCooldown(Material.BLAZE_ROD)) {
                if (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) {
                    event.setCancelled(true);
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.0f, 1.0f);
                    SmallFireball fireball = player.launchProjectile(SmallFireball.class);
                    fireball.setVelocity(player.getLocation().getDirection().multiply(1.5));
                    fireball.setIsIncendiary(false);
                    player.setCooldown(Material.BLAZE_ROD, 10);
                }
            }
            if (itemType == Material.FLINT_AND_STEEL && itemMeta.getDisplayName().equals("§c火焰护体") && Players.getJobFromPlayer(player).equals("烈焰人")) {
                event.setCancelled(true);
                blazeFly(player);
                blazeShield(player, 20);
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 20 * 20, 0, true, false, true));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 20, 0, true, false, true));
                Energy.startDecay(player, "§c火焰护体", 20);
            } else if (itemType == Material.BLAZE_POWDER && itemMeta.getDisplayName().equals("§e火焰机枪") && Players.getJobFromPlayer(player).equals("烈焰人")) {
                event.setCancelled(true);
                blazeGun(player);
                Energy.startDecay(player, "§e火焰机枪", 2 * 120 / 20);
            }
        }
    }

    public static void start(Player player) {
        Players.setJobData(player, "blaze");
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, true, false, true));
        Energy.createBar(player, Material.FLINT_AND_STEEL, "§c火焰护体", BarColor.RED, 15, 15);
        Energy.createBar(player, Material.BLAZE_POWDER, "§e火焰机枪", BarColor.YELLOW, 20, 20);
        startWaterDamageTask(player);
    }

    public static void equipKits(Player player) {
        Inventory inv = player.getInventory();
        inv.clear();
        player.getInventory().setHelmet(Items.createItem(Material.LEATHER_HELMET, "§9烈焰人头盔",
                Arrays.asList(
                        "",
                        "§b🛡 §f防御 1"
                ),
                null, 0,
                Color.fromRGB(255, 165, 0), 1));
        player.getInventory().setChestplate(Items.createItem(Material.LEATHER_CHESTPLATE, "§9烈焰人胸甲",
                Arrays.asList(
                        "",
                        "§b🛡 §f防御 3"
                ),
                null, 0,
                Color.fromRGB(255, 69, 0), 1));
        player.getInventory().setLeggings(Items.createItem(Material.DIAMOND_LEGGINGS, "§9烈焰人护腿",
                Arrays.asList(
                        "",
                        "§b🛡 §f防御 6"
                ),
                null, 0,
                null, 1));
        player.getInventory().setBoots(Items.createItem(Material.DIAMOND_BOOTS, "§9烈焰人靴子",
                Arrays.asList(
                        "",
                        "§b🛡 §f防御 3"
                ),
                null, 0,
                null, 1));
        inv.setItem(0, Items.createItem(Material.STONE_SWORD, "§f剑",
                Arrays.asList(
                        "§7烈焰人的剑",
                        "§7攻击使敌人着火",
                        "",
                        "§c❤ §f伤害 5  §6❇§f 攻速 较快"
                ),
                Enchantment.FIRE_ASPECT, 1,
                null, 1));
        inv.setItem(1, Items.createItem(Material.NETHERITE_HOE, "§f火锄",
                Arrays.asList(
                        "§7带火的锄",
                        "§7攻击使敌人着火",
                        "",
                        "§c❤ §f伤害 1  §6❇§f 攻速 快"
                ),
                Enchantment.FIRE_ASPECT, 3,
                null, 1));
        inv.setItem(2, Items.createItem(Material.BLAZE_ROD, "§f发射烈焰弹",
                Arrays.asList(
                        "§7按§f[右键]§7发射一枚烈焰弹",
                        "",
                        "§c❤ §f伤害 5  §6❇§f 冷却 0.5 秒"
                ),
                null, 0,
                null, 1));
        inv.setItem(3, Items.createItem(Material.FLINT_AND_STEEL, "§c火焰护体",
                Arrays.asList(
                        "§7获得力量 I 与速度 I",
                        "§7并产生大范围火焰",
                        "§7同时你可以缓慢飞行",
                        "",
                        "§a❖ §f能量 15  §e ⏱ §f时长 20 秒"
                ),
                null, 0,
                null, 1));
        inv.setItem(4, Items.createItem(Material.BLAZE_POWDER, "§e火焰机枪",
                Arrays.asList(
                        "§7发射大量烈焰弹",
                        "",
                        "§c❤ §f伤害 5  §a❖ §f能量 20  §e ⏱ §f时长 12 秒 "
                ),
                null, 0,
                null, 1));
        inv.setItem(7, Items.createItem(Material.GOLDEN_APPLE, "§f金苹果",
                List.of(
                        "§7长按§f[右键]§7食用恢复生命"
                ),
                null, 0,
                null, 3));
        inv.setItem(8, Items.createItem(Material.APPLE, "§f苹果",
                Arrays.asList(
                        "§7这是一些苹果",
                        "§7长按§f[右键]§7恢复饱食度"
                ),
                null, 0,
                null, 16));
    }
}


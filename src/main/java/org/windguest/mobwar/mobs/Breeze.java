package org.windguest.mobwar.mobs;

import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.windguest.mobwar.games.*;
import org.windguest.mobwar.Main;

import java.util.Arrays;
import java.util.List;

public class Breeze implements Listener {

    private static final Main plugin = Main.getInstance();

    public static void storm(Player player) {
        Vector direction = player.getLocation().getDirection().normalize();
        double speed = 6.0;
        player.setVelocity(direction.multiply(speed));

        BukkitTask task = new BukkitRunnable() {
            int ticks = 0;
            int cnt = 0;

            public void run() {
                ticks++;
                int amount = Items.countItems(player, Material.WIND_CHARGE);
                if (amount > 1) {
                    cnt++;
                    Items.reduceItemsInInventory(player, Material.WIND_CHARGE);
                } else if (amount == 1) {
                    cnt++;
                    Items.replaceItem(player, "§f风弹", Material.BARRIER);
                }
                Location location = player.getLocation();
                if (ticks > 5 && location.clone().subtract(0.0, 1.0, 0.0).getBlock().getType() != Material.AIR) {
                    Players.setJobData(player, "breeze_storm");
                    breezeBomb(player, location, cnt);
                    Players.setJobData(player, "breeze");
                    cancel();
                }
                double height = 20.0;
                double radiusStep = 0.3;
                int spiralTurns = 8;
                for (double y = height; y > 0.0; y -= 0.5) {
                    double currentRadius = y * radiusStep;
                    double angle = (double) (spiralTurns * 360) * (height - y) / height;
                    double radians = Math.toRadians(angle + (double) (ticks * 5));
                    double x = location.getX() + currentRadius * Math.cos(radians);
                    double z = location.getZ() + currentRadius * Math.sin(radians);
                    double yLocation = location.getY() + y;
                    Location particleLocation = new Location(player.getWorld(), x, yLocation, z);
                    player.getWorld().spawnParticle(Particle.CLOUD, particleLocation, 1, 0.0, 0.0, 0.0, 0.01);
                }
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 1.0f);
            }
        }.runTaskTimer(plugin, 0L, 1L);
        Players.addTaskToPlayer(player, task, Players.TaskTag.SKILL);
    }

    private static void breezeBomb(Player player, Location location, int cnt) {
        World world = location.getWorld();
        world.spawnParticle(Particle.EXPLOSION, location, 1);
        world.spawnParticle(Particle.SWEEP_ATTACK, location, 10, 0.5, 0.5, 0.5, 0.1);
        world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
        world.playSound(location, Sound.ITEM_ELYTRA_FLYING, 1.0f, 1.0f);
        world.playSound(location, Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 1.0f);
        int shockwaveLayers = 5;
        double shockwaveRadius = cnt;
        for (int layer = 1; layer <= shockwaveLayers; layer++) {
            double radius = (shockwaveRadius / shockwaveLayers) * layer;
            double angleStep = Math.PI / 30;
            for (double angle = 0; angle < 2 * Math.PI; angle += angleStep) {
                double x = radius * Math.cos(angle);
                double z = radius * Math.sin(angle);
                Location shockLocation = location.clone().add(x, 0, z);
                world.spawnParticle(Particle.CLOUD, shockLocation, 1, 0, 0, 0, 0.0);
                world.spawnParticle(Particle.SMOKE, shockLocation, 1, 0, 0, 0, 0.0);
            }
        }
        double radius = Math.max(cnt * 0.4, 3);
        double damage = 1.0 * cnt;
        double kb = Math.max(cnt * 0.2, 2);
        for (Entity entity : world.getNearbyEntities(location, radius, radius, radius)) {
            if (!(entity instanceof LivingEntity livingEntity)) continue;
            if (entity.equals(player)) continue;
            if (entity instanceof Player targetPlayer) {
                if (Players.isTeam(player, targetPlayer)) continue;
            }
            livingEntity.damage(damage, player);
            Vector direction = livingEntity.getLocation().toVector().subtract(location.toVector()).normalize();
            livingEntity.setVelocity(direction.multiply(kb).add(new Vector(0, 0.4, 0)));
            createParticleLine(player.getLocation(), livingEntity.getLocation());
        }
    }

    private static void createParticleLine(Location start, Location end) {
        World world = start.getWorld();
        double distance = start.distance(end);
        Vector vector = end.toVector().subtract(start.toVector()).normalize().multiply(0.1);
        Location currentLocation = start.clone();
        for (double covered = 0.0; covered < distance; covered += 0.1) {
            world.spawnParticle(Particle.CLOUD, currentLocation, 1, 0.0, 0.0, 0.0, 0.0);
            currentLocation.add(vector);
            currentLocation.setYaw(currentLocation.getYaw() + 10.0f);
        }
    }

    private void launchPlayerInParabola(Player player) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BREEZE_CHARGE, 1.0f, 1.0f);
        BukkitTask task = new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 10) {
                    cancel();
                    return;
                }
                Location location = player.getLocation().add(0, 0.1, 0);
                for (double angle = 0; angle < 360; angle += 10) {
                    double radians = Math.toRadians(angle);
                    double x = Math.cos(radians);
                    double z = Math.sin(radians);
                    location.getWorld().spawnParticle(Particle.SWEEP_ATTACK, location.clone().add(x, 0, z), 1, 0, 0, 0, 0);
                }
                ticks++;
            }
        }.runTaskTimer(plugin, 0, 1);
        Players.addTaskToPlayer(player, task, Players.TaskTag.SKILL);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BREEZE_JUMP, 1.0f, 1.0f);
            Vector direction = player.getLocation().getDirection().normalize();
            double launchSpeed = 2.0;
            Vector launchVector = direction.multiply(launchSpeed).setY(1.5);
            player.setVelocity(launchVector);
        }, 10L);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity victim = event.getEntity();
        if (event.getDamager().getType() == EntityType.WIND_CHARGE
                && ((Projectile) event.getDamager()).getShooter() instanceof Player attackerPlayer) {
            if (!Players.getJobFromPlayer(attackerPlayer).equals("旋风人")) return;
            if (victim instanceof Player victimPlayer) {
                if (Players.isTeam(victimPlayer, attackerPlayer)) {
                    event.setCancelled(true);
                    return;
                }
            }
            if (victim instanceof LivingEntity) {
                event.setDamage(4);
            }
        }
    }

    private static void bigWindCharge(Player player) {
        Disguise.disguise(player, 0);
        Players.addSkill(player, 1);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BREEZE_LAND, 1.0f, 1.0f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BREEZE_DEFLECT, 1.0f, 1.0f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BREEZE_IDLE_AIR, 1.0f, 1.0f);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Disguise.undisguise(player);
            Players.removeSkill(player);
        }, 20 * 20L);
    }

    public static void shootBigWindCharge(Player player) {
        int cnt = Math.min(Items.countItems(player, Material.WIND_CHARGE), 12);
        Items.keepOnlyOne(player, "§f风弹");
        Items.replaceItem(player, "§f风弹", Material.BARRIER);
        WindCharge windCharge = player.launchProjectile(WindCharge.class);
        Entities.addEntity(windCharge, player, cnt);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f + (cnt / 12.0f), 1.0f);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f + (cnt / 24.0f), 0.8f);
        windCharge.setGlowing(true);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (windCharge.isDead() || !windCharge.isValid()) {
                    cancel();
                    return;
                }
                Location location = windCharge.getLocation();
                World world = windCharge.getWorld();
                int particleCount = 20 + cnt * 20;
                double spread = 0.6 + (cnt / 8.0);
                for (int i = 0; i < particleCount; i++) {
                    double theta = Math.random() * 2 * Math.PI;
                    double phi = Math.acos(2 * Math.random() - 1);
                    double x = spread * Math.sin(phi) * Math.cos(theta);
                    double y = spread * Math.sin(phi) * Math.sin(theta);
                    double z = spread * Math.cos(phi);
                    Vector offset = new Vector(x, y, z);
                    Location particleLocation = location.clone().add(offset);
                    if (cnt == 12) {
                        world.spawnParticle(Particle.DUST, particleLocation, 1, new Particle.DustOptions(Color.RED, 0.5f));
                    } else if (cnt >= 10) {
                        world.spawnParticle(Particle.DUST, particleLocation, 1, new Particle.DustOptions(Color.ORANGE, 0.5f));
                    } else if (cnt >= 8) {
                        world.spawnParticle(Particle.DUST, particleLocation, 1, new Particle.DustOptions(Color.BLUE, 0.5f));
                    } else if (cnt >= 6) {
                        world.spawnParticle(Particle.DUST, particleLocation, 1, new Particle.DustOptions(Color.AQUA, 0.5f));
                    } else if (cnt >= 4) {
                        world.spawnParticle(Particle.DUST, particleLocation, 1, new Particle.DustOptions(Color.GREEN, 0.5f));
                    } else if (cnt >= 2) {
                        world.spawnParticle(Particle.DUST, particleLocation, 1, new Particle.DustOptions(Color.GRAY, 0.5f));
                    }
                    if (Math.random() < 0.3) {
                        world.spawnParticle(Particle.SMOKE, particleLocation, 1, 0.1, 0.1, 0.1, 0.05);
                    }
                }
                world.spawnParticle(Particle.END_ROD, location, 5 + cnt, 0.3, 0.3, 0.3, 0.02);
                if (cnt >= 6) {
                    for (int i = 0; i < 3; i++) {
                        Location flashLocation = location.clone().add(Math.random() * 2 - 1, Math.random() * 2 - 1, Math.random() * 2 - 1);
                        world.spawnParticle(Particle.FLASH, flashLocation, 1, 0, 0, 0, 0.0);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof WindCharge windCharge) {
            if (Entities.getEntityOwner(windCharge) == null || Entities.getEntityInfo(windCharge) == null) {
                return;
            }
            Location location = windCharge.getLocation();
            Player shooter = Entities.getEntityOwner(windCharge);
            int cnt = Entities.getEntityInfo(windCharge);
            Players.setJobData(shooter, "breeze_big");
            breezeBomb(shooter, location, cnt);
            Players.setJobData(shooter, "breeze");
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

            if (itemType == Material.WIND_CHARGE && Players.getJobFromPlayer(player).equals("旋风人")) {
                if (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) {
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BREEZE_SHOOT, 1.0f, 1.0f);
                    int totalWindCharges = Items.countItems(player, Material.WIND_CHARGE);
                    if (totalWindCharges <= 1) {
                        event.setCancelled(true);
                        Items.replaceItem(player, "§f风弹", Material.BARRIER);
                        if (Players.getSkill(player) == 1) {
                            shootBigWindCharge(player);
                        } else {
                            player.launchProjectile(WindCharge.class);
                        }
                    } else if (Players.getSkill(player) == 1) {
                        shootBigWindCharge(player);
                    }
                }

            } else if (itemType == Material.FEATHER && !player.hasCooldown(Material.FEATHER)) {
                event.setCancelled(true);
                launchPlayerInParabola(player);
                player.setCooldown(Material.FEATHER, 4 * 20);

            } else if (itemType == Material.BREEZE_ROD && itemMeta.getDisplayName().equals("§b风爆") && Players.getJobFromPlayer(player).equals("旋风人")) {
                event.setCancelled(true);
                storm(player);
                Energy.startDecay(player, "§b风爆", 2);

            } else if (itemType == Material.SNOWBALL && itemMeta.getDisplayName().equals("§9蓄能风弹") && Players.getJobFromPlayer(player).equals("旋风人")) {
                event.setCancelled(true);
                bigWindCharge(player);
                Energy.startDecay(player, "§9蓄能风弹", 20);
            }
        }
    }

    public static void start(Player player) {
        Players.setJobData(player, "breeze");
        Items.giveItemsInTimes(player, "§f风弹", Material.WIND_CHARGE, 1, 12);
        Energy.createBar(player, Material.BREEZE_ROD, "§b风爆", BarColor.GREEN, 10, 10);
        Energy.createBar(player, Material.SNOWBALL, "§9蓄能风弹", BarColor.BLUE, 20, 20);
    }


    public static void equipKits(Player player) {
        Inventory inv = player.getInventory();
        inv.clear();
        player.getInventory().setHelmet(Items.createItem(Material.LEATHER_HELMET, "§b旋风人头盔",
                Arrays.asList(
                        "§b弹射物保护 I",
                        "",
                        "§b🛡 §f防御 1"
                ),
                Enchantment.PROJECTILE_PROTECTION, 1,
                Color.fromRGB(173, 216, 230), 1));
        player.getInventory().setChestplate(Items.createItem(Material.LEATHER_CHESTPLATE, "§b旋风人胸甲",
                Arrays.asList(
                        "§b弹射物保护 I",
                        "",
                        "§b🛡 §f防御 3"
                ),
                Enchantment.PROJECTILE_PROTECTION, 1,
                Color.fromRGB(240, 248, 255), 1));
        player.getInventory().setLeggings(Items.createItem(Material.LEATHER_LEGGINGS, "§b旋风人护腿",
                Arrays.asList(
                        "§b弹射物保护 I",
                        "",
                        "§b🛡 §f防御 2"
                ),
                Enchantment.PROJECTILE_PROTECTION, 1,
                Color.fromRGB(173, 216, 230), 1));
        player.getInventory().setBoots(Items.createItem(Material.IRON_BOOTS, "§b旋风人靴子",
                Arrays.asList(
                        "§b弹射物保护 I",
                        "",
                        "§b🛡 §f防御 2"
                ),
                Enchantment.PROJECTILE_PROTECTION, 1,
                Color.fromRGB(173, 216, 230), 1));
        inv.setItem(2, Items.createItem(Material.WIND_CHARGE, "§f风弹",
                Arrays.asList(
                        "§7按§f[右键]§7发射一枚风弹",
                        "",
                        "§c❤ §f伤害 4  §b✳ §f冷却 1 秒  §e☒ §f上限 12 个"
                ),
                null, 0,
                null, 1));
        inv.setItem(0, Items.createItem(Material.WOODEN_SWORD, "§f剑",
                Arrays.asList(
                        "§7旋风人的剑",
                        "",
                        "§c❤ §f伤害 4  §6❇ §f攻速 较快"
                ),
                null, 0,
                null, 1));
        inv.setItem(1, Items.createItem(Material.FEATHER, "§f弹跳",
                Arrays.asList(
                        "§7按§f[右键]§7弹跳",
                        "",
                        "§6❇ §f冷却 4 秒"
                ),
                null, 0,
                null, 1));
        inv.setItem(3, Items.createItem(Material.BREEZE_ROD, "§b风爆",
                Arrays.asList(
                        "§7向准星方向弹射并不断吸收风弹",
                        "§7当触碰到地面后",
                        "§7对半径内的生物造成伤害和击退",
                        "§7伤害、击退、半径与吸收的风弹数目成正比",
                        "",
                        "§c❤ §f伤害 1.0 × 风弹数  §a❖ §f能量 10"
                ),
                null, 0,
                null, 1));
        inv.setItem(4, Items.createItem(Material.SNOWBALL, "§9蓄能风弹",
                Arrays.asList(
                        "§7在技能期间的每次射击",
                        "§7都会立即消耗全部风弹",
                        "§7并释放一个蓄能风弹",
                        "§7对半径内的生物造成伤害和击退",
                        "§7伤害、击退、半径与背包的风弹数目成正比",
                        "",
                        "§c❤ §f伤害 0.8 × 风弹数  §a❖ §f能量 20  §e ⏱ §f时长 20 秒"
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


package org.windguest.mobwar.Mobs;

import java.util.*;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.windguest.mobwar.Games.Disguise;
import org.windguest.mobwar.Games.Energy;
import org.windguest.mobwar.Games.Items;
import org.windguest.mobwar.Main;
import org.windguest.mobwar.Games.Players;
import fr.skytasul.guardianbeam.Laser.GuardianLaser;

public class Guardian implements Listener {

    private static final Main plugin = Main.getInstance();
    private final Map<UUID, Long> lastDamageTime = new HashMap<>();

    public void elderGuardian(Player player) { //debuff
        Players.setJobData(player, "guardian_elder");
        Disguise.disguise(player, 1);
        for (Entity entity : player.getNearbyEntities(10, 10, 10)) {
            if (!(entity instanceof LivingEntity livingEntity)) continue;
            if (entity instanceof Player p && Players.isTeam(player, p)) {
                continue;
            }
            Location loc = livingEntity.getLocation();
            if (entity instanceof Player p) {
                p.playSound(loc, Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1.0f, 1.0f);
            }
            livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 10 * 20, 6, false, false, true));
            livingEntity.getWorld().spawnParticle(Particle.ELDER_GUARDIAN, loc, 1);
        }
    }

    @EventHandler
    public void onPlayerDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player defender) {
            if (Players.getJobData(defender).equals("guardian_elder")) {
                if (event.getDamager() instanceof LivingEntity attacker) {
                    if (attacker instanceof Player player) {
                        if (Players.isTeam(defender, player)) return;
                    }
                    attacker.damage(3, defender);
                }
            }
        }
    }

    public Location calculateEndLocation(Player player) {
        Location eyeLocation = player.getEyeLocation();
        Vector direction = eyeLocation.getDirection();
        RayTraceResult result = player.getWorld().rayTraceBlocks(eyeLocation, direction, 100, FluidCollisionMode.NEVER, true);
        if (result != null && result.getHitBlock() != null) {
            return result.getHitPosition().toLocation(player.getWorld());
        }
        return eyeLocation.add(direction.multiply(100));
    }

    private void guardianShoot(Player player) throws ReflectiveOperationException { //普通激光
        Players.setJobData(player, "guardian_shoot");
        Disguise.disguise(player, 0);
        Location startLocation = player.getLocation().add(player.getLocation().getDirection().multiply(-0.5)).add(0.0, 1.5, 0.0);
        Location endLocation = calculateEndLocation(player);
        GuardianLaser guardianLaser = new GuardianLaser(startLocation, endLocation, -1, 1000);
        guardianLaser.start(plugin);
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 15 * 20 || player.getY() > 240) {
                    if (Players.getJobData(player).equals("guardian_shoot")) {
                        Players.setJobData(player, "guardian");
                    }
                    Disguise.undisguise(player);
                    guardianLaser.stop();
                    cancel();
                    return;
                }
                ++ticks;
                try {
                    if (ticks % 20 == 0) {
                        player.playSound(player.getLocation(), Sound.ENTITY_GUARDIAN_ATTACK, 1.0f, 1.0f);
                    }
                    Location newStart = player.getLocation().add(player.getLocation().getDirection().multiply(-0.5)).add(0.0, 2.0, 0.0);
                    Location newEnd = calculateEndLocation(player);
                    guardianLaser.moveStart(newStart);
                    guardianLaser.moveEnd(newEnd);
                    damageEntitiesInLineOfSight(player, newEnd, 3);
                } catch (ReflectiveOperationException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void guardianMax(Player player) { // 超级激光
        Disguise.undisguise(player);
        BukkitTask task = new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 5 * 20) {
                    Players.setJobData(player, "guardian_max");
                    Location newEnd = calculateEndLocation(player);
                    damageEntitiesInLineOfSight(player, newEnd, 1000);
                    player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1.0f, 1.0f);
                    if (Players.getJobData(player).equals("guardian_max")) {
                        Players.setJobData(player, "guardian");
                    }
                    Disguise.undisguise(player);
                    cancel();
                    return;
                }
                ++ticks;
                Location start = player.getLocation().add(player.getLocation().getDirection().multiply(-0.5)).add(0.0, 2.0, 0.0);
                Location end = calculateEndLocation(player);
                float pitch = 0.5f + (float) ticks / (5 * 20);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0f, pitch);
                Color currentColor = interpolateColor((double) ticks / (5 * 20));
                generateColoredLaser(player, start, end, currentColor);
                notifyPlayersInLineOfSight(player, end, pitch);
            }
        }.runTaskTimer(plugin, 0L, 1L);
        Players.addTaskToPlayer(player, task);
    }

    private void generateColoredLaser(Player player, Location start, Location end, Color color) {
        World world = player.getWorld();
        Vector startVec = start.toVector();
        Vector endVec = end.toVector();
        Vector direction = endVec.clone().subtract(startVec).normalize();
        double distance = start.distance(end);
        double step = 0.6;
        int steps = (int) (distance / step);
        Particle.DustOptions dustOptions = new Particle.DustOptions(color, 0.6f);
        for (int i = 0; i <= steps; i++) {
            Vector offset = direction.clone().multiply(step * i);
            Location particleLocation = start.clone().add(offset);
            world.spawnParticle(Particle.DUST, particleLocation, 1, dustOptions);
        }
    }

    private Color interpolateColor(double fraction) {
        if (fraction > 1.0) fraction = 1.0;
        if (fraction < 0.0) fraction = 0.0;

        int red = (int) (Color.GREEN.getRed() + (Color.RED.getRed() - Color.GREEN.getRed()) * fraction);
        int green = (int) (Color.GREEN.getGreen() + (Color.RED.getGreen() - Color.GREEN.getGreen()) * fraction);
        int blue = (int) (Color.GREEN.getBlue() + (Color.RED.getBlue() - Color.GREEN.getBlue()) * fraction);

        return Color.fromRGB(red, green, blue);
    }

    public void notifyPlayersInLineOfSight(Player player, Location targetLocation, float pitch) {
        Vector direction = targetLocation.toVector().subtract(player.getEyeLocation().toVector()).normalize();
        RayTraceResult result = player.getWorld().rayTrace(player.getEyeLocation(), direction, 200.0, FluidCollisionMode.NEVER, true, 0.5, entity -> entity instanceof LivingEntity && !entity.equals(player));
        if (result != null && result.getHitEntity() != null && result.getHitEntity() instanceof LivingEntity livingEntity) {
            if (!(livingEntity instanceof Player p)) {
                return;
            }
            if (Players.isTeam(player, p)) {
                return;
            }
            p.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0f, pitch);
        }
    }

    public void damageEntitiesInLineOfSight(Player player, Location targetLocation, double damage) {
        Vector direction = targetLocation.toVector().subtract(player.getEyeLocation().toVector()).normalize();
        RayTraceResult result = player.getWorld().rayTrace(player.getEyeLocation(), direction, 200.0, FluidCollisionMode.NEVER, true, 0.5, entity -> entity instanceof LivingEntity && !entity.equals(player));
        if (result != null && result.getHitEntity() != null && result.getHitEntity() instanceof LivingEntity livingEntity) {
            if (livingEntity instanceof Player p && Players.isTeam(player, p)) {
                return;
            }
            long currentTime = System.currentTimeMillis();
            long lastPlayTime = lastDamageTime.getOrDefault(livingEntity.getUniqueId(), 0L);
            if (currentTime - lastPlayTime >= 1000L) {
                double originalKnockbackResistance = livingEntity.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).getBaseValue();
                livingEntity.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(1.0);
                livingEntity.damage(damage, player);
                livingEntity.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(originalKnockbackResistance);
                player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
                lastDamageTime.put(livingEntity.getUniqueId(), currentTime);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) throws ReflectiveOperationException {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item != null) {
            Material itemType = item.getType();
            ItemMeta itemMeta = item.getItemMeta();
            if (itemMeta != null) {
                if (itemType == Material.COD && itemMeta.getDisplayName().equals("§9远古守护") && Players.getJobFromPlayer(player).equals("守卫者")) {
                    event.setCancelled(true);
                    elderGuardian(player);
                    Energy.startDecay(player, "§9远古守护", 10);
                } else if (itemType == Material.PRISMARINE_SHARD && itemMeta.getDisplayName().equals("§3穿刺激光") && Players.getJobFromPlayer(player).equals("守卫者")) {
                    event.setCancelled(true);
                    guardianShoot(player);
                    Energy.startDecay(player, "§3穿刺激光", 15);
                } else if (itemType == Material.BEACON && itemMeta.getDisplayName().equals("§c深渊射线") && Players.getJobFromPlayer(player).equals("守卫者")) {
                    event.setCancelled(true);
                    guardianMax(player);
                    Energy.startDecay(player, "§c深渊射线", 5);
                }
            }
        }
    }

    public static void start(Player player) {
        Players.setJobData(player, "guardian");
        player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, Integer.MAX_VALUE, 0, true, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, Integer.MAX_VALUE, 0, true, false, true));
        Energy.createBar(player, Material.COD, "§9远古守护", BarColor.BLUE, 15, 15);
        Energy.createBar(player, Material.PRISMARINE_SHARD, "§3穿刺激光", BarColor.GREEN, 30, 30);
        Energy.createBar(player, Material.BEACON, "§c深渊射线", BarColor.RED, 45, 45);
    }

    public static void equipKits(Player player) {
        Inventory inv = player.getInventory();
        inv.clear();

        player.getInventory().setHelmet(Items.createItem(
                Material.LEATHER_HELMET,
                "§3守卫者头盔",
                Arrays.asList(
                        "", "§b🛡 §f防御 1"),
                null,
                0,
                Color.fromRGB(95, 159, 159),
                1
        ));

        player.getInventory().setChestplate(Items.createItem(
                Material.LEATHER_CHESTPLATE,
                "§3守卫者胸甲",
                Arrays.asList(
                        "", "§b🛡 §f防御 3"),
                null,
                0,
                Color.fromRGB(152, 251, 152),
                1
        ));

        player.getInventory().setLeggings(Items.createItem(
                Material.IRON_LEGGINGS,
                "§3守卫者护腿",
                Arrays.asList(
                        "", "§b🛡 §f防御 5"),
                null,
                0,
                null,
                1
        ));

        player.getInventory().setBoots(Items.createItem(
                Material.IRON_BOOTS,
                "§3守卫者靴子",
                Arrays.asList(
                        "", "§b🛡 §f防御 2"),
                null,
                0,
                null,
                1
        ));

        inv.setItem(0, Items.createItem(
                Material.STONE_SWORD,
                "§f剑",
                Arrays.asList("§7守卫者的剑", "§c❤ §f伤害 5  §6❇§f 攻速 较快"),
                null,
                0,
                null,
                1
        ));

        inv.setItem(1, Items.createItem(
                Material.COD,
                "§9远古守护",
                Arrays.asList(
                        "§7变身远古守卫者",
                        "§7攻击你的玩家会受到伤害",
                        "§7给附近的玩家挖掘疲劳 10 秒",
                        "§7同时自身获得力量和速度 15 秒",
                        "",
                        "§c❤ §f伤害 3  §a❖ §f能量 15  §e ⏱ §f时长 10 秒"
                ),
                null,
                0,
                null,
                1
        ));

        inv.setItem(2, Items.createItem(
                Material.PRISMARINE_SHARD,
                "§3穿刺激光",
                Arrays.asList(
                        "§7持续释放一道激光",
                        "§7对触碰到激光的生物造成伤害",
                        "",
                        "§c❤ §f伤害 3  §a❖ §f能量 30  §e ⏱ §f时长 15 秒"
                ),
                null,
                0,
                null,
                1
        ));

        inv.setItem(3, Items.createItem(
                Material.BEACON,
                "§c深渊射线",
                Arrays.asList(
                        "§7瞄准蓄力5秒",
                        "§7释放深渊射线并造成巨大伤害",
                        "§c❤ §f伤害 ∞  §a❖ §f能量 45  §e ⏱ §f时长 5 秒"
                ),
                null,
                0,
                null,
                1
        ));

        inv.setItem(7, Items.createItem(
                Material.GOLDEN_APPLE,
                "§f金苹果",
                List.of("§7长按§f[右键]§7食用恢复生命"),
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


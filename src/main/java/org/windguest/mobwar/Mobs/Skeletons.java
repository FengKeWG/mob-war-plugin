package org.windguest.mobwar.Mobs;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.windguest.mobwar.Games.*;
import org.windguest.mobwar.Main;

public class Skeletons implements Listener {

    private static final Main plugin = Main.getInstance();
    private static final Random random = new Random();

    private static final ItemStack arrow1 = Items.createItem(Material.ARROW, "§f冲击箭",
            Arrays.asList(
                    "§c手持冲击箭才有冲击效果",
                    "§7骷髅的冲击箭",
                    "§7命中敌人造成冲击",
                    "",
                    "§b✳ §f冷却 2 秒  §e☒ §f上限 6 个"
            ),
            null, 1,
            null, 1);

    private static final ItemStack arrow2 = Items.createTippedArrow("§b缓慢箭",
            Arrays.asList(
                    "§7流浪者的缓慢箭",
                    "§7击中获得 5 秒缓慢 III",
                    "",
                    "§b✳ §f冷却 2 秒  §e☒ §f上限 6 个"
            ),
            PotionEffectType.SLOWNESS, 100, 2);

    private static final ItemStack arrow3 = Items.createTippedArrow("§2剧毒箭",
            Arrays.asList(
                    "§7沼骸的剧毒箭",
                    "§7击中获得 5 秒剧毒 I",
                    "",
                    "§b✳ §f冷却 2 秒  §e☒ §f上限 6 个"
            ),
            PotionEffectType.POISON, 100, 0);

    private void fireCircleArrows(Player player) {
        Location playerLocation = player.getLocation();
        World world = player.getWorld();
        double radius = 1.0;
        PotionEffectType[] debuffs = new PotionEffectType[]{
                PotionEffectType.SLOWNESS,
                PotionEffectType.POISON,
                PotionEffectType.WITHER
        };
        for (int i = 0; i < 64; ++i) {
            double phi = Math.acos(1.0 - 2.0 * ((double) i + 0.5) / 64);
            double theta = Math.PI * (1.0 + Math.sqrt(5.0)) * (double) i;
            double x = Math.cos(theta) * Math.sin(phi);
            double y = Math.sin(theta) * Math.sin(phi);
            double z = Math.cos(phi);
            if (y < 0.2) {
                continue;
            }
            Vector direction = new Vector(x, y, z);
            Arrow arrow = world.spawnArrow(
                    playerLocation.clone().add(direction.clone().multiply(radius)),
                    direction,
                    1.0f,
                    0.0f
            );
            arrow.setShooter(player);
            arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
            PotionEffectType debuff = debuffs[random.nextInt(debuffs.length)];
            int duration = 5 + random.nextInt(4);
            arrow.addCustomEffect(new PotionEffect(debuff, duration * 20, 1), true);
            arrow.setKnockbackStrength(5);
        }
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        if (!Players.getJobFromPlayer(player).equals("骷髅")) return;
        int newSlot = event.getNewSlot();
        ItemStack mainHandItem = player.getInventory().getItem(newSlot);
        if (mainHandItem != null && mainHandItem.hasItemMeta()) {
            ItemMeta mainMeta = mainHandItem.getItemMeta();
            if (!mainMeta.getDisplayName().equals("§f冲击箭")) {
                removePunchEnchantment(player);
                Disguise.undisguise(player);
            }
            if (mainMeta.getDisplayName().equals("§f冲击箭")) {
                addPunchEnchantment(player);
                Disguise.disguise(player, 0);
            } else if (mainMeta.getDisplayName().equals("§b缓慢箭")) {
                Players.setJobData(player, "skeleton_slow");
                Disguise.disguise(player, 1);
            } else if (mainMeta.getDisplayName().equals("§2剧毒箭")) {
                Players.setJobData(player, "skeleton_poison");
                Disguise.disguise(player, 2);
            }
        }
    }

    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if (Players.getJobFromPlayer(player).equals("骷髅")) {
            event.setCancelled(true);
        }
    }

    public void addPunchEnchantment(Player player) {
        Inventory inventory = player.getInventory();
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta.hasDisplayName() && meta.getDisplayName().equals("§f弓")) {
                    item.addUnsafeEnchantment(Enchantment.PUNCH, 2);
                    return;
                }
            }
        }
    }

    public void removePunchEnchantment(Player player) {
        Inventory inventory = player.getInventory();
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta.hasDisplayName() && meta.getDisplayName().equals("§f弓")) {
                    if (item.containsEnchantment(Enchantment.PUNCH)) {
                        item.removeEnchantment(Enchantment.PUNCH);
                    }
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof SkeletonHorse) {
            if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                event.setCancelled(true);
            }
        }
    }

    public void summonSkeletonHorse(Player player) {
        Location location = player.getLocation();
        Location horseLocation = location.clone();
        horseLocation.setY(location.getY() + 1);
        SkeletonHorse skeletonHorse = player.getWorld().spawn(horseLocation, SkeletonHorse.class);
        Entities.addEntity(skeletonHorse, player, 0);
        skeletonHorse.setMaxHealth(20);
        skeletonHorse.setHealth(20);
        skeletonHorse.setRemoveWhenFarAway(false);
        skeletonHorse.setAdult();
        skeletonHorse.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 20, 0, true, false, true));
        skeletonHorse.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 20 * 20, 0, true, false, true));
        ItemStack saddle = new ItemStack(Material.SADDLE);
        skeletonHorse.getInventory().setSaddle(saddle);
        skeletonHorse.setTamed(true);
        skeletonHorse.setOwner(player);
        Bukkit.getScheduler().runTask(plugin, () -> skeletonHorse.addPassenger(player));
        new BukkitRunnable() {
            public void run() {
                Entities.removeEntity(skeletonHorse);
            }
        }.runTaskLater(plugin, 20 * 20);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof SkeletonHorse skeletonHorse) {
            if (Entities.getEntityOwner(skeletonHorse) != null) {
                event.getDrops().clear();
                event.setDroppedExp(0);
            }
        }
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (player.isInsideVehicle() && player.getVehicle() instanceof SkeletonHorse skeletonHorse && Entities.getEntityOwner(skeletonHorse) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item != null) {
            Material itemType = item.getType();
            ItemMeta itemMeta = item.getItemMeta();
            if (itemType == Material.SUGAR && itemMeta.getDisplayName().equals("§f弓箭护体") && Players.getJobFromPlayer(player).equals("骷髅")) {
                event.setCancelled(true);
                fireCircleArrows(player);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        fireCircleArrows(player);
                    }
                }.runTaskLater(plugin, 10L);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        fireCircleArrows(player);
                    }
                }.runTaskLater(plugin, 20L);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        fireCircleArrows(player);
                    }
                }.runTaskLater(plugin, 30L);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        fireCircleArrows(player);
                    }
                }.runTaskLater(plugin, 40L);
                Energy.startDecay(player, "§f弓箭护体", 3);
            } else if (itemType == Material.BONE && itemMeta.getDisplayName().equals("§f骷髅骑手") && Players.getJobFromPlayer(player).equals("骷髅")) {
                event.setCancelled(true);
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 20 * 20, 0, true, false, true));
                summonSkeletonHorse(player);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SKELETON_HORSE_DEATH, 1.0f, 1.0f);
                Location location = player.getLocation();
                player.getWorld().strikeLightningEffect(location);
                Energy.startDecay(player, "§f骷髅骑手", 20);
            }
        }
    }

    public static void start(Player player) {
        Items.giveArrow(player, arrow1, 2, 6);
        Items.giveArrow(player, arrow2, 2, 6);
        Items.giveArrow(player, arrow3, 2, 6);
        Players.setJobData(player, "skeleton");
        Energy.createBar(player, Material.SUGAR, "§f弓箭护体", BarColor.WHITE, 15, 15);
        Energy.createBar(player, Material.BONE, "§f骷髅骑手", BarColor.WHITE, 30, 30);
    }

    public static void equipKits(Player player) {
        Inventory inv = player.getInventory();
        inv.clear();

        player.getInventory().setHelmet(Items.createItem(
                Material.IRON_HELMET,
                "§f骷髅帽子",
                Arrays.asList(
                        "",
                        "§b🛡 §f防御 2"
                ),
                null,
                0,
                null,
                1
        ));

        player.getInventory().setChestplate(Items.createItem(
                Material.IRON_CHESTPLATE,
                "§f骷髅胸甲",
                Arrays.asList(
                        "",
                        "§b🛡 §f防御 6"
                ),
                null,
                0,
                null,
                1
        ));

        player.getInventory().setLeggings(Items.createItem(
                Material.LEATHER_LEGGINGS,
                "§f骷髅护腿",
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
                Material.IRON_BOOTS,
                "§f骷髅靴子",
                Arrays.asList(
                        "",
                        "§b🛡 §f防御 2"
                ),
                null,
                0,
                null,
                1
        ));

        inv.setItem(0, arrow1);

        inv.setItem(1, arrow2);

        inv.setItem(2, arrow3);

        inv.setItem(40, Items.createItem(
                Material.BOW,
                "§f弓",
                Arrays.asList(
                        "§7骷髅的弓",
                        "§7通过切换物品栏选择箭矢",
                        "§7长按§f[右键]§7蓄力",
                        "§7消耗箭矢进行射击",
                        "",
                        "§c❤ §f伤害 1 - 14"
                ),
                Enchantment.POWER,
                1,
                null,
                1
        ));

        inv.setItem(3, Items.createItem(
                Material.SUGAR,
                "§f弓箭护体",
                Arrays.asList(
                        "§7立即向四周发射不同效果的箭矢",
                        "",
                        "§a❖ §f能量 15"
                ),
                null,
                0,
                null,
                1
        ));

        inv.setItem(4, Items.createItem(
                Material.BONE,
                "§f骷髅骑手",
                Arrays.asList(
                        "§7骑乘一匹骷髅马",
                        "§7同时获得抗性提升 I",
                        "",
                        "§a❖ §f能量 30  §e ⏱ §f时长 20 秒"
                ),
                null,
                0,
                null,
                1
        ));

        inv.setItem(7, Items.createItem(
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

        inv.setItem(8, Items.createItem(
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
    }
}
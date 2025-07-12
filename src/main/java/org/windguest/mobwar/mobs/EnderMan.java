package org.windguest.mobwar.mobs;

import java.util.*;

import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.windguest.mobwar.games.*;
import org.windguest.mobwar.Main;

public class EnderMan implements Listener {

    private static final Random random = new Random();
    private static final Main plugin = Main.getInstance();

    public static void superEnderMan(Player player) {
        Disguise.disguise(player, 0);
        Players.setJobData(player, "enderman_super");
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_SCREAM, 1.0f, 1.0f);
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 15 * 20, 0, true, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 15 * 20, 0, true, false, true));
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Players.setJobData(player, "enderman");
            Disguise.undisguise(player);
        }, 15 * 20L);
    }

    public static void endEnderMan(Player player) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        Players.cancelPlayerTasks(player, Players.TaskTag.ITEM_GIVE);
        Items.giveItemsInTimes(player, "§f末影珍珠", Material.ENDER_PEARL, 1, 5);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Players.cancelPlayerTasks(player, Players.TaskTag.ITEM_GIVE);
            Items.giveItemsInTimes(player, "§f末影珍珠", Material.ENDER_PEARL, 5, 5);
        }, 15 * 20L);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (Players.getJobFromPlayer(player).equals("末影人")) {
            if (player.getLocation().getBlock().getType() == Material.WATER
                    || player.getLocation().getBlock().getType() == Material.LAVA) {
                player.damage(10);
                randomTeleport(player);
            }
        }
    }

    private void randomTeleport(Player player) {
        List<Location> predefinedLocations = Arrays.asList(
                new Location(player.getWorld(), -98.0, 88.0, 35.0),
                new Location(player.getWorld(), -13.0, 87.0, -88.0),
                new Location(player.getWorld(), 55.0, 89.0, -74.0),
                new Location(player.getWorld(), -44.0, 59.0, 50.0),
                new Location(player.getWorld(), -4.0, 30.0, 15.0),
                new Location(player.getWorld(), 101.0, 87.0, 0.0));
        Location baseLocation = predefinedLocations.get(random.nextInt(predefinedLocations.size()));
        int dx = random.nextInt(11) - 5;
        int dz = random.nextInt(11) - 5;
        Location teleportLocation = baseLocation.clone().add(dx, 0.0, dz);
        player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation(), 100, 0.5, 1.0, 0.5, 0.5);
        player.teleport(teleportLocation);
        player.getWorld().playSound(teleportLocation, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        player.getWorld().playSound(teleportLocation, Sound.ENTITY_ENDERMAN_HURT, 1.0f, 1.0f);
        player.getWorld().spawnParticle(Particle.PORTAL, teleportLocation, 100, 0.5, 1.0, 0.5, 0.5);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player player && event.getDamager() instanceof Projectile) {
            if (Players.getJobData(player).equals("enderman_super")) {
                event.setCancelled(true);
            }
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

            if (itemType == Material.ENDER_PEARL && (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR)
                    && Players.getJobFromPlayer(player).equals("末影人")) {
                int totalPearls = Items.countItems(player, Material.ENDER_PEARL);
                if (totalPearls == 1) {
                    event.setCancelled(true);
                    Items.replaceItem(player, "§f末影珍珠", Material.BARRIER);
                    player.launchProjectile(EnderPearl.class);
                }

            }
            if (itemType == Material.ENDER_EYE && itemMeta.getDisplayName().equals("§9化为末影") && Players.getJobFromPlayer(player).equals("末影人")) {
                event.setCancelled(true);
                endEnderMan(player);
                Energy.startDecay(player, "§9化为末影", 15);

            } else if (itemType == Material.END_CRYSTAL && itemMeta.getDisplayName().equals("§c狂暴") && Players.getJobFromPlayer(player).equals("末影人")) {
                event.setCancelled(true);
                superEnderMan(player);
                Energy.startDecay(player, "§c狂暴", 15);
            }
        }
    }

    public static void start(Player player) {
        Players.setJobData(player, "enderman");
        Energy.createBar(player, Material.ENDER_EYE, "§9化为末影", BarColor.BLUE, 10, 10);
        Energy.createBar(player, Material.END_CRYSTAL, "§c狂暴", BarColor.RED, 20, 20);
        Items.giveItemsInTimes(player, "§f末影珍珠", Material.ENDER_PEARL, 5, 5);
    }

    public static void equipKits(Player player) {
        Inventory inv = player.getInventory();
        inv.clear();

        player.getInventory().setHelmet(Items.createItem(
                Material.IRON_HELMET,
                "§9末影人头盔",
                Arrays.asList(
                        "§b弹射物保护 I",
                        "",
                        "§b🛡 §f防御 2"
                ),
                Enchantment.PROJECTILE_PROTECTION, 1,
                null,
                1
        ));

        player.getInventory().setChestplate(Items.createItem(
                Material.LEATHER_CHESTPLATE,
                "§9末影人胸甲",
                Arrays.asList(
                        "§b弹射物保护 I",
                        "",
                        "§b🛡 §f防御 3"
                ),
                Enchantment.PROJECTILE_PROTECTION, 1,
                Color.fromRGB(0, 0, 0),
                1
        ));

        player.getInventory().setLeggings(Items.createItem(
                Material.IRON_LEGGINGS,
                "§9末影人护腿",
                Arrays.asList(
                        "§b弹射物保护 I",
                        "",
                        "§b🛡 §f防御 5"
                ),
                Enchantment.PROJECTILE_PROTECTION, 1,
                Color.fromRGB(0, 0, 0),
                1
        ));

        player.getInventory().setBoots(Items.createItem(
                Material.LEATHER_BOOTS,
                "§9末影人靴子",
                Arrays.asList(
                        "§b弹射物保护 I",
                        "",
                        "§b🛡 §f防御 1"
                ),
                Enchantment.PROJECTILE_PROTECTION, 1,
                Color.fromRGB(0, 0, 0),
                1
        ));

        inv.setItem(0, Items.createItem(
                Material.IRON_SWORD,
                "§f剑",
                Arrays.asList(
                        "§7末影人的剑",
                        "",
                        "§c❤ §f伤害 6  §6❇ §f攻速 较快"
                ),
                null,
                0,
                null,
                1
        ));

        inv.setItem(1, Items.createItem(
                Material.ENDER_PEARL,
                "§f末影珍珠",
                Arrays.asList(
                        "§7按§f[右键]§7使用末影珍珠",
                        "§7末影珍珠随时间补充",
                        "",
                        "§b✳ §f冷却 5 秒  §e ☒ §f上限 5 个"
                ),
                null,
                0,
                null,
                1
        ));

        inv.setItem(2, Items.createItem(
                Material.ENDER_EYE,
                "§9化为末影",
                Arrays.asList(
                        "§7快速恢复末影珍珠",
                        "",
                        "§a❖ §f能量 10  §e ⏱ §f时长 15 秒"
                ),
                null,
                0,
                null,
                1
        ));

        inv.setItem(3, Items.createItem(
                Material.END_CRYSTAL,
                "§c狂暴",
                Arrays.asList(
                        "§7获得力量 I 与速度 I",
                        "§7并免疫所有的远程武器伤害",
                        "",
                        "§a❖ §f能量 20  §e ⏱ §f时长 15 秒"
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
                Material.CHORUS_FRUIT,
                "§f紫菘果",
                Arrays.asList(
                        "§7这是一些紫菘果",
                        "§7长按§f[右键]§7恢复饱食度"
                ),
                null,
                0,
                null,
                16
        ));
    }
}


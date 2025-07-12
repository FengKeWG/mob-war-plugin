package org.windguest.mobwar.mobs;

import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.windguest.mobwar.games.Disguise;
import org.windguest.mobwar.games.Energy;
import org.windguest.mobwar.games.Items;
import org.windguest.mobwar.games.Players;
import org.windguest.mobwar.Main;

import java.util.Arrays;
import java.util.List;

public class Drowned implements Listener {

    private static final Main plugin = Main.getInstance();

    @EventHandler
    public void onTridentHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Trident trident) {
            if (trident.getShooter() instanceof Player shooter) {
                if (!Players.getJobFromPlayer(shooter).equals("溺尸") || Players.getSkill(shooter) != 1) {
                    return;
                }
                Entity hitEntity = event.getHitEntity();
                World world = trident.getWorld();
                Location strikeLocation;
                if (hitEntity != null) {
                    strikeLocation = hitEntity.getLocation();
                    world.strikeLightning(strikeLocation);
                } else if (event.getHitBlock() != null) {
                    strikeLocation = event.getHitBlock().getLocation();
                    world.strikeLightning(strikeLocation);
                } else {
                    return;
                }
                for (Entity entity : world.getNearbyEntities(strikeLocation, 3.0, 3.0, 3.0)) {
                    if (entity.equals(shooter))
                        return;
                    if (entity instanceof Player p) {
                        if (Players.isTeam(shooter, p))
                            return;
                    }
                    if (entity instanceof LivingEntity livingEntity) {
                        livingEntity.damage(10, shooter);
                    }
                }
            }
        }
    }

    public void setLoyalty(Player player, int level) {
        Inventory inventory = player.getInventory();
        for (ItemStack item : inventory.getContents()) {
            if (item != null) {
                if (item.getType() == Material.TRIDENT) {
                    item.removeEnchantment(Enchantment.LOYALTY);
                    item.addUnsafeEnchantment(Enchantment.LOYALTY, level);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item != null) {
            Material itemType = item.getType();
            ItemMeta itemMeta = item.getItemMeta();
            if (itemMeta != null) {
                if (itemType == Material.NAUTILUS_SHELL && itemMeta.getDisplayName().equals("§9忠诚")
                        && Players.getJobFromPlayer(player).equals("溺尸")) {
                    event.setCancelled(true);
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_DROWNED_AMBIENT, 1.0f, 1.0f);
                    setLoyalty(player, 10);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        setLoyalty(player, 15);
                    }, 20L);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        setLoyalty(player, 15);
                    }, 40L);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        setLoyalty(player, 15);
                    }, 60L);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        setLoyalty(player, 15);
                    }, 80L);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        setLoyalty(player, 15);
                    }, 100L);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        setLoyalty(player, 15);
                    }, 120L);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        setLoyalty(player, 4);
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            setLoyalty(player, 4);
                        }, 20L);
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            setLoyalty(player, 4);
                        }, 40L);
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            setLoyalty(player, 4);
                        }, 60L);
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            setLoyalty(player, 4);
                        }, 80L);
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            setLoyalty(player, 4);
                        }, 100L);
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            setLoyalty(player, 4);
                        }, 120L);
                    }, 60 * 20L);
                    Energy.startDecay(player, "§9忠诚", 60);
                } else if (itemType == Material.LIGHTNING_ROD && itemMeta.getDisplayName().equals("§c天雷")
                        && Players.getJobFromPlayer(player).equals("溺尸")) {
                    event.setCancelled(true);
                    Players.setJobData(player, "drowned_light");
                    Disguise.disguise(player, 0);
                    Players.addSkill(player, 1);
                    player.getWorld().strikeLightning(player.getLocation());
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        Players.removeSkill(player);
                        Disguise.undisguise(player);
                        Players.setJobData(player, "drowned");
                    }, 30 * 20L);
                    Energy.startDecay(player, "§c天雷", 30);
                }
            }
        }
    }

    public static void start(Player player) {
        Players.setJobData(player, "drowned");
        player.addPotionEffect(
                new PotionEffect(PotionEffectType.DOLPHINS_GRACE, Integer.MAX_VALUE, 0, true, false, true));
        player.addPotionEffect(
                new PotionEffect(PotionEffectType.WATER_BREATHING, Integer.MAX_VALUE, 0, true, false, true));
        Energy.createBar(player, Material.NAUTILUS_SHELL, "§9忠诚", BarColor.BLUE, 15, 15);
        Energy.createBar(player, Material.LIGHTNING_ROD, "§c天雷", BarColor.RED, 30, 30);
    }

    public static void equipKits(Player player) {
        Inventory inv = player.getInventory();
        inv.clear();

        player.getInventory().setHelmet(Items.createItem(
                Material.LEATHER_HELMET,
                "§3溺尸头盔",
                Arrays.asList(
                        "", "§b🛡 §f防御 1"),
                null,
                0,
                Color.fromRGB(50, 80, 90),
                1));

        player.getInventory().setChestplate(Items.createItem(
                Material.IRON_CHESTPLATE,
                "§3溺尸胸甲",
                Arrays.asList(
                        "", "§b🛡 §f防御 6"),
                null,
                0,
                Color.fromRGB(95, 159, 159),
                1));

        player.getInventory().setLeggings(Items.createItem(
                Material.LEATHER_LEGGINGS,
                "§3溺尸护腿",
                Arrays.asList(
                        "", "§b🛡 §f防御 2"),
                null,
                0,
                Color.fromRGB(55, 95, 100),
                1));

        player.getInventory().setBoots(Items.createItem(
                Material.LEATHER_BOOTS,
                "§3溺尸靴子",
                Arrays.asList(
                        "", "§b🛡 §f防御 1"),
                null,
                0,
                Color.fromRGB(65, 100, 110),
                1));

        inv.setItem(0, Items.createItem(
                Material.TRIDENT,
                "§f三叉戟",
                Arrays.asList(
                        "§7溺尸的三叉戟",
                        "§7丢出后自动回到身边",
                        "",
                        "§c❤ §f伤害 8"),
                Enchantment.LOYALTY,
                3,
                null,
                1));

        inv.setItem(1, Items.createItem(
                Material.TRIDENT,
                "§f三叉戟",
                Arrays.asList(
                        "§7溺尸的三叉戟",
                        "§7丢出后自动回到身边",
                        "",
                        "§c❤ §f伤害 8"),
                Enchantment.LOYALTY,
                3,
                null,
                1));

        inv.setItem(2, Items.createItem(
                Material.TRIDENT,
                "§f三叉戟",
                Arrays.asList(
                        "§7溺尸的三叉戟",
                        "§7丢出后自动回到身边",
                        "",
                        "§c❤ §f伤害 8"),
                Enchantment.LOYALTY,
                3,
                null,
                1));

        inv.setItem(3, Items.createItem(
                Material.FISHING_ROD,
                "§f钓鱼竿",
                Arrays.asList(
                        "§7溺尸的钓鱼竿",
                        "",
                        "§c❤ §f伤害 4"),
                null,
                0,
                null,
                1));

        inv.setItem(4, Items.createItem(
                Material.NAUTILUS_SHELL,
                "§9忠诚",
                Arrays.asList(
                        "§7加快三叉戟的回收速度",
                        "",
                        "§a❖ §f能量 15  §e ⏱ §f时长 60 秒"),
                null,
                0,
                null,
                1));

        inv.setItem(5, Items.createItem(
                Material.LIGHTNING_ROD,
                "§c天雷",
                Arrays.asList(
                        "§7三叉戟击中生物或者方块",
                        "§7会产生闪电并造成伤害",
                        "",
                        "§c❤ §f伤害 10  §a❖ §f能量 30  §e ⏱ §f时长 30 秒"),
                null,
                0,
                null,
                1));

        inv.setItem(7, Items.createItem(
                Material.GOLDEN_APPLE,
                "§f金苹果",
                List.of("§7长按§f[右键]§7食用恢复生命"),
                null,
                0,
                null,
                3));

        inv.setItem(8, Items.createItem(
                Material.APPLE,
                "§f苹果",
                Arrays.asList(
                        "§7这是一些苹果",
                        "§7长按§f[右键]§7恢复饱食度"),
                null,
                0,
                null,
                16));
    }
}

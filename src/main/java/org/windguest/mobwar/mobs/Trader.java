package org.windguest.mobwar.mobs;

import java.util.*;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.windguest.mobwar.games.*;
import org.windguest.mobwar.Main;

public class Trader implements Listener {

    private static final Main plugin = Main.getInstance();

    private final List<Material> items = Arrays.asList(Material.BRAIN_CORAL_BLOCK,
            Material.BUBBLE_CORAL_BLOCK, Material.FIRE_CORAL_BLOCK, Material.HORN_CORAL_BLOCK,
            Material.TUBE_CORAL_BLOCK, Material.FERN, Material.VINE, Material.DANDELION, Material.POPPY,
            Material.BLUE_ORCHID, Material.ALLIUM, Material.AZURE_BLUET, Material.RED_TULIP,
            Material.ORANGE_TULIP, Material.PINK_TULIP, Material.WHITE_TULIP, Material.OXEYE_DAISY,
            Material.CORNFLOWER, Material.WHEAT_SEEDS, Material.BEETROOT_SEEDS, Material.PUMPKIN_SEEDS,
            Material.MELON_SEEDS, Material.RED_DYE, Material.YELLOW_DYE, Material.ORANGE_DYE,
            Material.LIME_DYE, Material.GREEN_DYE, Material.CYAN_DYE, Material.LIGHT_BLUE_DYE,
            Material.PURPLE_DYE, Material.MAGENTA_DYE, Material.PINK_DYE, Material.GRAY_DYE,
            Material.LIGHT_GRAY_DYE, Material.LAPIS_LAZULI, Material.COCOA_BEANS, Material.BONE_MEAL,
            Material.INK_SAC, Material.OAK_SAPLING, Material.BIRCH_SAPLING, Material.SPRUCE_SAPLING,
            Material.DARK_OAK_SAPLING, Material.ACACIA_SAPLING, Material.JUNGLE_SAPLING,
            Material.MANGROVE_PROPAGULE, Material.CHERRY_SAPLING, Material.NAUTILUS_SHELL,
            Material.BLUE_ICE, Material.PUFFERFISH_BUCKET, Material.TROPICAL_FISH_BUCKET, Material.PODZOL,
            Material.PACKED_ICE, Material.GUNPOWDER, Material.SUGAR_CANE, Material.SAND,
            Material.LILY_OF_THE_VALLEY, Material.RED_SAND, Material.LILY_PAD, Material.POINTED_DRIPSTONE,
            Material.MOSS_BLOCK, Material.ROOTED_DIRT, Material.PUMPKIN, Material.SEA_PICKLE,
            Material.GLOWSTONE, Material.KELP, Material.CACTUS, Material.SLIME_BALL, Material.SMALL_DRIPLEAF);

    private static final ItemStack arrow = Items.createItem(
            Material.ARROW,
            "§f箭",
            List.of(
                    "§7流浪商人的箭"
            ),
            null, 0, null, 1
    );

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (player.isInsideVehicle() && player.getVehicle() instanceof Llama llama && Entities.getEntityOwner(llama) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDismount(EntityDismountEvent event) {
        Entity entity = event.getDismounted();
        if (entity instanceof Llama llama && Entities.getEntityOwner(llama) != null) {
            event.setCancelled(true);
        }
    }

    private void removeInvisibilityAndDarkness(Player player) {
        player.getActivePotionEffects().stream().filter(effect -> effect.getType() == PotionEffectType.INVISIBILITY || effect.getType() == PotionEffectType.DARKNESS).forEach(effect -> player.removePotionEffect(effect.getType()));
    }

    private void freezePlayersInRadius(Player player) {
        Players.setJobData(player, "trader_freeze");
        Disguise.disguise(player, 0);
        for (Entity entity : player.getNearbyEntities(5, 5, 5)) {
            if (!(entity instanceof LivingEntity)) continue;
            Llama llama = entity.getWorld().spawn(entity.getLocation(), Llama.class);
            llama.setInvulnerable(true);
            llama.setCollidable(false);
            llama.addPassenger(entity);
            llama.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 5 * 20, 1, false, false, true));
            llama.setLeashHolder(player);
            Entities.addEntity(llama, player, 0);
            new BukkitRunnable() {
                public void run() {
                    Entities.removeEntity(llama);
                    Players.setJobData(player, "trader");
                    Disguise.undisguise(player);
                }
            }.runTaskLater(plugin, 5 * 20);
        }
    }

    private void randomizeInventoryInRadius(Player player) {
        for (Player p : player.getWorld().getNearbyPlayers(player.getLocation(), 5, 5)) {
            if (Players.isTeam(player, p)) continue;
            if (p.equals(player)) continue;
            PlayerInventory inv = p.getInventory();
            ItemStack[] armorContents = inv.getArmorContents();
            ItemStack offHand = inv.getItemInOffHand();
            List<ItemStack> mainInventoryItems = new ArrayList<>(Arrays.asList(inv.getStorageContents()));
            mainInventoryItems.removeIf(Objects::isNull);
            Random rand = new Random();
            int newItemsCount = rand.nextInt(16) + 10;
            List<Material> shuffledItems = new ArrayList<>(items);
            Collections.shuffle(shuffledItems, rand);
            for (int i = 0; i < newItemsCount && i < shuffledItems.size(); i++) {
                Material mat = shuffledItems.get(i);
                int maxStack = mat.getMaxStackSize();
                int amount = rand.nextInt(maxStack) + 1;
                ItemStack newItem = new ItemStack(mat, amount);
                ItemMeta meta = newItem.getItemMeta();
                if (meta != null) {
                    List<String> lore = new ArrayList<>();
                    lore.add("§7来自流浪商人 " + player.getName() + " 的馈赠");
                    meta.setLore(lore);
                    newItem.setItemMeta(meta);
                }
                mainInventoryItems.add(newItem);
            }
            Collections.shuffle(mainInventoryItems, rand);
            ItemStack[] newMainContents = new ItemStack[inv.getStorageContents().length];
            for (int i = 0; i < newMainContents.length; i++) {
                if (i < mainInventoryItems.size()) {
                    newMainContents[i] = mainInventoryItems.get(i);
                } else {
                    newMainContents[i] = null;
                }
            }
            inv.setStorageContents(newMainContents);
            inv.setArmorContents(armorContents);
            inv.setItemInOffHand(offHand);
        }
    }


    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (!Players.getJobFromPlayer(player).equals("流浪商人")) return;
            removeInvisibilityAndDarkness(player);
        }
    }

    @EventHandler
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item.getType() == Material.POTION) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName() && ChatColor.stripColor(meta.getDisplayName()).equals("没有夜晚就创造夜晚的隐身药水")) {
                event.setCancelled(true);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WANDERING_TRADER_DISAPPEARED, 1.0f, 1.0f);
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false, true));
                player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, Integer.MAX_VALUE, 0, false, false, true));
            }
        } else if (item.getType() == Material.MILK_BUCKET) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName() && ChatColor.stripColor(meta.getDisplayName()).equals("特效牛奶")) {
                event.setCancelled(true);
                removeInvisibilityAndDarkness(player);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WANDERING_TRADER_REAPPEARED, 1.0f, 1.0f);
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
                if (itemType == Material.PUFFERFISH && itemMeta.getDisplayName().equals("§a盛情款待") && Players.getJobFromPlayer(player).equals("流浪商人")) {
                    event.setCancelled(true);
                    removeInvisibilityAndDarkness(player);
                    randomizeInventoryInRadius(player);
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WANDERING_TRADER_YES, 1.0f, 1.0f);
                    Energy.startDecay(player, "§a盛情款待", 5);
                } else if (itemType == Material.LEAD && itemMeta.getDisplayName().equals("§f羊驼战术") && Players.getJobFromPlayer(player).equals("流浪商人")) {
                    event.setCancelled(true);
                    freezePlayersInRadius(player);
                    removeInvisibilityAndDarkness(player);
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WANDERING_TRADER_YES, 1.0f, 1.0f);
                    Energy.startDecay(player, "§f羊驼战术", 5);
                }
            }
        }
    }


    public static void start(Player player) {
        Players.setJobData(player, "trader");
        Items.giveArrow(player, arrow, 3, 12);
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 0, true, false, true));
        Energy.createBar(player, Material.PUFFERFISH, "§a盛情款待", BarColor.GREEN, 10, 10);
        Energy.createBar(player, Material.LEAD, "§f羊驼战术", BarColor.WHITE, 20, 20);
    }

    public static void equipKits(Player player) {
        Inventory inv = player.getInventory();
        inv.clear();

        inv.setItem(0, Items.createItem(
                Material.DIAMOND_SWORD,
                "§f剑",
                Arrays.asList(
                        "§7流浪商人的剑",
                        "",
                        "§c❤ §f伤害 7  §6❇§f 攻速 较快"
                ),
                null,
                0,
                null,
                1
        ));

        inv.setItem(1, Items.createItem(
                Material.WOODEN_AXE,
                "§f斧头",
                Arrays.asList(
                        "§7流浪商人的斧头。",
                        "",
                        "§c❤ §f伤害 8  §6❇§f 攻速 较慢"
                ),
                Enchantment.SHARPNESS,
                1,
                null,
                1
        ));

        inv.setItem(2, Items.createItem(
                Material.BOW,
                "§f弓",
                Arrays.asList(
                        "§7流浪商人的弓",
                        "§7长按§f[右键]§7蓄力",
                        "§7消耗箭矢进行射击",
                        "",
                        "§c❤ §f伤害 1 - 10"
                ),
                null,
                0,
                null,
                1
        ));

        inv.setItem(3, arrow);

        inv.setItem(4, Items.createItem(
                Material.POTION,
                "§f没有夜晚就创造夜晚的隐身药水",
                Arrays.asList(
                        "§7流浪商人的隐身药水",
                        "§7长按§f[右键]饮用",
                        "§7获得隐身与黑暗效果"
                ),
                null,
                0,
                null,
                1
        ));

        inv.setItem(5, Items.createItem(
                Material.MILK_BUCKET,
                "§f特效牛奶",
                Arrays.asList(
                        "§7流浪商人的牛奶",
                        "§f长按[右键]§7饮用",
                        "§7消除隐身与黑暗效果"
                ),
                null,
                0,
                null,
                1
        ));

        inv.setItem(6, Items.createItem(
                Material.PUFFERFISH,
                "§a盛情款待",
                Arrays.asList(
                        "§7打乱半径 5 格内玩家的背包",
                        "§7并塞满流浪商人的馈赠",
                        "",
                        "§c§a❖ §f能量 10"
                ),
                null,
                0,
                null,
                1
        ));

        inv.setItem(7, Items.createItem(
                Material.LEAD,
                "§f羊驼战术",
                Arrays.asList(
                        "§7将半径 5 格内的玩家定身在羊驼上",
                        "",
                        "§a❖ §f能量 20  §e ⏱ §f时长 5 秒"
                ),
                null,
                0,
                null,
                1
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

        inv.setItem(9, Items.createItem(
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
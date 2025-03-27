package org.windguest.mobwar.Games;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.windguest.mobwar.Main;

import java.util.Arrays;
import java.util.List;

public class Items {

    private static final Main plugin = Main.getInstance();

    public static void giveArrow(Player player, ItemStack arrow, int delay, int maxNumbers) {
        BukkitTask task = new BukkitRunnable() {
            public void run() {
                int tot = countItems(player, arrow.getItemMeta().getDisplayName());
                if (tot < maxNumbers) {
                    player.getInventory().addItem(arrow);
                }
            }
        }.runTaskTimer(plugin, delay * 20L, delay * 20L);
        Players.addTaskToPlayer(player, task);
    }

    public static void giveItemsInTimes(Player player, String name, Material material, int delay, int maxNumbers) {
        BukkitTask task = new BukkitRunnable() {
            public void run() {
                int tot = countItems(player, material);
                if (tot > maxNumbers) {
                    keepOnlyOne(player, name);
                } else if (tot == 0) {
                    replaceItem(player, name, material);
                } else if (tot < maxNumbers) {
                    for (ItemStack item : player.getInventory().getContents()) {
                        if (item != null && item.getType() == material) {
                            item.setAmount(item.getAmount() + 1);
                            break;
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, delay * 20L, delay * 20L);
        Players.addTaskToPlayer(player, task);
    }

    public static int countItems(Player player, String name) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType() == Material.BARRIER || !item.hasItemMeta()) continue;
            ItemMeta meta = item.getItemMeta();
            if (meta.getDisplayName().equals(name)) {
                count += item.getAmount();
            }
        }
        return count;
    }

    public static int countItems(Player player, Material material) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType() != material) continue;
            count += item.getAmount();
        }
        return count;
    }

    public static void replaceItem(Player player, String name, Material material) {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack item = inventory.getItem(slot);
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName()) {
                String displayName = meta.getDisplayName();
                if (displayName.equals(name)) {
                    ItemStack newItem = new ItemStack(material, item.getAmount());
                    ItemMeta newMeta = newItem.getItemMeta();
                    if (meta.hasDisplayName()) {
                        newMeta.setDisplayName(meta.getDisplayName());
                    }
                    if (meta.hasLore()) {
                        newMeta.setLore(meta.getLore());
                    }
                    newMeta.setUnbreakable(true);
                    newMeta.addItemFlags(ItemFlag.values());
                    newItem.setItemMeta(newMeta);
                    inventory.setItem(slot, newItem);
                    break;
                }
            }
        }
    }

    public static void reduceItemsInInventory(Player player, Material material) {
        PlayerInventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() == material) {
                if (item.getAmount() > 1) {
                    item.setAmount(item.getAmount() - 1);
                } else {
                    inventory.setItem(i, null);
                }
                break;
            }
        }
    }

    public static void keepOnlyOne(Player player, String name) {
        PlayerInventory inventory = player.getInventory();
        boolean itemKept = false;
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.hasDisplayName()) {
                    String displayName = meta.getDisplayName();
                    if (displayName.equals(name)) {
                        if (!itemKept) {
                            item.setAmount(1);
                            inventory.setItem(i, item);
                            itemKept = true;
                        } else {
                            inventory.setItem(i, null);
                        }
                    }
                }
            }
        }
    }

    public static ItemStack createItem(Material material, String displayName, List<String> lore,
                                       Enchantment enchantment, int enchantLevel, Color color,
                                       int amount) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof LeatherArmorMeta leatherMeta && color != null) {
            leatherMeta.setColor(color);
        }
        meta.setDisplayName(displayName);
        meta.setLore(lore);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.values());
        if (enchantment != null) {
            meta.addEnchant(enchantment, enchantLevel, true);
        }
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createTippedArrow(String displayName, List<String> lore, PotionEffectType effectType, int duration, int level) {
        ItemStack arrow = new ItemStack(Material.TIPPED_ARROW);
        PotionMeta meta = (PotionMeta) arrow.getItemMeta();
        meta.addCustomEffect(new PotionEffect(effectType, duration, level), true);
        meta.setDisplayName(displayName);
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.values());
        arrow.setItemMeta(meta);
        return arrow;
    }

    public static ItemStack createGoldIngot(int amount) {
        return createItem(Material.GOLD_INGOT, "§e金锭",
                List.of(
                        "§7可以用来与猪灵兑换物品"
                ),
                Enchantment.UNBREAKING, 1,
                null, amount);
    }

    public static ItemStack createEmerald(int amount) {
        return Items.createItem(Material.EMERALD, "§2绿宝石",
                Arrays.asList(
                        "§7生物间的通用货币。可以用来与村民兑换物品",
                        "§7按§f[右键]§7使用并悬赏一位玩家"
                ),
                Enchantment.UNBREAKING, 1,
                null, amount);
    }
}

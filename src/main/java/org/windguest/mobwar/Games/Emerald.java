package org.windguest.mobwar.Games;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

public class Emerald {

    public static void dropEmerald(Location location, int amount) {
        ItemStack emerald = Items.createEmerald(amount);
        World world = location.getWorld();
        if (world != null) {
            world.dropItemNaturally(location, emerald);
        }
    }

    public static void giveEmerald(Player player, int amount) {
        ItemStack emerald = Items.createEmerald(amount);
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack currentItem = inventory.getItem(i);
            if (currentItem != null && currentItem.getType() == emerald.getType()) {
                int newAmount = currentItem.getAmount() + amount;
                if (newAmount <= currentItem.getMaxStackSize()) {
                    currentItem.setAmount(newAmount);
                    inventory.setItem(i, currentItem);
                    return;
                } else {
                    currentItem.setAmount(currentItem.getMaxStackSize());
                    inventory.setItem(i, currentItem);
                    amount = newAmount - currentItem.getMaxStackSize();
                }
            }
        }
        for (int i = 3; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null || inventory.getItem(i).getAmount() == 0) {
                emerald.setAmount(amount);
                inventory.setItem(i, emerald);
                return;
            }
        }
        player.getWorld().dropItem(player.getLocation(), emerald);
    }


    public static void reduceEmerald(Player player, int amount) {
        int currentAmount = countEmeraldsInInventory(player);
        if (currentAmount < amount) {
            amount = currentAmount;
        }
        PlayerInventory inventory = player.getInventory();
        int remaining = amount;
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() == Material.EMERALD) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && "§2绿宝石".equals(meta.getDisplayName()) &&
                        meta.hasEnchant(Enchantment.UNBREAKING)) {
                    int itemAmount = item.getAmount();
                    if (itemAmount >= remaining) {
                        item.setAmount(itemAmount - remaining);
                        if (item.getAmount() == 0) {
                            inventory.remove(item);
                        }
                        return;
                    } else {
                        remaining -= itemAmount;
                        inventory.remove(item);
                    }
                }
            }
        }
    }

    public static int countEmeraldsInInventory(Player player) {
        int count = 0;
        PlayerInventory inventory = player.getInventory();
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() == Material.EMERALD) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.getDisplayName().equals("§2绿宝石") &&
                        meta.hasEnchant(Enchantment.UNBREAKING)) {
                    count += item.getAmount();
                }
            }
        }
        return count;
    }
}

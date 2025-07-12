package org.windguest.mobwar.games;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.windguest.mobwar.Files;
import org.windguest.mobwar.Main;

import java.util.*;

public class Edit {

    private static final Main plugin = Main.getInstance();

    private static final Map<UUID, String> editingPlayers = new HashMap<>();

    public static void startEdit(Player player, String jobName) {
        UUID playerUUID = player.getUniqueId();
        player.teleport(new Location(Bukkit.getWorld("world"), -7.5, 273.0, 0.5, -90, 0));
        editingPlayers.put(playerUUID, jobName);
        player.setInvulnerable(true);
        if (hasEdited(player, jobName)) {
            loadPlayerEditedInventory(player, jobName);
        } else {
            Jobs.equipKits(player, jobName);
        }
    }

    public static void removeEdit(Player player) {
        editingPlayers.remove(player.getUniqueId());
    }

    public static void quitEdit(Player player) {
        if (!isEditing(player)) {
            player.sendMessage("你当前没有在编辑任何生物的装备顺序。");
            return;
        }
        savePlayerInventory(player);
        player.setInvulnerable(false);
        player.teleport(new Location(Bukkit.getWorld("world"), -14.5, 243.0, 0.5, 90, 0));
        Bukkit.getScheduler().runTaskLater(plugin, () -> player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f), 2L);
        player.getInventory().clear();
        Players.giveHubItems(player);
        editingPlayers.remove(player.getUniqueId());
    }

    public static boolean isEditing(Player player) {
        return editingPlayers.containsKey(player.getUniqueId());
    }

    public static String getEditingJobName(Player player) {
        return editingPlayers.get(player.getUniqueId());
    }

    public static void savePlayerInventory(Player player) {
        String jobName = getEditingJobName(player);
        ItemStack[] inventoryContents = player.getInventory().getContents();
        String path = "kits." + jobName;
        Files.setPlayerData(player, path, inventoryContents);
    }

    public static boolean hasEdited(Player player, String jobName) {
        if (player == null || jobName == null || jobName.isEmpty()) return false;
        String path = "kits." + jobName;
        Object data = Files.getPlayerData(player, path);
        return data != null;
    }

    public static void loadPlayerEditedInventory(Player player, String jobName) {
        if (!hasEdited(player, jobName)) {
            player.sendMessage("你没有编辑过该职业。");
            return;
        }
        String path = "kits." + jobName;
        Object data = Files.getPlayerData(player, path);
        if (data == null) {
            player.sendMessage("没有找到保存的装备数据。");
            return;
        }
        if (!(data instanceof List)) {
            player.sendMessage("装备数据格式不正确。");
            return;
        }
        @SuppressWarnings("unchecked")
        List<ItemStack> itemList = (List<ItemStack>) data;
        player.getInventory().setContents(itemList.toArray(new ItemStack[0]));
        player.updateInventory();
    }

    public static void clearSingleKits(String jobName) {
        for (UUID uuid : Files.getAllUserUUIDs()) {
            String path = "kits." + jobName;
            Files.removePlayerData(uuid, path);
        }
    }

    public static void clearAllKits() {
        for (UUID uuid : Files.getAllUserUUIDs()) {
            Files.removePlayerData(uuid, "kits");
        }
    }

    public static void clearPlayerNowEditedKits(Player player) {
        if (!isEditing(player)) {
            return;
        }
        String jobName = getEditingJobName(player);
        UUID uuid = player.getUniqueId();
        String path = "kits." + jobName;
        Files.removePlayerData(uuid, path);
        Jobs.equipKits(player, jobName);
        player.playSound(player.getLocation(), Sound.ENTITY_HORSE_ARMOR, 1.0f, 1.0f);
    }
}

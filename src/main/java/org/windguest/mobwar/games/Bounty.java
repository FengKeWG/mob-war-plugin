package org.windguest.mobwar.games;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class Bounty implements Listener {

    private static final Map<Player, Integer> playerPageIndex = new HashMap<>();
    private static final Map<Player, Integer> bountiesTmp = new HashMap<>();
    private static final Map<Player, Integer> playerBounties = new HashMap<>();

    public static void openBountyMenu(Player player, int pageIndex) {
        Inventory bountyMenu = Bukkit.createInventory(null, 54, "悬赏 - 第 " + (pageIndex + 1) + " 页");
        ItemStack previousPage = new ItemStack(Material.ARROW);
        ItemMeta previousPageMeta = previousPage.getItemMeta();
        if (previousPageMeta != null) {
            previousPageMeta.setDisplayName("§e上一页");
            previousPage.setItemMeta(previousPageMeta);
        }
        bountyMenu.setItem(48, previousPage);
        ItemStack closeItem = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeItem.getItemMeta();
        if (closeMeta != null) {
            closeMeta.setDisplayName("§c关闭");
            closeItem.setItemMeta(closeMeta);
        }
        bountyMenu.setItem(49, closeItem);
        ItemStack nextPage = new ItemStack(Material.ARROW);
        ItemMeta nextPageMeta = nextPage.getItemMeta();
        if (nextPageMeta != null) {
            nextPageMeta.setDisplayName("§e下一页");
            nextPage.setItemMeta(nextPageMeta);
        }
        bountyMenu.setItem(50, nextPage);
        List<Player> allPlayers = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!Players.getJobData(p).equals("?") && !p.equals(player) && !isPlayerBountied(p)) {
                allPlayers.add(p);
            }
        }
        int totalPages = (int) Math.ceil((double) allPlayers.size() / 28.0);
        if (pageIndex < 0) {
            pageIndex = 0;
        }
        if (pageIndex >= totalPages && totalPages > 0) {
            pageIndex = totalPages - 1;
        }
        int startIndex = pageIndex * 28;
        int endIndex = Math.min(startIndex + 28, allPlayers.size());
        int[] slots = new int[]{
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };
        for (int i = startIndex; i < endIndex; ++i) {
            Player p = allPlayers.get(i);
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            if (meta != null) {
                String prefix = Players.getPlayerPrefix(p);
                meta.setDisplayName(prefix + " " + p.getName());
                meta.setOwningPlayer(p);
                List<String> lore = new ArrayList<>();
                lore.add("§7点击进行悬赏");
                meta.setLore(lore);
                skull.setItemMeta(meta);
            }
            bountyMenu.setItem(slots[i - startIndex], skull);
        }
        playerPageIndex.put(player, pageIndex);
        player.openInventory(bountyMenu);
    }

    private void openBountyTargetMenu(Player player, Player target, int amount) {
        Inventory bountyTargetMenu = Bukkit.createInventory(null, 45, "悬赏目标 - " + target.getName());
        ItemStack confirmBounty = new ItemStack(Material.EMERALD);
        ItemMeta confirmMeta = confirmBounty.getItemMeta();
        if (confirmMeta != null) {
            confirmMeta.setDisplayName("§a确认悬赏");
            List<String> lore = new ArrayList<>();
            lore.add("§7当前悬赏: §a" + amount + " 个绿宝石");
            confirmMeta.setLore(lore);
            confirmBounty.setItemMeta(confirmMeta);
        }
        bountyTargetMenu.setItem(22, confirmBounty);
        ItemStack add10 = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta add10Meta = add10.getItemMeta();
        if (add10Meta != null) {
            add10Meta.setDisplayName("§a+10");
            add10.setItemMeta(add10Meta);
        }
        bountyTargetMenu.setItem(23, add10);
        ItemStack add1 = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta add1Meta = add1.getItemMeta();
        if (add1Meta != null) {
            add1Meta.setDisplayName("§a+1");
            add1.setItemMeta(add1Meta);
        }
        bountyTargetMenu.setItem(24, add1);
        ItemStack remove10 = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta remove10Meta = remove10.getItemMeta();
        if (remove10Meta != null) {
            remove10Meta.setDisplayName("§c-10");
            remove10.setItemMeta(remove10Meta);
        }
        bountyTargetMenu.setItem(21, remove10);
        ItemStack remove1 = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta remove1Meta = remove1.getItemMeta();
        if (remove1Meta != null) {
            remove1Meta.setDisplayName("§c-1");
            remove1.setItemMeta(remove1Meta);
        }
        bountyTargetMenu.setItem(20, remove1);
        player.openInventory(bountyTargetMenu);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack clickedItem = event.getCurrentItem();
        Player player = (Player) event.getWhoClicked();
        int emeraldCnt = Emerald.countEmeraldsInInventory(player);
        if (event.getView().getTitle().startsWith("悬赏 - 第")) {
            if (clickedItem == null || clickedItem.getType() == Material.AIR) {
                return;
            }
            event.setCancelled(true);
            if (clickedItem.getType() == Material.PLAYER_HEAD) {
                SkullMeta meta = (SkullMeta) clickedItem.getItemMeta();
                if (meta == null) return;
                OfflinePlayer p = meta.getOwningPlayer();
                if (p == null || !p.isOnline()) {
                    player.sendMessage("目标玩家不在线！");
                    return;
                }
                Player target = p.getPlayer();
                if (emeraldCnt < 5) {
                    player.sendMessage("§c你需要至少拥有5个绿宝石才能设置悬赏！");
                    return;
                }
                openBountyTargetMenu(player, target, 5);
            } else if (clickedItem.getType() == Material.ARROW) {
                int currentPage = playerPageIndex.getOrDefault(player, 0);
                if (clickedItem.getItemMeta() != null) {
                    String displayName = clickedItem.getItemMeta().getDisplayName();
                    if (displayName.equals("§e上一页")) {
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.3f, 1.0f);
                        openBountyMenu(player, currentPage - 1);
                    } else if (displayName.equals("§e下一页")) {
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.3f, 1.0f);
                        openBountyMenu(player, currentPage + 1);
                    }
                }
            } else if (clickedItem.getType() == Material.BARRIER) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                player.closeInventory();
            }
        } else if (event.getView().getTitle().startsWith("悬赏目标 - ")) {
            if (clickedItem == null || clickedItem.getType() == Material.AIR) {
                return;
            }
            event.setCancelled(true);
            String title = event.getView().getTitle();
            String targetName = title.substring("悬赏目标 - ".length());
            Player target = Bukkit.getPlayerExact(targetName);
            if (target == null) {
                player.sendMessage("目标玩家不在线！");
                player.closeInventory();
                return;
            }
            int currentAmount = bountiesTmp.getOrDefault(target, 5);
            if (clickedItem.getType() == Material.EMERALD) {
                Emerald.reduceEmerald(player, currentAmount);
                playerBounties.put(target, currentAmount);
                target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0, true, false, true));
                String senderName = player.getName();
                String targetNameStr = target.getName();
                Bukkit.broadcastMessage("§6[💰] §f玩家 §e" + senderName + " §f正悬赏 §c" + targetNameStr + " §f中，击败Ta将额外获得 §e" + currentAmount + " §f个§a§l绿宝石§f作为酬劳！");
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendTitle("§c§l!! 悬赏通告 !!", "§e" + senderName + " §f悬赏了 §c" + targetNameStr + " §e" + currentAmount + " §f个§a§l绿宝石", 10, 20, 5);
                    p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_YES, 1.0f, 1.0f);
                }
                player.closeInventory();
            } else if (clickedItem.getType() == Material.LIME_STAINED_GLASS_PANE) {
                String displayName = clickedItem.getItemMeta().getDisplayName();
                if (displayName.equals("§a+10")) {
                    if (currentAmount + 10 <= emeraldCnt) {
                        currentAmount += 10;
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                    } else {
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.5f, 0.4f);
                    }
                } else if (displayName.equals("§a+1")) {
                    if (currentAmount + 1 <= emeraldCnt) {
                        currentAmount += 1;
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                    } else {
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.5f, 0.4f);
                    }
                }
                bountiesTmp.put(target, currentAmount);
                openBountyTargetMenu(player, target, currentAmount);
            } else if (clickedItem.getType() == Material.RED_STAINED_GLASS_PANE) {
                String displayName = clickedItem.getItemMeta().getDisplayName();
                if (displayName.equals("§c-10")) {
                    if (currentAmount - 10 >= 5) {
                        currentAmount -= 10;
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                    } else {
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.5f, 0.4f);
                    }
                } else if (displayName.equals("§c-1")) {
                    if (currentAmount - 1 >= 5) {
                        currentAmount -= 1;
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                    } else {
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.5f, 0.4f);
                    }
                }
                bountiesTmp.put(target, currentAmount);
                openBountyTargetMenu(player, target, currentAmount);
            }
        }
    }

    public static void clearData(Player player) {
        playerPageIndex.remove(player);
        bountiesTmp.remove(player);
        playerBounties.remove(player);
    }

    public static boolean isPlayerBountied(Player player) {
        return playerBounties.containsKey(player);
    }

    public static int getPlayerBounties(Player player) {
        return playerBounties.getOrDefault(player, 0);
    }
}

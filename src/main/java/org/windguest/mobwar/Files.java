package org.windguest.mobwar;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.windguest.mobwar.games.Players;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Files {

    private static File usersFolder;
    static Main plugin = Main.getInstance();

    public static void generateFiles() {
        File pluginFolder = plugin.getDataFolder();
        if (!pluginFolder.exists()) {
            pluginFolder.mkdirs();
        }
        usersFolder = new File(pluginFolder, "users");
        if (!usersFolder.exists()) {
            usersFolder.mkdirs();
        }
        loadPlayerData();
    }

    public static void loadPlayerData() {
        File[] userFiles = usersFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (userFiles == null) return;
        for (File userFile : userFiles) {
            FileConfiguration userConfig = YamlConfiguration.loadConfiguration(userFile);
            String uuidStr = userFile.getName().replace(".yml", "");
            try {
                UUID uuid = UUID.fromString(uuidStr);
                int stars = userConfig.getInt("stars", 0);
                Players.setPlayerStarsInMap(uuid, stars);
                ConfigurationSection killsSection = userConfig.getConfigurationSection("kills");
                if (killsSection != null) {
                    for (String job : killsSection.getKeys(false)) {
                        int kills = killsSection.getInt(job, 0);
                        Players.setPlayerKillsInMap(uuid, job, kills);
                    }
                }
                ConfigurationSection deathsSection = userConfig.getConfigurationSection("deaths");
                if (deathsSection != null) {
                    for (String job : deathsSection.getKeys(false)) {
                        int deaths = deathsSection.getInt(job, 0);
                        Players.setPlayerDeathsInMap(uuid, job, deaths);
                    }
                }
                ConfigurationSection maxStreaksSection = userConfig.getConfigurationSection("maxStreaks");
                if (maxStreaksSection != null) {
                    for (String job : maxStreaksSection.getKeys(false)) {
                        int maxStreaks = maxStreaksSection.getInt(job, 0);
                        Players.setPlayerMaxStreaksInMap(uuid, job, maxStreaks);
                    }
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("文件 " + userFile.getName() + " 中的 UUID 格式无效。");
            }
        }
    }

    public static void savePlayerData(Player player) {
        UUID uuid = player.getUniqueId();
        File userFile = new File(usersFolder, uuid + ".yml");
        FileConfiguration userConfig = YamlConfiguration.loadConfiguration(userFile);
        int stars = Players.getPlayerStarsFromMap(uuid);
        userConfig.set("stars", stars);
        Map<String, Integer> kills = Players.getPlayerKillsMap(uuid);
        if (kills != null) {
            for (Map.Entry<String, Integer> entry : kills.entrySet()) {
                userConfig.set("kills." + entry.getKey(), entry.getValue());
            }
        }
        Map<String, Integer> deaths = Players.getPlayerDeathsMap(uuid);
        if (deaths != null) {
            for (Map.Entry<String, Integer> entry : deaths.entrySet()) {
                userConfig.set("deaths." + entry.getKey(), entry.getValue());
            }
        }
        Map<String, Integer> maxStreaks = Players.getPlayerMaxStreaksMap(uuid);
        if (maxStreaks != null) {
            for (Map.Entry<String, Integer> entry : maxStreaks.entrySet()) {
                userConfig.set("maxStreaks." + entry.getKey(), entry.getValue());
            }
        }
        try {
            userConfig.save(userFile);
        } catch (IOException e) {
            plugin.getLogger().warning("无法保存数据到文件 " + userFile.getName());
            e.printStackTrace();
        }
    }

    public static void removePlayerData(UUID uuid, String path) {
        File userFile = new File(usersFolder, uuid.toString() + ".yml");
        if (!userFile.exists()) {
            plugin.getLogger().warning("玩家文件 " + uuid.toString() + ".yml 不存在。");
            return;
        }

        FileConfiguration userConfig = YamlConfiguration.loadConfiguration(userFile);
        if (userConfig.contains(path)) {
            userConfig.set(path, null);
            try {
                userConfig.save(userFile);
                plugin.getLogger().info("已删除 " + path + " 从 " + uuid.toString() + ".yml");
            } catch (IOException e) {
                plugin.getLogger().severe("无法保存玩家文件 " + uuid.toString() + ".yml");
                e.printStackTrace();
            }
        } else {
            plugin.getLogger().info("玩家文件 " + uuid.toString() + ".yml 中不存在路径 " + path);
        }
    }

    public static Set<UUID> getAllUserUUIDs() {
        Set<UUID> uuidSet = new HashSet<>();
        File[] userFiles = usersFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (userFiles == null) return uuidSet;
        for (File userFile : userFiles) {
            String fileName = userFile.getName().replace(".yml", "");
            try {
                UUID uuid = UUID.fromString(fileName);
                uuidSet.add(uuid);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("文件 " + userFile.getName() + " 中的 UUID 格式无效。");
            }
        }
        return uuidSet;
    }

    public static void setPlayerData(Player player, String key, Object value) {
        UUID playerUUID = player.getUniqueId();
        File playerFile = new File(usersFolder, playerUUID + ".yml");
        if (playerFile.exists()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
            config.set(key, value);
            try {
                config.save(playerFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void addPlayerConfig(Player player, String key) {
        UUID playerUUID = player.getUniqueId();
        File playerFile = new File(usersFolder, playerUUID + ".yml");
        if (playerFile.exists()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
            int value = config.getInt(key);
            config.set(key, (value + 1));
            try {
                config.save(playerFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Object getPlayerData(Player player, String key) {
        UUID playerUUID = player.getUniqueId();
        File userFile = new File(usersFolder, playerUUID + ".yml");
        if (!userFile.exists()) {
            return null;
        }
        FileConfiguration userConfig = YamlConfiguration.loadConfiguration(userFile);
        return userConfig.get(key);
    }
}

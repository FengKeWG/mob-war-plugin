package org.windguest.mobwar.games;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Entities {

    private static final Map<UUID, Map<UUID, Integer>> summonedEntities = new HashMap<>();

    public static void addEntity(Entity entity, Player player, int info) {
        UUID playerUUID = player.getUniqueId();
        UUID entityUUID = entity.getUniqueId();
        summonedEntities.putIfAbsent(entityUUID, new HashMap<>());
        summonedEntities.get(entityUUID).put(playerUUID, info);
    }

    public static Player getEntityOwner(Entity entity) {
        UUID entityUUID = entity.getUniqueId();
        Map<UUID, Integer> ownerMap = summonedEntities.get(entityUUID);
        if (ownerMap != null) {
            UUID playerUUID = ownerMap.keySet().stream().findFirst().orElse(null);
            if (playerUUID != null) {
                return Bukkit.getPlayer(playerUUID);
            }
        }
        return null;
    }

    public static Integer getEntityInfo(Entity entity) {
        UUID entityUUID = entity.getUniqueId();
        Map<UUID, Integer> ownerMap = summonedEntities.get(entityUUID);
        if (ownerMap != null) {
            return ownerMap.values().stream().findFirst().orElse(null);
        }
        return null;
    }

    public static void removePlayer(Player player) {
        UUID playerUUID = player.getUniqueId();
        summonedEntities.entrySet().removeIf(entry -> {
            UUID entityUUID = entry.getKey();
            Map<UUID, Integer> ownerMap = entry.getValue();
            if (ownerMap.containsKey(playerUUID)) {
                Entity entity = Bukkit.getEntity(entityUUID);
                if (entity != null) {
                    entity.remove();
                }
                return true;
            }
            return false;
        });
    }

    public static void removeEntity(Entity entity) {
        UUID entityUUID = entity.getUniqueId();
        entity.remove();
        summonedEntities.remove(entityUUID);
    }

    public static void removeAllEntities() {
        for (UUID entityUUID : summonedEntities.keySet()) {
            Entity entity = Bukkit.getEntity(entityUUID);
            if (entity != null) {
                entity.remove();
            }
            summonedEntities.remove(entityUUID);
        }
    }
}

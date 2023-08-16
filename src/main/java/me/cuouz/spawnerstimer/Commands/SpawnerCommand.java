package me.cuouz.spawnerstimer.Commands;

import me.cuouz.spawnerstimer.Listeners.*;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;
import org.bukkit.plugin.*;

import java.util.*;

public class SpawnerCommand implements CommandExecutor {

    private final Plugin plugin;
    private final DatabaseManager dbManager;
    private final long defaultDuration;
    private final HashMap<String, Integer> entityIds = new HashMap<>();

    public SpawnerCommand(Plugin plugin, DatabaseManager dbManager, long defaultDuration) {
        this.plugin = plugin;
        this.dbManager = dbManager;
        this.defaultDuration = defaultDuration;

        // Mapear nombres de entidades a IDs numéricos en 1.8.8
        entityIds.put("zombie", 54);
        entityIds.put("skeleton", 51);
        entityIds.put("ender_dragon", 63);
        entityIds.put("cow", 92);
        entityIds.put("irongolem", 99);
        entityIds.put("creeper", 50);
        entityIds.put("spider", 52);
        entityIds.put("sheep", 91);
        entityIds.put("blaze", 61);
        entityIds.put("pig", 90);
        // ... (add other entity mappings)

        // Inicializar el mapa de IDs de entidades
        for (EntityType entityType : EntityType.values()) {
            entityIds.put(entityType.name().toLowerCase(), (int) entityType.getTypeId());
        }
    }

    private int getEntityTypeId(String entityName) {
        // Comprueba si el mapa de IDs de entidades contiene la entidad especificada
        if (entityIds.containsKey(entityName.toLowerCase())) {
            return entityIds.get(entityName.toLowerCase());
        }

        // Si la entidad no está en el mapa, devuelve -1
        return -1;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;
        if (args.length >= 2 && args[0].equalsIgnoreCase("givespawner")) {
            String targetPlayerName = args[1];
            EntityType entityType = null;
            long duration = defaultDuration;

            if (args.length >= 3) {
                String entityTypeString = args[2].toLowerCase();
                int entityTypeId = getEntityTypeId(entityTypeString);

                if (entityTypeId != -1) {
                    entityType = EntityType.fromId(entityTypeId);
                } else {
                    entityType = EntityType.fromName(entityTypeString);
                }
            } else {
                player.sendMessage("Falta el tipo de entidad.");
                return true;
            }

            if (args.length >= 4) {
                try {
                    duration = Long.parseLong(args[3]);
                } catch (NumberFormatException e) {
                    player.sendMessage("Duración inválida. Se usará la duración predeterminada.");
                }
            }

            Player targetPlayer = plugin.getServer().getPlayer(targetPlayerName);

            if (targetPlayer != null && entityType != null) {
                // Dar spawner al jugador objetivo
                dbManager.givePlayerSpawner(targetPlayer.getUniqueId(), duration, entityType);

                ItemStack spawnerItem = new ItemStack(Material.MOB_SPAWNER);
                ItemMeta spawnerMeta = spawnerItem.getItemMeta();

                // Set custom display name using NBT tag (only works in 1.8.8)
                spawnerMeta.setDisplayName("Spawner de " + entityType.getName());

                spawnerItem.setItemMeta(spawnerMeta);

                me.cuouz.spawnerstimer.Listeners.ItemStackUtils.addSpawnerExpiration(spawnerItem, System.currentTimeMillis() + duration);
                targetPlayer.getInventory().addItem(spawnerItem);

                String formattedDuration = formatDuration(duration);
                player.sendMessage("Se ha dado un spawner de " + entityType.getName() + " a " + targetPlayerName + " con una duración de " + formattedDuration + ".");
                return true;
            } else {
                player.sendMessage("Jugador o tipo de entidad inválidos.");
            }
        }

        return false;
    }

    private String formatDuration(long durationMillis) {
        long durationHours = durationMillis / (60 * 60 * 1000);
        return durationHours + " horas";
    }
}

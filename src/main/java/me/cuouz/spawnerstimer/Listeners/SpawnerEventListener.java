package me.cuouz.spawnerstimer.Listeners;

import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;
import org.bukkit.plugin.*;

import java.util.*;

public class SpawnerEventListener implements Listener {

    private Plugin plugin;
    private DatabaseManager dbManager;
    private Map<UUID, Long> pickupCooldowns = new HashMap<>();

    public SpawnerEventListener(Plugin plugin, DatabaseManager dbManager) {
        this.plugin = plugin;
        this.dbManager = dbManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!player.isSneaking()) return;

        if (event.getClickedBlock() != null && event.getClickedBlock().getState() instanceof CreatureSpawner) {
            CreatureSpawner spawner = (CreatureSpawner) event.getClickedBlock().getState();
            Date expirationDate = dbManager.getSpawnerExpiration(spawner.getLocation());

            if (expirationDate != null) {
                long remainingTime = calculateRemainingTime(expirationDate);
                player.sendMessage(ChatColor.YELLOW + "Tiempo restante para el spawner: " + remainingTime + " minutos");
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getState() instanceof CreatureSpawner) {
            CreatureSpawner spawner = (CreatureSpawner) event.getBlock().getState();
            dbManager.removeSpawnerExpiration(spawner.getLocation());
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack itemStack = event.getItemDrop().getItemStack();

        if (itemStack != null && itemStack.getType() == Material.MOB_SPAWNER) {
            long currentTimeMillis = System.currentTimeMillis();
            long cooldownMillis = 1000; // 1 segundo

            if (pickupCooldowns.containsKey(player.getUniqueId())) {
                long lastPickupTimeMillis = pickupCooldowns.get(player.getUniqueId());
                if (currentTimeMillis - lastPickupTimeMillis < cooldownMillis) {
                    return; // Evitar spam del evento
                }
            }

            pickupCooldowns.put(player.getUniqueId(), currentTimeMillis);
            Date expirationDate = ItemStackUtils.getSpawnerExpiration(itemStack);

            if (expirationDate != null) {
                long expirationMillis = expirationDate.getTime();
                long newExpirationMillis = expirationMillis + 4 * 60 * 60 * 1000; // Agregar 4 horas
                ItemStackUtils.updateSpawnerExpiration(itemStack, newExpirationMillis);
            } else {
                long expirationMillis = System.currentTimeMillis() + 4 * 60 * 60 * 1000; // 4 horas en milisegundos
                ItemStackUtils.addSpawnerExpiration(itemStack, expirationMillis);
            }
        }
    }

    private long calculateRemainingTime(Date expirationDate) {
        Date now = new Date();
        long remainingTimeInMinutes = (expirationDate.getTime() - now.getTime()) / (60 * 1000);
        return Math.max(0, remainingTimeInMinutes); // Asegurar que el tiempo restante no sea negativo
    }
}

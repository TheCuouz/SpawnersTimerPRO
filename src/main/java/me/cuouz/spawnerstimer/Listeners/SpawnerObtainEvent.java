// SpawnerObtainEvent
package me.cuouz.spawnerstimer;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class SpawnerObtainEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final ItemStack spawner;

    public SpawnerObtainEvent(Player player, ItemStack spawner) {
        this.player = player;
        this.spawner = spawner;
    }

    public Player getPlayer() {
        return player;
    }

    public ItemStack getSpawner() {
        return spawner;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}

package me.cuouz.spawnerstimer.Listeners;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;

import java.text.*;
import java.util.*;

public class ItemStackUtils {

    // Agregar fecha de expiración al lore del item
    public static ItemStack setSpawnerEntityType(ItemStack itemStack, EntityType entityType) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setLore(Collections.singletonList(entityType.name())); // Usar una lista de un solo elemento para el tipo de entidad
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    // Método para agregar la fecha de expiración al lore del item
    public static void addSpawnerExpiration(ItemStack itemStack, long expirationDateMillis) {
        ItemMeta itemMeta = itemStack.getItemMeta();

        List<String> lore = itemMeta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }
        lore.add("Expira en: " + formatMillisToDate(expirationDateMillis));

        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
    }

    public static EntityType getSpawnerEntityType(ItemStack itemStack) {
        if (itemStack != null && itemStack.getType() == Material.MOB_SPAWNER) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta.hasLore() && itemMeta.getLore().size() > 0) {
                return EntityType.valueOf(itemMeta.getLore().get(0));
            }
        }
        return null;
    }

    // Obtener la fecha de expiración desde el lore del item
    public static Date getSpawnerExpiration(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        List<String> lore = itemMeta.getLore();
        if (lore != null && lore.size() > 0) {
            String lastLine = lore.get(lore.size() - 1);
            if (lastLine.startsWith("Expira en: ")) {
                String formattedDate = lastLine.replace("Expira en: ", "");
                DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                try {
                    return dateFormat.parse(formattedDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    // Actualizar la fecha de expiración en el lore del item
    public static void updateSpawnerExpiration(ItemStack itemStack, long newExpirationDateMillis) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        List<String> lore = itemMeta.getLore();

        if (lore != null && lore.size() > 0) {
            lore.set(lore.size() - 1, "Expira en: " + formatMillisToDate(newExpirationDateMillis));
            itemMeta.setLore(lore);
            itemStack.setItemMeta(itemMeta);
        }
    }

    // Formatear una fecha en milisegundos a una cadena legible
    private static String formatMillisToDate(long millis) {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return dateFormat.format(new Date(millis));
    }
}

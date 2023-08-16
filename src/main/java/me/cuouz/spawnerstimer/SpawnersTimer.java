// Clase principal
package me.cuouz.spawnerstimer;

import me.cuouz.spawnerstimer.Commands.*;
import me.cuouz.spawnerstimer.Listeners.*;
import org.bukkit.configuration.file.*;
import org.bukkit.plugin.java.*;
import java.io.*;

public class SpawnersTimer extends JavaPlugin {

    private DatabaseManager dbManager;
    private SpawnerEventListener spawnerEventListener;

    @Override
    public void onEnable() {
        FileConfiguration config = getConfig();

        // Verificar si el plugin ya ha sido inicializado
        if (!config.contains("initialized")) {
            config.set("initialized", false);
            saveConfig();
        }

        boolean initialized = config.getBoolean("initialized");
        if (!initialized) {
            // Inicializar el gestor de la base de datos y el oyente de spawners
            dbManager = new DatabaseManager(new File(getDataFolder(), "database.db").getAbsolutePath(), this);
            spawnerEventListener = new SpawnerEventListener(this, dbManager);

            // Registrar el evento personalizado SpawnerObtainEvent
            getServer().getPluginManager().registerEvents(spawnerEventListener, this);

            // Duración predeterminada de 4 horas (4 * 60 * 60 * 1000 milisegundos)
            long defaultDuration = 4 * 60 * 60 * 1000;

            // Crear instancia del comando SpawnerCommand con duración predeterminada
            SpawnerCommand spawnerCommand = new SpawnerCommand(this, dbManager, defaultDuration);

            // Registrar el comando SpawnerCommand
            getCommand("st").setExecutor(spawnerCommand);

            // Marcar el plugin como inicializado y guardar la configuración
            config.set("initialized", true);
            saveConfig();

            getLogger().info("SpawnersTimer se ha habilitado correctamente.");
        }
    }

    @Override
    public void onDisable() {
        if (dbManager != null) {
            dbManager.closeConnection();
        }

        // Marcar el plugin como no inicializado y guardar la configuración
        getConfig().set("initialized", false);
        saveConfig();

        getLogger().info("SpawnersTimer se ha deshabilitado.");
    }

    public SpawnerEventListener getSpawnerEventListener() {
        return spawnerEventListener;
    }
}

package com.caiodev.minecraftgamesx.core.config

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.java.JavaPlugin

class ConfigManager(private val plugin: JavaPlugin) {
    val config: FileConfiguration = plugin.config

    init {
        plugin.saveDefaultConfig()
    }

    fun getDatabaseConfig(): DatabaseConfig {
        return DatabaseConfig(
            host = config.getString("database.host") ?: "localhost",
            port = config.getInt("database.port", 3306),
            database = config.getString("database.database") ?: "minecraftgamesx",
            username = config.getString("database.username") ?: "minecraftgamesx",
            password = config.getString("database.password") ?: ""
        )
    }
}

data class DatabaseConfig(
    val host: String,
    val port: Int,
    val database: String,
    val username: String,
    val password: String
)
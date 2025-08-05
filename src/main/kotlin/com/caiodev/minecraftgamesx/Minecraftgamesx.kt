package com.caiodev.minecraftgamesx

import com.caiodev.minecraftgamesx.auth.AuthManager
import com.caiodev.minecraftgamesx.auth.command.LoginCommand
import com.caiodev.minecraftgamesx.auth.command.RegisterCommand
import com.caiodev.minecraftgamesx.auth.listener.AuthListener
import com.caiodev.minecraftgamesx.core.config.ConfigManager
import com.caiodev.minecraftgamesx.core.database.DatabaseManager
import org.bukkit.plugin.java.JavaPlugin

class Minecraftgamesx : JavaPlugin() {
    lateinit var configManager: ConfigManager
    lateinit var databaseManager: DatabaseManager
    lateinit var authManager: AuthManager

    override fun onEnable() {
        logger.info("MinecraftGamesX iniciado com sucesso!")
        configManager = ConfigManager(this)
        databaseManager = DatabaseManager(configManager.getDatabaseConfig())
        authManager = AuthManager(databaseManager)

        // Registrar comandos
        getCommand("register")?.setExecutor(RegisterCommand(authManager))
        getCommand("login")?.setExecutor(LoginCommand(authManager))

        // Registrar listener
        server.pluginManager.registerEvents(AuthListener(this, authManager), this)
    }

    override fun onDisable() {
        logger.info("MinecraftGamesX desativado.")
        databaseManager.close()
    }
}
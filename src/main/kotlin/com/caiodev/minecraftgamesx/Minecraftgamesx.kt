package com.caiodev.minecraftgamesx

import com.caiodev.minecraftgamesx.auth.AuthManager
import com.caiodev.minecraftgamesx.auth.command.LoginCommand
import com.caiodev.minecraftgamesx.auth.command.RegisterCommand
import com.caiodev.minecraftgamesx.auth.listener.AuthListener
import com.caiodev.minecraftgamesx.command.AddNPCCommand
import com.caiodev.minecraftgamesx.command.RemoveNPCCommand
import com.caiodev.minecraftgamesx.core.config.ConfigManager
import com.caiodev.minecraftgamesx.core.database.DatabaseManager
import com.caiodev.minecraftgamesx.lobby.LobbyListener
import org.bukkit.plugin.java.JavaPlugin

class Minecraftgamesx : JavaPlugin() {
    lateinit var configManager: ConfigManager
    var databaseManager: DatabaseManager? = null
    lateinit var authManager: AuthManager

    override fun onEnable() {
        logger.info("MinecraftGamesX iniciado com sucesso!")
        configManager = ConfigManager(this)
        try {
            databaseManager = DatabaseManager(configManager.getDatabaseConfig())
            authManager = AuthManager(databaseManager!!)
            // Registrar comandos
            getCommand("register")?.setExecutor(RegisterCommand(authManager, this))
            getCommand("login")?.setExecutor(LoginCommand(authManager, this))
            getCommand("addnpc")?.setExecutor(AddNPCCommand(this))
            getCommand("removenpc")?.setExecutor(RemoveNPCCommand(this))
            // Registrar listeners
            server.pluginManager.registerEvents(AuthListener(this, authManager), this)
            server.pluginManager.registerEvents(LobbyListener(this, authManager), this)
        } catch (e: Exception) {
            logger.severe("Erro ao inicializar o DatabaseManager: ${e.message}")
            server.pluginManager.disablePlugin(this)
        }
    }

    override fun onDisable() {
        logger.info("MinecraftGamesX desativado.")
        databaseManager?.close()
    }
}
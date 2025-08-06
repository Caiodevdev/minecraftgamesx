package com.caiodev.minecraftgamesx

import com.caiodev.minecraftgamesx.auth.AuthManager
import com.caiodev.minecraftgamesx.auth.command.LoginCommand
import com.caiodev.minecraftgamesx.auth.command.RegisterCommand
import com.caiodev.minecraftgamesx.auth.listener.AuthListener
import com.caiodev.minecraftgamesx.command.LevelCommand
import com.caiodev.minecraftgamesx.command.RemoveNPCCommand
import com.caiodev.minecraftgamesx.commands.AddCoinsCommand
import com.caiodev.minecraftgamesx.commands.AddNPCCommand
import com.caiodev.minecraftgamesx.commands.AddXPCommand
import com.caiodev.minecraftgamesx.commands.TagsCommand
import com.caiodev.minecraftgamesx.commands.TagAdminCommand
import com.caiodev.minecraftgamesx.core.config.ConfigManager
import com.caiodev.minecraftgamesx.core.database.DatabaseManager
import com.caiodev.minecraftgamesx.lobby.LobbyListener
import com.caiodev.minecraftgamesx.lobby.ScoreboardManager
import com.caiodev.minecraftgamesx.listener.ChatListener
import org.bukkit.plugin.java.JavaPlugin

class Minecraftgamesx : JavaPlugin() {
    lateinit var configManager: ConfigManager
    var databaseManager: DatabaseManager? = null
    lateinit var authManager: AuthManager
    val scoreboard: ScoreboardManager = ScoreboardManager

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
            getCommand("setlevel")?.setExecutor(LevelCommand(databaseManager!!, scoreboard))
            getCommand("addxp")?.setExecutor(AddXPCommand(databaseManager!!, scoreboard))
            getCommand("addcoins")?.setExecutor(AddCoinsCommand(databaseManager!!, scoreboard))
            getCommand("tags")?.setExecutor(TagsCommand(databaseManager!!))
            getCommand("tagadmin")?.setExecutor(TagAdminCommand(databaseManager!!))
            logger.info("Comandos /setlevel, /addxp, /addcoins, /tags e /tagadmin registrados com sucesso.")
            // Registrar listeners
            server.pluginManager.registerEvents(AuthListener(this, authManager), this)
            server.pluginManager.registerEvents(LobbyListener(this, authManager, databaseManager!!, scoreboard), this)
            server.pluginManager.registerEvents(ChatListener(databaseManager!!), this)
            server.messenger.registerOutgoingPluginChannel(this, "BungeeCord")
        } catch (e: Exception) {
            logger.severe("Erro ao inicializar o DatabaseManager: ${e.message}")
            server.pluginManager.disablePlugin(this)
        }
    }

    override fun onDisable() {
        logger.info("MinecraftGamesX desativado.")
        databaseManager?.close()
        server.messenger.unregisterOutgoingPluginChannel(this)
    }
}
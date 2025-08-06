package com.caiodev.minecraftgamesx.auth.listener

import com.caiodev.minecraftgamesx.auth.AuthManager
import com.caiodev.minecraftgamesx.lobby.InventoryManager
import com.caiodev.minecraftgamesx.lobby.ScoreboardManager
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.plugin.java.JavaPlugin

class AuthListener(private val plugin: JavaPlugin, private val authManager: AuthManager) : Listener {
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        if (authManager.isRegistered(player)) {
            player.sendMessage("§f✦ §7Bem-vindo de volta, §e§l${player.name}§7!")
            player.sendMessage("§7➜ Use §b§l/login <sua senha> §7para acessar o §e§lMinecraftGamesX§7.")
            player.sendTitle("§e✦ §lMinecraftGamesX", "§7Use §b§l/login <sua senha>", 10, 70, 20)
        } else {
            player.sendMessage("§f✦ §7Saudações, §e§l${player.name}§7! Bem-vindo ao servidor!")
            player.sendMessage("§7➜ Use §b§l/register <senha> <confirme> §7para se registrar no §e§lMinecraftGamesX§7.")
            player.sendTitle("§e✦ §lBem-vindo ao MinecraftGamesX!", "§7Use §b§l/register <senha> <confirme>", 10, 70, 20)
        }

        // Iniciar ActionBar para lembretes contínuos
        object : BukkitRunnable() {
            override fun run() {
                if (!authManager.isAuthenticated(player)) {
                    if (authManager.isRegistered(player)) {
                        player.sendActionBar(Component.text("§f✦ §7Use §b§l/login <sua senha> §7para entrar"))
                    } else {
                        player.sendActionBar(Component.text("§f✦ §7Use §b§l/register <senha> <confirme> §7para começar"))
                    }
                } else {
                    InventoryManager.setupPlayerInventory(player)
                    ScoreboardManager.setupScoreboard(player, plugin)
                    cancel()
                }
            }
        }.runTaskTimer(plugin, 0L, 40L) // Executa a cada 2 segundos
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPlayerMove(event: PlayerMoveEvent) {
        if (!authManager.isAuthenticated(event.player)) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (!authManager.isAuthenticated(event.player)) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPlayerCommand(event: PlayerCommandPreprocessEvent) {
        val player = event.player
        if (!authManager.isAuthenticated(player)) {
            val command = event.message.lowercase()
            if (!command.startsWith("/login") && !command.startsWith("/register")) {
                event.isCancelled = true
                player.sendMessage("§f✦ §7Você precisa se autenticar primeiro!")
                player.sendMessage("§7➜ Use §b§l/login <sua senha> §7ou §b§l/register <senha> <confirme>.")
            }
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        authManager.unauthenticate(event.player)
        ScoreboardManager.removeScoreboard(event.player)
    }
}
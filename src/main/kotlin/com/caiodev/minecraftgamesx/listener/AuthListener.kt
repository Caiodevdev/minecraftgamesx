package com.caiodev.minecraftgamesx.auth.listener

import com.caiodev.minecraftgamesx.auth.AuthManager
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.plugin.java.JavaPlugin

class AuthListener(private val plugin: JavaPlugin, private val authManager: AuthManager) : Listener {
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        if (authManager.isRegistered(player)) {
            player.sendMessage("§eMinecraftGamesX §7- Autenticação")
            player.sendMessage("§7Bem-vindo de volta, §e${player.name}§7!")
            player.sendMessage("§7Para fazer login, use o comando §b/login <sua senha>.")
            player.sendTitle("§eMinecraftGamesX!", "§7Use §b/login <sua senha>", 10, 70, 20)
        } else {
            player.sendMessage("§eMinecraftGamesX §7- Autenticação")
            player.sendMessage("§7Saudações, §e${player.name}§7!")
            player.sendMessage("§7Para se registrar, use o comando §b/register <sua senha> <confirme sua senha>.")
            player.sendTitle("§eBem-vindo ao MinecraftGamesX!", "§7Use §b/register <senha> <confirme>", 10, 70, 20)
        }

        // Iniciar ActionBar para lembretes contínuos
        object : BukkitRunnable() {
            override fun run() {
                if (!authManager.isAuthenticated(player)) {
                    if (authManager.isRegistered(player)) {
                        player.sendActionBar(Component.text("§7Registre-se com §b/login <sua senha>"))
                    } else {
                        player.sendActionBar(Component.text("§7Registre-se com §b/register <senha> <confirme>"))
                    }
                } else {
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
                player.sendMessage("§eMinecraftGamesX §7- Autenticação")
                player.sendMessage("§7Você precisa se autenticar primeiro!")
            }
        }
    }
}
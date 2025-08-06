package com.caiodev.minecraftgamesx.listener

import com.caiodev.minecraftgamesx.core.database.DatabaseManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent

class ChatListener(private val databaseManager: DatabaseManager) : Listener {
    @EventHandler
    fun onPlayerChat(event: AsyncPlayerChatEvent) {
        val player = event.player
        val level = databaseManager.getPlayerLevel(player.uniqueId)
        val tag = databaseManager.getPlayerTag(player.uniqueId)
        val levelDisplay = when {
            level >= 100 -> "§6[$level✹]"
            level >= 50 -> "§b[$level✯]"
            level >= 10 -> "§a[$level✪]"
            else -> "§f[$level★]"
        }
        val tagDisplay = when (tag.lowercase()) {
            "membro" -> "§7${player.name}"
            else -> "§7[$tag] ${player.name}"
        }
        event.format = "$levelDisplay $tagDisplay: %2\$s"
    }
}
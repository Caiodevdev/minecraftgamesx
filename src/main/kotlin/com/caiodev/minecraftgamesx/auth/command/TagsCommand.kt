package com.caiodev.minecraftgamesx.commands

import com.caiodev.minecraftgamesx.core.database.DatabaseManager
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class TagsCommand(private val databaseManager: DatabaseManager) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val tags = databaseManager.getAvailableTags()
        sender.sendMessage("§f✦ §7Tags disponíveis: §e${tags.joinToString(", ")}")
        return true
    }
}
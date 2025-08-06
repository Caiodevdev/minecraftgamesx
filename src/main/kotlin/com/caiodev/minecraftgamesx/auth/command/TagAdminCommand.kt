package com.caiodev.minecraftgamesx.commands

import com.caiodev.minecraftgamesx.core.database.DatabaseManager
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class TagAdminCommand(private val databaseManager: DatabaseManager) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("lobby.tagadmin")) {
            sender.sendMessage("§cVocê não tem permissão para usar este comando!")
            return true
        }
        if (args.isEmpty()) {
            sender.sendMessage("§cUso: /tagadmin <set|remove|list> [jogador] [tag]")
            return true
        }
        when (args[0].lowercase()) {
            "set" -> {
                if (args.size != 3) {
                    sender.sendMessage("§cUso: /tagadmin set <jogador> <tag>")
                    return true
                }
                val target = sender.server.getPlayer(args[1])
                if (target == null) {
                    sender.sendMessage("§cJogador ${args[1]} não encontrado!")
                    return true
                }
                val tag = args[2].lowercase()
                if (tag !in databaseManager.getAvailableTags()) {
                    sender.sendMessage("§cTag inválida! Tags disponíveis: ${databaseManager.getAvailableTags().joinToString(", ")}")
                    return true
                }
                databaseManager.setPlayerTag(target.uniqueId, tag)
                sender.sendMessage("§f✦ §7Tag §e$tag §7definida para ${target.name}.")
                target.sendMessage("§f✦ §7Sua tag foi definida para §e$tag§7.")
            }
            "remove" -> {
                if (args.size != 2) {
                    sender.sendMessage("§cUso: /tagadmin remove <jogador>")
                    return true
                }
                val target = sender.server.getPlayer(args[1])
                if (target == null) {
                    sender.sendMessage("§cJogador ${args[1]} não encontrado!")
                    return true
                }
                databaseManager.removePlayerTag(target.uniqueId)
                sender.sendMessage("§f✦ §7Tag de ${target.name} removida, revertida para 'membro'.")
                target.sendMessage("§f✦ §7Sua tag foi revertida para §emembro§7.")
            }
            "list" -> {
                val tags = databaseManager.getAvailableTags()
                sender.sendMessage("§f✦ §7Tags disponíveis: §e${tags.joinToString(", ")}")
            }
            else -> {
                sender.sendMessage("§cUso: /tagadmin <set|remove|list> [jogador] [tag]")
            }
        }
        return true
    }
}
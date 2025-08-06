package com.caiodev.minecraftgamesx.command

import com.caiodev.minecraftgamesx.Minecraftgamesx
import com.caiodev.minecraftgamesx.core.database.DatabaseManager
import com.caiodev.minecraftgamesx.lobby.ScoreboardManager
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class LevelCommand(
    private val databaseManager: DatabaseManager,
    private val scoreboard: ScoreboardManager
) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("lobby.setlevel")) {
            sender.sendMessage("§cVocê não tem permissão para usar este comando!")
            return true
        }
        if (args.size != 2) {
            sender.sendMessage("§cUso: /setlevel <jogador> <nível>")
            return true
        }
        val target = sender.server.getPlayer(args[0])
        if (target == null) {
            sender.sendMessage("§cJogador ${args[0]} não encontrado!")
            return true
        }
        val level = args[1].toIntOrNull()
        if (level == null || level < 0) {
            sender.sendMessage("§cNível deve ser um número inteiro não negativo!")
            return true
        }
        databaseManager.setPlayerLevel(target.uniqueId, level)
        sender.sendMessage("§f✦ §7Nível de ${target.name} definido para $level.")
        target.sendMessage("§f✦ §7Seu nível foi definido para $level.")
        scoreboard.setupScoreboard(target, (sender.server.pluginManager.getPlugin("minecraftgamesx") as Minecraftgamesx), databaseManager)
        return true
    }
}
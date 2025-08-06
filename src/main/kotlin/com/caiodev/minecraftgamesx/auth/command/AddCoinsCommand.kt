package com.caiodev.minecraftgamesx.commands

import com.caiodev.minecraftgamesx.Minecraftgamesx
import com.caiodev.minecraftgamesx.core.database.DatabaseManager
import com.caiodev.minecraftgamesx.lobby.ScoreboardManager
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class AddCoinsCommand(
    private val databaseManager: DatabaseManager,
    private val scoreboard: ScoreboardManager
) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("lobby.addcoins")) {
            sender.sendMessage("§cVocê não tem permissão para usar este comando!")
            return true
        }
        if (args.size != 2) {
            sender.sendMessage("§cUso: /addcoins <jogador> <quantidade>")
            return true
        }
        val target = sender.server.getPlayer(args[0])
        if (target == null) {
            sender.sendMessage("§cJogador ${args[0]} não encontrado!")
            return true
        }
        val coins = args[1].toIntOrNull()
        if (coins == null || coins < 0) {
            sender.sendMessage("§cA quantidade deve ser um número inteiro não negativo!")
            return true
        }
        val plugin = sender.server.pluginManager.getPlugin("minecraftgamesx") as Minecraftgamesx
        scoreboard.addCoins(target, coins, plugin, databaseManager)
        sender.sendMessage("§f✦ §7Adicionado $coins coins para ${target.name}. Saldo: ${scoreboard.getCoins(target, databaseManager)}.")
        return true
    }
}
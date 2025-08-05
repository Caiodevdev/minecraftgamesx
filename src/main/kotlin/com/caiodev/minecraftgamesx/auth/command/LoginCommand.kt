package com.caiodev.minecraftgamesx.auth.command

import com.caiodev.minecraftgamesx.auth.AuthManager
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class LoginCommand(private val authManager: AuthManager) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("§cEste comando é apenas para jogadores!")
            return true
        }

        if (authManager.isAuthenticated(sender)) {
            sender.sendMessage("§eMinecraftGamesX §7- Autenticação")
            sender.sendMessage("§7Você já está autenticado!")
            return true
        }

        if (!authManager.isRegistered(sender)) {
            sender.sendMessage("§eMinecraftGamesX §7- Autenticação")
            sender.sendMessage("§7Você não está registrado! Use §b/register <sua senha> <confirme sua senha>.")
            sender.sendTitle("§eBem-vindo ao MinecraftGamesX!", "§7Use §b/register <senha> <confirme>", 10, 70, 20)
            return true
        }

        if (args.size != 1) {
            sender.sendMessage("§eMinecraftGamesX §7- Autenticação")
            sender.sendMessage("§7Use: §b/login <sua senha>")
            sender.sendTitle("§eMinecraftGamesX!", "§7Use §b/login <sua senha>", 10, 70, 20)
            return true
        }

        if (authManager.loginPlayer(sender, args[0])) {
            authManager.authenticate(sender)
            sender.sendMessage("§eMinecraftGamesX §7- Autenticação")
            sender.sendMessage("§7Login realizado com sucesso!")
            sender.sendTitle("§eBem-vindo de volta!", "§7Você está logado!", 10, 70, 20)
        } else {
            sender.sendMessage("§eMinecraftGamesX §7- Autenticação")
            sender.sendMessage("§7Senha incorreta! Tente novamente.")
            sender.sendTitle("§eErro no Login!", "§7Senha incorreta!", 10, 70, 20)
        }
        return true
    }
}
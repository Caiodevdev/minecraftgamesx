package com.caiodev.minecraftgamesx.auth.command

import com.caiodev.minecraftgamesx.auth.AuthManager
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class RegisterCommand(private val authManager: AuthManager) : CommandExecutor {
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

        if (authManager.isRegistered(sender)) {
            sender.sendMessage("§eMinecraftGamesX §7- Autenticação")
            sender.sendMessage("§7Você já está registrado! Use §b/login <sua senha> §7para fazer login.")
            return true
        }

        if (args.size != 2) {
            sender.sendMessage("§eMinecraftGamesX §7- Autenticação")
            sender.sendMessage("§7Use: §b/register <sua senha> <confirme sua senha>")
            sender.sendTitle("§eBem-vindo ao MinecraftGamesX!", "§7Use §b/register <senha> <confirme>", 10, 70, 20)
            return true
        }

        if (args[0] != args[1]) {
            sender.sendMessage("§eMinecraftGamesX §7- Autenticação")
            sender.sendMessage("§7As senhas não coincidem!")
            return true
        }

        if (authManager.registerPlayer(sender, args[0])) {
            authManager.authenticate(sender)
            sender.sendMessage("§eMinecraftGamesX §7- Autenticação")
            sender.sendMessage("§7Registro concluído com sucesso! Você está logado.")
            sender.sendTitle("§eRegistro Concluído!", "§7Bem-vindo ao MinecraftGamesX!", 10, 70, 20)
        } else {
            sender.sendMessage("§eMinecraftGamesX §7- Autenticação")
            sender.sendMessage("§7Erro ao registrar. Tente novamente.")
        }
        return true
    }
}
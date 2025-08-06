package com.caiodev.minecraftgamesx.auth.command

import com.caiodev.minecraftgamesx.auth.AuthManager
import com.caiodev.minecraftgamesx.lobby.InventoryManager
import com.caiodev.minecraftgamesx.lobby.ScoreboardManager
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class LoginCommand(private val authManager: AuthManager, private val plugin: JavaPlugin) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("§cEste comando é apenas para jogadores!")
            return true
        }

        if (authManager.isAuthenticated(sender)) {
            sender.sendMessage("§f✦ §7Você já está §e§lautenticado§7!")
            return true
        }

        if (!authManager.isRegistered(sender)) {
            sender.sendMessage("§f✦ §7Você não está §e§lregistrado§7!")
            sender.sendMessage("§7➜ Use §b§l/register <sua senha> <confirme sua senha> §7para se registrar.")
            sender.sendTitle("§e✦ §lMinecraftGamesX", "§7Use §b§l/register <senha> <confirme>", 10, 70, 20)
            return true
        }

        if (args.size != 1) {
            sender.sendMessage("§f✦ §7Uso incorreto do comando!")
            sender.sendMessage("§7➜ Use §b§l/login <sua senha> §7para fazer login.")
            sender.sendTitle("§e✦ §lMinecraftGamesX", "§7Use §b§l/login <sua senha>", 10, 70, 20)
            return true
        }

        if (authManager.loginPlayer(sender, args[0])) {
            authManager.authenticate(sender)
            InventoryManager.setupPlayerInventory(sender)
            ScoreboardManager.setupScoreboard(sender, plugin)
            sender.sendMessage("§f✦ §7Login realizado com §e§lsucesso§7!")
            sender.sendMessage("§7➜ Bem-vindo de volta, §e§l${sender.name}§7!")
            sender.sendTitle("§e✦ §lBem-vindo de volta!", "§7Você está §e§llogado§7!", 10, 70, 20)
        } else {
            sender.sendMessage("§f✦ §7§lSenha incorreta§7!")
            sender.sendMessage("§7➜ Tente novamente com a §b§lsenha correta§7.")
            sender.sendTitle("§e✦ §lErro no Login!", "§7§lSenha incorreta§7!", 10, 70, 20)
        }
        return true
    }
}
package com.caiodev.minecraftgamesx.auth.command

import com.caiodev.minecraftgamesx.auth.AuthManager
import com.caiodev.minecraftgamesx.lobby.InventoryManager
import com.caiodev.minecraftgamesx.lobby.ScoreboardManager
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class RegisterCommand(private val authManager: AuthManager, private val plugin: JavaPlugin) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("§cEste comando é apenas para jogadores!")
            return true
        }

        if (authManager.isAuthenticated(sender)) {
            sender.sendMessage("§f✦ §7Você já está §e§lautenticado§7!")
            return true
        }

        if (authManager.isRegistered(sender)) {
            sender.sendMessage("§f✦ §7Você já está §e§lregistrado§7!")
            sender.sendMessage("§7➜ Use §b§l/login <sua senha> §7para fazer login.")
            return true
        }

        if (args.size != 2) {
            sender.sendMessage("§f✦ §7Uso incorreto do comando!")
            sender.sendMessage("§7➜ Use §b§l/register <sua senha> <confirme sua senha> §7para se registrar.")
            sender.sendTitle("§e✦ §lMinecraftGamesX", "§7Use §b§l/register <senha> <confirme>", 10, 70, 20)
            return true
        }

        if (args[0] != args[1]) {
            sender.sendMessage("§f✦ §7As §e§lsenhas §7não §e§lcoincidem§7!")
            sender.sendMessage("§7➜ Tente novamente com §b§lsenhas iguais§7.")
            return true
        }

        if (authManager.registerPlayer(sender, args[0])) {
            authManager.authenticate(sender)
            InventoryManager.setupPlayerInventory(sender)
            ScoreboardManager.setupScoreboard(sender, plugin)
            sender.sendMessage("§f✦ §7Registro concluído com §e§lsucesso§7!")
            sender.sendMessage("§7➜ Você está §b§llogado §7e pronto para jogar no §e§lMinecraftGamesX§7!")
            sender.sendTitle("§e✦ §lRegistro Concluído!", "§7Bem-vindo ao §e§lMinecraftGamesX§7!", 10, 70, 20)
        } else {
            sender.sendMessage("§f✦ §7Erro ao §e§lregistrar§7!")
            sender.sendMessage("§7➜ Tente novamente ou contate um §b§ladministrador§7.")
        }
        return true
    }
}
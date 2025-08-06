package com.caiodev.minecraftgamesx.command

import com.caiodev.minecraftgamesx.lobby.NPCManager
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class AddNPCCommand(private val plugin: JavaPlugin) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("§cEste comando é apenas para jogadores!")
            return true
        }

        if (!sender.hasPermission("minecraftgamesx.admin")) {
            sender.sendMessage("§f✦ §7Você não tem §e§lpermissão §7para usar este comando!")
            return true
        }

        val npc = NPCManager.createNPC(sender.location, plugin)
        if (npc != null) {
            sender.sendMessage("§f✦ §7NPC §e§lJogar Agora §7criado com sucesso na sua localização!")
        } else {
            sender.sendMessage("§f✦ §7Erro ao criar o NPC! Verifique a localização.")
        }
        return true
    }
}
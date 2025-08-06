package com.caiodev.minecraftgamesx.command

import com.caiodev.minecraftgamesx.lobby.NPCManager
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin

class RemoveNPCCommand(private val plugin: JavaPlugin) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("minecraftgamesx.admin")) {
            sender.sendMessage("§f✦ §7Você não tem §e§lpermissão §7para usar este comando!")
            return true
        }

        NPCManager.removeNPCs(plugin)
        sender.sendMessage("§f✦ §7Todos os NPCs §e§lJogar Agora §7foram removidos!")
        return true
    }
}
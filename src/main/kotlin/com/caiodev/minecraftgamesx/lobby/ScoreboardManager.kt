package com.caiodev.minecraftgamesx.lobby

import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Scoreboard
import org.bukkit.scoreboard.ScoreboardManager
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable

object ScoreboardManager {
    private val scoreboards = mutableMapOf<Player, Scoreboard>()

    fun setupScoreboard(player: Player, plugin: JavaPlugin) {
        val scoreboardManager = plugin.server.scoreboardManager ?: return
        val scoreboard = scoreboardManager.mainScoreboard
        val objective = scoreboard.getObjective("lobby") ?: scoreboard.registerNewObjective("lobby", "dummy", Component.text("§b§lSKY WARS"))
        objective.displaySlot = DisplaySlot.SIDEBAR

        // Configurar linhas do scoreboard
        val lines = listOf(
            "§7",
            "§fSeu nível: §7[0§f✪§7]",
            "§f[§7■■■■■■■■■■§f] §7(0/500)",
            "§7 ",
            "§eSolo:",
            "§fVitórias: §7em breve",
            "§fKills: §7em breve",
            "§fWinstreak: §7em breve",
            "§7  ",
            "§fCoins: §7em breve",
            "§fJogadores: §7${plugin.server.onlinePlayers.size}",
            "§7   ",
            "§awww.minecraftgamesx.com.br"
        )

        // Limpar entradas antigas
        scoreboard.entries.forEach { scoreboard.resetScores(it) }

        // Adicionar novas linhas
        lines.forEachIndexed { index, line ->
            val score = objective.getScore(line)
            score.score = lines.size - index
        }

        player.scoreboard = scoreboard
        scoreboards[player] = scoreboard

        // Atualizar scoreboard periodicamente
        object : BukkitRunnable() {
            override fun run() {
                if (!player.isOnline || !scoreboards.containsKey(player)) {
                    cancel()
                    return
                }
                // Atualizar linhas dinâmicas
                val updatedLines = listOf(
                    "§7",
                    "§fSeu nível: §7[0§f✪§7]",
                    "§f[§7■■■■■■■■■■§f] §7(0/500)",
                    "§7 ",
                    "§eSolo:",
                    "§fVitórias: §7em breve",
                    "§fKills: §7em breve",
                    "§fWinstreak: §7em breve",
                    "§7  ",
                    "§fCoins: §7em breve",
                    "§fJogadores: §7${plugin.server.onlinePlayers.size}",
                    "§7   ",
                    "§awww.minecraftgamesx.com.br"
                )
                scoreboard.entries.forEach { scoreboard.resetScores(it) }
                updatedLines.forEachIndexed { index, line ->
                    val score = objective.getScore(line)
                    score.score = updatedLines.size - index
                }
            }
        }.runTaskTimer(plugin, 0L, 20L) // Atualiza a cada 1 segundo
    }

    fun removeScoreboard(player: Player) {
        scoreboards.remove(player)
        player.scoreboard = player.server.scoreboardManager?.mainScoreboard ?: return
    }
}
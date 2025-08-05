package com.caiodev.minecraftgamesx.lobby

import org.bukkit.entity.Player
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Scoreboard
import org.bukkit.scoreboard.ScoreboardManager
import org.bukkit.plugin.java.JavaPlugin

class ScoreboardManager(private val plugin: JavaPlugin) {
    private val scoreboards = mutableMapOf<Player, Scoreboard>()

    fun createScoreboard(player: Player) {
        val scoreboardManager: ScoreboardManager = plugin.server.scoreboardManager
        val scoreboard: Scoreboard = scoreboardManager.newScoreboard
        val objective: Objective = scoreboard.registerNewObjective("lobby", "dummy", "§e§lMinecraftGamesX")
        objective.displaySlot = DisplaySlot.SIDEBAR

        val scores = listOf(
            "§7 ",
            "§f§lJogadores online: §7${plugin.server.onlinePlayers.size}",
            "§f§lCoins: §70",
            "§7  ",
            "§b§lwww.minecraftgamesx.com.br"
        )

        scores.forEachIndexed { index, line ->
            objective.getScore(line).score = scores.size - index
        }

        player.scoreboard = scoreboard
        scoreboards[player] = scoreboard
    }

    fun removeScoreboard(player: Player) {
        player.scoreboard = plugin.server.scoreboardManager.newScoreboard
        scoreboards.remove(player)
    }

    fun updateScoreboard(player: Player) {
        if (scoreboards.containsKey(player)) {
            val scoreboard = scoreboards[player] ?: return
            val objective = scoreboard.getObjective("lobby") ?: return
            scoreboard.resetScores("§f§lJogadores online: §7${plugin.server.onlinePlayers.size - 1}")
            objective.getScore("§f§lJogadores online: §7${plugin.server.onlinePlayers.size}").score = 4
        }
    }
}
package com.caiodev.minecraftgamesx.lobby

import com.caiodev.minecraftgamesx.Minecraftgamesx
import com.caiodev.minecraftgamesx.core.database.DatabaseManager
import net.kyori.adventure.text.Component
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Scoreboard
import org.bukkit.FireworkEffect
import org.bukkit.Color
import org.bukkit.entity.Firework
import org.bukkit.inventory.meta.FireworkMeta

object ScoreboardManager {
    private val scoreboards = mutableMapOf<Player, Scoreboard>()
    private const val XP_PER_LEVEL = 500

    fun setupScoreboard(player: Player, plugin: Minecraftgamesx, databaseManager: DatabaseManager) {
        val scoreboardManager = plugin.server.scoreboardManager ?: return
        val scoreboard = scoreboardManager.mainScoreboard
        val objective = scoreboard.getObjective("lobby") ?: scoreboard.registerNewObjective("lobby", "dummy", Component.text("§e§lMCGAMES X"))
        objective.displaySlot = DisplaySlot.SIDEBAR

        scoreboards[player] = scoreboard
        updateScoreboard(player, plugin, scoreboard, databaseManager)

        // Atualizar scoreboard periodicamente
        object : BukkitRunnable() {
            override fun run() {
                if (!player.isOnline || !scoreboards.containsKey(player)) {
                    cancel()
                    return
                }
                updateScoreboard(player, plugin, scoreboard, databaseManager)
            }
        }.runTaskTimer(plugin, 0L, 100L) // Atualiza a cada 5 segundos
    }

    fun updateScoreboard(player: Player, plugin: Minecraftgamesx, databaseManager: DatabaseManager) {
        val scoreboard = scoreboards[player] ?: return
        updateScoreboard(player, plugin, scoreboard, databaseManager)
    }

    private fun updateScoreboard(player: Player, plugin: Minecraftgamesx, scoreboard: Scoreboard, databaseManager: DatabaseManager) {
        val objective = scoreboard.getObjective("lobby") ?: return
        val level = databaseManager.getPlayerLevel(player.uniqueId)
        val xp = databaseManager.getPlayerXP(player.uniqueId)
        val coins = databaseManager.getPlayerCoins(player.uniqueId)
        val levelDisplay = when {
            level >= 100 -> "§6[$level✹]"
            level >= 50 -> "§b[$level✯]"
            level >= 10 -> "§a[$level✪]"
            else -> "§f[$level★]"
        }
        val filledBlocks = (xp.toDouble() / XP_PER_LEVEL * 10).toInt().coerceIn(0, 10)
        val progressBar = "§f[" + "§a■".repeat(filledBlocks) + "§7■".repeat(10 - filledBlocks) + "§f] §7($xp/$XP_PER_LEVEL)"

        val lines = listOf(
            "§7",
            "§fSeu nível: $levelDisplay",
            progressBar,
            "§7 ",
            "§eSolo:",
            "§fVitórias: §7em breve",
            "§fKills: §7em breve",
            "§fWinstreak: §7em breve",
            "§7  ",
            "§fCoins: §e$coins",
            "§fJogadores: §7${player.server.onlinePlayers.size}/100",
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
    }

    fun addXP(player: Player, amount: Int, plugin: Minecraftgamesx, databaseManager: DatabaseManager) {
        val currentXP = databaseManager.getPlayerXP(player.uniqueId)
        var newXP = currentXP + amount
        var level = databaseManager.getPlayerLevel(player.uniqueId)

        while (newXP >= XP_PER_LEVEL) {
            newXP -= XP_PER_LEVEL
            level++
            databaseManager.setPlayerLevel(player.uniqueId, level)
            // Feedback visual e sonoro
            player.sendMessage("§f✦ §7Você subiu para o nível §e$level§7!")
            player.sendTitle("§eParabéns!", "§7Você alcançou o nível §e$level§7!", 10, 70, 20)
            player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f)
            // Lançar fogos de artifício
            val firework = player.world.spawn(player.location, Firework::class.java)
            val meta = firework.fireworkMeta
            meta.addEffect(
                FireworkEffect.builder()
                    .withColor(Color.YELLOW)
                    .withFade(Color.WHITE)
                    .with(FireworkEffect.Type.STAR)
                    .trail(true)
                    .build()
            )
            meta.power = 1
            firework.fireworkMeta = meta
            plugin.server.scheduler.runTaskLater(plugin, Runnable { firework.detonate() }, 20L)
        }

        databaseManager.setPlayerXP(player.uniqueId, newXP.coerceIn(0, XP_PER_LEVEL - 1))
        updateScoreboard(player, plugin, databaseManager)
    }

    fun addCoins(player: Player, amount: Int, plugin: Minecraftgamesx, databaseManager: DatabaseManager) {
        val currentCoins = databaseManager.getPlayerCoins(player.uniqueId)
        val newCoins = (currentCoins + amount).coerceAtLeast(0)
        databaseManager.setPlayerCoins(player.uniqueId, newCoins)
        player.sendMessage("§f✦ §7Você recebeu §e$amount coins§7! Saldo: §e$newCoins§7.")
        player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f)
        updateScoreboard(player, plugin, databaseManager)
    }

    fun getXP(player: Player, databaseManager: DatabaseManager): Int {
        return databaseManager.getPlayerXP(player.uniqueId)
    }

    fun getCoins(player: Player, databaseManager: DatabaseManager): Int {
        return databaseManager.getPlayerCoins(player.uniqueId)
    }

    fun removeScoreboard(player: Player) {
        scoreboards.remove(player)
        player.scoreboard = player.server.scoreboardManager?.mainScoreboard ?: return
    }
}
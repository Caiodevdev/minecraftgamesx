package com.caiodev.minecraftgamesx.collectibles

import com.caiodev.minecraftgamesx.Minecraftgamesx
import com.caiodev.minecraftgamesx.core.database.DatabaseManager
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.util.UUID
import java.util.logging.Level
import kotlin.random.Random

class ParticleManager(
    private val plugin: Minecraftgamesx,
    private val databaseManager: DatabaseManager
) {
    private val activeParticles = mutableMapOf<UUID, ParticleType>()
    private val particleTask: BukkitRunnable

    init {
        // Iniciar tarefa para exibir partículas ativas
        particleTask = object : BukkitRunnable() {
            override fun run() {
                activeParticles.forEach { (uuid, particleType) ->
                    val player = plugin.server.getPlayer(uuid) ?: return@forEach
                    if (player.isOnline) {
                        when (particleType) {
                            ParticleType.FLAME -> {
                                player.world.spawnParticle(
                                    particleType.particle,
                                    player.location.subtract(0.0, 0.2, 0.0),
                                    5,
                                    0.2,
                                    0.0,
                                    0.2,
                                    0.0
                                )
                            }
                            ParticleType.END_ROD -> {
                                val loc = player.location.add(0.0, 1.0, 0.0)
                                for (i in 0..3) {
                                    val angle = i * Math.PI / 2
                                    val x = Math.cos(angle) * 0.5
                                    val z = Math.sin(angle) * 0.5
                                    player.world.spawnParticle(
                                        particleType.particle,
                                        loc.clone().add(x, 0.0, z),
                                        1,
                                        0.0,
                                        0.0,
                                        0.0,
                                        0.0
                                    )
                                }
                            }
                            ParticleType.SMOKE_NORMAL -> {
                                player.world.spawnParticle(
                                    particleType.particle,
                                    player.location.add(0.0, 1.0, 0.0),
                                    10,
                                    0.5,
                                    0.5,
                                    0.5,
                                    0.0
                                )
                            }
                            ParticleType.FIREWORKS_SPARK -> {
                                player.world.spawnParticle(
                                    particleType.particle,
                                    player.location.add(Random.nextDouble(-0.5, 0.5), 1.0 + Random.nextDouble(0.0, 0.5), Random.nextDouble(-0.5, 0.5)),
                                    3,
                                    0.0,
                                    0.0,
                                    0.0,
                                    0.0
                                )
                            }
                            ParticleType.HEART -> {
                                player.world.spawnParticle(
                                    particleType.particle,
                                    player.location.add(Random.nextDouble(-0.3, 0.3), 1.5, Random.nextDouble(-0.3, 0.3)),
                                    1,
                                    0.0,
                                    0.0,
                                    0.0,
                                    0.0
                                )
                            }
                        }
                    }
                }
            }
        }.apply {
            runTaskTimer(plugin, 0L, 2L) // Executa a cada 2 ticks
        }
    }

    fun hasParticle(player: Player, particleType: ParticleType): Boolean {
        return databaseManager.hasCollectible(player.uniqueId, "particle_${particleType.name.lowercase()}")
    }

    fun purchaseParticle(player: Player, particleType: ParticleType): Boolean {
        val coins = databaseManager.getPlayerCoins(player.uniqueId)
        if (coins < particleType.cost) {
            player.sendMessage("§f✦ §7Você não tem coins suficientes! Precisos: §e${particleType.cost} coins§7.")
            plugin.logger.info("Jogador ${player.name} tentou comprar partícula ${particleType.name} sem coins suficientes")
            return false
        }
        if (hasParticle(player, particleType)) {
            player.sendMessage("§f✦ §7Você já possui a partícula §e${particleType.displayName}§7!")
            plugin.logger.info("Jogador ${player.name} tentou comprar partícula ${particleType.name} que já possui")
            return false
        }
        databaseManager.setPlayerCoins(player.uniqueId, coins - particleType.cost)
        databaseManager.addCollectible(player.uniqueId, "particle_${particleType.name.lowercase()}")
        player.sendMessage("§f✦ §7Você comprou a partícula §e${particleType.displayName}§7 por §e${particleType.cost} coins§7!")
        player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f)
        plugin.logger.info("Jogador ${player.name} comprou partícula ${particleType.name} por ${particleType.cost} coins")
        return true
    }

    fun activateParticle(player: Player, particleType: ParticleType): Boolean {
        if (!hasParticle(player, particleType)) {
            player.sendMessage("§f✦ §7Você não possui a partícula §e${particleType.displayName}§7!")
            plugin.logger.info("Jogador ${player.name} tentou ativar partícula ${particleType.name} não possuída")
            return false
        }
        deactivateParticle(player)
        activeParticles[player.uniqueId] = particleType
        databaseManager.setActiveParticle(player.uniqueId, particleType.name.lowercase())
        player.sendMessage("§f✦ §7Partícula §e${particleType.displayName}§7 ativada!")
        player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f)
        plugin.logger.info("Jogador ${player.name} ativou partícula ${particleType.name}")
        return true
    }

    fun deactivateParticle(player: Player): Boolean {
        if (!activeParticles.containsKey(player.uniqueId)) {
            player.sendMessage("§f✦ §7Você não tem nenhuma partícula ativa!")
            plugin.logger.info("Jogador ${player.name} tentou desativar partícula sem partícula ativa")
            return false
        }
        activeParticles.remove(player.uniqueId)
        databaseManager.setActiveParticle(player.uniqueId, null)
        player.sendMessage("§f✦ §7Partícula desativada!")
        plugin.logger.info("Jogador ${player.name} desativou partícula")
        return true
    }

    fun getActiveParticle(player: Player): ParticleType? {
        return activeParticles[player.uniqueId]
    }

    fun loadActiveParticle(player: Player) {
        val activeParticle = databaseManager.getActiveParticle(player.uniqueId)
        if (activeParticle != null) {
            val particleType = ParticleType.entries.find { it.name.lowercase() == activeParticle }
            if (particleType != null) {
                activeParticles[player.uniqueId] = particleType
                plugin.logger.info("Partícula ${particleType.name} carregada para jogador ${player.name}")
            }
        }
    }

    fun getPlayerCoins(player: Player): Int {
        return databaseManager.getPlayerCoins(player.uniqueId)
    }

    fun cleanup() {
        particleTask.cancel()
        activeParticles.clear()
        plugin.logger.info("ParticleManager finalizado")
    }
}
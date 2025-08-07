package com.caiodev.minecraftgamesx.collectibles

import com.caiodev.minecraftgamesx.Minecraftgamesx
import com.caiodev.minecraftgamesx.core.database.DatabaseManager
import net.kyori.adventure.text.Component
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.entity.Tameable
import java.util.UUID
import java.util.logging.Level

class PetManager(
    private val plugin: Minecraftgamesx,
    private val databaseManager: DatabaseManager
) {
    private val activePets = mutableMapOf<UUID, Pair<PetType, UUID>>()

    fun hasPet(player: Player, petType: PetType): Boolean {
        return databaseManager.hasCollectible(player.uniqueId, "pet_${petType.name.lowercase()}")
    }

    fun purchasePet(player: Player, petType: PetType): Boolean {
        val coins = databaseManager.getPlayerCoins(player.uniqueId)
        if (coins < petType.cost) {
            player.sendMessage("§f✦ §7Você não tem coins suficientes! Precisos: §e${petType.cost} coins§7.")
            plugin.logger.info("Jogador ${player.name} tentou comprar pet ${petType.name} sem coins suficientes")
            return false
        }
        if (hasPet(player, petType)) {
            player.sendMessage("§f✦ §7Você já possui o pet §e${petType.displayName}§7!")
            plugin.logger.info("Jogador ${player.name} tentou comprar pet ${petType.name} que já possui")
            return false
        }
        databaseManager.setPlayerCoins(player.uniqueId, coins - petType.cost)
        databaseManager.addCollectible(player.uniqueId, "pet_${petType.name.lowercase()}")
        player.sendMessage("§f✦ §7Você comprou o pet §e${petType.displayName}§7 por §e${petType.cost} coins§7!")
        player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f)
        plugin.logger.info("Jogador ${player.name} comprou pet ${petType.name} por ${petType.cost} coins")
        return true
    }

    fun activatePet(player: Player, petType: PetType): Boolean {
        if (!hasPet(player, petType)) {
            player.sendMessage("§f✦ §7Você não possui o pet §e${petType.displayName}§7!")
            plugin.logger.info("Jogador ${player.name} tentou ativar pet ${petType.name} não possuído")
            return false
        }
        // Desativar pet atual, se houver
        deactivatePet(player)
        // Spawnar o pet
        val petEntity = player.world.spawnEntity(player.location, petType.entityType)
        petEntity.customName(Component.text(petType.displayName))
        petEntity.isCustomNameVisible = true
        if (petEntity is Tameable) {
            petEntity.owner = player
            petEntity.isTamed = true
        }
        activePets[player.uniqueId] = Pair(petType, petEntity.uniqueId)
        databaseManager.setActivePet(player.uniqueId, petType.name.lowercase())
        player.sendMessage("§f✦ §7Pet §e${petType.displayName}§7 ativado!")
        player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f)
        plugin.logger.info("Jogador ${player.name} ativou pet ${petType.name}")
        return true
    }

    fun deactivatePet(player: Player): Boolean {
        val activePet = activePets[player.uniqueId]
        if (activePet == null) {
            player.sendMessage("§f✦ §7Você não tem nenhum pet ativo!")
            plugin.logger.info("Jogador ${player.name} tentou desativar pet sem pet ativo")
            return false
        }
        val petEntity = player.world.getEntity(activePet.second)
        petEntity?.remove()
        activePets.remove(player.uniqueId)
        databaseManager.setActivePet(player.uniqueId, null)
        player.sendMessage("§f✦ §7Pet desativado!")
        plugin.logger.info("Jogador ${player.name} desativou pet")
        return true
    }

    fun getActivePet(player: Player): PetType? {
        return activePets[player.uniqueId]?.first
    }

    fun loadActivePet(player: Player) {
        val activePetId = databaseManager.getActivePet(player.uniqueId)
        if (activePetId != null) {
            val petType = PetType.entries.find { it.name.lowercase() == activePetId }
            if (petType != null && hasPet(player, petType)) {
                val petEntity = player.world.spawnEntity(player.location, petType.entityType)
                petEntity.customName(Component.text(petType.displayName))
                petEntity.isCustomNameVisible = true
                if (petEntity is Tameable) {
                    petEntity.owner = player
                    petEntity.isTamed = true
                }
                activePets[player.uniqueId] = Pair(petType, petEntity.uniqueId)
                plugin.logger.info("Pet ${petType.name} carregado para jogador ${player.name}")
            }
        }
    }

    fun getPlayerCoins(player: Player): Int {
        return databaseManager.getPlayerCoins(player.uniqueId)
    }

    fun cleanup() {
        activePets.forEach { (playerUuid, petData) ->
            val player = plugin.server.getPlayer(playerUuid)
            if (player != null) {
                val petEntity = player.world.getEntity(petData.second)
                petEntity?.remove()
            }
        }
        activePets.clear()
        plugin.logger.info("PetManager finalizado")
    }
}
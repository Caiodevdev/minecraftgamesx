package com.caiodev.minecraftgamesx.collectibles

import com.caiodev.minecraftgamesx.Minecraftgamesx
import org.bukkit.entity.Player
import java.util.logging.Level

class CollectiblesManager(private val plugin: Minecraftgamesx) {

    fun openPetsMenu(player: Player) {
        PetMenu.openPetMenu(player, plugin.petManager)
        plugin.logger.info("Jogador ${player.name} abriu o menu de Pets")
    }

    fun openParticlesMenu(player: Player) {
        ParticleMenu.openParticleMenu(player, plugin.particleManager)
        plugin.logger.info("Jogador ${player.name} abriu o menu de Partículas")
    }
}
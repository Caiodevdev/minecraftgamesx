package com.caiodev.minecraftgamesx.collectibles

import org.bukkit.Material
import org.bukkit.Particle

enum class ParticleType(
    val displayName: String,
    val material: Material,
    val cost: Int,
    val description: String,
    val particle: Particle
) {
    FLAME(
        "§e§lTrilha de Chamas",
        Material.BLAZE_POWDER,
        600,
        "§7Um rastro ardente que te segue!",
        Particle.FLAME
    ),
    END_ROD(
        "§e§lAurora Mística",
        Material.END_ROD,
        800,
        "§7Um brilho etéreo como uma aurora mágica!",
        Particle.END_ROD
    ),
    SMOKE_NORMAL(
        "§e§lNuvem de Fumaça",
        Material.GUNPOWDER,
        500,
        "§7Uma nuvem misteriosa que te envolve!",
        Particle.SMOKE
    ),
    FIREWORKS_SPARK(
        "§e§lExplosão Estelar",
        Material.FIREWORK_ROCKET,
        1000,
        "§7Faíscas brilhantes como uma chuva de estrelas!",
        Particle.FIREWORK
    ),
    HEART(
        "§e§lCoração Encantado",
        Material.POPPY,
        700,
        "§7Corações flutuantes cheios de charme!",
        Particle.HEART
    )
}
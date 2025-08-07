package com.caiodev.minecraftgamesx.collectibles

import org.bukkit.Material
import org.bukkit.entity.EntityType

enum class PetType(
    val displayName: String,
    val material: Material,
    val cost: Int,
    val description: String,
    val entityType: EntityType
) {
    RABBIT("§e§lCoelho Saltitante", Material.RABBIT_FOOT, 500, "§7Um coelho fofinho que pula ao seu lado!", EntityType.RABBIT),
    CAT("§e§lGato Mágico", Material.COD, 750, "§7Um felino elegante com um toque de mistério!", EntityType.CAT),
    WOLF("§e§lLobo Feroz", Material.BONE, 1000, "§7Um lobo leal que te segue com coragem!", EntityType.WOLF)
}
package com.caiodev.minecraftgamesx.collectibles

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.NamespacedKey

object ParticleMenu {
    private const val PARTICLE_MENU_TITLE = "§e✦ §lPartículas"
    private const val CONFIRM_PURCHASE_TITLE = "§e✦ §lConfirmar Compra"
    private const val PARTICLE_ACTION_KEY = "particle_action"

    fun openParticleMenu(player: Player, particleManager: ParticleManager) {
        val inventory = Bukkit.createInventory(null, 27, Component.text(PARTICLE_MENU_TITLE))

        // Adicionar partículas disponíveis
        ParticleType.entries.forEachIndexed { index, particleType ->
            val isOwned = particleManager.hasParticle(player, particleType)
            val isActive = particleManager.getActiveParticle(player) == particleType
            val lore = mutableListOf(particleType.description)
            if (isOwned) {
                lore.add("§aVocê possui essa partícula")
                lore.add(if (isActive) "§aClique para Desativar" else "§eClique para Ativar")
            } else {
                lore.add("§7Custo: §e${particleType.cost} coins")
                lore.add("§cNão comprada")
                lore.add("§eClique para comprar!")
            }
            val item = createItem(
                particleType.material,
                particleType.displayName,
                lore
            ).apply {
                itemMeta = itemMeta?.apply {
                    persistentDataContainer.set(
                        NamespacedKey.minecraft(PARTICLE_ACTION_KEY),
                        PersistentDataType.STRING,
                        particleType.name.lowercase()
                    )
                }
            }
            inventory.setItem(10 + index, item)
        }

        // Item: Fechar
        val closeItem = createItem(
            Material.BARRIER,
            "§cFechar",
            listOf("§7Clique para fechar o menu.")
        ).apply {
            itemMeta = itemMeta?.apply {
                persistentDataContainer.set(
                    NamespacedKey.minecraft(PARTICLE_ACTION_KEY),
                    PersistentDataType.STRING,
                    "close"
                )
            }
        }
        inventory.setItem(26, closeItem)

        player.openInventory(inventory)
    }

    fun openConfirmPurchaseMenu(player: Player, particleType: ParticleType, particleManager: ParticleManager) {
        val inventory = Bukkit.createInventory(null, 27, Component.text(CONFIRM_PURCHASE_TITLE))

        // Item: Partícula
        val particleItem = createItem(
            particleType.material,
            particleType.displayName,
            listOf(
                particleType.description,
                "§7Custo: §e${particleType.cost} coins",
                "§7Coins disponíveis: §e${particleManager.getPlayerCoins(player)}"
            )
        )
        inventory.setItem(13, particleItem)

        // Item: Confirmar
        val confirmItem = createItem(
            Material.LIME_DYE,
            "§aConfirmar Compra",
            listOf("§7Clique para confirmar a compra da partícula!")
        ).apply {
            itemMeta = itemMeta?.apply {
                persistentDataContainer.set(
                    NamespacedKey.minecraft(PARTICLE_ACTION_KEY),
                    PersistentDataType.STRING,
                    "confirm_${particleType.name.lowercase()}"
                )
            }
        }
        inventory.setItem(11, confirmItem)

        // Item: Cancelar
        val cancelItem = createItem(
            Material.RED_DYE,
            "§cCancelar",
            listOf("§7Clique para cancelar a compra.")
        ).apply {
            itemMeta = itemMeta?.apply {
                persistentDataContainer.set(
                    NamespacedKey.minecraft(PARTICLE_ACTION_KEY),
                    PersistentDataType.STRING,
                    "cancel"
                )
            }
        }
        inventory.setItem(15, cancelItem)

        player.openInventory(inventory)
    }

    private fun createItem(material: Material, name: String, lore: List<String>): ItemStack {
        val item = ItemStack(material)
        item.itemMeta = item.itemMeta?.apply {
            displayName(Component.text(name))
            lore(lore.map { Component.text(it) })
            addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS)
        }
        return item
    }
}
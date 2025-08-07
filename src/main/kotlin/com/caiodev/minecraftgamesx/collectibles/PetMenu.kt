package com.caiodev.minecraftgamesx.collectibles

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.NamespacedKey

object PetMenu {
    private const val PET_MENU_TITLE = "§e✦ §lPets"
    private const val CONFIRM_PURCHASE_TITLE = "§e✦ §lConfirmar Compra"
    private const val PET_ACTION_KEY = "pet_action"

    fun openPetMenu(player: Player, petManager: PetManager) {
        val inventory = Bukkit.createInventory(null, 27, Component.text(PET_MENU_TITLE))

        // Adicionar pets disponíveis
        PetType.entries.forEachIndexed { index, petType ->
            val isOwned = petManager.hasPet(player, petType)
            val isActive = petManager.getActivePet(player) == petType
            val lore = mutableListOf(petType.description)
            if (isOwned) {
                lore.add("§aVocê possui esse pet")
                lore.add(if (isActive) "§aClique para Desativar" else "§eClique para Ativar")
            } else {
                lore.add("§7Custo: §e${petType.cost} coins")
                lore.add("§cNão comprado")
                lore.add("§eClique para comprar!")
            }
            val item = createItem(
                petType.material,
                petType.displayName,
                lore
            ).apply {
                itemMeta = itemMeta?.apply {
                    persistentDataContainer.set(
                        NamespacedKey.minecraft(PET_ACTION_KEY),
                        PersistentDataType.STRING,
                        petType.name.lowercase()
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
                    NamespacedKey.minecraft(PET_ACTION_KEY),
                    PersistentDataType.STRING,
                    "close"
                )
            }
        }
        inventory.setItem(26, closeItem)

        player.openInventory(inventory)
    }

    fun openConfirmPurchaseMenu(player: Player, petType: PetType, petManager: PetManager) {
        val inventory = Bukkit.createInventory(null, 27, Component.text(CONFIRM_PURCHASE_TITLE))

        // Item: Pet
        val petItem = createItem(
            petType.material,
            petType.displayName,
            listOf(
                petType.description,
                "§7Custo: §e${petType.cost} coins",
                "§7Coins disponíveis: §e${petManager.getPlayerCoins(player)}"
            )
        )
        inventory.setItem(13, petItem)

        // Item: Confirmar
        val confirmItem = createItem(
            Material.LIME_DYE,
            "§aConfirmar Compra",
            listOf("§7Clique para confirmar a compra do pet!")
        ).apply {
            itemMeta = itemMeta?.apply {
                persistentDataContainer.set(
                    NamespacedKey.minecraft(PET_ACTION_KEY),
                    PersistentDataType.STRING,
                    "confirm_${petType.name.lowercase()}"
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
                    NamespacedKey.minecraft(PET_ACTION_KEY),
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
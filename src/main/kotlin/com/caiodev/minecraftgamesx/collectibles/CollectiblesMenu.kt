package com.caiodev.minecraftgamesx.collectibles

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.NamespacedKey

object CollectiblesMenu {
    private const val MENU_TITLE = "§e✦ §lColetáveis"
    private const val COLLECTIBLE_ACTION_KEY = "collectible_action"

    fun openCollectiblesMenu(player: Player) {
        val inventory = Bukkit.createInventory(null, 27, Component.text(MENU_TITLE))

        // Item: Pets
        val petsItem = createItem(
            Material.BONE,
            "§e§lPets",
            listOf("§7Escolha seus pets para te acompanhar!")
        ).apply {
            itemMeta = itemMeta?.apply {
                persistentDataContainer.set(
                    NamespacedKey.minecraft(COLLECTIBLE_ACTION_KEY),
                    PersistentDataType.STRING,
                    "pets"
                )
            }
        }
        inventory.setItem(11, petsItem)

        // Item: Partículas
        val particlesItem = createItem(
            Material.BLAZE_POWDER,
            "§e§lPartículas",
            listOf("§7Efeitos de partículas incríveis!")
        ).apply {
            itemMeta = itemMeta?.apply {
                persistentDataContainer.set(
                    NamespacedKey.minecraft(COLLECTIBLE_ACTION_KEY),
                    PersistentDataType.STRING,
                    "particles"
                )
            }
        }
        inventory.setItem(15, particlesItem)

        // Item: Fechar
        val closeItem = createItem(
            Material.BARRIER,
            "§cFechar",
            listOf("§7Clique para fechar o menu.")
        ).apply {
            itemMeta = itemMeta?.apply {
                persistentDataContainer.set(
                    NamespacedKey.minecraft(COLLECTIBLE_ACTION_KEY),
                    PersistentDataType.STRING,
                    "close"
                )
            }
        }
        inventory.setItem(26, closeItem)

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
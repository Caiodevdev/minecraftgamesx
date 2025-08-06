package com.caiodev.minecraftgamesx.lobby

import com.caiodev.minecraftgamesx.auth.AuthManager
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.plugin.java.JavaPlugin

class LobbyListener(private val plugin: JavaPlugin, private val authManager: AuthManager) : Listener {
    private val customInventoryTitles = listOf(
        "§e✦ §lSelecionar Jogo",
        "§e✦ §lMeu Perfil",
        "§e✦ §lMenu do SkyWars",
        "§e✦ §lColetáveis",
        "§e✦ §lSelecionar Lobby"
    )

    private val fixedSlots = listOf(0, 1, 2, 4, 7, 8)

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.player
        if (!authManager.isAuthenticated(player)) {
            event.isCancelled = true
            return
        }

        val item = event.item ?: return
        if (!event.hasItem() || !event.action.isRightClick) return
        event.isCancelled = true // Cancela a ação padrão do item

        val itemName = item.itemMeta?.displayName()?.let {
            LegacyComponentSerializer.legacySection().serialize(it)
        } ?: return
        plugin.logger.info("Interação com item: $itemName") // Log de depuração

        when {
            itemName.contains("Selecionar Jogo") -> InventoryManager.openGameSelectorMenu(player)
            itemName.contains("Meu Perfil") -> InventoryManager.openProfileMenu(player)
            itemName.contains("Menu do SkyWars") -> InventoryManager.openSkyWarsMenu(player)
            itemName.contains("Coletáveis") -> InventoryManager.openCollectiblesMenu(player)
            itemName.contains("Jogadores:") -> InventoryManager.togglePlayerVisibility(player)
            itemName.contains("Selecionar Lobby") -> InventoryManager.openLobbySelectorMenu(player)
        }
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? org.bukkit.entity.Player ?: return
        if (!authManager.isAuthenticated(player)) return

        // Bloquear cliques em inventários personalizados
        val inventoryTitle = event.view.title().let { LegacyComponentSerializer.legacySection().serialize(it) }
        if (customInventoryTitles.contains(inventoryTitle)) {
            event.isCancelled = true
            plugin.logger.info("Clique bloqueado no menu: $inventoryTitle") // Log de depuração
            return
        }

        // Bloquear qualquer interação envolvendo itens fixos
        val slot = event.slot
        val currentItem = event.currentItem
        val cursorItem = event.cursor
        val action = event.action
        val hotbarSlot = event.hotbarButton.takeIf { it != -1 } // Verifica tecla numérica

        if (event.clickedInventory == player.inventory && (
                    slot in fixedSlots ||
                            hotbarSlot in fixedSlots ||
                            (currentItem != null && isFixedItem(currentItem)) ||
                            (cursorItem != null && isFixedItem(cursorItem)) ||
                            (action.toString().contains("SWAP") && hotbarSlot != -1 && hotbarSlot in fixedSlots)
                    )) {
            event.isCancelled = true
            plugin.logger.info("Tentativa de mover item fixo no slot $slot (hotbarButton: $hotbarSlot, ação: $action) bloqueada.") // Log de depuração
        }
    }

    @EventHandler
    fun onInventoryDrag(event: InventoryDragEvent) {
        val player = event.whoClicked as? org.bukkit.entity.Player ?: return
        if (!authManager.isAuthenticated(player)) return

        // Bloquear arrastar em inventários personalizados
        val inventoryTitle = event.view.title().let { LegacyComponentSerializer.legacySection().serialize(it) }
        if (customInventoryTitles.contains(inventoryTitle)) {
            event.isCancelled = true
            plugin.logger.info("Arrastar bloqueado no menu: $inventoryTitle") // Log de depuração
            return
        }

        // Bloquear arrastar itens fixos ou para slots fixos
        if (event.inventory == player.inventory && (
                    event.inventorySlots.any { slot -> slot in fixedSlots } ||
                            event.newItems.values.any { item -> isFixedItem(item) }
                    )) {
            event.isCancelled = true
            plugin.logger.info("Tentativa de arrastar item fixo ou para slot fixo bloqueada.") // Log de depuração
        }
    }

    @EventHandler
    fun onPlayerDropItem(event: PlayerDropItemEvent) {
        if (!authManager.isAuthenticated(event.player)) return
        val slot = event.player.inventory.heldItemSlot
        if (slot in fixedSlots || isFixedItem(event.itemDrop.itemStack)) {
            event.isCancelled = true
            plugin.logger.info("Tentativa de descartar item fixo no slot $slot bloqueada.") // Log de depuração
        }
    }

    private fun isFixedItem(item: org.bukkit.inventory.ItemStack): Boolean {
        val itemName = item.itemMeta?.displayName()?.let {
            LegacyComponentSerializer.legacySection().serialize(it)
        } ?: return false
        return itemName.contains("Selecionar Jogo") ||
                itemName.contains("Meu Perfil") ||
                itemName.contains("Menu do SkyWars") ||
                itemName.contains("Coletáveis") ||
                itemName.contains("Jogadores:") ||
                itemName.contains("Selecionar Lobby")
    }
}
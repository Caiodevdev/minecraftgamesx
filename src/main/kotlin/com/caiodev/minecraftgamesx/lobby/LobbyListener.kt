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
        val slot = event.slot
        if (slot in listOf(0, 1, 2, 4, 7, 8)) {
            event.isCancelled = true
            plugin.logger.info("Tentativa de mover item fixo no slot $slot bloqueada.") // Log de depuração
        }
    }

    @EventHandler
    fun onInventoryDrag(event: InventoryDragEvent) {
        val player = event.whoClicked as? org.bukkit.entity.Player ?: return
        if (!authManager.isAuthenticated(player)) return
        if (event.inventorySlots.any { it in listOf(0, 1, 2, 4, 7, 8) }) {
            event.isCancelled = true
            plugin.logger.info("Tentativa de arrastar item fixo bloqueada.") // Log de depuração
        }
    }

    @EventHandler
    fun onPlayerDropItem(event: PlayerDropItemEvent) {
        if (!authManager.isAuthenticated(event.player)) return
        val slot = event.player.inventory.heldItemSlot
        if (slot in listOf(0, 1, 2, 4, 7, 8)) {
            event.isCancelled = true
            plugin.logger.info("Tentativa de descartar item fixo no slot $slot bloqueada.") // Log de depuração
        }
    }
}
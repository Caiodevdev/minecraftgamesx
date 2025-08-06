package com.caiodev.minecraftgamesx.lobby

import com.caiodev.minecraftgamesx.Minecraftgamesx
import com.caiodev.minecraftgamesx.auth.AuthManager
import com.caiodev.minecraftgamesx.core.database.DatabaseManager
import com.google.common.io.ByteArrayDataOutput
import com.google.common.io.ByteStreams
import net.citizensnpcs.api.event.NPCLeftClickEvent
import net.citizensnpcs.api.event.NPCRightClickEvent
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class LobbyListener(
    private val plugin: Minecraftgamesx,
    private val authManager: AuthManager,
    private val databaseManager: DatabaseManager,
    private val scoreboard: ScoreboardManager
) : Listener {
    private val customInventoryTitles = listOf(
        "§e✦ §lSelecionar Jogo",
        "§e✦ §lMeu Perfil",
        "§e✦ §lMenu do SkyWars",
        "§e✦ §lColetáveis",
        "§e✦ §lSelecionar Lobby",
        "§b✦ §lJogar Agora"
    )

    private val fixedSlots = listOf(0, 1, 2, 4, 7, 8)

    init {
        plugin.server.messenger.registerOutgoingPluginChannel(plugin, "BungeeCord")
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        val level = databaseManager.getPlayerLevel(player.uniqueId)
        val xp = databaseManager.getPlayerXP(player.uniqueId)
        val tag = databaseManager.getPlayerTag(player.uniqueId)
        if (level == 0 && xp == 0) {
            databaseManager.setPlayerLevel(player.uniqueId, 0)
            databaseManager.setPlayerXP(player.uniqueId, 0)
        }
        if (tag == "membro" && !databaseManager.getPlayerTag(player.uniqueId).isNotEmpty()) {
            databaseManager.setPlayerTag(player.uniqueId, "membro")
        }
        scoreboard.setupScoreboard(player, plugin, databaseManager)
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        scoreboard.removeScoreboard(event.player)
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.player
        if (!authManager.isAuthenticated(player)) {
            event.isCancelled = true
            return
        }

        val item = event.item ?: return
        if (!event.hasItem() || !event.action.isRightClick) return
        event.isCancelled = true

        val itemName = item.itemMeta?.displayName()?.let {
            LegacyComponentSerializer.legacySection().serialize(it)
        } ?: return
        plugin.logger.info("Interação com item: $itemName")

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
    fun onNPCRightClick(event: NPCRightClickEvent) {
        val player = event.clicker
        if (!authManager.isAuthenticated(player)) {
            event.isCancelled = true
            player.sendMessage("§f✦ §7Você precisa estar §e§lautenticado §7para interagir com o NPC!")
            plugin.logger.info("Interação bloqueada: ${player.name} não está autenticado")
            return
        }

        if (NPCManager.isNPC(event.npc)) {
            event.isCancelled = true
            NPCManager.openNPCMenu(player)
            plugin.logger.info("Interação com botão direito no NPC 'Jogar Agora' por ${player.name}, ID do NPC: ${event.npc.id}")
        } else {
            plugin.logger.info("NPC clicado não é 'Jogar Agora': ${event.npc.name}, ID: ${event.npc.id}")
        }
    }

    @EventHandler
    fun onNPCLeftClick(event: NPCLeftClickEvent) {
        val player = event.clicker
        if (!authManager.isAuthenticated(player)) {
            event.isCancelled = true
            player.sendMessage("§f✦ §7Você precisa estar §e§lautenticado §7para interagir com o NPC!")
            plugin.logger.info("Interação bloqueada: ${player.name} não está autenticado")
            return
        }

        if (NPCManager.isNPC(event.npc)) {
            event.isCancelled = true
            NPCManager.openNPCMenu(player)
            plugin.logger.info("Interação com botão esquerdo no NPC 'Jogar Agora' por ${player.name}, ID do NPC: ${event.npc.id}")
        } else {
            plugin.logger.info("NPC clicado não é 'Jogar Agora': ${event.npc.name}, ID: ${event.npc.id}")
        }
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? org.bukkit.entity.Player ?: return
        if (!authManager.isAuthenticated(player)) return

        val inventoryTitle = event.view.title().let { LegacyComponentSerializer.legacySection().serialize(it) }
        if (customInventoryTitles.contains(inventoryTitle)) {
            event.isCancelled = true
            if (inventoryTitle == "§b✦ §lJogar Agora" && event.currentItem != null) {
                val itemName = event.currentItem?.itemMeta?.displayName()?.let {
                    LegacyComponentSerializer.legacySection().serialize(it)
                } ?: return
                plugin.logger.info("Item clicado no menu Jogar Agora: $itemName")
                if (itemName == "§b§lSkyWars (Solo)") {
                    val out: ByteArrayDataOutput = ByteStreams.newDataOutput()
                    out.writeUTF("Connect")
                    out.writeUTF("skywars")
                    player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray())
                    player.sendMessage("§f✦ §7Enviando para o servidor §e§lSkyWars§7...")
                    plugin.logger.info("Jogador ${player.name} enviado para o servidor skywars")
                }
            }
            plugin.logger.info("Clique bloqueado no menu: $inventoryTitle")
            return
        }

        val slot = event.slot
        val currentItem = event.currentItem
        val cursorItem = event.cursor
        val action = event.action
        val hotbarSlot = event.hotbarButton.takeIf { it != -1 }

        if (event.clickedInventory == player.inventory && (
                    slot in fixedSlots ||
                            hotbarSlot in fixedSlots ||
                            (currentItem != null && InventoryManager.isFixedItem(currentItem)) ||
                            (cursorItem != null && InventoryManager.isFixedItem(cursorItem)) ||
                            (action.toString().contains("SWAP") && hotbarSlot != -1 && hotbarSlot in fixedSlots)
                    )) {
            event.isCancelled = true
            plugin.logger.info("Tentativa de mover item fixo no slot $slot (hotbarButton: $hotbarSlot, ação: $action) bloqueada.")
        }
    }

    @EventHandler
    fun onInventoryDrag(event: InventoryDragEvent) {
        val player = event.whoClicked as? org.bukkit.entity.Player ?: return
        if (!authManager.isAuthenticated(player)) return

        val inventoryTitle = event.view.title().let { LegacyComponentSerializer.legacySection().serialize(it) }
        if (customInventoryTitles.contains(inventoryTitle)) {
            event.isCancelled = true
            plugin.logger.info("Arrastar bloqueado no menu: $inventoryTitle")
            return
        }

        if (event.inventory == player.inventory && (
                    event.inventorySlots.any { slot -> slot in fixedSlots } ||
                            event.newItems.values.any { item -> InventoryManager.isFixedItem(item) }
                    )) {
            event.isCancelled = true
            plugin.logger.info("Tentativa de arrastar item fixo ou para slot fixo bloqueada.")
        }
    }

    @EventHandler
    fun onPlayerDropItem(event: PlayerDropItemEvent) {
        if (!authManager.isAuthenticated(event.player)) return
        val slot = event.player.inventory.heldItemSlot
        if (slot in fixedSlots || InventoryManager.isFixedItem(event.itemDrop.itemStack)) {
            event.isCancelled = true
            plugin.logger.info("Tentativa de descartar item fixo no slot $slot bloqueada.")
        }
    }
}
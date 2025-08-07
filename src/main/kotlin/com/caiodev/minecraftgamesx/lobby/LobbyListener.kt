package com.caiodev.minecraftgamesx.lobby

import com.caiodev.minecraftgamesx.Minecraftgamesx
import com.caiodev.minecraftgamesx.auth.AuthManager
import com.caiodev.minecraftgamesx.collectibles.CollectiblesManager
import com.caiodev.minecraftgamesx.collectibles.PetMenu
import com.caiodev.minecraftgamesx.collectibles.PetType
import com.caiodev.minecraftgamesx.collectibles.ParticleMenu
import com.caiodev.minecraftgamesx.collectibles.ParticleType
import com.caiodev.minecraftgamesx.collectibles.PetManager
import com.caiodev.minecraftgamesx.collectibles.ParticleManager
import com.caiodev.minecraftgamesx.core.database.DatabaseManager
import com.google.common.io.ByteArrayDataOutput
import com.google.common.io.ByteStreams
import net.citizensnpcs.api.event.NPCLeftClickEvent
import net.citizensnpcs.api.event.NPCRightClickEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.NamespacedKey
import java.util.logging.Level

class LobbyListener(
    private val plugin: Minecraftgamesx,
    private val authManager: AuthManager,
    private val databaseManager: DatabaseManager,
    private val scoreboard: ScoreboardManager,
    private val petManager: PetManager,
    private val particleManager: ParticleManager
) : Listener {
    private val collectiblesManager = CollectiblesManager(plugin)
    private val customInventoryTitles = listOf(
        "§e✦ §lSelecionar Jogo",
        "§e✦ §lMeu Perfil",
        "§e✦ §lMenu do SkyWars",
        "§e✦ §lColetáveis",
        "§e✦ §lSelecionar Lobby",
        "§b✦ §lJogar Agora",
        "§e✦ §lPets",
        "§e✦ §lPartículas",
        "§e✦ §lConfirmar Compra"
    )

    private val fixedSlots = listOf(0, 1, 2, 4, 7, 8)

    init {
        plugin.server.messenger.registerOutgoingPluginChannel(plugin, "BungeeCord")
        plugin.logger.info("Canal BungeeCord registrado")
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        val level = databaseManager.getPlayerLevel(player.uniqueId)
        val xp = databaseManager.getPlayerXP(player.uniqueId)
        val tag = databaseManager.getPlayerTag(player.uniqueId)
        plugin.logger.info("Jogador ${player.name} entrou: nível=$level, xp=$xp, tag=$tag")
        if (level == 0 && xp == 0) {
            databaseManager.setPlayerLevel(player.uniqueId, 0)
            databaseManager.setPlayerXP(player.uniqueId, 0)
        }
        if (tag == "membro" && !databaseManager.getPlayerTag(player.uniqueId).isNotEmpty()) {
            databaseManager.setPlayerTag(player.uniqueId, "membro")
        }
        petManager.loadActivePet(player)
        particleManager.loadActiveParticle(player)
        scoreboard.setupScoreboard(player, plugin, databaseManager)
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        scoreboard.removeScoreboard(event.player)
        petManager.deactivatePet(event.player)
        particleManager.deactivateParticle(event.player)
        plugin.logger.info("Jogador ${event.player.name} saiu")
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
        if (!customInventoryTitles.contains(inventoryTitle)) return
        event.isCancelled = true
        val currentItem = event.currentItem ?: return
        val itemName = currentItem.itemMeta?.displayName()?.let {
            LegacyComponentSerializer.legacySection().serialize(it)
        } ?: return
        val actionData = currentItem.itemMeta?.persistentDataContainer?.get(
            NamespacedKey.minecraft("collectible_action"),
            PersistentDataType.STRING
        )
        val petActionData = currentItem.itemMeta?.persistentDataContainer?.get(
            NamespacedKey.minecraft("pet_action"),
            PersistentDataType.STRING
        )
        val particleActionData = currentItem.itemMeta?.persistentDataContainer?.get(
            NamespacedKey.minecraft("particle_action"),
            PersistentDataType.STRING
        )

        when (inventoryTitle) {
            "§e✦ §lSelecionar Jogo" -> {
                plugin.logger.info("Item clicado no menu Selecionar Jogo: $itemName")
                if (itemName == "§e§lSkyWars") {
                    val out: ByteArrayDataOutput = ByteStreams.newDataOutput()
                    out.writeUTF("Connect")
                    out.writeUTF("skywars")
                    player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray())
                    player.sendMessage("§f✦ §7Enviando para o servidor §e§lSkyWars§7...")
                    plugin.logger.info("Jogador ${player.name} enviado para o servidor skywars")
                }
            }
            "§e✦ §lMenu do SkyWars" -> {
                plugin.logger.info("Item clicado no menu SkyWars: $itemName")
                player.closeInventory()
            }
            "§e✦ §lColetáveis" -> {
                plugin.logger.info("Item clicado no menu Coletáveis: $itemName")
                when (actionData) {
                    "pets" -> collectiblesManager.openPetsMenu(player)
                    "particles" -> collectiblesManager.openParticlesMenu(player)
                    "close" -> {
                        player.closeInventory()
                        plugin.logger.info("Jogador ${player.name} fechou o menu de Coletáveis")
                    }
                }
            }
            "§e✦ §lSelecionar Lobby" -> {
                plugin.logger.info("Item clicado no menu Selecionar Lobby: $itemName")
                if (itemName == "§e§lLobby 1" || itemName == "§e§lLobby 2") {
                    player.sendMessage("§f✦ §7Lobby selecionado: $itemName (Em desenvolvimento)")
                    player.closeInventory()
                    plugin.logger.info("Jogador ${player.name} selecionou $itemName")
                }
            }
            "§b✦ §lJogar Agora" -> {
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
            "§e✦ §lPets" -> {
                plugin.logger.info("Item clicado no menu Pets: $itemName")
                if (petActionData == "close") {
                    player.closeInventory()
                    plugin.logger.info("Jogador ${player.name} fechou o menu de Pets")
                    return
                }
                val petType = petActionData?.let { data ->
                    PetType.entries.find { it.name.lowercase() == data }
                } ?: return
                if (petManager.hasPet(player, petType)) {
                    if (petManager.getActivePet(player) == petType) {
                        petManager.deactivatePet(player)
                    } else {
                        petManager.activatePet(player, petType)
                    }
                    PetMenu.openPetMenu(player, petManager)
                } else {
                    PetMenu.openConfirmPurchaseMenu(player, petType, petManager)
                }
            }
            "§e✦ §lPartículas" -> {
                plugin.logger.info("Item clicado no menu Partículas: $itemName")
                if (particleActionData == "close") {
                    player.closeInventory()
                    plugin.logger.info("Jogador ${player.name} fechou o menu de Partículas")
                    return
                }
                val particleType = particleActionData?.let { data ->
                    ParticleType.entries.find { it.name.lowercase() == data }
                } ?: return
                if (particleManager.hasParticle(player, particleType)) {
                    if (particleManager.getActiveParticle(player) == particleType) {
                        particleManager.deactivateParticle(player)
                    } else {
                        particleManager.activateParticle(player, particleType)
                    }
                    ParticleMenu.openParticleMenu(player, particleManager)
                } else {
                    ParticleMenu.openConfirmPurchaseMenu(player, particleType, particleManager)
                }
            }
            "§e✦ §lConfirmar Compra" -> {
                plugin.logger.info("Item clicado no menu Confirmar Compra: $itemName")
                when {
                    petActionData != null -> {
                        when (petActionData) {
                            "cancel" -> {
                                PetMenu.openPetMenu(player, petManager)
                            }
                            else -> {
                                val petId = petActionData.removePrefix("confirm_")
                                val petType = PetType.entries.find { it.name.lowercase() == petId } ?: return
                                if (petManager.purchasePet(player, petType)) {
                                    PetMenu.openPetMenu(player, petManager)
                                } else {
                                    player.closeInventory()
                                }
                            }
                        }
                    }
                    particleActionData != null -> {
                        when (particleActionData) {
                            "cancel" -> {
                                ParticleMenu.openParticleMenu(player, particleManager)
                            }
                            else -> {
                                val particleId = particleActionData.removePrefix("confirm_")
                                val particleType = ParticleType.entries.find { it.name.lowercase() == particleId } ?: return
                                if (particleManager.purchaseParticle(player, particleType)) {
                                    ParticleMenu.openParticleMenu(player, particleManager)
                                } else {
                                    player.closeInventory()
                                }
                            }
                        }
                    }
                }
            }
        }
        plugin.logger.info("Clique bloqueado no menu: $inventoryTitle")
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
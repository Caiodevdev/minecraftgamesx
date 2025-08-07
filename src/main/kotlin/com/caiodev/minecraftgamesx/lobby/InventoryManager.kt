package com.caiodev.minecraftgamesx.lobby

import com.caiodev.minecraftgamesx.Minecraftgamesx
import com.caiodev.minecraftgamesx.collectibles.CollectiblesMenu
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.enchantments.Enchantment

object InventoryManager {
    private const val SKYWARS_MENU_KEY = "skywars_menu"
    private const val PROFILE_MENU_KEY = "profile_menu"
    private const val COLLECTIBLES_MENU_KEY = "collectibles_menu"
    private const val LOBBY_SELECTOR_KEY = "lobby_selector"
    private const val PLAYER_VISIBILITY_KEY = "player_visibility"

    fun setupPlayerInventory(player: Player) {
        val inventory = player.inventory
        inventory.clear()

        // Slot 1: Bússola - Selecionar Jogo
        inventory.setItem(0, createItem(
            Material.COMPASS,
            "§e✦ §lSelecionar Jogo",
            listOf("§7Clique para abrir o menu de jogos.")
        ))

        // Slot 2: Cabeça do Jogador - Meu Perfil
        val skull = ItemStack(Material.PLAYER_HEAD)
        skull.itemMeta = (skull.itemMeta as SkullMeta).apply {
            owningPlayer = player
            displayName(Component.text("§e✦ §lMeu Perfil"))
            lore(listOf(Component.text("§7Clique para ver suas estatísticas.")))
            addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
        }
        inventory.setItem(1, skull)

        // Slot 3: Esmeralda - Menu do SkyWars
        inventory.setItem(2, createItem(
            Material.EMERALD,
            "§e✦ §lMenu do SkyWars",
            listOf("§7Clique para acessar opções do SkyWars.", "§7§o(Em desenvolvimento)")
        ).apply { addUnsafeEnchantment(Enchantment.EFFICIENCY, 1) })

        // Slot 5: Baú - Coletáveis
        inventory.setItem(4, createItem(
            Material.CHEST,
            "§e✦ §lColetáveis",
            listOf("§7Clique para ver seus itens cosméticos.", "§7§o(Em desenvolvimento)")
        ))

        // Slot 8: Argila - Jogadores: Desativados/Ativados
        inventory.setItem(7, createItem(
            Material.CLAY_BALL,
            "§e✦ §lJogadores: §fAtivados",
            listOf("§7Clique para alternar a visibilidade dos jogadores.")
        ))

        // Slot 9: Estrela do Nether - Selecionar Lobby
        inventory.setItem(8, createItem(
            Material.NETHER_STAR,
            "§e✦ §lSelecionar Lobby",
            listOf("§7Clique para escolher um lobby.", "§7§o(Em desenvolvimento)")
        ))

        player.updateInventory()
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

    fun openGameSelectorMenu(player: Player) {
        val inventory = Bukkit.createInventory(null, 27, Component.text("§e✦ §lSelecionar Jogo"))
        val skywarsItem = createItem(
            Material.DIAMOND_SWORD,
            "§e§lSkyWars",
            listOf("§7Clique para entrar no SkyWars.", "§7§o(Em desenvolvimento)")
        )
        inventory.setItem(13, skywarsItem)
        player.openInventory(inventory)
    }

    fun openProfileMenu(player: Player) {
        val plugin = player.server.pluginManager.getPlugin("minecraftgamesx") as Minecraftgamesx
        val databaseManager = plugin.databaseManager!!
        val level = databaseManager.getPlayerLevel(player.uniqueId)
        val xp = databaseManager.getPlayerXP(player.uniqueId)
        val coins = databaseManager.getPlayerCoins(player.uniqueId)
        val inventory = Bukkit.createInventory(null, 27, Component.text("§e✦ §lMeu Perfil"))
        val statsItem = createItem(
            Material.BOOK,
            "§e§lEstatísticas",
            listOf(
                "§7Nível: §e$level",
                "§7XP: §e$xp/500",
                "§7Coins: §e$coins",
                "§7Vitórias: §f0",
                "§7Mortes: §f0",
                "§7§o(Em desenvolvimento)"
            )
        )
        inventory.setItem(13, statsItem)
        player.openInventory(inventory)
    }

    fun openSkyWarsMenu(player: Player) {
        val inventory = Bukkit.createInventory(null, 27, Component.text("§e✦ §lMenu do SkyWars"))
        val kitsItem = createItem(
            Material.IRON_CHESTPLATE,
            "§e§lComprar Kits",
            listOf("§7Clique para comprar kits.", "§7§o(Em desenvolvimento)")
        )
        inventory.setItem(13, kitsItem)
        player.openInventory(inventory)
    }

    fun openCollectiblesMenu(player: Player) {
        CollectiblesMenu.openCollectiblesMenu(player)
    }

    fun openLobbySelectorMenu(player: Player) {
        val inventory = Bukkit.createInventory(null, 27, Component.text("§e✦ §lSelecionar Lobby"))
        val lobby1Item = createItem(
            Material.BEACON,
            "§e§lLobby 1",
            listOf("§7Clique para entrar no Lobby 1.", "§7§o(Em desenvolvimento)")
        )
        val lobby2Item = createItem(
            Material.BEACON,
            "§e§lLobby 2",
            listOf("§7Clique para entrar no Lobby 2.", "§7§o(Em desenvolvimento)")
        )
        inventory.setItem(11, lobby1Item)
        inventory.setItem(15, lobby2Item)
        player.openInventory(inventory)
    }

    fun togglePlayerVisibility(player: Player) {
        val item = player.inventory.getItem(7) ?: return
        val isEnabled = item.itemMeta?.displayName()?.toString()?.contains("Ativados") ?: true
        if (isEnabled) {
            Bukkit.getOnlinePlayers().forEach { other ->
                if (other != player) player.hidePlayer(other)
            }
            item.itemMeta = item.itemMeta?.apply {
                displayName(Component.text("§e✦ §lJogadores: §fDesativados"))
                lore(listOf(Component.text("§7Clique para mostrar os jogadores.")))
            }
            player.sendMessage("§f✦ §7Jogadores agora estão §e§lINVISÍVEIS§7.")
        } else {
            Bukkit.getOnlinePlayers().forEach { other ->
                if (other != player) player.showPlayer(other)
            }
            item.itemMeta = item.itemMeta?.apply {
                displayName(Component.text("§e✦ §lJogadores: §fAtivados"))
                lore(listOf(Component.text("§7Clique para esconder os jogadores.")))
            }
            player.sendMessage("§f✦ §7Jogadores agora estão §e§lVISÍVEIS§7.")
        }
        player.updateInventory()
    }

    fun isFixedItem(item: ItemStack): Boolean {
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
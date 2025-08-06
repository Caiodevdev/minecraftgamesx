package com.caiodev.minecraftgamesx.lobby

import net.citizensnpcs.api.CitizensAPI
import net.citizensnpcs.api.npc.NPC
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

object NPCManager {
    private const val NPC_NAME = "§e✦ §lJogar Agora"
    private const val NPC_CLEAN_NAME = "Jogar Agora"
    private const val MENU_TITLE = "§b✦ §lJogar Agora"

    fun createNPC(location: Location, plugin: JavaPlugin): NPC? {
        val registry = CitizensAPI.getNPCRegistry()
        val npc = registry.createNPC(EntityType.PLAYER, NPC_NAME)
        npc.spawn(location)
        plugin.logger.info("NPC criado com nome: ${npc.name} na localização: ${location.x}, ${location.y}, ${location.z}")
        return npc
    }

    fun removeNPCs(plugin: JavaPlugin) {
        val registry = CitizensAPI.getNPCRegistry()
        registry.iterator().forEach { npc ->
            if (isNPC(npc)) {
                plugin.logger.info("Removendo NPC com nome: ${npc.name}")
                npc.destroy()
            }
        }
    }

    fun openNPCMenu(player: Player) {
        val inventory = Bukkit.createInventory(null, 27, Component.text(MENU_TITLE))

        // Item: SkyWars (Solo)
        val skywarsItem = createItem(
            Material.ENDER_EYE,
            "§b§lSkyWars (Solo)",
            listOf(
                "§7",
                "§eClique para jogar.",
                "§7§o(Em desenvolvimento)"
            )
        )
        inventory.setItem(11, skywarsItem)

        // Item: Selecionar Mapa
        val mapSelectorItem = createItem(
            Material.OAK_LEAVES,
            "§b§lSelecionar Mapa",
            listOf(
                "§7",
                "§eEscolha o mapa que você quer jogar.",
                "§eExclusivo para §b§lVIPS§e.",
                "§7§o(Em desenvolvimento)"
            )
        )
        inventory.setItem(15, mapSelectorItem)

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

    fun isNPC(npc: NPC): Boolean {
        val npcName = npc.name?.let { LegacyComponentSerializer.legacySection().serialize(Component.text(it)) } ?: return false
        val cleanNpcName = npcName.replace("§[0-9a-fklmnor]".toRegex(), "").replace("✦", "").trim()
        Bukkit.getLogger().info("Verificando NPC com nome: $npcName (limpo: $cleanNpcName)")
        return cleanNpcName == NPC_CLEAN_NAME
    }
}
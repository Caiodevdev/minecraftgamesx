package com.caiodev.minecraftgamesx.auth

import com.caiodev.minecraftgamesx.core.database.DatabaseManager
import com.caiodev.minecraftgamesx.core.util.PasswordUtil
import org.bukkit.entity.Player
import java.sql.SQLException
import java.util.UUID

class AuthManager(private val databaseManager: DatabaseManager) {
    private val authenticatedPlayers = mutableSetOf<UUID>()

    fun isAuthenticated(player: Player): Boolean {
        return authenticatedPlayers.contains(player.uniqueId)
    }

    fun authenticate(player: Player) {
        authenticatedPlayers.add(player.uniqueId)
    }

    fun unauthenticate(player: Player) {
        authenticatedPlayers.remove(player.uniqueId)
    }

    fun registerPlayer(player: Player, password: String): Boolean {
        val hashedPassword = PasswordUtil.hashPassword(password)
        val sql = """
            INSERT INTO players (uuid, username, password, last_login)
            VALUES (?, ?, ?, NOW())
            ON DUPLICATE KEY UPDATE username = ?, password = ?, last_login = NOW()
        """.trimIndent()
        try {
            databaseManager.getConnection().use { conn ->
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setString(1, player.uniqueId.toString())
                    stmt.setString(2, player.name)
                    stmt.setString(3, hashedPassword)
                    stmt.setString(4, player.name)
                    stmt.setString(5, hashedPassword)
                    stmt.executeUpdate()
                }
            }
            return true
        } catch (e: SQLException) {
            e.printStackTrace()
            return false
        }
    }

    fun loginPlayer(player: Player, password: String): Boolean {
        val sql = "SELECT password FROM players WHERE uuid = ?"
        try {
            databaseManager.getConnection().use { conn ->
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setString(1, player.uniqueId.toString())
                    val rs = stmt.executeQuery()
                    if (rs.next()) {
                        val hashedPassword = rs.getString("password")
                        return PasswordUtil.verifyPassword(password, hashedPassword)
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return false
    }

    fun isRegistered(player: Player): Boolean {
        val sql = "SELECT COUNT(*) FROM players WHERE uuid = ?"
        try {
            databaseManager.getConnection().use { conn ->
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setString(1, player.uniqueId.toString())
                    val rs = stmt.executeQuery()
                    if (rs.next()) {
                        return rs.getInt(1) > 0
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return false
    }
}
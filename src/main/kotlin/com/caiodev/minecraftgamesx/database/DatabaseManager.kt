package com.caiodev.minecraftgamesx.core.database

import com.caiodev.minecraftgamesx.core.config.DatabaseConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.bukkit.Bukkit
import java.sql.Connection
import java.util.UUID

class DatabaseManager(config: DatabaseConfig) {
    private val dataSource: HikariDataSource
    private val databaseName: String = config.database

    init {
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = "jdbc:mysql://${config.host}:${config.port}/${config.database}"
            username = config.username
            password = config.password
            addDataSourceProperty("cachePrepStmts", "true")
            addDataSourceProperty("prepStmtCacheSize", "250")
            addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
        }
        try {
            dataSource = HikariDataSource(hikariConfig)
            createTables()
            ensureColumns()
        } catch (e: Exception) {
            Bukkit.getLogger().severe("Erro ao inicializar a conexão com o banco de dados $databaseName: ${e.message}")
            throw e
        }
    }

    private fun createTables() {
        dataSource.connection.use { connection ->
            try {
                connection.createStatement().use { statement ->
                    statement.execute("""
                        CREATE TABLE IF NOT EXISTS players (
                            uuid VARCHAR(36) PRIMARY KEY,
                            username VARCHAR(16) NOT NULL,
                            password VARCHAR(255) NOT NULL,
                            last_login TIMESTAMP,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            UNIQUE(username)
                        )
                    """)
                    Bukkit.getLogger().info("Tabela 'players' verificada/criada no banco de dados $databaseName.")
                }
                connection.createStatement().use { statement ->
                    statement.execute("""
                        CREATE TABLE IF NOT EXISTS player_levels (
                            uuid VARCHAR(36) PRIMARY KEY,
                            level INT NOT NULL DEFAULT 0,
                            xp INT NOT NULL DEFAULT 0,
                            coins INT NOT NULL DEFAULT 0
                        )
                    """)
                    Bukkit.getLogger().info("Tabela 'player_levels' verificada/criada no banco de dados $databaseName.")
                }
                connection.createStatement().use { statement ->
                    statement.execute("""
                        CREATE TABLE IF NOT EXISTS player_tags (
                            uuid VARCHAR(36) PRIMARY KEY,
                            tag VARCHAR(50) NOT NULL DEFAULT 'membro'
                        )
                    """)
                    Bukkit.getLogger().info("Tabela 'player_tags' verificada/criada no banco de dados $databaseName.")
                }
            } catch (e: Exception) {
                Bukkit.getLogger().severe("Erro ao criar tabelas no banco de dados $databaseName: ${e.message}")
                throw e
            }
        }
    }

    private fun ensureColumns() {
        dataSource.connection.use { connection ->
            try {
                connection.createStatement().use { statement ->
                    statement.executeQuery("SELECT xp, coins FROM player_levels LIMIT 1")
                }
            } catch (e: Exception) {
                if (e.message?.contains("Unknown column 'xp'") == true) {
                    try {
                        connection.createStatement().use { statement ->
                            statement.execute("ALTER TABLE player_levels ADD COLUMN xp INT NOT NULL DEFAULT 0")
                            Bukkit.getLogger().info("Coluna 'xp' adicionada à tabela 'player_levels' no banco de dados $databaseName.")
                        }
                    } catch (alterException: Exception) {
                        Bukkit.getLogger().severe("Erro ao adicionar coluna 'xp' à tabela 'player_levels': ${alterException.message}")
                        throw alterException
                    }
                }
                if (e.message?.contains("Unknown column 'coins'") == true) {
                    try {
                        connection.createStatement().use { statement ->
                            statement.execute("ALTER TABLE player_levels ADD COLUMN coins INT NOT NULL DEFAULT 0")
                            Bukkit.getLogger().info("Coluna 'coins' adicionada à tabela 'player_levels' no banco de dados $databaseName.")
                        }
                    } catch (alterException: Exception) {
                        Bukkit.getLogger().severe("Erro ao adicionar coluna 'coins' à tabela 'player_levels': ${alterException.message}")
                        throw alterException
                    }
                }
            }
        }
    }

    fun getPlayerLevel(uuid: UUID): Int {
        dataSource.connection.use { connection ->
            try {
                connection.prepareStatement("SELECT level FROM player_levels WHERE uuid = ?").use { statement ->
                    statement.setString(1, uuid.toString())
                    statement.executeQuery().use { result ->
                        return if (result.next()) result.getInt("level") else 0
                    }
                }
            } catch (e: Exception) {
                Bukkit.getLogger().severe("Erro ao recuperar nível para UUID $uuid: ${e.message}")
                throw e
            }
        }
    }

    fun setPlayerLevel(uuid: UUID, level: Int) {
        dataSource.connection.use { connection ->
            try {
                connection.prepareStatement("""
                    INSERT INTO player_levels (uuid, level, xp, coins) VALUES (?, ?, 0, 0)
                    ON DUPLICATE KEY UPDATE level = ?, xp = 0, coins = COALESCE(coins, 0)
                """).use { statement ->
                    statement.setString(1, uuid.toString())
                    statement.setInt(2, level)
                    statement.setInt(3, level)
                    statement.executeUpdate()
                    Bukkit.getLogger().info("Nível definido para UUID $uuid: $level")
                }
            } catch (e: Exception) {
                Bukkit.getLogger().severe("Erro ao definir nível para UUID $uuid: ${e.message}")
                throw e
            }
        }
    }

    fun getPlayerXP(uuid: UUID): Int {
        dataSource.connection.use { connection ->
            try {
                connection.prepareStatement("SELECT xp FROM player_levels WHERE uuid = ?").use { statement ->
                    statement.setString(1, uuid.toString())
                    statement.executeQuery().use { result ->
                        return if (result.next()) result.getInt("xp") else 0
                    }
                }
            } catch (e: Exception) {
                Bukkit.getLogger().severe("Erro ao recuperar XP para UUID $uuid: ${e.message}")
                throw e
            }
        }
    }

    fun setPlayerXP(uuid: UUID, xp: Int) {
        dataSource.connection.use { connection ->
            try {
                connection.prepareStatement("""
                    INSERT INTO player_levels (uuid, level, xp, coins) VALUES (?, 0, ?, 0)
                    ON DUPLICATE KEY UPDATE xp = ?, level = COALESCE(level, 0), coins = COALESCE(coins, 0)
                """).use { statement ->
                    statement.setString(1, uuid.toString())
                    statement.setInt(2, xp)
                    statement.setInt(3, xp)
                    statement.executeUpdate()
                    Bukkit.getLogger().info("XP definido para UUID $uuid: $xp")
                }
            } catch (e: Exception) {
                Bukkit.getLogger().severe("Erro ao definir XP para UUID $uuid: ${e.message}")
                throw e
            }
        }
    }

    fun getPlayerCoins(uuid: UUID): Int {
        dataSource.connection.use { connection ->
            try {
                connection.prepareStatement("SELECT coins FROM player_levels WHERE uuid = ?").use { statement ->
                    statement.setString(1, uuid.toString())
                    statement.executeQuery().use { result ->
                        return if (result.next()) result.getInt("coins") else 0
                    }
                }
            } catch (e: Exception) {
                Bukkit.getLogger().severe("Erro ao recuperar coins para UUID $uuid: ${e.message}")
                throw e
            }
        }
    }

    fun setPlayerCoins(uuid: UUID, coins: Int) {
        dataSource.connection.use { connection ->
            try {
                connection.prepareStatement("""
                    INSERT INTO player_levels (uuid, level, xp, coins) VALUES (?, 0, 0, ?)
                    ON DUPLICATE KEY UPDATE coins = ?, level = COALESCE(level, 0), xp = COALESCE(xp, 0)
                """).use { statement ->
                    statement.setString(1, uuid.toString())
                    statement.setInt(2, coins)
                    statement.setInt(3, coins)
                    statement.executeUpdate()
                    Bukkit.getLogger().info("Coins definidos para UUID $uuid: $coins")
                }
            } catch (e: Exception) {
                Bukkit.getLogger().severe("Erro ao definir coins para UUID $uuid: ${e.message}")
                throw e
            }
        }
    }

    fun getPlayerTag(uuid: UUID): String {
        dataSource.connection.use { connection ->
            try {
                connection.prepareStatement("SELECT tag FROM player_tags WHERE uuid = ?").use { statement ->
                    statement.setString(1, uuid.toString())
                    statement.executeQuery().use { result ->
                        return if (result.next()) result.getString("tag") else "membro"
                    }
                }
            } catch (e: Exception) {
                Bukkit.getLogger().severe("Erro ao recuperar tag para UUID $uuid: ${e.message}")
                throw e
            }
        }
    }

    fun setPlayerTag(uuid: UUID, tag: String) {
        dataSource.connection.use { connection ->
            try {
                connection.prepareStatement("""
                    INSERT INTO player_tags (uuid, tag) VALUES (?, ?)
                    ON DUPLICATE KEY UPDATE tag = ?
                """).use { statement ->
                    statement.setString(1, uuid.toString())
                    statement.setString(2, tag)
                    statement.setString(3, tag)
                    statement.executeUpdate()
                    Bukkit.getLogger().info("Tag definida para UUID $uuid: $tag")
                }
            } catch (e: Exception) {
                Bukkit.getLogger().severe("Erro ao definir tag para UUID $uuid: ${e.message}")
                throw e
            }
        }
    }

    fun removePlayerTag(uuid: UUID) {
        dataSource.connection.use { connection ->
            try {
                connection.prepareStatement("DELETE FROM player_tags WHERE uuid = ?").use { statement ->
                    statement.setString(1, uuid.toString())
                    statement.executeUpdate()
                    Bukkit.getLogger().info("Tag removida para UUID $uuid, revertendo para 'membro'")
                }
            } catch (e: Exception) {
                Bukkit.getLogger().severe("Erro ao remover tag para UUID $uuid: ${e.message}")
                throw e
            }
        }
    }

    fun getAvailableTags(): List<String> {
        return listOf("membro") // Adicione mais tags aqui no futuro
    }

    fun getConnection(): Connection {
        return dataSource.connection
    }

    fun close() {
        dataSource.close()
        Bukkit.getLogger().info("Conexão com o banco de dados fechada.")
    }
}
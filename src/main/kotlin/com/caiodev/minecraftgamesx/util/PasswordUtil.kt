package com.caiodev.minecraftgamesx.core.util

import at.favre.lib.crypto.bcrypt.BCrypt

object PasswordUtil {
    fun hashPassword(password: String): String {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray())
    }

    fun verifyPassword(password: String, hashed: String): Boolean {
        return BCrypt.verifyer().verify(password.toCharArray(), hashed).verified
    }
}
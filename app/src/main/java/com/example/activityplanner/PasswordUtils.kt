package com.example.activityplanner

import java.security.MessageDigest

/**
 * PasswordUtils provides hashing for secure password storage.
 * Uses SHA-256 for simplicity.
 */
object PasswordUtils {

    fun hashPassword(password: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

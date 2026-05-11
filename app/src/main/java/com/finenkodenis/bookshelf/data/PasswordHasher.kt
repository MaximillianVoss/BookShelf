package com.finenkodenis.bookshelf.data

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

class PasswordHasher(
    private val secureRandom: SecureRandom = SecureRandom()
) {
    fun createSalt(): String {
        val salt = ByteArray(SALT_BYTES)
        secureRandom.nextBytes(salt)
        return Base64.getEncoder().encodeToString(salt)
    }

    fun hash(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = "$salt:$password".toByteArray(Charsets.UTF_8)
        return Base64.getEncoder().encodeToString(digest.digest(bytes))
    }

    fun verify(password: String, salt: String, expectedHash: String): Boolean {
        return hash(password, salt) == expectedHash
    }

    private companion object {
        const val SALT_BYTES = 16
    }
}

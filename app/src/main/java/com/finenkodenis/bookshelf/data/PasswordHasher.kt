package com.finenkodenis.bookshelf.data

import java.security.MessageDigest
import java.security.SecureRandom

class PasswordHasher(
    private val secureRandom: SecureRandom = SecureRandom()
) {
    fun createSalt(): String {
        val salt = ByteArray(SALT_BYTES)
        secureRandom.nextBytes(salt)
        return salt.toHexString()
    }

    fun hash(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = "$salt:$password".toByteArray(Charsets.UTF_8)
        return digest.digest(bytes).toHexString()
    }

    fun verify(password: String, salt: String, expectedHash: String): Boolean {
        return hash(password, salt) == expectedHash
    }

    private companion object {
        const val SALT_BYTES = 16
    }
}

private fun ByteArray.toHexString(): String {
    return joinToString(separator = "") { byte -> "%02x".format(byte) }
}

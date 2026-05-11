package com.finenkodenis.bookshelf

import com.finenkodenis.bookshelf.data.PasswordHasher
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordHasherTest {
    @Test
    fun verify_returnsTrueForOriginalPassword() {
        val hasher = PasswordHasher()
        val salt = hasher.createSalt()
        val hash = hasher.hash("secret", salt)

        assertTrue(hasher.verify("secret", salt, hash))
    }

    @Test
    fun verify_returnsFalseForWrongPassword() {
        val hasher = PasswordHasher()
        val salt = hasher.createSalt()
        val hash = hasher.hash("secret", salt)

        assertFalse(hasher.verify("another", salt, hash))
    }

    @Test
    fun createSalt_returnsDifferentValues() {
        val hasher = PasswordHasher()

        assertNotEquals(hasher.createSalt(), hasher.createSalt())
    }
}

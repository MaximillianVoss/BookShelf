package com.finenkodenis.bookshelf.data

import com.finenkodenis.bookshelf.data.local.UserDao
import com.finenkodenis.bookshelf.data.local.UserEntity

sealed class AuthResult {
    data class Success(val user: User) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

const val DEMO_USERNAME = "demo"
const val DEMO_PASSWORD = "demo1234"

class UserRepository(
    private val userDao: UserDao,
    private val passwordHasher: PasswordHasher = PasswordHasher()
) {
    suspend fun register(username: String, password: String): AuthResult {
        val normalizedUsername = username.trim()
        if (normalizedUsername.length < 3) {
            return AuthResult.Error("Логин должен быть не короче 3 символов")
        }
        if (password.length < 4) {
            return AuthResult.Error("Пароль должен быть не короче 4 символов")
        }
        if (userDao.getByUsername(normalizedUsername) != null) {
            return AuthResult.Error("Пользователь с таким логином уже существует")
        }

        val salt = passwordHasher.createSalt()
        val id = userDao.insert(
            UserEntity(
                username = normalizedUsername,
                passwordSalt = salt,
                passwordHash = passwordHasher.hash(password, salt)
            )
        )
        return AuthResult.Success(User(id = id, username = normalizedUsername))
    }

    suspend fun login(username: String, password: String): AuthResult {
        val normalizedUsername = username.trim()
        val user = userDao.getByUsername(normalizedUsername)
            ?: return AuthResult.Error("Пользователь не найден")

        if (!passwordHasher.verify(password, user.passwordSalt, user.passwordHash)) {
            return AuthResult.Error("Неверный пароль")
        }

        userDao.update(user.copy(lastLoginAt = System.currentTimeMillis()))
        return AuthResult.Success(User(id = user.userId, username = user.username))
    }

    suspend fun loginDemo(): AuthResult {
        val existing = userDao.getByUsername(DEMO_USERNAME)
        val salt = passwordHasher.createSalt()
        val hash = passwordHasher.hash(DEMO_PASSWORD, salt)
        val now = System.currentTimeMillis()

        if (existing == null) {
            val id = userDao.insert(
                UserEntity(
                    username = DEMO_USERNAME,
                    passwordSalt = salt,
                    passwordHash = hash,
                    createdAt = now,
                    lastLoginAt = now
                )
            )
            return AuthResult.Success(User(id = id, username = DEMO_USERNAME))
        }

        val updated = existing.copy(
            passwordSalt = salt,
            passwordHash = hash,
            lastLoginAt = now
        )
        userDao.update(updated)
        return AuthResult.Success(User(id = updated.userId, username = updated.username))
    }
}

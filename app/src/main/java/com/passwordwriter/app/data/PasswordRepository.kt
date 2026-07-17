package com.passwordwriter.app.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import javax.crypto.spec.SecretKeySpec

class PasswordRepository(
    private val dao: PasswordDao,
    private val crypto: CryptoManager
) {
    companion object {
        var masterPassword: String = ""
    }

    private var cachedKey: SecretKeySpec? = null

    fun initFromContext(context: Context) {
        if (masterPassword.isEmpty()) {
            cachedKey = crypto.loadDerivedKey(context)
            if (cachedKey == null) {
                crypto.ensureDefaultKey(context)
                cachedKey = crypto.getDefaultKey(context)
            }
        }
    }

    fun getAllPasswords(): Flow<List<PasswordEntity>> = dao.getAllPasswords()

    fun getAllCategories(): Flow<List<String>> = dao.getAllCategories()

    fun getPasswordsByCategory(category: String): Flow<List<PasswordEntity>> =
        dao.getPasswordsByCategory(category)

    suspend fun getPasswordById(id: Long): PasswordEntity? = dao.getPasswordById(id)

    fun searchPasswords(query: String): Flow<List<PasswordEntity>> =
        dao.searchPasswords(query)

    suspend fun savePassword(entity: PasswordEntity): Long {
        return if (masterPassword.isNotEmpty()) {
            val encrypted = entity.copy(
                encryptedPassword = crypto.encrypt(entity.encryptedPassword, masterPassword)
            )
            dao.insert(encrypted)
        } else {
            val encrypted = entity.copy(
                encryptedPassword = crypto.encryptWithKey(entity.encryptedPassword, cachedKey!!)
            )
            dao.insert(encrypted)
        }
    }

    suspend fun updatePassword(entity: PasswordEntity) {
        if (masterPassword.isNotEmpty()) {
            val encrypted = entity.copy(
                encryptedPassword = crypto.encrypt(entity.encryptedPassword, masterPassword)
            )
            dao.update(encrypted)
        } else {
            val encrypted = entity.copy(
                encryptedPassword = crypto.encryptWithKey(entity.encryptedPassword, cachedKey!!)
            )
            dao.update(encrypted)
        }
    }

    suspend fun deletePassword(entity: PasswordEntity) = dao.delete(entity)

    suspend fun deletePasswordById(id: Long) = dao.deleteById(id)

    fun decryptPassword(entity: PasswordEntity): String? {
        return if (masterPassword.isNotEmpty()) {
            crypto.decrypt(entity.encryptedPassword, masterPassword)
        } else {
            cachedKey?.let { crypto.decryptWithKey(entity.encryptedPassword, it) }
        }
    }

    suspend fun countByCategory(category: String): Int = dao.countByCategory(category)

    suspend fun renameCategory(oldName: String, newName: String) {
        dao.updateCategoryName(oldName, newName)
    }

    suspend fun reassignCategory(category: String, newCategory: String) {
        dao.updateCategoryName(category, newCategory)
    }

    suspend fun deleteCategory(category: String) {
        dao.deleteByCategory(category)
    }
}

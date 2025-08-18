package io.composeflow.datastore

/**
 * Multiplatform wrapper for DataStore functionality
 */
interface PlatformDataStore {
    suspend fun putString(
        key: String,
        value: String,
    )

    suspend fun getString(key: String): String?

    suspend fun putBoolean(
        key: String,
        value: Boolean,
    )

    suspend fun getBoolean(key: String): Boolean?

    suspend fun putInt(
        key: String,
        value: Int,
    )

    suspend fun getInt(key: String): Int?

    suspend fun remove(key: String)

    suspend fun clear()
}

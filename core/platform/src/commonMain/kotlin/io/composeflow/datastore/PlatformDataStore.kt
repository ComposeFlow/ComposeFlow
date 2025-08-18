package io.composeflow.datastore

import kotlinx.coroutines.flow.Flow

/**
 * Multiplatform wrapper for DataStore functionality
 */
interface PlatformDataStore {
    suspend fun putString(
        key: String,
        value: String,
    )

    suspend fun getString(key: String): String?

    /**
     * Observes changes to a string value in the DataStore.
     * Returns a Flow that emits the current value and all subsequent changes.
     */
    fun observeString(key: String): Flow<String?>

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

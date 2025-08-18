package io.composeflow.datastore

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

// TODO: Consider using multiplatform-settings as an alternative for DataStore in wasm
actual object DataStoreFactory {
    /**
     * Gets the singleton DataStore instance, creating it if necessary.
     * WASM implementation provides a no-op in-memory store.
     */
    actual fun getOrCreateDataStore(producePath: () -> String): PlatformDataStore = WasmDataStore()
}

private class WasmDataStore : PlatformDataStore {
    private val storage = mutableMapOf<String, Any>()
    private val storageFlow = MutableStateFlow(storage.toMap())

    private fun updateStorageFlow() {
        storageFlow.value = storage.toMap()
    }

    override suspend fun putString(
        key: String,
        value: String,
    ) {
        storage[key] = value
        updateStorageFlow()
    }

    override suspend fun getString(key: String): String? = storage[key] as? String

    override fun observeString(key: String): Flow<String?> =
        storageFlow.map { it[key] as? String }

    override suspend fun putBoolean(
        key: String,
        value: Boolean,
    ) {
        storage[key] = value
        updateStorageFlow()
    }

    override suspend fun getBoolean(key: String): Boolean? = storage[key] as? Boolean

    override suspend fun putInt(
        key: String,
        value: Int,
    ) {
        storage[key] = value
        updateStorageFlow()
    }

    override suspend fun getInt(key: String): Int? = storage[key] as? Int

    override suspend fun remove(key: String) {
        storage.remove(key)
        updateStorageFlow()
    }

    override suspend fun clear() {
        storage.clear()
        updateStorageFlow()
    }
}

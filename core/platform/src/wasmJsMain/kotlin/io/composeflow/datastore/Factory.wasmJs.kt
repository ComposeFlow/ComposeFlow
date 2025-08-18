package io.composeflow.datastore

actual object Factory {
    /**
     * Gets the singleton DataStore instance, creating it if necessary.
     * WASM implementation provides a no-op in-memory store.
     */
    actual fun getOrCreateDataStore(producePath: () -> String): PlatformDataStore =
        WasmDataStore()
}

private class WasmDataStore : PlatformDataStore {
    private val storage = mutableMapOf<String, Any>()

    override suspend fun putString(key: String, value: String) {
        storage[key] = value
    }

    override suspend fun getString(key: String): String? {
        return storage[key] as? String
    }

    override suspend fun putBoolean(key: String, value: Boolean) {
        storage[key] = value
    }

    override suspend fun getBoolean(key: String): Boolean? {
        return storage[key] as? Boolean
    }

    override suspend fun putInt(key: String, value: Int) {
        storage[key] = value
    }

    override suspend fun getInt(key: String): Int? {
        return storage[key] as? Int
    }

    override suspend fun remove(key: String) {
        storage.remove(key)
    }

    override suspend fun clear() {
        storage.clear()
    }
}
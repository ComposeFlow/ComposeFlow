package io.composeflow.datastore

expect object DataStoreFactory {
    /**
     * Gets the singleton DataStore instance, creating it if necessary.
     */
    fun getOrCreateDataStore(producePath: () -> String): PlatformDataStore
}

const val DATA_STORE_FILENAME = "compose.builder.preferences_pb"

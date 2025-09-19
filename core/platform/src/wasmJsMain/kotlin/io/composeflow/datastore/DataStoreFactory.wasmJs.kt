package io.composeflow.datastore

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.StorageSettings
import com.russhwolf.settings.coroutines.FlowSettings
import com.russhwolf.settings.coroutines.toFlowSettings
import com.russhwolf.settings.observable.makeObservable
import kotlinx.coroutines.flow.Flow

actual object DataStoreFactory {
    private var dataStore: PlatformDataStore? = null

    /**
     * Gets the singleton DataStore instance, creating it if necessary.
     * WASM implementation uses multiplatform-settings with localStorage for persistence.
     */
    actual fun getOrCreateDataStore(producePath: () -> String): PlatformDataStore = dataStore ?: WasmDataStore().also { dataStore = it }
}

@OptIn(ExperimentalSettingsApi::class)
private class WasmDataStore : PlatformDataStore {
    // Use StorageSettings which persists to browser localStorage
    private val settings: ObservableSettings = StorageSettings().makeObservable()
    private val flowSettings: FlowSettings = settings.toFlowSettings()

    override suspend fun putString(
        key: String,
        value: String,
    ) {
        settings.putString(key, value)
    }

    override suspend fun getString(key: String): String? = settings.getStringOrNull(key)

    override fun observeString(key: String): Flow<String?> = flowSettings.getStringOrNullFlow(key)

    override suspend fun putBoolean(
        key: String,
        value: Boolean,
    ) {
        settings.putBoolean(key, value)
    }

    override suspend fun getBoolean(key: String): Boolean? = settings.getBooleanOrNull(key)

    override suspend fun putInt(
        key: String,
        value: Int,
    ) {
        settings.putInt(key, value)
    }

    override suspend fun getInt(key: String): Int? = settings.getIntOrNull(key)

    override suspend fun remove(key: String) {
        settings.remove(key)
    }

    override suspend fun clear() {
        settings.clear()
    }
}

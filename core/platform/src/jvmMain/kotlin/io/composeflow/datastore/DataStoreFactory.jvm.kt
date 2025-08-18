package io.composeflow.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import io.composeflow.di.ServiceLocator
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import okio.Path.Companion.toPath

actual object DataStoreFactory {
    private lateinit var dataStore: DataStore<Preferences>

    private val lock = SynchronizedObject()

    /**
     * Gets the singleton DataStore instance, creating it if necessary.
     */
    actual fun getOrCreateDataStore(producePath: () -> String): PlatformDataStore =
        synchronized(lock) {
            if (DataStoreFactory::dataStore.isInitialized) {
                DataStoreWrapper(dataStore)
            } else {
                PreferenceDataStoreFactory
                    .createWithPath(
                        produceFile = { producePath().toPath() },
                        scope =
                            ServiceLocator.getOrPutWithKey(ServiceLocator.KEY_IO_DISPATCHER_COROUTINE_SCOPE) {
                                CoroutineScope(Dispatchers.IO + SupervisorJob())
                            },
                    ).also { dataStore = it }
                    .let { DataStoreWrapper(it) }
            }
        }

    /**
     * Gets the underlying DataStore instance for desktop-specific usage.
     * This ensures we don't create multiple DataStore instances for the same file.
     */
    internal fun getOrCreateActualDataStore(producePath: () -> String): DataStore<Preferences> =
        synchronized(lock) {
            if (DataStoreFactory::dataStore.isInitialized) {
                dataStore
            } else {
                PreferenceDataStoreFactory
                    .createWithPath(
                        produceFile = { producePath().toPath() },
                        scope =
                            ServiceLocator.getOrPutWithKey(ServiceLocator.KEY_IO_DISPATCHER_COROUTINE_SCOPE) {
                                CoroutineScope(Dispatchers.IO + SupervisorJob())
                            },
                    ).also { dataStore = it }
            }
        }
}

private class DataStoreWrapper(
    private val dataStore: DataStore<Preferences>,
) : PlatformDataStore {
    override suspend fun putString(
        key: String,
        value: String,
    ) {
        dataStore.updateData { preferences ->
            preferences
                .toMutablePreferences()
                .apply {
                    set(stringPreferencesKey(key), value)
                }.toPreferences()
        }
    }

    override suspend fun getString(key: String): String? =
        dataStore.data
            .map { preferences ->
                preferences[stringPreferencesKey(key)]
            }.first()

    override fun observeString(key: String): kotlinx.coroutines.flow.Flow<String?> =
        dataStore.data
            .map { preferences ->
                preferences[stringPreferencesKey(key)]
            }

    override suspend fun putBoolean(
        key: String,
        value: Boolean,
    ) {
        dataStore.updateData { preferences ->
            preferences
                .toMutablePreferences()
                .apply {
                    set(booleanPreferencesKey(key), value)
                }.toPreferences()
        }
    }

    override suspend fun getBoolean(key: String): Boolean? =
        dataStore.data
            .map { preferences ->
                preferences[booleanPreferencesKey(key)]
            }.first()

    override suspend fun putInt(
        key: String,
        value: Int,
    ) {
        dataStore.updateData { preferences ->
            preferences
                .toMutablePreferences()
                .apply {
                    set(intPreferencesKey(key), value)
                }.toPreferences()
        }
    }

    override suspend fun getInt(key: String): Int? =
        dataStore.data
            .map { preferences ->
                preferences[intPreferencesKey(key)]
            }.first()

    override suspend fun remove(key: String) {
        dataStore.updateData { preferences ->
            preferences
                .toMutablePreferences()
                .apply {
                    remove(stringPreferencesKey(key))
                    remove(booleanPreferencesKey(key))
                    remove(intPreferencesKey(key))
                }.toPreferences()
        }
    }

    override suspend fun clear() {
        dataStore.updateData { preferences ->
            preferences
                .toMutablePreferences()
                .apply {
                    clear()
                }.toPreferences()
        }
    }
}

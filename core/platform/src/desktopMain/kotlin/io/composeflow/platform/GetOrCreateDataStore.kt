package io.composeflow.platform

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import io.composeflow.datastore.DATA_STORE_FILENAME
import io.composeflow.datastore.Factory
import io.composeflow.datastore.PlatformDataStore

actual fun getOrCreateDataStore(): PlatformDataStore =
    Factory.getOrCreateDataStore {
        getCacheDir().resolve(DATA_STORE_FILENAME).toFile().path
    }

// Desktop-specific function that returns actual DataStore for internal desktop usage
fun getOrCreateActualDataStore(): DataStore<Preferences> {
    return Factory.getOrCreateActualDataStore {
        getCacheDir().resolve(DATA_STORE_FILENAME).toFile().path
    }
}

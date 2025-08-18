package io.composeflow.platform

import io.composeflow.datastore.DATA_STORE_FILENAME
import io.composeflow.datastore.Factory
import io.composeflow.datastore.PlatformDataStore

actual fun getOrCreateDataStore(): PlatformDataStore =
    Factory.getOrCreateDataStore {
        // WASM doesn't have a real file system, so we provide a mock path
        "/tmp/composeflow/$DATA_STORE_FILENAME"
    }
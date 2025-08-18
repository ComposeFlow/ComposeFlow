package io.composeflow.platform

import io.composeflow.datastore.PlatformDataStore

expect fun getOrCreateDataStore(): PlatformDataStore

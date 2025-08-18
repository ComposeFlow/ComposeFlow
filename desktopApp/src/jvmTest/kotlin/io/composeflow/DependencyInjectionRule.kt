package io.composeflow

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import io.composeflow.di.ServiceLocator
import io.composeflow.platform.getCacheDir
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.TestScope
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import kotlin.random.Random

class DependencyInjectionRule(
    private val coroutinesDispatcherRule: CoroutinesDispatcherRule,
) : TestWatcher() {
    override fun starting(description: Description) {
        ServiceLocator.putWithKey(
            key = ServiceLocator.KEY_IO_DISPATCHER_COROUTINE_SCOPE,
            value = CoroutineScope(coroutinesDispatcherRule.standardTestDispatcher + SupervisorJob()),
        )
        ServiceLocator.putWithKey(
            key = ServiceLocator.KEY_DEFAULT_DISPATCHER,
            value = coroutinesDispatcherRule.standardTestDispatcher,
        )
        ServiceLocator.put<DataStore<Preferences>>(
            PreferenceDataStoreFactory.create(
                scope = TestScope(coroutinesDispatcherRule.standardTestDispatcher + Job()),
                produceFile = {
                    // We might migrate DataStore to another saving mechanism
                    // FIXME: Without random, I saw this error.
                    // IllegalStateException: There are multiple DataStores active for the same file
                    getCacheDir().resolve("test${Random.nextInt()}.compose.builder.preferences_pb").toFile()
                },
            ),
        )
    }

    override fun finished(description: Description) {
        ServiceLocator.clear()
        getCacheDir().deleteRecursively()
    }
}

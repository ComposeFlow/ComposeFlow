package io.composeflow.ui.about

/**
 * Platform-specific billing service.
 * JVM implementation uses the actual billing client, WASM implementation provides no-op functionality.
 */
expect class BillingService() {
    suspend fun createPricingTableLink(): String?
    fun isAvailable(): Boolean
}
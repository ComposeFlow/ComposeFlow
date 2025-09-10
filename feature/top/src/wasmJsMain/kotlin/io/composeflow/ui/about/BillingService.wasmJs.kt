package io.composeflow.ui.about

actual class BillingService {
    actual suspend fun createPricingTableLink(): String? {
        // Billing is not available in WASM version
        return null
    }

    actual fun isAvailable(): Boolean = false
}

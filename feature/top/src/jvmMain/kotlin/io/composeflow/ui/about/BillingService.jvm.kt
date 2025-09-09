package io.composeflow.ui.about

import co.touchlab.kermit.Logger
import com.github.michaelbull.result.mapBoth
import io.composeflow.BillingClient

actual class BillingService {
    private val billingClient = BillingClient()
    
    actual suspend fun createPricingTableLink(): String? {
        return try {
            var result: String? = null
            billingClient.createPricingTableLink().mapBoth(
                success = { result = it },
                failure = {
                    Logger.e("Failed to create pricing table link", it)
                    result = null
                }
            )
            result
        } catch (e: Exception) {
            Logger.e("Failed to create pricing table link", e)
            null
        }
    }
    
    actual fun isAvailable(): Boolean = true
}
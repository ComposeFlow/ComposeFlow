package io.composeflow.analytics

/**
 * WASM implementation of the analytics factory.
 * Returns NoOpAnalytics for WASM platforms as analytics libraries are not yet available for WASM.
 * TODO: Implement proper analytics when PostHog or similar provides WASM/JS interop support.
 */
actual fun createAnalytics(): Analytics = NoOpAnalytics()

@file:Suppress("ktlint:standard:no-empty-file", "ktlint:standard:no-consecutive-comments")

package io.composeflow.analytics

/**
 * Placeholder for future PostHog JavaScript/WASM implementation.
 *
 * Currently, there's no direct way to use PostHog's JavaScript SDK in Kotlin/WASM.
 * Possible future solutions:
 *
 * 1. Use external declarations to interface with PostHog JS SDK
 *    - Would require @JsModule declarations for PostHog API
 *    - Need to handle JS interop complexities
 *
 * 2. Use Kotlin/JS compatible HTTP client to send events directly to PostHog API
 *    - Would implement PostHog's HTTP API protocol
 *    - More control but more work
 *
 * 3. Wait for official Kotlin Multiplatform support from PostHog
 *    - Most sustainable long-term solution
 *
 * For now, we use NoOpAnalytics to ensure the app compiles and runs on WASM.
 */

// TODO: Implement when WASM/JS interop improves or PostHog provides KMP support

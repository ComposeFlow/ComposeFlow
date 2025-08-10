package io.composeflow.analytics

/**
 * Common interface for analytics tracking across different platforms.
 * Provides methods to track events, user properties, and screen views.
 */
interface Analytics {
    /**
     *
     * Initialize the analytics client with the given API key.
     * @param apiKey The PostHog API key
     * @param host The PostHog host URL (optional, defaults to PostHog cloud)
     */
    fun initialize(
        apiKey: String,
        host: String? = null,
    )

    /**
     * Track an event with optional properties.
     * @param event The event name
     * @param properties Optional map of event properties
     */
    fun track(
        event: String,
        properties: Map<String, Any>? = null,
    )

    /**
     * Identify a user with their unique ID and optional properties.
     * @param userId The unique user identifier
     * @param properties Optional map of user properties
     */
    fun identify(
        userId: String,
        properties: Map<String, Any>? = null,
    )

    /**
     * Set properties for the current user.
     * @param properties Map of user properties to set
     */
    fun setUserProperties(properties: Map<String, Any>)

    /**
     * Track a screen view.
     * @param screenName The name of the screen
     * @param properties Optional map of screen properties
     */
    fun screen(
        screenName: String,
        properties: Map<String, Any>? = null,
    )

    /**
     * Reset the current user and clear all properties.
     * Useful for logout scenarios.
     */
    fun reset()

    /**
     * Flush any pending events to the server.
     */
    fun flush()

    /**
     * Shutdown the analytics client and flush any remaining events.
     */
    fun shutdown()
}

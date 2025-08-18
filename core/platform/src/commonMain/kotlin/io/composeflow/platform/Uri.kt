package io.composeflow.platform

/**
 * Multiplatform URI representation
 */
expect class Uri(
    uri: String,
) {
    val uri: String

    override fun toString(): String
}


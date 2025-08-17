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

fun Uri.toJavaUri(): java.net.URI = java.net.URI(this.uri)

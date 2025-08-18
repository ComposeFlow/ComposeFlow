package io.composeflow.platform

actual class Uri actual constructor(
    uri: String,
) {
    actual val uri: String = uri

    actual override fun toString(): String = uri
}

/**
 * Desktop-specific extension to convert to Java URI
 */
fun Uri.toJavaUri(): java.net.URI = java.net.URI(this.uri)

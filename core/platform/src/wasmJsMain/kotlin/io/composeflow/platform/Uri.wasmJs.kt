package io.composeflow.platform

actual class Uri actual constructor(
    uri: String,
) {
    actual val uri: String = uri

    actual override fun toString(): String = uri
}

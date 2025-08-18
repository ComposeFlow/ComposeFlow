package io.composeflow.ui

actual fun openInBrowser(uri: String) {
    // On WASM, we could potentially use window.open() via external JS declarations
    // For now, just throw an exception indicating it's not implemented
    throw UnsupportedOperationException("Browser opening not available on WASM")
}

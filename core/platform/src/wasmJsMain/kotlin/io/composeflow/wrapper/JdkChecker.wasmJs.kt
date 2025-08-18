package io.composeflow.wrapper

actual object JdkChecker {
    actual fun isValidJavaHome(javaHome: String): Boolean {
        // JDK validation is not applicable in WASM environment
        return false
    }
}

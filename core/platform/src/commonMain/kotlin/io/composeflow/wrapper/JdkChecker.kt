package io.composeflow.wrapper

/**
 * Multiplatform JDK checker
 */
expect object JdkChecker {
    fun isValidJavaHome(javaHome: String): Boolean
}

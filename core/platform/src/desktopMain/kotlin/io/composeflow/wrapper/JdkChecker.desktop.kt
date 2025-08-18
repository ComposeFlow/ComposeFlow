package io.composeflow.wrapper

import java.io.File

actual object JdkChecker {
    actual fun isValidJavaHome(javaHome: String): Boolean {
        val javaHomeDir = File(javaHome)
        if (!javaHomeDir.exists() || !javaHomeDir.isDirectory) return false

        val javaBinDir = javaHomeDir.resolve("bin")
        if (!javaBinDir.exists()) return false
        if (!javaBinDir.resolve("java").exists() &&
            !javaBinDir
                .resolve("java.exe")
                .exists()
        ) {
            return false
        }
        return true
    }
}
package io.composeflow.platform

actual fun getEnvVar(name: String): String? = System.getenv(name)

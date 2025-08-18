package io.composeflow.logger

expect val logger: Logger

interface Logger {
    // Standard logging methods
    fun debug(message: String)

    fun info(message: String)

    fun warn(message: String)

    fun error(message: String)

    fun error(
        message: String,
        throwable: Throwable,
    )

    // SLF4J-style parameterized logging
    fun debug(
        message: String,
        vararg args: Any?,
    )

    fun info(
        message: String,
        vararg args: Any?,
    )

    fun warn(
        message: String,
        vararg args: Any?,
    )

    fun error(
        message: String,
        vararg args: Any?,
    )

    // Kermit-style compatibility methods
    fun d(message: String) = debug(message)

    fun i(message: String) = info(message)

    fun w(message: String) = warn(message)

    fun e(message: String) = error(message)
}

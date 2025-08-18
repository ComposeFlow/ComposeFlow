package io.composeflow.logger

import org.slf4j.LoggerFactory

private class Slf4jLoggerWrapper(
    private val slf4jLogger: org.slf4j.Logger,
) : Logger {
    override fun debug(message: String) {
        slf4jLogger.debug(message)
    }

    override fun info(message: String) {
        slf4jLogger.info(message)
    }

    override fun warn(message: String) {
        slf4jLogger.warn(message)
    }

    override fun error(message: String) {
        slf4jLogger.error(message)
    }

    override fun error(
        message: String,
        throwable: Throwable,
    ) {
        slf4jLogger.error(message, throwable)
    }

    override fun debug(
        message: String,
        vararg args: Any?,
    ) {
        slf4jLogger.debug(message, *args)
    }

    override fun info(
        message: String,
        vararg args: Any?,
    ) {
        slf4jLogger.info(message, *args)
    }

    override fun warn(
        message: String,
        vararg args: Any?,
    ) {
        slf4jLogger.warn(message, *args)
    }

    override fun error(
        message: String,
        vararg args: Any?,
    ) {
        slf4jLogger.error(message, *args)
    }
}

actual val logger: Logger = Slf4jLoggerWrapper(LoggerFactory.getLogger("slf4j-logger"))

package io.composeflow.logger

import org.slf4j.LoggerFactory

private class Slf4jLoggerWrapper(private val slf4jLogger: org.slf4j.Logger) : Logger {
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

    override fun error(message: String, throwable: Throwable) {
        slf4jLogger.error(message, throwable)
    }
}

actual val logger: Logger = Slf4jLoggerWrapper(LoggerFactory.getLogger("slf4j-logger"))
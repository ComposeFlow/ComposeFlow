package io.composeflow.template

actual object ResourceLoader {
    actual fun loadResourceAsText(resourcePath: String): String {
        val stream =
            object {}.javaClass.getResourceAsStream(resourcePath)
                ?: throw IllegalArgumentException("Resource not found: $resourcePath")
        return stream.reader().use { it.readText() }
    }
}

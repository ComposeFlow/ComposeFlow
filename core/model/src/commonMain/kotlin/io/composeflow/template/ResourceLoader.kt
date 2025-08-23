package io.composeflow.template

/**
 * Platform-specific resource loader for template files.
 */
expect object ResourceLoader {
    /**
     * Loads a resource file as text from the given path.
     * @param resourcePath The path to the resource file (e.g., "/blank_screen_template.yaml")
     * @return The content of the resource file as a String
     * @throws Exception if the resource cannot be loaded
     */
    fun loadResourceAsText(resourcePath: String): String
}

package io.composeflow.template

actual object ResourceLoader {
    actual fun loadResourceAsText(resourcePath: String): String =
        when (resourcePath) {
            "/blank_screen_template.yaml" -> ScreenTemplateConstants.BLANK_SCREEN_TEMPLATE
            "/messages_screen_template.yaml" -> ScreenTemplateConstants.MESSAGES_SCREEN_TEMPLATE
            "/login_screen_template.yaml" -> ScreenTemplateConstants2.LOGIN_SCREEN_TEMPLATE
            "/settings_screen_template.yaml" -> loadSettingsTemplate()
            else -> throw IllegalArgumentException("Resource not found: $resourcePath")
        }

    private fun loadSettingsTemplate(): String {
        // For now, return the full settings template content
        // This could be moved to a constant file if needed
        return ScreenTemplateConstants3.SETTINGS_SCREEN_TEMPLATE
    }
}

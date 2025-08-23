package io.composeflow.template

import androidx.annotation.VisibleForTesting
import io.composeflow.model.project.appscreen.screen.Screen
import io.composeflow.serializer.decodeFromStringWithFallback
import io.composeflow.template.ScreenTemplates.createNewScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ScreenTemplates {
    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        scope.launch {
            // initialize loading the templates so that deserialization doesn't happen on the
            // next load
            initialTemplates()
        }
    }

    @get:VisibleForTesting
    val blankScreen by lazy {
        val resourcePath = "/blank_screen_template.yaml"
        ScreenTemplatePair(
            resourcePath = resourcePath,
            screen = loadScreenTemplate(resourcePath),
        )
    }

    @get:VisibleForTesting
    val messagesScreen by lazy {
        val resourcePath = "/messages_screen_template.yaml"
        ScreenTemplatePair(
            resourcePath = resourcePath,
            screen = loadScreenTemplate(resourcePath),
        )
    }

    @get:VisibleForTesting
    val loginScreen by lazy {
        val resourcePath = "/login_screen_template.yaml"
        ScreenTemplatePair(
            resourcePath = resourcePath,
            screen = loadScreenTemplate(resourcePath),
        )
    }

    @get:VisibleForTesting
    val settingsScreen by lazy {
        val resourcePath = "/settings_screen_template.yaml"
        ScreenTemplatePair(
            resourcePath = resourcePath,
            screen = loadScreenTemplate(resourcePath),
        )
    }

    /**
     * Initial set of templates. Use [createNewScreen] when creating a new screen.
     */
    fun initialTemplates(): List<ScreenTemplatePair> =
        listOf(
            blankScreen,
            messagesScreen,
            loginScreen,
            settingsScreen,
        )

    // When creating new Screen from a template, replace Uuids with random ones while keeping the
    // relationships to keep the referenced statuses.
    fun createNewScreen(screenTemplatePair: ScreenTemplatePair): Screen =
        loadScreenTemplate(screenTemplatePair.resourcePath)

    private fun loadScreenTemplate(fileName: String): Screen {
        val yamlContent = ResourceLoader.loadResourceAsText(fileName)
        val newYaml = replaceUuids(yamlContent)
        return decodeFromStringWithFallback<Screen>(newYaml)
    }
}

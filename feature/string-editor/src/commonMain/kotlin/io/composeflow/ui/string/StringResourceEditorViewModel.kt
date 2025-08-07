package io.composeflow.ui.string

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.setValue
import co.touchlab.kermit.Logger
import io.composeflow.ai.LlmRepository
import io.composeflow.ai.TranslateStringsResult
import io.composeflow.auth.FirebaseIdToken
import io.composeflow.model.project.Project
import io.composeflow.model.project.string.ResourceLocale
import io.composeflow.model.project.string.StringResource
import io.composeflow.override.toMutableStateMapEqualsOverride
import io.composeflow.repository.ProjectRepository
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope

class StringResourceEditorViewModel(
    // TODO Disable translate strings button if token is for anonymous user.
    //      https://github.com/ComposeFlow/ComposeFlow/issues/56
    private val firebaseIdToken: FirebaseIdToken,
    private val project: Project,
    private val projectRepository: ProjectRepository = ProjectRepository(firebaseIdToken),
    private val stringResourceEditorOperator: StringResourceEditorOperator = StringResourceEditorOperator(),
    private val llmRepository: LlmRepository = LlmRepository(),
) : ViewModel() {
    val selectedResourceIds: MutableSet<String> = mutableStateSetOf()
    var isTranslating: Boolean by mutableStateOf(false)

    fun onAddStringResource(
        key: String,
        description: String,
        defaultValue: String,
    ) {
        if (key.isNotBlank() && defaultValue.isNotBlank()) {
            val newResource =
                StringResource(
                    key = key,
                    description = description.ifBlank { null },
                    localizedValues = mutableMapOf(project.stringResourceHolder.defaultLocale.value to defaultValue),
                )
            val result = stringResourceEditorOperator.addStringResource(project, newResource)
            if (result.errorMessages.isEmpty()) {
                saveProject()
            }
        }
    }

    fun onUpdateStringResourceKey(
        resource: StringResource,
        newKey: String,
    ) {
        val updatedResource = resource.copy(key = newKey)
        val result = stringResourceEditorOperator.updateStringResource(project, updatedResource)
        if (result.errorMessages.isEmpty()) {
            saveProject()
        }
    }

    fun onUpdateStringResourceDescription(
        resource: StringResource,
        newDescription: String,
    ) {
        val updatedResource = resource.copy(description = newDescription.ifBlank { null })
        val result = stringResourceEditorOperator.updateStringResource(project, updatedResource)
        if (result.errorMessages.isEmpty()) {
            saveProject()
        }
    }

    fun onUpdateStringResourceValue(
        resource: StringResource,
        locale: ResourceLocale,
        value: String,
    ) {
        val updatedResource =
            resource.copy(
                localizedValues =
                    resource.localizedValues.toMutableStateMapEqualsOverride().apply {
                        this[locale] = value
                    },
            )
        val result = stringResourceEditorOperator.updateStringResource(project, updatedResource)
        if (result.errorMessages.isEmpty()) {
            saveProject()
        }
    }

    fun onDeleteStringResource(resource: StringResource) {
        val result = stringResourceEditorOperator.deleteStringResource(project, resource.id)
        if (result.errorMessages.isEmpty()) {
            selectedResourceIds.remove(resource.id)
            saveProject()
        }
    }

    fun onUpdateSupportedLocales(newLocales: List<ResourceLocale>) {
        val result = stringResourceEditorOperator.updateSupportedLocales(project, newLocales)
        if (result.errorMessages.isEmpty()) {
            saveProject()
        }
    }

    fun onUpdateDefaultLocale(locale: ResourceLocale) {
        val result = stringResourceEditorOperator.setDefaultLocale(project, locale)
        if (result.errorMessages.isEmpty()) {
            saveProject()
        }
    }

    fun onUpdateResourceSelection(
        resourceId: String,
        isSelected: Boolean,
    ) {
        if (isSelected) {
            selectedResourceIds.add(resourceId)
        } else {
            selectedResourceIds.remove(resourceId)
        }
    }

    fun onSelectAllResources() {
        selectedResourceIds.clear()
        selectedResourceIds.addAll(
            project.stringResourceHolder.stringResources.map { it.id },
        )
    }

    fun onClearResourceSelection() {
        selectedResourceIds.clear()
    }

    fun onTranslateStrings() {
        val selectedResources = project.stringResourceHolder.stringResources.filter { selectedResourceIds.contains(it.id) }
        if (selectedResources.isEmpty()) {
            Logger.e("No valid resources selected for translation. Translate button should be disabled")
            return
        }

        val defaultLocale = project.stringResourceHolder.defaultLocale.value
        val targetLocales = project.stringResourceHolder.supportedLocales.filter { it != defaultLocale }
        if (targetLocales.isEmpty()) {
            Logger.e("No target locales available for translation. Translate button should be disabled")
            return
        }

        viewModelScope.launch {
            isTranslating = true
            try {
                val result =
                    llmRepository.translateStrings(
                        firebaseIdToken = firebaseIdToken.rawToken ?: throw IllegalStateException("Signed-in user has no raw token"),
                        stringResources = selectedResources,
                        defaultLocale = defaultLocale,
                        targetLocales = targetLocales,
                    )
                when (result) {
                    is TranslateStringsResult.Success -> {
                        selectedResources.forEach { resource ->
                            val translationsForResource = result.translations[resource.key]
                            if (translationsForResource != null) {
                                val updatedLocalizedValues = resource.localizedValues.toMutableStateMapEqualsOverride()
                                translationsForResource.forEach { (localeCode, translation) ->
                                    val locale = ResourceLocale.fromString(localeCode)
                                    if (locale != null && targetLocales.contains(locale)) {
                                        updatedLocalizedValues[locale] = translation
                                    }
                                }
                                val updatedResource = resource.copy(localizedValues = updatedLocalizedValues)
                                // TODO Support bulk update of string resources
                                //      https://github.com/ComposeFlow/ComposeFlow/issues/41
                                stringResourceEditorOperator.updateStringResource(project, updatedResource)
                            }
                        }
                        selectedResourceIds.clear()
                        saveProject()
                        Logger.i("Successfully translated ${result.translations.size} strings")
                    }

                    is TranslateStringsResult.Error -> {
                        Logger.e("Translation failed: ${result.message}", result.throwable)
                    }
                }
            } finally {
                isTranslating = false
            }
        }
    }

    private fun saveProject() {
        viewModelScope.launch {
            projectRepository.updateProject(project)
        }
    }
}

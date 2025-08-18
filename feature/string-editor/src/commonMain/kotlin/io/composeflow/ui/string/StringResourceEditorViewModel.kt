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
import io.composeflow.model.project.string.StringResourceUpdate
import io.composeflow.repository.ProjectRepository
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope

class StringResourceEditorViewModel(
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
        viewModelScope.launch {
            if (key.isNotBlank() && defaultValue.isNotBlank()) {
                val newResource =
                    StringResource(
                        key = key,
                        description = description.ifBlank { null },
                        localizedValues = mutableMapOf(project.stringResourceHolder.defaultLocale.value to defaultValue),
                    )
                val result = stringResourceEditorOperator.addStringResources(project, listOf(newResource))
                if (result.errorMessages.isEmpty()) {
                    saveProject()
                }
            }
        }
    }

    fun onUpdateStringResourceKey(
        resource: StringResource,
        newKey: String,
    ) {
        viewModelScope.launch {
            val update =
                StringResourceUpdate(
                    id = resource.id,
                    key = newKey,
                )
            val result = stringResourceEditorOperator.updateStringResource(project, update)
            if (result.errorMessages.isEmpty()) {
                saveProject()
            }
        }
    }

    fun onUpdateStringResourceDescription(
        resource: StringResource,
        newDescription: String,
    ) {
        viewModelScope.launch {
            val update =
                StringResourceUpdate(
                    id = resource.id,
                    description = newDescription,
                )
            val result = stringResourceEditorOperator.updateStringResource(project, update)
            if (result.errorMessages.isEmpty()) {
                saveProject()
            }
        }
    }

    fun onUpdateStringResourceValue(
        resource: StringResource,
        locale: ResourceLocale,
        value: String,
    ) {
        viewModelScope.launch {
            val update =
                StringResourceUpdate(
                    id = resource.id,
                    localizedValuesToSet = mapOf(locale to value),
                )
            val result = stringResourceEditorOperator.updateStringResource(project, update)
            if (result.errorMessages.isEmpty()) {
                saveProject()
            }
        }
    }

    fun onDeleteStringResources(resources: List<StringResource>) {
        viewModelScope.launch {
            val resourceIds = resources.map { it.id }
            val result = stringResourceEditorOperator.deleteStringResources(project, resourceIds)
            if (result.errorMessages.isEmpty()) {
                selectedResourceIds.removeAll(resourceIds)
                saveProject()
            }
        }
    }

    fun onUpdateSupportedLocales(newLocales: List<ResourceLocale>) {
        viewModelScope.launch {
            val result = stringResourceEditorOperator.updateSupportedLocales(project, newLocales)
            if (result.errorMessages.isEmpty()) {
                saveProject()
            }
        }
    }

    fun onUpdateDefaultLocale(locale: ResourceLocale) {
        viewModelScope.launch {
            val result = stringResourceEditorOperator.setDefaultLocale(project, locale)
            if (result.errorMessages.isEmpty()) {
                saveProject()
            }
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

    fun onTranslateNeedsUpdateStrings() {
        val resourcesToTranslate = project.stringResourceHolder.stringResources.filter { it.needsTranslationUpdate }
        if (resourcesToTranslate.isEmpty()) {
            Logger.e("No resources need translation updates. Button should be disabled")
            return
        }
        translateResourcesInternal(resourcesToTranslate)
    }

    fun onTranslateStrings() {
        val selectedResources = project.stringResourceHolder.stringResources.filter { selectedResourceIds.contains(it.id) }
        if (selectedResources.isEmpty()) {
            Logger.e("No valid resources selected for translation. Translate button should be disabled")
            return
        }
        translateResourcesInternal(selectedResources)
    }

    private fun translateResourcesInternal(resourcesToTranslate: List<StringResource>) {
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
                        stringResources = resourcesToTranslate,
                        defaultLocale = defaultLocale,
                        targetLocales = targetLocales,
                    )
                when (result) {
                    is TranslateStringsResult.Success -> {
                        val updates = mutableListOf<StringResourceUpdate>()
                        resourcesToTranslate.forEach { resource ->
                            val translations = result.translations[resource.key]
                            if (translations != null) {
                                val localizedValuesToSet =
                                    translations
                                        .mapNotNull { (localeCode, translation) ->
                                            val locale = ResourceLocale.fromString(localeCode)
                                            if (locale != null && targetLocales.contains(locale)) {
                                                locale to translation
                                            } else {
                                                null
                                            }
                                        }.toMap()
                                updates.add(
                                    StringResourceUpdate(
                                        id = resource.id,
                                        localizedValuesToSet = localizedValuesToSet,
                                        // Clear the flag after successful translation
                                        needsTranslationUpdate = false,
                                    ),
                                )
                            }
                        }
                        if (updates.isNotEmpty()) {
                            stringResourceEditorOperator.updateStringResources(project, updates)
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

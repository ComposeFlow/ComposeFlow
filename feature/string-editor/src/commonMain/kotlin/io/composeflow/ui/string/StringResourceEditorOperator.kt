package io.composeflow.ui.string

import co.touchlab.kermit.Logger
import io.composeflow.Res
import io.composeflow.error_failed_to_add_string_resource
import io.composeflow.error_failed_to_delete_string_resource
import io.composeflow.error_failed_to_parse_locales_yaml
import io.composeflow.error_failed_to_parse_string_resource_ids
import io.composeflow.error_failed_to_parse_string_resources_yaml
import io.composeflow.error_failed_to_set_default_locale
import io.composeflow.error_failed_to_update_string_resource
import io.composeflow.error_failed_to_update_supported_locales
import io.composeflow.error_invalid_locale
import io.composeflow.error_string_resource_not_found
import io.composeflow.ksp.LlmParam
import io.composeflow.ksp.LlmTool
import io.composeflow.model.project.Project
import io.composeflow.model.project.string.ResourceLocale
import io.composeflow.model.project.string.StringResource
import io.composeflow.model.project.string.isTranslatable
import io.composeflow.serializer.decodeFromStringWithFallback
import io.composeflow.serializer.encodeToString
import io.composeflow.ui.EventResult
import io.composeflow.util.generateUniqueName
import io.composeflow.util.toComposeResourceName
import org.jetbrains.compose.resources.getString

// TODO: Wire this class with ToolDispatcher to enable AI to edit string resources.
//       https://github.com/ComposeFlow/ComposeFlow/issues/34

/**
 * Handles operations related to string resource editor, such as adding, updating, or removing string resources.
 * Operations in this class are exposed to the LLM to allow them to call it as tools as well as used
 * from the GUI in ComposeFlow.
 */
class StringResourceEditorOperator {
    suspend fun addStringResources(
        project: Project,
        stringResources: List<StringResource>,
    ): EventResult {
        val result = EventResult()
        if (stringResources.isEmpty()) {
            return result
        }
        val existingKeys =
            project.stringResourceHolder.stringResources
                .map { it.key }
                .toMutableSet()
        stringResources.forEach { stringResource ->
            try {
                val newKey =
                    generateUniqueName(
                        stringResource.key.toComposeResourceName(),
                        existingKeys,
                    )
                val newResource =
                    stringResource.copy(
                        key = newKey,
                        needsTranslationUpdate = project.stringResourceHolder.isTranslatable,
                    )
                project.stringResourceHolder.stringResources.add(newResource)
                existingKeys.add(newKey)
            } catch (e: Exception) {
                Logger.e(e) { "Error adding string resource: ${stringResource.key}" }
                result.errorMessages.add(
                    getString(Res.string.error_failed_to_add_string_resource, stringResource.key, e.message ?: "Unknown error"),
                )
            }
        }
        return result
    }

    @LlmTool(
        name = "add_string_resources",
        "Adds one or more string resources to the project. String resources are used for internationalization and localization of text in the application.",
    )
    suspend fun onAddStringResources(
        project: Project,
        @LlmParam(
            description =
                "The YAML representation of StringResource(s) to be added. " +
                    "Can be a single StringResource or a list of StringResources. Keys will be made unique if necessary.",
        )
        stringResourceYaml: String,
    ): EventResult =
        try {
            // Try to parse as list first, fallback to single resource
            val stringResources =
                try {
                    decodeFromStringWithFallback<List<StringResource>>(stringResourceYaml)
                } catch (_: Exception) {
                    listOf(decodeFromStringWithFallback<StringResource>(stringResourceYaml))
                }
            addStringResources(project, stringResources)
        } catch (e: Exception) {
            Logger.e(e) { "Error parsing string resources YAML" }
            EventResult().apply {
                errorMessages.add(
                    getString(Res.string.error_failed_to_parse_string_resources_yaml, e.message ?: "Unknown error"),
                )
            }
        }

    suspend fun deleteStringResources(
        project: Project,
        stringResourceIds: List<String>,
    ): EventResult {
        val result = EventResult()
        if (stringResourceIds.isEmpty()) {
            return result
        }
        val resourcesToDelete = mutableListOf<StringResource>()
        stringResourceIds.forEach { stringResourceId ->
            val resource = project.stringResourceHolder.stringResources.find { it.id == stringResourceId }
            if (resource == null) {
                result.errorMessages.add(
                    getString(Res.string.error_string_resource_not_found, stringResourceId),
                )
            } else {
                resourcesToDelete.add(resource)
            }
        }
        resourcesToDelete.forEach { resource ->
            try {
                project.stringResourceHolder.stringResources.remove(resource)
            } catch (e: Exception) {
                Logger.e(e) { "Error deleting string resource: ${resource.id}" }
                result.errorMessages.add(
                    getString(Res.string.error_failed_to_delete_string_resource, resource.key, e.message ?: "Unknown error"),
                )
            }
        }
        return result
    }

    @LlmTool(
        name = "delete_string_resources",
        description = "Removes one or more string resources from the project by their IDs.",
    )
    suspend fun onDeleteStringResources(
        project: Project,
        @LlmParam(
            description = "The ID(s) of the string resource(s) to be deleted. Can be a single ID string or a list of ID strings.",
        )
        stringResourceIds: String,
    ): EventResult =
        try {
            // Try to parse as list first, fallback to single string
            val ids =
                try {
                    decodeFromStringWithFallback<List<String>>(stringResourceIds)
                } catch (_: Exception) {
                    listOf(stringResourceIds)
                }
            deleteStringResources(project, ids)
        } catch (e: Exception) {
            Logger.e(e) { "Error parsing string resource IDs" }
            EventResult().apply {
                errorMessages.add(
                    getString(Res.string.error_failed_to_parse_string_resource_ids, e.message ?: "Unknown error"),
                )
            }
        }

    suspend fun updateStringResources(
        project: Project,
        stringResources: List<StringResource>,
    ): EventResult {
        val result = EventResult()
        if (stringResources.isEmpty()) {
            return result
        }
        val defaultLocale = project.stringResourceHolder.defaultLocale.value
        stringResources.forEach { stringResource ->
            try {
                val existingResourceIndex =
                    project.stringResourceHolder.stringResources.indexOfFirst { it.id == stringResource.id }
                if (existingResourceIndex == -1) {
                    result.errorMessages.add(
                        getString(Res.string.error_string_resource_not_found, stringResource.id),
                    )
                    return@forEach
                }

                val existingResource = project.stringResourceHolder.stringResources[existingResourceIndex]

                val needsTranslationUpdate =
                    if (project.stringResourceHolder.isTranslatable) {
                        // If the flag is explicitly set to false (e.g., after translation), keep it false
                        if (!stringResource.needsTranslationUpdate && existingResource.needsTranslationUpdate) {
                            false
                        } else {
                            val descriptionChanged = existingResource.description != stringResource.description
                            val defaultValueChanged =
                                existingResource.localizedValues[defaultLocale] != stringResource.localizedValues[defaultLocale]

                            // Set flag if content changed, or keep existing flag
                            (descriptionChanged || defaultValueChanged) || existingResource.needsTranslationUpdate
                        }
                    } else {
                        // No translation update needed if there's only one locale
                        false
                    }

                val updatedResource = stringResource.copy(needsTranslationUpdate = needsTranslationUpdate)
                project.stringResourceHolder.stringResources[existingResourceIndex] = updatedResource
            } catch (e: Exception) {
                Logger.e(e) { "Error updating string resource: ${stringResource.id}" }
                result.errorMessages.add(
                    getString(Res.string.error_failed_to_update_string_resource, stringResource.key, e.message ?: "Unknown error"),
                )
            }
        }
        return result
    }

    @LlmTool(
        name = "update_string_resources",
        description = "Updates one or more existing string resources in the project.",
    )
    suspend fun onUpdateStringResources(
        project: Project,
        @LlmParam(
            description =
                "The YAML representation of the updated StringResource(s). " +
                    "Can be a single StringResource or a list of StringResources. Must include the ID of each resource to update.",
        )
        stringResourceYaml: String,
    ): EventResult =
        try {
            // Try to parse as list first, fallback to single resource
            val stringResources =
                try {
                    decodeFromStringWithFallback<List<StringResource>>(stringResourceYaml)
                } catch (_: Exception) {
                    listOf(decodeFromStringWithFallback<StringResource>(stringResourceYaml))
                }
            updateStringResources(project, stringResources)
        } catch (e: Exception) {
            Logger.e(e) { "Error parsing string resource YAML" }
            EventResult().apply {
                errorMessages.add(
                    getString(Res.string.error_failed_to_parse_string_resources_yaml, e.message ?: "Unknown error"),
                )
            }
        }

    suspend fun updateSupportedLocales(
        project: Project,
        newLocales: List<ResourceLocale>,
    ): EventResult {
        val result = EventResult()
        try {
            val defaultLocale = project.stringResourceHolder.defaultLocale.value
            // Ensure default locale is always included
            val localesWithDefault =
                if (newLocales.contains(defaultLocale)) {
                    newLocales
                } else {
                    listOf(defaultLocale) + newLocales
                }

            // Find locales to add and remove
            val currentLocales = project.stringResourceHolder.supportedLocales.toSet()
            val newLocalesSet = localesWithDefault.toSet()
            val localesToRemove = currentLocales - newLocalesSet
            val localesToAdd = newLocalesSet - currentLocales

            // Remove old locales from string resources
            if (localesToRemove.isNotEmpty()) {
                project.stringResourceHolder.stringResources.forEach { resource ->
                    localesToRemove.forEach { locale ->
                        resource.localizedValues.remove(locale)
                    }
                }
            }

            // Update needsTranslationUpdate flags based on the locale changes
            when {
                // If new locales are being added, mark all string resources as needing translation
                localesToAdd.isNotEmpty() -> {
                    project.stringResourceHolder.stringResources.replaceAll { resource ->
                        resource.copy(needsTranslationUpdate = true)
                    }
                }
                // If only the default locale remains, clear the flags
                localesWithDefault.size == 1 -> {
                    project.stringResourceHolder.stringResources.replaceAll { resource ->
                        resource.copy(needsTranslationUpdate = false)
                    }
                }
            }

            // Replace the supported locales list
            project.stringResourceHolder.supportedLocales.clear()
            project.stringResourceHolder.supportedLocales.addAll(localesWithDefault)
        } catch (e: Exception) {
            Logger.e(e) { "Error replacing supported locales" }
            result.errorMessages.add(
                getString(Res.string.error_failed_to_update_supported_locales, e.message ?: "Unknown error"),
            )
        }
        return result
    }

    @LlmTool(
        name = "update_supported_locales",
        description = "Updates the entire list of supported locales with a new list. The default locale is always preserved.",
    )
    suspend fun onUpdateSupportedLocales(
        project: Project,
        @LlmParam(description = "YAML representation of the new list of supported locales.")
        localesYaml: String,
    ): EventResult =
        try {
            val locales = decodeFromStringWithFallback<List<ResourceLocale>>(localesYaml)
            updateSupportedLocales(project, locales)
        } catch (e: Exception) {
            Logger.e(e) { "Error parsing locales YAML" }
            EventResult().apply {
                errorMessages.add(
                    getString(Res.string.error_failed_to_parse_locales_yaml, e.message ?: "Unknown error"),
                )
            }
        }

    suspend fun setDefaultLocale(
        project: Project,
        locale: ResourceLocale,
    ): EventResult {
        val result = EventResult()
        try {
            if (!project.stringResourceHolder.supportedLocales.contains(locale)) {
                project.stringResourceHolder.supportedLocales.add(locale)
            }
            val previousLocale = project.stringResourceHolder.defaultLocale.value
            project.stringResourceHolder.defaultLocale.value = locale

            // If the default locale has actually changed and there are multiple locales,
            // set needsTranslationUpdate flag on all resources
            if (previousLocale != locale && project.stringResourceHolder.isTranslatable) {
                project.stringResourceHolder.stringResources.replaceAll { resource ->
                    resource.copy(needsTranslationUpdate = true)
                }
            }
        } catch (e: Exception) {
            Logger.e(e) { "Error setting default locale" }
            result.errorMessages.add(
                getString(Res.string.error_failed_to_set_default_locale, e.message ?: "Unknown error"),
            )
        }
        return result
    }

    @LlmTool(
        name = "set_default_locale",
        description = "Sets the default locale for the project.",
    )
    suspend fun onSetDefaultLocale(
        project: Project,
        @LlmParam(description = "The locale code to set as default (e.g., 'en-US', 'fr-FR', 'es-ES').")
        localeCode: String,
    ): EventResult {
        val locale = ResourceLocale.fromString(localeCode)
        return if (locale == null) {
            EventResult().apply {
                errorMessages.add(
                    getString(Res.string.error_invalid_locale, localeCode, ResourceLocale.entries.joinToString { it.toString() }),
                )
            }
        } else {
            setDefaultLocale(project, locale)
        }
    }

    @LlmTool(
        name = "list_string_resources",
        description = "Lists all string resources in the project. Returns a YAML representation of all resources.",
    )
    fun onListStringResources(project: Project): String =
        try {
            val resources = project.stringResourceHolder.stringResources
            encodeToString(resources)
        } catch (e: Exception) {
            Logger.e(e) { "Error listing string resources" }
            "Error listing string resources: ${e.message}"
        }

    @LlmTool(
        name = "get_string_resource",
        description = "Gets a specific string resource by its ID. Returns a YAML representation of the resource.",
    )
    fun onGetStringResource(
        project: Project,
        @LlmParam(description = "The ID of the string resource to retrieve.")
        stringResourceId: String,
    ): String =
        try {
            val resource = project.stringResourceHolder.stringResources.find { it.id == stringResourceId }
            if (resource != null) {
                encodeToString(resource)
            } else {
                "String resource with ID $stringResourceId not found."
            }
        } catch (e: Exception) {
            Logger.e(e) { "Error getting string resource" }
            "Error getting string resource: ${e.message}"
        }

    @LlmTool(
        name = "get_supported_locales",
        description = "Gets all supported locales in the project. Returns a YAML representation of the locales.",
    )
    fun onGetSupportedLocales(project: Project): String =
        try {
            val locales = project.stringResourceHolder.supportedLocales
            encodeToString(locales)
        } catch (e: Exception) {
            Logger.e(e) { "Error getting supported locales" }
            "Error getting supported locales: ${e.message}"
        }
}

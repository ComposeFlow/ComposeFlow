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
import io.composeflow.model.project.string.AddStringResourceError
import io.composeflow.model.project.string.DeleteStringResourceError
import io.composeflow.model.project.string.ResourceLocale
import io.composeflow.model.project.string.SetDefaultLocaleError
import io.composeflow.model.project.string.StringResource
import io.composeflow.model.project.string.StringResourceUpdate
import io.composeflow.model.project.string.UpdateStringResourceError
import io.composeflow.model.project.string.UpdateSupportedLocalesError
import io.composeflow.model.project.string.addStringResources
import io.composeflow.model.project.string.deleteStringResources
import io.composeflow.model.project.string.setDefaultLocale
import io.composeflow.model.project.string.updateStringResources
import io.composeflow.model.project.string.updateSupportedLocales
import io.composeflow.serializer.decodeFromStringWithFallback
import io.composeflow.serializer.encodeToString
import io.composeflow.ui.EventResult
import org.jetbrains.compose.resources.getString

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
        val errors = project.stringResourceHolder.addStringResources(stringResources)
        errors.forEach { error ->
            val errorMessage =
                when (error) {
                    is AddStringResourceError.GeneralError ->
                        getString(Res.string.error_failed_to_add_string_resource, error.resourceKey, error.message)
                }
            Logger.e { errorMessage }
            result.errorMessages.add(errorMessage)
        }
        return result
    }

    @LlmTool(
        name = "add_string_resources",
        "Adds one or more string resources to the project. " +
            "String resources are used for internationalization and localization of text in the application. " +
            "The IDs for string resources are generated automatically. " +
            "When you refer to an added string resource later, you need to use the ID, not the key. " +
            "You can check the generated IDs using the list_string_resources tool.",
    )
    suspend fun onAddStringResources(
        project: Project,
        @LlmParam(
            description =
                "The YAML representation of StringResource(s) to be added. " +
                    "Can be a single StringResource or a list of StringResources. Keys will be made unique if necessary.",
        )
        stringResourcesYaml: String,
    ): EventResult =
        try {
            // Try to parse as list first, fallback to single resource
            val stringResources =
                try {
                    decodeFromStringWithFallback<List<StringResource>>(stringResourcesYaml)
                } catch (_: Exception) {
                    listOf(decodeFromStringWithFallback<StringResource>(stringResourcesYaml))
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
        val errors = project.stringResourceHolder.deleteStringResources(stringResourceIds)
        errors.forEach { error ->
            val errorMessage =
                when (error) {
                    is DeleteStringResourceError.ResourceIdNotFound ->
                        getString(Res.string.error_string_resource_not_found, error.resourceId)
                    is DeleteStringResourceError.GeneralError ->
                        getString(Res.string.error_failed_to_delete_string_resource, error.resourceKey, error.message)
                }
            Logger.e { errorMessage }
            result.errorMessages.add(errorMessage)
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

    suspend fun updateStringResource(
        project: Project,
        update: StringResourceUpdate,
    ): EventResult = updateStringResources(project, listOf(update))

    suspend fun updateStringResources(
        project: Project,
        updates: List<StringResourceUpdate>,
    ): EventResult {
        val result = EventResult()
        val errors = project.stringResourceHolder.updateStringResources(updates)
        errors.forEach { error ->
            val errorMessage =
                when (error) {
                    is UpdateStringResourceError.ResourceIdNotFound ->
                        getString(Res.string.error_string_resource_not_found, error.resourceId)
                    is UpdateStringResourceError.GeneralError ->
                        getString(Res.string.error_failed_to_update_string_resource, error.resourceKey, error.message)
                }
            Logger.e { errorMessage }
            result.errorMessages.add(errorMessage)
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
                "The YAML representation of StringResourceUpdate(s). " +
                    "Can be a single StringResourceUpdate or a list of StringResourceUpdate objects. Each update must include the ID of the resource to update.",
        )
        stringResourceUpdatesYaml: String,
    ): EventResult =
        try {
            // Try to parse as list first, fallback to single resource
            val updates =
                try {
                    decodeFromStringWithFallback<List<StringResourceUpdate>>(stringResourceUpdatesYaml)
                } catch (_: Exception) {
                    listOf(decodeFromStringWithFallback<StringResourceUpdate>(stringResourceUpdatesYaml))
                }
            updateStringResources(project, updates)
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
        val errors = project.stringResourceHolder.updateSupportedLocales(newLocales)
        errors.forEach { error ->
            val errorMessage =
                when (error) {
                    is UpdateSupportedLocalesError.GeneralError ->
                        getString(Res.string.error_failed_to_update_supported_locales, error.message)
                }
            Logger.e { errorMessage }
            result.errorMessages.add(errorMessage)
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
        val errors = project.stringResourceHolder.setDefaultLocale(locale)
        errors.forEach { error ->
            val errorMessage =
                when (error) {
                    is SetDefaultLocaleError.GeneralError ->
                        getString(Res.string.error_failed_to_set_default_locale, error.message)
                }
            Logger.e { errorMessage }
            result.errorMessages.add(errorMessage)
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

package io.composeflow.model.project.string

import androidx.annotation.VisibleForTesting
import io.composeflow.override.toMutableStateMapEqualsOverride
import io.composeflow.util.generateUniqueName
import io.composeflow.util.toComposeResourceName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Extension functions for StringResourceHolder that provide business logic operations
// for managing string resources.

private const val UNKNOWN_ERROR_MESSAGE = "Unknown error"

private val nonAlphaNumericRegex = Regex("[^a-z0-9_]")
private val underscoresRegex = Regex("_+")

@VisibleForTesting
fun StringResource.inferKeyIfEmpty(): String =
    key.ifEmpty {
        val englishValue =
            localizedValues
                .filterKeys { it.language == "en" }
                .values
                .firstOrNull { it.isNotBlank() }
                ?: return@ifEmpty key

        val normalized =
            englishValue
                .take(50)
                .lowercase()
                .replace(nonAlphaNumericRegex, "_")
                .replace(underscoresRegex, "_")
                .trim('_')

        // If truncated mid-word, trim to last incomplete word
        if (englishValue.length > 50) {
            normalized.substringBeforeLast('_')
        } else {
            normalized
        }
    }

/**
 * Adds string resources to the holder, generating unique keys if necessary.
 * @param stringResources List of string resources to add
 * @return List of errors that occurred during the operation
 */
fun StringResourceHolder.addStringResources(stringResources: List<StringResource>): List<AddStringResourceError> {
    val errors = mutableListOf<AddStringResourceError>()
    if (stringResources.isEmpty()) {
        return errors
    }

    val existingKeys = this.stringResources.map { it.key }.toMutableSet()
    stringResources.forEach { stringResource ->
        try {
            val newKey =
                generateUniqueName(
                    stringResource.inferKeyIfEmpty().toComposeResourceName(),
                    existingKeys,
                )
            val newResource =
                stringResource.copy(
                    key = newKey,
                    needsTranslationUpdate = this.isTranslatable,
                )
            this.stringResources.add(newResource)
            existingKeys.add(newKey)
        } catch (e: Exception) {
            errors.add(
                AddStringResourceError.GeneralError(
                    message = e.message ?: UNKNOWN_ERROR_MESSAGE,
                    resourceKey = stringResource.key,
                ),
            )
        }
    }

    return errors
}

sealed interface AddStringResourceError {
    data class GeneralError(
        val message: String,
        val resourceKey: String,
    ) : AddStringResourceError
}

/**
 * Deletes string resources by their IDs.
 * @param stringResourceIds List of string resource IDs to delete
 * @return List of errors that occurred during the operation
 */
fun StringResourceHolder.deleteStringResources(stringResourceIds: List<String>): List<DeleteStringResourceError> {
    val errors = mutableListOf<DeleteStringResourceError>()
    if (stringResourceIds.isEmpty()) {
        return errors
    }

    val resourcesToDelete = mutableListOf<StringResource>()
    stringResourceIds.forEach { stringResourceId ->
        val resource = this.stringResources.find { it.id == stringResourceId }
        if (resource == null) {
            errors.add(DeleteStringResourceError.ResourceIdNotFound(stringResourceId))
        } else {
            resourcesToDelete.add(resource)
        }
    }

    resourcesToDelete.forEach { resource ->
        try {
            this.stringResources.remove(resource)
        } catch (e: Exception) {
            errors.add(
                DeleteStringResourceError.GeneralError(
                    message = e.message ?: UNKNOWN_ERROR_MESSAGE,
                    resourceId = resource.id,
                    resourceKey = resource.key,
                ),
            )
        }
    }

    return errors
}

sealed interface DeleteStringResourceError {
    data class ResourceIdNotFound(
        val resourceId: String,
    ) : DeleteStringResourceError

    data class GeneralError(
        val message: String,
        val resourceId: String,
        val resourceKey: String,
    ) : DeleteStringResourceError
}

/**
 * Updates the default locale value of a string resource.
 * @param resourceId ID of the string resource to update
 * @param newDefaultLocaleValue New value for the default locale
 * @return List of errors that occurred during the operation
 */
fun StringResourceHolder.updateStringResourceDefaultLocaleValue(
    resourceId: String,
    newDefaultLocaleValue: String,
): List<UpdateStringResourceError> {
    val update =
        StringResourceUpdate(
            id = resourceId,
            localizedValuesToSet = mapOf(this.defaultLocale.value to newDefaultLocaleValue),
        )
    return this.updateStringResources(listOf(update))
}

/**
 * Updates existing string resources.
 * @param updates List of string resource updates to apply
 * @return List of errors that occurred during the operation
 */
fun StringResourceHolder.updateStringResources(updates: List<StringResourceUpdate>): List<UpdateStringResourceError> {
    val errors = mutableListOf<UpdateStringResourceError>()
    if (updates.isEmpty()) {
        return errors
    }

    val defaultLocale = this.defaultLocale.value
    updates.forEach { update ->
        val existingResourceIndex = this.stringResources.indexOfFirst { it.id == update.id }
        if (existingResourceIndex == -1) {
            errors.add(UpdateStringResourceError.ResourceIdNotFound(update.id))
            return@forEach
        }

        val existingResource = this.stringResources[existingResourceIndex]
        try {
            val updatedKey =
                update.key?.let { key ->
                    val existingKeys = this.stringResources.map { it.key }.toSet()
                    generateUniqueName(
                        key.toComposeResourceName(),
                        existingKeys,
                    )
                } ?: existingResource.key
            val updatedDescription = (update.description ?: existingResource.description)?.ifBlank { null }
            val updatedLocalizedValues =
                existingResource.localizedValues.toMutableStateMapEqualsOverride().also { newValues ->
                    update.localizedValuesToDelete.forEach { locale ->
                        if (locale != defaultLocale) {
                            newValues.remove(locale)
                        }
                    }
                    update.localizedValuesToSet.forEach { (locale, value) ->
                        newValues[locale] = value
                    }
                }

            val needsTranslationUpdate =
                // If explicitly set in the update, use that value
                update.needsTranslationUpdate
                    ?: if (this.isTranslatable) {
                        // Otherwise, calculate based on changes
                        val descriptionChanged = existingResource.description != updatedDescription
                        val defaultValueChanged =
                            update.localizedValuesToSet.containsKey(defaultLocale) &&
                                existingResource.localizedValues[defaultLocale] != update.localizedValuesToSet[defaultLocale]

                        // Set flag if content changed, or keep existing flag
                        (descriptionChanged || defaultValueChanged) || existingResource.needsTranslationUpdate
                    } else {
                        // No translation update needed if there's only one locale
                        false
                    }

            // Create the updated resource
            val updatedResource =
                existingResource.copy(
                    key = updatedKey,
                    description = updatedDescription,
                    localizedValues = updatedLocalizedValues,
                    needsTranslationUpdate = needsTranslationUpdate,
                )

            this.stringResources[existingResourceIndex] = updatedResource
        } catch (e: Exception) {
            errors.add(
                UpdateStringResourceError.GeneralError(
                    message = e.message ?: UNKNOWN_ERROR_MESSAGE,
                    resourceId = update.id,
                    resourceKey = existingResource.key,
                ),
            )
        }
    }

    return errors
}

@Serializable
@SerialName("StringResourceUpdate")
data class StringResourceUpdate(
    val id: String,
    val key: String? = null,
    val description: String? = null,
    val localizedValuesToSet: Map<ResourceLocale, String> = emptyMap(),
    val localizedValuesToDelete: Set<ResourceLocale> = emptySet(),
    val needsTranslationUpdate: Boolean? = null,
)

sealed interface UpdateStringResourceError {
    data class ResourceIdNotFound(
        val resourceId: String,
    ) : UpdateStringResourceError

    data class GeneralError(
        val message: String,
        val resourceId: String,
        val resourceKey: String,
    ) : UpdateStringResourceError
}

/**
 * Updates the list of supported locales.
 * @param newLocales New list of locales to support
 * @return List of errors that occurred during the operation
 */
fun StringResourceHolder.updateSupportedLocales(newLocales: List<ResourceLocale>): List<UpdateSupportedLocalesError> {
    val errors = mutableListOf<UpdateSupportedLocalesError>()

    try {
        val defaultLocale = this.defaultLocale.value
        // Ensure default locale is always included
        val newLocalesWithDefault =
            if (newLocales.contains(defaultLocale)) {
                newLocales
            } else {
                listOf(defaultLocale) + newLocales
            }

        // Find locales to add and remove
        val currentLocales = this.supportedLocales.toSet()
        val newLocalesSet = newLocalesWithDefault.toSet()
        val localesToRemove = currentLocales - newLocalesSet
        val localesToAdd = newLocalesSet - currentLocales

        // Remove old locales from string resources
        if (localesToRemove.isNotEmpty()) {
            this.stringResources.forEach { resource ->
                localesToRemove.forEach { locale ->
                    resource.localizedValues.remove(locale)
                }
            }
        }

        // Update needsTranslationUpdate flags based on the locale changes
        when {
            // If new locales are being added, mark all string resources as needing translation
            localesToAdd.isNotEmpty() -> {
                this.stringResources.replaceAll { resource ->
                    resource.copy(needsTranslationUpdate = true)
                }
            }
            // If only the default locale remains, clear the flags
            newLocalesWithDefault.size == 1 -> {
                this.stringResources.replaceAll { resource ->
                    resource.copy(needsTranslationUpdate = false)
                }
            }
        }

        // Replace the supported locales list
        this.supportedLocales.clear()
        this.supportedLocales.addAll(newLocalesWithDefault)
    } catch (e: Exception) {
        errors.add(
            UpdateSupportedLocalesError.GeneralError(
                message = e.message ?: UNKNOWN_ERROR_MESSAGE,
            ),
        )
    }

    return errors
}

sealed interface UpdateSupportedLocalesError {
    data class GeneralError(
        val message: String,
    ) : UpdateSupportedLocalesError
}

/**
 * Sets the default locale for the string resource holder.
 * @param locale The new default locale
 * @return List of errors that occurred during the operation
 */
fun StringResourceHolder.setDefaultLocale(locale: ResourceLocale): List<SetDefaultLocaleError> {
    val errors = mutableListOf<SetDefaultLocaleError>()

    try {
        if (!this.supportedLocales.contains(locale)) {
            this.supportedLocales.add(locale)
        }
        val previousLocale = this.defaultLocale.value
        this.defaultLocale.value = locale

        // If the default locale has actually changed and there are multiple locales,
        // set needsTranslationUpdate flag on all resources
        if (previousLocale != locale && this.isTranslatable) {
            this.stringResources.replaceAll { resource ->
                resource.copy(needsTranslationUpdate = true)
            }
        }
    } catch (e: Exception) {
        errors.add(
            SetDefaultLocaleError.GeneralError(
                message = e.message ?: UNKNOWN_ERROR_MESSAGE,
            ),
        )
    }

    return errors
}

sealed interface SetDefaultLocaleError {
    data class GeneralError(
        val message: String,
    ) : SetDefaultLocaleError
}

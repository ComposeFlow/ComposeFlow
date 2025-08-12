package io.composeflow.model.project.string

import io.composeflow.override.mutableStateMapEqualsOverrideOf
import io.composeflow.override.toMutableStateMapEqualsOverride
import io.composeflow.serializer.FallbackMutableStateMapSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

typealias StringResourceId = String

@Serializable
@SerialName("StringResource")
data class StringResource(
    val id: StringResourceId = Uuid.random().toString(),
    val key: String,
    /** Optional description for the string resource. Can be used to provide context for translators. */
    val description: String? = null,
    @Serializable(with = FallbackMutableStateMapSerializer::class)
    val localizedValues: MutableMap<ResourceLocale, String> = mutableStateMapEqualsOverrideOf(),
    /**
     * Tracks whether the default locale value has been updated since the last translation.
     * This helps identify which strings need re-translation.
     */
    val needsTranslationUpdate: Boolean = false,
)

@JvmName("stringResourceOfWithLocale")
fun stringResourceOf(
    key: String,
    vararg localizedValues: Pair<ResourceLocale, String>,
    description: String? = null,
    id: StringResourceId = Uuid.random().toString(),
): StringResource =
    StringResource(
        id = id,
        key = key,
        description = description,
        localizedValues = localizedValues.toMap().toMutableStateMapEqualsOverride(),
    )

@JvmName("stringResourceOfWithString")
fun stringResourceOf(
    key: String,
    vararg localizedValues: Pair<String, String>,
    description: String? = null,
    id: StringResourceId = Uuid.random().toString(),
): StringResource =
    StringResource(
        id = id,
        key = key,
        description = description,
        localizedValues =
            localizedValues
                .associate { (locale, value) ->
                    ResourceLocale.fromStringOrThrow(locale) to value
                }.toMutableStateMapEqualsOverride(),
    )

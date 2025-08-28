package io.composeflow.serializer

import co.touchlab.kermit.Logger
import com.charleskorn.kaml.MissingTypeTagException
import com.charleskorn.kaml.PolymorphismStyle
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.charleskorn.kaml.YamlNode
import io.composeflow.model.parameter.wrapper.shapeWrapperModule
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationException
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.modules.SerializersModule

val composeflowModule =
    SerializersModule {
        include(shapeWrapperModule)
    }

private val yamlConfiguration =
    YamlConfiguration(
        strictMode = false,
        breakScalarsAt = Int.MAX_VALUE,
        polymorphismStyle = PolymorphismStyle.Tag,
        encodeDefaults = false,
    )

/**
 * Default yaml serializer that deserializes the type tag as
 * (eg. !<typeOfThing> { property: value })
 *
 * @deprecated Use exposed functions like decodeFromStringWithFallback, encodeToString,
 * parseToYamlNode, or decodeFromYamlNodeWithFallback instead of accessing this serializer directly.
 */
@Deprecated("Use exposed functions instead of accessing serializer directly")
val yamlDefaultSerializer =
    Yaml(
        configuration = yamlConfiguration,
        serializersModule = composeflowModule,
    )

/**
 * Fallback yaml serializer where it deserializes the type tag as
 * { type: typeOfThing, property: value }
 *
 * @deprecated Use exposed functions like decodeFromStringWithFallback, encodeToString,
 * parseToYamlNode, or decodeFromYamlNodeWithFallback instead of accessing this serializer directly.
 */
@Deprecated("Use exposed functions instead of accessing serializer directly")
val yamlPropertyBasedSerializer =
    Yaml(
        configuration = yamlConfiguration.copy(polymorphismStyle = PolymorphismStyle.Property),
        serializersModule = composeflowModule,
    )

/**
 * Checks if this throwable or any of its causes is a [MissingTypeTagException].
 */
val Throwable.isCausedByMissingTypeTag: Boolean
    get() = this is MissingTypeTagException || this.cause?.isCausedByMissingTypeTag == true

/**
 * Wrapper method that attempts to decode using yamlDefaultSerializer first,
 * and falls back to yamlPropertyBasedSerializer only if the exception indicates
 * a missing 'type' property (property-based polymorphism issue).
 * Other SerializationExceptions are re-thrown to avoid masking real issues.
 */
@Suppress("deprecation")
inline fun <reified T> decodeFromStringWithFallback(yamlContent: String): T =
    try {
        yamlDefaultSerializer.decodeFromString<T>(yamlContent)
    } catch (e: SerializationException) {
        // Only fallback if the error is specifically about missing 'type' property
        // which indicates the YAML might be using property-based polymorphism
        if (e.isCausedByMissingTypeTag) {
            yamlPropertyBasedSerializer.decodeFromString<T>(yamlContent)
        } else {
            // Re-throw other serialization exceptions to avoid masking real issues
            throw e
        }
    }

/**
 * Wrapper method that attempts to decode using yamlDefaultSerializer first,
 * and falls back to yamlPropertyBasedSerializer only if the exception indicates
 * a missing 'type' property (property-based polymorphism issue).
 * Other SerializationExceptions are re-thrown to avoid masking real issues.
 * This version takes a DeserializationStrategy for non-reified types.
 */
@Suppress("deprecation")
fun <T> decodeFromStringWithFallback(
    deserializer: DeserializationStrategy<T>,
    yamlContent: String,
): T =
    try {
        yamlDefaultSerializer.decodeFromString(deserializer, yamlContent)
    } catch (e: SerializationException) {
        // Only fallback if the error is specifically about missing 'type' property
        // which indicates the YAML might be using property-based polymorphism
        if (e.isCausedByMissingTypeTag) {
            yamlPropertyBasedSerializer.decodeFromString(deserializer, yamlContent)
        } else {
            // Re-throw other serialization exceptions to avoid masking real issues
            throw e
        }
    }

/**
 * Encodes the given value to YAML string using the default tag-based serializer.
 * Uses the tag-based polymorphism format (eg. !<typeOfThing> { property: value })
 */
@Suppress("deprecation")
inline fun <reified T> encodeToString(value: T): String = yamlDefaultSerializer.encodeToString(value)

/**
 * Encodes the given value to YAML string using the default tag-based serializer.
 * Uses the tag-based polymorphism format (eg. !<typeOfThing> { property: value })
 * This version takes a SerializationStrategy for non-reified types.
 */
@Suppress("deprecation")
fun <T> encodeToString(
    serializer: SerializationStrategy<T>,
    value: T,
): String = yamlDefaultSerializer.encodeToString(serializer, value)

/**
 * Parses a YAML string into a YamlNode using the default tag-based serializer.
 * Uses the tag-based polymorphism format (eg. !<typeOfThing> { property: value })
 */
@Suppress("deprecation")
fun parseToYamlNode(yamlContent: String): YamlNode = yamlDefaultSerializer.parseToYamlNode(yamlContent)

/**
 * Decodes a YamlNode to the specified type with fallback support.
 * Attempts to decode using yamlDefaultSerializer first,
 * and falls back to yamlPropertyBasedSerializer only if the exception indicates
 * a missing 'type' property (property-based polymorphism issue).
 * Other SerializationExceptions are re-thrown to avoid masking real issues.
 */
@Suppress("deprecation")
fun <T> decodeFromYamlNodeWithFallback(
    deserializer: DeserializationStrategy<T>,
    yamlNode: YamlNode,
): T =
    try {
        yamlDefaultSerializer.decodeFromYamlNode(deserializer, yamlNode)
    } catch (e: SerializationException) {
        // Only fallback if the error is specifically about missing 'type' property
        // which indicates the YAML might be using property-based polymorphism
        if (e.isCausedByMissingTypeTag) {
            yamlPropertyBasedSerializer.decodeFromYamlNode(deserializer, yamlNode)
        } else {
            // Re-throw other serialization exceptions to avoid masking real issues
            throw e
        }
    }

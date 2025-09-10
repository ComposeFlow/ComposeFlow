@file:Suppress("ktlint:standard:filename")

package io.composeflow.serializer

import co.touchlab.kermit.Logger
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind.STRING
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.reflect.KClass

/**
 * WASM implementation of FallbackEnumSerializerInternal.
 * Limited functionality compared to JVM version due to reflection limitations.
 */
actual class FallbackEnumSerializerInternal<T : Enum<T>> actual constructor(
    enumClass: KClass<T>,
    private val values: Array<T>,
) : KSerializer<T> {
    private val fallback =
        values.firstOrNull()
            ?: throw IllegalArgumentException("Enum class has no values")
    private val enumClassName = enumClass.simpleName ?: "Unknown"

    actual override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(enumClassName, STRING)

    actual override fun serialize(
        encoder: Encoder,
        value: T,
    ) {
        Logger.i("FallbackEnumSerializerInternal.serialize called on WASM")
        encoder.encodeString(value.name)
    }

    actual override fun deserialize(decoder: Decoder): T {
        val stringValue = decoder.decodeString().trim()

        // Fast path: exact match
        val exactMatch = values.find { it.name == stringValue }
        if (exactMatch != null) return exactMatch

        // Fallback: case-insensitive match
        val caseInsensitiveMatch = values.find { it.name.equals(stringValue, ignoreCase = true) }
        if (caseInsensitiveMatch != null) return caseInsensitiveMatch

        // Last resort: use fallback and log
        if (stringValue.isNotEmpty()) {
            Logger.w("Warning: Unknown $enumClassName value '$stringValue', falling back to '${fallback.name}'")
        }

        return fallback
    }
}

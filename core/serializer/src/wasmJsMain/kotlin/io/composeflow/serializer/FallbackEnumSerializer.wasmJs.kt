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
) : KSerializer<T> {
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
        Logger.w("FallbackEnumSerializerInternal.deserialize called on WASM - enum deserialization not fully supported")

        // On WASM, we can't easily access enum constants without reflection
        // This is a limitation that would need to be handled differently
        throw UnsupportedOperationException("Enum deserialization not supported on WASM target")
    }
}

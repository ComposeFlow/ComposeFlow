package io.composeflow.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.reflect.KClass

expect class FallbackEnumSerializerInternal<T : Enum<T>>(
    enumClass: KClass<T>,
) : KSerializer<T> {
    override val descriptor: SerialDescriptor

    override fun serialize(
        encoder: Encoder,
        value: T,
    )

    override fun deserialize(decoder: Decoder): T
}

/**
 * Location-aware FallbackEnumSerializer class that can be used in @Serializable annotations.
 * Provides enhanced error reporting with precise location information when enum parsing fails.
 */
open class FallbackEnumSerializer<T : Enum<T>>(
    enumClass: KClass<T>,
) : KSerializer<T> {
    private val delegate = FallbackEnumSerializerInternal(enumClass).withLocationAwareExceptions()

    override val descriptor: SerialDescriptor = delegate.descriptor

    override fun serialize(
        encoder: Encoder,
        value: T,
    ) {
        delegate.serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): T = delegate.deserialize(decoder)
}

package io.composeflow.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

// TODO: For multiplatform compatibility, use Any as the Type for serializer
actual object ClassSerializer : KSerializer<Any> {
    actual override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("WithClassSerializer", PrimitiveKind.STRING)

    actual override fun serialize(
        encoder: Encoder,
        value: Any,
    ) {
        when (value) {
            is Class<*> -> encoder.encodeString(value.name)
            is String -> encoder.encodeString(value)
            else -> encoder.encodeString(value::class.java.name)
        }
    }

    actual override fun deserialize(decoder: Decoder): Any {
        val className = decoder.decodeString()
        return try {
            Class.forName(className)
        } catch (e: ClassNotFoundException) {
            throw SerializationException("Cannot find class $className")
        }
    }
}

package io.composeflow.serializer

import co.touchlab.kermit.Logger
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

actual object ClassSerializer : KSerializer<Any> {
    actual override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("WithClassSerializer", PrimitiveKind.STRING)

    actual override fun serialize(
        encoder: Encoder,
        value: Any,
    ) {
        Logger.i("ClassSerializer.serialize called on WASM - serializing class name as string")
        encoder.encodeString(value as String)
    }

    actual override fun deserialize(decoder: Decoder): Any {
        Logger.i("ClassSerializer.deserialize called on WASM - returning class name as string")
        return decoder.decodeString()
    }
}

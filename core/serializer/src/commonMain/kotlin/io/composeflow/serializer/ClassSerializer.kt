package io.composeflow.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

expect object ClassSerializer : KSerializer<Any> {
    override val descriptor: SerialDescriptor
    override fun serialize(encoder: Encoder, value: Any)
    override fun deserialize(decoder: Decoder): Any
}

package io.composeflow.model.project.issue

import io.composeflow.model.action.Action
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.serializer

/**
 * Custom serializer for issueContext that attempts to serialize the value if it's serializable,
 * and skips it (uses null) otherwise.
 */
@OptIn(ExperimentalSerializationApi::class)
object IssueContextSerializer : KSerializer<Any?> {
    private val actionSerializer = Action.serializer()

    override val descriptor: SerialDescriptor = actionSerializer.descriptor

    override fun serialize(
        encoder: Encoder,
        value: Any?,
    ) {
        when (value) {
            null -> {
                encoder.encodeNull()
            }
            is Action -> {
                encoder.encodeSerializableValue(actionSerializer, value)
            }
            // Add more cases here for other known serializable types if needed
            else -> {
                // If we don't know how to serialize it, encode null
                encoder.encodeNull()
            }
        }
    }

    override fun deserialize(decoder: Decoder): Any? =
        try {
            decoder.decodeNullableSerializableValue(actionSerializer)
        } catch (_: Exception) {
            // If deserialization fails, return null
            null
        }
}

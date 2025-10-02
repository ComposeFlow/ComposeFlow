package io.composeflow.model.property

import com.charleskorn.kaml.YamlContentPolymorphicSerializer
import com.charleskorn.kaml.YamlInput
import com.charleskorn.kaml.YamlNode
import com.charleskorn.kaml.YamlScalar
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * A custom serializer for AssignableProperty that handles two input formats:
 * 1. Plain string values (deserialized as StringIntrinsicValue for backward compatibility)
 * 2. Full AssignableProperty structures with type tags
 *
 * This serializer uses kaml's YamlContentPolymorphicSerializer for deserialization, which inspects
 * the YAML node structure before deserialization to determine the appropriate deserialization strategy.
 * A regular KSerializer cannot be used here because kaml tries to retrieve the type discriminator
 * before deserialization begins, which would fail when encountering plain string values.
 *
 * For serialization, this serializer delegates to the standard AssignableProperty.serializer() to
 * ensure that YAML tags are properly output according to the configured polymorphism style.
 *
 * When the input format is not YAML (e.g., JSON), this serializer falls back to using the default
 * AssignableProperty serializer.
 */
object AssignablePropertyOrStringSerializer : YamlContentPolymorphicSerializer<AssignableProperty>(AssignableProperty::class) {
    private val assignablePropertySerializer = AssignableProperty.serializer()

    override fun deserialize(decoder: Decoder): AssignableProperty =
        if (decoder is YamlInput) {
            super.deserialize(decoder)
        } else {
            assignablePropertySerializer.deserialize(decoder)
        }

    override fun serialize(
        encoder: Encoder,
        value: AssignableProperty,
    ) {
        assignablePropertySerializer.serialize(encoder, value)
    }

    override fun selectDeserializer(node: YamlNode): DeserializationStrategy<AssignableProperty> =
        if (node is YamlScalar) {
            StringIntrinsicValueFromStringSerializer
        } else {
            AssignableProperty.serializer()
        }
}

private object StringIntrinsicValueFromStringSerializer : DeserializationStrategy<AssignableProperty> {
    override val descriptor = String.serializer().descriptor

    override fun deserialize(decoder: Decoder): AssignableProperty {
        val stringValue = decoder.decodeString()
        return StringProperty.StringIntrinsicValue(value = stringValue)
    }
}

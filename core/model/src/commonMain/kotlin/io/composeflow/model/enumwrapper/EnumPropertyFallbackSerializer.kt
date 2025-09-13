package io.composeflow.model.enumwrapper

import co.touchlab.kermit.Logger
import io.composeflow.model.property.AssignableProperty
import io.composeflow.model.property.EnumProperty
import io.composeflow.serializer.decodeFromStringWithFallback
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Custom serializer for [EnumProperty] with fallbacks. If serialization fails, it falls back to
 * serialize with the specified default enum value. If deserialization fails, it will try to
 * deserialize as an EnumWrapper instead of an AssignableProperty. If that also fails, it returns
 * an EnumProperty property with the specified default value.
 *
 * @param T The EnumWrapper implementation type (e.g., ContentScaleWrapper, TextDecorationWrapper)
 * @param defaultValue The default value to use when deserialization fails
 * @param valueSerializer The serializer for the EnumWrapper values
 */
open class EnumPropertyFallbackSerializer<T>(
    private val valueSerializer: KSerializer<T>,
    private val defaultValue: T,
) : KSerializer<AssignableProperty> where T : EnumWrapper {
    private val delegateSerializer = AssignableProperty.serializer()

    override val descriptor: SerialDescriptor = delegateSerializer.descriptor

    override fun serialize(
        encoder: Encoder,
        value: AssignableProperty,
    ) {
        try {
            delegateSerializer.serialize(encoder, value)
        } catch (_: Exception) {
            // If serialization fails, serialize as EnumProperty with the default value
            delegateSerializer.serialize(encoder, EnumProperty(defaultValue))
        }
    }

    override fun deserialize(decoder: Decoder): AssignableProperty =
        try {
            // Handle YAML like: `!<EnumProperty> value: !<ContentScaleWrapper> "Inside"`
            delegateSerializer.deserialize(decoder)
        } catch (_: Exception) {
            try {
                // If deserialization fails, deserialize as EnumWrapper value and wrap it in EnumProperty
                Logger.d { "Falling back to EnumWrapper deserialization for ${valueSerializer.descriptor.serialName}" }
                val propertyValue =
                    try {
                        // Handle YAML like: `!<EnumProperty> "Inside"`
                        valueSerializer.deserialize(decoder)
                    } catch (_: Exception) {
                        // Handle YAML like: `!<EnumProperty> value: "Inside"`
                        val stringValueObj = StringValueEnumProperty.serializer().deserialize(decoder)
                        decodeFromStringWithFallback(valueSerializer, stringValueObj.value)
                    }
                EnumProperty(propertyValue)
            } catch (_: Exception) {
                // If that also fails, return EnumProperty with the default value
                Logger.d { "Falling back to default EnumProperty for ${valueSerializer.descriptor.serialName}: $defaultValue" }
                EnumProperty(defaultValue)
            }
        }

    /**
     * Helper serializable for deserializing EnumProperty with string value field.
     * This handles YAML like: `!<EnumProperty> value: "Inside"`
     */
    @Serializable
    private data class StringValueEnumProperty(
        val value: String,
    )
}

object TextDecorationPropertyFallbackSerializer : EnumPropertyFallbackSerializer<TextDecorationWrapper>(
    valueSerializer = TextDecorationWrapper.TextDecorationWrapperSerializer,
    defaultValue = TextDecorationWrapper.None,
)

object TextStylePropertyFallbackSerializer : EnumPropertyFallbackSerializer<TextStyleWrapper>(
    valueSerializer = TextStyleWrapper.TextStyleWrapperSerializer,
    defaultValue = TextStyleWrapper.BodyMedium,
)

object FontStylePropertyFallbackSerializer : EnumPropertyFallbackSerializer<FontStyleWrapper>(
    valueSerializer = FontStyleWrapper.FontStyleWrapperSerializer,
    defaultValue = FontStyleWrapper.Normal,
)

object TextAlignPropertyFallbackSerializer : EnumPropertyFallbackSerializer<TextAlignWrapper>(
    valueSerializer = TextAlignWrapper.TextAlignWrapperSerializer,
    defaultValue = TextAlignWrapper.Start,
)

object TextOverflowPropertyFallbackSerializer : EnumPropertyFallbackSerializer<TextOverflowWrapper>(
    valueSerializer = TextOverflowWrapper.TextOverflowWrapperSerializer,
    defaultValue = TextOverflowWrapper.Clip,
)

object ContentScalePropertyFallbackSerializer : EnumPropertyFallbackSerializer<ContentScaleWrapper>(
    valueSerializer = ContentScaleWrapper.ContentScaleWrapperSerializer,
    defaultValue = ContentScaleWrapper.Fit,
)

object TextFieldColorsPropertyFallbackSerializer : EnumPropertyFallbackSerializer<TextFieldColorsWrapper>(
    valueSerializer = TextFieldColorsWrapper.TextFieldColorsWrapperSerializer,
    defaultValue = TextFieldColorsWrapper.Default,
)

object NodeVisibilityPropertyFallbackSerializer : EnumPropertyFallbackSerializer<NodeVisibility>(
    valueSerializer = NodeVisibility.NodeVisibilitySerializer,
    defaultValue = NodeVisibility.AlwaysVisible,
)

package io.composeflow.model.enumwrapper

import io.composeflow.model.property.AssignableProperty
import io.composeflow.model.property.EnumProperty
import io.composeflow.serializer.decodeFromStringWithFallback
import io.composeflow.serializer.encodeToString
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalSerializationApi::class)
class EnumPropertyFallbackSerializerTest {
    @Serializable
    private data class ContentScalePropertyWrapper(
        @Serializable(with = ContentScalePropertyFallbackSerializer::class)
        val property: AssignableProperty,
    )

    @Test
    fun `serialize valid EnumProperty`() {
        // Given
        val property = EnumProperty(ContentScaleWrapper.Crop)
        val wrapper = ContentScalePropertyWrapper(property)

        // When
        val yamlString = encodeToString(ContentScalePropertyWrapper.serializer(), wrapper)

        // Then
        assertTrue(yamlString.contains("EnumProperty"))
        assertTrue(yamlString.contains("ContentScaleWrapper"))
        assertTrue(yamlString.contains("Crop"))
    }

    @Test
    fun `deserialize valid EnumProperty YAML`() {
        // Given
        val yamlString =
            """
            property: !<EnumProperty>
              value: !<ContentScaleWrapper> "FillBounds"
            """.trimIndent()

        // When
        val wrapper = decodeFromStringWithFallback(ContentScalePropertyWrapper.serializer(), yamlString)

        // Then
        assertIs<EnumProperty>(wrapper.property)
        assertIs<ContentScaleWrapper>(wrapper.property.value)
        assertEquals(ContentScaleWrapper.FillBounds, wrapper.property.value)
    }

    @Test
    fun `deserialize EnumProperty which content is directly enum value`() {
        // Given - Old format where the value was directly an EnumWrapper without wrapping in EnumProperty
        val yamlString =
            """
            property: !<EnumProperty> "Inside"
            """.trimIndent()

        // When
        val wrapper = decodeFromStringWithFallback(ContentScalePropertyWrapper.serializer(), yamlString)

        // Then
        assertIs<EnumProperty>(wrapper.property)
        assertIs<ContentScaleWrapper>(wrapper.property.value)
        assertEquals(ContentScaleWrapper.Inside, wrapper.property.value)
    }

    @Test
    fun `deserialize EnumProperty having enum value as value property`() {
        // Given - Old format where the value was directly an EnumWrapper without wrapping in EnumProperty
        val yamlString =
            """
            property: !<EnumProperty>
              value: Inside
            """.trimIndent()

        // When
        val wrapper = decodeFromStringWithFallback(ContentScalePropertyWrapper.serializer(), yamlString)

        // Then
        assertIs<EnumProperty>(wrapper.property)
        assertIs<ContentScaleWrapper>(wrapper.property.value)
        assertEquals(ContentScaleWrapper.Inside, wrapper.property.value)
    }

    @Test
    fun `deserialize with unknown enum value falls back to default`() {
        // Given
        val yamlString =
            """
            property: !<EnumProperty>
              value: !<ContentScaleWrapper>
                value: UnknownValue
            """.trimIndent()

        // When
        val wrapper = decodeFromStringWithFallback(ContentScalePropertyWrapper.serializer(), yamlString)

        // Then
        assertIs<EnumProperty>(wrapper.property)
        assertIs<ContentScaleWrapper>(wrapper.property.value)
        assertEquals(ContentScaleWrapper.Fit, wrapper.property.value) // Falls back to default
    }

    @Test
    fun `fallback behavior for each concrete serializer`() {
        // Test that each concrete serializer has correct default values
        val invalidYaml =
            """
            property: !<InvalidType>
              someField: value
            """.trimIndent()

        run {
            @Serializable
            data class Wrapper(
                @Serializable(with = TextDecorationPropertyFallbackSerializer::class)
                val property: AssignableProperty,
            )
            val wrapper = decodeFromStringWithFallback(Wrapper.serializer(), invalidYaml)
            assertIs<EnumProperty>(wrapper.property)
            assertEquals(TextDecorationWrapper.None, wrapper.property.value)
        }
        run {
            @Serializable
            data class Wrapper(
                @Serializable(with = TextStylePropertyFallbackSerializer::class)
                val property: AssignableProperty,
            )
            val wrapper = decodeFromStringWithFallback(Wrapper.serializer(), invalidYaml)
            assertIs<EnumProperty>(wrapper.property)
            assertEquals(TextStyleWrapper.BodyMedium, wrapper.property.value)
        }
        run {
            @Serializable
            data class Wrapper(
                @Serializable(with = FontStylePropertyFallbackSerializer::class)
                val property: AssignableProperty,
            )
            val wrapper = decodeFromStringWithFallback(Wrapper.serializer(), invalidYaml)
            assertIs<EnumProperty>(wrapper.property)
            assertEquals(io.composeflow.model.enumwrapper.FontStyleWrapper.Normal, wrapper.property.value)
        }
        run {
            @Serializable
            data class Wrapper(
                @Serializable(with = TextAlignPropertyFallbackSerializer::class)
                val property: AssignableProperty,
            )
            val wrapper = decodeFromStringWithFallback(Wrapper.serializer(), invalidYaml)
            assertIs<EnumProperty>(wrapper.property)
            assertEquals(io.composeflow.model.enumwrapper.TextAlignWrapper.Start, wrapper.property.value)
        }
        run {
            @Serializable
            data class Wrapper(
                @Serializable(with = TextOverflowPropertyFallbackSerializer::class)
                val property: AssignableProperty,
            )
            val wrapper = decodeFromStringWithFallback(Wrapper.serializer(), invalidYaml)
            assertIs<EnumProperty>(wrapper.property)
            assertEquals(io.composeflow.model.enumwrapper.TextOverflowWrapper.Clip, wrapper.property.value)
        }
        run {
            @Serializable
            data class Wrapper(
                @Serializable(with = ContentScalePropertyFallbackSerializer::class)
                val property: AssignableProperty,
            )
            val wrapper = decodeFromStringWithFallback(Wrapper.serializer(), invalidYaml)
            assertIs<EnumProperty>(wrapper.property)
            assertEquals(ContentScaleWrapper.Fit, wrapper.property.value)
        }
        run {
            @Serializable
            data class Wrapper(
                @Serializable(with = TextFieldColorsPropertyFallbackSerializer::class)
                val property: AssignableProperty,
            )
            val wrapper = decodeFromStringWithFallback(Wrapper.serializer(), invalidYaml)
            assertIs<EnumProperty>(wrapper.property)
            assertEquals(TextFieldColorsWrapper.Default, wrapper.property.value)
        }
        run {
            @Serializable
            data class Wrapper(
                @Serializable(with = NodeVisibilityPropertyFallbackSerializer::class)
                val property: AssignableProperty,
            )
            val wrapper = decodeFromStringWithFallback(Wrapper.serializer(), invalidYaml)
            assertIs<EnumProperty>(wrapper.property)
            assertEquals(NodeVisibility.AlwaysVisible, wrapper.property.value)
        }
    }
}

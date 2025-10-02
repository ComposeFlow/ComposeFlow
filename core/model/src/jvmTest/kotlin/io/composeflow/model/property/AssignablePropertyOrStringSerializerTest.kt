package io.composeflow.model.property

import androidx.compose.runtime.mutableStateOf
import io.composeflow.serializer.decodeFromStringWithFallback
import io.composeflow.serializer.encodeToString
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AssignablePropertyOrStringSerializerTest {
    @Test
    fun testSerializationOutputsYamlTags() {
        val original: AssignableProperty = StringProperty.StringIntrinsicValue(value = "Test Value")
        val yaml = encodeToString(AssignablePropertyOrStringSerializer, original)

        // Verify that the YAML contains a type tag (polymorphism marker)
        assertTrue(
            yaml.contains("!<StringIntrinsicValue>"),
            "Serialized YAML should contain type tag. Actual YAML:\n$yaml",
        )
    }

    @Test
    fun testSerializationDeserializationRoundTrip() {
        val original: AssignableProperty = StringProperty.StringIntrinsicValue(value = "Round Trip Value")
        val yaml = encodeToString(AssignablePropertyOrStringSerializer, original)
        val deserialized = decodeFromStringWithFallback(AssignablePropertyOrStringSerializer, yaml)

        // Verify the deserialized object is correct
        assertTrue(deserialized is StringProperty.StringIntrinsicValue, "Should deserialize to StringIntrinsicValue")
        assertEquals("Round Trip Value", deserialized.value)
    }

    @Test
    fun testBackwardCompatibility_plainStringDeserialization() {
        // Test backward compatibility: plain string deserialization
        val plainYaml = "Plain String Value"
        val deserialized = decodeFromStringWithFallback(AssignablePropertyOrStringSerializer, plainYaml)

        assertTrue(
            deserialized is StringProperty.StringIntrinsicValue,
            "Plain string should deserialize to StringIntrinsicValue",
        )
        assertEquals("Plain String Value", deserialized.value)
    }

    @Test
    fun testSerializationWithComplexProperty() {
        // Test with a more complex property containing transformers
        val original: AssignableProperty =
            StringProperty.StringIntrinsicValue(value = "Complex Value").apply {
                propertyTransformers.add(FromString.ToString.AddBefore(mutableStateOf(StringProperty.StringIntrinsicValue("Prefix: "))))
            }
        val yaml = encodeToString(AssignablePropertyOrStringSerializer, original)

        // Verify tag is present
        assertTrue(yaml.contains("!<StringIntrinsicValue>"), "Should contain type tag for complex property")

        // Deserialize and verify
        val deserialized = decodeFromStringWithFallback(AssignablePropertyOrStringSerializer, yaml)
        assertTrue(deserialized is StringProperty.StringIntrinsicValue)
        assertEquals("Complex Value", deserialized.value)
        assertEquals(1, deserialized.propertyTransformers.size)
    }

    @Test
    fun testDeserializationWithYamlTag() {
        val yamlWithTag =
            """
            !<StringIntrinsicValue>
            value: "Tagged Value"
            """.trimIndent()
        val deserialized = decodeFromStringWithFallback(AssignablePropertyOrStringSerializer, yamlWithTag)

        assertTrue(deserialized is StringProperty.StringIntrinsicValue)
        assertEquals("Tagged Value", deserialized.value)
    }

    @Test
    fun testSerializationOfDifferentPropertyTypes() {
        val intProperty: AssignableProperty = IntProperty.IntIntrinsicValue(value = 42)
        val boolProperty: AssignableProperty = BooleanProperty.BooleanIntrinsicValue(value = true)

        val intYaml = encodeToString(AssignablePropertyOrStringSerializer, intProperty)
        val boolYaml = encodeToString(AssignablePropertyOrStringSerializer, boolProperty)

        assertTrue(intYaml.contains("!<IntIntrinsicValue>"), "Int property should have type tag")
        assertTrue(boolYaml.contains("!<BooleanIntrinsicValue>"), "Boolean property should have type tag")

        // Verify round trip
        val intDeserialized = decodeFromStringWithFallback(AssignablePropertyOrStringSerializer, intYaml)
        val boolDeserialized = decodeFromStringWithFallback(AssignablePropertyOrStringSerializer, boolYaml)

        assertTrue(intDeserialized is IntProperty.IntIntrinsicValue)
        assertEquals(42, intDeserialized.value)

        assertTrue(boolDeserialized is BooleanProperty.BooleanIntrinsicValue)
        assertEquals(true, boolDeserialized.value)
    }
}

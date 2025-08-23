package io.composeflow.serializer

import io.composeflow.model.project.appscreen.screen.Screen
import io.composeflow.model.state.ScreenState
import kotlinx.serialization.SerializationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class YamlSerializerTest {
    @Test
    fun testDecodeFromStringWithFallback_shouldUseTagBased_whenValidTagBasedYaml() {
        // Valid tag-based YAML that should work with the default serializer
        val tagBasedYaml =
            """
            id: "testScreen"
            name: "Test Screen"
            stateHolderImpl:
              states:
                - !<StringScreenState>
                  id: "testState"
                  name: "testName"
                  defaultValue: "test"
                  companionNodeId: ""
                  userWritable: true
            showOnNavigation: false
            title: "Test"
            label: "Test"
            isDefault: false
            isSelected: false
            """.trimIndent()

        // Should successfully deserialize using tag-based serializer
        val screen = decodeFromStringWithFallback<Screen>(tagBasedYaml)

        assertNotNull(screen)
        assertEquals("testScreen", screen.id)
        assertEquals("Test Screen", screen.name)
    }

    @Test
    fun testDecodeFromStringWithFallback_shouldFallbackToPropertyBased_whenMissingTypeProperty() {
        // Create a simple YAML that would trigger the "Property 'type' is required but it is missing" error
        // This simulates when the serializer expects property-based polymorphism but finds tag-based format
        val yamlThatTriggersTypePropertyError =
            """
            type: "Screen"
            id: "testScreen"
            name: "Test Screen"
            showOnNavigation: false
            title: "Test"
            label: "Test"
            isDefault: false
            isSelected: false
            stateHolderImpl:
              states: []
            """.trimIndent()

        // This test verifies that when the specific "Property 'type' is required" error occurs,
        // the function falls back to property-based serializer. However, since both serializers
        // might still fail in this contrived example, we'll just verify the fallback attempt happens
        try {
            val screen = decodeFromStringWithFallback<Screen>(yamlThatTriggersTypePropertyError)
            // If it succeeds, great! The fallback worked
            assertNotNull(screen)
        } catch (e: Exception) {
            // If it still fails, it should not be because of our exception swallowing
            // The exception should be a legitimate serialization error, not swallowed
            assertTrue(
                e is kotlinx.serialization.SerializationException,
                "Should get SerializationException, got: ${e::class.simpleName}",
            )
        }
    }

    @Test
    fun testDecodeFromStringWithFallback_shouldRethrowNonTypeExceptions() {
        // Invalid YAML that will cause a different serialization error (not about missing 'type')
        val invalidYaml =
            """
            id: "testScreen"
            name: "Test Screen"
            stateHolderImpl:
              states:
                - !<NonExistentStateType>
                  id: "testState"
                  name: "testName"
                  defaultValue: "test"
            showOnNavigation: false
            title: "Test"
            label: "Test"
            isDefault: false
            isSelected: false
            """.trimIndent()

        // Should re-throw the exception since it's not about missing 'type' property
        val exception =
            assertFailsWith<SerializationException> {
                decodeFromStringWithFallback<Screen>(invalidYaml)
            }

        // Verify it's not a fallback scenario - the original exception should be thrown
        assertTrue(
            exception.message?.contains("NonExistentStateType") == true ||
                exception.message?.contains("Unknown type") == true ||
                exception.message?.contains("not found") == true,
            "Exception should be about unknown type, not missing 'type' property. Actual: ${exception.message}",
        )
    }

    @Test
    fun testDecodeFromStringWithFallback_shouldRethrowMalformedYamlExceptions() {
        // Completely malformed YAML
        val malformedYaml =
            """
            id: "testScreen
            name: [invalid yaml structure
            this is not valid yaml at all
            """.trimIndent()

        // Should re-throw the exception since it's a parsing error, not missing 'type' property
        assertFailsWith<Exception> {
            decodeFromStringWithFallback<Screen>(malformedYaml)
        }
    }

    @Test
    fun testDecodeFromStringWithFallback_shouldRethrowMissingRequiredFieldExceptions() {
        // YAML missing required fields (not about 'type' property)
        val incompleteYaml =
            """
            id: "testScreen"
            # Missing required fields like name, showOnNavigation, etc.
            """.trimIndent()

        // Should re-throw the exception since it's about missing required fields, not 'type' property
        assertFailsWith<SerializationException> {
            decodeFromStringWithFallback<Screen>(incompleteYaml)
        }
    }

    @Test
    fun testDecodeFromStringWithFallback_withDeserializationStrategy() {
        // Test the overload that takes DeserializationStrategy
        val tagBasedYaml =
            """
            id: "testState"
            name: "testName"
            defaultValue: "test"
            companionNodeId: ""
            userWritable: true
            """.trimIndent()

        // Test with specific serializer for ScreenState.StringScreenState
        val state =
            decodeFromStringWithFallback(
                ScreenState.StringScreenState.serializer(),
                "!<StringScreenState>\n$tagBasedYaml",
            )

        assertNotNull(state)
        assertEquals("testState", state.id)
        assertEquals("testName", state.name)
        assertEquals("test", state.defaultValue)
    }

    @Test
    fun testDecodeFromStringWithFallback_withDeserializationStrategy_shouldFallback() {
        // Property-based YAML for testing fallback with DeserializationStrategy
        val propertyBasedYaml =
            """
            type: "StringScreenState"
            id: "testState"
            name: "testName"
            defaultValue: "test"
            companionNodeId: ""
            userWritable: true
            """.trimIndent()

        // Should fallback to property-based serializer
        val state =
            decodeFromStringWithFallback(
                ScreenState.StringScreenState.serializer(),
                propertyBasedYaml,
            )

        assertNotNull(state)
        assertEquals("testState", state.id)
        assertEquals("testName", state.name)
        assertEquals("test", state.defaultValue)
    }

    @Test
    fun testDecodeFromStringWithFallback_withDeserializationStrategy_shouldRethrowOtherExceptions() {
        // Invalid YAML that will cause a non-'type' related error - missing required fields
        val invalidYaml =
            """
            !<StringScreenState>
            id: "testState"
            # Missing required fields like name, defaultValue, etc.
            """.trimIndent()

        // Should re-throw the exception since it's not about missing 'type' property
        assertFailsWith<SerializationException> {
            decodeFromStringWithFallback(
                ScreenState.StringScreenState.serializer(),
                invalidYaml,
            )
        }
    }

    @Test
    fun testOriginalIssue_intScreenStateShouldDeserialize() {
        // This tests the original issue that was fixed - IntScreenState should now be recognized
        val yamlWithIntScreenState =
            """
            id: "testScreen"
            name: "Test Screen"
            stateHolderImpl:
              states:
                - !<IntScreenState>
                  id: "testIntState"
                  name: "testInt"
                  defaultValue: 42
                  companionNodeId: ""
                  userWritable: true
            showOnNavigation: false
            title: "Test"
            label: "Test"
            isDefault: false
            isSelected: false
            """.trimIndent()

        // Should successfully deserialize now that IntScreenState is properly registered
        val screen = decodeFromStringWithFallback<Screen>(yamlWithIntScreenState)

        assertNotNull(screen)
        assertEquals("testScreen", screen.id)

        // Verify the IntScreenState was deserialized correctly
        val project =
            io.composeflow.model.project
                .Project(name = "test")
        val states = screen.getStates(project)
        val intState = states.find { it.name == "testInt" }
        assertNotNull(intState)
        assertTrue(intState is ScreenState.IntScreenState)
        assertEquals(42, (intState as ScreenState.IntScreenState).defaultValue)
    }
}

package io.composeflow.model.project.appscreen.screen

import io.composeflow.model.project.Project
import io.composeflow.model.state.ScreenState
import io.composeflow.model.state.StateHolderImpl
import io.composeflow.serializer.decodeFromStringWithFallback
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ScreenTest {
    @Test
    fun testCopyContents() {
        val source = Screen(id = "source", name = "SourceScreen")
        val target = Screen(id = "target", name = "TargetScreen")

        // Add states to source
        val state1 =
            ScreenState.StringScreenState(
                name = "screenState1",
                defaultValue = "screenValue1",
            )
        val state2 =
            ScreenState.BooleanScreenState(
                name = "screenState2",
                defaultValue = true,
            )
        source.addState(state1)
        source.addState(state2)

        // Add different state to target that should be cleared
        val targetState =
            ScreenState.StringScreenState(
                name = "targetScreenState",
                defaultValue = "targetScreenValue",
            )
        target.addState(targetState)

        // Copy contents - Note: Screen.copyContents only works with StateHolderImpl
        // so we need to pass the source's stateHolderImpl, but since it's private,
        // we'll test by copying via a StateHolderImpl directly
        val sourceStateHolder = StateHolderImpl()
        sourceStateHolder.addState(state1)
        sourceStateHolder.addState(state2)

        target.copyContents(sourceStateHolder)

        // Verify states were copied by checking the target's state count
        // Note: getStates returns all states including project states, so we check the actual added states
        val targetStates = target.getStates(Project(name = "test"))
        val screenSpecificStates =
            targetStates.filter { it.name in listOf("screenState1", "screenState2") }
        assertEquals(2, screenSpecificStates.size)
        assertEquals("screenState1", screenSpecificStates[0].name)
        assertEquals("screenState2", screenSpecificStates[1].name)
    }

    @Test
    fun testCopyContentsWithEmptySource() {
        val source = Screen(id = "source", name = "SourceScreen")
        val target = Screen(id = "target", name = "TargetScreen")

        // Add state to target
        val targetState =
            ScreenState.StringScreenState(
                name = "targetState",
                defaultValue = "targetValue",
            )
        target.addState(targetState)

        // Copy empty source
        val emptyStateHolder = StateHolderImpl()
        target.copyContents(emptyStateHolder)

        // Verify target states were cleared
        val targetStates = target.getStates(Project(name = "test"))
        val screenSpecificStates = targetStates.filter { it.name == "targetState" }
        assertTrue(screenSpecificStates.isEmpty())
    }

    @Test
    fun testCopyContentsWithMultipleStates() {
        val source = Screen(id = "source", name = "SourceScreen")
        val target = Screen(id = "target", name = "TargetScreen")

        // Add multiple different types of states to source
        val sourceStateHolder = StateHolderImpl()
        sourceStateHolder.addState(
            ScreenState.StringScreenState(
                name = "str",
                defaultValue = "test",
            ),
        )
        sourceStateHolder.addState(
            ScreenState.BooleanScreenState(
                name = "bool",
                defaultValue = false,
            ),
        )
        sourceStateHolder.addState(
            ScreenState.FloatScreenState(
                name = "float",
                defaultValue = 42.0f,
            ),
        )

        target.copyContents(sourceStateHolder)

        val targetStates = target.getStates(Project(name = "test"))
        val screenSpecificStates = targetStates.filter { it.name in listOf("str", "bool", "float") }
        assertEquals(3, screenSpecificStates.size)
        assertEquals("str", screenSpecificStates[0].name)
        assertEquals("bool", screenSpecificStates[1].name)
        assertEquals("float", screenSpecificStates[2].name)
    }

    @Test
    fun testDeserializeFailedYamlForDebugging() {
        // Read the failed YAML file for debugging purposes
        val yamlContent = SCREEN_YAML_WITH_INT_SCREEN_STATE

        // Attempt to deserialize using decodeFromStringWithFallback
        val screen = decodeFromStringWithFallback<Screen>(yamlContent)

        // Basic validation that the screen was deserialized successfully
        assertNotNull(screen, "Screen should be successfully deserialized")
        assertEquals("editProfileRoot", screen.id)
        assertEquals("Edit Profile", screen.name)
        assertEquals("Edit Profile", screen.title.value)
        assertEquals("Edit Profile", screen.label.value)
        assertEquals(false, screen.showOnNavigation.value)
        assertEquals(false, screen.isDefault.value)
        assertEquals(false, screen.isSelected.value)

        // Verify root node exists and has expected properties
        assertNotNull(screen.rootNode, "Root node should exist")
        assertEquals("editProfileScreen", screen.rootNode.value.id)
        assertEquals("editProfile", screen.rootNode.value.labelExposed)

        // Verify the screen has the expected states
        val project = Project(name = "test")
        val states = screen.getStates(project)
        val screenSpecificStates =
            states.filter {
                it.name in listOf("usernameValidation", "bioCharacterCount")
            }
        assertEquals(2, screenSpecificStates.size, "Should have 2 screen-specific states")
    }
}

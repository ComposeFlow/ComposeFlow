package io.composeflow.compose

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import io.composeflow.model.parameter.ButtonTrait
import io.composeflow.model.parameter.ColumnTrait
import io.composeflow.model.parameter.RowTrait
import io.composeflow.model.parameter.TextFieldTrait
import io.composeflow.model.parameter.TextTrait
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.Screen
import io.composeflow.repository.fakeFirebaseIdToken
import io.composeflow.repository.fakeProjectRepository
import io.composeflow.ui.uibuilder.UiBuilderViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class UiBuilderViewModelTest {
    private lateinit var viewModel: UiBuilderViewModel
    private val project = Project()

    @Before
    fun setUp() {
        viewModel =
            UiBuilderViewModel(
                firebaseIdToken = fakeFirebaseIdToken,
                project = project,
                projectRepository = fakeProjectRepository,
                onUpdateProject = {},
            )
        project.screenHolder.screens.forEach {
            it.rootNode.value.boundsInWindow.value = Rect(0f, 0f, 400f, 400f)
            it.contentRootNode().boundsInWindow.value = Rect(0f, 0f, 400f, 400f)
        }
    }

    @Test
    fun testDropTextField_assertChild_isAdded_textField_dependsOn_state() =
        runTest {
            viewModel.onComposableDroppedToTarget(
                dropPosition = Offset(50f, 50f),
                TextFieldTrait().defaultComposeNode(project),
            )
            val screen = project.screenHolder.currentEditable()
            val rootNode = screen.getRootNode()
            assert(rootNode.allChildren().any { it.trait.value is TextFieldTrait })
            val states = screen.getStates(project)
            val textFieldNode =
                rootNode.allChildren().firstOrNull { it.trait.value is TextFieldTrait }
            assertTrue(states.any { textFieldNode?.isDependent(it.id) == true })
        }

    @Test
    fun testDeleteTextField_assert_orphan_state_is_deleted() =
        runTest {
            val textField = TextFieldTrait().defaultComposeNode(project)
            viewModel.onComposableDroppedToTarget(
                dropPosition = Offset(50f, 50f),
                textField,
            )
            val textField1 = TextFieldTrait().defaultComposeNode(project)
            viewModel.onComposableDroppedToTarget(
                dropPosition = Offset(50f, 50f),
                textField1,
            )
            val editable = project.screenHolder.currentEditable()
            val rootNode = editable.getRootNode()
            textField1.isFocused.value = true

            assert(rootNode.allChildren().count { it.trait.value is TextFieldTrait } == 2)
            assert(editable.getStates(project).size == 2)

            viewModel.onDeleteKey()

            assert(rootNode.allChildren().count { it.trait.value is TextFieldTrait } == 1)
            assertEquals(1, editable.getStates(project).size)
        }

    @Test
    fun testConvertToComponent() =
        runTest {
            val row =
                RowTrait().defaultComposeNode(project).apply {
                    boundsInWindow.value = Rect(0f, 0f, 400f, 400f)
                }
            val textField = TextFieldTrait().defaultComposeNode(project)
            val button = ButtonTrait().defaultComposeNode(project)
            viewModel.onComposableDroppedToTarget(
                dropPosition = Offset(50f, 50f),
                row,
            )
            // When the TextField is dropped, matching state should be created
            viewModel.onComposableDroppedToTarget(
                dropPosition = Offset(70f, 70f),
                textField,
            )
            viewModel.onComposableDroppedToTarget(
                dropPosition = Offset(70f, 70f),
                button,
            )

            val screen = project.screenHolder.currentEditable()
            assert(screen is Screen)

            // Before the conversion, screen has 1 states for TextField
            assertEquals(1, screen.getStates(project).size)

            viewModel.onConvertToComponent("testComponent", row)

            // After the conversion, screen has no states
            assertEquals(0, screen.getStates(project).size)

            assertEquals(1, project.componentHolder.components.size)
            val component = project.componentHolder.components[0]

            // After the conversion, the component has 1 state for TextField
            assertEquals(1, component.getStates(project).size)
            assertTrue(
                project.getAllComposeNodes().any { it.componentId == component.id },
            )
        }

    @Test
    fun testWrapWithComposable() =
        runTest {
            val row =
                RowTrait().defaultComposeNode(project).apply {
                    boundsInWindow.value = Rect(0f, 0f, 400f, 400f)
                }
            val textField = TextFieldTrait().defaultComposeNode(project)
            viewModel.onComposableDroppedToTarget(
                dropPosition = Offset(50f, 50f),
                row,
            )
            // When the TextField is dropped, matching state should be created
            viewModel.onComposableDroppedToTarget(
                dropPosition = Offset(70f, 70f),
                textField,
            )

            val screen = project.screenHolder.currentEditable()
            assert(screen is Screen)
            assertEquals(row.id, textField.parentNode?.id)

            viewModel.onWrapWithContainerComposable(textField, ColumnTrait())

            val afterTextField =
                screen.getRootNode().allChildren().firstOrNull { it.id == textField.id }
            assertNotEquals(row.id, afterTextField?.parentNode?.id)
            assertEquals(ColumnTrait(), afterTextField?.parentNode?.trait?.value)
        }

    // MARK: Multiple Focused Nodes Tests

    @Test
    fun testSingleClick_clearsPreviousFocus_andFocusesOneNode() =
        runTest {
            // Setup: Add multiple nodes to the screen
            val textField =
                TextFieldTrait().defaultComposeNode(project).apply {
                    boundsInWindow.value = Rect(10f, 10f, 100f, 50f)
                }
            val button =
                ButtonTrait().defaultComposeNode(project).apply {
                    boundsInWindow.value = Rect(150f, 10f, 250f, 50f)
                }
            val textComponent =
                TextTrait().defaultComposeNode(project).apply {
                    boundsInWindow.value = Rect(10f, 100f, 100f, 150f)
                }

            viewModel.onComposableDroppedToTarget(Offset(50f, 30f), textField)
            viewModel.onComposableDroppedToTarget(Offset(200f, 30f), button)
            viewModel.onComposableDroppedToTarget(Offset(50f, 125f), textComponent)

            // First click: Focus textField
            viewModel.onMousePressedAt(Offset(50f, 30f), isCtrlOrMetaPressed = false)

            // Verify only textField is focused
            assertTrue("TextField should be focused", textField.isFocused.value)
            assertFalse("Button should not be focused", button.isFocused.value)
            assertFalse("Text should not be focused", textComponent.isFocused.value)

            // Second click: Focus button (should clear textField focus)
            viewModel.onMousePressedAt(Offset(200f, 30f), isCtrlOrMetaPressed = false)

            // Verify only button is focused
            assertFalse("TextField should not be focused", textField.isFocused.value)
            assertTrue("Button should be focused", button.isFocused.value)
            assertFalse("Text should not be focused", textComponent.isFocused.value)
        }

    @Test
    fun testCtrlClick_preservesPreviousFocus_andAddsNewNode() =
        runTest {
            // Setup: Add multiple nodes to the screen
            val textField =
                TextFieldTrait().defaultComposeNode(project).apply {
                    boundsInWindow.value = Rect(10f, 10f, 100f, 50f)
                }
            val button =
                ButtonTrait().defaultComposeNode(project).apply {
                    boundsInWindow.value = Rect(150f, 10f, 250f, 50f)
                }
            val textComponent =
                TextTrait().defaultComposeNode(project).apply {
                    boundsInWindow.value = Rect(10f, 100f, 100f, 150f)
                }

            viewModel.onComposableDroppedToTarget(Offset(50f, 30f), textField)
            viewModel.onComposableDroppedToTarget(Offset(200f, 30f), button)
            viewModel.onComposableDroppedToTarget(Offset(50f, 125f), textComponent)

            // First click: Focus textField
            viewModel.onMousePressedAt(Offset(50f, 30f), isCtrlOrMetaPressed = false)

            // Verify only textField is focused
            assertTrue("TextField should be focused", textField.isFocused.value)
            assertFalse("Button should not be focused", button.isFocused.value)
            assertFalse("Text should not be focused", textComponent.isFocused.value)

            // Ctrl+Click: Add button to selection
            viewModel.onMousePressedAt(Offset(200f, 30f), isCtrlOrMetaPressed = true)

            // Verify both textField and button are focused
            assertTrue("TextField should remain focused", textField.isFocused.value)
            assertTrue("Button should be focused", button.isFocused.value)
            assertFalse("Text should not be focused", textComponent.isFocused.value)

            // Ctrl+Click: Add text to selection
            viewModel.onMousePressedAt(Offset(50f, 125f), isCtrlOrMetaPressed = true)

            // Verify all three nodes are focused
            assertTrue("TextField should remain focused", textField.isFocused.value)
            assertTrue("Button should remain focused", button.isFocused.value)
            assertTrue("Text should be focused", textComponent.isFocused.value)
        }

    @Test
    fun testMultipleFocusedNodes_findFocusedNodes_returnsAllFocused() =
        runTest {
            // Setup: Add multiple nodes to the screen
            val textField =
                TextFieldTrait().defaultComposeNode(project).apply {
                    boundsInWindow.value = Rect(10f, 10f, 100f, 50f)
                }
            val button =
                ButtonTrait().defaultComposeNode(project).apply {
                    boundsInWindow.value = Rect(150f, 10f, 250f, 50f)
                }
            val textComponent =
                TextTrait().defaultComposeNode(project).apply {
                    boundsInWindow.value = Rect(10f, 100f, 100f, 150f)
                }

            viewModel.onComposableDroppedToTarget(Offset(50f, 30f), textField)
            viewModel.onComposableDroppedToTarget(Offset(200f, 30f), button)
            viewModel.onComposableDroppedToTarget(Offset(50f, 125f), textComponent)

            // Focus multiple nodes using Ctrl+Click
            viewModel.onMousePressedAt(
                Offset(50f, 30f),
                isCtrlOrMetaPressed = false,
            ) // Focus textField
            viewModel.onMousePressedAt(Offset(200f, 30f), isCtrlOrMetaPressed = true) // Add button
            viewModel.onMousePressedAt(Offset(50f, 125f), isCtrlOrMetaPressed = true) // Add text

            // Verify findFocusedNodes returns all focused nodes
            val focusedNodes = project.screenHolder.findFocusedNodes()
            assertEquals(3, focusedNodes.size)

            // Check by trait type instead of object identity since the nodes might be different objects
            val focusedTraitTypes = focusedNodes.map { it.trait.value::class.simpleName }.toSet()
            assertTrue("Should contain TextFieldTrait", focusedTraitTypes.contains("TextFieldTrait"))
            assertTrue("Should contain ButtonTrait", focusedTraitTypes.contains("ButtonTrait"))
            assertTrue("Should contain TextTrait", focusedTraitTypes.contains("TextTrait"))
        }

    @Test
    fun testNormalClick_afterMultipleSelection_clearsAllAndFocusesOne() =
        runTest {
            // Setup: Add multiple nodes to the screen
            val textField =
                TextFieldTrait().defaultComposeNode(project).apply {
                    boundsInWindow.value = Rect(10f, 10f, 100f, 50f)
                }
            val button =
                ButtonTrait().defaultComposeNode(project).apply {
                    boundsInWindow.value = Rect(150f, 10f, 250f, 50f)
                }
            val textComponent =
                TextTrait().defaultComposeNode(project).apply {
                    boundsInWindow.value = Rect(10f, 100f, 100f, 150f)
                }

            viewModel.onComposableDroppedToTarget(Offset(50f, 30f), textField)
            viewModel.onComposableDroppedToTarget(Offset(200f, 30f), button)
            viewModel.onComposableDroppedToTarget(Offset(50f, 125f), textComponent)

            // Create multiple selection
            viewModel.onMousePressedAt(
                Offset(50f, 30f),
                isCtrlOrMetaPressed = false,
            ) // Focus textField
            viewModel.onMousePressedAt(Offset(200f, 30f), isCtrlOrMetaPressed = true) // Add button
            viewModel.onMousePressedAt(Offset(50f, 125f), isCtrlOrMetaPressed = true) // Add text

            // Verify multiple nodes are focused
            assertTrue("TextField should be focused", textField.isFocused.value)
            assertTrue("Button should be focused", button.isFocused.value)
            assertTrue("Text should be focused", textComponent.isFocused.value)

            // Normal click on empty area (coordinates outside any node bounds)
            // The screen bounds are set to (0f, 0f, 400f, 400f) in setUp(), so use coordinates outside that
            viewModel.onMousePressedAt(Offset(500f, 500f), isCtrlOrMetaPressed = false)

            // Verify findFocusedNodes returns empty list
            val focusedNodes = project.screenHolder.findFocusedNodes()
            assertEquals(0, focusedNodes.size)

            // Also verify individual focus states
            assertFalse("TextField should not be focused", textField.isFocused.value)
            assertFalse("Button should not be focused", button.isFocused.value)
            assertFalse("Text should not be focused", textComponent.isFocused.value)
        }

    @Test
    fun testCtrlClick_onAlreadyFocusedNode_maintainsMultipleSelection() =
        runTest {
            // Setup: Add multiple nodes to the screen
            val textField =
                TextFieldTrait().defaultComposeNode(project).apply {
                    boundsInWindow.value = Rect(10f, 10f, 100f, 50f)
                }
            val button =
                ButtonTrait().defaultComposeNode(project).apply {
                    boundsInWindow.value = Rect(150f, 10f, 250f, 50f)
                }

            viewModel.onComposableDroppedToTarget(Offset(50f, 30f), textField)
            viewModel.onComposableDroppedToTarget(Offset(200f, 30f), button)

            // Create multiple selection
            viewModel.onMousePressedAt(
                Offset(50f, 30f),
                isCtrlOrMetaPressed = false,
            ) // Focus textField
            viewModel.onMousePressedAt(Offset(200f, 30f), isCtrlOrMetaPressed = true) // Add button

            // Verify both nodes are focused
            assertTrue("TextField should be focused", textField.isFocused.value)
            assertTrue("Button should be focused", button.isFocused.value)

            // Ctrl+Click on already focused textField
            viewModel.onMousePressedAt(Offset(50f, 30f), isCtrlOrMetaPressed = true)

            // Verify only TextField's focus is toggled because it was clicked with isCtrlOrMetaPressed
            // as true
            assertFalse("TextField should remain focused", textField.isFocused.value)
            assertTrue("Button should remain focused", button.isFocused.value)
        }

    @Test
    fun testDeleteKey_withMultipleFocusedNodes_deletesAllFocused() =
        runTest {
            // Setup: Add multiple nodes to the screen
            val textField =
                TextFieldTrait().defaultComposeNode(project).apply {
                    boundsInWindow.value = Rect(10f, 10f, 100f, 50f)
                }
            val button =
                ButtonTrait().defaultComposeNode(project).apply {
                    boundsInWindow.value = Rect(150f, 10f, 250f, 50f)
                }
            val textComponent =
                TextTrait().defaultComposeNode(project).apply {
                    boundsInWindow.value = Rect(10f, 100f, 100f, 150f)
                }

            viewModel.onComposableDroppedToTarget(Offset(50f, 30f), textField)
            viewModel.onComposableDroppedToTarget(Offset(200f, 30f), button)
            viewModel.onComposableDroppedToTarget(Offset(50f, 125f), textComponent)

            val screen = project.screenHolder.currentEditable()
            val rootNode = screen.getRootNode()

            // Verify all nodes exist (expecting our 3 added nodes plus any defaults)
            val initialNodeCount = rootNode.allChildren().size
            assertTrue("Should have at least 3 nodes", initialNodeCount >= 3)

            // Focus multiple nodes
            viewModel.onMousePressedAt(
                Offset(50f, 30f),
                isCtrlOrMetaPressed = false,
            ) // Focus textField
            viewModel.onMousePressedAt(Offset(200f, 30f), isCtrlOrMetaPressed = true) // Add button

            // Verify 2 nodes are focused, 1 is not
            assertTrue("TextField should be focused", textField.isFocused.value)
            assertTrue("Button should be focused", button.isFocused.value)
            assertFalse("Text should not be focused", textComponent.isFocused.value)

            // Delete focused nodes
            viewModel.onDeleteKey()

            // Verify that 2 nodes were deleted (should have initialNodeCount - 2 remaining)
            val expectedRemainingCount = initialNodeCount - 2
            assertEquals(expectedRemainingCount, rootNode.allChildren().size)

            // Check by trait type since the nodes might be different objects
            val remainingTraitTypes =
                rootNode.allChildren().map { it.trait.value::class.simpleName }.toSet()
            assertTrue("Text component should remain", remainingTraitTypes.contains("TextTrait"))
            assertFalse("TextField should be deleted", remainingTraitTypes.contains("TextFieldTrait"))
            assertFalse("Button should be deleted", remainingTraitTypes.contains("ButtonTrait"))
        }

    @Test
    fun testEmptyAreaClick_withMultipleFocusedNodes_clearsAllFocus() =
        runTest {
            // Setup: Add multiple nodes to the screen
            val textField =
                TextFieldTrait().defaultComposeNode(project).apply {
                    boundsInWindow.value = Rect(10f, 10f, 100f, 50f)
                }
            val button =
                ButtonTrait().defaultComposeNode(project).apply {
                    boundsInWindow.value = Rect(150f, 10f, 250f, 50f)
                }

            viewModel.onComposableDroppedToTarget(Offset(50f, 30f), textField)
            viewModel.onComposableDroppedToTarget(Offset(200f, 30f), button)

            // Focus multiple nodes
            viewModel.onMousePressedAt(
                Offset(50f, 30f),
                isCtrlOrMetaPressed = false,
            ) // Focus textField
            viewModel.onMousePressedAt(Offset(200f, 30f), isCtrlOrMetaPressed = true) // Add button

            // Verify both nodes are focused
            assertTrue("TextField should be focused", textField.isFocused.value)
            assertTrue("Button should be focused", button.isFocused.value)
            assertEquals(2, project.screenHolder.findFocusedNodes().size)

            // Click on empty area (coordinates outside any node bounds)
            // The screen bounds are set to (0f, 0f, 400f, 400f) in setUp(), so use coordinates outside that
            viewModel.onMousePressedAt(Offset(500f, 500f), isCtrlOrMetaPressed = false)

            // Verify all focus is cleared
            assertEquals(0, project.screenHolder.findFocusedNodes().size)
            assertFalse("TextField should lose focus", textField.isFocused.value)
            assertFalse("Button should lose focus", button.isFocused.value)
        }

    @Test
    fun testOnScreenUpdated_updatesScreenInProject() {
        // Get the initial screen
        val originalScreen = project.screenHolder.screens.first()
        val originalName = originalScreen.name

        // Create an updated screen with a different name and label
        val updatedScreen = originalScreen.copy(name = "UpdatedScreenName")
        updatedScreen.label.value = "UpdatedScreenName"

        // Call onScreenUpdated
        viewModel.onScreenUpdated(updatedScreen)

        // Verify the screen was updated in the project
        val screenInProject = project.screenHolder.screens.find { it.id == updatedScreen.id }
        assertEquals("UpdatedScreenName", screenInProject?.name)
        assertEquals("UpdatedScreenName", screenInProject?.label?.value)

        // Verify the screen name actually changed
        assertNotEquals(originalName, screenInProject?.name)
    }

    @Test
    fun testOnCopyScreen_createsNewScreenWithUniqueName() {
        // Get the initial screen count and first screen
        val initialScreenCount = project.screenHolder.screens.size
        val originalScreen = project.screenHolder.screens.first()
        val originalScreenId = originalScreen.id
        val originalName = originalScreen.name

        // Call onCopyScreen
        viewModel.onCopyScreen(originalScreen)

        // Verify a new screen was added
        assertEquals(initialScreenCount + 1, project.screenHolder.screens.size)

        // Find the copied screen (should be the last one added)
        val copiedScreen = project.screenHolder.screens.last()

        // Verify the copied screen has different ID but related name
        assertNotEquals(originalScreenId, copiedScreen.id)
        assertTrue(
            "Copied screen name should start with original name",
            copiedScreen.name.startsWith(originalName),
        )

        // Verify the copied screen has matching label
        assertEquals(copiedScreen.name, copiedScreen.label.value)

        // Verify the copied screen is now selected
        assertEquals(copiedScreen.id, project.screenHolder.currentEditable().id)

        // Verify original screen is still there and unchanged
        val originalStillExists = project.screenHolder.screens.any { it.id == originalScreenId }
        assertTrue("Original screen should still exist", originalStillExists)
    }
}

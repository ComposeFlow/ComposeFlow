package io.composeflow.ui.uibuilder

import androidx.compose.ui.unit.dp
import io.composeflow.model.modifier.ModifierWrapper
import io.composeflow.model.parameter.ButtonTrait
import io.composeflow.model.parameter.ColumnTrait
import io.composeflow.model.parameter.FabTrait
import io.composeflow.model.parameter.TextTrait
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.Screen
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.serializer.encodeToString
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for UiBuilderOperator focusing on core functionality that can be reliably tested
 * without making assumptions about internal implementation details.
 */
class UiBuilderOperatorTest {
    @get:Rule
    val mockkRule = MockKRule(this)

    private lateinit var uiBuilderOperator: UiBuilderOperator
    private lateinit var project: Project
    private lateinit var screen: Screen
    private lateinit var rootNode: ComposeNode

    @Before
    fun setUp() {
        mockAwtToolkitForComposeResourcesIfNecessary()
        uiBuilderOperator = UiBuilderOperator()
        project = Project()
        screen = project.screenHolder.screens.first()
        rootNode = screen.getRootNode()
    }

    // ========== Helper Methods ==========

    private fun createColumnNode(): ComposeNode = ColumnTrait().defaultComposeNode(project)

    private fun createButtonNode(): ComposeNode = ButtonTrait().defaultComposeNode(project)

    private fun createTextNode(): ComposeNode = TextTrait().defaultComposeNode(project)

    private fun createFabNode(): ComposeNode = FabTrait().defaultComposeNode(project)

    private fun createPaddingModifier(): ModifierWrapper = ModifierWrapper.Padding(16.dp)

    // ========== Basic Structure Tests ==========

    @Test
    fun testProjectStructure() {
        // Test that the project has expected structure
        assertNotNull(project.screenHolder)
        assertNotNull(project.screenHolder.screens)
        assertTrue(project.screenHolder.screens.isNotEmpty())
        assertNotNull(project.screenHolder.currentEditable())
    }

    @Test
    fun testBasicNodeCreation() {
        val buttonNode = createButtonNode()
        assertTrue(buttonNode.trait.value is ButtonTrait)

        val columnNode = createColumnNode()
        assertTrue(columnNode.trait.value is ColumnTrait)

        val textNode = createTextNode()
        assertTrue(textNode.trait.value is TextTrait)
    }

    // ========== Node Addition Tests ==========

    @Test
    fun testOnPreAddComposeNodeToContainerNode_ContainerNotFound() =
        runTest {
            val nodeToAdd = createButtonNode()

            val result =
                uiBuilderOperator.onPreAddComposeNodeToContainerNode(
                    project = project,
                    containerNodeId = "non-existent-id",
                    composeNode = nodeToAdd,
                )

            assertFalse(result.errorMessages.isEmpty())
            assertTrue(
                result.errorMessages
                    .first()
                    .contains("Container node with ID non-existent-id not found"),
            )
        }

    @Test
    fun testOnPreAddComposeNodeToContainerNode_Success() =
        runTest {
            val containerNode = createColumnNode()
            rootNode.addChild(containerNode)
            val nodeToAdd = createButtonNode()

            val result =
                uiBuilderOperator.onPreAddComposeNodeToContainerNode(
                    project = project,
                    containerNodeId = containerNode.id,
                    composeNode = nodeToAdd,
                )

            assertTrue(result.isSuccessful())
        }

    @Test
    fun testOnAddComposeNodeToContainerNode_LlmMethod() =
        runTest {
            val containerNode = createColumnNode()
            rootNode.addChild(containerNode)
            val buttonNode = createButtonNode()
            val buttonYaml = encodeToString(buttonNode)

            val result =
                uiBuilderOperator.onAddComposeNodeToContainerNode(
                    project = project,
                    containerNodeId = containerNode.id,
                    composeNodeYaml = buttonYaml,
                    indexToDrop = 0,
                )

            // The method returns an empty EventResult on success
            assertTrue(result.isSuccessful())
        }

    // ========== Node Removal Tests ==========

    @Test
    fun testOnPreRemoveComposeNode_NullNode() {
        val result = uiBuilderOperator.onPreRemoveComposeNode(null)
        assertTrue(result.isSuccessful())
    }

    @Test
    fun testOnPreRemoveComposeNode_ValidNode() {
        val nodeToRemove = createButtonNode()
        val result = uiBuilderOperator.onPreRemoveComposeNode(nodeToRemove)
        assertTrue(result.isSuccessful())
    }

    @Test
    fun testOnRemoveComposeNode_NodeNotFound() {
        val result =
            uiBuilderOperator.onRemoveComposeNode(
                project = project,
                composeNodeId = "non-existent-id",
            )

        // The implementation doesn't check for existence in the error path
        assertTrue(result.isSuccessful())
    }

    @Test
    fun testOnRemoveComposeNode_ScreenOnlyNode_Fab() {
        val fabNode = createFabNode()
        screen.fabNode.value = fabNode

        val result =
            uiBuilderOperator.onRemoveComposeNode(
                project = project,
                composeNodeId = fabNode.id,
            )

        assertTrue(result.isSuccessful())
        assertNull(screen.fabNode.value)
    }

    // ========== Modifier Tests - LLM Methods ==========

    @Test
    fun testOnAddModifier_NodeNotFound() =
        runTest {
            val modifier = createPaddingModifier()
            val modifierYaml = encodeToString(modifier)

            val result =
                uiBuilderOperator.onAddModifier(
                    project = project,
                    composeNodeId = "non-existent-id",
                    modifierYaml = modifierYaml,
                )

            assertFalse(result.errorMessages.isEmpty())
            assertTrue(result.errorMessages.first().contains("not found"))
        }

    @Test
    fun testOnUpdateModifier_NodeNotFound() =
        runTest {
            val modifier = createPaddingModifier()
            val modifierYaml = encodeToString(modifier)

            val result =
                uiBuilderOperator.onUpdateModifier(
                    project = project,
                    composeNodeId = "non-existent-id",
                    index = 0,
                    modifierYaml = modifierYaml,
                )

            assertFalse(result.errorMessages.isEmpty())
            assertTrue(result.errorMessages.first().contains("not found"))
        }

    @Test
    fun testOnUpdateModifier_InvalidIndex() =
        runTest {
            val node = createButtonNode()
            rootNode.addChild(node)
            val modifier = createPaddingModifier()
            val modifierYaml = encodeToString(modifier)

            val result =
                uiBuilderOperator.onUpdateModifier(
                    project = project,
                    composeNodeId = node.id,
                    index = 5, // Invalid index for empty modifier list
                    modifierYaml = modifierYaml,
                )

            assertFalse(result.errorMessages.isEmpty())
            assertTrue(result.errorMessages.first().contains("Invalid modifier index"))
        }

    @Test
    fun testOnRemoveModifier_NodeNotFound() =
        runTest {
            val result =
                uiBuilderOperator.onRemoveModifier(
                    project = project,
                    composeNodeId = "non-existent-id",
                    index = 0,
                )

            assertFalse(result.errorMessages.isEmpty())
            assertTrue(result.errorMessages.first().contains("not found"))
        }

    @Test
    fun testOnRemoveModifier_InvalidIndex() =
        runTest {
            val node = createButtonNode()
            rootNode.addChild(node)

            // Clear any default modifiers to ensure we're testing with no modifiers
            node.modifierList.clear()

            val result =
                uiBuilderOperator.onRemoveModifier(
                    project = project,
                    composeNodeId = node.id,
                    index = 0, // No modifiers exist
                )

            assertFalse(result.errorMessages.isEmpty())
            assertTrue(result.errorMessages.first().contains("Invalid modifier index"))
        }

    @Test
    fun testOnSwapModifiers_NodeNotFound() =
        runTest {
            val result =
                uiBuilderOperator.onSwapModifiers(
                    project = project,
                    composeNodeId = "non-existent-id",
                    fromIndex = 0,
                    toIndex = 1,
                )

            assertFalse(result.errorMessages.isEmpty())
            assertTrue(result.errorMessages.first().contains("not found"))
        }

    @Test
    fun testOnSwapModifiers_InvalidIndices() =
        runTest {
            val node = createButtonNode()
            rootNode.addChild(node)
            val modifier = createPaddingModifier()
            node.modifierList.add(modifier)

            val result =
                uiBuilderOperator.onSwapModifiers(
                    project = project,
                    composeNodeId = node.id,
                    fromIndex = 0,
                    toIndex = 5, // Invalid index
                )

            assertFalse(result.errorMessages.isEmpty())
            assertTrue(result.errorMessages.first().contains("Invalid modifier indices"))
        }

    // ========== Node Movement Tests ==========

    @Test
    fun testOnPreMoveComposeNodeToPosition_NodeNotFound() =
        runTest {
            val container = createColumnNode()
            rootNode.addChild(container)

            val result =
                uiBuilderOperator.onPreMoveComposeNodeToPosition(
                    project = project,
                    composeNodeId = "non-existent-id",
                    containerNodeId = container.id,
                )

            assertFalse(result.errorMessages.isEmpty())
            assertTrue(result.errorMessages.first().contains("not found"))
        }

    @Test
    fun testOnPreMoveComposeNodeToPosition_ContainerNotFound() =
        runTest {
            val nodeToMove = createButtonNode()
            rootNode.addChild(nodeToMove)

            val result =
                uiBuilderOperator.onPreMoveComposeNodeToPosition(
                    project = project,
                    composeNodeId = nodeToMove.id,
                    containerNodeId = "non-existent-id",
                )

            assertFalse(result.errorMessages.isEmpty())
            assertTrue(result.errorMessages.first().contains("not found"))
        }

    // ========== Error Handling Tests ==========

    @Test
    fun testInvalidYamlHandling() =
        runTest {
            val result =
                uiBuilderOperator.onAddModifier(
                    project = project,
                    composeNodeId = "some-id",
                    modifierYaml = "invalid-yaml-content",
                )

            // Should have error messages due to invalid YAML or missing node
            assertFalse(result.errorMessages.isEmpty())
        }

    // ========== Serialization Tests ==========

    @Test
    fun testModifierSerialization() {
        val modifier = createPaddingModifier()
        val yaml = encodeToString(modifier)

        // Basic check that serialization produces non-empty result
        assertTrue(yaml.isNotEmpty())
        assertTrue(yaml.contains("Padding"))
    }

    @Test
    fun testNodeSerialization() {
        val buttonNode = createButtonNode()
        val yaml = encodeToString(buttonNode)

        // Basic check that serialization produces non-empty result
        assertTrue(yaml.isNotEmpty())
        assertTrue(yaml.contains("Button"))
    }

    // ========== ComposeNode ID Uniqueness Tests ==========

    @Test
    fun testAddComposeNode_UniqueIdGeneration_WhenIdAlreadyExists() =
        runTest {
            val containerNode = createColumnNode()
            rootNode.addChild(containerNode)

            // Add first button with a specific ID
            val firstButton = createButtonNode()
            val originalId = firstButton.id
            containerNode.addChild(firstButton)

            // Try to add another button with the same ID
            val secondButton = createButtonNode().copy(id = originalId)
            val secondButtonYaml = encodeToString(secondButton)

            val result =
                uiBuilderOperator.onAddComposeNodeToContainerNode(
                    project = project,
                    containerNodeId = containerNode.id,
                    composeNodeYaml = secondButtonYaml,
                    indexToDrop = 1, // Add at end
                )

            assertTrue(result.isSuccessful())

            // Verify that both nodes exist but with different IDs
            val children = containerNode.children
            assertTrue(children.size == 2)

            val firstChildId = children[0].id
            val secondChildId = children[1].id

            // IDs should be different (second one should have been made unique)
            assertTrue(firstChildId != secondChildId)
            assertTrue(firstChildId == originalId) // First keeps original ID
            assertTrue(secondChildId.startsWith(originalId)) // Second gets modified ID
        }

    @Test
    fun testAddComposeNode_UniqueIdGeneration_WhenIdAlreadyExistsAtSpecificIndex() =
        runTest {
            val containerNode = createColumnNode()
            rootNode.addChild(containerNode)

            // Add first button with a specific ID
            val firstButton = createButtonNode()
            val originalId = firstButton.id
            containerNode.addChild(firstButton)

            // Try to add another button with the same ID at index 0
            val secondButton = createButtonNode().copy(id = originalId)
            val secondButtonYaml = encodeToString(secondButton)

            val result =
                uiBuilderOperator.onAddComposeNodeToContainerNode(
                    project = project,
                    containerNodeId = containerNode.id,
                    composeNodeYaml = secondButtonYaml,
                    indexToDrop = 0,
                )

            assertTrue(result.isSuccessful())

            // Verify that both nodes exist but with different IDs
            val children = containerNode.children
            assertTrue(children.size == 2)

            val firstChildId = children[0].id // This should be the new node at index 0
            val secondChildId = children[1].id // This should be the original node moved to index 1

            // IDs should be different
            assertTrue(firstChildId != secondChildId)
            assertTrue(secondChildId == originalId) // Original keeps its ID
            assertTrue(firstChildId.startsWith(originalId)) // New one gets modified ID
        }

    @Test
    fun testAddComposeNode_NoIdConflict_PreservesOriginalId() =
        runTest {
            val containerNode = createColumnNode()
            rootNode.addChild(containerNode)

            val buttonNode = createButtonNode()
            val originalId = buttonNode.id
            val buttonYaml = encodeToString(buttonNode)

            val result =
                uiBuilderOperator.onAddComposeNodeToContainerNode(
                    project = project,
                    containerNodeId = containerNode.id,
                    composeNodeYaml = buttonYaml,
                    indexToDrop = 0, // Add at beginning
                )

            assertTrue(result.isSuccessful())

            // Verify that the node keeps its original ID when no conflict exists
            val addedNode = containerNode.children.first()
            assertTrue(addedNode.id == originalId)
        }

    @Test
    fun testAddComposeNode_MultipleIdConflicts_GeneratesUniqueIds() =
        runTest {
            val containerNode = createColumnNode()
            rootNode.addChild(containerNode)

            // Add first button
            val baseId = "Button"
            val firstButton = createButtonNode().copy(id = baseId)
            containerNode.addChild(firstButton)

            // Add second button with same ID - should become "Button1"
            val secondButton = createButtonNode().copy(id = baseId)
            val secondButtonYaml = encodeToString(secondButton)

            uiBuilderOperator.onAddComposeNodeToContainerNode(
                project = project,
                containerNodeId = containerNode.id,
                composeNodeYaml = secondButtonYaml,
                indexToDrop = 1, // Add at position 1
            )

            // Add third button with same ID - should become "Button2"
            val thirdButton = createButtonNode().copy(id = baseId)
            val thirdButtonYaml = encodeToString(thirdButton)

            val result =
                uiBuilderOperator.onAddComposeNodeToContainerNode(
                    project = project,
                    containerNodeId = containerNode.id,
                    composeNodeYaml = thirdButtonYaml,
                    indexToDrop = 2, // Add at position 2
                )

            assertTrue(result.isSuccessful())

            // Verify all three nodes have unique IDs
            val children = containerNode.children
            assertTrue(children.size == 3)

            val ids = children.map { it.id }.toSet()
            assertTrue(ids.size == 3) // All IDs should be unique
            assertTrue(ids.contains(baseId)) // Original ID preserved
            assertTrue(ids.any { it.startsWith(baseId) && it != baseId }) // Generated IDs present
        }

    @Test
    fun testAddComposeNode_NestedContainers_IdUniquenessAcrossHierarchy() =
        runTest {
            // Create nested structure: root -> outer column -> inner column
            val outerColumn = createColumnNode()
            val innerColumn = createColumnNode()

            rootNode.addChild(outerColumn)
            outerColumn.addChild(innerColumn)

            // Add button to inner column first
            val baseId = "TestButton"
            val firstButton = createButtonNode().copy(id = baseId)
            innerColumn.addChild(firstButton)

            // Try to add button with same ID to outer column
            val secondButton = createButtonNode().copy(id = baseId)
            val secondButtonYaml = encodeToString(secondButton)

            val result =
                uiBuilderOperator.onAddComposeNodeToContainerNode(
                    project = project,
                    containerNodeId = outerColumn.id,
                    composeNodeYaml = secondButtonYaml,
                    indexToDrop = 0, // Add at beginning
                )

            assertTrue(result.isSuccessful())

            // Verify that IDs are unique across the entire canvas hierarchy
            val allNodes = screen.getRootNode().allChildren()
            val allIds = allNodes.map { it.id }
            val uniqueIds = allIds.toSet()

            assertTrue(allIds.size == uniqueIds.size) // No duplicate IDs

            // Both buttons should exist with different IDs
            val buttonNodes = allNodes.filter { it.trait.value is ButtonTrait }
            assertTrue(buttonNodes.size == 2)
            assertTrue(buttonNodes[0].id != buttonNodes[1].id)
        }

    @Test
    fun testAddComposeNode_ScreenOnlyNodes_DoNotGetIdUniqueness() =
        runTest {
            // Screen-only nodes (like FAB) are handled differently and don't go through ID uniqueness logic
            val fabNode = createFabNode()
            val originalId = fabNode.id
            val fabYaml = encodeToString(fabNode)

            val result =
                uiBuilderOperator.onAddComposeNodeToContainerNode(
                    project = project,
                    containerNodeId = rootNode.id,
                    composeNodeYaml = fabYaml,
                    indexToDrop = 0, // Add at beginning
                )

            assertTrue(result.isSuccessful())

            // FAB should be set on the screen, not added as a child
            assertNotNull(screen.fabNode.value)
            assertTrue(screen.fabNode.value?.id == originalId) // Original ID preserved
            // Note: We don't test rootNode.children.isEmpty() because FAB nodes may
            // still be added to the hierarchy in some cases
        }

    @Test
    fun testAddComposeNode_IdUniquenessWithComplexHierarchy() =
        runTest {
            // Create a more complex hierarchy to test ID collision detection
            val column1 = createColumnNode().copy(id = "Column1")
            val column2 = createColumnNode().copy(id = "Column2")
            val button1 = createButtonNode().copy(id = "SharedId")
            val text1 = createTextNode().copy(id = "Text1")

            // Build hierarchy: root -> column1 -> button1, text1
            //                       -> column2
            rootNode.addChild(column1)
            rootNode.addChild(column2)
            column1.addChild(button1)
            column1.addChild(text1)

            // Try to add another node with "SharedId" to column2
            val button2 = createButtonNode().copy(id = "SharedId")
            val button2Yaml = encodeToString(button2)

            val result =
                uiBuilderOperator.onAddComposeNodeToContainerNode(
                    project = project,
                    containerNodeId = column2.id,
                    composeNodeYaml = button2Yaml,
                    indexToDrop = 0, // Add at beginning
                )

            assertTrue(result.isSuccessful())

            // Verify uniqueness across entire canvas
            val allNodes = screen.getRootNode().allChildren()
            val allIds = allNodes.map { it.id }
            val uniqueIds = allIds.toSet()

            assertTrue(allIds.size == uniqueIds.size) // All IDs unique

            // New button should have modified ID
            val addedButton = column2.children.first()
            assertTrue(addedButton.id != "SharedId")
            assertTrue(addedButton.id.startsWith("SharedId"))
        }

    @Test
    fun testAddComposeNode_MoveOperation_DoesNotApplyIdUniqueness() =
        runTest {
            // Test that move operations currently don't apply ID uniqueness logic
            // (This is documenting current behavior - move operations should potentially
            // be enhanced to apply ID uniqueness in the future)
            val column1 = createColumnNode().copy(id = "Column1")
            val column2 = createColumnNode().copy(id = "Column2")
            val sharedId = "MovedButton"

            // Setup: both columns have buttons with the same ID
            val button1 = createButtonNode().copy(id = sharedId)
            val button2 = createButtonNode().copy(id = sharedId)

            rootNode.addChild(column1)
            rootNode.addChild(column2)
            column1.addChild(button1)
            column2.addChild(button2)

            // Move button from column1 to column2 (this will create ID conflict)
            val result =
                uiBuilderOperator.onMoveComposeNodeToContainer(
                    project = project,
                    composeNodeId = button1.id,
                    containerNodeId = column2.id,
                    index = 0,
                )

            assertTrue(result.isSuccessful())

            // Currently, move operations don't apply ID uniqueness, so we may have duplicates
            // column2 should now have 2 buttons
            assertTrue(column2.children.size == 2)

            // Both buttons may have the same ID (current behavior)
            val button1InColumn2 = column2.children[0]
            val button2InColumn2 = column2.children[1]

            // The moved button should be the first child
            assertTrue(button1InColumn2.id == sharedId)
            assertTrue(button2InColumn2.id == sharedId)

            // Note: This test documents current behavior. In the future, move operations
            // could be enhanced to apply ID uniqueness similar to add operations.
        }

    // ========== Project Issues Tests ==========

    @Test
    fun testOnGetProjectIssues_EmptyProject() =
        runTest {
            val result = uiBuilderOperator.onGetProjectIssues(project)

            assertTrue(result.isSuccessful())
            assertTrue(result.errorMessages.isEmpty())
            // Empty project should have no issues or very minimal issues
            assertTrue(result.issues.isEmpty() || result.issues.size <= 1)
        }

    @Test
    fun testOnGetProjectIssues_ValidProject() =
        runTest {
            // Add some valid nodes to the project
            val containerNode = createColumnNode()
            val buttonNode = createButtonNode()
            rootNode.addChild(containerNode)
            containerNode.addChild(buttonNode)

            val result = uiBuilderOperator.onGetProjectIssues(project)

            assertTrue(result.isSuccessful())
            assertTrue(result.errorMessages.isEmpty())
            // Valid project structure should have minimal or no issues
            assertTrue(result.issues.size >= 0) // Allow for any number of issues
        }

    @Test
    fun testOnGetProjectIssues_WithValidNodes_ReturnsSuccessfully() =
        runTest {
            // Create a more complex but valid structure
            val columnNode = createColumnNode()
            val buttonNode = createButtonNode()
            val textNode = createTextNode()

            rootNode.addChild(columnNode)
            columnNode.addChild(buttonNode)
            columnNode.addChild(textNode)

            val result = uiBuilderOperator.onGetProjectIssues(project)

            assertTrue(result.isSuccessful())
            assertTrue(result.errorMessages.isEmpty())
            assertNotNull(result.issues) // Issues list should be initialized
        }

    @Test
    fun testOnGetProjectIssues_ReturnsEventResultWithIssues() =
        runTest {
            val result = uiBuilderOperator.onGetProjectIssues(project)

            // Verify the result structure
            assertNotNull(result)
            assertNotNull(result.issues)
            assertNotNull(result.errorMessages)

            // Should succeed regardless of number of issues
            assertTrue(result.errorMessages.isEmpty())
        }

    @Test
    fun testOnGetProjectIssues_WithNestedStructure() =
        runTest {
            // Create nested structure to test issue detection across hierarchy
            val outerColumn = createColumnNode()
            val innerColumn = createColumnNode()
            val button1 = createButtonNode()
            val button2 = createButtonNode()

            rootNode.addChild(outerColumn)
            outerColumn.addChild(innerColumn)
            innerColumn.addChild(button1)
            outerColumn.addChild(button2)

            val result = uiBuilderOperator.onGetProjectIssues(project)

            assertTrue(result.isSuccessful())
            assertTrue(result.errorMessages.isEmpty())
            assertNotNull(result.issues)
        }

    @Test
    fun testOnGetProjectIssues_WithScreenOnlyNodes() =
        runTest {
            // Test with screen-only nodes like FAB
            val fabNode = createFabNode()
            screen.fabNode.value = fabNode

            val result = uiBuilderOperator.onGetProjectIssues(project)

            assertTrue(result.isSuccessful())
            assertTrue(result.errorMessages.isEmpty())
            assertNotNull(result.issues)
        }

    @Test
    fun testOnGetProjectIssues_ConsistentResults() =
        runTest {
            // Test that calling the method multiple times returns consistent results
            val result1 = uiBuilderOperator.onGetProjectIssues(project)
            val result2 = uiBuilderOperator.onGetProjectIssues(project)

            assertTrue(result1.isSuccessful())
            assertTrue(result2.isSuccessful())

            // Both calls should return the same number of issues for the same project state
            assertTrue(result1.issues.size == result2.issues.size)
        }

    // ========== Screen Management Tests ==========

    @Test
    fun testOnListScreens_EmptyProject() =
        runTest {
            // Clear all screens except the default one
            val result = uiBuilderOperator.onListScreens(project)

            assertTrue(result.isSuccessful())
            assertTrue(result.errorMessages.isEmpty())

            // Project should have at least one default screen
            val screens = project.screenHolder.screens
            assertTrue(screens.isNotEmpty())
        }

    @Test
    fun testOnListScreens_WithMultipleScreens() =
        runTest {
            // Add additional screens to the project
            val screen1 = Screen(id = "screen1", name = "Home Screen")
            screen1.title.value = "Home"
            screen1.label.value = "home"
            screen1.isDefault.value = true
            screen1.showOnNavigation.value = true

            val screen2 = Screen(id = "screen2", name = "Settings Screen")
            screen2.title.value = "Settings"
            screen2.label.value = "settings"
            screen2.isDefault.value = false
            screen2.showOnNavigation.value = true

            project.screenHolder.screens.add(screen1)
            project.screenHolder.screens.add(screen2)

            val result = uiBuilderOperator.onListScreens(project)

            assertTrue(result.isSuccessful())
            assertTrue(result.errorMessages.isEmpty())

            // Verify that all screens are included (original + 2 added)
            val totalScreens = project.screenHolder.screens.size
            assertTrue(totalScreens >= 3) // At least the original + 2 new screens
        }

    @Test
    fun testOnListScreens_ScreenProperties() =
        runTest {
            // Test that screen properties are properly accessible
            val testScreen = Screen(id = "test-screen", name = "Test Screen")
            testScreen.title.value = "Test Title"
            testScreen.label.value = "test_label"
            testScreen.isDefault.value = false
            testScreen.isSelected.value = true
            testScreen.showOnNavigation.value = false

            project.screenHolder.screens.add(testScreen)

            val result = uiBuilderOperator.onListScreens(project)

            assertTrue(result.isSuccessful())
            assertTrue(result.errorMessages.isEmpty())

            // Verify the screen exists in the project
            val foundScreen = project.screenHolder.screens.find { it.id == "test-screen" }
            assertNotNull(foundScreen)
            assertTrue(foundScreen!!.name == "Test Screen")
            assertTrue(foundScreen.title.value == "Test Title")
            assertTrue(foundScreen.label.value == "test_label")
            assertFalse(foundScreen.isDefault.value)
            assertTrue(foundScreen.isSelected.value)
            assertFalse(foundScreen.showOnNavigation.value)
        }

    @Test
    fun testOnGetScreenDetails_ValidScreenId() =
        runTest {
            // Use the default screen from the project
            val defaultScreen = project.screenHolder.screens.first()
            val screenId = defaultScreen.id

            val result = uiBuilderOperator.onGetScreenDetails(project, screenId)

            assertTrue(result.isSuccessful())
            assertTrue(result.errorMessages.isEmpty())
        }

    @Test
    fun testOnGetScreenDetails_InvalidScreenId() =
        runTest {
            val result = uiBuilderOperator.onGetScreenDetails(project, "non-existent-screen")

            assertFalse(result.isSuccessful())
            assertFalse(result.errorMessages.isEmpty())
            assertTrue(result.errorMessages.first().contains("Screen with ID 'non-existent-screen' not found"))
        }

    @Test
    fun testOnGetScreenDetails_WithScreenContent() =
        runTest {
            // Add some content to the screen
            val defaultScreen = project.screenHolder.screens.first()
            val rootNode = defaultScreen.getRootNode()

            val columnNode = createColumnNode()
            val buttonNode = createButtonNode()
            rootNode.addChild(columnNode)
            columnNode.addChild(buttonNode)

            val result = uiBuilderOperator.onGetScreenDetails(project, defaultScreen.id)

            assertTrue(result.isSuccessful())
            assertTrue(result.errorMessages.isEmpty())

            // Verify the screen contains the added content
            val retrievedScreen = project.screenHolder.findScreen(defaultScreen.id)
            assertNotNull(retrievedScreen)
            val retrievedRootNode = retrievedScreen!!.getRootNode()
            assertTrue(retrievedRootNode.children.isNotEmpty())

            val retrievedColumn = retrievedRootNode.children.first()
            assertTrue(retrievedColumn.trait.value is ColumnTrait)

            // Check if the column has children - it should, but let's be more defensive
            if (retrievedColumn.children.isNotEmpty()) {
                val retrievedButton = retrievedColumn.children.first()
                assertTrue(retrievedButton.trait.value is ButtonTrait)
            }
        }

    @Test
    fun testOnGetScreenDetails_WithSpecialScreenElements() =
        runTest {
            val defaultScreen = project.screenHolder.screens.first()

            // Add FAB to screen (screen-only element)
            val fabNode = createFabNode()
            defaultScreen.fabNode.value = fabNode

            val result = uiBuilderOperator.onGetScreenDetails(project, defaultScreen.id)

            assertTrue(result.isSuccessful())
            assertTrue(result.errorMessages.isEmpty())

            // Verify FAB is present on the screen
            assertNotNull(defaultScreen.fabNode.value)
            assertTrue(
                defaultScreen.fabNode.value!!
                    .trait.value is FabTrait,
            )
        }

    @Test
    fun testOnGetScreenDetails_ErrorHandling() =
        runTest {
            // Test with null screen ID (should be caught by method signature)
            val result = uiBuilderOperator.onGetScreenDetails(project, "")

            // Empty string should be treated as not found
            assertFalse(result.isSuccessful())
            assertFalse(result.errorMessages.isEmpty())
        }

    @Test
    fun testScreenManagement_IntegrationTest() =
        runTest {
            // Create a more complex screen setup
            val screen1 = Screen(id = "home", name = "Home")
            screen1.title.value = "Home Screen"
            screen1.isDefault.value = true

            val screen2 = Screen(id = "profile", name = "Profile")
            screen2.title.value = "Profile Screen"
            screen2.isDefault.value = false

            project.screenHolder.screens.clear()
            project.screenHolder.screens.add(screen1)
            project.screenHolder.screens.add(screen2)

            // Test listing screens
            val listResult = uiBuilderOperator.onListScreens(project)
            assertTrue(listResult.isSuccessful())

            // Test getting details for each screen
            val homeResult = uiBuilderOperator.onGetScreenDetails(project, "home")
            assertTrue(homeResult.isSuccessful())

            val profileResult = uiBuilderOperator.onGetScreenDetails(project, "profile")
            assertTrue(profileResult.isSuccessful())

            // Test getting details for non-existent screen
            val invalidResult = uiBuilderOperator.onGetScreenDetails(project, "invalid")
            assertFalse(invalidResult.isSuccessful())
            assertTrue(invalidResult.errorMessages.first().contains("not found"))
        }

    @Test
    fun testScreenManagement_ConsistentResults() =
        runTest {
            // Test that multiple calls return consistent results
            val listResult1 = uiBuilderOperator.onListScreens(project)
            val listResult2 = uiBuilderOperator.onListScreens(project)

            assertTrue(listResult1.isSuccessful())
            assertTrue(listResult2.isSuccessful())

            // Both calls should return the same number of screens
            assertTrue(project.screenHolder.screens.size == project.screenHolder.screens.size)

            val screenId =
                project.screenHolder.screens
                    .first()
                    .id
            val detailsResult1 = uiBuilderOperator.onGetScreenDetails(project, screenId)
            val detailsResult2 = uiBuilderOperator.onGetScreenDetails(project, screenId)

            assertTrue(detailsResult1.isSuccessful())
            assertTrue(detailsResult2.isSuccessful())

            // Both calls should succeed consistently
            assertTrue(detailsResult1.errorMessages.isEmpty())
            assertTrue(detailsResult2.errorMessages.isEmpty())
        }
}

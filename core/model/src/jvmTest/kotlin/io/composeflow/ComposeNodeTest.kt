package io.composeflow

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.dp
import io.composeflow.model.modifier.ModifierWrapper
import io.composeflow.model.palette.Constraint.Companion.DUPLICATE_VERTICAL_INFINITE_SCROLL
import io.composeflow.model.parameter.BoxTrait
import io.composeflow.model.parameter.ButtonTrait
import io.composeflow.model.parameter.ColumnTrait
import io.composeflow.model.parameter.IconTrait
import io.composeflow.model.parameter.LazyColumnTrait
import io.composeflow.model.parameter.RowTrait
import io.composeflow.model.parameter.TextFieldTrait
import io.composeflow.model.parameter.TextTrait
import io.composeflow.model.parameter.wrapper.AlignmentWrapper
import io.composeflow.model.parameter.wrapper.ColorWrapper
import io.composeflow.model.parameter.wrapper.Material3ColorWrapper
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.project.appscreen.screen.composenode.restoreInstance
import io.composeflow.model.property.ApiResultProperty
import io.composeflow.model.property.ColorProperty
import io.composeflow.model.property.StringProperty
import io.composeflow.override.mutableStateListEqualsOverrideOf
import io.composeflow.serializer.decodeFromStringWithFallback
import io.composeflow.serializer.encodeToString
import junit.framework.TestCase.assertFalse
import kotlinx.serialization.encodeToString
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class ComposeNodeTest {
    private val root =
        ComposeNode(
            trait = mutableStateOf(BoxTrait()),
            label = mutableStateOf("Root"),
        ).apply {
            boundsInWindow.value = Rect(left = 0f, top = 0f, right = 200f, bottom = 200f)
        }
    private lateinit var child: ComposeNode
    private lateinit var grandChild: ComposeNode

    @Before
    fun setUp() {
        root.addChild(
            ComposeNode(
                trait =
                    mutableStateOf(
                        BoxTrait(
                            contentAlignment = AlignmentWrapper.BottomCenter,
                        ),
                    ),
            ).apply {
                boundsInWindow.value = Rect(left = 0f, top = 0f, right = 150f, bottom = 150f)

                addChild(
                    ComposeNode(
                        trait =
                            mutableStateOf(
                                TextTrait(
                                    text = StringProperty.StringIntrinsicValue("testString"),
                                    colorWrapper =
                                        ColorProperty.ColorIntrinsicValue(
                                            ColorWrapper(
                                                themeColor = Material3ColorWrapper.OnPrimary,
                                            ),
                                        ),
                                ),
                            ),
                        modifierList = mutableStateListEqualsOverrideOf(ModifierWrapper.Padding(8.dp)),
                    ).apply {
                        boundsInWindow.value =
                            Rect(left = 0f, top = 0f, right = 100f, bottom = 100f)
                    },
                )
            },
        )
        child = root.children.first()
        grandChild =
            root.children
                .first()
                .children
                .first()
    }

    @Test
    fun verifyLevelIsAccumulatedOnAdded() {
        assertEquals(root.level, 0)
        assertTrue(root.children.all { it.level == 1 })
        assertTrue(child.children.all { it.level == 2 })
    }

    @Test
    fun verifyParentNodesArePropertySet() {
        assertTrue(root.parentNode == null)
        assertEquals(root, child.parentNode)
        assertEquals(grandChild.parentNode, child)
    }

    @Test
    fun findDeepestChildAtOrNull_verify_deepestChildIsFound() {
        val found = root.findDeepestChildAtOrNull(Offset(50f, 50f))
        val expected =
            root.children
                .first()
                .children
                .first()
        assertEquals(expected, found)
    }

    @Test
    fun findDeepestChildAtOrNull_verify_rootIsFound_atPosition_offChild() {
        val found = root.findDeepestChildAtOrNull(Offset(175f, 175f))
        assertEquals(root, found)
    }

    @Test
    fun findDeepestContainerAtOrNull_verify_deepestContainerIsFound() {
        val deepestContainer =
            ComposeNode(
                trait = mutableStateOf(ColumnTrait()),
            ).apply {
                boundsInWindow.value = Rect(left = 0f, top = 0f, right = 50f, bottom = 50f)
            }
        root.children.firstOrNull { it.isContainer() }?.addChild(
            deepestContainer,
        )

        val found = root.findDeepestContainerAtOrNull(Offset(25f, 25f))
        val expected =
            root.children
                .firstOrNull { it.isContainer() }
                ?.children
                ?.firstOrNull { it.isContainer() }
        assertEquals(expected, found)
    }

    @Test
    fun findDeepestFocusedNode() {
        val focused =
            ComposeNode(
                trait = mutableStateOf(ButtonTrait()),
                label = mutableStateOf("focused"),
            ).apply {
                boundsInWindow.value = Rect(left = 0f, top = 0f, right = 50f, bottom = 50f)
                isFocused.value = true
                level = 2
            }

        child.addChild(child = focused)
        val found = root.findFirstFocusedNodeOrNull()
        assertEquals(focused, found)
    }

    @Test
    fun findNodesUntilRoot() {
        val leaf =
            root.children
                .first()
                .children
                .first()
        val nodesUntilRoot = leaf.findNodesUntilRoot()
        val expected = listOf(root.children.first(), root)
        assertEquals(expected, nodesUntilRoot)
    }

    @Test
    fun findRoot_fromLeaf() {
        val leaf =
            root.children
                .first()
                .children
                .first()
        val found = leaf.findRoot()
        assertEquals(root, found)
    }

    @Test
    fun findRoot_fromRoot() {
        val found = root.findRoot()
        assertEquals(root, found)
    }

    @Test
    fun removeNode_verifyCallingNodeIsRemoved_withoutExcludeIndex() {
        grandChild.removeFromParent()
        assertTrue(child.children.isEmpty())
    }

    @Test
    fun removeNode_withExcludeIndex_verifyNodesOtherThanExcludeIndex_areRemoved() {
        root.children
            .first()
            .children
            .first()
            .removeFromParent()
        child.addChild(grandChild)
        child.addChild(ComposeNode(trait = mutableStateOf(TextFieldTrait())))
        child.addChild(grandChild)

        grandChild.removeFromParent(excludeIndex = 0)
        assertTrue(child.children.size == 2)
        assertEquals(child.children[0], grandChild)
        assertEquals(child.children[1].trait.value, TextFieldTrait())
    }

    @Test
    fun getHoveredNode_sameLevel_smallerRectNode_isPicked() {
        val box =
            ComposeNode(trait = mutableStateOf(BoxTrait())).apply {
                boundsInWindow.value = Rect(left = 0f, top = 0f, right = 200f, bottom = 200f)
            }
        val childColumn =
            ComposeNode(
                trait = mutableStateOf(ColumnTrait()),
                modifierList = mutableStateListEqualsOverrideOf(ModifierWrapper.FillMaxSize()),
            ).apply {
                boundsInWindow.value = Rect(left = 0f, top = 0f, right = 200f, bottom = 200f)
                level = 1
            }
        val childIcon =
            ComposeNode(
                trait = mutableStateOf(IconTrait()),
            ).apply {
                boundsInWindow.value = Rect(left = 0f, top = 0f, right = 50f, bottom = 50f)
                level = 1
            }
        box.addChild(childColumn)
        box.addChild(childIcon)

        val found = box.findDeepestChildAtOrNull(Offset(25f, 25f))

        assertEquals(childIcon, found)
    }

    @Test
    fun nesting_lazyColumn_is_not_allowed() {
        val rootLazyColumn =
            ComposeNode(
                trait = mutableStateOf(LazyColumnTrait()),
                label = mutableStateOf("Root"),
            ).apply {
                boundsInWindow.value = Rect(left = 0f, top = 0f, right = 200f, bottom = 200f)
            }
        rootLazyColumn.addChild(
            ComposeNode(
                trait = mutableStateOf(LazyColumnTrait()),
            ).apply {
                boundsInWindow.value = Rect(left = 0f, top = 0f, right = 150f, bottom = 150f)
            },
        )
        val child = rootLazyColumn.children.first()
        val errorMessages = child.checkConstraints(rootLazyColumn)
        assertTrue(DUPLICATE_VERTICAL_INFINITE_SCROLL in errorMessages)
    }

    @Test
    fun constructingTree_onTheFly_verifyLevelsAndChildParentRelationships() {
        val root =
            ComposeNode(
                trait = mutableStateOf(ColumnTrait()),
            ).apply {
                addChild(
                    ComposeNode(
                        trait = mutableStateOf(RowTrait()),
                    ).apply {
                        addChild(
                            ComposeNode(
                                trait = mutableStateOf(TextTrait()),
                            ),
                        )
                    },
                )
            }

        val row = root.children.first()
        val text = row.children.first()
        assertEquals(RowTrait(), row.trait.value)
        assertEquals(TextTrait(), text.trait.value)
        assertEquals(0, root.level)
        assertEquals(1, row.level)
        assertEquals(2, text.level)
        assertEquals(null, root.parentNode)
        assertEquals(root, row.parentNode)
        assertEquals(row, text.parentNode)
    }

    @Test
    fun restoreInstance_verifyEqualityOfValue_sameId() {
        val rootCopy = root.restoreInstance(sameId = true)
        assertTrue(root.contentEquals(rootCopy))
        assertNotEquals(root, rootCopy)
    }

    @Test
    fun restoreInstance_verifyEqualityOfValue_notSameId() {
        val rootCopy = root.restoreInstance(sameId = false)
        assertTrue(root.contentEquals(rootCopy))
        assertFalse(root.contentEquals(rootCopy, excludeId = false))
        assertNotEquals(root, rootCopy)
    }

    @Test
    fun findNearestContainerOrNull_fromRoot() {
        assertEquals(root, root.findNearestContainerOrNull())
    }

    @Test
    fun findNearestContainerOrNull_fromChild() {
        assertEquals(child, child.findNearestContainerOrNull())
    }

    @Test
    fun findNearestContainerOrNull_fromGrandChild() {
        assertEquals(child, grandChild.findNearestContainerOrNull())
    }

    @Test
    fun serialize_deserialize_grandChild() {
        val encoded = encodeToString(grandChild)
        val decoded = decodeFromStringWithFallback<ComposeNode>(encoded)

        assertTrue(grandChild.contentEquals(decoded))
    }

    @Test
    fun serialize_deserialize_child() {
        val encoded = encodeToString(child)
        val decoded = decodeFromStringWithFallback<ComposeNode>(encoded)

        assertTrue(child.contentEquals(decoded))
    }

    @Test
    fun serialize_deserialize_root() {
        val encoded = encodeToString(root)
        val decoded = decodeFromStringWithFallback<ComposeNode>(encoded)

        assertTrue(root.contentEquals(decoded))
    }

    @Test
    fun serialize_deserialize_withDynamicItems() {
        val node =
            ComposeNode(
                dynamicItems =
                    mutableStateOf(
                        ApiResultProperty(
                            apiId = Uuid.random().toString(),
                        ),
                    ),
            )
        val encoded = encodeToString(node)
        val decoded = decodeFromStringWithFallback<ComposeNode>(encoded)

        assertTrue(node.contentEquals(decoded))
    }
}

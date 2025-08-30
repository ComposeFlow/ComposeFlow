package io.composeflow.model.action

import androidx.compose.runtime.mutableStateOf
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.model.parameter.LazyVerticalGridTrait
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.property.IntProperty
import kotlin.test.Test
import kotlin.test.assertEquals

class StateOperationTest {

    @Test
    fun testGetUpdateIndexParamsAsString_verifyNoSpaces() {
        val project = Project()
        val mockContext = GenerationContext()
        val lazyListNodeId = "lazy-grid-node-123"

        val operation = StateOperationForList.RemoveValueAtIndex(
            IntProperty.ValueFromLazyListIndex(
                lazyListNodeId = lazyListNodeId
            )
        )
        project.screenHolder.currentScreen().getRootNode().addChild(
            ComposeNode(
                id = lazyListNodeId,
                trait = mutableStateOf(LazyVerticalGridTrait())
            )
        )

        val result = operation.getUpdateIndexParamsAsString(project, mockContext)

        assertEquals("lazyVGridIndex", result)
    }
}

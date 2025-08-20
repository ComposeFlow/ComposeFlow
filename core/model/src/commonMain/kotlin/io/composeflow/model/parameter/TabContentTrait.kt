package io.composeflow.model.parameter

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper
import io.composeflow.Res
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.model.palette.PaletteRenderParams
import io.composeflow.model.palette.TraitCategory
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.tooltip_tab_content_trait
import io.composeflow.ui.CanvasNodeCallbacks
import io.composeflow.ui.modifierForCanvas
import io.composeflow.ui.zoomablecontainer.ZoomableContainerStateHolder
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource

@Serializable
@SerialName("TabContentTrait")
data object TabContentTrait : ComposeTrait {
    override fun icon(): ImageVector = Icons.Outlined.Description

    override fun iconText(): String = "TabContent"

    override fun paletteCategories(): List<TraitCategory> = listOf(TraitCategory.Container, TraitCategory.TabContent)

    override fun tooltipResource(): StringResource = Res.string.tooltip_tab_content_trait

    override fun visibleInPalette(): Boolean = false

    override fun isResizeable(): Boolean = false

    override fun isEditable(): Boolean = false

    @Composable
    override fun RenderedNode(
        project: Project,
        node: ComposeNode,
        canvasNodeCallbacks: CanvasNodeCallbacks,
        paletteRenderParams: PaletteRenderParams,
        zoomableContainerStateHolder: ZoomableContainerStateHolder,
        modifier: Modifier,
    ) {
        Column(
            modifier =
                modifier.then(
                    node
                        .modifierChainForCanvas()
                        .modifierForCanvas(
                            project = project,
                            node = node,
                            canvasNodeCallbacks = canvasNodeCallbacks,
                            isDraggable = false,
                            paletteRenderParams = paletteRenderParams,
                            zoomableContainerStateHolder = zoomableContainerStateHolder,
                        ),
                ),
        ) {
            node.children.forEach {
                it.RenderedNodeInCanvas(
                    project = project,
                    canvasNodeCallbacks = canvasNodeCallbacks,
                    paletteRenderParams = paletteRenderParams,
                    zoomableContainerStateHolder = zoomableContainerStateHolder,
                )
            }
        }
    }

    override fun generateCode(
        project: Project,
        node: ComposeNode,
        context: GenerationContext,
        dryRun: Boolean,
    ): CodeBlockWrapper =
        ColumnTrait().generateCode(
            project = project,
            node,
            context = context,
            dryRun = dryRun,
        )
}

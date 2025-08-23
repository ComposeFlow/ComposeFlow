package io.composeflow.model.parameter
import DividerVertical
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.composeflow.Res
import io.composeflow.custom.ComposeFlowIcons
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper
import io.composeflow.kotlinpoet.wrapper.MemberNameWrapper
import io.composeflow.model.modifier.ModifierWrapper
import io.composeflow.model.modifier.generateModifierCode
import io.composeflow.model.palette.PaletteRenderParams
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.property.AssignableProperty
import io.composeflow.model.property.ColorProperty
import io.composeflow.override.mutableStateListEqualsOverrideOf
import io.composeflow.serializer.LocationAwareDpSerializer
import io.composeflow.tooltip_vertical_divider_trait
import io.composeflow.ui.CanvasNodeCallbacks
import io.composeflow.ui.modifierForCanvas
import io.composeflow.ui.zoomablecontainer.ZoomableContainerStateHolder
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource

@Serializable
@SerialName("VerticalDividerTrait")
data class VerticalDividerTrait(
    @Serializable(LocationAwareDpSerializer::class)
    override val thickness: Dp? = null,
    override val color: AssignableProperty? = null,
) : DividerTrait(thickness, color),
    ComposeTrait {
    // Explicitly extending ComposeTrait so that this class is recognized as a subclass of it.
    // As a result this class is considered as a subclass of ComposeTrait in the jsonschema

    override fun iconText(): String = "V Divider"

    override fun defaultComposeNode(project: Project): ComposeNode =
        ComposeNode(
            trait =
                mutableStateOf(
                    VerticalDividerTrait(),
                ),
            modifierList = mutableStateListEqualsOverrideOf(ModifierWrapper.Padding(8.dp)),
        )

    override fun icon(): ImageVector = ComposeFlowIcons.DividerVertical

    override fun tooltipResource(): StringResource = Res.string.tooltip_vertical_divider_trait

    override fun isResizeable(): Boolean = false

    @Composable
    override fun RenderedNode(
        project: Project,
        node: ComposeNode,
        canvasNodeCallbacks: CanvasNodeCallbacks,
        paletteRenderParams: PaletteRenderParams,
        zoomableContainerStateHolder: ZoomableContainerStateHolder,
        modifier: Modifier,
    ) {
        VerticalDivider(
            thickness = thickness ?: DividerDefaults.Thickness,
            color =
                (color as? ColorProperty.ColorIntrinsicValue)?.value?.getColor()
                    ?: DividerDefaults.color,
            modifier =
                modifier.then(
                    node
                        .modifierChainForCanvas()
                        .modifierForCanvas(
                            project = project,
                            node = node,
                            paletteRenderParams = paletteRenderParams,
                            canvasNodeCallbacks = canvasNodeCallbacks,
                            zoomableContainerStateHolder = zoomableContainerStateHolder,
                        ),
                ),
        )
    }

    override fun generateCode(
        project: Project,
        node: ComposeNode,
        context: GenerationContext,
        dryRun: Boolean,
    ): CodeBlockWrapper {
        val codeBlockBuilder = CodeBlockWrapper.builder()
        codeBlockBuilder.addStatement(
            "%M(",
            MemberNameWrapper.get("androidx.compose.material3", "VerticalDivider"),
        )
        codeBlockBuilder.add(
            generateParamsCode(
                project = project,
                context = context,
                dryRun = dryRun,
            ),
        )
        codeBlockBuilder.add(
            node.generateModifierCode(project, context, dryRun = dryRun),
        )
        codeBlockBuilder.addStatement(")")
        return codeBlockBuilder.build()
    }
}

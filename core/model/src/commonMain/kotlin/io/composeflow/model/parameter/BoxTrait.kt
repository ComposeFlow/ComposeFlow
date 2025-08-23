package io.composeflow.model.parameter

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BorderOuter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.composeflow.Res
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper
import io.composeflow.kotlinpoet.wrapper.MemberNameWrapper
import io.composeflow.model.modifier.ModifierWrapper
import io.composeflow.model.modifier.generateModifierCode
import io.composeflow.model.palette.PaletteRenderParams
import io.composeflow.model.palette.TraitCategory
import io.composeflow.model.parameter.wrapper.AlignmentWrapper
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.override.mutableStateListEqualsOverrideOf
import io.composeflow.tooltip_box_trait
import io.composeflow.ui.CanvasNodeCallbacks
import io.composeflow.ui.modifierForCanvas
import io.composeflow.ui.zoomablecontainer.ZoomableContainerStateHolder
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource

@Serializable
@SerialName("BoxTrait")
data class BoxTrait(
    val contentAlignment: AlignmentWrapper? = null,
    val propagateMinConstraints: Boolean? = null,
) : ComposeTrait {
    override fun areAllParamsEmpty(): Boolean = contentAlignment == null && propagateMinConstraints == null

    private fun generateParamsCode(): CodeBlockWrapper {
        val codeBlockBuilder = CodeBlockWrapper.builder()
        val alignmentMember = MemberNameWrapper.get("androidx.compose.ui", "Alignment")
        contentAlignment?.let {
            codeBlockBuilder.addStatement("contentAlignment = %M.${it.name},", alignmentMember)
        }
        propagateMinConstraints?.let {
            codeBlockBuilder.addStatement("propagateMinConstraints = $it,")
        }
        return codeBlockBuilder.build()
    }

    override fun defaultComposeNode(project: Project): ComposeNode =
        ComposeNode(
            modifierList = defaultModifierList(),
            trait = mutableStateOf(BoxTrait()),
        )

    override fun icon(): ImageVector = Icons.Rounded.BorderOuter

    override fun iconText(): String = "Box"

    override fun paletteCategories(): List<TraitCategory> =
        listOf(
            TraitCategory.Container,
            TraitCategory.WrapContainer,
            TraitCategory.Layout,
        )

    override fun tooltipResource(): StringResource = Res.string.tooltip_box_trait

    override fun defaultModifierList(): MutableList<ModifierWrapper> =
        mutableStateListEqualsOverrideOf(
            ModifierWrapper.Width(120.dp),
            ModifierWrapper.Height(120.dp),
            ModifierWrapper.Padding(all = 8.dp),
        )

    @Composable
    override fun RenderedNode(
        project: Project,
        node: ComposeNode,
        canvasNodeCallbacks: CanvasNodeCallbacks,
        paletteRenderParams: PaletteRenderParams,
        zoomableContainerStateHolder: ZoomableContainerStateHolder,
        modifier: Modifier,
    ) {
        Box(
            modifier =
                modifier.then(
                    node
                        .modifierChainForCanvas()
                        .modifierForCanvas(
                            project = project,
                            node = node,
                            canvasNodeCallbacks = canvasNodeCallbacks,
                            paletteRenderParams = paletteRenderParams,
                            zoomableContainerStateHolder = zoomableContainerStateHolder,
                        ),
                ),
            contentAlignment = contentAlignment?.alignment ?: Alignment.TopStart,
            propagateMinConstraints = propagateMinConstraints ?: false,
        ) {
            node.children.forEach { child ->
                child.RenderedNodeInCanvas(
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
    ): CodeBlockWrapper {
        val codeBlockBuilder = CodeBlockWrapper.builder()
        val boxMember = MemberNameWrapper.get("androidx.compose.foundation.layout", "Box")
        val allParamsEmpty = areAllParamsEmpty() && node.modifierList.isEmpty()
        if (allParamsEmpty) {
            codeBlockBuilder.addStatement("%M {", boxMember)
        } else {
            codeBlockBuilder.addStatement("%M(", boxMember)
            codeBlockBuilder.add(
                generateParamsCode(),
            )
            codeBlockBuilder.add(
                node.generateModifierCode(project, context, dryRun = dryRun),
            )
            codeBlockBuilder.addStatement(") {")
        }
        node.children.forEach {
            codeBlockBuilder.add(
                it.generateCode(
                    project = project,
                    context = context,
                    dryRun = dryRun,
                ),
            )
        }
        codeBlockBuilder.addStatement("}")
        return codeBlockBuilder.build()
    }
}

package io.composeflow.model.parameter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.composeflow.Res
import io.composeflow.custom.ComposeFlowIcons
import io.composeflow.custom.composeflowicons.Column
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.kotlinpoet.MemberHolder
import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper
import io.composeflow.kotlinpoet.wrapper.MemberNameWrapper
import io.composeflow.model.modifier.ModifierWrapper
import io.composeflow.model.modifier.generateModifierCode
import io.composeflow.model.palette.PaletteRenderParams
import io.composeflow.model.palette.TraitCategory
import io.composeflow.model.parameter.wrapper.AlignmentHorizontalWrapper
import io.composeflow.model.parameter.wrapper.ArrangementVerticalWrapper
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.override.mutableStateListEqualsOverrideOf
import io.composeflow.tooltip_column_trait
import io.composeflow.ui.CanvasNodeCallbacks
import io.composeflow.ui.modifierForCanvas
import io.composeflow.ui.zoomablecontainer.ZoomableContainerStateHolder
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource

@Serializable
@SerialName("ColumnTrait")
data class ColumnTrait(
    val verticalArrangementWrapper: ArrangementVerticalWrapper? = null,
    val horizontalAlignmentWrapper: AlignmentHorizontalWrapper? = null,
) : ComposeTrait {
    override fun areAllParamsEmpty(): Boolean = verticalArrangementWrapper == null && horizontalAlignmentWrapper == null

    private fun generateParamsCode(): CodeBlockWrapper {
        val codeBlockBuilder = CodeBlockWrapper.builder()
        verticalArrangementWrapper?.let {
            val arrangementMember = MemberNameWrapper.get("androidx.compose.foundation.layout", "Arrangement")
            codeBlockBuilder.addStatement(
                "verticalArrangement = %M.${it.name},",
                arrangementMember,
            )
        }
        horizontalAlignmentWrapper?.let {
            val alignmentMember = MemberNameWrapper.get("androidx.compose.ui", "Alignment")
            codeBlockBuilder.addStatement("horizontalAlignment = %M.${it.name},", alignmentMember)
        }
        return codeBlockBuilder.build()
    }

    override fun defaultComposeNode(project: Project): ComposeNode =
        ComposeNode(
            modifierList = defaultModifierList(),
            trait = mutableStateOf(ColumnTrait()),
        )

    override fun icon(): ImageVector = ComposeFlowIcons.Column

    override fun iconText(): String = "Column"

    override fun paletteCategories(): List<TraitCategory> =
        listOf(
            TraitCategory.Common,
            TraitCategory.Container,
            TraitCategory.WrapContainer,
            TraitCategory.Layout,
        )

    override fun tooltipResource(): StringResource = Res.string.tooltip_column_trait

    override fun defaultModifierList(): MutableList<ModifierWrapper> =
        mutableStateListEqualsOverrideOf(
            ModifierWrapper.Height(200.dp),
            ModifierWrapper.Width(120.dp),
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
        Column(
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
            verticalArrangement =
                verticalArrangementWrapper?.arrangement
                    ?: Arrangement.Top,
            horizontalAlignment =
                horizontalAlignmentWrapper?.alignment
                    ?: Alignment.Start,
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
        val allParamsEmpty = areAllParamsEmpty() && node.modifierList.isEmpty()
        if (allParamsEmpty) {
            codeBlockBuilder.addStatement("%M {", MemberHolder.AndroidX.Layout.Column)
        } else {
            codeBlockBuilder.addStatement("%M(", MemberHolder.AndroidX.Layout.Column)
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

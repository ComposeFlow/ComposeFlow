package io.composeflow.model.parameter

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper
import io.composeflow.Res
import io.composeflow.custom.ComposeFlowIcons
import io.composeflow.custom.composeflowicons.ComposeLogo
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.model.action.ActionType
import io.composeflow.model.modifier.generateModifierCode
import io.composeflow.model.palette.PaletteRenderParams
import io.composeflow.model.palette.TraitCategory
import io.composeflow.model.project.ParameterId
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.project.component.COMPONENT_KEY_NAME
import io.composeflow.model.project.findComponentOrNull
import io.composeflow.model.project.findParameterOrThrow
import io.composeflow.model.property.AssignableProperty
import io.composeflow.serializer.FallbackMutableStateMapSerializer
import io.composeflow.tooltip_component_trait
import io.composeflow.ui.CanvasNodeCallbacks
import io.composeflow.ui.modifierForCanvas
import io.composeflow.ui.zoomablecontainer.ZoomableContainerStateHolder
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource

@Serializable
@SerialName("ComponentTrait")
data class ComponentTrait(
    @Serializable(FallbackMutableStateMapSerializer::class)
    val paramsMap: MutableMap<ParameterId, AssignableProperty> =
        mutableStateMapOf(),
) : ComposeTrait {
    override fun areAllParamsEmpty(): Boolean = paramsMap.isEmpty()

    private fun generateParamsCode(
        project: Project,
        context: GenerationContext,
        dryRun: Boolean,
    ): CodeBlockWrapper {
        val codeBlockBuilder = CodeBlockWrapper.builder()
        paramsMap.forEach {
            val parameter = project.findParameterOrThrow(it.key)
            val assignableProperty = it.value
            codeBlockBuilder.add("${parameter.variableName} = ")
            codeBlockBuilder.add(
                assignableProperty.transformedCodeBlock(
                    project,
                    context,
                    dryRun = dryRun,
                ),
            )
            codeBlockBuilder.addStatement(",")
        }
        return codeBlockBuilder.build()
    }

    override fun icon(): ImageVector = ComposeFlowIcons.ComposeLogo

    override fun iconText(): String = "Component"

    override fun paletteCategories(): List<TraitCategory> = listOf(TraitCategory.Container)

    override fun tooltipResource(): StringResource = Res.string.tooltip_component_trait

    override fun visibleInPalette(): Boolean = false

    override fun isDroppable(): Boolean = false

    override fun isVisibilityConditional(): Boolean = false

    override fun actionTypes(): List<ActionType> = listOf(ActionType.OnInitialLoad)

    @Composable
    override fun RenderedNode(
        project: Project,
        node: ComposeNode,
        canvasNodeCallbacks: CanvasNodeCallbacks,
        paletteRenderParams: PaletteRenderParams,
        zoomableContainerStateHolder: ZoomableContainerStateHolder,
        modifier: Modifier,
    ) {
        Column(modifier = modifier) {
            if (node.children.isNotEmpty()) {
                node.children[0].RenderedNodeInCanvas(
                    project = project,
                    canvasNodeCallbacks = canvasNodeCallbacks,
                    paletteRenderParams = paletteRenderParams,
                    zoomableContainerStateHolder = zoomableContainerStateHolder,
                    modifier =
                        Modifier.modifierForCanvas(
                            project = project,
                            node = node,
                            canvasNodeCallbacks = canvasNodeCallbacks,
                            paletteRenderParams = paletteRenderParams,
                            zoomableContainerStateHolder = zoomableContainerStateHolder,
                        ),
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
        val component =
            node.componentId?.let { project.findComponentOrNull(it) }
                ?: return codeBlockBuilder.build()
        codeBlockBuilder.addStatement(
            "%M(",
            component.asMemberName(project),
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
        val componentInvocationCount = context.componentCountMap[component.name] ?: 0
        context.componentCountMap[component.name] = componentInvocationCount + 1
        codeBlockBuilder.addStatement("$COMPONENT_KEY_NAME = \"${component.name}-$componentInvocationCount\"")
        codeBlockBuilder.addStatement(")")
        return codeBlockBuilder.build()
    }
}

package io.composeflow.model.parameter

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import io.composeflow.Res
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper
import io.composeflow.kotlinpoet.wrapper.MemberNameWrapper
import io.composeflow.model.action.ActionType
import io.composeflow.model.modifier.generateModifierCode
import io.composeflow.model.palette.PaletteRenderParams
import io.composeflow.model.palette.TraitCategory
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.project.findCanvasEditableHavingNodeOrNull
import io.composeflow.model.property.AssignableProperty
import io.composeflow.model.property.BooleanProperty
import io.composeflow.model.property.PropertyContainer
import io.composeflow.model.property.ValueFromCompanionState
import io.composeflow.model.property.asBooleanValue
import io.composeflow.model.state.ScreenState
import io.composeflow.model.state.StateHolder
import io.composeflow.model.state.WriteableState
import io.composeflow.model.type.ComposeFlowType
import io.composeflow.tooltip_checkbox_trait
import io.composeflow.ui.CanvasNodeCallbacks
import io.composeflow.ui.modifierForCanvas
import io.composeflow.ui.zoomablecontainer.ZoomableContainerStateHolder
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource

@Serializable
@SerialName("CheckboxTrait")
data class CheckboxTrait(
    val checked: AssignableProperty = BooleanProperty.BooleanIntrinsicValue(false),
    val enabled: AssignableProperty = BooleanProperty.BooleanIntrinsicValue(true),
) : ComposeTrait {
    override fun getPropertyContainers(): List<PropertyContainer> =
        listOf(
            PropertyContainer("Checked", checked, ComposeFlowType.BooleanType()),
            PropertyContainer("Enabled", enabled, ComposeFlowType.BooleanType()),
        )

    private fun generateParamsCode(
        project: Project,
        node: ComposeNode,
        context: GenerationContext,
        dryRun: Boolean,
    ): CodeBlockWrapper {
        val codeBlockBuilder = CodeBlockWrapper.builder()
        codeBlockBuilder.add("checked = ")
        codeBlockBuilder.add(checked.transformedCodeBlock(project, context, dryRun = dryRun))
        codeBlockBuilder.addStatement(",")

        val canvasEditable = project.findCanvasEditableHavingNodeOrNull(node)

        val writeState = checked.findReadableState(project, canvasEditable, node)
        codeBlockBuilder.addStatement("onCheckedChange = {")
        if (node.actionsMap[ActionType.OnChange]?.isNotEmpty() == true) {
            node.actionsMap[ActionType.OnChange]?.forEach {
                codeBlockBuilder.add(it.generateCodeBlock(project, context, dryRun = dryRun))
            }
        }
        if (writeState != null &&
            canvasEditable != null &&
            writeState is WriteableState &&
            node.companionStateId == writeState.id
        ) {
            codeBlockBuilder.add(
                writeState.generateWriteBlock(
                    project,
                    canvasEditable,
                    context,
                    dryRun = dryRun,
                ),
            )
        }
        codeBlockBuilder.addStatement("},")

        enabled.let {
            codeBlockBuilder.add("enabled = ")
            codeBlockBuilder.add(it.transformedCodeBlock(project, context, dryRun = dryRun))
            codeBlockBuilder.addStatement(",")
        }
        return codeBlockBuilder.build()
    }

    // onCheckedChange is included in params. Handle the onClick in that method
    override fun onClickIncludedInParams(): Boolean = true

    override fun defaultComposeNode(project: Project): ComposeNode =
        ComposeNode(
            trait =
                mutableStateOf(
                    CheckboxTrait(
                        checked = BooleanProperty.BooleanIntrinsicValue(false),
                        enabled = BooleanProperty.BooleanIntrinsicValue(true),
                    ),
                ),
        )

    override fun icon(): ImageVector = Icons.Outlined.CheckBox

    override fun iconText(): String = "Checkbox"

    override fun paletteCategories(): List<TraitCategory> = listOf(TraitCategory.Basic)

    override fun tooltipResource(): StringResource = Res.string.tooltip_checkbox_trait

    override fun isResizeable(): Boolean = false

    override fun actionTypes(): List<ActionType> = listOf(ActionType.OnChange)

    override fun companionState(composeNode: ComposeNode): ScreenState<*> =
        ScreenState.BooleanScreenState(
            id = composeNode.companionStateId,
            name = composeNode.label.value,
            companionNodeId = composeNode.id,
        )

    override fun onAttachStateToNode(
        project: Project,
        stateHolder: StateHolder,
        node: ComposeNode,
    ) {
        node.label.value = stateHolder.createUniqueLabel(project, node, node.label.value)
        node.trait.value =
            this.copy(checked = ValueFromCompanionState(node))
    }

    override fun updateCompanionStateProperties(composeNode: ComposeNode) {
        if (checked is ValueFromCompanionState) {
            composeNode.trait.value = this.copy(checked = ValueFromCompanionState(composeNode))
        }
    }

    @Composable
    override fun RenderedNode(
        project: Project,
        node: ComposeNode,
        canvasNodeCallbacks: CanvasNodeCallbacks,
        paletteRenderParams: PaletteRenderParams,
        zoomableContainerStateHolder: ZoomableContainerStateHolder,
        modifier: Modifier,
    ) {
        Checkbox(
            checked = false,
            onCheckedChange = {
            },
            enabled = enabled.asBooleanValue(),
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
            MemberNameWrapper.get("androidx.compose.material3", "Checkbox"),
        )
        codeBlockBuilder.add(
            generateParamsCode(
                project = project,
                node = node,
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

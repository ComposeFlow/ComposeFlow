package io.composeflow.model.parameter

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper
import io.composeflow.kotlinpoet.wrapper.MemberNameWrapper
import io.composeflow.Res
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.model.action.ActionType
import io.composeflow.model.modifier.generateModifierCode
import io.composeflow.model.palette.PaletteRenderParams
import io.composeflow.model.palette.TraitCategory
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.project.findCanvasEditableHavingNodeOrNull
import io.composeflow.model.property.AssignableProperty
import io.composeflow.model.property.CustomEnumValuesProperty
import io.composeflow.model.property.PropertyContainer
import io.composeflow.model.property.StringProperty
import io.composeflow.model.property.ValueFromCompanionState
import io.composeflow.model.state.ScreenState
import io.composeflow.model.state.StateHolder
import io.composeflow.model.state.WriteableState
import io.composeflow.model.type.ComposeFlowType
import io.composeflow.tooltip_dropdown_trait
import io.composeflow.ui.CanvasNodeCallbacks
import io.composeflow.ui.modifierForCanvas
import io.composeflow.ui.textfield.DropdownMenuTextField
import io.composeflow.ui.zoomablecontainer.ZoomableContainerStateHolder
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource

@Serializable
@SerialName("DropdownTrait")
data class DropdownTrait(
    val selectedItem: AssignableProperty? = null,
    val items: AssignableProperty? = null,
    val label: AssignableProperty = StringProperty.StringIntrinsicValue("Dropdown item..."),
) : ComposeTrait {
    override fun getPropertyContainers(): List<PropertyContainer> =
        listOf(
            PropertyContainer("Items", items, ComposeFlowType.StringType(isList = true)),
            PropertyContainer("Label", label, ComposeFlowType.StringType()),
        )

    private fun generateParamsCode(
        project: Project,
        node: ComposeNode,
        context: GenerationContext,
        dryRun: Boolean,
    ): CodeBlockWrapper {
        val codeBlockBuilder = CodeBlockWrapper.builder()
        codeBlockBuilder.add("items = ")
        when (items) {
            null -> {
                codeBlockBuilder.add("emptyList<String>()")
            }

            else -> {
                codeBlockBuilder.add(
                    items.transformedCodeBlock(
                        project,
                        context,
                        dryRun = dryRun,
                        writeType = ComposeFlowType.StringType(isList = true),
                    ),
                )
            }
        }
        codeBlockBuilder.addStatement(",")

        codeBlockBuilder.add("label = ")
        codeBlockBuilder.add(label.transformedCodeBlock(project, context, dryRun = dryRun))
        codeBlockBuilder.addStatement(",")

        val canvasEditable = project.findCanvasEditableHavingNodeOrNull(node)
        val writeState = selectedItem?.findReadableState(project, canvasEditable, node)
        codeBlockBuilder.addStatement("onValueChange = {")
        if (writeState != null && canvasEditable != null && writeState is WriteableState) {
            codeBlockBuilder.add(
                writeState.generateWriteBlock(
                    project,
                    canvasEditable,
                    context,
                    dryRun = dryRun,
                ),
            )
        }
        if (node.actionsMap[ActionType.OnChange]?.isNotEmpty() == true) {
            node.actionsMap[ActionType.OnChange]?.forEach {
                codeBlockBuilder.add(it.generateCodeBlock(project, context, dryRun = dryRun))
            }
        }
        codeBlockBuilder.addStatement("},")

        selectedItem?.let {
            codeBlockBuilder.add("selectedItem = ")
            codeBlockBuilder.add(
                selectedItem.transformedCodeBlock(
                    project,
                    context,
                    ComposeFlowType.StringType(),
                    dryRun = dryRun,
                ),
            )
            codeBlockBuilder.addStatement(",")
        }
        return codeBlockBuilder.build()
    }

    override fun defaultComposeNode(project: Project): ComposeNode = ComposeNode(trait = mutableStateOf(DropdownTrait()))

    override fun icon(): ImageVector = Icons.Outlined.FormatListNumbered

    override fun iconText(): String = "Dropdown"

    override fun paletteCategories(): List<TraitCategory> = listOf(TraitCategory.Basic)

    override fun tooltipResource(): StringResource = Res.string.tooltip_dropdown_trait

    override fun isResizeable(): Boolean = false

    override fun actionTypes(): List<ActionType> = listOf(ActionType.OnChange)

    override fun companionState(composeNode: ComposeNode): ScreenState<*> =
        ScreenState.StringScreenState(
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
            this.copy(selectedItem = ValueFromCompanionState(node))
    }

    override fun updateCompanionStateProperties(composeNode: ComposeNode) {
        if (selectedItem is ValueFromCompanionState) {
            composeNode.trait.value = this.copy(selectedItem = ValueFromCompanionState(composeNode))
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
        val items =
            when (val items = items) {
                is CustomEnumValuesProperty -> {
                    items.rawVale(project)
                }

                null -> listOf("")
                else -> listOf(items.transformedValueExpression(project))
            }
        var selectedText by remember(node.id) { mutableStateOf("") }
        DropdownMenuTextField(
            items = items,
            label = label.transformedValueExpression(project),
            onValueChange = {
                selectedText = it
            },
            selectedItem = selectedText,
            modifier =
                modifier.then(
                    node
                        .modifierChainForCanvas()
                        .modifierForCanvas(
                            project = project,
                            node = node,
                            canvasNodeCallbacks = canvasNodeCallbacks,
                            zoomableContainerStateHolder = zoomableContainerStateHolder,
                            paletteRenderParams = paletteRenderParams,
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
            MemberNameWrapper.get("io.composeflow.ui.textfield", "DropdownMenuTextField"),
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

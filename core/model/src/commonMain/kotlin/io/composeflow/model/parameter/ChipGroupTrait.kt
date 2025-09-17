package io.composeflow.model.parameter

import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.composeflow.Res
import io.composeflow.custom.ComposeFlowIcons
import io.composeflow.custom.composeflowicons.Choice
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.kotlinpoet.MemberHolder
import io.composeflow.kotlinpoet.wrapper.CodeBlockBuilderWrapper
import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper
import io.composeflow.kotlinpoet.wrapper.MemberNameWrapper
import io.composeflow.model.action.ActionType
import io.composeflow.model.modifier.ModifierWrapper
import io.composeflow.model.modifier.generateCode
import io.composeflow.model.modifier.generateModifierCode
import io.composeflow.model.palette.PaletteRenderParams
import io.composeflow.model.palette.TraitCategory
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.project.findCanvasEditableHavingNodeOrNull
import io.composeflow.model.project.findLocalStateOrNull
import io.composeflow.model.property.AssignableProperty
import io.composeflow.model.property.CustomEnumValuesProperty
import io.composeflow.model.property.FromString
import io.composeflow.model.property.PropertyContainer
import io.composeflow.model.property.StringProperty
import io.composeflow.model.state.ScreenState
import io.composeflow.model.state.WriteableState
import io.composeflow.model.type.ComposeFlowType
import io.composeflow.tooltip_chip_group_trait
import io.composeflow.ui.CanvasNodeCallbacks
import io.composeflow.ui.modifierForCanvas
import io.composeflow.ui.zoomablecontainer.ZoomableContainerStateHolder
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource

@Serializable
@SerialName("ChipGroupTrait")
data class ChipGroupTrait(
    val chipItems: AssignableProperty? =
        StringProperty
            .StringIntrinsicValue("Chip1,Chip2,Chip3")
            .apply {
                propertyTransformers.add(
                    FromString.ToStringList.Split(
                        mutableStateOf(
                            StringProperty.StringIntrinsicValue(
                                ",",
                            ),
                        ),
                    ),
                )
            },
    val selectable: Boolean = true,
    val multiSelect: Boolean = false,
    val elevated: Boolean = false,
    // TODO: Temporarily disable the wrapContent since it may introduce nested horizontally scrollable
    // containers issue
    // val wrapContent: Boolean = false,
) : ComposeTrait {
    override fun getPropertyContainers(): List<PropertyContainer> =
        listOf(
            PropertyContainer("Chip items", chipItems, ComposeFlowType.StringType(isList = true)),
        )

    override fun defaultComposeNode(project: Project): ComposeNode {
        val chipGroup =
            ComposeNode(
                modifierList = defaultModifierList(),
                trait = mutableStateOf(ChipGroupTrait()),
            )
        return chipGroup
    }

    override fun companionState(composeNode: ComposeNode): ScreenState<*> =
        ScreenState.StringListScreenState(
            id = composeNode.companionStateId,
            name = composeNode.labelExposed,
            companionNodeId = composeNode.id,
        )

    override fun icon(): ImageVector = ComposeFlowIcons.Choice

    override fun iconText(): String = "ChipGroup"

    override fun paletteCategories(): List<TraitCategory> = listOf(TraitCategory.Basic, TraitCategory.Container)

    override fun tooltipResource(): StringResource = Res.string.tooltip_chip_group_trait

    override fun isDroppable(): Boolean = false

    override fun isResizeable(): Boolean = false

    override fun actionTypes(): List<ActionType> = listOf(ActionType.OnChange)

    @Composable
    override fun RenderedNode(
        project: Project,
        node: ComposeNode,
        canvasNodeCallbacks: CanvasNodeCallbacks,
        paletteRenderParams: PaletteRenderParams,
        zoomableContainerStateHolder: ZoomableContainerStateHolder,
        modifier: Modifier,
    ) {
        val selectedChipItems = remember(node.id) { mutableStateListOf<String>() }

        @Composable
        fun LocalChip(
            label: String = "",
            chipGroupTrait: ChipGroupTrait,
        ) {
            if (chipGroupTrait.selectable) {
                FilterChip(
                    selected = selectedChipItems.contains(label),
                    onClick = {
                        if (chipGroupTrait.multiSelect) {
                            if (selectedChipItems.contains(label)) {
                                selectedChipItems.remove(label)
                            } else {
                                selectedChipItems.add(label)
                            }
                        } else {
                            if (selectedChipItems.isEmpty()) {
                                selectedChipItems.add(label)
                            } else {
                                selectedChipItems.clear()
                                selectedChipItems.add(label)
                            }
                        }
                    },
                    label = {
                        Text(label)
                    },
                    colors =
                        if (chipGroupTrait.elevated) {
                            FilterChipDefaults.elevatedFilterChipColors()
                        } else {
                            FilterChipDefaults.filterChipColors()
                        },
                    elevation =
                        if (chipGroupTrait.elevated) {
                            FilterChipDefaults.elevatedFilterChipElevation()
                        } else {
                            FilterChipDefaults.filterChipElevation()
                        },
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            } else {
                AssistChip(
                    onClick = {},
                    label = {
                        Text(label)
                    },
                    colors =
                        if (chipGroupTrait.elevated) {
                            AssistChipDefaults.elevatedAssistChipColors()
                        } else {
                            AssistChipDefaults.assistChipColors()
                        },
                    elevation =
                        if (chipGroupTrait.elevated) {
                            AssistChipDefaults.elevatedAssistChipElevation()
                        } else {
                            AssistChipDefaults.elevatedAssistChipElevation()
                        },
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
        }

        val chipItems =
            when (val chipItems = chipItems) {
                is CustomEnumValuesProperty -> {
                    chipItems.rawVale(project)
                }

                null -> listOf("[Empty item]")
                else -> listOf(chipItems.transformedValueExpression(project))
            }
        FlowRow(
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
        ) {
            chipItems.forEachIndexed { _, item ->
                LocalChip(
                    label = item,
                    chipGroupTrait = node.trait.value as ChipGroupTrait,
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
        val chipGroupTrait = node.trait.value as ChipGroupTrait

        val companionState =
            node.companionStateId.let {
                project.findLocalStateOrNull(it)
            }
        val updatedState =
            if (companionState is ScreenState.StringListScreenState) {
                companionState.copy(singleValueOnly = !multiSelect)
            } else {
                companionState
            }
        val canvasEditable = project.findCanvasEditableHavingNodeOrNull(node)!!
        (updatedState as? WriteableState)?.let {
            canvasEditable.updateState(updatedState)
        }

        val itemsCodeBlock =
            when (val items = chipItems) {
                null -> {
                    CodeBlockWrapper.of("emptyList<String>()")
                }

                else -> {
                    items.transformedCodeBlock(
                        project,
                        context,
                        dryRun = dryRun,
                        writeType = ComposeFlowType.StringType(isList = true),
                    )
                }
            }

        fun generateChipCode(
            groupParams: ChipGroupTrait,
            builder: CodeBlockBuilderWrapper,
            dryRun: Boolean,
        ) {
            val chipMember =
                if (groupParams.selectable) {
                    MemberNameWrapper.get("androidx.compose.material3", "FilterChip")
                } else {
                    MemberNameWrapper.get("androidx.compose.material3", "AssistChip")
                }
            builder.add("%M(", chipMember)
            if (updatedState is WriteableState) {
                builder.addStatement(
                    "selected = ${
                        updatedState.getReadVariableName(
                            project,
                            context,
                        )
                    }.contains(it),",
                )
            } else {
                builder.addStatement("selected = false,")
            }
            builder.addStatement("label = { %M(it) },", MemberHolder.Material3.Text)
            builder.addStatement("onClick = {")
            if (node.actionsMap[ActionType.OnChange]?.isNotEmpty() == true) {
                node.actionsMap[ActionType.OnChange]?.forEach {
                    builder.add(it.generateCodeBlock(project, context, dryRun = dryRun))
                }
            }
            if (updatedState is WriteableState) {
                builder.add(
                    updatedState.generateWriteBlock(
                        project,
                        canvasEditable,
                        context,
                        dryRun = dryRun,
                    ),
                )
            }
            builder.addStatement("},")
            listOf(ModifierWrapper.Padding(4.dp)).generateCode(
                project,
                context,
                builder,
                dryRun = dryRun,
            )
            builder.add(")")
        }

        codeBlockBuilder.addStatement(
            "%M(",
            MemberNameWrapper.get("androidx.compose.foundation.layout", "FlowRow"),
        )
        codeBlockBuilder.add(
            node.generateModifierCode(project, context, dryRun = dryRun),
        )
        codeBlockBuilder.addStatement(") {")

        codeBlockBuilder.add(itemsCodeBlock)
        codeBlockBuilder.addStatement(".forEach {")
        generateChipCode(groupParams = chipGroupTrait, codeBlockBuilder, dryRun = dryRun)
        codeBlockBuilder.addStatement("}")
        codeBlockBuilder.addStatement("}")
        return codeBlockBuilder.build()
    }
}

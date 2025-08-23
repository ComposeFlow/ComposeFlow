package io.composeflow.model.parameter

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import io.composeflow.Res
import io.composeflow.custom.ComposeFlowIcons
import io.composeflow.custom.composeflowicons.DrawerMenu
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.kotlinpoet.MemberHolder
import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper
import io.composeflow.kotlinpoet.wrapper.MemberNameWrapper
import io.composeflow.materialicons.ImageVectorHolder
import io.composeflow.model.action.ActionType
import io.composeflow.model.modifier.generateModifierCode
import io.composeflow.model.palette.PaletteRenderParams
import io.composeflow.model.palette.TraitCategory
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.property.AssignableProperty
import io.composeflow.model.property.PropertyContainer
import io.composeflow.model.property.StringProperty
import io.composeflow.model.type.ComposeFlowType
import io.composeflow.tooltip_navigation_drawer_item_trait
import io.composeflow.ui.CanvasNodeCallbacks
import io.composeflow.ui.modifierForCanvas
import io.composeflow.ui.zoomablecontainer.ZoomableContainerStateHolder
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource

@Serializable
@SerialName("NavigationDrawerItemTrait")
data class NavigationDrawerItemTrait(
    val imageVectorHolder: ImageVectorHolder? = null,
    val labelProperty: AssignableProperty? = null,
) : ComposeTrait {
    override fun getPropertyContainers(): List<PropertyContainer> =
        listOf(
            PropertyContainer(
                "Text",
                labelProperty,
                ComposeFlowType.StringType(),
            ),
        )

    private fun generateParamsCode(
        project: Project,
        node: ComposeNode,
        context: GenerationContext,
        dryRun: Boolean,
    ): CodeBlockWrapper {
        val codeBlockBuilder = CodeBlockWrapper.builder()
        codeBlockBuilder.addStatement("onClick = {")
        node.actionsMap[ActionType.OnClick]?.forEach {
            codeBlockBuilder.add(it.generateCodeBlock(project, context, dryRun = dryRun))
        }
        codeBlockBuilder.addStatement("},")
        codeBlockBuilder.addStatement("selected = false,")
        codeBlockBuilder.add("label = {")
        codeBlockBuilder.add(
            """%M(text =  """,
            MemberHolder.Material3.Text,
        )
        codeBlockBuilder.add(
            labelProperty?.transformedCodeBlock(
                project,
                context,
                ComposeFlowType.StringType(),
                dryRun = dryRun,
            ) ?: CodeBlockWrapper.of(""),
        )
        codeBlockBuilder.addStatement(")")
        codeBlockBuilder.addStatement("},")

        imageVectorHolder?.let {
            codeBlockBuilder.addStatement("icon = {")
            val iconTrait =
                IconTrait(
                    assetType = IconAssetType.Material,
                    imageVectorHolder = imageVectorHolder,
                )
            codeBlockBuilder.add(
                iconTrait.generateCode(
                    project = project,
                    node = node,
                    context = context,
                    dryRun,
                ),
            )
            codeBlockBuilder.addStatement("},")
        }
        codeBlockBuilder.add(
            node.generateModifierCode(project, context, dryRun = dryRun),
        )
        return codeBlockBuilder.build()
    }

    override fun actionTypes(): List<ActionType> = listOf(ActionType.OnClick)

    override fun onClickIncludedInParams(): Boolean = true

    override fun icon(): ImageVector = ComposeFlowIcons.DrawerMenu

    override fun visibleInPalette(): Boolean = true

    override fun isDroppable(): Boolean = false

    override fun isResizeable(): Boolean = false

    override fun isEditable(): Boolean = true

    override fun iconText(): String = "Drawer Item"

    override fun paletteCategories(): List<TraitCategory> = listOf(TraitCategory.NavigationItem)

    override fun tooltipResource(): StringResource = Res.string.tooltip_navigation_drawer_item_trait

    override fun isVisibilityConditional(): Boolean = true

    override fun defaultComposeNode(project: Project): ComposeNode {
        val navDrawer =
            ComposeNode(
                label = mutableStateOf("NavigationDrawerItem"),
                trait =
                    mutableStateOf(
                        NavigationDrawerItemTrait(
                            labelProperty = StringProperty.StringIntrinsicValue("Navigation drawer item"),
                        ),
                    ),
            )
        return navDrawer
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
        NavigationDrawerItem(
            icon = {
                imageVectorHolder?.let {
                    Icon(
                        imageVector = it.imageVector,
                        contentDescription = "",
                    )
                }
            },
            selected = false,
            label = {
                labelProperty?.let {
                    Text(text = it.displayText(project))
                }
            },
            onClick = {},
            modifier =
                node
                    .modifierChainForCanvas()
                    .modifierForCanvas(
                        project = project,
                        node = node,
                        canvasNodeCallbacks = canvasNodeCallbacks,
                        paletteRenderParams = paletteRenderParams,
                        zoomableContainerStateHolder = zoomableContainerStateHolder,
                        isDraggable = true,
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
            MemberNameWrapper.get("androidx.compose.material3", "NavigationDrawerItem"),
        )
        codeBlockBuilder.add(generateParamsCode(project, node, context, dryRun))
        codeBlockBuilder.addStatement(")")
        return codeBlockBuilder.build()
    }
}

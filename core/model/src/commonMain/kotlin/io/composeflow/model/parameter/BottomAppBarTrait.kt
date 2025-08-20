package io.composeflow.model.parameter

import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper
import io.composeflow.kotlinpoet.wrapper.CodeBlockBuilderWrapper
import io.composeflow.kotlinpoet.wrapper.MemberNameWrapper
import io.composeflow.Res
import io.composeflow.custom.ComposeFlowIcons
import io.composeflow.custom.composeflowicons.BottomAppBar
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.materialicons.Outlined
import io.composeflow.model.action.ActionType
import io.composeflow.model.modifier.generateModifierCode
import io.composeflow.model.modifier.toModifierChain
import io.composeflow.model.palette.PaletteRenderParams
import io.composeflow.model.palette.TraitCategory
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.BottomAppBarNode
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.tooltip_bottom_app_bar_trait
import io.composeflow.ui.CanvasNodeCallbacks
import io.composeflow.ui.modifierForCanvas
import io.composeflow.ui.zoomablecontainer.ZoomableContainerStateHolder
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource

@Serializable
@SerialName("BottomAppBarTrait")
data class BottomAppBarTrait(
    val showFab: Boolean = true,
) : ComposeTrait {
    override fun icon(): ImageVector = ComposeFlowIcons.BottomAppBar

    override fun iconText(): String = "Bottom bar"

    override fun paletteCategories(): List<TraitCategory> = listOf(TraitCategory.Container, TraitCategory.ScreenOnly)

    override fun tooltipResource(): StringResource = Res.string.tooltip_bottom_app_bar_trait

    override fun visibleInPalette(): Boolean = true

    override fun isDroppable(): Boolean = false

    override fun isResizeable(): Boolean = false

    override fun isEditable(): Boolean = true

    override fun isVisibilityConditional(): Boolean = false

    override fun defaultComposeNode(project: Project): ComposeNode =
        ComposeNode(
            label = mutableStateOf("BottomAppBar"),
            trait =
                mutableStateOf(
                    BottomAppBarTrait(),
                ),
        ).apply {
            addChild(
                ComposeNode(
                    label = mutableStateOf("Fab"),
                    trait =
                        mutableStateOf(
                            FabTrait(imageVectorHolder = Outlined.Add),
                        ),
                ),
            )
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
        @Composable
        fun BottomAppBarIconButton(iconNode: ComposeNode) {
            IconButton(onClick = {}) {
                val iconTrait = iconNode.trait.value as IconTrait
                iconTrait.imageVectorHolder?.let {
                    Icon(
                        imageVector = it.imageVector,
                        contentDescription = null,
                        modifier =
                            iconNode
                                .modifierChainForCanvas()
                                .modifierForCanvas(
                                    project = project,
                                    node = iconNode,
                                    canvasNodeCallbacks = canvasNodeCallbacks,
                                    paletteRenderParams = paletteRenderParams,
                                    zoomableContainerStateHolder = zoomableContainerStateHolder,
                                    isDraggable = false,
                                ),
                    )
                }
            }
        }

        val bottomAppBarNode = node as BottomAppBarNode
        val fabNode = node.getBottomAppBarFab()
        BottomAppBar(
            actions = {
                bottomAppBarNode.getBottomAppBarActionIcons().forEach {
                    BottomAppBarIconButton(it)
                }
            },
            floatingActionButton =
                if (showFab) {
                    {
                        fabNode.trait.value.RenderedNode(
                            project = project,
                            node = fabNode,
                            canvasNodeCallbacks = canvasNodeCallbacks,
                            paletteRenderParams = PaletteRenderParams(),
                            zoomableContainerStateHolder = zoomableContainerStateHolder,
                            modifier =
                                fabNode
                                    .modifierList
                                    .toModifierChain(),
                        )
                    }
                } else {
                    null
                },
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
                            isDraggable = false,
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
        val bottomAppBarNode = node as BottomAppBarNode

        codeBlockBuilder.addStatement(
            "%M(",
            MemberNameWrapper.get("androidx.compose.material3", "BottomAppBar"),
        )

        fun writeIconButton(
            iconNode: ComposeNode,
            builder: CodeBlockBuilderWrapper,
        ) {
            val iconTrait = iconNode.trait.value as IconTrait
            iconTrait.imageVectorHolder?.let { imageVectorHolder ->
                val iconMember = MemberNameWrapper.get("androidx.compose.material3", "Icon")
                val iconButtonMember = MemberNameWrapper.get("androidx.compose.material3", "IconButton")
                val iconsMember = MemberNameWrapper.get("androidx.compose.material.icons", "Icons")
                val imageVectorMember =
                    MemberNameWrapper.get(
                        "androidx.compose.material.icons.${imageVectorHolder.packageDescriptor}",
                        imageVectorHolder.name,
                    )
                builder.addStatement("%M(onClick = {", iconButtonMember)
                iconNode.actionsMap[ActionType.OnClick]?.forEach {
                    builder.add(it.generateCodeBlock(project, context, dryRun = dryRun))
                }
                builder.addStatement("}) {")
                builder.addStatement(
                    "%M(imageVector = %M.${imageVectorHolder.memberDescriptor}.%M, contentDescription = null)",
                    iconMember,
                    iconsMember,
                    imageVectorMember,
                )
                builder.addStatement("}")
            }
        }

        val actionIcons = bottomAppBarNode.getBottomAppBarActionIcons()
        codeBlockBuilder.addStatement("actions = {")
        actionIcons.forEach { actionIcon ->
            val actionIconTrait = actionIcon.trait.value as IconTrait
            actionIconTrait.imageVectorHolder?.let {
                writeIconButton(actionIcon, codeBlockBuilder)
            }
        }
        codeBlockBuilder.addStatement("},")
        val fabNode = bottomAppBarNode.getBottomAppBarFab()
        if (showFab) {
            codeBlockBuilder.addStatement("floatingActionButton = {")
            codeBlockBuilder.add(
                fabNode.trait.value.generateCode(
                    project = project,
                    node = fabNode,
                    context = context,
                    dryRun = dryRun,
                ),
            )
            codeBlockBuilder.addStatement("},")
        }

        codeBlockBuilder.add(
            node.generateModifierCode(project, context, dryRun = dryRun),
        )
        codeBlockBuilder.addStatement(")")
        return codeBlockBuilder.build()
    }
}

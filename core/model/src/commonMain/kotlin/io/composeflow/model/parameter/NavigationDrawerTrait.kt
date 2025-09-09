package io.composeflow.model.parameter

import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import io.composeflow.Res
import io.composeflow.custom.ComposeFlowIcons
import io.composeflow.custom.composeflowicons.NavigationDrawer
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper
import io.composeflow.kotlinpoet.wrapper.MemberNameWrapper
import io.composeflow.model.modifier.generateModifierCode
import io.composeflow.model.palette.PaletteRenderParams
import io.composeflow.model.palette.TraitCategory
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.serializer.FallbackEnumSerializer
import io.composeflow.tooltip_navigation_drawer_trait
import io.composeflow.ui.CanvasNodeCallbacks
import io.composeflow.ui.modifierForCanvas
import io.composeflow.ui.zoomablecontainer.ZoomableContainerStateHolder
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.jetbrains.compose.resources.StringResource

@Serializable
@SerialName("NavigationDrawerTrait")
data class NavigationDrawerTrait(
    val gesturesEnabled: Boolean = true,
    val navigationDrawerType: NavigationDrawerType = NavigationDrawerType.Default,
    @Transient
    val expandedInCanvas: MutableState<Boolean> = mutableStateOf(false),
) : ComposeTrait {
    override fun icon(): ImageVector = ComposeFlowIcons.NavigationDrawer

    override fun visibleInPalette(): Boolean = true

    override fun isDroppable(): Boolean = true

    override fun isResizeable(): Boolean = false

    override fun isEditable(): Boolean = true

    override fun iconText(): String = "Nav Drawer"

    override fun paletteCategories(): List<TraitCategory> = listOf(TraitCategory.Container, TraitCategory.ScreenOnly)

    override fun tooltipResource(): StringResource = Res.string.tooltip_navigation_drawer_trait

    override fun isVisibilityConditional(): Boolean = false

    override fun defaultComposeNode(project: Project): ComposeNode {
        val navDrawer =
            ComposeNode(
                label = mutableStateOf("NavigationDrawer"),
                trait =
                    mutableStateOf(
                        NavigationDrawerTrait(
                            expandedInCanvas = mutableStateOf(true),
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
        ModalDrawerSheet(
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
        codeBlockBuilder.add(
            CodeBlockWrapper.of(
                "%M(",
                MemberNameWrapper.get("androidx.compose.material3", "ModalDrawerSheet"),
            ),
        )
        codeBlockBuilder.add(
            node.generateModifierCode(project, context, dryRun = dryRun),
        )
        codeBlockBuilder.addStatement(") {")
        node.children.forEach { child ->
            codeBlockBuilder.add(
                child.generateCode(project, context, dryRun = dryRun),
            )
        }
        codeBlockBuilder.addStatement("}")
        return codeBlockBuilder.build()
    }
}

object NavigationDrawerTypeSerializer :
    FallbackEnumSerializer<NavigationDrawerType>(NavigationDrawerType::class, NavigationDrawerType.entries.toTypedArray())

@Serializable(NavigationDrawerTypeSerializer::class)
enum class NavigationDrawerType {
    Default {
        override fun toMemberName(): MemberNameWrapper = MemberNameWrapper.get("androidx.compose.material3", "ModalNavigationDrawer")
    },
    Dismissible {
        override fun toMemberName(): MemberNameWrapper = MemberNameWrapper.get("androidx.compose.material3", "DismissibleNavigationDrawer")
    },
    ;

    abstract fun toMemberName(): MemberNameWrapper
}

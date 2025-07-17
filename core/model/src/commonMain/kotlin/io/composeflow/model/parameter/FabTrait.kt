package io.composeflow.model.parameter

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.FloatingActionButtonDefaults.containerColor
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.MemberName
import io.composeflow.Res
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.materialicons.ImageVectorHolder
import io.composeflow.materialicons.Outlined
import io.composeflow.materialicons.asCodeBlock
import io.composeflow.model.action.ActionType
import io.composeflow.model.modifier.generateModifierCode
import io.composeflow.model.palette.PaletteRenderParams
import io.composeflow.model.palette.TraitCategory
import io.composeflow.model.parameter.wrapper.ColorWrapper
import io.composeflow.model.parameter.wrapper.Material3ColorWrapper
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.property.AssignableProperty
import io.composeflow.model.property.ColorProperty
import io.composeflow.model.property.PropertyContainer
import io.composeflow.model.type.ComposeFlowType
import io.composeflow.serializer.FallbackEnumSerializer
import io.composeflow.tooltip_fab_trait
import io.composeflow.ui.CanvasNodeCallbacks
import io.composeflow.ui.modifierForCanvas
import io.composeflow.ui.zoomablecontainer.ZoomableContainerStateHolder
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource

@Serializable
@SerialName("FabTrait")
data class FabTrait(
    val imageVectorHolder: ImageVectorHolder = Outlined.Add,
    val contentDescription: String = "Floating Action Button for ${imageVectorHolder.name}",
    val fabPositionWrapper: FabPositionWrapper? = null,
    val containerColorWrapper: AssignableProperty? =
        ColorProperty.ColorIntrinsicValue(
            value =
                ColorWrapper(
                    themeColor = Material3ColorWrapper.PrimaryContainer,
                ),
        ),
    val contentColorWrapper: AssignableProperty? =
        ColorProperty.ColorIntrinsicValue(
            value =
                ColorWrapper(
                    themeColor = Material3ColorWrapper.Primary,
                ),
        ),
    val fabElevationWrapper: FabElevationWrapper? = null,
    val fabType: FabType = FabType.Default,
) : ComposeTrait {
    override fun getPropertyContainers(): List<PropertyContainer> =
        listOf(
            PropertyContainer("Container color", containerColorWrapper, ComposeFlowType.Color()),
            PropertyContainer("Content color", contentColorWrapper, ComposeFlowType.Color()),
        )

    override fun icon(): ImageVector = Icons.Outlined.AddCircle

    override fun iconText(): String = "Fab"

    override fun paletteCategories(): List<TraitCategory> = listOf(TraitCategory.ScreenOnly)

    override fun tooltipResource(): StringResource = Res.string.tooltip_fab_trait

    override fun visibleInPalette(): Boolean = true

    override fun isResizeable(): Boolean = false

    override fun actionTypes(): List<ActionType> = listOf(ActionType.OnClick)

    override fun onClickIncludedInParams(): Boolean = true

    override fun isVisibilityConditional(): Boolean = false

    override fun isEditable(): Boolean = true

    override fun defaultComposeNode(project: Project): ComposeNode =
        ComposeNode(
            trait = mutableStateOf(FabTrait(imageVectorHolder = Outlined.Add)),
            label = mutableStateOf("Fab"),
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
        when (fabType) {
            FabType.Default -> {
                FloatingActionButton(
                    onClick = {},
                    containerColor =
                        (containerColorWrapper as? ColorProperty.ColorIntrinsicValue)?.value?.getColor()
                            ?: containerColor,
                    contentColor =
                        (contentColorWrapper as? ColorProperty.ColorIntrinsicValue)?.value?.getColor()
                            ?: contentColorFor(containerColor),
                    elevation =
                        when (fabElevationWrapper) {
                            FabElevationWrapper.Default -> FloatingActionButtonDefaults.elevation()
                            FabElevationWrapper.Lowered -> FloatingActionButtonDefaults.loweredElevation()
                            null -> FloatingActionButtonDefaults.elevation()
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
                ) {
                    Icon(
                        imageVector = imageVectorHolder.imageVector,
                        contentDescription = contentDescription,
                    )
                }
            }

            FabType.Small -> {
                SmallFloatingActionButton(
                    onClick = {},
                    containerColor =
                        (containerColorWrapper as? ColorProperty.ColorIntrinsicValue)?.value?.getColor()
                            ?: containerColor,
                    contentColor =
                        (contentColorWrapper as? ColorProperty.ColorIntrinsicValue)?.value?.getColor()
                            ?: contentColorFor(containerColor),
                    elevation =
                        when (fabElevationWrapper) {
                            FabElevationWrapper.Default -> FloatingActionButtonDefaults.elevation()
                            FabElevationWrapper.Lowered -> FloatingActionButtonDefaults.loweredElevation()
                            null -> FloatingActionButtonDefaults.elevation()
                        },
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
                                    isDraggable = false,
                                ),
                        ),
                ) {
                    Icon(
                        imageVector = imageVectorHolder.imageVector,
                        contentDescription = contentDescription,
                    )
                }
            }

            FabType.Large -> {
                LargeFloatingActionButton(
                    onClick = {},
                    containerColor =
                        (containerColorWrapper as? ColorProperty.ColorIntrinsicValue)?.value?.getColor()
                            ?: containerColor,
                    contentColor =
                        (contentColorWrapper as? ColorProperty.ColorIntrinsicValue)?.value?.getColor()
                            ?: contentColorFor(containerColor),
                    elevation =
                        when (fabElevationWrapper) {
                            FabElevationWrapper.Default -> FloatingActionButtonDefaults.elevation()
                            FabElevationWrapper.Lowered -> FloatingActionButtonDefaults.loweredElevation()
                            null -> FloatingActionButtonDefaults.elevation()
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
                ) {
                    Icon(
                        imageVector = imageVectorHolder.imageVector,
                        contentDescription = contentDescription,
                    )
                }
            }

            FabType.Extended -> {
                ExtendedFloatingActionButton(
                    onClick = {},
                    containerColor =
                        (containerColorWrapper as? ColorProperty.ColorIntrinsicValue)?.value?.getColor()
                            ?: containerColor,
                    contentColor =
                        (contentColorWrapper as? ColorProperty.ColorIntrinsicValue)?.value?.getColor()
                            ?: contentColorFor(containerColor),
                    elevation =
                        when (fabElevationWrapper) {
                            FabElevationWrapper.Default -> FloatingActionButtonDefaults.elevation()
                            FabElevationWrapper.Lowered -> FloatingActionButtonDefaults.loweredElevation()
                            null -> FloatingActionButtonDefaults.elevation()
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
                ) {
                    Icon(
                        imageVector = imageVectorHolder.imageVector,
                        contentDescription = contentDescription,
                    )
                }
            }
        }
    }

    override fun generateCode(
        project: Project,
        node: ComposeNode,
        context: GenerationContext,
        dryRun: Boolean,
    ): CodeBlock {
        val codeBlockBuilder = CodeBlock.builder()
        codeBlockBuilder.addStatement(
            "%M(",
            fabType.toMemberName(),
        )
        codeBlockBuilder.addStatement("onClick = {")
        node.actionsMap[ActionType.OnClick]?.forEach {
            codeBlockBuilder.add(it.generateCodeBlock(project, context, dryRun = dryRun))
        }
        codeBlockBuilder.addStatement("},")
        containerColorWrapper?.let {
            codeBlockBuilder.add("containerColor = ")
            codeBlockBuilder.add(
                it.transformedCodeBlock(
                    project,
                    context,
                    ComposeFlowType.Color(),
                    dryRun = dryRun,
                ),
            )
            codeBlockBuilder.addStatement(",")
        }
        contentColorWrapper?.let {
            codeBlockBuilder.add("contentColor = ")
            codeBlockBuilder.add(
                it.transformedCodeBlock(
                    project,
                    context,
                    ComposeFlowType.Color(),
                    dryRun = dryRun,
                ),
            )
            codeBlockBuilder.addStatement(",")
        }
        fabElevationWrapper?.let {
            val elevationMember =
                when (it) {
                    FabElevationWrapper.Default ->
                        MemberName(
                            "androidx.compose.material3.FloatingActionButtonDefaults",
                            "elevation",
                        )

                    FabElevationWrapper.Lowered ->
                        MemberName(
                            "androidx.compose.material3.FloatingActionButtonDefaults",
                            "loweredElevation",
                        )
                }
            codeBlockBuilder.addStatement("elevation = %M(),", elevationMember)
        }
        codeBlockBuilder.add(
            node.generateModifierCode(project, context, dryRun = dryRun),
        )
        codeBlockBuilder.addStatement(") {")

        codeBlockBuilder.addStatement("%M(", MemberName("androidx.compose.material3", "Icon"))
        codeBlockBuilder.addStatement("imageVector = ")
        codeBlockBuilder.add(imageVectorHolder.asCodeBlock())
        codeBlockBuilder.addStatement(",")
        codeBlockBuilder.addStatement("""contentDescription = "$contentDescription",""")
        codeBlockBuilder.addStatement(")")
        codeBlockBuilder.addStatement("}")
        return codeBlockBuilder.build()
    }
}

object FabElevationWrapperSerializer : FallbackEnumSerializer<FabElevationWrapper>(
    FabElevationWrapper::class,
)

@Serializable(FabElevationWrapperSerializer::class)
enum class FabElevationWrapper {
    Default,
    Lowered,
}

object FabPositionWrapperSerializer : FallbackEnumSerializer<FabPositionWrapper>(
    FabPositionWrapper::class,
)

@Serializable(FabPositionWrapperSerializer::class)
enum class FabPositionWrapper {
    End,
    Center,
}

object FabTypeSerializer : FallbackEnumSerializer<FabType>(FabType::class)

@Serializable(FabTypeSerializer::class)
enum class FabType {
    Default {
        override fun toMemberName(): MemberName = MemberName("androidx.compose.material3", "FloatingActionButton")
    },
    Small {
        override fun toMemberName(): MemberName = MemberName("androidx.compose.material3", "SmallFloatingActionButton")
    },
    Large {
        override fun toMemberName(): MemberName = MemberName("androidx.compose.material3", "LargeFloatingActionButton")
    },
    Extended {
        override fun toMemberName(): MemberName = MemberName("androidx.compose.material3", "ExtendedFloatingActionButton")
    }, ;

    abstract fun toMemberName(): MemberName
}

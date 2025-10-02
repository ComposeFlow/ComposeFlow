package io.composeflow.model.parameter

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import io.composeflow.Res
import io.composeflow.asVariableName
import io.composeflow.auth.LocalFirebaseIdToken
import io.composeflow.cloud.storage.BlobInfoWrapper
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.kotlinpoet.MemberHolder
import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper
import io.composeflow.kotlinpoet.wrapper.MemberNameWrapper
import io.composeflow.materialicons.ImageVectorHolder
import io.composeflow.materialicons.Outlined
import io.composeflow.materialicons.asCodeBlock
import io.composeflow.model.palette.PaletteRenderParams
import io.composeflow.model.palette.TraitCategory
import io.composeflow.model.parameter.wrapper.ColorWrapper
import io.composeflow.model.parameter.wrapper.Material3ColorWrapper
import io.composeflow.model.project.COMPOSEFLOW_PACKAGE
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.project.issue.Issue
import io.composeflow.model.property.AssignableProperty
import io.composeflow.model.property.ColorProperty
import io.composeflow.model.property.PropertyContainer
import io.composeflow.model.property.StringProperty
import io.composeflow.model.type.ComposeFlowType
import io.composeflow.serializer.FallbackEnumSerializer
import io.composeflow.tooltip_icon_trait
import io.composeflow.ui.CanvasNodeCallbacks
import io.composeflow.ui.modifierForCanvas
import io.composeflow.ui.utils.asIconComposable
import io.composeflow.ui.zoomablecontainer.ZoomableContainerStateHolder
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.jetbrains.compose.resources.StringResource

@Serializable
abstract class AbstractIconTrait(
    @Transient
    open val assetType: IconAssetType = IconAssetType.Material,
    /**
     * Nullable because it's better to express an Icon with a nullable state.
     * E.g. The navigation icon in the TopAppBar
     */
    @Transient
    open val imageVectorHolder: ImageVectorHolder? = Outlined.Add,
    /**
     * Effective only when [IconAssetType] is [IconAssetType.CustomAsset]
     */
    @Transient
    open val blobInfoWrapper: BlobInfoWrapper? = null,
    @Transient
    open val contentDescription: AssignableProperty =
        StringProperty.StringIntrinsicValue(value = "Icon for ${imageVectorHolder?.name}"),
    @Transient
    open val tint: AssignableProperty? =
        ColorProperty.ColorIntrinsicValue(
            value =
                ColorWrapper(
                    themeColor = Material3ColorWrapper.OnSurface,
                ),
        ),
) : ComposeTrait {
    override fun getPropertyContainers(): List<PropertyContainer> =
        listOf(
            PropertyContainer("Tint", tint, ComposeFlowType.Color()),
        )

    override fun generateIssues(project: Project): List<Issue> =
        buildList {
            when (assetType) {
                IconAssetType.CustomAsset -> {
                    if (!project.assetHolder.icons.any { it.blobId.name == blobInfoWrapper?.blobId?.name }) {
                        add(Issue.InvalidAssetReference())
                    }
                }

                else -> {}
            }
        }

    override fun defaultComposeNode(project: Project): ComposeNode =
        ComposeNode(
            trait = mutableStateOf(defaultTrait()),
            modifierList = defaultModifierList(),
        )

    override fun icon(): ImageVector = Icons.Outlined.Add

    override fun iconText(): String = "Icon"

    override fun tooltipResource(): StringResource = Res.string.tooltip_icon_trait

    override fun isResizeable(): Boolean = false

    @Composable
    override fun RenderedNode(
        project: Project,
        node: ComposeNode,
        canvasNodeCallbacks: CanvasNodeCallbacks,
        paletteRenderParams: PaletteRenderParams,
        zoomableContainerStateHolder: ZoomableContainerStateHolder,
        modifier: Modifier,
    ) {
        // Technically IconButton and Icon are different Composables, but to simplify, we automatically
        // wrap it with IconButton if any click handler is set or IconButtonTrait is used as the
        // trait otherwise render it as Icon composable
        if (node.actionHandler.allActionNodes().isNotEmpty() || this is IconButtonTrait) {
            IconButton(onClick = {}) {
                RenderIcon(
                    project,
                    node,
                    canvasNodeCallbacks,
                    paletteRenderParams,
                    zoomableContainerStateHolder,
                    modifier,
                )
            }
        } else {
            RenderIcon(
                project,
                node,
                canvasNodeCallbacks,
                paletteRenderParams,
                zoomableContainerStateHolder,
                modifier,
            )
        }
    }

    @Composable
    private fun RenderIcon(
        project: Project,
        node: ComposeNode,
        canvasNodeCallbacks: CanvasNodeCallbacks,
        paletteRenderParams: PaletteRenderParams,
        zoomableContainerStateHolder: ZoomableContainerStateHolder,
        modifier: Modifier,
    ) {
        when (assetType) {
            IconAssetType.Material -> {
                imageVectorHolder?.let {
                    Icon(
                        imageVector = it.imageVector,
                        contentDescription = contentDescription.valueExpression(project),
                        tint =
                            (tint as? ColorProperty.ColorIntrinsicValue)?.value?.getColor()
                                ?: MaterialTheme.colorScheme.onBackground,
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
            }

            IconAssetType.CustomAsset -> {
                val userId = LocalFirebaseIdToken.current.user_id
                blobInfoWrapper?.asIconComposable(
                    userId = userId,
                    projectId = project.id,
                    tint =
                        (tint as? ColorProperty.ColorIntrinsicValue)?.value?.getColor()
                            ?: MaterialTheme.colorScheme.onBackground,
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
        }
    }

    fun generateIconParamsCode(
        project: Project,
        context: GenerationContext,
        dryRun: Boolean,
    ): CodeBlockWrapper {
        val codeBlockBuilder = CodeBlockWrapper.builder()
        when (assetType) {
            IconAssetType.Material -> {
                imageVectorHolder?.let { holder ->
                    codeBlockBuilder.add("imageVector = ")
                    codeBlockBuilder.add(holder.asCodeBlock())
                    codeBlockBuilder.addStatement(",")
                    codeBlockBuilder.add("contentDescription = ")
                    codeBlockBuilder.add(
                        contentDescription.transformedCodeBlock(
                            project,
                            context,
                            ComposeFlowType.StringType(),
                            dryRun = dryRun,
                        ),
                    )
                    codeBlockBuilder.addStatement(",")
                    tint?.let {
                        codeBlockBuilder.add("tint = ")
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
                }
            }

            IconAssetType.CustomAsset -> {
                blobInfoWrapper?.let { blob ->
                    if (blob.fileName.endsWith(".xml")) {
                        // Vector drawables
                        codeBlockBuilder.add(
                            CodeBlockWrapper.of(
                                "imageVector = %M(%M.drawable.%M),",
                                MemberHolder.JetBrains.vectorResource,
                                MemberHolder.ComposeFlow.Res,
                                MemberNameWrapper.get(
                                    COMPOSEFLOW_PACKAGE,
                                    blob.fileName.asVariableName().removeSuffix(".xml"),
                                ),
                            ),
                        )
                    } else {
                        codeBlockBuilder.add(
                            CodeBlockWrapper.of(
                                "bitmap = %M(%M.drawable.%M),",
                                MemberHolder.JetBrains.imageResource,
                                MemberHolder.ComposeFlow.Res,
                                MemberNameWrapper.get(
                                    COMPOSEFLOW_PACKAGE,
                                    blob.fileName.asVariableName().substringBeforeLast("."),
                                ),
                            ),
                        )
                    }
                    tint?.let {
                        codeBlockBuilder.add("tint = ")
                        codeBlockBuilder.add(
                            it.transformedCodeBlock(
                                project,
                                context,
                                ComposeFlowType.Color(),
                                dryRun,
                            ),
                        )
                        codeBlockBuilder.addStatement(",")
                    }
                    codeBlockBuilder.addStatement("""contentDescription = "",""")
                }
            }
        }
        return codeBlockBuilder.build()
    }

    override fun paletteCategories(): List<TraitCategory> = listOf(TraitCategory.Common, TraitCategory.Basic)

    companion object {
        fun defaultTrait(): IconTrait = IconTrait(imageVectorHolder = Outlined.Add)
    }
}

object IconAssetTypeSerializer :
    FallbackEnumSerializer<IconAssetType>(IconAssetType::class, IconAssetType.entries.toTypedArray())

@Serializable(IconAssetTypeSerializer::class)
enum class IconAssetType {
    /**
     * Built-in material icons
     */
    Material,

    /**
     * User defined custom icons
     */
    CustomAsset,
}

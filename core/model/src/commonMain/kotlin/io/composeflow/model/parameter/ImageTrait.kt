package io.composeflow.model.parameter

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.shimmer
import io.composeflow.Res
import io.composeflow.asVariableName
import io.composeflow.auth.LocalFirebaseIdToken
import io.composeflow.cloud.storage.BlobInfoWrapper
import io.composeflow.image_from_network
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.kotlinpoet.MemberHolder
import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper
import io.composeflow.kotlinpoet.wrapper.MemberNameWrapper
import io.composeflow.model.enumwrapper.ContentScaleWrapper
import io.composeflow.model.modifier.ModifierWrapper
import io.composeflow.model.modifier.generateModifierCode
import io.composeflow.model.modifier.toModifierChain
import io.composeflow.model.palette.PaletteRenderParams
import io.composeflow.model.palette.TraitCategory
import io.composeflow.model.parameter.wrapper.AlignmentWrapper
import io.composeflow.model.project.COMPOSEFLOW_PACKAGE
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.property.AssignableProperty
import io.composeflow.model.property.EnumProperty
import io.composeflow.model.property.PropertyContainer
import io.composeflow.model.property.StringProperty
import io.composeflow.model.type.ComposeFlowType
import io.composeflow.override.mutableStateListEqualsOverrideOf
import io.composeflow.serializer.FallbackEnumSerializer
import io.composeflow.testing.isTest
import io.composeflow.tooltip_image_trait
import io.composeflow.ui.CanvasNodeCallbacks
import io.composeflow.ui.image.AsyncImage
import io.composeflow.ui.modifierForCanvas
import io.composeflow.ui.utils.asImageComposable
import io.composeflow.ui.zoomablecontainer.ZoomableContainerStateHolder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Custom serializer for contentScaleWrapper that falls back to EnumProperty(ContentScaleWrapper.Crop)
 * when serialization/deserialization fails for any reason.
 */
object ContentScaleWrapperFallbackSerializer : KSerializer<AssignableProperty?> {
    private val delegateSerializer = AssignableProperty.serializer().nullable

    override val descriptor: SerialDescriptor = delegateSerializer.descriptor

    override fun serialize(
        encoder: Encoder,
        value: AssignableProperty?,
    ) {
        try {
            delegateSerializer.serialize(encoder, value)
        } catch (e: Exception) {
            // If serialization fails, serialize the fallback value
            delegateSerializer.serialize(encoder, EnumProperty(ContentScaleWrapper.Crop))
        }
    }

    override fun deserialize(decoder: Decoder): AssignableProperty? =
        try {
            delegateSerializer.deserialize(decoder)
        } catch (e: Exception) {
            // If deserialization fails, return the fallback value
            EnumProperty(ContentScaleWrapper.Crop)
        }
}

@Serializable
@SerialName("ImageTrait")
data class ImageTrait(
    val assetType: ImageAssetType = ImageAssetType.Network,
    /**
     * The url of the image effective only when [assetType] is [ImageAssetType.Network]
     */
    val url: AssignableProperty = StringProperty.StringIntrinsicValue(DEFAULT_URL),
    /**
     * The asset information of the image effective only when [assetType] is [ImageAssetType.Asset]
     */
    val blobInfoWrapper: BlobInfoWrapper? = null,
    val placeholderUrl: PlaceholderUrl = PlaceholderUrl.NoUsage,
    val alignmentWrapper: AlignmentWrapper? = null,
    @Serializable(with = ContentScaleWrapperFallbackSerializer::class)
    val contentScaleWrapper: AssignableProperty? = null,
    val alpha: Float? = null,
) : ComposeTrait {
    override fun getPropertyContainers(): List<PropertyContainer> =
        listOf(
            PropertyContainer("Url", url, ComposeFlowType.StringType()),
            PropertyContainer(
                "Content scale",
                contentScaleWrapper,
                ComposeFlowType.Enum(
                    isList = false,
                    enumClass = ContentScaleWrapper::class,
                ),
            ),
        )

    fun contentScaleValue(): ContentScale? =
        when (contentScaleWrapper) {
            is EnumProperty -> {
                ContentScaleWrapper.entries[contentScaleWrapper.value.enumValue().ordinal].contentScale
            }

            else -> null
        }

    private fun generateParamsCode(
        project: Project,
        context: GenerationContext,
        dryRun: Boolean,
    ): CodeBlockWrapper {
        val codeBlockBuilder = CodeBlockWrapper.builder()
        when (assetType) {
            ImageAssetType.Network -> {
                codeBlockBuilder.add("url = ")
                codeBlockBuilder.add(
                    url.transformedCodeBlock(
                        project,
                        context,
                        ComposeFlowType.StringType(),
                        dryRun = dryRun,
                    ),
                )
                codeBlockBuilder.addStatement(",")
            }

            ImageAssetType.Asset -> {
                blobInfoWrapper?.let { blob ->
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
            }
        }

        codeBlockBuilder.addStatement("""contentDescription = "",""")
        alignmentWrapper?.let {
            val alignmentMember = MemberNameWrapper.get("androidx.compose.ui", "Alignment")
            codeBlockBuilder.addStatement("alignment = %M.${it.name},", alignmentMember)
        }
        contentScaleWrapper?.let {
            codeBlockBuilder.add("contentScale = ")
            codeBlockBuilder.add(it.transformedCodeBlock(project, context, dryRun = dryRun))
            codeBlockBuilder.addStatement(",")
        }
        alpha?.let {
            codeBlockBuilder.addStatement("alpha = ${it}f,")
        }
        return codeBlockBuilder.build()
    }

    override fun defaultComposeNode(project: Project): ComposeNode =
        ComposeNode(
            modifierList = defaultModifierList(),
            trait = mutableStateOf(ImageTrait(url = StringProperty.StringIntrinsicValue(DEFAULT_URL))),
        )

    override fun defaultModifierList(): MutableList<ModifierWrapper> =
        mutableStateListEqualsOverrideOf(
            ModifierWrapper.Padding(all = 8.dp),
            ModifierWrapper.Width(width = 160.dp),
            ModifierWrapper.Height(height = 160.dp),
        )

    override fun icon(): ImageVector = Icons.Outlined.Image

    override fun iconText(): String = "Image"

    override fun paletteCategories(): List<TraitCategory> = listOf(TraitCategory.Basic)

    override fun tooltipResource(): StringResource = Res.string.tooltip_image_trait

    @Composable
    override fun RenderedNode(
        project: Project,
        node: ComposeNode,
        canvasNodeCallbacks: CanvasNodeCallbacks,
        paletteRenderParams: PaletteRenderParams,
        zoomableContainerStateHolder: ZoomableContainerStateHolder,
        modifier: Modifier,
    ) {
        val modifierForCanvas =
            if (paletteRenderParams.isShadowNode) {
                Modifier
            } else {
                Modifier.modifierForCanvas(
                    project = project,
                    node = node,
                    canvasNodeCallbacks = canvasNodeCallbacks,
                    paletteRenderParams = paletteRenderParams,
                    zoomableContainerStateHolder = zoomableContainerStateHolder,
                )
            }
        if (isTest()) {
            LoadedImage(
                painter = rememberVectorPainter(Icons.Outlined.Image),
                imageTrait = node.trait.value as ImageTrait,
                modifierList = node.modifierList,
                modifierForCanvas = modifierForCanvas,
            )
        } else {
            when (assetType) {
                ImageAssetType.Network -> {
                    val imageUrl =
                        when (val usage = placeholderUrl) {
                            PlaceholderUrl.NoUsage ->
                                url.transformedValueExpression(
                                    project,
                                )

                            is PlaceholderUrl.Used -> usage.url.transformedValueExpression(project)
                        }
                    AsyncImage(
                        url = imageUrl,
                        contentDescription = stringResource(Res.string.image_from_network),
                        alignment =
                            alignmentWrapper?.alignment
                                ?: Alignment.Center,
                        contentScale =
                            contentScaleValue()
                                ?: ContentScale.Fit,
                        alpha = alpha ?: DefaultAlpha,
                        modifier = node.modifierChainForCanvas().then(modifierForCanvas),
                    )
                }

                ImageAssetType.Asset -> {
                    blobInfoWrapper?.let { blob ->
                        val userId = LocalFirebaseIdToken.current.user_id
                        blob.asImageComposable(
                            userId = userId,
                            projectId = project.id.toString(),
                            alignment =
                                alignmentWrapper?.alignment
                                    ?: Alignment.Center,
                            contentScale =
                                contentScaleValue()
                                    ?: ContentScale.Fit,
                            alpha = alpha ?: DefaultAlpha,
                            modifier = node.modifierChainForCanvas().then(modifierForCanvas),
                        )
                    } ?: Box(
                        modifier =
                            Modifier
                                .shimmer()
                                .background(
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(8.dp),
                                ),
                    )
                }
            }
        }
    }

    @Composable
    private fun LoadedImage(
        painter: Painter,
        imageTrait: ImageTrait,
        modifierList: List<ModifierWrapper>,
        modifierForCanvas: Modifier,
    ) {
        Image(
            painter = painter,
            contentDescription = stringResource(Res.string.image_from_network),
            alignment = imageTrait.alignmentWrapper?.alignment ?: Alignment.Center,
            contentScale =
                imageTrait.contentScaleValue()
                    ?: ContentScale.Fit,
            alpha = imageTrait.alpha ?: DefaultAlpha,
            modifier = modifierList.toModifierChain().then(modifierForCanvas),
        )
    }

    override fun generateCode(
        project: Project,
        node: ComposeNode,
        context: GenerationContext,
        dryRun: Boolean,
    ): CodeBlockWrapper {
        val codeBlockBuilder = CodeBlockWrapper.builder()
        if (assetType == ImageAssetType.Asset && blobInfoWrapper?.mediaLink == null) {
            return codeBlockBuilder.build()
        }

        val imageMember =
            when (assetType) {
                ImageAssetType.Network -> MemberNameWrapper.get("${COMPOSEFLOW_PACKAGE}.ui", "AsyncImage")
                ImageAssetType.Asset -> MemberHolder.AndroidX.Foundation.Image
            }
        codeBlockBuilder.addStatement(
            "%M(",
            imageMember,
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
        codeBlockBuilder.addStatement(")")
        return codeBlockBuilder.build()
    }
}

const val DEFAULT_URL = "https://picsum.photos/480"

object ImageAssetTypeSerializer :
    FallbackEnumSerializer<ImageAssetType>(ImageAssetType::class)

@Serializable(ImageAssetTypeSerializer::class)
enum class ImageAssetType {
    Network,
    Asset,
}

/**
 * Represents the url of the image only visible in the editor as a placeholder.
 */
@Serializable
sealed interface PlaceholderUrl {
    @Serializable
    @SerialName("NoUsage")
    data object NoUsage : PlaceholderUrl

    @Serializable
    @SerialName("Used")
    data class Used(
        val url: StringProperty = StringProperty.StringIntrinsicValue(DEFAULT_URL),
    ) : PlaceholderUrl
}

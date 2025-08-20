package io.composeflow.model.parameter
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper
import io.composeflow.kotlinpoet.wrapper.MemberNameWrapper
import io.composeflow.Res
import io.composeflow.custom.ComposeFlowIcons
import io.composeflow.custom.composeflowicons.Grid3x2Gap
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.kotlinpoet.MemberHolder
import io.composeflow.model.modifier.ModifierWrapper
import io.composeflow.model.palette.Constraint
import io.composeflow.model.palette.LazyListTraitNode
import io.composeflow.model.palette.Orientation
import io.composeflow.model.palette.PaletteRenderParams
import io.composeflow.model.palette.TraitCategory
import io.composeflow.model.parameter.lazylist.LazyGridCells
import io.composeflow.model.parameter.wrapper.ArrangementHorizontalWrapper
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.override.mutableStateListEqualsOverrideOf
import io.composeflow.serializer.LocationAwareDpSerializer
import io.composeflow.tooltip_lazy_horizontal_grid_trait
import io.composeflow.ui.CanvasNodeCallbacks
import io.composeflow.ui.modifierForCanvas
import io.composeflow.ui.zoomablecontainer.ZoomableContainerStateHolder
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource

@Serializable
@SerialName("LazyHorizontalGridTrait")
data class LazyHorizontalGridTrait(
    override val lazyGridCells: LazyGridCells = LazyGridCells.Adaptive(),
    @Serializable(with = LocationAwareDpSerializer::class)
    val contentPadding: Dp? = null,
    val reverseLayout: Boolean? = null,
    val horizontalArrangement: ArrangementHorizontalWrapper? = null,
    val userScrollEnabled: Boolean? = null,
) : LazyGridTrait,
    ComposeTrait {
    // Explicitly extending ComposeTrait so that this class is recognized as a subclass of it.
    // As a result this class is considered as a subclass of ComposeTrait in the jsonschema

    override var defaultChildNumOfItems: Int = ComposeTrait.NUM_OF_ITEMS_IN_LAZY_LIST

    override fun areAllParamsEmpty(): Boolean =
        contentPadding == null &&
            reverseLayout == null &&
            horizontalArrangement == null &&
            userScrollEnabled == null

    override fun generateParamsCode(): CodeBlockWrapper {
        val codeBlockBuilder = CodeBlockWrapper.builder()
        codeBlockBuilder.add("rows = ")
        codeBlockBuilder.add(lazyGridCells.asCodeBlock())
        codeBlockBuilder.addStatement(",")
        val dpMember = MemberNameWrapper.get("androidx.compose.ui.unit", "dp")
        contentPadding?.let {
            val paddingValuesMember =
                MemberNameWrapper.get("androidx.compose.foundation.layout", "PaddingValues")
            codeBlockBuilder.addStatement(
                "contentPadding = %M(${it.value.toInt()}.%M),",
                paddingValuesMember,
                dpMember,
            )
        }
        reverseLayout?.let {
            codeBlockBuilder.addStatement("reverseLayout = $it,")
        }
        horizontalArrangement?.let {
            val arrangementMember = MemberNameWrapper.get("androidx.compose.foundation.layout", "Arrangement")
            codeBlockBuilder.addStatement(
                "horizontalArrangement = %M.${it.name},",
                arrangementMember,
            )
        }
        userScrollEnabled?.let {
            codeBlockBuilder.addStatement("userScrollEnabled = $it,")
        }
        return codeBlockBuilder.build()
    }

    override fun defaultComposeNode(project: Project): ComposeNode =
        ComposeNode(
            modifierList = defaultModifierList(),
            trait = mutableStateOf(LazyHorizontalGridTrait()),
        )

    override fun hasDynamicItems(): Boolean = true

    override fun iconText(): String = "Lazy H Grid"

    override fun icon(): ImageVector = ComposeFlowIcons.Grid3x2Gap

    override fun isLazyList(): Boolean = true

    override fun paletteCategories(): List<TraitCategory> =
        listOf(
            TraitCategory.Container,
            TraitCategory.WrapContainer,
            TraitCategory.Layout,
        )

    override fun tooltipResource(): StringResource = Res.string.tooltip_lazy_horizontal_grid_trait

    override fun defaultModifierList(): MutableList<ModifierWrapper> =
        mutableStateListEqualsOverrideOf(
            ModifierWrapper.Padding(top = 8.dp, bottom = 8.dp),
            ModifierWrapper.FillMaxWidth(),
            ModifierWrapper.Height(300.dp),
        )

    override fun defaultConstraints(): Set<Constraint> =
        super<LazyGridTrait>.defaultConstraints().toMutableSet().apply {
            add(Constraint.InfiniteScroll(Orientation.Horizontal))
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
        LazyHorizontalGrid(
            rows = lazyGridCells.asComposeGridCells(),
            contentPadding = PaddingValues(contentPadding?.value?.dp ?: 0.dp),
            reverseLayout = reverseLayout ?: false,
            horizontalArrangement =
                horizontalArrangement?.arrangement
                    ?: Arrangement.Start,
            userScrollEnabled =
                if (paletteRenderParams.isThumbnail) {
                    false
                } else {
                    userScrollEnabled ?: true
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
                        ),
                ),
        ) {
            node.children.forEach { child ->
                item {
                    child.RenderedNodeInCanvas(
                        project = project,
                        canvasNodeCallbacks = canvasNodeCallbacks,
                        paletteRenderParams = paletteRenderParams,
                        zoomableContainerStateHolder = zoomableContainerStateHolder,
                    )
                }
                items(
                    count =
                        child.lazyListChildParams.value.getNumOfItems(
                            project = project,
                            lazyList = node,
                        ) - 1,
                ) {
                    child.RenderedNodeInCanvas(
                        project = project,
                        canvasNodeCallbacks = canvasNodeCallbacks,
                        paletteRenderParams = paletteRenderParams.copy(isShadowNode = true),
                        zoomableContainerStateHolder = zoomableContainerStateHolder,
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
    ): CodeBlockWrapper {
        val lazyHorizontalGridMember =
            MemberNameWrapper.get("androidx.compose.foundation.lazy.grid", "LazyHorizontalGrid")
        return LazyListTraitNode.generateCode(
            project = project,
            node = node,
            context = context,
            itemsIndexedMember = MemberHolder.AndroidX.Lazy.gridItemsIndexed,
            lazyListMember = lazyHorizontalGridMember,
            lazyListParams = this,
            dryRun = dryRun,
        )
    }
}

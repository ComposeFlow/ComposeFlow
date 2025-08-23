package io.composeflow.model.parameter
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.composeflow.Res
import io.composeflow.custom.ComposeFlowIcons
import io.composeflow.custom.composeflowicons.HorizontalPager
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper
import io.composeflow.kotlinpoet.wrapper.MemberNameWrapper
import io.composeflow.model.modifier.ModifierWrapper
import io.composeflow.model.palette.Constraint
import io.composeflow.model.palette.Orientation
import io.composeflow.model.palette.PagerTraitNode
import io.composeflow.model.palette.PaletteRenderParams
import io.composeflow.model.palette.TraitCategory
import io.composeflow.model.parameter.lazylist.DEFAULT_NUM_OF_ITEMS
import io.composeflow.model.parameter.lazylist.LazyListChildParams
import io.composeflow.model.parameter.wrapper.AlignmentVerticalWrapper
import io.composeflow.model.parameter.wrapper.ColorWrapper
import io.composeflow.model.parameter.wrapper.Material3ColorWrapper
import io.composeflow.model.parameter.wrapper.SnapPositionWrapper
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.property.AssignableProperty
import io.composeflow.model.property.ColorProperty
import io.composeflow.override.mutableStateListEqualsOverrideOf
import io.composeflow.serializer.LocationAwareDpSerializer
import io.composeflow.tooltip_horizontal_pager_trait
import io.composeflow.ui.CanvasNodeCallbacks
import io.composeflow.ui.modifierForCanvas
import io.composeflow.ui.zoomablecontainer.ZoomableContainerStateHolder
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource

@Serializable
@SerialName("HorizontalPagerTrait")
data class HorizontalPagerTrait(
    @Serializable(with = LocationAwareDpSerializer::class)
    val contentPadding: Dp? = null,
    @Serializable(with = LocationAwareDpSerializer::class)
    val pageSpacing: Dp? = null,
    val reverseLayout: Boolean? = null,
    val snapPositionWrapper: SnapPositionWrapper = SnapPositionWrapper.Start,
    val verticalAlignment: AlignmentVerticalWrapper? = null,
    val userScrollEnabled: Boolean? = null,
    val showIndicator: Boolean = true,
    val indicatorSelectedColor: AssignableProperty =
        ColorProperty.ColorIntrinsicValue(
            value = ColorWrapper(themeColor = Material3ColorWrapper.OnSurface),
        ),
    val indicatorUnselectedColor: AssignableProperty =
        ColorProperty.ColorIntrinsicValue(
            value = ColorWrapper(themeColor = Material3ColorWrapper.SurfaceVariant),
        ),
) : PagerTrait,
    ComposeTrait {
    // Explicitly extending ComposeTrait so that this class is recognized as a subclass of it.
    // As a result this class is considered as a subclass of ComposeTrait in the jsonschema

    override var defaultChildNumOfItems: Int = ComposeTrait.NUM_OF_ITEMS_IN_PAGER

    override fun generateParamsCode(): CodeBlockWrapper {
        val codeBlockBuilder = CodeBlockWrapper.builder()
        val dpMember = MemberNameWrapper.get("androidx.compose.ui.unit", "dp")
        contentPadding?.let {
            val paddingValuesMember =
                MemberNameWrapper.get("androidx.compose.foundation.layout", "PaddingValues")
            codeBlockBuilder
                .addStatement(
                    "contentPadding = %M(${it.value.toInt()}.%M),",
                    paddingValuesMember,
                    dpMember,
                )
        }
        pageSpacing?.let {
            codeBlockBuilder
                .addStatement(
                    "pageSpacing = ${it.value.toInt()}.%M,",
                    dpMember,
                )
        }
        codeBlockBuilder.addStatement(
            "snapPosition = %M,",
            snapPositionWrapper.toMemberName(),
        )
        reverseLayout?.let {
            codeBlockBuilder.addStatement("reverseLayout = $it,")
        }
        verticalAlignment?.let {
            val alignmentMember = MemberNameWrapper.get("androidx.compose.ui", "Alignment")
            codeBlockBuilder.addStatement("verticalAlignment = %M.${it.name},", alignmentMember)
        }
        userScrollEnabled?.let {
            codeBlockBuilder.addStatement("userScrollEnabled = $it,")
        }
        return codeBlockBuilder.build()
    }

    override fun defaultComposeNode(project: Project): ComposeNode =
        ComposeNode(
            modifierList = defaultModifierList(),
            trait = mutableStateOf(HorizontalPagerTrait()),
        )

    override fun hasDynamicItems(): Boolean = true

    override fun icon(): ImageVector = ComposeFlowIcons.HorizontalPager

    override fun iconText(): String = "H Pager"

    override fun paletteCategories(): List<TraitCategory> =
        listOf(
            TraitCategory.Container,
            TraitCategory.Layout,
        )

    override fun tooltipResource(): StringResource = Res.string.tooltip_horizontal_pager_trait

    override fun defaultModifierList(): MutableList<ModifierWrapper> =
        mutableStateListEqualsOverrideOf(
            ModifierWrapper.Padding(all = 8.dp),
            ModifierWrapper.FillMaxWidth(),
            ModifierWrapper.Size(height = 240.dp),
        )

    override fun defaultConstraints(): Set<Constraint> =
        super<PagerTrait>.defaultConstraints().toMutableSet().apply {
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
        val childrenDependOnDynamicItems =
            node.children.any {
                it.lazyListChildParams.value is LazyListChildParams.DynamicItemsSource
            }
        if (childrenDependOnDynamicItems) {
            val pagerState =
                rememberPagerState(
                    pageCount = {
                        DEFAULT_NUM_OF_ITEMS
                    },
                )
            Box(
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
                HorizontalPager(
                    state = pagerState,
                    contentPadding = PaddingValues(contentPadding?.value?.dp ?: 0.dp),
                    pageSpacing = pageSpacing?.value?.dp ?: 0.dp,
                    reverseLayout = reverseLayout ?: false,
                    verticalAlignment = verticalAlignment?.alignment ?: Alignment.Top,
                    userScrollEnabled =
                        if (paletteRenderParams.isThumbnail) {
                            false
                        } else {
                            userScrollEnabled ?: true
                        },
                    snapPosition = snapPositionWrapper.toSnapPosition(),
                    modifier = Modifier.fillMaxSize(),
                ) { page ->
                    val child =
                        node.children.first {
                            it.lazyListChildParams.value is LazyListChildParams.DynamicItemsSource
                        }
                    if (page == 0) {
                        child.RenderedNodeInCanvas(
                            project = project,
                            canvasNodeCallbacks = canvasNodeCallbacks,
                            paletteRenderParams = paletteRenderParams,
                            zoomableContainerStateHolder = zoomableContainerStateHolder,
                        )
                    } else {
                        child.RenderedNodeInCanvas(
                            project = project,
                            canvasNodeCallbacks = canvasNodeCallbacks,
                            paletteRenderParams = paletteRenderParams.copy(isShadowNode = true),
                            zoomableContainerStateHolder = zoomableContainerStateHolder,
                        )
                    }
                }
                if (showIndicator) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier =
                            Modifier
                                .padding(bottom = 8.dp)
                                .align(Alignment.BottomCenter),
                    ) {
                        repeat(DEFAULT_NUM_OF_ITEMS) { index ->
                            val isSelected = pagerState.currentPage == index
                            Box(
                                modifier =
                                    Modifier
                                        .padding(horizontal = 4.dp)
                                        .size(if (isSelected) 10.dp else 8.dp)
                                        .background(
                                            color =
                                                if (isSelected) {
                                                    (indicatorSelectedColor as? ColorProperty.ColorIntrinsicValue)?.value?.getColor()
                                                        ?: Color.Unspecified
                                                } else {
                                                    (indicatorUnselectedColor as? ColorProperty.ColorIntrinsicValue)?.value?.getColor()
                                                        ?: Color.Unspecified
                                                },
                                            shape = CircleShape,
                                        ),
                            )
                        }
                    }
                }
            }
        } else {
            val pagerState =
                rememberPagerState(
                    pageCount = {
                        node.children.size
                    },
                )
            Box(
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
                HorizontalPager(
                    state = pagerState,
                    contentPadding = PaddingValues(contentPadding?.value?.dp ?: 0.dp),
                    pageSpacing = pageSpacing?.value?.dp ?: 0.dp,
                    reverseLayout = reverseLayout ?: false,
                    verticalAlignment = verticalAlignment?.alignment ?: Alignment.Top,
                    userScrollEnabled =
                        if (paletteRenderParams.isThumbnail) {
                            false
                        } else {
                            userScrollEnabled ?: true
                        },
                    snapPosition = snapPositionWrapper.toSnapPosition(),
                    modifier = Modifier.fillMaxSize(),
                ) { page ->
                    val child =
                        if (node.children.size > page) {
                            node.children[page]
                        } else {
                            node.children.last()
                        }
                    child.RenderedNodeInCanvas(
                        project = project,
                        canvasNodeCallbacks = canvasNodeCallbacks,
                        paletteRenderParams = paletteRenderParams,
                        zoomableContainerStateHolder = zoomableContainerStateHolder,
                    )
                }

                if (showIndicator) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier =
                            Modifier
                                .padding(bottom = 8.dp)
                                .align(Alignment.BottomCenter),
                    ) {
                        repeat(node.children.size) { index ->
                            val isSelected = pagerState.currentPage == index
                            Box(
                                modifier =
                                    Modifier
                                        .padding(horizontal = 4.dp)
                                        .size(if (isSelected) 10.dp else 8.dp)
                                        .background(
                                            color =
                                                if (isSelected) {
                                                    (indicatorSelectedColor as? ColorProperty.ColorIntrinsicValue)?.value?.getColor()
                                                        ?: Color.Unspecified
                                                } else {
                                                    (indicatorUnselectedColor as? ColorProperty.ColorIntrinsicValue)?.value?.getColor()
                                                        ?: Color.Unspecified
                                                },
                                            shape = CircleShape,
                                        ),
                            )
                        }
                    }
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
        val horizontalPagerMember =
            MemberNameWrapper.get("androidx.compose.foundation.pager", "HorizontalPager")
        return PagerTraitNode.generateCode(
            project = project,
            node = node,
            context = context,
            pagerMember = horizontalPagerMember,
            pagerTrait = this,
            dryRun = dryRun,
        )
    }
}

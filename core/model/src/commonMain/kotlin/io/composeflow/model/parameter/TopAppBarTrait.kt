package io.composeflow.model.parameter

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.enterAlwaysScrollBehavior
import androidx.compose.material3.TopAppBarDefaults.exitUntilCollapsedScrollBehavior
import androidx.compose.material3.TopAppBarDefaults.pinnedScrollBehavior
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import io.composeflow.Res
import io.composeflow.custom.ComposeFlowIcons
import io.composeflow.custom.composeflowicons.TopHeader
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.kotlinpoet.wrapper.CodeBlockBuilderWrapper
import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper
import io.composeflow.kotlinpoet.wrapper.MemberNameWrapper
import io.composeflow.model.action.ActionType
import io.composeflow.model.modifier.generateModifierCode
import io.composeflow.model.palette.PaletteRenderParams
import io.composeflow.model.palette.TraitCategory
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.project.appscreen.screen.composenode.TopAppBarNode
import io.composeflow.model.property.AssignableProperty
import io.composeflow.model.property.ColorProperty
import io.composeflow.model.property.PropertyContainer
import io.composeflow.model.property.StringProperty
import io.composeflow.model.type.ComposeFlowType
import io.composeflow.serializer.FallbackEnumSerializer
import io.composeflow.tooltip_top_app_bar_trait
import io.composeflow.ui.CanvasNodeCallbacks
import io.composeflow.ui.modifierForCanvas
import io.composeflow.ui.zoomablecontainer.ZoomableContainerStateHolder
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource

@Serializable
@SerialName("TopAppBarTrait")
data class TopAppBarTrait(
    val title: AssignableProperty = StringProperty.StringIntrinsicValue(),
    val titlePlaceholderText: PlaceholderText = PlaceholderText.NoUsage,
    val topAppBarType: TopAppBarTypeWrapper = TopAppBarTypeWrapper.Default,
    val scrollBehaviorWrapper: ScrollBehaviorWrapper = ScrollBehaviorWrapper.EnterAlways,
) : ComposeTrait {
    override fun getPropertyContainers(): List<PropertyContainer> =
        listOf(
            PropertyContainer("Title", title, ComposeFlowType.StringType()),
        )

    override fun icon(): ImageVector = ComposeFlowIcons.TopHeader

    override fun iconText(): String = "TopAppBar"

    override fun paletteCategories(): List<TraitCategory> = listOf(TraitCategory.Container, TraitCategory.ScreenOnly)

    override fun tooltipResource(): StringResource = Res.string.tooltip_top_app_bar_trait

    override fun visibleInPalette(): Boolean = true

    override fun isDroppable(): Boolean = false

    override fun isResizeable(): Boolean = false

    override fun isEditable(): Boolean = true

    override fun isVisibilityConditional(): Boolean = false

    override fun defaultComposeNode(project: Project): ComposeNode =
        ComposeNode(
            label = mutableStateOf("TopAppBar"),
            trait =
                mutableStateOf(
                    TopAppBarTrait(
                        title = StringProperty.StringIntrinsicValue("Title"),
                        topAppBarType = TopAppBarTypeWrapper.CenterAligned,
                    ),
                ),
        ).apply {
            addChild(
                ComposeNode(
                    label = mutableStateOf("Nav Icon"),
                    trait =
                        mutableStateOf(
                            IconTrait(imageVectorHolder = null),
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
        fun TopAppBarIconButton(iconNode: ComposeNode) {
            IconButton(onClick = {}) {
                val iconTrait = iconNode.trait.value as? IconTrait
                iconTrait?.imageVectorHolder?.let {
                    Icon(
                        imageVector = it.imageVector,
                        contentDescription = null,
                        tint =
                            (iconTrait.tint as? ColorProperty.ColorIntrinsicValue)?.value?.getColor()
                                ?: MaterialTheme.colorScheme.onBackground,
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

        @Composable
        fun scrollBehavior(scrollBehaviorWrapper: ScrollBehaviorWrapper?): TopAppBarScrollBehavior? =
            when (scrollBehaviorWrapper) {
                ScrollBehaviorWrapper.None -> null
                ScrollBehaviorWrapper.EnterAlways -> {
                    enterAlwaysScrollBehavior()
                }

                ScrollBehaviorWrapper.ExitUntilCollapsed -> {
                    exitUntilCollapsedScrollBehavior()
                }

                ScrollBehaviorWrapper.Pinned -> {
                    pinnedScrollBehavior()
                }

                null -> null
            }

        val topAppBarNode = node as TopAppBarNode
        val navIcon = topAppBarNode.getTopAppBarNavigationIcon()
        val navIconTrait = navIcon?.trait?.value as? IconTrait
        val textWithPlaceholder =
            when (val usage = titlePlaceholderText) {
                PlaceholderText.NoUsage -> title.transformedValueExpression(project)
                is PlaceholderText.Used -> usage.value.transformedValueExpression(project)
            }
        when (topAppBarType) {
            TopAppBarTypeWrapper.Default -> {
                TopAppBar(
                    title = {
                        Text(text = textWithPlaceholder)
                    },
                    navigationIcon =
                        navIconTrait?.imageVectorHolder?.let {
                            {
                                TopAppBarIconButton(navIcon)
                            }
                        } ?: {},
                    actions = {
                        topAppBarNode.getTopAppBarActionIcons().forEach {
                            TopAppBarIconButton(it)
                        }
                    },
                    scrollBehavior = scrollBehavior(scrollBehaviorWrapper),
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

            TopAppBarTypeWrapper.CenterAligned -> {
                CenterAlignedTopAppBar(
                    title = {
                        Text(text = textWithPlaceholder)
                    },
                    navigationIcon =
                        navIconTrait?.imageVectorHolder?.let {
                            {
                                TopAppBarIconButton(navIcon)
                            }
                        } ?: {},
                    actions = {
                        topAppBarNode.getTopAppBarActionIcons().forEach {
                            TopAppBarIconButton(it)
                        }
                    },
                    scrollBehavior = scrollBehavior(scrollBehaviorWrapper),
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

            TopAppBarTypeWrapper.Medium -> {
                MediumTopAppBar(
                    title = {
                        Text(text = textWithPlaceholder)
                    },
                    navigationIcon =
                        navIconTrait?.imageVectorHolder?.let {
                            {
                                TopAppBarIconButton(navIcon)
                            }
                        } ?: {},
                    actions = {
                        topAppBarNode.getTopAppBarActionIcons().forEach {
                            TopAppBarIconButton(it)
                        }
                    },
                    scrollBehavior = scrollBehavior(scrollBehaviorWrapper),
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

            TopAppBarTypeWrapper.Large -> {
                LargeTopAppBar(
                    title = {
                        Text(text = textWithPlaceholder)
                    },
                    navigationIcon =
                        navIconTrait?.imageVectorHolder?.let {
                            {
                                TopAppBarIconButton(navIcon)
                            }
                        } ?: {},
                    actions = {
                        topAppBarNode.getTopAppBarActionIcons().forEach {
                            TopAppBarIconButton(it)
                        }
                    },
                    scrollBehavior = scrollBehavior(scrollBehaviorWrapper),
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
        }
    }

    override fun generateCode(
        project: Project,
        node: ComposeNode,
        context: GenerationContext,
        dryRun: Boolean,
    ): CodeBlockWrapper {
        val codeBlockBuilder = CodeBlockWrapper.builder()
        val topAppBarNode = node as TopAppBarNode
        val topAppBarMemberName =
            when (topAppBarType) {
                TopAppBarTypeWrapper.Default ->
                    MemberNameWrapper.get(
                        "androidx.compose.material3",
                        "TopAppBar",
                    )

                TopAppBarTypeWrapper.CenterAligned ->
                    MemberNameWrapper.get(
                        "androidx.compose.material3",
                        "CenterAlignedTopAppBar",
                    )

                TopAppBarTypeWrapper.Medium ->
                    MemberNameWrapper.get(
                        "androidx.compose.material3",
                        "MediumTopAppBar",
                    )

                TopAppBarTypeWrapper.Large ->
                    MemberNameWrapper.get(
                        "androidx.compose.material3",
                        "LargeTopAppBar",
                    )
            }
        codeBlockBuilder.addStatement("%M(", topAppBarMemberName)

        val screen =
            project.screenHolder.screens.firstOrNull {
                it.topAppBarNode.value == node
            }
        screen?.topAppBarNode?.value?.let {
            // Title for screen specific TopAppBar
            codeBlockBuilder.addStatement(
                "title = { %M(",
                MemberNameWrapper.get("androidx.compose.material3", "Text"),
            )
            codeBlockBuilder.add(
                title.transformedCodeBlock(
                    project,
                    context,
                    writeType = ComposeFlowType.StringType(),
                    dryRun = dryRun,
                ),
            )
            codeBlockBuilder.addStatement(") },")
        } ?: {
            codeBlockBuilder.addStatement(
                "title = currentDestination?.let { { %M(",
                MemberNameWrapper.get("androidx.compose.material3", "Text"),
            )
            codeBlockBuilder.addStatement("it.title")
            codeBlockBuilder.addStatement(") }")
            codeBlockBuilder.addStatement("} ?: {},")
        }

        fun writeIconButton(
            iconNode: ComposeNode,
            builder: CodeBlockBuilderWrapper,
        ) {
            val iconTrait = iconNode.trait.value as? IconTrait
            iconTrait?.imageVectorHolder?.let { imageVectorHolder ->
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
                    "%M(imageVector = %M.${imageVectorHolder.memberDescriptor}.%M, contentDescription = null,",
                    iconMember,
                    iconsMember,
                    imageVectorMember,
                )
                iconTrait.tint?.let {
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
                builder.addStatement(")")
                builder.addStatement("}")
            }
        }

        val navIcon = topAppBarNode.getTopAppBarNavigationIcon()
        val navIconTrait = navIcon?.trait?.value as? IconTrait
        navIconTrait?.imageVectorHolder?.let {
            codeBlockBuilder.addStatement("navigationIcon = {")
            writeIconButton(navIcon, codeBlockBuilder)
            codeBlockBuilder.addStatement("},")
        }

        val actionIcons = topAppBarNode.getTopAppBarActionIcons()
        if (actionIcons.isNotEmpty()) {
            codeBlockBuilder.addStatement("actions = {")
            actionIcons.forEach { actionIcon ->
                val actionIconTrait = actionIcon.trait.value as? IconTrait
                actionIconTrait?.imageVectorHolder?.let {
                    writeIconButton(actionIcon, codeBlockBuilder)
                }
            }
            codeBlockBuilder.addStatement("},")
        }
        if (scrollBehaviorWrapper.hasScrollBehavior()) {
            // scrollBehavior variable is defined in the generateCode in ScreenHolder
            codeBlockBuilder.addStatement("scrollBehavior = scrollBehavior,")
        }
        codeBlockBuilder.add(
            node.generateModifierCode(project, context, dryRun = dryRun),
        )
        codeBlockBuilder.addStatement(")")
        return codeBlockBuilder.build()
    }

    fun contentEquals(other: TopAppBarTrait): Boolean =
        title == other.title &&
            topAppBarType == other.topAppBarType &&
            scrollBehaviorWrapper == other.scrollBehaviorWrapper
}

object TopAppBarTypeWrapperSerializer : FallbackEnumSerializer<TopAppBarTypeWrapper>(
    TopAppBarTypeWrapper::class,
    TopAppBarTypeWrapper.entries.toTypedArray(),
)

@Serializable(TopAppBarTypeWrapperSerializer::class)
enum class TopAppBarTypeWrapper {
    Default,
    CenterAligned,
    Medium,
    Large,
}

object ScrollBehaviorWrapperSerializer : FallbackEnumSerializer<ScrollBehaviorWrapper>(
    ScrollBehaviorWrapper::class,
    ScrollBehaviorWrapper.entries.toTypedArray(),
)

@Serializable(ScrollBehaviorWrapperSerializer::class)
enum class ScrollBehaviorWrapper {
    None {
        override fun hasScrollBehavior(): Boolean = false
    },
    EnterAlways {
        override fun hasScrollBehavior(): Boolean = true
    },
    ExitUntilCollapsed {
        override fun hasScrollBehavior(): Boolean = true
    },
    Pinned {
        override fun hasScrollBehavior(): Boolean = true
    },
    ;

    abstract fun hasScrollBehavior(): Boolean
}

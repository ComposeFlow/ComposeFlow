package io.composeflow.model.parameter

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Tab
import androidx.compose.material3.Icon
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import io.composeflow.Res
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.kotlinpoet.MemberHolder
import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper
import io.composeflow.kotlinpoet.wrapper.MemberNameWrapper
import io.composeflow.model.modifier.ModifierWrapper
import io.composeflow.model.modifier.generateModifierCode
import io.composeflow.model.modifier.toModifierChain
import io.composeflow.model.palette.Constraint
import io.composeflow.model.palette.PaletteRenderParams
import io.composeflow.model.palette.TraitCategory
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.override.mutableStateListEqualsOverrideOf
import io.composeflow.tooltip_tabs_trait
import io.composeflow.ui.CanvasNodeCallbacks
import io.composeflow.ui.modifierForCanvas
import io.composeflow.ui.zoomablecontainer.ZoomableContainerStateHolder
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource

@Serializable
@SerialName("TabsTrait")
data object TabsTrait : ComposeTrait {
    override fun defaultConstraints(): Set<Constraint> = setOf(Constraint.NestedTabs)

    override fun defaultComposeNode(project: Project): ComposeNode {
        val tabContainer =
            ComposeNode(
                trait = mutableStateOf(TabsTrait),
                modifierList =
                    mutableStateListEqualsOverrideOf(
                        ModifierWrapper.FillMaxSize(),
                    ),
            ).apply {
                addChild(
                    ComposeNode(
                        trait = mutableStateOf(TabRowTrait()),
                    ),
                )
            }
        for (i in 1..ComposeTrait.NUM_OF_DEFAULT_TABS) {
            tabContainer.addTab()
        }
        return tabContainer
    }

    override fun icon(): ImageVector = Icons.Outlined.Tab

    override fun iconText(): String = "Tabs"

    override fun paletteCategories(): List<TraitCategory> = listOf(TraitCategory.Container, TraitCategory.Basic)

    override fun tooltipResource(): StringResource = Res.string.tooltip_tabs_trait

    override fun onClickIncludedInParams(): Boolean = true

    private fun hasValidChildren(node: ComposeNode): Boolean {
        val tabRow = node.children.firstOrNull()
        return tabRow != null && tabRow.trait.value is TabRowTrait
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
        if (!hasValidChildren(node)) {
            return
        }
        Column(
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
            val tabRow = node.children.first()
            check(tabRow.trait.value is TabRowTrait) { "First child of TabsTrait must be TabRowTrait" }
            val tabs = tabRow.children
            val tabContents = node.children.filterNot { it.trait.value is TabRowTrait }

            @Composable
            fun LocalTab(
                tab: ComposeNode,
                index: Int,
            ) {
                val tabTrait = tab.trait.value as TabTrait
                Tab(
                    selected = node.selectedIndex.value == index,
                    text =
                        tabTrait.text?.let { text ->
                            {
                                Text(
                                    text = text.transformedValueExpression(project),
                                )
                            }
                        },
                    icon =
                        tabTrait.icon?.let { icon ->
                            {
                                Icon(
                                    imageVector = icon.imageVector,
                                    contentDescription = null,
                                )
                            }
                        },
                    onClick = {
                        if (!paletteRenderParams.isThumbnail) {
                            node.selectedIndex.value = index
                        }
                    },
                    modifier =
                        tab.modifierList
                            .toModifierChain()
                            .modifierForCanvas(
                                project = project,
                                node = tab,
                                canvasNodeCallbacks = canvasNodeCallbacks,
                                isDraggable = false,
                                paletteRenderParams = paletteRenderParams,
                                zoomableContainerStateHolder = zoomableContainerStateHolder,
                            ),
                )
            }

            val tabRowTrait = tabRow.trait.value as TabRowTrait
            if (tabRowTrait.scrollable) {
                ScrollableTabRow(
                    selectedTabIndex = node.selectedIndex.value,
                    modifier =
                        tabRow.modifierList
                            .toModifierChain()
                            .modifierForCanvas(
                                project = project,
                                node = tabRow,
                                canvasNodeCallbacks = canvasNodeCallbacks,
                                isDraggable = false,
                                paletteRenderParams = paletteRenderParams,
                                zoomableContainerStateHolder = zoomableContainerStateHolder,
                            ),
                ) {
                    tabs.forEachIndexed { i, tab ->
                        LocalTab(tab = tab, index = i)
                    }
                }
            } else {
                TabRow(
                    selectedTabIndex = node.selectedIndex.value,
                    modifier =
                        tabRow.modifierList
                            .toModifierChain()
                            .modifierForCanvas(
                                project = project,
                                node = tabRow,
                                canvasNodeCallbacks = canvasNodeCallbacks,
                                isDraggable = false,
                                paletteRenderParams = paletteRenderParams,
                                zoomableContainerStateHolder = zoomableContainerStateHolder,
                            ),
                ) {
                    tabs.forEachIndexed { i, tab ->
                        LocalTab(tab = tab, index = i)
                    }
                }
            }

            tabs.forEachIndexed { i, _ ->
                if (node.selectedIndex.value == i && tabContents.size > i) {
                    tabContents[i].RenderedNodeInCanvas(
                        project = project,
                        canvasNodeCallbacks = canvasNodeCallbacks,
                        paletteRenderParams = paletteRenderParams,
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
        if (!hasValidChildren(node)) {
            return CodeBlockWrapper.builder().build()
        }
        val codeBlockBuilder = CodeBlockWrapper.builder()
        if (node.modifierList.isEmpty()) {
            codeBlockBuilder.addStatement("%M {", MemberHolder.AndroidX.Layout.Column)
        } else {
            codeBlockBuilder.addStatement("%M(", MemberHolder.AndroidX.Layout.Column)
            codeBlockBuilder.add(
                node.generateModifierCode(project, context, dryRun = dryRun),
            )
            codeBlockBuilder.addStatement(") {")
        }
        val selectedIndexInitialName = "selectedIndex"
        val indexVariable =
            context
                .getCurrentComposableContext()
                .addComposeFileVariable(
                    id = "${node.id}-$selectedIndexInitialName",
                    initialIdentifier = selectedIndexInitialName,
                    dryRun,
                )
        codeBlockBuilder.addStatement(
            "var $indexVariable by %M { %M(0) }",
            MemberHolder.AndroidX.Runtime.rememberSaveable,
            MemberHolder.AndroidX.Runtime.mutableStateOf,
        )
        val tabRow = node.children.first()
        val tabContents = node.children.filterNot { it.trait.value is TabRowTrait }
        tabRow.children.forEach { tab ->
            val tabTrait = tab.trait.value as TabTrait
            tabTrait.selectedTabIndexVariableName = indexVariable
        }

        codeBlockBuilder.add(
            generateTabRowCode(
                project = project,
                node = tabRow,
                context = context,
                dryRun = dryRun,
                scrollable = (tabRow.trait.value as? TabRowTrait)?.scrollable == true,
            ),
        )

        codeBlockBuilder.addStatement(" when ($indexVariable) { ")
        tabContents.forEachIndexed { i, child ->
            codeBlockBuilder.addStatement(" $i -> { ")
            codeBlockBuilder.add(
                child.generateCode(
                    project = project,
                    context = context,
                    dryRun = dryRun,
                ),
            )
            codeBlockBuilder.addStatement("} ")
        }
        codeBlockBuilder.addStatement("  } ")
        codeBlockBuilder.addStatement("}")

        return codeBlockBuilder.build()
    }

    private fun generateTabRowCode(
        project: Project,
        node: ComposeNode,
        context: GenerationContext,
        dryRun: Boolean,
        scrollable: Boolean,
    ): CodeBlockWrapper {
        val codeBlockBuilder = CodeBlockWrapper.builder()
        val tabRowMember =
            if (scrollable) {
                MemberNameWrapper.get("androidx.compose.material3", "ScrollableTabRow")
            } else {
                MemberNameWrapper.get("androidx.compose.material3", "TabRow")
            }
        codeBlockBuilder.addStatement(
            """%M(
                selectedTabIndex = selectedIndex,
            """,
            tabRowMember,
        )
        codeBlockBuilder.add(
            node.generateModifierCode(project, context, dryRun = dryRun),
        )
        codeBlockBuilder.addStatement(") {")
        node.children.forEachIndexed { i, child ->
            val tabTrait = child.trait.value as TabTrait
            child.trait.value = tabTrait.copy(index = i)
            codeBlockBuilder.add(
                child.generateCode(
                    project = project,
                    context = context,
                    dryRun = dryRun,
                ),
            )
        }
        codeBlockBuilder.addStatement("}")
        return codeBlockBuilder.build()
    }
}

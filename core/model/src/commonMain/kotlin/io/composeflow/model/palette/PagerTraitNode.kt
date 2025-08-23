package io.composeflow.model.palette

import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.kotlinpoet.MemberHolder
import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper
import io.composeflow.kotlinpoet.wrapper.MemberNameWrapper
import io.composeflow.model.modifier.generateModifierCode
import io.composeflow.model.parameter.HorizontalPagerTrait
import io.composeflow.model.parameter.lazylist.LazyListChildParams
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.project.findComposeNodeOrNull
import io.composeflow.model.type.ComposeFlowType
import io.composeflow.replaceSpaces

object PagerTraitNode {
    fun generateCode(
        project: Project,
        node: ComposeNode,
        context: GenerationContext,
        pagerMember: MemberNameWrapper,
        pagerTrait: HorizontalPagerTrait,
        dryRun: Boolean,
    ): CodeBlockWrapper {
        val codeBlockBuilder = CodeBlockWrapper.builder()
        val itemName =
            node.trait.value
                .iconText()
                .replaceSpaces()
                .replaceFirstChar { it.lowercase() }

        val childrenDependOnDynamicItems =
            node.children.any {
                it.lazyListChildParams.value is LazyListChildParams.DynamicItemsSource
            }

        codeBlockBuilder.add("%M(", MemberHolder.AndroidX.Layout.Box)
        codeBlockBuilder.add(
            node.generateModifierCode(project, context, dryRun = dryRun),
        )
        codeBlockBuilder.addStatement(") {")
        val initialPagerStateName = "pagerState"
        val pagerStateName =
            context
                .getCurrentComposableContext()
                .addComposeFileVariable(
                    id = "${node.id}-$initialPagerStateName",
                    initialPagerStateName,
                    dryRun,
                )
        if (!childrenDependOnDynamicItems) {
            codeBlockBuilder.addStatement(
                "val $pagerStateName = %M(pageCount = {${node.children.size}})",
                MemberNameWrapper.get("androidx.compose.foundation.pager", "rememberPagerState"),
            )
            codeBlockBuilder.addStatement("%M(state = $pagerStateName,", pagerMember)
            codeBlockBuilder.add(
                pagerTrait.generateParamsCode(),
            )
            codeBlockBuilder.add(
                node.generateModifierCode(project, context, dryRun = dryRun),
            )
            codeBlockBuilder.addStatement(") { ${itemName}Index -> ")

            codeBlockBuilder.addStatement("listOf<@Composable () -> Unit>(")
            node.children.forEach { child ->
                codeBlockBuilder.add("{")
                codeBlockBuilder.add(
                    child.generateCode(
                        project = project,
                        context = context,
                        dryRun = dryRun,
                    ),
                )
                codeBlockBuilder.add("},")
            }
            codeBlockBuilder.addStatement(")[${itemName}Index]()")
            codeBlockBuilder.addStatement("}")
        } else {
            val child =
                node.children.first {
                    it.lazyListChildParams.value is LazyListChildParams.DynamicItemsSource
                }
            val dynamicItem =
                project
                    .findComposeNodeOrNull(
                        (child.lazyListChildParams.value as LazyListChildParams.DynamicItemsSource).composeNodeId,
                    )?.dynamicItems
                    ?.value

            dynamicItem?.let {
                codeBlockBuilder.add(
                    "val $pagerStateName = %M(pageCount = {",
                    MemberNameWrapper.get("androidx.compose.foundation.pager", "rememberPagerState"),
                )
                val transformedItemCodeBlock =
                    dynamicItem.transformedCodeBlock(project, context, dryRun = dryRun)
                codeBlockBuilder.add(transformedItemCodeBlock)
                codeBlockBuilder.add(".size")
                codeBlockBuilder.addStatement("})")

                codeBlockBuilder.addStatement("%M(state = $pagerStateName,", pagerMember)
                codeBlockBuilder.add(
                    pagerTrait.generateParamsCode(),
                )
                codeBlockBuilder.add(
                    node.generateModifierCode(project, context, dryRun = dryRun),
                )
                codeBlockBuilder.addStatement(") { ${itemName}Index -> ")

                val initialComposableListName = "composableList"
                val composableListName =
                    context
                        .getCurrentComposableContext()
                        .addComposeFileVariable(
                            id = "${node.id}-$initialComposableListName",
                            initialComposableListName,
                            dryRun,
                        )

                codeBlockBuilder.add("val $composableListName: List<@Composable () -> Unit> = ")
                codeBlockBuilder.add(transformedItemCodeBlock)
                codeBlockBuilder.add(".map { ${itemName}Item ->")

                codeBlockBuilder.addStatement("@Composable {")
                codeBlockBuilder.add(
                    child.generateCode(
                        project = project,
                        context = context,
                        dryRun = dryRun,
                    ),
                )
                codeBlockBuilder.addStatement("}")
                codeBlockBuilder.addStatement("}")

                codeBlockBuilder.addStatement("$composableListName[${itemName}Index]()")

                codeBlockBuilder.addStatement("}")
            }
        }

        if (pagerTrait.showIndicator) {
            codeBlockBuilder.addStatement(
                """
            %M(
                horizontalArrangement = %M.Center,
                modifier = %M.%M(bottom = 8.%M)
                    .align(%M.BottomCenter)
            ) {  
            """,
                MemberHolder.AndroidX.Layout.Row,
                MemberHolder.AndroidX.Layout.Arrangement,
                MemberHolder.AndroidX.Ui.Modifier,
                MemberHolder.AndroidX.Layout.padding,
                MemberHolder.AndroidX.Ui.dp,
                MemberHolder.AndroidX.Ui.Alignment,
            )
            codeBlockBuilder.add("repeat(")
            if (!childrenDependOnDynamicItems) {
                codeBlockBuilder.add("${node.children.size}")
            } else {
                val child =
                    node.children.first {
                        it.lazyListChildParams.value is LazyListChildParams.DynamicItemsSource
                    }
                val dynamicItem =
                    project
                        .findComposeNodeOrNull(
                            (child.lazyListChildParams.value as LazyListChildParams.DynamicItemsSource).composeNodeId,
                        )?.dynamicItems
                        ?.value
                dynamicItem?.let {
                    val transformedItemCodeBlock =
                        dynamicItem.transformedCodeBlock(project, context, dryRun = dryRun)
                    codeBlockBuilder.add(transformedItemCodeBlock)
                    codeBlockBuilder.add(".size")
                } ?: codeBlockBuilder.add("${node.children.size}")
            }
            codeBlockBuilder.addStatement(") { index ->")
            codeBlockBuilder.addStatement(
                """
               val isSelected = $pagerStateName.currentPage == index
               %M(
                   modifier = %M
                       .%M(horizontal = 4.%M)
                       .%M(if (isSelected) 10.%M else 8.%M)
                       .%M(
                           color = if (isSelected) {
               """,
                MemberHolder.AndroidX.Layout.Box,
                MemberHolder.AndroidX.Ui.Modifier,
                MemberHolder.AndroidX.Layout.padding,
                MemberHolder.AndroidX.Ui.dp,
                MemberHolder.AndroidX.Layout.size,
                MemberHolder.AndroidX.Ui.dp,
                MemberHolder.AndroidX.Ui.dp,
                MemberHolder.AndroidX.Foundation.background,
            )

            codeBlockBuilder.add(
                pagerTrait.indicatorSelectedColor.transformedCodeBlock(
                    project,
                    context,
                    writeType = ComposeFlowType.Color(),
                    dryRun,
                ),
            )
            codeBlockBuilder.addStatement("} else {")
            codeBlockBuilder.add(
                pagerTrait.indicatorUnselectedColor.transformedCodeBlock(
                    project,
                    context,
                    writeType = ComposeFlowType.Color(),
                    dryRun,
                ),
            )
            codeBlockBuilder.addStatement("},")
            codeBlockBuilder.addStatement(
                "shape = %M",
                MemberNameWrapper.get("androidx.compose.foundation.shape", "CircleShape"),
            )
            codeBlockBuilder.addStatement(")") // background
            codeBlockBuilder.addStatement(")") // Box
            codeBlockBuilder.addStatement("}") // repeat
            codeBlockBuilder.addStatement("}") // Row
        }
        codeBlockBuilder.addStatement("}") // Box
        return codeBlockBuilder.build()
    }
}

package io.composeflow.model.palette

import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.kotlinpoet.MemberHolder
import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper
import io.composeflow.kotlinpoet.wrapper.MemberNameWrapper
import io.composeflow.model.modifier.generateModifierCode
import io.composeflow.model.parameter.LazyListTrait
import io.composeflow.model.parameter.lazylist.LazyListChildParams
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.project.findComposeNodeOrNull
import io.composeflow.model.property.ApiResultProperty
import io.composeflow.replaceSpaces

object LazyListTraitNode {
    fun generateCode(
        project: Project,
        node: ComposeNode,
        context: GenerationContext,
        itemsIndexedMember: MemberNameWrapper,
        lazyListMember: MemberNameWrapper,
        // Two itemsIndexed exists between LazyList and LazyGrid
        lazyListParams: LazyListTrait,
        dryRun: Boolean,
    ): CodeBlockWrapper {
        val codeBlockBuilder = CodeBlockWrapper.builder()
        val allParamsEmpty = lazyListParams.areAllParamsEmpty() && node.modifierList.isEmpty()
        if (allParamsEmpty) {
            codeBlockBuilder.addStatement("%M {", lazyListMember)
        } else {
            codeBlockBuilder.addStatement("%M(", lazyListMember)
            codeBlockBuilder.add(
                lazyListParams.generateParamsCode(),
            )
            codeBlockBuilder.add(
                node.generateModifierCode(project, context, dryRun = dryRun),
            )
            codeBlockBuilder.addStatement(") {")
        }

        node.children.forEach { child ->
            val itemName =
                node.trait.value
                    .iconText()
                    .replaceSpaces()
                    .replaceFirstChar { it.lowercase() }
            when (val childParams = child.lazyListChildParams.value) {
                is LazyListChildParams.FixedNumber -> {
                    codeBlockBuilder.addStatement("items(count = ${childParams.numOfItems}) { ${itemName}Index -> ")
                    codeBlockBuilder.add(
                        child.generateCode(
                            project = project,
                            context = context,
                            dryRun = dryRun,
                        ),
                    )
                    codeBlockBuilder.addStatement("}")
                }

                is LazyListChildParams.DynamicItemsSource -> {
                    val dynamicItem =
                        project.findComposeNodeOrNull(childParams.composeNodeId)?.dynamicItems?.value
                    dynamicItem?.let {
                        val transformedItemCodeBlock =
                            dynamicItem.transformedCodeBlock(project, context, dryRun = dryRun)

                        when (dynamicItem) {
                            is ApiResultProperty -> {
                                codeBlockBuilder.add(
                                    CodeBlockWrapper.of(
                                        "%M(",
                                        MemberHolder.AndroidX.Lazy.itemsIndexed,
                                    ),
                                )
                                codeBlockBuilder.add(transformedItemCodeBlock)
                                codeBlockBuilder.add(") { ${itemName}Index, ${itemName}Item -> ")
                                codeBlockBuilder.addStatement(" ${itemName}Item.let { jsonElement -> ")
                            }

                            else -> {
                                codeBlockBuilder.add("%M(", itemsIndexedMember)
                                codeBlockBuilder.add(transformedItemCodeBlock)
                                codeBlockBuilder.add(") { ${itemName}Index, ${itemName}Item -> ")
                            }
                        }

                        codeBlockBuilder.add(
                            child.generateCode(
                                project = project,
                                context = context,
                                dryRun = dryRun,
                            ),
                        )
                        codeBlockBuilder.addStatement("}")
                        when (dynamicItem) {
                            is ApiResultProperty -> {
                                codeBlockBuilder.addStatement("}")
                            }

                            else -> {}
                        }
                    }
                }
            }
        }
        codeBlockBuilder.addStatement("}")
        return codeBlockBuilder.build()
    }
}

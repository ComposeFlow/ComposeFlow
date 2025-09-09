package io.composeflow.model.parameter

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.composeflow.Res
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper
import io.composeflow.kotlinpoet.wrapper.MemberNameWrapper
import io.composeflow.materialicons.Outlined
import io.composeflow.model.modifier.ModifierWrapper
import io.composeflow.model.modifier.generateModifierCode
import io.composeflow.model.palette.PaletteRenderParams
import io.composeflow.model.palette.TraitCategory
import io.composeflow.model.parameter.wrapper.AlignmentWrapper
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.property.StringProperty
import io.composeflow.override.mutableStateListEqualsOverrideOf
import io.composeflow.serializer.FallbackEnumSerializer
import io.composeflow.tooltip_card_trait
import io.composeflow.ui.CanvasNodeCallbacks
import io.composeflow.ui.modifierForCanvas
import io.composeflow.ui.zoomablecontainer.ZoomableContainerStateHolder
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource

@Serializable
@SerialName("CardTrait")
data class CardTrait(
    val cardType: CardType = CardType.Default,
) : ComposeTrait {
    override fun defaultComposeNode(project: Project): ComposeNode =
        ComposeNode(
            modifierList = defaultModifierList(),
            trait = mutableStateOf(CardTrait()),
        ).apply {
            addChild(
                ComposeNode(
                    trait = mutableStateOf(BoxTrait()),
                    modifierList =
                        mutableStateListEqualsOverrideOf(
                            ModifierWrapper.FillMaxSize(),
                            ModifierWrapper.Padding(16.dp),
                        ),
                ).apply {
                    addChild(
                        ComposeNode(
                            trait =
                                mutableStateOf(
                                    TextTrait(
                                        text = StringProperty.StringIntrinsicValue("Card content"),
                                    ),
                                ),
                            modifierList =
                                mutableStateListEqualsOverrideOf(
                                    ModifierWrapper.Align(AlignmentWrapper.BottomStart),
                                ),
                        ),
                    )
                    addChild(
                        ComposeNode(
                            trait =
                                mutableStateOf(
                                    IconTrait(imageVectorHolder = Outlined.MoreVert),
                                ),
                            modifierList =
                                mutableStateListEqualsOverrideOf(
                                    ModifierWrapper.Align(AlignmentWrapper.TopEnd),
                                ),
                        ),
                    )
                },
            )
        }

    override fun icon(): ImageVector = Icons.Outlined.Assessment

    override fun iconText(): String = "Card"

    override fun paletteCategories(): List<TraitCategory> =
        listOf(
            TraitCategory.Basic,
            TraitCategory.Container,
        )

    override fun tooltipResource(): StringResource = Res.string.tooltip_card_trait

    override fun defaultModifierList(): MutableList<ModifierWrapper> =
        mutableStateListEqualsOverrideOf(
            ModifierWrapper.Width(200.dp),
            ModifierWrapper.Height(200.dp),
            ModifierWrapper.Padding(all = 8.dp),
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
        when (cardType) {
            CardType.Default -> {
                Card(
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
                        child.RenderedNodeInCanvas(
                            project = project,
                            canvasNodeCallbacks = canvasNodeCallbacks,
                            paletteRenderParams = paletteRenderParams,
                            zoomableContainerStateHolder = zoomableContainerStateHolder,
                        )
                    }
                }
            }

            CardType.Elevated -> {
                ElevatedCard(
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
                        child.RenderedNodeInCanvas(
                            project = project,
                            canvasNodeCallbacks = canvasNodeCallbacks,
                            paletteRenderParams = paletteRenderParams,
                            zoomableContainerStateHolder = zoomableContainerStateHolder,
                        )
                    }
                }
            }

            CardType.Outlined -> {
                OutlinedCard(
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
                        child.RenderedNodeInCanvas(
                            project = project,
                            canvasNodeCallbacks = canvasNodeCallbacks,
                            paletteRenderParams = paletteRenderParams,
                            zoomableContainerStateHolder = zoomableContainerStateHolder,
                        )
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
        val codeBlockBuilder = CodeBlockWrapper.builder()
        val allParamsEmpty = areAllParamsEmpty() && node.modifierList.isEmpty()
        if (allParamsEmpty) {
            codeBlockBuilder.addStatement("%M {", cardType.toMemberName())
        } else {
            codeBlockBuilder.addStatement("%M(", cardType.toMemberName())
            codeBlockBuilder.add(
                node.generateModifierCode(project, context, dryRun = dryRun),
            )
            codeBlockBuilder.addStatement(") {")
        }
        node.children.forEach {
            codeBlockBuilder.add(
                it.generateCode(
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

object CardTypeSerializer : FallbackEnumSerializer<CardType>(CardType::class, CardType.entries.toTypedArray())

@Serializable(CardTypeSerializer::class)
enum class CardType {
    Default {
        override fun toMemberName(): MemberNameWrapper = MemberNameWrapper.get("androidx.compose.material3", "Card")
    },
    Elevated {
        override fun toMemberName(): MemberNameWrapper = MemberNameWrapper.get("androidx.compose.material3", "ElevatedCard")
    },
    Outlined {
        override fun toMemberName(): MemberNameWrapper = MemberNameWrapper.get("androidx.compose.material3", "OutlinedCard")
    },
    ;

    abstract fun toMemberName(): MemberNameWrapper
}

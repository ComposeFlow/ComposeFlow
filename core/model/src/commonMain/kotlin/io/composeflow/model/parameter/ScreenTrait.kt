package io.composeflow.model.parameter

import androidx.compose.ui.graphics.vector.ImageVector
import io.composeflow.Res
import io.composeflow.custom.ComposeFlowIcons
import io.composeflow.custom.composeflowicons.Smartphone
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper
import io.composeflow.model.action.ActionType
import io.composeflow.model.palette.TraitCategory
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.tooltip_screen_trait
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource

@Serializable
@SerialName("ScreenTrait")
data object ScreenTrait : ComposeTrait {
    override fun icon(): ImageVector = ComposeFlowIcons.Smartphone

    override fun iconText(): String = "Screen"

    override fun paletteCategories(): List<TraitCategory> = listOf(TraitCategory.Container)

    override fun tooltipResource(): StringResource = Res.string.tooltip_screen_trait

    override fun visibleInPalette(): Boolean = false

    override fun isDroppable(): Boolean = true

    override fun isVisibilityConditional(): Boolean = false

    override fun actionTypes(): List<ActionType> = listOf(ActionType.OnInitialLoad)

    override fun canBeAddedAsChildren(): Boolean = false

    override fun isModifierAttachable(): Boolean = false

    override fun generateCode(
        project: Project,
        node: ComposeNode,
        context: GenerationContext,
        dryRun: Boolean,
    ): CodeBlockWrapper = CodeBlockWrapper.of("")
}

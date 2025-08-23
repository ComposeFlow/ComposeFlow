package io.composeflow.model.parameter

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.ui.graphics.vector.ImageVector
import io.composeflow.Res
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper
import io.composeflow.model.palette.TraitCategory
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.tooltip_tab_row_trait
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource

@Serializable
@SerialName("TabRowTrait")
data class TabRowTrait(
    val scrollable: Boolean = false,
) : ComposeTrait {
    override fun icon(): ImageVector = Icons.Outlined.TableChart

    override fun iconText(): String = "TabRow"

    override fun paletteCategories(): List<TraitCategory> = listOf(TraitCategory.Container)

    override fun tooltipResource(): StringResource = Res.string.tooltip_tab_row_trait

    override fun visibleInPalette(): Boolean = false

    override fun isDroppable(): Boolean = false

    override fun isEditable(): Boolean = false

    override fun generateCode(
        project: Project,
        node: ComposeNode,
        context: GenerationContext,
        dryRun: Boolean,
    ): CodeBlockWrapper {
        // TabRow code is generated in TabsTrait
        return CodeBlockWrapper.builder().build()
    }
}

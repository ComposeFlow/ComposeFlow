package io.composeflow.model.parameter

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.ui.graphics.vector.ImageVector
import io.composeflow.Res
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper
import io.composeflow.kotlinpoet.wrapper.MemberNameWrapper
import io.composeflow.materialicons.ImageVectorHolder
import io.composeflow.model.modifier.generateModifierCode
import io.composeflow.model.palette.TraitCategory
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.property.StringProperty
import io.composeflow.model.type.ComposeFlowType
import io.composeflow.tooltip_tab_trait
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource

@Serializable
@SerialName("TabTrait")
data class TabTrait(
    val text: StringProperty? = null,
    val icon: ImageVectorHolder? = null,
    val enabled: Boolean? = null,
    val index: Int = 0, // index of this tab within parent TabRow (or other TabRow variants)
    var selectedTabIndexVariableName: String = "selectedIndex",
) : ComposeTrait {
    private fun generateParamsCode(
        project: Project,
        context: GenerationContext,
        dryRun: Boolean,
    ): CodeBlockWrapper {
        val codeBlockBuilder = CodeBlockWrapper.builder()
        codeBlockBuilder.add("selected = $selectedTabIndexVariableName == $index,")
        codeBlockBuilder.add(
            """
            onClick = { 
              $selectedTabIndexVariableName = $index
            },
        """,
        )
        text?.let {
            codeBlockBuilder.add(
                "text = { %M(",
                MemberNameWrapper.get("androidx.compose.material3", "Text"),
            )
            codeBlockBuilder.add(
                text.transformedCodeBlock(
                    project,
                    context,
                    ComposeFlowType.StringType(),
                    dryRun = dryRun,
                ),
            )
            codeBlockBuilder.addStatement(") },")
        }
        icon?.let {
            val iconMember = MemberNameWrapper.get("androidx.compose.material3", "Icon")
            val iconsMember = MemberNameWrapper.get("androidx.compose.material.icons", "Icons")
            val imageVectorMember =
                MemberNameWrapper.get(
                    "androidx.compose.material.icons.${icon.packageDescriptor}",
                    icon.name,
                )
            codeBlockBuilder
                .addStatement(
                    """icon = { 
                          %M(imageVector = %M.${icon.memberDescriptor}.%M,
                             contentDescription = null,
                          ) },""",
                    iconMember,
                    iconsMember,
                    imageVectorMember,
                )
        }
        return codeBlockBuilder.build()
    }

    override fun icon(): ImageVector = Icons.AutoMirrored.Outlined.Notes

    override fun iconText(): String = "Tab"

    override fun paletteCategories(): List<TraitCategory> = listOf()

    override fun tooltipResource(): StringResource = Res.string.tooltip_tab_trait

    override fun visibleInPalette(): Boolean = false

    override fun isResizeable(): Boolean = false

    override fun isEditable(): Boolean = false

    override fun generateCode(
        project: Project,
        node: ComposeNode,
        context: GenerationContext,
        dryRun: Boolean,
    ): CodeBlockWrapper {
        val codeBlockBuilder = CodeBlockWrapper.builder()
        codeBlockBuilder.addStatement(
            "%M(",
            MemberNameWrapper.get("androidx.compose.material3", "Tab"),
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

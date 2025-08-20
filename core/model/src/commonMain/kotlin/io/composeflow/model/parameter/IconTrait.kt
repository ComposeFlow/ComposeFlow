package io.composeflow.model.parameter

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.vector.ImageVector
import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper
import io.composeflow.kotlinpoet.wrapper.MemberNameWrapper
import io.composeflow.Res
import io.composeflow.cloud.storage.BlobInfoWrapper
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.materialicons.ImageVectorHolder
import io.composeflow.materialicons.Outlined
import io.composeflow.model.modifier.generateModifierCode
import io.composeflow.model.palette.TraitCategory
import io.composeflow.model.parameter.wrapper.ColorWrapper
import io.composeflow.model.parameter.wrapper.Material3ColorWrapper
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.property.AssignableProperty
import io.composeflow.model.property.ColorProperty
import io.composeflow.tooltip_icon_trait
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource

@Serializable
@SerialName("IconTrait")
data class IconTrait(
    override val assetType: IconAssetType = IconAssetType.Material,
    override val imageVectorHolder: ImageVectorHolder? = Outlined.Add,
    override val blobInfoWrapper: BlobInfoWrapper? = null,
    override val contentDescription: String = "Icon for ${imageVectorHolder?.name}",
    override val tint: AssignableProperty? =
        ColorProperty.ColorIntrinsicValue(
            value =
                ColorWrapper(
                    themeColor = Material3ColorWrapper.OnSurface,
                ),
        ),
) : AbstractIconTrait(assetType, imageVectorHolder, blobInfoWrapper, contentDescription, tint),
    ComposeTrait {
    override fun defaultComposeNode(project: Project): ComposeNode =
        ComposeNode(
            trait = mutableStateOf(defaultTrait()),
            modifierList = defaultModifierList(),
        )

    override fun icon(): ImageVector = Icons.Outlined.Add

    override fun iconText(): String = "Icon"

    override fun tooltipResource(): StringResource = Res.string.tooltip_icon_trait

    override fun isResizeable(): Boolean = false

    override fun paletteCategories(): List<TraitCategory> = listOf(TraitCategory.Common, TraitCategory.Basic)

    override fun generateCode(
        project: Project,
        node: ComposeNode,
        context: GenerationContext,
        dryRun: Boolean,
    ): CodeBlockWrapper {
        val codeBlockBuilder = CodeBlockWrapper.builder()

        if (node.actionHandler.allActionNodes().isNotEmpty()) {
            val iconButtonMember = MemberNameWrapper.get("androidx.compose.material3", "IconButton")
            // actual onClick code is written in the modifier in Icon
            codeBlockBuilder.addStatement("%M(onClick = {}) {", iconButtonMember)
        }
        val iconMember = MemberNameWrapper.get("androidx.compose.material3", "Icon")
        codeBlockBuilder.addStatement("%M(", iconMember)
        codeBlockBuilder.add(
            generateIconParamsCode(
                project = project,
                context = context,
                dryRun,
            ),
        )
        codeBlockBuilder.add(
            node.generateModifierCode(project, context, dryRun = dryRun),
        )
        codeBlockBuilder.addStatement(")")

        if (node.actionHandler.allActionNodes().isNotEmpty()) {
            // Close the lambda for IconButton
            codeBlockBuilder.addStatement("}")
        }
        return codeBlockBuilder.build()
    }

    companion object {
        fun defaultTrait(): IconTrait = IconTrait(imageVectorHolder = Outlined.Add)
    }
}

package io.composeflow.model.parameter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.composeflow.Res
import io.composeflow.custom.ComposeFlowIcons
import io.composeflow.custom.composeflowicons.Google
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.kotlinpoet.MemberHolder
import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper
import io.composeflow.kotlinpoet.wrapper.MemberNameWrapper
import io.composeflow.model.action.ActionType
import io.composeflow.model.modifier.ModifierWrapper
import io.composeflow.model.modifier.generateModifierCode
import io.composeflow.model.palette.PaletteRenderParams
import io.composeflow.model.palette.TraitCategory
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.override.mutableStateListEqualsOverrideOf
import io.composeflow.tooltip_google_sign_in_button_trait
import io.composeflow.ui.CanvasNodeCallbacks
import io.composeflow.ui.button.GoogleSignInButton
import io.composeflow.ui.button.GoogleSignInButtonIconOnly
import io.composeflow.ui.modifierForCanvas
import io.composeflow.ui.zoomablecontainer.ZoomableContainerStateHolder
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource

@Serializable
@SerialName("GoogleSignInButtonTrait")
data class GoogleSignInButtonTrait(
    val iconOnly: Boolean = false,
) : ComposeTrait {
    private fun generateParamsCode(
        project: Project,
        node: ComposeNode,
        context: GenerationContext,
        dryRun: Boolean,
    ): CodeBlockWrapper {
        val codeBlockBuilder = CodeBlockWrapper.builder()
        codeBlockBuilder.addStatement("onClick = {")
        node.actionsMap[ActionType.OnClick]?.forEach {
            codeBlockBuilder.add(it.generateCodeBlock(project, context, dryRun = dryRun))
        }
        codeBlockBuilder.addStatement("},")

        val validateResultBuilder = CodeBlockWrapper.builder()
        val validatorNodes =
            node.getDependentValidatorNodesForActionType(project, ActionType.OnClick)
        validatorNodes.forEachIndexed { index, validatorNode ->
            val validateResultName =
                validatorNode.getCompanionStateOrNull(project)?.getValidateResultName(context)
            validateResultBuilder.add(
                CodeBlockWrapper.of(
                    "$validateResultName.%M()",
                    MemberHolder.ComposeFlow.isSuccess,
                ),
            )
            if (index != validatorNodes.lastIndex) {
                validateResultBuilder.add(" && ")
            }
        }

        return codeBlockBuilder.build()
    }

    override fun defaultComposeNode(project: Project): ComposeNode =
        ComposeNode(
            trait = mutableStateOf(GoogleSignInButtonTrait()),
            modifierList =
                mutableStateListEqualsOverrideOf(
                    ModifierWrapper.Padding(8.dp),
                ),
        )

    override fun iconText(): String = "GoogleSignIn"

    override fun icon(): ImageVector = ComposeFlowIcons.Google

    override fun paletteCategories(): List<TraitCategory> = listOf(TraitCategory.Auth)

    override fun tooltipResource(): StringResource = Res.string.tooltip_google_sign_in_button_trait

    override fun actionTypes(): List<ActionType> = listOf(ActionType.OnClick)

    override fun onClickIncludedInParams(): Boolean = true

    override fun isResizeable(): Boolean = false

    @Composable
    override fun RenderedNode(
        project: Project,
        node: ComposeNode,
        canvasNodeCallbacks: CanvasNodeCallbacks,
        paletteRenderParams: PaletteRenderParams,
        zoomableContainerStateHolder: ZoomableContainerStateHolder,
        modifier: Modifier,
    ) {
        val modifierForCanvas =
            modifier.then(
                node
                    .modifierChainForCanvas()
                    .modifierForCanvas(
                        project = project,
                        node = node,
                        canvasNodeCallbacks = canvasNodeCallbacks,
                        zoomableContainerStateHolder = zoomableContainerStateHolder,
                        paletteRenderParams = paletteRenderParams,
                    ),
            )
        if (iconOnly) {
            GoogleSignInButtonIconOnly(
                onClick = {},
                modifier = modifierForCanvas,
            )
        } else {
            GoogleSignInButton(
                onClick = {},
                fontSize = 18.sp,
                modifier = modifierForCanvas,
            )
        }
    }

    override fun generateCode(
        project: Project,
        node: ComposeNode,
        context: GenerationContext,
        dryRun: Boolean,
    ): CodeBlockWrapper {
        val buttonMember =
            if (iconOnly) {
                MemberNameWrapper.get(
                    "com.mmk.kmpauth.uihelper.google",
                    "GoogleSignInButtonIconOnly",
                )
            } else {
                MemberNameWrapper.get("com.mmk.kmpauth.uihelper.google", "GoogleSignInButton")
            }
        val builder = CodeBlockWrapper.builder()
        builder.add("%M(", buttonMember)
        builder.add(
            generateParamsCode(
                project = project,
                node = node,
                context = context,
                dryRun = dryRun,
            ),
        )
        builder.add(node.generateModifierCode(project, context, dryRun = dryRun))
        builder.addStatement(")")
        return builder.build()
    }
}

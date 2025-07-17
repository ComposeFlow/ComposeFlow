package io.composeflow.ui

import io.composeflow.model.palette.Constraint.Companion.ONLY_SCREEN_IS_ALLOWED
import io.composeflow.model.palette.Constraint.Companion.SAME_NODE_EXISTS_IN_SCREEN
import io.composeflow.model.palette.TraitCategory
import io.composeflow.model.parameter.BottomAppBarTrait
import io.composeflow.model.parameter.FabTrait
import io.composeflow.model.parameter.NavigationDrawerTrait
import io.composeflow.model.parameter.TopAppBarTrait
import io.composeflow.model.project.CanvasEditable
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.Screen
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode

object UiBuilderHelper {
    fun checkIfNodeCanBeAddedDueToScreenOnlyNode(
        currentEditable: CanvasEditable,
        composeNode: ComposeNode,
    ): String? {
        if (TraitCategory.ScreenOnly in composeNode.trait.value.paletteCategories()) {
            if (currentEditable !is Screen) {
                return ONLY_SCREEN_IS_ALLOWED
            }

            if ((composeNode.trait.value is FabTrait && currentEditable.fabNode.value != null) ||
                (composeNode.trait.value is TopAppBarTrait && currentEditable.topAppBarNode.value != null) ||
                (composeNode.trait.value is BottomAppBarTrait && currentEditable.bottomAppBarNode.value != null) ||
                (composeNode.trait.value is NavigationDrawerTrait && currentEditable.navigationDrawerNode.value != null)
            ) {
                return String.format(
                    SAME_NODE_EXISTS_IN_SCREEN,
                    composeNode.trait.value.iconText(),
                )
            }
        }
        return null
    }

    fun addNodeToCanvasEditable(
        project: Project,
        containerNode: ComposeNode,
        composeNode: ComposeNode,
        canvasEditable: CanvasEditable,
        indexToDrop: Int?,
    ) {
        when (composeNode.trait.value) {
            is FabTrait -> {
                (canvasEditable as? Screen)?.fabNode?.value = composeNode
            }

            is TopAppBarTrait -> {
                (canvasEditable as? Screen)?.topAppBarNode?.value = composeNode
            }

            is BottomAppBarTrait -> {
                (canvasEditable as? Screen)?.bottomAppBarNode?.value = composeNode
            }

            is NavigationDrawerTrait -> {
                (canvasEditable as? Screen)?.navigationDrawerNode?.value = composeNode
            }

            else -> {
                composeNode.trait.value.onAttachStateToNode(
                    project = project,
                    stateHolder = project.screenHolder.currentEditable(),
                    node = composeNode,
                )
                indexToDrop?.let {
                    containerNode.insertChildAt(index = indexToDrop, composeNode)
                } ?: containerNode.addChild(composeNode)
            }
        }
    }
}

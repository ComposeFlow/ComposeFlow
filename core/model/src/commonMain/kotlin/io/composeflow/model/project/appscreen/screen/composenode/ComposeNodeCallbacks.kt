package io.composeflow.model.project.appscreen.screen.composenode

import io.composeflow.model.action.ActionNode
import io.composeflow.model.action.ActionType
import io.composeflow.model.apieditor.ApiDefinition
import io.composeflow.model.modifier.ModifierWrapper
import io.composeflow.model.parameter.ComposeTrait
import io.composeflow.model.parameter.lazylist.LazyListChildParams
import io.composeflow.model.project.CanvasEditable
import io.composeflow.model.project.ParameterWrapper
import io.composeflow.model.project.component.Component
import io.composeflow.model.property.AssignableProperty
import io.composeflow.ui.EventResult

data class ComposeNodeCallbacks(
    val onDynamicItemsUpdated: (ComposeNode, AssignableProperty?) -> Unit,
    val onLazyListChildParamsUpdated: (ComposeNode, LazyListChildParams) -> Unit,
    val onTraitUpdated: (ComposeNode, ComposeTrait) -> Unit,
    val onParamsUpdatedWithLazyListSource: (ComposeNode, ComposeTrait, lazyListChildParams: LazyListChildParams?) -> EventResult,
    val onVisibilityParamsUpdated: (ComposeNode, VisibilityParams) -> Unit,
    val onWrapWithContainerComposable: (ComposeNode, ComposeTrait) -> Unit,
    val onActionsMapUpdated: (ComposeNode, MutableMap<ActionType, MutableList<ActionNode>>) -> Unit,
    val onModifierUpdatedAt: (ComposeNode, Int, ModifierWrapper) -> Unit,
    val onModifierRemovedAt: suspend (ComposeNode, Int) -> Unit,
    val onModifierAdded: (ComposeNode, ModifierWrapper) -> Unit,
    val onModifierSwapped: suspend (ComposeNode, Int, Int) -> Unit,
    val onCreateComponent: (String) -> Unit,
    val onEditComponent: (Component) -> Unit,
    val onRemoveComponent: (Component) -> Unit,
    val onAddParameterToCanvasEditable: (CanvasEditable, ParameterWrapper<*>) -> Unit,
    val onUpdateParameterInCanvasEditable: (CanvasEditable, ParameterWrapper<*>) -> Unit,
    val onRemoveParameterFromCanvasEditable: (canvasEditable: CanvasEditable, parameter: ParameterWrapper<*>) -> Unit,
    val onApiUpdated: (updatedApi: ApiDefinition) -> Unit,
    val onComposeNodeLabelUpdated: (composable: ComposeNode, label: String) -> Unit,
)

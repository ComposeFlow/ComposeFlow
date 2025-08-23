package io.composeflow.model.parameter

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.composeflow.Res
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper
import io.composeflow.model.action.ActionType
import io.composeflow.model.modifier.ModifierWrapper
import io.composeflow.model.palette.Constraint
import io.composeflow.model.palette.PaletteDraggable
import io.composeflow.model.palette.PaletteRenderParams
import io.composeflow.model.palette.TraitCategory
import io.composeflow.model.parameter.lazylist.LazyGridCells
import io.composeflow.model.project.CanvasEditable
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.project.issue.Issue
import io.composeflow.model.property.AssignableProperty
import io.composeflow.model.property.PropertyContainer
import io.composeflow.model.property.ValueFromCompanionState
import io.composeflow.model.property.ValueFromState
import io.composeflow.model.state.ReadableState
import io.composeflow.model.state.ScreenState
import io.composeflow.model.state.StateHolder
import io.composeflow.model.validator.ComposeStateValidator
import io.composeflow.override.mutableStateListEqualsOverrideOf
import io.composeflow.tooltip_column_trait
import io.composeflow.ui.CanvasNodeCallbacks
import io.composeflow.ui.zoomablecontainer.ZoomableContainerStateHolder
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource

@Serializable
sealed interface ComposeTrait : PaletteDraggable {
    /**
     * True if all parameters are empty. Used for determining if the parentheses are omitted when
     * invoking [generateCode].
     */
    fun areAllParamsEmpty(): Boolean = false

    /**
     * Return the state validator if any validator is set to the state, e.g. TextFieldValidator
     */
    fun getStateValidator(): ComposeStateValidator? = null

    /**
     * Get the list of [ProperyContainer]s used in this ComposeParams
     */
    fun getPropertyContainers(): List<PropertyContainer> = emptyList()

    fun generateIssues(project: Project): List<Issue> = emptyList()

    /**
     * Attach any read/write states to the [node]. This is to attach states from the method which
     * is composition aware.
     */
    fun onAttachStateToNode(
        project: Project,
        stateHolder: StateHolder,
        node: ComposeNode,
    ) {
    }

    fun defaultModifierList(): MutableList<ModifierWrapper> = mutableStateListEqualsOverrideOf(ModifierWrapper.Padding(all = 8.dp))

    fun defaultConstraints(): Set<Constraint> = emptySet()

    fun isModifierAttachable(): Boolean = true

    override fun defaultComposeNode(project: Project): ComposeNode? {
        TODO("Not yet implemented")
    }

    /**
     * Composable when rendered in the canvas. In comparison to [defaultComposeNode] function,
     * this method can be rendered with a specific modifier only for the editor canvas.
     * For example enabling the Composable to be draggable within the canvas.
     *
     * @param node the [ComposeNode] to render
     * @param paletteRenderParams parameters that configure the rendered behavior in the
     *        canvas
     */
    @Composable
    fun RenderedNode(
        project: Project,
        node: ComposeNode,
        canvasNodeCallbacks: CanvasNodeCallbacks,
        paletteRenderParams: PaletteRenderParams,
        zoomableContainerStateHolder: ZoomableContainerStateHolder,
        modifier: Modifier,
    ) = Unit

    /**
     * Returns true if the node is able to have dynamic items (such as LazyColumn, LazyRow)
     */
    fun hasDynamicItems(): Boolean = false

    /**
     * Set to false if this node isn't able to accept a dropped node.
     * For example, ChipGroup is only able to add/remove its children through the Inspector.
     */
    fun isDroppable(): Boolean = true

    fun isResizeable(): Boolean = true

    /**
     * Set to false if the node isn't able to be edited, for example, wrapped by a container or
     * add a modifier to avoid the built-in hierarchy becomes broken.
     * For example, Tabs, TabRow, Tab, TabContent have fixed relationships.
     */
    fun isEditable(): Boolean = true

    /**
     * Set to true if the visibility can be set conditionally (depending on other states dynamically).
     * Set to false for the components its visibilities are determined constantly like
     * BottomAppBar, TopAppBar.
     */
    fun isVisibilityConditional(): Boolean = true

    /**
     * True if this node is visible in the palette
     */
    fun visibleInPalette(): Boolean = true

    fun isLazyList(): Boolean = false

    /**
     * Indicates if the node having the trait can be added as children of another node
     */
    fun canBeAddedAsChildren(): Boolean = true

    /**
     * True if onClick parameter is included as part of the parameters for the Composable.
     * This is to distinguish whether the onClick action is set to the Composable through the
     * onClick parameter or through a modifier.
     */
    fun onClickIncludedInParams(): Boolean = false

    fun actionTypes(): List<ActionType> =
        listOf(
            ActionType.OnClick,
            ActionType.OnDoubleClick,
            ActionType.OnLongClick,
        )

    fun generateCode(
        project: Project,
        node: ComposeNode,
        context: GenerationContext,
        dryRun: Boolean,
    ): CodeBlockWrapper

    /**
     * Defines a companion state if the trait needs a state for ComposeTraits that need to hold
     * some state.
     * For example, a TextFieldTrait needs a StringScreenState for storing the input value in the
     * TextField.
     */
    fun companionState(composeNode: ComposeNode): ScreenState<*>? = null

    /**
     * Update the properties if [ValueFromCompanionState] is used for any of the properties.
     * The ValueFromCompanionState doesn't store any ID for state or composeNode to reduce the
     * implication. So the composeNode reference needs to be passed at runtime.
     */
    fun updateCompanionStateProperties(composeNode: ComposeNode) {}

    fun AssignableProperty.findReadableState(
        project: Project,
        canvasEditable: CanvasEditable?,
        node: ComposeNode,
    ): ReadableState? =
        when (this) {
            is ValueFromState -> {
                canvasEditable?.findStateOrNull(project, readFromStateId)
            }

            is ValueFromCompanionState -> {
                companionState(node)
            }

            else -> null
        }

    companion object {
        const val NUM_OF_ITEMS_IN_LAZY_LIST = 1
        const val NUM_OF_ITEMS_IN_PAGER = 1
        const val NUM_OF_DEFAULT_TABS = 3
    }
}

interface LazyListTrait : ComposeTrait {
    var defaultChildNumOfItems: Int

    fun generateParamsCode(): CodeBlockWrapper
}

interface PagerTrait : ComposeTrait {
    var defaultChildNumOfItems: Int

    fun generateParamsCode(): CodeBlockWrapper
}

interface LazyGridTrait : LazyListTrait {
    val lazyGridCells: LazyGridCells
}

val ComposeTrait.Companion.entries: List<ComposeTrait>
    get() =
        listOf(
            TextTrait(),
            IconTrait(),
            ImageTrait(),
            ButtonTrait(),
            TextFieldTrait(),
            RowTrait(),
            ColumnTrait(),
            BoxTrait(),
            LazyColumnTrait(),
            LazyRowTrait(),
            LazyVerticalGridTrait(),
            LazyHorizontalGridTrait(),
            HorizontalPagerTrait(),
            CardTrait(),
            TabsTrait,
            TabRowTrait(),
            TabTrait(),
            TabContentTrait,
            ChipGroupTrait(),
            TopAppBarTrait(),
            BottomAppBarTrait(),
            NavigationDrawerTrait(),
            NavigationDrawerItemTrait(),
            FabTrait(),
            HorizontalDividerTrait(),
            VerticalDividerTrait(),
            SpacerTrait,
            SwitchTrait(),
            CheckboxTrait(),
            SliderTrait(),
            DropdownTrait(),
            ComponentTrait(),
            ScreenTrait,
            GoogleSignInButtonTrait(),
        )

@Serializable
@SerialName("EmptyTrait")
data object EmptyTrait : ComposeTrait {
    override fun icon(): ImageVector = Icons.Filled.Clear

    override fun iconText(): String = "Empty"

    override fun paletteCategories(): List<TraitCategory> = emptyList()

    override fun tooltipResource(): StringResource = Res.string.tooltip_column_trait

    override fun generateCode(
        project: Project,
        node: ComposeNode,
        context: GenerationContext,
        dryRun: Boolean,
    ): CodeBlockWrapper = CodeBlockWrapper.of("")

    override fun defaultComposeNode(project: Project): ComposeNode? = null
}

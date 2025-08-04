package io.composeflow.ui.uibuilder

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.PointerMatcher
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.onClick
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BorderBottom
import androidx.compose.material.icons.filled.BorderClear
import androidx.compose.material.icons.filled.BorderOuter
import androidx.compose.material.icons.filled.ModeNight
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.CenterFocusStrong
import androidx.compose.material.icons.outlined.DesktopMac
import androidx.compose.material.icons.outlined.Monitor
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.TabletAndroid
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DismissibleNavigationDrawer
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.isMetaPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.res.useResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.shimmer
import io.composeflow.Res
import io.composeflow.ai.AiAssistantUiState
import io.composeflow.ai_login_needed
import io.composeflow.ai_open_assistant
import io.composeflow.auth.LocalFirebaseIdToken
import io.composeflow.auth.isAiEnabled
import io.composeflow.back_to_screen
import io.composeflow.borders_hide
import io.composeflow.borders_show
import io.composeflow.chane_to_day_theme
import io.composeflow.chane_to_night_theme
import io.composeflow.component
import io.composeflow.component_name
import io.composeflow.custom.ComposeFlowIcons
import io.composeflow.custom.composeflowicons.NounAi
import io.composeflow.keyboard.getCtrlKeyStr
import io.composeflow.model.InspectorTabDestination
import io.composeflow.model.modifier.ModifierWrapper
import io.composeflow.model.modifier.toModifierChain
import io.composeflow.model.palette.PaletteNodeCallbacks
import io.composeflow.model.palette.PaletteRenderParams
import io.composeflow.model.parameter.FabPositionWrapper
import io.composeflow.model.parameter.FabTrait
import io.composeflow.model.parameter.NavigationDrawerTrait
import io.composeflow.model.parameter.NavigationDrawerType
import io.composeflow.model.project.CanvasEditable
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.Screen
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNodeCallbacks
import io.composeflow.model.project.component.Component
import io.composeflow.model.project.issue.DestinationContext
import io.composeflow.model.project.issue.NavigatableDestination
import io.composeflow.palette
import io.composeflow.platform.AsyncImage
import io.composeflow.remove_navigation
import io.composeflow.reset_position_and_zoom
import io.composeflow.show_navigation
import io.composeflow.template.ScreenTemplatePair
import io.composeflow.ui.CanvasNodeCallbacks
import io.composeflow.ui.FormFactor
import io.composeflow.ui.LocalOnAllDialogsClosed
import io.composeflow.ui.LocalOnAnyDialogIsShown
import io.composeflow.ui.LocalOnShowSnackbar
import io.composeflow.ui.Tooltip
import io.composeflow.ui.adaptive.ProvideDeviceSizeDp
import io.composeflow.ui.adaptive.computeNavigationSuiteType
import io.composeflow.ui.background.DotPatternBackground
import io.composeflow.ui.calculateScale
import io.composeflow.ui.common.AppTheme
import io.composeflow.ui.common.ProvideAppThemeTokens
import io.composeflow.ui.component.ComponentBuilderTab
import io.composeflow.ui.deviceframe.IPadTabletFrame
import io.composeflow.ui.deviceframe.PixelPhoneFrame
import io.composeflow.ui.drawLabel
import io.composeflow.ui.handleMessages
import io.composeflow.ui.icon.ComposeFlowIcon
import io.composeflow.ui.inspector.InspectorTab
import io.composeflow.ui.inspector.modifier.AddModifierDialog
import io.composeflow.ui.jewel.SplitLayoutState
import io.composeflow.ui.jewel.StatefulHorizontalSplitLayout
import io.composeflow.ui.jewel.rememberSplitLayoutState
import io.composeflow.ui.modifier.backgroundContainerNeutral
import io.composeflow.ui.modifier.hoverIconClickable
import io.composeflow.ui.modifierForCanvas
import io.composeflow.ui.nodetree.ComposeNodeTree
import io.composeflow.ui.palette.PaletteIcon
import io.composeflow.ui.palette.PaletteTab
import io.composeflow.ui.popup.SingleTextInputDialog
import io.composeflow.ui.screenbuilder.ScreenBuilderTab
import io.composeflow.ui.tab.ComposeFlowTab
import io.composeflow.ui.uibuilder.onboarding.OnboardingManager
import io.composeflow.ui.uibuilder.onboarding.OnboardingOverlay
import io.composeflow.ui.uibuilder.onboarding.TargetArea
import io.composeflow.ui.uibuilder.onboarding.onboardingTarget
import io.composeflow.ui.uibuilder.onboarding.rememberOnboardingManager
import io.composeflow.ui.zoomablecontainer.ZoomableContainerStateHolder
import io.composeflow.ui.zoomablecontainer.calculateAdjustedBoundsInZoomableContainer
import io.composeflow.zoom_in
import io.composeflow.zoom_out
import kotlinx.coroutines.CoroutineScope
import moe.tlaster.precompose.viewmodel.viewModel
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.jewel.ui.component.VerticalSplitLayout
import kotlin.math.roundToInt

@Composable
fun UiBuilderScreen(
    project: Project,
    aiAssistantUiState: AiAssistantUiState,
    onUpdateProject: (Project) -> Unit,
    screenMaxSize: Size,
    onToggleVisibilityOfAiChatDialog: () -> Unit,
) {
    val firebaseIdToken = LocalFirebaseIdToken.current
    val viewModel =
        viewModel(modelClass = UiBuilderViewModel::class) {
            UiBuilderViewModel(
                firebaseIdToken = firebaseIdToken,
                project = project,
                onUpdateProject = onUpdateProject,
            )
        }
    val editingProject = viewModel.editingProject.collectAsState().value
    editingProject.screenHolder.pendingDestinationContext?.let {
        (it as? DestinationContext.UiBuilderScreen)?.let { destination ->
            viewModel.onSetPendingFocus(destination)
        }
    }

    // Initialize onboarding manager
    val onboardingManager = rememberOnboardingManager(viewModel.settings)

    val coroutineScope = rememberCoroutineScope()
    val currentEditable = project.screenHolder.currentEditable()
    val draggedNode = viewModel.draggedNode
    val copiedNodes = viewModel.copiedNodes
    val currentFormFactor = viewModel.formFactor
    val canvasAreaUiState by viewModel.canvasAreaUiState.collectAsState()
    val mainViewUiState by viewModel.mainViewUiState.collectAsState()
    var dragStartPosition by remember { mutableStateOf(Offset.Zero) }
    var selectedInspectorDestination: InspectorTabDestination? by remember { mutableStateOf(null) }

    val onShowSnackbar = LocalOnShowSnackbar.current
    val composeNodeCallbacks =
        ComposeNodeCallbacks(
            onDynamicItemsUpdated = viewModel::onDynamicItemsUpdated,
            onLazyListChildParamsUpdated = viewModel::onLazyListChildParamsUpdated,
            onTraitUpdated = viewModel::onParamsUpdated,
            onParamsUpdatedWithLazyListSource = viewModel::onParamsUpdatedWithLazyListSource,
            onVisibilityParamsUpdated = viewModel::onVisibilityParamsUpdated,
            onWrapWithContainerComposable = viewModel::onWrapWithContainerComposable,
            onActionsMapUpdated = viewModel::onActionsMapUpdated,
            onModifierUpdatedAt = viewModel::onModifierUpdatedAt,
            onModifierRemovedAt = viewModel::onModifierRemovedAt,
            onModifierAdded = viewModel::onModifierAdded,
            onModifierSwapped = viewModel::onModifierSwapped,
            onCreateComponent = viewModel::onCreateComponent,
            onEditComponent = viewModel::onEditComponent,
            onRemoveComponent = {
                val result = viewModel.onRemoveComponent(it)
                result.handleMessages(onShowSnackbar, coroutineScope)
            },
            onAddParameterToCanvasEditable = viewModel::onAddParameterToCanvasEditable,
            onUpdateParameterInCanvasEditable = viewModel::onUpdateParameterInCanvasEditable,
            onRemoveParameterFromCanvasEditable = viewModel::onRemoveParameterFromCanvasEditable,
            onApiUpdated = viewModel::onApiUpdated,
            onComposeNodeLabelUpdated = viewModel::onComposeNodeLabelUpdated,
        )

    val canvasNodeCallbacks =
        CanvasNodeCallbacks(
            onBoundsInNodeUpdated = viewModel::onBoundsInNodeUpdated,
            onDraggedNodeUpdated = viewModel::onDraggedNodeUpdated,
            onDraggedPositionUpdated = viewModel::onDraggedPositionUpdated,
            onDragEnd = viewModel::onDragEnd,
            onNodeDropToPosition = viewModel::onNodeDropToPosition,
            onUndo = viewModel::onUndo,
            onRedo = viewModel::onRedo,
            onKeyPressed = viewModel::onKeyPressed,
            onDoubleTap = viewModel::onDoubleTap,
            onPopEditedComponent = viewModel::onPopEditedComponent,
            onCopyFocusedNode = viewModel::onCopyFocusedNodes,
            onPaste = viewModel::onPaste,
            onDeleteFocusedNode = viewModel::onDeleteKey,
            onBringToFront = viewModel::onBringToFront,
            onSendToBack = viewModel::onSendToBack,
            onPendingHeightModifierCommitted = viewModel::onPendingHeightModifierCommitted,
            onPendingWidthModifierCommitted = viewModel::onPendingWidthModifierCommitted,
            onConvertToComponent = viewModel::onConvertToComponent,
            onFormFactorChanged = viewModel::onFormFactorChanged,
            onUiBuilderCanvasSizeChanged = viewModel::onUiBuilderCanvasSizeChanged,
        )
    val paletteNodeCallbacks =
        PaletteNodeCallbacks(
            onComposableDroppedToTarget = { dropPosition, node ->
                val eventResult =
                    viewModel.onComposableDroppedToTarget(
                        dropPosition,
                        node,
                    )
                eventResult.handleMessages(onShowSnackbar, coroutineScope)
            },
            onDraggedNodeUpdated = viewModel::onDraggedNodeUpdated,
            onDraggedPositionUpdated = viewModel::onDraggedPositionUpdated,
            onDragEnd = viewModel::onDragEnd,
        )
    val zoomableContainerStateHolder = viewModel.zoomableContainerStateHolder
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(screenMaxSize) {
        val scale =
            calculateScale(
                formFactor = currentFormFactor,
                // To subtract the Top tool bar area some margin for bottom
                screenSize = Size(screenMaxSize.width, screenMaxSize.height - 80 - 40),
            )
        viewModel.onScaleChanged(scale)
    }

    val colorSchemeHolder = project.themeHolder.colorSchemeHolder
    var addModifierDialogVisible by remember { mutableStateOf(false) }
    ProvideAppThemeTokens(
        isDarkTheme = mainViewUiState.appDarkTheme,
        lightScheme = colorSchemeHolder.lightColorScheme.value.toColorScheme(),
        darkScheme = colorSchemeHolder.darkColorScheme.value.toColorScheme(),
        typography = project.themeHolder.fontHolder.generateTypography(),
    ) {
        OnboardingOverlay(
            state = onboardingManager.state,
            onAction = onboardingManager::handleAction,
        ) {
            Box(
                modifier =
                    Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                // To make sure the UI builder screen area has the proper focus.
                                // Otherwise it doesn't often receive key events such as delete, or
                                // plus (which opens the add modifier dialog)
                                focusRequester.requestFocus()
                            },
                        ).onPointerEvent(PointerEventType.Move) {
                            it.changes.firstOrNull()?.let { change ->
                                dragStartPosition = change.position
                            }
                        }.focusProperties { canFocus = true }
                        .focusRequester(focusRequester)
                        .onKeyEvent {
                            var consumed = false
                            if ((it.isCtrlPressed || it.isMetaPressed) &&
                                it.type == KeyEventType.KeyDown &&
                                it.key == Key.M
                            ) {
                                addModifierDialogVisible = true
                                consumed = true
                            } else {
                                if (it.type == KeyEventType.KeyDown) {
                                    val eventResult = canvasNodeCallbacks.onKeyPressed(it)
                                    eventResult.handleMessages(onShowSnackbar, coroutineScope)
                                    consumed = eventResult.consumed
                                }
                            }
                            consumed
                        },
            ) {
                val leftPaneSplitState =
                    rememberSplitLayoutState(initialDividerPosition = viewModel.leftPaneDividerPosition)
                StatefulHorizontalSplitLayout(
                    state = leftPaneSplitState,
                    maxRatio = 0.4f,
                    onDividerPositionChanged = viewModel::onLeftPaneDividerPositionChanged,
                    first = { firstModifier ->
                        LeftPane(
                            project = project,
                            copiedNodes = copiedNodes,
                            canvasNodeCallbacks = canvasNodeCallbacks,
                            composeNodeCallbacks = composeNodeCallbacks,
                            paletteNodeCallbacks = paletteNodeCallbacks,
                            zoomableContainerStateHolder = viewModel.zoomableContainerStateHolder,
                            onAddScreenFromTemplate = viewModel::onAddScreenFromTemplate,
                            onAddScreen = viewModel::onAddScreen,
                            onSelectScreen = viewModel::onSelectScreen,
                            onScreenUpdated = viewModel::onScreenUpdated,
                            onDeleteScreen = viewModel::onDeleteScreen,
                            onScreensSwapped = viewModel::onScreensSwapped,
                            onFocusedStatusUpdated = viewModel::onFocusedStatusUpdated,
                            onHoveredStatusUpdated = viewModel::onHoveredStatusUpdated,
                            onShowInspectorTab = {
                                selectedInspectorDestination = InspectorTabDestination.Inspector
                            },
                            onShowActionTab = {
                                selectedInspectorDestination = InspectorTabDestination.Action
                            },
                            onShowSnackbar = onShowSnackbar,
                            onboardingManager = onboardingManager,
                            modifier =
                                firstModifier
                                    .onboardingTarget(TargetArea.Palette, onboardingManager),
                        )
                    },
                    second = { secondModifier ->
                        BoxWithConstraints(modifier = secondModifier) {
                            val canvasDividerPosition = this.maxWidth - viewModel.inspectorTabWidth
                            // TODO: Moving speed doesn't align with the actual amount of in this
                            // SplitLayout
                            StatefulHorizontalSplitLayout(
                                state = SplitLayoutState.DpBased(canvasDividerPosition),
                                maxRatio = 0.8f,
                                onDividerPositionChanged = { newPosition ->
                                    val newInspectorWidth = maxWidth - newPosition
                                    viewModel.onInspectorTabWidthChanged(newInspectorWidth)
                                },
                                first = { canvasModifier ->
                                    CanvasArea(
                                        project = project,
                                        aiAssistantUiState = aiAssistantUiState,
                                        canvasEditable = currentEditable,
                                        copiedNodes = copiedNodes,
                                        currentFormFactor = currentFormFactor,
                                        canvasAreaUiState = canvasAreaUiState,
                                        canvasNodeCallbacks = canvasNodeCallbacks,
                                        composeNodeCallbacks = composeNodeCallbacks,
                                        onMousePressedAt = viewModel::onMousePressedAt,
                                        onMouseHoveredAt = viewModel::onMouseHoveredAt,
                                        onFocusedStatusUpdated = viewModel::onFocusedStatusUpdated,
                                        onHoveredStatusUpdated =
                                            { node, isHovered ->
                                                viewModel.onHoveredStatusUpdated(
                                                    node,
                                                    isHovered,
                                                )
                                            },
                                        onOpenAddModifierDialog = {
                                            addModifierDialogVisible = true
                                        },
                                        onToggleVisibilityOfAiChatDialog = onToggleVisibilityOfAiChatDialog,
                                        onShowSnackbar = onShowSnackbar,
                                        zoomableContainerStateHolder = zoomableContainerStateHolder,
                                        onboardingManager = onboardingManager,
                                        modifier =
                                            canvasModifier
                                                .onboardingTarget(
                                                    TargetArea.Canvas,
                                                    onboardingManager,
                                                ),
                                    )
                                },
                                second = { inspectorModifier ->
                                    InspectorTab(
                                        project = project,
                                        composeNodeCallbacks = composeNodeCallbacks,
                                        onShowSnackbar = onShowSnackbar,
                                        onResetPendingDestination = {
                                            selectedInspectorDestination = null
                                            viewModel.onResetPendingInspectorTab()
                                        },
                                        selectedDestination =
                                            (
                                                editingProject.screenHolder.pendingDestination as?
                                                    NavigatableDestination.UiBuilderScreen
                                            )?.inspectorTabDestination
                                                ?: selectedInspectorDestination,
                                        modifier =
                                            inspectorModifier
                                                .width(viewModel.inspectorTabWidth)
                                                .onboardingTarget(
                                                    TargetArea.Inspector,
                                                    onboardingManager,
                                                ),
                                    )
                                },
                            )
                        }
                    },
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                )
            }
        }
    }

    draggedNode?.let {
        val size = Size(80f, 64f)
        PaletteIcon(
            modifier =
                Modifier
                    .width(size.width.dp)
                    .height(size.height.dp)
                    .offset {
                        IntOffset(
                            (dragStartPosition.x - size.width / 2f).roundToInt(),
                            (dragStartPosition.y - size.height / 2f).roundToInt(),
                        )
                    }.clip(RoundedCornerShape(16.dp))
                    .alpha(0.5f)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            imageVector = it.trait.value.icon(),
            iconText = it.trait.value.iconText(),
            contentDescription = "Palette icon for ${it.trait.value.iconText()}",
        )
    }

    val onAnyDialogIsShown = LocalOnAnyDialogIsShown.current
    val onAllDialogsClosed = LocalOnAllDialogsClosed.current
    if (addModifierDialogVisible) {
        onAnyDialogIsShown()
        project.screenHolder.findFocusedNodes().firstOrNull()?.let { focused ->
            val modifiers =
                ModifierWrapper
                    .values()
                    .filter { m ->
                        focused.parentNode?.let {
                            m.hasValidParent(it.trait.value)
                        } ?: true
                    }.mapIndexed { i, modifierWrapper ->
                        i to modifierWrapper
                    }

            AddModifierDialog(
                modifiers = modifiers,
                onModifierSelected = {
                    addModifierDialogVisible = false
                    composeNodeCallbacks.onModifierAdded(focused, modifiers[it].second)
                    onAllDialogsClosed()
                },
                onCloseClick = {
                    addModifierDialogVisible = false
                    onAllDialogsClosed()
                },
            )
        }
    }
}

const val DEVICE_CANVAS_TEST_TAG = "DeviceCanvas"

@Immutable
data class CanvasAreaUiState(
    val topToolbarUiState: CanvasTopToolbarUiState,
)

const val TOGGLE_TOP_APP_BAR_BUTTON_TEST_TAG = "ToggleTopAppBarButton"
const val TOGGLE_NAV_BUTTON_TEST_TAG = "ToggleNavButton"

@Composable
private fun LeftPane(
    project: Project,
    copiedNodes: List<ComposeNode>,
    canvasNodeCallbacks: CanvasNodeCallbacks,
    composeNodeCallbacks: ComposeNodeCallbacks,
    paletteNodeCallbacks: PaletteNodeCallbacks,
    zoomableContainerStateHolder: ZoomableContainerStateHolder,
    onAddScreenFromTemplate: (name: String, screenTemplatePair: ScreenTemplatePair) -> Unit,
    onAddScreen: (screen: Screen) -> Unit,
    onSelectScreen: (screen: Screen) -> Unit,
    onScreenUpdated: (screen: Screen) -> Unit,
    onDeleteScreen: (screen: Screen) -> Unit,
    onScreensSwapped: (from: Int, to: Int) -> Unit,
    onFocusedStatusUpdated: (node: ComposeNode) -> Unit,
    onHoveredStatusUpdated: (node: ComposeNode, isHovered: Boolean) -> Unit,
    onShowSnackbar: suspend (String, String?) -> Boolean,
    onShowInspectorTab: () -> Unit,
    onShowActionTab: () -> Unit,
    onboardingManager: OnboardingManager,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier,
    ) {
        val maxHeight = maxHeight.value.dp
        VerticalSplitLayout(
            initialDividerPosition = maxHeight / 2,
            minRatio = 0.25f,
            maxRatio = 0.75f,
            first = { firstModifier ->
                Column(modifier = firstModifier) {
                    var selectedTabIndex by remember { mutableStateOf(0) }
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                    ) {
                        val palette = stringResource(Res.string.palette)
                        Tooltip(palette) {
                            ComposeFlowTab(
                                selected = selectedTabIndex == 0,
                                onClick = {
                                    selectedTabIndex = 0
                                },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Palette,
                                        contentDescription = palette,
                                        modifier = Modifier.size(20.dp),
                                    )
                                },
                                modifier = Modifier.testTag(UI_BUILDER_PALETTE_TAB_TEST_TAG),
                            )
                        }
                        val screenBuilder = "Screen Builder"
                        Tooltip(screenBuilder) {
                            ComposeFlowTab(
                                selected = selectedTabIndex == 1,
                                onClick = {
                                    selectedTabIndex = 1
                                },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Monitor,
                                        contentDescription = screenBuilder,
                                        modifier = Modifier.size(20.dp),
                                    )
                                },
                                modifier = Modifier.testTag(UI_BUILDER_SCREEN_BUILDER_TAB_TEST_TAG),
                            )
                        }
                        val componentPalette = stringResource(Res.string.component)
                        val density = LocalDensity.current
                        Tooltip(componentPalette) {
                            ComposeFlowTab(
                                selected = selectedTabIndex == 2,
                                onClick = {
                                    selectedTabIndex = 2
                                },
                                icon = {
                                    AsyncImage(
                                        load = {
                                            useResource("icons/compose_logo.svg") {
                                                loadSvgPainter(
                                                    it,
                                                    density,
                                                )
                                            }
                                        },
                                        painterFor = { it },
                                        contentDescription = componentPalette,
                                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                                        modifier = Modifier.size(18.dp, 20.dp),
                                    )
                                },
                                modifier = Modifier.testTag(UI_BUILDER_COMPONENT_TAB_TEST_TAG),
                            )
                        }
                    }

                    when (selectedTabIndex) {
                        0 -> {
                            PaletteTab(
                                project = project,
                                paletteNodeCallbacks = paletteNodeCallbacks,
                                zoomableContainerStateHolder = zoomableContainerStateHolder,
                                modifier = Modifier.weight(1f),
                            )
                        }

                        1 -> {
                            ScreenBuilderTab(
                                project = project,
                                onAddScreenFromTemplate = onAddScreenFromTemplate,
                                onAddScreen = onAddScreen,
                                onSelectScreen = onSelectScreen,
                                onScreenUpdated = onScreenUpdated,
                                onDeleteScreen = onDeleteScreen,
                                onScreensSwapped = onScreensSwapped,
                                modifier = Modifier.weight(1f),
                            )
                        }

                        2 -> {
                            ComponentBuilderTab(
                                project = project,
                                composeNodeCallbacks = composeNodeCallbacks,
                                paletteNodeCallbacks = paletteNodeCallbacks,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            },
            second = { secondModifier ->
                ComposeNodeTree(
                    project = project,
                    copiedNodes = copiedNodes,
                    canvasNodeCallbacks = canvasNodeCallbacks,
                    composeNodeCallbacks = composeNodeCallbacks,
                    onFocusedStatusUpdated = onFocusedStatusUpdated,
                    onHoveredStatusUpdated = { node, isHovered ->
                        onHoveredStatusUpdated(node, isHovered)
                    },
                    onShowInspectorTab = onShowInspectorTab,
                    onShowActionTab = onShowActionTab,
                    onShowSnackbar = onShowSnackbar,
                    modifier =
                        secondModifier.onboardingTarget(
                            TargetArea.ProjectStructure,
                            onboardingManager,
                        ),
                )
            },
        )
    }
}

@Composable
private fun CanvasArea(
    project: Project,
    aiAssistantUiState: AiAssistantUiState,
    canvasEditable: CanvasEditable,
    copiedNodes: List<ComposeNode>,
    currentFormFactor: FormFactor,
    canvasAreaUiState: CanvasAreaUiState,
    canvasNodeCallbacks: CanvasNodeCallbacks,
    composeNodeCallbacks: ComposeNodeCallbacks,
    onMouseHoveredAt: (Offset) -> Unit,
    onMousePressedAt: (Offset, Boolean) -> Unit,
    onShowSnackbar: suspend (String, String?) -> Boolean,
    onFocusedStatusUpdated: (ComposeNode) -> Unit,
    onHoveredStatusUpdated: (ComposeNode, Boolean) -> Unit,
    onOpenAddModifierDialog: () -> Unit,
    onToggleVisibilityOfAiChatDialog: () -> Unit,
    zoomableContainerStateHolder: ZoomableContainerStateHolder,
    onboardingManager: OnboardingManager,
    modifier: Modifier = Modifier,
) {
    var canvasAreaPosition by remember { mutableStateOf(Offset.Zero) }
    val coroutineScope = rememberCoroutineScope()

    var convertToComponentNode by remember { mutableStateOf<ComposeNode?>(null) }
    val density = LocalDensity.current

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .clipToBounds()
                .backgroundContainerNeutral()
                .onPointerEvent(PointerEventType.Press) { event ->
                    event.changes.firstOrNull()?.let { change ->
                        onMousePressedAt(
                            canvasAreaPosition +
                                (change.position - zoomableContainerStateHolder.offset) / zoomableContainerStateHolder.scale,
                            event.keyboardModifiers.isCtrlPressed || event.keyboardModifiers.isMetaPressed,
                        )
                    }
                }.onPointerEvent(PointerEventType.Move) {
                    it.changes.firstOrNull()?.let { change ->
                        onMouseHoveredAt(
                            canvasAreaPosition +
                                (change.position - zoomableContainerStateHolder.offset) / zoomableContainerStateHolder.scale,
                        )
                    }
                }.onGloballyPositioned {
                    canvasAreaPosition = it.boundsInWindow().topLeft
                    canvasNodeCallbacks.onUiBuilderCanvasSizeChanged(it.size / density.density.toInt())
                }.onPointerEvent(PointerEventType.Move) { event ->
                    val change = event.changes.firstOrNull()

                    zoomableContainerStateHolder.onMousePositionChanged(
                        change?.position ?: Offset.Zero,
                    )
                }.onSizeChanged { size ->
                    zoomableContainerStateHolder.onSizeChanged(size)
                },
    ) {
        DotPatternBackground(
            dotRadius = 1.dp,
            dotMargin = 12.dp,
            dotColor = Color.Black,
            modifier = Modifier.fillMaxSize(),
            onDrag = zoomableContainerStateHolder::panBy,
        ) {
            ZoomableContainer(
                zoomableContainerStateHolder,
                modifier = Modifier.offset(y = 16.dp),
            ) {
                AppTheme {
                    if (canvasEditable is Screen) {
                        DeviceInCanvas(
                            project = project,
                            aiAssistantUiState = aiAssistantUiState,
                            screen = canvasEditable,
                            currentFormFactor = currentFormFactor,
                            canvasNodeCallbacks = canvasNodeCallbacks,
                            composeNodeCallbacks = composeNodeCallbacks,
                            onShowSnackbar = onShowSnackbar,
                            copiedNodes = copiedNodes,
                            coroutineScope = coroutineScope,
                            onConvertToComponent = {
                                convertToComponentNode = it
                            },
                            onFocusedStatusUpdated = onFocusedStatusUpdated,
                            onHoveredStatusUpdated = onHoveredStatusUpdated,
                            onOpenAddModifierDialog = onOpenAddModifierDialog,
                            toolbarUiState = canvasAreaUiState.topToolbarUiState,
                            zoomableContainerStateHolder = zoomableContainerStateHolder,
                        )
                    } else if (canvasEditable is Component) {
                        ComponentInCanvas(
                            project = project,
                            component = canvasEditable,
                            canvasNodeCallbacks = canvasNodeCallbacks,
                            composeNodeCallbacks = composeNodeCallbacks,
                            onShowSnackbar = onShowSnackbar,
                            rootNode = canvasEditable.componentRoot.value,
                            copiedNodes = copiedNodes,
                            coroutineScope = coroutineScope,
                            onConvertToComponent = {
                                convertToComponentNode = it
                            },
                            onFocusedStatusUpdated = onFocusedStatusUpdated,
                            onHoveredStatusUpdated = onHoveredStatusUpdated,
                            onOpenAddModifierDialog = onOpenAddModifierDialog,
                            toolbarUiState = canvasAreaUiState.topToolbarUiState,
                            zoomableContainerStateHolder = zoomableContainerStateHolder,
                        )
                    }
                }
            }
        }
        CanvasTopToolbar(
            toolbarUiState = canvasAreaUiState.topToolbarUiState,
            canvasNodeCallbacks = canvasNodeCallbacks,
            currentFormFactor = currentFormFactor,
            canvasScale = zoomableContainerStateHolder.scale,
            onResetZoom = {
                zoomableContainerStateHolder.resetZoom()
            },
            onToolbarZoomScaleChange = { scale ->
                zoomableContainerStateHolder.onToolbarZoomScaleChanged(scale)
            },
            onToggleVisibilityOfAiChatDialog = onToggleVisibilityOfAiChatDialog,
            onboardingManager = onboardingManager,
        )

        if (canvasEditable is Screen) {
            ToggleBottomNavButton(
                project = project,
            )
        } else if (canvasEditable is Component) {
            Card(
                modifier =
                    modifier
                        .size(40.dp, 32.dp)
                        .hoverIconClickable()
                        .offset(x = 32.dp, y = 64.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(8.dp),
            ) {
                val contentDesc = stringResource(Res.string.back_to_screen)
                Tooltip(contentDesc) {
                    Icon(
                        modifier =
                            Modifier
                                .clickable {
                                    canvasNodeCallbacks.onPopEditedComponent()
                                }.padding(8.dp)
                                .size(24.dp),
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = contentDesc,
                    )
                }
            }
        }

        val onAnyDialogIsShown = LocalOnAnyDialogIsShown.current
        val onAllDialogsClosed = LocalOnAllDialogsClosed.current
        convertToComponentNode?.let { nodeToConvert ->
            onAnyDialogIsShown()
            SingleTextInputDialog(
                textLabel = stringResource(Res.string.component_name),
                onTextConfirmed = {
                    canvasNodeCallbacks.onConvertToComponent(it, nodeToConvert)
                    convertToComponentNode = null
                    onAllDialogsClosed()
                },
                onDismissDialog = {
                    convertToComponentNode = null
                    onAllDialogsClosed()
                },
            )
        }
    }
}

@Composable
private fun BoxScope.DeviceInCanvas(
    project: Project,
    aiAssistantUiState: AiAssistantUiState,
    screen: Screen,
    currentFormFactor: FormFactor,
    canvasNodeCallbacks: CanvasNodeCallbacks,
    composeNodeCallbacks: ComposeNodeCallbacks,
    onShowSnackbar: suspend (String, String?) -> Boolean,
    copiedNodes: List<ComposeNode>,
    coroutineScope: CoroutineScope,
    onConvertToComponent: (ComposeNode) -> Unit,
    onFocusedStatusUpdated: (ComposeNode) -> Unit,
    onHoveredStatusUpdated: (ComposeNode, Boolean) -> Unit,
    onOpenAddModifierDialog: () -> Unit,
    toolbarUiState: CanvasTopToolbarUiState,
    zoomableContainerStateHolder: ZoomableContainerStateHolder,
) {
    val formFactorDeviceSize = currentFormFactor.deviceSize
    var actualDeviceSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    var contextMenuExpanded by remember { mutableStateOf(false) }

    @Composable
    fun DrawEditorContents() {
        Box(
            modifier =
                if (aiAssistantUiState.isGenerating.value) {
                    Modifier
                        .fillMaxSize()
                        .alpha(0.85f)
                        .shimmer()
                } else {
                    Modifier
                },
        ) {
            Column(
                modifier =
                    screen.rootNode.value
                        .modifierChainForCanvas()
                        .modifierForCanvas(
                            project,
                            // Should be PaletteNode.Screen
                            node = screen.rootNode.value,
                            canvasNodeCallbacks = canvasNodeCallbacks,
                            zoomableContainerStateHolder = zoomableContainerStateHolder,
                            isDraggable = false,
                        ).onGloballyPositioned {
                            actualDeviceSize = it.size / density.density.toInt()
                        }.onClick(
                            matcher = PointerMatcher.mouse(PointerButton.Secondary),
                        ) {
                            contextMenuExpanded = true
                        },
            ) {
                @Composable
                fun CallRenderedDevice() {
                    RenderedDevice(
                        project = project,
                        screen = screen,
                        actualDeviceSize = actualDeviceSize,
                        canvasNodeCallbacks = canvasNodeCallbacks,
                        toolbarUiState = toolbarUiState,
                        contextMenuExpanded = contextMenuExpanded,
                        composeNodeCallbacks = composeNodeCallbacks,
                        copiedNodes = copiedNodes,
                        onOpenAddModifierDialog = onOpenAddModifierDialog,
                        onShowSnackbar = onShowSnackbar,
                        coroutineScope = coroutineScope,
                        onFocusedStatusUpdated = onFocusedStatusUpdated,
                        onHoveredStatusUpdated = onHoveredStatusUpdated,
                        onConvertToComponent = onConvertToComponent,
                        onCloseContextMenu = {
                            contextMenuExpanded = false
                        },
                        zoomableContainerStateHolder = zoomableContainerStateHolder,
                    )
                }

                screen.navigationDrawerNode.value?.let { navDrawer ->
                    val drawerTrait = navDrawer.trait.value as NavigationDrawerTrait
                    val drawerState =
                        rememberDrawerState(
                            if (drawerTrait.expandedInCanvas.value) DrawerValue.Open else DrawerValue.Closed,
                        )

                    LaunchedEffect(Unit) { drawerState.open() }

                    LaunchedEffect(drawerTrait.expandedInCanvas.value) {
                        if (drawerTrait.expandedInCanvas.value) {
                            drawerState.open()
                        } else {
                            drawerState.close()
                        }
                    }

                    when (drawerTrait.navigationDrawerType) {
                        NavigationDrawerType.Default -> {
                            ModalNavigationDrawer(
                                drawerState = drawerState,
                                drawerContent = {
                                    drawerTrait.RenderedNode(
                                        project = project,
                                        node = navDrawer,
                                        canvasNodeCallbacks = canvasNodeCallbacks,
                                        paletteRenderParams =
                                            PaletteRenderParams(
                                                showBorder = toolbarUiState.showBorders,
                                            ),
                                        zoomableContainerStateHolder = zoomableContainerStateHolder,
                                        modifier =
                                            Modifier.onGloballyPositioned {
                                                canvasNodeCallbacks.onBoundsInNodeUpdated(
                                                    navDrawer,
                                                    it
                                                        .boundsInWindow()
                                                        .calculateAdjustedBoundsInZoomableContainer(
                                                            zoomableContainerStateHolder,
                                                        ),
                                                )
                                            },
                                    )
                                },
                                gesturesEnabled = false,
                            ) {
                                CallRenderedDevice()
                            }
                        }

                        NavigationDrawerType.Dismissible -> {
                            DismissibleNavigationDrawer(
                                drawerState = drawerState,
                                drawerContent = {
                                    NavigationDrawerTrait().RenderedNode(
                                        project = project,
                                        node = navDrawer,
                                        canvasNodeCallbacks = canvasNodeCallbacks,
                                        paletteRenderParams =
                                            PaletteRenderParams(
                                                showBorder = toolbarUiState.showBorders,
                                            ),
                                        zoomableContainerStateHolder = zoomableContainerStateHolder,
                                        modifier =
                                            Modifier.onGloballyPositioned {
                                                canvasNodeCallbacks.onBoundsInNodeUpdated(
                                                    navDrawer,
                                                    it
                                                        .boundsInWindow()
                                                        .calculateAdjustedBoundsInZoomableContainer(
                                                            zoomableContainerStateHolder,
                                                        ),
                                                )
                                            },
                                    )
                                },
                                gesturesEnabled = false,
                            ) {
                                CallRenderedDevice()
                            }
                        }
                    }
                } ?: run {
                    CallRenderedDevice()
                }
            }
        }
    }

    val targetWidth = formFactorDeviceSize.width.dp + (currentFormFactor.vesselSize * 2).dp
    val targetHeight = formFactorDeviceSize.height.dp + (currentFormFactor.vesselSize * 2).dp

    val animatedWidth by animateDpAsState(
        targetValue = targetWidth,
        animationSpec = tween(durationMillis = 200),
    )
    val animatedHeight by animateDpAsState(
        targetValue = targetHeight,
        animationSpec = tween(durationMillis = 200),
    )
    Column(
        modifier =
            Modifier
                .testTag(DEVICE_CANVAS_TEST_TAG)
                .width(animatedWidth)
                .height(animatedHeight)
                .align(Alignment.Center)
                .clip(shape = RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.background),
    ) {
        when (currentFormFactor) {
            is FormFactor.Phone -> {
                PixelPhoneFrame(
                    contentPadding = currentFormFactor.vesselSize.dp,
                ) {
                    DrawEditorContents()
                }
            }

            is FormFactor.Tablet -> {
                IPadTabletFrame(
                    contentPadding = currentFormFactor.vesselSize.dp,
                ) {
                    DrawEditorContents()
                }
            }

            else -> {
                DrawEditorContents()
            }
        }
    }
}

@Composable
private fun RenderedDevice(
    project: Project,
    screen: Screen,
    actualDeviceSize: IntSize,
    canvasNodeCallbacks: CanvasNodeCallbacks,
    toolbarUiState: CanvasTopToolbarUiState,
    contextMenuExpanded: Boolean,
    composeNodeCallbacks: ComposeNodeCallbacks,
    copiedNodes: List<ComposeNode>,
    onOpenAddModifierDialog: () -> Unit,
    onShowSnackbar: suspend (String, String?) -> Boolean,
    coroutineScope: CoroutineScope,
    onFocusedStatusUpdated: (ComposeNode) -> Unit,
    onHoveredStatusUpdated: (ComposeNode, Boolean) -> Unit,
    onConvertToComponent: (ComposeNode) -> Unit,
    onCloseContextMenu: () -> Unit,
    zoomableContainerStateHolder: ZoomableContainerStateHolder,
) {
    NavigationSuiteScaffold(
        navigationSuiteItems = {
            if (project.screenHolder.showNavigation.value && screen.showOnNavigation.value) {
                project.screenHolder.screens
                    .filter { it.showOnNavigation.value }
                    .forEach { screen ->
                        item(
                            selected = project.screenHolder.currentEditable() == screen,
                            onClick = {
                                project.screenHolder.selectScreen(screen)
                            },
                            icon = {
                                Icon(
                                    imageVector = screen.icon.value.imageVector,
                                    contentDescription = null,
                                )
                            },
                            label = { Text(text = screen.label.value) },
                        )
                    }
            }
        },
        layoutType =
            computeNavigationSuiteType(
                actualDeviceSize.width.toFloat(),
                actualDeviceSize.height.toFloat(),
                showOnNavigation = screen.showOnNavigation.value && project.screenHolder.showNavigation.value,
            ),
    ) {
        ProvideDeviceSizeDp(actualDeviceSize) {
            Scaffold { outerScaffoldPadding ->
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(outerScaffoldPadding),
                ) {
                    val fab = screen.fabNode.value
                    Scaffold(
                        topBar = {
                            screen.topAppBarNode.value?.let { topAppBarNode ->
                                topAppBarNode.trait.value.RenderedNode(
                                    project = project,
                                    node = topAppBarNode,
                                    canvasNodeCallbacks = canvasNodeCallbacks,
                                    paletteRenderParams = PaletteRenderParams(),
                                    zoomableContainerStateHolder = zoomableContainerStateHolder,
                                    modifier =
                                        topAppBarNode
                                            .modifierList
                                            .toModifierChain(),
                                )
                            }
                        },
                        floatingActionButton = {
                            screen.fabNode.value?.let { fabNode ->
                                fabNode.trait.value.RenderedNode(
                                    project = project,
                                    node = fabNode,
                                    canvasNodeCallbacks = canvasNodeCallbacks,
                                    paletteRenderParams = PaletteRenderParams(),
                                    zoomableContainerStateHolder = zoomableContainerStateHolder,
                                    modifier =
                                        fabNode
                                            .modifierList
                                            .toModifierChain(),
                                )
                            }
                        },
                        floatingActionButtonPosition =
                            when ((fab?.trait?.value as? FabTrait)?.fabPositionWrapper) {
                                FabPositionWrapper.End -> FabPosition.End
                                FabPositionWrapper.Center -> FabPosition.Center
                                null -> FabPosition.End
                            },
                    ) { innerScaffoldPadding ->
                        val bottomAppBar = screen.getBottomAppBar()
                        val paletteRenderParams =
                            PaletteRenderParams(
                                showBorder = toolbarUiState.showBorders,
                            )
                        Column {
                            screen.contentRootNode().RenderedNodeInCanvas(
                                project = project,
                                canvasNodeCallbacks = canvasNodeCallbacks,
                                paletteRenderParams = paletteRenderParams,
                                zoomableContainerStateHolder = zoomableContainerStateHolder,
                                modifier =
                                    Modifier
                                        .padding(innerScaffoldPadding),
                            )
                            bottomAppBar?.trait?.value?.RenderedNode(
                                project = project,
                                node = bottomAppBar,
                                canvasNodeCallbacks = canvasNodeCallbacks,
                                paletteRenderParams = paletteRenderParams,
                                zoomableContainerStateHolder = zoomableContainerStateHolder,
                                modifier =
                                    bottomAppBar
                                        .modifierList
                                        .toModifierChain(),
                            )
                        }
                    }

                    if (contextMenuExpanded) {
                        UiBuilderContextMenuDropDown(
                            project = project,
                            canvasNodeCallbacks = canvasNodeCallbacks,
                            composeNodeCallbacks = composeNodeCallbacks,
                            copiedNodes = copiedNodes,
                            currentEditable = screen,
                            onAddModifier = {
                                onOpenAddModifierDialog()
                            },
                            onCloseMenu = {
                                onCloseContextMenu()
                            },
                            onShowSnackbar = onShowSnackbar,
                            coroutineScope = coroutineScope,
                            onFocusedStatusUpdated = onFocusedStatusUpdated,
                            onHoveredStatusUpdated = onHoveredStatusUpdated,
                            onOpenConvertToComponentDialog = {
                                onConvertToComponent(it)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BoxScope.ComponentInCanvas(
    project: Project,
    component: Component,
    canvasNodeCallbacks: CanvasNodeCallbacks,
    composeNodeCallbacks: ComposeNodeCallbacks,
    onShowSnackbar: suspend (String, String?) -> Boolean,
    rootNode: ComposeNode,
    copiedNodes: List<ComposeNode>,
    coroutineScope: CoroutineScope,
    onConvertToComponent: (ComposeNode) -> Unit,
    onFocusedStatusUpdated: (ComposeNode) -> Unit,
    onHoveredStatusUpdated: (ComposeNode, Boolean) -> Unit,
    onOpenAddModifierDialog: () -> Unit,
    toolbarUiState: CanvasTopToolbarUiState,
    zoomableContainerStateHolder: ZoomableContainerStateHolder,
) {
    val componentSize = 550.dp to 400.dp
    Column(
        modifier =
            Modifier
                .testTag(DEVICE_CANVAS_TEST_TAG)
                .wrapContentSize()
                .width(componentSize.first)
                .height(componentSize.second)
                .wrapContentSize()
                .align(Alignment.Center),
    ) {
        var contextMenuExpanded by remember { mutableStateOf(false) }
        Box(
            Modifier
                .fillMaxSize()
                .onClick(
                    matcher = PointerMatcher.mouse(PointerButton.Secondary),
                ) {
                    contextMenuExpanded = true
                },
        ) {
            Box(
                modifier =
                    Modifier
                        .wrapContentSize()
                        .background(MaterialTheme.colorScheme.background)
                        .drawLabel(
                            labelName = component.name,
                            textColor = MaterialTheme.colorScheme.onSurface,
                            labelRectColor =
                                MaterialTheme.colorScheme.surfaceContainerLow.copy(
                                    alpha = 0.7f,
                                ),
                        ),
            ) {
                rootNode.RenderedNodeInCanvas(
                    project = project,
                    canvasNodeCallbacks = canvasNodeCallbacks,
                    paletteRenderParams =
                        PaletteRenderParams(
                            showBorder = toolbarUiState.showBorders,
                        ),
                    zoomableContainerStateHolder = zoomableContainerStateHolder,
                )
            }
        }

        if (contextMenuExpanded) {
            UiBuilderContextMenuDropDown(
                project = project,
                canvasNodeCallbacks = canvasNodeCallbacks,
                composeNodeCallbacks = composeNodeCallbacks,
                copiedNodes = copiedNodes,
                currentEditable = component,
                onAddModifier = {
                    onOpenAddModifierDialog()
                },
                onCloseMenu = {
                    contextMenuExpanded = !contextMenuExpanded
                },
                onShowSnackbar = onShowSnackbar,
                coroutineScope = coroutineScope,
                onFocusedStatusUpdated = onFocusedStatusUpdated,
                onHoveredStatusUpdated = onHoveredStatusUpdated,
                onOpenConvertToComponentDialog = {
                    onConvertToComponent(it)
                },
            )
        }
    }
}

@Composable
fun BoxScope.ToggleBottomNavButton(project: Project) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp),
        modifier =
            Modifier
                .align(Alignment.BottomStart)
                .offset(
                    x = 32.dp,
                    y = (-32).dp,
                ).size(40.dp, 32.dp)
                .hoverIconClickable()
                .testTag(TOGGLE_NAV_BUTTON_TEST_TAG),
    ) {
        val contentDesc =
            if (project.screenHolder.showNavigation.value) {
                stringResource(Res.string.remove_navigation)
            } else {
                stringResource(Res.string.show_navigation)
            }

        Tooltip(contentDesc) {
            Icon(
                imageVector = if (project.screenHolder.showNavigation.value) Icons.Default.BorderBottom else Icons.Default.BorderClear,
                contentDescription = contentDesc,
                modifier =
                    Modifier
                        .clickable {
                            project.screenHolder.showNavigation.value =
                                !project.screenHolder.showNavigation.value
                        }.padding(8.dp)
                        .size(24.dp),
            )
        }
    }
}

const val CANVAS_TOP_TOOLBAR_TEST_TAG = "CanvasTopToolbar"
const val CANVAS_TOP_TOOLBAR_DARK_MODE_SWITCH_TEST_TAG =
    "$CANVAS_TOP_TOOLBAR_TEST_TAG/DarkModeSwitch"
const val CANVAS_TOP_TOOLBAR_SHOW_BORDERS_SWITCH_TEST_TAG =
    "$CANVAS_TOP_TOOLBAR_TEST_TAG/ShowBordersSwitch"
const val CANVAS_TOP_TOOLBAR_ZOOM_IN_TEST_TAG = "$CANVAS_TOP_TOOLBAR_TEST_TAG/ZoomIn"
const val CANVAS_TOP_TOOLBAR_ZOOM_OUT_TEST_TAG = "$CANVAS_TOP_TOOLBAR_TEST_TAG/ZoomOut"
const val CANVAS_TOP_TOOLBAR_POSITION_AND_ZOOM_RESET_TEST_TAG =
    "$CANVAS_TOP_TOOLBAR_TEST_TAG/PositionAndZoomReset"
const val UI_BUILDER_TAB_TEST_TAG = "UiBuilderTab"
const val UI_BUILDER_PALETTE_TAB_TEST_TAG = "$UI_BUILDER_TAB_TEST_TAG/Palette"
const val UI_BUILDER_COMPONENT_TAB_TEST_TAG = "$UI_BUILDER_TAB_TEST_TAG/Component"
const val UI_BUILDER_SCREEN_BUILDER_TAB_TEST_TAG = "$UI_BUILDER_TAB_TEST_TAG/ScreenBuilder"

@Immutable
data class CanvasTopToolbarUiState(
    val isDarkMode: Boolean,
    val onDarkModeChanged: (Boolean) -> Unit,
    val showBorders: Boolean,
    val onShowBordersChanged: (Boolean) -> Unit,
)

@Composable
fun CanvasTopToolbar(
    canvasNodeCallbacks: CanvasNodeCallbacks,
    toolbarUiState: CanvasTopToolbarUiState,
    currentFormFactor: FormFactor,
    canvasScale: Float,
    onToolbarZoomScaleChange: (Float) -> Unit,
    onResetZoom: () -> Unit = {},
    onToggleVisibilityOfAiChatDialog: () -> Unit,
    onboardingManager: OnboardingManager,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .testTag(CANVAS_TOP_TOOLBAR_TEST_TAG)
                .padding(
                    horizontal = 32.dp,
                    vertical = 8.dp,
                ).height(48.dp)
                .onboardingTarget(TargetArea.Toolbar, onboardingManager),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (isAiEnabled()) {
                OpenAiChatButton(
                    onToggleVisibilityOfAiChatDialog = onToggleVisibilityOfAiChatDialog,
                    modifier = Modifier.onboardingTarget(TargetArea.AiAssistant, onboardingManager),
                )
            } else {
                DisabledOpenAiChatButton(
                    modifier = Modifier.onboardingTarget(TargetArea.AiAssistant, onboardingManager),
                )
            }
            Spacer(modifier = Modifier.weight(1f))

            Card(
                modifier =
                    Modifier
                        .align(Alignment.CenterVertically)
                        .height(32.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(8.dp),
            ) {
                Row {
                    val contentDesc =
                        stringResource(Res.string.reset_position_and_zoom)
                    Tooltip(contentDesc) {
                        Icon(
                            modifier =
                                Modifier
                                    .testTag(CANVAS_TOP_TOOLBAR_POSITION_AND_ZOOM_RESET_TEST_TAG)
                                    .clickable {
                                        onResetZoom()
                                    }.hoverIconClickable()
                                    .padding(8.dp)
                                    .size(24.dp),
                            imageVector = Icons.Outlined.CenterFocusStrong,
                            contentDescription = contentDesc,
                        )
                    }
                    Box(
                        modifier =
                            Modifier
                                .fillMaxHeight()
                                .width(1.dp)
                                .background(MaterialTheme.colorScheme.outline),
                    )
                    Tooltip(stringResource(Res.string.zoom_out) + " (${getCtrlKeyStr()} + \"-\")") {
                        Text(
                            modifier =
                                Modifier
                                    .clickable {
                                        onToolbarZoomScaleChange(canvasScale - 0.1f)
                                    }.fillMaxHeight()
                                    .testTag(CANVAS_TOP_TOOLBAR_ZOOM_OUT_TEST_TAG)
                                    .wrapContentHeight(Alignment.CenterVertically)
                                    .hoverIconClickable()
                                    .width(40.dp),
                            text = "−",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    Box(
                        modifier =
                            Modifier
                                .fillMaxHeight()
                                .width(1.dp)
                                .background(MaterialTheme.colorScheme.outline),
                    )
                    Text(
                        modifier =
                            Modifier
                                .fillMaxHeight()
                                .wrapContentHeight(Alignment.CenterVertically)
                                .width(40.dp),
                        text = "${(canvasScale * 10).roundToInt() / 10.0}x",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Box(
                        modifier =
                            Modifier
                                .fillMaxHeight()
                                .width(1.dp)
                                .background(MaterialTheme.colorScheme.outline),
                    )
                    Tooltip(stringResource(Res.string.zoom_in) + " (${getCtrlKeyStr()} + \"+\")") {
                        Text(
                            modifier =
                                Modifier
                                    .clickable {
                                        onToolbarZoomScaleChange(canvasScale + 0.1f)
                                    }.fillMaxHeight()
                                    .wrapContentHeight(Alignment.CenterVertically)
                                    .testTag(CANVAS_TOP_TOOLBAR_ZOOM_IN_TEST_TAG)
                                    .hoverIconClickable()
                                    .width(40.dp),
                            text = "+",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.size(8.dp))
            Card(
                modifier =
                    Modifier
                        .size(48.dp)
                        .padding(8.dp)
                        .hoverIconClickable()
                        .align(Alignment.CenterVertically),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(8.dp),
            ) {
                val contentDesc =
                    if (toolbarUiState.isDarkMode) {
                        stringResource(Res.string.chane_to_day_theme)
                    } else {
                        stringResource(Res.string.chane_to_night_theme)
                    }
                Tooltip(contentDesc) {
                    Icon(
                        modifier =
                            Modifier
                                .testTag(CANVAS_TOP_TOOLBAR_DARK_MODE_SWITCH_TEST_TAG)
                                .clickable {
                                    toolbarUiState.onDarkModeChanged(!toolbarUiState.isDarkMode)
                                }.padding(8.dp)
                                .size(24.dp),
                        imageVector =
                            if (toolbarUiState.isDarkMode) {
                                Icons.Default.ModeNight
                            } else {
                                Icons.Default.WbSunny
                            },
                        contentDescription = contentDesc,
                    )
                }
            }

            Card(
                modifier =
                    Modifier
                        .padding(vertical = 8.dp)
                        .size(32.dp)
                        .hoverIconClickable()
                        .align(Alignment.CenterVertically),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(8.dp),
            ) {
                val contentDesc =
                    if (toolbarUiState.showBorders) {
                        stringResource(Res.string.borders_hide)
                    } else {
                        stringResource(Res.string.borders_show)
                    }
                Tooltip(contentDesc) {
                    Icon(
                        modifier =
                            Modifier
                                .testTag(CANVAS_TOP_TOOLBAR_SHOW_BORDERS_SWITCH_TEST_TAG)
                                .clickable {
                                    toolbarUiState.onShowBordersChanged(!toolbarUiState.showBorders)
                                }.padding(8.dp)
                                .size(24.dp),
                        imageVector =
                            if (toolbarUiState.showBorders) {
                                Icons.Default.BorderOuter
                            } else {
                                Icons.Default.BorderClear
                            },
                        contentDescription = contentDesc,
                    )
                }
            }
        }

        DeviceFormFactorCard(
            canvasNodeCallbacks = canvasNodeCallbacks,
            currentFormFactor = currentFormFactor,
            modifier =
                Modifier
                    .align(Alignment.Center)
                    .onboardingTarget(TargetArea.DevicePreview, onboardingManager),
        )
    }
}

@Composable
private fun DeviceFormFactorCard(
    canvasNodeCallbacks: CanvasNodeCallbacks,
    currentFormFactor: FormFactor,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier =
            modifier
                .height(34.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            fun formFactorSelectedModifier(selected: Boolean): Modifier =
                if (selected) {
                    Modifier
                } else {
                    Modifier.alpha(0.3f)
                }
            ComposeFlowIcon(
                imageVector = Icons.Outlined.Smartphone,
                contentDescription = null,
                modifier =
                    Modifier
                        .padding(6.dp)
                        .size(26.dp)
                        .hoverIconClickable()
                        .clickable {
                            canvasNodeCallbacks.onFormFactorChanged(FormFactor.Phone())
                        }.then(formFactorSelectedModifier(currentFormFactor is FormFactor.Phone)),
            )

            ComposeFlowIcon(
                imageVector = Icons.Outlined.TabletAndroid,
                contentDescription = null,
                modifier =
                    Modifier
                        .padding(6.dp)
                        .size(26.dp)
                        .hoverIconClickable()
                        .clickable {
                            canvasNodeCallbacks.onFormFactorChanged(FormFactor.Tablet())
                        }.then(formFactorSelectedModifier(currentFormFactor is FormFactor.Tablet)),
            )

            ComposeFlowIcon(
                imageVector = Icons.Outlined.DesktopMac,
                contentDescription = null,
                modifier =
                    Modifier
                        .padding(8.dp)
                        .size(26.dp)
                        .hoverIconClickable()
                        .clickable {
                            canvasNodeCallbacks.onFormFactorChanged(FormFactor.Desktop())
                        }.then(formFactorSelectedModifier(currentFormFactor is FormFactor.Desktop)),
            )
        }
    }
}

@Composable
private fun RowScope.OpenAiChatButton(
    onToggleVisibilityOfAiChatDialog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier =
            modifier
                .height(32.dp)
                .align(Alignment.CenterVertically)
                .hoverIconClickable(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp),
    ) {
        val openAiAssistant = stringResource(Res.string.ai_open_assistant)
        Tooltip(openAiAssistant + " (${getCtrlKeyStr()} + K)") {
            Icon(
                modifier =
                    Modifier
                        .clickable {
                            onToggleVisibilityOfAiChatDialog()
                        }.padding(4.dp)
                        .size(24.dp),
                imageVector = ComposeFlowIcons.NounAi,
                contentDescription = openAiAssistant,
            )
        }
    }
}

@Composable
private fun RowScope.DisabledOpenAiChatButton(modifier: Modifier = Modifier) {
    Tooltip(stringResource(Res.string.ai_login_needed)) {
        Card(
            modifier =
                modifier
                    .height(32.dp)
                    .align(Alignment.CenterVertically)
                    .alpha(0.3f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(8.dp),
        ) {
            val openAiAssistant = stringResource(Res.string.ai_open_assistant)
            Icon(
                modifier =
                    Modifier
                        .padding(4.dp)
                        .size(24.dp),
                imageVector = ComposeFlowIcons.NounAi,
                contentDescription = openAiAssistant,
            )
        }
    }
}

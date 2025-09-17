package io.composeflow.model.project.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import io.composeflow.ComposeScreenConstant
import io.composeflow.Res
import io.composeflow.ViewModelConstant
import io.composeflow.custom.ComposeFlowIcons
import io.composeflow.custom.composeflowicons.ComposeLogo
import io.composeflow.edit_component
import io.composeflow.kotlinpoet.FileSpecWithDirectory
import io.composeflow.kotlinpoet.GeneratedPlace
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.kotlinpoet.MemberHolder
import io.composeflow.kotlinpoet.wrapper.AnnotationSpecWrapper
import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper
import io.composeflow.kotlinpoet.wrapper.FileSpecWrapper
import io.composeflow.kotlinpoet.wrapper.FunSpecWrapper
import io.composeflow.kotlinpoet.wrapper.MemberNameWrapper
import io.composeflow.kotlinpoet.wrapper.ParameterSpecWrapper
import io.composeflow.kotlinpoet.wrapper.asTypeNameWrapper
import io.composeflow.kotlinpoet.wrapper.suppressRedundantVisibilityModifier
import io.composeflow.model.palette.PaletteDraggable
import io.composeflow.model.palette.PaletteNodeCallbacks
import io.composeflow.model.palette.PaletteRenderParams
import io.composeflow.model.palette.TraitCategory
import io.composeflow.model.parameter.ComponentTrait
import io.composeflow.model.project.CanvasEditable
import io.composeflow.model.project.ParameterWrapper
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNodeCallbacks
import io.composeflow.model.project.appscreen.screen.composenode.restoreInstance
import io.composeflow.model.project.findComponentOrNull
import io.composeflow.model.project.findComponentOrThrow
import io.composeflow.model.state.AppState
import io.composeflow.model.state.AuthenticatedUserState
import io.composeflow.model.state.ReadableState
import io.composeflow.model.state.ScreenState
import io.composeflow.model.state.StateHolder
import io.composeflow.model.state.StateHolderImpl
import io.composeflow.model.state.StateHolderType
import io.composeflow.model.state.StateId
import io.composeflow.model.state.StateResult
import io.composeflow.override.mutableStateListEqualsOverrideOf
import io.composeflow.remove_component
import io.composeflow.serializer.FallbackMutableStateListSerializer
import io.composeflow.serializer.MutableStateSerializer
import io.composeflow.tooltip_component_trait
import io.composeflow.ui.LocalOnAllDialogsClosed
import io.composeflow.ui.LocalOnAnyDialogIsShown
import io.composeflow.ui.Tooltip
import io.composeflow.ui.common.LocalUseDarkTheme
import io.composeflow.ui.common.ProvideAppThemeTokens
import io.composeflow.ui.draggableFromPalette
import io.composeflow.ui.emptyCanvasNodeCallbacks
import io.composeflow.ui.icon.ComposeFlowIcon
import io.composeflow.ui.icon.ComposeFlowIconButton
import io.composeflow.ui.popup.SimpleConfirmationDialog
import io.composeflow.ui.switchByHovered
import io.composeflow.ui.zoomablecontainer.ZoomableContainerStateHolder
import io.composeflow.util.generateUniqueName
import io.composeflow.util.toKotlinFileName
import io.composeflow.util.toPackageName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

typealias ComponentId = String

const val COMPONENT_KEY_NAME = "componentKey"

@Serializable
@SerialName("Component")
data class Component(
    override val id: ComponentId = Uuid.random().toString(),
    override val name: String,
    @Serializable(MutableStateSerializer::class)
    val componentRoot: MutableState<ComposeNode> =
        mutableStateOf(
            ComposeNode(),
        ),
    @Serializable(FallbackMutableStateListSerializer::class)
    override val parameters: MutableList<ParameterWrapper<*>> = mutableStateListEqualsOverrideOf(),
    private val stateHolderImpl: StateHolderImpl = StateHolderImpl(),
) : PaletteDraggable,
    CanvasEditable,
    StateHolder {
    init {
        componentRoot.value.updateChildParentRelationships()
        getAllComposeNodes().forEach { it.updateComposeNodeReferencesForTrait() }
        // isFocused is marked as non-Transient. But it's only used for giving LLM the context of
        // which nodes are focused. When initializing a component, it should be cleared.
        getAllComposeNodes().forEach { it.isFocused.value = false }
    }

    override fun getPackageName(project: Project): String = "${project.packageName}.components.$name".toPackageName()

    @Transient
    override val composableName: String = name.toKotlinFileName()

    @Transient
    override val viewModelFileName: String = name.capitalize(Locale.current) + "ViewModel"

    @Transient
    override val viewModelName: String =
        name.replaceFirstChar { it.lowercase() } + "ViewModel"

    fun asMemberName(project: Project): MemberNameWrapper = MemberNameWrapper.get(getPackageName(project), composableName)

    @Composable
    fun Thumbnail(
        project: Project,
        composeNodeCallbacks: ComposeNodeCallbacks,
        paletteNodeCallbacks: PaletteNodeCallbacks,
        modifier: Modifier = Modifier,
    ) {
        var removeComponentDialogOpen by remember { mutableStateOf(false) }
        Column(
            modifier =
                modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    ).aspectRatio(2.2f)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                    ).switchByHovered(
                        hovered =
                            Modifier.border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(8.dp),
                            ),
                        notHovered = Modifier.alpha(0.5f),
                    ).draggableFromPalette(
                        project = project,
                        paletteNodeCallbacks = paletteNodeCallbacks,
                        paletteDraggable = this,
                        zoomableContainerStateHolder = ZoomableContainerStateHolder(),
                    ).combinedClickable(
                        enabled = true,
                        onClick = {},
                        onDoubleClick = {
                            composeNodeCallbacks.onEditComponent(
                                project.findComponentOrThrow(
                                    id,
                                ),
                            )
                        },
                    ),
        ) {
            Row {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(start = 8.dp, top = 8.dp),
                )
                Spacer(Modifier.weight(1f))
                val editComponent = stringResource(Res.string.edit_component)
                Tooltip(editComponent) {
                    ComposeFlowIconButton(onClick = {
                        composeNodeCallbacks.onEditComponent(project.findComponentOrThrow(id))
                    }) {
                        ComposeFlowIcon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = editComponent,
                        )
                    }
                }
                Spacer(Modifier.size(8.dp))
                val removeComponent = stringResource(Res.string.remove_component)
                Tooltip(removeComponent) {
                    ComposeFlowIconButton(onClick = {
                        removeComponentDialogOpen = true
                    }) {
                        ComposeFlowIcon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = removeComponent,
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
            ProvideAppThemeTokens(isDarkTheme = LocalUseDarkTheme.current) {
                Surface(
                    modifier =
                        Modifier
                            .scale(0.65f)
                            .clip(RoundedCornerShape(8.dp)),
                ) {
                    // Calling restoreInstance so that same mutableStates such as Hovered or focused
                    // statuses are not used with the original ComposeNode tree
                    componentRoot.value.restoreInstance(sameId = true).RenderedNodeInCanvas(
                        project = project,
                        canvasNodeCallbacks = emptyCanvasNodeCallbacks,
                        paletteRenderParams = PaletteRenderParams(isThumbnail = true),
                        zoomableContainerStateHolder = ZoomableContainerStateHolder(),
                        modifier =
                            Modifier
                                .size(width = 330.dp, height = 460.dp),
                    )
                }
            }
        }

        val onAnyDialogIsShown = LocalOnAnyDialogIsShown.current
        val onAllDialogsClosed = LocalOnAllDialogsClosed.current
        if (removeComponentDialogOpen) {
            onAnyDialogIsShown()
            val closeDialog = {
                removeComponentDialogOpen = false
                onAllDialogsClosed()
            }
            SimpleConfirmationDialog(
                text = stringResource(Res.string.remove_component) + "?",
                onCloseClick = {
                    closeDialog()
                },
                onConfirmClick = {
                    closeDialog()
                    composeNodeCallbacks.onRemoveComponent(project.findComponentOrThrow(id))
                },
            )
        }
    }

    override fun defaultComposeNode(project: Project): ComposeNode? =
        project
            .findComponentOrNull(id)
            ?.componentRoot
            ?.value
            ?.createComponentWrapperNode(id)

    override fun paletteCategories(): List<TraitCategory> = listOf(TraitCategory.Container)

    override fun tooltipResource(): StringResource = Res.string.tooltip_component_trait

    override fun icon(): ImageVector = ComposeFlowIcons.ComposeLogo

    override fun iconText(): String = name

    override fun getRootNode(): ComposeNode = componentRoot.value

    override fun getContentRootNode(): ComposeNode = componentRoot.value

    override fun findFocusedNodes(): List<ComposeNode> = componentRoot.value.findFocusedNodes()

    override fun findNodeById(id: String): ComposeNode? {
        if (id == this.id) {
            if (getRootNode().trait.value is ComponentTrait) {
                return getRootNode()
            }
        }
        return getAllComposeNodes().firstOrNull { it.id == id }
    }

    override fun clearIsHoveredRecursively() {
        componentRoot.value.clearIsHoveredRecursively()
    }

    override fun clearIsFocusedRecursively() {
        componentRoot.value.clearIsFocusedRecursively()
    }

    override fun clearIsDraggedOnBoundsRecursively() {
        componentRoot.value.clearIsDraggedOnBoundsRecursively()
    }

    override fun clearIndexToBeDroppedRecursively() {
        componentRoot.value.clearIndexToBeDroppedRecursively()
    }

    override fun updateFocusedNode(
        eventPosition: Offset,
        isCtrlOrMetaPressed: Boolean,
    ) {
        if (!isCtrlOrMetaPressed) {
            componentRoot.value.clearIsFocusedRecursively()
        }
        componentRoot.value
            .findDeepestChildAtOrNull(eventPosition)
            ?.setFocus(toggleValue = isCtrlOrMetaPressed)
    }

    override fun updateHoveredNode(eventPosition: Offset) {
        componentRoot.value.clearIsHoveredRecursively()
        componentRoot.value.findDeepestChildAtOrNull(eventPosition)?.let {
            it.isHovered.value = true
        }
    }

    override fun findDeepestChildAtOrNull(position: Offset): ComposeNode? = componentRoot.value.findDeepestChildAtOrNull(position)

    override fun getAllComposeNodes(): List<ComposeNode> = componentRoot.value.allChildren()

    override fun getStates(project: Project): List<ReadableState> =
        project.getStates(project) + stateHolderImpl.getStates(project) +
            getRootNode()
                .allChildren()
                .flatMap {
                    it.getCompanionStates(project)
                }

    override fun addState(readableState: ReadableState) {
        stateHolderImpl.states.add(readableState)
    }

    override fun createUniqueLabel(
        project: Project,
        composeNode: ComposeNode,
        initial: String,
    ): String {
        val existingLabels =
            getRootNode()
                .allChildren()
                .filter { it.id != composeNode.id }
                .map { it.labelExposed }
                .toSet()
        return generateUniqueName(
            initial = initial,
            existing = existingLabels,
        )
    }

    override fun findStateOrNull(
        project: Project,
        stateId: StateId,
    ): ReadableState? = getStates(project).firstOrNull { it.id == stateId }

    override fun removeState(stateId: StateId): Boolean {
        val toRemove = stateHolderImpl.states.find { it.id == stateId }
        return if (toRemove != null) {
            stateHolderImpl.states.remove(toRemove)
        } else {
            false
        }
    }

    override fun updateState(readableState: ReadableState) {
        val index = stateHolderImpl.states.indexOfFirst { it.id == readableState.id }
        if (index != -1) {
            stateHolderImpl.states[index] = readableState
        }
    }

    override fun copyContents(other: StateHolder) {
        stateHolderImpl.states.clear()
        stateHolderImpl.states.addAll((other as? StateHolderImpl)?.states ?: emptyList())
    }

    override fun getStateResults(project: Project): List<StateResult> =
        project.getStates(project).map { StateHolderType.Global to it } +
            stateHolderImpl
                .getStates(project)
                .map { StateHolderType.Screen(screenId = id) to it } +
            getRootNode().allChildren().flatMap { node ->
                node.getCompanionStates(project).map { state ->
                    StateHolderType.Screen(screenId = id) to state
                }
            }

    override fun generateComposeScreenFileSpec(
        project: Project,
        context: GenerationContext,
        dryRun: Boolean,
    ): FileSpecWrapper {
        val fileBuilder =
            FileSpecWrapper
                .builder(getPackageName(project), composableName)
                .addImport("androidx.compose.runtime", "getValue")
                .addImport("androidx.compose.runtime", "setValue")
        val funSpecBuilder = FunSpecWrapper.builder(composableName).addAnnotation(AnnotationSpecWrapper.get(Composable::class))
        parameters.forEach {
            funSpecBuilder.addParameter(it.generateArgumentParameterSpec(project))
        }
        funSpecBuilder.addParameter(
            ParameterSpecWrapper
                .builder("modifier", Modifier::class.asTypeNameWrapper())
                .defaultValue("%T", Modifier::class.asTypeNameWrapper())
                .build(),
        )
        // Key to distinguish the ViewModel to avoid the same ViewModel is reused across
        // multiple component invocations.
        funSpecBuilder.addParameter(
            ParameterSpecWrapper
                .builder(
                    COMPONENT_KEY_NAME,
                    String::class.asTypeNameWrapper().copy(nullable = true),
                ).defaultValue("null")
                .build(),
        )

        getAllActions(project).distinctBy { it.argumentName(project) }.forEach {
            it.generateArgumentParameterSpec(project)?.let { parameterSpec ->
                funSpecBuilder.addParameter(parameterSpec)
            }
        }

        if (getAllActions(project).any { it.hasSuspendFunction() }) {
            funSpecBuilder.addStatement(
                "val ${ComposeScreenConstant.coroutineScope.name} = %M()",
                MemberHolder.AndroidX.Runtime.rememberCoroutineScope,
            )
        }
        context.getCurrentComposableContext().compositionLocalVariables.forEach { (variableName, memberName) ->
            funSpecBuilder.addStatement("val $variableName = %M.current", memberName)
        }

        // "componentKey" should be passed as a function in the Component
        funSpecBuilder.addStatement(
            "val $viewModelName = %M($viewModelFileName::class, key = $COMPONENT_KEY_NAME)",
            MemberHolder.PreCompose.koinViewModel,
        )
        getStates(project).forEach { state ->
            when (state) {
                is ScreenState<*> -> {
                    funSpecBuilder.addCode(
                        state.generateVariableInitializationBlock(
                            project,
                            context,
                            dryRun = dryRun,
                        ),
                    )
                    state
                        .generateValidatorInitializationBlock(project, context, dryRun = dryRun)
                        ?.let {
                            funSpecBuilder.addCode(it)
                        }
                    funSpecBuilder.addStatement("") // Enforce a new line
                }

                is AppState<*> -> {
                    if (isDependentOn(project, state)) {
                        funSpecBuilder.addCode(
                            state.generateVariableInitializationBlock(
                                project,
                                context,
                                dryRun = dryRun,
                            ),
                        )
                        funSpecBuilder.addStatement("") // Enforce a new line
                    }
                }

                is AuthenticatedUserState -> {
                    if (isDependentOn(project, state)) {
                        funSpecBuilder.addCode(
                            state.generateVariableInitializationBlock(
                                project,
                                context,
                                dryRun = dryRun,
                            ),
                        )
                        funSpecBuilder.addStatement("") // Enforce a new line
                    }
                }
            }
        }
        AuthenticatedUserState.entries().forEach { state ->
            if (isDependentOn(project, state)) {
                funSpecBuilder.addCode(
                    state.generateVariableInitializationBlock(
                        project,
                        context,
                        dryRun = dryRun,
                    ),
                )
                funSpecBuilder.addStatement("") // Enforce a new line
            }
        }

        val composableContext = context.getCurrentComposableContext()
        composableContext.launchedEffectBlock.forEach {
            fileBuilder.addCode(it)
        }

        project.apiHolder.getValidApiDefinitions().forEach {
            // Write the PagingSource as Flow if this Component depends on the API
            if (getRootNode().isDependent(it.id)) {
                funSpecBuilder.addStatement(
                    """
                    val ${it.apiResultName()} by $viewModelName.${it.apiResultName()}.%M()
                        """,
                    MemberHolder.AndroidX.Runtime.collectAsState,
                    MemberHolder.AppCash.collectAsLazyPagingItems,
                )
            }
        }
        project.firebaseAppInfoHolder.firebaseAppInfo.firestoreCollections.forEach { firestoreCollection ->
            if (getRootNode().isDependent(firestoreCollection.id)) {
                context
                    .getCurrentComposableContext()
                    .addDependency(viewModelConstant = ViewModelConstant.firestore)
                funSpecBuilder.addCode(
                    firestoreCollection.generateVariableInitializationBlock(
                        project,
                        context,
                        dryRun,
                    ),
                )
            }
        }

        getAllActionNodes()
            .flatMap { it.generateInitializationCodeBlocks(project, context, dryRun = dryRun) }
            .distinctBy { it }
            .forEach {
                it?.let {
                    funSpecBuilder.addCode(it)
                }
            }

        val codeBlockBuilder = CodeBlockWrapper.builder()
        // Wrap by Column to apply the modifier from the argument
        codeBlockBuilder.addStatement(
            "%M(modifier = modifier) {",
            MemberHolder.AndroidX.Layout.Column,
        )
        codeBlockBuilder.add(
            getRootNode().generateCode(
                project = project,
                context = context,
                dryRun = dryRun,
            ),
        )
        codeBlockBuilder.addStatement("}")

        funSpecBuilder.addCode(codeBlockBuilder.build())
        fileBuilder.addFunction(funSpecBuilder.build())
        fileBuilder.suppressRedundantVisibilityModifier()
        return fileBuilder.build()
    }

    fun generateCode(
        project: Project,
        context: GenerationContext,
    ): List<FileSpecWithDirectory> {
        val localContext = context.copy(currentEditable = this)
        // Execute generation first to construct the related dependencies in the GenerationContext
        // by passing dryRun as true
        generateComposeScreenFileSpec(
            project,
            context = localContext.copy(generatedPlace = GeneratedPlace.ComposeScreen),
            dryRun = true,
        )
        generateViewModelFileSpec(
            project,
            context = localContext.copy(generatedPlace = GeneratedPlace.ViewModel),
            dryRun = true,
        )
        return listOfNotNull(
            generateViewModelFileSpec(
                project,
                context = localContext.copy(generatedPlace = GeneratedPlace.ViewModel),
            ),
            generateComposeScreenFileSpec(
                project,
                context = localContext.copy(generatedPlace = GeneratedPlace.ComposeScreen),
            ),
        ).map {
            FileSpecWithDirectory(it)
        }
    }
}

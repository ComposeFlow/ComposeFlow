@file:OptIn(kotlin.time.ExperimentalTime::class)

package io.composeflow.model.project.appscreen.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.onClick
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import io.composeflow.ComposeScreenConstant
import io.composeflow.ON_SCREEN_INITIALLY_LOADED
import io.composeflow.SCREEN_INITIALLY_LOADED_FLAG
import io.composeflow.ViewModelConstant
import io.composeflow.asClassName
import io.composeflow.asVariableName
import io.composeflow.cloud.storage.asDateString
import io.composeflow.formatter.suppressRedundantVisibilityModifier
import io.composeflow.kotlinpoet.FileSpecWithDirectory
import io.composeflow.kotlinpoet.GeneratedPlace
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.kotlinpoet.MemberHolder
import io.composeflow.materialicons.Filled
import io.composeflow.materialicons.ImageVectorHolder
import io.composeflow.model.action.ActionType
import io.composeflow.model.palette.PaletteRenderParams
import io.composeflow.model.parameter.FabTrait
import io.composeflow.model.parameter.IconTrait
import io.composeflow.model.parameter.NavigationDrawerTrait
import io.composeflow.model.parameter.ScreenTrait
import io.composeflow.model.parameter.TopAppBarTrait
import io.composeflow.model.parameter.TopAppBarTypeWrapper
import io.composeflow.model.project.COMPOSEFLOW_PACKAGE
import io.composeflow.model.project.CanvasEditable
import io.composeflow.model.project.ParameterWrapper
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.SCREEN_ROUTE
import io.composeflow.model.project.appscreen.ScreenHolder
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.project.appscreen.screenRouteClass
import io.composeflow.model.property.StringProperty
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
import io.composeflow.random
import io.composeflow.serializer.FallbackMutableStateListSerializer
import io.composeflow.serializer.MutableStateSerializer
import io.composeflow.serializer.decodeFromStringWithFallback
import io.composeflow.serializer.encodeToString
import io.composeflow.ui.adaptive.ProvideDeviceSizeDp
import io.composeflow.ui.common.LocalUseDarkTheme
import io.composeflow.ui.common.ProvideAppThemeTokens
import io.composeflow.ui.emptyCanvasNodeCallbacks
import io.composeflow.ui.modifier.hoverIconClickable
import io.composeflow.ui.propertyeditor.DropdownItem
import io.composeflow.ui.switchByHovered
import io.composeflow.ui.zoomablecontainer.ZoomableContainerStateHolder
import io.composeflow.util.generateUniqueName
import io.composeflow.util.toKotlinFileName
import io.composeflow.util.toPackageName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.uuid.Uuid

typealias ScreenId = String

@Serializable
@SerialName("Screen")
data class Screen(
    override val id: String = Uuid.random().toString(),

    override val name: String,
    @Serializable(with = MutableStateSerializer::class)
    val rootNode: MutableState<ComposeNode> =
        mutableStateOf(
            ComposeNode(
                label = mutableStateOf(name),
                trait = mutableStateOf(ScreenTrait),
            ).apply {
                addChild(ComposeNode.createRootNode())
            },
        ),
    /**
     * If set to true, this Screen is visible in the Navigation.
     */
    @Serializable(with = MutableStateSerializer::class)
    val showOnNavigation: MutableState<Boolean> = mutableStateOf(true),
    /**
     * Title in the TopAppBar
     */
    @Serializable(with = MutableStateSerializer::class)
    val title: MutableState<String> = mutableStateOf(name),
    /**
     * Displayed label in the Navigation
     */
    @Serializable(with = MutableStateSerializer::class)
    val label: MutableState<String> = mutableStateOf(name),
    @Serializable(with = MutableStateSerializer::class)
    val icon: MutableState<ImageVectorHolder> =
        mutableStateOf(
            Filled.entries.toTypedArray().random(),
        ),
    /**
     * The default destination when the app is launched
     */
    @Serializable(with = MutableStateSerializer::class)
    val isDefault: MutableState<Boolean> = mutableStateOf(false),
    /**
     * Set to true when this screen is active in the UI Builder
     */
    @Serializable(with = MutableStateSerializer::class)
    val isSelected: MutableState<Boolean> = mutableStateOf(false),
    /**
     * ComposeNode that represents the TopAppBar specific to this Screen.
     */
    @Serializable(with = MutableStateSerializer::class)
    val topAppBarNode: MutableState<ComposeNode?> =
        mutableStateOf(
            ComposeNode(
                label = mutableStateOf("TopAppBar"),
                trait =
                    mutableStateOf(
                        TopAppBarTrait(
                            title = StringProperty.StringIntrinsicValue(title.value),
                            topAppBarType = TopAppBarTypeWrapper.CenterAligned,
                        ),
                    ),
            ).apply {
                addChild(
                    ComposeNode(
                        label = mutableStateOf("Nav Icon"),
                        trait =
                            mutableStateOf(
                                IconTrait(imageVectorHolder = null),
                            ),
                    ),
                )
            },
        ),
    /**
     * ComposeNode that represents the TopAppBar specific to this Screen.
     */
    @Serializable(with = MutableStateSerializer::class)
    val bottomAppBarNode: MutableState<ComposeNode?> = mutableStateOf(null),
    /**
     * ComposeNode that represents the NavigationDrawer
     */
    @Serializable(with = MutableStateSerializer::class)
    val navigationDrawerNode: MutableState<ComposeNode?> =
        mutableStateOf(null),
    /**
     * ComposeNode that represents the FAB
     */
    @Serializable(with = MutableStateSerializer::class)
    val fabNode: MutableState<ComposeNode?> =
        mutableStateOf(null),
    @Serializable(FallbackMutableStateListSerializer::class)
    override val parameters: MutableList<ParameterWrapper<*>> = mutableStateListEqualsOverrideOf(),
    private val stateHolderImpl: StateHolderImpl = StateHolderImpl(),
) : StateHolder,
    CanvasEditable,
    DropdownItem {
    init {
        topAppBarNode.value?.updateChildParentRelationships()
        bottomAppBarNode.value?.updateChildParentRelationships()
        navigationDrawerNode.value?.updateChildParentRelationships()

        if (rootNode.value.trait.value !is ScreenTrait) {
            val newRoot =
                ComposeNode(
                    label = mutableStateOf(label.value),
                    trait = mutableStateOf(ScreenTrait),
                )
            newRoot.addChild(rootNode.value)
            rootNode.value = newRoot
        }
        rootNode.value.label.value = label.value

        getAllComposeNodes().forEach { it.updateComposeNodeReferencesForTrait() }

        // isFocused is marked as non-Transient. But it's only used for giving LLM the context of
        // which nodes are focused. When initializing a screen, it should be cleared.
        getAllComposeNodes().forEach { it.isFocused.value = false }
    }

    /**
     * Returns the content root node which is the root node of the actual content (instead of
     * a ComposeNode where paletteNode == PaletteNode.Screen)
     */
    fun contentRootNode(): ComposeNode =
        if (rootNode.value.trait.value is ScreenTrait) {
            rootNode.value.children[0]
        } else {
            rootNode.value
        }

    fun defaultRouteCodeBlock(): CodeBlock =
        if (parameters.isEmpty()) {
            CodeBlock.of("%T.$routeName", screenRouteClass)
        } else {
            CodeBlock.of("%T.$routeName()", screenRouteClass)
        }

    private fun asRouteClassName(): ClassName = screenRouteClass.nestedClass(routeName)

    @Transient
    override val composableName: String = "${name.asClassName()}Screen".toKotlinFileName()

    @Transient
    private val composeNavigationName: String =
        "${name.asClassName()}Navigation".toKotlinFileName()

    @Transient
    override val viewModelFileName: String =
        name.asClassName().capitalize(Locale.current) + "ViewModel"

    @Transient
    override val viewModelName: String =
        name.asVariableName().replaceFirstChar { it.lowercase() } + "ViewModel"

    @Transient
    val routeName: String = name.asClassName().replaceFirstChar { it.uppercase() } + "Route"

    @Transient
    val sceneName: String = name.asClassName().lowercase().toKotlinFileName()

    override fun getPackageName(project: Project): String =
        "${project.packageName}.screens.$name".toPackageName()

    override fun getStates(project: Project): List<ReadableState> {
        val projectStates = project.getStates(project)
        val screenSpecificStates = stateHolderImpl.getStates(project)
        val companionStates =
            getRootNode()
                .allChildren()
                .flatMap {
                    it.getCompanionStates(project)
                }
        return projectStates + screenSpecificStates + companionStates
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
                .map { it.label.value }
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

    override fun removeState(stateId: StateId): Boolean =
        stateHolderImpl.states.removeIf {
            it.id == stateId
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
            generateComposeScreenFileSpec(
                project,
                context = localContext.copy(generatedPlace = GeneratedPlace.ComposeScreen),
                dryRun = false,
            ),
            generateViewModelFileSpec(
                project,
                context = localContext.copy(generatedPlace = GeneratedPlace.ViewModel),
                dryRun = false,
            ),
            generateComposeNavigationFileSpec(project),
        ).map {
            FileSpecWithDirectory(it)
        }
    }

    fun getBottomAppBar(): ComposeNode? = bottomAppBarNode.value

    override fun generateComposeScreenFileSpec(
        project: Project,
        context: GenerationContext,
        dryRun: Boolean,
    ): FileSpec {
        val fileBuilder =
            FileSpec
                .builder(getPackageName(project), composableName)
                .addImport("androidx.compose.runtime", "getValue")
                .addImport("androidx.compose.runtime", "setValue")

        val funSpecBuilder = FunSpec.builder(composableName).addAnnotation(Composable::class)
        if (parameters.isNotEmpty()) {
            funSpecBuilder.addParameter(
                ParameterSpec
                    .builder(ComposeScreenConstant.arguments.name, asRouteClassName())
                    .defaultValue(defaultRouteCodeBlock())
                    .build(),
            )
        }

        topAppBarNode.value?.let { topAppBarNode ->
            val topAppBarParams = topAppBarNode.trait.value as TopAppBarTrait
            funSpecBuilder.addCode(
                ScreenHolder.generateCodeBlockFromScrollBehavior(topAppBarParams.scrollBehaviorWrapper),
            )
        }

        getAllActions(project).distinctBy { it.generateArgumentParameterSpec(project) }.forEach {
            it.generateArgumentParameterSpec(project)?.let { parameterSpec ->
                funSpecBuilder.addParameter(parameterSpec)
            }
        }

        if (getAllActions(project).any { it.hasSuspendFunction() }) {
            context.getCurrentComposableContext().isCoroutineScopeUsed = true
        }
        if (context.getCurrentComposableContext().isCoroutineScopeUsed) {
            funSpecBuilder.addStatement(
                "val ${ComposeScreenConstant.coroutineScope.name} = %M()",
                MemberHolder.AndroidX.Runtime.rememberCoroutineScope,
            )
        }

        // Needs to be after generateInitializationCodeBlocks is called for each Action
        context.getCurrentComposableContext().compositionLocalVariables.forEach { (variableName, memberName) ->
            funSpecBuilder.addStatement("val $variableName = %M.current", memberName)
        }

        funSpecBuilder.addStatement(
            "val $viewModelName = %M($viewModelFileName::class)",
            MemberName("moe.tlaster.precompose.koin", "koinViewModel"),
        )
        getStates(project).forEach { state ->
            when (state) {
                is ScreenState<*> -> {
                    funSpecBuilder.addCode(
                        state.generateVariableInitializationBlock(
                            project,
                            context,
                            dryRun,
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
            funSpecBuilder.addCode(it)
        }
        if (rootNode.value.actionsMap[ActionType.OnInitialLoad]?.isNotEmpty() == true) {
            val onInitialLoadCodeBlock = CodeBlock.builder()
            rootNode.value.actionsMap[ActionType.OnInitialLoad]?.forEach {
                onInitialLoadCodeBlock.add(it.generateCodeBlock(project, context, dryRun = dryRun))
            }
            funSpecBuilder.addCode(
                """
                val screenLoaded by $viewModelName.$SCREEN_INITIALLY_LOADED_FLAG.%M()
                %M(screenLoaded) {
                    if (screenLoaded) {
                        ${onInitialLoadCodeBlock.build()}
                    } else {
                        $viewModelName.$ON_SCREEN_INITIALLY_LOADED()
                    }
                }
            """,
                MemberHolder.AndroidX.Runtime.collectAsState,
                MemberHolder.AndroidX.Runtime.LaunchedEffect,
            )
        }

        project.apiHolder.getValidApiDefinitions().forEach {
            // Write the PagingSource as Flow if this Screen depends on the API
            if (contentRootNode().isDependent(it.id)) {
                funSpecBuilder.addStatement(
                    """
                    val ${it.apiResultName()} by $viewModelName.${it.apiResultName()}.%M()
                        """,
                    MemberHolder.AndroidX.Runtime.collectAsState,
                )
            }
        }
        project.firebaseAppInfoHolder.firebaseAppInfo.firestoreCollections.forEach { firestoreCollection ->
            if (contentRootNode().isDependent(firestoreCollection.id)) {
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
            .flatMap {
                it.generateInitializationCodeBlocks(project, context, dryRun = dryRun)
            }.distinctBy { it }
            .forEach { initializationCode ->
                initializationCode?.let {
                    funSpecBuilder.addCode(it)
                }
            }

        navigationDrawerNode.value?.let { navDrawer ->
            context.getCurrentComposableContext().isCoroutineScopeUsed = true
            funSpecBuilder.addStatement(
                "val ${ComposeScreenConstant.navDrawerState.name} = %M(initialValue = %M.Closed)",
                MemberName("androidx.compose.material3", "rememberDrawerState"),
                MemberName("androidx.compose.material3", "DrawerValue"),
            )
            val navParams = navDrawer.trait.value as NavigationDrawerTrait
            navParams.navigationDrawerType.toMemberName()
            funSpecBuilder.addStatement(
                """%M(
                    drawerState = ${ComposeScreenConstant.navDrawerState.name},
                    drawerContent = {""",
                navParams.navigationDrawerType.toMemberName(),
            )

            funSpecBuilder.addCode(navDrawer.generateCode(project, context, dryRun))
            funSpecBuilder.addStatement("},")
            funSpecBuilder.addStatement("gesturesEnabled = ${navParams.gesturesEnabled},")
            funSpecBuilder.addStatement(") {")
        }

        funSpecBuilder.addStatement(
            "%M(",
            MemberName("androidx.compose.material3", "Scaffold"),
        )
        topAppBarNode.value?.let { topAppBarNode ->
            funSpecBuilder.addStatement("topBar = {")
            funSpecBuilder.addCode(
                topAppBarNode.trait.value.generateCode(
                    project = project,
                    node = topAppBarNode,
                    context = context,
                    dryRun = dryRun,
                ),
            )
            funSpecBuilder.addStatement("},")
            if ((topAppBarNode.trait.value as TopAppBarTrait).scrollBehaviorWrapper.hasScrollBehavior()) {
                funSpecBuilder.addStatement(
                    "modifier = %M.%M(scrollBehavior.nestedScrollConnection),",
                    MemberName("androidx.compose.ui", "Modifier"),
                    MemberName("androidx.compose.ui.input.nestedscroll", "nestedScroll"),
                )
            }
        }
        fabNode.value?.let { fabNode ->
            funSpecBuilder.addStatement("floatingActionButton = {")
            funSpecBuilder.addCode(
                fabNode.trait.value.generateCode(
                    project = project,
                    node = fabNode,
                    context = context,
                    dryRun = dryRun,
                ),
            )
            funSpecBuilder.addStatement("},")
            val fabParams = fabNode.trait.value as FabTrait
            fabParams.fabPositionWrapper?.let {
                funSpecBuilder.addStatement(
                    "floatingActionButtonPosition = %M.${fabParams.fabPositionWrapper.name},",
                    MemberName("androidx.compose.material3", "FabPosition"),
                )
            }
        }

        funSpecBuilder.addStatement(") {")

        if (hasOwnTopAppBar()) {
            // Apply ContentPadding from Scaffold only when the screen is not part of the navigation
            // because ContentPadding is applied from the outer Scaffold if it's part of the navigation
            funSpecBuilder.addCode(
                CodeBlock.of(
                    "%M(modifier = %M.%M(top = it.calculateTopPadding())) {",
                    MemberHolder.AndroidX.Layout.Column,
                    MemberHolder.AndroidX.Ui.Modifier,
                    MemberHolder.AndroidX.Layout.padding,
                ),
            )
        }

        val generatedCode =
            contentRootNode().generateCode(
                project = project,
                context = context,
                dryRun = dryRun,
            )
        funSpecBuilder.addCode("%M {", MemberHolder.AndroidX.Layout.Column)
        funSpecBuilder.addCode(generatedCode)

        val bottomAppBar = getBottomAppBar()
        if (bottomAppBar != null) {
            funSpecBuilder.addCode(
                bottomAppBar.trait.value.generateCode(
                    project = project,
                    node = bottomAppBar,
                    context = context,
                    dryRun = dryRun,
                ),
            )
        }

        if (hasOwnTopAppBar()) {
            funSpecBuilder.addStatement("}") // Close Column
        }

        funSpecBuilder.addStatement("}")

        funSpecBuilder.addStatement("}")

        navigationDrawerNode.value?.let {
            funSpecBuilder.addStatement("}") // Close the NavigationDrawer
        }
        fileBuilder.addFunction(funSpecBuilder.build())
        fileBuilder.suppressRedundantVisibilityModifier()
        return fileBuilder.build()
    }

    fun generateArgumentsInitializationCodeBlock(project: Project): CodeBlock {
        val builder = CodeBlock.builder()
        val actions = getAllActions(project)
        actions.distinctBy { it.generateNavigationInitializationBlock() }.forEach {
            it.generateNavigationInitializationBlock()?.let { codeBlock ->
                builder
                    .addStatement("${it.argumentName(project)} = ")
                    .add(codeBlock)
                    .addStatement(",")
            }
        }
        return builder.build()
    }

    private fun generateComposeNavigationFileSpec(project: Project): FileSpec {
        val fileBuilder = FileSpec.builder(getPackageName(project), composeNavigationName)
        val actionsDistinctByArgSpec =
            getAllActions(project).distinctBy { it.generateArgumentParameterSpec(project) }
        val funSpecBuilder = FunSpec.builder(sceneName).receiver(NavGraphBuilder::class)

        val argumentPassersString =
            buildString {
                actionsDistinctByArgSpec.forEach {
                    it.argumentName(project)?.let { argumentName ->
                        append("$argumentName = $argumentName,\n")
                    }
                }
            }
        actionsDistinctByArgSpec.forEach {
            it.generateArgumentParameterSpec(project)?.let { parameterSpec ->
                funSpecBuilder.addParameter(parameterSpec)
            }
        }

        val paramsBlock =
            if (parameters.isNotEmpty()) {
                CodeBlock.builder().add(
                    "val arguments: $SCREEN_ROUTE.$routeName = %M(backstackEntry)",
                    MemberName("${COMPOSEFLOW_PACKAGE}.util", "toRoute"),
                )
            } else {
                CodeBlock.builder()
            }

        val paramsPassersString =
            buildString {
                if (parameters.isNotEmpty()) {
                    append("arguments = arguments,\n")
                }
            }

        val funSpec =
            funSpecBuilder
                .addCode(
                    """
    %M<$SCREEN_ROUTE.$routeName> { backstackEntry ->
        ${paramsBlock.build()}
        $composableName($paramsPassersString$argumentPassersString)
    }
            """,
                    MemberName("androidx.navigation.compose", "composable"),
                ).build()
        fileBuilder.addFunction(funSpec).build()
        fileBuilder.suppressRedundantVisibilityModifier()
        return fileBuilder.build()
    }

    override fun updateHoveredNode(eventPosition: Offset) {
        rootNode.value.clearIsHoveredRecursively()
        fabNode.value?.clearIsHoveredRecursively()
        bottomAppBarNode.value?.clearIsHoveredRecursively()
        navigationDrawerNode.value?.clearIsHoveredRecursively()
        fabNode.value?.findDeepestChildAtOrNull(eventPosition)?.let {
            it.isHovered.value = true
            return
        }
        navigationDrawerNode.value?.findDeepestChildAtOrNull(eventPosition)?.let {
            it.isHovered.value = true
            return
        }
        getBottomAppBar()?.let { bottomAppBar ->
            bottomAppBar.findDeepestChildAtOrNull(eventPosition)?.let {
                it.isHovered.value = true
                return
            }
        }

        rootNode.value.findDeepestChildAtOrNull(eventPosition)?.let {
            it.isHovered.value = true
        }
    }

    override fun updateFocusedNode(
        eventPosition: Offset,
        isCtrlOrMetaPressed: Boolean,
    ) {
        if (!isCtrlOrMetaPressed) {
            rootNode.value.clearIsFocusedRecursively()
            fabNode.value?.clearIsFocusedRecursively()
            bottomAppBarNode.value?.clearIsFocusedRecursively()
            navigationDrawerNode.value?.clearIsFocusedRecursively()
        }
        fabNode.value?.findDeepestChildAtOrNull(eventPosition)?.let {
            it.setFocus(toggleValue = isCtrlOrMetaPressed)
            return
        }
        navigationDrawerNode.value?.findDeepestChildAtOrNull(eventPosition)?.let {
            it.setFocus(toggleValue = isCtrlOrMetaPressed)
            return
        }
        getBottomAppBar()?.let { bottomAppBar ->
            bottomAppBar.findDeepestChildAtOrNull(eventPosition)?.let {
                it.setFocus(toggleValue = isCtrlOrMetaPressed)
                return
            }
        }
        rootNode.value.findDeepestChildAtOrNull(eventPosition)?.let {
            it.setFocus(toggleValue = isCtrlOrMetaPressed)
        }
    }

    override fun findDeepestChildAtOrNull(position: Offset): ComposeNode? {
        val fab = fabNode.value?.findDeepestChildAtOrNull(position)
        if (fab != null) {
            return fab
        }
        val navDrawer = navigationDrawerNode.value?.findDeepestChildAtOrNull(position)
        if (navDrawer != null) {
            return navDrawer
        }
        val bottomAppBar = getBottomAppBar()?.findDeepestChildAtOrNull(position)
        if (bottomAppBar != null) {
            return bottomAppBar
        }
        return rootNode.value.findDeepestChildAtOrNull(position)
    }

    override fun findNodeById(id: String): ComposeNode? {
        if (id == this.id) {
            if (rootNode.value.trait.value is ScreenTrait) {
                return rootNode.value
            }
        }
        return getAllComposeNodes().firstOrNull {
            it.id == id
        }
    }

    private fun getAllRootNodes(): List<ComposeNode?> =
        listOf(
            navigationDrawerNode.value,
            topAppBarNode.value,
            bottomAppBarNode.value,
            rootNode.value,
            fabNode.value,
        )

    override fun getRootNode(): ComposeNode = rootNode.value

    override fun getContentRootNode(): ComposeNode = contentRootNode()

    override fun findFocusedNodes(): List<ComposeNode> =
        getAllComposeNodes().filter { it.isFocused.value }

    override fun clearIsHoveredRecursively() {
        getAllRootNodes().forEach { it?.clearIsHoveredRecursively() }
    }

    override fun clearIsFocusedRecursively() {
        getAllRootNodes().forEach { it?.clearIsFocusedRecursively() }
    }

    override fun clearIsDraggedOnBoundsRecursively() {
        getAllRootNodes().forEach { it?.clearIsDraggedOnBoundsRecursively() }
    }

    override fun clearIndexToBeDroppedRecursively() {
        getAllRootNodes().forEach { it?.clearIndexToBeDroppedRecursively() }
    }

    override fun getAllComposeNodes(): List<ComposeNode> =
        rootNode.value.allChildren() +
            (topAppBarNode.value?.allChildren() ?: emptyList()) +
            (bottomAppBarNode.value?.allChildren() ?: emptyList()) +
            (navigationDrawerNode.value?.allChildren() ?: emptyList()) +
            (fabNode.value?.allChildren() ?: emptyList())

    @Composable
    fun thumbnail(
        project: Project,
        thumbnailName: String = name,
        includeUpdateTime: Boolean = true,
        modifier: Modifier = Modifier,
    ) {
        var deviceSizeDp by remember { mutableStateOf(IntSize.Zero) }
        val density = LocalDensity.current
        Column(
            modifier =
                modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    ).switchByHovered(
                        hovered =
                            Modifier
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(16.dp),
                                ).hoverIconClickable(),
                        notHovered =
                            Modifier.alpha(0.5f).border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.secondary,
                                shape = RoundedCornerShape(16.dp),
                            ),
                    ),
        ) {
            Text(
                text = thumbnailName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(start = 24.dp, top = 16.dp),
            )
            if (includeUpdateTime) {
                project.lastModified?.let { lastModified ->
                    Text(
                        "Updated: ${lastModified.asDateString()}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = 8.dp, start = 24.dp),
                    )
                }
            }

            ProvideDeviceSizeDp(deviceSizeDp) {
                ProvideAppThemeTokens(
                    isDarkTheme = LocalUseDarkTheme.current,
                    lightScheme =
                        project.themeHolder.colorSchemeHolder.lightColorScheme.value
                            .toColorScheme(),
                    darkScheme =
                        project.themeHolder.colorSchemeHolder.darkColorScheme.value
                            .toColorScheme(),
                    typography = project.themeHolder.fontHolder.generateTypography(),
                ) {
                    Surface(
                        modifier =
                            Modifier
                                .scale(0.85f)
                                .clip(RoundedCornerShape(8.dp))
                                .onGloballyPositioned {
                                    deviceSizeDp = it.size / density.density.toInt()
                                },
                    ) {
                        contentRootNode().RenderedNodeInCanvas(
                            project = project,
                            canvasNodeCallbacks = emptyCanvasNodeCallbacks,
                            paletteRenderParams = PaletteRenderParams(isThumbnail = true),
                            zoomableContainerStateHolder = ZoomableContainerStateHolder(),
                            modifier =
                                Modifier
                                    .onClick(enabled = true, onClick = {})
                                    .size(width = 330.dp, height = 460.dp),
                        )
                    }
                }
            }
        }
    }

    /**
     * Returns true if the screen has screen specific TopAppBar.
     * If the screen is part of the navigation, common TopAppBar is defined in the in the outer
     * scaffold.
     */
    private fun hasOwnTopAppBar(): Boolean = topAppBarNode.value != null

    @Composable
    override fun asDropdownText(): AnnotatedString = AnnotatedString(name)

    override fun isSameItem(item: Any): Boolean {
        val otherScreen = item as? Screen ?: return false
        return otherScreen.id == id
    }
}

fun Screen.restoreInstance(): Screen {
    val encoded = encodeToString(this)
    val decoded = decodeFromStringWithFallback<Screen>(encoded)
    return decoded
}

fun Screen.createCopyOfNewName(newName: String): Screen =
    copy(
        name = newName,
        title = mutableStateOf(newName),
        label = mutableStateOf(newName),
    )

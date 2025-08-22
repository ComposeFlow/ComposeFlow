package io.composeflow.model.project.appscreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.coroutines.FlowSettings
import io.composeflow.kotlinpoet.wrapper.ClassNameWrapper
import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper
import io.composeflow.kotlinpoet.wrapper.FileSpecWrapper
import io.composeflow.kotlinpoet.wrapper.FunSpecWrapper
import io.composeflow.kotlinpoet.wrapper.KModifierWrapper
import io.composeflow.kotlinpoet.wrapper.MemberNameWrapper
import io.composeflow.kotlinpoet.wrapper.ParameterSpecWrapper
import io.composeflow.kotlinpoet.wrapper.PropertySpecWrapper
import io.composeflow.kotlinpoet.wrapper.TypeSpecWrapper
import io.composeflow.kotlinpoet.wrapper.AnnotationSpecWrapper
import io.composeflow.kotlinpoet.wrapper.asTypeNameWrapper
import io.composeflow.ViewModelConstant
import io.composeflow.kotlinpoet.wrapper.suppressRedundantVisibilityModifier
import io.composeflow.kotlinpoet.ClassHolder
import io.composeflow.kotlinpoet.FileSpecWithDirectory
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.kotlinpoet.MemberHolder
import io.composeflow.materialicons.Filled
import io.composeflow.materialicons.asCodeBlock
import io.composeflow.model.datatype.generateCodeBlock
import io.composeflow.model.parameter.ScrollBehaviorWrapper
import io.composeflow.model.project.COMPOSEFLOW_PACKAGE
import io.composeflow.model.project.CanvasEditable
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.Screen
import io.composeflow.model.project.appscreen.screen.ScreenId
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.project.appscreen.screen.restoreInstance
import io.composeflow.model.project.component.Component
import io.composeflow.model.project.issue.DestinationContext
import io.composeflow.model.project.issue.NavigatableDestination
import io.composeflow.model.state.AppState
import io.composeflow.override.mutableStateListEqualsOverrideOf
import io.composeflow.serializer.MutableStateListSerializer
import io.composeflow.serializer.MutableStateSerializer
import io.composeflow.util.generateUniqueName
import io.composeflow.util.toKotlinFileName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import moe.tlaster.precompose.viewmodel.ViewModel
import org.koin.core.component.KoinComponent

const val APP_VIEW_MODEL = "AppViewModel"
const val SCREEN_ROUTE = "ScreenRoute"
val screenRouteClass = ClassNameWrapper.get(packageName = "", SCREEN_ROUTE)
val screenRouteClassName = ClassNameWrapper.get("", SCREEN_ROUTE)

@Serializable
@SerialName("ScreenHolder")
data class ScreenHolder(
    @Serializable(with = MutableStateListSerializer::class)
    val screens: MutableList<Screen> =
        mutableStateListEqualsOverrideOf(
            Screen(
                name = "Home",
                rootNode = mutableStateOf(ComposeNode.createRootNode()),
                icon = mutableStateOf(Filled.Home),
                isDefault = mutableStateOf(true),
            ),
        ),
    /**
     * Set to true if the Navigation should be visible
     */
    @Serializable(with = MutableStateSerializer::class)
    val showNavigation: MutableState<Boolean> = mutableStateOf(true),
    @Serializable(MutableStateSerializer::class)
    val loginScreenId: MutableState<ScreenId?> =
        mutableStateOf(
            null,
        ),
    var pendingDestination: NavigatableDestination? = null,
    var pendingDestinationContext: DestinationContext? = null,
) {
    @Serializable(MutableStateSerializer::class)
    val editedComponent = mutableStateOf<Component?>(null)

    fun currentEditable(): CanvasEditable = editedComponent.value ?: currentScreen()

    fun currentScreen(): Screen =
        screens.firstOrNull { it.isSelected.value }
            ?: screens.firstOrNull { it.isDefault.value }
            ?: screens.first()

    fun currentRootNode(): ComposeNode = currentEditable().getRootNode()

    fun currentContentRootNode(): ComposeNode = currentEditable().getContentRootNode()

    fun findScreen(id: String) = screens.firstOrNull { it.id == id }

    fun selectScreen(screen: Screen) {
        if (!screens.contains(screen)) return
        screens.forEach { it.isSelected.value = false }
        screens.firstOrNull { it.id == screen.id }?.isSelected?.value = true
        editedComponent.value = null
    }

    fun setDefaultScreen(screen: Screen) {
        if (!screens.contains(screen)) return
        screens.forEach { it.isDefault.value = false }
        screens.firstOrNull { it.id == screen.id }?.isDefault?.value = true
    }

    fun addScreen(
        name: String,
        newScreen: Screen,
    ): Screen {
        val newName =
            generateUniqueName(initial = name, existing = screens.map { it.name }.toSet())
        val screenToAdd =
            newScreen.restoreInstance().copy(
                name = newName,
                title = mutableStateOf(newName),
                label = mutableStateOf(newName),
            )

        screenToAdd.rootNode.value.updateChildParentRelationships()
        screens.add(screenToAdd)
        return screenToAdd
    }

    fun updateScreen(screen: Screen) {
        val index = screens.indexOfFirst { it.id == screen.id }
        if (index != -1) {
            screens[index] = screen
        }
    }

    fun deleteScreen(screen: Screen) {
        screens.remove(screen)
    }

    fun findFocusedNodes(): List<ComposeNode> =
        if (editedComponent.value != null) {
            editedComponent.value?.findFocusedNodes()!!.distinctBy { it.id }
        } else {
            currentScreen().findFocusedNodes().distinctBy { it.id }
        }

    fun findDeepestChildAtOrNull(position: Offset): ComposeNode? {
        val currentEditable = currentEditable()
        return currentEditable.findDeepestChildAtOrNull(position)
    }

    /**
     * Update the focused node.
     *
     * if [isCtrlOrMetaPressed] is true, it doesn't clear the existing focused nodes.
     */
    fun updateFocusedNode(
        eventPosition: Offset,
        isCtrlOrMetaPressed: Boolean = false,
    ) {
        if (!isCtrlOrMetaPressed) {
            currentEditable().clearIsFocusedRecursively()
            screens.forEach {
                it.clearIsFocusedRecursively()
            }
        }
        val currentScreen = currentScreen()
        currentScreen.navigationDrawerNode.value?.findDeepestChildAtOrNull(eventPosition)?.let {
            it.setFocus(toggleValue = isCtrlOrMetaPressed)
            return
        }
        currentScreen.topAppBarNode.value?.findDeepestChildAtOrNull(eventPosition)?.let {
            it.setFocus(toggleValue = isCtrlOrMetaPressed)
            return
        }
        currentEditable().updateFocusedNode(eventPosition, isCtrlOrMetaPressed)
    }

    fun updateHoveredNode(eventPosition: Offset) {
        currentEditable().clearIsHoveredRecursively()
        screens.forEach {
            it.updateHoveredNode(eventPosition)
            it.topAppBarNode.value?.clearIsHoveredRecursively()
        }
        val currentScreen = currentScreen()
        currentScreen.navigationDrawerNode.value?.findDeepestChildAtOrNull(eventPosition)?.let {
            it.isHovered.value = true
            return
        }
        currentScreen.topAppBarNode.value?.findDeepestChildAtOrNull(eventPosition)?.let {
            it.isHovered.value = true
            return
        }
        currentEditable().updateHoveredNode(eventPosition)
    }

    fun getAllComposeNodes(): List<ComposeNode> = screens.flatMap { it.getAllComposeNodes() } + currentEditable().getAllComposeNodes()

    fun clearIsHovered() {
        screens.forEach {
            it.clearIsHoveredRecursively()
        }
        currentEditable().clearIsHoveredRecursively()
    }

    fun clearIsFocused() {
        screens.forEach {
            it.clearIsFocusedRecursively()
        }
        currentEditable().clearIsFocusedRecursively()
    }

    /**
     * Update the child parent relationships for all screens.
     * Decoded ComposeNode tree doesn't have the parentNode relationShip to avoid the
     * infinite loop when serializing the node tree. This is to make sure the restored rootNode
     * for all screens have correct child/parent relationships.
     */
    fun updateChildParentRelationships() {
        screens.forEach { screen ->
            // Decoded ComposeNode tree doesn't have the parentNode relationShip to avoid the
            // infinite loop when serializing the node tree. Restoring the relationShip
            screen.rootNode.value.updateChildParentRelationships()
        }
    }

    fun getLoginScreen(): Screen? =
        loginScreenId.value?.let { loginScreenId ->
            screens.firstOrNull { it.id == loginScreenId }
        }

    fun generateAppNavHostFile(
        project: Project,
        context: GenerationContext,
    ): FileSpecWithDirectory {
        val fileSpecBuilder = FileSpecWrapper.builder("", "AppNavHost")
        val funSpecBuilder = FunSpecWrapper.builder("AppNavHost").addAnnotation(AnnotationSpecWrapper.get(Composable::class))
        val defaultScreen = screens.firstOrNull { it.isDefault.value } ?: screens.first()
        val loginScreen = getLoginScreen()
        if (loginScreen != null) {
            val authenticatedUserName = "authenticatedUser"
            context.getCurrentComposableContext().addCompositionLocalVariableEntryIfNotPresent(
                id = "appNavHost",
                initialIdentifier = authenticatedUserName,
                MemberHolder.ComposeFlow.LocalAuthenticatedUser,
            )
            funSpecBuilder.addCode(
                """
                val $authenticatedUserName = %M.current
                val startDestination = if ($authenticatedUserName == null) { ScreenRoute.${loginScreen.routeName} } else { initialRoute }
            """,
                MemberHolder.ComposeFlow.LocalAuthenticatedUser,
            )
        }
        val initialRouteName = if (loginScreen != null) "startDestination" else "initialRoute"
        funSpecBuilder
            .addParameters(
                listOf(
                    ParameterSpecWrapper
                        .builder("navController", NavHostController::class.asTypeNameWrapper())
                        .build(),
                    ParameterSpecWrapper
                        .builder("initialRoute", screenRouteClassName)
                        .defaultValue(CodeBlockWrapper.of(defaultScreen.defaultRouteCodeBlock().toString()))
                        .build(),
                ),
            ).addCode(
                """
    %M(
        navController = navController,
        startDestination = $initialRouteName,
    ) {
        """,
                MemberNameWrapper.get("androidx.navigation.compose", "NavHost"),
            )

        val codeBuilder = CodeBlockWrapper.builder()
        screens.forEach {
            codeBuilder.addStatement("%M(", MemberNameWrapper.get(it.getPackageName(project), it.sceneName))
            codeBuilder.add(CodeBlockWrapper.of(it.generateArgumentsInitializationCodeBlock(project).toString()))
            codeBuilder.addStatement(")")
        }
        funSpecBuilder.addCode(codeBuilder.build())

        funSpecBuilder.addCode("}")
        fileSpecBuilder.addFunction(funSpecBuilder.build())
        fileSpecBuilder.suppressRedundantVisibilityModifier()
        return FileSpecWithDirectory(fileSpecBuilder.build())
    }

    fun generateComposeLauncherFile(): FileSpecWithDirectory {
        val fileBuilder =
            FileSpecWrapper
                .builder("", "App")
                .addImport("androidx.compose.runtime", "getValue")
                .addImport("androidx.compose.runtime", "mutableStateOf")
                .addImport("androidx.compose.runtime", "setValue")
        val appFunSpecBuilder = FunSpecWrapper.builder("App").addAnnotation(AnnotationSpecWrapper.get(Composable::class))
        appFunSpecBuilder.addStatement(
            """
    val appViewModel = %M($APP_VIEW_MODEL::class)
    val navController = %M()
    val snackbarHostState = %M { %M() }
    """,
            MemberHolder.PreCompose.koinViewModel,
            MemberNameWrapper.get("androidx.navigation.compose", "rememberNavController"),
            MemberNameWrapper.get("androidx.compose.runtime", "remember"),
            MemberNameWrapper.get("androidx.compose.material3", "SnackbarHostState"),
        )

        appFunSpecBuilder.addStatement(
            "val backstack by navController.%M()",
            MemberNameWrapper.get("androidx.navigation.compose", "currentBackStackEntryAsState"),
        )
        appFunSpecBuilder.addStatement(
            """
        val currentDestination by %M {
            ScreenDestination.entries
                .firstOrNull { it.route.isCurrentDestination(backstack) }
        }
        val isTopLevel by %M { currentDestination?.isTopLevel == true }
        """,
            MemberNameWrapper.get("androidx.compose.runtime", "derivedStateOf"),
            MemberNameWrapper.get("androidx.compose.runtime", "derivedStateOf"),
        )

        appFunSpecBuilder.addStatement(
            "%M.providesDefault(%M())",
            MemberNameWrapper.get("$COMPOSEFLOW_PACKAGE.common", "LocalUseDarkTheme"),
            MemberNameWrapper.get("androidx.compose.foundation", "isSystemInDarkTheme"),
        )

        appFunSpecBuilder.addStatement(
            """
%M {
    %M(snackbarHostState) {
        %M {
    """,
            MemberNameWrapper.get("${COMPOSEFLOW_PACKAGE}.auth", "ProvideAuthenticatedUser"),
            MemberNameWrapper.get("${COMPOSEFLOW_PACKAGE}.ui", "ProvideOnShowSnackbar"),
            MemberNameWrapper.get("${COMPOSEFLOW_PACKAGE}.common", "AppTheme"),
        )

        if (showNavigation.value) {
            appFunSpecBuilder.addStatement(
                """
            %M(
                navigationSuiteItems = {
                    ScreenDestination.entries.forEach { destination ->
                        item(
                            selected = currentDestination == destination,
                            onClick = {
                                val options = %M.Builder().setLaunchSingleTop(true).build()
                                navController.navigate(
                                    route = destination.route,
                                    navOptions = options
                                )
                            },
                            icon = {
                                %M(
                                    imageVector = destination.icon,
                                    contentDescription = null,
                                )
                            },
                            label = { %M(text = destination.label) },
                        )
                    }
                },
                layoutType = %M(isTopLevel),
                ) {""",
                MemberNameWrapper.get(
                    "androidx.compose.material3.adaptive.navigationsuite",
                    "NavigationSuiteScaffold",
                ),
                MemberNameWrapper.get("androidx.navigation", "NavOptions"),
                MemberNameWrapper.get("androidx.compose.material3", "Icon"),
                MemberHolder.Material3.Text,
                MemberNameWrapper.get("${COMPOSEFLOW_PACKAGE}.util", "calculateCustomNavSuiteType"),
            )
        }

        appFunSpecBuilder.addStatement(
            """
                %M (""",
            MemberNameWrapper.get("androidx.compose.material3", "Scaffold"),
        )

        appFunSpecBuilder.addStatement(
            "snackbarHost = { %M(snackbarHostState) }",
            MemberNameWrapper.get("androidx.compose.material3", "SnackbarHost"),
        )

        appFunSpecBuilder.addStatement(
            """
            ) {
                AppNavHost(
                    navController = navController,
                )
            }
        }
    }
}
    """,
            MemberHolder.AndroidX.Ui.Modifier,
            MemberNameWrapper.get("androidx.compose.foundation.layout", "padding"),
            MemberHolder.AndroidX.Layout.Column,
        )

        if (showNavigation.value) {
            appFunSpecBuilder.addStatement("}") // Close NavigationSuiteScaffold
        }
        fileBuilder.addFunction(appFunSpecBuilder.build())
        fileBuilder.addType(generateScreenDestinationEnum())
        fileBuilder.suppressRedundantVisibilityModifier()
        return FileSpecWithDirectory(fileBuilder.build())
    }

    @OptIn(ExperimentalSettingsApi::class)
    fun generateAppViewModel(project: Project): FileSpecWithDirectory {
        val fileBuilder = FileSpecWrapper.builder("", APP_VIEW_MODEL)
        val typeBuilder =
            TypeSpecWrapper
                .classBuilder(APP_VIEW_MODEL)
                .superclass(ViewModel::class.asTypeNameWrapper())
                .addSuperinterface(KoinComponent::class.asTypeNameWrapper())
        if (project.globalStateHolder
                .getStates(project)
                .any { it is AppState.CustomDataTypeListAppState }
        ) {
            typeBuilder.addProperty(
                PropertySpecWrapper
                    .builder(ViewModelConstant.jsonSerializer.name, Json::class.asTypeNameWrapper())
                    .addModifiers(KModifierWrapper.PRIVATE)
                    .delegate(
                        CodeBlockWrapper
                            .builder()
                            .add("%M()", MemberHolder.Koin.inject)
                            .build(),
                    ).build(),
            )
            typeBuilder.addProperty(
                PropertySpecWrapper
                    .builder(ViewModelConstant.flowSettings.name, FlowSettings::class.asTypeNameWrapper())
                    .addModifiers(KModifierWrapper.PRIVATE)
                    .delegate(
                        CodeBlockWrapper
                            .builder()
                            .beginControlFlow("lazy")
                            .add("%M()", MemberHolder.Koin.get)
                            .endControlFlow()
                            .build(),
                    ).build(),
            )

            val codeBlockBuilder = CodeBlockWrapper.builder()
            codeBlockBuilder.beginControlFlow(
                "%M.%M",
                MemberHolder.PreCompose.viewModelScope,
                MemberHolder.Coroutines.launch,
            )
            project.globalStateHolder.getStates(project).forEach { state ->
                if (state is AppState.CustomDataTypeListAppState && state.defaultValue.isNotEmpty()) {
                    codeBlockBuilder.add(
                        CodeBlockWrapper.of(
                            """
                            ${ViewModelConstant.flowSettings.name}.putString("${state.name}",
                                 ${ViewModelConstant.jsonSerializer.name}.%M(%L)
                            )
                        """,
                            MemberHolder.Serialization.encodeToString,
                            state.defaultValue.generateCodeBlock(project).toString(),
                        ),
                    )
                }
            }
            codeBlockBuilder.endControlFlow()

            typeBuilder.addInitializerBlock(
                codeBlockBuilder.build(),
            )
        }

        fileBuilder.addType(typeBuilder.build())
        fileBuilder.suppressRedundantVisibilityModifier()

        return FileSpecWithDirectory(fileBuilder.build())
    }

    fun generateKoinViewModelModule(project: Project): FileSpecWithDirectory {
        val fileBuilder =
            FileSpecWrapper.builder(
                fileName = "ViewModelModule",
                packageName = "${COMPOSEFLOW_PACKAGE}.screens",
            )
        val funSpecBuilder =
            FunSpecWrapper
                .builder("screenViewModelModule")
                .returns(org.koin.core.module.Module::class.asTypeNameWrapper())
                .addCode("return %M {", MemberNameWrapper.get("org.koin.dsl", "module"))

        funSpecBuilder.addStatement("factory { %T() } ", ClassNameWrapper.get("", APP_VIEW_MODEL))

        screens.forEach {
            funSpecBuilder.addStatement(
                "factory { %T() } ",
                ClassNameWrapper.get(it.getPackageName(project), it.viewModelFileName),
            )
        }
        funSpecBuilder.addCode("}")
        fileBuilder.addFunction(funSpecBuilder.build())
        fileBuilder.suppressRedundantVisibilityModifier()
        return FileSpecWithDirectory(fileBuilder.build())
    }

    fun generateScreenRouteFileSpec(project: Project): FileSpecWithDirectory {
        val fileBuilder =
            FileSpecWrapper.builder(
                fileName = SCREEN_ROUTE,
                packageName = "",
            )

        val routeName = "routeName"
        val screenRouteBuilder =
            TypeSpecWrapper
                .interfaceBuilder(SCREEN_ROUTE)
                .addAnnotation(AnnotationSpecWrapper.builder(ClassHolder.Kotlinx.Serialization.Serializable).build())
                .addModifiers(KModifierWrapper.SEALED)
                .addProperty(PropertySpecWrapper.builder(routeName, String::class.asTypeNameWrapper()).build())
                .addFunction(
                    FunSpecWrapper
                        .builder("isCurrentDestination")
                        .addParameter(
                            "backStackEntry",
                            ClassNameWrapper.get(
                                "androidx.navigation",
                                "NavBackStackEntry",
                            ).copy(nullable = true),
                        ).returns(Boolean::class.asTypeNameWrapper())
                        .addCode("""return backStackEntry?.destination?.route == $routeName""")
                        .build(),
                )

        project.screenHolder.screens.forEach { screen ->
            val typeSpecBuilder =
                if (screen.parameters.isEmpty()) {
                    TypeSpecWrapper.objectBuilder(screen.routeName)
                } else {
                    val classBuilder = TypeSpecWrapper.classBuilder(screen.routeName)
                    val primaryConstructorBuilder = FunSpecWrapper.constructorBuilder()
                    screen.parameters.forEach { parameter ->
                        primaryConstructorBuilder.addParameter(
                            parameter.generateArgumentParameterSpec(project),
                        )
                        classBuilder.addProperty(
                            PropertySpecWrapper
                                .builder(
                                    parameter.variableName,
                                    parameter.parameterType.asKotlinPoetTypeName(project),
                                ).initializer(parameter.variableName)
                                .build(),
                        )
                    }
                    classBuilder.primaryConstructor(primaryConstructorBuilder.build())
                    classBuilder
                }.apply {
                    addModifiers(KModifierWrapper.DATA)
                    addSuperinterface(screenRouteClass)
                    addAnnotation(AnnotationSpecWrapper.builder(ClassHolder.Kotlinx.Serialization.Serializable).build())
                    addProperty(
                        PropertySpecWrapper
                            .builder(routeName, String::class.asTypeNameWrapper())
                            .addModifiers(KModifierWrapper.OVERRIDE)
                            .initializer("%S", "$SCREEN_ROUTE.${screen.routeName}")
                            .build(),
                    )
                }
            screenRouteBuilder.addType(typeSpecBuilder.build())
        }

        fileBuilder.addType(screenRouteBuilder.build())
        fileBuilder.suppressRedundantVisibilityModifier()
        return FileSpecWithDirectory(fileBuilder.build())
    }

    private fun generateScreenDestinationEnum(): TypeSpecWrapper {
        val enumBuilder =
            TypeSpecWrapper
                .enumBuilder("ScreenDestination")
                .primaryConstructor(
                    FunSpecWrapper
                        .constructorBuilder()
                        .addParameter("icon", ImageVector::class.asTypeNameWrapper())
                        .addParameter("isTopLevel", Boolean::class.asTypeNameWrapper())
                        .addParameter("label", String::class.asTypeNameWrapper())
                        .addParameter("title", String::class.asTypeNameWrapper())
                        .addParameter("route", screenRouteClass)
                        .build(),
                ).addProperty(
                    PropertySpecWrapper
                        .builder("icon", ImageVector::class.asTypeNameWrapper())
                        .initializer("icon")
                        .build(),
                ).addProperty(
                    PropertySpecWrapper
                        .builder("isTopLevel", Boolean::class.asTypeNameWrapper())
                        .initializer("isTopLevel")
                        .build(),
                ).addProperty(
                    PropertySpecWrapper
                        .builder("label", String::class.asTypeNameWrapper())
                        .initializer("label")
                        .build(),
                ).addProperty(
                    PropertySpecWrapper
                        .builder("title", String::class.asTypeNameWrapper())
                        .initializer("title")
                        .build(),
                ).addProperty(
                    PropertySpecWrapper
                        .builder("route", screenRouteClass)
                        .initializer("route")
                        .build(),
                )

        screens
            .filter {
                it.showOnNavigation.value &&
                    // The screen specified as login screen can't be the top level destination
                    loginScreenId.value != it.id
            }.forEach {
                enumBuilder.addEnumConstant(
                    it.name.toKotlinFileName(),
                    TypeSpecWrapper
                        .anonymousClassBuilder()
                        .addSuperclassConstructorParameter(it.icon.value.asCodeBlock())
                        .addSuperclassConstructorParameter("%L", it.showOnNavigation.value)
                        .addSuperclassConstructorParameter("%S", it.label.value)
                        .addSuperclassConstructorParameter("%S", it.title.value)
                        .addSuperclassConstructorParameter(CodeBlockWrapper.of(it.defaultRouteCodeBlock().toString()))
                        .build(),
                )
            }
        return enumBuilder.build()
    }

    companion object {
        fun generateCodeBlockFromScrollBehavior(scrollBehaviorWrapper: ScrollBehaviorWrapper): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            when (scrollBehaviorWrapper) {
                ScrollBehaviorWrapper.None -> { // no-op
                }

                ScrollBehaviorWrapper.EnterAlways -> {
                    builder.addStatement(
                        "val scrollBehavior = %M()",
                        MemberNameWrapper.get(
                            "androidx.compose.material3.TopAppBarDefaults",
                            "enterAlwaysScrollBehavior",
                        ),
                    )
                }

                ScrollBehaviorWrapper.ExitUntilCollapsed -> {
                    builder.addStatement(
                        "val scrollBehavior = %M()",
                        MemberNameWrapper.get(
                            "androidx.compose.material3.TopAppBarDefaults",
                            "exitUntilCollapsedScrollBehavior",
                        ),
                    )
                }

                ScrollBehaviorWrapper.Pinned -> {
                    builder.addStatement(
                        "val scrollBehavior = %M()",
                        MemberNameWrapper.get(
                            "androidx.compose.material3.TopAppBarDefaults",
                            "pinnedScrollBehavior",
                        ),
                    )
                }
            }
            return builder.build()
        }
    }
}

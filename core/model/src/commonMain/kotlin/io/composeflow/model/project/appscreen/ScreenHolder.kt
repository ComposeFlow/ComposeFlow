package io.composeflow.model.project.appscreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.coroutines.FlowSettings
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import io.composeflow.ViewModelConstant
import io.composeflow.formatter.suppressRedundantVisibilityModifier
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
val screenRouteClass = ClassName(packageName = "", SCREEN_ROUTE)

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
        val fileSpecBuilder = FileSpec.builder("", "AppNavHost")
        val funSpecBuilder = FunSpec.builder("AppNavHost").addAnnotation(Composable::class)
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
                    ParameterSpec
                        .builder("navController", NavHostController::class)
                        .build(),
                    ParameterSpec
                        .builder("initialRoute", screenRouteClass)
                        .defaultValue(defaultScreen.defaultRouteCodeBlock())
                        .build(),
                ),
            ).addCode(
                """
    %M(
        navController = navController,
        startDestination = $initialRouteName,
    ) {
        """,
                MemberName("androidx.navigation.compose", "NavHost"),
            )

        val codeBuilder = CodeBlock.builder()
        screens.forEach {
            codeBuilder.addStatement("%M(", MemberName(it.getPackageName(project), it.sceneName))
            codeBuilder.add(it.generateArgumentsInitializationCodeBlock(project))
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
            FileSpec
                .builder("", "App")
                .addImport("androidx.compose.runtime", "getValue")
                .addImport("androidx.compose.runtime", "mutableStateOf")
                .addImport("androidx.compose.runtime", "setValue")
        val appFunSpecBuilder = FunSpec.builder("App").addAnnotation(Composable::class)
        appFunSpecBuilder.addStatement(
            """
    val appViewModel = %M($APP_VIEW_MODEL::class)
    val navController = %M()
    val snackbarHostState = %M { %M() }
    """,
            MemberHolder.PreCompose.koinViewModel,
            MemberName("androidx.navigation.compose", "rememberNavController"),
            MemberName("androidx.compose.runtime", "remember"),
            MemberName("androidx.compose.material3", "SnackbarHostState"),
        )

        appFunSpecBuilder.addStatement(
            "val backstack by navController.%M()",
            MemberName("androidx.navigation.compose", "currentBackStackEntryAsState"),
        )
        appFunSpecBuilder.addStatement(
            """
        val currentDestination by %M {
            ScreenDestination.entries
                .firstOrNull { it.route.isCurrentDestination(backstack) }
        }
        val isTopLevel by %M { currentDestination?.isTopLevel == true }
        """,
            MemberName("androidx.compose.runtime", "derivedStateOf"),
            MemberName("androidx.compose.runtime", "derivedStateOf"),
        )

        appFunSpecBuilder.addStatement(
            "%M.providesDefault(%M())",
            MemberName("$COMPOSEFLOW_PACKAGE.common", "LocalUseDarkTheme"),
            MemberName("androidx.compose.foundation", "isSystemInDarkTheme"),
        )

        appFunSpecBuilder.addStatement(
            """
%M {
    %M(snackbarHostState) {
        %M {
    """,
            MemberName("${COMPOSEFLOW_PACKAGE}.auth", "ProvideAuthenticatedUser"),
            MemberName("${COMPOSEFLOW_PACKAGE}.ui", "ProvideOnShowSnackbar"),
            MemberName("${COMPOSEFLOW_PACKAGE}.common", "AppTheme"),
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
                MemberName(
                    "androidx.compose.material3.adaptive.navigationsuite",
                    "NavigationSuiteScaffold",
                ),
                MemberName("androidx.navigation", "NavOptions"),
                MemberName("androidx.compose.material3", "Icon"),
                MemberHolder.Material3.Text,
                MemberName("${COMPOSEFLOW_PACKAGE}.util", "calculateCustomNavSuiteType"),
            )
        }

        appFunSpecBuilder.addStatement(
            """
                %M (""",
            MemberName("androidx.compose.material3", "Scaffold"),
        )

        appFunSpecBuilder.addStatement(
            "snackbarHost = { %M(snackbarHostState) }",
            MemberName("androidx.compose.material3", "SnackbarHost"),
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
            MemberName("androidx.compose.foundation.layout", "padding"),
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
        val fileBuilder = FileSpec.builder("", APP_VIEW_MODEL)
        val typeBuilder =
            TypeSpec
                .classBuilder(APP_VIEW_MODEL)
                .superclass(ViewModel::class)
                .addSuperinterface(KoinComponent::class)
        if (project.globalStateHolder
                .getStates(project)
                .any { it is AppState.CustomDataTypeListAppState }
        ) {
            typeBuilder.addProperty(
                PropertySpec
                    .builder(ViewModelConstant.jsonSerializer.name, Json::class)
                    .addModifiers(KModifier.PRIVATE)
                    .delegate(
                        CodeBlock
                            .builder()
                            .add("%M()", MemberHolder.Koin.inject)
                            .build(),
                    ).build(),
            )
            typeBuilder.addProperty(
                PropertySpec
                    .builder(ViewModelConstant.flowSettings.name, FlowSettings::class)
                    .addModifiers(KModifier.PRIVATE)
                    .delegate(
                        CodeBlock
                            .builder()
                            .beginControlFlow("lazy")
                            .add("%M()", MemberHolder.Koin.get)
                            .endControlFlow()
                            .build(),
                    ).build(),
            )

            val codeBlockBuilder = CodeBlock.builder()
            codeBlockBuilder.beginControlFlow(
                "%M.%M",
                MemberHolder.PreCompose.viewModelScope,
                MemberHolder.Coroutines.launch,
            )
            project.globalStateHolder.getStates(project).forEach { state ->
                if (state is AppState.CustomDataTypeListAppState && state.defaultValue.isNotEmpty()) {
                    codeBlockBuilder.add(
                        CodeBlock.of(
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
            FileSpec.builder(
                fileName = "ViewModelModule",
                packageName = "${COMPOSEFLOW_PACKAGE}.screens",
            )
        val funSpecBuilder =
            FunSpec
                .builder("screenViewModelModule")
                .returns(org.koin.core.module.Module::class)
                .addCode("return %M {", MemberName("org.koin.dsl", "module"))

        funSpecBuilder.addStatement("factory { %T() } ", ClassName("", APP_VIEW_MODEL))

        screens.forEach {
            funSpecBuilder.addStatement(
                "factory { %T() } ",
                ClassName(it.getPackageName(project), it.viewModelFileName),
            )
        }
        funSpecBuilder.addCode("}")
        fileBuilder.addFunction(funSpecBuilder.build())
        fileBuilder.suppressRedundantVisibilityModifier()
        return FileSpecWithDirectory(fileBuilder.build())
    }

    fun generateScreenRouteFileSpec(project: Project): FileSpecWithDirectory {
        val fileBuilder =
            FileSpec.builder(
                fileName = SCREEN_ROUTE,
                packageName = "",
            )

        val routeName = "routeName"
        val screenRouteBuilder =
            TypeSpec
                .interfaceBuilder(SCREEN_ROUTE)
                .addAnnotation(ClassHolder.Kotlinx.Serialization.Serializable)
                .addModifiers(KModifier.SEALED)
                .addProperty(PropertySpec.builder(routeName, String::class).build())
                .addFunction(
                    FunSpec
                        .builder("isCurrentDestination")
                        .addParameter(
                            "backStackEntry",
                            ClassName(
                                "androidx.navigation",
                                "NavBackStackEntry",
                            ).copy(nullable = true),
                        ).returns(Boolean::class)
                        .addCode("""return backStackEntry?.destination?.route == $routeName""")
                        .build(),
                )

        project.screenHolder.screens.forEach { screen ->
            val typeSpecBuilder =
                if (screen.parameters.isEmpty()) {
                    TypeSpec.objectBuilder(screen.routeName)
                } else {
                    val classBuilder = TypeSpec.classBuilder(screen.routeName)
                    val primaryConstructorBuilder = FunSpec.constructorBuilder()
                    screen.parameters.forEach { parameter ->
                        primaryConstructorBuilder.addParameter(
                            parameter.generateArgumentParameterSpec(project),
                        )
                        classBuilder.addProperty(
                            PropertySpec
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
                    addModifiers(KModifier.DATA)
                    addSuperinterface(screenRouteClass)
                    addAnnotation(ClassHolder.Kotlinx.Serialization.Serializable)
                    addProperty(
                        PropertySpec
                            .builder(routeName, String::class)
                            .addModifiers(KModifier.OVERRIDE)
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

    private fun generateScreenDestinationEnum(): TypeSpec {
        val enumBuilder =
            TypeSpec
                .enumBuilder("ScreenDestination")
                .primaryConstructor(
                    FunSpec
                        .constructorBuilder()
                        .addParameter("icon", ImageVector::class)
                        .addParameter("isTopLevel", Boolean::class)
                        .addParameter("label", String::class)
                        .addParameter("title", String::class)
                        .addParameter("route", screenRouteClass)
                        .build(),
                ).addProperty(
                    PropertySpec
                        .builder("icon", ImageVector::class)
                        .initializer("icon")
                        .build(),
                ).addProperty(
                    PropertySpec
                        .builder("isTopLevel", Boolean::class)
                        .initializer("isTopLevel")
                        .build(),
                ).addProperty(
                    PropertySpec
                        .builder("label", String::class)
                        .initializer("label")
                        .build(),
                ).addProperty(
                    PropertySpec
                        .builder("title", String::class)
                        .initializer("title")
                        .build(),
                ).addProperty(
                    PropertySpec
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
                    TypeSpec
                        .anonymousClassBuilder()
                        .addSuperclassConstructorParameter(it.icon.value.asCodeBlock())
                        .addSuperclassConstructorParameter("%L", it.showOnNavigation.value)
                        .addSuperclassConstructorParameter("%S", it.label.value)
                        .addSuperclassConstructorParameter("%S", it.title.value)
                        .addSuperclassConstructorParameter(it.defaultRouteCodeBlock())
                        .build(),
                )
            }
        return enumBuilder.build()
    }

    companion object {
        fun generateCodeBlockFromScrollBehavior(scrollBehaviorWrapper: ScrollBehaviorWrapper): CodeBlock {
            val builder = CodeBlock.builder()
            when (scrollBehaviorWrapper) {
                ScrollBehaviorWrapper.None -> { // no-op
                }

                ScrollBehaviorWrapper.EnterAlways -> {
                    builder.addStatement(
                        "val scrollBehavior = %M()",
                        MemberName(
                            "androidx.compose.material3.TopAppBarDefaults",
                            "enterAlwaysScrollBehavior",
                        ),
                    )
                }

                ScrollBehaviorWrapper.ExitUntilCollapsed -> {
                    builder.addStatement(
                        "val scrollBehavior = %M()",
                        MemberName(
                            "androidx.compose.material3.TopAppBarDefaults",
                            "exitUntilCollapsedScrollBehavior",
                        ),
                    )
                }

                ScrollBehaviorWrapper.Pinned -> {
                    builder.addStatement(
                        "val scrollBehavior = %M()",
                        MemberName(
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

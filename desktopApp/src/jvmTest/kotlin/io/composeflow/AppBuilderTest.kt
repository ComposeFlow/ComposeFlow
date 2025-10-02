@file:OptIn(kotlin.time.ExperimentalTime::class)

package io.composeflow

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.ExperimentalKermitApi
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.TestLogWriter
import co.touchlab.kermit.loggerConfigInit
import io.composeflow.appbuilder.AppRunner
import io.composeflow.font.FontFamilyWrapper
import io.composeflow.materialicons.Outlined
import io.composeflow.model.action.ActionNode
import io.composeflow.model.action.ActionType
import io.composeflow.model.action.Auth
import io.composeflow.model.action.CallApi
import io.composeflow.model.action.DataFieldUpdateProperty
import io.composeflow.model.action.DateOrTimePicker
import io.composeflow.model.action.FieldUpdateType
import io.composeflow.model.action.FirestoreAction
import io.composeflow.model.action.Navigation
import io.composeflow.model.action.SetValueToState
import io.composeflow.model.action.Share
import io.composeflow.model.action.ShowBottomSheet
import io.composeflow.model.action.ShowConfirmationDialog
import io.composeflow.model.action.ShowCustomDialog
import io.composeflow.model.action.ShowInformationDialog
import io.composeflow.model.action.ShowMessaging
import io.composeflow.model.action.ShowNavigationDrawer
import io.composeflow.model.action.StateAction
import io.composeflow.model.action.StateOperation
import io.composeflow.model.action.StateOperationForDataType
import io.composeflow.model.action.StateOperationForList
import io.composeflow.model.apieditor.ApiDefinition
import io.composeflow.model.apieditor.ApiProperty
import io.composeflow.model.apieditor.Authorization
import io.composeflow.model.apieditor.JsonWithJsonPath
import io.composeflow.model.apieditor.Method
import io.composeflow.model.datatype.DataField
import io.composeflow.model.datatype.DataFieldType
import io.composeflow.model.datatype.DataType
import io.composeflow.model.datatype.DataTypeDefaultValue
import io.composeflow.model.datatype.FieldDefaultValue
import io.composeflow.model.datatype.FieldType
import io.composeflow.model.datatype.FilterFieldType
import io.composeflow.model.datatype.FilterOperator
import io.composeflow.model.datatype.SingleFilter
import io.composeflow.model.enumwrapper.TextDecorationWrapper
import io.composeflow.model.enumwrapper.TextStyleWrapper
import io.composeflow.model.modifier.ModifierWrapper
import io.composeflow.model.parameter.BoxTrait
import io.composeflow.model.parameter.ButtonTrait
import io.composeflow.model.parameter.CardTrait
import io.composeflow.model.parameter.CheckboxTrait
import io.composeflow.model.parameter.ChipGroupTrait
import io.composeflow.model.parameter.ColumnTrait
import io.composeflow.model.parameter.DropdownTrait
import io.composeflow.model.parameter.HorizontalPagerTrait
import io.composeflow.model.parameter.IconButtonTrait
import io.composeflow.model.parameter.IconTrait
import io.composeflow.model.parameter.ImageTrait
import io.composeflow.model.parameter.LazyColumnTrait
import io.composeflow.model.parameter.LazyHorizontalGridTrait
import io.composeflow.model.parameter.LazyRowTrait
import io.composeflow.model.parameter.LazyVerticalGridTrait
import io.composeflow.model.parameter.NavigationDrawerItemTrait
import io.composeflow.model.parameter.NavigationDrawerTrait
import io.composeflow.model.parameter.RowTrait
import io.composeflow.model.parameter.SliderTrait
import io.composeflow.model.parameter.SwitchTrait
import io.composeflow.model.parameter.TabContentTrait
import io.composeflow.model.parameter.TabsTrait
import io.composeflow.model.parameter.TextFieldTrait
import io.composeflow.model.parameter.TextTrait
import io.composeflow.model.parameter.TopAppBarTrait
import io.composeflow.model.parameter.lazylist.LazyListChildParams
import io.composeflow.model.parameter.wrapper.AlignmentWrapper
import io.composeflow.model.parameter.wrapper.BrushType
import io.composeflow.model.parameter.wrapper.BrushWrapper
import io.composeflow.model.parameter.wrapper.ColorWrapper
import io.composeflow.model.parameter.wrapper.Material3ColorWrapper
import io.composeflow.model.parameter.wrapper.defaultColorWrapper
import io.composeflow.model.project.ParameterId
import io.composeflow.model.project.ParameterWrapper
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.Screen
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.project.appscreen.screen.composenode.FormFactorVisibility
import io.composeflow.model.project.appscreen.screen.composenode.VisibilityParams
import io.composeflow.model.project.component.Component
import io.composeflow.model.project.custom_enum.CustomEnum
import io.composeflow.model.project.firebase.FirestoreCollection
import io.composeflow.model.project.string.stringResourceOf
import io.composeflow.model.project.theme.TextStyleOverride
import io.composeflow.model.property.ApiResultProperty
import io.composeflow.model.property.AssignableProperty
import io.composeflow.model.property.BooleanProperty
import io.composeflow.model.property.BrushProperty
import io.composeflow.model.property.ColorProperty
import io.composeflow.model.property.ComposableParameterProperty
import io.composeflow.model.property.ConditionalProperty
import io.composeflow.model.property.CustomEnumValuesProperty
import io.composeflow.model.property.EnumProperty
import io.composeflow.model.property.FirestoreCollectionProperty
import io.composeflow.model.property.FloatProperty
import io.composeflow.model.property.FromInstant
import io.composeflow.model.property.FromString
import io.composeflow.model.property.InstantProperty
import io.composeflow.model.property.IntProperty
import io.composeflow.model.property.StringProperty
import io.composeflow.model.property.ValueFromDynamicItem
import io.composeflow.model.property.ValueFromGlobalProperty
import io.composeflow.model.property.ValueFromState
import io.composeflow.model.state.AppState
import io.composeflow.model.state.AuthenticatedUserState
import io.composeflow.model.state.ScreenState
import io.composeflow.model.validator.TextFieldValidator
import io.composeflow.override.mutableStateListEqualsOverrideOf
import io.composeflow.repository.fakeFirebaseIdToken
import io.composeflow.repository.fakeProjectRepository
import io.composeflow.ui.toolbar.ToolbarViewModel
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.junit.Before
import org.junit.Test
import kotlin.test.assertTrue

/**
 * Tests to build the generated app.
 *
 * The source of truth of the project is now loaded from the ProjectSaver.
 * Loading the project requires a CoroutineScope, but injecting a CoroutineScope in the
 * SkikoComposeUiTest isn't supported as in
 * https://github.com/JetBrains/compose-multiplatform/issues/2960
 *
 * Thus, this class verifies if the generated app builds by passing a Project programmatically.
 */
@OptIn(ExperimentalKermitApi::class)
class AppBuilderTest {
    private val testLogWriter =
        TestLogWriter(
            loggable = Severity.Verbose, // accept everything
        )

    private val toolbarViewModel =
        ToolbarViewModel(
            firebaseIdTokenArg = fakeFirebaseIdToken,
            projectRepository = fakeProjectRepository,
        )

    @Before
    fun setUp() {
        AppRunner.buildLogger = Logger(loggerConfigInit(testLogWriter))
    }

    @Test
    fun emptyProject_fontsOverride() {
        val project = Project()
        project.themeHolder.fontHolder.primaryFontFamily = FontFamilyWrapper.Lato
        project.themeHolder.fontHolder.textStyleOverrides[TextStyleWrapper.DisplayLarge] =
            TextStyleOverride(fontSize = 56)
        toolbarViewModel.onRunPreviewApp(
            project = project,
            onStatusBarUiStateChanged = { _ -> },
        )
        assertBuildSucceed()
    }

    @Test
    fun testStatesWithSameNames() {
        val project = Project()
        val screen = project.screenHolder.currentEditable() as Screen
        screen.addState(ScreenState.StringScreenState(id = "state1", name = "name"))
        screen.addState(ScreenState.StringScreenState(id = "state2", name = "name"))
        toolbarViewModel.onRunPreviewApp(
            project = project,
            onStatusBarUiStateChanged = { _ -> },
        )
        assertBuildSucceed()
    }

    @Test
    fun testBrushBackground() {
        val project = Project()
        val rootNode = project.screenHolder.currentContentRootNode()

        // Create a text component with a brush background
        val textNode =
            ComposeNode(
                trait =
                    mutableStateOf(
                        TextTrait(
                            text = StringProperty.StringIntrinsicValue("Text with Brush Background"),
                        ),
                    ),
                modifierList =
                    mutableStateListEqualsOverrideOf(
                        ModifierWrapper.Background(
                            brushWrapper =
                                BrushProperty.BrushIntrinsicValue(
                                    BrushWrapper(
                                        brushType = BrushType.LinearGradient,
                                        colors =
                                            listOf(
                                                ColorWrapper(themeColor = Material3ColorWrapper.Primary),
                                                ColorWrapper(themeColor = Material3ColorWrapper.Secondary),
                                            ),
                                        startX = 0f,
                                        startY = 0f,
                                        endX = Float.POSITIVE_INFINITY,
                                        endY = Float.POSITIVE_INFINITY,
                                    ),
                                ),
                        ),
                    ),
            )
        rootNode.addChild(textNode)

        // Create a column with horizontal gradient background
        val columnNode =
            ComposeNode(
                trait = mutableStateOf(ColumnTrait()),
                modifierList =
                    mutableStateListEqualsOverrideOf(
                        ModifierWrapper.Background(
                            brushWrapper =
                                BrushProperty.BrushIntrinsicValue(
                                    BrushWrapper(
                                        brushType = BrushType.HorizontalGradient,
                                        colors =
                                            listOf(
                                                ColorWrapper(themeColor = Material3ColorWrapper.Tertiary),
                                                ColorWrapper(themeColor = Material3ColorWrapper.Surface),
                                            ),
                                    ),
                                ),
                        ),
                    ),
            )
        columnNode.addChild(
            ComposeNode(
                trait =
                    mutableStateOf(
                        TextTrait(
                            text = StringProperty.StringIntrinsicValue("Inside Column"),
                        ),
                    ),
            ),
        )
        rootNode.addChild(columnNode)

        // Create a box with radial gradient background
        val boxNode =
            ComposeNode(
                trait = mutableStateOf(BoxTrait()),
                modifierList =
                    mutableStateListEqualsOverrideOf(
                        ModifierWrapper.Background(
                            brushWrapper =
                                BrushProperty.BrushIntrinsicValue(
                                    BrushWrapper(
                                        brushType = BrushType.RadialGradient,
                                        colors =
                                            listOf(
                                                ColorWrapper(themeColor = Material3ColorWrapper.Error),
                                                ColorWrapper(themeColor = Material3ColorWrapper.ErrorContainer),
                                            ),
                                        centerX = 0.5f,
                                        centerY = 0.5f,
                                        radius = 100f,
                                    ),
                                ),
                        ),
                    ),
            )
        boxNode.addChild(
            ComposeNode(
                trait =
                    mutableStateOf(
                        TextTrait(
                            text = StringProperty.StringIntrinsicValue("Inside Box"),
                        ),
                    ),
            ),
        )
        rootNode.addChild(boxNode)

        toolbarViewModel.onRunPreviewApp(
            project = project,
            onStatusBarUiStateChanged = { _ -> },
        )
        assertBuildSucceed()
    }

    @Test
    fun buttonInTabContent() {
        val project = Project()
        val rootNode = project.screenHolder.currentContentRootNode()
        rootNode.addChild(TabsTrait.defaultComposeNode(project))
        rootNode.children.firstOrNull { it.trait.value == TabContentTrait }?.addChild(
            ButtonTrait().defaultComposeNode(project),
        )

        toolbarViewModel.onRunPreviewApp(
            project = project,
            onStatusBarUiStateChanged = { _ -> },
        )
        assertBuildSucceed()
    }

    @Test
    fun textFieldWithValidator() {
        val project = Project()
        val rootNode = project.screenHolder.currentContentRootNode()
        val textField1 =
            ComposeNode(
                trait =
                    mutableStateOf(
                        TextFieldTrait(
                            enableValidator = true,
                            textFieldValidator =
                                TextFieldValidator.StringValidator(
                                    maxLength = 10,
                                    minLength = 0,
                                ),
                        ),
                    ),
            )
        val screen = project.screenHolder.currentEditable()
        TextFieldTrait().onAttachStateToNode(project, screen, textField1)
        rootNode.addChild(textField1)

        toolbarViewModel.onRunPreviewApp(
            project = project,
            onStatusBarUiStateChanged = { _ -> },
        )
        assertBuildSucceed()
    }

    @Test
    fun testLayoutModifiers() {
        // Test padding, border, background, zIndex
        val project1 = Project()
        val rootNode1 = project1.screenHolder.currentContentRootNode()
        rootNode1.addChild(
            ComposeNode(
                trait =
                    mutableStateOf(
                        TextTrait(
                            text = StringProperty.StringIntrinsicValue("default value"),
                            colorWrapper = ColorProperty.ColorIntrinsicValue(defaultColorWrapper),
                        ),
                    ),
                modifierList =
                    mutableStateListEqualsOverrideOf(
                        ModifierWrapper.Padding(),
                        ModifierWrapper.Border(
                            width = 1.dp,
                            colorWrapper =
                                ColorProperty.ColorIntrinsicValue(
                                    ColorWrapper(themeColor = Material3ColorWrapper.Secondary),
                                ),
                        ),
                        ModifierWrapper.Background(),
                        ModifierWrapper.ZIndex(),
                    ),
            ),
        )
        toolbarViewModel.onRunPreviewApp(
            project = project1,
            onStatusBarUiStateChanged = { _ -> },
        )
        assertBuildSucceed()

        // Test Row scope modifiers
        val project2 = Project()
        val rootNode2 = project2.screenHolder.currentContentRootNode()
        rootNode2.addChild(
            ComposeNode(
                trait = mutableStateOf(RowTrait()),
                modifierList =
                    mutableStateListEqualsOverrideOf(
                        ModifierWrapper.Size(180.dp),
                    ),
            ).apply {
                addChild(
                    ComposeNode(
                        trait = mutableStateOf(TextTrait()),
                        modifierList =
                            mutableStateListEqualsOverrideOf(
                                ModifierWrapper.Weight(),
                                ModifierWrapper.AlignVertical(),
                            ),
                    ),
                )
            },
        )

        val rowNode2 = RowTrait().defaultComposeNode(project2)
        rowNode2.visibilityParams.value =
            VisibilityParams(
                formFactorVisibility =
                    FormFactorVisibility(
                        visibleInCompact = false,
                    ),
            )
        rootNode2.addChild(rowNode2)

        toolbarViewModel.onRunPreviewApp(
            project = project2,
            onStatusBarUiStateChanged = { _ -> },
        )
        assertBuildSucceed()

        // Test Column scope modifiers
        val project3 = Project()
        val rootNode3 = project3.screenHolder.currentContentRootNode()
        rootNode3.addChild(
            ComposeNode(
                trait = mutableStateOf(ColumnTrait()),
                modifierList =
                    mutableStateListEqualsOverrideOf(
                        ModifierWrapper.Size(180.dp),
                    ),
            ).apply {
                addChild(
                    ComposeNode(
                        trait = mutableStateOf(TextTrait()),
                        modifierList =
                            mutableStateListEqualsOverrideOf(
                                ModifierWrapper.Weight(),
                                ModifierWrapper.AlignHorizontal(),
                            ),
                    ),
                )
            },
        )
        toolbarViewModel.onRunPreviewApp(
            project = project3,
            onStatusBarUiStateChanged = { _ -> },
        )
        assertBuildSucceed()

        // Test Box scope modifiers
        val project4 = Project()
        val rootNode4 = project4.screenHolder.currentContentRootNode()
        rootNode4.addChild(
            ComposeNode(
                trait = mutableStateOf(BoxTrait()),
                modifierList =
                    mutableStateListEqualsOverrideOf(
                        ModifierWrapper.Size(180.dp),
                    ),
            ).apply {
                addChild(
                    ComposeNode(
                        trait = mutableStateOf(TextTrait()),
                        modifierList =
                            mutableStateListEqualsOverrideOf(
                                ModifierWrapper.Align(align = AlignmentWrapper.BottomCenter),
                            ),
                    ),
                )
            },
        )
        toolbarViewModel.onRunPreviewApp(
            project = project4,
            onStatusBarUiStateChanged = { _ -> },
        )
        assertBuildSucceed()
    }

    @Test
    fun modifiers_wrap() {
        val project = Project()
        val rootNode = project.screenHolder.currentContentRootNode()
        rootNode.addChild(
            ComposeNode(
                trait = mutableStateOf(RowTrait()),
                modifierList =
                    mutableStateListEqualsOverrideOf(
                        // In a real world, wrapContentSize is enough to wrap the size. To verify the
                        // generated code builds, applying other types of modifiers, too
                        ModifierWrapper.WrapContentSize(),
                        ModifierWrapper.WrapContentHeight(),
                        ModifierWrapper.WrapContentWidth(),
                    ),
            ).apply {
                addChild(
                    ComposeNode(
                        trait = mutableStateOf(TextTrait()),
                        modifierList =
                            mutableStateListEqualsOverrideOf(
                                ModifierWrapper.Size(180.dp),
                            ),
                    ),
                )
            },
        )
        toolbarViewModel.onRunPreviewApp(
            project = project,
            onStatusBarUiStateChanged = { _ -> },
        )
        assertBuildSucceed()
    }

    @Test
    fun testLazyLists() {
        // Test simple lazy column with icon
        val project1 = Project()
        val rootNode1 = project1.screenHolder.currentContentRootNode()
        val lazyColumn1 = LazyColumnTrait().defaultComposeNode(project1)
        lazyColumn1.addChild(
            ComposeNode(
                trait = mutableStateOf(IconTrait()),
            ),
        )
        lazyColumn1.addChild(
            ComposeNode(
                trait = mutableStateOf(IconButtonTrait()),
            ),
        )
        rootNode1.addChild(lazyColumn1)
        toolbarViewModel.onRunPreviewApp(
            project = project1,
            onStatusBarUiStateChanged = { _ -> },
        )
        assertBuildSucceed()

        // Test multiple lazy list types with various items
        val project2 = Project()
        val rootNode2 = project2.screenHolder.currentContentRootNode()
        val lazyColumn2 = LazyColumnTrait().defaultComposeNode(project2)
        lazyColumn2.addChild(
            ComposeNode(
                trait = mutableStateOf(IconTrait()),
            ),
        )
        lazyColumn2.addChild(
            ComposeNode(
                trait = mutableStateOf(TextTrait()),
            ),
        )
        lazyColumn2.addChild(
            CardTrait().defaultComposeNode(project2),
        )
        val lazyRow = LazyRowTrait().defaultComposeNode(project2)
        lazyRow.addChild(
            ComposeNode(
                trait = mutableStateOf(IconTrait()),
            ),
        )
        lazyRow.addChild(
            ComposeNode(
                trait = mutableStateOf(TextTrait()),
            ),
        )
        lazyRow.addChild(
            CardTrait().defaultComposeNode(project2),
        )
        val lazyVerticalGrid = LazyVerticalGridTrait().defaultComposeNode(project2)
        lazyVerticalGrid.addChild(
            ComposeNode(
                trait = mutableStateOf(IconTrait()),
            ),
        )
        lazyVerticalGrid.addChild(
            ComposeNode(
                trait = mutableStateOf(TextTrait()),
            ),
        )
        val lazyHorizontalGrid = LazyHorizontalGridTrait().defaultComposeNode(project2)
        lazyHorizontalGrid.addChild(
            ComposeNode(
                trait = mutableStateOf(IconTrait()),
            ),
        )
        lazyHorizontalGrid.addChild(
            ComposeNode(
                trait = mutableStateOf(TextTrait()),
            ),
        )

        val horizontalPager =
            HorizontalPagerTrait(
                showIndicator = true,
            ).defaultComposeNode(project2)
        horizontalPager.addChild(
            ComposeNode(
                trait = mutableStateOf(IconTrait()),
            ),
        )
        horizontalPager.addChild(
            ComposeNode(
                trait = mutableStateOf(TextTrait()),
            ),
        )

        rootNode2.addChild(lazyColumn2)
        rootNode2.addChild(lazyRow)
        rootNode2.addChild(lazyVerticalGrid)
        rootNode2.addChild(lazyHorizontalGrid)
        rootNode2.addChild(horizontalPager)
        toolbarViewModel.onRunPreviewApp(
            project = project2,
            onStatusBarUiStateChanged = { _ -> },
        )
        assertBuildSucceed()
    }

    @Test
    fun multiple_textFields() {
        val project = Project()
        val rootNode = project.screenHolder.currentContentRootNode()
        val textField1 =
            ComposeNode(
                trait = mutableStateOf(TextFieldTrait()),
            )
        val screen = project.screenHolder.currentEditable()
        val textFieldParams = textField1.trait.value as TextFieldTrait
        TextFieldTrait().onAttachStateToNode(project, screen, textField1)

        val appStringState = AppState.StringAppState(name = "stringState including space")
        project.addState(appStringState)

        textField1.trait.value =
            textFieldParams.copy(
                placeholder = StringProperty.StringIntrinsicValue("placeholder text"),
                label = StringProperty.StringIntrinsicValue("label text"),
                enableValidator = true,
                textFieldValidator = TextFieldValidator.IntValidator(),
            )
        val button = ButtonTrait().defaultComposeNode(project)
        button.actionsMap[ActionType.OnClick] =
            mutableStateListOf(
                ActionNode.Simple(
                    action =
                        StateAction.SetAppStateValue(
                            setValueToStates =
                                mutableListOf(
                                    SetValueToState(
                                        writeToStateId = appStringState.id,
                                        operation =
                                            StateOperation.SetValue(
                                                readProperty = ValueFromState(readFromStateId = textField1.companionStateId),
                                            ),
                                    ),
                                ),
                        ),
                ),
            )
        rootNode.addChild(button)
        textField1.actionsMap[ActionType.OnSubmit] =
            mutableStateListOf(
                ActionNode.Simple(
                    action = Navigation.NavigateBack,
                ),
            )
        textField1.actionsMap[ActionType.OnChange] =
            mutableStateListOf(
                ActionNode.Simple(
                    action = Navigation.NavigateBack,
                ),
            )
        textField1.actionsMap[ActionType.OnFocused] =
            mutableStateListOf(
                ActionNode.Simple(
                    action = Navigation.NavigateBack,
                ),
            )
        textField1.actionsMap[ActionType.OnUnfocused] =
            mutableStateListOf(
                ActionNode.Simple(
                    action = Navigation.NavigateBack,
                ),
            )
        val textField2 =
            ComposeNode(
                trait = mutableStateOf(TextFieldTrait()),
            )
        TextFieldTrait().onAttachStateToNode(project, screen, textField2)
        rootNode.addChild(textField1)
        rootNode.addChild(textField2)
        toolbarViewModel.onRunPreviewApp(
            project = project,
            onStatusBarUiStateChanged = { _ -> },
        )
        assertBuildSucceed()
    }

    @Test
    fun multiple_input_composables() {
        val project = Project()
        val rootNode = project.screenHolder.currentContentRootNode()
        val currentEditable = project.screenHolder.currentEditable()
        val chipGroup1 = ChipGroupTrait().defaultComposeNode(project)
        ChipGroupTrait().onAttachStateToNode(project, currentEditable, chipGroup1)
        rootNode.addChild(chipGroup1)

        val chipGroup2 = ChipGroupTrait().defaultComposeNode(project)
        ChipGroupTrait().onAttachStateToNode(project, currentEditable, chipGroup2)
        rootNode.addChild(chipGroup2)

        val chipGroupWithoutState = ChipGroupTrait().defaultComposeNode(project)
        rootNode.addChild(chipGroupWithoutState)

        rootNode.addChild(
            SwitchTrait().defaultComposeNode(project),
        )
        rootNode.addChild(
            SwitchTrait().defaultComposeNode(project),
        )
        rootNode.addChild(
            CheckboxTrait().defaultComposeNode(project),
        )

        val customEnum =
            CustomEnum(name = "CustomEnum").apply {
                values.addAll(listOf("Item_1", "Item_2", "Item_3"))
            }
        rootNode.addChild(
            DropdownTrait().defaultComposeNode(project).apply {
                trait.value =
                    (trait.value as DropdownTrait).copy(
                        items = CustomEnumValuesProperty(customEnumId = customEnum.customEnumId),
                    )
            },
        )
        project.customEnumHolder.enumList.add(customEnum)

        toolbarViewModel.onRunPreviewApp(
            project = project,
            onStatusBarUiStateChanged = { _ -> },
        )
        assertBuildSucceed()
    }

    @Test
    fun composables_referring_stringResources() {
        val project = Project()
        val rootNode = project.screenHolder.currentContentRootNode()

        // Add string resources to the project
        project.stringResourceHolder.stringResources.add(
            stringResourceOf(
                key = "welcome_message",
                "en-US" to "Welcome to ComposeFlow!",
                description = "Welcome message for the app",
            ),
        )
        project.stringResourceHolder.stringResources.add(
            stringResourceOf(
                key = "button_label",
                "en-US" to "Click Me",
                description = "Button label text",
            ),
        )

        // Create a text node that uses a string resource
        val textNode =
            ComposeNode(
                trait =
                    mutableStateOf(
                        TextTrait(
                            text =
                                StringProperty.ValueFromStringResource(
                                    stringResourceId = project.stringResourceHolder.stringResources[0].id,
                                ),
                        ),
                    ),
            )
        rootNode.addChild(textNode)

        // Create a button that uses a string resource for its text
        val buttonNode = ButtonTrait().defaultComposeNode(project)
        val buttonTrait = buttonNode.trait.value as ButtonTrait
        buttonNode.trait.value =
            buttonTrait.copy(
                textProperty =
                    StringProperty.ValueFromStringResource(
                        stringResourceId = project.stringResourceHolder.stringResources[1].id,
                    ),
            )
        rootNode.addChild(buttonNode)

        toolbarViewModel.onRunPreviewApp(
            project = project,
            onStatusBarUiStateChanged = { _ -> },
        )
        assertBuildSucceed()
    }

    @Test
    fun screens_including_nonTopLevel_verify_noCrash() {
        val project = Project()
        val rootNode = project.screenHolder.currentContentRootNode()
        rootNode.addChild(
            ImageTrait().defaultComposeNode(project),
        )
        val screenName = "nonTopLevelScreen_hasTopAppBar"
        val screen = Screen(name = screenName)
        screen.showOnNavigation.value = false
        screen.topAppBarNode.value = TopAppBarTrait().defaultComposeNode(project)
        project.screenHolder.addScreen(name = screenName, screen)

        val screenName2 = "nonTopLevelScreen_hasTopAppBar"
        val screen2 = Screen(name = screenName2)
        screen2.showOnNavigation.value = false
        screen2.topAppBarNode.value = TopAppBarTrait().defaultComposeNode(project)
        project.screenHolder.addScreen(name = screenName2, screen2)

        toolbarViewModel.onRunPreviewApp(
            project = project,
            onStatusBarUiStateChanged = { _ -> },
        )
        assertBuildSucceed()
    }

    @Test
    fun topAppBar_including_navIcon_and_actionIcons() {
        val project = Project()
        val rootNode = project.screenHolder.currentContentRootNode()
        rootNode.addChild(
            ImageTrait().defaultComposeNode(project),
        )
        val screenName = "blankScreen"
        val screen = Screen(name = screenName)
        screen.showOnNavigation.value = false
        val topAppBarNode = TopAppBarTrait().defaultComposeNode(project)
        topAppBarNode.getTopAppBarNavigationIcon()?.trait?.value =
            topAppBarNode.getTopAppBarNavigationIcon()?.trait?.value as IconTrait
        topAppBarNode.addTopAppBarActionIcon()
        topAppBarNode.addTopAppBarActionIcon()
        screen.topAppBarNode.value = topAppBarNode

        toolbarViewModel.onRunPreviewApp(
            project = project,
            onStatusBarUiStateChanged = { _ -> },
        )
        assertBuildSucceed()
    }

    @Test
    fun navigateToAction_verifyNoCrash() {
        val project = Project()
        val rootNode = project.screenHolder.currentContentRootNode()
        rootNode.addChild(
            ImageTrait().defaultComposeNode(project),
        )
        val screenName = "blankScreen"
        val screen = Screen(name = screenName)
        screen.showOnNavigation.value = false
        screen.topAppBarNode.value = TopAppBarTrait().defaultComposeNode(project)
        val stringParameter = ParameterWrapper.StringParameter(name = "stringParam")
        val booleanParameter = ParameterWrapper.BooleanParameter(name = "booleanParam")
        val intParameter = ParameterWrapper.IntParameter(name = "intParam")
        val floatParameter = ParameterWrapper.FloatParameter(name = "floatParam")
        screen.parameters.add(stringParameter)
        screen.parameters.add(booleanParameter)
        screen.parameters.add(intParameter)
        screen.parameters.add(floatParameter)

        project.screenHolder.addScreen(name = screenName, screen)

        val button =
            ComposeNode(
                trait = mutableStateOf(ButtonTrait()),
            )
        val params: MutableMap<ParameterId, AssignableProperty> = mutableMapOf()
        params[stringParameter.id] = StringProperty.StringIntrinsicValue("argument")
        params[booleanParameter.id] = BooleanProperty.BooleanIntrinsicValue(true)
        params[stringParameter.id] = IntProperty.IntIntrinsicValue(3)
        params[stringParameter.id] = FloatProperty.FloatIntrinsicValue(2.0f)

        button.actionsMap[ActionType.OnClick] =
            mutableStateListOf(
                ActionNode.Simple(
                    action =
                        Navigation.NavigateTo(
                            screenId = screen.id,
                            paramsMap = params,
                        ),
                ),
            )
        button.actionsMap[ActionType.OnDoubleClick] =
            mutableStateListOf(
                ActionNode.Simple(action = Navigation.NavigateTo(screenId = screen.id)),
            )
        button.actionsMap[ActionType.OnLongClick] =
            mutableStateListOf(
                ActionNode.Simple(action = Navigation.NavigateTo(screenId = screen.id)),
                ActionNode.Conditional(
                    ifCondition = BooleanProperty.BooleanIntrinsicValue(true),
                    trueNodes =
                        mutableListOf(
                            ActionNode.Simple(action = Navigation.NavigateBack),
                        ),
                    falseNodes =
                        mutableListOf(
                            ActionNode.Simple(action = Navigation.NavigateBack),
                            ActionNode.Conditional(
                                ifCondition = BooleanProperty.BooleanIntrinsicValue(false),
                                trueNodes =
                                    mutableListOf(
                                        ActionNode.Simple(action = Navigation.NavigateBack),
                                    ),
                            ),
                        ),
                ),
            )
        rootNode.addChild(button)

        toolbarViewModel.onRunPreviewApp(
            project = project,
            onStatusBarUiStateChanged = { _ -> },
        )
        assertBuildSucceed()
    }

    @Test
    fun project_including_setStateAction_verifyNoCrash() {
        val project = Project()
        val rootNode = project.screenHolder.currentContentRootNode()
        val appStringState = AppState.StringAppState(name = "stringState")
        val appStringState2 = AppState.StringAppState(name = "stringState2")
        val appIntState = AppState.IntAppState(name = "intState")
        val appIntState2 = AppState.IntAppState(name = "intState2")
        val appInstantState = AppState.InstantAppState(name = "instantState")
        val appInstantState2 = AppState.InstantAppState(name = "instantState2")
        val appFloatState = AppState.FloatAppState(name = "floatState")
        val appFloatState2 = AppState.FloatAppState(name = "floatState2")
        val switchScreenState = ScreenState.BooleanScreenState(name = "switchScreenState")
        val sliderScreenState = ScreenState.FloatScreenState(name = "sliderScreenState")
        val stringResource =
            stringResourceOf(
                key = "string_key",
                "en-US" to "String resource value",
            )

        val slider = SliderTrait().defaultComposeNode(project)
        rootNode.addChild(slider)
        project.screenHolder.currentEditable().addState(switchScreenState)
        project.screenHolder.currentEditable().addState(sliderScreenState)
        project.globalStateHolder.addState(appStringState)
        project.globalStateHolder.addState(appStringState2)
        project.globalStateHolder.addState(appIntState)
        project.globalStateHolder.addState(appIntState2)
        project.globalStateHolder.addState(appInstantState)
        project.globalStateHolder.addState(appInstantState2)
        project.globalStateHolder.addState(appFloatState)
        project.globalStateHolder.addState(appFloatState2)
        project.stringResourceHolder.stringResources.add(stringResource)
        rootNode.actionsMap[ActionType.OnClick] =
            mutableStateListOf(
                ActionNode.Simple(
                    action =
                        StateAction.SetAppStateValue(
                            setValueToStates =
                                mutableListOf(
                                    SetValueToState(
                                        writeToStateId = appStringState.id,
                                        operation =
                                            StateOperation.SetValue(
                                                readProperty =
                                                    ValueFromState(
                                                        appStringState2.id,
                                                    ),
                                            ),
                                    ),
                                    SetValueToState(
                                        writeToStateId = appStringState.id,
                                        operation = StateOperation.ClearValue,
                                    ),
                                    SetValueToState(
                                        writeToStateId = appStringState2.id,
                                        StateOperation.SetValue(
                                            readProperty =
                                                StringProperty.ValueFromStringResource(
                                                    stringResourceId = stringResource.id,
                                                ),
                                        ),
                                    ),
                                    SetValueToState(
                                        writeToStateId = appIntState.id,
                                        operation =
                                            StateOperation.SetValue(
                                                readProperty =
                                                    ValueFromState(
                                                        appIntState2.id,
                                                    ),
                                            ),
                                    ),
                                    SetValueToState(
                                        writeToStateId = appInstantState.id,
                                        operation =
                                            StateOperation.SetValue(
                                                readProperty =
                                                    ValueFromState(
                                                        appInstantState2.id,
                                                    ),
                                            ),
                                    ),
                                    SetValueToState(
                                        writeToStateId = appInstantState.id,
                                        operation = StateOperation.ClearValue,
                                    ),
                                    SetValueToState(
                                        writeToStateId = appFloatState.id,
                                        operation =
                                            StateOperation.SetValue(
                                                readProperty =
                                                    ValueFromState(
                                                        appFloatState2.id,
                                                    ),
                                            ),
                                    ),
                                    SetValueToState(
                                        writeToStateId = appFloatState.id,
                                        operation = StateOperation.ClearValue,
                                    ),
                                    SetValueToState(
                                        writeToStateId = appStringState.id,
                                        operation =
                                            StateOperation.SetValue(
                                                readProperty =
                                                    ConditionalProperty(
                                                        defaultValue =
                                                            StringProperty.StringIntrinsicValue(
                                                                "defaultValue",
                                                            ),
                                                        ifThen =
                                                            ConditionalProperty.IfThenBlock(
                                                                ifExpression =
                                                                    ValueFromState(
                                                                        readFromStateId = switchScreenState.id,
                                                                    ),
                                                                thenValue =
                                                                    StringProperty.StringIntrinsicValue(
                                                                        "abc",
                                                                    ),
                                                            ),
                                                        elseIfBlocks =
                                                            mutableListOf(
                                                                ConditionalProperty.IfThenBlock(
                                                                    ifExpression =
                                                                        BooleanProperty.BooleanIntrinsicValue(
                                                                            true,
                                                                        ),
                                                                    thenValue =
                                                                        StringProperty.StringIntrinsicValue(
                                                                            "def",
                                                                        ),
                                                                ),
                                                                ConditionalProperty.IfThenBlock(
                                                                    ifExpression =
                                                                        BooleanProperty.BooleanIntrinsicValue(
                                                                            false,
                                                                        ),
                                                                    thenValue =
                                                                        StringProperty.StringIntrinsicValue(
                                                                            "hgi",
                                                                        ),
                                                                ),
                                                            ),
                                                        elseBlock =
                                                            ConditionalProperty.ElseBlock(
                                                                value =
                                                                    StringProperty.StringIntrinsicValue(
                                                                        "hgi",
                                                                    ),
                                                            ),
                                                    ),
                                            ),
                                    ),
                                ),
                        ),
                ),
            )

        toolbarViewModel.onRunPreviewApp(
            project = project,
            onStatusBarUiStateChanged = { _ -> },
        )
        assertBuildSucceed()
    }

    @Test
    fun project_including_setStateAction_with_customDataType_verifyNoCrash() {
        val project = Project()
        val rootNode = project.screenHolder.currentContentRootNode()
        val todoNameField =
            DataField(
                name = "todoName",
                fieldType = FieldType.String(),
            )
        val doneField =
            DataField(
                name = "done",
                fieldType = FieldType.Boolean(),
            )
        val todoItemDataType =
            DataType(
                name = "TodoItem",
            ).apply {
                fields.add(todoNameField)
                fields.add(doneField)
            }
        project.dataTypeHolder.dataTypes.add(todoItemDataType)

        val todoItemAppState =
            AppState.CustomDataTypeAppState(
                name = "todoItemState",
                dataTypeId = todoItemDataType.id,
            )
        val todoItemListAppState =
            AppState.CustomDataTypeListAppState(
                name = "todoItemListState",
                dataTypeId = todoItemDataType.id,
                defaultValue =
                    listOf(
                        DataTypeDefaultValue(
                            dataTypeId = todoItemDataType.id,
                            defaultFields =
                                mutableListOf(
                                    FieldDefaultValue(
                                        fieldId = todoNameField.id,
                                        defaultValue = StringProperty.StringIntrinsicValue("defaultTodo"),
                                    ),
                                    FieldDefaultValue(
                                        fieldId = doneField.id,
                                        defaultValue = BooleanProperty.BooleanIntrinsicValue(false),
                                    ),
                                ),
                        ),
                    ),
            )
        project.globalStateHolder.addState(todoItemAppState)
        project.globalStateHolder.addState(todoItemListAppState)
        rootNode.actionsMap[ActionType.OnClick] =
            mutableStateListOf(
                ActionNode.Simple(
                    action =
                        StateAction.SetAppStateValue(
                            setValueToStates =
                                mutableListOf(
                                    SetValueToState(
                                        writeToStateId = todoItemAppState.id,
                                        operation =
                                            StateOperationForDataType.DataTypeSetValue().apply {
                                                dataFieldUpdateProperties.add(
                                                    DataFieldUpdateProperty(
                                                        dataFieldId = todoItemDataType.fields[0].id,
                                                        assignableProperty =
                                                            StringProperty.StringIntrinsicValue(
                                                                "testItem",
                                                            ),
                                                    ),
                                                )
                                                dataFieldUpdateProperties.add(
                                                    DataFieldUpdateProperty(
                                                        dataFieldId = todoItemDataType.fields[1].id,
                                                        assignableProperty = BooleanProperty.BooleanIntrinsicValue(),
                                                    ),
                                                )
                                            },
                                    ),
                                    SetValueToState(
                                        writeToStateId = todoItemListAppState.id,
                                        operation =
                                            StateOperationForList
                                                .AddValueForCustomDataType()
                                                .apply {
                                                    dataFieldUpdateProperties.add(
                                                        DataFieldUpdateProperty(
                                                            dataFieldId = todoItemDataType.fields[0].id,
                                                            assignableProperty =
                                                                StringProperty.StringIntrinsicValue(
                                                                    "testItem",
                                                                ),
                                                        ),
                                                    )
                                                    dataFieldUpdateProperties.add(
                                                        DataFieldUpdateProperty(
                                                            dataFieldId = todoItemDataType.fields[1].id,
                                                            assignableProperty = BooleanProperty.BooleanIntrinsicValue(),
                                                        ),
                                                    )
                                                },
                                    ),
                                    SetValueToState(
                                        writeToStateId = todoItemListAppState.id,
                                        operation =
                                            StateOperationForList
                                                .UpdateValueAtIndexForCustomDataType()
                                                .apply {
                                                    dataFieldUpdateProperties.add(
                                                        DataFieldUpdateProperty(
                                                            dataFieldId = todoItemDataType.fields[0].id,
                                                            assignableProperty =
                                                                StringProperty.StringIntrinsicValue(
                                                                    "updatedValue",
                                                                ),
                                                        ),
                                                    )
                                                    dataFieldUpdateProperties.add(
                                                        DataFieldUpdateProperty(
                                                            dataFieldId = todoItemDataType.fields[1].id,
                                                            assignableProperty =
                                                                BooleanProperty.BooleanIntrinsicValue(
                                                                    true,
                                                                ),
                                                            fieldUpdateType = FieldUpdateType.ToggleValue,
                                                        ),
                                                    )
                                                },
                                    ),
                                    SetValueToState(
                                        writeToStateId = todoItemListAppState.id,
                                        operation =
                                            StateOperationForList
                                                .UpdateValueAtIndexForCustomDataType()
                                                .apply {
                                                    dataFieldUpdateProperties.add(
                                                        DataFieldUpdateProperty(
                                                            dataFieldId = todoItemDataType.fields[0].id,
                                                            assignableProperty =
                                                                StringProperty.StringIntrinsicValue(
                                                                    "newValue",
                                                                ),
                                                        ),
                                                    )
                                                    dataFieldUpdateProperties.add(
                                                        DataFieldUpdateProperty(
                                                            dataFieldId = todoItemDataType.fields[1].id,
                                                            assignableProperty =
                                                                BooleanProperty.BooleanIntrinsicValue(
                                                                    true,
                                                                ),
                                                            fieldUpdateType = FieldUpdateType.ClearValue,
                                                        ),
                                                    )
                                                },
                                    ),
                                ),
                        ),
                ),
            )

        toolbarViewModel.onRunPreviewApp(
            project = project,
            onStatusBarUiStateChanged = { _ -> },
        )
        assertBuildSucceed()
    }

    @Test
    fun project_including_lazyColumn_from_appState() {
        val project = Project()
        val rootNode = project.screenHolder.currentContentRootNode()
        val appStringListState = AppState.StringListAppState(name = "stringListState")
        val appIntListState = AppState.IntListAppState(name = "intListState")
        val appBooleanListState = AppState.BooleanListAppState(name = "booleanListState")
        project.globalStateHolder.addState(appStringListState)
        project.globalStateHolder.addState(appIntListState)
        project.globalStateHolder.addState(appBooleanListState)

        val lazyColumn = LazyColumnTrait().defaultComposeNode(project)
        val dynamicItems: AssignableProperty =
            ValueFromState(readFromStateId = appStringListState.id)

        lazyColumn.dynamicItems.value = dynamicItems
        lazyColumn.addChild(
            ComposeNode().apply {
                lazyListChildParams.value =
                    LazyListChildParams.DynamicItemsSource(composeNodeId = lazyColumn.id)
                trait.value =
                    TextTrait(
                        text =
                            ValueFromDynamicItem(
                                composeNodeId = lazyColumn.id,
                                fieldType = DataFieldType.Primitive,
                            ),
                    )
            },
        )

        val lazyColumnInt = LazyColumnTrait().defaultComposeNode(project)
        val dynamicItemsInt = ValueFromState(readFromStateId = appIntListState.id)
        lazyColumnInt.dynamicItems.value = dynamicItemsInt
        lazyColumnInt.addChild(
            ComposeNode(
                trait = mutableStateOf(TextTrait()),
            ).apply {
                lazyListChildParams.value =
                    LazyListChildParams.DynamicItemsSource(composeNodeId = lazyColumnInt.id)
                trait.value =
                    TextTrait(
                        text =
                            ValueFromDynamicItem(
                                composeNodeId = lazyColumnInt.id,
                                fieldType = DataFieldType.Primitive,
                            ),
                    )
            },
        )

        val lazyColumnBoolean = LazyColumnTrait().defaultComposeNode(project)
        val dynamicItemsBoolean = ValueFromState(readFromStateId = appBooleanListState.id)
        lazyColumnBoolean.dynamicItems.value = dynamicItemsBoolean
        lazyColumnBoolean.addChild(
            ComposeNode(
                trait = mutableStateOf(TextTrait()),
            ).apply {
                lazyListChildParams.value =
                    LazyListChildParams.DynamicItemsSource(composeNodeId = lazyColumnBoolean.id)
                trait.value =
                    TextTrait(
                        text =
                            ValueFromDynamicItem(
                                composeNodeId = lazyColumnBoolean.id,
                                fieldType = DataFieldType.Primitive,
                            ),
                    )
            },
        )

        val horizontalPagerTrait = HorizontalPagerTrait().defaultComposeNode(project)
        horizontalPagerTrait.dynamicItems.value = dynamicItems
        horizontalPagerTrait.addChild(
            ComposeNode(
                trait = mutableStateOf(TextTrait()),
            ).apply {
                lazyListChildParams.value =
                    LazyListChildParams.DynamicItemsSource(composeNodeId = horizontalPagerTrait.id)
                trait.value =
                    TextTrait(
                        text =
                            ValueFromDynamicItem(
                                composeNodeId = horizontalPagerTrait.id,
                                fieldType = DataFieldType.Primitive,
                            ),
                    )
            },
        )

        rootNode.addChild(lazyColumn)
        rootNode.addChild(lazyColumnInt)
        rootNode.addChild(lazyColumnBoolean)
        rootNode.addChild(horizontalPagerTrait)

        toolbarViewModel.onRunPreviewApp(
            project = project,
            onStatusBarUiStateChanged = { _ -> },
        )
        assertBuildSucceed()
    }

    @Test
    fun project_actions_for_listAppState() {
        val project = Project()
        val rootNode = project.screenHolder.currentContentRootNode()
        val stringParameter = ParameterWrapper.StringParameter(name = "strParams")

        project.screenHolder
            .currentEditable()
            .parameters
            .add(stringParameter)

        val appStringListState = AppState.StringListAppState(name = "stringListState")
        project.globalStateHolder.addState(appStringListState)

        val lazyColumn = LazyColumnTrait().defaultComposeNode(project)
        val dynamicItems = ValueFromState(readFromStateId = appStringListState.id)
        lazyColumn.dynamicItems.value = dynamicItems
        val textTrait =
            ComposeNode(
                trait = mutableStateOf(TextTrait()),
                label = mutableStateOf("text1"),
            ).apply {
                trait.value =
                    TextTrait(
                        text =
                            ValueFromDynamicItem(
                                composeNodeId = lazyColumn.id,
                                fieldType = DataFieldType.Primitive,
                            ),
                    )
                lazyListChildParams.value =
                    LazyListChildParams.DynamicItemsSource(composeNodeId = lazyColumn.id)
            }
        lazyColumn.addChild(
            textTrait,
        )

        rootNode.addChild(
            TextFieldTrait().defaultComposeNode(project).apply {
                setLabel("textField1")
            },
        )
        val editable = project.screenHolder.currentEditable()
        val textFieldState = ScreenState.StringScreenState(name = "textField")
        editable.addState(textFieldState)

        val button = ButtonTrait().defaultComposeNode(project)
        button.lazyListChildParams.value = LazyListChildParams.FixedNumber(1)
        button.actionsMap[ActionType.OnClick] =
            mutableStateListOf(
                ActionNode.Simple(
                    action =
                        StateAction.SetAppStateValue(
                            setValueToStates =
                                mutableListOf(
                                    SetValueToState(
                                        writeToStateId = appStringListState.id,
                                        operation =
                                            StateOperationForList.AddValue(
                                                readProperty = ValueFromState(textFieldState.id),
                                            ),
                                    ),
                                    SetValueToState(
                                        writeToStateId = appStringListState.id,
                                        operation = StateOperation.ClearValue,
                                    ),
                                    SetValueToState(
                                        writeToStateId = appStringListState.id,
                                        operation = StateOperationForList.RemoveFirstValue,
                                    ),
                                    SetValueToState(
                                        writeToStateId = appStringListState.id,
                                        operation = StateOperationForList.RemoveLastValue,
                                    ),
                                    SetValueToState(
                                        writeToStateId = appStringListState.id,
                                        operation =
                                            StateOperationForList.RemoveValueAtIndex(
                                                indexProperty =
                                                    IntProperty.ValueFromLazyListIndex(
                                                        lazyListNodeId = lazyColumn.id,
                                                    ),
                                            ),
                                    ),
                                    SetValueToState(
                                        writeToStateId = appStringListState.id,
                                        operation =
                                            StateOperationForList.UpdateValueAtIndex(
                                                indexProperty =
                                                    IntProperty.ValueFromLazyListIndex(
                                                        lazyListNodeId = lazyColumn.id,
                                                    ),
                                                readProperty = StringProperty.StringIntrinsicValue("aaa"),
                                            ),
                                    ),
                                    SetValueToState(
                                        writeToStateId = appStringListState.id,
                                        operation =
                                            StateOperationForList.AddValue(
                                                readProperty =
                                                    ComposableParameterProperty(
                                                        parameterId = stringParameter.id,
                                                    ),
                                            ),
                                    ),
                                ),
                        ),
                ),
            )

        lazyColumn.addChild(button)
        rootNode.addChild(lazyColumn)

        toolbarViewModel.onRunPreviewApp(
            project = project,
            onStatusBarUiStateChanged = { _ -> },
        )
        assertBuildSucceed()
    }

    @Test
    fun project_conditional_property() {
        val project = Project()
        val rootNode = project.screenHolder.currentContentRootNode()

        val text = TextTrait().defaultComposeNode(project)
        rootNode.addChild(text)
        rootNode.addChild(
            SwitchTrait().defaultComposeNode(project).apply {
                setLabel("switch1")
            },
        )
        val switchState = ScreenState.BooleanScreenState(name = "switch")
        val editable = project.screenHolder.currentEditable()
        editable.addState(switchState)

        val textParams = text.trait.value as TextTrait
        text.trait.value =
            textParams.copy(
                textDecoration =
                    ConditionalProperty(
                        defaultValue = EnumProperty(TextDecorationWrapper.None),
                        ifThen =
                            ConditionalProperty.IfThenBlock(
                                ifExpression = ValueFromState(readFromStateId = switchState.id),
                                thenValue = EnumProperty(value = TextDecorationWrapper.LineThrough),
                            ),
                        elseIfBlocks =
                            mutableListOf(
                                ConditionalProperty.IfThenBlock(
                                    ifExpression = BooleanProperty.BooleanIntrinsicValue(true),
                                    thenValue = EnumProperty(value = TextDecorationWrapper.None),
                                ),
                                ConditionalProperty.IfThenBlock(
                                    ifExpression = BooleanProperty.BooleanIntrinsicValue(false),
                                    thenValue = EnumProperty(value = TextDecorationWrapper.LineThrough),
                                ),
                            ),
                        elseBlock =
                            ConditionalProperty.ElseBlock(
                                value = EnumProperty(value = TextDecorationWrapper.Underline),
                            ),
                    ),
            )

        toolbarViewModel.onRunPreviewApp(
            project = project,
            onStatusBarUiStateChanged = { _ -> },
        )
        assertBuildSucceed()
    }

    @Test
    fun project_including_component() {
        val project = Project()
        val rootNode = project.screenHolder.currentContentRootNode()

        val row = RowTrait().defaultComposeNode(project)
        val text = TextTrait().defaultComposeNode(project)
        row.addChild(text)
        row.addChild(
            SwitchTrait().defaultComposeNode(project).apply {
                setLabel("switch1")
            },
        )
        val switchState = ScreenState.BooleanScreenState(name = "switch")

        val component =
            Component(
                name = "component",
                componentRoot = mutableStateOf(row),
            )
        rootNode.addChild(
            rootNode.createComponentWrapperNode(component.id),
        )
        rootNode.addChild(
            rootNode.createComponentWrapperNode(component.id),
        )
        rootNode.addChild(
            rootNode.createComponentWrapperNode(component.id),
        )
        project.componentHolder.components.add(component)
        component.addState(switchState)

        toolbarViewModel.onRunPreviewApp(
            project = project,
            onStatusBarUiStateChanged = { _ -> },
        )
        assertBuildSucceed()
    }

    @Test
    fun project_including_property_transformations() {
        val project = Project()
        val rootNode = project.screenHolder.currentContentRootNode()

        val text = TextTrait().defaultComposeNode(project)
        rootNode.addChild(text)
        rootNode.addChild(
            SwitchTrait().defaultComposeNode(project).apply {
                setLabel("switch1")
            },
        )
        val button = ButtonTrait().defaultComposeNode(project)
        rootNode.addChild(button)

        val textField =
            TextFieldTrait().defaultComposeNode(project).apply {
                setLabel("textField1")
            }
        rootNode.addChild(textField)
        val switchState = ScreenState.BooleanScreenState(name = "switch")
        val textFieldState = ScreenState.StringScreenState(name = "textField")
        val instantState = ScreenState.InstantScreenState(name = "instantState")
        val editable = project.screenHolder.currentEditable()
        editable.addState(switchState)
        editable.addState(textFieldState)
        editable.addState(instantState)

        val textParams = text.trait.value as TextTrait
        text.trait.value =
            textParams.copy(
                text =
                    StringProperty
                        .StringIntrinsicValue("original")
                        .apply {
                            propertyTransformers.add(
                                FromString.ToString.AddBefore(
                                    mutableStateOf(
                                        StringProperty.StringIntrinsicValue("add before string "),
                                    ),
                                ),
                            )
                            propertyTransformers.add(
                                FromString.ToString.AddAfter(
                                    mutableStateOf(
                                        StringProperty.StringIntrinsicValue("add after string "),
                                    ),
                                ),
                            )
                        },
                textDecoration =
                    ConditionalProperty(
                        defaultValue = EnumProperty(TextDecorationWrapper.None),
                        ifThen =
                            ConditionalProperty.IfThenBlock(
                                ifExpression =
                                    ValueFromState(readFromStateId = textFieldState.id).apply {
                                        propertyTransformers.add(
                                            FromString.ToBoolean.StringContains(
                                                mutableStateOf(
                                                    StringProperty.StringIntrinsicValue("abc"),
                                                ),
                                            ),
                                        )
                                    },
                                thenValue = EnumProperty(value = TextDecorationWrapper.LineThrough),
                            ),
                        elseIfBlocks =
                            mutableListOf(
                                ConditionalProperty.IfThenBlock(
                                    ifExpression = BooleanProperty.BooleanIntrinsicValue(true),
                                    thenValue = EnumProperty(value = TextDecorationWrapper.None),
                                ),
                                ConditionalProperty.IfThenBlock(
                                    ifExpression = BooleanProperty.BooleanIntrinsicValue(false),
                                    thenValue = EnumProperty(value = TextDecorationWrapper.LineThrough),
                                ),
                            ),
                        elseBlock =
                            ConditionalProperty.ElseBlock(
                                value = EnumProperty(value = TextDecorationWrapper.Underline),
                            ),
                    ),
            )

        val buttonParams = button.trait.value as ButtonTrait
        button.trait.value =
            buttonParams.copy(
                textProperty =
                    InstantProperty.InstantIntrinsicValue().apply {
                        propertyTransformers.add(
                            FromInstant.ToInstant.PlusDay(
                                mutableStateOf(
                                    IntProperty.IntIntrinsicValue(3),
                                ),
                            ),
                        )
                        propertyTransformers.add(
                            FromInstant.ToInstant.PlusMonth(
                                mutableStateOf(
                                    IntProperty.IntIntrinsicValue(3),
                                ),
                            ),
                        )
                        propertyTransformers.add(
                            FromInstant.ToInstant.PlusYear(
                                mutableStateOf(
                                    IntProperty.IntIntrinsicValue(3),
                                ),
                            ),
                        )
                    },
            )

        toolbarViewModel.onRunPreviewApp(
            project = project,
            onStatusBarUiStateChanged = { _ -> },
        )
        assertBuildSucceed()
    }

    @Test
    fun project_call_api_action() {
        val project = Project()
        val rootNode = project.screenHolder.currentContentRootNode()

        val textField =
            TextFieldTrait().defaultComposeNode(project).apply {
                setLabel("textField1")
            }
        rootNode.addChild(textField)
        val editable = project.screenHolder.currentEditable()
        val textFieldState = ScreenState.StringScreenState(name = "textField")
        editable.addState(textFieldState)
        val appStringState = AppState.StringAppState(name = "stringState")
        project.addState(appStringState)

        val api =
            ApiDefinition(
                name = "tes Api", // include a space to check if the app compiles
                url = "https://example.com/api",
                parameters =
                    mutableListOf(
                        ApiProperty.StringParameter(name = "query", defaultValue = "default query"),
                    ),
                exampleJsonResponse =
                    JsonWithJsonPath(
                        ".",
                        jsonElement = Json.encodeToJsonElement("{}"),
                    ),
                authorization =
                    Authorization.BasicAuth(
                        username = "username",
                        password = "password",
                    ),
            )
        val postApi =
            ApiDefinition(
                name = "tes Api", // include a space to check if the app compiles
                url = "https://example.com/api",
                method = Method.Post,
                headers =
                    mutableListOf(
                        "header" to
                            ApiProperty.StringParameter(
                                name = "query",
                                defaultValue = "default query",
                            ),
                    ),
                exampleJsonResponse =
                    JsonWithJsonPath(
                        ".",
                        jsonElement = Json.encodeToJsonElement("{}"),
                    ),
            )
        project.apiHolder.apiDefinitions.add(api)
        project.apiHolder.apiDefinitions.add(postApi)

        val button = ButtonTrait().defaultComposeNode(project)
        button.actionsMap[ActionType.OnClick] =
            mutableListOf(
                ActionNode.Simple(
                    action =
                        StateAction.SetAppStateValue(
                            setValueToStates =
                                mutableListOf(
                                    SetValueToState(
                                        writeToStateId = appStringState.id,
                                        operation =
                                            StateOperation.SetValue(
                                                readProperty = ApiResultProperty(apiId = api.id),
                                            ),
                                    ),
                                ),
                        ),
                ),
            )

        val lazyColumn = LazyColumnTrait().defaultComposeNode(project)
        rootNode.addChild(lazyColumn)
        lazyColumn.dynamicItems.value = ApiResultProperty(apiId = api.id)

        textField.actionsMap[ActionType.OnSubmit] =
            mutableListOf(
                ActionNode.Simple(
                    action =
                        CallApi(
                            apiId = api.id,
                            paramsMap =
                                mutableMapOf(
                                    api.parameters[0].parameterId to ValueFromState(readFromStateId = textFieldState.id),
                                ),
                        ),
                ),
                ActionNode.Simple(
                    action =
                        CallApi(
                            apiId = postApi.id,
                        ),
                ),
            )

        toolbarViewModel.onRunPreviewApp(
            project = project,
            onStatusBarUiStateChanged = { _ -> },
        )
        assertBuildSucceed()
    }

    @Test
    fun project_open_dialog_actions() {
        val project = Project()
        val rootNode = project.screenHolder.currentContentRootNode()
        val editable = project.screenHolder.currentEditable()

        val button1 = ButtonTrait().defaultComposeNode(project)
        rootNode.addChild(button1)
        button1.actionsMap[ActionType.OnClick] =
            mutableListOf(
                ActionNode.Simple(
                    action =
                        ShowInformationDialog(
                            title = StringProperty.StringIntrinsicValue("title"),
                            message = StringProperty.StringIntrinsicValue("message"),
                        ),
                ),
            )

        val screen = project.screenHolder.currentEditable()
        if (screen is Screen) {
            screen.fabNode.value?.actionsMap?.set(
                ActionType.OnClick,
                mutableListOf(
                    ActionNode.Simple(
                        action =
                            ShowInformationDialog(
                                title = StringProperty.StringIntrinsicValue("title"),
                                message = StringProperty.StringIntrinsicValue("message"),
                            ),
                    ),
                ),
            )
        }

        val row = RowTrait().defaultComposeNode(project)
        val text = TextTrait().defaultComposeNode(project)
        row.addChild(text)
        row.addChild(SwitchTrait().defaultComposeNode(project))
        val switchState = ScreenState.BooleanScreenState(name = "switch")
        editable.addState(switchState)

        val component =
            Component(
                name = "component",
                componentRoot = mutableStateOf(row),
            )
        val stringParameter = ParameterWrapper.StringParameter(name = "stringParam")
        val booleanParameter = ParameterWrapper.BooleanParameter(name = "booleanParam")
        val intParameter = ParameterWrapper.IntParameter(name = "intParam")
        val floatParameter = ParameterWrapper.FloatParameter(name = "floatParam")
        component.parameters.add(stringParameter)
        component.parameters.add(booleanParameter)
        component.parameters.add(intParameter)
        component.parameters.add(floatParameter)
        project.componentHolder.components.add(component)

        val showCustomDialogAction =
            ShowCustomDialog(
                componentId = component.id,
                paramsMap = mutableMapOf(),
            )
        val showBottomSheetAction =
            ShowBottomSheet(
                componentId = component.id,
                paramsMap = mutableMapOf(),
            )
        showCustomDialogAction.paramsMap[stringParameter.id] =
            StringProperty.StringIntrinsicValue("argument")
        showCustomDialogAction.paramsMap[booleanParameter.id] =
            BooleanProperty.BooleanIntrinsicValue(true)

        val button2 = ButtonTrait().defaultComposeNode(project)
        rootNode.addChild(button2)
        button2.actionsMap[ActionType.OnClick] =
            mutableListOf(
                ActionNode.Simple(
                    action =
                        ShowInformationDialog(
                            title = StringProperty.StringIntrinsicValue("title2"),
                            message = StringProperty.StringIntrinsicValue("message2"),
                        ),
                ),
                ActionNode.Simple(
                    action = showCustomDialogAction,
                ),
                ActionNode.Simple(
                    action = showBottomSheetAction,
                ),
            )

        val button3 = ButtonTrait().defaultComposeNode(project)
        rootNode.addChild(button3)
        button3.actionsMap[ActionType.OnClick] =
            mutableListOf(
                ActionNode.Forked(
                    forkedAction =
                        ShowConfirmationDialog(
                            title = StringProperty.StringIntrinsicValue("title2"),
                            message = StringProperty.StringIntrinsicValue("message2"),
                        ),
                    trueNodes =
                        mutableListOf(
                            ActionNode.Simple(action = Navigation.NavigateBack),
                        ),
                    falseNodes =
                        mutableListOf(
                            ActionNode.Simple(action = Navigation.NavigateBack),
                            ActionNode.Conditional(
                                ifCondition = BooleanProperty.BooleanIntrinsicValue(false),
                                trueNodes =
                                    mutableListOf(
                                        ActionNode.Simple(action = Navigation.NavigateBack),
                                    ),
                            ),
                        ),
                ),
            )

        if (screen is Screen) {
            screen.navigationDrawerNode.value =
                NavigationDrawerTrait().defaultComposeNode(project).apply {
                    addChild(
                        TextTrait().defaultComposeNode(project),
                    )
                    addChild(
                        ButtonTrait().defaultComposeNode(project),
                    )
                    addChild(
                        NavigationDrawerItemTrait(
                            imageVectorHolder = Outlined.Add,
                        ).defaultComposeNode(project),
                    )
                }
        }
        val openNavDrawerButton = ButtonTrait().defaultComposeNode(project)
        rootNode.addChild(openNavDrawerButton)
        openNavDrawerButton.actionsMap[ActionType.OnClick] =
            mutableListOf(
                ActionNode.Simple(
                    action = ShowNavigationDrawer(),
                ),
            )

        toolbarViewModel.onRunPreviewApp(
            project = project,
            onStatusBarUiStateChanged = { _ -> },
        )
        assertBuildSucceed()
    }

    @Test
    fun project_show_and_share_actions() {
        val project = Project()
        val rootNode = project.screenHolder.currentContentRootNode()

        val button1 = ButtonTrait().defaultComposeNode(project)
        rootNode.addChild(button1)
        button1.actionsMap[ActionType.OnClick] =
            mutableListOf(
                ActionNode.Simple(
                    action =
                        ShowMessaging.Snackbar(
                            message = StringProperty.StringIntrinsicValue("message"),
                            actionLabel = null,
                        ),
                ),
                ActionNode.Simple(
                    action =
                        Share.OpenUrl(
                            url = StringProperty.StringIntrinsicValue("https://en.wikipedia.org/wiki/Apple"),
                        ),
                ),
            )

        toolbarViewModel.onRunPreviewApp(
            project = project,
            onStatusBarUiStateChanged = { _ -> },
        )
        assertBuildSucceed()
    }

    @Test
    fun project_open_datePicker_action() {
        val project = Project()
        val rootNode = project.screenHolder.currentContentRootNode()

        val button1 = ButtonTrait().defaultComposeNode(project)
        rootNode.addChild(button1)
        val openDatePickerAction = DateOrTimePicker.OpenDatePicker()

        val openDateAndTimePickerAction = DateOrTimePicker.OpenDateAndTimePicker()

        button1.actionsMap[ActionType.OnClick] =
            mutableListOf(
                ActionNode.Simple(
                    action = openDatePickerAction,
                ),
                ActionNode.Simple(
                    action = openDateAndTimePickerAction,
                ),
            )

        toolbarViewModel.onRunPreviewApp(
            project = project,
            onStatusBarUiStateChanged = { _ -> },
        )
        assertBuildSucceed()
    }

    @Test
    fun project_auth_action() {
        val project = Project()
        val rootNode = project.screenHolder.currentContentRootNode()
        val screen = project.screenHolder.currentEditable()

        val button1 = ButtonTrait().defaultComposeNode(project)
        rootNode.addChild(button1)
        button1.actionsMap[ActionType.OnClick] =
            mutableListOf(
                ActionNode.Simple(
                    action = Auth.SignInWithGoogle,
                ),
            )
        val text = TextTrait().defaultComposeNode(project)
        text.trait.value =
            TextTrait(
                text = ValueFromGlobalProperty(readableState = AuthenticatedUserState.DisplayName),
            )
        rootNode.addChild(text)

        val emailField = TextFieldTrait().defaultComposeNode(project)
        val emailFieldState = ScreenState.StringScreenState(name = "emailField")
        screen.addState(emailFieldState)
        val passwordField = TextFieldTrait().defaultComposeNode(project)
        val passwordFieldState = ScreenState.StringScreenState(name = "passwordField")
        screen.addState(passwordFieldState)

        val createUserButton = ButtonTrait().defaultComposeNode(project)
        createUserButton.actionsMap[ActionType.OnClick] =
            mutableListOf(
                ActionNode.Simple(
                    action =
                        Auth.CreateUserWithEmailAndPassword(
                            email = ValueFromState(readFromStateId = emailFieldState.id),
                            password = ValueFromState(readFromStateId = passwordFieldState.id),
                        ),
                ),
            )
        rootNode.addChild(emailField)
        rootNode.addChild(passwordField)
        rootNode.addChild(createUserButton)

        toolbarViewModel.onRunPreviewApp(
            project = project,
            onStatusBarUiStateChanged = { _ -> },
        )
        assertBuildSucceed()
    }

    @Test
    fun addDocument_toFirestore() {
        val project = Project()
        val rootNode = project.screenHolder.currentContentRootNode()
        val textField1 =
            ComposeNode(
                trait = mutableStateOf(TextFieldTrait()),
            ).apply {
                setLabel("textField1")
            }
        val textFieldParams = textField1.trait.value as TextFieldTrait

        val todoNameField =
            DataField(
                name = "todoName",
                fieldType = FieldType.String(),
            )
        val doneField =
            DataField(
                name = "done",
                fieldType = FieldType.Boolean(),
            )
        val todoItemDataType =
            DataType(
                name = "TodoItem",
            ).apply {
                fields.add(todoNameField)
                fields.add(doneField)
            }
        project.dataTypeHolder.dataTypes.add(todoItemDataType)
        val firestoreCollection =
            FirestoreCollection(
                name = "todoCollection",
                dataTypeId = todoItemDataType.id,
            )
        project.firebaseAppInfoHolder.firebaseAppInfo.firestoreCollections
            .add(firestoreCollection)

        textField1.trait.value =
            textFieldParams.copy(
                placeholder = StringProperty.StringIntrinsicValue("placeholder text"),
                label = StringProperty.StringIntrinsicValue("label text"),
                enableValidator = true,
                textFieldValidator = TextFieldValidator.IntValidator(),
            )
        val button = ButtonTrait().defaultComposeNode(project)
        button.actionsMap[ActionType.OnClick] =
            mutableStateListOf(
                ActionNode.Simple(
                    action =
                        FirestoreAction.SaveToFirestore(
                            collectionId = firestoreCollection.id,
                            dataFieldUpdateProperties =
                                mutableListOf(
                                    DataFieldUpdateProperty(
                                        dataFieldId = todoItemDataType.fields[0].id,
                                        assignableProperty = StringProperty.StringIntrinsicValue("testItem"),
                                    ),
                                    DataFieldUpdateProperty(
                                        dataFieldId = todoItemDataType.fields[1].id,
                                        assignableProperty =
                                            BooleanProperty.BooleanIntrinsicValue(
                                                true,
                                            ),
                                    ),
                                ),
                        ),
                ),
            )
        rootNode.addChild(button)

        val lazyColumn = LazyColumnTrait().defaultComposeNode(project)
        val dynamicItems = FirestoreCollectionProperty(collectionId = firestoreCollection.id)
        lazyColumn.dynamicItems.value = dynamicItems
        lazyColumn.addChild(
            ComposeNode(
                trait = mutableStateOf(RowTrait()),
            ).apply {
                lazyListChildParams.value =
                    LazyListChildParams.DynamicItemsSource(composeNodeId = lazyColumn.id)
                addChild(
                    ComposeNode(
                        trait = mutableStateOf(TextTrait()),
                    ).apply {
                        trait.value =
                            TextTrait(
                                text =
                                    ValueFromDynamicItem(
                                        composeNodeId = lazyColumn.id,
                                        fieldType = DataFieldType.Primitive,
                                    ),
                            )
                    },
                )
                addChild(
                    ComposeNode(
                        trait = mutableStateOf(ButtonTrait()),
                    ).apply {
                        actionsMap[ActionType.OnClick] =
                            mutableStateListOf(
                                ActionNode.Simple(
                                    action =
                                        FirestoreAction.DeleteDocument(
                                            collectionId = firestoreCollection.id,
                                            filterExpression =
                                                SingleFilter(
                                                    filterFieldType =
                                                        FilterFieldType.DocumentId(
                                                            firestoreCollection.id,
                                                        ),
                                                    operator = FilterOperator.EqualTo,
                                                    property =
                                                        ValueFromDynamicItem(
                                                            composeNodeId = lazyColumn.id,
                                                            fieldType =
                                                                DataFieldType.DocumentId(
                                                                    firestoreCollection.id,
                                                                ),
                                                        ),
                                                ),
                                        ),
                                ),
                            )
                    },
                )
            },
        )
        rootNode.addChild(lazyColumn)

        toolbarViewModel.onRunPreviewApp(
            project = project,
            onStatusBarUiStateChanged = { _ -> },
        )
        assertBuildSucceed()
    }

    private fun assertBuildSucceed() {
        assertTrue(testLogWriter.logs.any { it.message.contains("BUILD SUCCESSFUL") })
    }
}

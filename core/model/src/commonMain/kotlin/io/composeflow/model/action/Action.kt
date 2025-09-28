@file:OptIn(kotlin.time.ExperimentalTime::class)

package io.composeflow.model.action

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.composeflow.ComposeScreenConstant
import io.composeflow.Res
import io.composeflow.ViewModelConstant
import io.composeflow.call_api
import io.composeflow.confirmation
import io.composeflow.create_user_with
import io.composeflow.custom
import io.composeflow.dialog_bottom_sheet_drawer
import io.composeflow.email_and_password
import io.composeflow.firestore_delete_document
import io.composeflow.firestore_save_to_firestore
import io.composeflow.firestore_update_document
import io.composeflow.information
import io.composeflow.kotlinpoet.ClassHolder
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.kotlinpoet.MemberHolder
import io.composeflow.kotlinpoet.wrapper.ClassNameWrapper
import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper
import io.composeflow.kotlinpoet.wrapper.FunSpecWrapper
import io.composeflow.kotlinpoet.wrapper.KModifierWrapper
import io.composeflow.kotlinpoet.wrapper.LambdaTypeNameWrapper
import io.composeflow.kotlinpoet.wrapper.MemberNameWrapper
import io.composeflow.kotlinpoet.wrapper.ParameterSpecWrapper
import io.composeflow.kotlinpoet.wrapper.PropertySpecWrapper
import io.composeflow.kotlinpoet.wrapper.UNIT
import io.composeflow.kotlinpoet.wrapper.asTypeNameWrapper
import io.composeflow.kotlinpoet.wrapper.parameterizedBy
import io.composeflow.model.InspectorTabDestination
import io.composeflow.model.apieditor.ApiId
import io.composeflow.model.datatype.FilterExpression
import io.composeflow.model.datatype.SingleFilter
import io.composeflow.model.project.COMPOSEFLOW_PACKAGE
import io.composeflow.model.project.ParameterId
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.SCREEN_ROUTE
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.project.component.COMPONENT_KEY_NAME
import io.composeflow.model.project.component.Component
import io.composeflow.model.project.component.ComponentId
import io.composeflow.model.project.findApiDefinitionOrNull
import io.composeflow.model.project.findComponentOrNull
import io.composeflow.model.project.findDataTypeOrNull
import io.composeflow.model.project.findFirestoreCollectionOrNull
import io.composeflow.model.project.findLocalStateOrNull
import io.composeflow.model.project.findParameterOrThrow
import io.composeflow.model.project.findScreenOrNull
import io.composeflow.model.project.firebase.CollectionId
import io.composeflow.model.project.issue.Issue
import io.composeflow.model.project.issue.NavigatableDestination
import io.composeflow.model.property.AssignableProperty
import io.composeflow.model.property.IntrinsicProperty
import io.composeflow.model.property.StringProperty
import io.composeflow.model.state.ScreenState
import io.composeflow.model.state.StateId
import io.composeflow.model.state.WriteableState
import io.composeflow.model.type.ComposeFlowType
import io.composeflow.model.type.convertCodeFromType
import io.composeflow.navigate_back
import io.composeflow.navigate_to
import io.composeflow.open_date_picker
import io.composeflow.open_date_plus_time_picker
import io.composeflow.open_url
import io.composeflow.override.mutableStateListEqualsOverrideOf
import io.composeflow.serializer.FallbackMutableStateListSerializer
import io.composeflow.serializer.FallbackMutableStateMapSerializer
import io.composeflow.serializer.MutableStateSerializer
import io.composeflow.set_state
import io.composeflow.show_bottom_sheet
import io.composeflow.show_nav_drawer
import io.composeflow.show_snackbar
import io.composeflow.sign_in_with
import io.composeflow.sign_out
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant
import kotlin.uuid.Uuid

typealias ActionId = String

@Serializable
sealed interface Action {
    val id: ActionId

    @Composable
    fun SimplifiedContent(project: Project)

    val name: String

    fun generateActionTriggerCodeBlock(
        project: Project,
        context: GenerationContext,
        dryRun: Boolean,
    ): CodeBlockWrapper? = null

    /**
     * Generate the CodeBlock when the action is initialized.
     *
     * @param additionalCodeBlocks codeBlocks are passed if any additional code blocks are needed
     * for the action. For example, [ShowConfirmationDialog] accepts code blocks when the
     * positive text is clicked and negative text is clicked for each.
     */
    fun generateInitializationCodeBlock(
        project: Project,
        context: GenerationContext,
        dryRun: Boolean,
        vararg additionalCodeBlocks: CodeBlockWrapper = arrayOf(),
    ): CodeBlockWrapper? = null

    fun argumentName(project: Project): String? = null

    fun generateArgumentParameterSpec(project: Project): ParameterSpecWrapper? = null

    fun generateNavigationInitializationBlock(): CodeBlockWrapper? = null

    /**
     * Generates the CodeBlock if the action that triggers the action needs to be wrapped with
     * some code.
     * E.g. Sign in With Google needs to be wrapped with the following code so that the
     * action that triggers sign in is within the expexted scope.
     *
     * GoogleButtonUiContainerFirebase(onResult = {}) {
     *      // Composable that triggers the action
     * }
     */
    fun generateWrapWithComposableBlock(insideContent: CodeBlockWrapper): CodeBlockWrapper? = null

    /**
     * Adds methods or properties to the given [context].
     *
     * For example, [SetValueToState] action adds the methods and properties to update the states
     */
    fun addUpdateMethodAndReadProperty(
        project: Project,
        context: GenerationContext,
        dryRun: Boolean,
    ) {
    }

    fun isDependent(sourceId: String): Boolean = false

    fun asActionNode(actionNodeId: ActionNodeId? = null): ActionNode

    /**
     * Defines a companion state if the action needs a state for hold
     * some state.
     * For example, a OpenDatePicker action needs a InstantState for storing the picked value in the
     * DatePicker.
     */
    fun companionState(project: Project): ScreenState<*>? = null

    fun hasSuspendFunction(): Boolean = false

    fun getDependentComposeNodes(project: Project): List<ComposeNode> = emptyList()

    fun generateIssues(project: Project): List<Issue> = emptyList()
}

@Serializable
sealed interface Navigation : Action {
    @Serializable
    @SerialName("NavigateTo")
    data class NavigateTo(
        override val id: String = Uuid.random().toString(),
        val screenId: String,
        val paramsMap: MutableMap<ParameterId, AssignableProperty> =
            mutableStateMapOf(),
        @Transient
        override val name: String = "Navigate to",
    ) : Navigation {
        override fun getDependentComposeNodes(project: Project): List<ComposeNode> =
            paramsMap.entries.flatMap { it.value.getDependentComposeNodes(project) }

        override fun generateIssues(project: Project): List<Issue> =
            buildList {
                val screen = project.findScreenOrNull(screenId)
                if (screen == null) {
                    add(
                        Issue.InvalidScreenReference(
                            screenId,
                            destination =
                                NavigatableDestination.UiBuilderScreen(
                                    inspectorTabDestination = InspectorTabDestination.Action,
                                ),
                            issueContext = this@NavigateTo,
                        ),
                    )
                }
                screen?.let {
                    paramsMap.forEach { entry ->
                        val parameter = screen.parameters.firstOrNull { it.id == entry.key }
                        parameter?.let {
                            val transformedValueType = entry.value.transformedValueType(project)
                            if (transformedValueType is ComposeFlowType.UnknownType) {
                                add(
                                    Issue.ResolvedToUnknownType(
                                        property = entry.value,
                                        destination =
                                            NavigatableDestination.UiBuilderScreen(
                                                inspectorTabDestination = InspectorTabDestination.Action,
                                            ),
                                        issueContext = this@NavigateTo,
                                    ),
                                )
                            }

                            if (!parameter.parameterType.isAbleToAssign(transformedValueType)) {
                                add(
                                    Issue.ResolvedToTypeNotAssignable(
                                        property = entry.value,
                                        acceptableType = parameter.parameterType,
                                        destination =
                                            NavigatableDestination.UiBuilderScreen(
                                                inspectorTabDestination = InspectorTabDestination.Action,
                                            ),
                                        issueContext = this@NavigateTo,
                                    ),
                                )
                            }
                        }
                    }
                }
            }

        @Composable
        override fun SimplifiedContent(project: Project) {
            val screen = project.screenHolder.screens.firstOrNull { it.id == screenId }
            Column {
                Text(
                    stringResource(Res.string.navigate_to),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    screen?.name ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        override fun generateActionTriggerCodeBlock(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper {
            val targetScreen = project.screenHolder.screens.firstOrNull { it.id == screenId }
            val builder = CodeBlockWrapper.builder()
            targetScreen?.let {
                val argString =
                    if (targetScreen.parameters.isEmpty()) {
                        // ScreenRoute is data object. No parameters are needed
                        ""
                    } else {
                        buildString {
                            append("(")
                            paramsMap.entries.forEachIndexed { index, (key, value) ->
                                val parameter =
                                    targetScreen.parameters.firstOrNull { parameter ->
                                        parameter.id == key
                                    }

                                parameter?.let {
                                    if (value is IntrinsicProperty<*>) {
                                        append(
                                            "${parameter.variableName} = ${
                                                value.transformedCodeBlock(
                                                    project,
                                                    context,
                                                    parameter.parameterType,
                                                    dryRun = dryRun,
                                                )
                                            }",
                                        )
                                    } else {
                                        append(
                                            "${parameter.variableName} = ${
                                                value.transformedCodeBlock(
                                                    project,
                                                    context,
                                                    parameter.parameterType,
                                                    dryRun = dryRun,
                                                )
                                            }",
                                        )
                                    }
                                }
                                if (index != paramsMap.entries.size - 1) {
                                    append(",\n")
                                }
                            }
                            append(")")
                        }
                    }
                builder.addStatement(
                    """${argumentName(project)}($SCREEN_ROUTE.${it.routeName}$argString)""",
                )
            }
            return builder.build()
        }

        override fun argumentName(project: Project): String = ComposeScreenConstant.onNavigateToRoute.name

        override fun generateArgumentParameterSpec(project: Project): ParameterSpecWrapper =
            argumentName(project).let { argumentName ->
                ParameterSpecWrapper
                    .builder(
                        argumentName,
                        LambdaTypeNameWrapper.get(
                            receiver = null,
                            parameters =
                                listOf(
                                    ParameterSpecWrapper.unnamed(ClassNameWrapper.get("", SCREEN_ROUTE)),
                                ),
                            returnType = UNIT,
                        ),
                    ).build()
            }

        override fun generateNavigationInitializationBlock(): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            builder.addStatement(
                """{
                navController.navigate(it)
            }
            """,
            )
            return builder.build()
        }

        override fun isDependent(sourceId: String): Boolean = paramsMap.any { it.value.isDependent(sourceId) }

        override fun asActionNode(actionNodeId: ActionNodeId?): ActionNode =
            ActionNode.Simple(id = actionNodeId ?: Uuid.random().toString(), action = this)
    }

    @Serializable
    @SerialName("NavigateBack")
    data object NavigateBack : Navigation {
        override val id: String = Uuid.random().toString()

        @Transient
        override val name: String = "Navigate back"

        @Composable
        override fun SimplifiedContent(project: Project) {
            Column {
                Text(
                    stringResource(Res.string.navigate_back),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        override fun generateActionTriggerCodeBlock(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            builder.addStatement("${argumentName(project)}()")
            return builder.build()
        }

        override fun argumentName(project: Project): String = ComposeScreenConstant.onNavigateBack.name

        override fun generateArgumentParameterSpec(project: Project): ParameterSpecWrapper =
            argumentName(project).let { argumentName ->
                ParameterSpecWrapper
                    .builder(
                        argumentName,
                        LambdaTypeNameWrapper.get(
                            returnType = UNIT,
                        ),
                    ).build()
            }

        override fun generateNavigationInitializationBlock(): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            builder.add(
                """{
                navController.navigateUp()
            }""",
            )
            return builder.build()
        }

        override fun asActionNode(actionNodeId: ActionNodeId?): ActionNode =
            ActionNode.Simple(id = actionNodeId ?: Uuid.random().toString(), action = this)
    }
}

@Serializable
sealed interface StateAction : Action {
    @Serializable
    @SerialName("SetAppStateValue")
    data class SetAppStateValue(
        override val id: String = Uuid.random().toString(),
        @Serializable(FallbackMutableStateListSerializer::class)
        val setValueToStates: MutableList<SetValueToState> = mutableStateListEqualsOverrideOf(),
        @Transient
        override val name: String = "Set state",
    ) : StateAction {
        override fun getDependentComposeNodes(project: Project): List<ComposeNode> =
            setValueToStates.flatMap { it.operation.getDependentComposeNodes(project) }

        override fun generateIssues(project: Project): List<Issue> =
            buildList {
                setValueToStates.forEach { setValueToState ->
                    project.findLocalStateOrNull(setValueToState.writeToStateId)?.let {
                        setValueToState.operation.getAssignableProperties().forEach {
                            if (it.transformedValueType(project) is ComposeFlowType.UnknownType) {
                                add(
                                    Issue.ResolvedToUnknownType(
                                        property = it,
                                        destination =
                                            NavigatableDestination.UiBuilderScreen(
                                                inspectorTabDestination = InspectorTabDestination.Action,
                                            ),
                                        issueContext = this@SetAppStateValue,
                                    ),
                                )
                            }
                        }
                    }
                }
            }

        override fun isDependent(sourceId: String): Boolean =
            setValueToStates.any {
                it.isDependent(sourceId)
            } ||
                setValueToStates.any {
                    it.writeToStateId == sourceId
                }

        @Composable
        override fun SimplifiedContent(project: Project) {
            Column {
                Text(
                    stringResource(Res.string.set_state),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (setValueToStates.size == 1) {
                    val writeState =
                        project.findLocalStateOrNull(setValueToStates.first().writeToStateId)
                    Text(
                        writeState?.name ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                } else if (setValueToStates.size > 1) {
                    Text(
                        "${setValueToStates.size} states",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }

        override fun generateActionTriggerCodeBlock(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            // CanvasEditable that has this action
            val editable =
                project
                    .getAllCanvasEditable()
                    .firstOrNull {
                        it
                            .getAllComposeNodes()
                            .any { node -> node.allActions().any { action -> action == this } }
                    }
                    ?: throw IllegalStateException("No CanvasEditable has State $this")
            setValueToStates.forEach {
                it.getUpdateMethodName(project, context)?.let { updateMethodName ->
                    builder.addStatement(
                        "${editable.viewModelName}.$updateMethodName(${
                            it.operation.getUpdateMethodParamsAsString(
                                project,
                                context,
                                dryRun = dryRun,
                            )
                        })",
                    )
                }
            }
            return builder.build()
        }

        override fun addUpdateMethodAndReadProperty(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ) {
            setValueToStates.forEach { setValueToState ->
                setValueToState.addUpdateMethodAndReadProperty(
                    project,
                    context = context,
                    dryRun = dryRun,
                )
            }
        }

        override fun asActionNode(actionNodeId: ActionNodeId?): ActionNode =
            ActionNode.Simple(id = actionNodeId ?: Uuid.random().toString(), action = this)
    }
}

@Serializable
@SerialName("SetValueToState")
data class SetValueToState(
    val writeToStateId: StateId,
    val operation: StateOperation,
) {
    fun isDependent(sourceId: String): Boolean = operation.isDependent(sourceId)

    fun getUpdateMethodName(
        project: Project,
        context: GenerationContext,
    ): String? {
        val state = project.findLocalStateOrNull(writeToStateId) ?: return null
        val writeState = state as? WriteableState ?: return null
        return operation.getUpdateMethodName(project, context, writeState)
    }

    fun addUpdateMethodAndReadProperty(
        project: Project,
        context: GenerationContext,
        dryRun: Boolean,
    ) {
        val state = project.findLocalStateOrNull(writeToStateId) ?: return
        val writeState = state as? WriteableState ?: return
        operation.addUpdateMethodAndReadProperty(
            project = project,
            context = context,
            writeState = writeState,
            dryRun = dryRun,
        )
    }
}

@Serializable
@SerialName("CallApi")
data class CallApi(
    override val id: String = Uuid.random().toString(),
    val apiId: ApiId,
    val paramsMap: MutableMap<ParameterId, AssignableProperty> =
        mutableStateMapOf(),
    @Transient
    override val name: String = "Call API",
) : Action {
    override fun getDependentComposeNodes(project: Project): List<ComposeNode> =
        paramsMap.entries.flatMap { it.value.getDependentComposeNodes(project) }

    override fun generateIssues(project: Project): List<Issue> =
        buildList {
            val api = project.findApiDefinitionOrNull(apiId)
            if (api == null) {
                add(
                    Issue.InvalidApiReference(
                        apiId = apiId,
                        destination =
                            NavigatableDestination.UiBuilderScreen(
                                inspectorTabDestination = InspectorTabDestination.Action,
                            ),
                        issueContext = this@CallApi,
                    ),
                )
            }
            api?.let {
                paramsMap.entries.forEach { entry ->
                    api.parameters.firstOrNull { it.parameterId == entry.key }?.let {
                        entry.value.getAssignableProperties().forEach { property ->
                            val transformedValueType = property.transformedValueType(project)
                            if (transformedValueType is ComposeFlowType.UnknownType) {
                                add(
                                    Issue.ResolvedToUnknownType(
                                        property = property,
                                        destination =
                                            NavigatableDestination.UiBuilderScreen(
                                                inspectorTabDestination = InspectorTabDestination.Action,
                                            ),
                                        issueContext = this@CallApi,
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        }

    @Composable
    override fun SimplifiedContent(project: Project) {
        Column {
            val api = project.findApiDefinitionOrNull(apiId)
            Text(
                stringResource(Res.string.call_api),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                api?.name ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.padding(top = 8.dp),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        }
    }

    override fun isDependent(sourceId: String): Boolean = paramsMap.any { it.value.isDependent(sourceId) }

    override fun generateActionTriggerCodeBlock(
        project: Project,
        context: GenerationContext,
        dryRun: Boolean,
    ): CodeBlockWrapper {
        val builder = CodeBlockWrapper.builder()
        val apiDefinition = project.findApiDefinitionOrNull(apiId)
        apiDefinition?.let {
            val argumentString =
                buildString {
                    paramsMap.entries.forEachIndexed { index, (parameterId, value) ->
                        val parameter =
                            apiDefinition.parameters.firstOrNull { it.parameterId == parameterId }
                        parameter?.let {
                            append("${parameter.variableName} = ")
                            append(
                                "${
                                    value.transformedCodeBlock(
                                        project,
                                        context,
                                        dryRun = dryRun,
                                    )
                                }",
                            )
                            if (index != paramsMap.entries.size - 1) {
                                append(",")
                            }
                        }
                    }
                }
            builder.addStatement(
                """${context.currentEditable.viewModelName}.${apiDefinition.callApiFunName()}($argumentString)""",
            )
        }
        return builder.build()
    }

    override fun asActionNode(actionNodeId: ActionNodeId?): ActionNode =
        ActionNode.Simple(id = actionNodeId ?: Uuid.random().toString(), action = this)
}

sealed interface ShowModal : Action {
    companion object {
        fun entries(): List<ShowModal> =
            listOf(
                ShowInformationDialog(),
                ShowConfirmationDialog(),
                ShowCustomDialog(),
                ShowBottomSheet(),
                ShowNavigationDrawer(),
            )
    }
}

@Serializable
@SerialName("ShowConfirmationDialog")
data class ShowConfirmationDialog(
    override val id: String = Uuid.random().toString(),
    val title: AssignableProperty? = null,
    val message: AssignableProperty? = null,
    val negativeText: AssignableProperty = StringProperty.StringIntrinsicValue("Cancel"),
    val positiveText: AssignableProperty = StringProperty.StringIntrinsicValue("Confirm"),
    @Transient
    override val name: String = "Show confirmation dialog",
    var dialogOpenVariableName: String = "openConfirmationDialog",
) : ShowModal,
    Action {
    override fun getDependentComposeNodes(project: Project): List<ComposeNode> =
        (title?.getDependentComposeNodes(project) ?: emptyList()) +
            (message?.getDependentComposeNodes(project) ?: emptyList()) +
            negativeText.getDependentComposeNodes(project) +
            positiveText.getDependentComposeNodes(project)

    override fun generateIssues(project: Project): List<Issue> =
        buildList {
            listOf(title, message, negativeText, positiveText)
                .mapNotNull { it }
                .flatMap { it.getAssignableProperties() }
                .forEach {
                    if (it.transformedValueType(project) is ComposeFlowType.UnknownType) {
                        add(
                            Issue.ResolvedToUnknownType(
                                property = it,
                                destination =
                                    NavigatableDestination.UiBuilderScreen(
                                        inspectorTabDestination = InspectorTabDestination.Action,
                                    ),
                                issueContext = this@ShowConfirmationDialog,
                            ),
                        )
                    }
                }
        }

    override fun asActionNode(actionNodeId: ActionNodeId?): ActionNode =
        ActionNode.Forked(id = actionNodeId ?: Uuid.random().toString(), forkedAction = this)

    @Composable
    override fun SimplifiedContent(project: Project) {
        Column {
            Text(
                stringResource(Res.string.dialog_bottom_sheet_drawer),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                stringResource(Res.string.confirmation),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }

    override fun generateInitializationCodeBlock(
        project: Project,
        context: GenerationContext,
        dryRun: Boolean,
        vararg additionalCodeBlocks: CodeBlockWrapper,
    ): CodeBlockWrapper {
        val builder = CodeBlockWrapper.builder()
        val composableContext = context.getCurrentComposableContext()
        val variableName =
            composableContext.getOrAddIdentifier(
                id = "$id-$dialogOpenVariableName",
                initialIdentifier = dialogOpenVariableName,
            )
        builder.addStatement(
            """
                var $variableName by %M { %M(false) }
                if ($variableName) {
                    %M(
                        positiveText = """,
            MemberHolder.AndroidX.Runtime.remember,
            MemberHolder.AndroidX.Runtime.mutableStateOf,
            MemberNameWrapper.get("${COMPOSEFLOW_PACKAGE}.ui.dialogs", "ConfirmationDialog"),
        )
        builder.add(
            positiveText.transformedCodeBlock(
                project,
                context,
                dryRun = dryRun,
            ),
        )
        builder.add(",")
        builder.addStatement("""negativeText = """)
        builder.add(
            negativeText.transformedCodeBlock(
                project,
                context,
                dryRun = dryRun,
            ),
        )
        builder.add(",")
        builder.addStatement("""positiveBlock = {""")
        if (additionalCodeBlocks.isNotEmpty()) {
            builder.add(additionalCodeBlocks[0])
        }
        builder.addStatement("},")
        builder.addStatement("""negativeBlock = {""")
        if (additionalCodeBlocks.size > 1) {
            builder.add(additionalCodeBlocks[1])
        }

        builder.addStatement("},")
        builder.addStatement(
            """
            onDismissRequest = {
                $variableName = false
            },
        """,
        )
        builder.add("title = ")
        builder.add(
            title?.transformedCodeBlock(project, context, dryRun = dryRun)
                ?: CodeBlockWrapper.of(""),
        )
        builder.addStatement(",")
        builder.add("message = ")
        builder.add(
            message?.transformedCodeBlock(project, context, dryRun = dryRun) ?: CodeBlockWrapper.of(
                "",
            ),
        )
        builder.addStatement(",")
        builder.addStatement(")") // Close ConfirmationDialog
        builder.addStatement("}") // Close if
        return builder.build()
    }

    override fun generateActionTriggerCodeBlock(
        project: Project,
        context: GenerationContext,
        dryRun: Boolean,
    ): CodeBlockWrapper {
        val builder = CodeBlockWrapper.builder()
        builder.addStatement("$dialogOpenVariableName = true")
        return builder.build()
    }
}

@Serializable
@SerialName("ShowInformationDialog")
data class ShowInformationDialog(
    override val id: ActionId = Uuid.random().toString(),
    val title: AssignableProperty? = null,
    val message: AssignableProperty? = null,
    val confirmText: AssignableProperty = StringProperty.StringIntrinsicValue("Ok"),
    @Transient
    override val name: String = "Show information dialog",
    @Transient
    val dialogOpenVariableName: String = "openInformationDialog",
) : ShowModal {
    override fun getDependentComposeNodes(project: Project): List<ComposeNode> =
        (title?.getDependentComposeNodes(project) ?: emptyList()) +
            (message?.getDependentComposeNodes(project) ?: emptyList()) +
            confirmText.getDependentComposeNodes(project)

    override fun generateIssues(project: Project): List<Issue> =
        buildList {
            listOf(title, message, confirmText)
                .mapNotNull { it }
                .flatMap { it.getAssignableProperties() }
                .forEach {
                    if (it.transformedValueType(project) is ComposeFlowType.UnknownType) {
                        add(
                            Issue.ResolvedToUnknownType(
                                property = it,
                                destination =
                                    NavigatableDestination.UiBuilderScreen(
                                        inspectorTabDestination = InspectorTabDestination.Action,
                                    ),
                                issueContext = this@ShowInformationDialog,
                            ),
                        )
                    }
                }
        }

    override fun asActionNode(actionNodeId: ActionNodeId?): ActionNode =
        ActionNode.Simple(id = actionNodeId ?: Uuid.random().toString(), action = this)

    @Composable
    override fun SimplifiedContent(project: Project) {
        Column {
            Text(
                stringResource(Res.string.dialog_bottom_sheet_drawer),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                stringResource(Res.string.information),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }

    override fun generateInitializationCodeBlock(
        project: Project,
        context: GenerationContext,
        dryRun: Boolean,
        vararg additionalCodeBlocks: CodeBlockWrapper,
    ): CodeBlockWrapper {
        val builder = CodeBlockWrapper.builder()
        val composableContext = context.getCurrentComposableContext()
        val dialogVariableName =
            composableContext.getOrAddIdentifier(
                id = "$id-$dialogOpenVariableName",
                initialIdentifier = dialogOpenVariableName,
            )
        val titleArg =
            title?.let {
                "title = ${it.transformedCodeBlock(project, context, dryRun = dryRun)},"
            } ?: ""
        val messageArg =
            message?.let {
                "message = ${it.transformedCodeBlock(project, context, dryRun = dryRun)},"
            } ?: ""
        builder.addStatement(
            """
                var $dialogVariableName by %M { %M(false) }
                if ($dialogVariableName) {
                    %M(
                        confirmText = ${
                confirmText.transformedCodeBlock(
                    project,
                    context,
                    dryRun = dryRun,
                )
            },
                        onDismissRequest = {
                            $dialogVariableName = false
                        },
                        $titleArg
                        $messageArg
                    )
                }
            """,
            MemberHolder.AndroidX.Runtime.remember,
            MemberHolder.AndroidX.Runtime.mutableStateOf,
            MemberNameWrapper.get("${COMPOSEFLOW_PACKAGE}.ui.dialogs", "InformationDialog"),
        )
        return builder.build()
    }

    override fun generateActionTriggerCodeBlock(
        project: Project,
        context: GenerationContext,
        dryRun: Boolean,
    ): CodeBlockWrapper {
        val builder = CodeBlockWrapper.builder()
        builder.addStatement("$dialogOpenVariableName = true")
        return builder.build()
    }
}

@Serializable
@SerialName("ShowModalWithComponent")
sealed interface ShowModalWithComponent : ShowModal {
    val componentId: ComponentId?
    val paramsMap: MutableMap<ParameterId, AssignableProperty>

    fun copy(
        paramsMap: MutableMap<
            ParameterId,
            AssignableProperty,
        >,
    ): ShowModalWithComponent

    fun findComponentOrNull(project: Project): Component? =
        if (componentId != null) {
            componentId?.let { project.findComponentOrNull(it) }
        } else if (project.componentHolder.components.isNotEmpty()) {
            project.componentHolder.components[0]
        } else {
            null
        }

    override fun generateIssues(project: Project): List<Issue> {
        val component = findComponentOrNull(project) ?: return emptyList()
        return buildList {
            paramsMap.entries.forEach { entry ->
                component.parameters.firstOrNull { it.id == entry.key }?.let { parameter ->
                    entry.value.getAssignableProperties().forEach { property ->
                        val transformedValueType = property.transformedValueType(project)
                        if (transformedValueType is ComposeFlowType.UnknownType) {
                            add(
                                Issue.ResolvedToUnknownType(
                                    property = property,
                                    destination =
                                        NavigatableDestination.UiBuilderScreen(
                                            inspectorTabDestination = InspectorTabDestination.Action,
                                        ),
                                    issueContext = this@ShowModalWithComponent,
                                ),
                            )
                        }
                        if (!parameter.parameterType.isAbleToAssign(transformedValueType)) {
                            add(
                                Issue.ResolvedToTypeNotAssignable(
                                    property = property,
                                    acceptableType = parameter.parameterType,
                                    destination =
                                        NavigatableDestination.UiBuilderScreen(
                                            inspectorTabDestination = InspectorTabDestination.Action,
                                        ),
                                    issueContext = this@ShowModalWithComponent,
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Serializable
@SerialName("ShowCustomDialog")
data class ShowCustomDialog(
    override val id: ActionId = Uuid.random().toString(),
    override val componentId: ComponentId? = null,
    @Serializable(FallbackMutableStateMapSerializer::class)
    override val paramsMap: MutableMap<ParameterId, AssignableProperty> =
        mutableStateMapOf(),
    @Transient
    override val name: String = "Show custom dialog",
    @Transient
    val dialogOpenVariableName: String = "openCustomDialog",
) : ShowModalWithComponent {
    override fun copy(paramsMap: MutableMap<ParameterId, AssignableProperty>): ShowModalWithComponent = this.copy(paramsMap = paramsMap)

    override fun getDependentComposeNodes(project: Project): List<ComposeNode> =
        paramsMap.entries.flatMap {
            it.value.getDependentComposeNodes(project)
        }

    override fun asActionNode(actionNodeId: ActionNodeId?): ActionNode =
        ActionNode.Simple(id = actionNodeId ?: Uuid.random().toString(), action = this)

    @Composable
    override fun SimplifiedContent(project: Project) {
        Column {
            Text(
                stringResource(Res.string.dialog_bottom_sheet_drawer),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                stringResource(Res.string.custom),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }

    override fun generateInitializationCodeBlock(
        project: Project,
        context: GenerationContext,
        dryRun: Boolean,
        vararg additionalCodeBlocks: CodeBlockWrapper,
    ): CodeBlockWrapper {
        val component = findComponentOrNull(project) ?: return CodeBlockWrapper.of("")
        val composableContext = context.getCurrentComposableContext()
        val dialogOpenVariableName =
            composableContext.addComposeFileVariable(
                id = "$id-$dialogOpenVariableName",
                initialIdentifier = dialogOpenVariableName,
                dryRun = dryRun,
            )

        val paramBuilder = CodeBlockWrapper.builder()
        paramsMap.forEach {
            val parameter = project.findParameterOrThrow(it.key)
            val assignableProperty = it.value
            paramBuilder.add("${parameter.variableName} = ")
            paramBuilder.add(
                assignableProperty.transformedCodeBlock(
                    project,
                    context,
                    dryRun = dryRun,
                ),
            )
            paramBuilder.addStatement(",")
        }
        val componentBuilder = CodeBlockWrapper.builder()
        val componentInvocationCount = context.componentCountMap[component.name] ?: 0
        context.componentCountMap[component.name] = componentInvocationCount + 1
        componentBuilder.addStatement(
            """
                %M(
                    ${paramBuilder.build()}
                    $COMPONENT_KEY_NAME = "${component.name}-$componentInvocationCount"
                )""",
            component.asMemberName(project),
        )

        val builder = CodeBlockWrapper.builder()
        builder.addStatement(
            """
                var $dialogOpenVariableName by %M { %M(false) }
                if ($dialogOpenVariableName) {
                    %M(
                        onDismissRequest = {
                            $dialogOpenVariableName = false
                        },
                        content = {
                            ${componentBuilder.build()}
                        }
                    )
                }
            """,
            MemberHolder.AndroidX.Runtime.remember,
            MemberHolder.AndroidX.Runtime.mutableStateOf,
            MemberNameWrapper.get("${COMPOSEFLOW_PACKAGE}.ui.dialogs", "CustomDialog"),
        )
        return builder.build()
    }

    override fun generateActionTriggerCodeBlock(
        project: Project,
        context: GenerationContext,
        dryRun: Boolean,
    ): CodeBlockWrapper {
        val builder = CodeBlockWrapper.builder()
        findComponentOrNull(project) ?: builder.build()

        builder.addStatement("$dialogOpenVariableName = true")
        return builder.build()
    }
}

@Serializable
@SerialName("ShowBottomSheet")
data class ShowBottomSheet(
    override val id: ActionId = Uuid.random().toString(),
    override val componentId: ComponentId? = null,
    @Serializable(FallbackMutableStateMapSerializer::class)
    override val paramsMap: MutableMap<ParameterId, AssignableProperty> =
        mutableStateMapOf(),
    @Transient
    override val name: String = "Show bottom sheet",
    @Transient
    val bottomSheetOpenVariableName: String = "openBottomSheet",
) : ShowModalWithComponent {
    override fun copy(paramsMap: MutableMap<ParameterId, AssignableProperty>): ShowModalWithComponent = this.copy(paramsMap = paramsMap)

    override fun getDependentComposeNodes(project: Project): List<ComposeNode> =
        paramsMap.entries.flatMap {
            it.value.getDependentComposeNodes(project)
        }

    override fun asActionNode(actionNodeId: ActionNodeId?): ActionNode =
        ActionNode.Simple(id = actionNodeId ?: Uuid.random().toString(), action = this)

    @Composable
    override fun SimplifiedContent(project: Project) {
        Column {
            Text(
                stringResource(Res.string.dialog_bottom_sheet_drawer),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                stringResource(Res.string.show_bottom_sheet),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }

    override fun generateInitializationCodeBlock(
        project: Project,
        context: GenerationContext,
        dryRun: Boolean,
        vararg additionalCodeBlocks: CodeBlockWrapper,
    ): CodeBlockWrapper {
        val component = findComponentOrNull(project) ?: return CodeBlockWrapper.of("")

        val composableContext = context.getCurrentComposableContext()
        val bottomSheetOpenVariableName =
            composableContext.addComposeFileVariable(
                id = "$id-$bottomSheetOpenVariableName",
                initialIdentifier = bottomSheetOpenVariableName,
                dryRun = dryRun,
            )
        val initialBottomSheetStateName = "bottomSheetState"
        val bottomSheetStateVariable =
            composableContext.addComposeFileVariable(
                id = "$id-$initialBottomSheetStateName",
                initialBottomSheetStateName,
                dryRun = dryRun,
            )

        val paramBuilder = CodeBlockWrapper.builder()
        paramsMap.forEach {
            val parameter = project.findParameterOrThrow(it.key)
            val assignableProperty = it.value
            paramBuilder.add("${parameter.variableName} = ")
            paramBuilder.add(
                assignableProperty.transformedCodeBlock(
                    project,
                    context,
                    dryRun = dryRun,
                ),
            )
            paramBuilder.addStatement(",")
        }
        val componentBuilder = CodeBlockWrapper.builder()
        val componentInvocationCount = context.componentCountMap[component.name] ?: 0
        context.componentCountMap[component.name] = componentInvocationCount + 1
        componentBuilder.addStatement(
            """
                %M(
                    ${paramBuilder.build()}
                    $COMPONENT_KEY_NAME = "${component.name}-$componentInvocationCount"
                )""",
            component.asMemberName(project),
        )

        val builder = CodeBlockWrapper.builder()
        builder.addStatement(
            """
                var $bottomSheetOpenVariableName by %M { %M(false) }
                val $bottomSheetStateVariable = %M()
                if ($bottomSheetOpenVariableName) {
                    %M(
                        onDismissRequest = {
                            $bottomSheetOpenVariableName = false
                        },
                        sheetState = $bottomSheetStateVariable
                    ) {
                        ${componentBuilder.build()}
                    }
                }
            """,
            MemberHolder.AndroidX.Runtime.remember,
            MemberHolder.AndroidX.Runtime.mutableStateOf,
            MemberNameWrapper.get("androidx.compose.material3", "rememberModalBottomSheetState"),
            MemberNameWrapper.get("androidx.compose.material3", "ModalBottomSheet"),
        )
        return builder.build()
    }

    override fun generateActionTriggerCodeBlock(
        project: Project,
        context: GenerationContext,
        dryRun: Boolean,
    ): CodeBlockWrapper {
        val builder = CodeBlockWrapper.builder()
        findComponentOrNull(project) ?: return builder.build()

        builder.addStatement("$bottomSheetOpenVariableName = true")
        return builder.build()
    }
}

@Serializable
@SerialName("ShowNavigationDrawer")
data class ShowNavigationDrawer(
    override val id: ActionId = Uuid.random().toString(),
    @Transient
    override val name: String = "Show nav drawer",
) : ShowModal {
    override fun asActionNode(actionNodeId: ActionNodeId?): ActionNode =
        ActionNode.Simple(id = actionNodeId ?: Uuid.random().toString(), action = this)

    @Composable
    override fun SimplifiedContent(project: Project) {
        Column {
            Text(
                stringResource(Res.string.dialog_bottom_sheet_drawer),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                stringResource(Res.string.show_nav_drawer),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }

    override fun generateIssues(project: Project): List<Issue> {
        val screenHavingThisAction =
            project.screenHolder.screens.firstOrNull { screen ->
                screen.getAllComposeNodes().firstOrNull { node ->
                    node.allActions().any { action ->
                        action.id == this.id
                    }
                } != null
            }
        return if (screenHavingThisAction?.navigationDrawerNode?.value == null) {
            listOf(Issue.NavigationDrawerIsNotSet())
        } else {
            emptyList()
        }
    }

    override fun generateActionTriggerCodeBlock(
        project: Project,
        context: GenerationContext,
        dryRun: Boolean,
    ): CodeBlockWrapper {
        val screenHavingThisAction =
            project.screenHolder.screens.firstOrNull { screen ->
                screen.getAllComposeNodes().firstOrNull { node ->
                    node.allActions().any { action ->
                        action.id == this.id
                    }
                } != null
            }
        if (screenHavingThisAction?.navigationDrawerNode?.value == null) {
            return CodeBlockWrapper.of(
                "",
            )
        }

        val builder = CodeBlockWrapper.builder()
        screenHavingThisAction.let {
            builder.addStatement(
                """
           ${ComposeScreenConstant.coroutineScope.name}.%M {
               ${ComposeScreenConstant.navDrawerState}.apply {
                   if (isClosed) { open() } else {}
               }
           }
        """,
                MemberHolder.Coroutines.launch,
            )
        }
        return builder.build()
    }
}

@Serializable
sealed interface ShowMessaging : Action {
    @Serializable
    @SerialName("Snackbar")
    data class Snackbar(
        override val id: ActionId = Uuid.random().toString(),
        val message: AssignableProperty = StringProperty.StringIntrinsicValue("Snackbar message"),
        val actionLabel: AssignableProperty? = null,
        @Transient
        override val name: String = "Show Snackbar",
        @Transient
        val snackbarOpenVariableName: String = "onShowSnackbar",
    ) : ShowMessaging {
        override fun getDependentComposeNodes(project: Project): List<ComposeNode> =
            message.getDependentComposeNodes(project) +
                (actionLabel?.getDependentComposeNodes(project) ?: emptyList())

        override fun generateIssues(project: Project): List<Issue> =
            buildList {
                listOf(message, actionLabel)
                    .mapNotNull { it }
                    .flatMap { it.getAssignableProperties() }
                    .forEach {
                        if (it.transformedValueType(project) is ComposeFlowType.UnknownType) {
                            add(
                                Issue.ResolvedToUnknownType(
                                    property = it,
                                    destination =
                                        NavigatableDestination.UiBuilderScreen(
                                            inspectorTabDestination = InspectorTabDestination.Action,
                                        ),
                                    issueContext = this@Snackbar,
                                ),
                            )
                        }
                    }
            }

        @Composable
        override fun SimplifiedContent(project: Project) {
            Column {
                Text(
                    stringResource(Res.string.show_snackbar),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    message.displayText(project),
                    style = MaterialTheme.typography.bodyMedium,
                    color =
                        if (message is IntrinsicProperty<*>) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            MaterialTheme.colorScheme.tertiary
                        },
                    modifier = Modifier.padding(top = 8.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        override fun generateInitializationCodeBlock(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
            vararg additionalCodeBlocks: CodeBlockWrapper,
        ): CodeBlockWrapper {
            context.getCurrentComposableContext().addCompositionLocalVariableEntryIfNotPresent(
                id = "$id-$snackbarOpenVariableName",
                initialIdentifier = snackbarOpenVariableName,
                MemberHolder.ComposeFlow.LocalOnShowsnackbar,
            )
            // Initialize the uriHandler once in the compose file instead of initializing it in
            // every action
            return CodeBlockWrapper.of("")
        }

        override fun generateActionTriggerCodeBlock(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper {
            val actionLabel =
                actionLabel?.transformedCodeBlock(project, context, dryRun = dryRun) ?: "null"
            return CodeBlockWrapper.of(
                """${ComposeScreenConstant.coroutineScope.name}.%M {
                $snackbarOpenVariableName(${
                    message.transformedCodeBlock(
                        project,
                        context,
                        dryRun = dryRun,
                    )
                }, $actionLabel)
            }
        """,
                MemberHolder.Coroutines.launch,
            )
        }

        override fun hasSuspendFunction(): Boolean = true

        override fun asActionNode(actionNodeId: ActionNodeId?): ActionNode =
            ActionNode.Simple(id = actionNodeId ?: Uuid.random().toString(), action = this)
    }

    companion object {
        fun entries(): List<ShowMessaging> = listOf(Snackbar())
    }
}

sealed interface YearRangeSelectableAction : Action {
    var minSelectableYear: MutableState<AssignableProperty?>
    var maxSelectableYear: MutableState<AssignableProperty?>
    var onlyPastDates: MutableState<Boolean>
    var onlyFutureDates: MutableState<Boolean>
    val outputStateName: MutableState<String>

    fun generateRememberDatePickerState(
        stateVariableName: String,
        project: Project,
        context: GenerationContext,
        dryRun: Boolean,
    ): CodeBlockWrapper {
        val builder = CodeBlockWrapper.builder()
        builder.addStatement(
            "val $stateVariableName = %M(",
            MemberHolder.Material3.rememberDatePickerState,
        )
        val minYear = minSelectableYear.value
        val maxYear = maxSelectableYear.value
        if (minYear != null && maxYear != null) {
            builder.add(
                CodeBlockWrapper.of(
                    "yearRange = %T(${
                        minYear.transformedCodeBlock(
                            project,
                            context,
                            dryRun = dryRun,
                        )
                    }, ${maxYear.transformedCodeBlock(project, context, dryRun = dryRun)}),",
                    IntRange::class.asTypeNameWrapper(),
                ),
            )
        }
        if (onlyPastDates.value) {
            builder.add(
                CodeBlockWrapper.of(
                    """
            selectableDates = object : %M {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis <= %T.System.now().toEpochMilliseconds()
                }
            },""",
                    MemberHolder.Material3.SelectableDates,
                    ClassHolder.Kotlinx.DateTime.Clock,
                ),
            )
        }
        if (!onlyPastDates.value && onlyFutureDates.value) {
            builder.add(
                CodeBlockWrapper.of(
                    """
            selectableDates = object : %M {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis >= %T.System.now().toEpochMilliseconds()
                }
            },""",
                    MemberHolder.Material3.SelectableDates,
                    ClassHolder.Kotlinx.DateTime.Clock,
                ),
            )
        }
        builder.add(")")
        return builder.build()
    }
}

@Serializable
sealed interface DateOrTimePicker : Action {
    @Serializable
    @SerialName("OpenDatePicker")
    data class OpenDatePicker(
        override val id: ActionId = Uuid.random().toString(),
        @Serializable(MutableStateSerializer::class)
        override var minSelectableYear: MutableState<AssignableProperty?> = mutableStateOf(null),
        @Serializable(MutableStateSerializer::class)
        override var maxSelectableYear: MutableState<AssignableProperty?> = mutableStateOf(null),
        @Serializable(MutableStateSerializer::class)
        override var onlyPastDates: MutableState<Boolean> = mutableStateOf(false),
        @Serializable(MutableStateSerializer::class)
        override var onlyFutureDates: MutableState<Boolean> = mutableStateOf(false),
        @Transient
        override val name: String = "Date picker",
        @Transient
        private val dialogOpenVariableName: String = "openDatePicker",
        @Transient
        override val outputStateName: MutableState<String> = mutableStateOf("pickedDate"),
        @Transient
        private val rememberedDatePickerStateName: String = "datePickerState",
    ) : DateOrTimePicker,
        YearRangeSelectableAction {
        val companionStateId: StateId = "$id-outputState"

        override fun asActionNode(actionNodeId: ActionNodeId?): ActionNode =
            ActionNode.Simple(id = actionNodeId ?: Uuid.random().toString(), action = this)

        override fun generateIssues(project: Project): List<Issue> =
            buildList {
                listOf(minSelectableYear, maxSelectableYear)
                    .mapNotNull { it.value }
                    .flatMap { it.getAssignableProperties() }
                    .forEach {
                        it.let { value ->
                            if (value.transformedValueType(project) is ComposeFlowType.UnknownType) {
                                add(
                                    Issue.ResolvedToUnknownType(
                                        property = value,
                                        destination =
                                            NavigatableDestination.UiBuilderScreen(
                                                inspectorTabDestination = InspectorTabDestination.Action,
                                            ),
                                        issueContext = this@OpenDatePicker,
                                    ),
                                )
                            }
                        }
                    }
            }

        @Composable
        override fun SimplifiedContent(project: Project) {
            Column {
                Text(
                    stringResource(Res.string.open_date_picker),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        override fun companionState(project: Project): ScreenState<*> =
            ScreenState.InstantScreenState(
                id = companionStateId,
                name = outputStateName.value,
                userWritable = false, // This state can be only changed from the Open date picker action
            )

        override fun generateInitializationCodeBlock(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
            vararg additionalCodeBlocks: CodeBlockWrapper,
        ): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            val composableContext = context.getCurrentComposableContext()
            val dialogOpenVariableName =
                composableContext.addComposeFileVariable(
                    id = "$id-$dialogOpenVariableName",
                    dialogOpenVariableName,
                    dryRun = dryRun,
                )
            val rememberedDatePickerStateName =
                composableContext.addComposeFileVariable(
                    id = "$id-$rememberedDatePickerStateName",
                    initialIdentifier = rememberedDatePickerStateName,
                    dryRun = dryRun,
                )

            val state =
                project.findLocalStateOrNull(companionStateId) ?: return builder.build()
            if (state !is WriteableState) return builder.build()
            builder.add(
                generateRememberDatePickerState(
                    stateVariableName = rememberedDatePickerStateName,
                    project,
                    context,
                    dryRun = dryRun,
                ),
            )
            builder.addStatement(
                """
                var $dialogOpenVariableName by %M { %M(false) }
                if ($dialogOpenVariableName) {
                    %M(
                        onDismissRequest = {
                            $dialogOpenVariableName = false
                        },
                        confirmButton = {
                            %M {
                                %M {
                                    %M(
                                        onClick = {
                                            $dialogOpenVariableName = false
                                            $rememberedDatePickerStateName.selectedDateMillis?.let {
                                                ${composableContext.canvasEditable.viewModelName}.${
                    state.getUpdateMethodName(
                        context,
                    )
                }(%T.fromEpochMilliseconds(it))
                                            }
                                        },
                                        enabled = $rememberedDatePickerStateName.selectedDateMillis != null
                                    ) {
                                        %M(%M(%M.string.%M))
                                    }
                                    %M(%M.%M(8.dp))
                                }
                                %M(%M.%M(8.dp))
                            }
                        },
                        dismissButton = {
                            %M(
                                onClick = {
                                    $dialogOpenVariableName = false
                                }
                            ) {
                                %M(%M(%M.string.%M))
                            }
                        },
                    ) {
                        %M($rememberedDatePickerStateName)
                    }
                }
            """,
                MemberHolder.AndroidX.Runtime.remember,
                MemberHolder.AndroidX.Runtime.mutableStateOf,
                MemberHolder.Material3.DatePickerDialog,
                MemberHolder.AndroidX.Layout.Column,
                MemberHolder.AndroidX.Layout.Row,
                MemberHolder.Material3.OutlinedButton,
                Instant::class.asTypeNameWrapper(),
                MemberHolder.Material3.Text,
                MemberHolder.JetBrains.stringResource,
                MemberHolder.ComposeFlow.Res,
                MemberHolder.ComposeFlow.String.confirm,
                MemberHolder.AndroidX.Layout.Spacer,
                MemberHolder.AndroidX.Ui.Modifier,
                MemberHolder.AndroidX.Layout.size,
                MemberHolder.AndroidX.Layout.Spacer,
                MemberHolder.AndroidX.Ui.Modifier,
                MemberHolder.AndroidX.Layout.size,
                MemberHolder.Material3.TextButton,
                MemberHolder.Material3.Text,
                MemberHolder.JetBrains.stringResource,
                MemberHolder.ComposeFlow.Res,
                MemberHolder.ComposeFlow.String.cancel,
                MemberHolder.Material3.DatePicker,
            )
            return builder.build()
        }

        override fun generateActionTriggerCodeBlock(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            builder.addStatement("$dialogOpenVariableName = true")
            return builder.build()
        }
    }

    @Serializable
    @SerialName("OpenDateAndTimePicker")
    data class OpenDateAndTimePicker(
        override val id: ActionId = Uuid.random().toString(),
        @Serializable(MutableStateSerializer::class)
        override var minSelectableYear: MutableState<AssignableProperty?> = mutableStateOf(null),
        @Serializable(MutableStateSerializer::class)
        override var maxSelectableYear: MutableState<AssignableProperty?> = mutableStateOf(null),
        @Serializable(MutableStateSerializer::class)
        override var onlyPastDates: MutableState<Boolean> = mutableStateOf(false),
        @Serializable(MutableStateSerializer::class)
        override var onlyFutureDates: MutableState<Boolean> = mutableStateOf(false),
        @Transient
        override val name: String = "Date+Time picker",
        @Transient
        val dateDialogOpenVariableName: String = "openDatePicker",
        @Transient
        val datePickerStateName: String = "datePickerState",
        @Transient
        val timeDialogOpenVariableName: String = "openTimePicker",
        @Transient
        val timePickerStateName: String = "timePickerState",
        @Transient
        override val outputStateName: MutableState<String> = mutableStateOf("pickedDateTime"),
        @Transient
        val rememberedStateName: String = "dateTimePickerState",
    ) : DateOrTimePicker,
        YearRangeSelectableAction {
        val companionStateId: StateId = "$id-outputState"

        override fun asActionNode(actionNodeId: ActionNodeId?): ActionNode =
            ActionNode.Simple(id = actionNodeId ?: Uuid.random().toString(), action = this)

        override fun generateIssues(project: Project): List<Issue> =
            buildList {
                listOf(minSelectableYear, maxSelectableYear)
                    .mapNotNull { it.value }
                    .flatMap { it.getAssignableProperties() }
                    .forEach {
                        it.let { value ->
                            if (value.transformedValueType(project) is ComposeFlowType.UnknownType) {
                                add(
                                    Issue.ResolvedToUnknownType(
                                        property = value,
                                        destination =
                                            NavigatableDestination.UiBuilderScreen(
                                                inspectorTabDestination = InspectorTabDestination.Action,
                                            ),
                                        issueContext = this@OpenDateAndTimePicker,
                                    ),
                                )
                            }
                        }
                    }
            }

        @Composable
        override fun SimplifiedContent(project: Project) {
            Column {
                Text(
                    stringResource(Res.string.open_date_plus_time_picker),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        override fun companionState(project: Project): ScreenState<*> =
            ScreenState.InstantScreenState(
                id = companionStateId,
                name = outputStateName.value,
                userWritable = false, // This state can be only changed from the Open date picker action
            )

        override fun generateInitializationCodeBlock(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
            vararg additionalCodeBlocks: CodeBlockWrapper,
        ): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            val composableContext = context.getCurrentComposableContext()
            val dateDialogOpenVariableName =
                composableContext.addComposeFileVariable(
                    id = "$id-$dateDialogOpenVariableName",
                    initialIdentifier = dateDialogOpenVariableName,
                    dryRun = dryRun,
                )
            val timeDialogOpenVariableName =
                composableContext.addComposeFileVariable(
                    id = "$id-$timeDialogOpenVariableName",
                    initialIdentifier = timeDialogOpenVariableName,
                    dryRun = dryRun,
                )
            val timePickerStateName =
                composableContext.addComposeFileVariable(
                    id = "$id-$timePickerStateName",
                    initialIdentifier =
                    timePickerStateName,
                    dryRun = dryRun,
                )
            val rememberedStateName =
                composableContext.addComposeFileVariable(
                    id = "$id-$rememberedStateName",
                    initialIdentifier = rememberedStateName,
                    dryRun = dryRun,
                )

            val state =
                project.findLocalStateOrNull(companionStateId) ?: return builder.build()
            if (state !is WriteableState) return builder.build()

            builder.add(
                generateRememberDatePickerState(
                    stateVariableName = rememberedStateName,
                    project,
                    context,
                    dryRun = dryRun,
                ),
            )
            // Code to open DatePicker
            builder.addStatement(
                """
                val $timePickerStateName = %M()
                var $dateDialogOpenVariableName by %M { %M(false) }
                var $timeDialogOpenVariableName by %M { %M(false) }
                if ($dateDialogOpenVariableName) {
                    %M(
                        onDismissRequest = {
                            $dateDialogOpenVariableName = false
                        },
                        confirmButton = {
                            %M {
                                %M {
                                    %M(
                                        onClick = {
                                            $dateDialogOpenVariableName = false
                                            $timeDialogOpenVariableName = true
                                        },
                                        enabled = $rememberedStateName.selectedDateMillis != null
                                    ) {
                                        %M(%M(%M.string.%M))
                                    }
                                    %M(%M.%M(8.dp))
                                }
                                %M(%M.%M(8.dp))
                            }
                        },
                        dismissButton = {
                            %M(
                                onClick = {
                                    $dateDialogOpenVariableName = false
                                }
                            ) {
                                %M(%M(%M.string.%M))
                            }
                        },
                    ) {
                        %M($rememberedStateName)
                    }
                }
            """,
                MemberHolder.Material3.rememberTimePickerState,
                MemberHolder.AndroidX.Runtime.remember,
                MemberHolder.AndroidX.Runtime.mutableStateOf,
                MemberHolder.AndroidX.Runtime.remember,
                MemberHolder.AndroidX.Runtime.mutableStateOf,
                MemberHolder.Material3.DatePickerDialog,
                MemberHolder.AndroidX.Layout.Column,
                MemberHolder.AndroidX.Layout.Row,
                MemberHolder.Material3.OutlinedButton,
                MemberHolder.Material3.Text,
                MemberHolder.JetBrains.stringResource,
                MemberHolder.ComposeFlow.Res,
                MemberHolder.ComposeFlow.String.confirm,
                MemberHolder.AndroidX.Layout.Spacer,
                MemberHolder.AndroidX.Ui.Modifier,
                MemberHolder.AndroidX.Layout.size,
                MemberHolder.AndroidX.Layout.Spacer,
                MemberHolder.AndroidX.Ui.Modifier,
                MemberHolder.AndroidX.Layout.size,
                MemberHolder.Material3.TextButton,
                MemberHolder.Material3.Text,
                MemberHolder.JetBrains.stringResource,
                MemberHolder.ComposeFlow.Res,
                MemberHolder.ComposeFlow.String.cancel,
                MemberHolder.Material3.DatePicker,
            )

            // Code to open TimePicker
            builder.add(
                CodeBlockWrapper.of(
                    """
    if ($timeDialogOpenVariableName) {
        %M(
            onDismissRequest = {
                $timeDialogOpenVariableName = false
            }
        ) {
            %M(shape = %M.shapes.extraLarge) {
                %M(modifier = %M.%M(16.dp)) {
                    %M(
                        state = $timePickerStateName,
                        modifier = %M.align(%M.CenterHorizontally)
                    )
                    %M(
                        horizontalArrangement = %M.End
                    ) {
                        %M(%M.weight(1f))
                        %M(onClick = { $timeDialogOpenVariableName = false }) {
                            %M(%M(%M.string.%M))
                        }
                        %M(modifier = %M.%M(32.dp))
                        %M(onClick = {
                            $timeDialogOpenVariableName = false
                            $rememberedStateName.selectedDateMillis?.let {
                                ${composableContext.canvasEditable.viewModelName}.${
                        state.getUpdateMethodName(
                            context,
                        )
                    }(
                                    %T.fromEpochMilliseconds(it).%M(
                                        $timePickerStateName.hour
                                    ).%M(
                                        $timePickerStateName.minute
                                    )
                                )
                            }
                        }) {
                            %M(%M(%M.string.%M))
                        }
                    }
                }
            }
        }
    }
                """,
                    MemberHolder.AndroidX.Ui.Dialog,
                    MemberHolder.Material3.Card,
                    MemberHolder.Material3.MaterialTheme,
                    MemberHolder.AndroidX.Layout.Column,
                    MemberHolder.AndroidX.Ui.Modifier,
                    MemberHolder.AndroidX.Layout.padding,
                    MemberHolder.Material3.TimePicker,
                    MemberHolder.AndroidX.Ui.Modifier,
                    MemberHolder.AndroidX.Ui.Alignment,
                    MemberHolder.AndroidX.Layout.Row,
                    MemberHolder.AndroidX.Layout.Arrangement,
                    MemberHolder.AndroidX.Layout.Spacer,
                    MemberHolder.AndroidX.Ui.Modifier,
                    MemberHolder.Material3.TextButton,
                    MemberHolder.Material3.Text,
                    MemberHolder.JetBrains.stringResource,
                    MemberHolder.ComposeFlow.Res,
                    MemberHolder.ComposeFlow.String.cancel,
                    MemberHolder.AndroidX.Layout.Spacer,
                    MemberHolder.AndroidX.Ui.Modifier,
                    MemberHolder.AndroidX.Layout.width,
                    MemberHolder.Material3.OutlinedButton,
                    Instant::class.asTypeNameWrapper(),
                    MemberNameWrapper.get(
                        "${COMPOSEFLOW_PACKAGE}.util",
                        "setHour",
                        isExtension = true,
                    ),
                    MemberNameWrapper.get(
                        "${COMPOSEFLOW_PACKAGE}.util",
                        "setMinute",
                        isExtension = true,
                    ),
                    MemberHolder.Material3.Text,
                    MemberHolder.JetBrains.stringResource,
                    MemberHolder.ComposeFlow.Res,
                    MemberHolder.ComposeFlow.String.confirm,
                ),
            )

            return builder.build()
        }

        override fun generateActionTriggerCodeBlock(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            builder.addStatement("$dateDialogOpenVariableName = true")
            return builder.build()
        }
    }

    companion object {
        fun entries(): List<DateOrTimePicker> =
            listOf(
                OpenDatePicker(),
                OpenDateAndTimePicker(),
            )
    }
}

@Serializable
sealed interface Share : Action {
    @Serializable
    @SerialName("OpenUrl")
    data class OpenUrl(
        override val id: ActionId = Uuid.random().toString(),
        @Transient
        override val name: String = "Open URL",
        val url: AssignableProperty = StringProperty.StringIntrinsicValue(""),
        @Transient
        val uriHandlerName: String = "uriHandler",
    ) : Share {
        override fun getDependentComposeNodes(project: Project): List<ComposeNode> = url.getDependentComposeNodes(project)

        override fun generateIssues(project: Project): List<Issue> =
            buildList {
                listOf(url).flatMap { it.getAssignableProperties() }.forEach {
                    if (it.transformedValueType(project) is ComposeFlowType.UnknownType) {
                        add(
                            Issue.ResolvedToUnknownType(
                                property = it,
                                destination =
                                    NavigatableDestination.UiBuilderScreen(
                                        inspectorTabDestination = InspectorTabDestination.Action,
                                    ),
                                issueContext = this@OpenUrl,
                            ),
                        )
                    }
                }
            }

        @Composable
        override fun SimplifiedContent(project: Project) {
            Column {
                Text(
                    stringResource(Res.string.open_url),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    url.displayText(project),
                    style = MaterialTheme.typography.bodyMedium,
                    color =
                        if (url is IntrinsicProperty<*>) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            MaterialTheme.colorScheme.tertiary
                        },
                    modifier = Modifier.padding(top = 8.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        override fun generateInitializationCodeBlock(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
            vararg additionalCodeBlocks: CodeBlockWrapper,
        ): CodeBlockWrapper {
            context.getCurrentComposableContext().addCompositionLocalVariableEntryIfNotPresent(
                id = "$id-$uriHandlerName",
                initialIdentifier = uriHandlerName,
                MemberHolder.AndroidX.Platform.LocalUriHandler,
            )
            // Initialize the uriHandler once in the compose file instead of initializing it in
            // every action
            return CodeBlockWrapper.of("")
        }

        override fun generateActionTriggerCodeBlock(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper =
            CodeBlockWrapper.of(
                """$uriHandlerName.openUri(${
                    url.transformedCodeBlock(
                        project,
                        context,
                        dryRun = dryRun,
                    )
                })""",
            )

        override fun asActionNode(actionNodeId: ActionNodeId?): ActionNode =
            ActionNode.Simple(id = actionNodeId ?: Uuid.random().toString(), action = this)
    }

    companion object {
        fun entries(): List<Share> = listOf(OpenUrl())
    }
}

@Serializable
sealed interface Auth : Action {
    @Serializable
    @SerialName("SignInWithGoogle")
    data object SignInWithGoogle : Auth {
        override val id: ActionId = Uuid.random().toString()

        @Composable
        override fun SimplifiedContent(project: Project) {
            Column {
                Text(
                    stringResource(Res.string.sign_in_with),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    "Google",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        @Transient
        override val name: String = "Sign in with Google"

        override fun asActionNode(actionNodeId: ActionNodeId?): ActionNode =
            ActionNode.Simple(id = actionNodeId ?: Uuid.random().toString(), action = this)

        override fun generateActionTriggerCodeBlock(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper = CodeBlockWrapper.of("this@GoogleButtonUiContainerFirebase.onClick()")

        override fun generateWrapWithComposableBlock(insideContent: CodeBlockWrapper): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()

            // The signed in user is managed by dev.gitlive.firebase.Firebase.auth.
            // So onResult doesn't have to do anything
            builder.add(
                """
                %M(onResult = {}) {
            """,
                MemberNameWrapper.get(
                    "com.mmk.kmpauth.firebase.google",
                    "GoogleButtonUiContainerFirebase",
                ),
            )
            builder.add(insideContent)
            builder.addStatement("}")
            return builder.build()
        }
    }

    @Serializable
    @SerialName("SignInWithEmailAndPassword")
    data class SignInWithEmailAndPassword(
        override val id: ActionId = Uuid.random().toString(),
        val email: AssignableProperty = StringProperty.StringIntrinsicValue(""),
        val password: AssignableProperty = StringProperty.StringIntrinsicValue(""),
        @Transient
        val signInStateViewModelFunName: String = "onSignInWithEmailAndPassword",
        @Transient
        val signInStateName: String = "signInUserState",
        @Transient
        val snackbarOpenVariableName: String = "onShowSnackbar",
        @Transient
        val resetEventResultFunName: String = "resetSignInEventResultState",
    ) : Auth {
        override fun getDependentComposeNodes(project: Project): List<ComposeNode> =
            email.getDependentComposeNodes(project) +
                password.getDependentComposeNodes(project)

        override fun generateIssues(project: Project): List<Issue> =
            buildList {
                listOf(email, password).flatMap { it.getAssignableProperties() }.forEach {
                    if (it.transformedValueType(project) is ComposeFlowType.UnknownType) {
                        add(
                            Issue.ResolvedToUnknownType(
                                property = it,
                                destination =
                                    NavigatableDestination.UiBuilderScreen(
                                        inspectorTabDestination = InspectorTabDestination.Action,
                                    ),
                                issueContext = this@SignInWithEmailAndPassword,
                            ),
                        )
                    }
                }
            }

        @Composable
        override fun SimplifiedContent(project: Project) {
            Column {
                Text(
                    stringResource(Res.string.sign_in_with),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    stringResource(Res.string.email_and_password),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        @Transient
        override val name: String = "Sign in with email/password"

        override fun asActionNode(actionNodeId: ActionNodeId?): ActionNode =
            ActionNode.Simple(id = actionNodeId ?: Uuid.random().toString(), action = this)

        private fun generateCreateUserFlowProperties(): List<PropertySpecWrapper> {
            val backingProperty =
                PropertySpecWrapper
                    .builder(
                        "_$signInStateName",
                        ClassHolder.Coroutines.Flow.MutableStateFlow.parameterizedBy(
                            ClassHolder.ComposeFlow.EventResultState,
                        ),
                    ).addModifiers(KModifierWrapper.PRIVATE)
                    .initializer(
                        "%T(%T.NotStarted)",
                        ClassHolder.Coroutines.Flow.MutableStateFlow,
                        ClassHolder.ComposeFlow.EventResultState,
                    ).build()
            val property =
                PropertySpecWrapper
                    .builder(
                        signInStateName,
                        ClassHolder.Coroutines.Flow.StateFlow.parameterizedBy(
                            ClassHolder.ComposeFlow.EventResultState,
                        ),
                    ).initializer("_$signInStateName")
                    .build()
            return listOf(
                backingProperty,
                property,
            )
        }

        override fun generateInitializationCodeBlock(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
            vararg additionalCodeBlocks: CodeBlockWrapper,
        ): CodeBlockWrapper {
            val snackbarOpenVariableName =
                context.getCurrentComposableContext().addCompositionLocalVariableEntryIfNotPresent(
                    id = "${id}_$snackbarOpenVariableName",
                    snackbarOpenVariableName,
                    MemberHolder.ComposeFlow.LocalOnShowsnackbar,
                )
            val currentEditable = context.getCurrentComposableContext().canvasEditable

            val signInStateName =
                context
                    .getCurrentComposableContext()
                    .addComposeFileVariable(
                        id = "$id-$signInStateName",
                        initialIdentifier = signInStateName,
                        dryRun = dryRun,
                    )

            context.getCurrentComposableContext().addFunction(
                FunSpecWrapper
                    .builder(signInStateViewModelFunName)
                    .addParameter(
                        ParameterSpecWrapper
                            .builder(
                                "email",
                                String::class.asTypeNameWrapper(),
                            ).build(),
                    ).addParameter(
                        ParameterSpecWrapper
                            .builder(
                                "password",
                                String::class.asTypeNameWrapper(),
                            ).build(),
                    ).addCode(
                        """%M.%M {
            _$signInStateName.value = %T.Loading
            try {
                val authResult = %M.%M.signInWithEmailAndPassword(email, password)
                _$signInStateName.value = %T.Success(
                    message = "User signed in with email: ${'$'}{authResult.user?.email}")
            } catch (e: Exception) {
                _$signInStateName.value = %T.Error(e.message ?: "Unknown error")
            }
        }""",
                        MemberHolder.PreCompose.viewModelScope,
                        MemberHolder.Coroutines.launch,
                        ClassHolder.ComposeFlow.EventResultState,
                        MemberHolder.Firebase.Firebase,
                        MemberHolder.Firebase.auth,
                        ClassHolder.ComposeFlow.EventResultState,
                        ClassHolder.ComposeFlow.EventResultState,
                    ).build(),
                dryRun = dryRun,
            )

            context.getCurrentComposableContext().addFunction(
                FunSpecWrapper
                    .builder(resetEventResultFunName)
                    .addCode(
                        "_$signInStateName.value = %T.NotStarted",
                        ClassHolder.ComposeFlow.EventResultState,
                    ).build(),
                dryRun = dryRun,
            )

            generateCreateUserFlowProperties().forEach {
                context.getCurrentComposableContext().addProperty(it, dryRun = dryRun)
            }

            val builder = CodeBlockWrapper.builder()
            builder.add(
                """
    val $signInStateName by ${currentEditable.viewModelName}.$signInStateName.%M()
    when (val state = $signInStateName) {
        is %T.Error -> {
            ${ComposeScreenConstant.coroutineScope.name}.%M {
                $snackbarOpenVariableName(state.message, null)
                ${currentEditable.viewModelName}.$resetEventResultFunName()
            }
        }
        %T.Loading -> {}
        %T.NotStarted -> {}
        is %T.Success -> {
            ${ComposeScreenConstant.coroutineScope.name}.%M {
                $snackbarOpenVariableName(state.message, null)
                ${currentEditable.viewModelName}.$resetEventResultFunName()
            }
        }
    }
            """,
                MemberHolder.AndroidX.Runtime.collectAsState,
                ClassHolder.ComposeFlow.EventResultState,
                MemberHolder.Coroutines.launch,
                ClassHolder.ComposeFlow.EventResultState,
                ClassHolder.ComposeFlow.EventResultState,
                ClassHolder.ComposeFlow.EventResultState,
                MemberHolder.Coroutines.launch,
            )
            return builder.build()
        }

        override fun generateActionTriggerCodeBlock(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper {
            val currentEditable = context.getCurrentComposableContext().canvasEditable
            return CodeBlockWrapper.of(
                """${currentEditable.viewModelName}.$signInStateViewModelFunName(
                ${email.transformedCodeBlock(project, context, dryRun = dryRun)},
                ${password.transformedCodeBlock(project, context, dryRun = dryRun)})""",
            )
        }

        override fun generateWrapWithComposableBlock(insideContent: CodeBlockWrapper): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            builder.add(
                """
                if ($signInStateName == %T.Loading) {
                    %M(modifier = %M.%M(24.%M))
                } else {
            """,
                ClassHolder.ComposeFlow.EventResultState,
                MemberHolder.Material3.CircularProgressIndicator,
                MemberHolder.AndroidX.Ui.Modifier,
                MemberHolder.AndroidX.Layout.size,
                MemberHolder.AndroidX.Ui.dp,
            )
            builder.add(insideContent)
            builder.add(
                """
                }
            """,
            )
            return builder.build()
        }

        override fun hasSuspendFunction(): Boolean = true
    }

    @Serializable
    @SerialName("CreateUserWithEmailAndPassword")
    data class CreateUserWithEmailAndPassword(
        override val id: ActionId = Uuid.random().toString(),
        val email: AssignableProperty = StringProperty.StringIntrinsicValue(""),
        val password: AssignableProperty = StringProperty.StringIntrinsicValue(""),
        @Transient
        private val createStateViewModelFunName: String = "onCreateUserWithEmailAndPassword",
        @Transient
        private val createStateName: String = "createUserState",
        @Transient
        private val snackbarOpenVariableName: String = "onShowSnackbar",
        @Transient
        private val resetEventResultFunName: String = "resetCreateUserEventResultState",
    ) : Auth {
        override fun getDependentComposeNodes(project: Project): List<ComposeNode> =
            email.getDependentComposeNodes(project) +
                password.getDependentComposeNodes(project)

        override fun generateIssues(project: Project): List<Issue> =
            buildList {
                listOf(email, password).flatMap { it.getAssignableProperties() }.forEach {
                    if (it.transformedValueType(project) is ComposeFlowType.UnknownType) {
                        add(
                            Issue.ResolvedToUnknownType(
                                property = it,
                                destination =
                                    NavigatableDestination.UiBuilderScreen(
                                        inspectorTabDestination = InspectorTabDestination.Action,
                                    ),
                                issueContext = this@CreateUserWithEmailAndPassword,
                            ),
                        )
                    }
                }
            }

        @Composable
        override fun SimplifiedContent(project: Project) {
            Column {
                Text(
                    stringResource(Res.string.create_user_with),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    stringResource(Res.string.email_and_password),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        @Transient
        override val name: String = "Create user with email/password"

        override fun asActionNode(actionNodeId: ActionNodeId?): ActionNode =
            ActionNode.Simple(id = actionNodeId ?: Uuid.random().toString(), action = this)

        private fun generateCreateUserFlowProperties(): List<PropertySpecWrapper> {
            val backingProperty =
                PropertySpecWrapper
                    .builder(
                        "_$createStateName",
                        ClassHolder.Coroutines.Flow.MutableStateFlow.parameterizedBy(
                            ClassHolder.ComposeFlow.EventResultState,
                        ),
                    ).addModifiers(KModifierWrapper.PRIVATE)
                    .initializer(
                        "%T(%T.NotStarted)",
                        ClassHolder.Coroutines.Flow.MutableStateFlow,
                        ClassHolder.ComposeFlow.EventResultState,
                    ).build()
            val property =
                PropertySpecWrapper
                    .builder(
                        createStateName,
                        ClassHolder.Coroutines.Flow.StateFlow.parameterizedBy(
                            ClassHolder.ComposeFlow.EventResultState,
                        ),
                    ).initializer("_$createStateName")
                    .build()
            return listOf(
                backingProperty,
                property,
            )
        }

        override fun generateInitializationCodeBlock(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
            vararg additionalCodeBlocks: CodeBlockWrapper,
        ): CodeBlockWrapper {
            val snackbarOpenVariableName =
                context.getCurrentComposableContext().addCompositionLocalVariableEntryIfNotPresent(
                    id = "$id-$snackbarOpenVariableName",
                    snackbarOpenVariableName,
                    MemberHolder.ComposeFlow.LocalOnShowsnackbar,
                )
            val currentEditable = context.getCurrentComposableContext().canvasEditable

            val createStateName =
                context
                    .getCurrentComposableContext()
                    .addComposeFileVariable(
                        id = "$id-$createStateName",
                        createStateName,
                        dryRun = dryRun,
                    )

            context.getCurrentComposableContext().addFunction(
                FunSpecWrapper
                    .builder(createStateViewModelFunName)
                    .addParameter(
                        ParameterSpecWrapper
                            .builder(
                                "email",
                                String::class.asTypeNameWrapper(),
                            ).build(),
                    ).addParameter(
                        ParameterSpecWrapper
                            .builder(
                                "password",
                                String::class.asTypeNameWrapper(),
                            ).build(),
                    ).addCode(
                        """%M.%M {
            _$createStateName.value = %T.Loading
            try {
                val authResult = %M.%M.createUserWithEmailAndPassword(email, password)
                _$createStateName.value = %T.Success(
                    message = "Created user with email: ${'$'}{authResult.user?.email}")
            } catch (e: Exception) {
                _$createStateName.value = %T.Error(e.message ?: "Unknown error")
            }
        }""",
                        MemberHolder.PreCompose.viewModelScope,
                        MemberHolder.Coroutines.launch,
                        ClassHolder.ComposeFlow.EventResultState,
                        MemberHolder.Firebase.Firebase,
                        MemberHolder.Firebase.auth,
                        ClassHolder.ComposeFlow.EventResultState,
                        ClassHolder.ComposeFlow.EventResultState,
                    ).build(),
                dryRun = dryRun,
            )

            context.getCurrentComposableContext().addFunction(
                FunSpecWrapper
                    .builder(resetEventResultFunName)
                    .addCode(
                        "_$createStateName.value = %T.NotStarted",
                        ClassHolder.ComposeFlow.EventResultState,
                    ).build(),
                dryRun = dryRun,
            )

            generateCreateUserFlowProperties().forEach {
                context.getCurrentComposableContext().addProperty(it, dryRun = dryRun)
            }

            val builder = CodeBlockWrapper.builder()
            builder.add(
                """
    val $createStateName by ${currentEditable.viewModelName}.$createStateName.%M()
    when (val state = $createStateName) {
        is %T.Error -> {
            ${ComposeScreenConstant.coroutineScope.name}.%M {
                $snackbarOpenVariableName(state.message, null)
                ${currentEditable.viewModelName}.$resetEventResultFunName()
            }
        }
        %T.Loading -> {}
        %T.NotStarted -> {}
        is %T.Success -> {
            ${ComposeScreenConstant.coroutineScope.name}.%M {
                $snackbarOpenVariableName(state.message, null)
                ${currentEditable.viewModelName}.$resetEventResultFunName()
            }
        }
    }
            """,
                MemberHolder.AndroidX.Runtime.collectAsState,
                ClassHolder.ComposeFlow.EventResultState,
                MemberHolder.Coroutines.launch,
                ClassHolder.ComposeFlow.EventResultState,
                ClassHolder.ComposeFlow.EventResultState,
                ClassHolder.ComposeFlow.EventResultState,
                MemberHolder.Coroutines.launch,
            )
            return builder.build()
        }

        override fun generateActionTriggerCodeBlock(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper {
            val currentEditable = context.getCurrentComposableContext().canvasEditable
            return CodeBlockWrapper.of(
                """${currentEditable.viewModelName}.$createStateViewModelFunName(
                ${email.transformedCodeBlock(project, context, dryRun = dryRun)},
                ${password.transformedCodeBlock(project, context, dryRun = dryRun)})""",
            )
        }

        override fun generateWrapWithComposableBlock(insideContent: CodeBlockWrapper): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            builder.add(
                """
                if ($createStateName == %T.Loading) {
                    %M(modifier = %M.%M(24.%M))
                } else {
            """,
                ClassHolder.ComposeFlow.EventResultState,
                MemberHolder.Material3.CircularProgressIndicator,
                MemberHolder.AndroidX.Ui.Modifier,
                MemberHolder.AndroidX.Layout.size,
                MemberHolder.AndroidX.Ui.dp,
            )
            builder.add(insideContent)
            builder.add(
                """
                }
            """,
            )
            return builder.build()
        }

        override fun hasSuspendFunction(): Boolean = true
    }

    @Serializable
    @SerialName("SignOut")
    data object SignOut : Auth {
        override val id: ActionId = Uuid.random().toString()

        @Composable
        override fun SimplifiedContent(project: Project) {
            Column {
                Text(
                    stringResource(Res.string.sign_out),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        @Transient
        override val name: String = "Sign out"

        override fun asActionNode(actionNodeId: ActionNodeId?): ActionNode =
            ActionNode.Simple(id = actionNodeId ?: Uuid.random().toString(), action = this)

        override fun generateActionTriggerCodeBlock(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper =
            CodeBlockWrapper.of(
                """${ComposeScreenConstant.coroutineScope.name}.%M {
            %M.%M.signOut()
        }
        """,
                MemberHolder.Coroutines.launch,
                MemberHolder.Firebase.Firebase,
                MemberHolder.Firebase.auth,
            )

        override fun hasSuspendFunction(): Boolean = true
    }

    companion object {
        fun entries(): List<Auth> =
            listOf(
                SignInWithGoogle,
                SignInWithEmailAndPassword(),
                CreateUserWithEmailAndPassword(),
                SignOut,
            )
    }
}

@Serializable
sealed interface FirestoreAction : Action {
    fun generateIssues(
        project: Project,
        collectionId: CollectionId?,
        dataFieldUpdateProperties: MutableList<DataFieldUpdateProperty>,
    ): List<Issue> {
        val firestoreCollection =
            collectionId?.let { project.findFirestoreCollectionOrNull(it) }
        val dataType =
            firestoreCollection?.dataTypeId?.let { project.findDataTypeOrNull(it) }
                ?: return emptyList()
        return buildList {
            dataFieldUpdateProperties.forEach { entry ->
                val dataField = dataType.fields.firstOrNull { it.id == entry.dataFieldId }
                dataField?.let {
                    val transformedType =
                        entry.assignableProperty.transformedValueType(project)
                    if (!dataField.fieldType.type().isAbleToAssign(transformedType)) {
                        add(
                            Issue.ResolvedToTypeNotAssignable(
                                property = entry.assignableProperty,
                                acceptableType = dataField.fieldType.type(),
                                destination =
                                    NavigatableDestination.UiBuilderScreen(
                                        inspectorTabDestination = InspectorTabDestination.Action,
                                    ),
                                issueContext = this@FirestoreAction,
                            ),
                        )
                    }

                    entry.assignableProperty.getAssignableProperties().forEach { property ->
                        val transformedValueType =
                            property.transformedValueType(project)
                        if (transformedValueType is ComposeFlowType.UnknownType) {
                            add(
                                Issue.ResolvedToUnknownType(
                                    property = property,
                                    destination =
                                        NavigatableDestination.UiBuilderScreen(
                                            inspectorTabDestination = InspectorTabDestination.Action,
                                        ),
                                    issueContext = this@FirestoreAction,
                                ),
                            )
                        }
                    }
                }
            }
        }
    }

    @Serializable
    @SerialName("SaveToFirestore")
    data class SaveToFirestore(
        override val id: ActionId = Uuid.random().toString(),
        val collectionId: CollectionId? = null,
        val dataFieldUpdateProperties: MutableList<DataFieldUpdateProperty> = mutableListOf(),
        @Transient
        override val name: String = "Save to Firestore",
        @Transient
        private val updateMethodName: String = "onSaveToFirestore",
    ) : FirestoreAction {
        @Composable
        override fun SimplifiedContent(project: Project) {
            Column {
                Text(
                    stringResource(Res.string.firestore_save_to_firestore),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                val collection = collectionId?.let { project.findFirestoreCollectionOrNull(it) }
                Text(
                    collection?.name ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        override fun generateIssues(project: Project): List<Issue> =
            generateIssues(
                project = project,
                collectionId = collectionId,
                dataFieldUpdateProperties = dataFieldUpdateProperties,
            )

        override fun addUpdateMethodAndReadProperty(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ) {
            val firestoreCollection =
                collectionId?.let { project.findFirestoreCollectionOrNull(it) } ?: return
            val dataType =
                firestoreCollection.dataTypeId?.let { project.findDataTypeOrNull(it) } ?: return

            val composableContext = context.getCurrentComposableContext()
            val updateMethodName =
                composableContext.generateUniqueFunName(
                    id = "$id-$updateMethodName",
                    initial = "onSave${dataType.className}ToFirestore",
                )

            val funSpecBuilder = FunSpecWrapper.builder(updateMethodName)

            context
                .getCurrentComposableContext()
                .addDependency(viewModelConstant = ViewModelConstant.firestore)
            val updateString =
                buildString {
                    dataFieldUpdateProperties.forEach { (dataFieldId, readProperty) ->
                        val dataField = dataType.findDataFieldOrNull(dataFieldId)
                        dataField?.let {
                            readProperty
                                .transformedCodeBlock(project, context, dryRun = dryRun)
                                .let { codeBlock ->
                                    val expression =
                                        dataField.fieldType.type().convertCodeFromType(
                                            inputType = readProperty.valueType(project),
                                            codeBlock = codeBlock,
                                        )
                                    // TODO: Stop converting CodeBlockWrapper to code by using toString().
                                    //       https://github.com/ComposeFlow/ComposeFlow/issues/192
                                    append("${dataField.variableName} = $expression,\n")
                                }
                        }

                        readProperty.generateParameterSpec(project)?.let {
                            funSpecBuilder.addParameter(it)
                        }
                    }
                }

            funSpecBuilder.addCode(
                """val document = ${ViewModelConstant.firestore}.collection("${firestoreCollection.name}").document
                val updated = %T($updateString)
                %M.%M {
                    document.set(updated)
                }
            """,
                dataType.asKotlinPoetClassName(project),
                MemberHolder.PreCompose.viewModelScope,
                MemberHolder.Coroutines.launch,
            )
            context.addFunction(funSpecBuilder.build(), dryRun = dryRun)
        }

        override fun generateActionTriggerCodeBlock(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            val firestoreCollection =
                collectionId?.let { project.findFirestoreCollectionOrNull(it) }
                    ?: return builder.build()
            val dataType =
                firestoreCollection.dataTypeId?.let { project.findDataTypeOrNull(it) }
                    ?: return builder.build()

            val paramString =
                buildString {
                    dataFieldUpdateProperties.forEach { (_, readProperty) ->
                        readProperty.generateParameterSpec(project)?.let {
                            append(
                                "${it.name} = ${
                                    readProperty.transformedCodeBlock(
                                        project,
                                        context,
                                        dryRun = dryRun,
                                    )
                                },",
                            )
                        }
                    }
                }
            val updateMethodName =
                context.getCurrentComposableContext().generateUniqueFunName(
                    id = "$id-$updateMethodName",
                    initial = "onSave${dataType.className}ToFirestore",
                )
            val currentEditable = context.getCurrentComposableContext().canvasEditable
            builder.add("""${currentEditable.viewModelName}.$updateMethodName($paramString)""")
            return builder.build()
        }

        override fun getDependentComposeNodes(project: Project): List<ComposeNode> =
            dataFieldUpdateProperties.flatMap {
                it.assignableProperty.getDependentComposeNodes(
                    project,
                )
            }

        override fun isDependent(sourceId: String): Boolean =
            dataFieldUpdateProperties.any {
                it.assignableProperty.isDependent(
                    sourceId,
                )
            }

        override fun asActionNode(actionNodeId: ActionNodeId?): ActionNode =
            ActionNode.Simple(id = actionNodeId ?: Uuid.random().toString(), action = this)
    }

    @Serializable
    @SerialName("UpdateDocument")
    data class UpdateDocument(
        override val id: ActionId = Uuid.random().toString(),
        val collectionId: CollectionId? = null,
        val dataFieldUpdateProperties: MutableList<DataFieldUpdateProperty> = mutableListOf(),
        val filterExpression: FilterExpression? = SingleFilter(),
        @Transient
        override val name: String = "Update document",
        @Transient
        private val updateMethodName: String = "onUpdateDocument",
    ) : FirestoreAction {
        override fun generateIssues(project: Project): List<Issue> {
            val parameterIssues =
                generateIssues(
                    project = project,
                    collectionId = collectionId,
                    dataFieldUpdateProperties = dataFieldUpdateProperties,
                )
            val filterIssues =
                buildList {
                    filterExpression?.getAssignableProperties()?.forEach {
                        val transformedValueType = it.transformedValueType(project)
                        if (transformedValueType is ComposeFlowType.UnknownType) {
                            add(
                                Issue.ResolvedToUnknownType(
                                    property = it,
                                    destination =
                                        NavigatableDestination.UiBuilderScreen(
                                            inspectorTabDestination = InspectorTabDestination.Action,
                                        ),
                                    issueContext = this@UpdateDocument,
                                ),
                            )
                        }
                    }
                }
            return parameterIssues + filterIssues
        }

        @Composable
        override fun SimplifiedContent(project: Project) {
            Column {
                Text(
                    stringResource(Res.string.firestore_update_document),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                val collection = collectionId?.let { project.findFirestoreCollectionOrNull(it) }
                Text(
                    collection?.name ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        override fun addUpdateMethodAndReadProperty(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ) {
            val firestoreCollection =
                collectionId?.let { project.findFirestoreCollectionOrNull(it) } ?: return
            val dataType =
                firestoreCollection.dataTypeId?.let { project.findDataTypeOrNull(it) } ?: return
            val composableContext = context.getCurrentComposableContext()

            val updateMethodName =
                composableContext.generateUniqueFunName(
                    id = "$id-$updateMethodName",
                    initial = "onUpdate${dataType.className}Document",
                )
            val funSpecBuilder = FunSpecWrapper.builder(updateMethodName)

            context
                .getCurrentComposableContext()
                .addDependency(viewModelConstant = ViewModelConstant.firestore)

            filterExpression?.getAssignableProperties()?.forEach {
                it.generateParameterSpec(project)?.let { param ->
                    funSpecBuilder.addParameter(param)
                }
            }

            val updateString =
                buildString {
                    dataFieldUpdateProperties.forEach { (dataFieldId, readProperty) ->
                        val dataField = dataType.findDataFieldOrNull(dataFieldId)
                        dataField?.let {
                            readProperty
                                .transformedCodeBlock(project, context, dryRun = dryRun)
                                .let { codeBlock ->
                                    val expression =
                                        dataField.fieldType.type().convertCodeFromType(
                                            inputType = readProperty.valueType(project),
                                            codeBlock = codeBlock,
                                        )
                                    // TODO: Stop converting CodeBlockWrapper to code by using toString().
                                    //       https://github.com/ComposeFlow/ComposeFlow/issues/192
                                    append("${dataField.variableName} = $expression,\n")
                                }
                        }

                        readProperty.generateParameterSpec(project)?.let {
                            funSpecBuilder.addParameter(it)
                        }
                    }
                }

            funSpecBuilder.addCode(
                """val collection = ${ViewModelConstant.firestore}.collection("${firestoreCollection.name}")""",
            )
            filterExpression?.let {
                funSpecBuilder.addStatement(".where {")
                funSpecBuilder.addCode(it.generateCodeBlock(project, context, dryRun = dryRun))
                funSpecBuilder.addStatement("}")

                funSpecBuilder.addCode(
                    CodeBlockWrapper.of(
                        """%M.%M {
                    collection.snapshots.%M().documents.forEach { document ->
                        val entity = document.data(%T.serializer())
                        document.reference.set(entity.copy($updateString))
                    }
                }
                """,
                        MemberHolder.PreCompose.viewModelScope,
                        MemberHolder.Coroutines.launch,
                        MemberHolder.Coroutines.Flow.first,
                        dataType.asKotlinPoetClassName(project),
                    ),
                )
            }
            context.addFunction(funSpecBuilder.build(), dryRun = dryRun)
        }

        override fun generateActionTriggerCodeBlock(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            val firestoreCollection =
                collectionId?.let { project.findFirestoreCollectionOrNull(it) }
            firestoreCollection?.dataTypeId?.let { project.findDataTypeOrNull(it) }
                ?: return builder.build()

            val paramStringFromFilter =
                buildString {
                    filterExpression?.getAssignableProperties()?.forEach { readProperty ->
                        readProperty.generateParameterSpec(project)?.let {
                            append(
                                "${it.name} = ${
                                    readProperty.transformedCodeBlock(
                                        project,
                                        context,
                                        dryRun = dryRun,
                                    )
                                },",
                            )
                        }
                    }
                }
            val paramStringFromUpdateProperties =
                buildString {
                    dataFieldUpdateProperties.forEach { (_, readProperty) ->
                        readProperty.generateParameterSpec(project)?.let {
                            append(
                                "${it.name} = ${
                                    readProperty.transformedCodeBlock(
                                        project,
                                        context,
                                        dryRun = dryRun,
                                    )
                                },",
                            )
                        }
                    }
                }
            val currentEditable = context.getCurrentComposableContext().canvasEditable
            builder.add("""${currentEditable.viewModelName}.$updateMethodName(${paramStringFromFilter}$paramStringFromUpdateProperties)""")
            return builder.build()
        }

        override fun getDependentComposeNodes(project: Project): List<ComposeNode> {
            val nodesFromFilter =
                filterExpression?.getAssignableProperties()?.flatMap {
                    it.getDependentComposeNodes(
                        project,
                    )
                } ?: emptyList()

            val nodesFromUpdate =
                dataFieldUpdateProperties.flatMap {
                    it.assignableProperty.getDependentComposeNodes(
                        project,
                    )
                }
            return nodesFromUpdate + nodesFromFilter
        }

        override fun isDependent(sourceId: String): Boolean {
            val dependsOnFilter =
                filterExpression?.getAssignableProperties()?.any {
                    it.isDependent(
                        sourceId,
                    )
                } == true
            val dependsOnUpdateProperties =
                dataFieldUpdateProperties.any {
                    it.assignableProperty.isDependent(
                        sourceId,
                    )
                }
            return dependsOnFilter || dependsOnUpdateProperties
        }

        override fun asActionNode(actionNodeId: ActionNodeId?): ActionNode =
            ActionNode.Simple(id = actionNodeId ?: Uuid.random().toString(), action = this)
    }

    @Serializable
    @SerialName("DeleteDocument")
    data class DeleteDocument(
        override val id: ActionId = Uuid.random().toString(),
        val collectionId: CollectionId? = null,
        val filterExpression: FilterExpression? = SingleFilter(),
        @Transient
        override val name: String = "Delete document",
        @Transient
        private val updateMethodName: String = "onDeleteDocument",
    ) : FirestoreAction {
        override fun generateIssues(project: Project): List<Issue> {
            val filterIssues =
                buildList {
                    filterExpression?.getAssignableProperties()?.forEach {
                        val transformedValueType = it.transformedValueType(project)
                        if (transformedValueType is ComposeFlowType.UnknownType) {
                            add(
                                Issue.ResolvedToUnknownType(
                                    property = it,
                                    destination =
                                        NavigatableDestination.UiBuilderScreen(
                                            inspectorTabDestination = InspectorTabDestination.Action,
                                        ),
                                    issueContext = this@DeleteDocument,
                                ),
                            )
                        }
                    }
                }
            return filterIssues
        }

        @Composable
        override fun SimplifiedContent(project: Project) {
            Column {
                Text(
                    stringResource(Res.string.firestore_delete_document),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                val collection = collectionId?.let { project.findFirestoreCollectionOrNull(it) }
                Text(
                    collection?.name ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        override fun addUpdateMethodAndReadProperty(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ) {
            val firestoreCollection =
                collectionId?.let { project.findFirestoreCollectionOrNull(it) } ?: return
            val dataType =
                firestoreCollection.dataTypeId?.let { project.findDataTypeOrNull(it) } ?: return

            val updateMethodName =
                context.getCurrentComposableContext().generateUniqueFunName(
                    id = "$id-$updateMethodName",
                    initial = "onDelete${dataType.className}Document",
                )
            val funSpecBuilder = FunSpecWrapper.builder(updateMethodName)

            context
                .getCurrentComposableContext()
                .addDependency(viewModelConstant = ViewModelConstant.firestore)

            filterExpression?.getAssignableProperties()?.forEach {
                it.generateParameterSpec(project)?.let { param ->
                    funSpecBuilder.addParameter(param)
                }
            }

            funSpecBuilder.addCode(
                """val collection = ${ViewModelConstant.firestore}.collection("${firestoreCollection.name}")""",
            )
            filterExpression?.let {
                funSpecBuilder.addStatement(".where {")
                funSpecBuilder.addCode(
                    it.generateCodeBlock(project, context, dryRun = dryRun),
                )
                funSpecBuilder.addStatement("}")

                funSpecBuilder.addCode(
                    CodeBlockWrapper.of(
                        """%M.%M {
                    collection.snapshots.%M().documents.forEach { document ->
                        document.reference.delete()
                    }
                }
                """,
                        MemberHolder.PreCompose.viewModelScope,
                        MemberHolder.Coroutines.launch,
                        MemberHolder.Coroutines.Flow.first,
                    ),
                )
            }
            context.addFunction(funSpecBuilder.build(), dryRun = dryRun)
        }

        override fun generateActionTriggerCodeBlock(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper {
            val firestoreCollection =
                collectionId?.let { project.findFirestoreCollectionOrNull(it) }
            val dataType =
                firestoreCollection?.dataTypeId?.let { project.findDataTypeOrNull(it) }
                    ?: return CodeBlockWrapper.of("")

            val paramString =
                buildString {
                    filterExpression?.getAssignableProperties()?.forEach { readProperty ->
                        readProperty.generateParameterSpec(project)?.let {
                            append(
                                "${it.name} = ${
                                    readProperty.transformedCodeBlock(
                                        project,
                                        context,
                                        dryRun = dryRun,
                                    )
                                },",
                            )
                        }
                    }
                }
            val builder = CodeBlockWrapper.builder()
            val currentEditable = context.getCurrentComposableContext().canvasEditable
            val updateMethodName =
                context.getCurrentComposableContext().generateUniqueFunName(
                    id = "$id-$updateMethodName",
                    initial = "onDelete${dataType.className}Document",
                )
            builder.add("""${currentEditable.viewModelName}.$updateMethodName($paramString)""")
            return builder.build()
        }

        override fun getDependentComposeNodes(project: Project): List<ComposeNode> =
            filterExpression?.getAssignableProperties()?.flatMap {
                it.getDependentComposeNodes(
                    project,
                )
            } ?: emptyList()

        override fun isDependent(sourceId: String): Boolean =
            filterExpression?.getAssignableProperties()?.any {
                it.isDependent(
                    sourceId,
                )
            } == true

        override fun asActionNode(actionNodeId: ActionNodeId?): ActionNode =
            ActionNode.Simple(id = actionNodeId ?: Uuid.random().toString(), action = this)
    }

    companion object {
        fun entries(): List<FirestoreAction> =
            listOf(
                SaveToFirestore(),
                UpdateDocument(),
                DeleteDocument(),
            )
    }
}

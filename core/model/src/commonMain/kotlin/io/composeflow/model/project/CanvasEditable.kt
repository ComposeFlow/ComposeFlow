package io.composeflow.model.project

import androidx.compose.ui.geometry.Offset
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import io.composeflow.ON_SCREEN_INITIALLY_LOADED
import io.composeflow.SCREEN_INITIALLY_LOADED_FLAG
import io.composeflow.ViewModelConstant
import io.composeflow.formatter.suppressRedundantVisibilityModifier
import io.composeflow.kotlinpoet.ComposeEditableContext
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.model.action.Action
import io.composeflow.model.action.ActionNode
import io.composeflow.model.action.ActionType
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.state.AppState
import io.composeflow.model.state.AuthenticatedUserState
import io.composeflow.model.state.ReadableState
import io.composeflow.model.state.ScreenState
import io.composeflow.model.state.StateHolder
import io.composeflow.util.generateUniqueName
import io.composeflow.util.toKotlinFileName
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import moe.tlaster.precompose.viewmodel.ViewModel
import org.koin.core.component.KoinComponent

/**
 * Represents that classes that implement this interface are editable in the canvas in the
 * UI builder
 */
interface CanvasEditable : StateHolder {
    val id: String

    /**
     * Returns the root node (including a ComposeNode where paletteNode == Screen)
     */
    fun getRootNode(): ComposeNode

    /**
     * Returns the content root (not including a ComposeNode where paletteNode == Screen).
     * It may return a same ComposeNode depending on the implementation. For example,
     * Component returns the same node.
     */
    fun getContentRootNode(): ComposeNode

    fun findFocusedNodes(): List<ComposeNode>

    fun findNodeById(id: String): ComposeNode?

    fun clearIsHoveredRecursively()

    fun clearIsFocusedRecursively()

    fun clearIsDraggedOnBoundsRecursively()

    fun clearIndexToBeDroppedRecursively()

    /**
     * Update the focused node.
     *
     * if [isCtrlOrMetaPressed] is passed as true, it doesn't clear the existing focused nodes.
     */
    fun updateFocusedNode(
        eventPosition: Offset,
        isCtrlOrMetaPressed: Boolean = false,
    )

    fun updateHoveredNode(eventPosition: Offset)

    fun findDeepestChildAtOrNull(position: Offset): ComposeNode?

    fun getAllComposeNodes(): List<ComposeNode>

    fun newComposableContext(): ComposeEditableContext =
        ComposeEditableContext(
            typeSpecBuilder =
                TypeSpec
                    .classBuilder(viewModelFileName)
                    .superclass(ViewModel::class)
                    .addSuperinterface(KoinComponent::class),
            canvasEditable = this,
        )

    fun getPackageName(project: Project): String

    val name: String
    val viewModelFileName: String
    val viewModelName: String
    val composableName: String

    val parameters: MutableList<ParameterWrapper<*>>

    /**
     * Generates the file spec for the ComposeScreen file.
     *
     * @param dryRun if set to true, the method is executed only to construct the related
     * dependencies and generated file spec will not be used.
     */
    fun generateComposeScreenFileSpec(
        project: Project,
        context: GenerationContext,
        dryRun: Boolean = false,
    ): FileSpec

    fun getAllActions(project: Project): List<Action> =
        getAllComposeNodes()
            .flatMap {
                it.allActions()
            }.sortedBy { it.argumentName(project) }

    fun getAllActionNodes(): List<ActionNode> =
        getAllComposeNodes().flatMap {
            it.allActionNodes()
        }

    fun hasComposeNode(node: ComposeNode): Boolean = getAllComposeNodes().any { it.fallbackId == node.fallbackId }

    fun generateViewModelFileSpec(
        project: Project,
        context: GenerationContext,
        dryRun: Boolean = false,
    ): FileSpec {
        val fileBuilder =
            FileSpec.builder(getPackageName(project), viewModelFileName.toKotlinFileName())

        var dependsOnAppState = false
        var dependsOnListAppState = false
        var dependsOnCustomDataTypeAppState = false
        getStates(project).forEach { state ->
            when (state) {
                is ScreenState<*> -> {
                    state.generateStatePropertiesToViewModel(project, context).forEach {
                        context.addProperty(it, dryRun)
                    }
                    context.addFunction(state.generateUpdateStateMethodToViewModel(context), dryRun)
                }

                is AppState<*> -> {
                    if (isDependentOn(project, state)) {
                        dependsOnAppState = true
                        if (state.isList) {
                            dependsOnListAppState = true
                        }
                        if (state is AppState.CustomDataTypeAppState) {
                            dependsOnCustomDataTypeAppState = true
                        }
                        state.generateStatePropertiesToViewModel(project, context).forEach {
                            context.addProperty(it, dryRun)
                        }
                    }
                }

                is AuthenticatedUserState -> {}
            }
        }

        if (getRootNode().actionsMap[ActionType.OnInitialLoad]?.isNotEmpty() == true) {
            val allProperties = context.getCurrentComposableContext().allProperties()
            val backingFieldName =
                generateUniqueName(
                    initial = "_$SCREEN_INITIALLY_LOADED_FLAG",
                    existing = allProperties.map { it.name }.toSet(),
                )
            val fieldName =
                generateUniqueName(
                    initial = SCREEN_INITIALLY_LOADED_FLAG,
                    existing = allProperties.map { it.name }.toSet(),
                )
            context.addProperty(
                PropertySpec
                    .builder(
                        name = backingFieldName,
                        MutableStateFlow::class.parameterizedBy(Boolean::class),
                    ).initializer("%T(false)", MutableStateFlow::class)
                    .build(),
                dryRun = dryRun,
            )
            context.addProperty(
                PropertySpec
                    .builder(
                        name = fieldName,
                        StateFlow::class.parameterizedBy(Boolean::class),
                    ).initializer(backingFieldName)
                    .build(),
                dryRun = dryRun,
            )
            context.addFunction(
                FunSpec
                    .builder(ON_SCREEN_INITIALLY_LOADED)
                    .addCode("$backingFieldName.value = true")
                    .build(),
                dryRun = dryRun,
            )
        }

        if (dependsOnAppState) {
            context.addPrioritizedProperty(
                ViewModelConstant.flowSettings.generateProperty(),
                dryRun = dryRun,
            )
        }
        if (dependsOnListAppState || dependsOnCustomDataTypeAppState) {
            context.addPrioritizedProperty(
                ViewModelConstant.jsonSerializer.generateProperty(),
                dryRun = dryRun,
            )
        }
        if (context.getCurrentComposableContext().dependencies.contains(ViewModelConstant.firestore)) {
            // TODO: Different logic other than other enum entries
            context.addPrioritizedProperty(
                ViewModelConstant.firestore.generateProperty(),
                dryRun = dryRun,
            )
        }

        getAllComposeNodes().forEach { node ->
            node.actionHandler.allActions().forEach {
                it.addUpdateMethodAndReadProperty(
                    project,
                    context = context,
                    dryRun = dryRun,
                )
            }
        }

        project.apiHolder.getValidApiDefinitions().forEach { apiDefinition ->
            if (getRootNode().isDependent(apiDefinition.id)) {
                apiDefinition.generateApiResultFlowProperties().forEach {
                    context.addProperty(it, dryRun)
                }
                context.addFunctionInConstructor(
                    apiDefinition.generateInitApiResultInViewModelFunSpec(),
                    dryRun,
                )
                context.addFunction(apiDefinition.generateUpdateApiResultFunSpec(), dryRun)
                context.addFunction(apiDefinition.generateApiResultFunSpec(), dryRun)
                context.addFunction(apiDefinition.generateCallApiFunSpec(), dryRun)
            }
        }
        project.firebaseAppInfoHolder.firebaseAppInfo.firestoreCollections.forEach { firestoreCollection ->
            if (getRootNode().isDependent(firestoreCollection.id)) {
                firestoreCollection.generateStatePropertiesToViewModel(project).forEach {
                    context.addProperty(it, dryRun)
                }
            }
        }

        fileBuilder.addType(context.getCurrentComposableContext().buildTypeSpec())
        fileBuilder.suppressRedundantVisibilityModifier()
        return fileBuilder.build()
    }

    private fun anyActionHasWriteDependency(
        project: Project,
        readableState: ReadableState,
    ): Boolean = anyActionsDependOn(project = project, readableState = readableState)

    private fun anyActionHasReadDependency(
        project: Project,
        readableState: ReadableState,
    ): Boolean = anyActionsDependOn(project = project, readableState = readableState)

    private fun anyActionsDependOn(
        project: Project,
        readableState: ReadableState,
    ): Boolean =
        getAllComposeNodes().any { node ->
            node.actionHandler.allActions().any { action ->
                action.isDependent(readableState.id)
            }
        }

    private fun anyNodeHasReadDependency(
        project: Project,
        readableState: ReadableState,
    ): Boolean =
        getAllComposeNodes().any { node ->
            node.isDependent(readableState.id)
        }

    fun isDependentOn(
        project: Project,
        readableState: ReadableState,
    ): Boolean =
        anyActionHasWriteDependency(project, readableState) ||
            anyActionHasReadDependency(
                project,
                readableState,
            ) ||
            anyNodeHasReadDependency(project, readableState)
}

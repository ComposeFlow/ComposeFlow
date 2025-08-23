@file:OptIn(kotlin.time.ExperimentalTime::class)

package io.composeflow.model.project

import io.composeflow.cloud.storage.BlobInfoWrapper
import io.composeflow.kotlinpoet.FileSpecWithDirectory
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.model.apieditor.ApiDefinition
import io.composeflow.model.apieditor.ApiId
import io.composeflow.model.datatype.DataType
import io.composeflow.model.project.api.ApiHolder
import io.composeflow.model.project.appassets.AppAssetHolder
import io.composeflow.model.project.appassets.copyContents
import io.composeflow.model.project.appscreen.ScreenHolder
import io.composeflow.model.project.appscreen.screen.Screen
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.project.asset.AssetHolder
import io.composeflow.model.project.component.Component
import io.composeflow.model.project.component.ComponentHolder
import io.composeflow.model.project.component.ComponentId
import io.composeflow.model.project.custom_enum.CustomEnum
import io.composeflow.model.project.custom_enum.CustomEnumHolder
import io.composeflow.model.project.datatype.DataTypeHolder
import io.composeflow.model.project.firebase.CollectionId
import io.composeflow.model.project.firebase.FirebaseAppInfoHolder
import io.composeflow.model.project.firebase.FirestoreCollection
import io.composeflow.model.project.issue.TrackableIssue
import io.composeflow.model.project.string.StringResourceHolder
import io.composeflow.model.project.string.copyContents
import io.composeflow.model.project.theme.ThemeHolder
import io.composeflow.model.project.theme.copyContents
import io.composeflow.model.state.ReadableState
import io.composeflow.model.state.StateHolder
import io.composeflow.model.state.StateHolderImpl
import io.composeflow.model.state.StateId
import io.composeflow.serializer.decodeFromStringWithFallback
import io.composeflow.serializer.encodeToString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

const val COMPOSEFLOW_PACKAGE = "io.composeflow"

@Serializable
@SerialName("Project")
data class Project(
    val id: String = Uuid.random().toString(),
    val name: String = "project",
    val packageName: String = "com.example",
    val screenHolder: ScreenHolder = ScreenHolder(),
    val apiHolder: ApiHolder = ApiHolder(),
    val dataTypeHolder: DataTypeHolder = DataTypeHolder(),
    val customEnumHolder: CustomEnumHolder = CustomEnumHolder(),
    val componentHolder: ComponentHolder = ComponentHolder(),
    val themeHolder: ThemeHolder = ThemeHolder(),
    val assetHolder: AssetHolder = AssetHolder(),
    val appAssetHolder: AppAssetHolder = AppAssetHolder(),
    val stringResourceHolder: StringResourceHolder = StringResourceHolder(),
    val firebaseAppInfoHolder: FirebaseAppInfoHolder = FirebaseAppInfoHolder(),
    val globalStateHolder: StateHolderImpl = StateHolderImpl(),
) : StateHolder by globalStateHolder {
    @Transient
    var lastModified: Instant? = Clock.System.now()

    @Transient
    val bundleId: String = "$packageName.$name"

    fun generateCode(): List<FileSpecWithDirectory> {
        // Memorize the count of how many components are used to generate the viewModelKey
        val context = GenerationContext()

        val commonMainFiles =
            screenHolder.screens.flatMap { it.generateCode(this, context = context) } +
                screenHolder.generateComposeLauncherFile() +
                screenHolder.generateAppNavHostFile(this, context = context) +
                screenHolder.generateKoinViewModelModule(this) +
                screenHolder.generateAppViewModel(this) +
                screenHolder.generateScreenRouteFileSpec(this) +
                dataTypeHolder.generateDataTypeFiles(this) +
                customEnumHolder.generateEnumFiles(this) +
                themeHolder.generateThemeFiles() +
                componentHolder.generateKoinViewModelModule(this) +
                componentHolder.components.flatMap {
                    it.generateCode(this, context = context)
                }
        return commonMainFiles + appAssetHolder.generateCode(this)
    }

    /**
     * Generate the instructions to copy specified resources into the destination directory.
     * For example, to ensure font files are included in the generated app.
     *
     * The from path is expected to be loaded from resources folder obtained like
     * `object {}.javaClass.getResourceAsStream("/composeResources/io.composeflow/font/Caveat-Bold.ttf")`
     *
     * The destination path is a relative path from the app's root directory e.g.:
     * `"composeApp/src/commonMain/composeResources/font/Caveat-Bold.ttf"`
     */
    fun generateCopyInstructions(): Map<String, String> = themeHolder.fontHolder.generateCopyFileInstructions()

    fun generateCopyLocalFileInstructions(userId: String): Map<String, String> =
        assetHolder.generateCopyLocalFileInstructions(
            userId = userId,
            projectId = id,
        ) +
            appAssetHolder.generateCopyLocalFileInstructions(
                userId = userId,
                projectId = id,
            )

    fun getAllCanvasEditable(): List<CanvasEditable> = componentHolder.components + screenHolder.screens

    /**
     * Generate the instructions to write files to the destination directory.
     * For example, to generate string resource XML files.
     *
     * The key represents a relative path from the app's root directory e.g.:
     * `"composeApp/src/commonMain/composeResources/values/strings.xml"`
     *
     * The value represents the content of the file as ByteArray.
     */
    fun generateWriteFileInstructions(): Map<String, ByteArray> =
        (stringResourceHolder.generateStringResourceFiles() + appAssetHolder.generateXmlFiles()).mapValues { (_, content) ->
            content.encodeToByteArray()
        }

    fun getAllComposeNodes(): List<ComposeNode> =
        (screenHolder.screens + componentHolder.components).flatMap {
            it.getAllComposeNodes()
        }

    fun getAssetFiles(): List<BlobInfoWrapper> = assetHolder.images + assetHolder.icons + appAssetHolder.getAssetFiles()

    fun generateTrackableIssues(): List<TrackableIssue> =
        (screenHolder.screens + componentHolder.components)
            .flatMap { it.getAllComposeNodes() }
            .flatMap { node ->
                node.generateTrackableIssues(this)
            } + apiHolder.generateTrackableIssues()

    companion object {
        fun deserializeFromString(yaml: String): Project {
            val project = decodeFromStringWithFallback<Project>(yaml)
            project.screenHolder.updateChildParentRelationships()
            return project
        }
    }
}

fun Project.serialize(): String = encodeToString(this)

/**
 * Find matching State from the all states including all Screens within the project
 */
fun Project.findLocalStateOrNull(stateId: StateId): ReadableState? =
    globalStateHolder.findStateOrNull(this, stateId)
        ?: screenHolder.screens
            .firstOrNull { it.findStateOrNull(this, stateId) != null }
            ?.findStateOrNull(this, stateId)
        ?: componentHolder.components
            .firstOrNull { it.findStateOrNull(this, stateId) != null }
            ?.findStateOrNull(this, stateId)

fun Project.asSummarizedContext(): String {
    val currentEditable = screenHolder.currentEditable()

    // Serialize the current editable as full YAML based on its concrete type
    val currentEditableYaml =
        when (currentEditable) {
            is Screen -> encodeToString(Screen.serializer(), currentEditable)
            is Component -> encodeToString(Component.serializer(), currentEditable)
            else -> encodeToString(currentEditable)
        }

    // Create summarized versions of all other screens and components
    val otherScreensSummary =
        screenHolder.screens
            .filter { it.id != currentEditable.id }
            .map { screen ->
                mapOf(
                    "type" to "Screen",
                    "id" to screen.id,
                    "name" to screen.name,
                    "title" to screen.title.value,
                    "label" to screen.label.value,
                    "isDefault" to screen.isDefault.value,
                    "showOnNavigation" to screen.showOnNavigation.value,
                    "nodeCount" to screen.getAllComposeNodes().size,
                )
            }

    val otherComponentsSummary =
        componentHolder.components
            .filter { it.id != currentEditable.id }
            .map { component ->
                mapOf(
                    "type" to "Component",
                    "id" to component.id,
                    "name" to component.name,
                    "nodeCount" to component.getAllComposeNodes().size,
                    "parameterCount" to component.parameters.size,
                )
            }

    // Build the summarized context structure
    val summarizedContext =
        mapOf(
            "projectInfo" to
                mapOf(
                    "id" to id,
                    "name" to name,
                    "packageName" to packageName,
                ),
            "currentEditable" to
                mapOf(
                    "type" to
                        when (currentEditable) {
                            is Screen -> "Screen"
                            is Component -> "Component"
                            else -> "Unknown"
                        },
                    "id" to currentEditable.id,
                    "name" to currentEditable.name,
                    "fullYaml" to currentEditableYaml,
                ),
            "otherScreens" to otherScreensSummary,
            "otherComponents" to otherComponentsSummary,
            "globalInfo" to
                mapOf(
                    "totalScreens" to screenHolder.screens.size,
                    "totalComponents" to componentHolder.components.size,
                    "totalDataTypes" to dataTypeHolder.dataTypes.size,
                    "totalAPIs" to apiHolder.apiDefinitions.size,
                    "hasNavigation" to screenHolder.showNavigation.value,
                ),
        )

    return summarizedContext.toString()
}

fun Project.findComposeNodeOrNull(nodeId: String): ComposeNode? {
    var node: ComposeNode?
    getAllCanvasEditable().forEach { canvasEditable ->
        node = canvasEditable.getAllComposeNodes().firstOrNull { it.id == nodeId }
        if (node != null) {
            return node
        }
    }
    return null
}

fun Project.findComposeNodeOrThrow(nodeId: String): ComposeNode {
    var node: ComposeNode?
    getAllCanvasEditable().forEach { canvasEditable ->
        node = canvasEditable.getAllComposeNodes().firstOrNull { it.id == nodeId }
        if (node != null) {
            return node
        }
    }
    throw IllegalStateException("No node is found : $nodeId")
}

fun Project.replaceNode(
    nodeId: String,
    newNode: ComposeNode,
) {
    var node: ComposeNode?
    getAllCanvasEditable().forEach { editable ->
        node = editable.getAllComposeNodes().firstOrNull { it.id == nodeId }
        node?.let { node ->
            val indexInParent = node.parentNode?.children?.indexOfFirst { it.id == nodeId }
            indexInParent?.let { index ->
                node.parentNode?.children?.set(index, newNode)
            }
        }
    }
}

fun Project.findDataTypeOrNull(dataTypeId: String): DataType? = dataTypeHolder.dataTypes.firstOrNull { it.id == dataTypeId }

fun Project.findCustomEnumOrNull(customEnumId: String): CustomEnum? =
    customEnumHolder.enumList.firstOrNull {
        it.customEnumId == customEnumId
    }

fun Project.findDataTypeOrThrow(dataTypeId: String): DataType =
    findDataTypeOrNull(dataTypeId)
        ?: throw IllegalStateException("No Data Type is found : $dataTypeId")

fun Project.findCanvasEditableHavingNodeOrNull(node: ComposeNode): CanvasEditable? {
    val screen = screenHolder.screens.firstOrNull { it.hasComposeNode(node) }
    if (screen != null) return screen
    val component = componentHolder.components.firstOrNull { it.hasComposeNode(node) }
    return component
}

fun Project.findCanvasEditableOrNull(id: String): CanvasEditable? {
    val screen = screenHolder.screens.firstOrNull { it.id == id }
    if (screen != null) return screen
    val component = componentHolder.components.firstOrNull { it.id == id }
    return component
}

fun Project.findScreenOrNull(screenId: String): Screen? = screenHolder.findScreen(screenId)

fun Project.findComponentOrNull(componentId: ComponentId): Component? = componentHolder.components.firstOrNull { it.id == componentId }

fun Project.findComponentOrThrow(componentId: ComponentId): Component =
    findComponentOrNull(componentId)
        ?: throw IllegalStateException("No component is found : $componentId")

fun Project.findParameterOrNull(parameterId: ParameterId): ParameterWrapper<*>? {
    val allParameters =
        componentHolder.components.flatMap {
            it.parameters
        } +
            screenHolder.screens.flatMap {
                it.parameters
            }

    return allParameters.firstOrNull {
        it.id == parameterId
    }
}

fun Project.findParameterOrThrow(parameterId: ParameterId): ParameterWrapper<*> =
    findParameterOrNull(parameterId)
        ?: throw IllegalStateException("No parameter is found : $parameterId")

fun Project.findApiDefinitionOrNull(apiId: ApiId): ApiDefinition? = apiHolder.findApiDefinitionOrNull(apiId)

fun Project.findFirestoreCollectionOrNull(collectionId: CollectionId): FirestoreCollection? =
    firebaseAppInfoHolder.firebaseAppInfo.firestoreCollections.firstOrNull {
        it.id == collectionId
    }

/**
 * Utility function to copy contents from another project.
 */
fun Project.copyProjectContents(other: Project) {
    screenHolder.screens.clear()
    screenHolder.screens.addAll(other.screenHolder.screens)
    componentHolder.components.clear()
    componentHolder.components.addAll(other.componentHolder.components)
    apiHolder.apiDefinitions.clear()
    apiHolder.apiDefinitions.addAll(other.apiHolder.apiDefinitions)
    dataTypeHolder.dataTypes.clear()
    dataTypeHolder.dataTypes.addAll(other.dataTypeHolder.dataTypes)
    customEnumHolder.enumList.clear()
    customEnumHolder.enumList.addAll(other.customEnumHolder.enumList)
    themeHolder.copyContents(other.themeHolder)
    assetHolder.images.clear()
    assetHolder.images.addAll(other.assetHolder.images)
    assetHolder.icons.clear()
    assetHolder.icons.addAll(other.assetHolder.icons)
    stringResourceHolder.copyContents(other.stringResourceHolder)
    firebaseAppInfoHolder.firebaseAppInfo = other.firebaseAppInfoHolder.firebaseAppInfo
    globalStateHolder.copyContents(other.globalStateHolder)
    appAssetHolder.copyContents(other.appAssetHolder)
}

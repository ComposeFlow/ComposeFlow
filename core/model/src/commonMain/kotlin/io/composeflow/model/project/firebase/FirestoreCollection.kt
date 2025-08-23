package io.composeflow.model.project.firebase

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import io.composeflow.ViewModelConstant
import io.composeflow.kotlinpoet.ClassHolder
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.kotlinpoet.MemberHolder
import io.composeflow.kotlinpoet.wrapper.AnnotationSpecWrapper
import io.composeflow.kotlinpoet.wrapper.ClassNameWrapper
import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper
import io.composeflow.kotlinpoet.wrapper.ParameterizedTypeNameWrapper
import io.composeflow.kotlinpoet.wrapper.PropertySpecWrapper
import io.composeflow.kotlinpoet.wrapper.parameterizedBy
import io.composeflow.model.datatype.DataType
import io.composeflow.model.datatype.DataTypeId
import io.composeflow.model.project.Project
import io.composeflow.model.project.findDataTypeOrNull
import io.composeflow.model.project.findFirestoreCollectionOrNull
import io.composeflow.model.type.ComposeFlowType
import io.composeflow.ui.propertyeditor.DropdownItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

typealias CollectionId = String

@Serializable
@SerialName("FirestoreCollection")
data class FirestoreCollection(
    val id: CollectionId = Uuid.random().toString(),
    val name: String,
    val dataTypeId: DataTypeId? = null,
) : DropdownItem {
    fun findCompanionDataType(project: Project): DataType? = project.dataTypeHolder.dataTypes.firstOrNull { it.id == dataTypeId }

    @Composable
    override fun asDropdownText(): AnnotatedString = AnnotatedString(name)

    override fun isSameItem(item: Any): Boolean = item is FirestoreCollection && item.id == id

    fun getReadVariableName(project: Project): String {
        val dataType = findCompanionDataType(project) ?: return "firestoreCollection"
        return "${dataType.className.replaceFirstChar { it.lowercase() }}Collection"
    }

    fun generateStatePropertiesToViewModel(project: Project): List<PropertySpecWrapper> {
        val firestoreCollection = project.findFirestoreCollectionOrNull(id)
        val dataType = firestoreCollection?.dataTypeId?.let { project.findDataTypeOrNull(it) }
        if (dataType == null) return emptyList()

        val listDataType: ParameterizedTypeNameWrapper =
            ClassNameWrapper.get("kotlin.collections", "List").parameterizedBy(
                dataType.asKotlinPoetClassName(project),
            )
        val queryResultType =
            ClassHolder.ComposeFlow.DataResult.parameterizedBy(
                listDataType,
            )
        val stateFlowType =
            ClassHolder.Coroutines.Flow.StateFlow
                .parameterizedBy(queryResultType)
        val optInAnnotation =
            AnnotationSpecWrapper
                .builder(ClassNameWrapper.get("kotlin", "OptIn"))
                .addMember("%T::class", ClassNameWrapper.get("kotlinx.coroutines", "ExperimentalCoroutinesApi"))
                .build()

        val collectionStateFlowProperty =
            PropertySpecWrapper
                .builder(getReadVariableName(project), stateFlowType)
                .addAnnotation(optInAnnotation)
                .initializer(
                    CodeBlockWrapper.of(
                        """${ViewModelConstant.firestore}.collection("${firestoreCollection.name}")
        .snapshots(includeMetadataChanges = false)
        .%M { querySnapshot ->
            %T.Success(querySnapshot.documents.map { document ->
                document.data(%T.serializer()).copy(documentId = document.id)
            })
        }
        .%M(
            scope = %M,
            started = %M.WhileSubscribed(5_000),
            initialValue = %T.Loading
        ) 
            """,
                        MemberHolder.Coroutines.Flow.mapLatest,
                        ClassHolder.ComposeFlow.DataResult,
                        dataType.asKotlinPoetClassName(project),
                        MemberHolder.Coroutines.Flow.stateIn,
                        MemberHolder.PreCompose.viewModelScope,
                        MemberHolder.Coroutines.Flow.SharingStarted,
                        ClassHolder.ComposeFlow.DataResult,
                    ),
                ).build()
        return listOf(collectionStateFlowProperty)
    }

    fun generateVariableInitializationBlock(
        project: Project,
        context: GenerationContext,
        dryRun: Boolean,
    ): CodeBlockWrapper {
        val builder = CodeBlockWrapper.builder()
        builder.addStatement(
            "val ${getReadVariableName(project)} = ${context.currentEditable.viewModelName}.${
                getReadVariableName(
                    project,
                )
            }.%M().value",
            MemberHolder.AndroidX.Runtime.collectAsState,
        )
        return builder.build()
    }

    fun valueType(project: Project): ComposeFlowType {
        val firestoreCollection = project.findFirestoreCollectionOrNull(id)
        val dataType = firestoreCollection?.dataTypeId?.let { project.findDataTypeOrNull(it) }
        if (dataType == null) return ComposeFlowType.UnknownType()
        return ComposeFlowType.CustomDataType(isList = true, dataTypeId = dataType.id)
    }
}

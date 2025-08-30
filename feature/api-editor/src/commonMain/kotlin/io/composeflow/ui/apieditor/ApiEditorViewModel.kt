package io.composeflow.ui.apieditor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.composeflow.auth.FirebaseIdToken
import io.composeflow.model.apieditor.ApiDefinition
import io.composeflow.model.apieditor.JsonWithJsonPath
import io.composeflow.model.project.Project
import io.composeflow.model.project.issue.DestinationContext
import io.composeflow.repository.ProjectRepository
import io.composeflow.ui.apieditor.model.ApiResponseUiState
import io.composeflow.ui.apieditor.model.Result
import io.composeflow.ui.apieditor.model.asResult
import io.composeflow.ui.apieditor.repository.ApiCallRepository
import io.composeflow.util.generateUniqueName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope
import kotlin.uuid.Uuid

class ApiEditorViewModel(
    firebaseIdToken: FirebaseIdToken,
    private val project: Project,
    private val apiCallRepository: ApiCallRepository = ApiCallRepository(),
    private val projectRepository: ProjectRepository = ProjectRepository(firebaseIdToken),
) : ViewModel() {
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    // Project that is updated when the the project that is being edited.
    // This may conflicts with the [project] field in this ViewModel, but to detect the real time
    // updates when the Project.ScreenHolder.pending* fields, having this field.
    // This may need to be commonized with the [project] field
    val editingProject =
        projectRepository.editingProject.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = Project(),
        )

    var focusedApiIndex by mutableStateOf<Int?>(null)
        private set

    var apiDefinitionInEdit by mutableStateOf<ApiDefinition?>(null)
        private set

    private val _apiResponse: MutableStateFlow<ApiResponseUiState> =
        MutableStateFlow(ApiResponseUiState.NotStarted)
    val apiResponse: StateFlow<ApiResponseUiState> = _apiResponse

    private val _selectedJsonElement: MutableStateFlow<JsonWithJsonPath?> = MutableStateFlow(null)
    val selectedJsonElement: StateFlow<JsonWithJsonPath?> = _selectedJsonElement

    fun onJsonElementSelected(jsonWithJsonPath: JsonWithJsonPath) {
        _selectedJsonElement.value = jsonWithJsonPath
        apiDefinitionInEdit = apiDefinitionInEdit?.copy(exampleJsonResponse = jsonWithJsonPath)
    }

    fun onApiDefinitionUpdated(apiDefinition: ApiDefinition?) {
        this.apiDefinitionInEdit = apiDefinition

        saveProject()
    }

    fun onApiDefinitionCreated() {
        val apiDefinition = ApiDefinition()
        project.apiHolder.apiDefinitions.add(apiDefinition)
        focusedApiIndex = project.apiHolder.apiDefinitions.size - 1
        clearApiInEdit()
        apiDefinitionInEdit = apiDefinition

        saveProject()
    }

    fun onApiDefinitionSaved(apiDefinition: ApiDefinition) {
        focusedApiIndex?.let {
            project.apiHolder.apiDefinitions[it] = apiDefinition

            saveProject()
        }
    }

    fun onFocusedApiDefinitionDeleted() {
        focusedApiIndex?.let {
            project.apiHolder.apiDefinitions.removeAt(it)

            saveProject()
        }
        focusedApiIndex = null
        clearApiInEdit()
    }

    fun onApiDefinitionCopied(apiDefinition: ApiDefinition) {
        val newName =
            generateUniqueName(
                initial = "${apiDefinition.name}_copy",
                existing =
                    project.apiHolder.apiDefinitions
                        .map { it.name }
                        .toSet(),
            )
        project.apiHolder.apiDefinitions.add(
            apiDefinition.copy(
                id = Uuid.random().toString(),
                name = newName,
            ),
        )

        saveProject()
    }

    fun onApiDefinitionDeleted(apiDefinition: ApiDefinition) {
        project.apiHolder.apiDefinitions
            .removeAll(
                project.apiHolder.apiDefinitions
                    .filter { it.id == apiDefinition.id })

        saveProject()
    }

    fun onFocusedApiDefinitionUpdated(index: Int) {
        focusedApiIndex = index
        apiDefinitionInEdit = project.apiHolder.apiDefinitions[index]
    }

    fun executeApiCall() {
        val api = apiDefinitionInEdit ?: return
        coroutineScope.launch {
            _apiResponse.value = ApiResponseUiState.Loading
            _apiResponse.value =
                apiCallRepository
                    .makeApiCall(
                        api,
                    ).asResult()
                    .map {
                        when (it) {
                            is Result.Error -> ApiResponseUiState.Error(it.exception)
                            Result.Loading -> ApiResponseUiState.Loading
                            is Result.Success -> ApiResponseUiState.Success(it.data)
                        }
                    }.last()
        }
    }

    fun onSetPendingFocus(apiEditorContext: DestinationContext.ApiEditorScreen) {
        val indexToBeFocused =
            project.apiHolder.apiDefinitions.indexOfFirst { it.id == apiEditorContext.apiId }
        if (indexToBeFocused != -1) {
            focusedApiIndex = indexToBeFocused
            apiDefinitionInEdit = project.apiHolder.apiDefinitions[indexToBeFocused]
        }
    }

    private fun clearApiInEdit() {
        apiDefinitionInEdit = null
        _apiResponse.value = ApiResponseUiState.NotStarted
        _selectedJsonElement.value = null
    }

    private fun saveProject() {
        viewModelScope.launch {
            projectRepository.updateProject(project)
        }
    }
}

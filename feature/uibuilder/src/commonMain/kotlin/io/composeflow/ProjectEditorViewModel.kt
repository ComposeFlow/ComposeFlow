package io.composeflow

import io.composeflow.ai.AiAssistantUiState
import io.composeflow.auth.FirebaseIdToken
import io.composeflow.model.project.LoadedProjectUiState
import io.composeflow.model.project.Project
import io.composeflow.model.project.asLoadedProjectUiState
import io.composeflow.model.project.copyProjectContents
import io.composeflow.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope

class ProjectEditorViewModel(
    firebaseIdToken: FirebaseIdToken?,
    projectId: String,
    private val projectRepository: ProjectRepository = if (firebaseIdToken != null) ProjectRepository(firebaseIdToken) else ProjectRepository.createAnonymous(),
) : ViewModel() {
    private val _projectUiState: MutableStateFlow<LoadedProjectUiState> =
        MutableStateFlow(LoadedProjectUiState.Loading)
    val projectUiState: StateFlow<LoadedProjectUiState> = _projectUiState.asStateFlow()

    private val _project = MutableStateFlow(Project())
    val project = _project.asStateFlow()

    private val _aiAssistantUiState = MutableStateFlow<AiAssistantUiState>(AiAssistantUiState.Idle)
    val aiAssistantUiState: StateFlow<AiAssistantUiState> = _aiAssistantUiState.asStateFlow()

    private val _showAiChatDialog = MutableStateFlow(false)
    val showAiChatDialog = _showAiChatDialog.asStateFlow()

    init {
        viewModelScope.launch {
            _projectUiState.value = LoadedProjectUiState.Loading
            _projectUiState.value =
                projectRepository
                    .loadProject(projectId)
                    .asLoadedProjectUiState(projectId)
            when (val state = _projectUiState.value) {
                is LoadedProjectUiState.Success -> {
                    _project.value = state.project
                }

                else -> {}
            }
        }
    }

    fun onUpdateProject(arg: Project) {
        // Copy the contents instead of assigning a new instance
        // Assigning a new instance makes the UiBuilderScreen's hover/focus states
        _project.value.copyProjectContents(arg)
    }

    fun onUpdateAiAssistantState(arg: AiAssistantUiState) {
        _aiAssistantUiState.value = arg
    }

    fun onToggleShowAiChatDialog() {
        _showAiChatDialog.value = !_showAiChatDialog.value
    }
}

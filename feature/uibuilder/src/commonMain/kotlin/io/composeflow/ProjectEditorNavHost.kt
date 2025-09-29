package io.composeflow

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Size
import io.composeflow.ai.AiAssistantUiState
import io.composeflow.model.UI_BUILDER_ROUTE
import io.composeflow.model.project.Project
import io.composeflow.ui.apieditor.apiEditorScreen
import io.composeflow.ui.appstate.appStateEditorScreen
import io.composeflow.ui.asset.assetEditorScreen
import io.composeflow.ui.datatype.dataTypeEditorScreen
import io.composeflow.ui.firestore.firestoreEditorScreen
import io.composeflow.ui.settings.settingsScreen
import io.composeflow.ui.string.stringResourceEditorScreen
import io.composeflow.ui.themeeditor.themeEditorScreen
import io.composeflow.ui.uibuilder.uiBuilderScreen
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.Navigator

@Composable
fun ProjectEditorNavHost(
    navigator: Navigator,
    project: Project,
    aiAssistantUiState: AiAssistantUiState,
    onUpdateProject: (Project) -> Unit,
    onToggleVisibilityOfAiChatDialog: () -> Unit,
    screenMaxSize: Size,
) {
    NavHost(
        navigator = navigator,
        initialRoute = UI_BUILDER_ROUTE,
    ) {
        uiBuilderScreen(
            project = project,
            aiAssistantUiState = aiAssistantUiState,
            onUpdateProject = onUpdateProject,
            onToggleVisibilityOfAiChatDialog = onToggleVisibilityOfAiChatDialog,
            screenMaxSize = screenMaxSize,
        )
        dataTypeEditorScreen(
            project = project,
        )
        appStateEditorScreen(
            project = project,
        )
        firestoreEditorScreen(
            project = project,
        )
        apiEditorScreen(
            project = project,
        )
        themeEditorScreen(
            project = project,
        )
        assetEditorScreen(
            project = project,
        )
        stringResourceEditorScreen(
            project = project,
        )
        settingsScreen(
            project = project,
        )
    }
}

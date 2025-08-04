package io.composeflow.ui.project

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.unit.dp
import io.composeflow.Res
import io.composeflow.ai.AiAssistantDialog
import io.composeflow.ai_create_project_alternative_manually
import io.composeflow.ai_create_project_placeholder
import io.composeflow.ai_need_to_sign_in_to_use_ai_assisted_project_creation
import io.composeflow.ai_prompt_suggestion_chat
import io.composeflow.ai_prompt_suggestion_ecommerce
import io.composeflow.ai_prompt_suggestion_fitness
import io.composeflow.ai_prompt_suggestion_notes
import io.composeflow.ai_prompt_suggestion_recipes
import io.composeflow.ai_prompt_suggestion_social_media
import io.composeflow.ai_prompt_suggestion_task_management
import io.composeflow.ai_prompt_suggestion_weather
import io.composeflow.ai_prompt_suggestions_label
import io.composeflow.ai_title_prompt_dialog
import io.composeflow.auth.isAiEnabled
import io.composeflow.cancel
import io.composeflow.confirm
import io.composeflow.editor.validator.NonEmptyStringValidator
import io.composeflow.editor.validator.ValidateResult
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.Screen
import io.composeflow.ui.Tooltip
import io.composeflow.ui.common.ComposeFlowTheme
import io.composeflow.ui.modifier.moveFocusOnTab
import io.composeflow.ui.popup.PositionCustomizablePopup
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun NewProjectDialog(
    onDismissDialog: () -> Unit,
    onConfirmProject: (projectName: String, packageName: String) -> Unit,
    onConfirmProjectWithScreens: (project: Project, screens: List<Screen>) -> Unit,
    modifier: Modifier = Modifier,
) {
    PositionCustomizablePopup(
        onDismissRequest = {
            onDismissDialog()
        },
        onKeyEvent = {
            if (it.key == Key.Escape) {
                onDismissDialog()
                true
            } else {
                false
            }
        },
        modifier = modifier,
    ) {
        NewProjectDialogContent(
            onConfirmProject = onConfirmProject,
            onConfirmProjectWithScreens = onConfirmProjectWithScreens,
            onDismissDialog = onDismissDialog,
        )
    }
}

@Composable
private fun AiAssistedCreationContent(
    onConfirmProject: (projectName: String, packageName: String) -> Unit,
    onConfirmProjectWithScreens: (project: Project, screens: List<Screen>) -> Unit,
    onDismissDialog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var userQuery by remember { mutableStateOf("") }
    var openAiAssistantDialog by remember { mutableStateOf(false) }
    var openProjectDialogManually by remember { mutableStateOf(false) }

    var queryValidator by remember {
        mutableStateOf(NonEmptyStringValidator().validate(userQuery))
    }
    val isFormValid by remember {
        derivedStateOf {
            queryValidator is ValidateResult.Success
        }
    }

    val second = remember { FocusRequester() }
    val third = remember { FocusRequester() }

    Column(modifier = modifier) {
        if (isAiEnabled()) {
            AiAssistedCreationContentInput(
                userQuery = userQuery,
                onUserQueryChange = { newQuery ->
                    userQuery = newQuery
                    queryValidator = NonEmptyStringValidator().validate(newQuery)
                },
                modifier = Modifier.weight(1f),
            )
        } else {
            Tooltip(stringResource(Res.string.ai_need_to_sign_in_to_use_ai_assisted_project_creation)) {
                AiAssistedCreationContentInput(
                    userQuery = userQuery,
                    onUserQueryChange = { newQuery ->
                        userQuery = newQuery
                        queryValidator = NonEmptyStringValidator().validate(newQuery)
                    },
                    modifier =
                        Modifier
                            .alpha(0.3f)
                            .weight(1f),
                )
            }
        }

        Spacer(Modifier.size(16.dp))
        TextButton(
            onClick = {
                openProjectDialogManually = true
            },
            modifier =
                Modifier
                    .align(Alignment.End)
                    .focusRequester(third)
                    .padding(horizontal = 32.dp),
        ) {
            Text(stringResource(Res.string.ai_create_project_alternative_manually))
        }
        Spacer(Modifier.size(16.dp))
        Row(
            modifier =
                Modifier
                    .padding(top = 16.dp)
                    .padding(horizontal = 32.dp),
        ) {
            Spacer(modifier = Modifier.weight(1f))
            TextButton(
                onClick = {
                    onDismissDialog()
                },
                modifier = Modifier.padding(end = 16.dp).focusRequester(second),
            ) {
                Text(stringResource(Res.string.cancel))
            }

            if (isAiEnabled()) {
                OutlinedButton(
                    onClick = {
                        openAiAssistantDialog = true
                    },
                    enabled = isFormValid,
                    modifier = Modifier.focusRequester(third),
                ) {
                    Text(stringResource(Res.string.confirm))
                }
            } else {
                Tooltip(
                    stringResource(Res.string.ai_need_to_sign_in_to_use_ai_assisted_project_creation),
                ) {
                    OutlinedButton(
                        onClick = {
                            openAiAssistantDialog = true
                        },
                        enabled = false,
                        modifier = Modifier.focusRequester(third),
                    ) {
                        Text(stringResource(Res.string.confirm))
                    }
                }
            }
        }
    }

    if (openAiAssistantDialog) {
        AiAssistantDialog(
            // Project is required to render the thumbnail. This is a temporary solution to create a
            // fake project as here is the place before creating a project
            project = Project(),
            onCloseClick = {
                openAiAssistantDialog = false
            },
            onConfirmProjectWithScreens = onConfirmProjectWithScreens,
            projectCreationPrompt = userQuery,
        )
    }
    if (openProjectDialogManually) {
        val closeDialog = {
            openProjectDialogManually = false
        }
        AddNewProjectDialog(
            onDismissDialog = closeDialog,
            onConfirmProject = onConfirmProject,
        )
    }
}

@Composable
fun AiAssistedCreationContentInput(
    userQuery: String,
    onUserQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val promptSuggestions =
        listOf(
            stringResource(Res.string.ai_prompt_suggestion_social_media),
            stringResource(Res.string.ai_prompt_suggestion_task_management),
            stringResource(Res.string.ai_prompt_suggestion_ecommerce),
            stringResource(Res.string.ai_prompt_suggestion_weather),
            stringResource(Res.string.ai_prompt_suggestion_fitness),
            stringResource(Res.string.ai_prompt_suggestion_notes),
            stringResource(Res.string.ai_prompt_suggestion_recipes),
            stringResource(Res.string.ai_prompt_suggestion_chat),
        )

    val first = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        first.requestFocus()
    }

    Column(modifier = modifier) {
        Text(
            text = stringResource(Res.string.ai_title_prompt_dialog),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 32.dp),
        )
        Spacer(Modifier.size(16.dp))
        Text(
            text = stringResource(Res.string.ai_prompt_suggestions_label),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(horizontal = 32.dp),
        )
        Spacer(Modifier.size(8.dp))
        LazyHorizontalGrid(
            rows = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(horizontal = 32.dp),
            modifier = Modifier.fillMaxWidth().height(80.dp),
        ) {
            items(promptSuggestions) { suggestion ->
                SuggestionChip(
                    onClick = {
                        onUserQueryChange(suggestion)
                    },
                    label = {
                        Text(
                            text = suggestion,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    },
                )
            }
        }
        Spacer(Modifier.size(16.dp))
        OutlinedTextField(
            value = userQuery,
            onValueChange = {
                onUserQueryChange(it)
            },
            placeholder = {
                Text(
                    stringResource(Res.string.ai_create_project_placeholder),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
            },
            enabled = isAiEnabled(),
            singleLine = false,
            minLines = 9,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .focusRequester(first)
                    .moveFocusOnTab(),
        )
    }
}

@Composable
fun NewProjectDialogContent(
    onConfirmProject: (projectName: String, packageName: String) -> Unit,
    onConfirmProjectWithScreens: (project: Project, screens: List<Screen>) -> Unit,
    onDismissDialog: () -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
        Column(
            modifier =
                Modifier
                    .padding(vertical = 32.dp)
                    .size(820.dp, 580.dp),
        ) {
            AiAssistedCreationContent(
                onConfirmProject = onConfirmProject,
                onConfirmProjectWithScreens = onConfirmProjectWithScreens,
                onDismissDialog = onDismissDialog,
            )
        }
    }
}

@Composable
private fun ThemedNewProjectDialogPreview(useDarkTheme: Boolean) {
    ComposeFlowTheme(useDarkTheme = useDarkTheme) {
        NewProjectDialogContent(
            onConfirmProject = { _, _ -> },
            onConfirmProjectWithScreens = { _, _ -> },
            onDismissDialog = {},
        )
    }
}

@Preview
@Composable
fun NewProjectDialogPreview_Light() {
    ThemedNewProjectDialogPreview(useDarkTheme = false)
}

@Preview
@Composable
fun NewProjectDialogPreview_Dark() {
    ThemedNewProjectDialogPreview(useDarkTheme = true)
}

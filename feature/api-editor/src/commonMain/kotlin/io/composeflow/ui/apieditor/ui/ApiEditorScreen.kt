package io.composeflow.ui.apieditor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CursorDropdownMenu
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import io.composeflow.Res
import io.composeflow.add_api
import io.composeflow.add_header
import io.composeflow.add_query_parameter
import io.composeflow.api_editor_instruction_1
import io.composeflow.api_editor_instruction_2
import io.composeflow.api_editor_instruction_3
import io.composeflow.api_editor_instruction_4
import io.composeflow.auth.LocalFirebaseIdToken
import io.composeflow.copy
import io.composeflow.delete
import io.composeflow.delete_api_confirmation
import io.composeflow.model.apieditor.ApiDefinition
import io.composeflow.model.apieditor.ApiDefinition.Companion.NAME_MUST_NOT_BE_EMPTY
import io.composeflow.model.apieditor.ApiDefinition.Companion.URL_MUST_NOT_BE_EMPTY
import io.composeflow.model.apieditor.ApiProperty
import io.composeflow.model.apieditor.Authorization
import io.composeflow.model.apieditor.JsonWithJsonPath
import io.composeflow.model.apieditor.Method
import io.composeflow.model.apieditor.asDisplayText
import io.composeflow.model.project.Project
import io.composeflow.model.project.issue.DestinationContext
import io.composeflow.remove_header
import io.composeflow.toggle_visibility
import io.composeflow.ui.LocalOnAllDialogsClosed
import io.composeflow.ui.LocalOnAnyDialogIsShown
import io.composeflow.ui.LocalOnShowSnackbar
import io.composeflow.ui.Tooltip
import io.composeflow.ui.apieditor.ApiEditorViewModel
import io.composeflow.ui.apieditor.model.ApiResponseUiState
import io.composeflow.ui.icon.ComposeFlowIcon
import io.composeflow.ui.icon.ComposeFlowIconButton
import io.composeflow.ui.jsonpath.createJsonTreeWithJsonPath
import io.composeflow.ui.treeview.TreeView
import io.composeflow.ui.treeview.node.Node
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import io.composeflow.ui.modifier.hoverIconClickable
import io.composeflow.ui.popup.SimpleConfirmationDialog
import io.composeflow.ui.propertyeditor.BasicDropdownPropertyEditor
import io.composeflow.ui.propertyeditor.EditableTextProperty
import io.composeflow.ui.textfield.SmallOutlinedTextField
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import moe.tlaster.precompose.viewmodel.viewModel
import org.jetbrains.compose.resources.stringResource

@Composable
fun ApiEditorScreen(project: Project) {
    val firebaseIdToken = LocalFirebaseIdToken.current
    val viewModel =
        viewModel(modelClass = ApiEditorViewModel::class) {
            ApiEditorViewModel(
                project = project,
                firebaseIdToken = firebaseIdToken,
            )
        }
    val callbacks =
        ApiEditorCallbacks(
            onFocusedApiDefinitionUpdated = viewModel::onFocusedApiDefinitionUpdated,
            onApiDefinitionCopied = viewModel::onApiDefinitionCopied,
            onApiDefinitionDeleted = viewModel::onApiDefinitionDeleted,
        )
    val editingProject = viewModel.editingProject.collectAsState().value
    editingProject.screenHolder.pendingDestinationContext?.let {
        (it as? DestinationContext.ApiEditorScreen)?.let { screen ->
            viewModel.onSetPendingFocus(screen)
        }
    }
    Surface {
        Row(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.weight(1f)) {
                TextButton(
                    onClick = {
                        viewModel.onApiDefinitionCreated()
                    },
                    modifier = Modifier.padding(16.dp),
                ) {
                    Text("+ " + stringResource(Res.string.add_api))
                }
                LazyColumn {
                    itemsIndexed(project.apiHolder.apiDefinitions) { index, item ->
                        ApiRow(
                            apiDefinition = item,
                            index = index,
                            isFocused = index == viewModel.focusedApiIndex,
                            callbacks = callbacks,
                        )
                    }
                }
            }
            VerticalDivider(
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .width(1.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
            )

            ApiEditorPane(
                project = project,
                viewModel,
                allApiDefinitions = project.apiHolder.apiDefinitions,
            )
        }
    }
}

@Composable
private fun ApiRow(
    apiDefinition: ApiDefinition,
    index: Int,
    isFocused: Boolean,
    callbacks: ApiEditorCallbacks,
) {
    var optionMenuOpen by remember { mutableStateOf(false) }
    var deleteApiDefinitionOpen by remember(apiDefinition) { mutableStateOf(false) }

    val border =
        if (!apiDefinition.isValid()) {
            Modifier.border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.error,
                shape = RoundedCornerShape(8.dp),
            )
        } else {
            Modifier.border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(8.dp),
            )
        }

    val focusedModifier =
        if (isFocused) {
            Modifier.background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp),
            )
        } else {
            Modifier
        }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(62.dp)
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clickable {
                    callbacks.onFocusedApiDefinitionUpdated(index)
                }.then(border)
                .then(focusedModifier),
    ) {
        Column(
            modifier =
                Modifier
                    .padding(horizontal = 8.dp)
                    .weight(1f)
                    .fillMaxHeight(),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = apiDefinition.name,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier =
                        Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                )
                apiDefinition.exampleJsonResponse?.let { json ->
                    Text(
                        text =
                            buildAnnotatedString {
                                append(": ")
                                withStyle(
                                    style =
                                        SpanStyle(
                                            color = MaterialTheme.colorScheme.tertiary,
                                        ),
                                ) {
                                    append(json.jsonElement.asDisplayText())
                                }
                            },
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            Text(
                text = apiDefinition.url,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier =
                    Modifier
                        .padding(horizontal = 8.dp),
            )
        }

        IconButton(
            onClick = {
                optionMenuOpen = true
            },
            modifier = Modifier.hoverIconClickable(),
        ) {
            Icon(
                imageVector = Icons.Outlined.MoreVert,
                contentDescription = "API options",
            )
        }

        if (optionMenuOpen) {
            ApiEditorRowOptionMenu(
                apiDefinition = apiDefinition,
                callbacks = callbacks,
                onDismissRequest = {
                    optionMenuOpen = false
                },
                onDeleteApiDefinitionDialogOpen = {
                    deleteApiDefinitionOpen = true
                },
            )
        }

        val onAnyDialogIsShown = LocalOnAnyDialogIsShown.current
        val onAllDialogsClosed = LocalOnAllDialogsClosed.current
        if (deleteApiDefinitionOpen) {
            onAnyDialogIsShown()
            val closeDialog = {
                deleteApiDefinitionOpen = false
                onAllDialogsClosed()
            }
            SimpleConfirmationDialog(
                text = stringResource(Res.string.delete_api_confirmation),
                onConfirmClick = {
                    callbacks.onApiDefinitionDeleted(apiDefinition)
                    closeDialog()
                },
                onCloseClick = {
                    closeDialog()
                },
            )
        }
    }
}

@Composable
private fun ApiEditorRowOptionMenu(
    apiDefinition: ApiDefinition,
    callbacks: ApiEditorCallbacks,
    onDismissRequest: () -> Unit,
    onDeleteApiDefinitionDialogOpen: () -> Unit,
) {
    CursorDropdownMenu(
        expanded = true,
        onDismissRequest = onDismissRequest,
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column {
                DropdownMenuItem(text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = stringResource(Res.string.copy),
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }, onClick = {
                    callbacks.onApiDefinitionCopied(apiDefinition)
                    onDismissRequest()
                })

                DropdownMenuItem(text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = stringResource(Res.string.delete),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }, onClick = {
                    onDeleteApiDefinitionDialogOpen()
                    onDismissRequest()
                })
            }
        }
    }
}

@Composable
private fun RowScope.ApiEditorPane(
    project: Project,
    viewModel: ApiEditorViewModel,
    allApiDefinitions: List<ApiDefinition>,
) {
    Column(
        modifier =
            Modifier
                .weight(4f)
                .padding(16.dp),
    ) {
        ApiDefinitionEditor(
            project = project,
            viewModel = viewModel,
            allApiDefinitions = allApiDefinitions,
        )
        Spacer(
            modifier = Modifier.padding(bottom = 16.dp),
        )
    }
}

@Composable
private fun ApiDefinitionEditor(
    project: Project,
    viewModel: ApiEditorViewModel,
    allApiDefinitions: List<ApiDefinition>,
    modifier: Modifier = Modifier,
) {
    val initialApiDefinition = viewModel.apiDefinitionInEdit ?: return
    var isNameValid by remember(initialApiDefinition.name) { mutableStateOf(initialApiDefinition.name.isNotEmpty()) }
    var nameErrorMessage by remember { mutableStateOf<String?>(null) }
    var isUrlValid by remember(initialApiDefinition.url) { mutableStateOf(initialApiDefinition.url.isNotEmpty()) }
    var urlErrorMessage by remember { mutableStateOf<String?>(null) }
    var deleteDialogVisible by remember { mutableStateOf(false) }
    val apiResponse =
        viewModel.apiResponse.collectAsState().value
    val coroutineScope = rememberCoroutineScope()

    Row(
        modifier =
            modifier
                .fillMaxSize(),
    ) {
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxHeight(),
        ) {
            Row {
                SmallOutlinedTextField(
                    value = initialApiDefinition.name,
                    label = {
                        Text(
                            "Name",
                            style = MaterialTheme.typography.labelMedium,
                        )
                    },
                    onValueChange = {
                        if (it.isEmpty()) {
                            isNameValid = false
                            nameErrorMessage = NAME_MUST_NOT_BE_EMPTY
                        } else {
                            isNameValid = true
                            nameErrorMessage = null
                        }
                        viewModel.onApiDefinitionUpdated(initialApiDefinition.copy(name = it))
                    },
                    isError = !isNameValid,
                    supportingText = {
                        nameErrorMessage?.let {
                            Text(it)
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                )
            }

            var methodExpanded by remember { mutableStateOf(false) }
            val methodSelectedIndex = initialApiDefinition.method.ordinal
            val methods = Method.entries.toTypedArray()
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier =
                        Modifier
                            .wrapContentSize(Alignment.TopStart)
                            .padding(end = 16.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                            Modifier
                                .clickable(onClick = { methodExpanded = true })
                                .background(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(8.dp),
                                ).border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(8.dp),
                                ).padding(vertical = 6.dp)
                                .padding(horizontal = 8.dp),
                    ) {
                        Text(methods[methodSelectedIndex].name)
                        Icon(
                            imageVector = Icons.Outlined.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                        )
                    }
                    DropdownMenu(
                        expanded = methodExpanded,
                        onDismissRequest = { methodExpanded = false },
                    ) {
                        methods.forEachIndexed { index, s ->
                            DropdownMenuItem(
                                onClick = {
                                    viewModel.onApiDefinitionUpdated(
                                        initialApiDefinition.copy(
                                            method =
                                                Method.fromOrdinal(
                                                    index,
                                                ),
                                        ),
                                    )
                                    methodExpanded = false
                                },
                                text = { Text(s.name) },
                            )
                        }
                    }
                }

                SmallOutlinedTextField(
                    value = initialApiDefinition.url,
                    label = {
                        Text(
                            "Url",
                            style = MaterialTheme.typography.labelMedium,
                        )
                    },
                    onValueChange = {
                        if (it.isEmpty()) {
                            isUrlValid = false
                            urlErrorMessage = URL_MUST_NOT_BE_EMPTY
                        } else {
                            isUrlValid = true
                            urlErrorMessage = null
                        }
                        viewModel.onApiDefinitionUpdated(initialApiDefinition.copy(url = it))
                    },
                    isError = !isUrlValid,
                    singleLine = true,
                    supportingText = {
                        urlErrorMessage?.let {
                            Text(it)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            RequestParameterEditor(
                project = project,
                initialValue = initialApiDefinition,
                onApiDefinitionChanged = viewModel::onApiDefinitionUpdated,
            )

            Row(modifier = Modifier.padding(top = 16.dp)) {
                Button(
                    onClick = {
                        viewModel.executeApiCall()
                    },
                    enabled = isNameValid && isUrlValid,
                    modifier = Modifier.padding(end = 32.dp),
                ) {
                    Text("Test API")
                }

                Spacer(modifier = Modifier.weight(1f))

                Row {
                    OutlinedButton(
                        onClick = {
                            deleteDialogVisible = true
                        },
                        modifier = Modifier.padding(end = 32.dp),
                    ) {
                        Text(
                            "Delete",
                            color = MaterialTheme.colorScheme.error,
                        )
                    }

                    val onShowSnackbar = LocalOnShowSnackbar.current
                    val isApiValid =
                        isNameValid && isUrlValid && initialApiDefinition.exampleJsonResponse != null
                    Button(
                        onClick = {
                            viewModel.onApiDefinitionSaved(initialApiDefinition)
                            coroutineScope.launch {
                                onShowSnackbar("Saved API successfully", null)
                            }
                        },
                    ) {
                        if (isApiValid) {
                            Text("Save")
                        } else {
                            Text("Save for later")
                        }
                    }
                }
            }

            Spacer(Modifier.size(24.dp))

            Column(modifier = Modifier.padding(end = 16.dp)) {
                val step1Completed = isNameValid && isUrlValid
                val step2Completed =
                    step1Completed && (apiResponse is ApiResponseUiState.Success || initialApiDefinition.exampleJsonResponse != null)
                val step3Completed =
                    step2Completed && initialApiDefinition.exampleJsonResponse != null
                val step4Completed =
                    step3Completed &&
                        (
                            allApiDefinitions
                                .firstOrNull { it.id == initialApiDefinition.id }
                                ?.isValid() == true
                        )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(Res.string.api_editor_instruction_1),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 12.dp),
                        color =
                            if (step1Completed) {
                                MaterialTheme.colorScheme.onSurface.copy(
                                    alpha = 0.5f,
                                )
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                    )

                    if (step1Completed) {
                        Spacer(Modifier.size(16.dp))
                        ComposeFlowIcon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = "",
                            tint = MaterialTheme.colorScheme.tertiary,
                        )
                    }
                }

                if (step1Completed) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(Res.string.api_editor_instruction_2),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 12.dp),
                            color =
                                if (step2Completed) {
                                    MaterialTheme.colorScheme.onSurface.copy(
                                        alpha = 0.5f,
                                    )
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                        )
                        if (step2Completed) {
                            Spacer(Modifier.size(16.dp))
                            ComposeFlowIcon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = "",
                                tint = MaterialTheme.colorScheme.tertiary,
                            )
                        }
                    }
                }

                if (step2Completed) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(Res.string.api_editor_instruction_3),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 12.dp),
                            color =
                                if (step3Completed) {
                                    MaterialTheme.colorScheme.onSurface.copy(
                                        alpha = 0.5f,
                                    )
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                        )
                        if (step3Completed) {
                            Spacer(Modifier.size(16.dp))
                            ComposeFlowIcon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = "",
                                tint = MaterialTheme.colorScheme.tertiary,
                            )
                        }
                    }
                }

                if (step3Completed) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(Res.string.api_editor_instruction_4),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 12.dp),
                            color =
                                if (step4Completed) {
                                    MaterialTheme.colorScheme.onSurface.copy(
                                        alpha = 0.5f,
                                    )
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                        )
                        if (step4Completed) {
                            Spacer(Modifier.size(16.dp))
                            ComposeFlowIcon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = "",
                                tint = MaterialTheme.colorScheme.tertiary,
                            )
                        }
                    }
                }
            }
        }

        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                when (apiResponse) {
                    is ApiResponseUiState.Error -> {
                        apiResponse.throwable?.message?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }

                    ApiResponseUiState.Loading -> {
                        CircularProgressIndicator()
                    }

                    ApiResponseUiState.NotStarted -> {}
                    is ApiResponseUiState.Success -> {
                        JsonTreeViewer(
                            jsonElement = apiResponse.data,
                            onJsonElementSelected = viewModel::onJsonElementSelected,
                        )
                    }
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                JsonPreviewPanel(
                    jsonWithJsonPath = viewModel.selectedJsonElement.collectAsState().value,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }

    val onAnyDialogIsShown = LocalOnAnyDialogIsShown.current
    val onAllDialogsClosed = LocalOnAllDialogsClosed.current
    if (deleteDialogVisible) {
        onAnyDialogIsShown()
        val closeDialog = {
            deleteDialogVisible = false
            onAllDialogsClosed()
        }
        SimpleConfirmationDialog(
            text = stringResource(Res.string.delete_api_confirmation),
            onCloseClick = closeDialog,
            onConfirmClick = {
                viewModel.onFocusedApiDefinitionDeleted()
                closeDialog()
            },
        )
    }
}

@Composable
private fun RequestParameterEditor(
    project: Project,
    initialValue: ApiDefinition,
    onApiDefinitionChanged: (ApiDefinition) -> Unit,
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    TabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = Modifier.padding(bottom = 8.dp),
    ) {
        Tab(
            selected = selectedTabIndex == 0,
            onClick = {
                selectedTabIndex = 0
            },
            text = {
                Text("Header")
            },
        )
        Tab(
            selected = selectedTabIndex == 1,
            onClick = {
                selectedTabIndex = 1
            },
            text = {
                Text("Query parameters")
            },
        )
        Tab(
            selected = selectedTabIndex == 2,
            onClick = {
                selectedTabIndex = 2
            },
            text = {
                Text("Authorization")
            },
        )
    }

    when (selectedTabIndex) {
        0 -> {
            RequestHeaderEditor(
                initialValue = initialValue,
                onApiDefinitionChanged = onApiDefinitionChanged,
            )
        }

        1 -> {
            QueryParametersEditor(
                initialValue = initialValue,
                onApiDefinitionChanged = onApiDefinitionChanged,
            )
        }

        2 -> {
            AuthorizationParametersEditor(
                project = project,
                initialValue = initialValue,
                onApiDefinitionChanged = onApiDefinitionChanged,
            )
        }
    }
}

@Composable
private fun RequestHeaderEditor(
    initialValue: ApiDefinition,
    onApiDefinitionChanged: (ApiDefinition) -> Unit,
) {
    LazyColumn {
        items(count = initialValue.headers.size) { index ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
            ) {
                val header = initialValue.headers[index]
                SmallOutlinedTextField(
                    value = header.first,
                    onValueChange = {
                        val headers = initialValue.headers.toMutableList()
                        headers[index] = it to headers[index].second
                        onApiDefinitionChanged(initialValue.copy(headers = headers))
                    },
                    label = {
                        Text(
                            "Header name",
                            color = MaterialTheme.colorScheme.secondary,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    },
                    placeholder = {
                        Text(
                            text = "Content-Type",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.alpha(0.3f),
                        )
                    },
                    singleLine = true,
                    modifier =
                        Modifier
                            .weight(2f)
                            .padding(end = 16.dp, bottom = 12.dp),
                )

                ApiParameterEditor(
                    initialProperty = header.second,
                    parameters = initialValue.parameters,
                    onApiPropertyChanged = {
                        val headers = initialValue.headers.toMutableList()
                        headers[index] = headers[index].first to it
                        onApiDefinitionChanged(initialValue.copy(headers = headers))
                    },
                    onAddApiParameter = {
                        initialValue.parameters.add(it)
                        onApiDefinitionChanged(initialValue)
                    },
                    onRemoveParameterAt = {
                        initialValue.parameters.removeAt(it)
                        onApiDefinitionChanged(initialValue)
                    },
                    label = "Value",
                    placeholder = "application/json",
                    modifier = Modifier.weight(3f),
                )

                ComposeFlowIconButton(
                    onClick = {
                        val headers = initialValue.headers.toMutableList()
                        headers.removeAt(index)
                        onApiDefinitionChanged(initialValue.copy(headers = headers))
                    },
                    modifier = Modifier.padding(bottom = 12.dp),
                ) {
                    ComposeFlowIcon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = stringResource(Res.string.remove_header),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }

        item {
            OutlinedButton(
                onClick = {
                    val headers = initialValue.headers.toMutableList()
                    headers.add("" to ApiProperty.IntrinsicValue(""))
                    onApiDefinitionChanged(initialValue.copy(headers = headers))
                },
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = stringResource(Res.string.add_header),
                )
                Text(stringResource(Res.string.add_header))
            }
        }
    }
}

@Composable
private fun QueryParametersEditor(
    initialValue: ApiDefinition,
    onApiDefinitionChanged: (ApiDefinition) -> Unit,
) {
    LazyColumn {
        items(count = initialValue.queryParameters.size) { index ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
            ) {
                val queryParam = initialValue.queryParameters[index]
                SmallOutlinedTextField(
                    value = queryParam.first,
                    onValueChange = {
                        val params = initialValue.queryParameters.toMutableList()
                        params[index] = it to params[index].second
                        onApiDefinitionChanged(initialValue.copy(queryParameters = params))
                    },
                    label = {
                        Text(
                            "Name",
                            color = MaterialTheme.colorScheme.secondary,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    },
                    singleLine = true,
                    modifier =
                        Modifier
                            .weight(2f)
                            .padding(end = 16.dp, bottom = 12.dp),
                )

                ApiParameterEditor(
                    initialProperty = queryParam.second,
                    parameters = initialValue.parameters,
                    onApiPropertyChanged = {
                        val queryParams = initialValue.queryParameters.toMutableList()
                        queryParams[index] = queryParam.first to it
                        onApiDefinitionChanged(initialValue.copy(queryParameters = queryParams))
                    },
                    onAddApiParameter = {
                        initialValue.parameters.add(it)
                        onApiDefinitionChanged(initialValue)
                    },
                    onRemoveParameterAt = {
                        initialValue.parameters.removeAt(it)
                        onApiDefinitionChanged(initialValue)
                    },
                    label = "Value",
                    modifier = Modifier.weight(3f),
                )

                ComposeFlowIconButton(
                    onClick = {
                        val params = initialValue.queryParameters.toMutableList()
                        params.removeAt(index)
                        onApiDefinitionChanged(initialValue.copy(queryParameters = params))
                    },
                    modifier = Modifier.padding(bottom = 12.dp),
                ) {
                    ComposeFlowIcon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = stringResource(Res.string.remove_header),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }

        item {
            OutlinedButton(
                onClick = {
                    val params = initialValue.queryParameters.toMutableList()
                    params.add("" to ApiProperty.IntrinsicValue(""))
                    onApiDefinitionChanged(initialValue.copy(queryParameters = params))
                },
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = stringResource(Res.string.add_query_parameter),
                )
                Text(stringResource(Res.string.add_query_parameter))
            }
        }
    }
}

@Composable
private fun AuthorizationParametersEditor(
    project: Project,
    initialValue: ApiDefinition,
    onApiDefinitionChanged: (ApiDefinition) -> Unit,
) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        BasicDropdownPropertyEditor(
            project = project,
            items = Authorization.entries(),
            onValueChanged = { _, item ->
            },
            selectedItem = initialValue.authorization,
            modifier = Modifier.width(320.dp),
        )
    }

    when (val authorization = initialValue.authorization) {
        is Authorization.BasicAuth -> {
            EditableTextProperty(
                initialValue = authorization.username,
                onValidValueChanged = {
                    onApiDefinitionChanged(
                        initialValue.copy(
                            authorization =
                                authorization.copy(
                                    username = it,
                                ),
                        ),
                    )
                },
                label = "Username",
                modifier = Modifier.width(320.dp),
            )

            var keyboardTypePassword by remember { mutableStateOf(true) }
            Row(verticalAlignment = Alignment.CenterVertically) {
                EditableTextProperty(
                    initialValue = authorization.password,
                    onValidValueChanged = {
                        onApiDefinitionChanged(
                            initialValue.copy(
                                authorization =
                                    authorization.copy(
                                        password = it,
                                    ),
                            ),
                        )
                    },
                    label = "Password",
                    modifier = Modifier.width(320.dp),
                    visualTransformation =
                        if (keyboardTypePassword) {
                            PasswordVisualTransformation()
                        } else {
                            VisualTransformation.None
                        },
                    keyboardOptions =
                        if (keyboardTypePassword) {
                            KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                            )
                        } else {
                            KeyboardOptions.Default
                        },
                )

                Spacer(Modifier.size(8.dp))
                val toggleVisibility = stringResource(Res.string.toggle_visibility)
                Tooltip(toggleVisibility) {
                    ComposeFlowIconButton(onClick = {
                        keyboardTypePassword = !keyboardTypePassword
                    }) {
                        ComposeFlowIcon(
                            imageVector =
                                if (keyboardTypePassword) {
                                    Icons.Outlined.VisibilityOff
                                } else {
                                    Icons.Outlined.Visibility
                                },
                            contentDescription = toggleVisibility,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun JsonPreviewPanel(
    jsonWithJsonPath: JsonWithJsonPath?,
    modifier: Modifier = Modifier,
) {
    if (jsonWithJsonPath == null) return
    Column(modifier = modifier) {
        Text(
            text =
                buildAnnotatedString {
                    append("Preview: ")
                    withStyle(
                        style =
                            SpanStyle(
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.Bold,
                            ),
                    ) {
                        append(jsonWithJsonPath.jsonElement.asDisplayText())
                    }
                },
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        Column(
            modifier =
                Modifier
                    .border(
                        width = 1.dp,
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                    ).padding(8.dp),
        ) {
            val tree = createJsonTreeWithJsonPath(jsonWithJsonPath.jsonElement.toString())
            val lazyListState = rememberLazyListState()
            
            TreeView(
                tree = tree,
                listState = lazyListState,
            )
            
            // Expand root nodes initially
            LaunchedEffect(tree) {
                tree.nodes.forEach { node ->
                    if (node.depth == 0) {
                        tree.expandNode(node)
                    }
                }
            }
        }
    }
}

@Composable
private fun JsonTreeViewer(
    jsonElement: JsonElement,
    onJsonElementSelected: (JsonWithJsonPath) -> Unit,
) {
    val tree = createJsonTreeWithJsonPath(jsonElement.toString())
    val lazyListState = rememberLazyListState()
    
    TreeView(
        tree = tree,
        listState = lazyListState,
        onClick = { node: Node<JsonWithJsonPath>, _, _ ->
            onJsonElementSelected(node.content)
        },
    )
    
    // Expand root nodes initially
    LaunchedEffect(tree) {
        tree.nodes.forEach { node ->
            if (node.depth == 0) {
                tree.expandNode(node)
            }
        }
    }
}

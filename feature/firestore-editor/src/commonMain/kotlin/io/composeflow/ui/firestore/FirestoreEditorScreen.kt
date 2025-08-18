package io.composeflow.ui.firestore

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component1
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component2
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component3
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.composeflow.Res
import io.composeflow.add_field
import io.composeflow.auth.LocalFirebaseIdToken
import io.composeflow.cancel
import io.composeflow.confirm
import io.composeflow.connect_with_firebase_to_proceed
import io.composeflow.custom.ComposeFlowIcons
import io.composeflow.custom.composeflowicons.Plugin
import io.composeflow.data_type
import io.composeflow.delete_data_type
import io.composeflow.editor.validator.KotlinIdentifierValidator.MUST_NOT_BE_EMPTY
import io.composeflow.editor.validator.ValidateResult
import io.composeflow.firebase.management.FIREBASE_CONSOLE_URL
import io.composeflow.firebase_firestore_enable_message
import io.composeflow.firestore_collection_associate
import io.composeflow.firestore_collection_associate_description
import io.composeflow.firestore_collection_delete_data_type_relationship_confirmation
import io.composeflow.firestore_collection_name_warning
import io.composeflow.firestore_collection_tooltip
import io.composeflow.model.LocalNavigator
import io.composeflow.model.SETTINGS_ROUTE
import io.composeflow.model.project.Project
import io.composeflow.model.project.firebase.FirebaseAppInfo
import io.composeflow.open_firebase_console
import io.composeflow.open_settings
import io.composeflow.ui.LocalOnAllDialogsClosed
import io.composeflow.ui.LocalOnAnyDialogIsShown
import io.composeflow.ui.LocalOnShowSnackbar
import io.composeflow.ui.Tooltip
import io.composeflow.ui.common.warning
import io.composeflow.ui.datatype.AddDataFieldDialog
import io.composeflow.ui.datatype.DataTypeDetailContentHeader
import io.composeflow.ui.datatype.DataTypeDetailFieldRow
import io.composeflow.ui.datatype.DeleteDataFieldDialog
import io.composeflow.ui.icon.ComposeFlowIcon
import io.composeflow.ui.icon.ComposeFlowIconButton
import io.composeflow.ui.modifier.backgroundContainerNeutral
import io.composeflow.ui.modifier.hoverIconClickable
import io.composeflow.ui.modifier.hoverOverlay
import io.composeflow.ui.modifier.moveFocusOnTab
import io.composeflow.ui.openInBrowser
import io.composeflow.ui.popup.PositionCustomizablePopup
import io.composeflow.ui.propertyeditor.BasicDropdownPropertyEditor
import io.composeflow.ui.textfield.SmallOutlinedTextField
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.viewModel
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

@Composable
fun FirestoreEditorScreen(
    project: Project,
    modifier: Modifier = Modifier,
) {
    val firebaseIdToken = LocalFirebaseIdToken.current
    val viewModel =
        viewModel(modelClass = FirestoreEditorViewModel::class) {
            FirestoreEditorViewModel(firebaseIdToken = firebaseIdToken, project = project)
        }

    Surface(modifier = modifier.fillMaxSize()) {
        Row {
            val callbacks =
                FirestoreCollectionScreenCallbacks(
                    onFirestoreCollectionAdded = viewModel::onFirestoreCollectionAdded,
                    onFocusedFirestoreCollectionIndexUpdated = viewModel::onFocusedFirestoreCollectionIndexUpdated,
                    onDataFieldAdded = viewModel::onDataFieldAdded,
                    onDataFieldNameUpdated = viewModel::onDataFieldNameUpdated,
                    onDataFieldDefaultValueUpdated = viewModel::onDataFieldDefaultValueUpdated,
                    onDeleteDataField = viewModel::onDeleteDataField,
                    onDeleteFirestoreCollectionRelationship = viewModel::onDeleteFirestoreCollectionRelationship,
                )
            FirestoreHeaderContainer(
                project = project,
                callbacks = callbacks,
                focusedFirestoreCollectionIndex = viewModel.focusedFirestoreCollectionIndex,
            )
            FirestoreCollectionToDataTypeRelationshipDetail(
                project = project,
                callbacks = callbacks,
                focusedFirestoreCollectionIndex = viewModel.focusedFirestoreCollectionIndex,
            )
        }
    }
}

@Composable
private fun FirestoreHeaderContainer(
    project: Project,
    callbacks: FirestoreCollectionScreenCallbacks,
    focusedFirestoreCollectionIndex: Int?,
) {
    Column(
        Modifier
            .padding(16.dp)
            .width(240.dp),
    ) {
        if (project.firebaseAppInfoHolder.firebaseAppInfo.firebaseProjectId == null) {
            ConnectWithFirebaseMessage()
        } else {
            FirestoreHeader(
                project = project,
                callbacks = callbacks,
            )
            Spacer(Modifier.size(8.dp))
            FirebaseFirestoreContent(
                firebaseAppInfo = project.firebaseAppInfoHolder.firebaseAppInfo,
            )
            FirestoreCollectionList(
                project = project,
                callbacks = callbacks,
                focusedFirestoreCollectionIndex = focusedFirestoreCollectionIndex,
            )
        }
    }
}

@Composable
private fun ConnectWithFirebaseMessage() {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Firestore",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                val contentDesc = stringResource(resource = Res.string.firestore_collection_tooltip)
                Tooltip(contentDesc) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = contentDesc,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier =
                            Modifier
                                .padding(start = 8.dp)
                                .size(18.dp),
                    )
                }
            }
        }

        Spacer(Modifier.size(16.dp))
        val connectWithFirebase = stringResource(Res.string.connect_with_firebase_to_proceed)
        Text(
            connectWithFirebase,
            color = MaterialTheme.colorScheme.warning,
            style = MaterialTheme.typography.bodyMedium,
        )

        val openSettings = stringResource(Res.string.open_settings)
        val navigator = LocalNavigator.current
        TextButton(onClick = {
            navigator.navigate("$SETTINGS_ROUTE?destination=Firebase")
        }) {
            ComposeFlowIcon(
                imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                contentDescription = openSettings,
            )
            Spacer(Modifier.size(8.dp))
            Text(openSettings)
        }
    }
}

@Composable
private fun FirestoreHeader(
    project: Project,
    callbacks: FirestoreCollectionScreenCallbacks,
) {
    val coroutineScope = rememberCoroutineScope()

    Row(verticalAlignment = Alignment.CenterVertically) {
        var addCollectionDialogOpen by remember { mutableStateOf(false) }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Firestore",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            val contentDesc = stringResource(resource = Res.string.firestore_collection_tooltip)
            Tooltip(contentDesc) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = contentDesc,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier =
                        Modifier
                            .padding(start = 8.dp)
                            .size(18.dp),
                )
            }
            Spacer(Modifier.weight(1f))
            val associateFirestoreCollection =
                stringResource(Res.string.firestore_collection_associate)
            Tooltip(associateFirestoreCollection) {
                ComposeFlowIconButton(
                    onClick = {
                        addCollectionDialogOpen = true
                    },
                    modifier =
                        Modifier
                            .hoverIconClickable()
                            .hoverOverlay(),
                ) {
                    Icon(
                        ComposeFlowIcons.Plugin,
                        contentDescription = associateFirestoreCollection,
                    )
                }
            }
        }

        val onAnyDialogIsShown = LocalOnAnyDialogIsShown.current
        val onAllDialogsClosed = LocalOnAllDialogsClosed.current
        val onShowsnackbar = LocalOnShowSnackbar.current
        if (addCollectionDialogOpen) {
            onAnyDialogIsShown()
            val closeDialog = {
                addCollectionDialogOpen = false
                onAllDialogsClosed()
            }

            NewFirestoreCollectionDialog(
                project = project,
                onCloseClick = {
                    closeDialog()
                },
                onCollectionConfirmed = { name, dataTypeToAssociate ->
                    val result =
                        callbacks.onFirestoreCollectionAdded(name, dataTypeToAssociate)

                    if (result is FirestoreOperationResult.Failure) {
                        coroutineScope.launch {
                            onShowsnackbar(getString(result.stringResource), null)
                        }
                    }
                    closeDialog()
                },
            )
        }
    }
}

@Composable
private fun FirebaseFirestoreContent(
    firebaseAppInfo: FirebaseAppInfo,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        TextButton(
            onClick = {
                openInBrowser("${FIREBASE_CONSOLE_URL}project/${firebaseAppInfo.firebaseProjectId}/firestore")
            },
            modifier = Modifier.hoverIconClickable(),
        ) {
            val openFirebaseConsole = stringResource(Res.string.open_firebase_console)
            Row {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                    contentDescription = openFirebaseConsole,
                    modifier = Modifier.padding(end = 8.dp),
                )
                Text(openFirebaseConsole)
            }
        }
        val enableFirestoreOnFirebase =
            stringResource(Res.string.firebase_firestore_enable_message)
        Text(
            enableFirestoreOnFirebase,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}

@Composable
private fun FirestoreCollectionList(
    project: Project,
    callbacks: FirestoreCollectionScreenCallbacks,
    focusedFirestoreCollectionIndex: Int?,
) {
    Column(
        Modifier.padding(vertical = 16.dp),
    ) {
        val firestoreCollections =
            project.firebaseAppInfoHolder.firebaseAppInfo.firestoreCollections
        LazyColumn(modifier = Modifier.padding(top = 16.dp)) {
            itemsIndexed(firestoreCollections) { i, firestoreCollection ->
                val focusedModifier =
                    if (i == focusedFirestoreCollectionIndex) {
                        Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                MaterialTheme.colorScheme.tertiaryContainer.copy(
                                    alpha = 0.8f,
                                ),
                            )
                    } else {
                        Modifier.alpha(0.4f)
                    }
                val companionDataType =
                    firestoreCollection.findCompanionDataType(project)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .hoverIconClickable()
                            .then(focusedModifier)
                            .clickable {
                                callbacks.onFocusedFirestoreCollectionIndexUpdated(i)
                            },
                ) {
                    Text(
                        firestoreCollection.name,
                        modifier = Modifier.padding(horizontal = 8.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        "->",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    companionDataType?.let {
                        Text(
                            companionDataType.className,
                            modifier = Modifier.padding(horizontal = 8.dp),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NewFirestoreCollectionDialog(
    project: Project,
    onCloseClick: () -> Unit,
    onCollectionConfirmed: (String, DataTypeToAssociate) -> Unit,
) {
    var collectionName by remember { mutableStateOf("") }
    var dataTypeToAssociate by remember { mutableStateOf<DataTypeToAssociate>(DataTypeToAssociate.CreateNewDataType) }

    PositionCustomizablePopup(
        onDismissRequest = {
            onCloseClick()
        },
        onKeyEvent = {
            if (it.key == Key.Escape) {
                onCloseClick()
                true
            } else {
                false
            }
        },
    ) {
        val (first, second, third) = remember { FocusRequester.createRefs() }
        LaunchedEffect(Unit) {
            first.requestFocus()
        }

        Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
            Column(
                modifier =
                    Modifier
                        .size(width = 420.dp, height = 400.dp)
                        .padding(16.dp),
            ) {
                var validateResult by remember {
                    mutableStateOf<ValidateResult>(
                        ValidateResult.Failure(
                            MUST_NOT_BE_EMPTY,
                        ),
                    )
                }
                Text(
                    text = stringResource(Res.string.firestore_collection_associate),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp),
                )

                Text(
                    text = stringResource(Res.string.firestore_collection_associate_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 16.dp),
                )

                SmallOutlinedTextField(
                    value = collectionName,
                    onValueChange = {
                        collectionName = it
                        validateResult =
                            if (it.isBlank()) ValidateResult.Failure(MUST_NOT_BE_EMPTY) else ValidateResult.Success
                    },
                    label = {
                        Text(
                            "Collection name",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.alpha(0.8f),
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    isError = validateResult is ValidateResult.Failure,
                    supportingText =
                        (validateResult as? ValidateResult.Failure)?.let {
                            {
                                Text(it.message)
                            }
                        },
                    modifier =
                        Modifier
                            .focusRequester(first)
                            .moveFocusOnTab()
                            .fillMaxWidth(),
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    text = stringResource(Res.string.firestore_collection_name_warning),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.warning,
                    modifier = Modifier.padding(bottom = 24.dp),
                )

                val dataTypes =
                    project.dataTypeHolder.dataTypes.map {
                        DataTypeToAssociate.ExistingDataType(it)
                    } + DataTypeToAssociate.CreateNewDataType

                BasicDropdownPropertyEditor(
                    project = project,
                    items = dataTypes,
                    onValueChanged = { _, item ->
                        dataTypeToAssociate =
                            when (item) {
                                DataTypeToAssociate.CreateNewDataType -> {
                                    DataTypeToAssociate.CreateNewDataType
                                }

                                is DataTypeToAssociate.ExistingDataType -> {
                                    DataTypeToAssociate.ExistingDataType(item.dataType)
                                }
                            }
                    },
                    selectedItem = dataTypeToAssociate,
                    label = stringResource(Res.string.data_type),
                    modifier = Modifier.fillMaxWidth(),
                )

                Row(
                    modifier = Modifier.padding(top = 16.dp),
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(
                        onClick = {
                            onCloseClick()
                        },
                        modifier =
                            Modifier
                                .padding(end = 16.dp)
                                .focusRequester(second),
                    ) {
                        Text(stringResource(Res.string.cancel))
                    }
                    OutlinedButton(
                        onClick = {
                            onCollectionConfirmed(collectionName, dataTypeToAssociate)
                        },
                        enabled = validateResult is ValidateResult.Success,
                        modifier =
                            Modifier
                                .focusRequester(third),
                    ) {
                        Text(stringResource(Res.string.confirm))
                    }
                }
            }
        }
    }
}

@Composable
private fun FirestoreCollectionRelationshipDetailContent(
    project: Project,
    focusedFirestoreCollectionIndex: Int?,
    callbacks: FirestoreCollectionScreenCallbacks,
) {
    var addDataFieldDialogOpen by remember { mutableStateOf(false) }
    var deleteRelationshipDialogOpen by remember { mutableStateOf(false) }
    var indexOfDataFieldToBeEdited by remember { mutableStateOf<Int?>(null) }
    var indexOfDataFieldToBeDeleted by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier =
            Modifier
                .width(960.dp)
                .fillMaxHeight()
                .padding(vertical = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(color = MaterialTheme.colorScheme.surface),
    ) {
        focusedFirestoreCollectionIndex ?: return

        val firestoreCollection =
            project.firebaseAppInfoHolder.firebaseAppInfo.firestoreCollections[focusedFirestoreCollectionIndex]
        val dataTypeId =
            project.firebaseAppInfoHolder.firebaseAppInfo.firestoreCollections[focusedFirestoreCollectionIndex]
                .dataTypeId
        val dataType =
            project.dataTypeHolder.dataTypes.firstOrNull { it.id == dataTypeId } ?: return
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(
                        "Firestore collection name",
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        firestoreCollection.name,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 2.dp).padding(bottom = 16.dp),
                    )
                }
                Spacer(Modifier.width(32.dp))
                Text(
                    "->",
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.width(32.dp))
                Column {
                    Text(
                        "Data type",
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        dataType.className,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 2.dp).padding(bottom = 16.dp),
                    )
                }

                Spacer(Modifier.weight(1f))
                val contentDesc = stringResource(Res.string.delete_data_type)
                Tooltip(contentDesc) {
                    IconButton(onClick = {
                        deleteRelationshipDialogOpen = true
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = contentDesc,
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
            Spacer(Modifier.size(16.dp))

            DataTypeDetailContentHeader()
            LazyColumn(modifier = Modifier.heightIn(max = 800.dp)) {
                itemsIndexed(dataType.fields) { i, dataField ->
                    DataTypeDetailFieldRow(
                        dataField = dataField,
                        index = i,
                        onDataFieldNameUpdated = callbacks.onDataFieldNameUpdated,
                        onDataFieldDefaultValueUpdated = callbacks.onDataFieldDefaultValueUpdated,
                        onDeleteDataFieldDialogOpen = {
                            indexOfDataFieldToBeDeleted = i
                        },
                    )
                }
            }
            TextButton(
                onClick = {
                    addDataFieldDialogOpen = true
                },
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Text("+ ${stringResource(Res.string.add_field)}")
            }
        }

        val onAnyDialogIsOpen = LocalOnAnyDialogIsShown.current
        val onAllDialogsClosed = LocalOnAllDialogsClosed.current
        if (addDataFieldDialogOpen) {
            onAnyDialogIsOpen()
            val dialogClosed = {
                addDataFieldDialogOpen = false
                onAllDialogsClosed()
            }
            val initialValue =
                indexOfDataFieldToBeEdited?.let {
                    dataType.fields[it]
                }
            AddDataFieldDialog(
                project = project,
                initialValue = initialValue,
                updateIndex = indexOfDataFieldToBeEdited,
                onDataFieldAdded = {
                    callbacks.onDataFieldAdded(it)
                    dialogClosed()
                },
                onDataFieldNameUpdated = { i, inputName ->
                    callbacks.onDataFieldNameUpdated(i, inputName)
                    dialogClosed()
                    indexOfDataFieldToBeEdited = null
                },
                onDialogClosed = dialogClosed,
            )
        }

        indexOfDataFieldToBeDeleted?.let { indexToBeDeleted ->
            onAnyDialogIsOpen()
            DeleteDataFieldDialog(
                index = indexToBeDeleted,
                onCloseClick = {
                    indexOfDataFieldToBeDeleted = null
                    onAllDialogsClosed()
                },
                onDeleteDataFieldOfIndex = {
                    callbacks.onDeleteDataField(it)
                    onAllDialogsClosed()
                    indexOfDataFieldToBeDeleted = null
                },
            )
        }

        val onAnyDialogIsShown = LocalOnAnyDialogIsShown.current
        val onAllDialogClosed = LocalOnAllDialogsClosed.current
        if (deleteRelationshipDialogOpen) {
            onAnyDialogIsShown()
            val closeDeleteDataTypeDialog = {
                deleteRelationshipDialogOpen = false
                onAllDialogClosed()
            }
            DeleteFirestoreCollectionRelationshipDialog(
                onCloseClick = {
                    closeDeleteDataTypeDialog()
                },
                onDeleteRelationship = {
                    callbacks.onDeleteFirestoreCollectionRelationship()
                    closeDeleteDataTypeDialog()
                },
            )
        }
    }
}

@Composable
private fun FirestoreCollectionToDataTypeRelationshipDetail(
    project: Project,
    callbacks: FirestoreCollectionScreenCallbacks,
    focusedFirestoreCollectionIndex: Int?,
) {
    Row(
        modifier =
            Modifier
                .fillMaxSize()
                .backgroundContainerNeutral()
                .padding(16.dp),
    ) {
        Spacer(Modifier.weight(1f))
        FirestoreCollectionRelationshipDetailContent(
            project = project,
            callbacks = callbacks,
            focusedFirestoreCollectionIndex = focusedFirestoreCollectionIndex,
        )
        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun DeleteFirestoreCollectionRelationshipDialog(
    onCloseClick: () -> Unit,
    onDeleteRelationship: () -> Unit,
) {
    PositionCustomizablePopup(
        onDismissRequest = {
            onCloseClick()
        },
        onKeyEvent = {
            if (it.type == KeyEventType.KeyDown && it.key == Key.Escape) {
                onCloseClick()
                true
            } else {
                false
            }
        },
    ) {
        Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
            Column(
                modifier =
                    Modifier
                        .size(width = 300.dp, height = 160.dp)
                        .padding(16.dp),
            ) {
                Text(
                    text = stringResource(resource = Res.string.firestore_collection_delete_data_type_relationship_confirmation),
                )

                Spacer(modifier = Modifier.weight(1f))
                Row(
                    modifier = Modifier.padding(top = 16.dp),
                ) {
                    TextButton(
                        onClick = {
                            onCloseClick()
                        },
                        modifier =
                            Modifier
                                .padding(end = 16.dp),
                    ) {
                        Text("Cancel")
                    }
                    OutlinedButton(
                        onClick = {
                            onDeleteRelationship()
                        },
                        modifier = Modifier,
                    ) {
                        Text(
                            text = "Delete",
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }
}

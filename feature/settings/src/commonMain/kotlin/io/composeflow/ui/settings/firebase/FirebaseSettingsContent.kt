package io.composeflow.ui.settings.firebase

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import io.composeflow.Res
import io.composeflow.auth.FirebaseIdToken
import io.composeflow.auth.LocalFirebaseIdToken
import io.composeflow.custom.ComposeFlowIcons
import io.composeflow.custom.composeflowicons.Apple
import io.composeflow.custom.composeflowicons.Plugin
import io.composeflow.custom.composeflowicons.Web
import io.composeflow.edit_firebase_project_id
import io.composeflow.enable_auth_on_firebase
import io.composeflow.enabled
import io.composeflow.firebase.management.FIREBASE_CONSOLE_URL
import io.composeflow.firebase_auth_description
import io.composeflow.firebase_integration_description
import io.composeflow.firebase_settings_sign_in_required
import io.composeflow.model.project.Project
import io.composeflow.model.project.firebase.FirebaseAppInfo
import io.composeflow.model.project.firebase.FirebaseConnectedStatus
import io.composeflow.not_enabled
import io.composeflow.ui.Tooltip
import io.composeflow.ui.common.warning
import io.composeflow.ui.icon.ComposeFlowIcon
import io.composeflow.ui.icon.ComposeFlowIconButton
import io.composeflow.ui.modifier.hoverIconClickable
import io.composeflow.ui.openInBrowser
import io.composeflow.ui.settings.SettingsCallbacks
import io.composeflow.ui.textfield.SmallOutlinedTextField
import org.jetbrains.compose.resources.stringResource
import java.net.URI

@Composable
fun FirebaseSettingsContent(
    project: Project,
    firebaseApiResultState: FirebaseApiResultState,
    firebaseApiAppResultState: FirebaseApiAppResultState,
    settingsCallbacks: SettingsCallbacks,
    modifier: Modifier = Modifier,
) {
    val currentUser = LocalFirebaseIdToken.current
    val isFirebaseAvailable = currentUser is FirebaseIdToken.SignedInToken

    Column(modifier = modifier.padding(16.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp),
        ) {
            val firebaseIntegrationDesc =
                stringResource(Res.string.firebase_integration_description)
            Text(
                "Firebase integration",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(end = 8.dp),
            )
            Tooltip(firebaseIntegrationDesc) {
                ComposeFlowIcon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = firebaseIntegrationDesc,
                )
            }
        }

        if (isFirebaseAvailable) {
            FirebaseProjectIdEditor(
                firebaseAppInfo = project.firebaseAppInfoHolder.firebaseAppInfo,
                firebaseApiResultState = firebaseApiResultState,
                firebaseApiAppResultState = firebaseApiAppResultState,
                settingsCallbacks = settingsCallbacks,
            )

            if (firebaseApiResultState is FirebaseApiResultState.Success ||
                firebaseApiResultState is FirebaseApiResultState.PartialSuccess
            ) {
                FirebaseAuthContent(
                    firebaseAppInfo = project.firebaseAppInfoHolder.firebaseAppInfo,
                    modifier = Modifier.padding(top = 32.dp),
                )
            }
        } else {
            Tooltip(stringResource(Res.string.firebase_settings_sign_in_required)) {
                Column(modifier = Modifier.alpha(0.3f)) {
                    FirebaseProjectIdEditor(
                        firebaseAppInfo = project.firebaseAppInfoHolder.firebaseAppInfo,
                        firebaseApiResultState = firebaseApiResultState,
                        firebaseApiAppResultState = firebaseApiAppResultState,
                        settingsCallbacks = settingsCallbacks,
                        isEnabled = false,
                    )

                    if (firebaseApiResultState is FirebaseApiResultState.Success ||
                        firebaseApiResultState is FirebaseApiResultState.PartialSuccess
                    ) {
                        FirebaseAuthContent(
                            firebaseAppInfo = project.firebaseAppInfoHolder.firebaseAppInfo,
                            modifier = Modifier.padding(top = 32.dp),
                        )
                    }
                }
            }
        }
    }
}

private val androidAppBorderColor = Color(0xFF38a59c)
private val iOSAppBorderColor = Color(0xFF4ba6ec)
private val webAppBorderColor = Color(0xFF8b3157)

@Composable
private fun FirebaseProjectIdEditor(
    firebaseAppInfo: FirebaseAppInfo,
    firebaseApiResultState: FirebaseApiResultState,
    firebaseApiAppResultState: FirebaseApiAppResultState,
    settingsCallbacks: SettingsCallbacks,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
) {
    var firebaseProjectIdInEdit by remember {
        mutableStateOf(
            firebaseAppInfo.firebaseProjectId ?: "",
        )
    }
    var idInEdit by remember { mutableStateOf(firebaseAppInfo.firebaseProjectId.isNullOrEmpty()) }
    val focusRequester = remember { FocusRequester() }
    if (idInEdit) {
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }
    Column(modifier = modifier) {
        Text(
            text =
                buildAnnotatedString {
                    withStyle(
                        style = MaterialTheme.typography.bodySmall.toSpanStyle(),
                    ) {
                        append("Create a new Firebase project at ")

                        withLink(LinkAnnotation.Url(url = FIREBASE_CONSOLE_URL)) {
                            withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                                append("Firebase console")
                            }
                        }
                        append(" and enter the project Id below to connect with ComposeFlow.")
                    }
                },
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SmallOutlinedTextField(
                    value = firebaseProjectIdInEdit,
                    onValueChange = {
                        firebaseProjectIdInEdit = it
                    },
                    label = {
                        Text(
                            "Firebase Project Id",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.alpha(0.6f),
                        )
                    },
                    singleLine = true,
                    readOnly = !idInEdit,
                    enabled = isEnabled,
                    modifier =
                        Modifier
                            .width(350.dp)
                            .focusRequester(focusRequester),
                )

                ComposeFlowIconButton(
                    onClick = {
                        idInEdit = true
                    },
                    enabled = !idInEdit && isEnabled,
                ) {
                    val editFirebaseProjectId = stringResource(Res.string.edit_firebase_project_id)
                    ComposeFlowIcon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = editFirebaseProjectId,
                    )
                }
            }

            if (firebaseApiResultState == FirebaseApiResultState.Loading) {
                CircularProgressIndicator()
            } else {
                TextButton(
                    onClick = {
                        settingsCallbacks.onConnectFirebaseProjectId(firebaseProjectIdInEdit)
                    },
                    enabled =
                        isEnabled &&
                            (
                                (
                                    firebaseProjectIdInEdit.isNotEmpty() &&
                                        firebaseProjectIdInEdit != firebaseAppInfo.firebaseProjectId
                                ) ||
                                    (
                                        firebaseProjectIdInEdit.isNotEmpty() &&
                                            firebaseAppInfo.getConnectedStatus() == FirebaseConnectedStatus.PartiallyConnected
                                    )
                            ),
                ) {
                    Row {
                        ComposeFlowIcon(
                            imageVector = ComposeFlowIcons.Plugin,
                            contentDescription = "",
                            modifier = Modifier.padding(end = 8.dp),
                        )
                        Text("Connect")
                    }
                }
            }

            if (firebaseApiResultState is FirebaseApiResultState.Failure) {
                Text(
                    text = firebaseApiResultState.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            if (firebaseApiResultState is FirebaseApiResultState.Success) {
                Text(
                    text = firebaseApiResultState.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
            if (firebaseApiResultState is FirebaseApiResultState.PartialSuccess) {
                Text(
                    text = firebaseApiResultState.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.warning,
                )
            }
        }

        Column(modifier = Modifier.padding(top = 24.dp)) {
            Column {
                Text(
                    "Android app",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier =
                            Modifier
                                .border(
                                    width = 1.dp,
                                    color = androidAppBorderColor,
                                    shape = CircleShape,
                                ).size(42.dp),
                    ) {
                        ComposeFlowIcon(
                            imageVector = Icons.Outlined.Android,
                            contentDescription = "",
                        )
                    }
                    FirebaseAppLabelContainer(firebaseApiAppResultState.androidApp)
                }
            }
            Column(modifier = Modifier.padding(top = 8.dp)) {
                Text(
                    "iOS app",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier =
                            Modifier
                                .border(
                                    width = 1.dp,
                                    color = iOSAppBorderColor,
                                    shape = CircleShape,
                                ).size(42.dp),
                    ) {
                        ComposeFlowIcon(
                            imageVector = ComposeFlowIcons.Apple,
                            contentDescription = "",
                        )
                    }
                    FirebaseAppLabelContainer(firebaseApiAppResultState.iOSApp)
                }
            }

            Column(modifier = Modifier.padding(top = 8.dp)) {
                Text(
                    "Web app",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier =
                            Modifier
                                .border(
                                    width = 1.dp,
                                    color = webAppBorderColor,
                                    shape = CircleShape,
                                ).size(42.dp),
                    ) {
                        ComposeFlowIcon(
                            imageVector = ComposeFlowIcons.Web,
                            contentDescription = "",
                        )
                    }
                    FirebaseAppLabelContainer(firebaseApiAppResultState.webApp)
                }
            }
        }
    }
}

@Composable
private fun FirebaseAppLabelContainer(firebaseApiAppResult: FirebaseApiAppResult) {
    Column(modifier = Modifier.padding(start = 16.dp)) {
        val failureMessage = firebaseApiAppResult.failureMessage
        if (failureMessage != null) {
            Text(
                text = failureMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        } else if (firebaseApiAppResult.isLoading) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(end = 8.dp),
                )
                firebaseApiAppResult.loadingMessage?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
            }
        } else if (
            firebaseApiAppResult.appBundleName() != null ||
            firebaseApiAppResult.appDisplayName() != null
        ) {
            val bundleName = firebaseApiAppResult.appBundleName()
            val displayName = firebaseApiAppResult.appDisplayName()
            val consoleUrl = firebaseApiAppResult.firebaseConsoleUrl()
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (displayName != null && bundleName != null) {
                    Column {
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                        Text(
                            text = bundleName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                } else if (displayName != null) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                } else if (bundleName != null) {
                    Text(
                        text = bundleName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }

                consoleUrl?.let {
                    IconButton(
                        onClick = {
                            openInBrowser(URI(it))
                        },
                        modifier =
                            Modifier
                                .padding(start = 8.dp)
                                .hoverIconClickable(),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                            contentDescription = "",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FirebaseAuthContent(
    firebaseAppInfo: FirebaseAppInfo,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            "Firebase Authentication",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(end = 8.dp),
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 8.dp),
        ) {
            if (firebaseAppInfo.authenticationEnabled.value) {
                Text(
                    stringResource(Res.string.enabled),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            } else {
                Text(
                    stringResource(Res.string.not_enabled),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.warning,
                )
            }

            TextButton(
                onClick = {
                    openInBrowser(
                        URI(
                            "${FIREBASE_CONSOLE_URL}project/${firebaseAppInfo.firebaseProjectId}/authentication",
                        ),
                    )
                },
                modifier = Modifier.hoverIconClickable(),
            ) {
                val enableAuthOnFirebase = stringResource(Res.string.enable_auth_on_firebase)
                Row {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                        contentDescription = enableAuthOnFirebase,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    Text(enableAuthOnFirebase)
                }
            }
        }

        Text(
            stringResource(Res.string.firebase_auth_description),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

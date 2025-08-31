package io.composeflow.ui.toolbar

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.composeflow.Res
import io.composeflow.auth.FirebaseIdToken
import io.composeflow.device.Device
import io.composeflow.device.EmulatorStatus
import io.composeflow.device.SimulatorStatus
import io.composeflow.download_jdk_confirmation
import io.composeflow.preview_app_disabled_due_to_issues
import io.composeflow.ui.Tooltip
import io.composeflow.ui.icon.ComposeFlowIconButton
import io.composeflow.ui.modifier.hoverIconClickable
import io.composeflow.ui.modifier.hoverOverlay
import io.composeflow.ui.popup.SimpleConfirmationDialog
import io.composeflow.ui.statusbar.StatusBarUiState
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.viewmodel.viewModel
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Dropdown
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.painter.badge.DotBadgeShape
import org.jetbrains.jewel.ui.painter.hints.Badge
import org.jetbrains.jewel.ui.painter.rememberResourcePainterProvider
import org.jetbrains.jewel.ui.theme.colorPalette

const val TOOLBAR_TEST_TAG = "Toolbar"
const val TOOLBAR_RUN_BUTTON_TEST_TAG = "$TOOLBAR_TEST_TAG/RunButton"

@Composable
fun RightToolbar(
    firebaseIdToken: FirebaseIdToken?,
    projectFileName: String,
    onStatusBarUiStateChanged: (StatusBarUiState) -> Unit,
    statusBarUiState: StatusBarUiState,
    navigator: Navigator,
    modifier: Modifier = Modifier,
) {
    val viewModel =
        viewModel(modelClass = ToolbarViewModel::class, keys = listOf(firebaseIdToken?.user_id ?: "anonymous")) {
            ToolbarViewModel(
                firebaseIdTokenArg = firebaseIdToken,
            )
        }
    val availableDevices by viewModel.availableDevices.collectAsState()
    val project by viewModel.editingProject.collectAsState()
    val buttonEnabled = statusBarUiState !is StatusBarUiState.Loading
    val buttonModifier =
        if (buttonEnabled) {
            Modifier
                .hoverIconClickable()
                .hoverOverlay()
        } else {
            Modifier.alpha(0.5f)
        }
    val javaHomePath = viewModel.javaHomePath.collectAsState().value
    val pendingPreviewAppParams = viewModel.pendingPreviewAppParams.collectAsState().value

    if (pendingPreviewAppParams != null) {
        SimpleConfirmationDialog(
            text = stringResource(Res.string.download_jdk_confirmation),
            onCloseClick = {
                viewModel.onResetPendingPreviewRequest()
            },
            onConfirmClick = {
                viewModel.onRunPreviewApp(
                    previewAppParams = pendingPreviewAppParams,
                    onStatusBarUiStateChanged = onStatusBarUiStateChanged,
                    downloadJdk = true,
                )
                viewModel.onResetPendingPreviewRequest()
            },
            positiveText = "Download",
            positiveButtonColor = MaterialTheme.colorScheme.primary,
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .testTag(TOOLBAR_TEST_TAG),
    ) {
        val selectedDevice by viewModel.selectedDevice.collectAsState()

        @Composable
        fun DeviceIcon(device: Device) {
            val icon =
                when (device) {
                    is Device.AndroidEmulator -> {
                        val iconProvider =
                            rememberResourcePainterProvider(
                                "icons/androidDevice.svg",
                                Icons::class.java,
                            )
                        if (device.status == EmulatorStatus.Device) {
                            val badged by iconProvider.getPainter(
                                Badge(
                                    Color.Green,
                                    DotBadgeShape.Default,
                                ),
                            )
                            badged
                        } else {
                            val icon by iconProvider.getPainter()
                            icon
                        }
                    }

                    is Device.IosSimulator -> {
                        val iconProvider =
                            rememberResourcePainterProvider(
                                "icons/iPhoneDevice.svg",
                                Icons::class.java,
                            )
                        if (device.status == SimulatorStatus.Booted) {
                            val badged by iconProvider.getPainter(
                                Badge(
                                    Color.Green,
                                    DotBadgeShape.Default,
                                ),
                            )
                            badged
                        } else {
                            val icon by iconProvider.getPainter()
                            icon
                        }
                    }

                    Device.Web -> {
                        val iconProvider =
                            rememberResourcePainterProvider("icons/web.svg", Icons::class.java)
                        val icon by iconProvider.getPainter()
                        icon
                    }
                }
            org.jetbrains.jewel.ui.component.Icon(
                painter = icon,
                "device icon",
                modifier = Modifier.padding(end = 4.dp),
            )
        }

        Spacer(Modifier.weight(1f))

        val issues = project.generateTrackableIssues()
        IssuesBadge(
            project = project,
            issues = issues,
            navigator = navigator,
            onSetPendingFocus = viewModel::onSetPendingFocuses,
        )
        Spacer(Modifier.width(16.dp))

        Dropdown(
            menuContent = {
                availableDevices.forEach {
                    selectableItem(
                        selected = selectedDevice == it,
                        onClick = {
                            viewModel.onSelectedDeviceNameChanged(it.deviceName)
                        },
                    ) {
                        Row {
                            Spacer(Modifier.weight(1f))
                            DeviceIcon(it)
                            Text(it.deviceName)
                        }
                    }
                }
            },
            modifier = Modifier.wrapContentWidth(),
        ) {
            Row(modifier = Modifier.wrapContentWidth()) {
                Spacer(Modifier.weight(1f))
                DeviceIcon(selectedDevice)
                Text(selectedDevice.deviceName)
            }
        }

        val runAppContentDesc =
            if (issues.isEmpty()) {
                "Preview app"
            } else {
                stringResource(Res.string.preview_app_disabled_due_to_issues)
            }
        Tooltip(runAppContentDesc) {
            ComposeFlowIconButton(
                onClick = {
                    val availability =
                        viewModel.onCheckPreviewAvailability(
                            previewAppParams =
                                PreviewAppParams(
                                    projectFileName = projectFileName,
                                    targetDevice = selectedDevice,
                                    availableDevices = availableDevices,
                                    javaHomePath = javaHomePath,
                                ),
                        )

                    when (availability) {
                        is PreviewAvailability.Available -> {
                            viewModel.onRunPreviewApp(
                                onStatusBarUiStateChanged = onStatusBarUiStateChanged,
                                previewAppParams = availability.previewAppParams,
                            )
                        }

                        is PreviewAvailability.JdkNotInstalled -> {
                            viewModel.onShowDownloadJdkConfirmationDialog(
                                previewAppParams = availability.previewAppParams,
                            )
                        }
                    }
                },
                enabled = statusBarUiState !is StatusBarUiState.Loading && issues.isEmpty(),
                modifier =
                    Modifier
                        .testTag(TOOLBAR_RUN_BUTTON_TEST_TAG)
                        .then(
                            if (issues.isEmpty()) {
                                buttonModifier
                            } else {
                                Modifier
                            },
                        ),
            ) {
                Icon(
                    imageVector = Icons.Outlined.PlayArrow,
                    contentDescription = runAppContentDesc,
                    tint =
                        if (issues.isEmpty()) {
                            JewelTheme.colorPalette.green(5)
                        } else {
                            JewelTheme.colorPalette.grey(7)
                        },
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))

        val downloadCodeContentDesc = "Download code"
        Tooltip(downloadCodeContentDesc) {
            ComposeFlowIconButton(
                onClick = {
                    viewModel.onDownloadCode(
                        projectFileName = projectFileName,
                        onStatusBarUiStateChanged = onStatusBarUiStateChanged,
                    )
                },
                enabled = buttonEnabled,
                modifier = buttonModifier,
            ) {
                Icon(
                    imageVector = Icons.Filled.Download,
                    contentDescription = downloadCodeContentDesc,
                    tint = JewelTheme.colorPalette.grey(11),
                )
            }
        }

        Spacer(modifier = Modifier.width(80.dp))
    }
}

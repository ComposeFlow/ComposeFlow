package io.composeflow.ui.toolbar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.composeflow.Res
import io.composeflow.custom.ComposeFlowIcons
import io.composeflow.custom.composeflowicons.AndroiddeviceDark
import io.composeflow.custom.composeflowicons.IphonedeviceDark
import io.composeflow.custom.composeflowicons.WebDark
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
import org.jetbrains.compose.resources.stringResource

const val TOOLBAR_TEST_TAG = "Toolbar"
const val TOOLBAR_RUN_BUTTON_TEST_TAG = "$TOOLBAR_TEST_TAG/RunButton"

private val playIconNormalColor = Color(0xFF57965C)
private val playIconWarningColor = Color(0xFF73BD79)
private val downloadIconColor = Color(0xFFCED0D6)
private val dropdownBackgroundHoverColor = Color(0xFF393B40)
private val dropdownBackgroundSelectedColor = Color(0xFF2E436E)
private val dropdownBackgroundHoverSelectedColor = Color(0xFF35538F)
private val dropdownBorderColor = Color(0xFF43454A)
private val arrowIconColor = Color(0xFF9DA0A8)

@Composable
fun RightToolbar(
    viewModel: ToolbarViewModel,
    projectFileName: String,
    onStatusBarUiStateChanged: (StatusBarUiState) -> Unit,
    statusBarUiState: StatusBarUiState,
    navigator: Navigator,
    modifier: Modifier = Modifier,
) {
    val availableDevices by viewModel.availableDevices.collectAsState()
    val project by viewModel.editingProject.collectAsState()
    val buttonEnabled = statusBarUiState !is StatusBarUiState.Loading
    val buttonModifier =
        if (buttonEnabled) {
            Modifier.hoverIconClickable().hoverOverlay()
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
        modifier = modifier.testTag(TOOLBAR_TEST_TAG),
    ) {
        val selectedDevice by viewModel.selectedDevice.collectAsState()

        Spacer(Modifier.weight(1f))

        val issues = project.generateTrackableIssues()
        IssuesBadge(
            project = project,
            issues = issues,
            navigator = navigator,
            onSetPendingFocus = viewModel::onSetPendingFocuses,
        )
        Spacer(Modifier.width(16.dp))

        DeviceDropdown(
            availableDevices = availableDevices,
            selectedDevice = selectedDevice,
            onSelectedDeviceNameChanged = viewModel::onSelectedDeviceNameChanged,
        )

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
                    Modifier.testTag(TOOLBAR_RUN_BUTTON_TEST_TAG).then(
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
                            playIconNormalColor
                        } else {
                            playIconWarningColor
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
                    tint = downloadIconColor,
                )
            }
        }

        Spacer(modifier = Modifier.width(80.dp))
    }
}

@Composable
fun DeviceDropdown(
    availableDevices: List<Device>,
    selectedDevice: Device,
    onSelectedDeviceNameChanged: (String) -> Unit,
) {
    @Composable
    fun DeviceIconNew(device: Device) {
        val showBadge by remember(device) {
            mutableStateOf(
                when (device) {
                    is Device.AndroidEmulator -> {
                        device.status == EmulatorStatus.Device
                    }

                    is Device.IosSimulator -> {
                        device.status == SimulatorStatus.Booted
                    }

                    Device.Web -> true
                },
            )
        }

        val icon =
            when (device) {
                is Device.AndroidEmulator -> {
                    ComposeFlowIcons.AndroiddeviceDark
                }

                is Device.IosSimulator -> {
                    ComposeFlowIcons.IphonedeviceDark
                }

                Device.Web -> {
                    ComposeFlowIcons.WebDark
                }
            }

        BadgedBox(
            badge = {
                if (showBadge) {
                    androidx.compose.material3.Badge(
                        containerColor = Color.Green,
                    )
                }
            },
        ) {
            Icon(
                imageVector = icon,
                tint = Color.Unspecified,
                contentDescription = "device icon",
                modifier = Modifier.size(15.dp),
            )
        }
    }

    var isExpanded by remember { mutableStateOf(false) }
    Box {
        Box(
            modifier =
                Modifier
                    .wrapContentWidth()
                    .clickable {
                        isExpanded = !isExpanded
                    }.hoverOverlay(dropdownBackgroundHoverColor, 4.dp)
                    .width(IntrinsicSize.Max)
                    .height(24.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp, vertical = 3.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DeviceIconNew(selectedDevice)
                Text(
                    text = selectedDevice.deviceName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(
                    modifier = Modifier.width(4.dp),
                )

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    tint = arrowIconColor,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                )
            }
        }

        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(Dp.Hairline, dropdownBorderColor),
            modifier = Modifier.width(IntrinsicSize.Max),
        ) {
            availableDevices.forEach { device ->
                val isSelected = device == selectedDevice
                val interactionSource = remember { MutableInteractionSource() }
                val isHovered by interactionSource.collectIsHoveredAsState()

                val backgroundColor =
                    when {
                        isHovered && isSelected -> dropdownBackgroundHoverSelectedColor
                        isHovered -> dropdownBackgroundHoverColor
                        isSelected -> dropdownBackgroundSelectedColor
                        else -> Color.Unspecified
                    }

                Box(
                    modifier =
                        Modifier
                            .padding(horizontal = 8.dp)
                            .padding(top = 2.dp)
                            .hoverable(interactionSource)
                            .clickable {
                                onSelectedDeviceNameChanged(device.deviceName)
                                isExpanded = false
                            }.background(backgroundColor, RoundedCornerShape(4.dp)),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        DeviceIconNew(device)
                        Text(
                            text = device.deviceName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}

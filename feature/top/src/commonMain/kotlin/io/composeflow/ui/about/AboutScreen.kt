package io.composeflow.ui.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import com.github.michaelbull.result.mapBoth
import dev.hydraulic.conveyor.control.SoftwareUpdateController
import io.composeflow.BillingClient
import io.composeflow.BuildConfig
import io.composeflow.ComposeFlow_Logo_Symbol
import io.composeflow.Res
import io.composeflow.check_for_update
import io.composeflow.no_updates_available
import io.composeflow.open_source_licenses
import io.composeflow.ui.LocalOnAllDialogsClosed
import io.composeflow.ui.LocalOnAnyDialogIsShown
import io.composeflow.ui.openInBrowser
import io.composeflow.ui.popup.LicenseDialog
import io.composeflow.ui.popup.SimpleConfirmationDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

val updateController: SoftwareUpdateController? = SoftwareUpdateController.getInstance()
val canDoOnlineUpdates get() = updateController?.canTriggerUpdateCheckUI() == SoftwareUpdateController.Availability.AVAILABLE
val billingClient = BillingClient()

@Composable
fun AboutScreen(modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    val scale = density.density / 2
    var showLicenseDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Image(
            painterResource(Res.drawable.ComposeFlow_Logo_Symbol),
            contentDescription = "ComposeFlow logo",
            modifier = Modifier.scale(scale),
        )
        Text(
            text = "ComposeFlow",
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier.padding(bottom = 24.dp),
        )

        VersionCell()

        // License button
        TextButton(
            onClick = { showLicenseDialog = true },
            modifier = Modifier.padding(top = 8.dp),
        ) {
            Text(stringResource(Res.string.open_source_licenses))
        }
    }

    // License dialog
    val onAnyDialogIsShown = LocalOnAnyDialogIsShown.current
    val onAllDialogsClosed = LocalOnAllDialogsClosed.current
    if (showLicenseDialog) {
        onAnyDialogIsShown()

        LicenseDialog(
            libraries = LibraryData.libraries,
            onCloseClick = {
                showLicenseDialog = false
                onAllDialogsClosed()
            },
        )
    }
}

@Composable
private fun VersionCell() {
    Column {
        val coroutineScope = rememberCoroutineScope()
        var remoteVersion by remember { mutableStateOf("Checking...") }
        var updateAvailable by remember { mutableStateOf(false) }
        var noUpdatesAvailableDialogOpen by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val remoteVersionObj: SoftwareUpdateController.Version? =
                        updateController?.currentVersionFromRepository
                    remoteVersion = remoteVersionObj?.version ?: "Unknown"
                    updateAvailable =
                        (remoteVersionObj?.compareTo(updateController.currentVersion) ?: 0) > 0
                } catch (e: Exception) {
                    remoteVersion = "Error: ${e.message}"
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "App version",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.size(8.dp))
            Text(
                updateController?.currentVersion?.version ?: "Checking version...",
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        TextButton(
            onClick = {
                if (updateAvailable && canDoOnlineUpdates) {
                    updateController?.triggerUpdateCheckUI()
                } else {
                    noUpdatesAvailableDialogOpen = true
                }
            },
        ) {
            Text(stringResource(Res.string.check_for_update))
        }

        if (!BuildConfig.isRelease) {
            TextButton(
                onClick = {
                    coroutineScope.launch {
                        billingClient.createPricingTableLink().mapBoth(
                            success = {
                                openInBrowser(it)
                            },
                            failure = {
                                Logger.e("Failed to create pricing table link", it)
                            },
                        )
                    }
                },
            ) {
                Text("料金ページ")
            }
        }

        val onAnyDialogIsShown = LocalOnAnyDialogIsShown.current
        val onAllDialogsClosed = LocalOnAllDialogsClosed.current
        if (noUpdatesAvailableDialogOpen) {
            onAnyDialogIsShown()
            SimpleConfirmationDialog(
                text = stringResource(Res.string.no_updates_available),
                onCloseClick = {
                    noUpdatesAvailableDialogOpen = false
                    onAllDialogsClosed()
                },
                onConfirmClick = {
                    noUpdatesAvailableDialogOpen = false
                    onAllDialogsClosed()
                },
                positiveText = "Ok",
                positiveButtonColor = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

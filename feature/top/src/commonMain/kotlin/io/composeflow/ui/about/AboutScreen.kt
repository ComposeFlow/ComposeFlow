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
import io.composeflow.BuildConfig
import io.composeflow.ComposeFlow_Logo_Symbol
import io.composeflow.Res
import io.composeflow.check_for_update
import io.composeflow.no_updates_available
import io.composeflow.open_source_licenses
import io.composeflow.pricing_page
import io.composeflow.ui.LocalOnAllDialogsClosed
import io.composeflow.ui.LocalOnAnyDialogIsShown
import io.composeflow.ui.openInBrowser
import io.composeflow.ui.popup.LicenseDialog
import io.composeflow.ui.popup.SimpleConfirmationDialog
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

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
        val updateService = remember { SoftwareUpdateService() }
        var currentVersion by remember { mutableStateOf<VersionInfo?>(null) }
        var remoteVersion by remember { mutableStateOf<VersionInfo?>(null) }
        var noUpdatesAvailableDialogOpen by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            coroutineScope.launch {
                currentVersion = updateService.getCurrentVersion()
                remoteVersion = updateService.checkForUpdates()
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "App version",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.size(8.dp))
            Text(
                currentVersion?.version ?: "Checking version...",
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        // Only show update button if the platform supports it
        if (updateService.canDoOnlineUpdates()) {
            TextButton(
                onClick = {
                    if (remoteVersion?.isUpdateAvailable == true) {
                        updateService.triggerUpdateUI()
                    } else {
                        noUpdatesAvailableDialogOpen = true
                    }
                },
            ) {
                Text(stringResource(Res.string.check_for_update))
            }
        }

        val billingService = remember { BillingService() }
        if (!BuildConfig.isRelease && billingService.isAvailable()) {
            TextButton(
                onClick = {
                    coroutineScope.launch {
                        billingService.createPricingTableLink()?.let {
                            openInBrowser(it)
                        }
                    }
                },
            ) {
                Text(stringResource(Res.string.pricing_page))
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

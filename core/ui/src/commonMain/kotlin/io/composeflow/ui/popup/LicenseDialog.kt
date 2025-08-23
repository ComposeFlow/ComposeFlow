package io.composeflow.ui.popup

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.composeflow.Res
import io.composeflow.close
import io.composeflow.open_source_licenses
import io.composeflow.ui.common.ComposeFlowTheme
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

data class Library(
    val name: String,
    val group: String,
    val version: String,
)

@Composable
fun LicenseDialog(
    libraries: List<Library>,
    onCloseClick: () -> Unit,
) {
    PositionCustomizablePopup(
        onDismissRequest = onCloseClick,
        onKeyEvent = {
            if (it.key == Key.Escape) {
                onCloseClick()
                true
            } else {
                false
            }
        },
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.size(width = 600.dp, height = 500.dp),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
            ) {
                Text(
                    text = stringResource(Res.string.open_source_licenses),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp),
                )

                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                ) {
                    val scrollState = rememberScrollState()

                    Column(
                        modifier =
                            Modifier
                                .verticalScroll(scrollState)
                                .fillMaxWidth()
                                .padding(end = 8.dp),
                    ) {
                        libraries.forEachIndexed { index, library ->
                            if (index > 0) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                )
                            }

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    text = library.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                )
                                Text(
                                    text = "${library.group}:${library.version}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 2.dp),
                                )
                            }
                        }
                    }

                    VerticalScrollbar(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        adapter = rememberScrollbarAdapter(scrollState),
                    )
                }

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onCloseClick) {
                        Text(stringResource(Res.string.close))
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemedLicenseDialogPreview(useDarkTheme: Boolean) {
    ComposeFlowTheme(useDarkTheme = useDarkTheme) {
        val sampleLibraries =
            listOf(
                Library("Compose Multiplatform", "org.jetbrains.compose", "1.5.11"),
                Library("Kotlin", "org.jetbrains.kotlin", "1.9.21"),
                Library("Ktor Client", "io.ktor", "2.3.7"),
                Library("Kotlinx Serialization", "org.jetbrains.kotlinx", "1.6.2"),
                Library("PreCompose", "moe.tlaster", "1.5.10"),
                Library("KotlinPoet", "com.squareup", "1.15.3"),
                Library("Koin", "io.insert-koin", "3.5.3"),
                Library("Material3", "androidx.compose.material3", "1.5.4"),
                Library("Reorderable", "sh.calvin.reorderable", "3.0.0"),
                Library("Color Picker", "com.godaddy.android", "1.0.0"),
            )

        LicenseDialog(
            libraries = sampleLibraries,
            onCloseClick = {},
        )
    }
}

@Preview
@Composable
fun LicenseDialogPreview_Light() {
    ThemedLicenseDialogPreview(useDarkTheme = false)
}

@Preview
@Composable
fun LicenseDialogPreview_Dark() {
    ThemedLicenseDialogPreview(useDarkTheme = true)
}

@Composable
private fun ThemedLicenseDialogFewItemsPreview(useDarkTheme: Boolean) {
    ComposeFlowTheme(useDarkTheme = useDarkTheme) {
        val sampleLibraries =
            listOf(
                Library("Compose Multiplatform", "org.jetbrains.compose", "1.5.11"),
                Library("Kotlin", "org.jetbrains.kotlin", "1.9.21"),
            )

        LicenseDialog(
            libraries = sampleLibraries,
            onCloseClick = {},
        )
    }
}

@Preview
@Composable
fun LicenseDialogFewItemsPreview_Light() {
    ThemedLicenseDialogFewItemsPreview(useDarkTheme = false)
}

@Preview
@Composable
fun LicenseDialogFewItemsPreview_Dark() {
    ThemedLicenseDialogFewItemsPreview(useDarkTheme = true)
}

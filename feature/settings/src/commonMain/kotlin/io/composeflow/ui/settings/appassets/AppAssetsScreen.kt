package io.composeflow.ui.settings.appassets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import io.composeflow.Res
import io.composeflow.auth.LocalFirebaseIdToken
import io.composeflow.model.parameter.wrapper.ColorWrapper
import io.composeflow.model.project.Project
import io.composeflow.model.project.asset.UploadResult
import io.composeflow.ui.LocalOnShowSnackbar
import io.composeflow.ui.Tooltip
import io.composeflow.ui.labeledbox.LabeledBorderBox
import io.composeflow.ui.modifier.hoverIconClickable
import io.composeflow.ui.propertyeditor.ColorPropertyEditor
import io.composeflow.ui.propertyeditor.DropdownProperty
import io.composeflow.ui.utils.asIconComposable
import io.composeflow.upload_failed
import io.composeflow.upload_file
import io.composeflow.upload_succeeded
import io.github.vinceglb.filekit.core.FileKit
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.viewModel
import org.jetbrains.compose.resources.stringResource

@Composable
fun AppAssetsScreen(
    project: Project,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val onShowSnackbar = LocalOnShowSnackbar.current
    val firebaseIdToken = LocalFirebaseIdToken.current
    val viewModel =
        viewModel(modelClass = AppAssetsViewModel::class) {
            AppAssetsViewModel(
                project = project,
                firebaseIdToken = firebaseIdToken,
            )
        }
    val uploadResult = viewModel.uploadResult.collectAsState().value
    Column(modifier = modifier.padding(16.dp)) {
        Row {
            Text(
                "App Assets",
                style = MaterialTheme.typography.titleLarge,
                modifier =
                    Modifier.padding(
                        end = 8.dp,
                        bottom = 24.dp,
                    ),
            )

            Spacer(Modifier.size(8.dp))

            when (uploadResult) {
                UploadResult.Uploading -> {
                    CircularProgressIndicator()
                }

                is UploadResult.Failure -> {
                    val uploadFailed = stringResource(Res.string.upload_failed)
                    Text(
                        uploadFailed + ": ${uploadResult.message ?: ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                UploadResult.NotStarted -> {}
                is UploadResult.Success -> {
                    val uploadSucceeded = stringResource(Res.string.upload_succeeded)
                    coroutineScope.launch {
                        onShowSnackbar(
                            uploadSucceeded + ": ${uploadResult.blobInfoWrapper.fileName}",
                            null,
                        )
                    }
                }
            }
        }
        SplashScreenEditorArea(
            project = project,
            callbacks =
                AppAssetCallbacks(
                    onChangeAndroidSplashBackgroundColor = viewModel::onChangeAndroidSplashBackgroundColor,
                    onChangeAndroidSplashScreenImageIcon = viewModel::onChangeAndroidSplashScreenImageIcon,
                    onUploadAndroidSplashScreenImageFile = viewModel::onUploadAndroidSplashScreenImageIcon,
                    onChangeIosSplashBackgroundColor = viewModel::onChangeIosSplashBackgroundColor,
                    onChangeIosSplashScreenImageIcon = viewModel::onChangeIosSplashScreenImageIcon,
                    onUploadIosSplashScreenImageFile = viewModel::onUploadIosSplashScreenImageIcon,
                ),
        )
    }
}

@Composable
fun SplashScreenEditorArea(
    project: Project,
    callbacks: AppAssetCallbacks,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier) {
        item {
            Text(
                "Splash Screen",
                style = MaterialTheme.typography.titleMedium,
                modifier =
                    Modifier.padding(
                        end = 8.dp,
                        bottom = 16.dp,
                    ),
            )
        }

        item {
            AndroidSplashScreenEditorArea(
                callbacks = callbacks,
                project = project,
            )
            Spacer(Modifier.size(16.dp))
        }

        item {
            IosSplashScreenEditorArea(
                callbacks = callbacks,
                project = project,
            )
        }
    }
}

@Composable
fun AndroidSplashScreenEditorArea(
    project: Project,
    callbacks: AppAssetCallbacks,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    Column {
        Text(
            "Android",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 4.dp),
        )

        Text(
            text =
                buildAnnotatedString {
                    withStyle(
                        style = MaterialTheme.typography.bodySmall.toSpanStyle(),
                    ) {
                        append("Must be a vector drawables xml file. See ")

                        withLink(LinkAnnotation.Url(url = "https://developer.android.com/develop/ui/views/launch/splash-screen#elements")) {
                            withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                                append("the document")
                            }
                        }
                        append(" for more details")
                    }
                },
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier,
        ) {
            val blobInfo =
                project.appAssetHolder.splashScreenInfoHolder.androidSplashScreenImageBlobInfo.value

            LabeledBorderBox(
                label = "Splash screen vector drawable",
                modifier = Modifier.width(340.dp),
            ) {
                val iconAssetsMap =
                    // The first item means unset the icon
                    listOf(0 to null) +
                        project.assetHolder.icons.mapIndexed { i, icon ->
                            i + 1 to icon
                        }

                val userId = LocalFirebaseIdToken.current.user_id
                DropdownProperty(
                    project = project,
                    items =
                        iconAssetsMap
                            .map { it.second }
                            .filter { it == null || it.fileName.endsWith(".xml") },
                    dropDownMenuText = {
                        Row {
                            it?.asIconComposable(
                                userId = userId,
                                projectId = project.id.toString(),
                            )

                            Text(
                                it?.fileName ?: "",
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                    },
                    displayText = {
                        blobInfo?.let {
                            Text(
                                it.fileName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    },
                    onValueChanged = { index, _ ->
                        callbacks.onChangeAndroidSplashScreenImageIcon(
                            iconAssetsMap[index].second,
                        )
                    },
                    selectedItem = blobInfo,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }

            val uploadFile = stringResource(Res.string.upload_file)
            Tooltip(uploadFile) {
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            val file =
                                FileKit.pickFile(
                                    type = PickerType.File(extensions = listOf("xml")),
                                    mode = PickerMode.Single,
                                    title = "Pick an vector drawable file",
                                )
                            file?.let {
                                callbacks.onUploadAndroidSplashScreenImageFile(it)
                            }
                        }
                    },
                    modifier = Modifier.hoverIconClickable(),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FileUpload,
                        contentDescription = uploadFile,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            blobInfo?.let {
                Spacer(Modifier.size(16.dp))
                val initialColor =
                    project.appAssetHolder.splashScreenInfoHolder.androidSplashScreenBackgroundColor.value
                ColorPropertyEditor(
                    label = "Background color",
                    initialColor =
                        if (initialColor != null) {
                            ColorWrapper(
                                themeColor = null,
                                color = initialColor,
                            )
                        } else {
                            null
                        },
                    extendedColors = project.themeHolder.colorSchemeHolder.extendedColors.value,
                    onColorUpdated = {
                        callbacks.onChangeAndroidSplashBackgroundColor(it)
                    },
                    onThemeColorSelected = {},
                    onColorDeleted = {
                        callbacks.onChangeAndroidSplashBackgroundColor(null)
                    },
                    includeThemeColor = false,
                    modifier = Modifier.width(280.dp),
                )
            }
        }
    }
}

@Composable
fun IosSplashScreenEditorArea(
    project: Project,
    callbacks: AppAssetCallbacks,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    Column {
        Text(
            "iOS",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 4.dp),
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier,
        ) {
            val blobInfo =
                project.appAssetHolder.splashScreenInfoHolder.iOSSplashScreenImageBlobInfo.value

            LabeledBorderBox(
                label = "Splash screen image",
                modifier = Modifier.width(340.dp),
            ) {
                val imageAssetsMap =
                    // The first item means unset the icon
                    listOf(0 to null) +
                        project.assetHolder.images.mapIndexed { i, icon ->
                            i + 1 to icon
                        }

                val userId = LocalFirebaseIdToken.current.user_id
                DropdownProperty(
                    project = project,
                    items = imageAssetsMap.map { it.second },
                    dropDownMenuText = {
                        Row {
                            it?.asIconComposable(
                                userId = userId,
                                projectId = project.id.toString(),
                            )

                            Text(
                                it?.fileName ?: "",
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                    },
                    displayText = {
                        blobInfo?.let {
                            Text(
                                it.fileName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    },
                    onValueChanged = { index, _ ->
                        callbacks.onChangeIosSplashScreenImageIcon(
                            imageAssetsMap[index].second,
                        )
                    },
                    selectedItem = blobInfo,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }

            val uploadFile = stringResource(Res.string.upload_file)
            Tooltip(uploadFile) {
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            val file =
                                FileKit.pickFile(
                                    type = PickerType.Image,
                                    mode = PickerMode.Single,
                                    title = "Pick an image for splash screen",
                                )
                            file?.let {
                                callbacks.onUploadIosSplashScreenImageFile(it)
                            }
                        }
                    },
                    modifier = Modifier.hoverIconClickable(),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FileUpload,
                        contentDescription = uploadFile,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            blobInfo?.let {
                Spacer(Modifier.size(16.dp))
                val initialColor =
                    project.appAssetHolder.splashScreenInfoHolder.iOSSplashScreenBackgroundColor.value
                ColorPropertyEditor(
                    label = "Background color",
                    initialColor =
                        if (initialColor != null) {
                            ColorWrapper(
                                themeColor = null,
                                color = initialColor,
                            )
                        } else {
                            null
                        },
                    extendedColors = project.themeHolder.colorSchemeHolder.extendedColors.value,
                    onColorUpdated = {
                        callbacks.onChangeIosSplashBackgroundColor(it)
                    },
                    onThemeColorSelected = {},
                    onColorDeleted = {
                        callbacks.onChangeIosSplashScreenImageIcon(null)
                    },
                    includeThemeColor = false,
                    modifier = Modifier.width(280.dp),
                )
            }
        }
    }
}

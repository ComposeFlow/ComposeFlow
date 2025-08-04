package io.composeflow.ui.top

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.seiko.imageloader.ImageLoader
import com.seiko.imageloader.LocalImageLoader
import com.seiko.imageloader.createDefault
import io.composeflow.ProjectEditorView
import io.composeflow.Res
import io.composeflow.auth.FirebaseIdToken
import io.composeflow.auth.LocalFirebaseIdToken
import io.composeflow.formatter.ProvideCodeTheme
import io.composeflow.log_out
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.Screen
import io.composeflow.model.settings.DarkThemeSetting
import io.composeflow.profile_image
import io.composeflow.ui.ProvideCloseDialogCallback
import io.composeflow.ui.ProvideShowDialogCallback
import io.composeflow.ui.TopDestination
import io.composeflow.ui.about.AboutScreen
import io.composeflow.ui.common.ComposeFlowTheme
import io.composeflow.ui.image.AsyncImage
import io.composeflow.ui.jewel.ProvideLazyTreeStyle
import io.composeflow.ui.jewel.TitleBarContent
import io.composeflow.ui.modifier.hoverIconClickable
import io.composeflow.ui.project.ProjectScreen
import io.composeflow.ui.settings.AccountSettingsScreen
import moe.tlaster.precompose.viewmodel.viewModel
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.jewel.intui.standalone.theme.IntUiTheme

@Composable
fun TopScreen(
    onLogOut: () -> Unit,
    onTitleBarRightContentSet: (TitleBarContent) -> Unit,
    onTitleBarLeftContentSet: (TitleBarContent) -> Unit,
    isAnonymous: Boolean = false,
) {
    // To reset the TitleBar content when the user navigates back to top screen
    onTitleBarLeftContentSet({})
    onTitleBarRightContentSet({})

    val firebaseIdToken = if (!isAnonymous) LocalFirebaseIdToken.current else null
    val viewModel =
        viewModel(
            modelClass = TopScreenViewModel::class,
            keys = listOf(firebaseIdToken?.user_id ?: "anonymous"),
        ) { TopScreenViewModel(firebaseIdToken) }
    val settings = viewModel.settings.collectAsState()
    val useComposeFlowDarkTheme =
        when (settings.value.composeBuilderDarkThemeSetting) {
            DarkThemeSetting.System -> isSystemInDarkTheme()
            DarkThemeSetting.Light -> false
            DarkThemeSetting.Dark -> true
        }

    val projectUiState = viewModel.projectListUiState.collectAsState().value

    CompositionLocalProvider(LocalImageLoader provides ImageLoader.createDefault()) {
        ComposeFlowTheme(useDarkTheme = useComposeFlowDarkTheme) {
            IntUiTheme(isDark = useComposeFlowDarkTheme) {
                ProvideLazyTreeStyle {
                    ProvideCodeTheme(useDarkTheme = useComposeFlowDarkTheme) {
                        var anyDialogShown by remember { mutableStateOf(false) }
                        ProvideShowDialogCallback(
                            onAnyDialogIsShown = {
                                anyDialogShown = true
                            },
                        ) {
                            ProvideCloseDialogCallback(
                                onAllDialogsClosed = {
                                    anyDialogShown = false
                                },
                            ) {
                                val overlayModifier =
                                    if (anyDialogShown) {
                                        Modifier
                                            .alpha(0.2f)
                                            .blur(
                                                8.dp,
                                                edgeTreatment = BlurredEdgeTreatment.Unbounded,
                                            )
                                    } else {
                                        Modifier
                                    }

                                Box(modifier = overlayModifier) {
                                    when (projectUiState) {
                                        is ProjectUiState.HasNotSelected.ProjectListLoaded ->
                                            TopNavigationDrawerScreen(
                                                onCreateProject = viewModel::onCreateProject,
                                                onCreateProjectWithScreens = viewModel::onCreateProjectWithScreens,
                                                onDeleteProject = viewModel::onDeleteProject,
                                                onProjectSelected = viewModel::onProjectSelected,
                                                onLogOut = onLogOut,
                                                projectUiState = projectUiState,
                                                useComposeFlowDarkTheme = useComposeFlowDarkTheme,
                                                isAnonymous = isAnonymous,
                                            )

                                        ProjectUiState.HasNotSelected.ProjectListLoading ->
                                            TopNavigationDrawerScreen(
                                                onCreateProject = viewModel::onCreateProject,
                                                onCreateProjectWithScreens = viewModel::onCreateProjectWithScreens,
                                                onDeleteProject = viewModel::onDeleteProject,
                                                onProjectSelected = viewModel::onProjectSelected,
                                                onLogOut = onLogOut,
                                                projectUiState = ProjectUiState.HasNotSelected.ProjectListLoading,
                                                useComposeFlowDarkTheme = useComposeFlowDarkTheme,
                                                isAnonymous = isAnonymous,
                                            )

                                        is ProjectUiState.Selected -> {
                                            ProjectEditorView(
                                                projectId = projectUiState.project.id,
                                                onTitleBarRightContentSet = onTitleBarRightContentSet,
                                                onTitleBarLeftContentSet = onTitleBarLeftContentSet,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DestinationScreenWrapper(content: @Composable () -> Unit) {
    Row(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
    ) {
        Spacer(Modifier.width(48.dp))
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(vertical = 16.dp)
                    .background(color = MaterialTheme.colorScheme.surface),
        ) {
            content()
        }
        Spacer(Modifier.width(48.dp))
    }
}

@Composable
private fun TopNavigationDrawerScreen(
    onCreateProject: (String, String) -> Unit,
    onCreateProjectWithScreens: (project: Project, List<Screen>) -> Unit,
    onDeleteProject: (String) -> Unit,
    onProjectSelected: (Project) -> Unit,
    onLogOut: () -> Unit,
    projectUiState: ProjectUiState.HasNotSelected,
    useComposeFlowDarkTheme: Boolean,
    isAnonymous: Boolean = false,
) {
    var selectedDestination by remember { mutableStateOf(TopDestination.Project) }

    PermanentNavigationDrawer(
        drawerContent = {
            PermanentDrawerSheet(
                modifier = Modifier.width(280.dp),
            ) {
                TopDestination.entries.forEach {
                    Spacer(Modifier.height(8.dp))
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                it.icon,
                                contentDescription = null,
                            )
                        },
                        label = { Text(it.title) },
                        selected = it == selectedDestination,
                        onClick = {
                            selectedDestination = it
                        },
                        modifier =
                            Modifier
                                .heightIn(max = 40.dp)
                                .padding(horizontal = 12.dp),
                    )
                }

                Spacer(Modifier.weight(1f))
                if (!isAnonymous) {
                    UserProfileContainer(onLogOut = onLogOut)
                } else {
                    AnonymousUserContainer(onLogOut = onLogOut)
                }
                Spacer(Modifier.size(16.dp))
            }
        },
        content = {
            Scaffold {
                ComposeFlowTheme(useDarkTheme = useComposeFlowDarkTheme) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        when (projectUiState) {
                            ProjectUiState.HasNotSelected.ProjectListLoading -> {
                                LoadingScreen()
                            }

                            is ProjectUiState.HasNotSelected.ProjectListLoaded -> {
                                when (selectedDestination) {
                                    TopDestination.Project -> {
                                        ProjectScreen(
                                            projectUiStateList = projectUiState.projectList,
                                            onCreateProject = onCreateProject,
                                            onCreateProjectWithScreens = onCreateProjectWithScreens,
                                            onDeleteProject = onDeleteProject,
                                            onProjectSelected = onProjectSelected,
                                            modifier = Modifier.padding(16.dp),
                                        )
                                    }

                                    TopDestination.Settings -> {
                                        DestinationScreenWrapper {
                                            AccountSettingsScreen()
                                        }
                                    }

                                    TopDestination.About -> {
                                        DestinationScreenWrapper {
                                            AboutScreen()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
    )
}

@Composable
private fun AnonymousUserContainer(onLogOut: () -> Unit) {
    Column {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.wrapContentSize(),
        ) {
            Box(
                modifier =
                    Modifier
                        .padding(all = 8.dp)
                        .size(width = 48.dp, height = 48.dp)
                        .clip(shape = CircleShape)
                        .background(color = MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "A",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            Column(
                modifier = Modifier.wrapContentSize().padding(vertical = 8.dp),
            ) {
                Text(
                    text = "Anonymous User",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "Limited features available",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        TextButton(
            onClick = {
                onLogOut()
            },
            modifier = Modifier.padding(start = 8.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.Logout,
                contentDescription = "Back to login",
            )
            Text("Back to login")
        }
    }
}

@Composable
private fun UserProfileContainer(onLogOut: () -> Unit) {
    val firebaseIdToken = LocalFirebaseIdToken.current
    val signedInToken = firebaseIdToken as? FirebaseIdToken.SignedInToken

    if (signedInToken != null) {
        Column {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.wrapContentSize(),
            ) {
                AsyncImage(
                    url = signedInToken.picture,
                    contentDescription = stringResource(Res.string.profile_image),
                    modifier =
                        Modifier
                            .padding(all = 8.dp)
                            .size(
                                width = 48.dp,
                                height = 48.dp,
                            ).clip(shape = CircleShape)
                            .hoverIconClickable(),
                )
                Column(
                    modifier = Modifier.wrapContentSize().padding(vertical = 8.dp),
                ) {
                    Text(
                        text = signedInToken.name,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = signedInToken.email,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }

            TextButton(
                onClick = {
                    onLogOut()
                },
                modifier = Modifier.padding(start = 8.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Logout,
                    contentDescription = stringResource(Res.string.log_out),
                )
                Text(stringResource(Res.string.log_out))
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

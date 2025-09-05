package io.composeflow

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.application
import co.touchlab.kermit.Logger
import co.touchlab.kermit.SystemWriter
import dev.hydraulic.conveyor.control.SoftwareUpdateController
import io.composeflow.analytics.Analytics
import io.composeflow.analytics.AnalyticsTracker
import io.composeflow.analytics.createAnalytics
import io.composeflow.custom.ComposeFlowIcons
import io.composeflow.custom.composeflowicons.ComposeFlowLogo
import io.composeflow.di.ServiceLocator
import io.composeflow.logger.logger
import io.composeflow.platform.CloudProjectSaverRunner
import io.composeflow.ui.common.ComposeFlowTheme
import io.composeflow.ui.login.LOGIN_ROUTE
import io.composeflow.ui.uibuilder.onboarding.OnboardingLayoutOffsets
import io.composeflow.ui.uibuilder.onboarding.ProvideOnboardingLayoutOffsets
import io.composeflow.ui.window.ComposeFlowWindow
import io.composeflow.ui.window.frame.LocalWindow
import io.composeflow.ui.window.rememberWindowController
import io.sentry.Sentry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import moe.tlaster.precompose.PreComposeApp
import moe.tlaster.precompose.navigation.rememberNavigator
import moe.tlaster.precompose.viewmodel.viewModel
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.intui.standalone.theme.IntUiTheme
import org.jetbrains.jewel.intui.standalone.theme.darkThemeDefinition
import org.jetbrains.jewel.intui.window.decoratedWindow
import org.jetbrains.jewel.intui.window.styling.dark
import org.jetbrains.jewel.ui.ComponentStyling
import org.jetbrains.jewel.window.styling.TitleBarStyle
import java.nio.file.Paths
import kotlin.concurrent.thread

val updateController: SoftwareUpdateController? = SoftwareUpdateController.getInstance()
val canDoOnlineUpdates get() = updateController?.canTriggerUpdateCheckUI() == SoftwareUpdateController.Availability.AVAILABLE

fun main() {
    Logger.setLogWriters(SystemWriter())

    logger.info("release = ${BuildConfig.isRelease}")
    logger.info("Current directory = ${Paths.get("").toAbsolutePath()}")
    val envVariables = System.getenv()
    for ((key, value) in envVariables) {
        logger.info("$key = $value")
    }

    // Initialize analytics
    val analytics = createAnalytics()
    ServiceLocator.put(analytics)

    // Initialize PostHog only in release mode with API key from BuildConfig
    if (updateController != null && BuildConfig.POSTHOG_API_KEY.isNotBlank()) {
        analytics.initialize(BuildConfig.POSTHOG_API_KEY)
        logger.info("Analytics initialized")

        // Track app startup
        AnalyticsTracker.trackAppStartup()
    } else if (updateController != null) {
        logger.warn("PostHog API key not configured in local.properties")
    }

    // updateController != null implies that app is built from Conveyor (= release mode)
    if (updateController != null && BuildConfig.SENTRY_DSN.isNotBlank()) {
        Sentry.init {
            it.setDsn(BuildConfig.SENTRY_DSN)
            it.setTracesSampleRate(1.0)
            it.isAttachStacktrace = true
            it.isAttachThreads = true
        }
        logger.info("Sentry analytics initialized")
    } else if (updateController != null) {
        logger.warn("Sentry DSN not configured in local.properties")
    }

    try {
        application {
            App(
                onExitApplication = {
                    Runtime.getRuntime().addShutdownHook(
                        thread(start = false) {
                            println("Exit application callback detected. Syncing project yaml before exiting...")
                            runBlocking {
                                CloudProjectSaverRunner.syncProjectYaml()
                            }
                            println("Sync completed.")

                            // Shutdown analytics
                            try {
                                ServiceLocator.get<Analytics>().shutdown()
                                println("Analytics shutdown completed.")
                            } catch (e: Exception) {
                                println("Error during analytics shutdown: ${e.message}")
                            }
                        },
                    )
                    exitApplication()
                },
            )
        }
    } catch (e: Exception) {
        Sentry.captureException(e)
        logger.error(e.stackTraceToString())
        throw e
    }
}

@Composable
fun App(onExitApplication: () -> Unit) {
    IntUiTheme(
        JewelTheme.darkThemeDefinition(),
        ComponentStyling.decoratedWindow(
            titleBarStyle = TitleBarStyle.dark(),
        ),
        swingCompatMode = false,
    ) {
        ComposeFlowTheme {
            val isDebug = if (!BuildConfig.isRelease) "-debug" else ""
            val windowController =
                rememberWindowController(
                    title = "ComposeFlow$isDebug",
                    icon = rememberVectorPainter(ComposeFlowIcons.ComposeFlowLogo),
                )

            ComposeFlowWindow(
                windowController = windowController,
                onCloseRequest = onExitApplication,
            ) {
                val window = LocalWindow.current

                CompositionLocalProvider(
                    moe.tlaster.precompose.LocalWindow provides window,
                ) {
                    ProvideOnboardingLayoutOffsets(
                        offsets =
                            OnboardingLayoutOffsets(
                                titleBarHeight = 30.dp, // Jewel TitleBar default height
                                navigationRailWidth = 40.dp, // From ProjectEditorView
                                statusBarHeight = 24.dp, // Estimated status bar height
                            ),
                    ) {
                        PreComposeApp {
                            val navigator = rememberNavigator()
                            val titleBarViewModel =
                                viewModel(modelClass = TitleBarViewModel::class) { TitleBarViewModel() }
                            val titleBarLeftContent by titleBarViewModel.titleBarLeftContent.collectAsState()
                            val titleBarRightContent by titleBarViewModel.titleBarRightContent.collectAsState()

                            windowController.start = {
                                TitleBarView(
                                    title = windowController.title.orEmpty(),
                                    onComposeFlowLogoClicked = {
                                        navigator.navigate(LOGIN_ROUTE)
                                    },
                                    titleBarRightContent = titleBarRightContent,
                                    titleBarLeftContent = titleBarLeftContent,
                                )
                            }
                            val versionAskedToUpdate =
                                titleBarViewModel.versionAskedToUpdate.collectAsState().value
                            CheckForUpdateDialog(
                                onShowUpdateDialog = {
                                    logger.info("onShowUpdateDialog: $it")
                                    titleBarViewModel.onSaveVersionAskedToUpdate(it)
                                },
                                versionAskedToUpdate = versionAskedToUpdate,
                            )

                            ComposeFlowApp(
                                navigator = navigator,
                                onTitleBarRightContentSet = {
                                    titleBarViewModel.onTitleBarRightContentSet(it)
                                },
                                onTitleBarLeftContentSet = {
                                    titleBarViewModel.onTitleBarLeftContentSet(it)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckForUpdateDialog(
    onShowUpdateDialog: (String) -> Unit,
    versionAskedToUpdate: VersionAskedToUpdate,
) {
    if (versionAskedToUpdate !is VersionAskedToUpdate.Ready) return

    val coroutineScope = rememberCoroutineScope()
    var remoteVersion by remember { mutableStateOf("Checking...") }
    var updateAvailable by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val remoteVersionObj: SoftwareUpdateController.Version? =
                    updateController?.currentVersionFromRepository
                remoteVersion = remoteVersionObj?.version ?: "Unknown"
                logger.info("VersionAskedToUpdate : $versionAskedToUpdate")
                logger.info("remoteVersionObj?.version : ${remoteVersionObj?.version}")
                val versionHasNotAskedToUpdate =
                    versionAskedToUpdate.version == null || (
                        remoteVersionObj?.version?.compareTo(
                            versionAskedToUpdate.version,
                        ) ?: 0
                    ) > 0

                updateAvailable = (
                    remoteVersionObj?.compareTo(updateController.currentVersion)
                        ?: 0
                ) > 0 && versionHasNotAskedToUpdate
            } catch (e: Exception) {
                remoteVersion = "Error: ${e.message}"
            }
        }
    }

    if (updateAvailable && canDoOnlineUpdates) {
        onShowUpdateDialog(remoteVersion)
        updateController?.triggerUpdateCheckUI()
    }
}

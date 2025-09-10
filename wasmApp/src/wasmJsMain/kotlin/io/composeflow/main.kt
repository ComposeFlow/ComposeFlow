package io.composeflow

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import co.touchlab.kermit.Logger
import co.touchlab.kermit.platformLogWriter
import io.composeflow.analytics.Analytics
import io.composeflow.di.ServiceLocator
import io.composeflow.ui.common.ComposeFlowTheme
import io.composeflow.ui.login.LOGIN_ROUTE
import io.composeflow.ui.utils.TitleBarContent
import kotlinx.browser.document
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import moe.tlaster.precompose.PreComposeApp
import moe.tlaster.precompose.navigation.rememberNavigator
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModel

private val logger = Logger.withTag("ComposeFlowWASM")

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val scope = MainScope()

    // Initialize logging
    Logger.setLogWriters(platformLogWriter())
    logger.i { "ComposeFlow WASM starting..." }

    // Initialize analytics (no-op for WASM in this case)
    val analytics =
        object : Analytics {
            override fun initialize(
                apiKey: String,
                host: String?,
            ) {}

            override fun track(
                event: String,
                properties: Map<String, Any>?,
            ) {}

            override fun identify(
                userId: String,
                properties: Map<String, Any>?,
            ) {}

            override fun setUserProperties(properties: Map<String, Any>) {}

            override fun screen(
                screenName: String,
                properties: Map<String, Any>?,
            ) {}

            override fun reset() {}

            override fun flush() {}

            override fun shutdown() {}
        }
    ServiceLocator.put(analytics)

    // Hide loading spinner
    document.getElementById("loading")?.remove()

    logger.i { "Starting ComposeViewport..." }
    ComposeViewport(document.body!!) {
        ComposeFlowWasmApp()
    }
}

@Composable
fun ComposeFlowWasmApp() {
    logger.i { "ComposeFlowWasmApp composable starting..." }
    val isDebug = if (!BuildConfig.isRelease) "-debug" else ""

    ComposeFlowTheme {
        logger.i { "Theme initialized" }

        PreComposeApp {
            logger.i { "PreComposeApp initialized" }
            val navigator = rememberNavigator()
            logger.i { "Navigator created" }

            val titleBarViewModel =
                viewModel(modelClass = WasmTitleBarViewModel::class) {
                    WasmTitleBarViewModel()
                }
            logger.i { "TitleBarViewModel created" }
            val titleBarLeftContent by titleBarViewModel.titleBarLeftContent.collectAsState()
            val titleBarRightContent by titleBarViewModel.titleBarRightContent.collectAsState()

            CompositionLocalProvider(
                LocalWasmEnvironment provides true,
            ) {
                logger.i { "Calling ComposeFlowApp..." }
                Column {
                    TitleBarView(
                        title = "ComposeFlow$isDebug",
                        onComposeFlowLogoClicked = {
                            navigator.navigate(LOGIN_ROUTE)
                        },
                        titleBarRightContent = titleBarRightContent,
                        titleBarLeftContent = titleBarLeftContent,
                    )
                    ComposeFlowApp(
                        navigator = navigator,
                        onTitleBarRightContentSet = { content ->
                            titleBarViewModel.onTitleBarRightContentSet(content)
                        },
                        onTitleBarLeftContentSet = { content ->
                            titleBarViewModel.onTitleBarLeftContentSet(content)
                        },
                    )
                }
            }
        }
    }
}

/**
 * CompositionLocal to indicate we're running in WASM environment
 */
val LocalWasmEnvironment = androidx.compose.runtime.staticCompositionLocalOf { false }

/**
 * WASM-compatible version of TitleBarViewModel.
 * Since WASM doesn't have a native title bar, this is a simplified version.
 */
class WasmTitleBarViewModel : ViewModel() {
    private val _titleBarRightContent = MutableStateFlow<TitleBarContent>({})
    val titleBarRightContent: StateFlow<TitleBarContent> = _titleBarRightContent

    private val _titleBarLeftContent = MutableStateFlow<TitleBarContent>({})
    val titleBarLeftContent: StateFlow<TitleBarContent> = _titleBarLeftContent

    fun onTitleBarRightContentSet(content: TitleBarContent) {
        _titleBarRightContent.value = content
    }

    fun onTitleBarLeftContentSet(content: TitleBarContent) {
        _titleBarLeftContent.value = content
    }
}

package io.composeflow

import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.composeflow.ui.ProvideOnShowSnackbar
import io.composeflow.ui.jewel.TitleBarContent
import io.composeflow.ui.login.LOGIN_ROUTE
import kotlinx.coroutines.launch
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.viewmodel.viewModel

@Composable
fun ComposeFlowApp(
    navigator: Navigator,
    onTitleBarRightContentSet: (TitleBarContent) -> Unit = {},
    onTitleBarLeftContentSet: (TitleBarContent) -> Unit = {},
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutine = rememberCoroutineScope()
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) {
        Surface {
            val appViewModel =
                viewModel(modelClass = ComposeFlowAppViewModel::class) { ComposeFlowAppViewModel() }
            val onShowSnackbar: (suspend (String, String?) -> Boolean) = { message, action ->
                snackbarHostState.showSnackbar(
                    message = message,
                    actionLabel = action,
                    duration = SnackbarDuration.Short,
                ) == SnackbarResult.ActionPerformed
            }
            ProvideOnShowSnackbar(onShowSnackbar) {
                ComposeBuilderNavHost(
                    navigator = navigator,
                    onGoogleSignInClicked = appViewModel::onGoogleSignClicked,
                    onLogOut = {
                        coroutine.launch {
                            appViewModel.onLogOut()
                            navigator.navigate(LOGIN_ROUTE)
                        }
                    },
                    onTitleBarRightContentSet = onTitleBarRightContentSet,
                    onTitleBarLeftContentSet = onTitleBarLeftContentSet,
                    onUseWithoutSignIn = appViewModel::onUseWithoutSignIn,
                    loginUiStateProvider = {
                        appViewModel.loginResultUiState.collectAsState().value
                    },
                )
            }
        }
    }
}

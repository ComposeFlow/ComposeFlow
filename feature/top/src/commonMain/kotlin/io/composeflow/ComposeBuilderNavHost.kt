package io.composeflow

import androidx.compose.runtime.Composable
import io.composeflow.ui.utils.TitleBarContent
import io.composeflow.ui.login.LOGIN_ROUTE
import io.composeflow.ui.login.loginScreen
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.Navigator

@Composable
fun ComposeBuilderNavHost(
    navigator: Navigator,
    onGoogleSignInClicked: () -> Unit,
    onLogOut: () -> Unit,
    onTitleBarRightContentSet: (TitleBarContent) -> Unit = {},
    onTitleBarLeftContentSet: (TitleBarContent) -> Unit = {},
    onUseWithoutSignIn: () -> Unit = {},
    loginUiStateProvider: @Composable () -> LoginResultUiState,
) {
    NavHost(
        navigator = navigator,
        initialRoute = LOGIN_ROUTE,
    ) {
        loginScreen(
            onGoogleSignInClicked = onGoogleSignInClicked,
            onLogOut = onLogOut,
            onTitleBarRightContentSet = onTitleBarRightContentSet,
            onTitleBarLeftContentSet = onTitleBarLeftContentSet,
            onUseWithoutSignIn = onUseWithoutSignIn,
            loginUiStateProvider = loginUiStateProvider,
        )
    }
}

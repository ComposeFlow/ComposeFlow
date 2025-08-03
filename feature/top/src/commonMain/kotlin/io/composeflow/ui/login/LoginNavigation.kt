package io.composeflow.ui.login

import androidx.compose.runtime.Composable
import io.composeflow.LoginResultUiState
import io.composeflow.ui.jewel.TitleBarContent
import moe.tlaster.precompose.navigation.RouteBuilder
import moe.tlaster.precompose.navigation.transition.NavTransition

const val LOGIN_ROUTE = "login_route"

fun RouteBuilder.loginScreen(
    onGoogleSignInClicked: () -> Unit,
    onLogOut: () -> Unit,
    onTitleBarRightContentSet: (TitleBarContent) -> Unit = {},
    onTitleBarLeftContentSet: (TitleBarContent) -> Unit = {},
    onUseWithoutSignIn: () -> Unit = {},
    loginUiStateProvider: @Composable () -> LoginResultUiState,
    navTransition: NavTransition? = null,
) {
    scene(
        route = LOGIN_ROUTE,
        navTransition = navTransition,
    ) {
        LoginScreen(
            onGoogleSignInClicked = onGoogleSignInClicked,
            onLogOut = onLogOut,
            onTitleBarRightContentSet = onTitleBarRightContentSet,
            onTitleBarLeftContentSet = onTitleBarLeftContentSet,
            onUseWithoutSignIn = onUseWithoutSignIn,
            loginUiState = loginUiStateProvider(),
        )
    }
}

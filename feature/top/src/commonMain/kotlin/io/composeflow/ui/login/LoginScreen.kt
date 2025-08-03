package io.composeflow.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.dp
import io.composeflow.ComposeFlowAppViewModel
import io.composeflow.LoginResultUiState
import io.composeflow.Res
import io.composeflow.auth.ProvideFirebaseIdToken
import io.composeflow.composeflow_main_logo
import io.composeflow.isAuthConfigured
import io.composeflow.sign_in_with_google
import io.composeflow.ui.common.ComposeFlowTheme
import io.composeflow.ui.jewel.TitleBarContent
import io.composeflow.ui.modifier.hoverIconClickable
import io.composeflow.ui.top.TopScreen
import org.jetbrains.compose.resources.stringResource

@Composable
fun LoginScreen(
    onGoogleSignInClicked: () -> Unit,
    onLogOut: () -> Unit,
    onTitleBarRightContentSet: (TitleBarContent) -> Unit,
    onTitleBarLeftContentSet: (TitleBarContent) -> Unit,
    onUseWithoutSignIn: () -> Unit = {},
    loginUiState: LoginResultUiState,
) {
    ComposeFlowTheme(useDarkTheme = true) {
        Surface {
            when (val state = loginUiState) {
                LoginResultUiState.NotStarted -> {
                    InitialContent(
                        onGoogleSignInClicked = onGoogleSignInClicked,
                        onUseWithoutSignIn = onUseWithoutSignIn,
                    )
                }

                LoginResultUiState.Loading -> LoadingContent()
                is LoginResultUiState.Success -> {
                    ProvideFirebaseIdToken(state.firebaseIdToken) {
                        TopScreen(
                            onLogOut = onLogOut,
                            onTitleBarRightContentSet = onTitleBarRightContentSet,
                            onTitleBarLeftContentSet = onTitleBarLeftContentSet,
                        )
                    }
                }
                LoginResultUiState.Anonymous -> {
                    TopScreen(
                        onLogOut = onLogOut,
                        onTitleBarRightContentSet = onTitleBarRightContentSet,
                        onTitleBarLeftContentSet = onTitleBarLeftContentSet,
                        isAnonymous = true,
                    )
                }
            }
        }
    }
}

@Composable
private fun InitialContent(
    onGoogleSignInClicked: () -> Unit,
    onUseWithoutSignIn: () -> Unit,
) {
    val isAuthConfigured = isAuthConfigured()
    
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize(),
    ) {
        val density = LocalDensity.current
        val scale = density.density / 2
        Image(
            bitmap =
                useResource("ComposeFlow_inverted_800x600.png") {
                    loadImageBitmap(it)
                },
            contentDescription = stringResource(Res.string.composeflow_main_logo),
            modifier = Modifier.scale(scale),
        )
        
        if (isAuthConfigured) {
            Image(
                bitmap =
                    useResource("btn_google_signin_dark.png") {
                        loadImageBitmap(it)
                    },
                contentDescription = stringResource(Res.string.sign_in_with_google),
                modifier =
                    Modifier
                        .scale(scale * 1.7f)
                        .hoverIconClickable()
                        .clickable {
                            onGoogleSignInClicked()
                        },
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedButton(
                onClick = onUseWithoutSignIn
            ) {
                Text("Use without sign in")
            }
        } else {
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = onUseWithoutSignIn
            ) {
                Text("Get Started")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Sign in is not available - configure authentication in local.properties",
                modifier = Modifier.scale(0.8f)
            )
        }
    }
}

@Composable
private fun LoadingContent() {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize(),
    ) {
        CircularProgressIndicator()
    }
}

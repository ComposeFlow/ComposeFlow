package io.composeflow

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import io.composeflow.custom.ComposeFlowIcons
import io.composeflow.custom.composeflowicons.ComposeFlowLogo
import io.composeflow.ui.modifier.hoverIconClickable
import io.composeflow.ui.window.WasmJsTitleBar
import io.composeflow.ui.window.styling.TitleBarStyle
import io.composeflow.ui.window.styling.dark

@Composable
fun TitleBarView(
    title: String,
    onComposeFlowLogoClicked: () -> Unit,
    titleBarLeftContent: @Composable () -> Unit,
    titleBarRightContent: @Composable () -> Unit,
    titleBarStyle: TitleBarStyle = TitleBarStyle.dark(),
    modifier: Modifier = Modifier,
) {
    val titleBar = WasmJsTitleBar
    val icon = rememberVectorPainter(ComposeFlowIcons.ComposeFlowLogo)
    Column(
        modifier = modifier,
    ) {
        titleBar.RenderTitleBar(
            modifier = Modifier.fillMaxWidth(),
            titleBar = WasmJsTitleBar,
            title = title,
            windowIcon = icon,
            showTitle = false,
            showWindowIcon = false,
            style = titleBarStyle,
            start = {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier =
                            Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable {
                                    onComposeFlowLogoClicked()
                                }.hoverIconClickable(),
                    ) {
                        Image(
                            painter = icon,
                            contentDescription = "icon",
                            modifier = Modifier.size(36.dp),
                        )
                    }
                    Row(modifier = Modifier.weight(1f)) {
                        titleBarLeftContent()
                    }
                    Text(
                        text = title,
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.bodyMedium,
                    )

                    Row(modifier = Modifier.weight(1f)) {
                        titleBarRightContent()
                    }
                }
            },
            end = { },
            onWindowIconClicked = onComposeFlowLogoClicked,
            onRequestClose = { },
            onRequestMinimize = { },
            onRequestToggleMaximize = { },
        )
    }
}

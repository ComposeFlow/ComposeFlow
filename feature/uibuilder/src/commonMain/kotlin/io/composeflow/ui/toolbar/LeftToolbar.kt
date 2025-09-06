package io.composeflow.ui.toolbar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.composeflow.Res
import io.composeflow.ai_open_assistant
import io.composeflow.custom.ComposeFlowIcons
import io.composeflow.custom.composeflowicons.NounAi
import io.composeflow.keyboard.getCtrlKeyStr
import io.composeflow.ui.Tooltip
import io.composeflow.ui.modifier.hoverIconClickable
import org.jetbrains.compose.resources.stringResource

@Composable
fun LeftToolbar(
    onToggleVisibilityOfAiChatDialog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .testTag(TOOLBAR_TEST_TAG),
    ) {
        Spacer(Modifier.width(32.dp))

        val openAiAssistant = stringResource(Res.string.ai_open_assistant)
        Tooltip(openAiAssistant + " (${getCtrlKeyStr()} + K)") {
            Box(
                contentAlignment = Alignment.Center,
                modifier =
                    Modifier
                        .padding(6.dp)
                        .background(
                            color = Color.White,
                            shape =
                                androidx.compose.foundation.shape
                                    .RoundedCornerShape(4.dp),
                        ).clickable {
                            onToggleVisibilityOfAiChatDialog()
                        }.hoverIconClickable(),
            ) {
                Icon(
                    imageVector = ComposeFlowIcons.NounAi,
                    contentDescription = openAiAssistant,
                )
            }
        }
    }
}

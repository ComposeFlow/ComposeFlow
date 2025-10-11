package io.composeflow.robots

import androidx.compose.ui.test.DesktopComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import co.touchlab.kermit.ExperimentalKermitApi
import com.github.takahirom.roborazzi.DEFAULT_ROBORAZZI_OUTPUT_DIR_PATH
import io.composeflow.ProvideTestPreCompose
import io.composeflow.TestPreComposeWindowHolder
import io.composeflow.ui.ProvideCloseDialogCallback
import io.composeflow.ui.ProvideShowDialogCallback
import io.composeflow.ui.common.ProvideAppThemeTokens
import io.composeflow.ui.propertyeditor.ColorPropertyDialogContent
import io.github.takahirom.roborazzi.captureRoboImage
import org.jetbrains.jewel.intui.standalone.theme.IntUiTheme

@OptIn(ExperimentalTestApi::class, ExperimentalKermitApi::class)
class ColorPropertyDialogRobot(
    private val precomposeStateHolder: TestPreComposeWindowHolder = TestPreComposeWindowHolder(),
) {
    context(DesktopComposeUiTest)
    fun capture(fileName: String) {
        onRoot().captureRoboImage(
            filePath = "$DEFAULT_ROBORAZZI_OUTPUT_DIR_PATH/$fileName.png",
        )
    }

    context(DesktopComposeUiTest)
    fun capture() {
        onRoot().captureRoboImage()
    }

    context(DesktopComposeUiTest)
    fun setContent(isDarkTheme: Boolean = false) {
        setContent {
            ProvideTestPreCompose(
                precomposeStateHolder = precomposeStateHolder,
            ) {
                ProvideShowDialogCallback(
                    onAnyDialogIsShown = {},
                ) {
                    IntUiTheme {
                        ProvideCloseDialogCallback(onAllDialogsClosed = {}) {
                            ProvideAppThemeTokens(isDarkTheme = isDarkTheme) {
                                ColorPropertyDialogContent(
                                    initialColor = null,
                                    extendedColors = emptyList(),
                                    onThemeColorSelected = { color -> },
                                    onColorUpdated = {},
                                    onCloseClick = {},
                                    includeThemeColor = true,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

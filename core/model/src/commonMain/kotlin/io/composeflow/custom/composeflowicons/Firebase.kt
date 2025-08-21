package io.composeflow.custom.composeflowicons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import io.composeflow.custom.ComposeFlowIcons

val ComposeFlowIcons.Firebase: ImageVector
    get() {
        if (_LogoFirebaseSvgrepoCom != null) {
            return _LogoFirebaseSvgrepoCom!!
        }
        _LogoFirebaseSvgrepoCom = ImageVector.Builder(
            name = "LogoFirebaseSvgrepoCom",
            defaultWidth = 800.dp,
            defaultHeight = 800.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(93.19f, 329.38f)
                lineTo(140.64f, 25.31f)
                curveToRelative(1.64f, -10.37f, 15.55f, -12.82f, 20.46f, -3.55f)
                lineToRelative(51f, 95.45f)
                close()
                moveTo(432f, 400f)
                lineTo(385.26f, 123.21f)
                arcToRelative(11f, 11f, 0f, isMoreThanHalf = false, isPositiveArc = false, -18.54f, -6f)
                lineTo(80f, 400f)
                lineToRelative(159.36f, 91.91f)
                arcToRelative(33.18f, 33.18f, 0f, isMoreThanHalf = false, isPositiveArc = false, 31.91f, 0f)
                close()
                moveTo(302.36f, 158.93f)
                lineTo(265.82f, 89.39f)
                arcToRelative(10.86f, 10.86f, 0f, isMoreThanHalf = false, isPositiveArc = false, -19.36f, 0f)
                lineTo(85.83f, 375.74f)
                close()
            }
        }.build()

        return _LogoFirebaseSvgrepoCom!!
    }

@Suppress("ObjectPropertyName")
private var _LogoFirebaseSvgrepoCom: ImageVector? = null

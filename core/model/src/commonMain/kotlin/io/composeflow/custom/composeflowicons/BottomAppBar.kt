package io.composeflow.custom.composeflowicons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import io.composeflow.custom.ComposeFlowIcons

val ComposeFlowIcons.BottomAppBar: ImageVector
    get() {
        if (_BottomAppBar != null) {
            return _BottomAppBar!!
        }
        _BottomAppBar = ImageVector.Builder(
            name = "BottomAppBar",
            defaultWidth = 1200.dp,
            defaultHeight = 1200.dp,
            viewportWidth = 1200f,
            viewportHeight = 1200f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveToRelative(972f, 100f)
                horizontalLineToRelative(-744f)
                curveToRelative(-70.7f, 0f, -128f, 57.3f, -128f, 128f)
                verticalLineToRelative(744f)
                curveToRelative(0f, 70.7f, 57.3f, 128f, 128f, 128f)
                horizontalLineToRelative(744f)
                curveToRelative(70.7f, 0f, 128f, -57.3f, 128f, -128f)
                verticalLineToRelative(-744f)
                curveToRelative(0f, -70.7f, -57.3f, -128f, -128f, -128f)
                close()
                moveTo(228f, 169.5f)
                horizontalLineToRelative(744f)
                curveToRelative(32.1f, 0.3f, 58f, 26.4f, 58f, 58.5f)
                verticalLineToRelative(430f)
                horizontalLineToRelative(-280f)
                curveToRelative(2f, -9.7f, 3f, -19.6f, 3f, -29.5f)
                curveToRelative(0f, -82.8f, -67.2f, -150f, -150f, -150f)
                reflectiveCurveToRelative(-150f, 67.2f, -150f, 150f)
                curveToRelative(0f, 9.9f, 1f, 19.8f, 3f, 29.5f)
                horizontalLineToRelative(-286f)
                verticalLineToRelative(-430f)
                curveToRelative(0f, -32.1f, 25.9f, -58.2f, 58f, -58.5f)
                close()
                moveTo(518.5f, 628.5f)
                curveToRelative(0f, -33f, 19.9f, -62.7f, 50.3f, -75.3f)
                curveToRelative(30.4f, -12.6f, 65.5f, -5.7f, 88.8f, 17.7f)
                curveToRelative(23.3f, 23.3f, 30.3f, 58.4f, 17.7f, 88.8f)
                curveToRelative(-12.6f, 30.5f, -42.3f, 50.3f, -75.3f, 50.3f)
                curveToRelative(-45f, 0f, -81.5f, -36.5f, -81.5f, -81.5f)
                close()
                moveTo(972f, 1028.5f)
                horizontalLineToRelative(-744f)
                curveToRelative(-31.4f, -0.2f, -56.9f, -25.2f, -58f, -56.5f)
                verticalLineToRelative(-244f)
                horizontalLineToRelative(317f)
                curveToRelative(28.5f, 32.6f, 69.7f, 51.3f, 113f, 51.3f)
                reflectiveCurveToRelative(84.5f, -18.7f, 113f, -51.3f)
                horizontalLineToRelative(317f)
                verticalLineToRelative(244f)
                curveToRelative(0f, 32.1f, -25.9f, 58.2f, -58f, 58.5f)
                close()
            }
        }.build()

        return _BottomAppBar!!
    }

@Suppress("ObjectPropertyName")
private var _BottomAppBar: ImageVector? = null

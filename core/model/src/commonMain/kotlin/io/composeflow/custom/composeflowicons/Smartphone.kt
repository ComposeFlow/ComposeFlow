package io.composeflow.custom.composeflowicons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import io.composeflow.custom.ComposeFlowIcons

val ComposeFlowIcons.Smartphone: ImageVector
    get() {
        if (_Smartphone != null) {
            return _Smartphone!!
        }
        _Smartphone = ImageVector.Builder(
            name = "Smartphone",
            defaultWidth = 1200.dp,
            defaultHeight = 1200.dp,
            viewportWidth = 1200f,
            viewportHeight = 1200f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveToRelative(825f, 37.5f)
                horizontalLineToRelative(-450f)
                curveToRelative(-51.8f, 0f, -93.8f, 42f, -93.8f, 93.8f)
                verticalLineToRelative(937.5f)
                curveToRelative(0f, 51.8f, 42f, 93.8f, 93.8f, 93.8f)
                horizontalLineToRelative(450f)
                curveToRelative(51.8f, 0f, 93.8f, -42f, 93.8f, -93.8f)
                verticalLineToRelative(-937.5f)
                curveToRelative(0f, -51.8f, -42f, -93.8f, -93.8f, -93.8f)
                close()
                moveTo(700.9f, 75f)
                lineTo(687.4f, 102.2f)
                curveToRelative(-3.2f, 6.4f, -9.6f, 10.3f, -16.7f, 10.3f)
                horizontalLineToRelative(-141.2f)
                curveToRelative(-7.1f, 0f, -13.5f, -3.9f, -16.7f, -10.3f)
                lineToRelative(-13.5f, -27.2f)
                horizontalLineToRelative(201.7f)
                close()
                moveTo(881.2f, 1068.7f)
                curveToRelative(0f, 30.9f, -25.3f, 56.3f, -56.3f, 56.3f)
                horizontalLineToRelative(-450f)
                curveToRelative(-30.9f, 0f, -56.3f, -25.3f, -56.3f, -56.3f)
                verticalLineToRelative(-937.5f)
                curveToRelative(0f, -30.9f, 25.3f, -56.3f, 56.3f, -56.3f)
                horizontalLineToRelative(82.1f)
                lineToRelative(21.9f, 43.9f)
                curveToRelative(9.6f, 19.1f, 28.9f, 31.1f, 50.3f, 31.1f)
                horizontalLineToRelative(141.2f)
                curveToRelative(21.4f, 0f, 40.7f, -12f, 50.3f, -31.1f)
                lineToRelative(21.9f, -43.9f)
                horizontalLineToRelative(82.1f)
                curveToRelative(30.9f, 0f, 56.3f, 25.3f, 56.3f, 56.3f)
                verticalLineToRelative(937.5f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveToRelative(693.7f, 1050f)
                horizontalLineToRelative(-187.5f)
                curveToRelative(-10.3f, 0f, -18.8f, 8.4f, -18.8f, 18.8f)
                reflectiveCurveToRelative(8.4f, 18.8f, 18.8f, 18.8f)
                horizontalLineToRelative(187.5f)
                curveToRelative(10.3f, 0f, 18.8f, -8.4f, 18.8f, -18.8f)
                reflectiveCurveToRelative(-8.4f, -18.8f, -18.8f, -18.8f)
                close()
            }
        }.build()

        return _Smartphone!!
    }

@Suppress("ObjectPropertyName")
private var _Smartphone: ImageVector? = null

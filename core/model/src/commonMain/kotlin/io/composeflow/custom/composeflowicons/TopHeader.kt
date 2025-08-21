package io.composeflow.custom.composeflowicons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import io.composeflow.custom.ComposeFlowIcons

val ComposeFlowIcons.TopHeader: ImageVector
    get() {
        if (_TopHeader != null) {
            return _TopHeader!!
        }
        _TopHeader = ImageVector.Builder(
            name = "TopHeader",
            defaultWidth = 1200.dp,
            defaultHeight = 1200.dp,
            viewportWidth = 1200f,
            viewportHeight = 1200f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveToRelative(150f, 350f)
                verticalLineToRelative(575f)
                curveToRelative(0f, 41.4f, 33.6f, 75f, 75f, 75f)
                horizontalLineToRelative(700f)
                curveToRelative(41.4f, 0f, 75f, -33.6f, 75f, -75f)
                verticalLineToRelative(-575f)
                close()
                moveTo(150f, 300f)
                horizontalLineToRelative(850f)
                verticalLineToRelative(-75f)
                curveToRelative(0f, -41.4f, -33.6f, -75f, -75f, -75f)
                horizontalLineToRelative(-700f)
                curveToRelative(-41.4f, 0f, -75f, 33.6f, -75f, 75f)
                close()
                moveTo(225f, 1050f)
                curveToRelative(-69f, 0f, -125f, -56f, -125f, -125f)
                verticalLineToRelative(-700f)
                curveToRelative(0f, -69f, 56f, -125f, 125f, -125f)
                horizontalLineToRelative(700f)
                curveToRelative(69f, 0f, 125f, 56f, 125f, 125f)
                verticalLineToRelative(700f)
                curveToRelative(0f, 69f, -56f, 125f, -125f, 125f)
                close()
                moveTo(925f, 200f)
                curveToRelative(13.8f, 0f, 25f, 11.2f, 25f, 25f)
                reflectiveCurveToRelative(-11.2f, 25f, -25f, 25f)
                horizontalLineToRelative(-50f)
                curveToRelative(-13.8f, 0f, -25f, -11.2f, -25f, -25f)
                reflectiveCurveToRelative(11.2f, -25f, 25f, -25f)
                close()
                moveTo(275f, 200f)
                curveToRelative(13.8f, 0f, 25f, 11.2f, 25f, 25f)
                reflectiveCurveToRelative(-11.2f, 25f, -25f, 25f)
                horizontalLineToRelative(-50f)
                curveToRelative(-13.8f, 0f, -25f, -11.2f, -25f, -25f)
                reflectiveCurveToRelative(11.2f, -25f, 25f, -25f)
                close()
                moveTo(425f, 200f)
                curveToRelative(13.8f, 0f, 25f, 11.2f, 25f, 25f)
                reflectiveCurveToRelative(-11.2f, 25f, -25f, 25f)
                horizontalLineToRelative(-50f)
                curveToRelative(-13.8f, 0f, -25f, -11.2f, -25f, -25f)
                reflectiveCurveToRelative(11.2f, -25f, 25f, -25f)
                close()
            }
        }.build()

        return _TopHeader!!
    }

@Suppress("ObjectPropertyName")
private var _TopHeader: ImageVector? = null

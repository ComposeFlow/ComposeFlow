package io.composeflow.custom.composeflowicons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.EvenOdd
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import io.composeflow.custom.ComposeFlowIcons

public val ComposeFlowIcons.Placeholder: ImageVector
    get() {
        if (_placeholder != null) {
            return _placeholder!!
        }
        _placeholder = Builder(name = "Placeholder", defaultWidth = 800.0.dp, defaultHeight =
                800.0.dp, viewportWidth = 120.0f, viewportHeight = 120.0f).apply {
            path(fill = SolidColor(Color(0xFF687787)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(33.25f, 38.482f)
                curveTo(33.26f, 37.047f, 34.42f, 35.886f, 35.854f, 35.875f)
                horizontalLineTo(83.146f)
                curveTo(84.585f, 35.875f, 85.75f, 37.043f, 85.75f, 38.482f)
                verticalLineTo(80.518f)
                curveTo(85.74f, 81.953f, 84.581f, 83.114f, 83.146f, 83.125f)
                horizontalLineTo(35.854f)
                curveTo(34.416f, 83.124f, 33.25f, 81.957f, 33.25f, 80.518f)
                verticalLineTo(38.482f)
                close()
                moveTo(80.501f, 41.125f)
                horizontalLineTo(38.501f)
                verticalLineTo(77.875f)
                lineTo(62.892f, 53.478f)
                curveTo(63.917f, 52.454f, 65.579f, 52.454f, 66.604f, 53.478f)
                lineTo(80.501f, 67.401f)
                verticalLineTo(41.125f)
                close()
                moveTo(43.75f, 51.625f)
                curveTo(43.75f, 54.524f, 46.1f, 56.875f, 49.0f, 56.875f)
                curveTo(51.9f, 56.875f, 54.25f, 54.524f, 54.25f, 51.625f)
                curveTo(54.25f, 48.725f, 51.9f, 46.375f, 49.0f, 46.375f)
                curveTo(46.1f, 46.375f, 43.75f, 48.725f, 43.75f, 51.625f)
                close()
            }
        }
        .build()
        return _placeholder!!
    }

private var _placeholder: ImageVector? = null

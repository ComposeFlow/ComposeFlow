package io.composeflow.custom.composeflowicons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import io.composeflow.custom.ComposeFlowIcons

public val ComposeFlowIcons.Colors: ImageVector
    get() {
        if (_colors != null) {
            return _colors!!
        }
        _colors = Builder(name = "Colors", defaultWidth = 14.0.dp, defaultHeight = 14.0.dp,
                viewportWidth = 14.0f, viewportHeight = 14.0f).apply {
            path(fill = SolidColor(Color(0xFF55A76A)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(0.0f, 1.0f)
                curveTo(0.0f, 0.4477f, 0.4477f, 0.0f, 1.0f, 0.0f)
                horizontalLineTo(5.0f)
                curveTo(5.5523f, 0.0f, 6.0f, 0.4477f, 6.0f, 1.0f)
                verticalLineTo(5.0f)
                curveTo(6.0f, 5.5523f, 5.5523f, 6.0f, 5.0f, 6.0f)
                horizontalLineTo(1.0f)
                curveTo(0.4477f, 6.0f, 0.0f, 5.5523f, 0.0f, 5.0f)
                verticalLineTo(1.0f)
                close()
            }
            path(fill = SolidColor(Color(0xFFFFAF0F)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(7.0f, 1.0f)
                curveTo(7.0f, 0.4477f, 7.4477f, 0.0f, 8.0f, 0.0f)
                horizontalLineTo(12.0f)
                curveTo(12.5523f, 0.0f, 13.0f, 0.4477f, 13.0f, 1.0f)
                verticalLineTo(5.0f)
                curveTo(13.0f, 5.5523f, 12.5523f, 6.0f, 12.0f, 6.0f)
                horizontalLineTo(8.0f)
                curveTo(7.4477f, 6.0f, 7.0f, 5.5523f, 7.0f, 5.0f)
                verticalLineTo(1.0f)
                close()
            }
            path(fill = SolidColor(Color(0xFF4682FA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(0.0f, 8.0f)
                curveTo(0.0f, 7.4477f, 0.4477f, 7.0f, 1.0f, 7.0f)
                horizontalLineTo(5.0f)
                curveTo(5.5523f, 7.0f, 6.0f, 7.4477f, 6.0f, 8.0f)
                verticalLineTo(12.0f)
                curveTo(6.0f, 12.5523f, 5.5523f, 13.0f, 5.0f, 13.0f)
                horizontalLineTo(1.0f)
                curveTo(0.4477f, 13.0f, 0.0f, 12.5523f, 0.0f, 12.0f)
                verticalLineTo(8.0f)
                close()
            }
            path(fill = SolidColor(Color(0xFFE55765)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(7.0f, 8.0f)
                curveTo(7.0f, 7.4477f, 7.4477f, 7.0f, 8.0f, 7.0f)
                horizontalLineTo(12.0f)
                curveTo(12.5523f, 7.0f, 13.0f, 7.4477f, 13.0f, 8.0f)
                verticalLineTo(12.0f)
                curveTo(13.0f, 12.5523f, 12.5523f, 13.0f, 12.0f, 13.0f)
                horizontalLineTo(8.0f)
                curveTo(7.4477f, 13.0f, 7.0f, 12.5523f, 7.0f, 12.0f)
                verticalLineTo(8.0f)
                close()
            }
        }
        .build()
        return _colors!!
    }

private var _colors: ImageVector? = null

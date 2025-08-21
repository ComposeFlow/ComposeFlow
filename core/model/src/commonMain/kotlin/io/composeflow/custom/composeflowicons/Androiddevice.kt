package io.composeflow.custom.composeflowicons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.EvenOdd
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

public val ComposeFlowIcons.Androiddevice: ImageVector
    get() {
        if (_androiddevice != null) {
            return _androiddevice!!
        }
        _androiddevice = Builder(name = "Androiddevice", defaultWidth = 16.0.dp, defaultHeight =
                16.0.dp, viewportWidth = 16.0f, viewportHeight = 16.0f).apply {
            path(fill = SolidColor(Color(0xFF5FB865)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(9.9288f, 11.2428f)
                curveTo(9.7867f, 11.006f, 9.4796f, 10.9292f, 9.2428f, 11.0713f)
                curveTo(9.006f, 11.2133f, 8.9293f, 11.5205f, 9.0713f, 11.7573f)
                lineTo(9.6672f, 12.7503f)
                curveTo(8.6575f, 13.4764f, 8.0f, 14.6614f, 8.0f, 16.0f)
                horizontalLineTo(16.0f)
                curveTo(16.0f, 14.6615f, 15.3425f, 13.4765f, 14.3329f, 12.7504f)
                lineTo(14.9288f, 11.7573f)
                curveTo(15.0709f, 11.5205f, 14.9941f, 11.2133f, 14.7573f, 11.0713f)
                curveTo(14.5205f, 10.9292f, 14.2134f, 11.006f, 14.0713f, 11.2428f)
                lineTo(13.4536f, 12.2723f)
                curveTo(13.003f, 12.0965f, 12.5128f, 12.0f, 12.0f, 12.0f)
                curveTo(11.4873f, 12.0f, 10.997f, 12.0965f, 10.5465f, 12.2723f)
                lineTo(9.9288f, 11.2428f)
                close()
                moveTo(10.0f, 14.0f)
                horizontalLineTo(11.0f)
                verticalLineTo(15.0f)
                horizontalLineTo(10.0f)
                verticalLineTo(14.0f)
                close()
                moveTo(14.0f, 14.0f)
                horizontalLineTo(13.0f)
                verticalLineTo(15.0f)
                horizontalLineTo(14.0f)
                verticalLineTo(14.0f)
                close()
            }
            path(fill = SolidColor(Color(0xFF6C707E)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(5.0f, 2.0f)
                horizontalLineTo(11.0f)
                curveTo(11.5523f, 2.0f, 12.0f, 2.4477f, 12.0f, 3.0f)
                verticalLineTo(11.0f)
                curveTo(12.3389f, 11.0f, 12.6707f, 11.0339f, 12.9917f, 11.0986f)
                lineTo(13.0f, 11.0847f)
                verticalLineTo(3.0f)
                curveTo(13.0f, 1.8954f, 12.1046f, 1.0f, 11.0f, 1.0f)
                horizontalLineTo(5.0f)
                curveTo(3.8954f, 1.0f, 3.0f, 1.8954f, 3.0f, 3.0f)
                verticalLineTo(13.0f)
                curveTo(3.0f, 14.1046f, 3.8954f, 15.0f, 5.0f, 15.0f)
                horizontalLineTo(7.1001f)
                curveTo(7.1708f, 14.6522f, 7.2775f, 14.3175f, 7.4163f, 14.0f)
                horizontalLineTo(5.0f)
                curveTo(4.4477f, 14.0f, 4.0f, 13.5523f, 4.0f, 13.0f)
                verticalLineTo(3.0f)
                curveTo(4.0f, 2.4477f, 4.4477f, 2.0f, 5.0f, 2.0f)
                close()
            }
        }
        .build()
        return _androiddevice!!
    }

private var _androiddevice: ImageVector? = null

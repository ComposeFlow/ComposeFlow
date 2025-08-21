package io.composeflow.custom.composeflowicons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import io.composeflow.custom.ComposeFlowIcons

public val ComposeFlowIcons.Slider: ImageVector
    get() {
        if (_Slider != null) {
            return _Slider!!
        }
        _Slider = ImageVector.Builder(
            name = "Slider",
            defaultWidth = 15.dp,
            defaultHeight = 15.dp,
            viewportWidth = 15f,
            viewportHeight = 15f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)),
                fillAlpha = 1.0f,
                stroke = null,
                strokeAlpha = 1.0f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(10.3004f, 7.49991f)
                curveTo(10.3004f, 8.4943f, 9.4943f, 9.3004f, 8.4999f, 9.3004f)
                curveTo(7.5055f, 9.3004f, 6.6994f, 8.4943f, 6.6994f, 7.4999f)
                curveTo(6.6994f, 6.5055f, 7.5055f, 5.6994f, 8.4999f, 5.6994f)
                curveTo(9.4943f, 5.6994f, 10.3004f, 6.5055f, 10.3004f, 7.4999f)
                close()
                moveTo(11.205f, 8f)
                curveTo(10.9699f, 9.2803f, 9.8482f, 10.2504f, 8.4999f, 10.2504f)
                curveTo(7.1516f, 10.2504f, 6.0299f, 9.2803f, 5.7947f, 8f)
                horizontalLineTo(0.5f)
                curveTo(0.2239f, 8f, 0f, 7.7761f, 0f, 7.5f)
                curveTo(0f, 7.2239f, 0.2239f, 7f, 0.5f, 7f)
                horizontalLineTo(5.7947f)
                curveTo(6.0298f, 5.7196f, 7.1515f, 4.7494f, 8.4999f, 4.7494f)
                curveTo(9.8482f, 4.7494f, 10.97f, 5.7196f, 11.2051f, 7f)
                horizontalLineTo(14.5f)
                curveTo(14.7761f, 7f, 15f, 7.2239f, 15f, 7.5f)
                curveTo(15f, 7.7761f, 14.7761f, 8f, 14.5f, 8f)
                horizontalLineTo(11.205f)
                close()
            }
        }.build()
        return _Slider!!
    }

private var _Slider: ImageVector? = null

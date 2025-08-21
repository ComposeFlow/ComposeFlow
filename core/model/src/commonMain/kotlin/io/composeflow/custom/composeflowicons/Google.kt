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

public val ComposeFlowIcons.Google: ImageVector
    get() {
        if (_Google != null) {
            return _Google!!
        }
        _Google = ImageVector.Builder(
            name = "Google",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 16f,
            viewportHeight = 16f
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
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(15.545f, 6.558f)
                arcToRelative(9.4f, 9.4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0.139f, 1.626f)
                curveToRelative(0f, 2.434f, -0.87f, 4.492f, -2.384f, 5.885f)
                horizontalLineToRelative(0.002f)
                curveTo(11.978f, 15.292f, 10.158f, 16f, 8f, 16f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = true, isPositiveArc = true, 8f, 0f)
                arcToRelative(7.7f, 7.7f, 0f, isMoreThanHalf = false, isPositiveArc = true, 5.352f, 2.082f)
                lineToRelative(-2.284f, 2.284f)
                arcTo(4.35f, 4.35f, 0f, isMoreThanHalf = false, isPositiveArc = false, 8f, 3.166f)
                curveToRelative(-2.087f, 0f, -3.86f, 1.408f, -4.492f, 3.304f)
                arcToRelative(4.8f, 4.8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 3.063f)
                horizontalLineToRelative(0.003f)
                curveToRelative(0.635f, 1.893f, 2.405f, 3.301f, 4.492f, 3.301f)
                curveToRelative(1.078f, 0f, 2.004f, -0.276f, 2.722f, -0.764f)
                horizontalLineToRelative(-0.003f)
                arcToRelative(3.7f, 3.7f, 0f, isMoreThanHalf = false, isPositiveArc = false, 1.599f, -2.431f)
                horizontalLineTo(8f)
                verticalLineToRelative(-3.08f)
                close()
            }
        }.build()
        return _Google!!
    }

private var _Google: ImageVector? = null

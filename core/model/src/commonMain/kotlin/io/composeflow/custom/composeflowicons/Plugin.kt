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

public val ComposeFlowIcons.Plugin: ImageVector
    get() {
        if (_Plugin != null) {
            return _Plugin!!
        }
        _Plugin = ImageVector.Builder(
            name = "Plugin",
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
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(1f, 8f)
                arcToRelative(7f, 7f, 0f, isMoreThanHalf = true, isPositiveArc = true, 2.898f, 5.673f)
                curveToRelative(-0.1670f, -0.1210f, -0.2160f, -0.4060f, -0.0020f, -0.620f)
                lineToRelative(1.8f, -1.8f)
                arcToRelative(3.5f, 3.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, 4.572f, -0.328f)
                lineToRelative(1.414f, -1.415f)
                arcToRelative(0.5f, 0.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, -0.707f)
                lineToRelative(-0.707f, -0.707f)
                lineToRelative(1.559f, -1.563f)
                arcToRelative(0.5f, 0.5f, 0f, isMoreThanHalf = true, isPositiveArc = false, -0.708f, -0.706f)
                lineToRelative(-1.559f, 1.562f)
                lineToRelative(-1.414f, -1.414f)
                lineToRelative(1.56f, -1.562f)
                arcToRelative(0.5f, 0.5f, 0f, isMoreThanHalf = true, isPositiveArc = false, -0.707f, -0.706f)
                lineToRelative(-1.56f, 1.56f)
                lineToRelative(-0.707f, -0.706f)
                arcToRelative(0.5f, 0.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.707f, 0f)
                lineTo(5.318f, 5.975f)
                arcToRelative(3.5f, 3.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.328f, 4.571f)
                lineToRelative(-1.8f, 1.8f)
                curveToRelative(-0.580f, 0.580f, -0.620f, 1.60f, 0.1210f, 2.1370f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = true, isPositiveArc = false, 0f, 8f)
                arcToRelative(0.5f, 0.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, 1f, 0f)
            }
        }.build()
        return _Plugin!!
    }

private var _Plugin: ImageVector? = null

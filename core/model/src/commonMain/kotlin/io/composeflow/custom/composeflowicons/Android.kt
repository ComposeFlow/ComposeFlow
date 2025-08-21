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

public val ComposeFlowIcons.Android: ImageVector
    get() {
        if (_android != null) {
            return _android!!
        }
        _android = Builder(name = "Android", defaultWidth = 16.0.dp, defaultHeight = 16.0.dp,
                viewportWidth = 16.0f, viewportHeight = 16.0f).apply {
            path(fill = SolidColor(Color(0xFF3DDC84)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(5.768f, 6.429f)
                arcTo(5.984f, 5.984f, 0.0f, false, true, 8.0f, 6.0f)
                curveToRelative(0.787f, 0.0f, 1.54f, 0.152f, 2.228f, 0.427f)
                lineTo(11.63f, 4.0f)
                lineToRelative(0.866f, 0.5f)
                lineToRelative(-1.372f, 2.376f)
                arcTo(5.996f, 5.996f, 0.0f, false, true, 14.0f, 12.0f)
                lineTo(2.0f, 12.0f)
                arcToRelative(5.996f, 5.996f, 0.0f, false, true, 2.873f, -5.122f)
                lineTo(3.5f, 4.5f)
                lineToRelative(0.866f, -0.5f)
                lineToRelative(1.402f, 2.429f)
                close()
                moveTo(5.0f, 9.0f)
                verticalLineToRelative(1.0f)
                horizontalLineToRelative(1.0f)
                lineTo(6.0f, 9.0f)
                lineTo(5.0f, 9.0f)
                close()
                moveTo(10.0f, 9.0f)
                verticalLineToRelative(1.0f)
                horizontalLineToRelative(1.0f)
                lineTo(11.0f, 9.0f)
                horizontalLineToRelative(-1.0f)
                close()
            }
        }
        .build()
        return _android!!
    }

private var _android: ImageVector? = null

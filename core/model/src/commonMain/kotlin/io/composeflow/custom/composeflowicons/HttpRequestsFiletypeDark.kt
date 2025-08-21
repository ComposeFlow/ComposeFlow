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
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import io.composeflow.custom.ComposeFlowIcons

public val ComposeFlowIcons.HttpRequestsFiletypeDark: ImageVector
    get() {
        if (_httpRequestsFiletypeDark != null) {
            return _httpRequestsFiletypeDark!!
        }
        _httpRequestsFiletypeDark = Builder(name = "HttpRequestsFiletypeDark", defaultWidth =
                16.0.dp, defaultHeight = 16.0.dp, viewportWidth = 16.0f, viewportHeight =
                16.0f).apply {
            group {
                path(fill = SolidColor(Color(0xFF548AF7)), stroke = null, strokeLineWidth = 0.0f,
                        strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                        pathFillType = NonZero) {
                    moveTo(2.25f, 13.0f)
                    lineTo(3.8113f, 4.005f)
                    horizontalLineTo(5.3404f)
                    lineTo(6.9338f, 13.0f)
                    horizontalLineTo(5.7452f)
                    lineTo(4.6594f, 5.9325f)
                    lineTo(4.5759f, 5.335f)
                    lineTo(4.4923f, 5.9325f)
                    lineTo(3.4708f, 13.0f)
                    horizontalLineTo(2.25f)
                    close()
                    moveTo(3.1495f, 10.6549f)
                    verticalLineTo(9.6654f)
                    horizontalLineTo(5.9315f)
                    verticalLineTo(10.6549f)
                    horizontalLineTo(3.1495f)
                    close()
                }
                path(fill = SolidColor(Color(0xFF548AF7)), stroke = null, strokeLineWidth = 0.0f,
                        strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                        pathFillType = NonZero) {
                    moveTo(8.0f, 12.995f)
                    verticalLineTo(4.0f)
                    horizontalLineTo(9.7742f)
                    curveTo(10.2817f, 4.0f, 10.6989f, 4.0964f, 11.0258f, 4.2891f)
                    curveTo(11.357f, 4.4819f, 11.6022f, 4.7731f, 11.7613f, 5.1629f)
                    curveTo(11.9204f, 5.5527f, 12.0f, 6.0431f, 12.0f, 6.6343f)
                    curveTo(12.0f, 7.2296f, 11.9183f, 7.7222f, 11.7548f, 8.112f)
                    curveTo(11.5957f, 8.5018f, 11.3462f, 8.7952f, 11.0065f, 8.9922f)
                    curveTo(10.671f, 9.185f, 10.243f, 9.2814f, 9.7226f, 9.2814f)
                    horizontalLineTo(9.1677f)
                    verticalLineTo(12.995f)
                    horizontalLineTo(8.0f)
                    close()
                    moveTo(9.7613f, 8.2212f)
                    curveTo(10.1183f, 8.2212f, 10.3742f, 8.0949f, 10.529f, 7.8422f)
                    curveTo(10.6882f, 7.5851f, 10.7677f, 7.1825f, 10.7677f, 6.6343f)
                    curveTo(10.7677f, 6.0946f, 10.6882f, 5.6983f, 10.529f, 5.4456f)
                    curveTo(10.3742f, 5.1886f, 10.1183f, 5.0601f, 9.7613f, 5.0601f)
                    horizontalLineTo(9.1677f)
                    verticalLineTo(8.2212f)
                    horizontalLineTo(9.7613f)
                    close()
                }
                path(fill = SolidColor(Color(0xFF548AF7)), stroke = null, strokeLineWidth = 0.0f,
                        strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                        pathFillType = NonZero) {
                    moveTo(13.0f, 13.0f)
                    verticalLineTo(4.005f)
                    horizontalLineTo(14.0f)
                    verticalLineTo(13.0f)
                    horizontalLineTo(13.0f)
                    close()
                }
            }
        }
        .build()
        return _httpRequestsFiletypeDark!!
    }

private var _httpRequestsFiletypeDark: ImageVector? = null

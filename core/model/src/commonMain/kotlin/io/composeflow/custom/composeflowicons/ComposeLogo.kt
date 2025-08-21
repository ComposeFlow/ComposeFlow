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

public val ComposeFlowIcons.ComposeLogo: ImageVector
    get() {
        if (_composeLogo != null) {
            return _composeLogo!!
        }
        _composeLogo = Builder(name = "ComposeLogo", defaultWidth = 67.0.dp, defaultHeight =
                74.0.dp, viewportWidth = 67.0f, viewportHeight = 74.0f).apply {
            path(fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(35.999f, 2.663f)
                arcToRelative(5.01f, 5.01f, 0.0f, false, false, -4.998f, 0.0f)
                lineToRelative(-26.5f, 15.253f)
                arcToRelative(4.994f, 4.994f, 0.0f, false, false, -1.198f, 0.962f)
                lineToRelative(11.108f, 6.366f)
                curveToRelative(0.268f, -0.29f, 0.58f, -0.54f, 0.931f, -0.744f)
                lineToRelative(16.156f, -9.342f)
                arcToRelative(4.0f, 4.0f, 0.0f, false, true, 4.004f, 0.0f)
                lineTo(51.657f, 24.5f)
                curveToRelative(0.351f, 0.203f, 0.664f, 0.455f, 0.932f, 0.744f)
                lineToRelative(11.108f, -6.366f)
                arcToRelative(4.991f, 4.991f, 0.0f, false, false, -1.198f, -0.962f)
                lineToRelative(-26.5f, -15.253f)
                close()
                moveTo(64.722f, 20.596f)
                lineTo(53.539f, 27.004f)
                curveToRelative(0.076f, 0.31f, 0.116f, 0.632f, 0.116f, 0.959f)
                verticalLineToRelative(17.794f)
                arcToRelative(4.0f, 4.0f, 0.0f, false, true, -1.958f, 3.44f)
                lineToRelative(-16.235f, 9.638f)
                arcToRelative(3.998f, 3.998f, 0.0f, false, true, -0.962f, 0.412f)
                verticalLineToRelative(12.63f)
                arcToRelative(5.005f, 5.005f, 0.0f, false, false, 1.428f, -0.569f)
                lineToRelative(26.62f, -15.73f)
                arcTo(4.986f, 4.986f, 0.0f, false, false, 65.0f, 51.284f)
                lineTo(65.0f, 22.237f)
                curveToRelative(0.0f, -0.567f, -0.097f, -1.12f, -0.278f, -1.64f)
                close()
                moveTo(2.0f, 22.237f)
                curveToRelative(0.0f, -0.567f, 0.097f, -1.12f, 0.278f, -1.64f)
                lineToRelative(11.183f, 6.407f)
                curveToRelative(-0.076f, 0.31f, -0.116f, 0.632f, -0.116f, 0.959f)
                verticalLineToRelative(18.633f)
                arcToRelative(4.0f, 4.0f, 0.0f, false, false, 2.08f, 3.509f)
                lineToRelative(16.074f, 8.8f)
                curveToRelative(0.32f, 0.174f, 0.656f, 0.302f, 1.001f, 0.384f)
                verticalLineToRelative(12.638f)
                arcToRelative(5.005f, 5.005f, 0.0f, false, true, -1.517f, -0.533f)
                lineTo(4.603f, 57.02f)
                arcTo(4.987f, 4.987f, 0.0f, false, true, 2.0f, 52.642f)
                lineTo(2.0f, 22.237f)
                close()
                moveTo(30.002f, 0.935f)
                arcToRelative(7.014f, 7.014f, 0.0f, false, true, 6.996f, 0.0f)
                lineToRelative(26.5f, 15.253f)
                arcTo(6.98f, 6.98f, 0.0f, false, true, 67.0f, 22.238f)
                verticalLineToRelative(29.047f)
                arcToRelative(6.98f, 6.98f, 0.0f, false, true, -3.433f, 6.009f)
                lineToRelative(-26.62f, 15.731f)
                arcToRelative(7.014f, 7.014f, 0.0f, false, true, -6.923f, 0.12f)
                lineTo(3.644f, 58.771f)
                arcTo(6.981f, 6.981f, 0.0f, false, true, 0.0f, 52.641f)
                lineTo(0.0f, 22.238f)
                arcToRelative(6.98f, 6.98f, 0.0f, false, true, 3.502f, -6.05f)
                lineTo(30.002f, 0.936f)
                close()
                moveTo(21.398f, 28.487f)
                lineTo(31.98f, 22.377f)
                curveToRelative(0.94f, -0.542f, 2.1f, -0.542f, 3.04f, 0.0f)
                lineToRelative(10.582f, 6.11f)
                arcToRelative(2.996f, 2.996f, 0.0f, false, true, 1.503f, 2.593f)
                verticalLineToRelative(11.653f)
                curveToRelative(0.0f, 1.056f, -0.56f, 2.034f, -1.473f, 2.576f)
                lineToRelative(-10.643f, 6.308f)
                arcToRelative(3.044f, 3.044f, 0.0f, false, true, -3.009f, 0.052f)
                lineToRelative(-10.52f, -5.75f)
                arcToRelative(2.996f, 2.996f, 0.0f, false, true, -1.565f, -2.627f)
                lineTo(19.895f, 31.08f)
                curveToRelative(0.0f, -1.068f, 0.573f, -2.056f, 1.503f, -2.593f)
                close()
            }
        }
        .build()
        return _composeLogo!!
    }

private var _composeLogo: ImageVector? = null

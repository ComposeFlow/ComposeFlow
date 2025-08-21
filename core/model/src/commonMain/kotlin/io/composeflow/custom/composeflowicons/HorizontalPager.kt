package io.composeflow.custom.composeflowicons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import io.composeflow.custom.ComposeFlowIcons

val ComposeFlowIcons.HorizontalPager: ImageVector
    get() {
        if (_HorizongalPager != null) {
            return _HorizongalPager!!
        }
        _HorizongalPager = ImageVector.Builder(
            name = "HorizontalPager",
            defaultWidth = 800.dp,
            defaultHeight = 800.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(5.872f, 19.667f)
                curveTo(4.905f, 19.667f, 4.122f, 20.451f, 4.122f, 21.417f)
                curveTo(4.122f, 22.384f, 4.905f, 23.167f, 5.872f, 23.167f)
                lineTo(5.882f, 23.167f)
                curveTo(6.848f, 23.167f, 7.632f, 22.384f, 7.632f, 21.417f)
                curveTo(7.632f, 20.451f, 6.848f, 19.667f, 5.882f, 19.667f)
                lineTo(5.872f, 19.667f)
                close()
                moveTo(10.122f, 21.417f)
                curveTo(10.122f, 20.451f, 10.905f, 19.667f, 11.872f, 19.667f)
                lineTo(11.882f, 19.667f)
                curveTo(12.848f, 19.667f, 13.632f, 20.451f, 13.632f, 21.417f)
                curveTo(13.632f, 22.384f, 12.848f, 23.167f, 11.882f, 23.167f)
                lineTo(11.872f, 23.167f)
                curveTo(10.905f, 23.167f, 10.122f, 22.384f, 10.122f, 21.417f)
                close()
                moveTo(16.122f, 21.417f)
                curveTo(16.122f, 20.451f, 16.905f, 19.667f, 17.872f, 19.667f)
                lineTo(17.882f, 19.667f)
                curveTo(18.848f, 19.667f, 19.632f, 20.451f, 19.632f, 21.417f)
                curveTo(19.632f, 22.384f, 18.848f, 23.167f, 17.882f, 23.167f)
                lineTo(17.872f, 23.167f)
                curveTo(16.905f, 23.167f, 16.122f, 22.384f, 16.122f, 21.417f)
                close()
            }
            path(
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 2f
            ) {
                moveTo(7.295f, 2.556f)
                lineTo(16.476f, 2.556f)
                arcTo(1.519f, 1.519f, 0f, isMoreThanHalf = false, isPositiveArc = true, 17.995f, 4.075f)
                lineTo(17.995f, 15.105f)
                arcTo(1.519f, 1.519f, 0f, isMoreThanHalf = false, isPositiveArc = true, 16.476f, 16.624f)
                lineTo(7.295f, 16.624f)
                arcTo(1.519f, 1.519f, 0f, isMoreThanHalf = false, isPositiveArc = true, 5.776f, 15.105f)
                lineTo(5.776f, 4.075f)
                arcTo(1.519f, 1.519f, 0f, isMoreThanHalf = false, isPositiveArc = true, 7.295f, 2.556f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFFD8D8D8)),
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 1f
            ) {
                moveTo(7.526f, -0.345f)
                lineTo(9.107f, -1.387f)
                lineTo(9.084f, 0.731f)
                lineTo(7.526f, -0.345f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFFD8D8D8)),
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 1f
            ) {
                moveTo(12.954f, 2.994f)
                lineTo(11.39f, 4.061f)
                lineTo(11.378f, 1.943f)
                lineTo(12.954f, 2.994f)
                close()
            }
        }.build()

        return _HorizongalPager!!
    }

@Suppress("ObjectPropertyName")
private var _HorizongalPager: ImageVector? = null

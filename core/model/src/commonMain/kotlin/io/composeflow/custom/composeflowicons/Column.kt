package io.composeflow.custom.composeflowicons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import io.composeflow.custom.ComposeFlowIcons

val ComposeFlowIcons.Column: ImageVector
    get() {
        if (_Column != null) {
            return _Column!!
        }
        _Column = ImageVector.Builder(
            name = "Column",
            defaultWidth = 500.dp,
            defaultHeight = 500.dp,
            viewportWidth = 500f,
            viewportHeight = 500f
        ).apply {
            path(
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 32f
            ) {
                moveTo(257.34f, 42.72f)
                horizontalLineToRelative(213.71f)
                verticalLineToRelative(395.98f)
                horizontalLineToRelative(-213.71f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFFD8D8D8)),
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 32f
            ) {
                moveTo(98.03f, 45.95f)
                lineToRelative(-4.92f, 366.68f)
            }
            path(
                fill = SolidColor(Color(0xFFD8D8D8)),
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 32f
            ) {
                moveTo(19.67f, 332.3f)
                lineToRelative(72.86f, 81.11f)
            }
            path(
                fill = SolidColor(Color(0xFFD8D8D8)),
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 32f
            ) {
                moveTo(80.08f, 422.46f)
                lineToRelative(78.6f, -84.24f)
            }
        }.build()

        return _Column!!
    }

@Suppress("ObjectPropertyName")
private var _Column: ImageVector? = null

package io.composeflow.custom.composeflowicons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import io.composeflow.custom.ComposeFlowIcons

val ComposeFlowIcons.LazyRow: ImageVector
    get() {
        if (_LazyRow != null) {
            return _LazyRow!!
        }
        _LazyRow = ImageVector.Builder(
            name = "LazyRow",
            defaultWidth = 500.dp,
            defaultHeight = 500.dp,
            viewportWidth = 500f,
            viewportHeight = 500f
        ).apply {
            path(
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 32f
            ) {
                moveTo(104.52f, 234.52f)
                horizontalLineToRelative(163.9f)
                verticalLineToRelative(227.9f)
                horizontalLineToRelative(-163.9f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFFD8D8D8)),
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 32f
            ) {
                moveTo(75.8f, 92.41f)
                lineToRelative(340.01f, -0.5f)
            }
            path(
                fill = SolidColor(Color(0xFFD8D8D8)),
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 32f
            ) {
                moveTo(425.51f, 105.71f)
                lineToRelative(-75.49f, -72.89f)
            }
            path(
                fill = SolidColor(Color(0xFFD8D8D8)),
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 32f
            ) {
                moveTo(415.14f, 93.4f)
                lineToRelative(-63.02f, 68.2f)
            }
            path(
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 32f
            ) {
                moveTo(268.61f, 234.46f)
                horizontalLineToRelative(163.9f)
                verticalLineToRelative(227.9f)
                horizontalLineToRelative(-163.9f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFFD8D8D8)),
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 32f
            ) {
                moveTo(-0.01f, 234.26f)
                lineToRelative(90.32f, 0.41f)
            }
            path(
                fill = SolidColor(Color(0xFFD8D8D8)),
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 32f
            ) {
                moveTo(-1.39f, 462.02f)
                lineToRelative(90.32f, 0.41f)
            }
            path(
                fill = SolidColor(Color(0xFFD8D8D8)),
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 32f
            ) {
                moveTo(409.63f, 234.21f)
                lineToRelative(90.32f, 0.41f)
            }
            path(
                fill = SolidColor(Color(0xFFD8D8D8)),
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 32f
            ) {
                moveTo(409.61f, 462.32f)
                lineToRelative(90.32f, 0.41f)
            }
        }.build()

        return _LazyRow!!
    }

@Suppress("ObjectPropertyName")
private var _LazyRow: ImageVector? = null

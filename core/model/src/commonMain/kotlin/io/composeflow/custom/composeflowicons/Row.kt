package io.composeflow.custom.composeflowicons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import io.composeflow.custom.ComposeFlowIcons

val ComposeFlowIcons.Row: ImageVector
    get() {
        if (_Row != null) {
            return _Row!!
        }
        _Row = ImageVector.Builder(
            name = "Row",
            defaultWidth = 500.dp,
            defaultHeight = 500.dp,
            viewportWidth = 500f,
            viewportHeight = 500f
        ).apply {
            path(
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 32f
            ) {
                moveTo(33.02f, 234.52f)
                horizontalLineToRelative(434.64f)
                verticalLineToRelative(227.9f)
                horizontalLineToRelative(-434.64f)
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
        }.build()

        return _Row!!
    }

@Suppress("ObjectPropertyName")
private var _Row: ImageVector? = null

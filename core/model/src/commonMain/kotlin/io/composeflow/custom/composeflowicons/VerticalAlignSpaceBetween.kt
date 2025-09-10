package io.composeflow

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import io.composeflow.custom.ComposeFlowIcons

val ComposeFlowIcons.VerticalAlignSpaceBetween: ImageVector
    get() {
        if (_VerticalAlignSpaceBetween != null) {
            return _VerticalAlignSpaceBetween!!
        }
        _VerticalAlignSpaceBetween = ImageVector.Builder(
            name = "VerticalAlignSpaceBetween",
            defaultWidth = 48.dp,
            defaultHeight = 48.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(110f, 880f)
                quadToRelative(-12.75f, 0f, -21.38f, -8.68f)
                quadTo(80f, 862.65f, 80f, 849.83f)
                quadTo(80f, 837f, 88.63f, 828.5f)
                reflectiveQuadTo(110f, 820f)
                horizontalLineToRelative(180f)
                verticalLineToRelative(-70f)
                quadToRelative(0f, -12.75f, 8.63f, -21.38f)
                reflectiveQuadTo(320f, 720f)
                horizontalLineToRelative(320f)
                quadToRelative(12.75f, 0f, 21.38f, 8.63f)
                reflectiveQuadTo(670f, 750f)
                verticalLineToRelative(70f)
                horizontalLineToRelative(180f)
                quadToRelative(12.75f, 0f, 21.38f, 8.68f)
                quadToRelative(8.63f, 8.68f, 8.63f, 21.5f)
                quadToRelative(0f, 12.82f, -8.63f, 21.33f)
                reflectiveQuadTo(850f, 880f)
                lineTo(110f, 880f)
                close()
                moveTo(320f, 240f)
                quadToRelative(-12.75f, 0f, -21.38f, -8.63f)
                reflectiveQuadTo(290f, 210f)
                verticalLineToRelative(-70f)
                lineTo(110f, 140f)
                quadToRelative(-12.75f, 0f, -21.38f, -8.68f)
                quadTo(80f, 122.65f, 80f, 109.82f)
                quadTo(80f, 97f, 88.63f, 88.5f)
                reflectiveQuadTo(110f, 80f)
                horizontalLineToRelative(740f)
                quadToRelative(12.75f, 0f, 21.38f, 8.68f)
                quadToRelative(8.63f, 8.68f, 8.63f, 21.5f)
                quadToRelative(0f, 12.82f, -8.63f, 21.33f)
                reflectiveQuadTo(850f, 140f)
                lineTo(670f, 140f)
                verticalLineToRelative(70f)
                quadToRelative(0f, 12.75f, -8.63f, 21.38f)
                reflectiveQuadTo(640f, 240f)
                lineTo(320f, 240f)
                close()
            }
        }.build()

        return _VerticalAlignSpaceBetween!!
    }

@Suppress("ObjectPropertyName")
private var _VerticalAlignSpaceBetween: ImageVector? = null

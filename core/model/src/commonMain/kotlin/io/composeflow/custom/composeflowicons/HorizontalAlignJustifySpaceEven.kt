package io.composeflow

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import io.composeflow.custom.ComposeFlowIcons

val ComposeFlowIcons.HorizontalAlignJustifySpaceEven: ImageVector
    get() {
        if (_HorizontalAlignJustifySpaceEven != null) {
            return _HorizontalAlignJustifySpaceEven!!
        }
        _HorizontalAlignJustifySpaceEven = ImageVector.Builder(
            name = "HorizontalAlignJustifySpaceEven",
            defaultWidth = 48.dp,
            defaultHeight = 48.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(600f, 670f)
                quadToRelative(-12.75f, 0f, -21.38f, -8.63f)
                reflectiveQuadTo(570f, 640f)
                lineTo(570f, 320f)
                quadToRelative(0f, -12.75f, 8.63f, -21.38f)
                reflectiveQuadTo(600f, 290f)
                horizontalLineToRelative(40f)
                quadToRelative(12.75f, 0f, 21.38f, 8.63f)
                reflectiveQuadTo(670f, 320f)
                verticalLineToRelative(320f)
                quadToRelative(0f, 12.75f, -8.63f, 21.38f)
                reflectiveQuadTo(640f, 670f)
                horizontalLineToRelative(-40f)
                close()
                moveTo(320f, 670f)
                quadToRelative(-12.75f, 0f, -21.38f, -8.63f)
                reflectiveQuadTo(290f, 640f)
                lineTo(290f, 320f)
                quadToRelative(0f, -12.75f, 8.63f, -21.38f)
                reflectiveQuadTo(320f, 290f)
                horizontalLineToRelative(40f)
                quadToRelative(12.75f, 0f, 21.38f, 8.63f)
                reflectiveQuadTo(390f, 320f)
                verticalLineToRelative(320f)
                quadToRelative(0f, 12.75f, -8.63f, 21.38f)
                reflectiveQuadTo(360f, 670f)
                horizontalLineToRelative(-40f)
                close()
                moveTo(849.83f, 880f)
                quadTo(837f, 880f, 828.5f, 871.38f)
                reflectiveQuadTo(820f, 850f)
                lineTo(820f, 110f)
                quadToRelative(0f, -12.75f, 8.68f, -21.38f)
                quadToRelative(8.68f, -8.63f, 21.5f, -8.63f)
                quadToRelative(12.82f, 0f, 21.33f, 8.63f)
                reflectiveQuadTo(880f, 110f)
                verticalLineToRelative(740f)
                quadToRelative(0f, 12.75f, -8.68f, 21.38f)
                quadToRelative(-8.68f, 8.63f, -21.5f, 8.63f)
                close()
                moveTo(109.83f, 880f)
                quadTo(97f, 880f, 88.5f, 871.38f)
                reflectiveQuadTo(80f, 850f)
                lineTo(80f, 110f)
                quadToRelative(0f, -12.75f, 8.68f, -21.38f)
                quadToRelative(8.68f, -8.63f, 21.5f, -8.63f)
                quadToRelative(12.82f, 0f, 21.33f, 8.63f)
                reflectiveQuadTo(140f, 110f)
                verticalLineToRelative(740f)
                quadToRelative(0f, 12.75f, -8.68f, 21.38f)
                quadToRelative(-8.68f, 8.63f, -21.5f, 8.63f)
                close()
            }
        }.build()

        return _HorizontalAlignJustifySpaceEven!!
    }

@Suppress("ObjectPropertyName")
private var _HorizontalAlignJustifySpaceEven: ImageVector? = null

import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.PathFillType.Companion.EvenOdd
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.composeflow.custom.ComposeFlowIcons

public val ComposeFlowIcons.DividerVertical: ImageVector
    get() {
        if (_DividerVertical != null) {
            return _DividerVertical!!
        }
        _DividerVertical = ImageVector.Builder(
            name = "DividerVertical",
            defaultWidth = 15.dp,
            defaultHeight = 15.dp,
            viewportWidth = 15f,
            viewportHeight = 15f
        ).apply {
            materialPath(pathFillType = EvenOdd) {
                moveTo(7.0f, 4.0f)
                verticalLineToRelative(16.0f)
                horizontalLineToRelative(1.5f)
                verticalLineToRelative(-16.0f)
                close()
            }
        }.build()
        return _DividerVertical!!
    }

private var _DividerVertical: ImageVector? = null

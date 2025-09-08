package io.composeflow.ui.inspector.propertyeditor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VerticalAlignBottom
import androidx.compose.material.icons.outlined.VerticalAlignCenter
import androidx.compose.material.icons.outlined.VerticalAlignTop
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.composeflow.Res
import io.composeflow.alignment_bottom
import io.composeflow.alignment_center_vertical
import io.composeflow.alignment_top
import io.composeflow.model.parameter.wrapper.AlignmentVerticalWrapper
import io.composeflow.ui.Tooltip
import io.composeflow.ui.icon.ComposeFlowIcon
import io.composeflow.ui.icon.ComposeFlowIconToggleButton
import io.composeflow.ui.inspector.ParamInspectorHeaderRow
import io.composeflow.ui.modifier.hoverOverlay
import org.jetbrains.compose.resources.stringResource

@Composable
fun AlignmentVerticalPropertyEditor(
    initialValue: AlignmentVerticalWrapper?,
    onAlignmentSelected: (AlignmentVerticalWrapper) -> Unit,
    label: String? = null,
) {
    Column(modifier = Modifier.hoverOverlay().padding(vertical = 4.dp)) {
        label?.let {
            ParamInspectorHeaderRow(
                label = it,
                modifier = Modifier.padding(vertical = 4.dp),
            )
        }

        @Composable
        fun runIconToggleButton(
            verticalAlignment: AlignmentVerticalWrapper,
            imageVector: ImageVector,
            contentDesc: String,
        ) = run {
            val thisItemSelected = initialValue == verticalAlignment
            Tooltip(contentDesc) {
                ComposeFlowIconToggleButton(
                    checked = thisItemSelected,
                    onCheckedChange = {
                        onAlignmentSelected(verticalAlignment)
                    },
                    modifier =
                        Modifier.then(
                            if (thisItemSelected) {
                                Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                            alpha = 0.3f,
                                        ),
                                    )
                            } else {
                                Modifier
                            },
                        ),
                ) {
                    ComposeFlowIcon(
                        imageVector = imageVector,
                        contentDescription = contentDesc,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
        FlowRow {
            runIconToggleButton(
                AlignmentVerticalWrapper.Top,
                imageVector = Icons.Outlined.VerticalAlignTop,
                contentDesc = stringResource(Res.string.alignment_top),
            )
            runIconToggleButton(
                AlignmentVerticalWrapper.CenterVertically,
                imageVector = Icons.Outlined.VerticalAlignCenter,
                contentDesc = stringResource(Res.string.alignment_center_vertical),
            )
            runIconToggleButton(
                AlignmentVerticalWrapper.Bottom,
                imageVector = Icons.Outlined.VerticalAlignBottom,
                contentDesc = stringResource(Res.string.alignment_bottom),
            )
        }
    }
}

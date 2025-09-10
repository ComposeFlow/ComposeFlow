package io.composeflow.ui.inspector.propertyeditor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.AlignHorizontalLeft
import androidx.compose.material.icons.automirrored.outlined.AlignHorizontalRight
import androidx.compose.material.icons.outlined.AlignHorizontalCenter
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.composeflow.Res
import io.composeflow.align_horizontal_start
import io.composeflow.arrangement_horizontal_center
import io.composeflow.arrangement_horizontal_end
import io.composeflow.model.parameter.wrapper.AlignmentHorizontalWrapper
import io.composeflow.ui.Tooltip
import io.composeflow.ui.icon.ComposeFlowIcon
import io.composeflow.ui.icon.ComposeFlowIconToggleButton
import io.composeflow.ui.inspector.ParamInspectorHeaderRow
import io.composeflow.ui.modifier.hoverOverlay
import org.jetbrains.compose.resources.stringResource

@Composable
fun AlignmentHorizontalPropertyEditor(
    initialValue: AlignmentHorizontalWrapper?,
    onAlignmentSelected: (AlignmentHorizontalWrapper) -> Unit,
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
            horizontalAlignment: AlignmentHorizontalWrapper,
            imageVector: ImageVector,
            contentDesc: String,
        ) = run {
            val thisItemSelected = initialValue == horizontalAlignment
            Tooltip(contentDesc) {
                ComposeFlowIconToggleButton(
                    checked = thisItemSelected,
                    onCheckedChange = {
                        onAlignmentSelected(horizontalAlignment)
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
                AlignmentHorizontalWrapper.Start,
                imageVector = Icons.AutoMirrored.Outlined.AlignHorizontalLeft,
                contentDesc = stringResource(Res.string.align_horizontal_start),
            )
            runIconToggleButton(
                AlignmentHorizontalWrapper.CenterHorizontally,
                imageVector = Icons.Outlined.AlignHorizontalCenter,
                contentDesc = stringResource(Res.string.arrangement_horizontal_center),
            )
            runIconToggleButton(
                AlignmentHorizontalWrapper.End,
                imageVector = Icons.AutoMirrored.Outlined.AlignHorizontalRight,
                contentDesc = stringResource(Res.string.arrangement_horizontal_end),
            )
        }
    }
}

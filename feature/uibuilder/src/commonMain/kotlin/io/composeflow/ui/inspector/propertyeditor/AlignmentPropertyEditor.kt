package io.composeflow.ui.inspector.propertyeditor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.East
import androidx.compose.material.icons.outlined.FilterCenterFocus
import androidx.compose.material.icons.outlined.North
import androidx.compose.material.icons.outlined.NorthEast
import androidx.compose.material.icons.outlined.NorthWest
import androidx.compose.material.icons.outlined.South
import androidx.compose.material.icons.outlined.SouthEast
import androidx.compose.material.icons.outlined.SouthWest
import androidx.compose.material.icons.outlined.West
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.composeflow.Res
import io.composeflow.alignment_bottom_center
import io.composeflow.alignment_bottom_end
import io.composeflow.alignment_bottom_start
import io.composeflow.alignment_center
import io.composeflow.alignment_center_end
import io.composeflow.alignment_center_start
import io.composeflow.alignment_top_center
import io.composeflow.alignment_top_end
import io.composeflow.alignment_top_start
import io.composeflow.model.parameter.wrapper.AlignmentWrapper
import io.composeflow.ui.Tooltip
import io.composeflow.ui.icon.ComposeFlowIcon
import io.composeflow.ui.icon.ComposeFlowIconToggleButton
import io.composeflow.ui.inspector.ParamInspectorHeaderRow
import io.composeflow.ui.modifier.hoverOverlay
import org.jetbrains.compose.resources.stringResource

@Composable
fun AlignmentPropertyEditor(
    initialValue: AlignmentWrapper?,
    onAlignmentSelected: (AlignmentWrapper) -> Unit,
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
            boxAlignment: AlignmentWrapper,
            imageVector: ImageVector,
            contentDesc: String,
        ) = run {
            val thisItemSelected = initialValue == boxAlignment
            Tooltip(contentDesc) {
                ComposeFlowIconToggleButton(
                    checked = thisItemSelected,
                    onCheckedChange = {
                        onAlignmentSelected(boxAlignment)
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
                    )
                }
            }
        }
        Row {
            runIconToggleButton(
                AlignmentWrapper.TopStart,
                imageVector = Icons.Outlined.NorthWest,
                contentDesc = stringResource(Res.string.alignment_top_start),
            )
            runIconToggleButton(
                AlignmentWrapper.TopCenter,
                imageVector = Icons.Outlined.North,
                contentDesc = stringResource(Res.string.alignment_top_center),
            )
            runIconToggleButton(
                AlignmentWrapper.TopEnd,
                imageVector = Icons.Outlined.NorthEast,
                contentDesc = stringResource(Res.string.alignment_top_end),
            )
        }
        Row {
            runIconToggleButton(
                AlignmentWrapper.CenterStart,
                imageVector = Icons.Outlined.West,
                contentDesc = stringResource(Res.string.alignment_center_start),
            )
            runIconToggleButton(
                AlignmentWrapper.Center,
                imageVector = Icons.Outlined.FilterCenterFocus,
                contentDesc = stringResource(Res.string.alignment_center),
            )
            runIconToggleButton(
                AlignmentWrapper.CenterEnd,
                imageVector = Icons.Outlined.East,
                contentDesc = stringResource(Res.string.alignment_center_end),
            )
        }
        Row {
            runIconToggleButton(
                AlignmentWrapper.BottomStart,
                imageVector = Icons.Outlined.SouthWest,
                contentDesc = stringResource(Res.string.alignment_bottom_start),
            )
            runIconToggleButton(
                AlignmentWrapper.BottomCenter,
                imageVector = Icons.Outlined.South,
                contentDesc = stringResource(Res.string.alignment_bottom_center),
            )
            runIconToggleButton(
                AlignmentWrapper.BottomEnd,
                imageVector = Icons.Outlined.SouthEast,
                contentDesc = stringResource(Res.string.alignment_bottom_end),
            )
        }
    }
}

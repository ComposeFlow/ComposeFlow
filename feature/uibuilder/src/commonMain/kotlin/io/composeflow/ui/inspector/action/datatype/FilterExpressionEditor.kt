package io.composeflow.ui.inspector.action.datatype

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import io.composeflow.ui.dropdown.PlatformCursorDropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.composeflow.Res
import io.composeflow.filter_add_and_operator
import io.composeflow.filter_add_or_operator
import io.composeflow.filter_add_single_filter
import io.composeflow.model.datatype.AndFilter
import io.composeflow.model.datatype.DataType
import io.composeflow.model.datatype.FilterExpression
import io.composeflow.model.datatype.OrFilter
import io.composeflow.model.datatype.SingleFilter
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.project.firebase.FirestoreCollection
import org.jetbrains.compose.resources.stringResource

@Composable
fun FilterExpressionEditor(
    project: Project,
    composeNode: ComposeNode,
    firestoreCollection: FirestoreCollection,
    dataType: DataType,
    filterExpression: FilterExpression?,
    onFilterExpressionUpdated: (FilterExpression) -> Unit,
    onRemoveFilter: () -> Unit,
    modifier: Modifier = Modifier,
    level: Int = 0,
) {
    Column(
        modifier =
            modifier
                .padding(start = (level * 16).dp),
    ) {
        when (filterExpression) {
            is AndFilter -> {
                LogicalFilterEditor(
                    project = project,
                    composeNode = composeNode,
                    firestoreCollection = firestoreCollection,
                    dataType = dataType,
                    title = "AND",
                    filters = filterExpression.filters,
                    onFiltersChange = {
                        onFilterExpressionUpdated(
                            filterExpression.copy(filters = it),
                        )
                    },
                    onRemoveFilter = onRemoveFilter,
                )
            }

            is OrFilter -> {
                LogicalFilterEditor(
                    project = project,
                    composeNode = composeNode,
                    firestoreCollection = firestoreCollection,
                    dataType = dataType,
                    title = "OR",
                    filters = filterExpression.filters,
                    onFiltersChange = {
                        onFilterExpressionUpdated(
                            filterExpression.copy(filters = it),
                        )
                    },
                    onRemoveFilter = onRemoveFilter,
                )
            }

            is SingleFilter -> {
                SingleFilterEditor(
                    project = project,
                    composeNode = composeNode,
                    firestoreCollection = firestoreCollection,
                    dataType = dataType,
                    filter = filterExpression,
                    onFilterChange = {
                        onFilterExpressionUpdated(it)
                    },
                    onRemoveFilter = onRemoveFilter,
                )
            }

            null -> {}
        }
    }
}

@Composable
fun AddFilterMenu(
    onAndFilterAdded: () -> Unit,
    onOrFilterAdded: () -> Unit,
    onCloseMenu: () -> Unit,
    onSingleFilterAdded: (() -> Unit)? = null,
) {
    PlatformCursorDropdownMenu(
        expanded = true,
        onDismissRequest = {
            onCloseMenu()
        },
        modifier =
            Modifier
                .width(280.dp)
                .background(color = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        if (onSingleFilterAdded != null) {
            DropdownMenuItem(text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(Res.string.filter_add_single_filter),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }, onClick = {
                onSingleFilterAdded()
                onCloseMenu()
            })
        }
        DropdownMenuItem(text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(Res.string.filter_add_and_operator),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }, onClick = {
            onAndFilterAdded()
            onCloseMenu()
        })

        DropdownMenuItem(text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(Res.string.filter_add_or_operator),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }, onClick = {
            onOrFilterAdded()
            onCloseMenu()
        })
    }
}

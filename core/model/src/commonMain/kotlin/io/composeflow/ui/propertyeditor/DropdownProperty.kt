package io.composeflow.ui.propertyeditor

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.onClick
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import io.composeflow.model.project.Project
import io.composeflow.ui.Tooltip
import io.composeflow.ui.icon.ComposeFlowIcon

@OptIn(ExperimentalFoundationApi::class)
@Composable
inline fun <reified T> DropdownProperty(
    project: Project,
    items: List<T>,
    crossinline onValueChanged: (Int, T) -> Unit,
    modifier: Modifier = Modifier,
    crossinline displayText: @Composable (T) -> Unit = {
        val text: AnnotatedString? =
            when (it) {
                is CustomizedDropdownTextDisplayable -> it.asDropdownText(project)
                is DropdownTextDisplayable -> it.asDropdownText()
                is String -> AnnotatedString(it)
                is Enum<*> -> AnnotatedString(it.name)
                else -> null
            }
        text?.let { t ->
            Text(
                text = t,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.wrapContentWidth(),
            )
        }
    },
    crossinline dropDownMenuText: @Composable (T) -> Unit = {
        val text: AnnotatedString? =
            when (it) {
                is CustomizedDropdownTextDisplayable -> it.asDropdownText(project)
                is DropdownTextDisplayable -> it.asDropdownText()
                is String -> AnnotatedString(it)
                is Enum<*> -> AnnotatedString(it.name)
                else -> null
            }
        text?.let { t ->
            Text(
                text = t,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    },
    supportToolTipText: String? = null,
    selectedIndex: Int? = null,
    selectedItem: T? = null,
    editable: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .wrapContentHeight()
                .onClick {
                    if (editable) {
                        expanded = true
                    }
                },
    ) {
        Box(modifier = Modifier.wrapContentSize(Alignment.Center)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (items.isNotEmpty()) {
                    val item =
                        items.firstOrNull {
                            if (it is DropdownItemComparable && selectedItem != null) {
                                it.isSameItem(selectedItem)
                            } else {
                                it == selectedItem
                            }
                        }
                            ?: selectedIndex?.let {
                                items[selectedIndex]
                            }
                    item?.let {
                        displayText(it)
                    } ?: Text(
                        text = "",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodySmall,
                        modifier =
                            Modifier
                                .padding(end = 8.dp)
                                .alpha(if (editable) 1f else 0.5f),
                    )
                }
                ComposeFlowIcon(
                    imageVector = Icons.Outlined.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                items.forEachIndexed { index, item ->
                    DropdownMenuItem(
                        onClick = {
                            onValueChanged(index, item)
                            expanded = false
                        },
                        text = { dropDownMenuText(item) },
                    )
                }
            }
        }
        supportToolTipText?.let {
            Tooltip(it) {
                ComposeFlowIcon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = it,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier =
                        Modifier
                            .size(18.dp),
                )
            }
        }
    }
}

interface DropdownItem :
    DropdownTextDisplayable,
    DropdownItemComparable

interface DropdownTextDisplayable {
    @Composable
    fun asDropdownText(): AnnotatedString
}

interface DropdownItemComparable {
    fun isSameItem(item: Any): Boolean
}

/**
 * Same as [DropdownTextDisplayable] except that [asDropdownText] takes a [Project]
 */
interface CustomizedDropdownTextDisplayable {
    @Composable
    fun asDropdownText(project: Project): AnnotatedString
}

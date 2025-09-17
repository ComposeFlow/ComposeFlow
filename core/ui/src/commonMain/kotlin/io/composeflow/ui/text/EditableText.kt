package io.composeflow.ui.text

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.composeflow.ui.icon.ComposeFlowIcon
import io.composeflow.ui.icon.ComposeFlowIconButton
import io.composeflow.editor.validator.InputValidator
import io.composeflow.editor.validator.ValidateResult
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth

@Composable
fun EditableText(
    initialText: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    allowEmptyText: Boolean = false,
    validator: InputValidator? = null,
    textStyle: TextStyle =
        TextStyle(
            color = MaterialTheme.colorScheme.onSurface,
        ),
) {
    var text by remember(initialText) { mutableStateOf(initialText) }
    var isEditable by remember { mutableStateOf(false) }
    var tempText by remember(initialText) { mutableStateOf(text) }
    var validationResult by remember { mutableStateOf<ValidateResult>(ValidateResult.Success) }

    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }
    val focusManager = LocalFocusManager.current

    val onCancelEdit = {
        tempText = text
        isEditable = false
        focusManager.clearFocus()
    }

    val onCommitChange = {
        val currentValidationResult = validator?.validate(tempText) ?: ValidateResult.Success
        validationResult = currentValidationResult

        if (!allowEmptyText && tempText.isBlank()) {
            onCancelEdit()
        } else if (currentValidationResult is ValidateResult.Success) {
            text = tempText
            isEditable = false
            focusManager.clearFocus()
            onValueChange(text)
        }
        // If validation fails, keep the field in edit mode
    }

    Column(
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp),
        ) {
        Box(contentAlignment = Alignment.CenterStart) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicTextField(
                    value = tempText,
                    onValueChange = { newText ->
                        tempText = newText
                        if (validator != null) {
                            validationResult = validator.validate(newText)
                        }
                    },
                    readOnly = !isEditable,
                    singleLine = true,
                    textStyle = textStyle,
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions =
                        KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Done,
                        ),
                    keyboardActions =
                        KeyboardActions(
                            onDone = {
                                onCommitChange()
                            },
                        ),
                    modifier =
                        Modifier
                            .focusRequester(focusRequester)
                            .defaultMinSize(minWidth = Dp.Unspecified)
                            .drawUnderline(
                                isEditable,
                                color = if (validationResult is ValidateResult.Failure) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.primary
                                }
                            )
                            .onPreviewKeyEvent { keyEvent ->
                                // Handle Escape key to cancel edit
                                if (isEditable && keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.Escape) {
                                    tempText = text
                                    isEditable = false
                                    focusManager.clearFocus()
                                    true // Consume the event
                                } else {
                                    false
                                }
                            },
                    interactionSource = interactionSource,
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier,
                        ) {
                            innerTextField()
                        }
                    },
                )

                if (isEditable) {
                    ComposeFlowIconButton(
                        onClick = {
                            onCommitChange()
                        },
                    ) {
                        ComposeFlowIcon(
                            imageVector = Icons.Default.Done,
                            contentDescription = "Done",
                        )
                    }
                }
            }
        }

        if (!isEditable) {
            ComposeFlowIconButton(
                onClick = {
                    isEditable = true
                    tempText = text
                    focusRequester.requestFocus()
                },
            ) {
                ComposeFlowIcon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                )
            }
        }
        }

        // Show validation error message if any
        val currentValidationResult = validationResult
        if (isEditable && currentValidationResult is ValidateResult.Failure) {
            Text(
                text = currentValidationResult.message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .padding(start = 8.dp, top = 2.dp, bottom = 4.dp)
                    .fillMaxWidth(),
            )
        }
    }
}

fun Modifier.drawUnderline(
    isEditable: Boolean,
    color: Color,
): Modifier =
    this.then(
        if (isEditable) {
            Modifier.drawBehind {
                val strokeWidth = 1.dp.toPx()
                val y = size.height - strokeWidth / 2 + 1.dp.toPx()
                drawLine(
                    color = color,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = strokeWidth,
                )
            }
        } else {
            Modifier
        },
    )

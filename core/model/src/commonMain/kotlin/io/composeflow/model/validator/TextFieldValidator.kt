package io.composeflow.model.validator

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import io.composeflow.kotlinpoet.wrapper.ClassNameWrapper
import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper
import io.composeflow.Res
import io.composeflow.allow_blank_description
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.model.project.COMPOSEFLOW_PACKAGE
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.ui.Tooltip
import io.composeflow.ui.modifier.hoverOverlay
import io.composeflow.ui.propertyeditor.BooleanPropertyEditor
import io.composeflow.ui.propertyeditor.DropdownItem
import io.composeflow.ui.propertyeditor.EditableTextProperty
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.stringResource

@Serializable
sealed interface TextFieldValidator :
    ComposeStateValidator,
    DropdownItem {
    @Composable
    fun Editor(
        project: Project,
        node: ComposeNode,
        onValidatorEdited: (textFieldValidator: TextFieldValidator) -> Unit,
        modifier: Modifier,
    ) {
    }

    @Serializable
    @SerialName("StringValidator")
    data class StringValidator(
        val allowBlank: Boolean = true,
        val maxLength: Int? = null,
        val minLength: Int? = null,
    ) : TextFieldValidator {
        @Composable
        override fun asDropdownText(): AnnotatedString = AnnotatedString("String")

        override fun isSameItem(item: Any): Boolean = item is StringValidator

        @Composable
        override fun Editor(
            project: Project,
            node: ComposeNode,
            onValidatorEdited: (textFieldValidator: TextFieldValidator) -> Unit,
            modifier: Modifier,
        ) {
            Column {
                Tooltip(stringResource(Res.string.allow_blank_description)) {
                    BooleanPropertyEditor(
                        checked = allowBlank,
                        onCheckedChange = {
                            onValidatorEdited(this@StringValidator.copy(allowBlank = it))
                        },
                        label = "Allow blank",
                        modifier = Modifier.hoverOverlay(),
                    )
                }

                Row {
                    EditableTextProperty(
                        initialValue = maxLength?.toString() ?: "",
                        onValidValueChanged = {
                            onValidatorEdited(this@StringValidator.copy(maxLength = it.toInt()))
                        },
                        label = "Max length",
                        validateInput = io.composeflow.editor.validator
                            .IntValidator()::validate,
                        modifier =
                            Modifier
                                .padding(end = 8.dp)
                                .weight(1f)
                                .hoverOverlay(),
                    )
                    EditableTextProperty(
                        initialValue = minLength?.toString() ?: "",
                        onValidValueChanged = {
                            onValidatorEdited(this@StringValidator.copy(minLength = it.toInt()))
                        },
                        label = "Min length",
                        validateInput = io.composeflow.editor.validator
                            .IntValidator()::validate,
                        modifier =
                            Modifier
                                .padding(end = 8.dp)
                                .weight(1f)
                                .hoverOverlay(),
                    )
                }
            }
        }

        override fun asCodeBlock(
            project: Project,
            context: GenerationContext,
        ): CodeBlockWrapper {
            val argString =
                buildString {
                    if (!allowBlank) {
                        append("allowBlank = $allowBlank,\n")
                    }
                    maxLength?.let {
                        append("maxLength = $maxLength,\n")
                    }
                    minLength?.let {
                        append("minLength = $minLength,\n")
                    }
                }
            return CodeBlockWrapper.of(
                "%T($argString)",
                ClassNameWrapper.get("${COMPOSEFLOW_PACKAGE}.validator", "StringValidator"),
            )
        }
    }

    @Serializable
    @SerialName("IntValidator")
    data class IntValidator(
        val allowLessThanZero: Boolean = true,
        val maxValue: Int? = null,
        val minValue: Int? = null,
    ) : TextFieldValidator {
        @Composable
        override fun asDropdownText(): AnnotatedString = AnnotatedString("Int")

        override fun isSameItem(item: Any): Boolean = item is IntValidator

        @Composable
        override fun Editor(
            project: Project,
            node: ComposeNode,
            onValidatorEdited: (textFieldValidator: TextFieldValidator) -> Unit,
            modifier: Modifier,
        ) {
            Column {
                BooleanPropertyEditor(
                    checked = allowLessThanZero,
                    onCheckedChange = {
                        onValidatorEdited(this@IntValidator.copy(allowLessThanZero = it))
                    },
                    label = "Allow less than zero",
                    modifier = Modifier.hoverOverlay(),
                )

                Row {
                    EditableTextProperty(
                        initialValue = maxValue?.toString() ?: "",
                        onValidValueChanged = {
                            onValidatorEdited(this@IntValidator.copy(maxValue = it.toInt()))
                        },
                        label = "Max value",
                        validateInput = io.composeflow.editor.validator
                            .IntValidator()::validate,
                        modifier =
                            Modifier
                                .padding(end = 8.dp)
                                .weight(1f)
                                .hoverOverlay(),
                    )
                    EditableTextProperty(
                        initialValue = minValue?.toString() ?: "",
                        onValidValueChanged = {
                            onValidatorEdited(this@IntValidator.copy(minValue = it.toInt()))
                        },
                        label = "Min value",
                        validateInput = io.composeflow.editor.validator
                            .IntValidator()::validate,
                        modifier =
                            Modifier
                                .padding(end = 8.dp)
                                .weight(1f)
                                .hoverOverlay(),
                    )
                }
            }
        }

        override fun asCodeBlock(
            project: Project,
            context: GenerationContext,
        ): CodeBlockWrapper {
            val argString =
                buildString {
                    if (!allowLessThanZero) {
                        append("allowLessThanZero = $allowLessThanZero,\n")
                    }
                    maxValue?.let {
                        append("maxValue = $maxValue,\n")
                    }
                    minValue?.let {
                        append("minValue = $minValue,\n")
                    }
                }
            return CodeBlockWrapper.of(
                "%T($argString)",
                ClassNameWrapper.get("${COMPOSEFLOW_PACKAGE}.validator", "IntValidator"),
            )
        }
    }

    @Serializable
    @SerialName("FloatValidator")
    data class FloatValidator(
        val allowLessThanZero: Boolean = true,
        val maxValue: Float? = null,
        val minValue: Float? = null,
    ) : TextFieldValidator {
        @Composable
        override fun asDropdownText(): AnnotatedString = AnnotatedString("Float")

        override fun isSameItem(item: Any): Boolean = item is FloatValidator

        @Composable
        override fun Editor(
            project: Project,
            node: ComposeNode,
            onValidatorEdited: (textFieldValidator: TextFieldValidator) -> Unit,
            modifier: Modifier,
        ) {
            Column {
                BooleanPropertyEditor(
                    checked = allowLessThanZero,
                    onCheckedChange = {
                        onValidatorEdited(this@FloatValidator.copy(allowLessThanZero = it))
                    },
                    label = "Allow less than zero",
                    modifier = Modifier.hoverOverlay(),
                )

                Row {
                    EditableTextProperty(
                        initialValue = maxValue?.toString() ?: "",
                        onValidValueChanged = {
                            onValidatorEdited(this@FloatValidator.copy(maxValue = it.toFloat()))
                        },
                        label = "Max value",
                        validateInput = io.composeflow.editor.validator
                            .FloatValidator()::validate,
                        modifier =
                            Modifier
                                .padding(end = 8.dp)
                                .weight(1f)
                                .hoverOverlay(),
                    )
                    EditableTextProperty(
                        initialValue = minValue?.toString() ?: "",
                        onValidValueChanged = {
                            onValidatorEdited(this@FloatValidator.copy(minValue = it.toFloat()))
                        },
                        label = "Min value",
                        validateInput = io.composeflow.editor.validator
                            .FloatValidator()::validate,
                        modifier =
                            Modifier
                                .padding(end = 8.dp)
                                .weight(1f)
                                .hoverOverlay(),
                    )
                }
            }
        }

        override fun asCodeBlock(
            project: Project,
            context: GenerationContext,
        ): CodeBlockWrapper {
            val argString =
                buildString {
                    if (!allowLessThanZero) {
                        append("allowLessThanZero = $allowLessThanZero,\n")
                    }
                    maxValue?.let {
                        append("maxValue = ${maxValue}f,\n")
                    }
                    minValue?.let {
                        append("minValue = ${minValue}f,\n")
                    }
                }
            return CodeBlockWrapper.of(
                "%T($argString)",
                ClassNameWrapper.get("${COMPOSEFLOW_PACKAGE}.validator", "FloatValidator"),
            )
        }
    }

    @Serializable
    @SerialName("EmailValidator")
    data object EmailValidator : TextFieldValidator {
        @Composable
        override fun asDropdownText(): AnnotatedString = AnnotatedString("Email address")

        override fun isSameItem(item: Any): Boolean = item is EmailValidator

        override fun asCodeBlock(
            project: Project,
            context: GenerationContext,
        ): CodeBlockWrapper =
            CodeBlockWrapper.of(
                "%T()",
                ClassNameWrapper.get("${COMPOSEFLOW_PACKAGE}.validator", "EmailValidator"),
            )
    }

    companion object {
        fun entries(): List<TextFieldValidator> =
            listOf(
                StringValidator(),
                IntValidator(),
                FloatValidator(),
                EmailValidator,
            )
    }
}

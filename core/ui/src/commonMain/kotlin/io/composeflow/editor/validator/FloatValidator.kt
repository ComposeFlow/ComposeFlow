package io.composeflow.editor.validator

import com.github.michaelbull.result.runCatching

internal const val INVALID_FLOAT_FORMAT = "Invalid float format"
internal const val MUST_BE_SMALLER_THAN_FLOAT = "Must be smaller than %.2f"
internal const val MUST_BE_GREATER_THAN_FLOAT = "Must be greater than %.2f"

private class FloatValidatorInternal(
    private val allowEmpty: Boolean = true,
    private val allowLessThanZero: Boolean = true,
    private val maxValue: Float,
    private val minValue: Float,
) : InputValidatorInternal {
    override fun validate(input: String) =
        runCatching {
            if (!allowEmpty && input.isEmpty()) {
                ValidateResult.Failure(MUST_NOT_BE_EMPTY)
            } else {
                val value = input.toFloatOrNull()
                if (value == null) {
                    ValidateResult.Failure(INVALID_FLOAT_FORMAT)
                } else {
                    if (!allowLessThanZero && value <= 0) {
                        ValidateResult.Failure(MUST_BE_GREATER_THAN_ZERO)
                    } else if (value > maxValue) {
                        ValidateResult.Failure("Must be smaller than ${maxValue.toString().take(10)}")
                    } else if (value < minValue) {
                        ValidateResult.Failure("Must be greater than ${minValue.toString().take(10)}")
                    } else {
                        ValidateResult.Success
                    }
                }
            }
        }
}

class FloatValidator(
    allowEmpty: Boolean = true,
    allowLessThanZero: Boolean = true,
    maxValue: Float = 9999999f,
    minValue: Float = -9999999f,
    private val delegate: InputValidatorImpl =
        InputValidatorImpl(
            delegate =
                FloatValidatorInternal(
                    allowEmpty = allowEmpty,
                    allowLessThanZero = allowLessThanZero,
                    maxValue = maxValue,
                    minValue = minValue,
                ),
        ),
) : InputValidator by delegate

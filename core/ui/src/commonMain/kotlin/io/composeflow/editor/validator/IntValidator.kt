package io.composeflow.editor.validator

import com.github.michaelbull.result.runCatching

internal const val INVALID_FORMAT = "Invalid integer format"
internal const val MUST_NOT_BE_EMPTY = "Must not be empty"
internal const val MUST_BE_GREATER_THAN_ZERO = "Value must be greater than 0"
internal const val MUST_BE_SMALLER_THAN = "Must be smaller than %d"
internal const val MUST_BE_GREATER_THAN = "Must be greater than %d"

private class IntValidatorInternal(
    private val allowEmpty: Boolean = true,
    private val allowLessThanZero: Boolean = true,
    private val maxValue: Int = Int.MAX_VALUE,
    private val minValue: Int = Int.MIN_VALUE,
) : InputValidatorInternal {
    override fun validate(input: String) =
        runCatching {
            if (!allowEmpty && input.isEmpty()) {
                ValidateResult.Failure(MUST_NOT_BE_EMPTY)
            } else {
                val value = input.toIntOrNull()
                if (value == null) {
                    ValidateResult.Failure(INVALID_FORMAT)
                } else {
                    if (!allowLessThanZero && value <= 0) {
                        ValidateResult.Failure(MUST_BE_GREATER_THAN_ZERO)
                    } else if (value > maxValue) {
                        ValidateResult.Failure("Must be smaller than $maxValue")
                    } else if (value < minValue) {
                        ValidateResult.Failure("Must be greater than $minValue")
                    } else {
                        ValidateResult.Success
                    }
                }
            }
        }
}

class IntValidator(
    allowEmpty: Boolean = true,
    allowLessThanZero: Boolean = true,
    maxValue: Int = Int.MAX_VALUE,
    minValue: Int = Int.MIN_VALUE,
    private val delegate: InputValidatorImpl =
        InputValidatorImpl(
            delegate =
                IntValidatorInternal(
                    allowEmpty = allowEmpty,
                    allowLessThanZero = allowLessThanZero,
                    maxValue = maxValue,
                    minValue = minValue,
                ),
        ),
) : InputValidator by delegate

class NotEmptyIntValidator(
    private val delegate: InputValidatorImpl =
        InputValidatorImpl(
            delegate =
                IntValidatorInternal(
                    allowEmpty = false,
                ),
        ),
) : InputValidator by delegate

class NotEmptyNotLessThanZeroIntValidator(
    private val delegate: InputValidatorImpl =
        InputValidatorImpl(
            delegate =
                IntValidatorInternal(
                    allowEmpty = false,
                    allowLessThanZero = false,
                ),
        ),
) : InputValidator by delegate

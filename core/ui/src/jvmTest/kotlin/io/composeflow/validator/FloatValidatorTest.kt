package io.composeflow.validator

import io.composeflow.editor.validator.FloatValidator
import io.composeflow.editor.validator.INVALID_FLOAT_FORMAT
import io.composeflow.editor.validator.MUST_BE_GREATER_THAN_ZERO
import io.composeflow.editor.validator.MUST_NOT_BE_EMPTY
import io.composeflow.editor.validator.ValidateResult
import org.junit.Assert.assertEquals
import kotlin.test.Test

class FloatValidatorTest {
    @Test
    fun invalidFloat() {
        val validator = FloatValidator()
        assertEquals(
            ValidateResult.Failure(INVALID_FLOAT_FORMAT),
            validator.validate("aa"),
        )
    }

    @Test
    fun empty() {
        val validator = FloatValidator(allowEmpty = false)
        assertEquals(
            ValidateResult.Failure(MUST_NOT_BE_EMPTY),
            validator.validate(""),
        )
    }

    @Test
    fun zero() {
        val validator = FloatValidator(allowEmpty = false, allowLessThanZero = false)
        assertEquals(
            ValidateResult.Failure(MUST_BE_GREATER_THAN_ZERO),
            validator.validate("0"),
        )
    }

    @Test
    fun negative() {
        val validator = FloatValidator(allowEmpty = false, allowLessThanZero = false)
        assertEquals(
            ValidateResult.Failure(MUST_BE_GREATER_THAN_ZERO),
            validator.validate("-1"),
        )
    }

    @Test
    fun beyondMaxValue() {
        val maxValue = 1000f
        val validator = FloatValidator(maxValue = maxValue)
        assertEquals(
            ValidateResult.Success,
            validator.validate("1000"),
        )

        assertEquals(
            ValidateResult.Failure("Must be smaller than ${maxValue.toString().take(10)}"),
            validator.validate("1001"),
        )
    }

    @Test
    fun validFloat() {
        val validator = FloatValidator()
        assertEquals(
            ValidateResult.Success,
            validator.validate("-1"),
        )
        assertEquals(
            ValidateResult.Success,
            validator.validate("-1.0"),
        )
        assertEquals(
            ValidateResult.Success,
            validator.validate("0"),
        )
        assertEquals(
            ValidateResult.Success,
            validator.validate("0.0"),
        )
        assertEquals(
            ValidateResult.Success,
            validator.validate("1"),
        )
        assertEquals(
            ValidateResult.Success,
            validator.validate("1.0"),
        )
    }
}

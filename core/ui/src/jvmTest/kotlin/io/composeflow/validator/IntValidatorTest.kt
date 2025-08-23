package io.composeflow.validator

import io.composeflow.editor.validator.INVALID_FORMAT
import io.composeflow.editor.validator.IntValidator
import io.composeflow.editor.validator.MUST_BE_GREATER_THAN_ZERO
import io.composeflow.editor.validator.MUST_NOT_BE_EMPTY
import io.composeflow.editor.validator.ValidateResult
import org.junit.Assert.assertEquals
import kotlin.test.Test

class IntValidatorTest {
    @Test
    fun invalidInt() {
        val validator = IntValidator()
        assertEquals(
            ValidateResult.Failure(INVALID_FORMAT),
            validator.validate("aa"),
        )
    }

    @Test
    fun empty() {
        val validator = IntValidator(allowEmpty = false)
        assertEquals(
            ValidateResult.Failure(MUST_NOT_BE_EMPTY),
            validator.validate(""),
        )
    }

    @Test
    fun zero() {
        val validator = IntValidator(allowEmpty = false, allowLessThanZero = false)
        assertEquals(
            ValidateResult.Failure(MUST_BE_GREATER_THAN_ZERO),
            validator.validate("0"),
        )
    }

    @Test
    fun negative() {
        val validator = IntValidator(allowEmpty = false, allowLessThanZero = false)
        assertEquals(
            ValidateResult.Failure(MUST_BE_GREATER_THAN_ZERO),
            validator.validate("-1"),
        )
    }

    @Test
    fun beyondMaxValue() {
        val maxValue = 1000
        val validator = IntValidator(maxValue = maxValue)
        assertEquals(
            ValidateResult.Success,
            validator.validate("1000"),
        )

        assertEquals(
            ValidateResult.Failure("Must be smaller than $maxValue"),
            validator.validate("1001"),
        )
    }

    @Test
    fun validInt() {
        val validator = IntValidator()
        assertEquals(
            ValidateResult.Success,
            validator.validate("-1"),
        )
        assertEquals(
            ValidateResult.Success,
            validator.validate("0"),
        )
        assertEquals(
            ValidateResult.Success,
            validator.validate("1"),
        )
    }
}

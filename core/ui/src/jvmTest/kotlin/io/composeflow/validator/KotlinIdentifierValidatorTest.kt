package io.composeflow.validator

import io.composeflow.editor.validator.KotlinClassNameValidator
import io.composeflow.editor.validator.KotlinIdentifierValidator.CONTAINS_INVALID_CHARS
import io.composeflow.editor.validator.KotlinPackageNameValidator
import io.composeflow.editor.validator.KotlinVariableNameValidator
import io.composeflow.editor.validator.ValidateResult
import org.junit.Assert
import org.junit.Test

class KotlinIdentifierValidatorTest {
    @Test
    fun testValidateVariableName() {
        val validator = KotlinVariableNameValidator()
        Assert.assertEquals(
            ValidateResult.Success,
            validator.validate("aa"),
        )
        Assert.assertEquals(
            ValidateResult.Success,
            validator.validate("aa11"),
        )
        Assert.assertEquals(
            ValidateResult.Success,
            validator.validate("_aa11"),
        )
        Assert.assertEquals(
            ValidateResult.Success,
            validator.validate("aa11_"),
        )

        Assert.assertEquals(
            ValidateResult.Failure(CONTAINS_INVALID_CHARS),
            validator.validate("aa-"),
        )
        Assert.assertEquals(
            ValidateResult.Failure(CONTAINS_INVALID_CHARS),
            validator.validate("aa bb"),
        )
        Assert.assertEquals(
            ValidateResult.Failure(CONTAINS_INVALID_CHARS),
            validator.validate("aa-bb"),
        )
    }

    @Test
    fun testValidateClassName() {
        val validator = KotlinClassNameValidator()
        Assert.assertEquals(
            ValidateResult.Success,
            validator.validate("ClassName"),
        )
        Assert.assertEquals(
            ValidateResult.Success,
            validator.validate("className"),
        )
        Assert.assertEquals(
            ValidateResult.Success,
            validator.validate("ClassName_"),
        )
        Assert.assertEquals(
            ValidateResult.Success,
            validator.validate("ClassName123"),
        )

        Assert.assertEquals(
            ValidateResult.Failure(CONTAINS_INVALID_CHARS),
            validator.validate("123ClassName"),
        )
        Assert.assertEquals(
            ValidateResult.Failure(CONTAINS_INVALID_CHARS),
            validator.validate("Class Name"),
        )
        Assert.assertEquals(
            ValidateResult.Failure(CONTAINS_INVALID_CHARS),
            validator.validate("Class-Name"),
        )
        Assert.assertEquals(
            ValidateResult.Failure(CONTAINS_INVALID_CHARS),
            validator.validate("Class#Name"),
        )
    }

    @Test
    fun testValidatePackageName() {
        val validator = KotlinPackageNameValidator(allowEmpty = true)
        Assert.assertEquals(
            ValidateResult.Success,
            validator.validate(""),
        )
        Assert.assertEquals(
            ValidateResult.Success,
            validator.validate("mypackage"),
        )
        Assert.assertEquals(
            ValidateResult.Success,
            validator.validate("com.example"),
        )
        Assert.assertEquals(
            ValidateResult.Success,
            validator.validate("com.example.common"),
        )

        Assert.assertEquals(
            ValidateResult.Success,
            validator.validate("com.example.my_common"),
        )
        Assert.assertEquals(
            ValidateResult.Failure(CONTAINS_INVALID_CHARS),
            validator.validate("package name"),
        )
        Assert.assertEquals(
            ValidateResult.Failure(CONTAINS_INVALID_CHARS),
            validator.validate("123package"),
        )
        Assert.assertEquals(
            ValidateResult.Failure(CONTAINS_INVALID_CHARS),
            validator.validate(".package"),
        )
        Assert.assertEquals(
            ValidateResult.Failure(CONTAINS_INVALID_CHARS),
            validator.validate("my.package-common"),
        )
    }
}

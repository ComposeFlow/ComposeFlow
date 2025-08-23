package io.composeflow.model.action.datatype

import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.model.datatype.AndFilter
import io.composeflow.model.datatype.DataField
import io.composeflow.model.datatype.DataType
import io.composeflow.model.datatype.FieldType
import io.composeflow.model.datatype.FilterFieldType
import io.composeflow.model.datatype.FilterOperator
import io.composeflow.model.datatype.OrFilter
import io.composeflow.model.datatype.SingleFilter
import io.composeflow.model.project.Project
import io.composeflow.model.property.BooleanProperty
import io.composeflow.model.property.IntProperty
import io.composeflow.model.property.StringProperty
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

class FilterExpressionTest {
    private val project = Project()
    private val dataType =
        DataType(
            name = "testType",
        )
    private val stringField = DataField(name = "stringField", fieldType = FieldType.String())
    private val booleanField = DataField(name = "booleanField", fieldType = FieldType.Boolean())
    private val intField = DataField(name = "intField", fieldType = FieldType.Int())

    @Before
    fun setUp() {
        dataType.fields.addAll(listOf(stringField, booleanField, intField))
        project.dataTypeHolder.dataTypes.add(dataType)
    }

    @Test
    fun testFilterOperatorCodeBlock() {
        assertEquals("equalTo", FilterOperator.EqualTo.asCodeBlock().toString())
        assertEquals("notEqualTo", FilterOperator.NotEqualTo.asCodeBlock().toString())
        assertEquals("lessThan", FilterOperator.LessThan.asCodeBlock().toString())
        assertEquals("greaterThan", FilterOperator.GreaterThan.asCodeBlock().toString())
    }

    @Test
    fun testPrimitiveFields_string() {
        val singleFilter =
            SingleFilter(
                filterFieldType = FilterFieldType.DataField(dataType.id, stringField.id),
                operator = FilterOperator.EqualTo,
                property = StringProperty.StringIntrinsicValue("123"),
            )

        val context = GenerationContext()

        val codeBlock = singleFilter.generateCodeBlock(project, context, dryRun = false)

        val expectedCode = """("stringField" equalTo "123")"""
        assertEquals(expectedCode, codeBlock.toString())
    }

    @Test
    fun testPrimitiveFields_int() {
        val singleFilter =
            SingleFilter(
                filterFieldType = FilterFieldType.DataField(dataType.id, intField.id),
                operator = FilterOperator.EqualTo,
                property = IntProperty.IntIntrinsicValue(123),
            )

        val context = GenerationContext()

        val codeBlock = singleFilter.generateCodeBlock(project, context, dryRun = false)

        val expectedCode = """("intField" equalTo 123)"""
        assertEquals(expectedCode, codeBlock.toString())
    }

    @Test
    fun testAndFilterGenerateCodeBlock() {
        val filters =
            listOf(
                SingleFilter(
                    filterFieldType = FilterFieldType.DataField(dataType.id, stringField.id),
                    operator = FilterOperator.EqualTo,
                    property = StringProperty.StringIntrinsicValue("123"),
                ),
                SingleFilter(
                    filterFieldType = FilterFieldType.DataField(dataType.id, booleanField.id),
                    operator = FilterOperator.NotEqualTo,
                    property = BooleanProperty.BooleanIntrinsicValue(true),
                ),
            )

        val andFilter = AndFilter(filters)
        val context = GenerationContext()

        val codeBlock = andFilter.generateCodeBlock(project, context, dryRun = false)

        val expectedCode =
            """(("stringField" equalTo "123") and ("booleanField" notEqualTo true))"""
        assertEquals(expectedCode, codeBlock.toString())
    }

    @Test
    fun testOrFilterGenerateCodeBlock() {
        val filters =
            listOf(
                SingleFilter(
                    filterFieldType = FilterFieldType.DataField(dataType.id, stringField.id),
                    operator = FilterOperator.EqualTo,
                    property = StringProperty.StringIntrinsicValue("123"),
                ),
                SingleFilter(
                    filterFieldType = FilterFieldType.DataField(dataType.id, booleanField.id),
                    operator = FilterOperator.NotEqualTo,
                    property = BooleanProperty.BooleanIntrinsicValue(true),
                ),
            )

        val orFilter = OrFilter(filters)
        val context = GenerationContext()

        val codeBlock = orFilter.generateCodeBlock(project, context, dryRun = false)

        val expectedCode = """(("stringField" equalTo "123") or ("booleanField" notEqualTo true))"""
        assertEquals(expectedCode, codeBlock.toString())
    }

    @Test
    fun testEmptyAndFilterGeneratesEmptyCode() {
        val andFilter = AndFilter()
        val context = GenerationContext()

        val codeBlock = andFilter.generateCodeBlock(project, context, dryRun = false)

        assertEquals("", codeBlock.toString())
    }
}
